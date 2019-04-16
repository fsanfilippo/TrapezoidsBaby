package main.structures;
import main.Query;
import main.structures.DCEL.Face;
import main.structures.DCEL.Vertex;
import main.structures.search.QueryResponse;
import main.structures.search.SearchStructure;

import java.util.List;
import java.util.ArrayList;

public class TrapezoidMap {
    public List<Segment> segments = new ArrayList<>();
    private List<Vertex> endpoints = new ArrayList<>();
    public List<Trapezoid> traps = new ArrayList<>();
    private SearchStructure D;

    public TrapezoidMap(SearchStructure D, List<Segment> boundingBox, Trapezoid trapRoot) {
        this.D = D;
        traps.add(trapRoot);
        segments.addAll(boundingBox);
    }

    public QueryResponse query(Query q){
        return D.query(q);
    }

}