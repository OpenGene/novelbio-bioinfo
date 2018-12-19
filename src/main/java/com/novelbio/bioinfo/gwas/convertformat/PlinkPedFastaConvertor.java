package com.novelbio.bioinfo.gwas.convertformat;

import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fasta.SeqFastaReader;

/**
 * 把plinkped和fasta文件互相转化
 * @author novelbio
 *
 */
public class PlinkPedFastaConvertor {
	/**
	 * 将plinkped转化为fasta文件，注意plinkped中的位点不能太多
	 * 主要给pegas使用
	 * @param plinkPed
	 * @param fasta
	 * @param 具体将哪几个标记拿出来分析--这个主要是用于haploview，从1开始计算
	 */
	public static void convertPed2Fasta(String plinkPed, String fasta) {
		TxtReadandWrite txtRead = new TxtReadandWrite(plinkPed);
		TxtReadandWrite txtWrite = new TxtReadandWrite(fasta, true);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			String seqName = ss[0];
			StringBuilder seq = new StringBuilder(ss.length);
			for (int i = 6; i < ss.length; i++) {
				String base = ss[i].split(" ")[0];
				if (base.equalsIgnoreCase("N")) {
					txtRead.close();
					txtWrite.close();
					throw new RuntimeException(seqName + " contains N");
				}
				seq.append(base);
			}
			SeqFasta seqFasta = new SeqFasta(seqName, seq.toString());
			txtWrite.writefileln(seqFasta.toStringNRfasta(100000));
		}
		txtRead.close();
		txtWrite.close();
	}
	
	/**
	 * 将plinkped转化为fasta文件，注意plinkped中的位点不能太多
	 * 主要给pegas使用
	 * @param plinkPed
	 * @param fasta
	 * @param 具体将哪几个标记拿出来分析--这个主要是用于haploview，<b>从1开始计算</b>
	 */
	public static void convertPed2Fasta(String plinkPed, String fasta, List<Integer> lsIndex) {
		TxtReadandWrite txtRead = new TxtReadandWrite(plinkPed);
		TxtReadandWrite txtWrite = new TxtReadandWrite(fasta, true);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			String seqName = ss[0];
			StringBuilder seq = new StringBuilder(ss.length);
			for (int index : lsIndex) {
				String base = ss[index+5].split(" ")[0];
				if (base.equalsIgnoreCase("N")) {
					txtRead.close();
					txtWrite.close();
					throw new RuntimeException(seqName + " contains N");
				}
				seq.append(base);
			}
			SeqFasta seqFasta = new SeqFasta(seqName, seq.toString());
			txtWrite.writefileln(seqFasta.toStringNRfasta(100000));
		}
		txtRead.close();
		txtWrite.close();
	}
	
	/**
	 * 将plinkped转化为fasta文件，注意plinkped中的位点不能太多
	 * 主要给pegas使用
	 * @param plinkPed
	 * @param fasta
	 * @param 具体将哪几个标记拿出来分析--这个主要是用于haploview，<b>从1开始计算</b>
	 */
	public static void convertFasta2Ped(String fasta, String plinkPed) {
		SeqFastaReader seqFastaReader = new SeqFastaReader(fasta);
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkPed, true);
		for (SeqFasta seqFasta : seqFastaReader.readlines()) {
			String strain = seqFasta.getSeqName();
			StringBuilder sBuilder = new StringBuilder(strain);
			sBuilder.append("\t").append(strain).append("\t0\t0\t0\t-9");
			for (char c : seqFasta.toString().toCharArray()) {
				sBuilder.append("\t").append(c).append(" ").append(c);
			}
			txtWrite.writefileln(sBuilder.toString());
		}
		seqFastaReader.close();
		txtWrite.close();
	}
}
