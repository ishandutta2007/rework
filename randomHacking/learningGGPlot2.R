# ggplot2 newbie script
# code inspired by the book "ggplot2 Elegant Graphics for Data Analysis" by Hadley Wickham
library(ggplot2)

set.seed(1410)
dsmall = diamonds[sample(nrow(diamonds), 100),]

qplot(carat, price, data=diamonds)
qplot(log(carat), log(price), data=diamonds)
qplot(carat, x*y*z, data=diamonds)
qplot(carat, price, data=dsmall, color=color)
qplot(carat, price, data=dsmall, shape=cut)
qplot(carat, price, data=diamonds, alpha=I(1/10))
qplot(carat, price, data=diamonds, alpha=I(1/100))
qplot(carat, price, data=diamonds, alpha=I(1/200))
# color and shape work well for categorical values, size works well for continuous variables 
qplot(carat, price, data=dsmall, geom=c("point", "smooth"))
qplot(carat, price, data=diamonds, geom=c("point", "smooth"))
qplot(carat, price, data=dsmall, geom=c("point", "smooth"), span=0.2)
qplot(carat, price, data=dsmall, geom=c("point", "smooth"), span=1)

library(mgcv)
qplot(carat, price, data=dsmall, geom=c("point", "smooth"), method="gam", formula=y~s(x))
qplot(carat, price, data=dsmall, geom=c("point", "smooth"), method="gam", formula=y~s(x, bs="cs"))

library(splines)
qplot(carat, price, data=dsmall, geom=c("point", "smooth"), method="lm")
qplot(carat, price, data=dsmall, geom=c("point", "smooth"), method="lm", formula=y~ns(x,5))

library(MASS)
qplot(carat, price, data=dsmall, geom=c("point", "smooth"), method="rlm")

qplot(color, price/carat, data=diamonds, geom="boxplot")
qplot(color, price/carat, data=diamonds, geom="jitter", alpha=I(1/5))
qplot(color, price/carat, data=diamonds, geom="jitter", alpha=I(1/50))
qplot(color, price/carat, data=diamonds, geom="jitter", alpha=I(1/200))

qplot(carat, data=diamonds, geom="histogram")
qplot(carat, data=diamonds, geom="density")
qplot(carat, data=diamonds, geom="histogram", binwidth=1, xlim=c(0,3))
qplot(carat, data=diamonds, geom="histogram", binwidth=0.1, xlim=c(0,3))
qplot(carat, data=diamonds, geom="histogram", binwidth=0.01, xlim=c(0,3))
qplot(carat, data=diamonds, geom="histogram", fill=color)
qplot(carat, data=diamonds, geom="density", color=color)

qplot(color, data=diamonds, geom="bar")
qplot(color, data=diamonds, geom="bar", weight=carat) + scale_y_continuous("carat")

qplot(date, unemploy/pop, data=economics, geom="line")
qplot(date, uempmed, data=economics, geom="line")

str(economics)

df <- data.frame(
    x=c(3,1,5),
    y=c(2,4,6),
    label=c("a","b","c"))
p <- ggplot(df, aes(x, y, label=label)) + xlab(NULL) + ylab(NULL)
p + geom_point()
p + geom_point() + geom_text()
p + geom_text()
p + geom_bar(stat="identity")

library(maps)
data(us.cities)
big_cities <- subset(us.cities, pop > 500000)
map <- qplot(long, lat, data=big_cities)
map + borders("state", size=0.5)