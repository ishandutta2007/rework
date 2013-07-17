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