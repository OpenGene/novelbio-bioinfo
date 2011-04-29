package com.novelBio.chIPSeq.preprocess;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.fileOperate.FileOperate;
import com.novelBio.tools.compare.ComTxt;
import com.novelBio.tools.compare.runCompSimple;
import com.novelBio.tools.formatConvert.FastQ;
import com.novelBio.tools.formatConvert.bedFormat.Soap2Bed;

public class Comb {
	public static void main(String[] args) {
		String filePath = getProjectPath();
		System.out.println(filePath);
		try {
			combMapPeak(filePath);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//这个不乱码;

	}
	 public static String getProjectPath() {
		 java.net.URL url = Comb.class.getProtectionDomain().getCodeSource().getLocation();
		 String filePath = null;
		 try {
		 filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
		 } catch (Exception e) {
		 e.printStackTrace();
		 }
		 if (filePath.endsWith(".jar"))
		 filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
		 java.io.File file = new java.io.File(filePath);
		 filePath = file.getAbsolutePath();
		 return filePath;
		 }
		 
	/**
	 * 整合分析
	 * @throws Exception 
	 */
	public static void combMapPeak(String filePath) throws Exception {
		String thisFilePath=filePath;

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
			if (content.startsWith("#")||content.trim().equals("")) {
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
		String outFilePath = null;String prix = "";
		//读取配置文件，species有 hs，os，mm，dm，ce
		txtConf.setParameter(thisFilePath+"/Param.txt", false, true);
		ArrayList<String[]> lsConf2 = txtConf.ExcelRead("\t", 1, 1, txtConf.ExcelRows(), txtConf.ExcelColumns("\t"), 0);
		for (String[] strings : lsConf2) {
			if (strings[0].equals("species")) {
				try {
					species = strings[1].trim();
				} catch (Exception e) {
				}
				
			}
			if (strings[0].equals("TreatFile1")) {
				try {
					TreatFile1 = strings[1].trim();
				} catch (Exception e) {
				}
				
			}
			if (strings[0].equals("TreatFile2")) {
				try {
					TreatFile2 = strings[1].trim();
				} catch (Exception e) {
				}
				
			}
			if (strings[0].equals("ColFile1")) {
				try {
					ColFile1 = strings[1].trim();
				} catch (Exception e) {
				}
				
			}
			if (strings[0].equals("ColFile2")) {
				try {
					ColFile2 = strings[1].trim();
				} catch (Exception e) {
				}
				
			}
			else if (strings[0].equals("outFilePath")) {
				try {
					outFilePath = strings[1].trim()+"/";
				} catch (Exception e) {
				}
				
			}
			else if (strings[0].equals("prix")) {
				try {
					prix = strings[1].trim();
				} catch (Exception e) {
				}
			}
		}
		boolean Col = false;
		if (ColFile1 != null && !ColFile1.trim().equals("")) {
			Col = true;
		}
		boolean SETreat = true;
	if (TreatFile2 != null && !TreatFile2.trim().equals("")) {
			SETreat = false;
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
		ArrayList<String> lsFastQ = Soap2Bed.getSoapFastQStr(outFile);
		String fastQ = FastQ.guessFastOFormat(lsFastQ);
		Soap2Bed.copeSope2Bed(fastQ,SETreat, outFile, outPutTreat, outCombTreat, errorTreat);
		String outCombTreatSort = outFilePath+"bedFile/"+prix+"_Treat_Cal_Sort.bed";
		MapPeak.sortBedFile(thisFilePath,outCombTreat, 1, outCombTreatSort, 2,3);
		
		
		
		//QC
		TxtReadandWrite txtQC = new TxtReadandWrite();
		ArrayList<String[]> lsQCTreat = QualityCol.calCover(outCombTreatSort, chrLenFile, ReadsNumTreat, !SETreat, false, null);
		txtQC.setParameter(outFilePath+prix+"_mapping_SummaryTreat", true, false);
		txtQC.ExcelWrite(lsQCTreat, "\t", 1, 1);

		if (Col) {
			outPutCol = outFilePath+"bedFile/"+prix+"_Col_macs.bed";
			outCombCol = outFilePath+"bedFile/"+prix+"_Col_Cal.bed";
			errorCol = outFilePath+"bedFile/"+prix+"_Col_error";
			lsFastQ = Soap2Bed.getSoapFastQStr(outFile);
			fastQ = FastQ.guessFastOFormat(lsFastQ);
			Soap2Bed.copeSope2Bed(fastQ,SECol, outCol, outPutCol, outCombCol, errorCol);
			String outCombColSort = outFilePath+"bedFile/"+prix+"_Col_Cal_Sort.bed";
			MapPeak.sortBedFile(outFilePath,outCombCol, 1, outCombColSort, 2,3);
			
			
			//QC
			ArrayList<String[]> lsQCCol = QualityCol.calCover(outCombColSort, chrLenFile, ReadsNumCol, !SECol, false, null);
			txtQC.setParameter(outFilePath+prix+"_mapping_SummaryCol", true, false);
			txtQC.ExcelWrite(lsQCCol, "\t", 1, 1);
		}
		//peakcalling
		FileOperate.createFolder(outFilePath+"peakCalling");
		MapPeak.peakCalMacs(thisFilePath,outPutTreat, outPutCol, species, outFilePath+"peakCalling", prix);
	}
}
