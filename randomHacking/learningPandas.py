import os.path, json
import numpy as np
import pandas as pd

dataDir = os.path.expanduser('~/rework/gettingStartedScripts/data/')

with open(dataDir + 'electionResults.json', 'rb') as infile:
    electionResults = json.load(infile)

# unlist the json results
results = [candidateResult for stateResults in electionResults['results'][1]['states'] for candidateResult in stateResults['parties']]
df = pd.DataFrame(results)

allPopVote =  df['pv'].sum()
# TODO multiindex or groupby might have been better here
obamaPopVote =  df.ix[df['lastName']=='Obama']['pv'].sum()
romneyPopVote =  df.ix[df['lastName']=='Romney']['pv'].sum()

print('Popular Vote Total: %s' % allPopVote)
print('Obama: %s %f' % (obamaPopVote, float(obamaPopVote)/float(allPopVote)))
print('Romney: %s %f' % (romneyPopVote, float(romneyPopVote)/float(allPopVote)))
print('Obama plus third party: %s %f' % (allPopVote-romneyPopVote, float(allPopVote-romneyPopVote)/float(allPopVote)))
print('Romney plus third party: %s %f' % (allPopVote-obamaPopVote, float(allPopVote-obamaPopVote)/float(allPopVote)))
