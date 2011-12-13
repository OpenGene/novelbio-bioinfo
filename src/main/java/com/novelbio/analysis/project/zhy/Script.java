package com.novelbio.analysis.project.zhy;

import java.util.HashMap;
import java.util.Set;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class Script {
	public static void main(String[] args) {
		String filepath = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_N/annotation.txt";
		String filepath2 = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_N/miRNAseq.txt";
		TxtReadandWrite txt = new TxtReadandWrite(filepath, false);
		TxtReadandWrite txtOut = new TxtReadandWrite(filepath2, true);
		HashMap<String, String> hashSeq = new HashMap<String, String>();
		for (String string : txt.readlines()) {
			String[] ss = string.split("\t");
			if (!ss[4].equals("miRNA")) {
				continue;
			}
			if (hashSeq.containsKey(ss[5])) {
				if (ss[3].length() > hashSeq.get(ss[5]).length()) {
					hashSeq.put(ss[5], ss[3]);
				}
			}
			else {
				hashSeq.put(ss[5], ss[3]);
			}
		}
		for (String string : hashSeq.keySet()) {
			String tmpResult = string + "\t" + hashSeq.get(string);
			txtOut.writefileln(tmpResult);
		}
		txtOut.close();
	}
}
