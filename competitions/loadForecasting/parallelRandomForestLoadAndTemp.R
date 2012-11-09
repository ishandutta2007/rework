library(randomForest)
library(testthat)
library(foreach)
library(doMC)

registerDoMC()
getDoParWorkers()

setwd('~/rework/competitions/loadForecasting/data')

trainInputFilename = 'fullLoadAndTempTrainData.csv'
lh = read.csv(trainInputFilename, header=TRUE)
names(lh)
dim(lh)
str(lh)
expect_that(nrow(lh), equals(sum(complete.cases(lh))))

testInputFilename = 'loadAndTempTestData.csv'
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
gc()

#--------------------------------------------------------------------------------------------------------
startTime=date(); startTime

# Note, when passing all zones in, we run out of memory.  Instead building a model per zone plus
# an additional model with most recent data for all zones to catch interactions.  
numRowsForInteraction = 250000
rowsForInteraction=seq(nrow(lh)-numRowsForInteraction, nrow(lh), 1)

interactionRF <- foreach(ntree=rep(250, 4), .combine=combine, .packages='randomForest') %dopar% randomForest(load ~ ., 
                              lh[rowsForInteraction,], 
                              xtest=test[,-1], 
                              keep.forest=TRUE,
                              ntree=ntree)
interactionRF
interactionPredictions <- interactionRF$test$predicted


#-----
stopTime=date(); stopTime
test$load <- round(interactionPredictions)

predictionOutputFile = paste('loadAndTemp200kInteractionRFPredictions', gsub(' ', '_', stopTime), '.csv', sep='')
write.csv(test, file=predictionOutputFile)

rdataOutputFile = paste('loadAndTemp200kInteraction', gsub(' ', '_', stopTime), '.RData', sep='')
save.image(file=rdataOutputFile)

submissionOutputFile = paste('loadAndTemp200kInteractionRFSubmission', gsub(' ', '_', stopTime), '.csv', sep='')
system(paste('python2.7 ~/rework/competitions/loadForecasting/transformLoadOnlyToSubmission.py',
             predictionOutputFile,
             submissionOutputFile))

rm(interactionRF)
gc()

#-----------------------------------------------------------------------------------------------------
buildZoneRF <- function(zone) {
  zoneRF <- randomForest(load ~ ., 
                                  lh[lh$zone_id==zone,], 
                                  xtest=test[test$zone_id==zone,-1], 
                                  keep.forest=TRUE,
                                  ntree=500)
  print(zoneRF)
  stopTime=date(); stopTime
  predictionOutputFile = paste('loadAndTempZone', zone, 'RFPredictions', gsub(' ', '_', stopTime), '.csv', sep='')
  write.csv(zoneRF$test$predicted, file=predictionOutputFile)
  rdataOutputFile = paste('loadAndTempZone', zone, gsub(' ', '_', stopTime), '.RData', sep='')
  save.image(file=rdataOutputFile)
  zoneRF$test$predicted
}

zoneRFResults <- foreach(zone=1:20) %dopar% buildZoneRF(zone) 

stopTime=date(); stopTime

for (zone in 1:20) {
  test$load[test$zone_id==zone] <- round(zoneRFResults[[zone]])
}

predictionOutputFile = paste('loadAndTempRFPredictions', gsub(' ', '_', stopTime), '.csv', sep='')
write.csv(test, file=predictionOutputFile)

submissionOutputFile = paste('loadAndTempRFSubmission', gsub(' ', '_', stopTime), '.csv', sep='')
system(paste('python2.7 ~/rework/competitions/loadForecasting/transformLoadOnlyToSubmission.py',
             predictionOutputFile,
             submissionOutputFile))

rdataOutputFile = paste('loadAndTemp', gsub(' ', '_', stopTime), '.RData', sep='')
save.image(file=rdataOutputFile)

