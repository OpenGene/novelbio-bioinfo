package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataStructure.ArrayOperate;


/**
 * ���е�ChrFa��ȡʱ�򣬱��뽫ÿ�еĻ��з��޶�Ϊ"\n",��С��������
 * @author zong0jie
 *
 */
public class GffChrHanYanChrom extends GffChrHanYan{
	public static void main(String[] args) {
		GffChrAbs gffChrAbs = new GffChrAbs(9606);
		GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO("NM_006036");
		System.out.println(gffGeneIsoInfo.getATGsite());
		System.out.println(gffGeneIsoInfo.getUAGsite());
		for (ExonInfo exonInfo : gffGeneIsoInfo) {
			System.out.println(exonInfo.getStartAbs() + "\t" + exonInfo.getEndAbs());
		}
	}
	private static Logger logger = Logger.getLogger(GffChrHanYanChrom.class);
	
	/**
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�
	 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 * @param invNum ÿ������λ����
	 * @param tagLength �趨˫��readsTagƴ�����󳤶ȵĹ���ֵ������20�Ż�������á�Ŀǰsolexa˫���������ȴ����200-400bp������̫��ȷ ,Ĭ����400
	 * @param uniqReads ͬһλ����ظ��Ƿ������һ��
	 * @param cis5To3 �Ƿ���ѡĳһ�������reads
	 * @param uniqMapping �Ƿ���ѡΨһ�ȶԵ� 
	 */
	public void loadMap(String mapFile,int tagLength, boolean uniqReads, int startCod, Boolean cis5To3, boolean uniqMapping) {
		mapReads=new MapReadsHanyanChrom();
		mapReads.setBedSeq(mapFile);
		mapReads.setInvNum(1);
		mapReads.setMapChrID2Len(gffChrAbs.getSpecies().getMapChromInfo());
		mapReads.setFilter(uniqReads, startCod, uniqMapping, cis5To3);
		if (tagLength > 20) {
			mapReads.setTagLength(tagLength);
		}
		mapReads.run();
	}

//////////////////////////////////////////////////�����趨/////////////////////////////////////////////////////////
	protected double[] getReadsInfo(String geneID, GffGeneIsoInfo gffGeneIsoInfo) {
		double[] iso = mapReads.getRegionInfo(gffGeneIsoInfo.getParentGffDetailGene().getRefID(), gffGeneIsoInfo);
		if (iso == null) {
			return null;
		}
		if (!gffGeneIsoInfo.isCis5to3()) {
			ArrayOperate.convertArray(iso);
		}
		double[] isoResult = new double[iso.length+1];
		isoResult[0] = gffGeneIsoInfo.getLocDistmRNA(gffGeneIsoInfo.getTSSsite(), gffGeneIsoInfo.getATGsite());
		for (int i = 0; i < iso.length; i++) {
			isoResult[i+1] = iso[i];
		}
		return isoResult;
	}

	@Override
	protected ArrayList<String> getAllGeneName() {
		return gffChrAbs.getGffHashGene().getLsNameAll();
	}
	
	/////////////////////////////////////   �������Ŀ   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}
