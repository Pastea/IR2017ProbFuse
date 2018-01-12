package com.latuarisposta;


import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

import static com.latuarisposta.Utils.*;
import static java.lang.System.exit;
import static javax.script.ScriptEngine.FILENAME;

public class Main {

	public static final String CONFIGURATION_PATH="properties";
	public static int[] SEG;
	public static int[] TRAININGSIZE;
	public static int EXPERIMENT;
	public static int MODELS;
	public static String COLLECTION_PATH;
	public static String TOPIC_FILE;
	public static String GT_FILE;
	public static String RESULTFUSION_PATH;
	public static String CSV_PATH;
	public static String RUN_PATH;
	public static boolean TEST;

	public static void main(String[] args) throws Exception {

		loadConfiguration();

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

	private static void loadConfiguration(){
		String s;
		try {
			FileReader fr = new FileReader(CONFIGURATION_PATH);
			BufferedReader br = new BufferedReader(fr);
			while((s=br.readLine())!=null) {
				if(!s.startsWith("/*")){
					String[] line =s.split("=");
					line[1]=line[1].trim();
					switch (line[0].trim())
					{	case "SEG":	SEG=Arrays.stream(line[1].substring(1,line[1].length()-1).split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray(); break;
						case "TRAININGSIZE": TRAININGSIZE=Arrays.stream(line[1].substring(1,line[1].length()-1).split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray(); break;
						case "EXPERIMENT": EXPERIMENT=Integer.parseInt(line[1]); break;
						case "MODELS": MODELS=Integer.parseInt(line[1]); break;
						case "COLLECTION_PATH":	COLLECTION_PATH=line[1].substring(1,line[1].length()-1); break;
						case "TOPIC_FILE":	TOPIC_FILE =line[1].substring(1,line[1].length()-1); break;
						case "GT_FILE": GT_FILE=line[1].substring(1,line[1].length()-1); break;
						case "RESULTFUSION_PATH" : RESULTFUSION_PATH =line[1].substring(1,line[1].length()-1); break;
						case "CSV_PATH" : CSV_PATH=line[1].substring(1,line[1].length()-1); break;
						case "RUN_PATH" : RUN_PATH=line[1].substring(1,line[1].length()-1); break;
						case "TEST" : TEST=(line[1].equals("true")?true:false); break;
						default: throw new Exception();
					}
				}
				}
		}
		catch (Exception e){
			System.out.println("Errore nella lettura della configurazione");
			exit(0);
		}
	}
}
