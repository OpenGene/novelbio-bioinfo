package com.novelbio.analysis.seq.genomeNew;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataStructure.ArrayOperate;


/**
 * ���е�ChrFa��ȡʱ�򣬱��뽫ÿ�еĻ��з��޶�Ϊ"\n",��С��������
 * @author zong0jie
 *
 */
public class GffChrHanYanChrom extends GffChrHanYan{
	

public GffChrHanYanChrom(String gffClass, String GffFile, String ChrFilePath, int taxID) {
		super(gffClass, GffFile, ChrFilePath, taxID);
		// TODO Auto-generated constructor stub
	}
/**
 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�
 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
 * @param startRegion bed�ļ��ĵ�һ��ֵ�Ƿ�Ϊ������
 * @param chrFilePath ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ��<b>�ļ����������ν�Ӳ���"/"��"\\"</b>
 * @param invNum ÿ������λ����
 * @param tagLength �趨˫��readsTagƴ�����󳤶ȵĹ���ֵ������20�Ż�������á�Ŀǰsolexa˫���������ȴ����200-400bp������̫��ȷ ,Ĭ����400
 * @param uniqReads ͬһλ����ظ��Ƿ������һ��
 * @param colUnique UniqueMapping�ı������һ��
 * @param cis5To3 �Ƿ���ѡĳһ�������reads
 */
public void loadMap(String mapFile,int startRegion,String chrFilePath,int invNum,int tagLength, boolean uniqReads,
		int startCod, int colUnique,Boolean cis5To3, boolean uniqMapping) 
{
	mapReads=new MapReadsHanyanChrom(invNum, chrFilePath, mapFile,(GffHashGeneAbs)gffHash);
	mapReads.setstartRegion(startRegion);
	try {
		if (tagLength > 20) {
			mapReads.setTagLength(tagLength);
		}
		readsNum = mapReads.ReadMapFile(uniqReads, startCod, colUnique, uniqMapping, cis5To3);
	} catch (Exception e) {	e.printStackTrace();	}
}

private static Logger logger = Logger.getLogger(GffChrHanYanChrom.class);
//////////////////////////////////////////////////�����趨/////////////////////////////////////////////////////////
	@Override
	protected double[] getReadsInfo(String geneID,
			GffGeneIsoInfo gffGeneIsoInfo, int normalizeType) {
		ArrayList<int[]> lsiso = gffGeneIsoInfo.getIsoInfo();
		if (lsiso == null || lsiso.size() == 0) {
			return null;
		}
		double[] iso = mapReads.getRengeInfo(gffGeneIsoInfo.getThisGffDetailGene().getParentName(), -1, 0, lsiso);
		mapReads.normDouble(iso, normalizeType);
		if (iso == null) {
			return null;
		}
		if (!gffGeneIsoInfo.isCis5to3()) {
			ArrayOperate.convertArray(iso);
		}
		double[] isoResult = new double[iso.length+1];
		isoResult[0] = gffGeneIsoInfo.getLocDistmRNA(gffGeneIsoInfo.getATGsite(), gffGeneIsoInfo.getTSSsite());
		for (int i = 0; i < iso.length; i++) {
			isoResult[i+1] = iso[i];
		}
		return isoResult;
	}
	
	/////////////////////////////////////   �������Ŀ   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}
