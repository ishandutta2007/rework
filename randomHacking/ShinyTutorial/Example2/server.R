library(shiny)
library(Biobase)

# Load in the sampled ExpressionSets we've generated ahead of time.
tumor <- readRDS("tumorExpr.Rds")
normal <- readRDS("normalExpr.Rds")

shinyServer(function(input, output) {
  
  # Return the requested dataset
  datasetInput <- reactive({
    t(switch(input$tissue,
           "Tumor" = exprs(tumor),
           "Normal" = exprs(normal)))
  })
  
  # Generate a summary of the dataset
  output$summary <- renderPrint({
    dataset <- datasetInput()[,1:input$numCols]
    summary(dataset)
  })
  
  # Show the first "n" observations
  output$view <- renderTable({
    head(datasetInput()[,1:input$numCols], n = input$numRows)
  })
})