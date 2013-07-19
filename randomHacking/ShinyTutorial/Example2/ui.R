library(shiny)

shinyUI(pageWithSidebar(
  # Application title
  headerPanel("Hello Shiny Bioconductor Text!"),
  
  # Sidebar with a selector to choose a gene
  sidebarPanel(
    selectInput("tissue", "Tissue Type:", c("Tumor", "Normal")),
    numericInput("numCols", "Max Genes to Display:", value=5),
    numericInput("numRows", "Max Samples to Display:", value=5)
  ),
  
  # Show a plot of the generated distribution
  mainPanel(
    verbatimTextOutput("summary"),
    
    tableOutput("view")
  )
))