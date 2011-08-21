package com.novelbio.analysis.seq.genomeNew;

import java.util.List;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class GffChrChIP extends GffChr{

	public GffChrChIP(String gffClass, String GffFile, String ChrFilePath,
			int taxID) {
		super(gffClass, GffFile, ChrFilePath, taxID);
	}
	/**
	 * 给定基因，返回以该基因为中点，左右两边为region的一段区域
	 * 这个主要用于批量提取某些基因周围的区域，给IGV画图用
	 * @param accID
	 * @param region
	 * @return string[3] : 0:chrID 1: startNum 2: endNum<br>
	 * 没有则返回null
	 */
	public String[] getGeneRegion(String accID,int region) {
		GffHashGene gffHashGene = (GffHashGene) gffHash;
		GffDetailGene gffDetailGene = gffHashGene.getGeneDetail(accID);
		if (gffDetailGene == null) {
			return null;
		}
		int center = (gffDetailGene.getNumStart() + gffDetailGene.getNumEnd())/2;
		String[] result = new String[3];
		result[0] = gffDetailGene.getChrID();
		result[1] = center - region +"";
		result[2] = center + region + "";
		return result;
	}
	/**
	 * 给定一系列geneID和区域，返回一个能让IGV识别的脚本
	 * @param lsGeneID
	 * @param region
	 * @param resultScriptFile
	 * @throws Exception
	 */
	public void getIGVInfo(List<String> lsGeneID, int region, String resultScriptFile) throws Exception {
		TxtReadandWrite txtIGV = new TxtReadandWrite();
		txtIGV.setParameter(resultScriptFile, true, false);
		txtIGV.writefileln("snapshotDirectory "+ FileOperate.getParentPathName(resultScriptFile));
		for (String string : lsGeneID) {
			String[] tmpResult = getGeneRegion(string, region);
			String tmpWrite = "goto " + tmpResult[0] + ":" + tmpResult[1] + "-" + tmpResult[2] + "\n" + "snapshot "+ string+".png";
			txtIGV.writefileln(tmpWrite);
		}
		txtIGV.close();
	}
}
