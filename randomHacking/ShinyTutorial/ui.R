geneList <- c( "KRAS", "EGFR", "KLF6", "FOXO1", "JAK2", "BRCA1", "BRCA2", "PPM1D")

shinyUI(pageWithSidebar(
  headerPanel("Hello, Shiny! (title)"),
  sidebarPanel(
    selectInput("gene", "Gene:", geneList)
    ),
  mainPanel(
    plotOutput("ndBoxPlot")
    )
))