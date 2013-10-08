package com.novelbio.analysis.seq.mapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

@Component
@Scope("prototype")
public class MapBowtie extends MapDNA implements IntCmdSoft {
	public static final int Sensitive_Very_Fast = 11;
	public static final int Sensitive_Fast = 12;
	public static final int Sensitive_Sensitive = 13;
	public static final int Sensitive_Very_Sensitive = 14;
	
	/** 默认bowtie2 */
	SoftWare bowtieVersion = SoftWare.bowtie2;
	/** 待比对的染色体 */
	String chrFile = "";
	/** bowtie所在路径 */
	String ExePathBowtie = "";
	
	List<FastQ> lsLeftFq = new ArrayList<FastQ>();
	List<FastQ> lsRightFq = new ArrayList<FastQ>();
	
	List<String> lsSampleGroup = null;
	/** 非unique mapping的话，取几个 */
	int mappingNum = 0;
	
	/** 插入片段 pairend是500， mate pair就要很大了 */
	int insertMax = 500;
	
	int threadNum = 5;
	
	/** 一条reads最多比对到8个不同的位置上去 */
	int maxMultipHit = 4;
	
	int sensitive = Sensitive_Sensitive;
	/**
	 * pe -fr
	 * mp -rf
	 */
	MapLibrary mapLibrary = MapLibrary.PairEnd;
	
	/**
	 * 设定tophat所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 * @param chrFile
	 */
	public void setExePath(String exePathBowtie) {
		if (exePathBowtie == null || exePathBowtie.trim().equals(""))
			this.ExePathBowtie = "";
		else
			this.ExePathBowtie = FileOperate.addSep(exePathBowtie);
	}
	public void setExePathBowtie(String exePathBowtie) {
		ExePathBowtie = exePathBowtie;
	}
	public void setChrFile(String chrFile) {
		this.chrFile = chrFile;
	}
	/** 设定是bowtie还是bowtie2 */
	public void setSubVersion(SoftWare bowtieVersion) {
		this.bowtieVersion = bowtieVersion;
	}

	public void setSensitive(int sensitive) {
		this.sensitive = sensitive;
	}
	
	private String getChrFile() {
		return chrFile;
	}
	/** 获得没有后缀名的序列，不带引号 */
	protected String getChrNameWithoutSuffix() {
		String chrFileName = FileOperate.getParentPathName(chrFile) + FileOperate.getFileNameSep(chrFile)[0];
		return chrFileName;
	}
	public void setMapLibrary(MapLibrary mapLibrary) {
		this.mapLibrary = mapLibrary;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	
	public void setFqFile(FastQ leftFq, FastQ rightFq) {
		this.lsLeftFq.clear();
		this.lsRightFq.clear();
		if (leftFq != null) {
			lsLeftFq.add(leftFq);
		}
		if (rightFq != null) {
			lsRightFq.add(rightFq);
		}
	}
	/**
	 * 设置左端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setLeftFq(List<FastQ> lsLeftFastQs) {
		this.lsLeftFq = lsLeftFastQs;
	}
	/**
	 * 设置右端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setRightFq(List<FastQ> lsRightFastQs) {
		this.lsRightFq = lsRightFastQs;
	}
	
	/**
	 * 返回输入的文件，根据是否为pairend，调整返回的结果
	 * @return
	 */
	private List<String> getLsFqFile() {
		List<String> lsCmd = new ArrayList<>();
		String lsFileName = lsLeftFq.get(0).getReadFileName();
		for (int i = 1; i < lsLeftFq.size(); i++) {
			lsFileName = lsFileName + "," + lsLeftFq.get(i).getReadFileName();
		}
		if (!isPairEnd()) {
			lsCmd.add("-U");
			lsCmd.add(lsFileName);
		} else {
			lsCmd.add("-1");
			lsCmd.add(lsFileName);
			lsCmd.add("-2");
			String lsFileName2 = lsRightFq.get(0).getReadFileName();
			for (int i = 1; i < lsLeftFq.size(); i++) {
				lsFileName2 = lsFileName2 + "," + lsRightFq.get(i).getReadFileName();
			}
			lsCmd.add(lsFileName2);
		}
		return lsCmd;
	}
	
	private String[] getOutFileName() {
		outFileName = addSamToFileName(outFileName);
		if (outFileName.equals("")) {
			outFileName = FileOperate.changeFileSuffix(lsLeftFq.get(0).getReadFileName(), "_result", "sam");
		}
		String outName = MapBwa.addSamToFileName(outFileName);
		return new String[]{"-S", outName};
	}

	private String getOffset() {
		if (lsLeftFq.get(0).getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return "--phred64";
		}
		return "--phred33";
	}
	/** 非unique mapping，最多可以比对到多少地方上去，设定为10比较合适把 */
	private String[] getMappingNum() {
		if (mappingNum <= 0) {
			return null;
		}
		return new String[]{"-k", mappingNum + ""};
	}
	
	private String getMapLibrary() {
		if (!isPairEnd()) {
			return null;
		} else if (mapLibrary == MapLibrary.SingleEnd || mapLibrary == MapLibrary.PairEnd) {
			return "--fr";
		} else if (mapLibrary == MapLibrary.MatePair) {
			return "--rf";
		}
		return null;
	}
	
	private String[] getInsertSize() {
		if (isPairEnd()) {
			if (mapLibrary == MapLibrary.SingleEnd || mapLibrary == MapLibrary.PairEnd) {
				insertMax = 500;
			} else if (mapLibrary == MapLibrary.MatePair) {
				insertMax = 10000;
			} else if (mapLibrary == MapLibrary.MatePairLong) {
				insertMax = 25000;
			}
			return new String[]{"-X", insertMax + ""};
		}
		return null;
	}
	
	private String[] getMultiHit() {
		return new String[]{"-k", maxMultipHit + ""};
	}
	
	/**
	 * 本次mapping的组，所有参数都不能有空格
	 * @param sampleID 
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform
	 */
	public void setSampleGroup(String sampleID, String LibraryName, String SampleName, String Platform) {
		if (sampleID == null || sampleID.equals("")) {
			return;
		}
		lsSampleGroup = new ArrayList<>();
		lsSampleGroup.add("--rg-id"); lsSampleGroup.add(sampleID);
		if (SampleName != null && !SampleName.trim().equals("")) {
			lsSampleGroup.add("--rg");
			lsSampleGroup.add("SM:" + SampleName.trim());
		} else {
			lsSampleGroup.add("--rg");
			lsSampleGroup.add("SM:" + sampleID.trim());
		}
		if (LibraryName != null && !LibraryName.trim().equals("")) {
			lsSampleGroup.add("--rg");
			lsSampleGroup.add("LB:" + LibraryName.trim());
		}
		
		if (Platform != null && !Platform.trim().equals("")) {
			lsSampleGroup.add("--rg");
			lsSampleGroup.add("PL:" + Platform);
		} else {
			lsSampleGroup.add("--rg");
			if (mapLibrary == MapLibrary.MatePair) {
				lsSampleGroup.add("PL:IonProton");
			} else {
				lsSampleGroup.add("PL:Illumina");
			}
		}
	}
	
	private List<String> getSampleGroup() {
		return lsSampleGroup;
	}
	private String[] getThreadNum() {
		return new String[]{"-p", threadNum + ""};
	}
	
	private boolean isPairEnd() {
		if (lsLeftFq.size() == 0|| lsRightFq.size() == 0) {
			return false;
		}
		return true;
	}
	
	/**
	 * 制作索引
	 * 这个暴露出来是给MirDeep用的
	 * @param forceMakeIndex 强制建立索引
	 * @return true：表示运行了建索引程序，不代表成功建立了索引
	 */
	public boolean IndexMake(boolean forceMakeIndex) {
//		linux命令如下 
//	 	bwa index -p prefix -a algoType -c  chrFile
//		-c 是solid用
		if (!forceMakeIndex && bowtieVersion == SoftWare.bowtie) {
			if (FileOperate.isFileExist(getChrNameWithoutSuffix() + ".3.ebwt") == true)
				return false;
		}
		else if (!forceMakeIndex && bowtieVersion == SoftWare.bowtie2) {
			if (FileOperate.isFileExist(getChrNameWithoutSuffix() + ".3.bt2") == true)
				return false;
		}

		List<String> lsCmd = getLsCmdIndex();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
		return true;
	}
	
	private List<String> getLsCmdIndex() {
		List<String> lsCmd = new ArrayList<>();
		SoftWareInfo softWareInfo = new SoftWareInfo(bowtieVersion);
		if (bowtieVersion == SoftWare.bowtie) {
			lsCmd.add(softWareInfo.getExePath() + "bowtie-build");
		} else if (bowtieVersion == SoftWare.bowtie2) {
			lsCmd.add(softWareInfo.getExePath() + "bowtie2-build");
		}
		lsCmd.add(getChrFile());
		lsCmd.add(getChrNameWithoutSuffix());
		return lsCmd;
	}
	
	protected SamFile mapping() {
		List<String> lsCmd = getLsCmdMapping();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetCmdInStdStream(true);
		Thread thread = new Thread(cmdOperate);
		thread.start();
		InputStream inputStream = cmdOperate.getStdStream();
		SamFile samResult = copeSamStream(inputStream, isNeedSort);
		if (samResult != null && !cmdOperate.isRunning() && cmdOperate.isFinishedNormal()) {
			return samResult;
		} else {
			deleteFailFile();
			return null;
		}
	}
	
	/** 目前只能做bowtie2的mapping */
	private List<String> getLsCmdMapping() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePathBowtie + "bowtie2");
		lsCmd.add("--local");
		if (sensitive == Sensitive_Very_Fast) {
			lsCmd.add("--very-fast-local");
		} else if (sensitive == Sensitive_Fast) {
			lsCmd.add("--fast-local");
		} else if (sensitive == Sensitive_Sensitive) {
			lsCmd.add("--sensitive-local");
		} else if (sensitive == Sensitive_Very_Sensitive) {
			lsCmd.add("--very-sensitive-local");
		} else {
			lsCmd.add("--sensitive-local");
		}		
		lsCmd.add(getOffset());
		ArrayOperate.addArrayToList(lsCmd, getMappingNum());
		addListStr(lsCmd, getMapLibrary());
		lsCmd.addAll(getSampleGroup());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getInsertSize());
		ArrayOperate.addArrayToList(lsCmd, getMultiHit());
		lsCmd.add("-x"); lsCmd.add(getChrNameWithoutSuffix());
		lsCmd.addAll(getLsFqFile());
		ArrayOperate.addArrayToList(lsCmd, getOutFileName());
		return lsCmd;
	}
	
	private void addListStr(List<String> lsCmd, String param) {
		if (param == null) return;
		lsCmd.add(param);
	}
	
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmd = getLsCmdMapping();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsCmd.add(cmdOperate.getCmdExeStr());
		return lsCmd;
	}
	/**
	 * 将sam文件压缩成bam文件，然后做好统计并返回
	 * @param outSamFile
	 * @return
	 */
	@Override
	protected SamFile copeAfterMapping() {
		if (!FileOperate.isFileExistAndBigThanSize(outFileName, 1)) {
			return null;
		}
		SamFile samFile = new SamFile(outFileName);
		SamFile bamFile = samFile.convertToBam(lsAlignmentRecorders, true);
		samFile.close();
		deleteFile(samFile.getFileName(), bamFile.getFileName());
		return bamFile;
	}
	
	/**
	 * 删除sai文件
	 * @param samFileName
	 */
	private void deleteFile(String samFile, String bamFile) {
		double samFileSize = FileOperate.getFileSize(samFile);
		if (FileOperate.isFileExistAndBigThanSize(bamFile, samFileSize/15)) {
			FileOperate.delFile(samFile);
		}
	}
	
	/** 没用 */
	public void setMismatch(double mismatch) { }

	/** 没用 */
	public void setGapLength(int gapLength) {}
	
	public String getVersion() {
		String bowtie = "";
		if (bowtieVersion == SoftWare.bowtie) {
			bowtie = "bowtie";
		} else if (bowtieVersion == SoftWare.bowtie2) {
			bowtie = "bowtie2";
		}
		CmdOperate cmdOperate = new CmdOperate(this.ExePathBowtie + bowtie + " --version");
		cmdOperate.setGetLsErrOut();
		
		List<String> lsInfo = cmdOperate.getLsStdOut();
		String version = lsInfo.get(0).toLowerCase().split("version")[1].trim();
		return version;
	}
	
	
	public static Map<String, Integer> getMapSensitive() {
		Map<String, Integer> mapSensitive = new LinkedHashMap<>();
		mapSensitive.put("Sensitive", Sensitive_Sensitive);
		mapSensitive.put("Very Sensitive", Sensitive_Very_Sensitive);
		mapSensitive.put("Fast", Sensitive_Fast);
		mapSensitive.put("VeryFast", Sensitive_Very_Fast);
		return mapSensitive;
	}


}
