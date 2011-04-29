package com.novelBio.chIPSeq.prepare;

import java.util.ArrayList;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.genome.GffChrUnion;
import com.novelBio.base.genome.GffLocatCod;
import com.novelBio.chIPSeq.cGIsland.CpG;


public class GenomeBasePrepare {
	protected static GffLocatCod gffLocatCod=new GffLocatCod();
	static int invNum=10;
	public static String sep="\t";
	/**
	 * �趨��ȡ����СƬ�μ����Ĭ��Ϊ10��Ҳ����10bp����
	 * @param invNum
	 */
	public void setInvNum(int invNum) {
		this.invNum = invNum;
	}
	
	/**
	 * ���ĳ��ֵ����Ϊ""�����ֵ������
	 * @param chrFilePath
	 * @param colMap mapping �ļ��� chr ��� �յ��λ��,��� mapFilePathû�У���ô����Ϊnull
	 * ����bed�ļ� 1��2��3
	 * ����ï���ļ���0��1��2
	 * @param gffClass ��ʵ������Gffhash���ֻ࣬���� "TIGR","CG","UCSC","Peak","Repeat"�⼸��
	 * @param gffFilePath
	 * @param mapFilePath Ĭ��10bp�ļ������
	 */
	public static void  prepare(String chrFilePath,int[] colMap,String gffClass,String gffFilePath,String mapFilePath) {
		if (chrFilePath != null && !chrFilePath.trim().equals("")) {
			gffLocatCod.loadChr(chrFilePath);
		}
		if (gffFilePath != null && !gffFilePath.trim().equals("")) {
			gffLocatCod.loadGff(gffClass,gffFilePath);
		}
		if (mapFilePath != null && !mapFilePath.trim().equals("")) {
			gffLocatCod.loadMap(mapFilePath, chrFilePath, sep, colMap[0], colMap[1], colMap[2], invNum, 0);
		}
	}
	
	/**
	 * ����Ⱦɫ��ͳ�ƽ��
	 * @throws Exception 
	 */
	public static void getChrStatistic(String txtchrInfo) throws Exception
	{
		//ArrayList<Long> lsGeneStructure = gffLocatCod.getGeneStructureLength();
		ArrayList<String[]> lsChrLength =gffLocatCod.getChrLengthInfo();
		TxtReadandWrite txtChrInfo = new TxtReadandWrite();
		txtChrInfo.setParameter(txtchrInfo, true, false);
		for (String[] strings : lsChrLength) {
			txtChrInfo.writefile(strings[0]+"\t"+strings[1]+"\n");
		}
	}
	
}
