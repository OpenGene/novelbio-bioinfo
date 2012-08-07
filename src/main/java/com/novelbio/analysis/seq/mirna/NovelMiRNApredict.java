package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashSet;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public abstract class NovelMiRNApredict {
	/** ���Ҷ�λ�ڷ���exon��intron�ϵ����� */
	GffChrAbs gffChrAbs = null;
	/** �����һ��bedseq�ļ� */
	BedSeq bedSeqInput = null;
	
	public abstract void setOutPath(String outPath);
	
	/**
	 * @param gffChrAbs �趨gff����
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * ��setBedSeq(String bedFile) ��ѡһ
	 * ����������õ���mapping �� genome�ϵ�bed�ļ��ϲ�������Ϊ����mireap���ļ�
	 * @param outFile �ϲ���bed�ļ���
	 * @param bedSeqFile
	 */
	public void setBedSeq(String outFile, String... bedSeqFile) {
		BedSeq bedSeq = BedSeq.combBedFile(outFile, bedSeqFile);
		setBedSeqInput(bedSeq.getFileName());
	}
	/**
	 * ��setBedSeq(String bedFile) ��ѡһ
	 * ����������õ���mapping �� genome�ϵ�bed�ļ��ϲ�������Ϊ����mireap���ļ�
	 * @param outFile ��úϲ���bed�ļ���
	 * @param lsBedSeqFile һϵ�е�bed�ļ�
	 */
	public void setBedSeqInput(String outFile, ArrayList<String> lsBedSeqFile) {
		BedSeq bedSeq = BedSeq.combBedFile(outFile, lsBedSeqFile);
		setBedSeqInput(bedSeq.getFileName());
	}
	/**
	 * ��setBedSeq(String outFile, String... bedSeqFile) ��ѡһ
	 * �����õ���bed�ļ�
	 * @param bedFile
	 */
	public void setBedSeqInput(String bedFile) {
		bedSeqInput = new BedSeq(bedFile);
	}
	/**
	 * ����bed�ļ������reads���ڻ����ϵ�����
	 * @param outBed reads���ڻ������ϵ�����
	 */
	protected BedSeq getBedReadsNotOnCDS(String outBed) {
		BedSeq bedResult = new BedSeq(outBed, true);
		if (gffChrAbs == null || gffChrAbs.getGffHashGene() == null) {
			return new BedSeq(bedSeqInput.getFileName());
		}
		for (BedRecord bedRecord : bedSeqInput.readlines()) {
			GffCodGene gffCod = gffChrAbs.getGffHashGene().searchLocation(bedRecord.getRefID(), bedRecord.getMidLoc());
			if (readsNotOnCDS(gffCod, bedRecord.isCis5to3()))
				bedResult.writeBedRecord(bedRecord);
		}
		bedResult.closeWrite();
		return bedResult;
	}
	/**
	 * �ж������reads�Ƿ�λ��intron��gene�����exon��
	 * @param gffCodGene
	 * @param bedCis
	 * @return
	 */
	private boolean readsNotOnCDS(GffCodGene gffCodGene, boolean bedCis) {
		if (gffCodGene == null) {
			return true;
		}
		if (!gffCodGene.isInsideLoc()) {
			return true;
		}
		GffDetailGene gffDetailGene = gffCodGene.getGffDetailThis();
		int locInfo = 0;
		try {
			locInfo = gffDetailGene.getLongestSplit().getCodLoc(gffCodGene.getCoord());
		} catch (Exception e) {
			locInfo = gffDetailGene.getLongestSplit().getCodLoc(gffCodGene.getCoord());
		}
		if (locInfo == GffGeneIsoInfo.COD_LOC_INTRON 
				|| locInfo == GffGeneIsoInfo.COD_LOC_OUT
				|| bedCis != gffDetailGene.getLongestSplit().isCis5to3()
				) {
			return true;
		}
		return false;
	}
}
