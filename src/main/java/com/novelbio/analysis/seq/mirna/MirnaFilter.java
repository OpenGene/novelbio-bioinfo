package com.novelbio.analysis.seq.mirna;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * ���˻����miRNA������������ˮ�ߵ�һ����
 * @author zong0jie
 *
 */
public class MirnaFilter {
	public static void main(String[] args) {
		MirnaFilter mirnaFilter = new MirnaFilter();
		mirnaFilter.Filter("/home/zong0jie/����/ZHY/clean_fas_N/clean-miRNA.fas", "/home/zong0jie/����/ZHY/clean_fas_N/clean-miRNA.fasResult.txt");
	}
	/**
	 * @param miRNAFile �����miRNA�ļ�
	 * ���˻����miRNA���
	 * ��ʽ����
	 * >t0000001_1  655863
	TCGCTTGGTGCAGATCGGGAC
	>t0000001_2  655863
	TCGCTTGGTGCAGATCGGGAC
	>t0000001_3  655863
	TCGCTTGGTGCAGATCGGGAC
	 */
	public void Filter(String miRNAFile, String outFile)
	{
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		
		TxtReadandWrite txtIn = new TxtReadandWrite(miRNAFile, false);
		String tmpMiRNA = ""; String tmpInfo = ""; boolean flag = false;//�Ƿ�д�����еı�ǩ
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
