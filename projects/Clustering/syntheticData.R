library(multicore)
library(doMC)
library(foreach)
library(testthat)
library(ggplot2)
library(mvtnorm)

source('centroidSamplingFunctions.R')
source('kmeans.R')

mixtureOfGaussians <- function(k, data, delta, samplingFunction=randomSample, plotFilename=NA) {
    # Randomly choose our k centroids from our N data points
    centroids = samplingFunction(k=k, data=data)
    clusterMu = as.matrix(centroids[,c('x1','x2')])
    expect_that(clusterMu[k,2], equals(as.numeric(centroids[k,'x2'])))
    clusterPi = rep(1/3, k)
    clusterSigma = lapply(seq(1,k), function(x) { diag(2) })
    
    X = as.matrix(data[,c('x1','x2')])
    expect_that(as.numeric(X[5,2]), equals(data[5,'x2']))
    prevLikelihood = NA
    currentLikelihood = NA
    iteration = 0
    
    while(TRUE) {
        expect_that(sum(clusterPi), equals(1))
        
        # E Step: Determine responsibility of each cluster
        responsibilities <- lapply(seq(1,k),
                                   function(cluster) {
                                           apply(X, 
                                                 1, 
                                                 function(xi) { 
                                                     numerator = clusterPi[cluster] * pmvnorm(mean=clusterMu[cluster,], clusterSigma[[cluster]], lower=rep(-Inf, 2), upper=xi)
                                                     allClusters = lapply(seq(1,k), function(cl) { 
                                                         clusterPi[cl] * pmvnorm(mean=clusterMu[cluster,], clusterSigma[[cl]], lower=rep(-Inf, 2), upper=xi)
                                                     })
                                                     numerator/sum(unlist(allClusters))    
                                                 })
                                           })
        expect_that(length(responsibilities), equals(k))
        expect_that(sum(unlist(responsibilities)), equals(nrow(data)))
        
        # M Step: Use responsibilities to update our parameters
        clusterPi = unlist(lapply(responsibilities, function(rk) { sum(rk)/nrow(X) }))
        expect_that(sum(clusterPi), equals(1))
        
        for(cluster in seq(1,k)) { # TODO vectorize this for loop
            clusterMu[k,1] = sum(responsibilities[[cluster]]*X[,1])/sum(responsibilities[[cluster]])
            clusterMu[k,2] = sum(responsibilities[[cluster]]*X[,2])/sum(responsibilities[[cluster]])
        }

        for(cluster in seq(1,k)) { # TODO vectorize this for loop
            muMatrix = clusterMu[k,]*t(clusterMu[k,])
            rowContributions = 0
            for(row in seq(1,nrow(X))) {
                rowContributions = rowContributions + responsibilities[[cluster]][row] * X[row,] * t(X[row,]) - muMatrix
            }
            clusterSigma[[cluster]] = diag(as.numeric(rowContributions/sum(responsibilities[[cluster]])))
            expect_that(c(2,2), equals(dim(clusterSigma[[cluster]])))
        }
        
#        Browse[2]> apply(X, 1, function(x) {
#            +     pmvnorm(mean = clusterMu[cluster, ], clusterSigma[[cluster]], 
#                          +             lower = rep(-Inf, 2), upper = x)
#            + })
#        Error in checkmvArgs(lower = lower, upper = upper, mean = mean, corr = corr,  : 
#                                 ‘corr’ is not a correlation matrix
        
        
        # Compute likelihood
        likelihoods <- sapply(seq(1:k), 
                        function(cluster) { sum(clusterPi[cluster] *
                            apply(X, 
                                  1, # THE BUG IS HERE
                                  function(x) { pmvnorm(mean=clusterMu[cluster,], clusterSigma[[cluster]], lower=rep(-Inf, 2), upper=x) }))})
        currentLikelihood = sum(likelihoods)
        if(!is.na(prevLikelihood)) {
            if(delta > (prevLikelihood - currentLikelihood)) {
                break;
            }
        }
        prevLikelihood = currentLikelihood
        iteration = iteration+1
        print(paste('NumClusters:', k, 'Iteration:', iteration))
    }
    
    centriods[,c('x1','x2')] <- clusterMu
    if(!is.na(plotFilename)) {
        plotClusters(data, centroids)
        ggsave(file=plotFilename)
    }
    list(k=k, numIterations=iteration, centroids=centroids, likelihoods=likelihoods)
}

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
results = foreach(k=c(2, 3, 5), .options.multicore=mcoptions) %do% mixtureOfGaussians(k=k, 
                                                                                   data=origData[,c('x1','x2')], 
                                                                                   delta=DELTA, 
                                                                                   samplingFunction=kmeansPlusPlusSample,
                                                                                   plotFilename=paste(k,'mixed.pdf',sep='_'))
lapply(results, function(result) { print(paste('k:', result$k, 'number of iterations:', result$numIterations))})

