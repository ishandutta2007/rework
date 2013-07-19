####Code for BioC workshop on ReportingTools
##July 19 2013 Seattle WA
##Basic ReportingTools

###################################################
### (1) Load ReportingTools
###################################################
##use this code to get devel version:
#source("http://bioconductor.org/biocLite.R")
#library(BiocInstaller)
##useDevel()
#biocLite('ReportingTools')

library(ReportingTools)


###################################################
### (2) Load a iris data frame and publish it
###################################################
data(iris)

htmlRep <- HTMLReport(shortName = 'my_html_file',
    reportDirectory = './reports')
publish(iris, htmlRep)
finish(htmlRep)

##Can also specify the basePath if want to publish to a directory that is not
##downstream of working directory
htmlRep <- HTMLReport(shortName = 'my_html_file', basePath = "~",
                      reportDirectory = 'reports')
publish(iris, htmlRep)
finish(htmlRep)

htmlRep <- HTMLReport(shortName = 'my_html_file', basePath = "~/reports")
publish(iris, htmlRep)
finish(htmlRep)

##all three of the above produce the same report if the working directory
## is "~"

###################################################
### (3) Add a plot and some text
###################################################
##basic plot
plot(iris$Sepal.Length, iris$Sepal.Width, xlab='Sepal Length', 
	ylab='Sepal Width', main='Scatter plot of Iris sepal length and width', col=c("cornflowerblue", "forestgreen")[iris$Species])
basicPlot <- recordPlot()

##lattice plots behave slightly differently
library(lattice)
latticePlot <- xyplot(iris$Sepal.Width~iris$Sepal.Length, 
	col=c("cornflowerblue", "forestgreen", "goldenrod")[iris$Species],
	xlab='Speal Length', 
	ylab='Sepal Width', main='Scatter plot of Iris sepal length and width')  

##make the report	
htmlRep <- HTMLReport(shortName = 'my_html_file_imagetext', title='Adding plots directly to the page',
    reportDirectory = './reports')
##this is how to add text directly to the page
publish('This is a scatterplot', htmlRep)
publish(basicPlot, htmlRep, name = 'scatterPlot')  
publish('This is a scatterplot from lattice', htmlRep)
publish(latticePlot, htmlRep, name = 'latticeScatter')
publish(iris, htmlRep, name='Table')
finish(htmlRep)

###################################################
### (4) The use of 'name'
###################################################
##The name argument is for assigning identifiers to elements within a document
names(htmlRep$.report)
htmlRep$.report$latticeScatter

htmlRep2 <- HTMLReport(shortName = 'my_html_file_imagetext2', title='Adding plots directly to the page with name',
    reportDirectory = './reports')
publish(htmlRep$.report$latticeScatter, htmlRep2)  ##pull the other scatter plot
publish(latticePlot, htmlRep2)
finish(htmlRep2)


###################################################
### (5) Add a pre-made plot, more specialized text and a link to another page
###################################################
png(filename='reports/xyplot.png')
xyplot(iris$Sepal.Width~iris$Sepal.Length, 
	col=c("cornflowerblue", "forestgreen", "goldenrod")[iris$Species],
	xlab='Speal Length', 
	ylab='Sepal Width', main='Scatter plot of Iris sepal length and width')
dev.off()

library(hwriter)

htmlRep <- HTMLReport(shortName = 'my_html_file_hwriter', title='Adding a link, text and image',
    reportDirectory = './reports')
publish(hwrite('This is a link to Bioconductor', link = 'http://www.bioconductor.org'), htmlRep)
publish(c(hwrite("Here",link='http://www.ebi.ac.uk/~gpau/hwriter/',br=FALSE), " is a link to more hwriter techniques"), htmlRep, para=FALSE)
publish(hwrite('Scatterplot of results', heading=3), htmlRep)
himg <- hwriteImage('xyplot.png', link='xyplot.png', width=100)
publish(hwrite(himg, br=TRUE), htmlRep)
publish(hwrite('Table of data', heading=3, style='color:red'), htmlRep)
publish(iris, htmlRep)
finish(htmlRep)


###################################################
### (6) Publishing multiple tables
###################################################
htmlRep <- HTMLReport(shortName = 'my_html_file_manytables', title = 'Many tables on one page',
                       reportDirectory = './reports')
publish(iris[iris$Species=="virginica",], htmlRep, name = 'Df1')
publish(iris[iris$Species=="versicolor",], htmlRep, name = 'Df3')
publish(iris[iris$Species=="setosa",], htmlRep, name = 'Df2', pos=2)
finish(htmlRep)



###################################################
### (7) Modify data frames upon publication
###################################################
##this function rounds the value of iris$Petal.Length to a specified number of digits
roundLength<- function(object, numdigits, ...){
	object$roundedPetalLength <- signif(object$Petal.Length, numdigits)
	return(object)
}

##this function replaces the scatter plot images with new plots
makeImages<-function(object,...){
  if (!file.exists('reports/images')){
    dir.create('reports/images')    
  }
	imagename <- c()
	for (i in 1:nrow(object)){
		imagename[i] <- paste0('images/plotNew', i, '.png')
		png(filename = paste0('reports/', imagename[i]))
		plot(object$Petal.Length[i], ylab = 'Petal Length', xlab = object$Species[i],
                     main = 'Petal Length Plot', col = 'red', pch = 15, cex=3)
		dev.off()
	}
	object$Image <- hwriteImage(imagename, link = imagename, table = FALSE, height=150, width=150)
	return(object)
}

##This function links species column to wiki
addSpeciesLink <- function(object, ...){
	object$Species <- hwrite(as.character(object$Species), 
                              link = paste0('http://en.wikipedia.org/wiki/Iris_',
                                as.character(object$Species)), table = FALSE)
	return(object)
}

##This function reorganizes the df
cleanUpDF <- function(object, ...){
	object <- object[, c("Species", "Petal.Length", "roundedPetalLength", "Image")]
	colnames(object) <- c("Species", "Petal Length", "Rounded Petal Length", "Image of Petal Length") 
	return(object)
}

htmlRep <- HTMLReport(shortName = 'my_html_file_modify', 
                       title = 'Manipulating the data frame directly before publishing',
                       reportDirectory = './reports')
publish(iris, htmlRep, numdigits = 1, 
        .modifyDF = list(roundLength, makeImages, addSpeciesLink, cleanUpDF))
finish(htmlRep)



###################################################
### (8) Publishing to CSV files
###################################################
csvFile <- CSVFile(shortName = 'my_csv_file', 
    reportDirectory = './reports')
publish(iris, csvFile)