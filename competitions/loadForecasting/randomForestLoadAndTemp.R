library(randomForest)
library(testthat)

setwd('~/rework/competitions/loadForecasting/data')

trainInputFilename = 'loadAndTempTrainData.csv'
system(paste('python2.7 ~/rework/competitions/loadForecasting/transformForLoadAndTempML.py', trainInputFilename))
lh = read.csv(trainInputFilename, header=TRUE)
names(lh)
dim(lh)
str(lh)
expect_that(nrow(lh), equals(sum(complete.cases(lh))))

testInputFilename = 'loadAndTempTestData.csv'
system(paste('python2.7 ~/rework/competitions/loadForecasting/createTestDataLoadAndTemp.py', testInputFilename))
test = read.csv(testInputFilename, header=TRUE)
names(test)
dim(test)
str(test)
expect_that(nrow(test), equals(sum(complete.cases(test))))

#--------------------------------------------------------------------------------------------------------
# Add a few tests to check that the data was transformed correctly by the python scripts
lhOrig <- read.csv('Load_history.csv', na.strings = '')
lhOrig = na.omit(lhOrig)
expect_that(nrow(lh), equals(nrow(lhOrig)*24 + 6*20), 
            info="there are some load values for the partial day of 2008/06/30 h1-h6 inclusive")
expect_that(lh[lh$zone_id == 2 & lh$year == 2004 & lh$day_of_year == 166 & lh$hour_of_day == 11, 'load'], 
            equals(202770))
expect_that(as.character(lhOrig[lhOrig$zone_id == 2 & lhOrig$year == 2004 & lhOrig$month == 6 & lhOrig$day == 14, 'h12']),
            equals('202,770'))
rm(lhOrig)

thOrig = read.csv('temperature_history.csv', header=TRUE)
names(thOrig)
dim(thOrig)
str(thOrig)
expect_that(lh[lh$zone_id == 2 & lh$year == 2004 & lh$day_of_year == 166 & lh$hour_of_day == 11, 't9'], 
            equals(thOrig[thOrig$station_id == 9 & thOrig$year == 2004 & thOrig$month == 6 & thOrig$day == 14, 'h12']))

rm(thOrig)

#--------------------------------------------------------------------------------------------------------
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

predictionOutputFile = paste('loadAndTempRFPredictions', gsub(' ', '_', stopTime), '.csv', sep='')
write.csv(test, file=predictionOutputFile)

submissionOutputFile = paste('loadAndTempRFSubmission', gsub(' ', '_', stopTime), '.csv', sep='')
system(paste('python2.7 ~/rework/competitions/loadForecasting/transformloadOnlyToSubmission.py',
             predictionOutputFile,
             submissionOutputFile))

rdataOutputFile = paste('loadAndTemp', gsub(' ', '_', stopTime), '.RData', sep='')
save.image(file=rdataOutputFile)

#-----

test$load <- round(interactionPredictions)

predictionOutputFile = paste('loadAndTempInteractionRFPredictions', gsub(' ', '_', stopTime), '.csv', sep='')
write.csv(test, file=predictionOutputFile)

submissionOutputFile = paste('loadAndTempInteractionRFSubmission', gsub(' ', '_', stopTime), '.csv', sep='')
system(paste('python2.7 ~/rework/competitions/loadForecasting/transformLoadOnlyToSubmission.py',
             predictionOutputFile,
             submissionOutputFile))

rdataOutputFile = paste('loadAndTempInteraction', gsub(' ', '_', stopTime), '.RData', sep='')
save.image(file=rdataOutputFile)