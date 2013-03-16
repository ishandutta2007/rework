# 11 Existence f-score: 0.7511368294612811, ErrorMetrics [truePositive=39396.0, falsePositive=17504.0, trueNegative=0.0, falseNegative=8601.0, getPrecision()=0.6923725834797891, getRecall()=0.8208013000812551, getLoss()=26105.0, getFScore()=0.7511368294612811], org.deflaux.facebook2.ExistenceModel [step=0.05, lambda=0.1, numDimensions=65536, trainingCount=477663, metrics for epoch 11=ErrorMetrics [truePositive=48249.0, falsePositive=0.0, trueNegative=0.0, falseNegative=744.0, getPrecision()=1.0, getRecall()=0.9848141571244872, getLoss()=744.0, getFScore()=0.9923489850064786]]
# 11 Cost f-score: 0.9947273780134711, ErrorMetrics [truePositive=32638.0, falsePositive=55.0, trueNegative=6412.0, falseNegative=291.0, getPrecision()=0.9983176826843667, getRecall()=0.9911628048224969, getLoss()=346.0, getFScore()=0.9947273780134711], org.deflaux.facebook2.CostModel [step=0.05, lambda=0.0010, numDimensions=65536, trainingCount=477663, metrics for epoch 11=ErrorMetrics [truePositive=7540.0, falsePositive=452.0, trueNegative=40216.0, falseNegative=785.0, getPrecision()=0.9434434434434434, getRecall()=0.9057057057057057, getLoss()=1237.0, getFScore()=0.9241894956180671]]

library(ggplot2)
setwd('/Users/deflaux/rework/competitions/facebook2')

x <-    rbind(c(epoch=11, model='Cost', fscore=0.9947273780134711),
    c(epoch=11, model='Existence', fscore=0.7511368294612811),
      c(epoch=12, model='Cost', fscore=0.9943872922144823),
      c(epoch=12, model='Existence', fscore=0.7563505914291255),
      c(epoch=13, model='Cost', fscore= 0.9943001102188631),
      c(epoch=13, model='Existence', fscore=0.7560101390154066),
      c(epoch=14, model='Cost', fscore= 0.9935555993950155),
      c(epoch=14, model='Existence', fscore=0.7384952369144298),
      c(epoch=15, model='Cost', fscore= 0.9920433579335792),
      c(epoch=15, model='Existence', fscore=0.6923697478991596))
data <- data.frame(cbind(epoch=as.numeric(x[,'epoch']), model=x[,'model'], FScore=as.numeric(x[,'fscore'])))

Epoch=as.numeric(x[,'epoch'])
FScore=as.numeric(x[,'fscore'])
Model=x[,'model']
theme_set(theme_gray(base_size = 18))
qplot(Epoch, FScore, shape=Model, color=Model,   
      geom=c("point", "smooth"), alpha=I(1/50),
      ylim=c(0.0, 1.0), size= I(1),
      main='Predicting Future Graphs')
ggsave("predictingFutureGraphs.png")

losses = rbind(c(model='Existence', cases=46638.0, losses=3251.0, epoch=1, averageLoss=0.0697071057935589),
c(model='Cost', cases=46638.0, losses=3779.0, epoch=1, averageLoss=0.08102834598396158),
c(model='Existence', cases=46604.0, losses=1128.0, epoch=2, averageLoss=0.024203930993047806),
c(model='Cost', cases=46604.0, losses=1759.0, epoch=2, averageLoss=0.03774354132692473),
c(model='Existence', cases=47211.0, losses=907.0, epoch=3, averageLoss=0.01921162440956557),
c(model='Cost', cases=47211.0, losses=1380.0, epoch=3, averageLoss=0.029230475948401856),
c(model='Existence', cases=47461.0, losses=813.0, epoch=4, averageLoss=0.017129853985377468),
c(model='Cost', cases=47461.0, losses=1123.0, epoch=4, averageLoss=0.02366153262678831),
c(model='Existence', cases=48033.0, losses=806.0, epoch=5, averageLoss=0.01678013032706681),
c(model='Cost', cases=48033.0, losses=1310.0, epoch=5, averageLoss=0.02727291653654779),
c(model='Existence', cases=47875.0, losses=762.0, epoch=6, averageLoss=0.01591644908616188),
c(model='Cost', cases=47875.0, losses=1195.0, epoch=6, averageLoss=0.02496083550913838),
c(model='Existence', cases=48586.0, losses=702.0, epoch=7, averageLoss=0.014448606594492241),
c(model='Cost', cases=48586.0, losses=1224.0, epoch=7, averageLoss=0.025192442267319804),
c(model='Existence', cases=47749.0, losses=788.0, epoch=8, averageLoss=0.016502963412846343),
c(model='Cost', cases=47749.0, losses=1269.0, epoch=8, averageLoss=0.026576472805713208),
c(model='Existence', cases=48513.0, losses=710.0, epoch=9, averageLoss=0.014635252406571434),
c(model='Cost', cases=48513.0, losses=1156.0, epoch=9, averageLoss=0.023828664481685323),
c(model='Existence', cases=48993.0, losses=753.0, epoch=10, averageLoss=0.015369542587716613),
c(model='Cost', cases=48993.0, losses=1251.0, epoch=10, averageLoss=0.025534259996326004),
c(model='Existence', cases=48498.0, losses=706.0, epoch=11, averageLoss=0.01455730133201369),
c(model='Cost', cases=48498.0, losses=1198.0, epoch=11, averageLoss=0.024702049569054394),
c(model='Existence', cases=48983.0, losses=778.0, epoch=12, averageLoss=0.015883061470306024),
c(model='Cost', cases=48983.0, losses=1171.0, epoch=12, averageLoss=0.023906253189882203),
c(model='Existence', cases=49521.0, losses=812.0, epoch=13, averageLoss=0.016397084065346016),
c(model='Cost', cases=49521.0, losses=1268.0, epoch=13, averageLoss=0.025605298762141314),
c(model='Existence', cases=48679.0, losses=723.0, epoch=14, averageLoss=0.01485240041907188),
c(model='Cost', cases=48679.0, losses=1173.0, epoch=14, averageLoss=0.024096633045050227),
c(model='Existence', cases=49244.0, losses=791.0, epoch=15, averageLoss=0.016062870603525303),
c(model='Cost', cases=49244.0, losses=1334.0, epoch=15, averageLoss=0.027089594671432054))

Epoch=as.numeric(losses[,'epoch'])
AverageLoss=as.numeric(losses[,'averageLoss'])
Model=losses[,'model']
qplot(Epoch, AverageLoss, shape=Model, color=Model,   
      geom=c("point", "smooth"), alpha=I(1/50),
      ylim=c(0.0, 1.0),
      main='Training Average Loss')
ggsave("Training Average Loss.png")
