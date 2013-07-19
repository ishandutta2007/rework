library(shiny)
library(Biobase)

shinyServer(function(input, output, session) {
  
  filter <- reactive({
    if (!exists("gn"))
      stop(paste("'gn' var doesn't exist. This Shiny App is intended to be run",
                 "as a part of a larger workflow in which some objects would ",
                 "already be defined in this environment prior to `runApp` being", 
                 "executed. Try evaluating the code in `ReconstructGRN.Rmd` which",
                 "wraps this Shiny app in a larger workflow."))
    abs(gn) > input$cutoff
  })
  
  output$tbl <- renderTable({
    filtered <- filter()
    suppressWarnings({head(mat2adj(filtered), n=10)})
  })
  
  output$dim <- renderText({
    filtered <- filter()
    filtered <- filtered[upper.tri(filtered)]
    paste("Edges: ", sum(filtered), sep="")
  })
  
  observe({
    if (input$submit == 0)
      return()
    
    stopApp(input$cutoff)
  })
  
})
