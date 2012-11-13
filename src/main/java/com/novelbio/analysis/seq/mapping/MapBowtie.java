package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMReadGroupRecord;

import com.novelbio.analysis.annotation.functiontest.TopGO.GoAlgorithm;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

public class MapBowtie extends MapDNA {
	/** 默认bowtie2 */
	SoftWare bowtieVersion = SoftWare.bowtie2;
	/** 待比对的染色体 */
	String chrFile = "";
	/** bowtie所在路径 */
	String ExePathBowtie = "";
	
	List<FastQ> lsLeftFq = new ArrayList<FastQ>();
	List<FastQ> lsRightFq = new ArrayList<FastQ>();
	
	String outFileName = "";
	String sampleGroup = "";
	/** 非unique mapping的话，取几个 */
	int mappingNum = 0;
	
	/** 插入片段 pairend是500， mate pair就要很大了 */
	int insertMax = 500;
	
	int threadNum = 4;
	/**
	 * pe -fr
	 * mp -rf
	 */
	MapLibrary mapLibrary = MapLibrary.PairEnd;
	
	public MapBowtie() {
		// TODO Auto-generated constructor stub
	}
	/** mapping功能只能由bowtie2实现 */
	public MapBowtie(SoftWare bowtieVersion) {
		setBowtieVersion(bowtieVersion);
	}
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
	public void setBowtieVersion(SoftWare bowtieVersion) {
		this.bowtieVersion = bowtieVersion;
	}
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}
	/** 获得没有后缀名的序列 */
	private String getChrNameWithoutSuffix() {
		String chrFileName = FileOperate.getParentPathName(chrFile) + FileOperate.getFileNameSep(chrFile)[0];
		return chrFileName;
	}
	private String getChrFile() {
		return chrFile;
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
	private String getLsFqFile() {
		String lsFileName = CmdOperate.addQuot(lsLeftFq.get(0).getReadFileName());
		for (int i = 1; i < lsLeftFq.size(); i++) {
			lsFileName = lsFileName + "," + CmdOperate.addQuot(lsLeftFq.get(i).getReadFileName());
		}
		if (isPairEnd()) {
			lsFileName = lsFileName + " -2 " + CmdOperate.addQuot(lsRightFq.get(0).getReadFileName());
			for (int i = 1; i < lsRightFq.size(); i++) {
				lsFileName = lsFileName + "," + CmdOperate.addQuot(lsRightFq.get(i).getReadFileName());
			}
			lsFileName = " -1 " + lsFileName;
		} else {
			lsFileName = " -U " + lsFileName;
		}
		return lsFileName + " ";
	}
	
	private String getOutFileName() {
		if (outFileName.equals("")) {
			outFileName = FileOperate.changeFileSuffix(lsLeftFq.get(0).getReadFileName(), "_result", "sam");
		}
		String outName = MapBwa.addSamToFileName(outFileName);
		return " -S " + outName;
	}
	
	private String getOffset() {
		if (lsLeftFq.get(0).getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return " --phred64 ";
		}
		return " --phred33 ";
	}
	/** 非unique mapping，最多可以比对到多少地方上去，设定为10比较合适把 */
	private String getMappingNum() {
		if (mappingNum <= 0) {
			return "";
		}
		return " -k " + mappingNum + " ";
	}
	
	private String getMapLibrary() {
		if (isPairEnd()) {
			return "";
		} else if (mapLibrary == MapLibrary.SingleEnd || mapLibrary == MapLibrary.PairEnd) {
			return " --fr ";
		} else if (mapLibrary == MapLibrary.MatePair) {
			return " --rf ";
		}
		return "";
	}
	
	private String getInsertSize() {
		if (isPairEnd()) {
			if (mapLibrary == MapLibrary.SingleEnd || mapLibrary == MapLibrary.PairEnd) {
				insertMax = 500;
			} else if (mapLibrary == MapLibrary.MatePair) {
				insertMax = 10000;
			} else if (mapLibrary == MapLibrary.MatePairLong) {
				insertMax = 25000;
			}
			return " -X " + insertMax + " ";
		}
		return "";
	}
	
	/**
	 * 本次mapping的组，所有参数都不能有空格
	 * @param sampleID 
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform
	 */
	public void setSampleGroup(String sampleID, String LibraryName, String SampleName, String Platform) {
		sampleGroup = "";
		if (sampleID == null || sampleID.equals("")) {
			return;
		}
		this.sampleGroup = " --rg-id " +  sampleID + " ";
		
		ArrayList<String> lsSampleDetail = new ArrayList<String>();

		if (SampleName != null && !SampleName.trim().equals("")) {
			lsSampleDetail.add("SM:" + SampleName.trim());
		} else {
			lsSampleDetail.add("SM:" + sampleID.trim());
		}
		
		if (LibraryName != null && !LibraryName.trim().equals("")) {
			lsSampleDetail.add("LB:" + LibraryName.trim());
		}
		
		if (Platform != null && !Platform.trim().equals("")) {
			lsSampleDetail.add("PL:" + Platform);
		} else {
			if (mapLibrary == MapLibrary.MatePair) {
				lsSampleDetail.add("PL:IonProton");
			} else {
				lsSampleDetail.add("PL:Illumina");
			}
		}
		if (lsSampleDetail.size() == 0) {
			return;
		}
		
		for (String string : lsSampleDetail) {
			sampleGroup = sampleGroup + " --rg " + string + " ";
		}
	}
	
	private String getSampleGroup() {
		return sampleGroup;
	}
	private String getThreadNum() {
		return " -p " + threadNum + " ";
	}
	private String getOptions() {
		String options = " --local --sensitive-local";
		options = options + getOffset() + getMappingNum() + getMapLibrary() + getSampleGroup() + getThreadNum() + getInsertSize();
		return options;
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
	 */
	public void IndexMakeBowtie() {
		SoftWareInfo softWareInfo = new SoftWareInfo();
//		linux命令如下 
//	 	bwa index -p prefix -a algoType -c  chrFile
//		-c 是solid用
		if (bowtieVersion == SoftWare.bowtie) {
			if (FileOperate.isFileExist(getChrNameWithoutSuffix() + ".3.ebwt") == true)
				return;
		}
		else if (bowtieVersion == SoftWare.bowtie2) {
			if (FileOperate.isFileExist(getChrNameWithoutSuffix() + ".3.bt2") == true)
				return;
		}

		String cmd = "";
		softWareInfo.setName(bowtieVersion);
		
		if (bowtieVersion == SoftWare.bowtie) {
			cmd = softWareInfo.getExePath() + "bowtie-build ";
		}
		else if (bowtieVersion == SoftWare.bowtie2) {
			cmd = softWareInfo.getExePath() + "bowtie2-build ";
		}
		
		cmd = cmd + CmdOperate.addQuot(getChrFile()) + " " + CmdOperate.addQuot(getChrNameWithoutSuffix());
		CmdOperate cmdOperate = new CmdOperate(cmd, "bwaMakeIndex");
		cmdOperate.run();
	}
	
	public SamFile mapReads() {
		outFileName = MapBwa.addSamToFileName(outFileName);
		IndexMakeBowtie();

		String cmd = ""; cmd = ExePathBowtie + "bowtie2 ";
		cmd = cmd + getOptions() + " -x " + getChrNameWithoutSuffix() + getLsFqFile() + getOutFileName();
		CmdOperate cmdOperate = new CmdOperate(cmd, "bwaMapping2");
		cmdOperate.run();
		SamFile samFile = new SamFile(outFileName);
		return samFile;
	}
	
	/** 没用 */
	public void setMismatch(double mismatch) { }

	/** 没用 */
	public void setGapLength(int gapLength) {}
}
