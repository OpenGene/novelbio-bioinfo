package com.novelbio.analysis.seq.chipseq.regDensity;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.util.MathUtils;
import com.novelbio.analysis.seq.chipseq.prepare.GenomeBasePrepare;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.java.HeatChart;
import com.novelbio.generalConf.NovelBioConst;


public class RegDensity extends GenomeBasePrepare
{
	
	String[][] LocInfo=null;
	/**
	 * 读取peak信息
	 * @param txtExcelFile peak文件，用“\t”隔开
	 * @param columnID 读取哪几列，依次为 chrID， 起点， 终点
	 * @param rowStart 从第几行读起
	 * @param rowEnd 读到第几行，<1 表示
	 */
	public void getPeakInfo(String txtFile,int[] columnID,int rowStart,int rowEnd) 
	{
		try { LocInfo=ExcelTxtRead.readtxtExcel(txtFile, sep, columnID, rowStart, rowEnd); } catch (Exception e) { 	e.printStackTrace(); 	}
	}
	
	int tssRegion = 10000;
	/**
	 * 设定peak在Tss的多少范围内时，将该基因Tss计入计算范围,默认10k
	 * @param invNum
	 */
	public void setTssRegion(int tssRegion) {
		this.tssRegion = tssRegion;
	}
	int geneEndRegion = 10000;
	/**
	 * 设定peak在Tss的多少范围内时，将该基因Tss计入计算范围,默认10k
	 * @param invNum
	 */
	public void setGeneEndRegion(int geneEndRegion) {
		this.geneEndRegion = geneEndRegion;
	}
	/**
	 * 
	 * @param type Tss或GeneEnd
	 * @param range Tss两端区域
	 * @param binNum 分割分数
	 * @param figure 图片路径
	 * @param RworkSpace 
	 */
	public void getRegionDensity(String type,int range,int binNum,String resultFilePath,String prefix) {
		if (type.equals("Tss")) {
			getTssDensity(range, binNum,resultFilePath,prefix);
		}
		else if (type.equals("GeneEnd")) {
			getGeneEndDensity(range, binNum,resultFilePath,prefix);
		}
	}
	
	
	/**
	 * 指定一系列基因名的做出TSS图
	 * @param geneFile txt文件，读取第一列 指定geneID，注意是RefSeq内的geneID，也就是说，该ID必须在gff文件中出现过。
	 * @param range Tss两端区域
	 * @param binNum 分割分数
	 * @param RworkSpace 
	 * @param resultFilePath 保存至哪个文件夹
	 * @param prefix 文件名前缀
	 * @throws Exception 
	 */
	public void getGeneNameTssDensity(String geneFile,int range,int binNum,String resultFilePath,String prefix) throws Exception {
		TxtReadandWrite txtGeneID = new TxtReadandWrite();
		txtGeneID.setParameter(geneFile, false, true);
		String[][] geneID = txtGeneID.ExcelRead("\t", 1, 1, txtGeneID.ExcelRows(), 1);
		String[] geneID2 = new String[geneID.length];
		for (int i = 0; i < geneID.length; i++) {
			geneID2[i] = geneID[i][0];
		}
		
		gffLocatCod.setUpstreamTSSbp(tssRegion);gffLocatCod.setDownStreamTssbp(tssRegion);
		gffLocatCod.setGeneEnd3UTR(geneEndRegion);
		double[] TssDensity=gffLocatCod.getTssRange(geneID2, range, binNum);
 
		TxtReadandWrite tssReadandWrite=new TxtReadandWrite();
		tssReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_R, true,false);
		try { tssReadandWrite.Rwritefile(TssDensity); 	} catch (Exception e) { 	e.printStackTrace(); }
		tssReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_PARAM, true,false);
		try{
			tssReadandWrite.writefile(range+""); 
		} catch (Exception e) { 	e.printStackTrace(); }
		try {density("Tss");	} catch (Exception e) {	e.printStackTrace();}
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_EXCEL, resultFilePath,prefix+"tss.txt",true);
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_PIC, resultFilePath,prefix+"TSSReads.jpg",true);
		
		TxtReadandWrite txtTmpGenNum = new TxtReadandWrite();
		//写入该区域进行统计的基因数目
		 if (!resultFilePath.endsWith(File.separator)) {  
			 resultFilePath = resultFilePath + File.separator;  
	         }
		 txtTmpGenNum.setParameter(resultFilePath+prefix+"tssGenNum.txt", true, false);
		 try {
			txtTmpGenNum.writefile(gffLocatCod.getRegGenNum()+"");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 	 * 指定一系列基因名的做出GeneEnd图
	 * @param geneFIle
	 * @param range GeneEnd两端区域
	 * @param binNum 分割分数
	 * @param RworkSpace 
	 * @param resultFilePath 保存至哪个文件夹
	 * @param prefix 文件名前缀
	 * @throws Exception 
	 */
	public void getGeneNameGeneEndDensity(String geneFIle,int range,int binNum,String resultFilePath,String prefix) throws Exception {
		
		TxtReadandWrite txtGeneID = new TxtReadandWrite();
		txtGeneID.setParameter(geneFIle, false, true);
		String[][] geneID = txtGeneID.ExcelRead("\t", 1, 1, txtGeneID.ExcelRows(), 1);
		String[] geneID2 = new String[geneID.length];
		for (int i = 0; i < geneID.length; i++) {
			geneID2[i] = geneID[i][0];
		}
		
		
		gffLocatCod.setUpstreamTSSbp(tssRegion);
		gffLocatCod.setGeneEnd3UTR(geneEndRegion);
		double[] GeneEndDensity=gffLocatCod.getGeneEndRange(geneID2, range, binNum);
		TxtReadandWrite geneEndReadandWrite=new TxtReadandWrite();
		geneEndReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_GENEEND_R, true,false);
		try { geneEndReadandWrite.Rwritefile(GeneEndDensity); 	} catch (Exception e) { 	e.printStackTrace(); }
		geneEndReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_PARAM, true,false);
		try { geneEndReadandWrite.writefile(range+""); 	} catch (Exception e) { 	e.printStackTrace(); }
		try {density("GeneEnd");	} catch (Exception e) {	e.printStackTrace();}
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_GENEEND_EXCEL, resultFilePath,prefix+"geneEnd2.txt",true);
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_GENEEND_PIC, resultFilePath,prefix+"GeneEndReads.jpg",true);
		TxtReadandWrite txtTmpGenNum = new TxtReadandWrite();
		//写入该区域进行统计的基因数目
		 if (!resultFilePath.endsWith(File.separator)) {  
			 resultFilePath = resultFilePath + File.separator;  
	         }
		 txtTmpGenNum.setParameter(resultFilePath+prefix+"geneEndGenNum.txt", true, false);
		 try {
				txtTmpGenNum.writefile(gffLocatCod.getRegGenNum()+"");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	/**
	 * 指定基因名的做出TSS图
	 * @param geneFile txt文件，读取第一列 指定geneID，注意是RefSeq内的geneID，也就是说，该ID必须在gff文件中出现过。
	 * @param range Tss两端区域
	 * @param binNum 分割分数
	 * @param RworkSpace 
	 * @param resultFilePath 保存至哪个文件夹
	 * @param prefix 文件名前缀
	 * @throws Exception 
	 */
	public void getGeneNameTssDensity(String geneName,int range,String prefix,int binNum,String resultFilePath) throws Exception {
		
		gffLocatCod.setUpstreamTSSbp(tssRegion);gffLocatCod.setDownStreamTssbp(tssRegion);
		gffLocatCod.setGeneEnd3UTR(geneEndRegion);
		double[] TssDensity=gffLocatCod.getTssRange(geneName, range, binNum);
 
		TxtReadandWrite tssReadandWrite=new TxtReadandWrite();
		tssReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_R, true,false);
		try { tssReadandWrite.Rwritefile(TssDensity); 	} catch (Exception e) { 	e.printStackTrace(); }
		tssReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_PARAM, true,false);
		try{
			tssReadandWrite.writefile(range+""); 
		} catch (Exception e) { 	e.printStackTrace(); }
		try {density("Tss");	} catch (Exception e) {	e.printStackTrace();}
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_EXCEL, resultFilePath,prefix+"tss.txt",true);
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_PIC, resultFilePath,prefix+"TSSReads.jpg",true);
		
		TxtReadandWrite txtTmpGenNum = new TxtReadandWrite();
		//写入该区域进行统计的基因数目
		 if (!resultFilePath.endsWith(File.separator)) {  
			 resultFilePath = resultFilePath + File.separator;  
	         }
		 txtTmpGenNum.setParameter(resultFilePath+prefix+"tssGenNum.txt", true, false);
		 try {
			txtTmpGenNum.writefile(gffLocatCod.getRegGenNum()+"");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 	 * 指定基因名的做出GeneEnd图
	 * @param geneFIle
	 * @param range GeneEnd两端区域
	 * @param binNum 分割分数
	 * @param RworkSpace 
	 * @param resultFilePath 保存至哪个文件夹
	 * @param prefix 文件名前缀
	 * @throws Exception 
	 */
	public void getGeneNameGeneEndDensity(String geneName,int range,String prefix,int binNum,String resultFilePath) throws Exception {
		gffLocatCod.setUpstreamTSSbp(tssRegion);
		gffLocatCod.setGeneEnd3UTR(geneEndRegion);
		double[] GeneEndDensity=gffLocatCod.getGeneEndRange(geneName, range, binNum);
		TxtReadandWrite geneEndReadandWrite=new TxtReadandWrite();
		geneEndReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_GENEEND_R, true,false);
		try { geneEndReadandWrite.Rwritefile(GeneEndDensity); 	} catch (Exception e) { 	e.printStackTrace(); }
		geneEndReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_PARAM, true,false);
		try { geneEndReadandWrite.writefile(range+""); 	} catch (Exception e) { 	e.printStackTrace(); }
		try {density("GeneEnd");	} catch (Exception e) {	e.printStackTrace();}
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_GENEEND_EXCEL, resultFilePath,prefix+"geneEnd2.txt",true);
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_GENEEND_PIC, resultFilePath,prefix+"GeneEndReads.jpg",true);
		TxtReadandWrite txtTmpGenNum = new TxtReadandWrite();
		//写入该区域进行统计的基因数目
		 if (!resultFilePath.endsWith(File.separator)) {  
			 resultFilePath = resultFilePath + File.separator;  
	         }
		 txtTmpGenNum.setParameter(resultFilePath+prefix+"geneEndGenNum.txt", true, false);
		 try {
				txtTmpGenNum.writefile(gffLocatCod.getRegGenNum()+"");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	/**
	 * 根据Peak文件做出TSS图
	 * @param range Tss两端区域
	 * @param binNum 分割分数
	 * @param figure 图片路径
	 * @param RworkSpace 
	 * @param resultFilePath 保存至哪个文件夹
	 * @param prefix 文件名前缀
	 */
	private void getTssDensity(int range,int binNum,String resultFilePath,String prefix) {
		
		gffLocatCod.setUpstreamTSSbp(tssRegion);gffLocatCod.setDownStreamTssbp(tssRegion);
		gffLocatCod.setGeneEnd3UTR(geneEndRegion);
		double[] TssDensity=gffLocatCod.getUCSCTssRange(LocInfo, range, binNum);
 
		TxtReadandWrite tssReadandWrite=new TxtReadandWrite();
		tssReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_R, true,false);
		try { tssReadandWrite.Rwritefile(TssDensity); 	} catch (Exception e) { 	e.printStackTrace(); }
		tssReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_PARAM, true,false);
		try{
			tssReadandWrite.writefile(range+""); 
		} catch (Exception e) { 	e.printStackTrace(); }
		try {density("Tss");	} catch (Exception e) {	e.printStackTrace();}
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_EXCEL, resultFilePath,prefix+"tss.txt",true);
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_PIC, resultFilePath,prefix+"TSSReads.jpg",true);
		
		TxtReadandWrite txtTmpGenNum = new TxtReadandWrite();
		//写入该区域进行统计的基因数目
		 if (!resultFilePath.endsWith(File.separator)) {  
			 resultFilePath = resultFilePath + File.separator;  
	         }
		 txtTmpGenNum.setParameter(resultFilePath+prefix+"tssGenNum.txt", true, false);
		 try {
			txtTmpGenNum.writefile(gffLocatCod.getRegGenNum()+"");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 *  根据Peak文件做出GeneEnd图
	 * @param range geneEnd两端区域
	 * @param binNum 分割分数
	 * @param figure 图片路径
	 * @param RworkSpace 
	 */
	private void getGeneEndDensity(int range,int binNum,String resultFilePath,String prefix) {
		gffLocatCod.setUpstreamTSSbp(tssRegion);
		gffLocatCod.setGeneEnd3UTR(geneEndRegion);
		double[] GeneEndDensity=gffLocatCod.getUCSCGeneEndRange(LocInfo, range, binNum);
		TxtReadandWrite geneEndReadandWrite=new TxtReadandWrite();
		geneEndReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_GENEEND_R, true,false);
		try { geneEndReadandWrite.Rwritefile(GeneEndDensity); 	} catch (Exception e) { 	e.printStackTrace(); }
		geneEndReadandWrite.setParameter(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_PARAM, true,false);
		try { geneEndReadandWrite.writefile(range+""); 	} catch (Exception e) { 	e.printStackTrace(); }
		try {density("GeneEnd");	} catch (Exception e) {	e.printStackTrace();}
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_GENEEND_EXCEL, resultFilePath,prefix+"geneEnd2.txt",true);
		FileOperate.moveFile(NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_GENEEND_PIC, resultFilePath,prefix+"GeneEndReads.jpg",true);
		TxtReadandWrite txtTmpGenNum = new TxtReadandWrite();
		//写入该区域进行统计的基因数目
		 if (!resultFilePath.endsWith(File.separator)) {  
			 resultFilePath = resultFilePath + File.separator;  
	         }
		 txtTmpGenNum.setParameter(resultFilePath+prefix+"geneEndGenNum.txt", true, false);
		 try {
				txtTmpGenNum.writefile(gffLocatCod.getRegGenNum()+"");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	
	/**
	 * 
	 * @param type Tss或GeneEnd
	 * @param range Tss两端区域
	 * @param binNum 分割分数
	 * @param figure 图片路径
	 * @param RworkSpace 
	 */
	public void getRegionDensityHeatMap(String type,int range,int binNum,String resultFilePath,String prefix) {
		double[][] Density= null;
		if (type.equals("Tss")) {
			Density=gffLocatCod.getUCSCTssRangeArray(LocInfo, range, binNum);
		}
		else if (type.equals("GeneEnd")) {
			Density=gffLocatCod.getUCSCGeneEndRangeArray(LocInfo, range, binNum);
		}
		getDensityHeatMap(Density, range, binNum,resultFilePath,prefix, type);
	}
	
	
	/**
	 * 
	 * 根据Peak文件做出TSS图
	 * @param TssDensity 左右两端的坐标信息
	 * @param range Tss两端区域
	 * @param binNum 分割分数
	 * @param resultFilePath 保存至哪个文件夹
	 * @param prefix 文件名前缀
	 * @param regix 后缀
	 */
	private void getDensityHeatMap(double[][] TssDensity, int range,int binNum,String resultFilePath,String prefix, String regix) {
		
		gffLocatCod.setUpstreamTSSbp(tssRegion);gffLocatCod.setDownStreamTssbp(tssRegion);
		gffLocatCod.setGeneEnd3UTR(geneEndRegion);
		
		double[] tssValue = new double[TssDensity.length*TssDensity[0].length];
		int k = 0;
		for (double[] tss : TssDensity) {
			for (double d : tss) {
				tssValue[k] = d; k++;
			}
		}
		System.out.println(StatUtils.percentile(tssValue, 95));
		
//		HeatChart map = new HeatChart(TssDensity,0,StatUtils.percentile(tssValue, 95));
		HeatChart map = new HeatChart(TssDensity,0,50);
		map.setTitle("This is my heat chart title");
		map.setXAxisLabel("X Axis");
		map.setYAxisLabel("Y Axis");
		
		String[] aa = new String[]{"a","b","c","d","e","f"};
		map.setXValues(aa);
		String[] nn = new String[TssDensity.length];
		for (int i = 0; i < nn.length; i++) {
			nn[i] = "";
		}
		map.setYValues(nn);
		Dimension bb = new Dimension();
		bb.setSize(1, 0.01);
		map.setCellSize(bb );
		//Output the chart to a file.
		Color colorblue = Color.BLUE;
		Color colorRed = Color.WHITE;
		//map.setBackgroundColour(color);
		map.setHighValueColour(colorblue);
		map.setLowValueColour(colorRed);
		try {
			map.saveToFile(new File(resultFilePath+prefix+regix+".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(map.getChartSize().getHeight());
	}
	
	
	/**
	 * 
	 * @param RworkSpace R空间
	 * @param type Tss或GeneEnd
	 * @throws Exception
	 */
	private void density(String type) throws Exception{
		String command="";
		//这个就是相对路径，必须在当前文件夹下运行
		if (type.equals("Tss")) {
			command="Rscript "+NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_TSS_RSCRIPT;
		}
		else if (type.equals("GeneEnd")) {
			command="Rscript "+NovelBioConst.R_WORKSPACE_CHIP_READS_REGION_GENEEND_RSCRIPT;
		}
		Runtime   r=Runtime.getRuntime();
		Process p = r.exec(command);
		p.waitFor();
		System.out.println("ok");
	}
}
