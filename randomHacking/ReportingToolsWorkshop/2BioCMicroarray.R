####Code for BioC workshop on ReportingTools
##July 19 2013 Seattle WA
##Microarray ReportingTools


###################################################
###(1) load ALL data and filter
###################################################
library(ReportingTools)
library(ALL)
library(hgu95av2.db)
library(genefilter)
library(hwriter)
data(ALL)

ALL <- ALL[,!is.na(ALL$sex)]
ALL <- featureFilter(ALL)
ALL  ##is an eset


###################################################
###(2) Run linear model testing for male/female difference
###################################################

library(limma)  ###need limma ≥3.17.5

model <- model.matrix(~sex, ALL)
fit <- eBayes(lmFit(ALL, model))


###################################################
###(3) Make the DE report
###################################################
library(lattice)
rep.theme <- reporting.theme()
lattice.options(default.theme = rep.theme)

head(topTable(fit, coef=2))

deReport <- HTMLReport(shortName = 'de_analysis',
                       title = 'Differential expression analysis of ALL data (Male vs Female)',
                       reportDirectory = './reports')
publish(fit, deReport, eSet=ALL, factor=ALL$sex, coef=2)
finish(deReport)

###################################################
###(4) Select the top 100 DE genes (no plots)
###################################################
##can change the defaults for a different set of genes:
##lfc = 0, n = 1000, pvalueCutoff = 0.01, adjust.method = 'BH'
##make.plots = TRUE
deReport2 <- HTMLReport(shortName = 'de_analysis2',
                       title = 'Differential expression analysis of ALL data (Male vs Female), top 100 genes',
                       reportDirectory = './reports')
publish(fit, deReport2, eSet=ALL, factor=ALL$sex, coef=2, pvalueCutoff = 1, n=100, make.plots = FALSE)
finish(deReport2)



###################################################
###(5) Make the DE report with new images
###################################################
makeNewImages2Factors <- function(df, eSet, factor, ...){
	imagename <- c()
	for (i in 1:nrow(df)){
		probeId <- df$ProbeId[i]
		imagename[i] <- paste0('plot', probeId, '.png')
		png(filename = paste0('./reports/figuresde_analysis/', 
                      imagename[i]))
		print(stripplot(exprs(eSet)[probeId,]~factor|ALL$mol.biol, groups = factor))
		dev.off()
	}
	df$Image <- hwriteImage(paste0('figuresde_analysis/', imagename), 
                                link=paste0('figuresde_analysis/', imagename), 
                                table=FALSE, width=100)
	return(df)
}

deReport3 <- HTMLReport(shortName='de_analysis3',
                        title = 'Differential expression analysis of ALL data (Male vs Female) with new plots',
                        reportDirectory = './reports')
publish(fit, deReport3, eSet = ALL, factor = ALL$sex,  coef = 2, pvalueCutoff = 1,
        n=25,  .modifyDF = makeNewImages2Factors)
finish(deReport3)


###################################################
###(6) Make the DE report with entrez Id links
###################################################
addLinks <- function(df,...){
	df$EntrezId <- hwrite(as.character(df$EntrezId), 
                              link = paste0('http://www.ncbi.nlm.nih.gov/gene/',
                              as.character(df$EntrezId)), table = FALSE)
	return(df)
}

deReport4 <- HTMLReport(shortName='de_analysis4',
                        title = 'Differential expression analysis of ALL data (Male vs Female) with entrez id links',
                        reportDirectory = './reports')
publish(fit, deReport4, eSet = ALL, factor = ALL$mol.biol, coef=2, pvalueCutoff = 1,
        n=25, make.plots = FALSE, .modifyDF=addLinks)
finish(deReport4)



###################################################
###(7) Run GO analysis and generate report
###################################################
library(GOstats)
tt <- topTable(fit, coef = 2, n = 100)
head(tt)
selectedIDs <- unlist(mget(rownames(tt), hgu95av2ENTREZID))
universeIDs <- unlist(mget(featureNames(ALL), hgu95av2ENTREZID))
goParams <- new('GOHyperGParams', 
     geneIds = selectedIDs, 
     universeGeneIds = universeIDs, 
     annotation = annotation(ALL), 
     ontology = 'BP', 
     pvalueCutoff = 0.01,
     conditional = TRUE, 
     testDirection = 'over')
goResults <- hyperGTest(goParams)

goReport <- HTMLReport(shortName = 'go_analysis',
                       title = 'GO analysis of ALL data (Male vs Female)',
                       reportDirectory = './reports')
publish(goResults, goReport, selectedIDs = selectedIDs, 
		annotation.db = 'org.Hs.eg')
finish(goReport)

##Can play with defaults for more refined results:
#pvalueCutoff = 0.01, categorySize = 10


###################################################
###(8) Run PFAM analysis and generate report
###################################################
library(Category)
pfamParams <- new('PFAMHyperGParams', 
     geneIds = selectedIDs, 
     universeGeneIds = universeIDs, 
     annotation = annotation(ALL),  
     pvalueCutoff = 0.01,
     testDirection = 'over')
PFAMResults <- hyperGTest(pfamParams)

PFAMReport <- HTMLReport(shortName = 'pfam_analysis',
                         title = 'PFAM analysis',
                         reportDirectory = './reports')
publish(PFAMResults, PFAMReport, selectedIDs = selectedIDs, 
        annotation.db = 'org.Hs.eg', categorySize = 3)
finish(PFAMReport)


###################################################
### (9) Publish GeneSetCollections
###################################################
library(GSEAlm)
library(GSEABase)
mapped_genes <- mappedkeys(org.Hs.egSYMBOL)
eidsAndSymbols <- as.list(org.Hs.egSYMBOL[mapped_genes])
geneEids<-names(eidsAndSymbols)

set.seed(123)
set1<-GeneSet(geneIds=sample(geneEids,100, replace=FALSE), setName='set1', 
 	shortDescription='This is set1')
set2<-GeneSet(geneIds=sample(geneEids,10, replace=FALSE), setName='set2',  
 	shortDescription='This is set2')
set3<-GeneSet(geneIds=sample(geneEids,37, replace=FALSE), setName='set3',  
 	shortDescription='This is set3')
set4<-GeneSet(geneIds=sample(geneEids,300, replace=FALSE), setName='set4', 
 	shortDescription='This is set4')
geneSets<-GeneSetCollection(c(set1,set2,set3,set4))

geneSetsReport <- HTMLReport(shortName = 'gene_sets',
                             title = 'Gene Sets', 
                             reportDirectory = './reports')
publish(geneSets, geneSetsReport, annotation.db = 'org.Hs.eg')
finish(geneSetsReport)


###################################################
### (10) Publish GeneSetCollections with GSEA statistics
###################################################
mat <- matrix(data=0, ncol=length(universeIDs),nrow=length(geneSets))
for(i in 1:length(geneSets)){
 	geneIdEntrez<-unlist(geneIds(geneSets[[i]]))
 	mat[i,match(geneIdEntrez, universeIDs)] <- 1
}
colnames(mat) <- universeIDs
rownames(mat) <- sapply(geneSets, function(x) x@setName)

### Run GSEA (eval = FALSE)
lm <- lmPerGene(ALL, ~sex, na.rm=TRUE)
GSNorm <- GSNormalize(lm$tstat[2,], mat)
#one-sided p-values
pVals <- gsealmPerm(ALL,~sex, mat, nperm=100)  
bestPval <- apply(pVals, 1, min)

###make the GSEA report and add plot
addPvalPlot <- function(df, setStats, ...){
	pvalImage <- c()
	for (i in 1:nrow(df)){
		pvalImage[i] <- paste0('pvalPlot', i, '.png')
		png(filename = paste0('./reports/', pvalImage[i]))
		plot(function(x) dnorm(x),  main = paste0("Standard Normal Distribution"), xlim=c(-5,5), xlab="", ylab="Density")
		  abline(v=setStats[i], col="red")
	 	dev.off()
	}
	df$Image <- hwriteImage(pvalImage, 
                                link= pvalImage, 
                                table=FALSE, width=100)
	return(df)
}
gseaReport <- HTMLReport(shortName = 'gsea_analysis',
                         title = 'GSEA analysis', 
                         reportDirectory = './reports')
publish(geneSets, gseaReport, annotation.db = 'org.Hs.eg', 
        setStats = GSNorm, setPValues = bestPval, .modifyDF=addPvalPlot)
finish(gseaReport)


###################################################
###(11) Make an index page 
###################################################
indexPage <- HTMLReport(shortName = 'indexMicroarray',
                        title = 'Analysis of ALL Gene Expression',
                        reportDirectory = './reports')
publish(Link(list(deReport, goReport), report = indexPage), indexPage)
publish(Link(PFAMReport, report = indexPage), indexPage)
publish(Link('GSEA report has a new title', 'gsea_analysis.html'), indexPage)
finish(indexPage)

