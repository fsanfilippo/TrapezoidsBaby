package main.input;

import main.structures.DCEL.Vertex;
import main.structures.Segment;

import java.util.*;

public class SegmentReader {

    private static Map<String, Vertex> duplicates = new HashMap<>();
    private static int vertexCount = 0;
    private static int segmentCount = 0;

    public static List<Segment> readFile(Scanner fileScanner){
//        List<Segment> segments = new ArrayList<>();
//        List<Vertex> lastTwoVertices = new ArrayList<>();
//        int vertexCount = 0;
//        while(fileScanner.hasNextLine()){
//            String line = fileScanner.nextLine();
//            if(line.isEmpty()){
//                break;
//            }
//            Scanner lineScanner = new Scanner(stripNonDigits(line));
//
//            while(lineScanner.hasNextInt()){
//                int a1 = lineScanner.nextInt();
//                int a2 = lineScanner.nextInt();
//                Vertex a = getNewOrExisting(a1, a2);
//
//                lastTwoVertices.add(a);
//                if(lastTwoVertices.size() >= 2){
//                    segments.add(new Segment(segmentCount++, lastTwoVertices.get(0), lastTwoVertices.get(1)));
//                    lastTwoVertices.clear();
//                }
//            }
//        }
       return null;
    }

    public static String stripNonDigits(final CharSequence input){
        final StringBuilder sb = new StringBuilder( input.length() );
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
            else{
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static Vertex getNewOrExisting(int x, int y){

        if(duplicates.get(x +  ","  + y) == null){
            Vertex v = new Vertex(x, y, vertexCount++);
            duplicates.put(x + "," + y, v);
            return v;
        }
        return duplicates.get(x +  ","  + y);

    }


}
