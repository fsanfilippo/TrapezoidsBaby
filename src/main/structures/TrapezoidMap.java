package main.structures;
import main.structures.DCEL.Face;
import main.structures.DCEL.Vertex;
import main.structures.search.SearchStructure;

import java.util.List;
import java.util.ArrayList;

public class TrapezoidMap {
    private List<Segment> segments = new ArrayList<>();
    private List<Vertex> endpoints = new ArrayList<>();
    public List<Trapezoid> traps = new ArrayList<>();
    private SearchStructure D;

    public TrapezoidMap(SearchStructure D) {
        this.D = D;
    }

}