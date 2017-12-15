package com.latuarisposta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ProbFuseMainProva {

    private ArrayList<Integer> train_queries;
    private String result_trec_eval;

    public ProbFuseMainProva(int nSeg) {

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

        ArrayList<ArrayList<Utils.ResultTopic>> terrier = Utils.terrier(); //sistema --> topic --> lines(documenti)

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

        ProbFuseHandler cont = new ProbFuseHandler();   //lista di sistemi, ognuno con le 50 query, ognuna con i segmenti,ognuno con le linee del segmento
        for (int s = 0; s < Utils.how_many_models; s++) //scorro i sistemi
        {
            cont.addSystem();                           //creo un sistema
            for (int i=0; i<50; i++)                    //scorro le query
            {
                cont.addQuery(s);                       //creo una query
                ArrayList<Utils.ResultLine> documents = terrier.get(s).get(i).getLines();
                int k = documents.size() / x;           //numero di documenti per segmento, prendo la parte bassa
                int left = documents.size()-k*x;
                int offset = 0;
                for (int n = 0; n < x; n++)             //scorro i segmenti
                {
                    cont.addSegment(s,i);            //creo un segmento

                    for (int p = n*k+offset; p<(n+1)*k+offset; p++)   //smisto i documenti nel loro segmento
                    {
                        cont.addLine(s,i,n, documents.get(p)); //aggiungo un risultato
                    }
                    if(left>0)
                    {
                        cont.addLine(s,i,n,documents.get((n+1)*k+offset));
                        left--;
                        offset++;
                    }

                }
            }
        }

        List<List<Float>> pdkm = new LinkedList<>();
        float rkq = 0;
        float tmp = 0;

        //per ogni modello fisso un segmento, per ogni query guardo i documenti rilevanti in quel segmento e calcolo P(d_k|m)
        for (int s = 0; s < Utils.how_many_models; s++) //scorro i modelli
        {
            pdkm.add(new LinkedList<>());               //aggiungo un modello
            for (int n = 0; n < x; n++)                 //scorro i segmenti
            {
                for (int i : train_queries)             //scorro le query
                {
                    List<Utils.ResultLine> documents = cont.getSegment(s,i-351,n);
                    for (Utils.ResultLine l : documents)
                    {                            //Per ogni documento nel segmento n, vedo se è rilevante o meno
                        if(GT.containsKey(i + "/" + l.getDocName()))
                            if (GT.get(i + "/" + l.getDocName()))
                                rkq++;
                    }
                    tmp += rkq / cont.getSegmentSize(s, i-351, n);
                    rkq = 0;
                }
                pdkm.get(s).add(tmp/train_queries.size());
                tmp = 0;
            }
        }


        //Setto lo score di un documento pari a pkdm/k per poi sommarli successivamente
        for (int s = 0; s < Utils.how_many_models; s++)
        {
            for (int query = 0; query < 50; query++)
            {
                if (!train_queries.contains(query + 351))
                {
                    for(int i=0; i<x; i++)
                    {
                        for (Utils.ResultLine l: cont.getSegment(s,query,i))
                        {
                            l.setScore(pdkm.get(s).get(i)/(i+1));
                        }
                    }
                }
            }
        }

        //elimina topic di training e rileva se c'e' stato un cattivo training
        List<List<List<Utils.ResultLine>>> model;
        for (int k=0; k<Utils.how_many_models; k++) //scorro i modelli
        {
            model = cont.getSystem(k);
            for (int i = model.size() - 1; i >= 0; i--) //scorro le query
            {
               for(int tq : train_queries){
                   if(tq-351==i)
                       model.remove(i);
               }
            }
        }

        Utils.theyretakingthehobbitstoisengardTheSequel(cont, new CombProbFuse());

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