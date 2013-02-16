#!/usr/bin/env python
# -*- coding: latin-1 -*-

import unittest, string, re, cPickle

def normalize(name):
    name = string.strip(name)
    name = string.lower(name)
    name = ''.join([translate(c) for c in name])
    name = re.sub('[^a-zA-Z0-9_\s]', ' ', name)
    name = re.sub('\s+', ' ', name)
    return(name)

def translate(char):
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


def keyify(name):
    normalizedName = normalize(name)
    words = normalizedName.split()
    if(1 == len(words) and re.match('^\d+$', words[0])):
        # if the name is just a number, do not sort it, return it as-is
        return (['as'+words[0]], ['as'+words[0]])

    ### ASNAME
    # specifically parse for AS names
    asNames = [ word for word in words if re.match('^as\d+$', word)]
    # remove duplicate words
    asNames = [ asNames for asNames in set(asNames)]
    if(1 < len(asNames)):
        print('too many asNames %s for %s' % (str(asNames), name))

    ### KEY
    # sort the letters in the words
    sortedWords = [ ''.join(sorted(word)) for word in words]
    # remove duplicate words
    sortedWords = [w for w in set(sortedWords)]
    # sort the unique words
    sortedWords.sort()
    
    return (sortedWords, asNames)
    
class Test(unittest.TestCase):

    def test_keyify(self):
        self.assertEqual((['abr', 'foo'], []), keyify('foo bar'))
        self.assertEqual((['abr', 'foo'], []), keyify('foo!bar'))
        self.assertEqual((['abfoor'], []), keyify('foobar'))
        self.assertEqual((['abfoor'], []), keyify('foobar foobar'))
        self.assertEqual((['abfoor', 'abr', 'foo'], []), keyify('foobar FOO.BAR'))
        self.assertEqual((['as60069'], ['as60069']), keyify('60069 '))
        self.assertEqual((['as60690'], ['as60690']), keyify('60690 '))
        self.assertEqual((['01159as', 'eeiorrtz'], ['as11509']), keyify('TIERZERO-AS11509 - Tierzero'))
    
    def test_translate(self):
        self.assertEqual('a', translate('a'))
        self.assertEqual('5', translate('5'))
        self.assertEqual('e', translate(u'é'))
        self.assertEqual('u', translate(u'\u00FC'))
        unicodeShould = u'sho\u00FCld'
        should = [translate(c) for c in unicodeShould]
        self.assertEqual(''.join(should), 'should')
        
    def test_normalize(self):
        self.assertEqual('stb asn 32 onsismp thacher bartlett',
                         normalize(u'STB-ASN - 32 onSismp Thachér  &  Bartlett '))

    def test_nameCleaning(self):
        nameMapping = {}
        asMapping = {}
        numEntries = 0
        for i in range(1, 2) :
            infile = open('/Users/deflaux/rework/competitions/facebook2/data/train' + str(i) + '.txt', 'r')
            for line in infile:
                values = line.split('|')
                if(3 != len(values)):
                    raise ValueError("file is not formatted correctly")
                self.insertMapping(nameMapping, asMapping,  values[0])
                self.insertMapping(nameMapping, asMapping, values[1])
                numEntries += 2

        self.mergeMappings(nameMapping, asMapping)
         
        outfile = open('/Users/deflaux/rework/competitions/facebook2/data/mapping.bin', 'wb')
        cPickle.dump(nameMapping, outfile)
        outfile.close()

        outfile = open('/Users/deflaux/rework/competitions/facebook2/data/mapping.txt', 'w')
        for key in nameMapping:
            outfile.write('%s|%d|%s\n' % (key, len(nameMapping[key]), str([w for w in set(nameMapping[key])])))
        outfile.close()

        print('num entries %d, num keys %d' % (numEntries, len(nameMapping)))
                
    def mergeMappings(self, nameMapping, asMapping):
        for asName in asMapping:
            for key in [k for k in set(asMapping[asName])]:
                if(asName == key):
                    continue
                if(3 >= len(asName)):
                    continue # as1 as2 and as3 seem to be bad
                print('Merging %s and %s' % (asName, key))
                if asName in nameMapping:
                    nameMapping[asName].extend(nameMapping[key])
                else:
                    nameMapping[asName] = nameMapping[key]
                del(nameMapping[key])
                    
    def insertMapping(self, nameMapping, asMapping, item):
        (keyParts, asNames) = keyify(item) 
        key = string.join(keyParts)
        if(key in nameMapping):
            nameMapping[key].append(item)
        else:
            nameMapping[key] = [item]

        for asName in asNames:
            if(asName in asMapping):
                asMapping[asName].append(key)
            else:
                asMapping[asName] = [key]
            break # only insert one
#         if(1 < len(asNames)):
#             print('too many asNames %s for %s' % (str(asNames), key))
                                
if __name__ == '__main__':
    unittest.main()

