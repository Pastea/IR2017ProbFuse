package com.latuarisposta;


import java.util.LinkedList;
import java.util.List;

public class Main {


	public static void main(String[] args) throws Exception {

		int maxSegm = 1000;
		int step = 20;
		int numExp = 10;
		List<List<Float>> results = new LinkedList<>();
		for(int k=1; k<maxSegm; k=k+step) {
			List<Float> diocane = new LinkedList<>();
			for(int i=0; i<numExp; i++) {
				ProbFuseMainProva probfuse = new ProbFuseMainProva();
				//BaseFuse basefuse=new BaseFuse(probfuse.getTrainQueries());

				System.out.println("ProbFuse\n" + probfuse.getResult_trec_eval());
				//System.out.println("baseFuse\n"+basefuse.getResult_trec_eval());

				float map_probfuse = Float.parseFloat(probfuse.getResult_trec_eval().split("map")[1].split("gm_ap")[0].split("\t")[2]);

				//float map_basefuse =Float.parseFloat(basefuse.getResult_trec_eval().split("map")[1].split("gm_ap")[0].split("\t")[2]);
				diocane.add(map_probfuse);
				System.out.println("Map\nProb: " + map_probfuse);//+"\t Base:"+map_basefuse);
			}
			results.add(diocane);
		}
		System.out.println("Dopo qui non ci sono più le bestemmie, promesso");
	}
}
