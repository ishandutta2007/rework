source("http://bioconductor.org/biocLite.R")
biocLite("BiocParallel")
library("BiocParallel")
ls("package:BiocParallel")
registered()
?bplapply
foo <- bplapply(1:10, function(x) { mean(rnorm(x))})
foo
foo <- bplapply(-10:10, function(x) { return(x)})
foo
# New error stuff not available yet
foo <- bplapply(-10:10, function(x) { mean(rnorm(x))})
foo
