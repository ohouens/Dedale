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


    def joint(self, n1, n2):
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

    def square(self,stick=-1):
        self.start(stick)
        origin = self.origin
        border = []
        border += self.straight(self.origin)
        border += self.straight(self.nodeCursor)
        border += self.straight(self.nodeCursor)
        self.joint(self.nodeCursor, origin)
        return self.clean(border)

    def triangle(self,stick=-1):
        self.start(stick)
        origin = self.origin
        border = []
        border += self.straight(self.origin)
        border += self.straight(self.nodeCursor)
        self.joint(self.nodeCursor, origin)
        return self.clean(border)

if __name__ == "__main__":
    map = MapCreation()
    map.straight()
    map.square()
    map.triangle()
    map.build()
