package main.structures.DCEL;

public class Vertex {
    public float x;
    public float y;
    public HalfEdge incidentEdge;
    public String name;
    public int no;

    public Vertex(float x, float y, int no){
        this.x = x;
        this.y = y;
        name = "v" + no;
        this.no = no;
    }

    public Vertex(float x, float y, String name){
        this.x = x;
        this.y = y;
        this.name = name;
    }


}
