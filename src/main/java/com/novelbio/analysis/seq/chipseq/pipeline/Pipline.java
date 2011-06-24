package com.novelbio.analysis.seq.chipseq.pipeline;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.chipseq.BedPeakMacs;
import com.novelbio.analysis.seq.chipseq.peakAnnotation.PeakAnno;
import com.novelbio.analysis.seq.chipseq.peakAnnotation.peakLoc.PeakLOC;
import com.novelbio.analysis.seq.chipseq.preprocess.Comb;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.test.mytest;

public class Pipline {
	private static Logger logger = Logger.getLogger(mytest.class);  
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String outPath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110608Paper/PeakCalling";
		
		try {
			String parentPath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110608Paper/mapping/GSM531966_H3K9me1_PHF8_RNAi_control.bed/";
			String prix = "PHF8_RNAi_control";
//			String file = parentPath + "GSM531966_H3K9me1_PHF8_RNAi_control.bed";
//			String Outfile = parentPath + "GSM531966_H3K9me1_PHF8_RNAi_control_Filter.bed";
//			String outBedFileExtend = parentPath + "GSM531966_H3K9me1_PHF8_RNAi_control_Filter_extend.bed";
//			filterBed(file, Outfile, outBedFileExtend,200);
//			peakCalling(Outfile,outPath,prix);
			addSumMid(outPath+"/"+prix+"_peaks.xls", outPath+"/"+prix+"_peaks_Summit.xls");
		}
		catch (Exception e) {}
		
		try {
//			String parentPath = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110608Paper/mapping/GSM531965_H3K9me1_PHF8_RNAi.bed/";
			String prix = "PHF8_RNAi";
//			String file = parentPath + "GSM531965_H3K9me1_PHF8_RNAi.bed";
//			String Outfile = parentPath + "GSM531965_H3K9me1_PHF8_RNAi_Filter.bed";
//			String outBedFileExtend = parentPath + "GSM531965_H3K9me1_PHF8_RNAi_Filter_extend.bed";
//			filterBed(file, Outfile, outBedFileExtend,200);
//			peakCalling(Outfile,outPath,prix);
			addSumMid(outPath+"/"+prix+"_peaks.xls",outPath+"/"+prix +"_peaks_Summit.xls");
		}
		catch (Exception e) {}
	}
	
	/**
	 * 专门为王彦儒的项目定制的过滤程序
	 * @param inBedFile 
	 * @param outBedFile
	 * @param extendLen 延长至多少bp
	 */
	public static void filterBed(String inBedFile, String outBedFile1,String outBedFileExtend,int extendLen) {
		
		BedSeq bedSeq = new BedSeq(inBedFile);
		try {
			bedSeq = bedSeq.filter(outBedFile1);
			bedSeq.extend(extendLen, outBedFileExtend);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void peakCalling(String bedFile,String outFilePath, String prix) {
		BedPeakMacs bedPeakMacs = new BedPeakMacs(bedFile);
		try {
			bedPeakMacs.peakCallling(Comb.getProjectPath()+"/../..", null, "hs", outFilePath, prix);
//			bedPeakMacs.peakCallling(Comb.getProjectPath()+"", null, "hs", outFilePath, prix); //正式用
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}
	/**
	 * 给summit位点添上middle
	 */
	public static void addSumMid(String peakFile,String outFile) {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite();
		txtReadandWrite.setParameter(peakFile, false, true);
		ArrayList<String> lsResult = new ArrayList<String>();
		try {
			List<String> lsStrings =  txtReadandWrite.readfileLs();
			ArrayList<String[]> lsMid = name(20, lsStrings);
			txtReadandWrite.setParameter(outFile, true, false);
			txtReadandWrite.ExcelWrite(lsMid, "\t", 1, 1);
		} catch (Exception e) {
			logger.error("读取peak文件出错");
			e.printStackTrace();
		}
	}
	
	/**
	 * @param rowNum 从第几行开始计算summitMid，实际行
	 * @param lsInput
	 * @param lsOutput
	 */
	private  static ArrayList<String[]> name(int rowNum, List<String> lsInput) {
		rowNum--;
		ArrayList<String[]> lsOutput = new ArrayList<String[]>();
		
		for (int i = rowNum; i < lsInput.size(); i++) {
			String[] ss= lsInput.get(i).split("\t");
			String[] ssNew = new String[ss.length+1];
			for (int j = 0; j < 5; j++) {
				ssNew[j] = ss[j];
			}
			try {
				ssNew[5] = Integer.parseInt(ss[1])+Integer.parseInt(ss[4])+"";
			} catch (Exception e) {
				ssNew[5] = "SummitMid";
			}
			
			for (int j = 5; j < ss.length; j++) {
				ssNew[j+1] = ss[j];
			}
			lsOutput.add(ssNew);
		}
		return lsOutput;
	}
	
	
}
