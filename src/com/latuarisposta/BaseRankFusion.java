package com.latuarisposta;

import java.util.ArrayList;
import java.util.Arrays;

import static com.latuarisposta.Utils.toDoubleArray;

interface RankFusionIF {
	public double computeScore(ArrayList<Utils.ResultLine> results);
}

class CombMAX implements RankFusionIF {
	public double computeScore(ArrayList<Utils.ResultLine> results) {
		double[] scoreArray = toDoubleArray(results);

		double max = scoreArray[scoreArray.length - 1];
		return max;
	}
}

class CombMIN implements RankFusionIF {
	public double computeScore(ArrayList<Utils.ResultLine> results) {
		double[] scoreArray = toDoubleArray(results);

		double min = scoreArray[0];
		return min;

	}
}

class CombSUM implements RankFusionIF {
	//the summation of the set of similarity values, or, equivalently, the numerical mean of the set of the set of similarity
	public double computeScore(ArrayList<Utils.ResultLine> results) {
		double[] target = toDoubleArray(results);

		double sum = 0;
		for (int i = 0; i < target.length; i++) {
			sum = sum + target[i];
		}

		return sum;
	}

}

class CombMED implements RankFusionIF {
	//the median similarity value
	public double computeScore(ArrayList<Utils.ResultLine> results) {
		double[] target = toDoubleArray(results);
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
	public double computeScore(ArrayList<Utils.ResultLine> results) {
		double[] target = toDoubleArray(results);

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
	public double computeScore(ArrayList<Utils.ResultLine> results) {
		double[] target = toDoubleArray(results);

		double nonZero = 0;
		double sum = 0;
		for (int i = 0; i < target.length; i++) {
			sum = sum + target[i];
			if (target[i] > 0) nonZero++;
		}

		return (sum * nonZero);
	}
}

class CombProbFuse implements RankFusionIF {
	public double computeScore(ArrayList<Utils.ResultLine> results) {
		double[] target = toDoubleArray(results);

		double sum = 0;
		for (int i = 0; i < target.length; i++) {
			sum = sum + target[i];
		}

		return sum;
	}
}
