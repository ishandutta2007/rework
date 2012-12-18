#!/usr/bin/env Rscript
 
trimWhiteSpace <- function(line) gsub("(^ +)|( +$)", "", line)
splitIntoWords <- function(line) unlist(strsplit(line, "[[:space:]]+"))
     
con <- file("stdin", open = "r")
while (length(line <- readLines(con, n = 1, warn = FALSE)) > 0) {
    line <- trimWhiteSpace(line)
    words <- splitIntoWords(line)
    cat(paste(words, "\t1\n", sep=""), sep="")
}

close(con)
