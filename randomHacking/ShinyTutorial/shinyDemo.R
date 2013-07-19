# From Shiny tutorial at BioC 2013

install.packages("devtools")
library(devtools)
install_github("shiny","rstudio")
source("http://bit.ly/DLBIOC")
# now have two objects in R environment
tumor
normal
library(shiny)
options(shiny.launch.browser=FALSE) # needed with BioC AMI only
exprs(tumor)
boxplot(list(Tumore=exprs(tumor), Normal=exprs(normal)))
boxplot(list(Tumore=exprs(tumor)["KRAS",], Normal=exprs(normal)["KRAS",]))
# see ui.R and server.R for how to make this interactive