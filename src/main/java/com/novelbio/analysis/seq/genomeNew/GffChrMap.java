package com.novelbio.analysis.seq.genomeNew;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReadsHanyanChrom;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ������������򣬻�������ͳ��ͼ
 * @author zong0jie
 *
 */
public class GffChrMap extends GffChrAbs{
	/**
	 * 
	 */
	boolean HanYanFstrand =false;
	/**
	 * 
	 * @param gffType
	 * @param gffFile
	 * @param readsBed
	 * @param binNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 * @param HanYanFstrand �Ƿ�ѡ����ģʽ������reads�Ƿ������ķ�����һ�¶����й��˹����������ר����Ժ������Ŀ���ķ�����
	 */
	public GffChrMap(String gffType, String gffFile, String chrFile,String readsBed, int binNum, boolean HanYanFstrand) {
		super(gffType, gffFile, chrFile, readsBed, binNum);
		this.HanYanFstrand = HanYanFstrand;
	}
	
	/**
	 * @param readsFile mapping�Ľ���ļ��������Ź���һ��Ϊbed��ʽ
	 * @param binNum ÿ������λ����������趨Ϊ1�����㷨��仯��Ȼ���ܾ�ȷ
	 */
	public void setMapReads(String readsFile, int binNum) {
		if (FileOperate.isFileExist(readsFile)) {
			if (HanYanFstrand) {
				mapReads = new MapReadsHanyanChrom(binNum, readsFile);
				mapReads.setChrLenFile(getRefLenFile());
				mapReads.setNormalType(mapNormType);
			}
			else {
				mapReads = new MapReads(binNum, readsFile);
				mapReads.setChrLenFile(getRefLenFile());
				mapReads.setNormalType(mapNormType);
			}
		}
	}
	
	/**
	 * ��ȡbed�ļ�
	 */
	public void readMapBed() {
		try {
			mapReads.ReadMapFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ��reads�ļ���bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @param colUnique Unique��reads����һ�� novelbio�ı���ڵ����У���1��ʼ����
	 * @param booUniqueMapping �ظ���reads�Ƿ�ֻѡ��һ��
	 * @param cis5to3 �Ƿ��ѡȡĳһ�����reads��null������
	 */
	public void setFilter(boolean uniqReads, int startCod, int colUnique, boolean booUniqueMapping, Boolean cis5to3) {
		mapReads.setFilter(uniqReads, startCod, colUnique, booUniqueMapping, cis5to3);
	}
	
	/**
	 * ����ĳ��Ⱦɫ���ϵ�reads����������ܶ�ͼ��ֻ�Ǽ򵥵ļ���reads��һ��Ⱦɫ���ϵ����
	 * ��Ҫ����RefSeqʱ��һ�������ϵ�reads���
	 * @param chrID
	 * @param thisInvNum ÿ�����伸bp
	 * @parm type ȡ������ 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return û�еĻ��ͷ���null
	 */
	public double[] getChrInfo(String chrID, int thisInvNum, int type)
	{
		double[] tmpResult = mapReads.getRengeInfo(thisInvNum, chrID, 0, 0, type);
		mapReads.normDouble(tmpResult, super.mapNormType);
		return tmpResult;
	}
	

	
	int maxresolution =10000;
	
	/**
	 * ��������Ⱦɫ�����ܶ�ͼ
	 * @param gffChrMap2 �Ƿ��еڶ���Ⱦɫ�壬û�еĻ�����null
	 * @throws Exception
	 */
	public void getAllChrDist(GffChrMap gffChrMap2) 
	{
		ArrayList<String[]> chrlengthInfo=seqHash.getChrLengthInfo();
		for (int i = chrlengthInfo.size()-1; i>=0; i--) {
			try {
				getChrDist(chrlengthInfo.get(i)[0], maxresolution, gffChrMap2);
			} catch (Exception e) { 	e.printStackTrace();			}
		}
	}
	
	
	/**
	 * 
	 * ����Ⱦɫ�壬���ظ�Ⱦɫ����reads�ֲ�
	 * @param chrID �ڼ�����ɫ��
	 * @param maxresolution ��ֱ���
	 * @param gffChrMap2 �����Ҫ���ڶ���Ⱦɫ���ͼ��Ҳ���ǶԳ��˻�
	 * @param ����ļ���������׺"_chrID"
	 * @throws Exception
	 */
	private void getChrDist(String chrID,int maxresolution, GffChrMap gffChrMap2) throws Exception
	{
		int[] resolution=seqHash.getChrRes(chrID, maxresolution);
		double[] chrReads=getChrDensity(chrID.toLowerCase(),resolution.length);
		long chrLength =seqHash.getChrLength(chrID);
		if (chrReads!=null)
		{
			TxtReadandWrite txtRparamater=new TxtReadandWrite();
			////////// �� �� �� �� /////////////////////
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_PARAM,true, false);
			txtRparamater.writefile("Item"+"\t"+"Info"+"\r\n");//����Ҫ���ϵģ�����R��ȡ��������
			txtRparamater.writefile("tihsresolution"+"\t"+chrLength+"\r\n");
			txtRparamater.writefile("maxresolution"+"\t"+seqHash.getChrLenMax()+"\r\n");
			txtRparamater.writefile("ChrID"+"\t"+chrID+"\r\n");
			
			////////// �� �� �� �� ///////////////////////
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_X, true,false);
			txtRparamater.Rwritefile(resolution);
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_Y, true,false);
			txtRparamater.Rwritefile(chrReads);
			
			///////////����ڶ���Ⱦɫ�����ж�������ôҲд���ı�/////////////////////////////////////////
			if (gffChrMap2!=null) 
			{
				double[] chrReads2=gffChrMap2.getChrDensity(chrID.toLowerCase(), resolution.length);
				txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_2Y, true,false);
				txtRparamater.Rwritefile(chrReads2);
			}
			hist();
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_X,chrID+"readsx");
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_Y,chrID+"readsy");
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_2Y,chrID+"reads2y");
			FileOperate.changeFileName(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_PARAM,chrID+"parameter");
		}
	}
	
	/**
	 * ����ĳ��Ⱦɫ���ϵ�reads��������ܶ�ͼ
	 * ��Ҫ���ڻ������ϣ�һ��Ⱦɫ���ϵ�reads���
	 * @param chrID
	 * @param binNum �ֳɼ�������
	 * @parm type ȡ������ 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return û�еĻ��ͷ���null
	 */
	private double[] getChrDensity(String chrID, int binNum)
	{
		double[] tmpResult = mapReads.getReadsDensity(chrID, 0, 0, binNum);
		mapReads.normDouble(tmpResult, super.mapNormType);
		return tmpResult;
	}
	
	/**
	 * ����R��ͼ
	 * @throws Exception
	 */
	private void hist() throws Exception
	{
		//����������·���������ڵ�ǰ�ļ���������
		String command="Rscript "+ NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_RSCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}
	
	
	
	
	/**
	 * ָ��һ������Χ��list������ÿ��list��reads��Ϣ
	 * Ĭ�ϼ�Ȩƽ��
	 * @param lsMapInfo ֱ���޸ı�List
	 * @param binNum
	 */
	private void getRegionReads( int binNum) {
		int type = 0;
		mapReads.getRegionLs(binNum, lsMapInfos, type);
	}
	
	private void getTssMapInfo()
	{
		super.getPeakGeneStructure(lsPeakInfo, structure);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * רΪ�������<br>
	 * ��Ϊrefseqʱ����õ�ĳ������ķֲ����������3��barcode����
	 * @return
	 * û�иû����򷵻�null
	 */
	public double[] getGeneReadsHYRefseq(String geneID) {
		double[] tmpResult = getChrInfo(geneID, 1, 0);
		if (tmpResult == null) {
			return null;
		}
		//��þ���ת¼������Ϣ
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		return combineLoc(tmpResult, gffGeneIsoInfoOut.getLenUTR5()+1);
	}
	/**
	 * ����atgλ�㣬��ø�atgλ���ںϲ����������Ӧ���ǵڼ�������1��ʼ
	 * @param atgSite
	 * @return
	 */
	public int getCombAtgSite(String geneID)
	{
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		int atgSite = gffGeneIsoInfoOut.getLenUTR5()+1;
		//����3��ָ3�����
		return (int)Math.ceil((double)(atgSite -  1)/3);
	}
	
	/**
	 * ����atgλ�㣬��ø�atgλ���ںϲ����������Ӧ���ǵڼ�������1��ʼ
	 * @param atgSite
	 * @return
	 */
	public int getAtgSite(String geneID)
	{
		GffGeneIsoInfo gffGeneIsoInfoOut = gffHashGene.searchISO(geneID);
		return gffGeneIsoInfoOut.getLenUTR5()+1;
		//����3��ָ3�����
	}
	/**
	 * רΪ�������
	 * ����������ϲ�Ϊ1��coding��ȡ3�������һ�������Ӧ��reads��
	 * @param geneReads �û����reads��Ϣ�������ǵ��������
	 * @param AtgSite �û����atgλ�㣬��1��ʼ����
	 * @return
	 * ���ؾ����ϲ��Ľ����Ʃ��
	 * {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
	 * atgλ��Ϊ6
	 * ���{ 2,5,8,11,14,17};
	 */
	private double[] combineLoc(double[] geneReads, int AtgSite)
	{
		//��ʱ��SeqInfo��һλ����ʵ�ʵĵ�һλ������atgsite��		
		return MathComput.mySplineHY(geneReads, 3, AtgSite, 3);
	}

	/**
	 * ����<b>����</b>ʹ��<br>
	 * ��û������Ϣ��Ȼ�����򣬿��Դ�������ѡ�����ļ���Ȼ��ͼ
	 * ���ؾ��������mapinfo��list��ÿһ��mapInfo�����˸û���ĺ�������Ϣ
	 */
	public ArrayList<MapInfo> getChrInfo() {
		ArrayList<String> lsChrID = mapReads.getChrIDLs();
		ArrayList<MapInfo> lsMapInfo = new ArrayList<MapInfo>();
		for (String string : lsChrID) {
			mapReads.setNormalType(MapReads.NORMALIZATION_NO);
			GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(string);
			if (!gffGeneIsoInfo.getGeneType().equals( GffGeneIsoInfo.TYPE_GENE_MRNA)
					&& !gffGeneIsoInfo.getGeneType().equals( GffGeneIsoInfo.TYPE_GENE_MRNA_TE)
			) {
				continue;
			}
			
			double[] tmp = mapReads.getRengeInfo(mapReads.getBinNum(), string, 0, 0,0);
			mapReads.setNormalType(super.mapNormType);
			double[] tmp2 = mapReads.getRengeInfo(mapReads.getBinNum(), string, 0, 0,0);
			///////////////////  �� �� �� �� /////////////////////////////////////////////////////////////////////
			if (tmp == null && tmp2 == null) {
				continue;
			}
			else if (tmp == null) {
				tmp = new double[tmp2.length];
			}
			else if (tmp2 == null) {
				tmp2 = new double[tmp.length];
			}
			////////////////////////////////////////////////////////////////////////////////////////
			int combatgSite = getCombAtgSite(string);
			tmp2 = combineLoc(tmp2, getAtgSite(string));

			
			
			double weight = MathComput.sum(tmp);
			MapInfo mapInfo = new MapInfo(string);
			mapInfo.setWeight(weight);
			mapInfo.setDouble(tmp2);
			mapInfo.setFlagLoc(combatgSite);
			CopedID copedID = new CopedID(string, 0, false);
			mapInfo.setTitle(copedID.getSymbo());
			lsMapInfo.add(mapInfo);
		}
		Collections.sort(lsMapInfo);
		return lsMapInfo;
	}
	
	
}
