package com.latuarisposta;


import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Main {


	public static void main(String[] args) throws Exception {

		int maxSegm = 1000;
		int step = 20;
		int numExp = 20;
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		try
		{
			fw = new FileWriter("results.txt", true);
			bw = new BufferedWriter(fw);
			out = new PrintWriter(bw);

			for (int k = 1; k < maxSegm; k = k + step)
			{
				float somma = 0;
				for (int i = 0; i < numExp; i++)
				{
					ProbFuseMainProva probfuse = new ProbFuseMainProva(k);
					//BaseFuse basefuse=new BaseFuse(probfuse.getTrainQueries());

					System.out.println("ProbFuse\n" + probfuse.getResult_trec_eval());
					//System.out.println("baseFuse\n"+basefuse.getResult_trec_eval());

					float map_probfuse = Float.parseFloat(probfuse.getResult_trec_eval().split("map")[1].split("gm_ap")[0].split("\t")[2]);
					somma += map_probfuse;
					//float map_basefuse =Float.parseFloat(basefuse.getResult_trec_eval().split("map")[1].split("gm_ap")[0].split("\t")[2]);
					System.out.println("Map\nProb: " + map_probfuse);//+"\t Base:"+map_basefuse);
				}

				out.println("# segmenti: " + k + " -> map = " + somma / numExp);
				out.flush();
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		finally
		{
			System.out.println("Dopo qui non ci sono più le bestemmie, promesso");
			out.close();
			bw.close();
			fw.close();
		}
	}
}
