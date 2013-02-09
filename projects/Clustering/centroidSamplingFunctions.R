randomSample <- function(k, data) {
    # Randomly choose our k centroids from our N data points
    centroids = data[sample(nrow(data),k),]
    expect_that(nrow(centroids), equals(k))
    print(paste("Random sample:", centroids))
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
        centroids <- rbind(centroids, 
                           data[sample(nrow(data),1,prob=probs),])
    }
    expect_that(nrow(centroids), equals(k))
    print(paste("KMeans++ sample:", centroids))
    centroids
}
