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
	/** 查找定位在反向exon和intron上的序列 */
	GffChrAbs gffChrAbs = null;
	/** 输入的一个bedseq文件 */
	BedSeq bedSeqInput = null;
	
	public abstract void setOutPath(String outPath);
	
	/**
	 * @param gffChrAbs 设定gff即可
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 与setBedSeq(String bedFile) 二选一
	 * 将多个样本得到的mapping 至 genome上的bed文件合并，并作为输入mireap的文件
	 * @param outFile 合并的bed文件名
	 * @param bedSeqFile
	 */
	public void setBedSeq(String outFile, String... bedSeqFile) {
		BedSeq bedSeq = BedSeq.combBedFile(outFile, bedSeqFile);
		setBedSeqInput(bedSeq.getFileName());
	}
	/**
	 * 与setBedSeq(String bedFile) 二选一
	 * 将多个样本得到的mapping 至 genome上的bed文件合并，并作为输入mireap的文件
	 * @param outFile 获得合并的bed文件名
	 * @param lsBedSeqFile 一系列的bed文件
	 */
	public void setBedSeqInput(String outFile, ArrayList<String> lsBedSeqFile) {
		BedSeq bedSeq = BedSeq.combBedFile(outFile, lsBedSeqFile);
		setBedSeqInput(bedSeq.getFileName());
	}
	/**
	 * 与setBedSeq(String outFile, String... bedSeqFile) 二选一
	 * 样本得到的bed文件
	 * @param bedFile
	 */
	public void setBedSeqInput(String bedFile) {
		bedSeqInput = new BedSeq(bedFile);
	}
	/**
	 * 遍历bed文件，获得reads不在基因上的序列
	 * @param outBed reads不在基因组上的序列
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
	 * 判定输入的reads是否位于intron或gene外或反向exon上
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
