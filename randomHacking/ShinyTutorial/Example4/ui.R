library(shiny)

shinyUI(pageWithSidebar(
  # Application title
  headerPanel("Hello Shiny Bioconductor Survival!"),
  
  # Sidebar with a selector to choose a gene
  sidebarPanel(
    sliderInput("cutoff", "Cutoff", min=.1, max=1, step=.05, value=.8),
    actionButton("submit", "Use Selected Cutoff")
  ),
  
  # Show a plot of the generated distribution
  mainPanel(
    verbatimTextOutput("dim"),
    tableOutput("tbl")
  )
))