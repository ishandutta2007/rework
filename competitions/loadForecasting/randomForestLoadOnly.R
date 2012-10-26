library(randomForest)
library(testthat)

setwd('~/rework/competitions/loadForecasting/data')

trainInputFilename = 'loadOnlyTrainData.csv'
system(paste('python2.7 ~/rework/competitions/loadForecasting/transformForLoadOnlyML.py', trainInputFilename))
lh = read.csv(trainInputFilename, header=TRUE)
names(lh)
dim(lh)
str(lh)
expect_that(nrow(lh), equals(sum(complete.cases(lh))))

testInputFilename = 'loadOnlyTestData.csv'
system(paste('python2.7 ~/rework/competitions/loadForecasting/createTestDataLoadOnly.py', testInputFilename))
test = read.csv(testInputFilename, header=TRUE)
names(test)
dim(test)
str(test)

startTime=date(); startTime

# Note, when passing all zones in, we run out of memory.  Instead building a model per zone plus
# an additional model with most recent data for all zones to catch interactions.  
numRowsForInteraction = 100000
rowsForInteraction=seq(nrow(lh)-numRowsForInteraction, nrow(lh), 1)
interactionRF <- randomForest(load ~ ., 
                              lh[rowsForInteraction,], 
                              xtest=test[,-1], 
                              ntree=500)
interactionRF
interactionPredictions <- interactionRF$test$predicted

zoneRFs <- list()
zonePredictions <- vector(mode = 'numeric', length = length(interactionPredictions))
for (zone in 1:20) {
  zoneRFs[[zone]] <- randomForest(load ~ ., 
                                  lh[lh$zone_id==zone,], 
                                  xtest=test[test$zone_id==zone,-1], 
                                  ntree=2000)
  print(zoneRFs[[zone]])
  zonePredictions[test$zone_id==zone] <- zoneRFs[[zone]]$test$predicted
}

stopTime=date(); stopTime

# Take the mean of the interaction prediction and zone prediction for each case
test$load <- round((interactionPredictions + zonePredictions)/2)

predictionOutputFile = paste('loadOnlyRFPredictions', gsub(' ', '_', stopTime), '.csv', sep='')
write.csv(test, file=predictionOutputFile)

submissionOutputFile = paste('loadOnlyRFSubmission', gsub(' ', '_', stopTime), '.csv', sep='')
system(paste('python2.7 ~/rework/competitions/loadForecasting/transformLoadOnlyToSubmission.py',
             predictionOutputFile,
             submissionOutputFile))

rdataOutputFile = paste('loadOnly', gsub(' ', '_', stopTime), '.RData', sep='')
save.image(file=rdataOutputFile)
