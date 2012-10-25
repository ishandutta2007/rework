library(randomForest)
library(testthat)

setwd('~/rework/competitions/loadForecasting/data')

system('python2.7 ~/rework/competitions/loadForecasting/transformForLoadOnlyML.py')
lh = read.csv('Load_history_for_ML.csv', header=TRUE)
names(lh)
dim(lh)
str(lh)
expect_that(nrow(lh), equals(sum(complete.cases(lh))))

labels <- lh[,1]
train <- lh[,-1]

startTime=date(); startTime
rf <- randomForest(train, labels, xtest=test, ntree=2000)
stopTime=date(); stopTime
rf

# predictions <- levels(labels)[rf$test$predicted]
# write(predictions, file="20121004_rf_benchmark.csv", ncolumns=1)

save.image(file="20121025_rf_load_only.RData")
