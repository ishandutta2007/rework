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

# TODO install dev version of BiocParallel for the rest of this to work
### BATCHJOBS INTEGRATION
library(BatchJobs)

param <- BatchJobsParam()

numbers <- 10:20
ans <- bplapply(numbers, function(x) mean(rnorm(x)), BPPARAM = param)
unlist(ans)

### ERROR HANDLING (not just for BatchJobs)

# oops
numbers <- -20:20
ans <- bplapply(numbers, function(x) mean(rnorm(x)), BPPARAM = param)
unlist(ans)

LastError # global symbol

# resubmission
ans <- bplapply(LastError, function(x) mean(rnorm(abs(x))), BPPARAM = param)
unlist(ans)

