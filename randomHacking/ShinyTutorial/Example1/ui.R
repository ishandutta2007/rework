library(shiny)

geneList <- c( "KRAS", "EGFR", "KLF6", "FOXO1", "JAK2", "BRCA1", "BRCA2", "PPM1D")

shinyUI(pageWithSidebar(
  # Application title
  headerPanel("Hello Shiny Bioconductor!"),
  
  # Sidebar with a selector to choose a gene
  sidebarPanel(
    selectInput("gene", "Gene", geneList)
  ),
  
  # Show a plot of the generated distribution
  mainPanel(
    plotOutput("genePlot")
  )
))