package com.novelBio.chIPSeq.preprocess;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.fileOperate.FileOperate;
import com.novelBio.tools.compare.ComTxt;
import com.novelBio.tools.compare.runCompSimple;
import com.novelBio.tools.formatConvert.bedFormat.Soap2Bed;

public class Comb {
	public static void main(String[] args) {
		try {
			combMapPeak();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 整合分析
	 * @throws Exception 
	 */
	public static void combMapPeak() throws Exception {
		String thisFilePath=null;
		try {
			thisFilePath = runCompSimple.class.getResource("/").toURI().getPath();
			//thisFilePath= "/home/zong0jie/桌面/CDG/Compare/XYLCompare/eee/";
			//thisFilePath=thisFilePath.substring(1);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//这个不乱码;
		String IndexFile = "";
		String soapFile = "";
		//读取配置文件
		//配置文件格式，第一行soap程序的路径
		//第二行到结束： species \t ChrLenFile \t IndexFile \n
		//物种有 hs，os，mm，dm，ce
		TxtReadandWrite txtConf = new TxtReadandWrite();
		txtConf.setParameter(thisFilePath+"/Conf.txt", false, true);
		BufferedReader reader = txtConf.readfile();
		String content = "";
		HashMap<String, String[]> hashConf = new HashMap<String, String[]>();
		while ((content = reader.readLine()) != null) {
			content = content.trim();
			if (content.startsWith("#")) {
				continue;
			}
			String[] ss = content.split("\t");
			if (ss[0].trim().equals("soap")) {
				soapFile = ss[1];
				continue;
			}
			hashConf.put(ss[0], ss);
		}
		
		
		
		String species = null;
		String TreatFile1 = null;String TreatFile2 = null;
		String ColFile1 = null;String ColFile2 = null;
		String outFilePath = null;String prix = null;
		//读取配置文件，species有 hs，os，mm，dm，ce
		txtConf.setParameter(thisFilePath+"/Param.txt", false, true);
		ArrayList<String[]> lsConf2 = txtConf.ExcelRead("\t", 1, 1, txtConf.ExcelRows(), txtConf.ExcelColumns("\t"), 0);
		for (String[] strings : lsConf2) {
			if (strings[0].equals("species")) {
				species = strings[1].trim();
			}
			if (strings[0].equals("TreatFile1")) {
				TreatFile1 = strings[1].trim();
			}
			if (strings[0].equals("TreatFile2")) {
				TreatFile2 = strings[1].trim();
			}
			if (strings[0].equals("ColFile1")) {
				ColFile1 = strings[1].trim();
			}
			if (strings[0].equals("ColFile2")) {
				ColFile2 = strings[1].trim();
			}
			else if (strings[0].equals("outFilePath")) {
				outFilePath = strings[1].trim()+"/";
			}
			else if (strings[0].equals("prix")) {
				prix = strings[1].trim();
			}
		}
		boolean Col = false;
		if (ColFile1 != null && !ColFile1.trim().equals("")) {
			Col = true;
		}
		boolean SETreat = true;
	if (TreatFile2 != null && !TreatFile2.trim().equals("")) {
			SETreat = true;
		}
		boolean SECol = true;
		if (ColFile2 != null && !ColFile2.trim().equals("")) {
			SECol = false;
		}
		
		IndexFile = hashConf.get(species)[2];
		String chrLenFile = hashConf.get(species)[1];
		//mapping
		FileOperate.createFolder(outFilePath+"mapping");
		String outFile = outFilePath+"mapping/"+prix+"_Treat_SoapMap";
		long ReadsNumTreat = MapPeak.mapSoap(soapFile, TreatFile1, TreatFile2, IndexFile, outFile, 20, 500);
		long ReadsNumCol = 0;
		String outCol = null;
		if (Col) {
			outCol = outFilePath+"mapping/"+prix+"_Col_SoapMap";
			ReadsNumCol =MapPeak.mapSoap(soapFile, ColFile1, ColFile2, IndexFile, outCol, 20, 500);
		}
		//soap2bed
		FileOperate.createFolder(outFilePath+"bedFile");
		String outPutTreat = outFilePath+"bedFile/"+""+prix+"_Treat_macs.bed";
		String outCombTreat = outFilePath+"bedFile/"+prix+"_Treat_Cal.bed";
		String errorTreat = outFilePath+"bedFile/"+prix+"_Treat_error";
		String outPutCol = null;
		String outCombCol = null;
		String errorCol = null;
		Soap2Bed.getBed2Macs(SETreat, outFile, outPutTreat, outCombTreat, errorTreat);
		//QC
		TxtReadandWrite txtQC = new TxtReadandWrite();
		ArrayList<String[]> lsQCTreat = QualityCol.calCover(outCombTreat, chrLenFile, ReadsNumTreat, !SETreat, false, null);
		txtQC.setParameter(outFilePath+prix+"_mapping_SummaryTreat", true, false);
		txtQC.ExcelWrite(lsQCTreat, "\t", 1, 1);

		if (Col) {
			outPutCol = outFilePath+"bedFile/"+prix+"_Col_macs.bed";
			outCombCol = outFilePath+"bedFile/"+prix+"_Col_Cal.bed";
			errorCol = outFilePath+"bedFile/"+prix+"_Col_error";
			Soap2Bed.getBed2Macs(SECol, outCol, outPutCol, outCombCol, errorCol);
			//QC
			ArrayList<String[]> lsQCCol = QualityCol.calCover(outCombCol, chrLenFile, ReadsNumCol, !SECol, false, null);
			txtQC.setParameter(outFilePath+prix+"_mapping_SummaryCol", true, false);
			txtQC.ExcelWrite(lsQCCol, "\t", 1, 1);
		}
		//peakcalling
		FileOperate.createFolder(outFilePath+"peakCalling");
		MapPeak.peakCalMacs(outPutTreat, outPutCol, species, outFilePath+"peakCalling", prix);
	}
}
