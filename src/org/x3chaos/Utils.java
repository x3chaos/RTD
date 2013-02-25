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

}
