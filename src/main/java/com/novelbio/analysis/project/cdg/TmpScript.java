package com.novelbio.analysis.project.cdg;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class TmpScript {
	public static void main(String[] args) {
		getGenePrimeLoc();
	
	
	}
	
	
	
	private static void convert2FastQ()
	{
		String seqFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/QPCR/Qpcr_primer.txt";
		String fastQfile = FileOperate.changeFileSuffix(seqFile, "_fastq", "fq");
		TxtReadandWrite txtRead = new TxtReadandWrite(seqFile, false);
		TxtReadandWrite txtWrite = new TxtReadandWrite(fastQfile, true);
		for (String string : txtRead.readlines()) {
			String tmpResult = "@"+string + "\r\n" + string + "\r\n+\r\n";
			tmpResult = tmpResult + getQuality(string.length());
			txtWrite.writefileln(tmpResult);
		}
		txtRead.close();
		txtWrite.close();
	}
	
	
	private static String getQuality(int length)
	{
		char[] chr = new char[length];
		for (int i = 0; i < chr.length; i++) {
			chr[i] = 'f';
		}
		return String.copyValueOf(chr);
	}
	
	
	private static void getGenePrimeLoc()
	{
		String seqFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/QPCR/Qpcr_primer_gene.txt";
		String bedFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/QPCR/Qpcr_primer_fastq_BwaMapping_fromSam.bed";
		String out = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/QPCR/primer_loc";
		TxtReadandWrite txtOut = new TxtReadandWrite(out, true);
		ArrayList<String[]> lsSeq = ExcelTxtRead.readLsExcelTxt(seqFile, 1);
		ArrayList<String[]> lsBed = ExcelTxtRead.readLsExcelTxt(bedFile, 1);
		HashMap<String, String> hashSeq2Gene = new HashMap<String, String>();
		for (String[] strings : lsSeq) {
			hashSeq2Gene.put(strings[1], strings[0].substring(0, strings[0].length() - 2));
		}
		String tmpID = ""; String[] tmpResult = new String[4];
		for (String[] strings : lsBed) {
			String geneID = hashSeq2Gene.get(strings[7]);
			if (!geneID.equals(tmpID)) {
				txtOut.writefileln(tmpResult);
				tmpResult = new String[4];
				tmpID = geneID;
			}
			if (strings[5].equals("+")) {
				tmpResult[0] = geneID;
				tmpResult[1] = strings[0];
				tmpResult[2] = strings[1];
			}
			if (strings[5].equals("-")) {
				tmpResult[0] = geneID;
				tmpResult[1] = strings[0];
				tmpResult[3] = strings[2];
			}
		}
		txtOut.close();
	}
	
	
	
}
