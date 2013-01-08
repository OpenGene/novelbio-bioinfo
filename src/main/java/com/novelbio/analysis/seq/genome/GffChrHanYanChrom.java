package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataStructure.ArrayOperate;


/**
 * 其中的ChrFa读取时候，必须将每行的换行符限定为"\n",有小工具能用
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
	 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。
	 * @param mapFile mapping的结果文件，一般为bed格式
	 * @param invNum 每隔多少位计数
	 * @param tagLength 设定双端readsTag拼起来后长度的估算值，大于20才会进行设置。目前solexa双端送样长度大概是200-400bp，不用太精确 ,默认是400
	 * @param uniqReads 同一位点的重复是否仅保留一个
	 * @param cis5To3 是否挑选某一个方向的reads
	 * @param uniqMapping 是否挑选唯一比对的 
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

//////////////////////////////////////////////////参数设定/////////////////////////////////////////////////////////
	protected double[] getReadsInfo(String geneID, GffGeneIsoInfo gffGeneIsoInfo) {
		double[] iso = mapReads.getRangeInfo(gffGeneIsoInfo.getParentGffDetailGene().getRefID(), gffGeneIsoInfo);
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
	
	/////////////////////////////////////   韩燕的项目   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}
