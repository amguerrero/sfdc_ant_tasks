package sfanttasks.sfnodesubstitution.nodes

class Node {
    String name

    Node() {}

    Node(String name) {
        this.name = name
    }

    void addText(String text) {
        name = text
    }

    String toString() {
        name
    }
}
