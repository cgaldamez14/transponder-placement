package metrics;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import models.Pair;
import simulator.Simulator;
import utilities.NetworkTopology;

public class TransponderMetric{
	
	private static enum BANDWIDTH_DISTRIBUTION { RANDOM, GAUSSIAN, UNIFORM };
	private static enum EMBEDDING_METHOD { BACKUP, WO_BACKUP }; 
	
	private final String PROJECT_DIRECTORY =  new File(".").getCanonicalPath();
	private final String RESULTS_DIRECTORY = "/src/results/";
	
	public TransponderMetric()throws IOException{}
	
	public void start() throws IOException{
		getResults(BANDWIDTH_DISTRIBUTION.RANDOM,EMBEDDING_METHOD.WO_BACKUP);
		getResults(BANDWIDTH_DISTRIBUTION.UNIFORM,EMBEDDING_METHOD.WO_BACKUP);
		getResults(BANDWIDTH_DISTRIBUTION.GAUSSIAN,EMBEDDING_METHOD.WO_BACKUP);
		
		getResults(BANDWIDTH_DISTRIBUTION.RANDOM,EMBEDDING_METHOD.BACKUP);
		getResults(BANDWIDTH_DISTRIBUTION.UNIFORM,EMBEDDING_METHOD.BACKUP);
		getResults(BANDWIDTH_DISTRIBUTION.GAUSSIAN,EMBEDDING_METHOD.BACKUP);

	}
	
	public void getResults(BANDWIDTH_DISTRIBUTION distributionType, EMBEDDING_METHOD method) throws IOException{
		File file = new File(PROJECT_DIRECTORY + RESULTS_DIRECTORY + "transponder_new_4node_"+ distributionType.name().toLowerCase() + "_" + method.name().toLowerCase() + ".csv");
		PrintWriter pw = new PrintWriter(file);
		
		pw.println("Max_Bandwidth,ODU,OTN,# ODU better than OTN ");

		for(int i = 10; i <= 200; i+=10){
			System.out.println("Starting Transponder Metric with max bandwidth: " + i);
			int oduWins = 0;
			
			int sum1 = 0;
			int sum2 = 0;
			for(int j = 0; j < 1000; j++){
				Simulator simulator = new Simulator(NetworkTopology.SIMPLE,Integer.MAX_VALUE, Integer.MAX_VALUE);
				//simulator.setNumberOfRequest(500);
				simulator.setMaxNodes(1);// setting requests with only two nodes.
				simulator.setNumberOfRequest(6);
				simulator.generateRequests();
				simulator.setRequests();
				
				int oduTransponders = simulator.getTranspondersODU(100,i, distributionType.name().toLowerCase(), (method.equals(EMBEDDING_METHOD.WO_BACKUP))?false:true);
				int otnTransponders = simulator.getTransponderOTN(100,i, distributionType.name().toLowerCase(), (method.equals(EMBEDDING_METHOD.WO_BACKUP))?false:true);
				
				if(oduTransponders < otnTransponders) oduWins++;
				
				sum1 += oduTransponders; 
				sum2 += otnTransponders;
			}
			Pair<Integer,Integer> results = new Pair<Integer, Integer>(sum1/1000,sum2/1000);			
			pw.println(i + "," + results.first() + "," + results.second() + "," + oduWins);
		}
		
		pw.close();
		
		System.out.println("********************** done *******************");
	}
	
	public static void main(String args[]) throws IOException{
		new TransponderMetric().start();
	}
}
