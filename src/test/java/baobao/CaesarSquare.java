package baobao;

import java.util.ArrayList;
import java.util.List;

public class CaesarSquare {
	public static void main(String[] args) {
		String aaa = "A long time ago, in a galaxy far, far away";
		aaa = aaa.trim().replace(" ", "").replace("\t", "").replace("\r", "").replace("\n", "");
		int length = aaa.length();
		int num = (int) Math.ceil(Math.sqrt(length));
		List<List<Character>> lsResult = new ArrayList<List<Character>>();
		for (char word : aaa.toCharArray()) {
			List<Character> lsTmp = getLsChar(lsResult, num);
			lsTmp.add(word);
		}
		List<Character> lsEncodeResult = new ArrayList<Character>();
		for (int j = 0; j < num; j++) {
			for (List<Character> list : lsResult) {
				if (j < list.size()) {
					lsEncodeResult.add(list.get(j));
				} else {
					continue;
				}
			}
		}
	
		char[] result = new char[lsEncodeResult.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = lsEncodeResult.get(i);
		}
		String encode = String.valueOf(result);
		System.out.println(encode);
		
	}
	
	private static List<Character> getLsChar(List<List<Character>> lsMatrix, int length) {
		List<Character> lsResult = null;
		if (lsMatrix.size() == 0 || lsMatrix.get(lsMatrix.size() - 1).size() == length) {
			lsResult = new ArrayList<Character>();
			lsMatrix.add(lsResult);
		} else {
			lsResult = lsMatrix.get(lsMatrix.size() - 1);
		}
		return lsResult;
	}
	
	
}
