import copy as cp

class MapCreation():
    def __init__(self):
        self.nodeCursor = 0
        self.edgeCursor = 0
        self.origin = 0
        self.border = []
        self.edges = set()
        self.nodes = set()

    def start(self, stick):
        self.border = []
        if(stick >= 0):
            self.origin = stick
        else:
            self.nodeCursor+=1
            self.origin = self.nodeCursor

    def clean(self, border):
        result = []
        for node in border:
            if node not in result:
                result.append(node)
        return result

    def build(self):
        for node in self.nodes:
            print("an {0}\tlabel: {0}".format(node))
        for eID, n1, n2 in self.edges:
            print("ae e{0}\t{1} {2}".format(eID, n1, n2))

    def merge(self, b1, b2):
        c1, i1 = b1
        c2, i2 = b2
        n1 = c1[i1]
        n2 = c2[i2]
        self.nodeCursor+=1
        n3 = self.nodeCursor
        self.nodes.add(n3)
        dejaVu = set()
        dejaVu.add(n3)
        for e in cp.deepcopy(self.edges):
            if(n1==e[1] or n1==e[2]):
                self.edges.remove(e)
                dejaVu.add(e[1])
                dejaVu.add(e[2])
                if(n1==e[1]):
                    self.joint(n3, e[2])
                else:
                    self.joint(e[1], n3)
            if(n2==e[1] or n2==e[2]):
                self.edges.remove(e)
                if(n2==e[1]):
                    self.joint(n3, e[2])
                else:
                    self.joint(e[1], n3)
        self.nodes.remove(n1)
        self.nodes.remove(n2)
        c1[i1] = n3
        c2[i2] = n3
        return c1, c2

    def joint(self, n1, n2):
        for e, t1, t2 in self.edges:
            if(t1==n2 and t2==n1):
                return
        self.edges.add((self.edgeCursor, n1, n2))
        self.edgeCursor += 1

    def straight(self,stick=-1):
        self.start(stick)
        self.nodes.add(self.origin)
        self.border.append(self.origin)
        self.nodeCursor+=1
        self.nodes.add(self.nodeCursor)
        self.border.append(self.nodeCursor)
        border = self.border
        self.joint(border[0], border[1])
        return border

    def bridge(self,stick=-1,length=5):
        self.start(stick)
        self.start(stick)
        origin = self.origin
        border = []
        border += self.straight(self.origin)
        for i in range(length-1):
            border += self.straight(self.nodeCursor)
        return self.clean(border)

    def circle(self, stick=-1, length=5):
        self.start(stick)
        self.start(stick)
        origin = self.origin
        border = []
        border += self.straight(self.origin)
        for i in range(1, length-1):
            border += self.straight(self.nodeCursor)
        self.joint(self.nodeCursor, origin)
        return self.clean(border)

    def triangle(self,stick=-1):
        return self.circle(stick,3)

    def square(self,stick=-1):
        return self.circle(stick,4)

    def bracket(self, stick=-1):
        return self.bridge(stick,4)

    def branch(self, stick=-1):
        return self.bridge(stick,2)

if __name__ == "__main__":
    #Example of map: March Madness men's basketball championship bracket
    #available at this link -> https://www.ncaa.com/sites/default/files/public/styles/original/public-s3/images/2019/06/27/2020-NCAA-bracket-March-Madness.jpg?itok=ZFsTQ3uO
    map = MapCreation()
    side = "left"
    level = {}
    depth = 5
    for a in range(2):
        level[side] = {}
        for i in range(depth):
            level[side][i] = []
            for j in range(2**i):
                level[side][i].append(map.bracket())
        for k,v in level[side].items():
            if(k!=0):
                for x in range(len(v)):
                    p=0
                    if(x%2==1):
                        p=4
                    map.merge((level[side][k][x],2), (level[side][k-1][x//2],p))
        side = "right"
    map.joint((level["left"][0][0][2]), (level["right"][0][0][2]))
    map.build()

    #Example of map: 2 squares being merged
    squares = MapCreation()
    c1 = squares.square()
    c2 = squares.square()
    c1, c2 = squares.merge((c1,1), (c2,0))
    c1, c2 = squares.merge((c1,2), (c2,3))
    # squares.build()
