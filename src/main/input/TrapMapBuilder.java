package main.input;
import main.structures.DCEL.DCEL;
import main.structures.DCEL.Face;
import main.structures.DCEL.HalfEdge;
import main.structures.DCEL.Vertex;
import main.structures.Trapezoid;
import main.structures.TrapezoidMap;
import main.structures.search.*;
import main.structures.Segment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrapMapBuilder {

    private static float epsilon = 0.0001f;

    public static TrapezoidMap buildTrapMap(DCEL dcel) {
        List<Segment> segments = getSegments(dcel.halfEdges);
        Face unbounded = getUnboundedFace(dcel.faces);
        Map<String, Segment> bb = getBoundingBox(segments, unbounded);
        List<Segment> boundingList = new ArrayList<>(bb.values());
        Segment l = bb.get("L");
        Segment r = bb.get("R");
        Segment t = bb.get("T");
        Segment b = bb.get("B");
        Trapezoid trapRoot = new Trapezoid(b.p, b.q, t, b);
        SearchStructure ss = new SearchStructure(l, r, t, b, trapRoot);
        TrapezoidMap trapMap = new TrapezoidMap(ss, boundingList, trapRoot);

        for (Segment s : segments) {
            trapMap.segments.add(s);

            //Find the set ∆0,∆1,...,∆k of trapezoids in T properly intersected
            //by si.
            List<Trapezoid> intersecting = ss.followSegment(s);

            //Remove ∆0,∆1,...,∆k from T and replace
            //them by the new trapezoids that appear because of the insertion of si.
            trapMap.traps.removeAll(intersecting);

            if (intersecting.size() == 1) {
                handleOneIntersectingTrap(trapMap, s, intersecting, ss);
            } else {
                handleManyIntersectingTrap(trapMap, s, intersecting, ss);
            }
        }

//        for(Trapezoid trap: trapMap.traps){
//            Segment top = trap.top;
//            HalfEdge e1 = top.halfEdges[0];
//            HalfEdge e2 = top.halfEdges[1];
//            if(e2.origin.x > e2.next.origin.x){
//                trap.containingFace = e2.incidentFace;
//            }
//            else{
//                trap.containingFace = e1.incidentFace;
//            }
//        }

        return trapMap;
    }

    private static Map<String, Segment> getBoundingBox(List<Segment> segments, Face unbounded) {
        float bottom = Float.MAX_VALUE;
        float left = Float.MAX_VALUE;
        float right = Float.MIN_VALUE;
        float top = Float.MIN_VALUE;

        for (Segment s : segments) {
            left = (s.p.x < left) ? s.p.x : left;
            left = (s.q.x < left) ? s.q.x : left;

            bottom = (s.p.y < bottom) ? s.p.y : bottom;
            bottom = (s.q.y < bottom) ? s.q.y : bottom;

            right = (s.p.x > right) ? s.p.x : right;
            right = (s.q.x > right) ? s.q.x : right;

            top = (s.p.y > top) ? s.p.y : top;
            top = (s.q.y > top) ? s.q.y : top;
        }

        //The four vertices of R, starting at the upper left corner,
        // are named counterclockwise as c1, c2, c3, and c4.
        Vertex tl = new Vertex(left - 1.0f, top + 1.0f, "c1");
        Vertex bl = new Vertex(left - 1.0f, bottom - 1.0f, "c2");
        Vertex br = new Vertex(right + 1.0f, bottom - 1.0f, "c3");
        Vertex tr = new Vertex(right + 1.0f, top + 1.0f, "c4");

        //The left, right, top, and bottom sides of the bounding box R for
        // all the segments are named L, R, T, and B, respectively.
        Segment l = new Segment(bl, tl, "L");
        Segment r = new Segment(br, tr, "R");
        Segment t = new Segment(tr, tl, "T");
        Segment b = new Segment(br, bl, "B");

        Map<String, Segment> R = new HashMap<>();
        R.put(l.name, l);
        R.put(r.name, r);
        R.put(t.name, t);
        R.put(b.name, b);

        return R;
    }

    //transforms halfEdges into segments
    private static List<Segment> getSegments(List<HalfEdge> halfEdges) {
        List<HalfEdge> accountedFor = new ArrayList<>();
        List<Segment> segments = new ArrayList<>();
        int segmentCount = 0;
        for (HalfEdge h : halfEdges) {
            if (!accountedFor.contains(h)) {
                Segment s = new Segment(segmentCount++, h, h.twin);
                accountedFor.add(h);
                accountedFor.add(h.twin);
                segments.add(s);
                h.segment = s;
                h.twin.segment = s;
            }
        }
        return segments;
    }

    private static Face getUnboundedFace(List<Face> faces) {
        for (Face f : faces) {
            if (f.outerComponent == null) return f;
        }
        return null;
    }

//    private static boolean endPointNotContainedInTrap(Vertex trapVertex, Vertex segEndPoint) {
//        if (trapVertex == segEndPoint) return true;
//        if (Math.abs(trapVertex.x - segEndPoint.x) < epsilon) return true;
//        return false;
//    }

    private static void handleOneIntersectingTrap(TrapezoidMap trapMap, Segment s, List<Trapezoid> intersecting, SearchStructure ss) {
        Trapezoid old = intersecting.get(0);
        LeafNode toReplace = ss.segmentQueryNode(s, s.p, true);
        List<Node> parentsOfReplace = toReplace.parents;
        Node subRoot;

        if (old.leftp == s.p) {
            if (old.rightp == s.q) {
                // Both endpoints of s are already in T
                // Therefore only create two new trapezoids
                Trapezoid A; // Above s
                Trapezoid B; // Below s

                Vertex[] left = setLeftRightp(old.leftp, s.p);
                Vertex[] right = setLeftRightp(old.rightp, s.q);

                Vertex leftp_A = left[0];
                Vertex leftp_B = left[1];
                Vertex rightp_A = right[0];
                Vertex rightp_B = right[1];

                A = new Trapezoid(leftp_A, rightp_A, old.top, s);
                B = new Trapezoid(leftp_B, rightp_B, s, old.bottom);

                // Set left neighbors
                setLeftNeighbors_spOnVert(A, B, old, s);
                // Set right neighbors
                setRightNeighbors_sqOnVert(A, B, old, s);

                // Set left neighbors of old traps
                setNeighborsForOldTraps_sqOnVert(A, B, old);

                //Set right neighbors of old traps
                setNeighborsForOldTraps_spOnVert(A, B, old);

                trapMap.traps.add(A);
                trapMap.traps.add(B);

                //Add new nodes to the search structure
                subRoot = new YNode(s);
                subRoot.parents.addAll(parentsOfReplace);
                subRoot.lChild = new LeafNode(A);
                A.node = (LeafNode) subRoot.lChild;
                subRoot.lChild.parents.add(subRoot);
                subRoot.rChild = new LeafNode(B);
                B.node = (LeafNode) subRoot.rChild;
                subRoot.rChild.parents.add(subRoot);

            } else {
                // Left endpoint of s lies is already in T but not right endpoint
                // Therefore create three trapezoids
                Trapezoid A; // Above s
                Trapezoid B; // Below s
                Trapezoid C; // Right of s

                Vertex[] left = setLeftRightp(old.leftp, s.p);
                Vertex leftp_A = left[0];
                Vertex leftp_B = left[1];
                Vertex leftp_C = s.q;
                Vertex rightp_A = s.q;
                Vertex rightp_B = s.q;
                Vertex rightp_C = old.rightp;

                A = new Trapezoid(leftp_A, rightp_A, old.top, s);
                B = new Trapezoid(leftp_B, rightp_B, s, old.bottom);
                C = new Trapezoid(leftp_C, rightp_C, old.top, old.bottom);

                // Add left neighbors
                setLeftNeighbors_spOnVert(A, B, old, s);
                C.leftNeighbors.add(B);
                C.leftNeighbors.add(A);

                // Add right neighbors
                A.rightNeighbors.add(C);
                B.rightNeighbors.add(C);
                C.rightNeighbors.addAll(old.rightNeighbors);

                // Set left neighbors of old traps
                setNeighborsForOldTraps_sqNOTonVert(C, old);

                // Set right neighbors of old traps
                setNeighborsForOldTraps_spOnVert(A, B, old);

                trapMap.traps.add(A);
                trapMap.traps.add(B);
                trapMap.traps.add(C);

                // Add new nodes to the search structure
                subRoot = new XNode(s.q);
                subRoot.parents.addAll(parentsOfReplace);
                subRoot.rChild = new LeafNode(C);
                C.node = (LeafNode) subRoot.rChild;
                subRoot.rChild.parents.add(subRoot);

                Node si = new YNode(s);
                si.lChild = new LeafNode(A);
                si.lChild.parents.add(si);
                A.node = (LeafNode) si.lChild;

                si.rChild = new LeafNode(B);
                B.node = (LeafNode) si.rChild;
                si.rChild.parents.add(si);

                subRoot.lChild = si;
                si.parents.add(subRoot);

            }
        } else {
            if (old.rightp == s.q) {
                // right endpoint of s is already in T but not left endpoint
                // Therefore create three trapezoids
                Trapezoid A; // Above s
                Trapezoid B; // Below s
                Trapezoid C; // Left of s

                Vertex[] right = setLeftRightp(old.rightp, s.q);

                Vertex leftp_A = s.p;
                Vertex leftp_B = s.p;
                Vertex leftp_C = old.leftp;
                Vertex rightp_A = right[0];
                Vertex rightp_B = right[1];
                Vertex rightp_C = s.q;

                A = new Trapezoid(leftp_A, rightp_A, old.top, s);
                B = new Trapezoid(leftp_B, rightp_B, s, old.bottom);
                C = new Trapezoid(leftp_C, rightp_C, old.top, old.bottom);

                // Add left neighbors
                C.leftNeighbors.addAll(old.leftNeighbors);
                A.leftNeighbors.add(C);
                B.leftNeighbors.add(C);

                // Add right neighbors
                setRightNeighbors_sqOnVert(A, B, old, s);
                C.rightNeighbors.add(B);
                C.rightNeighbors.add(A);

                // Set left neighbors of old traps
                setNeighborsForOldTraps_sqOnVert(A, B, old);

                // Set right neighbors of old traps
                setNeighborsForOldTraps_spNOTonVert(C, old);

                trapMap.traps.add(A);
                trapMap.traps.add(B);
                trapMap.traps.add(C);

                subRoot = new XNode(s.p);
                subRoot.parents.addAll(parentsOfReplace);
                subRoot.lChild = new LeafNode(C);
                C.node = (LeafNode) subRoot.lChild;
                subRoot.lChild.parents.add(subRoot);

                Node si = new YNode(s);
                si.lChild = new LeafNode(A);
                A.node = (LeafNode) si.lChild;
                si.lChild.parents.add(si);

                si.rChild = new LeafNode(B);
                B.node = (LeafNode) si.rChild;
                si.rChild.parents.add(si);

                subRoot.rChild = si;
                si.parents.add(subRoot);
            } else {
                // s is completely contained in the trapezoid
                // Therefore create four new trapezoids

                Trapezoid A = new Trapezoid(old.leftp, s.p, old.top, old.bottom); // Left of s
                Trapezoid B = new Trapezoid(s.p, s.q, old.top, s); // Above s
                Trapezoid C = new Trapezoid(s.p, s.q, s, old.bottom); // Below s
                Trapezoid D = new Trapezoid(s.q, old.rightp, old.top, old.bottom); // Right of s

                trapMap.traps.add(A);
                trapMap.traps.add(B);
                trapMap.traps.add(C);
                trapMap.traps.add(D);

                // Add left neighbors
                A.leftNeighbors.addAll(old.leftNeighbors);
                B.leftNeighbors.add(A);
                C.leftNeighbors.add(A);
                D.leftNeighbors.add(C);
                D.leftNeighbors.add(B);

                // Add right neighbors
                A.rightNeighbors.add(C);
                A.rightNeighbors.add(B);
                B.rightNeighbors.add(D);
                C.rightNeighbors.add(D);
                D.rightNeighbors.addAll(old.rightNeighbors);

                // Set left neighbors of old traps
                setNeighborsForOldTraps_sqNOTonVert(D, old);

                // Set right neighbors of old traps
                setNeighborsForOldTraps_spNOTonVert(A, old);

                Node si = new YNode(s);
                si.lChild = new LeafNode(B);
                B.node = (LeafNode) si.lChild;
                si.lChild.parents.add(si);

                si.rChild = new LeafNode(C);
                C.node = (LeafNode) si.rChild;
                si.rChild.parents.add(si);

                Node qi = new XNode(s.q);
                qi.rChild = new LeafNode(D);
                D.node = (LeafNode) qi.rChild;
                qi.rChild.parents.add(qi);
                qi.lChild = si;
                si.parents.add(qi);

                subRoot = new XNode(s.p);
                subRoot.parents.addAll(parentsOfReplace);

                subRoot.lChild = new LeafNode(A);
                A.node = (LeafNode) subRoot.lChild;
                subRoot.lChild.parents.add(subRoot);
                subRoot.rChild = qi;
                qi.parents.add(subRoot);
            }
        }
        replaceNode(parentsOfReplace, toReplace, subRoot, ss);
    }

    private static void handleManyIntersectingTrap(TrapezoidMap trapMap, Segment s, List<Trapezoid> intersecting, SearchStructure ss) {
        Trapezoid delta0 = intersecting.get(0);
        Trapezoid deltak = intersecting.get(intersecting.size() - 1);

        Map<Trapezoid, List<Trapezoid>> newToOld = new HashMap<>();

        // Replace the leftmost node
        LeafNode toReplaceL = ss.segmentQueryNode(s, s.p, true);
        List<Node> parentsOfReplaceL = toReplaceL.parents;
        YNode subRootL;
        Node toAttach;
        Trapezoid leftMost = null;
        if(s.p == delta0.leftp){
            subRootL = new YNode(s);
            subRootL.parents.addAll(parentsOfReplaceL);
            toAttach = subRootL;
        }
        else{
            leftMost = new Trapezoid(delta0.leftp, s.p, delta0.top, delta0.bottom);
            trapMap.traps.add(leftMost);
            Node pi = new XNode(s.p);
            pi.parents.addAll(parentsOfReplaceL);
            LeafNode leftMostLeaf = new LeafNode(leftMost);
            leftMost.node = leftMostLeaf;
            leftMostLeaf.parents.add(pi);
            pi.lChild = leftMostLeaf;

            subRootL = new YNode(s);
            pi.rChild = subRootL;
            subRootL.parents.add(pi);
            toAttach = pi;
        }
        replaceNode(parentsOfReplaceL, toReplaceL, toAttach, ss);


        // Replace the rightmost node
        LeafNode toReplaceR = ss.segmentQueryNode(s, s.q, false);
        List<Node> parentsOfReplaceR = toReplaceR.parents;
        Node subRootR;
        Trapezoid rightMost = null;
        if(s.q == deltak.rightp){
            subRootR = new YNode(s);
            subRootR.parents.addAll(parentsOfReplaceR);
            toAttach = subRootR;
        }
        else{
            rightMost = new Trapezoid(s.q, deltak.rightp, deltak.top, deltak.bottom);
            trapMap.traps.add(rightMost);
            Node qi = new XNode(s.q);
            qi.parents.addAll(parentsOfReplaceR);
            LeafNode rightMostLeaf = new LeafNode(rightMost);
            rightMost.node = rightMostLeaf;
            rightMostLeaf.parents.add(qi);
            qi.rChild = rightMostLeaf;

            subRootR = new YNode(s);
            qi.lChild = subRootR;
            subRootR.parents.add(qi);

            toAttach = qi;
        }
        replaceNode(parentsOfReplaceR, toReplaceR, toAttach, ss);

        // Replace all inbetween nodes with YNodes
        Map<Trapezoid, YNode> newSegmentNodes = new HashMap<>();
        for(Trapezoid replace: intersecting.subList(1, intersecting.size() - 1)){
            YNode segmentNode = new YNode(s);
            Node replacing  = replace.node;
            List<Node> replacingParents = replacing.parents;

            replaceNode(replacingParents, replacing, segmentNode, ss);
            newSegmentNodes.put(replace, segmentNode);
        }


        List<Trapezoid> topTraps = new ArrayList<>();
        // Find all top trapezoids
        int intersectingIndex = 0;
        List<Trapezoid> replacing = findNewUpperTraps(intersectingIndex, intersecting, s);
        Vertex rightp = replacing.get(replacing.size() - 1).rightp;
        Trapezoid newTrap = new Trapezoid(s.p, rightp, delta0.top, s);
        List l = new ArrayList<>();
        l.add(delta0);
        l.add(replacing.get(replacing.size() - 1));
        newToOld.put(newTrap, l);
        trapMap.traps.add(newTrap);
        topTraps.add(newTrap);
        LeafNode newLeaf = new LeafNode(newTrap);
        newTrap.node = newLeaf;

        int replaceIndex = 0;
        intersectingIndex++;

        boolean firstLoop = true;
        while(replacing.get(replaceIndex) != intersecting.get(intersecting.size() - 1)){
            Trapezoid t = null;
            if(firstLoop){
                subRootL.lChild = newLeaf;
                newLeaf.parents.add(subRootL);
            }
            else{
                t = replacing.get(replaceIndex);
                YNode setChild = newSegmentNodes.get(t);
                setChild.lChild = newLeaf;
                newLeaf.parents.add(setChild);
            }
            replaceIndex++;
            if(replaceIndex == replacing.size()){
                replacing = findNewUpperTraps(intersectingIndex, intersecting, s);
                rightp = replacing.get(replacing.size() - 1).rightp;
                newTrap = new Trapezoid(replacing.get(0).leftp, rightp, replacing.get(0).top, s);
                l.add(replacing.get(0));
                l.add(replacing.get(replacing.size() - 1));
                newToOld.put(newTrap, l);
                trapMap.traps.add(newTrap);
                topTraps.add(newTrap);
                newLeaf = new LeafNode(newTrap);
                newTrap.node = newLeaf;
                replaceIndex = 0;
            }
            intersectingIndex++;
            firstLoop = false;
        }

        subRootR.lChild = newLeaf;
        newLeaf.parents.add(subRootR);


        List<Trapezoid> bottomTraps = new ArrayList<>();
        // Find all bottom trapezoids
        intersectingIndex = 0;
        replacing = findNewLowerTraps(intersectingIndex, intersecting, s);
        rightp = replacing.get(replacing.size() - 1).rightp;
        newTrap = new Trapezoid(s.p, rightp, s, delta0.bottom);

        l = new ArrayList<>();
        l.add(delta0);
        l.add(replacing.get(replacing.size() - 1));
        newToOld.put(newTrap, l);

        bottomTraps.add(newTrap);
        trapMap.traps.add(newTrap);
        newLeaf = new LeafNode(newTrap);
        newTrap.node = newLeaf;

        replaceIndex = 0;
        intersectingIndex++;
        firstLoop = true;
        while(replacing.get(replaceIndex) != intersecting.get(intersecting.size() - 1)){
            Trapezoid t = null;
            if(firstLoop){
                subRootL.rChild = newLeaf;
                newLeaf.parents.add(subRootL);
            }
            else{
                t = replacing.get(replaceIndex);
                YNode setChild = newSegmentNodes.get(t);
                setChild.rChild = newLeaf;
                newLeaf.parents.add(setChild);
            }
            replaceIndex++;
            if(replaceIndex == replacing.size()){
                replacing = findNewLowerTraps(intersectingIndex, intersecting, s);
                rightp = replacing.get(replacing.size() - 1).rightp;
                newTrap = new Trapezoid(replacing.get(0).leftp, rightp,s, replacing.get(0).bottom);
                l = new ArrayList<>();
                l.add(replacing.get(0));
                l.add(replacing.get(replacing.size() - 1));
                newToOld.put(newTrap, l);
                trapMap.traps.add(newTrap);
                bottomTraps.add(newTrap);
                newLeaf = new LeafNode(newTrap);
                newTrap.node = newLeaf;
                replaceIndex = 0;
            }
            intersectingIndex++;
            firstLoop = false;
        }

        subRootR.rChild = newLeaf;
        newLeaf.parents.add(subRootR);

        // Add neighbors
        if(s.p == delta0.leftp){
            setLeftNeighbors_spOnVert(topTraps.get(0), bottomTraps.get(0),delta0, s);
            setNeighborsForOldTraps_spOnVert(topTraps.get(0), bottomTraps.get(0),delta0);
        }
        else{
            leftMost.leftNeighbors.addAll(delta0.leftNeighbors);

            leftMost.rightNeighbors.add(bottomTraps.get(0));
            leftMost.rightNeighbors.add(topTraps.get(0));
            topTraps.get(0).leftNeighbors.add(leftMost);
            bottomTraps.get(0).leftNeighbors.add(leftMost);
            setNeighborsForOldTraps_spNOTonVert(leftMost, delta0);
        }

        if(s.q == deltak.rightp){
            setRightNeighbors_sqOnVert(topTraps.get(topTraps.size() - 1), bottomTraps.get(bottomTraps.size() - 1),deltak, s);
            setNeighborsForOldTraps_sqOnVert(topTraps.get(topTraps.size() - 1), bottomTraps.get(bottomTraps.size() - 1), deltak);
        }
        else{
            rightMost.rightNeighbors.addAll(deltak.rightNeighbors);

            rightMost.leftNeighbors.add(bottomTraps.get(bottomTraps.size() - 1));
            rightMost.leftNeighbors.add(topTraps.get(topTraps.size() - 1));

            topTraps.get(topTraps.size() - 1).rightNeighbors.add(rightMost);
            bottomTraps.get(bottomTraps.size() - 1).rightNeighbors.add(rightMost);
            setNeighborsForOldTraps_sqNOTonVert(rightMost, deltak);
        }

        Trapezoid cur;
        Trapezoid next;
        // Link the trapezoids together
        for(int i = 0; i < topTraps.size() - 1; i++){
            cur = topTraps.get(i);
            next = topTraps.get(i + 1);
            List<Trapezoid> oldCur = newToOld.get(cur);
            if(oldCur.get(1).rightNeighbors.size() == 2){
                cur.rightNeighbors.add(next);
                cur.rightNeighbors.add(oldCur.get(1).rightNeighbors.get(1));
            }else{
                cur.rightNeighbors.add(next);
            }

            List<Trapezoid> oldNext = newToOld.get(next);
            if(oldNext.get(0).leftNeighbors.size() == 2){
                next.leftNeighbors.add(cur);
                next.leftNeighbors.add(oldNext.get(0).leftNeighbors.get(1));
                oldNext.get(0).leftNeighbors.get(0).rightNeighbors.set(0, next);
            }else{
                next.leftNeighbors.add(cur);
            }
        }

        // Link the trapezoids together
        for(int i = 0; i < bottomTraps.size() - 1; i++){
            cur = bottomTraps.get(i);
            next = bottomTraps.get(i + 1);
            List<Trapezoid> oldCur = newToOld.get(cur);
            if(oldCur.get(1).rightNeighbors.size() == 2){
                cur.rightNeighbors.add(oldCur.get(1).rightNeighbors.get(0));
                cur.rightNeighbors.add(next);
            }else{
                cur.rightNeighbors.add(next);
            }

            List<Trapezoid> oldNext = newToOld.get(next);
            if(oldNext.get(0).leftNeighbors.size() == 2){
                next.leftNeighbors.add(oldNext.get(0).leftNeighbors.get(0));
                next.leftNeighbors.add(cur);
                oldNext.get(0).leftNeighbors.get(0).rightNeighbors.set(0, next);

            }else{
                next.leftNeighbors.add(cur);
            }
        }

    }


    private static Vertex[] setLeftRightp(Vertex old, Vertex sEndpoint) {
        Vertex A;
        Vertex B;
        if (old == sEndpoint) {
            A = B = sEndpoint;
        } else if (old.y > sEndpoint.y) {
            A = old;
            B = sEndpoint;
        } else {
            A = sEndpoint;
            B = old;
        }
        Vertex AB[] = {A, B};
        return AB;
    }

    private static boolean incident(Vertex v, Segment s) {
        HalfEdge start = v.incidentEdge;
        HalfEdge next = start;
        do {
            next = next.next;
            if (next.segment == s) {
                return true;
            }
        } while (start != next);
        return false;
    }

    private static void setLeftNeighbors_spOnVert(Trapezoid A, Trapezoid B, Trapezoid old, Segment s) {
        if (old.leftp == s.p) {
            if (old.leftNeighbors.size() == 1) {
                Trapezoid oln = old.leftNeighbors.get(0);
                if (!incident(s.p, old.top)) {
                    A.leftNeighbors.add(oln);
                }
                if (!incident(s.p, old.bottom)) {
                    B.leftNeighbors.add(oln);
                }
            } else if (old.leftNeighbors.size() == 2) {
                Trapezoid olnb = old.leftNeighbors.get(0);
                Trapezoid olnt = old.leftNeighbors.get(1);
                A.leftNeighbors.add(olnt);
                B.leftNeighbors.add(olnb);
            }
            return;
        }
        throw new Error("FUCK");
//        else if(old.leftp.y > s.p.y){
//            if(old.leftNeighbors.size() == 1){
//                Trapezoid oln = old.leftNeighbors.get(0);
//                A.leftNeighbors.add(oln);
//                B.leftNeighbors.add(oln);
//            }
//            else if(old.leftNeighbors.size() == 2){
//                Trapezoid olnb = old.leftNeighbors.get(0);
//                Trapezoid olnt = old.leftNeighbors.get(1);
//
//                A.leftNeighbors.add(olnb);
//                A.leftNeighbors.add(olnt);
//                B.leftNeighbors.add(olnb);
//            }
//        }
//        else{
//            if(old.leftNeighbors.size() == 1){
//                Trapezoid oln = old.leftNeighbors.get(0);
//                A.leftNeighbors.add(oln);
//                B.leftNeighbors.add(oln);
//            }
//            else if(old.leftNeighbors.size() == 2){
//                Trapezoid olnb = old.leftNeighbors.get(0);
//                Trapezoid olnt = old.leftNeighbors.get(1);
//
//               A.leftNeighbors.add(olnt);
//               B.leftNeighbors.add(olnb);
//               B.leftNeighbors.add(olnt);
//            }
//        }
    }

    private static void setRightNeighbors_sqOnVert(Trapezoid A, Trapezoid B, Trapezoid old, Segment s) {
        //Should always occur
        if (old.rightp == s.q) {
            if (old.rightNeighbors.size() == 1) {
                Trapezoid orn = old.rightNeighbors.get(0);
                if (!incident(s.q, old.top)) {
                    A.rightNeighbors.add(orn);
                }
                if (!incident(s.p, old.bottom)) {
                    B.rightNeighbors.add(orn);
                }
            } else if (old.rightNeighbors.size() == 2) {
                Trapezoid olnb = old.rightNeighbors.get(0);
                Trapezoid olnt = old.rightNeighbors.get(1);
                A.rightNeighbors.add(olnt);
                B.rightNeighbors.add(olnb);
            }
            return;
        }
        throw new Error("FUCK");
//        else if(old.rightp.y > s.q.y){
//            if(old.rightNeighbors.size() == 1){
//                Trapezoid orn = old.rightNeighbors.get(0);
//                A.rightNeighbors.add(orn);
//                B.rightNeighbors.add(orn);
//            }
//            else if(old.rightNeighbors.size() == 2){
//                Trapezoid ornb = old.rightNeighbors.get(0);
//                Trapezoid ornt = old.rightNeighbors.get(1);
//
//                A.rightNeighbors.add(ornb);
//                A.rightNeighbors.add(ornt);
//                B.rightNeighbors.add(ornb);
//            }
//        }
//        else{
//            if(old.rightNeighbors.size() == 1){
//                Trapezoid orn = old.rightNeighbors.get(0);
//                A.rightNeighbors.add(orn);
//                B.rightNeighbors.add(orn);
//            }
//            else if(old.rightNeighbors.size() == 2){
//                Trapezoid ornb = old.rightNeighbors.get(0);
//                Trapezoid ornt = old.rightNeighbors.get(1);
//
//                A.rightNeighbors.add(ornt);
//                B.rightNeighbors.add(ornb);
//                B.rightNeighbors.add(ornt);
//            }
//        }
//
    }

    public static void setNeighborsForOldTraps_spOnVert(Trapezoid A, Trapezoid B, Trapezoid old) {
        if (old.leftNeighbors.size() != 0) {
            if (old.leftNeighbors.size() == 1) {
                Trapezoid oln = old.leftNeighbors.get(0);
                if (oln.rightNeighbors.size() == 2) {
                    if (oln.rightNeighbors.get(0) == old) {
                        oln.rightNeighbors.set(0, B);
                    } else {
                        oln.rightNeighbors.set(1, A);
                    }
                } else {
                    if(B.leftNeighbors.get(0) == oln){
                        oln.rightNeighbors.set(0, B);
                    }
                    else{
                        oln.rightNeighbors.set(0, A);
                    }
                }
            } else {
                Trapezoid olnt = old.leftNeighbors.get(1);
                Trapezoid olnb = old.leftNeighbors.get(0);
                if (olnt.rightNeighbors.size() == 1) {
                    olnt.rightNeighbors.set(0, A);
                } else {
                    throw new Error("I thought this was impossible");
                }
                if (olnb.rightNeighbors.size() == 1) {
                    olnb.rightNeighbors.set(0, B);
                } else {
                    throw new Error("I Thought this was impossible");
                }
            }
        }
    }

    public static void setNeighborsForOldTraps_sqOnVert(Trapezoid A, Trapezoid B, Trapezoid old) {
        if (old.rightNeighbors.size() != 0) {
            if (old.rightNeighbors.size() == 1) {
                Trapezoid oln = old.rightNeighbors.get(0);
                if (oln.leftNeighbors.size() == 2) {
                    if (oln.leftNeighbors.get(0) == old) {
                        oln.leftNeighbors.set(0, B);
                    } else {
                        oln.leftNeighbors.set(1, A);
                    }
                } else {
                    throw new Error("I thought this was impossible");
                }
            } else {
                Trapezoid olnt = old.rightNeighbors.get(1);
                Trapezoid olnb = old.rightNeighbors.get(0);
                if (olnt.leftNeighbors.size() == 1) {
                    olnt.leftNeighbors.set(0, A);
                } else {
                    throw new Error("I thought this was impossible");
                }
                if (olnb.leftNeighbors.size() == 1) {
                    olnb.leftNeighbors.set(0, B);
                } else {
                    throw new Error("I Thought this was impossible");
                }
            }
        }
    }

    public static void setNeighborsForOldTraps_spNOTonVert(Trapezoid A, Trapezoid old) {

        if(old.leftNeighbors.size() == 2){
            Trapezoid olnt = old.leftNeighbors.get(1);
            Trapezoid olnb = old.leftNeighbors.get(0);

            if(olnt.rightNeighbors.size() == 2){
                if(olnt.rightNeighbors.get(0) == old){
                    olnt.rightNeighbors.set(0, A);
                }
                else{
                    olnt.rightNeighbors.set(1, A);
                }
            }
            else if(olnt.rightNeighbors.size() == 1){
                olnt.rightNeighbors.set(0, A);
            }

            if(olnb.rightNeighbors.size() == 2){
                if(olnb.rightNeighbors.get(0) == old){
                    olnb.rightNeighbors.set(0, A);
                }
                else{
                    olnb.rightNeighbors.set(1, A);
                }
            }
            else if(olnb.rightNeighbors.size() == 1){
                olnb.rightNeighbors.set(0, A);
            }
        }
        else if(old.leftNeighbors.size() == 1){
            Trapezoid oln = old.leftNeighbors.get(0);
            if(oln.rightNeighbors.size() == 2){
                if(oln.rightNeighbors.get(0) == old){
                    oln.rightNeighbors.set(0, A);
                }
                else{
                    oln.rightNeighbors.set(1, A);
                }
            }
            else if(oln.rightNeighbors.size() == 1){
                oln.rightNeighbors.set(0, A);
            }
        }
        else{
            // do nothing. Don't need to update anything
        }

    }

    public static void setNeighborsForOldTraps_sqNOTonVert(Trapezoid A, Trapezoid old) {

        if(old.rightNeighbors.size() == 2){
            Trapezoid olnt = old.rightNeighbors.get(1);
            Trapezoid olnb = old.rightNeighbors.get(0);

            if(olnt.leftNeighbors.size() == 2){
                if(olnt.leftNeighbors.get(0) == old){
                    olnt.leftNeighbors.set(0, A);
                }
                else{
                    olnt.leftNeighbors.set(1, A);
                }
            }
            else if(olnt.leftNeighbors.size() == 1){
                olnt.leftNeighbors.set(0, A);
            }

            if(olnb.leftNeighbors.size() == 2){
                if(olnb.leftNeighbors.get(0) == old){
                    olnb.leftNeighbors.set(0, A);
                }
                else{
                    olnb.leftNeighbors.set(1, A);
                }
            }
            else if(olnb.leftNeighbors.size() == 1){
                olnb.leftNeighbors.set(0, A);
            }
        }
        else if(old.rightNeighbors.size() == 1){
            Trapezoid oln = old.rightNeighbors.get(0);
            if(oln.leftNeighbors.size() == 2){
                if(oln.leftNeighbors.get(0) == old){
                    oln.leftNeighbors.set(0, A);
                }
                else{
                    oln.leftNeighbors.set(1, A);
                }
            }
            else if(oln.leftNeighbors.size() == 1){
                oln.leftNeighbors.set(0, A);
            }
        }
        else{
            // do nothing. Don't need to update anything
        }

    }

    public static List<Trapezoid> findNewUpperTraps(int startIndex, List<Trapezoid> intersecting, Segment s){
        List<Trapezoid> ret = new ArrayList<>();
        Trapezoid start = intersecting.get(startIndex);
        ret.add(start);

        // traverse till we find a right p that is above s
        Vertex rightp = start.rightp;
        int i = startIndex;
        while(right_pBelowS(rightp, s) && i != intersecting.size() - 1){
            Trapezoid next = intersecting.get(++i);
            ret.add(next);
            rightp = next.rightp;
        }
        return ret;
    }

    public static List<Trapezoid> findNewLowerTraps(int startIndex, List<Trapezoid> intersecting, Segment s){
        List<Trapezoid> ret = new ArrayList<>();
        Trapezoid start = intersecting.get(startIndex);
        ret.add(start);

        // traverse till we find a right p that is above s
        Vertex rightp = start.rightp;
        int i = startIndex;
        while(!right_pBelowS(rightp, s) && i != intersecting.size() - 1){
            Trapezoid next = intersecting.get(++i);
            ret.add(next);
            rightp = next.rightp;
        }
        return ret;
    }

    private static boolean right_pBelowS(Vertex p, Segment s){
        float m = (s.p.y - s.q.y)/(s.p.x - s.q.x);
        float sy = m * (p.x - s.p.x) + s.p.y;
        boolean ret = p.y < sy;
        return ret;
    }

    private static void replaceNode(List<Node> parents, Node oldChild, Node newChild, SearchStructure ss){
        if (parents.size() == 0) {
            ss.replaceAtRoot(newChild);
        }

        for(Node parent: parents){
            if (oldChild == parent.lChild) {
                parent.lChild = newChild;
            } else {
                parent.rChild = newChild;
            }
        }

    }


}
