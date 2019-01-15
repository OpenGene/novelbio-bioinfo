package com.novelbio.bioinfo.gwas.convertformat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.fasta.SeqFasta;

/**
 * 将plinkped转化为fasta文件，注意plinkped中的位点不能太多
 * 主要给pegas使用
 * @param plinkPed
 * @param fasta
 */
public class PlinkPedExtract {
	/**
	 * 将plinkped转化为fasta文件，注意plinkped中的位点不能太多
	 * 主要给pegas使用
	 * @param plinkPed
	 * @param fasta
	 * @param 具体将哪几个标记拿出来分析--这个主要是用于haploview，从1开始计算
	 */
	public static void extractPed(String plinkPedIn, String plinkPedOut, List<Integer> lsIndex) {
		TxtReadandWrite txtRead = new TxtReadandWrite(plinkPedIn);
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkPedOut, true);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			String seqName = ss[0];
			StringBuilder seq = new StringBuilder(ss.length);
			seq.append(seqName);
			for (int i = 1; i < 6; i++) {
				seq.append("\t").append(ss[i]);
			}
			
			for (int index : lsIndex) {
				seq.append("\t").append(ss[index+5]);
			}
			txtWrite.writefileln(seq.toString());
		}
		txtRead.close();
		txtWrite.close();
	}
	
	/**
	 * 将plinkmid提取出来，注意plinkped中的位点不能太多
	 * 主要给pegas使用
	 * @param plinkPed
	 * @param fasta
	 * @param 具体将哪几个标记拿出来分析--这个主要是用于haploview，从1开始计算
	 */
	public static void extractMid(String plinkMidIn, String plinkMidOut, List<Integer> lsIndex) {
		TxtReadandWrite txtRead = new TxtReadandWrite(plinkMidIn);
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkMidOut, true);
		Set<Integer> setNumExtract = new HashSet<>(lsIndex);
		int num = 1;
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) {
				txtWrite.writefileln(content);
				continue;
			}
			if (setNumExtract.contains(num)) {
				txtWrite.writefileln(content);
			}
			num++;
		}
		txtRead.close();
		txtWrite.close();
	}
	
}
