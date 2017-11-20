package com.latuarisposta;

import java.util.ArrayList;
import java.util.Arrays;

public class BaseRankFusion {

	static double[] toDoubleArray(ArrayList<Main.ResultLine> results) {
		double[] scoreArray = new double[results.size()];
		int count = 0;
		for (Main.ResultLine line : results) {
			scoreArray[count] = line.getScore();
			count++;
		}
		Arrays.sort(scoreArray);
		return scoreArray;
	}


	static double CombMAX(ArrayList<Main.ResultLine> results) {
		double[] scoreArray = toDoubleArray(results);

		double max = scoreArray[scoreArray.length - 1];
		return max;
	}


	static double CombMIN(ArrayList<Main.ResultLine> results) {
		double[] scoreArray = toDoubleArray(results);

		double min = scoreArray[0];
		return min;

	}

	//the summation of the set of similarity values, or, equivalently, the numerical mean of the set of the set of similarity
	static double CombSUM(ArrayList<Main.ResultLine> results) {
		double[] target = toDoubleArray(results);

		double sum = 0;
		for (int i = 0; i < target.length; i++) {
			sum = sum + target[i];
		}

		return sum;
	}

	//the median similarity value
	static double CombMED(ArrayList<Main.ResultLine> results) {
		double[] target = toDoubleArray(results);
		Arrays.sort(target);
		int middle = target.length / 2;
		if (target.length % 2 == 0) {
			return (target[middle] + target[middle - 1]) / 2;
		} else {
			return target[middle + 1];
		}
	}

	//average of the non-zero similarity values
	static double CombANZ(ArrayList<Main.ResultLine> results) {
		double[] target = toDoubleArray(results);

		double nonZero = 0;
		double sum = 0;
		for (int i = 0; i < target.length; i++) {
			sum = sum + target[i];
			if (target[i] > 0) nonZero++;
		}

		//evita risultati NaN
		if(nonZero==0) return 0;
		return (sum / nonZero);
	}

	//average of the non-zero similarity values
	static double CombMNZ(ArrayList<Main.ResultLine> results) {
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
