package com.latuarisposta;



public class Main {


	public static void main(String[] args) throws Exception {

		ProbFuse probfuse=new ProbFuse();
		BaseFuse basefuse=new BaseFuse(probfuse.getTrainQueries());

		System.out.println("ProbFuse\n"+probfuse.getResult_trec_eval());
		System.out.println("baseFuse\n"+basefuse.getResult_trec_eval());

		float map_probfuse =Float.parseFloat(probfuse.getResult_trec_eval().split("map")[1].split("gm_ap")[0].split("\t")[2]);

		float map_basefuse =Float.parseFloat(basefuse.getResult_trec_eval().split("map")[1].split("gm_ap")[0].split("\t")[2]);

		System.out.println("Map\nProb: "+map_probfuse+"\t Base:"+map_basefuse);
	}
}