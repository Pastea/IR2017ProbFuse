package com.latuarisposta;

import java.io.*;
import java.util.*;

public class Utils {

	public static final int how_many_models = 10;

	public static ArrayList<ArrayList<ResultTopic>> terrier() {
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
			executeCommand("terrier-core-4.2-" + i + "/bin/trec_setup.sh " + PATH_COLLECTION, false);
			executeCommand("cp Sh_10Sist/terrier.properties." + i + " terrier-core-4.2-" + i + "/etc/terrier.properties", false);
			executeCommand("terrier-core-4.2-" + i + "/bin/trec_terrier.sh -i -j", false);
			executeCommand("terrier-core-4.2-" + i + "/bin/trec_terrier.sh --printstats;", false);
			executeCommand("terrier-core-4.2-" + i + "/bin/trec_terrier.sh -r -Dtrec.topics=topics/topics.351-400_trec7.bin", false);

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

		ArrayList<Integer> queryNumber = queryCount("topics/topics.351-400_trec7.bin");

		File FILENAMEFUSIONRANKING = new File("terrier-core-4.2-0/var/results/resultFusionRanking.res"); //scelta arbitraria, non è relativo al sistema 0 ma è il risultato della fusione di tutti e 10 i sistemi

		ArrayList<ArrayList<ResultTopic>> result = new ArrayList<>();

		//carico in memoria le run
		for (String FILENAME : runs) {

			try {

				FileReader fr = new FileReader(FILENAME);
				BufferedReader br = new BufferedReader(fr);

				String sCurrentLine;

				ArrayList<ResultTopic> modelX = new ArrayList<>();
				result.add(modelX);

				for (int q : queryNumber) {
					modelX.add(new ResultTopic(q));
				}

				while ((sCurrentLine = br.readLine()) != null) {
					String[] parsedLine = sCurrentLine.split(" ");
					int topicId = Integer.parseInt(parsedLine[0]);
					modelX.get(topicId - 351).add(sCurrentLine);
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

	public static void theyretakingthehobbitstoisengardTheSequel(ProbFuseHandler result, RankFusionIF rankFusionAlg) {
		int topicsToFuse = result.getSystemSize(0);
		for (int i = 0; i < topicsToFuse; i++)
		{

			//per ogni run si costruisce una hash map <DocID,List> di risultati
			HashMap<String, ArrayList<ResultLine>> docResult = new HashMap<>();
			//topicResult è il sistema

			for (int k=0; k<Utils.how_many_models; k++)// topicResults : result)
			{
				for(int j=0; j<result.getQuerySize(k,i); j++)
				{
					//risultati (o linee) cdi quel topic
					List<ResultLine> temp = result.getSegment(k,i,j);
					for (ResultLine line : temp) {
						if (!docResult.containsKey("" + line.DocName))
						{    //da sistemare
							docResult.put("" + line.DocName, new ArrayList<ResultLine>());
						}
						docResult.get("" + line.DocName).add(line);
					}
				}
			}

			//now the fusion ranking
			//metodi base
			ArrayList<ResultLine> fusionRankingResult = new ArrayList<>();

			for (Map.Entry<String, ArrayList<ResultLine>> entry : docResult.entrySet()) {
				ResultLine fusionResult = new ResultLine();

				double score = rankFusionAlg.computeScore(entry.getValue());

				//prende un oggetto a caso per riempire i campi e cambia lo score con quello del fusion ranking
				fusionResult.set(entry.getValue().get(0), score);
				fusionRankingResult.add(fusionResult);
			}
			//ora ordino

			Collections.sort(fusionRankingResult, new Utils.CustomComparator());


			File FILENAMEFUSIONRANKING = new File("terrier-core-4.2-0/var/results/resultFusionRanking.res"); //scelta arbitraria, non è relativo al sistema 0 ma è il risultato della fusione di tutti e 10 i sistem

			Utils.writeToFile(fusionRankingResult, FILENAMEFUSIONRANKING.getPath(), 1000);
		}
	}

	public static void createFinalRank(ArrayList<ArrayList<ResultTopic>> result, RankFusionIF rankFusionAlg) {
		int topicToFuse = result.get(0).size();
		for (int i = 0; i < topicToFuse; i++) {

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

				double score = rankFusionAlg.computeScore(entry.getValue());

				//prende un oggetto a caso per riempire i campi e cambia lo score con quello del fusion ranking
				fusionResult.set(entry.getValue().get(0), score);
				fusionRankingResult.add(fusionResult);
			}
			//ora ordino

			Collections.sort(fusionRankingResult, new Utils.CustomComparator());


			File FILENAMEFUSIONRANKING = new File("terrier-core-4.2-0/var/results/resultFusionRanking.res"); //scelta arbitraria, non è relativo al sistema 0 ma è il risultato della fusione di tutti e 10 i sistem

			Utils.writeToFile(fusionRankingResult, FILENAMEFUSIONRANKING.getPath(), 1000);
		}
	}

	static class CustomComparator implements Comparator<ResultLine> {

		public int compare(ResultLine o1, ResultLine o2) {
			return -Double.compare(o1.getScore(), o2.getScore());
		}
	}

	static double[] toDoubleArray(ArrayList<ResultLine> results) {
		double[] scoreArray = new double[results.size()];
		int count = 0;
		for (ResultLine line : results) {
			scoreArray[count] = line.getScore();
			count++;
		}
		Arrays.sort(scoreArray);
		return scoreArray;
	}

	public static String executeCommand(String command, boolean returnString) {

		//StringBuffer output = new StringBuffer();
		StringBuilder s = new StringBuilder();
		String str;
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			BufferedReader br = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			if (returnString) {
				while ((str = br.readLine()) != null) {
					s.append(str);s.append("\n");
					System.out.println("line: " + str);
				}
			} else {
				while ((str = br.readLine()) != null)
					System.out.println("line: " + str);
			}
			p.waitFor();
			System.out.println("exit: " + p.exitValue());
			p.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s.toString();

	}


	public static void writeToFile(ArrayList<ResultLine> toWrite, String path, int howMany) {
		try {
			int count = 0;
			FileWriter fw = new FileWriter(path, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			for (ResultLine line : toWrite) {
				if (count < howMany)
					out.println(line.toString(count));
				count++;
			}
			out.flush();
			fw.close();
		} catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}

	/**
	 * Delete a file or a directory and its children.
	 *
	 * @param file The directory to delete.
	 * @throws IOException Exception when problem occurs during deleting the directory.
	 */
	public static void delete(File file) throws IOException {

		for (File childFile : file.listFiles()) {

			if (childFile.isDirectory()) {
				delete(childFile);
			} else {
				if (!childFile.delete()) {
					throw new IOException();
				}
			}
		}

		if (!file.delete()) {
			throw new IOException();
		}
	}

	public static ArrayList<Integer> queryCount(String file) {
		ArrayList<Integer> queryNumbers = null;
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String sCurrentLine;
			queryNumbers = new ArrayList<Integer>();
			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.contains("<num>")) {
					queryNumbers.add(Integer.parseInt(sCurrentLine.split(":")[1].trim()));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return queryNumbers;
	}


	public static class ResultTopic {
		private int topicId;
		private ArrayList<ResultLine> lines;

		//il loro settaggio si basa sul fatto che il primo ResultLine avra' lo score piu' alto
		//e l'ultimo ResultLine avra' lo score piu' basso (sono ordinati)
		private double maxScore = 0;
		private double minScore = 0;

		public ResultTopic() {
			lines = new ArrayList<>();
		}

		public ResultTopic(int Id) {
			lines = new ArrayList<>();
			topicId = Id;
		}

		//aggiune la riga
		public void add(String line) {
			ResultLine currentLine = new ResultLine();
			topicId = currentLine.set(line);
			if (lines.size() == 0) {
				maxScore = currentLine.getScore();
			} else {
				minScore = currentLine.getScore();
			}
			lines.add(currentLine);
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

		public String toString(int positionValue) {
			return "" + topicId + " " + boh1 + " " + DocName + " " + positionValue + " " + score + " " + boh2;
		}
	}
}