package sfanttasks.sfnodesubstitution.nodes

class Always {
    String fileType
    def value
    Set<Node> nodes = new HashSet<>()

    Node createNode() {
        Node node = new Node()
        nodes.add(node)

        node
    }

    void setNode(String node) {
        nodes.add(new Node(node))
    }
}
