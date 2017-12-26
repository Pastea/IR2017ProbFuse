package com.latuarisposta;
import java.util.ArrayList;
import static com.latuarisposta.Main.*;

public class ProbFuse {

	public static ArrayList<Integer> train_topics;
	private boolean badTraining=false;
	private int badTopic=-1;
	private int badModel=-1;

	public ProbFuse(int nSeg, int nTraining, ArrayList<ArrayList<Utils.ResultTopic>> pool) {

		badTraining = false;
		badTopic=-1;
		badModel=-1;

		int t = nTraining;        //percentuale di topic nel training set
		int x = nSeg;             //numero segmenti
		train_topics = new ArrayList<>();

		//scelgo arbitrariamente i traning topics
		while (train_topics.size() < (t * Utils.topics.size())/100) {
			int tmp = Utils.topics.get((int) (Math.random() * Utils.topics.size()));
			//evita anche i topic 354, 367, 369,379 perche' tendono a dare pochi risultati e quindi sono poco indicati per essere usati nel training
			//non becca tutti i casi di training cattivo ma ne diminuisce la comparsa
			if (!train_topics.contains(tmp)) {
				train_topics.add(tmp);
			}
		}

		//calcolo il punteggio per i vari segmenti di ogni sistema
		ArrayList<ArrayList<Double>> ProbFuseAll = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> ProbFuseJudged = new ArrayList<ArrayList<Double>>();
		for (int s = 0; s < pool.size(); s++) {
			ArrayList<Double> PFA_tmp = new ArrayList<Double>();  //probabilità di un sistema
			ArrayList<Double> PFJ_tmp = new ArrayList<Double>();  //probabilità di un sistema
			for (int n = 0; n < x; n++) {
				double PFA_sum_rkq = 0;
				double PFJ_sum_rkq= 0;
				for (int i : train_topics) {
					double Rkq = 0;
					double Nkq = 0;
					ArrayList<Utils.MultipleResultLine> documents = pool.get(s).get(Utils.topics.indexOf(i)).getLines();
					int k = documents.size() / x;         	//prendo la parte bassa
					int docRim = documents.size() - k * x;  //sono il numero di documenti che resterebbero fuori prendendo solo k elementi per ogni segmento
					int size;                           	//li ridistribuisco nei primi docRim segmenti
					int start;
					if (n < docRim) {
						size = k + 1;
						start = n * (k + 1);
					} else {
						size = k;
						start = docRim * (k + 1) + (n - docRim) * k;
					}
					for (int d = start; d < start + size; d++) {
						if (Utils.GT.containsKey(i + "/" + documents.get(d).getDocName())) {
							if (Utils.GT.get(i + "/" + documents.get(d).getDocName())) {
								Rkq++;
							}
							else{
								Nkq++;
							}
						}
					}
					PFA_sum_rkq = PFA_sum_rkq + Rkq / size;
					if(Rkq+Nkq==0){
						PFJ_sum_rkq = Double.NaN;
					}
					else {
						if(PFJ_sum_rkq!=Double.NaN) {
							PFJ_sum_rkq = PFJ_sum_rkq + Rkq / (Rkq + Nkq);
						}
					}
				}
				PFA_tmp.add(PFA_sum_rkq / train_topics.size());
				PFJ_tmp.add(PFJ_sum_rkq/ train_topics.size());
			}
			ProbFuseAll.add(PFA_tmp);
			ProbFuseJudged.add(PFJ_tmp);
		}

		//calcolo i punteggi di ogni documento
		for (int topic = 0; topic < Utils.topics.size(); topic++) {
			if (!train_topics.contains(Utils.topics.get(topic))) {
				for (int s = 0; s < pool.size(); s++) {
					ArrayList<Utils.MultipleResultLine> documents = pool.get(s).get(topic).getLines();
					for (int doc = 0; doc < documents.size(); doc++) {
						Utils.MultipleResultLine rl = documents.get(doc);
						int k = documents.size() / x;
						int docRim = documents.size() - k * x;
						int seg;
						if (doc - docRim * (k + 1) < 0) {
							seg = doc / (k + 1);
							//System.out.println("documento:" + doc + " nel segmento " + seg + " di dimensione " + (k + 1) + " avendo un numero di documenti " + documents.size());
						} else {
							seg = (doc - docRim * (k + 1)) / k + docRim;
							//System.out.println("documento:" + doc + " nel segmento " + seg + " di dimensione " + k + " avendo un numero di documenti " + documents.size());
						}
						rl.setScore("ProbFuseAll",ProbFuseAll.get(s).get(seg) / (seg + 1));          //doc/(docSize/x) rappresenta il numero di segmento a cui appartiene il documento doc, il +1 nel secondo termine serve per farlo partire da 1 e non da 0
						rl.setScore("ProbFuseJudged",ProbFuseJudged.get(s).get(seg) / (seg + 1));
					}
				}
			}
		}

		//elimina topic di training e rileva se c'e' stato un cattivo training
		int currentModel=0;
		for (ArrayList<Utils.ResultTopic> model : pool) {
			currentModel++;
			for (int i = model.size() - 1; i >= 0; i--) {
				int topicId = model.get(i).getTopicID();
				//scansiona la lista dei topic di training
				for (int j = 0; j < train_topics.size(); j++) {
					if (train_topics.get(j).compareTo(topicId) == 0) {
						if (model.get(i).getLines().size() < x) {
							badTraining = true;
							badTopic=topicId;
							badModel=currentModel;
						}
						break;
					}
				}
			}
		}
	}

	public boolean isTrainingBad()
	{
		return badTraining;
	}

	public int getBadTrainingTopic(){return badTopic;}

	public int getBadTrainingModel(){return badModel;}

}