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
 * ���е�ChrFa��ȡʱ�򣬱��뽫ÿ�еĻ��з��޶�Ϊ"\n",��С��������
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
	 *	����ת¼�������ظ�ת¼����mRNAˮƽ����
	 *@param geneID
	 * @param gffGeneIsoSearch ��ת¼������Ϣ��
	 * @param normalizeType ��MapReads.Normalize_Type
	 * @return
	 * double[] 0: atgλ��,����λ�㣬1-���� ��tss��tes��ÿ��λ���reads��Ŀ
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
	/////////////////////////////////////   �������Ŀ   //////////////////////////////////////////////////////////////////////////////////////////////////////////

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
