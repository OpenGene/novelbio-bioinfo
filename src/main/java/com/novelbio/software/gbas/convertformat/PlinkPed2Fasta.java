package com.novelbio.software.gbas.convertformat;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.fasta.SeqFasta;

/**
 * 将plinkped转化为fasta文件，注意plinkped中的位点不能太多
 * 主要给pegas使用
 * @param plinkPed
 * @param fasta
 */
public class PlinkPed2Fasta {
	/**
	 * 将plinkped转化为fasta文件，注意plinkped中的位点不能太多
	 * 主要给pegas使用
	 * @param plinkPed
	 * @param fasta
	 */
	public static void convert(String plinkPed, String fasta) {
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
			txtWrite.writefile(seqFasta.toStringNRfasta(100000));
		}
		txtRead.close();
		txtWrite.close();
	}
}
