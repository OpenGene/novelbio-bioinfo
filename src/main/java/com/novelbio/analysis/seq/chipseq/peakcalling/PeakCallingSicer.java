package com.novelbio.analysis.seq.chipseq.peakcalling;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;


public class PeakCallingSicer implements IntCmdSoft {
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
	//结尾不需要"/"
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
	PeakCallingSicerType sicerType;
	/**
	 * 后面自动加上"/"
	 */
	String exePath = "";
	
	public PeakCallingSicer() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.sicer);
		exePath = softWareInfo.getExePathRun();
		PathTo = FileOperate.getParentPathNameWithSep(exePath);
		PathTo = FileOperate.removeSep(PathTo);
	}
	
	public void setInputDir(String inputDir) {
		this.InputDir = inputDir;
	}
	public void setPeakCallingType(PeakCallingSicerType sicerType) {
		this.sicerType = sicerType;
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
	 * 输入项目在1-100之间
	 * 输入之后会除以100获得effective genome size的百分比
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
	
	private String getInputDir() {
		if (InputDir == null) {
			return null;
		}
		return InputDir;
	}
	
	private String getOutputDir() {
		if (outputDir == null ) {
			return null;
		}
		return outputDir;
	}
	
	private String getSpecies() {
		return speciesString;
	}
	
	private String getRedundancyThreshold() {
		return redundancyThreshold + "";
	}
	
	private String getWindowSize() {
		return windowSize + "";
	}
	
	private String getFragmentSize() {
		return fragmentSize + "";
	}
	
	private String getEffectiveGenomeSize() {
		return effectiveGenomeSize + "";
	}
	
	private String getGapSize() {
		return gapSize + "";
	}
	
	private String getKoBedFile() {
		if (koBedFile == null ) {
			return null;
		}
		return koBedFile;
	}
	
	private String getKoControlFile() {
		if (koControlFile == null) {
			return null;
		}
		return koControlFile;
	}
	
	private String getKoThreshold() {
		return koThreshold + "";
	}
	
	private String getWtBedFile() {
		if (wtBedFile == null) {
			return null;
		}
		return wtBedFile;
	}
	
	private String getWtControlFile() {
		if (wtControlFile == null) {
			return null;
		}
		return wtControlFile;
	}
	
	private String getWtThreshold() {
		return wtThreshold + "";
	}
	
	
	private String getEvalue() {
		return Evalue + "";
	}
	
	private String getFDR() {
		return FDR + "";
	}
	
	private String getPathTo() {
		return PathTo + "";
	}
	
	public String sicerCmd() {
		String strsicerCmd = null;
		String tmpCmd = getInputDir() + getKoBedFile()  + getKoControlFile()  + getOutputDir() + getSpecies() + getRedundancyThreshold()
				+ getWindowSize() + getFragmentSize() + getEffectiveGenomeSize() + getGapSize() + getFDR() + getPathTo();
		String tmpCmdrb = getInputDir() + getKoBedFile() + getOutputDir()  + getSpecies() + getRedundancyThreshold()
				+ getWindowSize() + getFragmentSize() + getEffectiveGenomeSize() + getGapSize() + getEvalue() +getPathTo();
		String tmpCmd2 = getKoBedFile() + getKoControlFile() + getWtBedFile() + getWtControlFile() + getWindowSize() 
				+ getGapSize() + getEvalue() + getFDR() + getSpecies() + getEffectiveGenomeSize() + getFragmentSize() + getKoThreshold() 
				+ getWtThreshold() + getInputDir() + getOutputDir() + getPathTo();
		if (sicerType.equals(PeakCallingSicerType.SICER )){
			strsicerCmd = "sh " + exePath + "SICER.sh " +tmpCmd;
		}
		if (sicerType.equals(PeakCallingSicerType.SICERrb)) {
			strsicerCmd = "sh "  + exePath + "SICER-rb.sh " +tmpCmdrb;
		}
		if (sicerType.equals(PeakCallingSicerType.SICERdf)) {
			strsicerCmd = "sh " + exePath + "SICER-df.sh" + tmpCmd2 ;
		}
		if (sicerType.equals(PeakCallingSicerType.SICERdfrb)) {
			strsicerCmd = "sh " + exePath + "SICER-df-rb.sh" + tmpCmd2;
		}
		return strsicerCmd;
	}
	
	private List<String> getLsCmd(PeakCallingSicerType sicerType) {
		List<String> lsCmd = null;
		if (sicerType.equals(PeakCallingSicerType.SICER )){
			lsCmd = getLsCmd();
			lsCmd.add(0, exePath + "SICER.sh");
		} else if (sicerType.equals(PeakCallingSicerType.SICERrb)) {
			lsCmd = getLsCmdrb();
			lsCmd.add(0, exePath + "SICER-rb.sh");
		} else if (sicerType.equals(PeakCallingSicerType.SICERdf)) {
			lsCmd = getLsCmd2();
			lsCmd.add(0, exePath + "SICER-df.sh");
		} else if (sicerType.equals(PeakCallingSicerType.SICERdfrb)) {
			lsCmd = getLsCmd2();
			lsCmd.add(0, exePath + "SICER-df-rb.sh");
		}
		lsCmd.add(0, "sh");
		return lsCmd;
	}
	
	private List<String> getLsCmd2() {
		List<String> lsCmd = new ArrayList<>();
		addListStr(lsCmd, getKoBedFile());
		addListStr(lsCmd, getKoControlFile());
		addListStr(lsCmd, getWtBedFile());
		addListStr(lsCmd, getWtControlFile());
		addListStr(lsCmd, getWindowSize());
		addListStr(lsCmd, getGapSize());
		addListStr(lsCmd, getEvalue());
		addListStr(lsCmd, getFDR());
		addListStr(lsCmd, getSpecies());
		addListStr(lsCmd, getEffectiveGenomeSize());
		addListStr(lsCmd, getFragmentSize());
		addListStr(lsCmd, getKoThreshold());
		addListStr(lsCmd, getWtThreshold());
		addListStr(lsCmd, getInputDir());
		addListStr(lsCmd, getOutDir());
		addListStr(lsCmd, getPathTo());
		return lsCmd;
	}
	
	private List<String> getLsCmdrb() {
		List<String> lsCmd = new ArrayList<>();
		addListStr(lsCmd, getInputDir());
		addListStr(lsCmd, getKoBedFile());
		addListStr(lsCmd, getOutputDir());
		addListStr(lsCmd, getSpecies());
		addListStr(lsCmd, getRedundancyThreshold());
		addListStr(lsCmd, getWindowSize());
		addListStr(lsCmd, getFragmentSize());
		addListStr(lsCmd, getEffectiveGenomeSize());
		addListStr(lsCmd, getGapSize());
		addListStr(lsCmd, getEvalue());
		addListStr(lsCmd, getPathTo());
		return lsCmd;
	}
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		addListStr(lsCmd, getInputDir());
		addListStr(lsCmd, getKoBedFile());
		addListStr(lsCmd, getKoControlFile());
		addListStr(lsCmd, getOutputDir());
		addListStr(lsCmd, getSpecies());
		addListStr(lsCmd, getRedundancyThreshold());
		addListStr(lsCmd, getWindowSize());
		addListStr(lsCmd, getFragmentSize());
		addListStr(lsCmd, getEffectiveGenomeSize());
		addListStr(lsCmd, getGapSize());
		addListStr(lsCmd, getFDR());
		addListStr(lsCmd, getPathTo());
		return lsCmd;
	}
	
	private void addListStr(List<String> lsCmd, String tmpCmd) {
		if(tmpCmd == null) return;
		lsCmd.add(tmpCmd);
	}
	/**
	 * peakCalling，然后返回结果文件
	 * @param sicerType
	 * @return
	 */
	public ArrayList<String> peakCallingAndGetResultFile() {
		ArrayList<String> lsOutFile = new ArrayList<String>();;
		List<String> lsCmd = getLsCmd(sicerType);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
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
	

	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmd = getLsCmd(sicerType);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsCmd.add(cmdOperate.getCmdExeStr());
		return lsCmd;
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
