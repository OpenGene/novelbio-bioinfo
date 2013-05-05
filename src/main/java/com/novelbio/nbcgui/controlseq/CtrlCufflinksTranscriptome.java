package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffType;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.rnaseq.CufflinksGTF;
import com.novelbio.analysis.seq.rnaseq.GffHashMerge;
import com.novelbio.analysis.seq.rnaseq.TranscriptomStatistics;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class CtrlCufflinksTranscriptome {
	public static void main(String[] args) {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.cufflinks);
		System.out.println(softWareInfo.getExePath());
	}
	boolean reconstructTranscriptome = false;
	CufflinksGTF cufflinksGTF = new CufflinksGTF();
	GffHashMerge gffHashMerge = new GffHashMerge();
	GffChrAbs gffChrAbs;
	String outPrefix;
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
		cufflinksGTF.setGffChrAbs(gffChrAbs);
	}
	public void setLsBamFile2Prefix(ArrayList<String[]> lsBamFile2Prefix) {
		cufflinksGTF.setBam(lsBamFile2Prefix);
	}
	public void setThreadNum(int threadNum) {
		cufflinksGTF.setThreadNum(threadNum);
	}
	public void setStrandSpecifictype(StrandSpecific strandSpecific) {
		cufflinksGTF.setStrandSpecifictype(strandSpecific);
	}
	/** 是否重建转录本 */
	public void setReconstructTranscriptome(boolean reconstructTranscriptome) {
		this.reconstructTranscriptome = reconstructTranscriptome;
	}
	private void setExepath() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.cufflinks);
		cufflinksGTF.setExePath(softWareInfo.getExePath(), gffChrAbs.getSpecies().getChromSeq());
	}
	
	public void setOutPathPrefix(String outPathPrefix) {
		cufflinksGTF.setOutPathPrefix(outPathPrefix);
		this.outPrefix = outPathPrefix;
	}
	public void run() {
		setExepath();
		String outGtf = outPrefix + "novelTranscriptom.gff";
		String outStatistics =  outPrefix + "novelTranscriptomStatistics.txt";
		
		cufflinksGTF.runCufflinks();
		if (!reconstructTranscriptome) {
			return;
		}
		
		//TODO 重建转录本需要重做
		
		
//		String cufGTF = cufflinksGTF.getCufflinksGTFPath();
//		gffHashMerge.setSpecies(gffChrAbs.getSpecies());
//		gffHashMerge.setGffHashGeneRef(gffChrAbs.getGffHashGene());
//		gffHashMerge.addGffHashGene(new GffHashGene(GffType.GTF, cufGTF));
//		GffHashGene gffHashGene = gffHashMerge.getGffHashGeneModifyResult();
//		gffHashGene.removeDuplicateIso();
//		gffHashGene.writeToGTF(outGtf, "novelbio");
//
//		
//		gffHashMerge = new GffHashMerge();
//		gffHashMerge.setSpecies(gffChrAbs.getSpecies());
//		gffHashMerge.setGffHashGeneRef(gffChrAbs.getGffHashGene());
//		gffHashMerge.addGffHashGene(new GffHashGene(GffType.GTF, outGtf));
//
//		TranscriptomStatistics transcriptomStatistics = gffHashMerge.getStatisticsCompareGff();
//		TxtReadandWrite txtOut = new TxtReadandWrite(outStatistics, true);
//
//		txtOut.ExcelWrite(transcriptomStatistics.getStatisticsResult());
	}
}
