####Exercise solutions




## Solution Exercise 4.5
deReport <- HTMLReport(shortName = 'Top20',
    title = 'RNA-seq analysis of differential expression using edgeR (LRT test, accounting for age)',
    reportDirectory = './reports')
publish(edgeRLRT, deReport, countTable = mockRnaSeqData,
	conditions = conditions, annotation.db = 'org.Mm.eg', 
	pvalueCutoff = .01, lfc = 1, n=20, name = 'edgeR')
finish(deReport)



## Solution Exercise 9.5
makeDESeq2DFmodified<-function(object, countTable, pvalueCutoff, conditions,
    annotation.db, expName, reportDir, numGenes, ...){
   	sigGenes <- which(object$padj < pvalueCutoff)
	if(length(sigGenes) < 1){
		stop("No genes meet the selection criteria. Try changing the p-value cutoff.")
	}
	resSig <- object[sigGenes,]
        ## order object by adjusted p-values and return user specified number of genes only
        resSig <- resSig[order(resSig$padj),]
        resSig <- resSig[1:numGenes,]
        ## Convert log2 fold-change to linear
        resSig$log2FoldChange <- 2^resSig$log2FoldChange
	countTableSig <- countTable[rownames(resSig),]
	eids <- rownames(countTableSig)
	eidsLink <- hwrite(as.character(eids), 
    	link=paste("http://www.ncbi.nlm.nih.gov/gene/", as.character(eids), sep=''), 
    	table=FALSE)
	gnamesAndSymbols <- ReportingTools:::getNamesAndSymbols(eids, annotation.db)
	na.index<-which(is.na(gnamesAndSymbols$symbol)==TRUE)
	gnamesAndSymbols$symbol[na.index]<-gnamesAndSymbols$entrez[na.index]

	imageFiles <- makeDESeq2Figures(countTableSig, conditions,
	 		gnamesAndSymbols$symbol, expName, reportDir)
        images <- hwriteImage(imageFiles, link=imageFiles, table=FALSE, width=50)
    
	ret <- data.frame(
	    eidsLink, 
	    gnamesAndSymbols$symbol, 
	    gnamesAndSymbols$name,
	    images,resSig$baseMean,
	    resSig$log2FoldChange,
	    resSig$pvalue,
	    resSig$padj)
	colnames(ret) <- c("Entrez Id", "Symbol", "Gene Name", "Image", "Mean Counts",
	    "Fold Change", "P-value","Adjusted p-value")
	return(ret)
}

des2Report <- HTMLReport(shortName = 'DESeq2_results_modify_by_functionDF.html',
    	title = 'RNA-seq analysis of differential expression using DESeq2',
    	reportDirectory = './reports')
publish(res, des2Report, countTable = mockRnaSeqData, pvalueCutoff = 0.01,
		conditions = conditions, annotation.db = 'org.Mm.eg.db', expName = 'deseq2',
		numGenes = 20, reportDir = './reports', .modifyDF = makeDESeq2DFmodified)
finish(des2Report)

