package com.novelbio.analysis.project.cr;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class MirnaSum {
	public static void main(String[] args) {
		String parent = "/media/winF/NBC/Project/RNA-Seq_CR_20111201/miRNA/";
		String BGImiRNAannoFile = "";
		BGImiRNAannoFile = parent + "Ns6dmiRNA.txt";
		addMirna(BGImiRNAannoFile, 2);
		BGImiRNAannoFile = parent + "E6dnmiRNA.txt";
		addMirna(BGImiRNAannoFile, 2);
		BGImiRNAannoFile = parent + "E6dmiRNA.txt";
		addMirna(BGImiRNAannoFile, 2);
	}
	/**
	 * 将华大的annotation的miRNA的reads进行加和
	 */
	private static void addMirna(String BGImiRNAannoFile, int readFirstLine) {
		TxtReadandWrite txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(BGImiRNAannoFile, "_nonred", null), true);
		
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(BGImiRNAannoFile, readFirstLine);
		HashMap<String, String[]> hashResult = new HashMap<String, String[]>();
		for (String[] strings : lsInfo) {
			if (hashResult.containsKey(strings[5])) {
				String[] old = hashResult.get(strings[5]);
				old[2] = Integer.parseInt(old[2]) + Integer.parseInt(strings[2]) + "";
				if (old[3].length() < strings[3].length()) {
					old[3] = strings[3];
					old[1] = strings[1];
				}
			}
			else {
				hashResult.put(strings[5], strings);
			}
		}
		for (String[] strings : hashResult.values()) {
			txtOut.writefileln(strings);
		}
		txtOut.close();
	}
}
