package com.novelbio.analysis.seq.chipseq.peakcalling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;


public class PeakCallingSicer {
	public static void main(String[] args) {
		System.out.println(		PeakCallingSicerType.SICERrb.toString());

	}

	private int windowSize = 200;
	private long fragmentSize = 200;
	private double effectiveGenomeSize;
	private int gapSize = 200;
	private String speciesString = "hg19";
	private String outputDir;
	private String InputDir;
	private String PathTo;

	private int redundancyThreshold = 2;
	private double FDR = 0.01;
	private int  Evalue = 100;
	private int koThreshold;
	private int wtThreshold;
	private String koBedFile;
	private String koControlFile;
	private String wtBedFile;
	private String wtControlFile;

	String exePath = "";
	/**
	 * �趨SICER��ִ��·��
	 * @param exePath
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.exePath = "";
		} else {
			this.exePath = FileOperate.addSep(exePath);
		}
	}
	public void setInputDir(String inputDir) {
		this.InputDir = inputDir;
	}
	
	public void setoutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	public String getOutDir() {
		return this.outputDir;
	}
	public void setSpecies(String species) {
		this.speciesString = species;
	}
	
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}
	
	public void setFragmentSize(long fragmentSize) {
		this.fragmentSize = fragmentSize;
	}
	/**
	 * ������Ŀ��1-100֮��
	 * ����֮������100���effective genome size�İٷֱ�
	 * @param effectiveGenomeFraction
	 */
	public void setEffectiveGenomeSize(int effectiveGenomeFraction) {
		this.effectiveGenomeSize = (double)effectiveGenomeFraction/100;
	}
	
	public void setGapSize(int gapSize) {
		this.gapSize = gapSize;
	}
	
	public void setKoBedFile(String koBedFile) {
		this.koBedFile = koBedFile;
	}
	
	public void setWtBedFile(String wtBedFile) {
		this.wtBedFile = wtBedFile;
	}
	
	public void setKoControlFile(String koControlFile) {
		this.koControlFile = koControlFile;
	}
	
	public void setWtControlFile(String wtControlFile) {
		this.wtControlFile = wtControlFile;
	}
	
	public void setKoThreshold(int koThreshold) {
		this.koThreshold = koThreshold;
	}
	
	public void setWtThreshold(int wtThreshold) {
		this.wtThreshold = wtThreshold;
	}
	
	public void setFDR(double fdr) {
		this.FDR = fdr;
	}
	
	public void setEvalue(int evalue) {
		Evalue = evalue;
	}
	public void setPathTo(String pathTo) {
		PathTo = pathTo;
	}
	
	private String getInputDir() {
		if (InputDir == null) {
			return "";
		}
		else {
			return " " + InputDir;
		}
	}
	
	private String getOutputDir() {
		if (outputDir ==null ) {
			return "";
		}
		else {
			return " " + outputDir;
		}
	}
	
	private String getSpecies() {
		return " " + speciesString;
	}
	
	private String getRedundancyThreshold() {
		return " " + redundancyThreshold;
	}
	
	private String getWindowSize() {
		return " " + windowSize;
	}
	
	private String getFragmentSize() {
		return " " + fragmentSize;
	}
	
	private String getEffectiveGenomeSize() {
		return " " + effectiveGenomeSize;
	}
	
	private String getGapSize() {
		return " " + gapSize;
	}
	
	private String getKoBedFile() {
		if (koBedFile == null ) {
			return "";
		}
		return " " + koBedFile;
	}
	
	private String getKoControlFile() {
		if (koControlFile == null) {
			return "";
		}
		return " " + koControlFile;
	}
	
	private String getKoThreshold() {
		return " " + koThreshold;
	}
	
	private String getWtBedFile() {
		if (wtBedFile == null) {
			return "";
		}
		return " " + wtBedFile;
	}
	
	private String getWtControlFile() {
		if (wtControlFile == null) {
			return "";
		}
		return " " + wtControlFile;
	}
	
	private String getWtThreshold() {
		return " " + wtThreshold;
	}
	
	
	private String getEvalue() {
		return " " + Evalue;
	}
	
	private String getFDR() {
		return " " + FDR;
	}
	
	private String getPathTo() {
		return " " + PathTo;
	}
	
	public String sicerCmd(PeakCallingSicerType sicerType) {
		String sicerPath = exePath + "SICER";
		String strsicerCmd = null;
		String tmpCmd = getInputDir() + getKoBedFile()  + getKoControlFile()  + getOutputDir() + getSpecies() + getRedundancyThreshold()
				+ getWindowSize() + getFragmentSize() + getEffectiveGenomeSize() + getGapSize() + getFDR() + getPathTo();
		String tmpCmdrb = getInputDir() + getKoBedFile() + getOutputDir()  + getSpecies() + getRedundancyThreshold()
				+ getWindowSize() + getFragmentSize() + getEffectiveGenomeSize() + getGapSize() + getEvalue() +getPathTo();
		String tmpCmd2 = getKoBedFile() + getKoControlFile() + getWtBedFile() + getWtControlFile() + getWindowSize() 
				+ getGapSize() + getEvalue() + getFDR() + getSpecies() + getEffectiveGenomeSize() + getFragmentSize() + getKoThreshold() 
				+ getWtThreshold() + getInputDir() + getOutputDir() + getPathTo();
		if (sicerType.equals(PeakCallingSicerType.SICER )){
			strsicerCmd = "sh " + sicerPath + "/SICER.sh " +tmpCmd;
		}
		if (sicerType.equals(PeakCallingSicerType.SICERrb)) {
			strsicerCmd = "sh "  + sicerPath + "/SICER-rb.sh " +tmpCmdrb;
		}
		if (sicerType.equals(PeakCallingSicerType.SICERdf)) {
			strsicerCmd = "sh " + sicerPath + "/SICER-df.sh" + tmpCmd2 ;
		}
		if (sicerType.equals(PeakCallingSicerType.SICERdfrb)) {
			strsicerCmd = "sh " + sicerPath + "/SICER-df-rb.sh" + tmpCmd2;
		}
		return strsicerCmd;
	}
	/**
	 * peakCalling��Ȼ�󷵻ؽ���ļ�
	 * @param sicerType
	 * @return
	 */
	public ArrayList<String> peakCallingAndGetResultFile(PeakCallingSicerType sicerType) {
		ArrayList<String> lsOutFile = new ArrayList<String>();;
		String cmd = sicerCmd(sicerType);
		CmdOperate cmdOperate = new CmdOperate(cmd, "macsPeakCalling");
		cmdOperate.run();
		if (sicerType == PeakCallingSicerType.SICERrb) {
			String resultPathAndFile = outputDir + koBedFile.split("\\.")[0] + "-W" + windowSize + "-G" + gapSize + "-E" + Evalue + ".scoreisland";
			
			lsOutFile.add(resultPathAndFile);
			return lsOutFile;
		}
		if (sicerType == PeakCallingSicerType.SICER) {
			String resultPathAndFile = outputDir + koBedFile.split("\\.")[0] + "-W" + windowSize + "-G" + gapSize + "-islands-summary";
			lsOutFile.add(resultPathAndFile);
			return lsOutFile;
		}
		if (sicerType == PeakCallingSicerType.SICERdfrb || sicerType == PeakCallingSicerType.SICERdf) {
			String resultPathAndFile1 = outputDir + koBedFile.split("\\.")[0]  + "-and-" + wtBedFile.split("\\.")[0] + "-W" + windowSize + "-G" + gapSize + "-summary";
			String resultPathAndFile2 = outputDir + koBedFile.split("\\.")[0] + "-W" + windowSize + "-G" + gapSize + "-decreased-islands-summary-FDR" +FDR;
			String resultPathAndFile3 = outputDir + koBedFile.split("\\.")[0] + "-W" + windowSize + "-G" + gapSize + "-increased-islands-summary-FDR" +FDR;
			lsOutFile.add(resultPathAndFile1);
			lsOutFile.add(resultPathAndFile2);
			lsOutFile.add(resultPathAndFile3);
			return lsOutFile;
		}
		return null;
	}
	
	
	public static  enum PeakCallingSicerType{
		SICER,SICERrb,SICERdf,SICERdfrb;
		static LinkedHashMap<String, PeakCallingSicerType> mapType = new LinkedHashMap<String, PeakCallingSicer.PeakCallingSicerType>();
		public static LinkedHashMap<String, PeakCallingSicerType> getMapType() {
			if (mapType.size() == 0) {
				mapType.put("A set of experiments without control", SICERrb);
				mapType.put("A set of experiments with control", SICER);
				mapType.put("Two set of experiments with control", SICERdf);
				mapType.put("Two set of experiments without control", SICERdfrb);
			}
			return mapType;
		}
	}
	
}
