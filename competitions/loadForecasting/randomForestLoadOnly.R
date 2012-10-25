library(randomForest)
library(testthat)

setwd('~/rework/competitions/loadForecasting/data')

system('python2.7 ~/rework/competitions/loadForecasting/transformForLoadOnlyML.py')
lh = read.csv('Load_history_for_ML.csv', header=TRUE)
names(lh)
dim(lh)
str(lh)
expect_that(nrow(lh), equals(sum(complete.cases(lh))))

# TODO consider separate models per zone
# TODO can split into train/test since rf can't use all the training data


startTime=date(); startTime

# Note, when passing all zones in, we run out of memory.  Instead building a model per zone plus
# an additional model with most recent data for all zones to catch interactions.  
numRowsForInteraction = 100000
rowsForInteraction=seq(nrow(lh)-numRowsForInteraction, nrow(lh), 1)
rfInteraction <- randomForest(load ~ ., lh[rowsForInteraction,], ntree=500)
rfInteraction

zoneRFs <- list()
for (zone in 1:20) {
  zoneRFs[[zone]] <- randomForest(load ~ ., lh[lh$zone_id==zone,], ntree=2000)
  zoneRFs[[zone]]
}

stopTime=date(); stopTime


# predictions <- levels(labels)[rf$test$predicted]
# write(predictions, file="20121004_rf_benchmark.csv", ncolumns=1)

save.image(file="20121025_rf_load_only.RData")
