#!/usr/bin/env Rscript
 
trimWhiteSpace <- function(line) gsub("(^ +)|( +$)", "", line)
splitIntoWords <- function(line) unlist(strsplit(line, "[[:space:]]+"))
     
con <- file("stdin", open = "r")
while (length(line <- readLines(con, n = 1, warn = FALSE)) > 0) {
    line <- trimWhiteSpace(line)
    words <- splitIntoWords(line)
    ## can also be done as cat(paste(words, "\t1\n", sep=""), sep="")
    for (w in words)
        cat(w, "\t1\n", sep="")
}

## Add the R version to the output as a sanity check to see what all the mappers are running
cat(R.version.string, "\t1\n", sep="")

close(con)
