package com.latuarisposta;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import static com.latuarisposta.Utils.delete;

public class Main {

	public static File FILENAMEFUSIONRANKING;

	public static void main(String[] args) throws Exception {

		ArrayList<ArrayList<ResultTopic>> result = gandalfiles_ushallnotpassargument();

		//result contiene alla fine un array dei 10 sistemi e per ogni sistema un array di 50 topic dove all'interno troviamo l'id del topic e un array con gli score di ogni documento

		theyretakingthehobbitstoisengard(result, new CombANZ());

		//valutazione usando qrels, di default il fusion ranking e' in terrier-core-4.2-0
		//executeCommand("terrier-core-4.2-0/bin/trec_terrier.sh -e -Dtrec.qrels=qrels/qrels.trec7.bin");

		executeCommand("trec_eval/trec_eval qrels/qrels.trec7.bin terrier-core-4.2-0/var/results/resultFusionRanking.res");

	}

	public static ArrayList<ArrayList<ResultTopic>> gandalfiles_ushallnotpassargument() {
		String PATH_COLLECTION = "linkCollection/";

		//rimuove le cartelle result se ci sono
		for (int i = 0; i < Utils.how_many_models; i++) {
			try {
				delete(new File("terrier-core-4.2-" + i + "/var/results"));

			} catch (Exception e) {
			}
		}

		//esegue i dieci modelli, ogni modello i-esimo e' in terrier-core-4.2-i
		//il retrival va cambiato in base al modello ma per ora sta cosi'
		for (int i = 0; i < Utils.how_many_models; i++) {
			executeCommand("terrier-core-4.2-" + i + "/bin/trec_setup.sh " + PATH_COLLECTION);
			executeCommand("cp Sh_10Sist/terrier.properties." + i + " terrier-core-4.2-"+i+"/etc/terrier.properties");
			executeCommand("terrier-core-4.2-" + i + "/bin/trec_terrier.sh -i -j");
			executeCommand("terrier-core-4.2-" + i + "/bin/trec_terrier.sh --printstats;");
			executeCommand("terrier-core-4.2-" + i + "/bin/trec_terrier.sh -r -Dtrec.topics=topics/topics.351-400_trec7.bin");

		}
		//tira fuori i risultati

		ArrayList<String> runs = new ArrayList<>();

		for (int i = 0; i < Utils.how_many_models; i++) {
			String path = "terrier-core-4.2-" + i + "/var/results";
			File[] files = new File(path).listFiles();

			for (File file : files) {
				if (file.isFile()) {
					if (file.getName().contains(".res") && !file.getName().contains(".res.settings")) {
						runs.add(path + "/" + file.getName());
					}
				}
			}
		}

		FILENAMEFUSIONRANKING = new File("terrier-core-4.2-0/var/results/resultFusionRanking.res"); //scelta arbitraria, non è relativo al sistema 0 ma è il risultato della fusione di tutti e 10 i sistemi

		ArrayList<ArrayList<ResultTopic>> result = new ArrayList<>();

		//carico in memoria le run
		for (String FILENAME : runs) {

			try {

				FileReader fr = new FileReader(FILENAME);
				BufferedReader br = new BufferedReader(fr);

				String sCurrentLine;

				ArrayList<ResultTopic> modelX = new ArrayList<>();
				result.add(modelX);
				ResultTopic lastTopic = new ResultTopic();

				while ((sCurrentLine = br.readLine()) != null) {
					//se gli id dei topic di ora e quello precedente non corrispondono crea un nuovo oggetto
					//altrimenti l'add va a buon fine e la riga e' aggiunta subito da dentro l'if
					if (!lastTopic.add(sCurrentLine, lastTopic.getTopicID())) {
						lastTopic = new ResultTopic();
						lastTopic.add(sCurrentLine);
						modelX.add(lastTopic);
					}
				}

				for (ResultTopic topic : modelX) {
					topic.normalize();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			FILENAMEFUSIONRANKING.delete();
		} catch (Exception e) {
		}

		return result;
	}

	public static void theyretakingthehobbitstoisengard(ArrayList<ArrayList<ResultTopic>> result, RankFusionIF rankFusionAlg) {
		for (int i = 0; i < 50; i++) {

			//per ogni run si costruisce una hash map <DocID,List> di risultati
			HashMap<String, ArrayList<ResultLine>> docResult = new HashMap<>();
			//ArrayList<String> topicIdList = new ArrayList<>();
			//topicResult è il sistema

			for (ArrayList<ResultTopic> topicResults : result) {
				//risultati (o linee) cdi quel topic
				ArrayList<ResultLine> temp = topicResults.get(i).getLines();
				for (ResultLine line : temp) {
					if (!docResult.containsKey("" + line.DocName)) {    //da sistemare
						docResult.put("" + line.DocName, new ArrayList<ResultLine>());
					}
					docResult.get("" + line.DocName).add(line);
				}
			}

			//now the fusion ranking
			//metodi base
			ArrayList<ResultLine> fusionRankingResult = new ArrayList<>();

			for (Map.Entry<String, ArrayList<ResultLine>> entry : docResult.entrySet()) {
				ResultLine fusionResult = new ResultLine();

				double score = -1;
				score = (double) rankFusionAlg.computeScore(entry.getValue());

				//prende un oggetto a caso per riempire i campi e cambia lo score con quello del fusion ranking
				fusionResult.set(entry.getValue().get(0), score);
				fusionRankingResult.add(fusionResult);
			}
			//ora ordino

			Collections.sort(fusionRankingResult, new Utils.CustomComparator());

			Utils.writeToFile(fusionRankingResult, FILENAMEFUSIONRANKING.getPath(), 1000);
		}
	}

	public static void executeCommand(String command) {

		//StringBuffer output = new StringBuffer();
		String s;
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			BufferedReader br = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			while ((s = br.readLine()) != null)
				System.out.println("line: " + s);
			p.waitFor();
			System.out.println("exit: " + p.exitValue());
			p.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//return output.toString();

	}

	public static class ResultTopic {
		private int topicId;
		private ArrayList<ResultLine> lines;

		//il loro settaggio si basa sul fatto che il primo ResultLine avra' lo score piu' alto
		//e l'ultimo ResultLine avra' lo score piu' basso (sono ordinati)
		private double maxScore = -1;
		private double minScore = -1;

		public ResultTopic() {
			lines = new ArrayList<>();
		}

		//ritorna true se la riga appartiene al topic
		public boolean add(String line, int topicToExpect) {
			ResultLine currentLine = new ResultLine();
			int line_topicID = currentLine.set(line);
			if (line_topicID == topicId) {
				lines.add(currentLine);
				minScore = currentLine.getScore();
				return true;
			} else
				return false;
		}

		//forza l'aggiunta della riga
		public void add(String line) {
			ResultLine currentLine = new ResultLine();
			topicId = currentLine.set(line);
			lines.add(currentLine);
			maxScore = currentLine.getScore();
		}

		public int getTopicID() {
			return topicId;
		}

		public void normalize() {
			for (ResultLine line : lines) {
				line.setScore((line.getScore() - minScore) / (maxScore - minScore));
			}
		}

		public ArrayList<ResultLine> getLines() {
			return lines;
		}
	}


	public static class ResultLine {
		private int topicId = -1;
		private String boh1 = "NULL";
		private String DocName = "NULL";
		private int position = -1;
		private double score = -1;
		private String boh2 = "NULL";

		public ResultLine() {
		}

		//ritorna il topicID
		public int set(String line) {
			String[] parsedLine = line.split(" ");
			topicId = Integer.parseInt(parsedLine[0]);
			boh1 = parsedLine[1];
			DocName = parsedLine[2];
			position = Integer.parseInt(parsedLine[3]);
			score = Double.parseDouble(parsedLine[4]);
			boh2 = parsedLine[1];
			return topicId;
		}

		public void set(ResultLine oldObject, double valueScore) {
			topicId = oldObject.getTopicId();
			boh1 = oldObject.getBoh1();
			DocName = oldObject.getDocName();
			position = -1;
			score = valueScore;
			boh2 = oldObject.getBoh2();
		}

		public double getScore() {
			return score;
		}

		public void setScore(double value) {
			score = value;
		}

		public int getTopicId() {
			return topicId;
		}

		public String getBoh1() {
			return boh1;
		}

		public String getDocName() {
			return DocName;
		}

		public String getBoh2() {
			return boh2;
		}

		public String toString() {
			return "" + topicId + " " + boh1 + " " + DocName + " " + position + " " + score + " " + boh2;
		}
	}
}