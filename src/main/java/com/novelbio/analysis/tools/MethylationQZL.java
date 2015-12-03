package com.novelbio.analysis.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;

public class MethylationQZL {
	private static Logger logger = Logger.getLogger(MethylationQZL.class);
	
	
	public static void main(String[] args) throws Exception {
		String excelFile = "/home/zong0jie/桌面/NB90m VS UN12D.xls";
		String out = "/home/zong0jie/桌面/QZL/NB90mVSUN12D.txt";
		getInfo(excelFile, out);
		
		
	}
	
	
	/**
	 * 输入文件
	 * @param excelFile
	 */
	public static void getInfo(String excelFile, String OutfileName) {
		ExcelOperate excelOperate = new ExcelOperate(excelFile);
		ArrayList<String[]> lsInfo = excelOperate.readLsExcel();
		ArrayList<String[]> lsTmpInfo = new ArrayList<String[]>();
		
		
		
		String accID = "";
		
		HashMap<String, String[]> hashAccIDInfoDown = new HashMap<String, String[]>();
		HashMap<String, String[]> hashAccIDInfoUp = new HashMap<String, String[]>();
		
		
		for (String[] strings : lsInfo) {
			if (strings[8]== null || strings[8].equals("") || strings[8].equals("AccID")) {
				continue;
			}
			if (strings[8]!= null && !strings[8].equals("") && !strings[8].equals(accID)) {
				String[] tmp = copLsInfo(lsTmpInfo);
				if ( tmp == null) {
					accID = strings[8];
				}
				else {
					hashAccIDInfoUp.put(strings[8], tmp);
					lsTmpInfo = new ArrayList<String[]>();
				}
			}
			String[] strings2 = new String[12];
			for (int i = 0; i < strings2.length; i++) {
				strings2[i] = strings[i];
			}
			lsTmpInfo.add(strings2);
			accID = strings[8];
		}
		
		for (String[] strings : lsInfo) {
			if (strings[20]== null || strings[20].equals("") || strings[20].equals("AccID")) {
				continue;
			}
			if (strings[20]!= null && !strings[20].equals("") && !strings[20].equals(accID)) {
				String[] tmp = copLsInfo(lsTmpInfo);
				if ( tmp == null) {
					accID = strings[20];
				}
				else {
					hashAccIDInfoDown.put(strings[20], tmp);
					lsTmpInfo = new ArrayList<String[]>();
				}
			}
			String[] strings2 = new String[12];
			for (int i = 0; i < strings2.length; i++) {
				strings2[i] = strings[i+12];
			}
			lsTmpInfo.add(strings2);
			accID = strings[20];
		}
		
		cope2HashInfo(hashAccIDInfoUp, hashAccIDInfoDown, OutfileName);
		
	}
	
	
	
	
	/**
	 * 给定lsInfo信息，获得坐标middle和各种信息的中位数
	 * @param lsInfo
	 * @return
	 */
	public static String[] copLsInfo(List<String[]> lsInfo) {
		if (lsInfo == null || lsInfo.size() == 0) {
			return null;
		}
		ArrayList<Integer> lsInt4 = new ArrayList<Integer>();
		ArrayList<Double> lsInt5 = new ArrayList<Double>();
		ArrayList<Double> lsInt6 = new ArrayList<Double>();
		ArrayList<Double> lsInt7 = new ArrayList<Double>();
		ArrayList<Integer> lsInt11 = new ArrayList<Integer>();
		for (String[] strings : lsInfo) {
			lsInt4.add((int)Double.parseDouble(strings[4]));
			lsInt5.add(Double.parseDouble(strings[5]));
			lsInt6.add(Double.parseDouble(strings[6]));
			lsInt7.add(Double.parseDouble(strings[7]));
			lsInt11.add(Integer.parseInt(strings[11].replace("bp DownStreamOfTss", "").split(":")[1].trim()));
		}
		String[] result = lsInfo.get(0);
		result[3] = lsInfo.get(lsInfo.size() - 1)[3];
		result[4] = (int)MathComput.median(lsInt4) + "";
		result[5] = MathComput.median(lsInt5) + "";
		result[6] = MathComput.median(lsInt6) + "";
		result[7] = MathComput.median(lsInt7) + "";
		
		result[11] = lsInfo.get(lsInfo.size()/2)[11].replaceFirst("\\d+", (int)MathComput.median(lsInt11)+"");
		
		return result;
	}
	
	/**
	 * 给定两个hash表，将结果整理为6个文件
	 * @param hashAccIDInfoUp
	 * @param hashAccIDInfoDown
	 */
	public static void cope2HashInfo(HashMap<String, String[]> hashAccIDInfoUp, HashMap<String, String[]> hashAccIDInfoDown,String fileName) {
		TxtReadandWrite txtDTU = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_DTU", null), true);
		TxtReadandWrite txtUTD = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_UTD", null), true);
		TxtReadandWrite txtTUD = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_TUD", null), true);
		TxtReadandWrite txtTDU = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_TDU", null), true);
		TxtReadandWrite txtUDT = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_UDT", null), true);
		TxtReadandWrite txtDUT = new TxtReadandWrite(FileOperate.changeFileSuffix(fileName, "_DUT", null), true);
	
		
		ArrayList<String> lsAccID = ArrayOperate.getArrayListKey(hashAccIDInfoUp);
		for (String string : lsAccID) {
			if (!hashAccIDInfoDown.containsKey(string)) {
				continue;
			}
			String[] tmpInfoUp = hashAccIDInfoUp.get(string);
			String[] tmpInfoDown = hashAccIDInfoDown.get(string);
			if (tmpInfoUp[11].contains("DownStreamOfTss") && !tmpInfoDown[11].contains("DownStreamOfTss")) {
				txtDTU.writefileln(combString(tmpInfoUp,tmpInfoDown));
			}
			if (!tmpInfoUp[11].contains("DownStreamOfTss") && tmpInfoDown[11].contains("DownStreamOfTss")) {
				txtUTD.writefileln(combString(tmpInfoUp,tmpInfoDown));
			}
			if (tmpInfoUp[11].contains("Promoter_Distance to TSS") && tmpInfoDown[11].contains("Promoter_Distance to TSS")) {
				if (Integer.parseInt(tmpInfoUp[11].split(":")[1].trim()) < Integer.parseInt(tmpInfoDown[11].split(":")[1].trim())) {
					txtDUT.writefileln(combString(tmpInfoUp,tmpInfoDown));
				}
				else if (Integer.parseInt(tmpInfoUp[11].split(":")[1].trim()) > Integer.parseInt(tmpInfoDown[11].split(":")[1].trim())) {
					txtUDT.writefileln(combString(tmpInfoUp,tmpInfoDown));
				}
				else {
					logger.error("find unknown condition: "  + combString(tmpInfoUp,tmpInfoDown));
				}
			}
			
			if (tmpInfoUp[11].contains("DownStreamOfTss") && tmpInfoDown[11].contains("DownStreamOfTss")) {
				int up = Integer.parseInt(tmpInfoUp[11].replace("bp DownStreamOfTss", "").split(":")[1].trim());
				int down = Integer.parseInt(tmpInfoDown[11].replace("bp DownStreamOfTss", "").split(":")[1].trim());
				if (up > down) {
					txtTDU.writefileln(combString(tmpInfoUp,tmpInfoDown));
				}
				else if (up < down) {
					txtTUD.writefileln(combString(tmpInfoUp,tmpInfoDown));
				}
				else {
					logger.error("find unknown condition: "  + combString(tmpInfoUp,tmpInfoDown));
				}
			}
		}
		 txtDTU.close();
		 txtUTD.close();
		 txtTUD.close();
		 txtTDU.close();
		 txtUDT.close();
		 txtDUT.close();
	}
	
	
	private static String combString(String[]...arg)
	{
		String result = "";
		for (String[] strings : arg) {
			for (String string : strings) {
				result = result + "\t" + string;
			}
		}
		return result.trim();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
