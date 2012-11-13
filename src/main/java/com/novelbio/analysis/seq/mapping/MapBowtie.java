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
	/** Ĭ��bowtie2 */
	SoftWare bowtieVersion = SoftWare.bowtie2;
	/** ���ȶԵ�Ⱦɫ�� */
	String chrFile = "";
	/** bowtie����·�� */
	String ExePathBowtie = "";
	
	List<FastQ> lsLeftFq = new ArrayList<FastQ>();
	List<FastQ> lsRightFq = new ArrayList<FastQ>();
	
	String outFileName = "";
	String sampleGroup = "";
	/** ��unique mapping�Ļ���ȡ���� */
	int mappingNum = 0;
	
	/** ����Ƭ�� pairend��500�� mate pair��Ҫ�ܴ��� */
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
	/** mapping����ֻ����bowtie2ʵ�� */
	public MapBowtie(SoftWare bowtieVersion) {
		setBowtieVersion(bowtieVersion);
	}
	/**
	 * �趨tophat���ڵ��ļ����Լ����ȶԵ�·��
	 * @param exePath ����ڸ�Ŀ¼��������Ϊ""��null
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
	/** �趨��bowtie����bowtie2 */
	public void setBowtieVersion(SoftWare bowtieVersion) {
		this.bowtieVersion = bowtieVersion;
	}
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}
	/** ���û�к�׺�������� */
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
	 * ������˵����У����û����ǰ�����
	 * @param fqFile
	 */
	public void setLeftFq(List<FastQ> lsLeftFastQs) {
		this.lsLeftFq = lsLeftFastQs;
	}
	/**
	 * �����Ҷ˵����У����û����ǰ�����
	 * @param fqFile
	 */
	public void setRightFq(List<FastQ> lsRightFastQs) {
		this.lsRightFq = lsRightFastQs;
	}
	
	/**
	 * ����������ļ��������Ƿ�Ϊpairend���������صĽ��
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
	/** ��unique mapping�������ԱȶԵ����ٵط���ȥ���趨Ϊ10�ȽϺ��ʰ� */
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
	 * ����mapping���飬���в����������пո�
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
	 * ��������
	 * �����¶�����Ǹ�MirDeep�õ�
	 */
	public void IndexMakeBowtie() {
		SoftWareInfo softWareInfo = new SoftWareInfo();
//		linux�������� 
//	 	bwa index -p prefix -a algoType -c  chrFile
//		-c ��solid��
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
	
	/** û�� */
	public void setMismatch(double mismatch) { }

	/** û�� */
	public void setGapLength(int gapLength) {}
}
