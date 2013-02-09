require(testthat)

llyodsKmeans <- function(k, data, delta, labels=NA, samplingFunction=randomSample) {
    # Choose our k centroids from our N data points
    centroids = samplingFunction(k=k, data=data)
    
    prevCost = NA
    currentCost = NA
    iteration = 0
    loss = list()
    
    while(TRUE) {
        iteration = iteration+1
        
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
        
        # Compute Loss
        if(0 == sum(is.na(labels))) {
            loss[[iteration]] = sum(labels != clusters)            
        }
        
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
        print(paste('NumClusters:', k, 'Iteration:', iteration))
    }
    
    list(k=k, numIterations=iteration, centroids=centroids, clusters=clusters, finalCosts=costs, loss=loss)
}