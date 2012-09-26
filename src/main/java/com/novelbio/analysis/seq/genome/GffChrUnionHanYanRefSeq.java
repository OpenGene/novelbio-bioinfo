package com.novelbio.analysis.seq.genome;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
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

	/**
	 *	给定转录本，返回该转录本的mRNA水平坐标
	 *@param geneID
	 * @param gffGeneIsoSearch 该转录本的信息类
	 * @param normalizeType 看MapReads.Normalize_Type
	 * @return
	 * double[] 0: atg位点,绝对位点，1-结束 从tss到tes的每个位点的reads数目
	 */
	protected double[] getReadsInfo(String geneID, GffGeneIsoInfo gffGeneIsoSearch) {
		int geneLength = 0;
		try {
			geneLength = seqFastaHash.getHashChrLength().get(geneID.toLowerCase()).intValue();
		} catch (Exception e) {
			return null;
		}
		
		double[] iso = mapReads.getRengeInfo(1, geneID.toLowerCase(), 1, geneLength, 0);
		if (iso == null) {
			return null;
		}
		mapReads.normDouble(iso);
		double[] isoResult = new double[iso.length+1];
		isoResult[0] = gffGeneIsoSearch.getLocDistmRNA(gffGeneIsoSearch.getATGsite(), gffGeneIsoSearch.getTSSsite());
		for (int i = 0; i < iso.length; i++) {
			isoResult[i+1] = iso[i];
		}
		return isoResult;
	}
	/////////////////////////////////////   韩燕的项目   //////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void loadMap(String mapFile, int invNum, int tagLength, boolean uniqReads, int startCod, Boolean cis5To3, boolean uniqMapping) {
		mapReads = new MapReads(invNum, chrFilePath, mapFile, "");
		mapReads = new MapReads();
		
		mapReads.setstartRegion(startRegion);
		try {
			if (tagLength > 20) {
				mapReads.setTagLength(tagLength);
			}
			readsNum = mapReads.ReadMapFile(uniqReads, startCod, colUnique, uniqMapping, cis5To3);
		} catch (Exception e) {	e.printStackTrace();	}
	}

}
