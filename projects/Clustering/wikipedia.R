library(ggplot2)

setwd('/Users/deflaux/rework/projects/Clustering')
set.seed(42)

# three nodes
totalDistance = c( 143559.10939817992,
                   143369.591697176,
                   143272.5103684651,
                   143185.9465843796,
                   143114.7752995141)

qplot(seq(1, length(totalDistance)), totalDistance, 
      geom=c("point", "smooth"), alpha=I(1/50),
      main="l(Z,mu) versus the number of iterations",
      xlab="iteration",
      ylab="l(Z,mu)")

ggsave("wikipediaLikelihood.jpg")