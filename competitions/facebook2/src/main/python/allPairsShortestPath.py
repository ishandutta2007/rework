import sys, unittest, string
from collections import namedtuple

Edge = namedtuple('Edge', ['tail', 'head'])

class AllPairsShortestPaths:
    
    '''Assumes vertices are consecutively numbered beginning with 1 (not zero).  TODO this can be fixed by using an index into the vertices set'''
    
    def __init__(self, vertices, edges):
        self.vertices = vertices
        self.edges = edges
        self.numVertices = len(vertices)
        self.numEdges = len(edges)
        self._clear()

    def _clear(self):
        self.pathCosts = None
        self.paths = [[-1]*(self.numVertices) for x in xrange(self.numVertices)]
        self.hasNegativeCycle = None

    def containsNegativeCycle(self):
        ''' compute this lazily and only once'''
        if(None == self.pathCosts):
            self.floydWarshall()

        if(None == self.hasNegativeCycle):
            self.hasNegativeCycle = False
            for vertex in range(0, self.numVertices):
                if(0 > self.pathCosts[vertex][vertex]):
                    self.hasNegativeCycle = True
                    
        return(self.hasNegativeCycle)

    def shortestPathCost(self, tailName, headName):
        if(self.hasNegativeCycle):
            raise Error('graph has negative cycle')
        
        return(self.pathCosts[self.vertices.index(tailName)][self.vertices.index(headName)])

    def shortestShortestPathCost(self):
        shortestPathCost = sys.maxint
        for tail in range(0, self.numVertices):
            for head in range(0, self.numVertices):
                if(tail == head):
                    continue
                if(shortestPathCost > self.pathCosts[tail][head]):
                    shortestPathCost = self.pathCosts[tail][head]

        return(shortestPathCost)
        
    def shortestPath(self, tailName, headName):
        if(self.hasNegativeCycle):
            raise Error('graph has negative cycle')

        path = [headName]
        head = self.vertices.index(headName)
        tail = self.vertices.index(tailName)
        prev = None
        while(tail != head):
            prev = self.paths[tail][head]
            if(-1 == prev):
                return([])
            path.append(self.vertices[prev])
            head = prev
            
        path.reverse()
        return(path)

    def floydWarshall(self):
        self._clear()

        previous = [[sys.maxint]*(self.numVertices) for x in xrange(self.numVertices)]
        
        # Base case
        prev = 0
        for tail in range(0, self.numVertices):
            for head in range(0, self.numVertices):
                edgeKey = Edge(tail=self.vertices[tail], head=self.vertices[head])
                if(tail == head):
                    previous[tail][head] = 0
                elif(edgeKey in self.edges):
                    previous[tail][head] = self.edges[edgeKey]
                    #print('tail %d prev %d head %d' %(tail, prev, head))
                    self.paths[tail][head] = tail
                else:
                    previous[tail][head] = sys.maxint

        # nuke the edges hash since we no longer need it
        del(self.edges)
                    
        for prev in range(1, self.numVertices):
            if(0 == (prev % 50)):
                print('Including vertex %d in solutions' % (prev))
            current = [[sys.maxint]*(self.numVertices) for x in xrange(self.numVertices)]
            for tail in range(0, self.numVertices):
                for head in range(0, self.numVertices):
                    costWithoutPrev = previous[tail][head]
                    costWithPrev =  previous[tail][prev] +  previous[prev][head]
                    if(costWithoutPrev <= costWithPrev):
                        current[tail][head] = costWithoutPrev
                    else:
                        current[tail][head] = costWithPrev
                        #print('tail %d prev %d head %d' %(tail, prev, head))
                        self.paths[tail][head] = prev
            previous = current

        self.pathCosts = current
        return()

class TestAllPairsShortestPath(unittest.TestCase):
        
    def footest_courseraG1(self):
        file = '/Users/deflaux/courseWork/courseraAlg/p2a4/g1.txt'
        (vertices, edges) = self.fileInputTestHelper(file)
        apsp = AllPairsShortestPaths(vertices, edges)
        if(apsp.containsNegativeCycle()):
            print('For Coursera: has negative cycle')
        else:
            shortestShortestPathCost = apsp.shortestShortestPathCost()
            print('For Coursera: %d' % (shortestShortestPathCost))
            self.assertEqual(shortestShortestPathCost, 42)

    def footest_courseraG2(self):
        file = '/Users/deflaux/courseWork/courseraAlg/p2a4/g2.txt'
        (vertices, edges) = self.fileInputTestHelper(file)
        apsp = AllPairsShortestPaths(vertices, edges)
        if(apsp.containsNegativeCycle()):
            print('For Coursera: has negative cycle')
        else:
            shortestShortestPathCost = apsp.shortestShortestPathCost()
            print('For Coursera: %d' % (shortestShortestPathCost))
            self.assertEqual(shortestShortestPathCost, 42)

    def footest_courseraG3(self):
        file = '/Users/deflaux/courseWork/courseraAlg/p2a4/g3.txt'
        (vertices, edges) = self.fileInputTestHelper(file)
        apsp = AllPairsShortestPaths(vertices, edges)
        if(apsp.containsNegativeCycle()):
            print('For Coursera: has negative cycle')
        else:
            shortestShortestPathCost = apsp.shortestShortestPathCost()
            print('For Coursera: %d' % (shortestShortestPathCost))
            self.assertEqual(shortestShortestPathCost, -19)

    def test_kaggle(self):
        dataDir = '/Users/deflaux/rework/competitions/facebook2/data/' 
        outfile = open(dataDir + 'optimalPathPreds.txt', 'w')
        for epoch in range(16,21):
            file = dataDir + 'graph' + str(epoch) + '.txt'
            (vertices, edges) = self.fileInputTestHelper(file,separator='|')
            apsp = AllPairsShortestPaths(vertices, edges)
            if(apsp.containsNegativeCycle()):
                print('Kaggle: has negative cycle for epoch ' + str(epoch))
                continue
            infile = open('/Users/deflaux/rework/competitions/facebook2/data/normTestPaths.txt', 'r')
            for line in infile:
                values = line.split('|')
                shortestPath = apsp.shortestPath(tailName=values[0],
                                                 headName=values[len(values)-1])
                testPath = [string.strip(p) for p in values]
                if(shortestPath == testPath):
                    outfile.write('1\n')
                else:
                     outfile.write('0\n')
                    
    def test_containsNegCycle(self):
        edges = {}
        vertices = range(1,6)
        edges[Edge(tail=1, head=2)] = 3 
        edges[Edge(tail=2, head=5)] = -4 
        edges[Edge(tail=5, head=4)] = 3 
        edges[Edge(tail=4, head=3)] = -5 
        edges[Edge(tail=3, head=2)] = 4
        apsp = AllPairsShortestPaths(vertices, edges)
        self.assertTrue(apsp.containsNegativeCycle())
        
    def test_containsNoNegCycle(self):
        edges = {}
        vertices = range(1,6)
        edges[Edge(tail=1, head=2)] = 3 
        edges[Edge(tail=2, head=5)] = 4 
        edges[Edge(tail=5, head=4)] = 3 
        edges[Edge(tail=4, head=3)] = 5 
        edges[Edge(tail=3, head=2)] = 4
        apsp = AllPairsShortestPaths(vertices, edges)
        self.assertFalse(apsp.containsNegativeCycle())
        self.assertEqual(apsp.shortestPathCost(tailName=1, headName=5), 7)
        self.assertEqual(apsp.shortestPathCost(tailName=5, headName=1), sys.maxint)
        self.assertEqual(apsp.shortestPathCost(tailName=3, headName=3), 0)
        self.assertEqual(apsp.shortestShortestPathCost(), 3)
        self.assertEqual([1,2,5], apsp.shortestPath(tailName=1, headName=5))
        self.assertEqual([1,2], apsp.shortestPath(tailName=1, headName=2))
        self.assertEqual([1], apsp.shortestPath(tailName=1, headName=1))
        self.assertEqual([], apsp.shortestPath(tailName=2, headName=1))
        
    def test_containsStringNodeNames(self):
        edges = {}
        vertices = ['one', 'two', 'three', 'four', 'five']
        edges[Edge(tail='one', head='two')] = 3 
        edges[Edge(tail='two', head='five')] = 4 
        edges[Edge(tail='five', head='four')] = 3 
        edges[Edge(tail='four', head='three')] = 5 
        edges[Edge(tail='three', head='two')] = 4
        apsp = AllPairsShortestPaths(vertices, edges)
        self.assertFalse(apsp.containsNegativeCycle())
        self.assertEqual(apsp.shortestPathCost(tailName='one', headName='five'), 7)
        self.assertEqual(apsp.shortestPathCost(tailName='five', headName='one'), sys.maxint)
        self.assertEqual(apsp.shortestPathCost(tailName='three', headName='three'), 0)
        self.assertEqual(apsp.shortestShortestPathCost(), 3)
        self.assertEqual(['one','two','five'], apsp.shortestPath(tailName='one', headName='five'))
        self.assertEqual(['one','two'], apsp.shortestPath(tailName='one', headName='two'))
        self.assertEqual(['one'], apsp.shortestPath(tailName='one', headName='one'))
        self.assertEqual([], apsp.shortestPath(tailName='two', headName='one'))
        
    def fileInputTestHelper(self, filepath, separator=' '):
        infile = open(filepath, 'r')
        edges = {}
        vertices = set()
        hasHeader = 0
        for line in infile:
            values = line.split(separator);
            if(2 == len(values)):
                if(0 == hasHeader):
                    numVertices = int(values[0])
                    numEdges = int(values[1])
                    hasHeader = 1
                    continue
                else:
                    raise ValueError('file is not formatted correctly')
            if(3 != len(values)):
                raise ValueError('file is not formatted correctly')
            tail = values[0]
            head = values[1]
            cost = int(values[2])
            vertices.add(tail)
            vertices.add(head)
            edge = Edge(tail=tail, head=head)
            edges[edge] = cost
        if(1 == hasHeader and numVertices != len(vertices)):
            raise ValueError('incorrect number of vertices')
        if(1 == hasHeader and numEdges != len(edges)):
            raise ValueError('incorrect number of edges')
        print('Loaded %d edges' % (len(edges)))
        return([v for v in vertices], edges)

if __name__ == '__main__':
    unittest.main()

