library(ggplot2)
path = "/Users/deflaux/rework/projects/ClickPrediction/"
suffix = "output.csv"
plotStep <- function(step) {
    averageLosses = read.table(paste(path, step, suffix, sep=""), header=F)
    head(averageLosses)
    qplot(seq(1, nrow(averageLosses)), averageLosses$V1, 
          geom=c("point", "smooth"), alpha=I(1/50),
          main=paste("Average Losses for step size", step))
}

plotStep('0.0010')
plotStep('0.01')
plotStep('0.05')


# P(y=1)
curve(1/(1 + exp(-1*(5 + 3*x))), from = -5, to = 5)
curve(exp(5 + 3*x)/(1 + exp(5 + 3*x)), from = -5, to = 5)

# versus

# P(y=-1)
curve(exp(-1*(5 + 3*x))/(1 + exp(-1*(5 + 3*x))), from = -5, to = 5)
curve(1/(1 + exp(5 + 3*x)), from = -5, to = 5)

# create a data frame from scratch 
x1 <- c(2,3,3,3,4,4,6,6,6,7,7,8,8)
x2 <- c(6,1,5,6,6,7,2,3,5,2,3,2,3)
y <- c(1,1,1,1,1,1,0,0,0,0,0,0,0)
data <- data.frame(y,x1,x2)
library(ggplot2)
plot <- qplot(x1, x2, data=data, shape=as.factor(y))
plot 
model = lm( data$y ~ data$x1 + data$x2)
model
w0 = model$coefficients[1]
w1 = model$coefficients[2]
w2 = model$coefficients[3]
# predictions (rounding to convert regression to classification)
round(model$fitted.values)

# number of classification errors on training set
nrow(data) - sum(y == round(w0 + w1*x1 + w2*x2))
plot + geom_vline(xintercept=5) + ggtitle("1.2 (a)")

# number of classification errors with w0==0
nrow(data) - sum(y == round(0 + w1*x1 + w2*x2))
# this is wrong, really need to 3d plot
plot + geom_smooth((0 + w1*x1 + w2*x2)) + ggtitle("1.2 (b)")

nrow(data) - sum(y == round(w0   +  0*x1  +    w2*x2))
nrow(data) - sum(y == round(w0   +  w1*x1  +    0*x2))

y
round(w0 + w1*x1 + w2*x2)
round(0 + w1*x1 + w2*x2)
round(w0   +  0*x1  +    w2*x2)
round(w0   +  w1*x1  +    0*x2)