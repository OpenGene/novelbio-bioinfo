package com.novelbio.analysis.seq.chipseq.peakcalling;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.chipseq.peakcalling.PeakCallingSicer.PeakCallingSicerType;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.tools.Mas3.getProbID;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.TitleFormatNBC;
import com.sun.tools.javac.code.Attribute.Array;

public class SicerControl {
	private static Logger logger = Logger.getLogger(SicerControl.class);
	
	public static final int METHY_H3K4 = 10;
	public static final int METHY_H3K27 = 20;
	public static final int METHY_DNA5MC = 30;
	public static final int METHY_UNKNOWN = 40;
	
	Species species;
	
	String PathTo;
	int methylationType = METHY_H3K4;
	PeakCallingSicer peakCallingSicer = new PeakCallingSicer();
	
	String dir;
	int WindowSize;
	int  GapSize;
	
	String prefixKO = "";
	String prefixWT = "";
	
	PeakCallingSicerType peakCallingSicerType;
	
	public SicerControl() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.sicer);
		peakCallingSicer.setExePath(softWareInfo.getExePath());
	}
	public void setSicerType( PeakCallingSicerType peakCallingSicerType) {
		this.peakCallingSicerType = peakCallingSicerType;
	}
	/** 璁惧��插���俊����规�杩�釜�ュ�瀹�indow size ��ap size */
	public void setMethylationType(int methylationType) {
		if (methylationType == METHY_H3K4) {
			peakCallingSicer.setWindowSize(200);
			peakCallingSicer.setGapSize(200);
			WindowSize = 200;
			GapSize = 200;
		} else if (methylationType == METHY_H3K27) {
			peakCallingSicer.setWindowSize(200);
			peakCallingSicer.setGapSize(600);
			WindowSize = 200;
			GapSize = 600;
		} else if (methylationType == METHY_DNA5MC) {
			peakCallingSicer.setWindowSize(200);
			peakCallingSicer.setGapSize(600);
			WindowSize = 200;
			GapSize = 600;
			
		} else {
			peakCallingSicer.setWindowSize(200);
			peakCallingSicer.setGapSize(600);
			WindowSize = 200;
			GapSize = 600;
		}
	}
	
	public void setMethylationType(int windowSize, int gapSize){
		peakCallingSicer.setWindowSize(windowSize);
		peakCallingSicer.setGapSize(gapSize);
	}

	
	private void setEffectiveGenomeSize(int readsLength) {
		if (readsLength < 25) {
			peakCallingSicer.setEffectiveGenomeSize(65);
		} else if (readsLength >= 25 && readsLength < 30) {
			peakCallingSicer.setEffectiveGenomeSize(75);
		} else if (readsLength >= 35 && readsLength < 50) {
			peakCallingSicer.setEffectiveGenomeSize(80);
		} else if (readsLength >= 50 && readsLength < 60) {
			peakCallingSicer.setEffectiveGenomeSize(85);
		} else if (readsLength >= 60 && readsLength < 70) {
			peakCallingSicer.setEffectiveGenomeSize(88);
		} else if (readsLength >= 70) {
			peakCallingSicer.setEffectiveGenomeSize(90);
		}
	}
	
	public void setOutputDir(String outputPathAndFilenameDir) {
		peakCallingSicer.setoutputDir(outputPathAndFilenameDir);
	}
	
	public void setSpecies(Species species) {
		peakCallingSicer.setSpecies(species.getVersion().split("_")[0]);
	}
	
	/**
	 * �规�杩�釜�ヨ�瀹�ndir锛�����浠�ed��欢���涓�ed��欢涓�����涓��浠跺す涓��灏变�灏��浠��璐����	 * 蹇�』绗��姝ヨ�瀹�	 * @param koBedPathAndFile
	 */
	public void setKoBedFile(String koBedPathAndFile, String prefix) {
		this.prefixKO = getPrefix(koBedPathAndFile, prefix);
		
		String indir = FileOperate.getParentPathName(koBedPathAndFile);
		setInputDir(indir);
		peakCallingSicer.setInputDir(indir);
		setParamFromBedFile(koBedPathAndFile);
		String koBedFile = FileOperate.getFileName(koBedPathAndFile);
		peakCallingSicer.setKoBedFile(koBedFile);
	}
	
	public void setWtBedFile(String wtBedPathAndFile, String prefix) {
		this.prefixWT = getPrefix(wtBedPathAndFile, prefix);;
		linkBed(wtBedPathAndFile);
		peakCallingSicer.setWtBedFile(FileOperate.getFileName(wtBedPathAndFile));
	}
	/** �规���欢���杈�����缂����杩����� */
	private String getPrefix(String fileName, String prefix) {
		if (prefix != null && !prefix.trim().equals("")) {
			return prefix.trim();
		}
		return FileOperate.getFileNameSep(fileName)[0];
	}
	public void setKoControlFile(String koColBed) {
		linkBed(koColBed);
		peakCallingSicer.setWtBedFile(FileOperate.getFileName(koColBed));
	}
	
	public void setWtControlFile(String wetColBed) {
		linkBed(wetColBed);
		peakCallingSicer.setWtBedFile(FileOperate.getFileName(wetColBed));
	}
	
	private void setInputDir(String indir) {
		this.dir = indir;
		peakCallingSicer.setInputDir(indir);
	}
	/**
	 * �规�杈����ed��欢�峰�涓�郴������璀��effective genomsize��ragmentsize绛�	 */
	private void setParamFromBedFile(String inputBed) {
		BedSeq bedSeq = new BedSeq(inputBed);
		int i = 1;
		List<Integer> lsReadsLen = new ArrayList<Integer>();
		for (BedRecord bedRecord : bedSeq.readLines()) {
			if (i > 1000) {
				break;
			}
			lsReadsLen.add(Math.abs(bedRecord.getEndAbs() - bedRecord.getStartAbs()));
			i++;
		}
		int length = (int) MathComput.median(lsReadsLen, 80);
		setEffectiveGenomeSize(length);
		peakCallingSicer.setFragmentSize(250-length);
	}
	
	/**
	 * 濡��杈����ed��欢��ndir涓��涓�捣锛���疯�杩��
	 * @param inputBed
	 */
	private void linkBed(String inputBed) {
		String indir = FileOperate.getParentPathName(inputBed);
		String bedfile = FileOperate.getFileName(inputBed);
		if (!this.dir.equals(indir)) {
			if (!FileOperate.linkFile(inputBed, this.dir + bedfile, true)) {
				logger.error("绉诲���欢�洪�锛� + inputBed + "   " + this.dir + bedfile);
			}
		}
	}
	
	/**
	 * @param fdr 榛��0.01
	 */
	public void setFDR(double fdr) {
		peakCallingSicer.setFDR(fdr);
	}
	/**
	 * E-value is not p-value. Suggestion for first try on histone modification data: 
	 * E-value=100. If you find ~10000 islands using this evalue, an empirical estimate of FDR
	 * is 1E-2.
	 * @param evalue 榛��100
	 */
	public void setEvalue(int evalue) {
		peakCallingSicer.setEvalue(evalue);
	}
	
	public void peakCalling() {
		modifyGenomePy();
		
		//淇��瀹�����寰��涓���匡��叉����SICER璇诲�Genome.py��欢�洪� 
		try {Thread.sleep(1000);} catch (InterruptedException e) {}
		
		ArrayList<String> lsOutFile = peakCallingSicer.peakCallingAndGetResultFile(peakCallingSicerType);
		dealResult(peakCallingSicerType, lsOutFile, peakCallingSicer.getOutDir(), prefixKO, prefixWT);
	}
	
	private void modifyGenomePy() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.sicer);
		String GenomeDataFile = softWareInfo.getExePath() + "SICER/lib/GenomeData.py";
		ModifyGenomeData.modifyDataFile(species, GenomeDataFile);
	}
	
	private static void dealResult(PeakCallingSicerType sicerType,ArrayList<String> lsOutFile,String outDir,String prefixKO,String prefixWT) {
		String resultDir = creatForde(sicerType,outDir);
		if (sicerType == PeakCallingSicerType.SICERrb || sicerType == PeakCallingSicerType.SICER) {
			modiyResultSicer(sicerType, resultDir, lsOutFile.get(0));
		}
		if (sicerType == PeakCallingSicerType.SICERdfrb || sicerType == PeakCallingSicerType.SICERdf ) {
			modiyResultSicer_DIF(lsOutFile, resultDir, prefixKO, prefixWT);
		}
	}
	
	private static String creatForde(PeakCallingSicerType sicerType,String outDir) {
		String resultDir =FileOperate.getParentPathName(outDir) + sicerType +"_result/";
		FileOperate.createFolders(resultDir);
		return resultDir;
	}
	/**
	 * 澶��娌℃�KO��icer缁��
	 * ���sicer��icer-rb�����	 * @param resultDir
	 * @param resultFile
	 */
	private static void modiyResultSicer(PeakCallingSicerType peakCallingSicerType, String resultDir, String resultFile) {
		TxtReadandWrite txtRead;
		TxtReadandWrite txtWrite;
		String tempOutFile = FileOperate.getFileName(resultFile);
		FileOperate.copyFile(resultFile, resultDir+tempOutFile, false);
		String finalOutFile = resultDir+tempOutFile + "_result.txt";
		txtRead = new TxtReadandWrite(resultDir+tempOutFile, false);
		txtWrite = new TxtReadandWrite(finalOutFile, true);
		txtWrite.writefileln(getTitleSICER(peakCallingSicerType));
		for (String string2 : txtRead.readlines()) {	
			txtWrite.writefileln(string2);
		}
		txtWrite.close();
		txtRead.close();
	}
	/**
	 * @param lsSicerDifResultFile ��icerdif璺��涔���烘����涓�����浠�	 */
	private static void modiyResultSicer_DIF(ArrayList<String> lsSicerDifResultFile, String resultDir, String prefixKO, String prefixWT) {
		for (String sicerResultFileName : lsSicerDifResultFile) {
			modifyResult(sicerResultFileName, resultDir, prefixKO, prefixWT);
		}
	}
	
	private static void modifyResult(String sicerResultFileName, String resultDir, String prefixKO, String prefixWT) {
		String tempOutFile = FileOperate.getFileName(sicerResultFileName);
		FileOperate.copyFile(tempOutFile, resultDir + tempOutFile, true);
		TxtReadandWrite txtRead = new TxtReadandWrite(resultDir+tempOutFile, false);
		
		String finalOutFile = resultDir+tempOutFile + "_result.txt";
		TxtReadandWrite txtWrite = new TxtReadandWrite(finalOutFile, true);
		
		//write the title
		txtWrite.writefileln(getTitleSICER_DIF(prefixKO, prefixWT));
		
		int i = 1;
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			if (i == 1) {
				try { int num = Integer.parseInt(ss[1]); } 
				catch (Exception e) {
					continue;
				}
			}
			txtWrite.writefileln(content);
			i++;
		}
		txtRead.close();
		txtWrite.close();
	}
	
	/**
	 * @param peakCallingSicerType
	 * @param prefixKO 濡��涓��
	 * @param prefixWT
	 * @return
	 */
	private static String[] getTitleSICER(PeakCallingSicerType peakCallingSicerType) {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.ChrID.toString());
		lsTitle.add(TitleFormatNBC.LocStart.toString());
		lsTitle.add(TitleFormatNBC.LocEnd.toString());
		
		if (peakCallingSicerType == PeakCallingSicerType.SICER) {
			lsTitle.add("ChIP_island_read_count");
			lsTitle.add("CONTROL_island_read_count");
			lsTitle.add(TitleFormatNBC.Pvalue.toString());
			lsTitle.add(TitleFormatNBC.FoldChange.toString());
			lsTitle.add(TitleFormatNBC.FDR.toString());
		} else if (peakCallingSicerType == PeakCallingSicerType.SICERrb) {
			lsTitle.add(TitleFormatNBC.Score.toString());
		}
		
		String[] tiltes = lsTitle.toArray(new String[0]);
		return tiltes;
	}
	
	/**
	 * @param peakCallingSicerType
	 * @param prefixKO 濡��涓��
	 * @param prefixWT
	 * @return
	 */
	private static String[] getTitleSICER_DIF(String prefixKO, String prefixWT) {
		ArrayList<String> lsTitle = new ArrayList<String>();
		lsTitle.add(TitleFormatNBC.ChrID.toString());
		lsTitle.add(TitleFormatNBC.LocStart.toString());
		lsTitle.add(TitleFormatNBC.LocEnd.toString());
		lsTitle.add("Readcount_" + prefixKO);
		lsTitle.add("Normalized_Readcount_" + prefixKO);
		lsTitle.add("Readcount_" + prefixWT);
		lsTitle.add("Normalized_Readcount_" + prefixWT);
		lsTitle.add(TitleFormatNBC.FoldChange.toString() + getA_vs_B(prefixKO, prefixWT));
		lsTitle.add(TitleFormatNBC.Pvalue.toString() + getA_vs_B(prefixKO, prefixWT));
		lsTitle.add(TitleFormatNBC.FDR.toString() + getA_vs_B(prefixKO, prefixWT));
			
		lsTitle.add(TitleFormatNBC.FoldChange.toString() + getA_vs_B(prefixWT, prefixKO));
		lsTitle.add(TitleFormatNBC.Pvalue.toString() + getA_vs_B(prefixWT, prefixKO));
		lsTitle.add(TitleFormatNBC.FDR.toString() + getA_vs_B(prefixWT, prefixKO));
		
		String[] tiltes = lsTitle.toArray(new String[0]);
		return tiltes;
	}
	/**
	 * 杩�� _A_vs_B��舰寮�	 * @param A
	 * @param B
	 * @return
	 */
	private static String getA_vs_B(String A, String B) {
		return  "_" + A + "_vs_" + B;
	}
	
	
	static HashMap<String, Integer> mapMethyStr2Int;
	public static HashMap<String, Integer> getMapMethyStr2Int() {
		if (mapMethyStr2Int == null) {
			mapMethyStr2Int = new HashMap<String, Integer>();
			mapMethyStr2Int.put("METHY_H3K4", METHY_H3K4);
			mapMethyStr2Int.put("METHY_H3K27", METHY_H3K27);
			mapMethyStr2Int.put("METHY_DNA5MC", METHY_DNA5MC);
			mapMethyStr2Int.put("unKnowMethylationType", METHY_UNKNOWN);
		}
		return mapMethyStr2Int;
	}
	
}

/**
 * �规�杈�����绉��淇��SICER��enome.py
 * @author zong0jie
 *
 */
class ModifyGenomeData {
	static ArrayListMultimap<String, String> mapSpeciesName_chroms = ArrayListMultimap.create();
	static ArrayListMultimap<String, String> mapSpeciesName_chromLengths = ArrayListMultimap.create();
	static ArrayListMultimap<String, String> mapSpecies_chroms = ArrayListMultimap.create();
	static ArrayListMultimap<String, String> mapSpecies_chromLengths  = ArrayListMultimap.create();
	static ArrayList<String> lsFirstLine =new ArrayList<String>();
	public static void readGenomeData( String GenomeDataFile) {
		/**
		 * 涓��澶���崇郴锛��搴�enomeData��绉��绯�		 * mapSpeciesName_chroms
		 * mapSpeciesName_chromLengths
		 * mapSpecies_chroms
		 * mapSpecies_chromLengths
		 */

		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(GenomeDataFile, false);
		lsFirstLine = txtReadandWrite.readFirstLines(41);
		for (String string : txtReadandWrite.readlines()) {
			/*
			 * �峰�涓�釜�崇郴mapSpeciesName_chroms
			 */
			if (string.contains("_chroms = ['")) {
				String[] strings= string.split("=");
				String[] chroms = strings[1].split("'");
				for (int i = 1; i < chroms.length; i=i+2) {
					mapSpeciesName_chroms.put(strings[0].trim(), "'" +chroms[i].trim() +"'");
				}
			}
			/*
			 * �峰�绗��涓��绯�apSpeciesName_chromLengths
			 */
			if (string.contains("_chrom_lengths = {'")) {
				String[] strings= string.split("=");
				String[] chromLengths = strings[1].split(",");
				for (int i = 0; i < chromLengths.length; i++) {
					if (chromLengths[i].contains("{")||chromLengths[i].contains("}")) {
						chromLengths[i] = chromLengths[i].replace("{", "");
						chromLengths[i] = chromLengths[i].replace("}", "");
						
					}
					mapSpeciesName_chromLengths.put(strings[0].trim(), chromLengths[i].trim());
				}
			}
			/*
			 * �峰�绗���崇郴mapSpecies_chroms
			 */
			if (string.contains("_chroms = {'")) {
				String[] strings= string.split("=");
				String[] chroms = strings[1].split(",");
				for (int i = 0; i < chroms.length; i++) {
					if (chroms[i].contains("{")||chroms[i].contains("}")) {
						chroms[i] = chroms[i].replace("{", "");
						chroms[i] = chroms[i].replace("}", "");
					}
					mapSpecies_chroms.put(strings[0].trim(), chroms[i].trim());
				}
				
 			}
			/*
			 * �峰�绗��涓��绯�apSpecies_chromLengths
			 */
			if (string.contains("_chrom_lengths={'")) {
				String[] strings= string.split("=");
				String[] chromsLengths= strings[1].split(",");
				for (int i = 0; i < chromsLengths.length; i++) {
					if (chromsLengths[i].contains("{")||chromsLengths[i].contains("}")) {
						chromsLengths[i] = chromsLengths[i].replace("{", "");
						chromsLengths[i] = chromsLengths[i].replace("}", "");
					}
					mapSpecies_chromLengths.put(strings[0].trim(), chromsLengths[i].trim());
				}
				
 			}
		}
//		System.out.println(mapSpecies_chromLengths.get("species_chrom_lengths"));
	}
	
	public static  void  modifyDataFile(Species species,String GenomeDataFile) {
		DateFormat d4 = DateFormat.getInstance();
		Date now = new Date();
		String dataAndTime = d4.format(now);
//		System.out.println(dataAndTime);
		readGenomeData(GenomeDataFile);
		String tmpstrVersion =  species.getVersion();
		String[] strsVersion = tmpstrVersion.split("_");
		String strVersion = strsVersion[0];
		System.out.println("map:" + mapSpeciesName_chroms.size() + " " + strVersion + "_chroms");
		if (!(mapSpeciesName_chroms.containsKey(strVersion + "_chroms"))) {
			for (String string : species.getMapChromInfo().keySet()) {
				/*
				 * chrY��hry�藉�瀹�				 */
				if (string.equals("chry") || string.equals("chrx") || string.equals("chrm")) {
					String string1 = string.substring(0, 3) + string.substring(3).toUpperCase();
					mapSpeciesName_chroms.put(strVersion + "_chroms", "'" + string1 + "'");
					mapSpeciesName_chromLengths.put(strVersion +"_chrom_lengths", "'" + string1 + "':" + species.getMapChromInfo().get(string).toString());
				}
				mapSpeciesName_chroms.put(strVersion + "_chroms", "'" + string + "'");
				mapSpeciesName_chromLengths.put(strVersion +"_chrom_lengths", "'" + string + "':" + species.getMapChromInfo().get(string).toString());
				
			}
			mapSpecies_chroms.put("species_chroms","'" + strVersion + "':" + strVersion +"_chroms");
			mapSpecies_chromLengths.put("species_chrom_lengths","'" + strVersion + "':" + strVersion +"_chrom_lengths");
			FileOperate.changeFileSuffixReal(GenomeDataFile,dataAndTime, null);
			TxtReadandWrite txtWrite = new TxtReadandWrite(GenomeDataFile, true);
			/*
			 * ��1琛�互�����疆
			 */
			for (String string : lsFirstLine) {
				txtWrite.writefileln(string);
			}
			txtWrite.writefileln();
			txtWrite.writefileln();
			/*
			 * ��peciesName_chroms ��			 * hg19_chroms = ['chr1','chr2','chr3','chr4']
			 */
			for (String string : mapSpeciesName_chroms.keySet()) {
				List<String> lsChroms = mapSpeciesName_chroms.get(string);
				String tmpString =string +" = [ " + lsChroms.get(0);
				for (int i = 1; i < lsChroms.size(); i++) {
					tmpString = tmpString + "," + lsChroms.get(i);
					}
				tmpString = tmpString + "]";
				txtWrite.writefileln(tmpString);
				txtWrite.writefileln();
			}
			txtWrite.writefileln();
			txtWrite.writefileln();
			/*
			 * 
			 * ��SpeciesName_chromLengths
			 * ��ombe_chrom_lengths = {'chr1':5580032,'chr2':4541604,'chr3':2453783,'mat':41249}
			 */
			for (String string : mapSpeciesName_chromLengths.keySet()) {
				List<String> lschromLengths = mapSpeciesName_chromLengths.get(string);
				String tmpString =string +" = { " + lschromLengths.get(0);
				for (int i = 1; i < lschromLengths.size(); i++) {
					tmpString = tmpString + "," + lschromLengths.get(i);
				}
				tmpString = tmpString + "}";
				txtWrite.writefileln(tmpString);
				txtWrite.writefileln();
			}
			txtWrite.writefileln();
			txtWrite.writefileln();
			
			/*
			 * ��pecies_chroms
			 * ��pecies_chroms = {'mm9':mm9_chroms, 'hg19':hg19_chroms,"dm2":dm2_chroms}
			 */
			for (String string : mapSpecies_chroms.keySet()) {
				List<String> lschroms = mapSpecies_chroms.get(string);
				String tmpString =string +" = { " + lschroms.get(0);
				for (int i = 1; i < lschroms.size(); i++) {
					tmpString = tmpString + "," + lschroms.get(i);
				}
				tmpString = tmpString + "}";
				txtWrite.writefileln(tmpString);
				txtWrite.writefileln();
			}
			txtWrite.writefileln();
			txtWrite.writefileln();
			
			/*
			 * ��pecies_chrom_lengths
			 * ��pecies_chrom_lengths={'mm8':mm8_chrom_lengths, 'mm9':mm9_chrom_lengths, 'hg19':hg19_chrom_lengths}
			 */
			for (String string : mapSpecies_chromLengths.keySet()) {
				List<String> lschromLengths = mapSpecies_chromLengths.get(string);
				String tmpString =string +" = { " + lschromLengths.get(0);
				for (int i = 1; i < lschromLengths.size(); i++) {
					tmpString = tmpString+ "," + lschromLengths.get(i);
				}
				tmpString = tmpString + "}";
				txtWrite.writefileln(tmpString);
				txtWrite.writefileln();
			}
			txtWrite.writefileln();
			txtWrite.writefileln();
			txtWrite.close();
		}
	}
}
