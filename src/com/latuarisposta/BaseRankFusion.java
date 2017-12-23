package com.latuarisposta;

import java.util.ArrayList;
import java.util.Arrays;

import static com.latuarisposta.Utils.toDoubleArray;

interface RankFusionIF {
	//tipo di score utilizzato per fare il calcolo dello score finale, base si riferisce al punteggio dato da terrier
	String scoreUsed="";
	double computeScore(ArrayList<Utils.MultipleResultLine> results);
}

class CombMAX implements RankFusionIF {
	public String scoreUsed = "base";
	public double computeScore(ArrayList<Utils.MultipleResultLine> results) {
		double[] scoreArray = toDoubleArray(results,scoreUsed);
		double max = scoreArray[scoreArray.length - 1];
		return max;
	}
}

class CombMIN implements RankFusionIF {
	public String scoreUsed = "base";
	public double computeScore(ArrayList<Utils.MultipleResultLine> results) {
		double[] scoreArray = toDoubleArray(results,scoreUsed);

		double min = scoreArray[0];
		return min;

	}
}

class CombSUM implements RankFusionIF {
	//the summation of the set of similarity values, or, equivalently, the numerical mean of the set of the set of similarity
	public String scoreUsed = "base";

	public double computeScore(ArrayList<Utils.MultipleResultLine> results) {
		double[] target = toDoubleArray(results,scoreUsed);

		double sum = 0;
		for (int i = 0; i < target.length; i++) {
			sum = sum + target[i];
		}

		return sum;
	}

}

class CombMED implements RankFusionIF {
	//the median similarity value
	public String scoreUsed = "base";

	public double computeScore(ArrayList<Utils.MultipleResultLine> results) {
		double[] target = toDoubleArray(results,scoreUsed);
		Arrays.sort(target);
		int middle = target.length / 2;
		if (target.length % 2 == 0) {
			return (target[middle] + target[middle - 1]) / 2;
		} else {
			return target[middle + 1];
		}
	}
}

class CombANZ implements RankFusionIF {
	//average of the non-zero similarity values
	public String scoreUsed = "base";

	public double computeScore(ArrayList<Utils.MultipleResultLine> results) {
		double[] target = toDoubleArray(results,scoreUsed);

		double nonZero = 0;
		double sum = 0;
		for (int i = 0; i < target.length; i++) {
			sum = sum + target[i];
			if (target[i] > 0) nonZero++;
		}

		//evita risultati NaN
		if (nonZero == 0) return 0;
		return (sum / nonZero);
	}

}

class CombMNZ implements RankFusionIF {
	//average of the non-zero similarity values
	public String scoreUsed = "base";

	public double computeScore(ArrayList<Utils.MultipleResultLine> results) {
		double[] target = toDoubleArray(results,scoreUsed);

		double nonZero = 0;
		double sum = 0;
		for (int i = 0; i < target.length; i++) {
			sum = sum + target[i];
			if (target[i] > 0) nonZero++;
		}

		return (sum * nonZero);
	}
}

class ProbFuseAll implements RankFusionIF {
	public String scoreUsed = "ProbFuseAll";

	public double computeScore(ArrayList<Utils.MultipleResultLine> results) {
		double[] target = toDoubleArray(results,scoreUsed);

		double sum = 0;
		for (int i = 0; i < target.length; i++) {
			sum = sum + target[i];
		}

		return sum;
	}
}

class ProbFuseJudged implements RankFusionIF {
	public String scoreUsed = "ProbFuseJudged";

	public double computeScore(ArrayList<Utils.MultipleResultLine> results) {
		double[] target = toDoubleArray(results,scoreUsed);

		double sum = 0;
		for (int i = 0; i < target.length; i++) {
			sum = sum + target[i];
		}

		return sum;
	}
}
