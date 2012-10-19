package com.novelbio.analysis.tools.ncbisubmit;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 将gff文件整理成为Sequin识别的格式
 * 如下：
 * 1830	2966	gene
			gene	dnaN
			locus_tag     OBB_0002
1830	2966	CDS
			product	DNA-directed DNA polymerase III beta chain
			EC_number	2.7.7.7
			protein_id	gnl|ncbi|OBB_0002
3219	3440	gene
			locus_tag     OBB_0003
3219	3440	CDS
			product	hypothetical protein
			protein_id	gnl|ncbi|OBB_0003
3443	4552	gene
			gene	recF
			locus_tag     OBB_0004
3443	4552	CDS
			product	RecF
			function	DNA repair and genetic recombination
			protein_id	gnl|ncbi|OBB_0004

 * @author zong0jie
 *
 */
public class GenerateSequinGeneInfo {
	String gffBactriumFile;
	String outFile;
	
	public void setGffBactriumFile(String gffBactriumFile) {
		this.gffBactriumFile = gffBactriumFile;
		if (outFile == null) {
			outFile = FileOperate.changeFileSuffix(gffBactriumFile, "_Sequin", null);
		}
	}
	public void setOutFile(String outFile) {
		this.outFile = outFile;
	}
	
	private void copeFile() {
		TxtReadandWrite txtGffFile = new TxtReadandWrite(gffBactriumFile, false);
		for (String content : txtGffFile.readlines()) {
			String[] ss = content.split("\t");
			
		}
	}
}

class SequinGene {
	int start;
	int end;
	/** gene名 */
	String geneName;
	/** locus名 */
	String locus_tag;
	/** CDS，rrna等等 */
	String type;
	
	String product;
	String note;
	
	public void setStartEnd(int start, int end, boolean cis5to3) {
		if (cis5to3) {
			this.start = Math.min(start, end);
			this.end = Math.max(start, end);
		}
		else {
			this.end = Math.min(start, end);
			this.start = Math.max(start, end);
		}
	}
}
