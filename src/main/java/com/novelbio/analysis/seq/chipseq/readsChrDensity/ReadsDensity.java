package com.novelbio.analysis.seq.chipseq.readsChrDensity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genome.GffChrUnion;
import com.novelbio.analysis.seq.genome.getChrSequence.ChrSearch;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.ChrStringHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqHashAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;


 

public class ReadsDensity
{
	GffChrUnion gffChrUnion=new GffChrUnion();
	GffChrUnion gffChrUnion2=null;
	
	SeqHashAbs seqHash = null;
	/**
	 * @param chrFilePath ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ��<b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	 * @param regx ��������������ʽ��null���趨 ��ȡChr�ļ��е�ʱ��Ĭ���趨�� "\\bchr\\w*"
	 */
	private SeqHashAbs getSeqInfo(String chrFilePath, String regx) 
	{
		SeqHashAbs seqHash = null; 
		if (FileOperate.isFile(chrFilePath)) 
			seqHash = new SeqFastaHash(chrFilePath);
		if (FileOperate.isFileDirectory(chrFilePath)) 
			seqHash = new ChrStringHash(chrFilePath, regx, true);
		return seqHash;
	}
	
	int maxresolution =10000;
	/**
	 * ����macs�Ľ��bed�ļ���������Ҫ��������
	 * sort -k1,1 -k2,2n test #��һ�����һ����ֹ���򣬵ڶ�����ڶ�����ֹ����������
	 * @param mapFile mapping�ļ�
	 * @param mapFile2 ��Ϊ""ʱ������������еڶ���mapping�ļ��������Ҫ�ǿ����������ֿ�ʱ����������Բ�����mapFile3��
	 * @param chrFilePath ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ���ļ����������ν�Ӳ���"/"��"\\"
	 * @param sep 
	 * @param colChrID chrID�ڵڼ���
	 * @param colStartNum ��������ڵڼ���
	 * @param colEndNum �����յ��ڵڼ���
	 * @param invNum ���ÿ�����Ϊ����bp
	 */
	public void prepare(String mapFile,String mapFile2,String chrFilePath,String sep,int colChrID,int colStartNum,int colEndNum,int invNum,int tagLength) 
	{
		seqHash = getSeqInfo(chrFilePath, "");
		gffChrUnion.loadMap(mapFile, chrFilePath, sep, colChrID, colStartNum, colEndNum, invNum,tagLength);
		if (!mapFile2.trim().equals("")) {
			gffChrUnion2=new GffChrUnion();
			gffChrUnion2.loadMap(mapFile2, chrFilePath, sep, colChrID, colStartNum, colEndNum, invNum, tagLength);
		}
	}
	
	/**
	 * ����macs�Ľ��bed�ļ���������Ҫ��������
	 * sort -k1,1 -k2,2n test #��һ�����һ����ֹ���򣬵ڶ�����ڶ�����ֹ����������
	 * @param mapFile mapping�ļ�
	 * @param mapFile2 ��Ϊ""ʱ������������еڶ���mapping�ļ��������Ҫ�ǿ����������ֿ�ʱ����������Բ�����mapFile3��
	 * @param chrFilePath ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ���ļ����������ν�Ӳ���"/"��"\\"
	 * @param sep 
	 * @param colChrID chrID�ڵڼ���
	 * @param colStartNum ��������ڵڼ���
	 * @param colEndNum �����յ��ڵڼ���
	 * @param invNum ���ÿ�����Ϊ����bp
	 */
	public void prepare(String mapFile,String mapFile2,String chrFilePath,String regx,String sep,int colChrID,int colStartNum,int colEndNum,int invNum,int tagLength) 
	{
		seqHash = getSeqInfo(chrFilePath, "");
		gffChrUnion.loadMap(mapFile, chrFilePath,regx, sep, colChrID, colStartNum, colEndNum, invNum,tagLength);
		if (!mapFile2.trim().equals("")) {
			gffChrUnion2=new GffChrUnion();
			gffChrUnion2.loadMap(mapFile2, chrFilePath,regx, sep, colChrID, colStartNum, colEndNum, invNum, tagLength);
		}
	}
	public double[] getLocReadsFigure(String chrID,int locStart,int locEnd,int resolution) 
	{
		return gffChrUnion.getRangReadsDist(chrID, locStart, locEnd,resolution);
	}
	
	/**
	 * ��������Ⱦɫ�����ܶ�ͼ
	 * @throws Exception
	 */
	public void getAllChrDist() throws Exception 
	{
		ArrayList<String[]> chrlengthInfo=seqHash.getChrLengthInfo();
		for (int i = chrlengthInfo.size()-1; i>=0; i--) {
			getChrDist(chrlengthInfo.get(i)[0], maxresolution);
		}
	}
	
 
	/**
	 * ����Ⱦɫ�壬���ظ�Ⱦɫ����reads�ֲ�
	 * @param chrID
	 * @param locStart
	 * @param locEnd
	 * @param resolution
	 * @throws Exception
	 */
	private void getChrDist(String chrID,int maxresolution) throws Exception
	{
		int[] resolution=seqHash.getChrRes(chrID, maxresolution);
		double[] chrReads=gffChrUnion.getReadsDensity(chrID.toLowerCase(), 1, -1, resolution.length);
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
			if (gffChrUnion2!=null) 
			{
				double[] chrReads2=gffChrUnion2.getReadsDensity(chrID.toLowerCase(), 1, -1, resolution.length);
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
