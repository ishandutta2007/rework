numCombinationsWithRepetitionAllowed <- function(n,k) {
    # (n+k-1)!/k!(n-1)!
    # see also R function 'choose'
    factorial(n+k-1)/(factorial(k)*factorial(n-1))
}
comb <- numCombinationsWithRepetitionAllowed  # short name alias

yahtzeeRollProbability <- function(numDiceToRoll) {
    numDiceSides = 6
    1/numCombinationsWithRepetitionAllowed(n=numDiceSides,
                                           k=numDiceToRoll)
}
prob <- yahtzeeRollProbability # short name alias

payoff <- function(numDiceToRoll, numWinningCombinations, points) {
    probability <- numWinningCombinations*yahtzeeRollProbability(numDiceToRoll)
    expectedValue <- points*probability
}

#----------------------------------------------------------------
# Compute probabilities of one particular combination appearing for a given number of dice rolled
numDice <- list(oneDie=1,twoDice=2,threeDice=3,fourDice=4,fiveDice=5)
lapply(numDice, function(x) { numCombinationsWithRepetitionAllowed(n=6, k=x) })
lapply(numDice, yahtzeeRollProbability)

#----------------------------------------------------------------
# I have two 6s and a 2,3,4. Should I go for a small straight or three 6s?
plays=list(oneSix=list(3,21,18), smallStraight=list(3,42,30))
lapply(plays, function(x) {do.call(payoff,x) })

# TODO check that numWinningCombinations is correct

total = 0
for (i in 1:6) {
    for (j in 1:6) {
        for (k in 1:6) {
            if((i==1) || (j==1) || (k==1)) { total = total + 1 }
            if((i==5) || (j==5) || (k==5)) { total = total + 1 }
            cat(paste(i,j,k,"\n"))
        }
    }
}