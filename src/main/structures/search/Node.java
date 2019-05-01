package main.structures.search;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {

    public Node lChild;
    public Node rChild;
    public List<Node> parents = new ArrayList<>();
}
