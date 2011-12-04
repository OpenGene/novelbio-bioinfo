package com.novelbio.analysis.project.zhy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * 将华大的miRNA结果整理出表格
 * @author zong0jie
 *
 */
public class MiRNAcount {
	
	public static void main(String[] args) {
		MiRNAcount miRNAcount = new MiRNAcount();
		miRNAcount.readFile("/home/zong0jie/桌面/ZHY/result_advance_3N/annotation.txt", "/home/zong0jie/桌面/ZHY/result_advance_3N/RNANum_3N.txt", 
				"/home/zong0jie/桌面/ZHY/result_advance_3N/miRNANum_3N.txt");
	}
	
	
	
	/**
	 * 读取result_advance//result_advance
	 * @param txtMiRNA
	 */
	private void readFile(String txtMiRNA, String outFileRNA, String outFileMiRNA) {
		/**
		 * snRNA，miRNA，snoRNA 数量
		 */
		HashMap<String, Integer> hashRNAnum = new LinkedHashMap<String, Integer>();
		/**
		 * miRNA的名字和数量
		 */
		HashMap<String, Integer> hashMiRNAnum = new LinkedHashMap<String, Integer>();
		
		TxtReadandWrite txtReads = new TxtReadandWrite(txtMiRNA, false);
		for (String string : txtReads.readlines()) {
			String[] ss = string.split("\t");
			if (ss[4].equals("miRNA")) {
				if (!hashRNAnum.containsKey("miRNA")) {
					hashRNAnum.put("miRNA", Integer.parseInt(ss[2]));
				}
				else {
					Integer num = hashRNAnum.get("miRNA");
					hashRNAnum.put("miRNA", num + Integer.parseInt(ss[2]));
				}
				
				if (!hashMiRNAnum.containsKey(ss[5])) {
					hashMiRNAnum.put(ss[5], Integer.parseInt(ss[2]));
				}
				else {
					Integer num = hashMiRNAnum.get(ss[5]);
					hashMiRNAnum.put(ss[5], num + Integer.parseInt(ss[2]));
				}
			}
			else if (ss[5].equals("snoRNA")) {
				if (!hashRNAnum.containsKey("snoRNA")) {
					hashRNAnum.put("snoRNA", Integer.parseInt(ss[2]));
				}
				else {
					Integer num = hashRNAnum.get("snoRNA");
					hashRNAnum.put("snoRNA", num + Integer.parseInt(ss[2]));
				}
			}
			else if (ss[5].equals("snRNA")) {
				if (!hashRNAnum.containsKey("snRNA")) {
					hashRNAnum.put("snRNA", Integer.parseInt(ss[2]));
				}
				else {
					Integer num = hashRNAnum.get("snRNA");
					hashRNAnum.put("snRNA", num + Integer.parseInt(ss[2]));
				}
			}
		}
		TxtReadandWrite txtOutRNA = new TxtReadandWrite(outFileRNA, true);
		TxtReadandWrite txtOutMiRNA = new TxtReadandWrite(outFileMiRNA, true);
		for (Entry<String, Integer> entry : hashRNAnum.entrySet()) {
			txtOutRNA.writefileln(entry.getKey() + "\t" + entry.getValue());
		}
		for (Entry<String, Integer> entry : hashMiRNAnum.entrySet()) {
			txtOutMiRNA.writefileln(entry.getKey() + "\t" + entry.getValue());
		}
		txtOutMiRNA.close();
		txtOutRNA.close();
	}
	
	
	
}
