package com.novelbio.analysis.seq.mirna;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 新的miRNA的预测
 * @author zong0jie
 *
 */
public class NovelMiRNA {
	GffHashGene gffHashGene = new GffHashGene();
	BedSeq bedSeq = null;
	public void setBedSeq(String bedFile) {
		bedSeq = new BedSeq(bedFile);
	}
	/**
	 * 读取repeat文件
	 * @param repeatGffFile
	 */
	public void readGff(String geneGffUCSC) {
		gffHashGene.setParam(NovelBioConst.GENOME_GFF_TYPE_UCSC);
		gffHashGene.readGffFile(geneGffUCSC);
	}
	/**
	 * 获得reads不在基因上的序列
	 */
	private BedSeq getBedReadsNotOnCDS(String outBed) {
		BedSeq bedResult = new BedSeq(outBed, true);
		for (BedRecord bedRecord : bedSeq.readlines()) {
			GffCodGene gffCod = gffHashGene.searchLocation(bedRecord.getRefID(), bedRecord.getMiddle());
			readsNotOnCDS(gffCod, bedRecord.isCis());
		}
		
		
		
		bedResult.closeWrite();
		return bedResult;
	}
	
	private boolean readsNotOnCDS(GffCodGene gffCodGene, boolean bedCis) {
		if (gffCodGene == null) {
			return true;
		}
		if (!gffCodGene.isInsideLoc()) {
			return true;
		}
		GffDetailGene gffDetailGene = gffCodGene.getGffDetailThis();
		int locInfo = gffDetailGene.getLongestSplit().getCodLoc(gffCodGene.getCoord());
		if (locInfo == GffGeneIsoInfo.COD_LOC_INTRON 
				|| locInfo == GffGeneIsoInfo.COD_LOC_OUT
				|| bedCis != gffDetailGene.getLongestSplit().isCis5to3()
				) {
			return true;
		}
		return false;
	}
}
