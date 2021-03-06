library(multicore)
library(doMC)
library(foreach)
library(testthat)
library(ggplot2)
library(mvtnorm)

setwd('/Users/deflaux/rework/projects/Clustering')
source('centroidSamplingFunctions.R')
source('kmeans.R')
source('mixtureOfGaussians.R')

plotClusters <- function(data, centroids) {
    p <- ggplot(data=data, aes(x=x1, y=x2, color=cluster)) + geom_point()
    p <- p + geom_point(data=centroids, aes(x=x1, y=x2, color=cluster), size=5)
    p <- p + geom_point(data=centroids, aes(x=x1, y=x2, color=cluster), shape=19, size=52, alpha=.5, show_guide=FALSE)
    p
}

plotAllCentroids <- function(data, allCentroids) {
    p <- ggplot(data=data, aes(x=x1, y=x2)) + geom_point(color='grey75')
    p <- p + geom_point(data=allCentroids, aes(x=x1, y=x2), color='black', size=5)
    p
}

set.seed(42)
registerDoMC()
getDoParWorkers()
mcoptions <- list(preschedule=FALSE, set.seed=TRUE)

origData <- read.csv('./data/2DGaussianMixture.csv', header=TRUE)
str(origData)
expect_that(ncol(origData), equals(3))
DELTA = 0.000001

#---------------------------------------------------------
# 2.2.1 (b)
results = foreach(k=c(2, 3, 5, 10, 15, 20), .options.multicore=mcoptions) %dopar% llyodsKmeans(k=k, 
                                                                                               data=origData[,c('x1','x2')], 
                                                                                               delta=DELTA)
lapply(results, function(result) { 
    print(paste('k:', result$k, 'number of iterations:', result$numIterations))
    centroids <- as.data.frame(result$centroids)
    centroids$cluster <- as.factor(seq(1,result$k))
    expect_that(ncol(centroids), equals(3))
    data <- as.data.frame(cbind(origData, cluster=as.factor(result$clusters)))
    expect_that(ncol(data), equals(4))
    plotFilename=paste(result$k,'kmeans.jpg',sep='_')
    plotClusters(data, centroids)
    ggsave(file=plotFilename)
    })

#---------------------------------------------------------
# 2.2.1 (c)
resultsC = foreach(i=seq(1,20), .options.multicore=mcoptions) %dopar% llyodsKmeans(k=3, 
                                                                                  data=origData[,c('x1','x2')], 
                                                                                  delta=DELTA)
allCosts = lapply(resultsC, function(result) {result$finalCosts})
min(unlist(allCosts))
mean(unlist(allCosts))
sd(unlist(allCosts))
sum(unlist(lapply(resultsC, function(result){result$numIterations})))
allCentroids = lapply(resultsC, function(result) {as.data.frame(result$centroids)})
allCentroids = as.data.frame(do.call(rbind,allCentroids))
plotAllCentroids(origData, allCentroids)
ggsave(file='3by20kmeans.jpg')

#---------------------------------------------------------
# 2.2.1 (d)
resultsD = foreach(i=seq(1,20), .options.multicore=mcoptions) %dopar% llyodsKmeans(k=3, 
                                                                                  data=origData[,c('x1','x2')], 
                                                                                  delta=DELTA, 
                                                                                  samplingFunction=kmeansPlusPlusSample)
allCosts = lapply(resultsD, function(result) {result$finalCosts})
min(unlist(allCosts))
mean(unlist(allCosts))
sd(unlist(allCosts))
sum(unlist(lapply(resultsD, function(result){result$numIterations})))
allCentroids = lapply(resultsD, function(result) {as.data.frame(result$centroids)})
allCentroids = as.data.frame(do.call(rbind,allCentroids))
plotAllCentroids(origData, allCentroids)
ggsave(file='3by20kmeansPlusPlus.jpg')

#---------------------------------------------------------
# 2.2.1 (g)
result <- mixtureOfGaussians(k=3,
                             data=origData[,c('x1','x2')],
                             delta=DELTA,
                             samplingFunction=kmeansPlusPlusSample)
print(paste('k:', result$k, 'number of iterations:', result$numIterations))
    
likelihoods = as.data.frame(cbind(likelihood=unlist(result$likelihoods), iteration=seq(1,result$numIterations)))
p <- qplot(iteration, likelihood, data=likelihoods)
p + geom_smooth()
ggsave(file='mogLikelihood.jpg')
    
centroids <- as.data.frame(result$centroids)
centroids$cluster <- as.factor(seq(1,result$k))
expect_that(ncol(centroids), equals(3))
data <- as.data.frame(cbind(origData, cluster=as.factor(result$clusters)))
expect_that(ncol(data), equals(4))
plotFilename=paste(result$k,'mog.jpg',sep='_')
plotClusters(data, centroids)
ggsave(file=plotFilename)

