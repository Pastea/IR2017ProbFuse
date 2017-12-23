package com.latuarisposta;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

	public static final int SEG_MAX = 1000;
	public static final float TRAININGSIZE_MAX =0.6f;
	public static final int STEP = 20;
	public static final int EXP_NUMBER = 20;
	public static final int MODELS_NUMBER = 10;
	public static final String COLLECTION_PATH = "linkCollection/";
	public static final String TOPIC_FILE = "topics/topics.351-400_trec7.bin";
	public static final String GT_FILE="qrels/qrels.trec7.bin";
	public static final String RESULTFUSION_PATH ="trec_eval/";
	public static final String CSV_PATH="";


	public static void main(String[] args) throws Exception {

		//scelgo i metodi da usare per la fusione delle run
		Utils.ListRankFusion listRankFusion = new Utils.ListRankFusion();
		listRankFusion.add(new Utils.RankFusion(new CombMNZ()));
		listRankFusion.add(new Utils.RankFusion(new ProbFuseAll()));
		listRankFusion.add(new Utils.RankFusion(new ProbFuseJudged()));

		try
		{
			//inizializzo tutte le variabili di ogni algoritmo di fusione (writer, etc)
			listRankFusion.initializeAll();

			//eseguo l'indexing e il retrieval una sola volta per avere i file .res
			Utils.executeTerrier();

			//serializzo i risultati di terrier nel formato sistema-topic-documenti
			ArrayList<ArrayList<Utils.ResultTopic>> pool = Utils.getTerrierResults();

			/*struttura file.csv
			segmenti		----------------->
			perctraining
			*/

			//scrivo nel file la prima riga contenente il numero di segmenti che analizziamo
			for (int currentSegm = 1; currentSegm < SEG_MAX; currentSegm = currentSegm + STEP) {
				listRankFusion.printAll(currentSegm+";");
			}
			listRankFusion.printAll("\n");

			//inizio facendo variare il numero di training topics
			for(float currentPercTraining = 0.1f; currentPercTraining< TRAININGSIZE_MAX; currentPercTraining=currentPercTraining+0.1f) {
				listRankFusion.printAll(currentPercTraining+";");

				//faccio variare il numero di segmenti per ogni run
				for (int currentSegm = 1; currentSegm < SEG_MAX; currentSegm = currentSegm + STEP) {
					listRankFusion.initializeParametersAll();

					//faccio EXP_NUMBER di esperimenti per ogni coppia di parametri size training e numero segmenti scelta
					for (int i = 0; i < EXP_NUMBER; i++) {

						//calcolo i valori di probfuseAll e probfuseJudge per ogni segmento e poi li assegno ad ogni documento
						ProbFuse probfuse = new ProbFuse(currentSegm, currentPercTraining,pool);

						//controllo nel caso ci sia stato un bad training set e nel caso rifaccio tutto da zero
						while (probfuse.isTrainingBad()) {
							System.out.println("BAD TRAINING on topic "+probfuse.getBadTrainingTopic()+" and model "+probfuse.getBadTrainingModel());
							probfuse = new ProbFuse(currentSegm, currentPercTraining,pool);
						}

						//fondo le rank dei sistemi per ogni algoritmo utilizzato e li salvo nei file res in trec_eval
						Utils.createFinalRank(pool,listRankFusion.listRF);

						//valuto il sistema con trec_eval
						HashMap<String, String> resultTrecEval = Utils.evaluateTerrier(listRankFusion.listRF);

						//aggiorno i risultati dell'esperimento
						listRankFusion.update(resultTrecEval);
					}
					//salvo i risultati dell'esperimento in un file
					listRankFusion.writeResult();
				}
				listRankFusion.printAll("\n");
			}
			Utils.executeCommand("python heatmap.py", false);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			listRankFusion.close();
		}
	}


}
