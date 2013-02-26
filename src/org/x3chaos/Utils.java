package org.x3chaos;

public class Utils {

	public static String splitStringArray(String[] array, int index) {
		String result = "";
		for (int i = index; i < array.length; i++) {
			result += array[i] + " ";
		}
		return result.trim();
	}

	public static String mergeStringArray(String[] array) {
		return splitStringArray(array, 0);
	}

	public static int getMax(int[] range, int cap) {
		int result = 0;
		for (int i : range)
			if (i > result && i <= cap) result = i;

		return result;
	}

	public static int getMin(int[] range, int cap) {
		int result = cap;
		for (int i : range)
			if (i < result && i >= cap) result = i;

		return result;
	}
}
