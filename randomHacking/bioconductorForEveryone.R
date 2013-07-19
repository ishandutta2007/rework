# http://bioconductor.org/help/course-materials/2013/BioC2013/

# R and Bioconductor for Everyone
x = rnorm(1000)
y = x + rnorm(1000, sd=.5)
plot(x, y)
fit = lm(y ~ x)
fit
abline(fit)
anova(fit)
nobs(fit)
class(fit)
methods(class='lm')
methods(anova)
nobs.lm
getAnywhere("nobs.lm")

library(IRanges)
ir = IRanges(c(10, 100), width=c(30,20))
class(ir)
showMethods(class='IRanges', where=search()) # look on current search path

library(BSgenome)
installed.genomes()
library(BSgenome.Hsapiens.UCSC.hg18)

# Rsamtools (see also GenomicRanges)


###################################################
### code chunk number 16: setup
###################################################
library(pasillaBamSubset)
library(GenomicRanges)  # readGAlignments
library(ShortRead)  # alphabetByCycle


###################################################
### code chunk number 17: param
###################################################
flag <- scanBamFlag(isMinusStrand=FALSE)
param <- ScanBamParam(what=c("seq", "qual"), flag=flag)


###################################################
### code chunk number 18: query
###################################################
fl <- untreated1_chr4()
res <- readGAlignments(fl, param=param)


###################################################
### code chunk number 19: abcplot
###################################################
abc <- alphabetByCycle(mcols(res)$seq)
matplot(t(abc[1:4,]), type="l", lty=1, lwd=2, xlab="Cycle", ylab="Count")
legend("topright", legend=rownames(abc)[1:4], lty=1, lwd=2, col=1:4)

# observe that there is a strong bias for the first few sites, has to do with protocol for machine
# see periodicity in longer portions of line

# also see that quality score is good (33) early on and poor later (20), probability in neg log base 1-

###################################################
### code chunk number 20: qualplot
###################################################
qual <- as(mcols(res)$qual, "matrix")
boxplot(qual ~ col(qual), outline=FALSE, xlab="Cycle", ylab="Quality")


###################################################
### code chunk number 21: annotations
###################################################
library(TxDb.Dmelanogaster.UCSC.dm3.ensGene)  # genome coordinates
exByGn <- exonsBy(TxDb.Dmelanogaster.UCSC.dm3.ensGene, "gene")
chr4 <- exByGn[ all(seqnames(exByGn) == "chr4") ]


###################################################
### code chunk number 22: files
###################################################
fls <- c(untreated1_chr4(), untreated3_chr4())
names(fls) <- sub("_chr4.bam", "", basename(fls))
bfl <- BamFileList(fls)


###################################################
### code chunk number 23: counts
###################################################
counts <- summarizeOverlaps(chr4, bfl, ignore.strand=TRUE)
head(assay(counts))


###################################################
### code chunk number 24: countsplot
###################################################
plot(asinh(assay(counts)), asp=1, main="asinh(counts), chr4")
abline(0, 1, lty=2)


###################################################
### code chunk number 25: gff (eval = FALSE)
###################################################
## library(rtracklayer)                    # import gff
## fl <- paste0("ftp://ftp.ensembl.org/pub/release-62/", "gtf/drosophila_melanogaster/",
##              "Drosophila_melanogaster.BDGP5.25.62.gtf.gz")
## gffFile <- file.path(tempdir(), basename(fl))
## download.file(fl, gffFile)
## gff0 <- import(gffFile)
## idx <- gff0$source == "protein_coding" & gff0$type == "exon" & seqnames(gff0) == "4"
## gff1 <- gff0[idx]
## chr4.gff <- split(gff1, mcols(gff1)$gene_id)


###################################################
### code chunk number 26: counter
###################################################
counter <-
  function(aln, roi)
  {
    strand(aln) <- "*"                  # strand-neutral protocol
    hits <- findOverlaps(aln, roi)
    keep <- which(countQueryHits(hits) == 1)
    cnts <- countSubjectHits(hits[queryHits(hits) %in% keep])
    setNames(cnts, names(roi))
  }


###################################################
### code chunk number 27: counter-sapply
###################################################
countFile <- 
  function(fl, roi)
  {
    open(fl); on.exit(close(fl))
    aln <- readGAlignments(fl)
    counter(aln, roi)
  }
count0 <- sapply(bfl, countFile, chr4)
head(count0)


###################################################
### code chunk number 28: counter-chunks
###################################################
countInChunks <- 
  function(fl, roi)
  {
    yieldSize(fl) <- 1000000            # chunks of size 1 million
    open(fl); on.exit(close(fl))
    count <- integer(length(range))     # initial count vector
    while (length(aln <- readGAlignments(fl)))
      count <- count + counter(aln, roi)
    count
  }
count1 <- sapply(bfl, countInChunks, chr4)
identical(count0, count1)


###################################################
### code chunk number 29: counter-parallel
###################################################
library(parallel)
options(mc.cores=detectCores())         # use all cores for parallel evaluation
mcsapply <- function(...) simplify2array(mclapply(...))
count2 <- mcsapply(bfl, countInChunks, chr4)
identical(count0, count2)


