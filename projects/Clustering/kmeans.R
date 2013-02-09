library(multicore)
library(doMC)
library(foreach)
library(testthat)
library(ggplot2)
library(mvtnorm)

llyodsKmeans <- function(k, data, delta, samplingFunction=randomSample) {
    # Randomly choose our k centroids from our N data points
    centroids = samplingFunction(k=k, data=data)
    
    prevCost = NA
    currentCost = NA
    iteration = 0
    
    while(TRUE) {
        # E Step: Assign each data point to the closest cluster
        clusters = apply(data, 
                         1, 
                         function(x) { 
                             distances <- apply(centroids, 1, function(mu) { sqrt(sum((x - mu) ^ 2)) } )    
                             which.min(distances)
                         }
        )
        expect_that(length(clusters), equals(nrow(data)))
        clusters <- as.factor(clusters)
        
        # M Step: Compute the average of all points assigned to each centroid and then update each
        newcentroids = sapply(seq(1:k), function(c) { colSums(data[clusters==c,])/nrow(data[clusters==c,]) })
        centroids = t(newcentroids)
        
        # Compute Cost
        costs <- sapply(seq(1:k), 
                        function(cluster) { sum(
                            apply(data[clusters==cluster,], 
                                  1, 
                                  function(x) { (x - centroids[cluster,])^2 })) })
        currentCost = sum(costs)
        if(!is.na(prevCost)) {
            if(delta > (prevCost - currentCost)) {
                break;
            }
        }
        prevCost = currentCost
        iteration = iteration+1
        print(paste('NumClusters:', k, 'Iteration:', iteration))
    }
    
    list(k=k, numIterations=iteration, centroids=centroids, clusters=clusters, costs=costs)
}