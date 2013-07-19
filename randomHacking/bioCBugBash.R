# from http://www.bioconductor.org/help/course-materials/2013/BioC2013/developer-day-debug/

# http://bioconductor.org/help/course-materials/2013/BioC2013/developer-day-debug.md
# http://bioconductor.org/help/course-materials/2013/BioC2013/developer-day-debug_full.md

## Example
isIn <- function(x, y) {
  sel <- match(x, y)
  y[sel]
}

## Expected
x <- sample(LETTERS, 5)
isIn(x, LETTERS)

## Bug!
isIn(c(x, "a"), LETTERS)

## updated function
isIn <- function(x, y) {
  sel <- x %in% y
  x[sel]
}

## Unit test:
library("RUnit")
test_isIn <- function() {
  x <- c("A", "B", "Z")
  checkIdentical(x, isIn(x, LETTERS))
  checkIdentical(x, isIn(c(x, "a"), LETTERS))
}

test_isIn()