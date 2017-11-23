package com.latuarisposta;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Utils {
	static class CustomComparator implements Comparator<Main.ResultLine> {

		public int compare(Main.ResultLine o1, Main.ResultLine o2) {
			return -Double.compare(o1.getScore(), o2.getScore());
		}
	}

	public static void writeToFile(ArrayList<Main.ResultLine> toWrite, String path,int howMany) {
		try {
			int count=0;
			FileWriter fw = new FileWriter(path, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			for (Main.ResultLine line : toWrite) {
				if(count<howMany)
					out.println(line.toString());
				count++;
			}
			out.flush();
			fw.close();
		} catch (IOException e) {
			//exception handling left as an exercise for the reader
		}
	}

	/**
	 * Delete a file or a directory and its children.
	 * @param file The directory to delete.
	 * @throws IOException Exception when problem occurs during deleting the directory.
	 */
	public static void delete(File file) throws IOException {

		for (File childFile : file.listFiles()) {

			if (childFile.isDirectory()) {
				delete(childFile);
			} else {
				if (!childFile.delete()) {
					throw new IOException();
				}
			}
		}

		if (!file.delete()) {
			throw new IOException();
		}
	}
}
