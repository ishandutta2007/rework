#!/usr/bin/env Rscript
 
parseKeyValuePair <- function(line) {
    # Trim whitespace on each end
    line <- gsub("(^\\s+)|(\\s+$)", "", line, perl=TRUE)
    # Skip malformed lines
    if(!grepl("\t", line)) {
      warning("Skipping malformed line: ", line)
      return(NULL)
    }
    keyValuePair <- unlist(strsplit(line, "\t"))
    list(key = keyValuePair[1], value = as.integer(keyValuePair[2]))
}

accumulateCounts <- function(line, env) {
    keyValuePair <- parseKeyValuePair(line)
    if(is.null(keyValuePair)) return()
    word <- keyValuePair$key
    count <- keyValuePair$value
    if (exists(word, envir = env, inherits = FALSE)) {
        oldcount <- get(word, envir = env)
        assign(word, oldcount + count, envir = env)
    }
    else assign(word, count, envir = env)
}

env <- new.env(hash = TRUE)

con <- file("stdin", open = "r")
while (length(line <- readLines(con, n = 1, warn = FALSE)) > 0) {
    accumulateCounts(line=line, env=env)
}
close(con)

for (w in ls(env, all = TRUE))
    cat(w, "\t", get(w, envir = env), "\n", sep = "")

