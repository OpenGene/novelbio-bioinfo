package com.novelbio.analysis.seq.mirna;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * 过滤华大的miRNA，不是我们流水线的一部分
 * @author zong0jie
 *
 */
public class MirnaFilter {
	public static void main(String[] args) {
		MirnaFilter mirnaFilter = new MirnaFilter();
		mirnaFilter.Filter("/home/zong0jie/桌面/ZHY/clean_fas_N/clean-miRNA.fas", "/home/zong0jie/桌面/ZHY/clean_fas_N/clean-miRNA.fasResult.txt");
	}
	/**
	 * @param miRNAFile 输入的miRNA文件
	 * 过滤华大的miRNA结果
	 * 格式如下
	 * >t0000001_1  655863
	TCGCTTGGTGCAGATCGGGAC
	>t0000001_2  655863
	TCGCTTGGTGCAGATCGGGAC
	>t0000001_3  655863
	TCGCTTGGTGCAGATCGGGAC
	 */
	public void Filter(String miRNAFile, String outFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		
		TxtReadandWrite txtIn = new TxtReadandWrite(miRNAFile, false);
		String tmpMiRNA = ""; String tmpInfo = ""; boolean flag = false;//是否写入序列的标签
		for (String string : txtIn.readlines()) {
			if (string.startsWith(">") && !string.split(" ")[0].split("_")[0].substring(1).equals(tmpMiRNA)) {
				String[] ss = string.split(" ");
				tmpMiRNA = ss[0].split("_")[0].substring(1);
				
				tmpInfo = tmpMiRNA + "\t" + ss[ss.length - 1].trim();
				flag = true;
			}
			else if (flag) {
				tmpInfo = tmpInfo + "\t" + string;
				txtOut.writefileln(tmpInfo);
				flag = false;
			}
		}
		txtOut.close();
	}
	
}
