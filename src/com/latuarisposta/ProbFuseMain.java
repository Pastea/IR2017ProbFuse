package com.latuarisposta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ProbFuseMain {

    public static void main(String[] args){
        double t= 0.3;        //percentuale query training set
        int k=11;           //dimensione segmento, il paper indica 90 come numero di segmenti, 1000/90=11
        ArrayList<Integer> train_queries = new ArrayList<>();

        while(train_queries.size()<t*50){
            int tmp = (int)(Math.random()*50)+351;
            if(!train_queries.contains(tmp)){
                train_queries.add(tmp);
            }
        }

        ArrayList<ArrayList<Main.ResultTopic>> frodo = Main.gandalfiles_ushallnotpassargument(); //sistema --> topic --> lines(documenti)

        HashMap<String,Boolean> thering = new HashMap<>();

        try {

            FileReader fr = new FileReader("qrels/qrels.trec7.bin");
            BufferedReader br = new BufferedReader(fr);

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                String[] tmp = sCurrentLine.split(" ");
                thering.put(tmp[0]+"/"+tmp[2],tmp[3].equals("0")?false:true); //lo slassssh non è messo a caso ma ha il suo senso, im not joking guys its important, trust me
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<ArrayList<Float>> Pdkm= new ArrayList<ArrayList<Float>>();
        int n=0;
        int ndoc= frodo.get(0).get(0).getLines().size();

        for(int s=0; s<Utils.how_many_models;s++){
            ArrayList<Float> tmp = new ArrayList<Float>();
            for(n=0;n<ndoc/k+1;n++) {
                float sum_rkq=0f;
                for (int i : train_queries) {
                    float Rkq = 0;
                    ArrayList<Main.ResultLine> rl = frodo.get(s).get(i-351).getLines();
                    for (int d = n*k; d < (n+1)*k && d < rl.size(); d++) {
                        if (thering.containsKey( i+ "/" + rl.get(d).getDocName())) {
                            if(thering.get(i+ "/" + rl.get(d).getDocName())){
                                Rkq++;
                            }
                        }
                    }
                    if((n+1)*k>rl.size()){
                        sum_rkq=sum_rkq + Rkq / (rl.size()-n*k);
                    }
                    else{
                        sum_rkq=sum_rkq + Rkq / k;
                    }
                }
                tmp.add(sum_rkq / train_queries.size());
            }
            Pdkm.add(tmp);
        }

        //t=TOPIC SCELTO
        for(int query=0; query<50;query++){
            if (!train_queries.contains(query+351)){
                for(int s=0; s<Utils.how_many_models;s++){
                    for(int doc=0; doc<frodo.get(s).get(query).getLines().size()-1; doc++) {
                        Main.ResultLine rl = frodo.get(s).get(query).getLines().get(doc);
                        rl.setScore(Pdkm.get(s).get(doc/k)/(doc/k+1));
                    }
                }
            }
        }

        Main.theyretakingthehobbitstoisengard(frodo,new ProbFuse());

        Main.executeCommand("trec_eval/trec_eval qrels/qrels.trec7.bin terrier-core-4.2-0/var/results/resultFusionRanking.res");

        System.out.println("Il mio tessoro");
    }


}
