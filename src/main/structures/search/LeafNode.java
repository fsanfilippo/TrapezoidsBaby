package main.structures.search;

import main.structures.Trapezoid;

public class LeafNode extends Node {

    public Trapezoid trapezoid;

    public LeafNode(Trapezoid trap){
        trapezoid = trap;
    }
}
