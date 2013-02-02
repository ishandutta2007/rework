library(multicore)
library(doMC)
library(foreach)
library(testthat)
library(ggplot2)

setwd('/Users/deflaux/rework/projects/Clustering')

origData <- read.csv('/Users/deflaux/rework/projects/Clustering/data/2DGaussianMixture.csv', header=TRUE)
str(origData)
expect_that(ncol(origData), equals(3))
data <- cbind(origData, cluster=as.factor(rep(-1, nrow(origData))))
str(data)
expect_that(ncol(data), equals(4))

plotClusters <- function(data, centroids) {
    p <- ggplot(data=data, aes(x=x1, y=x2, color=cluster))
    p <- p + geom_point() + geom_point(data=centroids, aes(x=x1, y=x2, color=cluster), size=5)
    p <- p + geom_point(data=centroids, aes(x=x1, y=x2, color=cluster), shape=19, size=52, alpha=.5, show_guide=FALSE)
    p
}

llyodsKmeans <- function(k, data, delta) {
    # Randomly choose our k centroids from our N data points
    centroids = data[sample(nrow(data),k),c('x1','x2')]
    centroids <- cbind(centroids, cluster=as.factor(seq(1,k)))

    prevCost = NA
    currentCost = NA
    iteration = 0
    
    while(TRUE) {
        # E Step: Assign each data point to the closest cluster
        clusters = apply(data[,c('x1','x2')], 
                         1, 
                         function(x) { 
                             distances <- apply(centroids[,c('x1','x2')], 1, function(mu) { sqrt(sum((x - mu) ^ 2)) } )    
                             which.min(distances)
                         }
        )
        expect_that(length(clusters), equals(nrow(data)))
        data$cluster <- as.factor(clusters)
        expect_that(ncol(data), equals(4))
        
        # M Step: Compute the average of all points assigned to each centroid and then update each
        newcentroids = sapply(seq(1:k), function(c) { colSums(data[data$cluster==c,c('x1','x2')])/nrow(data[data$cluster==c,c('x1','x2')]) })
        centroids[,c('x1','x2')] = t(newcentroids)
        
        # Compute Cost
        costs <- sapply(seq(1:k), 
                        function(c) { sum(
                            apply(data[data$cluster==c,c('x1','x2')], 
                                  1, 
                                  function(x) { (x - centroids[c,c('x1','x2')])^2 })) })
        currentCost = sum(costs)
        if(!is.na(prevCost)) {
            if(delta > (prevCost - currentCost)) {
                break;
            }
        }
        prevCost = currentCost
        iteration = iteration+1
        print(paste("NumClusters:", k, "Iteration:", iteration))
        expect_that(data[,c('x1','x2')], equals(origData[,c('x1','x2')]))
    }
    
    plotClusters(data, centroids)
    ggsave(file=paste(k,'kmeans.pdf',sep='_'))
    iteration
}



set.seed(42)
registerDoMC()
getDoParWorkers()
mcoptions <- list(preschedule=FALSE, set.seed=FALSE)
foreach(k=c(2, 3, 5, 10, 15, 20), .options.multicore=mcoptions) %dopar% llyodsKmeans(k=k, data=data, delta=0.01)

#debug(llyodsKmeans)
#llyodsKmeans(k=15, data=data, delta=0.01)
