# Using formulas from http://en.wikipedia.org/wiki/Bloom_filter
determineRequiredNumberOfBits <- function(n, p) {
    -1 * ((n*log(p))/(log(2)^2))
}
determineOptimalNumberOfHashFunctions <- function(n, m) {
    k=(m/n)*log(2)
    ceiling(k)
}

# Egde BloomFilter
maximumNumberOfEdgesPerEpoch = 50000
maxDesiredProbabilityOfFalsePositive = 0.001
sizeOfEdgeBloomFilter = determineRequiredNumberOfBits(n=maximumNumberOfEdgesPerEpoch, p=maxDesiredProbabilityOfFalsePositive)
numHashFunctionsForEdgeBloomFilter = determineOptimalNumberOfHashFunctions(n=maximumNumberOfEdgesPerEpoch, m=sizeOfEdgeBloomFilter)