package main;

import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;

import main.input.DCELReader;
import main.input.TrapMapBuilder;
import main.structures.DCEL.DCEL;
import main.structures.Segment;
import java.util.List;



public class QueryRunner {

    enum State {
        GETTING_FILE, QUERYING
    }

    private Scanner input;
    private int trialNo;
    private State state;

    public static void main(String[] args){
        QueryRunner qr = new QueryRunner();
        qr.run();
    }

    public QueryRunner(){
        input = new Scanner(System.in);
        trialNo = 0;
        state = State.GETTING_FILE;

    }

    private void run(){
        Scanner fileScanner;
        File segmentsFile;

        println("Point Location Using Trapezoidal Maps");
        println();
        println("Trial" + trialNo + ":");
        while(true){
            while(state == state.GETTING_FILE){

                print("Name of input DCEL file: ");

                try{
                    segmentsFile = new File(input.next());
                    println();
                    fileScanner = new Scanner(segmentsFile);
                    DCELReader reader = new DCELReader(fileScanner);
                    DCEL dcel = reader.readDCEL();
                    dcel.print();
//                    TrapMapBuilder.buildTrapMap(dcel);
//                    state = state.QUERYING;
                    return;
                }catch(FileNotFoundException e){
                    println("This file doesn't exist!");
                }

            }
            while(state == state.QUERYING){

            }
        }


    }

    private void println(String s){
        System.out.println(s);
    }
    private void println(){
        System.out.println();
    }
    private void print(String s){
        System.out.print(s);
    }

    
    
}
