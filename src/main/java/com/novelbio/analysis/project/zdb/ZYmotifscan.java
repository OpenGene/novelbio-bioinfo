package com.novelbio.analysis.project.zdb;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.GffChrSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class ZYmotifscan {
	public static void main(String[] args) {
		scanMotif();
	}
	private static void scanMotif()
	{
		String regex = "CC[AT]{7,8}G|CC[AT]{4}[ATCG]{2}G";
		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_TIGR,  
				NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM);
		gffChrSeq.loadChrFile();
		String outTxtFile = "/media/winE/NBC/Project/Project_ZDB_Lab/YZ/tssUp2000bp_motif";
		
		int upBp = -1500;
		int downBp = 500;
//		SeqFasta seqFastaww = gffChrSeq.getPromoter("LOC_Os06g01200", upBp, downBp);
//		seqFastaww.toString();
		ArrayList<SeqFasta> lsSeqFastas = gffChrSeq.getGenomePromoterSeq(upBp, downBp);
		TxtReadandWrite txtOut = new TxtReadandWrite(outTxtFile+"_Seq", true);
		for (SeqFasta seqFasta : lsSeqFastas) {
			txtOut.writefileln(seqFasta.toStringNRfasta());
		}
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(SeqFasta.getMotifScanTitle());
		for (SeqFasta seqFasta : lsSeqFastas) {
			lsResult.addAll(seqFasta.getMotifScanResult(regex));
		}
		TxtReadandWrite txtMotifOut = new TxtReadandWrite(outTxtFile, true);
		txtMotifOut.ExcelWrite(lsResult, "\t", 1, 1);
		
	}
}

