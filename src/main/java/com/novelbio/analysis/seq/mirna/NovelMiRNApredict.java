package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public abstract class NovelMiRNApredict {
	/** 查找定位在反向exon和intron上的序列 */
	GffChrAbs gffChrAbs = null;
	/** 输入的bedseq文件 */
	Collection<AlignSeq> lsAlignSeqFile = new ArrayList<AlignSeq>();
	
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
	 * @param outFile 获得合并的bed文件名
	 * @param lsBedSeqFile 一系列的bed文件
	 */
	public void setSeqInput(Collection<? extends AlignSeq> lsAlignSeqFile) {
		this.lsAlignSeqFile = new ArrayList<AlignSeq>(lsAlignSeqFile);
	}
	/**
	 * 与setBedSeq(String outFile, String... bedSeqFile) 二选一
	 * 样本得到的bed文件
	 * @param bedFile
	 */
	public void setSeqInput(AlignSeq alignSeq) {
		lsAlignSeqFile.clear();
		lsAlignSeqFile.add(alignSeq);
	}
	/**
	 * 遍历输入的文件，获得reads不在基因上的序列
	 * @param outFileName reads不在基因上的序列的文件名
	 */
	protected BedSeq getReadsNotOnCDS(String outFileName) {
		boolean search = true;
		if (gffChrAbs == null || gffChrAbs.getGffHashGene() == null) {
			search = false;
		}
		
		BedSeq bedSeq = new BedSeq(outFileName, true);
		for (AlignSeq alignSeq : lsAlignSeqFile) {
			for (AlignRecord alignRecord : alignSeq.readLines()) {
				if (!alignRecord.isMapped()) {
					continue;
				}
				
				if (search) {
					GffCodGene gffCod = gffChrAbs.getGffHashGene().searchLocation(alignRecord.getRefID(), (alignRecord.getStartAbs() + alignRecord.getEndAbs())/2);
					if (!readsNotOnCDS(gffCod, alignRecord.isCis5to3())) {
						continue;
					}
				}
				bedSeq.writeBedRecord(new BedRecord(alignRecord));
			}
		}
		bedSeq.closeWrite();
		return bedSeq;
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
			locInfo = gffDetailGene.getLongestSplitMrna().getCodLoc(gffCodGene.getCoord());
		} catch (Exception e) {
			locInfo = gffDetailGene.getLongestSplitMrna().getCodLoc(gffCodGene.getCoord());
		}
		if (locInfo == GffGeneIsoInfo.COD_LOC_INTRON 
				|| locInfo == GffGeneIsoInfo.COD_LOC_OUT
				|| bedCis != gffDetailGene.getLongestSplitMrna().isCis5to3()
				) {
			return true;
		}
		return false;
	}
}
