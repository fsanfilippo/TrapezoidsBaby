package main.structures.DCEL;

import main.structures.Segment;

public class HalfEdge {
    public Vertex origin;
    public HalfEdge twin;
    public Face incidentFace;
    public HalfEdge next;
    public HalfEdge prev;
    public String name;
    public Segment segment;
    
    //Constructor for Bounding box halfEdges
    public HalfEdge(Vertex origin, Face incidentFace, int a, int b){
        this.origin = origin;
        this.incidentFace = incidentFace;
        this.name = "e" + a + "," + b;

    }
    

}
