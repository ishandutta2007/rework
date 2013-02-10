require(testthat)
require(mvtnorm)

mixtureOfGaussians <- function(k, data, delta, labels=NA, samplingFunction=randomSample, lambda=0.2) {
    # Choose our k centroids from our N data points
    centroids = samplingFunction(k=k, data=data)
    clusterMu = as.matrix(centroids)
    expect_that(clusterMu[k,2], equals(as.numeric(centroids[k,2])))
    clusterPi = rep(1/3, k)
    clusterSigma = lapply(seq(1,k), function(x) { diag(2) })
    
    X = as.matrix(data)
    expect_that(as.numeric(X[5,2]), equals(data[5,2]))
    prevLikelihood = NA
    currentLikelihood = NA
    iteration = 0
    losses = list()
    likelihoods = list()
    
    for(i in seq(1,2000)) {
#    while(TRUE) {
        iteration = iteration+1
        expect_that(sum(clusterPi), equals(1))
        
        # E Step: Determine responsibility of each cluster
        responsibilities <- lapply(seq(1,k), # foreach cluster
                                   function(cluster) {
                                           apply(X, # foreach observation
                                                 1, 
                                                 function(xi) { 
                                                     numerator = clusterPi[cluster] * pmvnorm(mean=clusterMu[cluster,], sigma=clusterSigma[[cluster]], lower=rep(-Inf, 2), upper=xi)
                                                     allClusters = lapply(seq(1,k), function(cl) { # foreach cluster
                                                         clusterPi[cl] * pmvnorm(mean=clusterMu[cl,], sigma=clusterSigma[[cl]], lower=rep(-Inf, 2), upper=xi)
                                                     })
                                                     numerator/sum(unlist(allClusters))    
                                                 })
                                           })
        responsibilities = do.call(cbind,responsibilities)
        expect_that(c(nrow(X),k), equals(dim(responsibilities)))
        
        # M Step: Use responsibilities to update our parameters
        clusterPi = colSums(responsibilities)/nrow(X)
        expect_that(sum(clusterPi), equals(1))
        
        clusterMu = t(responsibilities) %*% X
        expect_that(c(k,ncol(X)), equals(dim(clusterMu)))
                
        for(cluster in seq(1,k)) { # TODO vectorize this the rest of the way
            distance = X - clusterMu[cluster,]
            expect_that(dim(distance), equals(dim(X)))
            squaredDistance = distance * distance
            expect_that(dim(squaredDistance), equals(dim(X)))
            numerator = squaredDistance * responsibilities[,cluster]
            expect_that(dim(numerator), equals(dim(X)))
            sigmaSquared = colSums(numerator)/sum(responsibilities[,cluster])
            # Perform regularization
            expect_that(length(sigmaSquared), equals(ncol(X)))            
            clusterSigma[[cluster]] = (1-lambda)*diag(sigmaSquared) + lambda*diag(ncol(X))
            expect_that(c(ncol(X),ncol(X)), equals(dim(clusterSigma[[cluster]])))
        }
                
        # Perform hard assignment
        clusters = factor(x=apply(responsibilities, 1, which.max), levels=seq(1,k))
        expect_that(nrow(X), equals(length(clusters)))
        
        # Compute Loss
        if(0 == sum(is.na(labels))) {
           loss[[iteration]] = sum(labels != clusters)            
        }
        
        # Compute likelihood
        clusterLikelihoods <- sapply(seq(1:k), 
                        function(cluster) { sum(clusterPi[cluster] *
                            apply(X, 
                                  1,
                                  function(x) { pmvnorm(mean=clusterMu[cluster,], sigma=clusterSigma[[cluster]], lower=rep(-Inf, 2), upper=x) }))})
        currentLikelihood = sum(clusterLikelihoods)
        if(!is.na(prevLikelihood)) {
            if(delta > (prevLikelihood - currentLikelihood)) {
#                break;
            }
        }
        prevLikelihood = currentLikelihood
        likelihoods[iteration] = currentLikelihood
        print(paste('NumClusters:', k, 'Iteration:', iteration, 'Likelihood:', currentLikelihood))
    }
    
    centriods <- clusterMu
    list(k=k, numIterations=iteration, centroids=centroids, clusters=clusters, likelihoods=likelihoods, loss=loss)
}