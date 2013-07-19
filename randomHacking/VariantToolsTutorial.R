
library(gmapR)
param <- GsnapParam(TP53Genome(), unique_only = TRUE,
                    molecule = "DNA")

extdata.dir <- system.file("extdata",
                           package="VariantToolsTutorial")
first.fastq <- dir(extdata.dir, "first.fastq",
                   full.names=TRUE)
last.fastq <- dir(extdata.dir, "last.fastq",
                  full.names=TRUE)

output <- gsnap(first.fastq[1], last.fastq[1], param, output="/home/ubuntu/gsnap.sam")
bam <- as(output, "BamFile")

library(VariantTools)
data(repeats, package = "VariantToolsTutorial")
param <- TallyVariantsParam(TP53Genome(), mask = repeats)

tallies <- tallyVariants(bam, param)

data(tallies, package = "VariantToolsTutorial")

stacked.tallies <- stackSamples(tallies)
merged.tallies <- merge(tallies)
sampleNames(merged.tallies) <- "merged"

calling.filters <- VariantCallingFilters()

post.filters <- VariantPostFilters()

merged.variants <- callVariants(merged.tallies,
                                calling.filters,
                                post.filters)

merged.variants <- callVariants(merged.tallies)
stacked.variants <- callVariants(stacked.tallies)

variants <- callVariants(bam, param)

pdf(file="fig/density-altFraction.pdf")
stacked.variants$altFraction <-
  altDepth(stacked.variants) / totalDepth(stacked.variants)
library(ggplot2)
qplot(altFraction, geom = "density", color = sampleNames,
      data = as.data.frame(stacked.variants))
dev.off()

data(geno, package = "VariantToolsTutorial")

naToZero <- function(x) ifelse(is.na(x), 0L, x)
addExpectedFreqs <- function(x) {
  expected.freq <- geno$expected.freq[match(x, geno)]
  x$expected.freq <- naToZero(expected.freq)
  x
}
stacked.variants <- addExpectedFreqs(stacked.variants)
merged.variants <- addExpectedFreqs(merged.variants)

softFilterMatrix(geno) <-
  cbind(in.merged = geno %in% merged.variants)
mean(called(geno))

m <- match(geno, merged.tallies)
altDepth(geno) <- naToZero(altDepth(merged.tallies)[m])
totalDepth(geno) <- naToZero(totalDepth(merged.tallies)[m])

fn.geno <- geno[!called(geno)]
fn.geno <- resetFilter(fn.geno)
filters <- hardFilters(merged.variants)[3:4]
fn.geno <- softFilter(fn.geno, filters)
t(summary(softFilterMatrix(fn.geno)))

fn.geno <- resetFilter(fn.geno)
fn.geno <- softFilter(fn.geno, filters, serial = TRUE)
t(summary(softFilterMatrix(fn.geno)))

vcfPath <- system.file("extdata", "dbsnp-p53.vcf.gz",
                       package = "VariantToolsTutorial")
param <- ScanVcfParam(fixed = "ALT", info = NA, geno = NA)
dbSNP <- as(readVcf(vcfPath, param, genome = "hg19"),
            "VRanges")
dbSNP <- dbSNP[!isIndel(dbSNP)]

stacked.variants$dbSNP <- stacked.variants %in% dbSNP
xtabs(~ dbSNP + expected.freq, mcols(stacked.variants))

tabulated.variants <- tabulate(stacked.variants)
xtabs(~ dbSNP + sample.count, mcols(tabulated.variants))

library(SRAdb)
startIGV("lm")
sock <- IGVsocket()

mcols(merged.variants) <- NULL
vcf <- writeVcf(sort(merged.variants),
                "merged.variants.vcf",
                index = TRUE)
vcf <- tools::file_path_as_absolute(vcf)

extdata <- system.file("extdata",
                       package = "VariantToolsTutorial")
bams <- tools::list_files_with_exts(extdata, "bam")
p53fasta <- tempfile("p53", fileext = ".fasta")
rtracklayer::export(TP53Genome(), p53fasta)
session <- IGVsession(c(bams, vcf), "session.xml",
                      p53fasta)

IGVload(sock, session)

rtracklayer::export(merged.variants, "roi.bed")
