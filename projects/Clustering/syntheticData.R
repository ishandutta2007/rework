library(multicore)
library(doMC)
library(foreach)
library(testthat)
library(ggplot2)
library(mvtnorm)

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

setwd('/Users/deflaux/rework/projects/Clustering')
set.seed(42)
registerDoMC()
getDoParWorkers()
mcoptions <- list(preschedule=FALSE, set.seed=TRUE)

origData <- read.csv('./data/2DGaussianMixture.csv', header=TRUE)
str(origData)
expect_that(ncol(origData), equals(3))
DELTA = 0.000001

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
    plotFilename=paste(result$k,'kmeans.pdf',sep='_')
    plotClusters(data, centroids)
    ggsave(file=plotFilename)
    })

# 2.2.2 (c)
results = foreach(i=seq(1,20), .options.multicore=mcoptions) %dopar% llyodsKmeans(k=3, 
                                                                                  data=origData[,c('x1','x2')], 
                                                                                  labels=as.factor(origData[,'class']), 
                                                                                  delta=DELTA)
allCosts = lapply(results, function(result) {result$finalCosts})
min(unlist(allCosts))
mean(unlist(allCosts))
sd(unlist(allCosts))
allCentroids = lapply(results, function(result) {as.data.frame(result$centroids)})
allCentroids = as.data.frame(do.call(rbind,allCentroids))
plotAllCentroids(origData, allCentroids)
ggsave(file='3by20kmeans.pdf')

# 2.2.2 (d)
results = foreach(i=seq(1,20), .options.multicore=mcoptions) %dopar% llyodsKmeans(k=3, 
                                                                                  data=origData[,c('x1','x2')], 
                                                                                  delta=DELTA, 
                                                                                  samplingFunction=kmeansPlusPlusSample)
allCosts = lapply(results, function(result) {result$finalCosts})
min(unlist(allCosts))
mean(unlist(allCosts))
sd(unlist(allCosts))
allCentroids = lapply(results, function(result) {as.data.frame(result$centroids)})
allCentroids = as.data.frame(do.call(rbind,allCentroids))
plotAllCentroids(origData, allCentroids)
ggsave(file='3by20kmeansPlusPlus.pdf')

# 2.2.2 (g)
results = foreach(k=c(2, 3, 5), .options.multicore=mcoptions) %dopar% mixtureOfGaussians(k=k,
                                                                                         data=origData[,c('x1','x2')],
                                                                                         delta=DELTA,
                                                                                         samplingFunction=kmeansPlusPlusSample)
lapply(results, function(result) { 
    print(paste('k:', result$k, 'number of iterations:', result$numIterations))
    centroids <- as.data.frame(result$centroids)
    centroids$cluster <- as.factor(seq(1,result$k))
    expect_that(ncol(centroids), equals(3))
    data <- as.data.frame(cbind(origData, cluster=as.factor(result$clusters)))
    expect_that(ncol(data), equals(4))
    plotFilename=paste(result$k,'mog.pdf',sep='_')
    plotClusters(data, centroids)
    ggsave(file=plotFilename)
})

foo = mixtureOfGaussians(k=3,
                   data=origData[,c('x1','x2')],
                   delta=DELTA,
                         labels=as.factor(origData[,'class']), 
                         samplingFunction=kmeansPlusPlusSample)

k=3
data=origData[,c('x1','x2')]
delta=DELTA
labels=as.factor(origData[,'class']) 
samplingFunction=kmeansPlusPlusSample