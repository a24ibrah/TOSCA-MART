package mart.test;

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

import mart.TOSCAMart;

public class TimePerformances_MART {

	private static int numberOfRuns = 300;

	private static String nodeURL = "http://www.di.unipi.it/~soldani/TOSCA-MART/Dataset/NodeTypes/DesiredEnvironment.tosca";
	private static String servURLs = "http://www.di.unipi.it/~soldani/TOSCA-MART/Dataset/ServiceTemplates/AllServTempURLs.txt";
//	private static String nodeURL = "file:///C:/Users/Jacopo/Desktop/TOSCA-MART/Dataset/NodeTypes/DesiredEnvironment.tosca";
//	private static String servURLs = "file:///C:/Users/Jacopo/Desktop/TOSCA-MART/Dataset/ServiceTemplates-5/AllServTempURLs.txt";

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

	public static void main(String[] args) throws Exception {
		//#################################
		//#Create a folder for the results#
		//#################################
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
		Date date = new Date();
		String resultDirPath = "./Test_Times_MART_" + dateFormat.format(date);
		File resultDir = new File(resultDirPath);
		resultDir.mkdir();

		//#################################
		//##TEST1: Varying size r of Repo##
		//#################################
		//Create output file
		File testR = new File(resultDirPath+"/test_varying_r.log");
		PrintStream outR = new PrintStream(testR);
		//Write first line (which specifies CSV file format)
		outR.println("r,run,CandidatesUnion(micros),RateAndFilter(micros),Cut(micros),MappingSelection(micros),Adaptation(micros),Total(micros)");
		//Redirects System.out
		System.setOut(outR);
		//Sets test environment
		int maxR = 5;
		//Starts test
		System.err.print("TEST1: Varying r");
		for(int r=0; r<=maxR; r++) {
			for(int run=1; run<=numberOfRuns; run++) {
				List<String> servTempLocs = new ArrayList<String>();
				for(int i=0; i<r; i++)
					servTempLocs.addAll(parseURLsFile(servURLs));
				System.out.print(r+","+run+",");
				long elapsedTime = System.nanoTime();
				TOSCAMart.run(nodeURL, servTempLocs, 1);
				elapsedTime = System.nanoTime() - elapsedTime;
				System.out.println(elapsedTime/1000);
				System.err.print(".");
			}
			System.err.print(((r+1)*100/(maxR+1))+"%");
		}
		System.err.println();
		//Close output file
		outR.close();

		//#################################
		//##TEST2: Varying size t of serv##
		//#################################
		//Create output file
		File testT = new File(resultDirPath+"/test_varying_t.log");
		PrintStream outT = new PrintStream(testT);
		//Write first line (which specifies CSV file format)
		outT.println("t,run,CandidatesUnion(micros),RateAndFilter(micros),Cut(micros),MappingSelection(micros),Adaptation(micros),Total(micros)");
		//Redirects System.out
		System.setOut(outT);
		//Sets test environment
		int maxT = 5;
		System.err.print("TEST2: Varying r");
		for(int t=0; t<=maxT; t++) {
			for(int run=1; run<=numberOfRuns; run++) {
				List<String> servTempLocs = new ArrayList<String>();
				if(t!=0)
					servTempLocs.addAll(parseURLsFile(servURLs.replace("ServiceTemplates", "ServiceTemplates-"+t)));
				System.out.print(t+","+servTempLocs.size()+","+run+",");
				long elapsedTime = System.nanoTime();
				TOSCAMart.run(nodeURL, servTempLocs, 1);
				elapsedTime = System.nanoTime() - elapsedTime;
				System.out.println(elapsedTime/1000);
				System.err.print(".");
			}
			System.err.print(((t+1)*100/(maxT+1))+"%");
		}
		System.err.println("");
		//Close output file
		outT.close();

		//#################################
		//##TEST3: Varying size n of node##
		//#################################
		//Create output file
		File testN = new File(resultDirPath+"/test_varying_n.log");
		PrintStream outN = new PrintStream(testN);
		//Write first line (which specifies CSV file format)
		outN.println("n,run,CandidatesUnion(micros),RateAndFilter(micros),Cut(micros),MappingSelection(micros),Adaptation(micros),Total(micros)");
		//Redirects System.out
		System.setOut(outN);
		//Sets test environment
		int maxN = 4;
		System.err.print("TEST3: Varying n");
		for(int n=0; n<=maxN; n++) {
			for(int run=1; run<=numberOfRuns; run++) {
				List<String> servTempLocs = new ArrayList<String>();
				servTempLocs.addAll(parseURLsFile(servURLs));
				System.out.print(n+","+run+",");
				long elapsedTime = System.nanoTime();
				String nodeUpdatedURL = nodeURL.replace("DesiredEnvironment", "VaryingN/DesiredEnvironment-"+n);
				TOSCAMart.run(nodeUpdatedURL,servTempLocs,1);
				elapsedTime = System.nanoTime() - elapsedTime;
				System.out.println(elapsedTime/1000);
				System.err.print(".");
			}
			System.err.print(((n+1)*100/(maxN+1))+"%");
		}
		System.err.println("");
		//Close output file
		outN.close();
		
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
