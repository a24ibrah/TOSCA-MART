package mart.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import mart.TOSCAMart;

public class Old_Test {

    public static void main(String[] args) throws FileNotFoundException {
        //Number of iterations to be performed (per test)
        int n = 5;
        int r = 40;

        //=====================================
        // Redirection of System.out/err
        //=====================================
        File testLogFile = new File("test.log");
        FileOutputStream testLogOutStream = new FileOutputStream(testLogFile);
        PrintStream testLogPrintStream = new PrintStream(testLogOutStream);
        PrintStream console = System.out;
        System.setOut(testLogPrintStream);
        System.setErr(testLogPrintStream);

        //=====================================
        // Processing of cmd line args
        //=====================================
        if(args.length<4) {
            System.err.println("[Test]: Too few command line arguments.");
            return;
        }
        //NodeType location
        String nodeTypeLoc = args[0];
        System.out.println("Target node location: [" + nodeTypeLoc + "]");

        //ServiceTemplate locations
        String servTempMatched = args[1];
        String servTempMatched2 = args[2];
        String servTempUnmatched = args[3];
        int howManyImplementations = Integer.parseInt(args[4]);

        //=====================================
        // TEST 1: r services in "Repo",
        // ("n", "t" fixed by available
        //   node and services)
        //=====================================
        console.print("Test 1: ");
        long elapsedTime;

        System.out.println();
        System.out.println("|=============================|");
        System.out.println("|=== TEST 1: |Repo|=r ========|");
        System.out.println("|=== (n,t fixed)==============|");
        System.out.println("|=============================|");
        List<String> servTempLocsTest1 = new ArrayList<String>();
        for(int i=0; i<r; i++) {
        	if(i%10 == 0)
        		servTempLocsTest1.add(servTempMatched);
        	else
        		servTempLocsTest1.add(servTempUnmatched);
        }

        //Test repeated N times
        for(int i=1; i<=n; i++) {
            console.print(i + ",");
            if(i<10)
                System.out.println("\n<-----Iteration n. 0"+i+"--------->");
            else
                System.out.println("\n<-----Iteration n. "+i+"--------->");
            elapsedTime = System.currentTimeMillis();
            TOSCAMart.run(nodeTypeLoc,servTempLocsTest1,howManyImplementations);
            elapsedTime = System.currentTimeMillis() - elapsedTime;
            System.out.println("\n Total Elapsed Time: [" + (elapsedTime / 1000) + "s," + (elapsedTime % 1000) + "ms]");
        }
        console.println("FINISHED!");

        //=====================================
        // TEST 2: 2*r services in "Repo",
        // ("n", "t" fixed by available
        //   node and services)
        //=====================================
        console.print("Test 2: ");
        System.out.println();
        System.out.println("|=============================|");
        System.out.println("|=== TEST 2: |Repo|=2*r ======|");
        System.out.println("|=== (n,t fixed)==============|");
        System.out.println("|=============================|");
        List<String> servTempLocsTest2 = new ArrayList<String>();
        for(int i=0; i<2*r; i++) {
        	if(i%10 == 0)
        		servTempLocsTest2.add(servTempMatched);
        	else
        		servTempLocsTest2.add(servTempUnmatched);
        }
        //Test repeated N times
        for(int i=1; i<=n; i++) {
            console.print(i+",");
            if(i<10)
                System.out.println("\n<-----Iteration n. 0"+i+"--------->");
            else
                System.out.println("\n<-----Iteration n. "+i+"--------->");
            elapsedTime = System.currentTimeMillis();
            TOSCAMart.run(nodeTypeLoc,servTempLocsTest2,howManyImplementations);
            elapsedTime = System.currentTimeMillis() - elapsedTime;
            System.out.println("\n Total Elapsed Time: [" + (elapsedTime / 1000) + "s," + (elapsedTime % 1000) + "ms]");
        }
        console.println("FINISHED!");

        //=====================================
        // TEST 3: r services in "Repo",
        //         "t" doubled.
        // ("n" fixed by available node)
        //=====================================
        console.print("Test 3: ");
        System.out.println();
        System.out.println("|=============================|");
        System.out.println("|=== TEST 3: |Repo|=r ========|");
        System.out.println("|=== t doubled================|");
        System.out.println("|=== (n fixed)================|");
        System.out.println("|=============================|");
        List<String> servTempLocsTest3 = new ArrayList<String>();
        //System.out.println("Service Locations: ");
        for(int i=0; i<r; i++){
        	if(i%10 == 0)
        		servTempLocsTest3.add(servTempMatched2);
        	else
        		servTempLocsTest3.add(servTempUnmatched);
        }
        //Test repeated N times
        for(int i=1; i<=n; i++) {
            console.print(i+",");
            if(i<10)
                System.out.println("\n<-----Iteration n. 0"+i+"--------->");
            else
                System.out.println("\n<-----Iteration n. "+i+"--------->");
            elapsedTime = System.currentTimeMillis();
            TOSCAMart.run(nodeTypeLoc,servTempLocsTest3,howManyImplementations);
            elapsedTime = System.currentTimeMillis() - elapsedTime;
            System.out.println("\n Total Elapsed Time: [" + (elapsedTime / 1000) + "s," + (elapsedTime % 1000) + "ms]");
        }
        console.println("FINISHED!");
    }
}
