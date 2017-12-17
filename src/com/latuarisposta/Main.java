package com.latuarisposta;


import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Main {


	public static void main(String[] args) throws Exception {

		int maxSegm = 1000;
		float maxPercTraining=0.6f;
		int step = 20;
		int numExp = 20;
		FileWriter fwMean = null;
		BufferedWriter bwMean = null;
		PrintWriter outMean = null;

		FileWriter fwVariance = null;
		BufferedWriter bwVariance = null;
		PrintWriter outVariance = null;
		try
		{
			Utils.executeCommand("rm resultsMean.csv",false);
			fwMean = new FileWriter("resultsMean.csv", false);
			bwMean = new BufferedWriter(fwMean);
			outMean = new PrintWriter(bwMean);

			Utils.executeCommand("rm resultsVariance.csv",false);
			fwVariance = new FileWriter("resultsVariance.csv", false);
			bwVariance = new BufferedWriter(fwVariance);
			outVariance = new PrintWriter(bwVariance);

			//esegue il terieval una sola volta per avere i file .res, poi non serve piu' eseguire ogni singola volta
			//Utils.executeTerrier();

			/*struttura file.csv
			segmenti		----------------->
			perctraining
				|
				|
				|
			*/
			outMean.println("#segmenti");
			outVariance.println("#segmenti");
			outMean.print(";");
			outVariance.print(";");
			for (int currentSegm = 1; currentSegm < maxSegm; currentSegm = currentSegm + step) {
				outMean.print(currentSegm+";");
				outVariance.print(currentSegm+";");
			}
			for(float currentPercTraining=0.1f;currentPercTraining<maxPercTraining;currentPercTraining=currentPercTraining+0.1f) {
				outMean.print(currentPercTraining+";");
				outVariance.print(currentPercTraining+";");

				for (int currentSegm = 1; currentSegm < maxSegm; currentSegm = currentSegm + step) {
					float somma = 0;
					LinkedList<Double> mapValues=new LinkedList<>();
					for (int i = 0; i < numExp; i++) {
						ProbFuse probfuse = new ProbFuse(currentSegm,currentPercTraining);
						while(probfuse.isTrainingBad())
						{
							probfuse = new ProbFuse(currentSegm,currentPercTraining);
							System.out.println("BAD TRAINING");
						}

						//BaseFuse basefuse=new BaseFuse(probfuse.getTrainQueries());

						//System.out.println("ProbFuse\n" + probfuse.getResult_trec_eval());
						//System.out.println("baseFuse\n"+basefuse.getResult_trec_eval());

						float map_probfuse = Float.parseFloat(probfuse.getResult_trec_eval().split("map")[1].split("gm_ap")[0].split("\t")[2]);
						somma += map_probfuse;
						mapValues.add(Double.valueOf(map_probfuse));
						//float map_basefuse =Float.parseFloat(basefuse.getResult_trec_eval().split("map")[1].split("gm_ap")[0].split("\t")[2]);
						//System.out.println("Map\nProb: " + map_probfuse);//+"\t Base:"+map_basefuse);
					}
					float mean=somma / numExp;

					float variance=0;
					for (Double value:mapValues) {
						variance=variance+(float)Math.pow(value-mean,2);
					}
					variance=variance/numExp;

					outMean.print(mean+";");
					outVariance.print(variance+";");
					outMean.flush();
					outVariance.flush();
				}
				outMean.print("\n");
				outVariance.print("\n");
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			outMean.close();
			bwMean.close();
			fwMean.close();

			outVariance.close();
			bwVariance.close();
			fwVariance.close();
		}
	}
}
