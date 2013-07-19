####Code for BioC workshop on ReportingTools
##July 19 2013 Seattle WA
##RNA-seq ReportingTools


###################################################
### (1) Load RNA-seq data
###################################################
library(ReportingTools)
data(mockRnaSeqData)
head(mockRnaSeqData)
conditions <- c(rep('case',3), rep('control', 3))
age <- c(1, 2, 5, 1, 2, 5)


###################################################
### (2) Run edgeR exact and LRT analyses
###################################################
library(edgeR)

### Exact method
d <- DGEList(counts = mockRnaSeqData, group = conditions)
d <- calcNormFactors(d)
d <- estimateCommonDisp(d)
d <- estimateTagwiseDisp(d)
## ## Get an edgeR DGEExact object
edgeRExact <- exactTest(d)
class(edgeRExact)
topTags(edgeRExact)

### LRT (accounting for age in model)
design <- model.matrix(~conditions+age)
design
d <- estimateGLMCommonDisp(d,design)
d <- estimateGLMTrendedDisp(d,design)
d <- estimateGLMTagwiseDisp(d,design)
fit <- glmFit(d,design)
edgeRLRT <- glmLRT(fit, coef=2)
class(edgeRLRT)
topTags(edgeRLRT)


###################################################
### (3) Make the edgeR report for the DGEExact object
###################################################
library(lattice)
rep.theme <- reporting.theme()
lattice.options(default.theme = rep.theme)

deReport <- HTMLReport(shortName = 'RNAseq_analysis_with_edgeR_exact',
    title = 'RNA-seq analysis of differential expression using edgeR (exact test)',
    reportDirectory = './reports')
publish(edgeRExact, deReport, countTable = mockRnaSeqData,
	conditions = conditions, annotation.db = 'org.Mm.eg', 
	pvalueCutoff = .01, lfc = 5, n = 20, name ='edgeR')
finish(deReport)

### again can play with defaults:
##pvalueCutoff = 0.01, n = 1000, lfc = 0, 
##adjust.method = 'BH', make.plots = TRUE


###################################################
### (4) Make the edgeR report for the DGELRT object
###################################################
deReport <- HTMLReport(shortName = 'RNAseq_analysis_with_edgeR_lrt',
    title = 'RNA-seq analysis of differential expression using edgeR (LRT test, accounting for age)',
    reportDirectory = './reports')
publish(edgeRLRT, deReport, countTable = mockRnaSeqData,
	conditions = conditions, annotation.db = 'org.Mm.eg', 
	pvalueCutoff = .01, lfc = 5, n = 20, name = 'edgeR')
finish(deReport)

###################################################
### (4.5) Exercise: 
###################################################
###Make an edgeR report in the same reports directory containing a report named Top20.html 
###that only outputs the top 20 genes with log2 fold-change of 1.


###################################################
### (5) Run DESeq nbinom test
###################################################
##run DESeq
library(DESeq)
cds<-newCountDataSet(mockRnaSeqData, conditions)
cds<-estimateSizeFactors(cds)
cds<-estimateDispersions(cds)
res<-nbinomTest(cds,'case', 'control' )
class(res)


###################################################
### (6) Make DESeq report
###################################################
##built in function for DESeq results
ReportingTools:::makeDESeqDF
desReport <- HTMLReport(shortName = 'RNAseq_analysis_with_DESeq',
    	title = 'RNA-seq analysis of differential expression using DESeq',
    	reportDirectory = './reports')
publish(res, desReport, countTable = mockRnaSeqData, pvalueCutoff = 0.01,
		conditions = conditions, annotation.db = 'org.Mm.eg.db', expName = 'deseq',
		reportDir = './reports', .modifyDF = makeDESeqDF)
finish(desReport)



###################################################
### (7) Run DESeq2
###################################################
detach(package:DESeq)
library(DESeq2)
dds <- DESeqDataSetFromMatrix(countData = mockRnaSeqData,
                              colData = as.data.frame(cbind(conditions, age)),
                              design = ~ age + conditions)
dds <- DESeq(dds)
#dds <- estimateSizeFactors(dds)
#dds <- estimateDispersions(dds)
#ddsLRT <- nbinomLRT(dds, reduced = ~ age)
class(dds)
res <- results(dds)
class(res)
head(res)


###################################################
### (8) Make DESeq2 report
###################################################
makeDESeq2DF<-function(object, countTable, pvalueCutoff, conditions,
    annotation.db, expName, reportDir,...){
   	sigGenes <- which(object$padj < pvalueCutoff)
	if(length(sigGenes) < 1){
		stop("No genes meet the selection criteria. Try changing the p-value cutoff.")
	}
	resSig <- object[sigGenes,]
	countTableSig <- countTable[sigGenes,]
	eids <- rownames(countTableSig)
	eidsLink <- hwrite(as.character(eids), 
    	link=paste("http://www.ncbi.nlm.nih.gov/gene/", as.character(eids), sep=''), 
    	table=FALSE)
	gnamesAndSymbols <- ReportingTools:::getNamesAndSymbols(eids, annotation.db)
	na.index<-which(is.na(gnamesAndSymbols$symbol)==TRUE)
	gnamesAndSymbols$symbol[na.index]<-gnamesAndSymbols$entrez[na.index]

	imageFiles <- makeDESeq2Figures(countTableSig, conditions,
	 		gnamesAndSymbols$symbol, expName, reportDir)
  pngImageFiles <- sub("\\.pdf", ".png", imageFiles)
  images <- hwriteImage(pngImageFiles, link=imageFiles, table=FALSE, width=100)
    
	ret <- data.frame(
	    eidsLink, 
	    gnamesAndSymbols$symbol, 
	    gnamesAndSymbols$name,
	    images,resSig$baseMean,
	    resSig$log2FoldChange,
	    resSig$pvalue,
	    resSig$padj)
	colnames(ret) <- c("Entrez Id", "Symbol", "Gene Name", "Image", "Mean Counts",
	    "Log2 Fold Change", "P-value","Adjusted p-value")
	return(ret)
}


makeDESeq2Figures<-function(countTable, conditions, symbols, expName, reportDir){
	figures.dirname <- paste0(reportDir,"/figures", expName)  
    figure.directory <- file.path(figures.dirname)
    ReportingTools:::.safe.dir.create(figure.directory)
	pdfFiles<-c()
	countTable <- log2(countTable + 1)
	for(i in 1:length(symbols)){
		gene <- as.character(symbols[i])
		if (is.null(gene)==TRUE) {gene<-"NoSymbol"}
        ylab <- expression(log[2]~(Counts))
        bigplot <- stripplot(as.numeric(countTable[i, ]) ~ conditions,
            groups = conditions, 
            ylab = ylab, main = gene, scales = list(x=list(rot=45)))
      	pdf.filename <- paste("boxplot", gene, "pdf", sep=".")
        pdf.file <- paste0(reportDir, "/figures", expName, "/", pdf.filename)
		    png.filename <- paste("boxplot", gene, "png", sep=".")
		    png.file <- paste0(reportDir, "/figures", expName, "/", png.filename)
        if (!file.exists(pdf.file)) {
        	pdf(pdf.file, height=4.5, width=4.5)
        	print(bigplot)
        	dev.off()
        	png(png.file)
        	print(bigplot)
        	dev.off()
      }
       pdfFiles[i]<-paste0("figures", expName, "/", pdf.filename)
    }
	return(pdfFiles)
}


library(hwriter)

res <- as.data.frame(res)
class(res)

des2Report <- HTMLReport(shortName = 'RNAseq_analysis_with_DESeq2',
    	title = 'RNA-seq analysis of differential expression using DESeq2',
    	reportDirectory = './reports')
publish(res, des2Report, countTable = mockRnaSeqData, pvalueCutoff = 0.00001,
		conditions = conditions, annotation.db = 'org.Mm.eg.db', expName = 'deseq2',
		reportDir = './reports', .modifyDF = makeDESeq2DF)
finish(des2Report)

###################################################
### (9) Make DESeq2 report with .toDF
###################################################
toDESeq2DF<-function(object, countTable, pvalueCutoff, conditions,
                     annotation.db, expName, reportDir,...){
  ###these two lines are included in the .toDF function only
  res <- results(object)
  res <- as.data.frame(res)
  #####
  sigGenes <- which(res$padj < pvalueCutoff)
  if(length(sigGenes) < 1){
    stop("No genes meet the selection criteria. Try changing the p-value cutoff.")
  }
  resSig <- res[sigGenes,]
  countTableSig <- countTable[sigGenes,]
  eids <- rownames(countTableSig)
  eidsLink <- hwrite(as.character(eids), 
                     link=paste("http://www.ncbi.nlm.nih.gov/gene/", as.character(eids), sep=''), 
                     table=FALSE)
  gnamesAndSymbols <- ReportingTools:::getNamesAndSymbols(eids, annotation.db)
  na.index<-which(is.na(gnamesAndSymbols$symbol)==TRUE)
  gnamesAndSymbols$symbol[na.index]<-gnamesAndSymbols$entrez[na.index]
  
  imageFiles <- makeDESeq2Figures(countTableSig, conditions,
                                  gnamesAndSymbols$symbol, expName, reportDir)
  pngImageFiles <- sub("\\.pdf", ".png", imageFiles)
  images <- hwriteImage(pngImageFiles, link=imageFiles, table=FALSE, width=100)
  
  ret <- data.frame(
    eidsLink, 
    gnamesAndSymbols$symbol, 
    gnamesAndSymbols$name,
    images,resSig$baseMean,
    resSig$log2FoldChange,
    resSig$pvalue,
    resSig$padj)
  colnames(ret) <- c("Entrez Id", "Symbol", "Gene Name", "Image", "Mean Counts",
                     "Log2 Fold Change", "P-value","Adjusted p-value")
  return(ret)
}

class(dds)

des2Report <- HTMLReport(shortName = 'RNAseq_analysis_with_DESeq2_toDF',
                         title = 'RNA-seq analysis of differential expression using DESeq2',
                         reportDirectory = './reports')
publish(dds, des2Report, countTable = mockRnaSeqData, pvalueCutoff = 0.00001,
        conditions = conditions, annotation.db = 'org.Mm.eg.db', expName = 'deseq2',
        reportDir = './reports', .toDF = toDESeq2DF, .modifyDF = list())
finish(des2Report)


###################################################
### (9.5) Excercise
###################################################
##Make another DESeq2 report in the same reports directory. This report should have the following:
##linear fold-change instead of log2 fold-change by modifying the makeDESeq2DF function and 
##change log2 fold-change to linear fold-change.
##also add an argument to pass in a user specified number of genes to display in the report and 
##change code within makeDESeq2DF function for this to work.
