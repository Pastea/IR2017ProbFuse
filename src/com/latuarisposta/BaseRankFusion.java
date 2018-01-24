package com.latuarisposta;

import java.util.ArrayList;
import java.util.Arrays;

import static com.latuarisposta.Utils.toDoubleArray;

interface RankFusionIF {
	//tipo di score utilizzato per fare il calcolo dello score finale, base si riferisce al punteggio dato da terrier
	double computeScore(ArrayList<Utils.MultipleResultLine> results);
}

/**
 * CombMAX fusion algorithm
 */
class CombMAX implements RankFusionIF {
	public String scoreUsed = "base";
	public double computeScore(ArrayList<Utils.MultipleResultLine> results) {
		double[] scoreArray = toDoubleArray(results,scoreUsed);
		double max = scoreArray[scoreArray.length - 1];
		return max;
	}
}

/**
 * CombMIN fusion alogrithm
 */
class CombMIN implements RankFusionIF {
	public String scoreUsed = "base";
	public double computeScore(ArrayList<Utils.MultipleResultLine> results) {
		double[] scoreArray = toDoubleArray(results,scoreUsed);
		double min = scoreArray[0];
		return min;

	}
}

/**
 * CombSUM fusion algorithm
 */
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

/**
 * CombMED fusion algorithm
 */
class CombMED implements RankFusionIF {
	//the median similarity value
	public String scoreUsed = "base";

	public double computeScore(ArrayList<Utils.MultipleResultLine> results) {
		double[] target = toDoubleArray(results,scoreUsed);
		Arrays.sort(target);
		if (target.length % 2 == 0) {
			return (target[(target.length / 2)-1] + target[target.length / 2]) / 2;
		} else {
			return target[((target.length+1)/ 2)-1];
		}
	}
}

/**
 * CombANZ fusion algorithm
 */
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

/**
 * CombMNZ fusion algorithm
 */
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

/**
 * ProbFuseAll fusion algorithm
 */
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

/**
 * ProbFuseJedged fusion algorithm
 */
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
