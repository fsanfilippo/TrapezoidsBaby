package main.structures;

import main.structures.DCEL.Face;
import main.structures.DCEL.Vertex;

import java.util.ArrayList;
import java.util.List;

public class Trapezoid {

    public Vertex leftp;
    public Vertex rightp;
    public Segment top;
    public Segment bottom;
    public String name;
    public Face containingFace;
    public List<Trapezoid> neighbors = new ArrayList<>();

    public Trapezoid(Vertex leftp, Vertex rightp, Segment top, Segment bottom, int no) {
        this.leftp = leftp;
        this.rightp = rightp;
        this.top = top;
        this.bottom = bottom;
        this.name = "t" + no;
    }

    public Trapezoid(Vertex leftp, Vertex rightp, Segment top, Segment bottom) {
        this.leftp = leftp;
        this.rightp = rightp;
        this.top = top;
        this.bottom = bottom;

    }
}
