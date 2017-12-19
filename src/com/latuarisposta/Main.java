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
		FileWriter fwMeanProb = null;
		BufferedWriter bwMeanProb = null;
		PrintWriter outMeanProb = null;

		FileWriter fwVarianceProb = null;
		BufferedWriter bwVarianceProb = null;
		PrintWriter outVarianceProb = null;

		FileWriter fwMeanComb = null;
		BufferedWriter bwMeanComb = null;
		PrintWriter outMeanComb = null;

		FileWriter fwVarianceComb = null;
		BufferedWriter bwVarianceComb = null;
		PrintWriter outVarianceComb = null;
		try
		{
			Utils.executeCommand("rm resultsMeanProb.csv",false);
			fwMeanProb = new FileWriter("resultsMeanProb.csv", false);
			bwMeanProb = new BufferedWriter(fwMeanProb);
			outMeanProb = new PrintWriter(bwMeanProb);

			Utils.executeCommand("rm resultsVarianceProb.csv",false);
			fwVarianceProb = new FileWriter("resultsVarianceProb.csv", false);
			bwVarianceProb = new BufferedWriter(fwVarianceProb);
			outVarianceProb = new PrintWriter(bwVarianceProb);

			Utils.executeCommand("rm resultsMeanComb.csv",false);
			fwMeanComb = new FileWriter("resultsMeanComb.csv", false);
			bwMeanComb = new BufferedWriter(fwMeanComb);
			outMeanComb = new PrintWriter(bwMeanComb);

			Utils.executeCommand("rm resultsVarianceComb.csv",false);
			fwVarianceComb = new FileWriter("resultsVarianceComb.csv", false);
			bwVarianceComb = new BufferedWriter(fwVarianceComb);
			outVarianceComb = new PrintWriter(bwVarianceComb);

			//esegue il terieval una sola volta per avere i file .res, poi non serve piu' eseguire ogni singola volta
			Utils.executeTerrier();

			/*struttura file.csv
			segmenti		----------------->
			perctraining
				|
				|
				|
			*/
			outMeanProb.println("#segmenti");
			outVarianceProb.println("#segmenti");
			outMeanComb.println("#segmenti");
			outVarianceComb.println("#segmenti");

			outMeanProb.print(";");
			outVarianceProb.print(";");
			outMeanComb.print(";");
			outVarianceComb.print(";");
			for (int currentSegm = 1; currentSegm < maxSegm; currentSegm = currentSegm + step) {
				outMeanProb.print(currentSegm+";");
				outVarianceProb.print(currentSegm+";");
				outMeanComb.print(currentSegm+";");
				outVarianceComb.print(currentSegm+";");
			}
			outMeanProb.print("\n");
			outVarianceProb.print("\n");
			outMeanComb.print("\n");
			outVarianceComb.print("\n");
			for(float currentPercTraining=0.1f;currentPercTraining<maxPercTraining;currentPercTraining=currentPercTraining+0.1f) {
				outMeanProb.print(currentPercTraining + ";");
				outVarianceProb.print(currentPercTraining + ";");
				outMeanComb.print(currentPercTraining + ";");
				outVarianceComb.print(currentPercTraining + ";");

				for (int currentSegm = 1; currentSegm < maxSegm; currentSegm = currentSegm + step) {
					float sommaProb = 0;
					LinkedList<Double> mapValuesProb = new LinkedList<>();

					float sommaComb = 0;
					LinkedList<Double> mapValuesComb = new LinkedList<>();

					for (int i = 0; i < numExp; i++) {
						ProbFuse probfuse = new ProbFuse(currentSegm, currentPercTraining);
						while (probfuse.isTrainingBad()) {
							System.out.println("BAD TRAINING on topic "+probfuse.getBadTrainingTopic()+" and model "+probfuse.getBadTrainingModel());
							probfuse = new ProbFuse(currentSegm, currentPercTraining);
						}

						BaseFuse basefuse = new BaseFuse(probfuse.getTrainQueries());

						float map_probFuse = Float.parseFloat(probfuse.getResult_trec_eval().split("map")[1].split("gm_ap")[0].split("\t")[2]);
						float map_combFuse = Float.parseFloat(basefuse.getResult_trec_eval().split("map")[1].split("gm_ap")[0].split("\t")[2]);

						sommaProb += map_probFuse;
						sommaComb += map_combFuse;

						mapValuesProb.add(Double.valueOf(map_probFuse));
						mapValuesComb.add(Double.valueOf(map_combFuse));
						//float map_basefuse =Float.parseFloat(basefuse.getResult_trec_eval().split("map")[1].split("gm_ap")[0].split("\t")[2]);
						//System.out.println("Map\nProb: " + map_probfuse);//+"\t Base:"+map_basefuse);
					}
					float meanProb = sommaProb / numExp;
					float meanComb = sommaComb / numExp;

					float varianceProb = 0;
					float varianceComb = 0;
					for (Double value : mapValuesProb) {
						varianceProb = varianceProb + (float) Math.pow(value - meanProb, 2);
						varianceComb = varianceComb + (float) Math.pow(value - meanComb, 2);
					}
					varianceProb = varianceProb / numExp;
					varianceComb = varianceComb / numExp;

					outMeanProb.print(meanProb + ";");
					outVarianceProb.print(varianceProb + ";");
					outMeanProb.flush();
					outVarianceProb.flush();

					outMeanComb.print(meanComb + ";");
					outVarianceComb.print(varianceComb + ";");
					outMeanComb.flush();
					outVarianceComb.flush();
				}
				outMeanProb.print("\n");
				outVarianceProb.print("\n");
				outMeanComb.print("\n");
				outVarianceComb.print("\n");
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			outMeanProb.close();
			bwMeanProb.close();
			fwMeanProb.close();

			outVarianceProb.close();
			bwVarianceProb.close();
			fwVarianceProb.close();

			outMeanComb.close();
			bwMeanComb.close();
			fwMeanComb.close();

			outVarianceComb.close();
			bwVarianceComb.close();
			fwVarianceComb.close();
		}
	}
}
