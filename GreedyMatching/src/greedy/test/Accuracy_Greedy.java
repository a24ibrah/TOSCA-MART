package greedy.test;

import greedy.GreedyMatching;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class Accuracy_Greedy {

	private static int numberOfRuns = 300;

	private static String nodeURL = "http://www.di.unipi.it/~soldani/TOSCA-MART/Dataset/NodeTypes/DesiredEnvironment.tosca";
	private static String servURLs = "http://www.di.unipi.it/~soldani/TOSCA-MART/Dataset/ServiceTemplates/AllServTempURLs.txt";
//	private static String nodeURL = "file:///C:/Users/Jacopo/Desktop/TOSCA-MART/Dataset/NodeTypes/DesiredEnvironment.tosca";
//	private static String servURLs = "file:///C:/Users/Jacopo/Desktop/TOSCA-MART/Dataset/ServiceTemplates/AllServTempURLs.txt";
	private static String varyingServURLs = "http://www.di.unipi.it/~soldani/TOSCA-MART/Dataset/ServiceTemplates/VaryingTPPercentage/ServiceTemplates_percTP_%%.txt";

	private static List<String> parseURLsFile(String location) {
		List<String> servTempLocs = new ArrayList<String>();
		try {
			// URL to load
			URL url = new URL(location);

			// Read directly from file url
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String defLine;
			while((defLine = in.readLine())!=null) {
				servTempLocs.add(defLine);
			}
			in.close();
		} catch(Exception e) {
			servTempLocs = null;
		}

		return servTempLocs;
	}
	
	private static List<String> permute(List<String> servTempLocs) {
		//Randomly permute ServiceTemplate locations
		Random r = new Random();
		List<String> locs1 = new ArrayList<String>();
		List<String> locs2 = new ArrayList<String>();
		for(String loc : servTempLocs){
			if(r.nextBoolean())
				locs1.add(loc);
			else
				locs2.add(loc);
		}
		servTempLocs = new ArrayList<String>();
		servTempLocs.addAll(locs1);
		servTempLocs.addAll(locs2);
		
		return servTempLocs;
	}

    public static void main(String[] args) throws Exception {
    	//#################################
    	//#Create a folder for the results#
    	//#################################
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    	Date date = new Date();
    	String resultDirPath = "./Test_Accuracy_Greedy_" + dateFormat.format(date);
    	File resultDir = new File(resultDirPath);
    	resultDir.mkdir();

    	//Parses ServiceTemplate locations
    	List<String> servTempLocs = parseURLsFile(servURLs);
    	
    	//Redirect output to tmp file
    	File tmp = new File("xxx.tmp");
    	PrintStream console = System.out;
    	System.setOut(new PrintStream(tmp));
    	//Other needed defs
    	String runSpec;

        //#################################
    	//#TEST1: Accuracy wrt probability#
    	//#################################
    	console.print("TEST1: Accuracy wrt. Probability");
    	//Create output file
    	File testProb = new File(resultDirPath + "/accuracy_wrt_probability.log");
        PrintStream outProb = new PrintStream(testProb);
        //Test
        outProb.println("Probability,Run,TruePositive,FileName");
        for(int probability=1; probability<=10; probability++) {
        	for(int run=0; run<numberOfRuns; run++) {
    			servTempLocs = permute(servTempLocs);
    			
    			//Runs
        		outProb.print(probability+","+run+",");
        		runSpec = GreedyMatching.run(nodeURL, servTempLocs, probability);
        		if(runSpec==null)
        			outProb.println("0,null");
        		else {
        			if(runSpec.contains("_X"))
        				outProb.println("1,"+runSpec);
        			else
        				outProb.println("0,"+runSpec);
        		}
            	console.print(".");
        	}
        	console.print((probability*10)+"%");
        }

        console.println();
        
        //#################################
    	//#TEST2: Accuracy wrt TP_Percent #
    	//#################################
        console.print("TEST2: Accuracy wrt. Percentage of True Positives");
    	//Create output file
    	File testPerc = new File(resultDirPath + "/accuracy_wrt_percentage.log");
        PrintStream outPerc= new PrintStream(testPerc);
        //Test
        outPerc.println("Percentage,Run,TruePositive,FileName");
        for(int perc=0; perc<=100; perc+=5) {
        	String percURLs = varyingServURLs.replace("%%", String.valueOf(perc));
        	servTempLocs = parseURLsFile(percURLs);
        	for(int run=0; run<numberOfRuns; run++) {
        		servTempLocs = permute(servTempLocs);
        		//Runs
        		outPerc.print(perc+","+run+",");
        		runSpec = GreedyMatching.run(nodeURL,servTempLocs,10);
        		if(runSpec==null)
        			outPerc.println("0,null");
        		else {
        			if(runSpec.contains("_X"))
        				outPerc.println("1,"+runSpec);
        			else
        				outPerc.println("0,"+runSpec);
        		}
        		console.print(".");
        	}
        	console.print(perc + "%");
        }
        
        //Remove temp file storing output
        tmp.delete();
        
        //Remove generated specs
        File dir = new File(".");
        for(String s : dir.list()) {
        	if(s.contains(".tosca")) {
        		File tosca = new File(s);
        		if(tosca.isFile())
        			tosca.delete();
        	}
        }
    }
}
