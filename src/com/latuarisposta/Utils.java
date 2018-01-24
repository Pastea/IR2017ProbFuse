package com.latuarisposta;

import java.io.*;
import java.util.*;

import static com.latuarisposta.Main.*;

public class Utils {

    public static ArrayList<ArrayList<Utils.ResultTopic>> run;
    public static ArrayList<Integer> topics;
    public static HashMap<String, Boolean> GT;

    /**
     * Setup the directory for the system creation
     */
    public static void setupTerrierModels() {
        //creo i vari sistemi
        for (int i = 0; i < MODELS; i++) {
            String result = executeCommand("ls terrier-core-4.2-" + i, true);
            //se non trova i file necessari cancella e ricrea
            if (!result.contains("bin")) {
                executeCommand("rm -r terrier-core-4.2-" + i, false);
                executeCommand("cp -r terrier-core-4.2-start terrier-core-4.2-" + i, false);
            }

        }

    }

    /**
     * Execute terrier and parse the topics, GT and the results of the execution
     */
    public static void executeTerrier() {
        if (!TEST) {
            setupTerrierModels();

            //rimuove le cartelle result se ci sono
            for (int i = 0; i < MODELS; i++) {
                try {
                    delete(new File("terrier-core-4.2-" + i + "/var/results"));

                } catch (Exception e) {
                }
            }
            executeCommand("rm " + RESULTFUSION_PATH + "*.res", false);

            //esegue i dieci modelli, ogni modello i-esimo e' in terrier-core-4.2-i
            //il retrival va cambiato in base al modello ma per ora sta cosi' ???
            for (int i = 0; i < MODELS; i++) {
                executeCommand("terrier-core-4.2-" + i + "/bin/trec_setup.sh " + COLLECTION_PATH, false);
                executeCommand("cp Sh_10Sist/terrier.properties." + i + " terrier-core-4.2-" + i + "/etc/terrier.properties", false);
                executeCommand("terrier-core-4.2-" + i + "/bin/trec_terrier.sh -i -j", false);
                executeCommand("terrier-core-4.2-" + i + "/bin/trec_terrier.sh --printstats;", false);
                executeCommand("terrier-core-4.2-" + i + "/bin/trec_terrier.sh -r -Dtrec.topics=" + TOPIC_FILE, false);

            }
        }
        //serializza i topic utilizzati
        getTopics();
        //serializza la grand truth per i topic utilizzati
        getGT();
        //serializzo i risultati di terrier nel formato sistema-topic-documenti
        getTerrierResults();
    }

    /**
     * Parse the results of terrier for the model or parse the run pre-made
     */
    public static void getTerrierResults() {
        ArrayList<String> runs = new ArrayList<>();
        if (!TEST) {
            //indexing e retrieval dei sistemi di terrier
            for (int i = 0; i < MODELS; i++) {
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

            run = new ArrayList<>();

            //carico in memoria le run
            for (String FILENAME : runs) {

                try {

                    FileReader fr = new FileReader(FILENAME);
                    BufferedReader br = new BufferedReader(fr);

                    String sCurrentLine;

                    ArrayList<ResultTopic> modelX = new ArrayList<>();
                    run.add(modelX);

                    for (int q : topics) {
                        modelX.add(new ResultTopic(q));
                    }

                    while ((sCurrentLine = br.readLine()) != null) {
                        String[] parsedLine = sCurrentLine.split(" ");
                        int topicId = Integer.parseInt(parsedLine[0]);
                        modelX.get(topics.indexOf(topicId)).add(sCurrentLine);
                    }

                    for (ResultTopic topic : modelX) {
                        topic.normalize("base");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //trasformo in oggetto le run dei file txt
            //trovo tutti i file relativi alle run dentro le cartelle e sottocartelle di RUN_PATH
            getFile(RUN_PATH, runs);

            run = new ArrayList<>();

            //carico in memoria le run
            for (String FILENAME : runs) {

                try {

                    FileReader fr = new FileReader(FILENAME);
                    BufferedReader br = new BufferedReader(fr);

                    String sCurrentLine;

                    ArrayList<ResultTopic> modelX = new ArrayList<>();
                    run.add(modelX);

                    for (int q : topics) {
                        modelX.add(new ResultTopic(q));
                    }

                    while ((sCurrentLine = br.readLine()) != null) {
                        String[] parsedLine = sCurrentLine.replaceAll("\\s+", " ").trim().split(" ");
                        int topicId = Integer.parseInt(parsedLine[0]);
                        modelX.get(topics.indexOf(topicId)).add(sCurrentLine);
                    }

                    for (ResultTopic topic : modelX) {
                        topic.normalize("base");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Parse the Grand Truth file and save it in the HashMap GT
     */
    public static void getGT() {
        GT = new HashMap<>();
        try {

            FileReader fr = new FileReader(GT_FILE);
            BufferedReader br = new BufferedReader(fr);

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                String[] tmp = sCurrentLine.split(" ");
                GT.put(tmp[0] + "/" + tmp[2], tmp[3].equals("0") ? false : true); //lo slassssh non è messo a caso ma ha il suo senso, im not joking guys its important, trust me
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform fusion algorithm and save the ranking ladder
     * @param run  ArrayList of run for each model
     * @param listRankFusion    list of the fusion data algorithms
     */
    public static void createFinalRank(ArrayList<ArrayList<ResultTopic>> run, ArrayList<RankFusion> listRankFusion) {

        for (int topic = 0; topic < Utils.topics.size(); topic++) {
            if (!ProbFuse.train_topics.contains(topics.get(topic))) {
                //per ogni run si costruisce una hash map <DocID,List> di risultati
                HashMap<String, ArrayList<MultipleResultLine>> docResult = new HashMap<>();

                for (ArrayList<ResultTopic> model : run) {
                    //risultati (o linee) di quel topic
                    ArrayList<MultipleResultLine> documents = model.get(topic).getLines();
                    for (MultipleResultLine line : documents) {
                        if (!docResult.containsKey(line.getDocId())) {
                            docResult.put(line.getDocId(), new ArrayList<MultipleResultLine>());
                        }
                        docResult.get(line.getDocId()).add(line);
                    }
                }

                //now the fusion ranking
                for (RankFusion rankFusion : listRankFusion) {
                    ArrayList<SingleResultLine> fusionRank = new ArrayList<>();

                    for (Map.Entry<String, ArrayList<MultipleResultLine>> document : docResult.entrySet()) {
                        SingleResultLine documentFusionLine = new SingleResultLine();

                        double score = rankFusion.algorithm.computeScore(document.getValue());

                        //prende la prima linea relativa a quel documento per riempire i campi comuni e cambia lo score con quello del fusion ranking
                        documentFusionLine.migrate(document.getValue().get(0), score);
                        fusionRank.add(documentFusionLine);
                    }

                    //ora ordino
                    Collections.sort(fusionRank, new Utils.CustomComparator());


                    File FILENAMEFUSIONRANKING = new File("trec_eval/resultFusionRank" + rankFusion.algorithm.getClass().getSimpleName() + ".res"); //scelta arbitraria, non è relativo al sistema 0 ma è il risultato della fusione di tutti e 10 i sistem

                    Utils.writeToFile(fusionRank, FILENAMEFUSIONRANKING.getPath(), 1000);
                }
            }
        }
    }

    /**
     * Evalutation of the data fusion with trec_eval
     * @param listRankFusion    list of fusion algorithms used
     * @return  trec_eval result
     */
    public static HashMap<String, String> evaluateTerrier(ArrayList<RankFusion> listRankFusion) {
        HashMap<String, String> result_trec_eval = new HashMap<>();
        for (RankFusion rankFusion : listRankFusion) {
            String trec_eval = Utils.executeCommand("trec_eval/trec_eval " + GT_FILE + " " + RESULTFUSION_PATH + "/resultFusionRank" + rankFusion.algorithm.getClass().getSimpleName() + ".res", true);
            result_trec_eval.put(rankFusion.algorithm.getClass().getSimpleName(), trec_eval);
            try {
                File file = new File(RESULTFUSION_PATH + "/resultFusionRank" + rankFusion.algorithm.getClass().getSimpleName() + ".res");
                file.delete();
            } catch (Exception e) {
            }
        }
        return result_trec_eval;
    }

    /**
     *
     */
    static class CustomComparator implements Comparator<SingleResultLine> {

        public int compare(SingleResultLine o1, SingleResultLine o2) {
            return -Double.compare(o1.getScore(), o2.getScore());
        }
    }

    /**
     * Filter the results and create a array of double for the score specified
     * @param results   arrayList of results
     * @param scoreUsed score to insert in output
     * @return  array of score
     */
    static double[] toDoubleArray(ArrayList<MultipleResultLine> results, String scoreUsed) {
        double[] scoreArray = new double[results.size()];
        int count = 0;
        for (MultipleResultLine line : results) {
            scoreArray[count] = line.getScore(scoreUsed);
            count++;
        }
        Arrays.sort(scoreArray);
        return scoreArray;
    }

    /**
     * Execute a command in the terminal and in case returns the result
     * @param command   command to be executed
     * @param returnString  needed of the output result
     * @return  result of the command execution
     */
    public static String executeCommand(String command, boolean returnString) {

        StringBuilder s = new StringBuilder();
        String str;
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            if (returnString) {
                while ((str = br.readLine()) != null) {
                    s.append(str);
                    s.append("\n");
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

    /**
     * Write a defined number of result to a file
     *
     * @param toWrite   data to be written
     * @param path      where write data
     * @param howMany   how many result write to file
     */
    public static void writeToFile(ArrayList<SingleResultLine> toWrite, String path, int howMany) {
        try {
            int count = 0;
            FileWriter fw = new FileWriter(path, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            for (SingleResultLine line : toWrite) {
                if (count < howMany)
                    out.println(line.toString(count));
                count++;
            }
            out.flush();
            out.close();
            bw.close();
            fw.close();
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }

    /**
     * Delete a file or a directory and its children.
     *
     * @param file the directory to delete.
     * @throws IOException exception when problem occurs during deleting the directory.
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

    /**
     * Retrieve and store the path of files in directory and in subdirectory of the given directory
     *
     * @param directoryName path of the given directory to analyze
     * @param files container where store retrieved path files
     */
    public static void getFile(String directoryName, ArrayList<String> files) {
        File directory = new File(directoryName);

        // estrae tutti i file da una directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile() && !file.getName().startsWith(".")) {
                files.add(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                getFile(file.getAbsolutePath(), files);
            }
        }
    }

    /**
     * Parse topic file and store topics identifier in @topics
     */
    public static void getTopics() {
        topics = null;
        try {
            FileReader fr = new FileReader(TOPIC_FILE);
            BufferedReader br = new BufferedReader(fr);
            String sCurrentLine;
            topics = new ArrayList<>();
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.contains("<num>")) {
                    topics.add(Integer.parseInt(sCurrentLine.split(":")[1].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start of class Section
     */
    public static class ResultTopic {
        private int topicId;
        private ArrayList<MultipleResultLine> lines;

        //il loro settaggio si basa sul fatto che il primo ResultLine avra' lo score piu' alto
        //e l'ultimo ResultLine avra' lo score piu' basso (sono ordinati)
        private double maxScore = -1;
        private double minScore = -1;

        public ResultTopic(int Id) {
            lines = new ArrayList<>();
            topicId = Id;
        }

        //aggiune la riga
        public void add(String line) {
            MultipleResultLine currentLine = new MultipleResultLine();
            topicId = currentLine.set(line);
            if (lines.size() == 0) {
                maxScore = currentLine.getScore("base");
            } else {
                minScore = currentLine.getScore("base");
            }
            lines.add(currentLine);
        }

        public int getTopicID() {
            return topicId;
        }

        public void normalize(String scoreName) {
            for (MultipleResultLine line : lines) {
                line.setScore(scoreName, (line.getScore(scoreName) - minScore) / (maxScore - minScore));
            }
        }

        public ArrayList<MultipleResultLine> getLines() {
            return lines;
        }
    }

    public static class ResultLine {
        private int topicId;
        private String q0;            //da sistemare
        private String docId;
        private int rank;
        private String runId;

        public ResultLine() {
            topicId = -1;
            q0 = "NULL";
            docId = "NULL";
            rank = -1;
            runId = "NULL";
        }

        //ritorna il topicID
        public int set(String line) {
            String[] parsedLine = line.replaceAll("\\s+", " ").trim().split(" ");
            topicId = Integer.parseInt(parsedLine[0]);
            q0 = parsedLine[1];
            docId = parsedLine[2];
            rank = Integer.parseInt(parsedLine[3]);
            runId = parsedLine[5];
            return topicId;
        }

        public void clone(ResultLine oldObject) {
            topicId = oldObject.getTopicId();
            q0 = oldObject.getQ0();
            docId = oldObject.getDocId();
            rank = -1;
            runId = oldObject.getRunId();
        }

        public int getTopicId() {
            return topicId;
        }

        public String getQ0() {
            return q0;
        }

        public String getDocId() {
            return docId;
        }

        public String getRunId() {
            return runId;
        }

    }

    public static class SingleResultLine extends ResultLine {
        private double score;

        public SingleResultLine() {
            super();
            score = -1;
        }

        public void migrate(MultipleResultLine oldObject, double valueScore) {
            super.clone(oldObject);
            score = valueScore;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double value) {
            score = value;
        }

        public String toString() {
            return "" + super.topicId + " " + super.q0 + " " + super.docId + " " + super.rank + " " + score + " " + super.runId;
        }

        public String toString(int positionValue) {
            return "" + super.topicId + " " + super.q0 + " " + super.docId + " " + positionValue + " " + score + " " + super.runId;
        }
    }

    public static class MultipleResultLine extends ResultLine {
        private HashMap<String, Double> score;

        public MultipleResultLine() {
            super();
            score = new HashMap<>();
        }

        //ritorna il topicID
        public int set(String line) {
            super.set(line);
            String[] parsedLine = line.replaceAll("\\s+", " ").trim().split(" ");
            score.put("base", Double.parseDouble(parsedLine[4]));
            return super.getTopicId();
        }

        public double getScore(String scoreName) {
            return score.get(scoreName);
        }

        public void setScore(String scoreName, double value) {
            if (score.containsKey(scoreName)) {
                score.replace(scoreName, value);
            } else {
                score.put(scoreName, value);
            }
        }

    }

    public static class RankFusion {

        public RankFusionIF algorithm;

        public FileWriter fwMean = null;
        public BufferedWriter bwMean = null;
        public PrintWriter outMean = null;

        public FileWriter fwVariance = null;
        public BufferedWriter bwVariance = null;
        public PrintWriter outVariance = null;

        public double somma = 0;
        public LinkedList<Double> mapValues;

        public RankFusion(RankFusionIF alg) {
            algorithm = alg;
        }

        public void initialize() {
            try {
                Utils.executeCommand("rm " + CSV_PATH + "resultsMean" + algorithm.getClass().getSimpleName() + ".csv", false);
                fwMean = new FileWriter(CSV_PATH + "resultsMean" + algorithm.getClass().getSimpleName() + ".csv", false);
                bwMean = new BufferedWriter(fwMean);
                outMean = new PrintWriter(bwMean);
                outMean.println("#segmenti");
                outMean.print(";");
                Utils.executeCommand("rm " + CSV_PATH + "resultsVariance" + algorithm.getClass().getSimpleName() + ".csv", false);
                fwVariance = new FileWriter(CSV_PATH + "resultsVariance" + algorithm.getClass().getSimpleName() + ".csv", false);
                bwVariance = new BufferedWriter(fwVariance);
                outVariance = new PrintWriter(bwVariance);
                outVariance.println("#segmenti");
                outMean.print(";");
            }
            catch (Exception e){
                System.err.println("Controllare la configurazione delle directory, possibili directory mancanti");
            }
        }

        public void printAll(String s) {
            printMean(s);
            printVar(s);
        }

        public void printMean(String s) {
            outMean.print(s);
            outMean.flush();
        }

        public void printVar(String s) {
            outVariance.print(s);
            outVariance.flush();
        }

        public void initializeParameters() {
            somma = 0;
            mapValues = new LinkedList<>();
        }

        public void close() throws Exception {
            if (outMean != null) outMean.close();
            if (bwMean != null) bwMean.close();
            if (fwMean != null) fwMean.close();

            if (outVariance != null) outVariance.close();
            if (bwVariance != null) bwVariance.close();
            if (fwVariance != null) fwVariance.close();
        }
    }

    public static class ListRankFusion {
        public ArrayList<RankFusion> listRF;

        public ListRankFusion() {
            listRF = new ArrayList<>();
        }

        public void add(RankFusion RF) {
            listRF.add(RF);
        }

        public void initializeAll() throws Exception {
            for (Utils.RankFusion RF : listRF) {
                RF.initialize();
            }

        }

        public void printAll(String s) {
            for (Utils.RankFusion RF : listRF) {
                RF.printAll(s);
            }
        }

        public void initializeParametersAll() {
            for (Utils.RankFusion RF : listRF) {
                RF.initializeParameters();
            }
        }

        public void update(HashMap<String, String> resultTrecEval) {
            for (RankFusion RF : listRF) {
                double map = Double.parseDouble(resultTrecEval.get(RF.algorithm.getClass().getSimpleName()).split("map")[1].split("gm_ap")[0].split("\t")[2]);
                RF.somma += map;
                RF.mapValues.add(map);
            }

        }

        public void writeResult() {
            for (RankFusion RF : listRF) {
                double mean = RF.somma / EXPERIMENT;
                double variance = 0;
                for (Double value : RF.mapValues) {
                    variance = variance + Math.pow(value - mean, 2);
                }
                variance = variance / EXPERIMENT;
                RF.printMean(mean + ";");
                RF.printVar(variance + ";");
            }
        }

        public void close() throws Exception {
            for (Utils.RankFusion RF : listRF) {
                RF.close();
            }
        }
    }
}