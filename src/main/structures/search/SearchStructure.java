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
    private float epsilon = 0.00001f;

    public SearchStructure(Segment l, Segment r, Segment t, Segment b, Trapezoid trapRoot){
        this.l = l;
        this.r = r;
        this.t = t;
        this.b = b;

        root = new LeafNode(trapRoot);
    }

    public boolean outSideBoundingBox(Query q){
        if(q.x < l.p.x) return true;
        if(q.y > t.p.y) return true;
        if(q.y < b.p.y) return true;
        if(q.x > r.p.x) return true;
        return false;
    }

    //Get the list of trapezoids intersected by a segment
    public List<Trapezoid> followSegment(Segment s){
        Trapezoid delta_i = segmentQuery(s, s.p, true);
        List<Trapezoid> traps = new ArrayList<>();
        traps.add(delta_i);

        Trapezoid delta_k = segmentQuery(s, s.q, false);

        while(delta_i != delta_k){
            //Multiple Neighbors
            if(delta_i.rightNeighbors.size() > 1 && right_pBelowS(delta_i.rightp, s)){
                //if rightp p of delta_i is below si, then delta_i+1 is the upper neighbor (position 1 in list)
                delta_i = delta_i.rightNeighbors.get(1);
            }
            else{
                delta_i = delta_i.rightNeighbors.get(0);
            }
            traps.add(delta_i);
        }
        return traps;

    }

    public QueryResponse query(Query q){
        if(outSideBoundingBox(q)) return null;

        Node cur = root;
        while(!(cur instanceof LeafNode)){
            if(cur instanceof YNode){
                YNode yCur = (YNode) cur;
                if(isVertex(yCur.s.p, q))
                    return new QueryResponse(ResponseType.VERTEX,yCur.s.p);
                if(isVertex(yCur.s.q, q))
                    return new QueryResponse(ResponseType.VERTEX,yCur.s.q);
                if(onSegment(yCur, q))
                    return new QueryResponse(ResponseType.SEGMENT, ((YNode)cur).s);
                cur = below((YNode) cur, q) ? cur.rChild : cur.lChild;
            }
            else if(cur instanceof XNode){
                if(isVertex(((XNode) cur).vertex, q))
                    return new QueryResponse(ResponseType.VERTEX,((XNode)cur).vertex);
                cur = rightOrOn((XNode) cur, q) ? cur.rChild : cur.lChild;
            }
        }
        return new QueryResponse(ResponseType.TRAPEZOID, ((LeafNode) cur).trapezoid);
    }

    private boolean below(YNode node, Query query){

        if(isVertical(node.s.p, node.s.q)){
            //We know point isn't on line because we onSegment returned false prior
            if(query.y < node.s.p.y) return true;
            return false;
        }
        Vertex p = node.s.p;
        Vertex q = node.s.q;
        float kx = query.x;
        float m = (p.y - q.y)/(p.x - q.x);
        float ky = m * (kx - p.x) + p.y;
        return query.y < ky;
    }

    private boolean rightOrOn(XNode node, Query query){
        return query.x > node.vertex.x || Math.abs(query.x - node.vertex.x) < epsilon;
    }
    private boolean right(XNode node, Query query){
        return query.x > node.vertex.x;
    }

    private boolean isVertex(Vertex v, Query query){
        return (Math.abs(v.x - query.x) < epsilon && Math.abs(v.y - query.y) < epsilon);
    }

    private boolean onSegment(YNode node, Query query){

        if(isVertical(node.s.p, node.s.q))
            if(query.y < node.s.q.y && query.y > node.s.p.y) return true;

        Vertex p = node.s.p;
        Vertex q = node.s.q;
        float kx = query.x;
        float m = (p.y - q.y)/(p.x - q.x);
        float ky = m * (kx - p.x) + p.y;
        return Math.abs(query.y - ky) < epsilon;
    }

    public Trapezoid segmentQuery(Segment s, Vertex v, boolean p){
        return segmentQueryNode(s, v, p).trapezoid;
    }

    private boolean segmentBelowP(YNode node, Segment s, Vertex v){
        Segment n = node.s;
        if(n.p == v){
            if(isVertical(node.s.p, node.s.q)) return true;
            if(isVertical(s.p, s.q)) return false;
            return (s.p.y - s.q.y)/(s.p.x - s.q.x) < (n.p.y - n.q.y)/(n.p.x - n.q.x);
        }
        return below(node, new Query(v.x, v.y));
    }

    private boolean segmentBelowQ(YNode node, Segment s, Vertex v){
        Segment n = node.s;
        if(n.q == v){
            if(isVertical(node.s.p, node.s.q)) return true;
            if(isVertical(s.p, s.q)) return false;
            return (s.p.y - s.q.y)/(s.p.x - s.q.x) > (n.p.y - n.q.y)/(n.p.x - n.q.x);
        }
        return below(node, new Query(v.x, v.y));
    }


    private boolean right_pBelowS(Vertex p, Segment s){
        float m = (s.p.y - s.q.y)/(s.p.x - s.q.x);
        float sy = m * (p.x - s.p.x) + s.p.y;
        return p.y < sy;
    }

    public LeafNode segmentQueryNode(Segment s, Vertex v, boolean p){
        Node cur = root;
        while(!(cur instanceof LeafNode)){
            if(cur instanceof YNode){
                if(p)
                    cur = segmentBelowP((YNode) cur, s, v) ? cur.rChild : cur.lChild;
                else
                    cur = segmentBelowQ((YNode) cur, s, v) ? cur.rChild : cur.lChild;
            }
            else if(cur instanceof XNode){
                //If a q endpoint lies on a vertical extension, we say it lies on the left trapezoid
                if(p)
                    cur = rightOrOn(((XNode) cur), new Query(v.x, v.y)) ? cur.rChild : cur.lChild;
                else
                    cur = right(((XNode) cur), new Query(v.x, v.y)) ? cur.rChild : cur.lChild;
            }
        }
        return ((LeafNode) cur);
    }

    private boolean isVertical(Vertex p, Vertex q){
        return Math.abs(p.x - q.x) < epsilon;
    }

    public void replaceAtRoot(Node n){
        root = n;
    }

}
