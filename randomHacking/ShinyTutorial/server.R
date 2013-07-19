# w00t!
tumorData = readRDS("Example1/tumorExpr.Rds")
normalData = readRDS("Example1/normalExpr.Rds")
shinyServer(function(input,output){
  output$ndBoxPlot <- renderPlot({
    gene = input$gene
    boxplot(list(Tumor=exprs(tumorData)[gene,], Normal=exprs(normalData)[gene,]),
            main=paste("Gene", gene))
  })
})
