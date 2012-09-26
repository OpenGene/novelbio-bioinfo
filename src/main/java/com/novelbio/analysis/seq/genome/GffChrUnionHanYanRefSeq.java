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
 * ���е�ChrFa��ȡʱ�򣬱��뽫ÿ�еĻ��з��޶�Ϊ"\n",��С��������
 * 
 * @author zong0jie
 * 
 */
public class GffChrUnionHanYanRefSeq extends GffChrHanYan{
	private static Logger logger = Logger.getLogger(GffChrUnionHanYanRefSeq.class);

	/**
	 *	����ת¼�������ظ�ת¼����mRNAˮƽ����
	 *@param geneID
	 * @param gffGeneIsoSearch ��ת¼������Ϣ��
	 * @param normalizeType ��MapReads.Normalize_Type
	 * @return
	 * double[] 0: atgλ��,����λ�㣬1-���� ��tss��tes��ÿ��λ���reads��Ŀ
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
	/////////////////////////////////////   �������Ŀ   //////////////////////////////////////////////////////////////////////////////////////////////////////////

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
