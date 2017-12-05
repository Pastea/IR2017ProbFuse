package com.latuarisposta;

import java.util.ArrayList;

import static com.latuarisposta.Utils.executeCommand;
import static com.latuarisposta.Utils.gandalfiles_ushallnotpassargument;
import static com.latuarisposta.Utils.theyretakingthehobbitstoisengard;

public class BaseFuse {

	private String result_trec_eval;

	public BaseFuse() {
		ArrayList<ArrayList<Utils.ResultTopic>> result = gandalfiles_ushallnotpassargument();

		//result contiene alla fine un array dei 10 sistemi e per ogni sistema un array di 50 topic dove all'interno troviamo l'id del topic e un array con gli score di ogni documento

		theyretakingthehobbitstoisengard(result, new CombMNZ());

		//valutazione usando qrels, di default il fusion ranking e' in terrier-core-4.2-0
		//executeCommand("terrier-core-4.2-0/bin/trec_terrier.sh -e -Dtrec.qrels=qrels/qrels.trec7.bin");

		result_trec_eval=executeCommand("trec_eval/trec_eval qrels/qrels.trec7.bin terrier-core-4.2-0/var/results/resultFusionRanking.res", true);

	}

	public BaseFuse(ArrayList<Integer> excluded_topics) {
		ArrayList<ArrayList<Utils.ResultTopic>> result = gandalfiles_ushallnotpassargument();

		//result contiene alla fine un array dei 10 sistemi e per ogni sistema un array di 50 topic dove all'interno troviamo l'id del topic e un array con gli score di ogni documento

		//elimina topic usati nel training di probfuse
		for (ArrayList<Utils.ResultTopic> model : result) {
			for (int i = model.size() - 1; i > 0; i--) {
				int topicId = model.get(i).getTopicID();
				//scansiona la lista dei topic di training
				for (int j = 0; j < excluded_topics.size(); j++) {
					if (excluded_topics.get(j).compareTo(topicId) == 0) {
						model.remove(i);
						//per sicurezza riparte a controllare ogni volta che rimuove qualcosa
						i = model.size() - 1;
					}
				}
			}
		}

		theyretakingthehobbitstoisengard(result, new CombMNZ());

		//valutazione usando qrels, di default il fusion ranking e' in terrier-core-4.2-0
		//executeCommand("terrier-core-4.2-0/bin/trec_terrier.sh -e -Dtrec.qrels=qrels/qrels.trec7.bin");

		result_trec_eval = executeCommand("trec_eval/trec_eval qrels/qrels.trec7.bin terrier-core-4.2-0/var/results/resultFusionRanking.res", true);
	}

	public String getResult_trec_eval() {
		return result_trec_eval;
	}
}