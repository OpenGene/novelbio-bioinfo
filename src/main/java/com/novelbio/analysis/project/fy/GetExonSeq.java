package com.novelbio.analysis.project.fy;

import org.broad.tribble.bed.FullBEDFeature.Exon;

import com.novelbio.analysis.seq.genomeNew.GffChrSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.generalConf.NovelBioConst;

public class GetExonSeq {
	public static void main(String[] args) {
		GetExonSeq getExonSeq = new GetExonSeq();
		getExonSeq.loading("/home/zong0jie/×ÀÃæ/FYexon/exonSeq.txt");
		getExonSeq.getExonSeq();
	}
	
	GffChrSeq gffChrSeq = null;
	TxtReadandWrite txtReadandWrite = null;
	public void loading(String outTxt) {
		gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_UCSC, null, "/media/winE/Bioinformatics/GenomeData/checken/chromFa");
		gffChrSeq.setGffFile(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, "/media/winE/NBC/Project/Project_FY_Lab/Result/cufflinkAll/cufcompare/cmpAll.combined_cope.gtf");
		gffChrSeq.loadChrFile();
//		String aaa = gffChrSeq.getSeq("TCONS_00010177", true, 22, 24, true, false);
//		System.out.println(aaa);
		txtReadandWrite = new TxtReadandWrite(outTxt, false);
		
	}
	
	String isoName = "";String tmpSeq = "";
	public void getExonSeq() {
		isoName = "TCONS_00018166";
		int ExonStart = 8;
		int ExonEnd = 10;
		tmpSeq = gffChrSeq.getSeq(isoName, true, ExonStart, ExonEnd, true, false);
		txtReadandWrite.writefileln(isoName + "  exonStart: "+ ExonStart + "  exonEnd: " + ExonEnd );
		txtReadandWrite.writefileln(tmpSeq);
		txtReadandWrite.writefileln();
//		isoName = "TCONS_00010177";
//		int ExonStart = 22;
//		int ExonEnd = 24;
//		tmpSeq = gffChrSeq.getSeq("TCONS_00010177", true, ExonStart, ExonEnd, true, false);
//		txtReadandWrite.writefileln(isoName + "  exonStart: "+ ExonStart + "exonEnd: " + ExonEnd );
//		txtReadandWrite.writefileln(tmpSeq);
//		
//		
//		isoName = "TCONS_00010177";
//		int ExonStart = 22;
//		int ExonEnd = 24;
//		tmpSeq = gffChrSeq.getSeq("TCONS_00010177", true, ExonStart, ExonEnd, true, false);
//		txtReadandWrite.writefileln(isoName + "  exonStart: "+ ExonStart + "exonEnd: " + ExonEnd );
//		txtReadandWrite.writefileln(tmpSeq);
//		
//		
//		isoName = "TCONS_00010177";
//		int ExonStart = 22;
//		int ExonEnd = 24;
//		tmpSeq = gffChrSeq.getSeq("TCONS_00010177", true, ExonStart, ExonEnd, true, false);
//		txtReadandWrite.writefileln(isoName + "  exonStart: "+ ExonStart + "exonEnd: " + ExonEnd );
//		txtReadandWrite.writefileln(tmpSeq);
	}
}
