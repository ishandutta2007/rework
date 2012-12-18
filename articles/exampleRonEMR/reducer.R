#!/usr/bin/env Rscript
 
trimWhiteSpace <- function(line) gsub("(^ +)|( +$)", "", line)
 
splitLine <- function(line) {
    val <- unlist(strsplit(line, "\t"))
    list(word = val[1], count = as.integer(val[2]))
}

accumulateCounts <- function(line, env) {
    line <- trimWhiteSpace(line)
    split <- splitLine(line)
    word <- split$word
    count <- split$count
    if (!is.na(word) && exists(word, envir = env, inherits = FALSE)) {
        oldcount <- get(word, envir = env)
        assign(word, oldcount + count, envir = env)
    }
    else assign(word, count, envir = env)
}

env <- new.env(hash = TRUE)

con <- file("stdin", open = "r")
while (length(line <- readLines(con, n = 1, warn = FALSE)) > 0) {
  tryCatch(
           accumulateCounts(line=line, env=env),
           error = function(e) { message("Got error ", e, " for line ", line)}
           )
}
close(con)

for (w in ls(env, all = TRUE))
    cat(w, "\t", get(w, envir = env), "\n", sep = "")

