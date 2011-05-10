package com.novelBio.chIPSeq.motif;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import com.novelBio.base.dataOperate.ExcelOperate;
import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.dataStructure.Patternlocation;
import com.novelBio.base.fileOperate.FileOperate;
import com.novelBio.base.genome.GffChrUnion;
import com.novelBio.base.genome.motifSearch.MotifSearch;
import com.novelBio.generalConf.NovelBioConst;


 


public class Motifsearch {
	int maxChrLength=10000;
	/**
	 * 获得motif在全基因组上的密度
	 * @param chrFilePath
	 * @param Rworkspace
	 * @param motifRex
	 * @throws Exception
	 */
	public void getChrMoitfDensity(String chrFilePath,String Rworkspace,String motifRex) throws Exception {
		
		 if (!Rworkspace.endsWith(File.separator)) {  
			 Rworkspace = Rworkspace + File.separator;  
	         }
		 GffChrUnion gffChrUnion=new GffChrUnion();
		MotifSearch cdgmotif=new MotifSearch();
		cdgmotif.MotifloadChrFile(chrFilePath);
		cdgmotif.startMotifSearch(motifRex, maxChrLength);
		Hashtable<String,ArrayList< double[]>> hashMotifDensity=cdgmotif.getMotifDensity();
		ArrayList<String[]> lsChrID=gffChrUnion.getChrLengthInfo();
		TxtReadandWrite txtReadandWrite=new TxtReadandWrite();
		txtReadandWrite.setParameter(Rworkspace+"MotifDensity/motifAllNum.txt", true,false);
		txtReadandWrite.writefile(cdgmotif.getMotifAllNum()+"");
		gffChrUnion.saveChrLengthToFile(Rworkspace+"MotifDensity/chrLen.txt");
		
		for (int i = 0; i < lsChrID.size(); i++) {
			String tmpChrID=lsChrID.get(i)[0].toLowerCase();
			ArrayList<double[]> tmpMotifInfo = hashMotifDensity.get(tmpChrID);
			
			txtReadandWrite.setParameter(Rworkspace+"MotifDensity/motifx",true, false);
			txtReadandWrite.Rwritefile(tmpMotifInfo.get(0));
			
			txtReadandWrite.setParameter(Rworkspace+"MotifDensity/motify", true,false);
			txtReadandWrite.Rwritefile(tmpMotifInfo.get(1));
			
			txtReadandWrite.setParameter(Rworkspace+"MotifDensity/parameter", true,false);
			txtReadandWrite.writefile("Item"+"\t"+"Info"+"\r\n");//必须要加上的，否则R读取会有问题
			ArrayList<String[]>  lschrInfo=gffChrUnion.getChrLengthInfo();
			int maxChrLen=Integer.parseInt(lschrInfo.get(lschrInfo.size()-1)[1]);
			txtReadandWrite.writefile("maxresolution"+"\t"+maxChrLen+"\r\n");
			txtReadandWrite.writefile("ChrID"+"\t"+tmpChrID+"\r\n");
			
			Rprogram("/media/winE/Bioinformatics/R/practice_script/platform/");
			//FileOperate.delFile(Rworkspace+"motifx");
			//FileOperate.delFile(Rworkspace+"motify");
			//FileOperate.delFile(Rworkspace+"parameter");
			FileOperate.changeFileName(Rworkspace+"MotifDensity/motifx",tmpChrID+"motifx");
			FileOperate.changeFileName(Rworkspace+"MotifDensity/motify",tmpChrID+"motify");
			FileOperate.changeFileName(Rworkspace+"MotifDensity/parameter",tmpChrID+"parameter");
		}
	}
	
	/**
	 * 执行R程序，直到R程序结束再返回
	 * @return
	 * @throws IOException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	private int Rprogram(String bin) throws IOException, InterruptedException  
	{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+bin+ "MyMotifDensity.R";
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		return 1;
	}
	
	
	
	/**
	 * @Gffclass 待读取的gffhash的类，目前只能有 "TIGR","CG","UCSC","Peak","Repeat"这几种
	 * @param gfffilename gff文件
	 * @param chrPah chr文件夹
	 * @param peaklength peak左右两端长度
	 * @param condition 
	 * 0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向
	 * 1: 通通提取正向
	 * 2: 通通提取反向
	 * @param txtFilepeakFile peak信息，以文本形式保存
	 * @param sep 文本里面的分割符，一般为"\t"
	 * @param columnID 读取哪几列，用int[]保存，列可以有间隔，现在读取这两列，0：chrID，1:peakSummit
	 * @param rowStart 从第几列读取
	 * @param rowEnd 读到第几列 如果rowEnd=-1，则一直读到文件结尾
	 * @param motifReg motif的正则表达式
	 * @throws Exception
	 * @return  ArrayList-String 每个peak的序列
	 */
	private ArrayList<Integer> getMotifLocation(String Gffclass, String gfffilename,String chrPah,int peaklength,int condition,String txtFilepeakFile,String sep
			,int[] columnID,int rowStart,int rowEnd,
			String motifReg
	) throws Exception
	{
		ArrayList<Integer> lsResult = new ArrayList<Integer>();
		GetSeq.prepare(chrPah, null, Gffclass, gfffilename, "");
		ArrayList<String> lsPeakSeq = GetSeq.getPeakSeq(peaklength, condition, txtFilepeakFile, sep, columnID, rowStart, rowEnd, false);//(Gffclass, gfffilename, chrPah, peaklength, condition, txtFilepeakFile, sep, columnID, rowStart, rowEnd,false);
		for (String string : lsPeakSeq) {
			ArrayList<String[]> lstmpResult = Patternlocation.getPatLoc(string, motifReg, false);
			for (String[] strings : lstmpResult) {
				int motiflength = strings[0].length();
				Integer motifLocation = Integer.parseInt(strings[1]) - peaklength + motiflength/2;
				lsResult.add(motifLocation);
			}
		}
		return lsResult;
	}
	
	/**
	 * @Gffclass 待读取的gffhash的类，目前只能有 "TIGR","CG","UCSC","Peak","Repeat"这几种
	 * @param gfffilename gff文件
	 * @param chrPah chr文件夹
	 * @param peaklength peak左右两端长度
	 * @param condition 
	 * 0:按照peak在gff里的情况提取，也就是基因内按基因方向，基因外正向
	 * 1: 通通提取正向
	 * 2: 通通提取反向
	 * @param txtFilepeakFile peak信息，以文本形式保存
	 * @param sep 文本里面的分割符，一般为"\t"
	 * @param columnID 读取哪几列，用int[]保存，列可以有间隔，现在读取这两列，0：chrID，1:peakSummit
	 * @param rowStart 从第几列读取
	 * @param rowEnd 读到第几列 如果rowEnd=-1，则一直读到sheet1文件结尾
	 * @param motifReg motif的正则表达式
	 * @param resultPath 文件保存路径
	 * @param resultPrix 文件保存的前缀
	 * @throws Exception
	 * @return  ArrayList-String 每个peak的序列
	 */
	public void getMotifSummitDensity(String Gffclass, String gfffilename,String chrPah,int peaklength,int condition,String txtFilepeakFile,String sep
			,int[] columnID,int rowStart,int rowEnd,
			String motifReg,String resultPath,String resultPrix
			) throws Exception
	{
		ArrayList<Integer> lsMotifLocation = getMotifLocation(Gffclass, gfffilename, chrPah, peaklength, condition, txtFilepeakFile, sep, columnID, rowStart, rowEnd, motifReg);
		TxtReadandWrite txtMotifDensity = new TxtReadandWrite();
		txtMotifDensity.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_DATA, true, false);
		TxtReadandWrite txtMotifDensityParam = new TxtReadandWrite();
		txtMotifDensityParam.setParameter(NovelBioConst.R_WORKSPACE_DENSITY_PARAM, true, false);
		txtMotifDensity.writefile(lsMotifLocation);
		//参数输入，第一行maintitle，第二行横坐标，第二行纵坐标
		txtMotifDensityParam.writefile("Motif Near Peak Summit"+"\n");
		txtMotifDensityParam.writefile("Peak Summit"+"\n");
		txtMotifDensityParam.writefile("Motif Density"+"\n");
		RDensity();
		FileOperate.moveFoldFile(NovelBioConst.R_WORKSPACE_DENSITY, resultPath, resultPrix,true);
	}
	
	/**
	 * @param Gffclass 待读取的gffhash的类，目前只能有 "TIGR","CG","UCSC","Peak","Repeat"这几种
	 * @param gfffilename gff文件
	 * @param chrPah chr文件夹
	 * @param length tss向上延生长度
	 * @param excelLoc 含有LOC的excel文件
	 * @param columnID 读取哪几列，用int[]保存，列可以有间隔，现在读取一列，0：LOCID
	 * @param writeCol 写入第几列
	 * @param rowStart 从第几列读取
	 * @param rowEnd 读到第几列 如果rowEnd=-1，则一直读到sheet1文件结尾
	 * @param motifReg motif的正则表达式
	 * @throws Exception
	 */
	public void getMotifDetail(String Gffclass, String gfffilename,String chrPah,int length,String excelLoc
			,int[] columnID,int writeCol, int rowStart, int rowEnd,
			String motifReg
			) throws Exception
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		GetSeq.prepare(chrPah, null, Gffclass, gfffilename, "");
		String[][] LOCIDInfo=ExcelTxtRead.readExcel(excelLoc, columnID, rowStart, rowEnd);
		for (String[] strings : LOCIDInfo) {
			if (strings[0].equals("LOC_Os04g59380")) {
				System.out.println("aaa");
			}
			String upstream = GetSeq.getGffLocatCod().getUpGenSeq(strings[0], length, true, true, Gffclass);
			ArrayList<String[]> lstmpResult = Patternlocation.getPatLoc(upstream, motifReg, false);
			String[] tmpResult = new String[lstmpResult.size()*2];
			for (int i = 0; i < lstmpResult.size(); i++) {
				tmpResult[i*2] = lstmpResult.get(i)[0];
				tmpResult[i*2+1] =  lstmpResult.get(i)[2];
			}
			lsResult.add(tmpResult);
		}
		ExcelOperate excel = new ExcelOperate();
		excel.openExcel(excelLoc);
		excel.WriteExcel(true,rowStart, writeCol, lsResult);
	}
	
	/**
	 * 执行R程序，直到R程序结束再返回
	 * @return
	 * @throws IOException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws Exception
	 */
	private int RDensity() throws IOException, InterruptedException  
	{
		//这个就是相对路径，必须在当前文件夹下运行
		String command="Rscript "+NovelBioConst.R_WORKSPACE_DENSITY_RSCRIPT;
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		return 1;
	}

}
