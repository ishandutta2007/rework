require(testthat)

randomSample <- function(k, data) {
    # Randomly choose our k centroids from our N data points
    centroids = data[sample(nrow(data),k),]
    expect_that(dim(centroids), equals(c(k,ncol(data))))
    print("Random sample:")
    print(centroids)
    centroids
}

kmeansPlusPlusSample <- function(k, data) {
    # Randomly choose our _first_ centroid from our N data points
    centroids = data[sample(nrow(data),1),]
    # Choose the rest using the probability distribution given by the 
    # distance from the most recently chosen centroid
    for(i in seq(2,k)) {
        distances = apply(data[,], 1, function(x){
            sqrt(sum((x - centroids[i-1,]) ^ 2))
        })
        probs = distances/sum(distances)
        expect_that(length(probs),equals(nrow(data)))
        expect_that(sum(probs), equals(1))
        centroids <- rbind(centroids, 
                           data[sample(nrow(data),1,prob=probs),])
    }
    expect_that(dim(centroids), equals(c(k,ncol(data))))
    print("KMeans++ sample:")
    print(centroids)
    centroids
}
