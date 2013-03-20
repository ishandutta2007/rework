library(ggplot2)
library(testthat)
setwd('/Users/deflaux/rework/competitions/facebook2')

x <- rbind(
    c(epoch=11,model='Existence',fscore=0.9078122641984003),
    c(epoch=11,model='Cost',fscore=0.9958175609497844),
    c(epoch=12,model='Existence',fscore=0.9029328974847908),
    c(epoch=12,model='Cost',fscore=0.995648924243233),
    c(epoch=13,model='Existence',fscore=0.8937851390366206),
    c(epoch=13,model='Cost',fscore=0.994868323065466),
    c(epoch=14,model='Existence',fscore=0.8716191510922141),
    c(epoch=14,model='Cost',fscore=0.9939125312087973),
    c(epoch=15,model='Existence',fscore=0.8555241995374397),
    c(epoch=15,model='Cost',fscore=0.9924948164588383)
    )
data <- data.frame(cbind(epoch=as.numeric(x[,'epoch']), model=x[,'model'], FScore=as.numeric(x[,'fscore'])))

Epoch=as.numeric(x[,'epoch'])
FScore=as.numeric(x[,'fscore'])
Model=x[,'model']
theme_set(theme_gray(base_size = 18))
qplot(Epoch, FScore, linetype=Model, color=Model,   
      geom=c("point", "smooth"), alpha=I(1/50),
      ylim=c(0.8, 1.0), size= I(1),
      main='Predicting Future Graphs')
ggsave("Paper/predictingFutureGraphs.png")

losses = rbind(
    c(model='Existence', cases=46638.0, losses=2758.0, epoch=1, averageLoss=0.0591363266006261),
    c(model='Cost', cases=46638.0, losses=5875.0, epoch=1, averageLoss=0.12597023886101463),
    c(model='Existence', cases=46604.0, losses=241.0, epoch=2, averageLoss=0.005171229937344434),
    c(model='Cost', cases=46604.0, losses=965.0, epoch=2, averageLoss=0.02070637713500987),
    c(model='Existence', cases=47211.0, losses=159.0, epoch=3, averageLoss=0.003367859185359344),
    c(model='Cost', cases=47211.0, losses=1028.0, epoch=3, averageLoss=0.02177458643112834),
    c(model='Existence', cases=47461.0, losses=140.0, epoch=4, averageLoss=0.002949790354185542),
    c(model='Cost', cases=47461.0, losses=509.0, epoch=4, averageLoss=0.010724594930574576),
    c(model='Existence', cases=48033.0, losses=121.0, epoch=5, averageLoss=0.002519101451085712),
    c(model='Cost', cases=48033.0, losses=1231.0, epoch=5, averageLoss=0.02562821393625216),
    c(model='Existence', cases=47875.0, losses=134.0, epoch=6, averageLoss=0.0027989556135770235),
    c(model='Cost', cases=47875.0, losses=833.0, epoch=6, averageLoss=0.01739947780678851),
    c(model='Existence', cases=48586.0, losses=115.0, epoch=7, averageLoss=0.00236693697773021),
    c(model='Cost', cases=48586.0, losses=756.0, epoch=7, averageLoss=0.015560037870991644),
    c(model='Existence', cases=47749.0, losses=115.0, epoch=8, averageLoss=0.0024084274016209764),
    c(model='Cost', cases=47749.0, losses=956.0, epoch=8, averageLoss=0.02002136170391003),
    c(model='Existence', cases=48513.0, losses=108.0, epoch=9, averageLoss=0.002226207408323542),
    c(model='Cost', cases=48513.0, losses=679.0, epoch=9, averageLoss=0.013996248428256343),
    c(model='Existence', cases=48993.0, losses=116.0, epoch=10, averageLoss=0.002367685179515441),
    c(model='Cost', cases=48993.0, losses=770.0, epoch=10, averageLoss=0.015716530932990427),
    c(model='Existence', cases=48498.0, losses=127.0, epoch=11, averageLoss=0.002618664687203596),
    c(model='Cost', cases=48498.0, losses=691.0, epoch=11, averageLoss=0.014248010227225865),
    c(model='Existence', cases=48983.0, losses=113.0, epoch=12, averageLoss=0.002306922809954474),
    c(model='Cost', cases=48983.0, losses=688.0, epoch=12, averageLoss=0.014045689320784762),
    c(model='Existence', cases=49521.0, losses=130.0, epoch=13, averageLoss=0.002625148926717958),
    c(model='Cost', cases=49521.0, losses=827.0, epoch=13, averageLoss=0.0166999858645827),
    c(model='Existence', cases=48679.0, losses=129.0, epoch=14, averageLoss=0.00265001335278046),
    c(model='Cost', cases=48679.0, losses=767.0, epoch=14, averageLoss=0.015756280942500873),
    c(model='Existence', cases=49244.0, losses=107.0, epoch=15, averageLoss=0.0021728535456096176),
    c(model='Cost', cases=49244.0, losses=1207.0, epoch=15, averageLoss=0.024510600276175777)
    )
    
Epoch=as.numeric(losses[,'epoch'])
AverageLoss=as.numeric(losses[,'averageLoss'])
Losses=as.numeric(losses[,'losses'])
Model=losses[,'model']
qplot(Epoch, Losses, linetype=Model, color=Model,   
      geom=c("point", "smooth"), alpha=I(1/50),
      size= I(1),
      main='Training Losses')
ggsave("Paper/trainingLosses.png")

qplot(Epoch, AverageLoss, linetype=Model, color=Model,          
      geom=c("point", "smooth"), alpha=I(1/50),
      ylim=c(0.0, 0.25), size= I(1),
      main='Training Average Loss')
ggsave("Paper/trainingAverageLoss.png")

edges = c(freeEdges=119276, paidEdges=603312)
edges/sum(edges)


# See how much our optimality predictions differ when computed upon our predicted 
# graph for epoch 11 versus the actual graph for epoch 11
setwd('/Users/deflaux/rework/competitions/facebook2/data')
foo=read.table('optimalPathPredsPredicted11.txt', header=F)
bar=read.table('optimalPathPredsActual11.txt', header=F)
sum(foo$V1 != bar$V1)
length(foo$V1)
sum(foo$V1 != bar$V1)/length(foo$V1)


# Sanity check, compute optimality predictions upon the normalized _training_ data to see
# if the normalization process was okay
computeHistoricalMean <- function() {
    foo = lapply(seq(1,15), function(i) {
        bar=read.table(paste('optimalPathPredsActual', i, '.txt', sep=''), header=F)
        bar$V1
    })
    data = as.data.frame(do.call(cbind,foo))
    expect_that(dim(data), equals(c(10000,15)))
    meanPred = apply(data, 1,mean)
    expect_that(length(meanPred), equals(10000))
    write(rep(meanPred,5), file="historicalMean.txt", ncolumns=1) 
    modePred = rowSums(data) > 7
    expect_that(length(modePred), equals(10000))
    write(rep(as.numeric(modePred),5), file="historicalMode.txt", ncolumns=1) 
}
setwd('/Users/deflaux/rework/competitions/facebook2/data')
computeHistoricalMean()
setwd('/Users/deflaux/rework/competitions/facebook2/data/FromPosterSession')
computeHistoricalMean()
