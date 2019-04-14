package main.structures.DCEL;

import java.util.List;

public class DCEL {

    public List<Vertex> vertices;
    public List<HalfEdge> halfEdges;
    public List<Face> faces;

    public DCEL(List<Vertex> vertices, List<HalfEdge> halfEdges, List<Face> faces){
        this.vertices = vertices;
        this.faces = faces;
        this.halfEdges = halfEdges;
    }

    public void print(){
        System.out.println("~~~~ VERTICES ~~~~");
        for(Vertex v: vertices){
            System.out.println(v.name + " ( " + v.x + "," + v.y + " ) " + v.incidentEdge.name);
        }

        System.out.println("~~~~ FACES ~~~~");
        for(Face f: faces){
            String outer = f.outerComponent != null ? f.outerComponent.name : "null";
            String inner = f.innerComponent != null ? f.innerComponent.name : "null";
            System.out.println(f.name + " " + outer + " " + inner);
        }

        System.out.println("~~~~ HALF-EDGES ~~~~");
        for(HalfEdge h: halfEdges){
            System.out.println(h.name + " " + h.origin.name + " " + h.twin.name +
                    " " + h.incidentFace.name + " " + h.next.name + " " + h.prev.name);
        }

    }


}
