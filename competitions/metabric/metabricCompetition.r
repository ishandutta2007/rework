### R code from vignette source 'metabricCompetitionDemo.Rnw'

###################################################
### code chunk number 1: loadLibraries
###################################################
library(predictiveModeling)
library(BCC)
library(survival)
library(MASS)


###################################################
### code chunk number 2: loadData
###################################################
## Before downloading data from the R client you must sign the terms of use through the web client.
## This is done by attempting to download any dataset included in the competition and agreeing to the terms of use.
## For example, clicking the "Download" link at https://synapse.sagebase.org/\#Synapse:syn375502 will bring up
## the terms of use dialogue if you have not already agreed to them.

synapseLogin() ### not required if configured for automatic login
trainingData <- loadMetabricTrainingData()
names(trainingData)
lapply(trainingData, dim)
lapply(trainingData, class)


as.factor(trainingData$clinicalFeaturesData$Treatment)

formatData <- function(exprData, copyData, clinicalFeaturesData,clinicalSurvData,...) {
  if(class(clinicalSurvData) != "Surv"){
    stop("Expecting 'responseData' object of type 'Surv'")
  }
  
# Extract the expression data
exprData.exprs <- exprs(exprData)
copyData.exprs <- exprs(copyData)          
rowsWithoutNas <- rowSums(is.na(copyData.exprs)) == 0
copyData.exprs <- copyData.exprs[rowsWithoutNas,]

# Calculate the Genomic Instability Index
GII <- colSums(copyData.exprs < -1 | copyData.exprs > 1) / sum(rowsWithoutNas)
survTime <- clinicalSurvData[,1]
survStatus <- clinicalSurvData[,2]          
  
# Merge the new features with the clinical covariates
featureData <- cbind(survStatus, clinicalFeaturesData[,1:12],GII,t(copyData.exprs),t(exprData.exprs))

return(featureData)
}

fd <- formatData(exprData=trainingData$exprData, 
                 copyData=trainingData$copyData,
                 clinicalFeaturesData=trainingData$clinicalFeaturesData, 
                 clinicalSurvData=trainingData$clinicalSurvData)
dim(fd)
head(rownames(fd))


write.csv(fd, "metabric-20121010.csv", row.names=T, col.names=T)
write.csv(fd[,1], "metabric-outcome2-20121010.csv", row.names=T, col.names=T)

cols = c(seq(1,13,1), seq(14, 68000, 100))
write.csv(fd[,cols], "metabric-small-20121010.csv", row.names=F, col.names=F)


mediumCols = c(seq(1,13,1), seq(14, 68000, 10))
write.csv(fd[,mediumCols], "metabric-medium-20121010.csv", row.names=F, col.names=F)



###################################################
### code chunk number 3: defineModelClassFile
###################################################
modelClassFile <- "~/BCC/R/DemoModel.R"
source(modelClassFile)


###################################################
### code chunk number 4: trainModel
###################################################
demoPredictiveModel <- DemoModel$new()
### class can be instantiated directly from source file with the syntax:
### demoPredictiveModel <- source(modelClassFile)$value$new()
demoPredictiveModel$customTrain(trainingData$exprData, trainingData$copyData,
                                trainingData$clinicalFeaturesData, trainingData$clinicalSurvData)
trainPredictions <- demoPredictiveModel$customPredict(trainingData$exprData, trainingData$copyData,
                                                      trainingData$clinicalFeaturesData)


###################################################
### code chunk number 5: computeTrainCIndex
###################################################
trainPerformance <- SurvivalModelPerformance$new(trainPredictions, trainingData$clinicalSurvData)
print(trainPerformance$getExactConcordanceIndex())


###################################################
### code chunk number 6: runModelOnTestDataset
###################################################
### Note that the test data used here is synthetic 'unit test' clinical, 
### expression, and copy numnber data that include missing values and aggregate 
### distributions similar to the real validation data. Obviously, the 
### predictions generated from these data are meaningless, but they show that 
### the mechamics of these submissions and testing work.

testData <- loadMetabricTestData(loadSurvData=FALSE)
testPredictions <- demoPredictiveModel$customPredict(testData$exprData, testData$copyData,
                                                     testData$clinicalFeaturesData)


###################################################
### code chunk number 7: submitModel
###################################################
myModelName = "Erhan demo model" #change this name to something unique
submitCompetitionModel(modelName = myModelName, trainedModel=demoPredictiveModel,
                       rFiles=c(modelClassFile))
onWeb("syn1125643")
## model results will display automatically at http://validation.bcc.sagebase.org/bcc-leaderboard-public.php


###################################################
### code chunk number 8: crossValidateModel
###################################################
cvPerformance <- crossValidatePredictiveSurvivalModel(DemoModel$new(), trainingData$exprData,
                                                      trainingData$copyData,
                                                      trainingData$clinicalFeaturesData,
                                                      trainingData$clinicalSurvData, numFolds = 3)


###################################################
### code chunk number 9: computeCvPerformance
###################################################
cvPerformance$trainPerformanceCV$getFoldCIndices()
cvPerformance$testPerformanceCV$getFoldCIndices()


