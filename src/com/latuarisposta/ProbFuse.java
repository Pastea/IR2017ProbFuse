package com.latuarisposta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ProbFuse {

	private ArrayList<Integer> train_queries;
	private String result_trec_eval;

	public ProbFuse(int nSeg) {

		boolean badTraining = false;

		double t = 0.2;        //percentuale query training set
		int x = nSeg;             //numero segmenti
		train_queries = new ArrayList<>();

		while (train_queries.size() < t * 50) {
			int tmp = (int) (Math.random() * 50) + 351;
			//evita anche i topic 354, 367, 369,379 perche' tendono a dare pochi risultati e quindi sono poco indicati per essere usati nel training
			//non becca tutti i casi di training cattivo ma ne diminuisce la comparsa
			if (!train_queries.contains(tmp) && tmp != 364 && tmp != 367 && tmp != 369 && tmp != 379) {
				train_queries.add(tmp);
			}
		}

		ArrayList<ArrayList<Utils.ResultTopic>> pool = Utils.terrier(); //sistema --> topic --> lines(documenti)

		//serializza la grand truth
		HashMap<String, Boolean> GT = new HashMap<>();

		try {

			FileReader fr = new FileReader("qrels/qrels.trec7.bin");
			BufferedReader br = new BufferedReader(fr);

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				String[] tmp = sCurrentLine.split(" ");
				GT.put(tmp[0] + "/" + tmp[2], tmp[3].equals("0") ? false : true); //lo slassssh non è messo a caso ma ha il suo senso, im not joking guys its important, trust me
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		ArrayList<ArrayList<Float>> Pdkm = new ArrayList<ArrayList<Float>>();
		for (int s = 0; s < Utils.how_many_models; s++) {
			ArrayList<Float> tmp = new ArrayList<Float>();  //probabilità di un sistema
			for (int n = 0; n < x; n++) {
				float sum_rkq = 0f;
				for (int i : train_queries) {
					float Rkq = 0;
					ArrayList<Utils.ResultLine> documents = pool.get(s).get(i - 351).getLines();
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
						if (GT.containsKey(i + "/" + documents.get(d).getDocName())) {
							if (GT.get(i + "/" + documents.get(d).getDocName())) {
								Rkq++;
							}
						}
					}
					sum_rkq = sum_rkq + Rkq / size;
				}
				tmp.add(sum_rkq / train_queries.size());
			}
			Pdkm.add(tmp);
		}

		//t=TOPIC SCELTO
		for (int query = 0; query < 50; query++) {
			if (!train_queries.contains(query + 351)) {
				for (int s = 0; s < Utils.how_many_models; s++) {
					ArrayList<Utils.ResultLine> documents = pool.get(s).get(query).getLines();
					for (int doc = 0; doc < documents.size(); doc++) {
						Utils.ResultLine rl = documents.get(doc);
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
						rl.setScore(Pdkm.get(s).get(seg) / (seg + 1));          //doc/(docSize/x) rappresenta il numero di segmento a cui appartiene il documento doc, il +1 nel secondo termine serve per farlo partire da 1 e non da 0
					}
				}
			}
		}

		//elimina topic di training e rileva se c'e' stato un cattivo training
		for (ArrayList<Utils.ResultTopic> model : pool) {
			for (int i = model.size() - 1; i > 0; i--) {
				int topicId = model.get(i).getTopicID();
				//scansiona la lista dei topic di training
				for (int j = 0; j < train_queries.size(); j++) {
					if (train_queries.get(j).compareTo(topicId) == 0) {
						if (model.get(i).getLines().size() < x) {
							badTraining = true;
						}
						model.remove(i);
						//per sicurezza riparte a controllare ogni volta che rimuove qualcosa
						i = model.size() - 1;
					}
				}
			}
		}

		Utils.createFinalRank(pool, new CombProbFuse());

		result_trec_eval = Utils.executeCommand("trec_eval/trec_eval qrels/qrels.trec7.bin terrier-core-4.2-0/var/results/resultFusionRanking.res", true);

		//String map_value = s.split("map")[1].split("gm_ap")[0].split("\t")[2];

		if (badTraining) {
			System.out.println("Cattivo training");
			System.exit(-123);
		}
	}

	public ArrayList<Integer> getTrainQueries() {
		return train_queries;
	}

	public String getResult_trec_eval() {
		return result_trec_eval;
	}
}