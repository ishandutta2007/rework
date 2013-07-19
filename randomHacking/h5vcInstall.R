# From http://www.ebi.ac.uk/~pyl/h5vc/

# wget http://www.ebi.ac.uk/~pyl/h5vc/h5vc.experimental.tgz
# tar xvzf
# R CMD BUILD
library(rhdf5)
biocLite("bit64")
# R CMD INSTALL
library("h5vc")
# wget http://www.ebi.ac.uk/~pyl/h5vc/data/example.tally.hfs5

source('~/rework/h5vc/examples/h5vc.test.R')

tallyFile = "example.tally.hfs5"
h5ls(tallyFile)
# 12 bases
#  6 samples
#  2 strands
# 90,354,753

coverageSums <- h5dapply(
  filename = tallyFile,
  group = "/ExampleStudy/16",
  names = c("Coverages"),
  range = c(28500000, 29500000),
  blocksize=10000,
  FUN = function(x)x
  )
