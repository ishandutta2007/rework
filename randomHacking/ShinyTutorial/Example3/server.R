library(shiny)
library(Biobase)
library(survival)

# Load in the sampled matrices we've generated ahead of time.
tumor <- readRDS("tumorExpr.Rds")
# Distill the expression data away from the ExpressionSet object.
tumorExp <- exprs(tumor)

shinyServer(function(input, output, session) {
  
  # Need a wrapper around the densityClick input so we can manage whether or
  # not the click occured on the current Gene. If it occured on a previous
  # gene, we'll want to mark that click as 'stale' so we don't try to use it
  # later.
  currentClick <- list(click=NULL, stale=FALSE)
  handleClick <- observe({
    if (!is.null(input$densityClick) && !is.null(input$densityClick$x)){
      currentClick$click <<- input$densityClick
      currentClick$stale <<- FALSE
    }
  }, priority=100)
  
  getCutoff <- reactive({
    # We need this function to subscribe to a couple of dependencies. Without
    # explicitly providing these two at the top, this function may return a 
    # cached value without realizing that a new click has occured, or a new 
    # gene has been loaded.
    input$densityClick
    geneExp()
    
    # See if there's been a click since the last gene change.
    if (!is.null(currentClick$click) && !currentClick$stale){
      return(currentClick$click$x)
    }
    
    return (mean(geneExp()))
  })
  
  #' Extract the relevant tumor expression values.
  geneExp <- reactive({
    geneExp <- tumorExp[rownames(tumorExp) == input$gene, ]
    
    currentClick$stale <<- TRUE
    
    geneExp
  })
  
  #' Render a plot to show the distribution of the gene's expression
  output$densityPlot <- renderPlot({
    # Plot to density plot
    tumorGene <- geneExp()
    plot(density(tumorGene), main="Distribution", xlab="")
    
    # Add a vertical line to show where the current cutoff is.
    abline(v=getCutoff(), col=4)
    
    # Draw a line where they're hovering
    if (!is.null(input$densityHover) && !is.null(input$densityHover$y)){
      abline(v=input$densityHover$x, col=5)
    }
  }, bg="transparent")
  
  #' A reactive survival formula
  survivalFml <- reactive({
    tumorGene <- geneExp()
    
    # Create the groups based on which samples are above/below the cutoff
    expressionGrp <- as.integer(tumorGene < getCutoff())
    
    # Make sure there's more than one group!
    if (length(unique(expressionGrp)) < 2){
      stop("You must specify a cutoff that places at least one sample in each group!")
    }
    
    # Create the survival object
    surv <- with(pData(tumor), 
                 Surv(days_to_death, recurrence_status=="recurrence"))
    return(surv ~ expressionGrp)
  })
  
  #' Print out some information about the fit
  output$info <- renderPrint({
    surv <- survivalFml()
    sDiff <- survdiff(surv)
    
    # Calculate the p value
    # Extracted from the print.survdiff function in the survival package
    df <- (sum(1 * (sDiff$exp > 0))) - 1
    pv <- format(signif(1 - pchisq(sDiff$chisq, df), 2))
    
    cat ("p-value: ", pv, "\n")
  })
  
  #' Create a Kaplan Meier plot
  output$kmPlot <- renderPlot({
    surv <- survivalFml()
    plot(survfit(surv), col=1:2, xlab="Survival Time (Days)",
         ylab="% Survival")
    legend(10,.4,c("Low Expr", "High Expr"), lty=1, col=1:2)
  })
})
