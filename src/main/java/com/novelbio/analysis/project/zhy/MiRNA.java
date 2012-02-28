package com.novelbio.analysis.project.zhy;

import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.mapping.FastQMapBwa;
import com.novelbio.analysis.seq.mapping.FastQMapSoap;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class MiRNA {
	public static void main(String[] args) {
		mapping2("/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_N/annotation_fastq.fq");
	}
	
	private void formatConvert()
	{
		String file = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_N/annotation.txt";
		cope2Fastq(file);
		file = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_2N/annotation.txt";
		cope2Fastq(file);
		file = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_3N/annotation.txt";
		cope2Fastq(file);
	}
	
	private static void cope2Fastq(String file)
	{
		String out = FileOperate.changeFileSuffix(file, "_fastq", "fq");
		TxtReadandWrite txtRead = new TxtReadandWrite(file, false);
		TxtReadandWrite txtWrite = new TxtReadandWrite(out, true);
		for (String string : txtRead.readlines()) {
			String[] ss = string.split("\t");
			String outInfo = "@"+ ss[0] + "_" + ss[2] + "_" + ss[4] + "_" + ss[5];
			outInfo = outInfo + "\r\n" + ss[3] + "\r\n+\r\n";
			char[] quality = new char[ss[3].length()];
			for (int i = 0; i < quality.length; i++) {
				quality[i] = 'f';
			}
			outInfo = outInfo + ""+ String.copyValueOf(quality) + "\r\n";
			txtWrite.writefile(outInfo, false);
		}
		txtWrite.close();
	}
	
	private static void mapping(String seqFile1)
	{
//		String seqFile1 = "";
		FastQMapSoap fastQMapSoap = new FastQMapSoap(seqFile1, FastQ.QUALITY_LOW, FileOperate.changeFileSuffix(seqFile1, "_map", "txt"), false);
		fastQMapSoap.setFilePath("", "/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/IndexSoap/TIGRrice6.1.con");
		fastQMapSoap.mapReads();
	}
	
	private static void mapping2(String seqFile1)
	{
//		String seqFile1 = "";
		FastQMapBwa fastQMapBwa = new FastQMapBwa(seqFile1, FastQ.QUALITY_LOW, FileOperate.changeFileSuffix(seqFile1, "_Bwa_map", "txt"), false);
		fastQMapBwa.setFilePath("", "/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/IndexBwa/TIGRrice6.1.con");
		fastQMapBwa.mapReads();
	}
}
