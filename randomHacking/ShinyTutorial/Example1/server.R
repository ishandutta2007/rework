library(shiny)
library(Biobase)

# Load in the sampled matrices we've generated ahead of time.
tumor <- readRDS("tumorExpr.Rds")
# Distill the expression data away from the ExpressionSet object.
tumorExp <- exprs(tumor)

# And now the normal sample.
normal <- readRDS("normalExpr.Rds")
normalExp <- exprs(normal)

shinyServer(function(input, output) {
  # Expression that generates a boxplot of gene expression. The 
  # expression is wrapped in a call to renderPlot to indicate that:
  #
  #  1) It is "reactive" and therefore should be automatically
  #     re-executed when inputs change
  #  2) Its output type is a plot
  #
  output$genePlot <- renderPlot({
    
    #Extract the relevant tumor and normal expression values.
    tumorGene <- tumorExp[input$gene, ]
    normalGene <- normalExp[input$gene, ]
    
    #Format as a list and plot as a boxplot
    expr <- list(Tumor=tumorGene, Normal=normalGene)
    boxplot(expr, main=input$gene)
  })
})