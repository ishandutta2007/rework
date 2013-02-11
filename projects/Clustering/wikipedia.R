library(ggplot2)

setwd('/Users/deflaux/rework/projects/Clustering')
set.seed(42)

totalDistance = c(5.945752097869632E7, 1.1813248119681323E7, 1.3096243857693925E7, 1.2598338476819722E7, 1.2630886824721366E7)

qplot(seq(1, length(totalDistance)), totalDistance, 
      geom=c("point", "smooth"), alpha=I(1/50),
      main="l(Z,mu) versus the number of iterations",
      xlab="iteration",
      ylab="l(Z,mu)")

ggsave("wikipediaLikelihood.jpg")