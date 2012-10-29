library(gputools)
library(testthat)

setwd('~/')

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

scaleNegOneToOne = function(x) { (x-min(x)-((max(x)-min(x))/2))/((max(x)-min(x))/2)}
for(i in 1:ncol(lh)) {
    print(paste("min:", min(lh[,i]), "max:", max(lh[,i])))
    lh[,i] = scaleNegOneToOne(lh[,i])
    print(paste("min:", min(lh[,i]), "max:", max(lh[,i])))
}

for(i in 2:ncol(test)) {
    print(paste("min:", min(test[,i]), "max:", max(test[,i])))
    test[,i] = scaleNegOneToOne(test[,i])
    print(paste("min:", min(test[,i]), "max:", max(test[,i])))
}

startTime=date(); startTime

a <- gpuSvmTrain(as.numeric(lh[,1]), as.matrix(lh[,-1]), isRegression = TRUE, C = 10, kernelWidth = 0.125, eps = 0.5,
                 stoppingCrit = 0.001)
print(a)
b <- gpuSvmPredict(as.matrix(test[,-1]), a$supportVectors, a$svCoefficients, a$svOffset, isRegression = TRUE)
print(str(b))

stopTime=date(); stopTime

rdataOutputFile = paste('loadAndTempInteractionSVM', gsub(' ', '_', stopTime), '.RData', sep='')
save.image(file=rdataOutputFile)

test$load <- round(b)

predictionOutputFile = paste('loadAndTempInteractionSVMPredictions', gsub(' ', '_', stopTime), '.csv', sep='')
write.csv(test, file=predictionOutputFile)


#--------------------------
library(gputools)
# From http://cran.r-project.org/web/packages/gputools/gputools.pdf
# y is discrete: -1 or 1 and we set isRegression to FALSE
y <- round(runif(100, min = 0, max = 1))
for(i in 1:5) { if(y[i] == 0) {y[i] <- -1}}
x <- matrix(runif(500), 100, 5)
a <- gpuSvmTrain(y, x, isRegression = FALSE, C = 10, kernelWidth = 0.125, eps = 0.5, stoppingCrit = 0.001) ;str(a);lapply(a,str)
b <- gpuSvmPredict(x, a$supportVectors, a$svCoefficients, a$svOffset, isRegression = FALSE)
print(b)

# this time around, y : -1 or 1 and we set isRegression to FALSE
y <- runif(100, min = -1, max = 1)
x <- matrix(runif(500), 100, 5)
a <- gpuSvmTrain(y, x, isRegression = TRUE)
print(a)
b <- gpuSvmPredict(x, a$supportVectors, a$svCoefficients, a$svOffset, isRegression = TRUE)
print(b)

rows = seq(2, 100, 2)
rows
y = rep(-1, 100)
y[rows]=1
y
x[,]=0
x
x[rows, 2] = .5
x[rows, 4] = .5
x
