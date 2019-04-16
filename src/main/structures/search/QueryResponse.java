package main.structures.search;

import main.structures.DCEL.Vertex;
import main.structures.Segment;
import main.structures.Trapezoid;

public class QueryResponse {

    public ResponseType type;
    public Trapezoid t;
    public Segment s;
    public Vertex v;

    QueryResponse(ResponseType rt, Trapezoid t){
        this.type = rt;
        this.t = t;
    }
    QueryResponse(ResponseType rt, Segment s){
        this.type = rt;
        this.s = s;
    }
    QueryResponse(ResponseType rt, Vertex v){
        this.type = rt;
        this.v = v;
    }
}
