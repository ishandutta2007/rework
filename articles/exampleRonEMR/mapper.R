#!/usr/bin/env Rscript
 
normalizeCharacters <- function(line) {
  # Trim whitespace on each end
  line <- gsub("(^ +)|( +$)", "", line)
  # Convert all characters to lower case
  line <- tolower(line)
  # Discard punctuation by keeping only letters, numbers and hyphens
  line <- gsub("[^a-z0-9-]", "", line)
}
splitIntoWords <- function(line) unlist(strsplit(line, "[[:space:]]+"))
     
con <- file("stdin", open = "r")
while (length(line <- readLines(con, n = 1, warn = FALSE)) > 0) {
    line <- normalizeCharacters(line)
    words <- splitIntoWords(line)
    cat(paste(words, "\t1\n", sep=""), sep="")
}

close(con)
