#!/usr/bin/env Rscript
 
normalizeCharacters <- function(line) {
    # Convert all characters to lower case
    line <- tolower(line)
    # Discard punctuation by keeping only whitespace, letters, numbers, and hyphens
    line <- gsub("[^\\sa-z0-9-]", "", line, perl=TRUE)
}

parseWords <- function(line) {
    line <- normalizeCharacters(line)
    # Trim whitespace on each end
    line <- gsub("(^\\s+)|(\\s+$)", "", line, perl=TRUE)
    # Now split the line at whitespace to obtain the individual words
    words <- unlist(strsplit(line, "[\\s]+", perl=TRUE))
}

con <- file("stdin", open = "r")
while (length(line <- readLines(con, n = 1, warn = FALSE)) > 0) {
    words <- parseWords(line)
    for (w in words)
        cat(w, "\t1\n", sep="")
}

close(con)
