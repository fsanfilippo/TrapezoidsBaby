package main.structures.DCEL;

import main.structures.Trapezoid;

import java.util.ArrayList;
import java.util.List;

public class Face {

    public HalfEdge outerComponent;
    public List<HalfEdge> innerComponent;
    public String name;
    private List<Trapezoid> trapezoids = new ArrayList<>();

    public Face (int no, List<HalfEdge> inner, HalfEdge outer){
        this.name = "f" + no;
        this.innerComponent = inner;
        this.outerComponent = outer;
    }

    public Face(String name){
        this.name = name;
        outerComponent = null;
        innerComponent = null;
    }


}
