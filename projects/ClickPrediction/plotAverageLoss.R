
path = "/Users/deflaux/rework/projects/ClickPrediction/"
steps = c(0.001, 0.01, 0.05)
suffix = "output.csv"
for step in steps {
    averageLosses = read.table(paste(path, step, suffix, sep=""), header=F)
    head(averageLosses)
    plot(averageLosses$V1 ~ seq(1, nrow(averageLosses)))
    plot(log(averageLosses$V1) ~ seq(1, nrow(averageLosses)))
}

# P(y=1)
curve(1/(1 + exp(-1*(5 + 3*x))), from = -5, to = 5)
curve(exp(5 + 3*x)/(1 + exp(5 + 3*x)), from = -5, to = 5)

# versus

# P(y=-1)
curve(exp(-1*(5 + 3*x))/(1 + exp(-1*(5 + 3*x))), from = -5, to = 5)
curve(1/(1 + exp(5 + 3*x)), from = -5, to = 5)