library(ggplot2)
library(testthat)
library(Matrix)

setwd('/Users/deflaux/rework/projects/Clustering')
source('kmeans.R')
source('mixtureOfGaussians.R')

# Constants and some environment initialization
set.seed(42)
DELTA = 0.000001
NUM_TERMS = 99
NUM_DOCS = 1791
NUM_TERMDOC_COUNTS = 4983
NUM_CLASSES = 5

# Load Data
termCount <- read.table('./data/bbc.mtx', skip=2, col.names=c('termid','docid','count'))
expect_that(ncol(termCount), equals(3))
expect_that(length(unique(termCount[,'termid'])), equals(NUM_TERMS))
expect_that(unique(termCount[,'termid']), equals(seq(1,NUM_TERMS)))
expect_that(length(unique(termCount[,'docid'])), equals(NUM_DOCS))
expect_that(nrow(termCount), equals(NUM_TERMDOC_COUNTS))

docClasses <- read.table('./data/bbc.classes', col.names=c('docid','classid'))
expect_that(ncol(docClasses), equals(2))
expect_that(length(unique(docClasses[,'docid'])), equals(NUM_DOCS))
expect_that(length(unique(docClasses[,'classid'])), equals(NUM_CLASSES))
expect_that(nrow(docClasses), equals(NUM_DOCS))

terms <- read.table('./data/bbc.terms', col.names=c('term'), stringsAsFactors=FALSE)
expect_that(ncol(terms), equals(1))
expect_that(length(unique(terms[,'term'])), equals(NUM_TERMS))
expect_that(nrow(terms), equals(NUM_TERMS))

centers <- read.table('./data/bbc.centers')
expect_that(ncol(centers), equals(NUM_TERMS))
expect_that(nrow(centers), equals(NUM_CLASSES))

# Convert term counts to tf-idf
termFreq <- apply(termCount, 1, function(row) {
    maxTermCountInDoc = max(subset(termCount, docid==row['docid'], count))
    c(row['termid'],row['docid'],row['count']/maxTermCountInDoc)
})
termFreq <- as.data.frame(t(termFreq))
colnames(termFreq) <- c('termid','docid','freq')
expect_that(dim(termFreq), equals(dim(termCount)))
expect_that(termFreq[termFreq$docid==42, 'freq'], equals(c(0.8, 1.0, 0.2, 0.2)))

invDocFreq <- unlist(lapply(seq(1,NUM_TERMS), function(termid) {
    log(NUM_DOCS/nrow(subset(termCount, termid==12)))
}))
expect_that(length(invDocFreq), equals(NUM_TERMS))
expect_that(invDocFreq[12], equals(log(NUM_DOCS/207)))

tfidf <- apply(termFreq, 1, function(row) {
    c(row['termid'],row['docid'],row['freq']*invDocFreq[row['termid']])
})
tfidf <- as.data.frame(t(tfidf))
colnames(tfidf) <- c('termid','docid','tfidf')
expect_that(tfidf$termid, equals(termCount$termid))
expect_that(tfidf$docid, equals(termCount$docid))
expect_that(dim(tfidf), equals(dim(termCount)))
expect_that(tfidf[tfidf$docid==42, 'tfidf'], equals(c(1.7262485, 2.1578106, 0.4315621, 0.4315621)))

labels = as.factor(docClasses$classid+1) # 0-4 -> 1-5

# Let's write our sparse matrix out to a file, and then read it back in via MatrixMarket format since that is what it is
# Rows correspond to documents, columns to terms
# TODO how to skip writing this out to a file?
tfidfFile <- './data/bbc-tfidf.txt'
tfidfMMFile <- './data/bbc-tfidf.mtx'
write.table(rbind(c(NUM_TERMS, NUM_DOCS, NUM_TERMDOC_COUNTS), tfidf), tfidfFile, row.names=FALSE, col.names=FALSE)
system(paste('echo "%%MatrixMarket matrix coordinate real general" >', tfidfMMFile))
system(paste('cat', tfidfFile, '>>', tfidfMMFile))
tfidfMatrix <- t(readMM(tfidfMMFile))
expect_equal(tfidf[tfidf$docid==42 & tfidf$termid==23, 'tfidf'], tfidfMatrix[42,23])

# 2.2.2.(a)
for(class in seq(0,NUM_CLASSES-1)) {
    docsInClass = unlist(subset(docClasses, classid == class, docid))
    numDocsInClass = length(docsInClass)
    sortedSums = sort(colSums(tfidfMatrix[docsInClass,]), decreasing=TRUE, index.return=TRUE)
    print(paste('class:', class))
    for(termImportance in seq(1,5)) {
        print(paste('   term:', terms[sortedSums$ix[termImportance],1], 
                    '   avg tfidf:', sortedSums$x[termImportance]/numDocsInClass))
    }
}

hardcodedCentroids <- function(k, data) {
    as.matrix(centers)
}

# 2.2.2 (b)
result = llyodsKmeans(k=NUM_CLASSES,
                      data=tfidfMatrix,
                      labels=labels,
                      samplingFunction=hardcodedCentroids,
                      delta=DELTA)
losses = as.data.frame(cbind(loss=unlist(result$loss)[1:5], iteration=seq(1,5)))
p <- qplot(iteration, loss, data=losses)
p + geom_smooth()
ggsave(file='bbcKmeansLosses.jpg')

# 2.2.2 (c)
result = mixtureOfGaussians(k=NUM_CLASSES,
                            data=tfidfMatrix,
                            labels=labels,
                            samplingFunction=hardcodedCentroids,
                            lambda=0.2,
                            delta=DELTA)
losses = as.data.frame(cbind(loss=unlist(result$loss)[1:5], iteration=seq(1,5)))
p <- qplot(iteration, loss, data=losses)
p + geom_smooth()
ggsave(file='bbcMoGLosses.jpg')

likelihoods = as.data.frame(cbind(likelihood=unlist(result$likelihoods)[1:5], iteration=seq(1,5)))
p <- qplot(iteration, likelihood, data=likelihoods)
p + geom_smooth()
ggsave(file='bbcMoGLikelihood.jpg')
