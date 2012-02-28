package com.novelbio.analysis.project.zdb;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.GffChrSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.generalConf.NovelBioConst;

public class ZYmotifscan {
	public static void main(String[] args) {
		 scanMotifPerGene();
	}
	private static void scanMotif()
	{
		String regex = "CC[AT]{7,8}G|CC[AT]{4}[ATCG]{2}G";
		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_TIGR,  
				NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM);
		gffChrSeq.loadChrFile();
		String outTxtFile = "/media/winE/NBC/Project/Project_ZDB_Lab/YZ/tssUp1500bpDown500bp_motif";
		
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
			lsResult.addAll(seqFasta.getMotifScanResult(regex, -500));
		}
		TxtReadandWrite txtMotifOut = new TxtReadandWrite(outTxtFile, true);
		txtMotifOut.ExcelWrite(lsResult, "\t", 1, 1);
		
	}
	
	private static void scanMotifPerGene()
	{
		String geneFile = "/media/winE/NBC/Project/Project_ZDB_Lab/TJH/motif/tjhGeneID.txt";
		ArrayList<String[]> lsGeneID = ExcelTxtRead.readLsExcelTxt(geneFile, 1);

		
		GffChrSeq gffChrSeq = new GffChrSeq(NovelBioConst.GENOME_GFF_TYPE_TIGR,  
				NovelBioConst.GENOME_PATH_RICE_TIGR_GFF_GENE, NovelBioConst.GENOME_PATH_RICE_TIGR_CHROM);
		gffChrSeq.loadChrFile();
		
		
		int upBp = -1500;
		int downBp = 500;
		ArrayList<SeqFasta> lsSeqFastas = new ArrayList<SeqFasta>();
		for (String[] strings : lsGeneID) {
			lsSeqFastas.add(gffChrSeq.getPromoter(strings[0], upBp, downBp));
		}
		String outTxtSeq = "/media/winE/NBC/Project/Project_ZDB_Lab/TJH/motif/tssUp1500bpDown500bp_motif";
		TxtReadandWrite txtOut = new TxtReadandWrite(outTxtSeq, true);
		for (SeqFasta seqFasta : lsSeqFastas) {
			txtOut.writefileln(seqFasta.toStringNRfasta());
		}
		
		String regex = "C[AT]{6}G";
		String outTxtFile = "/media/winE/NBC/Project/Project_ZDB_Lab/TJH/motif/tssUp1500bpDown500bp_motif_C[AT]6G.txt";
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(SeqFasta.getMotifScanTitle());
		for (SeqFasta seqFasta : lsSeqFastas) {
			lsResult.addAll(seqFasta.getMotifScanResult(regex, -500));
		}
		TxtReadandWrite txtMotifOut = new TxtReadandWrite(outTxtFile, true);
		txtMotifOut.ExcelWrite(lsResult, "\t", 1, 1);
		
		
		regex = "CC[AT]{7,8}G";
		outTxtFile = "/media/winE/NBC/Project/Project_ZDB_Lab/TJH/motif/tssUp1500bpDown500bp_motif_CC[AT]78G.txt";		
		lsResult = new ArrayList<String[]>();
		lsResult.add(SeqFasta.getMotifScanTitle());
		for (SeqFasta seqFasta : lsSeqFastas) {
			lsResult.addAll(seqFasta.getMotifScanResult(regex, -500));
		}
		txtMotifOut = new TxtReadandWrite(outTxtFile, true);
		txtMotifOut.ExcelWrite(lsResult, "\t", 1, 1);
		
		regex = "CC(AT){4}(ATCG){2}GG";
		outTxtFile = "/media/winE/NBC/Project/Project_ZDB_Lab/TJH/motif/tssUp1500bpDown500bp_motif_CC[AT]4[ATCG]2GG.txt";
		lsResult = new ArrayList<String[]>();
		lsResult.add(SeqFasta.getMotifScanTitle());
		for (SeqFasta seqFasta : lsSeqFastas) {
			lsResult.addAll(seqFasta.getMotifScanResult(regex, -500));
		}
		txtMotifOut = new TxtReadandWrite(outTxtFile, true);
		txtMotifOut.ExcelWrite(lsResult, "\t", 1, 1);

	}
}

