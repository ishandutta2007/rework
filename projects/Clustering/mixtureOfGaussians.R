require(testthat)
require(mvtnorm)

pOfX <- function(x, mu, sigma) {
    dmvnorm(x,
            mean=mu, 
            sigma=sigma)
}

mixtureOfGaussians <- function(k, data, delta, labels=NA, samplingFunction=randomSample, lambda=NA) {
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
    
    for(i in seq(1,35)) {
#    while(TRUE) {
        iteration = iteration+1
        expect_that(sum(clusterPi), equals(1))
        
        # E Step: Determine responsibility of each cluster
        responsibilities <- lapply(seq(1,k), # foreach cluster
                                   function(cluster) {
                                           apply(X, # foreach observation
                                                 1, 
                                                 function(xi) { 
                                                     numerator = clusterPi[cluster] * pOfX(x=xi, mu=clusterMu[cluster,], sigma=clusterSigma[[cluster]])
                                                     allClusters = lapply(seq(1,k), function(cl) { # foreach cluster
                                                         clusterPi[cl] * pOfX(x=xi, mu=clusterMu[cl,], sigma=clusterSigma[[cl]])
                                                     })
                                                     numerator/sum(unlist(allClusters))    
                                                 })
                                           })
        responsibilities = do.call(cbind,responsibilities)
        expect_that(c(nrow(X),k), equals(dim(responsibilities)))
        expect_that(sum(rowSums(responsibilities)), equals(nrow(X)))
        
        # M Step: Use responsibilities to update our parameters
        clusterPi = colSums(responsibilities)/nrow(X)
        expect_that(sum(clusterPi), equals(1))
        
        # check this
        clusterMu = (t(responsibilities) %*% X)/colSums(responsibilities)
        expect_that(c(k,ncol(X)), equals(dim(clusterMu)))
                
        for(cluster in seq(1,k)) { # TODO vectorize this the rest of the way
            sigma = (1/sum(responsibilities[,cluster]) * (t(X) %*% (responsibilities[,cluster] * X))) - (clusterMu[cluster,] %*% t(clusterMu[cluster,]))
            if(is.na(lambda)) {
                clusterSigma[[cluster]] = sigma
            } else {
                # Perform regularization
                clusterSigma[[cluster]] = (1-lambda)*sigma + lambda*diag(ncol(X))
            }
            expect_that(c(ncol(X),ncol(X)), equals(dim(clusterSigma[[cluster]])))
        }
                
        # Perform hard assignment
        clusters = factor(x=apply(responsibilities, 1, which.max), levels=seq(1,k))
        expect_that(nrow(X), equals(length(clusters)))
        
        # Compute Loss
        if(0 == sum(is.na(labels))) {
           losses[[iteration]] = sum(labels != clusters)            
        }
        
        # Compute likelihood
        clusterLikelihoods <- sapply(seq(1:k), 
                        function(cluster) { 
                            prior = sum(responsibilities[,cluster] * log(clusterPi[cluster]))
                            foo =   sum(responsibilities[,cluster] *                
                                            apply(X,
                                                  1,
                                                  function(x) { 
                                                      log(pOfX(x=x, mu=clusterMu[cluster,], sigma=clusterSigma[[cluster]]))
                                                  })
                                        )
                            prior + foo
                            })
        currentLikelihood = sum(clusterLikelihoods)
        if(!is.na(prevLikelihood)) {
            if(delta > (prevLikelihood - currentLikelihood)) {
#                break;
            }
        }
        prevLikelihood = currentLikelihood
        likelihoods[iteration] = currentLikelihood
        print(paste('NumClusters:', k, 'Iteration:', iteration, 'Likelihood:', currentLikelihood))
        print(clusterMu)
        print(clusterPi)
    }
    
    centriods <- clusterMu
    list(k=k, numIterations=iteration, centroids=centroids, clusters=clusters, likelihoods=likelihoods, loss=losses)
}
