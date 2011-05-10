package com.novelbio.chIPSeq.readsChrDensity;

import java.util.ArrayList;

import com.novelBio.base.dataStructure.MathComput;
import com.novelBio.base.genome.GffChrUnion;
import com.novelBio.base.genome.GffPeakOverlap;
import com.novelBio.base.genome.gffOperate.GffDetail;
import com.novelBio.base.genome.gffOperate.GffHashPeak;
import com.novelBio.base.genome.mappingOperate.MapReads;




public class ReadsInRegion  
{
	/**
	 * ����һ�������bed������Ϣ��Ʃ��bivalent����ȣ�����������Щ�������ۼ�reads���ܶ����������TSSͼ
	 *@param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
     *@param   chrFilePath ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ���ļ����������ν�Ӳ���"/"��"\\"
     *@param sep 
     *@param colChrID ChrID�ڵڼ��У���1��ʼ
     *@param colStartNum mapping����ڵڼ��У���1��ʼ
     *@param colEndNum mapping�յ��ڵڼ��У���1��ʼ
     *@param invNum ÿ������λ����
     *@param tagLength �趨˫��readsTagƴ�����󳤶ȵĹ���ֵ������20�Ż�������á�Ŀǰsolexa˫���������ȴ����200-400bp������̫��ȷ ,Ĭ����400
	* @param RegInfo һ�������bed������Ϣ��Ʃ��bivalent����� ������Region��������Ϣ��string[3]��0�� chrID��1��startNum  2:endNum 
     * @param binNum ��󷵻ص����򱻷�Ϊ���ٿ�
	 * @param RegInfo
	 * @throws Exception 
	 */
	public double[] getRegionReads(String mapFile,String chrFilePath,String sep,int colChrID,int colStartNum,int colEndNum,int invNum,int tagLength,String[][] RegInfo,int binNum) throws Exception 
	{
		GffChrUnion gffChrUnion=new GffChrUnion();
		gffChrUnion.loadMap(mapFile, chrFilePath, sep, colChrID, colStartNum, colEndNum, invNum, tagLength);
		
		
		GffHashPeak gffHashRegion=new GffHashPeak();
		gffHashRegion.ReadGffarray(RegInfo);
		ArrayList<String> chrIDlist=gffHashRegion.getLOCChrHashIDList();
		double[] regionReads=new double[binNum];
		for (int i = 0; i < chrIDlist.size(); i++) {
			String tmpRegion=chrIDlist.get(i).split("/")[0];
			GffDetail gffRegionDetail=gffHashRegion.LOCsearch(tmpRegion);
			double[] tmpReads=gffChrUnion.getRangReadsDist(binNum,gffRegionDetail.ChrID, gffRegionDetail.numberstart, gffRegionDetail.numberend);
			MathComput.addArray(regionReads, tmpReads);
		}
		return regionReads;
	}
	

	
}
