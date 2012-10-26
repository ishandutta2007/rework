library(randomForest)
library(testthat)

setwd('~/rework/competitions/loadForecasting/data')

system('python2.7 ~/rework/competitions/loadForecasting/transformForLoadOnlyML.py')
lh = read.csv('Load_history_for_ML.csv', header=TRUE)
names(lh)
dim(lh)
str(lh)
expect_that(nrow(lh), equals(sum(complete.cases(lh))))

system('python2.7 ~/rework/competitions/loadForecasting/createTestDataLoadOnly.py')
test = read.csv('testData_for_LoadOnly.csv', header=TRUE)
names(test)
dim(test)
str(test)


# TODO consider separate models per zone
# TODO can split into train/test since rf can't use all the training data


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
zonePredictions <- vector(mode = "numeric", length = length(interactionPredictions))
for (zone in 1:20) {
  zoneRFs[[zone]] <- randomForest(load ~ ., 
                                  lh[lh$zone_id==zone,], 
                                  xtest=test[test$zone_id==zone,-1], 
                                  ntree=500)
  zoneRFs[[zone]]
  zonePredictions[test$zone_id==zone] <- zoneRFs[[zone]]$test$predicted
}

stopTime=date(); stopTime


test$load <- round((interactionPredictions + zonePredictions)/2)
write.csv(test, file="20121025_rf_load_only.csv")

system('python2.7 ~/rework/competitions/loadForecasting/transformLoadOnlyToSubmission.py 20121025_rf_load_only.csv 20121025_rf_load_only_submission.csv')


save.image(file="20121025_rf_load_only.RData")
