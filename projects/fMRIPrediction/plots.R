library(ggplot2)

setwd('/Users/deflaux/rework/projects/fMRIPrediction')
set.seed(42)

lambda = c(0.02, 0.04, 0.06, 0.08, 0.1, 0.12, 0.14, 0.16, 0.18, 0.2)

trainingError = c(0.0035960497, 0.009521798, 0.016894074, 0.02290719, 0.029293407, 0.03006931, 0.03584384, 0.041060448, 0.055163935, 0.05598209)

testError = c(0.026354674, 0.023009276, 0.024373936, 0.042012658, 0.03935851, 0.040371627, 0.04775139, 0.054238297, 0.08731667, 0.07222149)

l0Norm = c(14.0, 10.0, 4.0, 3.0, 1.0, 1.0, 1.0, 1.0, 2.0, 1.0)


qplot(lambda, trainingError,    
      geom=c("point", "smooth"), alpha=I(1/50))
ggsave("trainingErrorVersusLambda.jpg")

qplot(lambda, testError,    
      geom=c("point", "smooth"), alpha=I(1/50))
ggsave("testErrorVersusLambda.jpg")

qplot(lambda, l0Norm,    
      geom=c("point", "smooth"), alpha=I(1/50))
ggsave("l0NormVersusLambda.jpg")
