import sys, unittest, string
from collections import namedtuple
from pygraph.classes.digraph import digraph
from pygraph.algorithms.minmax import shortest_path

Edge = namedtuple('Edge', ['tail', 'head'])

class Dijkstra:
    
    def __init__(self, vertices, edges):
        self.gr = digraph()
        self.gr.add_nodes(vertices)
        for edge in edges:
            self.gr.add_edge(edge, wt=edges[edge])
        self.vertices = vertices
        self.numVertices = len(vertices)
        self.numEdges = len(edges)

    def _runDijkstra(self, tailName, headName):
        if((tailName not in self.vertices) or (headName not in self.vertices)):
            return({},{})
        return(shortest_path(self.gr,tailName))
    
    def shortestPathCost(self, tailName, headName):
        (shortestPathSpanningTree, shortestDistance) = self._runDijkstra(tailName, headName)
        if(headName in shortestDistance):
            return(shortestDistance[headName])

        return(sys.maxint)

    def shortestPath(self, tailName, headName):

        (shortestPathSpanningTree, shortestDistance) = self._runDijkstra(tailName, headName)
        if(headName not in shortestPathSpanningTree):
            return([])

        head = headName
        path = []
        while(None != head):
            path.append(head)
            head = shortestPathSpanningTree[head]
        path.reverse()
        return(path)

class TestAllPairsShortestPath(unittest.TestCase):

    def test_smallkaggle(self):
        dataDir = '/Users/deflaux/rework/competitions/facebook2/data/' 
        file = dataDir + 'graph16.txt'
        (vertices, edges) = self.fileInputTestHelper(file,separator='|')
        dijk = Dijkstra(vertices, edges)
        self.assertEqual(['as62836','aegjmmoo'],
                         dijk.shortestPath(tailName='as62836', headName='aegjmmoo'))
        self.assertEqual(0,
                         dijk.shortestPathCost(tailName='as62836', headName='aegjmmoo'))
        self.assertEqual(['bciklmnnoprrst su','as60366','0002 0002accnott aaeiilnnnortt accnott'],
                         dijk.shortestPath(tailName='bciklmnnoprrst su', headName='0002 0002accnott aaeiilnnnortt accnott'))
        self.assertEqual(0,
                         dijk.shortestPathCost(tailName='bciklmnnoprrst su', headName='0002 0002accnott aaeiilnnnortt accnott'))
        
    def test_kaggle(self):
        dataDir = '/Users/deflaux/rework/competitions/facebook2/data/' 
        outfile = open(dataDir + 'optimalPathPreds.txt', 'w')
        for epoch in range(16,21):
            file = dataDir + 'graph' + str(epoch) + '.txt'
            (vertices, edges) = self.fileInputTestHelper(file,separator='|')
            dijk = Dijkstra(vertices, edges)
            infile = open('/Users/deflaux/rework/competitions/facebook2/data/normTestPaths.txt', 'r')
            for line in infile:
                values = line.split('|')
                if(1 == len(values)):
                    outfile.write('1\n')
                    continue
                values = [v.strip() for v in values]
                shortestPath = dijk.shortestPath(tailName=values[0],
                                                 headName=values[len(values)-1])
                testPath = [string.strip(p) for p in values]
                if(shortestPath == testPath):
                    outfile.write('1\n')
                else:
                     outfile.write('0\n')
                    
    def test_simple(self):
        edges = {}
        vertices = range(1,7)
        edges[Edge(tail=1, head=2)] = 3 
        edges[Edge(tail=2, head=5)] = 4 
        edges[Edge(tail=5, head=4)] = 3 
        edges[Edge(tail=4, head=3)] = 5 
        edges[Edge(tail=3, head=2)] = 4
        edges[Edge(tail=5, head=6)] = 4
        dijk = Dijkstra(vertices, edges)
        self.assertEqual(dijk.shortestPathCost(tailName=1, headName=5), 7)
        self.assertEqual(dijk.shortestPathCost(tailName=5, headName=1), sys.maxint)
        self.assertEqual(dijk.shortestPathCost(tailName=3, headName=3), 0)
        self.assertEqual([1,2,5,6], dijk.shortestPath(tailName=1, headName=6))
        self.assertEqual([1,2,5], dijk.shortestPath(tailName=1, headName=5))
        self.assertEqual([1,2], dijk.shortestPath(tailName=1, headName=2))
        self.assertEqual([1], dijk.shortestPath(tailName=1, headName=1))
        self.assertEqual([], dijk.shortestPath(tailName=2, headName=1))
        
    def test_containsStringNodeNames(self):
        edges = {}
        vertices = ['one', 'two', 'three', 'four', 'five']
        edges[Edge(tail='one', head='two')] = 3 
        edges[Edge(tail='two', head='five')] = 4 
        edges[Edge(tail='five', head='four')] = 3 
        edges[Edge(tail='four', head='three')] = 5 
        edges[Edge(tail='three', head='two')] = 4
        dijk = Dijkstra(vertices, edges)
        self.assertEqual(dijk.shortestPathCost(tailName='one', headName='five'), 7)
        self.assertEqual(dijk.shortestPathCost(tailName='five', headName='one'), sys.maxint)
        self.assertEqual(dijk.shortestPathCost(tailName='three', headName='three'), 0)
        self.assertEqual(['one','two','five'], dijk.shortestPath(tailName='one', headName='five'))
        self.assertEqual(['one','two'], dijk.shortestPath(tailName='one', headName='two'))
        self.assertEqual(['one'], dijk.shortestPath(tailName='one', headName='one'))
        self.assertEqual([], dijk.shortestPath(tailName='two', headName='one'))
        
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

