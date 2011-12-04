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
	 * @param chrFilePath 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，<b>文件夹最后无所谓加不加"/"或"\\"</b>
	 * @param regx 序列名的正则表达式，null不设定 读取Chr文件夹的时候默认设定了 "\\bchr\\w*"
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
	 * 对于macs的结果bed文件，首先需要进行排序
	 * sort -k1,1 -k2,2n test #第一列起第一列终止排序，第二列起第二列终止按数字排序
	 * @param mapFile mapping文件
	 * @param mapFile2 当为""时，不读。如果有第二个mapping文件，这个主要是考虑正负链分开时的情况。所以不会有mapFile3了
	 * @param chrFilePath 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，文件夹最后无所谓加不加"/"或"\\"
	 * @param sep 
	 * @param colChrID chrID在第几列
	 * @param colStartNum 坐标起点在第几列
	 * @param colEndNum 坐标终点在第几列
	 * @param invNum 最后每个间隔为几个bp
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
	 * 对于macs的结果bed文件，首先需要进行排序
	 * sort -k1,1 -k2,2n test #第一列起第一列终止排序，第二列起第二列终止按数字排序
	 * @param mapFile mapping文件
	 * @param mapFile2 当为""时，不读。如果有第二个mapping文件，这个主要是考虑正负链分开时的情况。所以不会有mapFile3了
	 * @param chrFilePath 给定一个文件夹，这个文件夹里面保存了某个物种的所有染色体序列信息，文件夹最后无所谓加不加"/"或"\\"
	 * @param sep 
	 * @param colChrID chrID在第几列
	 * @param colStartNum 坐标起点在第几列
	 * @param colEndNum 坐标终点在第几列
	 * @param invNum 最后每个间隔为几个bp
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
	 * 画出所有染色体上密度图
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
	 * 给定染色体，返回该染色体上reads分布
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
			////////// 参 数 设 置 /////////////////////
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_PARAM,true, false);
			txtRparamater.writefile("Item"+"\t"+"Info"+"\r\n");//必须要加上的，否则R读取会有问题
			txtRparamater.writefile("tihsresolution"+"\t"+chrLength+"\r\n");
			txtRparamater.writefile("maxresolution"+"\t"+seqHash.getChrLenMax()+"\r\n");
			txtRparamater.writefile("ChrID"+"\t"+chrID+"\r\n");
			
			
			////////// 数 据 输 入 ///////////////////////
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_X, true,false);
			txtRparamater.Rwritefile(resolution);
			txtRparamater.setParameter(NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_Y, true,false);
			txtRparamater.Rwritefile(chrReads);
			
			///////////如果第二条染色体上有东西，那么也写入文本/////////////////////////////////////////
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
	 * 调用R画图
	 * @throws Exception
	 */
	private void hist() throws Exception
	{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+ NovelBioConst.R_WORKSPACE_CHIP_CHRREADS_RSCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
