package com.novelbio.nbcgui.controlseq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.rnaseq.CufflinksGTF;
import com.novelbio.analysis.seq.rnaseq.GffHashMerge;
import com.novelbio.analysis.seq.rnaseq.TranscriptomStatistics;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.generalConf.NovelBioConst;

public class CtrlCufflinksTranscriptome {
	boolean reconstructTranscriptome = false;
	CufflinksGTF cufflinksGTF = new CufflinksGTF();
	GffHashMerge gffHashMerge = new GffHashMerge();
	GffChrAbs gffChrAbs;
	String outPrefix;
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
		cufflinksGTF.setGffChrAbs(gffChrAbs);
	}
	public void setBamFile(ArrayList<String> lsBamFile) {
		cufflinksGTF.setBam(lsBamFile);
	}
	public void setThreadNum(int threadNum) {
		cufflinksGTF.setThreadNum(threadNum);
	}
	public void setStrandSpecifictype(StrandSpecific strandSpecific) {
		cufflinksGTF.setStrandSpecifictype(strandSpecific);
	}
	/** �Ƿ��ؽ�ת¼�� */
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
		String cufGTF = cufflinksGTF.getCufflinksGTFPath();
		gffHashMerge.setSpecies(gffChrAbs.getSpecies());
		gffHashMerge.setGffHashGeneRef(gffChrAbs.getGffHashGene());
		gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, cufGTF));
		GffHashGene gffHashGene = gffHashMerge.getGffHashGeneModifyResult();
		gffHashGene.removeDuplicateIso();
		gffHashGene.writeToGTF(outGtf, "novelbio");

		
		gffHashMerge = new GffHashMerge();
		gffHashMerge.setSpecies(gffChrAbs.getSpecies());
		gffHashMerge.setGffHashGeneRef(gffChrAbs.getGffHashGene());
		gffHashMerge.addGffHashGene(new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, outGtf));

		TranscriptomStatistics transcriptomStatistics = gffHashMerge.getStatisticsCompareGff();
		TxtReadandWrite txtOut = new TxtReadandWrite(outStatistics, true);

		txtOut.ExcelWrite(transcriptomStatistics.getStatisticsResult());
	}
}