package main.structures;
import main.input.PQComparator;
import main.structures.DCEL.HalfEdge;
import main.structures.DCEL.Vertex;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Segment {
    public String name;
    public Vertex p;
    public Vertex q;
    public HalfEdge[] halfEdges = new HalfEdge[2]; //Should only be two

    public Segment(int no, HalfEdge e1, HalfEdge e2) {
        List<Vertex> pl = new ArrayList<>();
        halfEdges[0] = e1;
        halfEdges[1] = e2;
        pl.add(e1.origin);
        pl.add(e2.origin);
        Collections.sort(pl, new PQComparator());
        p = pl.get(0);
        q = pl.get(1);
        name = "s" + no;
    }

    public Segment(Vertex a, Vertex b, String name){
        List<Vertex> pl = new ArrayList<>();
        pl.add(a);
        pl.add(b);
        Collections.sort(pl, new PQComparator());
        p = pl.get(0);
        q = pl.get(1);
        this.name = name;
    }

    public String toString(){
        return name;
    }


    public String queryString(){

        if(name.equals("T") || name.equals("B") || name.equals("R") || name.equals("L")){
            return name;
        }
        return "( " + p.queryString() + ", " + q.queryString() + ")";
    }
}
