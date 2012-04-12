package com.novelbio.analysis.seq.genomeNew;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGeneAbs;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataStructure.ArrayOperate;


/**
 * 其中的ChrFa读取时候，必须将每行的换行符限定为"\n",有小工具能用
 * @author zong0jie
 *
 */
public class GffChrHanYanChrom extends GffChrHanYan{
	

public GffChrHanYanChrom(String gffClass, String GffFile, String ChrFilePath, int taxID) {
		super(gffClass, GffFile, ChrFilePath, taxID);
		// TODO Auto-generated constructor stub
	}
/**
 * 读取Mapping文件，生成相应的一维坐标数组，最后保存在一个哈希表中。
 * @param mapFile mapping的结果文件，一般为bed格式
 * @param startRegion bed文件的第一个值是否为开区间
 * @param chrFilePath 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，<b>文件夹最后无所谓加不加"/"或"\\"</b>
 * @param invNum 每隔多少位计数
 * @param tagLength 设定双端readsTag拼起来后长度的估算值，大于20才会进行设置。目前solexa双端送样长度大概是200-400bp，不用太精确 ,默认是400
 * @param uniqReads 同一位点的重复是否仅保留一个
 * @param colUnique UniqueMapping的标记在哪一列
 * @param cis5To3 是否挑选某一个方向的reads
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
//////////////////////////////////////////////////参数设定/////////////////////////////////////////////////////////
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
	
	/////////////////////////////////////   韩燕的项目   //////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}
