package com.novelbio.analysis.seq.genome;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 其中的ChrFa读取时候，必须将每行的换行符限定为"\n",有小工具能用
 * 
 * @author zong0jie
 * 
 */
public class GffChrUnionHanYanRefSeq extends GffChrHanYan{
	private static Logger logger = Logger.getLogger(GffChrUnionHanYanRefSeq.class);
	
	SeqFastaHash seqFastaHash;
	
	public void setRefSeq(String seqfasta) {
		seqFastaHash = new SeqFastaHash(seqfasta);
	}
	/**
	 *	给定转录本，返回该转录本的mRNA水平坐标
	 *@param geneID
	 * @param gffGeneIsoSearch 该转录本的信息类
	 * @param normalizeType 看MapReads.Normalize_Type
	 * @return
	 * double[] 0: atg位点,绝对位点，1-结束 从tss到tes的每个位点的reads数目
	 */
	protected double[] getReadsInfo(String geneID, GffGeneIsoInfo gffGeneIsoSearch) {		
		double[] iso = mapReads.getRangeInfo(1, geneID.toLowerCase(), 0, 0, 0);
		if (iso == null) {
			return null;
		}
		double[] isoResult = new double[iso.length+1];
		isoResult[0] = gffGeneIsoSearch.getLocDistmRNA(gffGeneIsoSearch.getTSSsite(), gffGeneIsoSearch.getATGsite());
		for (int i = 0; i < iso.length; i++) {
			isoResult[i+1] = iso[i];
		}
		return isoResult;
	}
	/////////////////////////////////////   韩燕的项目   //////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void loadMap(String mapFile, int tagLength, boolean uniqReads, int startCod, Boolean cis5To3, boolean uniqMapping) {
		mapReads = new MapReads();
		mapReads.setMapChrID2Len(seqFastaHash.getMapChrLength());
		mapReads.setBedSeq(mapFile);
		mapReads.setInvNum(1);
		mapReads.setNormalType(MapReads.NORMALIZATION_PER_GENE);
		mapReads.setFilter(uniqReads, startCod, uniqMapping, cis5To3);
		if (tagLength > 20) {
			mapReads.setTagLength(tagLength);
		}
		mapReads.run();
	}
	@Override
	protected ArrayList<String> getAllGeneName() {
		return mapReads.getChrIDLs();
	}

}
