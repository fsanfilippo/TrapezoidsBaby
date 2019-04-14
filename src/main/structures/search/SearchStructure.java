package main.structures.search;

import main.Query;
import main.structures.DCEL.Vertex;
import main.structures.Segment;
import main.structures.Trapezoid;

import java.util.ArrayList;
import java.util.List;

public class SearchStructure {

    private Node root;
    private Segment l;
    private Segment r;
    private Segment t;
    private Segment b;
    private int trapCount = 0;

    public SearchStructure(Segment l, Segment r, Segment t, Segment b){
        this.l = l;
        this.r = r;
        this.t = t;
        this.b = b;

        Trapezoid trapRoot = new Trapezoid(b.p, b.q, t, b, trapCount++);
        root = new LeafNode(trapRoot);
    }

    public boolean outSideBoundingBox(Query q){
        if(q.x < l.p.x) return true;
        if(q.y > t.p.y) return true;
        if(q.y < b.p.y) return true;
        if(q.x > r.p.x) return true;
        return false;
    }

    public List<Trapezoid> followSegment(Segment s){
        Trapezoid delta_i = segmentQuery(s, s.p);
        List<Trapezoid> traps = new ArrayList<>();
        traps.add(delta_i);

        Trapezoid delta_k = segmentQuery(s, s.q);

        while(delta_i != delta_k){
            //Multiple Neightbors
            if(delta_i.neighbors.size() > 1 && right_pBelowS(delta_i.rightp, s)){
                //if rightp p of delta_i is below si, then delta_i+1 is the upper neighbor (position 1 in list)
                delta_i = delta_i.neighbors.get(1);
            }
            else{
                delta_i = delta_i.neighbors.get(0);
            }
            traps.add(delta_i);
        }
        return traps;

    }

    public Trapezoid query(Query q){
        if(outSideBoundingBox(q)) return null;

        Node cur = root;
        while(cur.getClass() != LeafNode.class){
            if(cur.getClass() == YNode.class){
                cur = below((YNode) cur, q) ? cur.rChild : cur.lChild;
            }
            else if(cur.getClass() == XNode.class){
                cur = right((XNode) cur, q) ? cur.rChild : cur.lChild;
            }
        }
        return ((LeafNode) cur).trapezoid;
    }

    public Trapezoid segmentQuery(Segment s, Vertex v){

        Node cur = root;
        while(cur.getClass() != LeafNode.class){
            if(cur instanceof YNode){
                cur = segmentBelow((YNode) cur, s, v) ? cur.rChild : cur.lChild;
            }
            else if(cur instanceof XNode){
                cur =  right(((XNode) cur), new Query(v.x, v.y)) ? cur.rChild : cur.lChild;
            }
        }
        return ((LeafNode) cur).trapezoid;
    }

    private boolean below(YNode node, Query query){
        Vertex p = node.s.p;
        Vertex q = node.s.q;
        float kx = query.x;
        float m = (p.y - q.y)/(p.x - q.x);
        float ky = m * (kx - p.x) + p.y;
        return query.y < ky;
    }

    private boolean right(XNode node, Query query){
        return query.x > node.vertex.x;
    }

    private boolean segmentBelow(YNode node, Segment s, Vertex v){
        Segment n = node.s;
        if(n.p == v){
            return (n.p.y - n.q.y)/(n.p.x - n.q.x) < (b.p.y - b.q.y)/(b.p.x - b.q.x);
        }
        return below(node, new Query(v.x, v.y));
    }

    private boolean right_pBelowS(Vertex p, Segment s){
        float m = (s.p.y - s.q.y)/(s.p.x - s.q.x);
        float sy = m * (p.x - s.p.x) + s.p.y;
        return p.y < sy;
    }


}
