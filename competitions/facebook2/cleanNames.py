#!/usr/bin/env python
# -*- coding: latin-1 -*-

import unittest, string, re, cPickle, os, sys

DATA_DIR = '/Users/deflaux/rework/competitions/facebook2/data'
# TODO compute term frequency to determine the stop words, as60 was badly merged
STOP_WORDS = ['llc', 'inc', 'ltd', 'isp', 'fop', 'co', 'sa', 'com', 'llp', 'plc', 'sa', 'net', 'limited', 'incorporated', 'services', 'network', 'information', 'technology']
STOP_WORDS = set([ ''.join(sorted(stopWord)) for stopWord in STOP_WORDS])

class CleanNames(unittest.TestCase):

    def test_keyify(self):
        self.assertEqual((['abr', 'foo'], []), self.keyify('foo bar'))
        self.assertEqual((['abr', 'foo'], []), self.keyify('foo&bar'))
        self.assertEqual((['abfoor'], []), self.keyify('foo!bar'))
        self.assertEqual((['abfoor'], []), self.keyify('foobar'))
        self.assertEqual((['abfoor'], []), self.keyify('foobar foobar'))
        self.assertEqual((['abfoor'], []), self.keyify('foobar FOO.BAR'))
        self.assertEqual((['as60069'], ['as60069']), self.keyify('60069 '))
        self.assertEqual((['as60690'], ['as60690']), self.keyify('60690 '))
        self.assertEqual((['01159aeeiorrstz', 'eeiorrtz'], ['as11509']), self.keyify('TIERZERO-AS11509 - Tierzero'))

        self.assertEqual((['01224abeilmost'], ['as22140']), self.keyify('T-MOBILE-AS22140'))
        self.assertEqual((['03339aenst'], ['as30933']), self.keyify('AS30933.net')) 
        self.assertEqual((['03339aenst'], ['as30933']), self.keyify('AS30933.net L.L.C.'))
        self.assertEqual((['aeeegmrrsv'], []), self.keyify('MEGASERVER-AS MegaServer MegaServer Ltd.'))
        self.assertEqual((['aeeegmrrsv'], []), self.keyify('MEGASERVER-AS MegaServer MegaServer inc.'))
        self.assertEqual((['cin'], []), self.keyify('NIC'))
        self.assertEqual((['abcdefghijklmnopqrstuvwxyz'], []), self.keyify('==='))
    
    def test_translate(self):
        self.assertEqual('a', self.translate('a'))
        self.assertEqual('5', self.translate('5'))
        self.assertEqual('e', self.translate(u'é'))
        self.assertEqual('u', self.translate(u'\u00FC'))
        unicodeShould = u'sho\u00FCld'
        should = [self.translate(c) for c in unicodeShould]
        self.assertEqual(''.join(should), 'should')
        
    def test_normalize(self):
        self.assertEqual('stbasn 32 onsismp thacher bartlett',
                         self.normalize(u'STB-ASN - 32 onSismp Thachér  &  Bartlett '))
        self.assertEqual('', self.normalize('===='))

    def test_mergeMappings(self):
        m = {}
        m['four one three two xxx yyy zzz'] = [4]
        m['four three two xxx yyy zzz'] = [3]
        m['four one three xxx yyy zzz'] = [3]
        m['four one two xxx yyy zzz'] = [3]
        m['one three two xxx yyy zzz'] = [3]
        m['one two xxx yyy zzz'] = [2]
        m['one three xxx yyy zzz'] = [2]
        m['four one xxx yyy zzz'] = [2]
        m['three two xxx yyy zzz'] = [2]
        m['four two xxx yyy zzz'] = [2]
        m['four three xxx yyy zzz'] = [2]
        m['one xxx yyy zzz'] = [1]
        m['four xxx yyy zzz'] = [1]
        self.consolidateSubKeys(m, {})
        self.nukeIndirectMappings(m)
        print(m)
        self.assertEquals(3, len(m))
        

    def test_nameCleaning(self):
        numGraphs = 15
        
        (keyToName, asnameToKey) = self.cleanNames(numGraphs)

        (termFreq, stopWords) = self.filterStopWords(keyToName)

        expectedStopWords = ['llc', 'inc', 'ltd', 'isp', 'fop', 'co', 'sa', 'com', 'llp', 'plc', 'sa', 'net', 'limited', 'incorporated', 'services', 'network', 'information', 'technology']
        # TODO tests 
        
        self.consolidateSubKeys(keyToName, termFreq)
        
        self.mergeMappings(keyToName, asnameToKey)

        self.writeKeyifiedData(keyToName, numGraphs)
        self.writeKeyifiedPaths(keyToName)

        self.nukeIndirectMappings(keyToName)
         
        outfile = open(DATA_DIR + '/mapping_' + str(numGraphs) + '.txt', 'w')
        numSingleUseKeys = 0
        for key in keyToName:
            outfile.write('%s|%d|%s\n' % (key, len(keyToName[key]), str([w for w in set(keyToName[key])])))
            if(1 == len(keyToName[key])):
                   numSingleUseKeys = numSingleUseKeys +1
        outfile.close()

        if(numGraphs == 1):
            self.assertEqual(len(keyToName), 21523)
            self.assertEqual(numSingleUseKeys, 8277)
        else:
            self.assertEqual(len(keyToName), 44372)
            self.assertEqual(numSingleUseKeys, 9861)

    def cleanNames(self, numGraphs, unpickle=True):
        '''Load data from file and compute normalized keys for all entries'''
        
        suffix = '_' + str(numGraphs) + '.bin'
        self.keyToNameFile = DATA_DIR + '/keytoname' + suffix
        self.asnameToKeyFile = DATA_DIR + '/asnametokey' + suffix

        numEntries = 0
        if(unpickle and os.path.isfile(self.keyToNameFile) and os.path.isfile(self.asnameToKeyFile)):
            infile = open(self.keyToNameFile, 'r')
            keyToName = cPickle.load(infile)
            infile.close()

            infile = open(self.asnameToKeyFile, 'r')
            asnameToKey = cPickle.load(infile)
            infile.close()
        else:
            keyToName = {}
            asnameToKey = {}
            for i in range(1, 1+numGraphs):
                infile = open(DATA_DIR + '/train' + str(i) + '.txt', 'r')
                for line in infile:
                    values = line.split('|')
                    if(3 != len(values)):
                        raise ValueError('file is not formatted correctly')
                    self.insertMapping(keyToName, asnameToKey,  values[0])
                    self.insertMapping(keyToName, asnameToKey, values[1])
                    numEntries += 2

            infile = open(DATA_DIR + '/testPaths.txt', 'r')
            for line in infile:
                values = line.split('|')
                if(1 > len(values)):
                    raise ValueError('file is not formatted correctly')
                for value in values:
                    self.insertMapping(keyToName, asnameToKey, value)
                    numEntries += 1

            outfile = open(self.keyToNameFile, 'wb')
            cPickle.dump(keyToName, outfile)
            outfile.close()

            outfile = open(self.asnameToKeyFile, 'wb')
            cPickle.dump(asnameToKey, outfile)
            outfile.close()

        print('num entries %d, num keys %d' % (numEntries, len(keyToName)))
        return(keyToName, asnameToKey)

    def insertMapping(self, keyToName, asnameToKey, item):
        (keyParts, asnames) = self.keyify(item) 
        key = string.join(keyParts)
        item = string.strip(item) # take white space off either end of
                                  # the raw name
        if(key in keyToName):
            keyToName[key].append(item)
        else:
            keyToName[key] = [item]

        for asname in asnames:
            if(asname in asnameToKey):
                asnameToKey[asname].append(key)
            else:
                asnameToKey[asname] = [key]
            break # only insert one
#         if(1 < len(asnames)):
#             print('too many asnames %s for %s' % (str(asnames), key))
                                
    def writeKeyifiedData(self, keyToName, numGraphs):
        for i in range(1, 1+numGraphs):
            infile = open(DATA_DIR + '/train' + str(i) + '.txt', 'r')
            outfile = open(DATA_DIR + '/normTrain' + str(i) + '.txt', 'w')
            for line in infile:
                values = line.split('|')
                if(3 != len(values)):
                    raise ValueError('file is not formatted correctly')
                (keyParts, asnames) = self.keyify(values[0]) 
                tailKey = string.join(keyParts)
                (keyParts, asnames) = self.keyify(values[1]) 
                headKey = string.join(keyParts)
                tail = self.find(tailKey, keyToName)
                head = self.find(headKey, keyToName)
                cost = int(values[2])
                outfile.write('%s|%s|%d\n' % (tail, head, cost))
                
            outfile.close()
            infile.close()
        
    def writeKeyifiedPaths(self, keyToName):
        infile = open(DATA_DIR + '/testPaths.txt', 'r')
        outfile = open(DATA_DIR + '/normTestPaths.txt', 'w')
        for line in infile:
            values = line.split('|')
            if(1 > len(values)):
                raise ValueError('file is not formatted correctly')
            normalizedPath = ''
            for value in values:
                (keyParts, asnames) = self.keyify(value) 
                nodeKey = string.join(keyParts)
                node = self.find(nodeKey, keyToName)
                if('' == normalizedPath):
                    normalizedPath += node
                else:
                    normalizedPath += '|' + node
            outfile.write(normalizedPath + '\n')
                
        outfile.close()
        infile.close()
        
    def filterStopWords(self, keyToName):
        ''' Compute term frequencies, no need to do tf-idf because we
        already reduced it to unique terms per 'document'.  TODO use most
        frequent words as stop words'''
        termFreq = {}
        for key in keyToName:
            for part in key.split():
                if(part in termFreq):
                    termFreq[part] = termFreq[part]+1
                else:
                    termFreq[part] = 1

        # TODO
        # dictionary to tuple
        # sort on freq
        # use threshold for stopwords
        stopWords = set([])
        return(termFreq, stopWords)

    def consolidateSubKeys(self, keyToName, termFreq):            
        '''For each key, remove one word and see if it is still a key,
        if so merge them.   TODO consider difflib http://stackoverflow.com/questions/3551423/python-comparing-two-strings'''
        for key in keyToName:
            keyParts = set(key.split())
            if(1 == len(keyParts)): 
                continue
            for partToRemove in keyParts:
                keyPartsSubset = keyParts.copy()
                keyPartsSubset.remove(partToRemove)
                if(keyPartsSubset.issubset(STOP_WORDS)):
                       # Don't make smaller keys that are all stop words
                       continue
                partsRemaining = [p for p in keyPartsSubset]
                partsRemaining.sort()
                smallerKey = ' '.join(partsRemaining)
                # Special handling for short keys
                if(1 == len(partsRemaining)):
                    if(termFreq[partToRemove] < termFreq[smallerKey]):
                        # if we did not retain the item with lower freq
                        # in our smallerKey, skip this loop
                        continue
                if(smallerKey in keyToName):
                    self._merge(key, smallerKey, keyToName)
                    break
                
    def mergeMappings(self, keyToName, asnameToKey):
        for asname in asnameToKey:
            for key in [k for k in set(asnameToKey[asname])]:
                if(asname == key):
                    continue
                self._merge(key, asname, keyToName)

    def find(self, key, keyToName):
        '''This method treats the hashtable like a union-find data structure'''
        if key in keyToName:
            while(not isinstance(keyToName[key], list)):
#                 print('Find: following link from %s to %s' % (key, keyToName[key]))
                key = keyToName[key]
        return(key)

    def _merge(self, fromKey, toKey, keyToName):
        '''This method treats the hashtable like a union-find data structure'''
#         print('Merging %s into %s' % (fromKey, toKey))
        toKey = self.find(toKey, keyToName)
        if toKey not in keyToName:
            keyToName[toKey] = []

        fromValue = None
        while(None == fromValue):
            value = keyToName[fromKey]
            if(value == toKey):
                return() # already merged
            keyToName[fromKey] = toKey
            if(isinstance(value, list)):
                fromValue = value
            else: # follow the pointer
#                 print('FROM: following link from %s to %s' % (fromKey, value))
                fromKey = value

#         print('tokey %s fromkey %s tovalue %s fromvalue %s' % (toKey,
#                                                                fromKey,
#                                                                str(keyToName[toKey]),
#                                                                str(fromValue)))
        keyToName[toKey].extend(fromValue)
#         print('\t%s' % (str(set(keyToName[toKey]))))
                    
    def nukeIndirectMappings(self, keyToName):
        '''Remove all name pointers from our mapping.  Do this when we
        want to see how small our mapping has become.'''
        keys = keyToName.keys()
        for key in keys:
            if(not isinstance(keyToName[key], list)):
                del(keyToName[key])
                
    def keyify(self, name):
        '''Convert the name into both a hash key and an asname, if
        possible.  Since we know that letters are potentially scrambled in
        the name, we unscramble them by sorting the letters in the words
        that make up the name and then sorting all those words'''

        # more ideas
        # 1) move stop word logic to here, use term freq threshold to determine what is a stop word???
        # 2) similarity measures that take into account tf-idf
        name = re.sub('-AS\s', ' ', name)
        
        normalizedName = self.normalize(name)
        if('' == normalizedName):
            normalizedName = 'abcdefghijklmnopqrstuvwxyz'
        words = normalizedName.split()
        if(1 == len(words) and re.match('^\d+$', words[0])):
            # if the name is just a number, do not sort it, return it as-is
            return (['as'+words[0]], ['as'+words[0]])
        
        ### ASNAME, specifically parse for asnames
        asnames = [ word for word in words if re.match('^as[1-9]\d+', word)]
        asnames.extend([ word for word in words if re.search('as[1-9]\d+$', word)])
        asnames = [re.search('(as[1-9]\d+)', asname).group(1) for asname in asnames]
        # remove duplicate words
        asnames = [ asnames for asnames in set(asnames)]
        if(1 < len(asnames)):
            print('too many asnames %s for %s' % (str(asnames), name))
            asnames.sort()
        
        ### KEY
            
        # sort the letters in the words
        sortedWords = [ ''.join(sorted(word)) for word in words]
        
        # remove duplicate words
        sortedWords = set(sortedWords)
        if(not set(sortedWords).issubset(STOP_WORDS)):
            # remove stop words IF there are some non-stop word parts
            sortedWords = [w for w in sortedWords if w not in STOP_WORDS]
        else:
            sortedWords = [w for w in sortedWords]            

        # sort the unique words
        sortedWords.sort()
    
        return (sortedWords, asnames)
    
    def normalize(self, name):
        '''Remove unimportant information from the name such as uppercase letters'''
        name = string.strip(name)
        name = string.lower(name)
        name = ''.join([self.translate(c) for c in name])
        name = re.sub('[&]', ' ', name) # turn these into spaces
        name = re.sub('[^a-zA-Z0-9_\s]', '', name) # collapse these
        name = re.sub('\s+', ' ', name)
        return(name)

    def translate(self, char):
        '''Convert unicode characters to a reasonable ascii equivalent.
        Python probaby can do this already, but this mapping was taken
        from
        https://github.com/OpenRefine/OpenRefine/blob/master/main/src/com/google/refine/clustering/binning/FingerprintKeyer.java'''
        return {
            u'\u00C0': 'a',
            u'\u00C1': 'a',
            u'\u00C2': 'a',
            u'\u00C3': 'a',
            u'\u00C4': 'a',
            u'\u00C5': 'a',
            u'\u00E0': 'a',
            u'\u00E1': 'a',
            u'\u00E2': 'a',
            u'\u00E3': 'a',
            u'\u00E4': 'a',
            u'\u00E5': 'a',
            u'\u0100': 'a',
            u'\u0101': 'a',
            u'\u0102': 'a',
            u'\u0103': 'a',
            u'\u0104': 'a',
            u'\u0105': 'a',
            # return 'a';
            u'\u00C7': 'c',
            u'\u00E7': 'c',
            u'\u0106': 'c',
            u'\u0107': 'c',
            u'\u0108': 'c',
            u'\u0109': 'c',
            u'\u010A': 'c',
            u'\u010B': 'c',
            u'\u010C': 'c',
            u'\u010D': 'c',
            # return 'c';
            u'\u00D0': 'd',
            u'\u00F0': 'd',
            u'\u010E': 'd',
            u'\u010F': 'd',
            u'\u0110': 'd',
            u'\u0111': 'd',
            # return 'd';
            u'\u00C8': 'e',
            u'\u00C9': 'e',
            u'\u00CA': 'e',
            u'\u00CB': 'e',
            u'\u00E8': 'e',
            u'\u00E9': 'e',
            u'\u00EA': 'e',
            u'\u00EB': 'e',
            u'\u0112': 'e',
            u'\u0113': 'e',
            u'\u0114': 'e',
            u'\u0115': 'e',
            u'\u0116': 'e',
            u'\u0117': 'e',
            u'\u0118': 'e',
            u'\u0119': 'e',
            u'\u011A': 'e',
            u'\u011B': 'e',
            # return 'e';
            u'\u011C': 'g',
            u'\u011D': 'g',
            u'\u011E': 'g',
            u'\u011F': 'g',
            u'\u0120': 'g',
            u'\u0121': 'g',
            u'\u0122': 'g',
            u'\u0123': 'g',
            # return 'g';
            u'\u0124': 'h',
            u'\u0125': 'h',
            u'\u0126': 'h',
            u'\u0127': 'h',
            # return 'h';
            u'\u00CC': 'i',
            u'\u00CD': 'i',
            u'\u00CE': 'i',
            u'\u00CF': 'i',
            u'\u00EC': 'i',
            u'\u00ED': 'i',
            u'\u00EE': 'i',
            u'\u00EF': 'i',
            u'\u0128': 'i',
            u'\u0129': 'i',
            u'\u012A': 'i',
            u'\u012B': 'i',
            u'\u012C': 'i',
            u'\u012D': 'i',
            u'\u012E': 'i',
            u'\u012F': 'i',
            u'\u0130': 'i',
            u'\u0131': 'i',
            # return 'i';
            u'\u0134': 'j',
            u'\u0135': 'j',
            # return 'j';
            u'\u0136': 'k',
            u'\u0137': 'k',
            u'\u0138': 'k',
            # return 'k';
            u'\u0139': 'l',
            u'\u013A': 'l',
            u'\u013B': 'l',
            u'\u013C': 'l',
            u'\u013D': 'l',
            u'\u013E': 'l',
            u'\u013F': 'l',
            u'\u0140': 'l',
            u'\u0141': 'l',
            u'\u0142': 'l',
            # return 'l';
            u'\u00D1': 'n',
            u'\u00F1': 'n',
            u'\u0143': 'n',
            u'\u0144': 'n',
            u'\u0145': 'n',
            u'\u0146': 'n',
            u'\u0147': 'n',
            u'\u0148': 'n',
            u'\u0149': 'n',
            u'\u014A': 'n',
            u'\u014B': 'n',
            # return 'n';
            u'\u00D2': 'o',
            u'\u00D3': 'o',
            u'\u00D4': 'o',
            u'\u00D5': 'o',
            u'\u00D6': 'o',
            u'\u00D8': 'o',
            u'\u00F2': 'o',
            u'\u00F3': 'o',
            u'\u00F4': 'o',
            u'\u00F5': 'o',
            u'\u00F6': 'o',
            u'\u00F8': 'o',
            u'\u014C': 'o',
            u'\u014D': 'o',
            u'\u014E': 'o',
            u'\u014F': 'o',
            u'\u0150': 'o',
            u'\u0151': 'o',
            # return 'o';
            u'\u0154': 'r',
            u'\u0155': 'r',
            u'\u0156': 'r',
            u'\u0157': 'r',
            u'\u0158': 'r',
            u'\u0159': 'r',
            # return 'r';
            u'\u015A': 's',
            u'\u015B': 's',
            u'\u015C': 's',
            u'\u015D': 's',
            u'\u015E': 's',
            u'\u015F': 's',
            u'\u0160': 's',
            u'\u0161': 's',
            u'\u017F': 's',
            # return 's';
            u'\u0162': 't',
            u'\u0163': 't',
            u'\u0164': 't',
            u'\u0165': 't',
            u'\u0166': 't',
            u'\u0167': 't',
            # return 't';
            u'\u00D9': 'u',
            u'\u00DA': 'u',
            u'\u00DB': 'u',
            u'\u00DC': 'u',
            u'\u00F9': 'u',
            u'\u00FA': 'u',
            u'\u00FB': 'u',
            u'\u00FC': 'u',
            u'\u0168': 'u',
            u'\u0169': 'u',
            u'\u016A': 'u',
            u'\u016B': 'u',
            u'\u016C': 'u',
            u'\u016D': 'u',
            u'\u016E': 'u',
            u'\u016F': 'u',
            u'\u0170': 'u',
            u'\u0171': 'u',
            u'\u0172': 'u',
            u'\u0173': 'u',
            # return 'u';
            u'\u0174': 'w',
            u'\u0175': 'w',
            # return 'w';
            u'\u00DD': 'y',
            u'\u00FD': 'y',
            u'\u00FF': 'y',
            u'\u0176': 'y',
            u'\u0177': 'y',
            u'\u0178': 'y',
            # return 'y';
            u'\u0179': 'z',
            u'\u017A': 'z',
            u'\u017B': 'z',
            u'\u017C': 'z',
            u'\u017D': 'z',
            u'\u017E': 'z',
            # return 'z';
            }.get(char, char)

if __name__ == '__main__':
    unittest.main()

