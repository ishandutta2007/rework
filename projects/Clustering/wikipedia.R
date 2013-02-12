library(ggplot2)

setwd('/Users/deflaux/rework/projects/Clustering')
set.seed(42)

# three nodes
totalDistance = c(5.945752097869632E7, 1.1813248119681323E7, 1.3096243857693925E7, 1.2598338476819722E7, 1.2630886824721366E7)

# one node
totalDistance = c(5.9453092920299456E7, 1.1818372928186502E7, 1.3066770367804162E7, 1.2540065184168313E7, 1.2598434861398766E7)

qplot(seq(1, length(totalDistance)), totalDistance, 
      geom=c("point", "smooth"), alpha=I(1/50),
      main="l(Z,mu) versus the number of iterations",
      xlab="iteration",
      ylab="l(Z,mu)")

ggsave("wikipediaLikelihood.jpg")