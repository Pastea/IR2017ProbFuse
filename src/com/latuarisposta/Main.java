package com.latuarisposta;


import java.io.*;
import java.util.HashMap;

import static com.latuarisposta.Utils.*;

public class Main {

	public static final int[] SEG={25};
	public static final int[] TRAININGSIZE ={50};
	public static final int EXPERIMENT = 20;
	public static final int MODELS = 10;
	public static final String COLLECTION_PATH = "linkCollection/";
	public static final String TOPIC_FILE = "topics/topics.151-200_trec3.bin";
	public static final String GT_FILE="qrels/qrels.151-200_trec3.bin";
	public static final String RESULTFUSION_PATH ="trec_eval/";
	public static final String CSV_PATH="";
	public static final String RUN_PATH="runs_probfuse2006/TEST/";
	public static final boolean TEST=true;


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

			//eseguo l'indexing e il retrieval una sola volta per avere i file .res e serializzo i topic, la GT e il pool
			Utils.executeTerrier();

			/*struttura file.csv
			segmenti		----------------->
			perctraining
			*/

			//scrivo nel file la prima riga contenente il numero di segmenti che analizziamo
			for (int currentSegm : SEG) {
				listRankFusion.printAll(currentSegm+";");
			}
			listRankFusion.printAll("\n");

			//inizio facendo variare il numero di training topics
			for(int currentPercTraining : TRAININGSIZE) {
				listRankFusion.printAll(currentPercTraining+";");

				//faccio variare il numero di segmenti per ogni run
				for (int currentSegm : SEG) {
					listRankFusion.initializeParametersAll();

					//faccio EXPERIMENT esperimenti per ogni coppia di parametri size training e numero segmenti scelta
					for (int i = 0; i < EXPERIMENT; i++) {

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
