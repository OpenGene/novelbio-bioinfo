package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.mapping.MapIndexMaker.IndexMapSplice;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

public class MapSplice implements MapRNA {
	/** mapping文件的后缀，包含 ".bam" 字符串 */
	public static final String MapSpliceSuffix = "_mapsplice.bam";
	/** mapsplice mapping完后又用bowtie2 mapping的后缀，包含 ".bam" 字符串 */
	public static final String MapSpliceAllSuffix = "_mapspliceAll.bam";
	
	String exePath = "";
	/** bowtie就是用来做索引的 */
	IndexMapSplice indexMaker;
	
	String outFile;
	int indelLen = 6;
	int threadNum = 10;
	//输入的fastq
	List<FastQ> lsLeftFq = new ArrayList<FastQ>();
	List<FastQ> lsRightFq = new ArrayList<FastQ>();
	//将输入的fastq.gz转换成常规fastq
	List<FastQ> lsLeftRun = new ArrayList<>();
	List<FastQ> lsRightRun = new ArrayList<>();
	//转换的文件放在这个里面，最后要被删掉
	List<String> lsTmp = new ArrayList<>();
	boolean isPrepare = false;
	int mismatch = 3;
	boolean fusion = false;
	String gtfFile;
	int seedLen = 22;
	
	/** 将没有mapping上的reads用bowtie2比对到基因组上，仅用于proton数据 */
	boolean mapUnmapedReads = false;
	/** 比对到的index */
	String dnaIndex;
	
	/** 第二次mapping所使用的命令 */
	List<String> lsCmdMapping2nd = new ArrayList<>();
	
	public MapSplice(GffChrAbs gffChrAbs) {
		SoftWareInfo softMapSplice = new SoftWareInfo();
		softMapSplice.setName(SoftWare.mapsplice);
		this.exePath = softMapSplice.getExePathRun();
		indexMaker = (IndexMapSplice)MapIndexMaker.createIndexMaker(SoftWare.mapsplice);
		if (gffChrAbs != null && gffChrAbs.getGffHashGene() != null) {
			indexMaker.setGffHashGene(gffChrAbs.getGffHashGene());
		}
	}
	
	/**
	 * 是否将没有mapping上的reads用bowtie2比对到基因组上，<b>注意目前仅用于proton数据</b>
	 * @param mapUnmapedReads
	 * @param bowtie2ChrIndex
	 */
	public void setMapUnmapedReads(boolean mapUnmapedReads, String bwaIndex) {
		this.mapUnmapedReads = mapUnmapedReads;
		if (mapUnmapedReads) {
			this.dnaIndex = bwaIndex;
		}
	}
	
	/** 是否检测fusion gene，检测fusion gene会获得比较少的junction reads */
	public void setFusion(boolean fusion) {
		this.fusion = fusion;
	}
	/** 这个输入的应该是一个包含分割Chr文件的文件夹 */
	@Override
	public void setRefIndex(String chrFile) {
		indexMaker.setChrIndex(chrFile);
	}
	
	@Override
	public void setGtf_Gene2Iso(String gtfFile) {
		this.gtfFile = gtfFile;
	}
	@Override
	public void setOutPathPrefix(String outPathPrefix) {
		this.outFile = outPathPrefix;
	}

	/** indel长度默认为6 */
	@Override
	public void setIndelLen(int indelLen) {
		this.indelLen = indelLen;
	}

	@Override
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	
	@Deprecated
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype) {
	}

	@Deprecated
	public void setInsert(int insert) {
	}

	/**
	 * 设置左端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setLeftFq(List<FastQ> lsLeftFastQs) {
		if (lsLeftFastQs == null) return;
		this.lsLeftFq = lsLeftFastQs;
		isPrepare = false;
	}
	/**
	 * 设置右端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setRightFq(List<FastQ> lsRightFastQs) {
		if (lsRightFastQs == null) return;
		this.lsRightFq = lsRightFastQs;
		isPrepare = false;
	}
	
	@Override
	public void setMismatch(int mismatch) {
		this.mismatch = mismatch;
	}

	@Override
	public void mapReads() {
		prepareReads();
		indexMaker.IndexMake();
		lsCmdMapping2nd.clear();

		String prefix = FileOperate.getFileName(outFile);
		String parentPath = FileOperate.getParentPathNameWithSep(outFile);
		String mapSpliceBam = parentPath + prefix + MapSpliceSuffix;
		if (!FileOperate.isFileExistAndBigThanSize(mapSpliceBam, 1_000_000)) {
			CmdOperate cmdOperate = new CmdOperate(getLsCmd());
			
			cmdOperate.setRedirectInToTmp(true);
			for (FastQ fqL : lsLeftRun) {
				cmdOperate.addCmdParamInput(fqL.getReadFileName());
			}
			for (FastQ fqR : lsRightRun) {
				cmdOperate.addCmdParamInput(fqR.getReadFileName());
			}
			
			cmdOperate.setRedirectOutToTmp(true);
			cmdOperate.addCmdParamOutput(outFile, false);
			
			cmdOperate.run();
			if (!cmdOperate.isFinishedNormal()) {
				FileOperate.DeleteFileFolder(FileOperate.addSep(outFile) + "tmp");
				throw new ExceptionCmd("error running mapsplice:" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
			}
			clearTmpReads_And_MoveFile();
		}
		
		if (mapUnmapedReads) {
			String finalBam = parentPath + prefix + MapSpliceAllSuffix;
			lsCmdMapping2nd = MapTophat.mapUnmapedReads(threadNum, dnaIndex, mapSpliceBam, null, finalBam);
		}
		
	}
	
	private void prepareReads() {
		if (isPrepare) {
			return;
		}
		isPrepare = true;
		lsLeftRun.clear();
		lsRightRun.clear();
		lsTmp.clear();
		for (FastQ fastQ : lsLeftFq) {
			if (fastQ.getReadFileName().endsWith("gz")) {
				fastQ = deCompressFq(fastQ);
				lsTmp.add(fastQ.getReadFileName());
			}
			lsLeftRun.add(fastQ);
		}
		for (FastQ fastQ : lsRightFq) {
			if (fastQ.getReadFileName().endsWith("gz")) {
				fastQ = deCompressFq(fastQ);
				lsTmp.add(fastQ.getReadFileName());
			}
			lsRightRun.add(fastQ);
		}
	}
	
	private void clearTmpReads_And_MoveFile() {
		for (String fastQname : lsTmp) {
			FileOperate.delFile(fastQname);
		}
		
		if (outFile.endsWith("/") || outFile.endsWith("\\")) {
			return;
		}
		String prefix = FileOperate.getFileName(outFile);
		String parentPath = FileOperate.getParentPathNameWithSep(outFile);
		FileOperate.moveFile(FileOperate.addSep(outFile) + "alignments.bam", parentPath, prefix + MapSpliceSuffix,false);
		FileOperate.moveFile(FileOperate.addSep(outFile) + "junctions.txt", parentPath, prefix + "_junctions.txt",false);
		FileOperate.DeleteFileFolder(FileOperate.addSep(outFile) + "tmp");
	}
	
	@Override
	public String getFinishName() {
		String prefix = FileOperate.getFileName(outFile);
		String parentPath = FileOperate.getParentPathNameWithSep(outFile);
		if (!mapUnmapedReads) {
			return parentPath + prefix + MapSpliceSuffix;
		} else {
			return parentPath + prefix + MapSpliceAllSuffix;
		}
	}
	
	private FastQ deCompressFq(FastQ fastQ) {
		String fileName = fastQ.getReadFileName();
		fileName = fileName.replace("fastq.gz", "fastq").replace("fq.gz", "fastq");
		String newFastqName = FileOperate.changeFileSuffix(fileName, "_decompress", null);
		if (FileOperate.isFileExistAndBigThanSize(newFastqName, 0)) {
			return new FastQ(newFastqName);
		}
		String fastqTmp = FileOperate.changeFileSuffix(newFastqName, "_tmp", null);
		FastQ fastQdecompress = new FastQ(fastqTmp, true);
		for (FastQRecord fastQRecord : fastQ.readlines()) {
			fastQdecompress.writeFastQRecord(fastQRecord);
		}
		fastQ.close();
		fastQdecompress.close();
		FileOperate.changeFileName(fastqTmp, FileOperate.getFileName(newFastqName), true);
		return new FastQ(newFastqName);
	}
	
	private String[] getRefseq() {
		String fileRefSep = indexMaker.getChrSepFolder();
		return new String[]{"-c", fileRefSep};
	}
	private String[] getIndex() {
		return new String[]{"-x", indexMaker.getIndexName()};
	}
	private String[] getThreadNum() {
		return new String[]{"-p", threadNum + ""};
	}
	private String[] getGtfFile() {
		if (gtfFile != null) {
			return new String[]{"--gene-gtf", gtfFile};
		}
		return null;
	}
	private String[] getOutPath() {
		return new String[]{"-o", outFile};
	}
	private String[] getSeedLen() {
		return new String[]{"-s", seedLen + ""};
	}
	private List<String> getIndelLen() {
		List<String> lsIndel = new ArrayList<>();
		lsIndel.add("--ins");
		lsIndel.add(6+"");
		lsIndel.add("--del");
		lsIndel.add(6+"");
		return lsIndel;
	}
	private List<String> getFqFile() {
		List<String> lsFqFileInfo = new ArrayList<>();
		lsFqFileInfo.add("-1");
		String fqLeft = lsLeftRun.get(0).getReadFileName();
		for (int i = 1; i < lsLeftRun.size(); i++) {
			fqLeft = fqLeft + "," + lsLeftRun.get(i).getReadFileName();
		}
		lsFqFileInfo.add(fqLeft);
		if (lsRightRun.isEmpty())
			return lsFqFileInfo;
		
		lsFqFileInfo.add("-2");
		String fqRight = lsRightRun.get(0).getReadFileName();
		for (int i = 1; i < lsRightRun.size(); i++) {
			fqRight = fqRight + "," + lsRightRun.get(i).getReadFileName();
		}
		lsFqFileInfo.add(fqRight);
		return lsFqFileInfo;
	}
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("python");
		lsCmd.add(exePath + "mapsplice.py");
		ArrayOperate.addArrayToList(lsCmd, getRefseq());
		ArrayOperate.addArrayToList(lsCmd, getIndex());
		lsCmd.addAll(getFqFile());
		ArrayOperate.addArrayToList(lsCmd, getSeedLen());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		lsCmd.addAll(getIndelLen());
		lsCmd.add("--non-canonical");
		if (fusion) {
			lsCmd.add("--fusion");
		}
		lsCmd.add("--bam");
		ArrayOperate.addArrayToList(lsCmd, getOutPath());
		return lsCmd;
	}
	
	
	
	@Override
	public SoftWare getSoftWare() {
		return SoftWare.bowtie;
	}
	
	public String getVersionMapSplice() {
		List<String> lsCmdVersion = new ArrayList<>();
		lsCmdVersion.add("python");
		lsCmdVersion.add(exePath + "mapsplice.py");
		lsCmdVersion.add("--version");
		CmdOperate cmdOperate = new CmdOperate(lsCmdVersion);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.run();
		List<String> lsInfo = cmdOperate.getLsErrOut();
		String[] ss = lsInfo.get(0).trim().split(" ");
		String version = ss[ss.length-1];
		return version;
	}
	
	@Override
	public List<String> getCmdExeStr() {
		prepareReads();
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("MapSplice version: " + getVersionMapSplice());
		lsCmd.add(getSoftWare().toString() + " version: " + indexMaker.getVersion());
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		lsCmd.add(cmdOperate.getCmdExeStr());
		if (!lsCmdMapping2nd.isEmpty()) {
			lsCmd.addAll(lsCmdMapping2nd);
		}
		return lsCmd;
	}
	
	public void clear() {
		exePath = "";
		/** bowtie就是用来做索引的 */
		indexMaker = null;
		
		outFile = null;
		indelLen = 6;
		threadNum = 10;
		//输入的fastq
		lsLeftFq = new ArrayList<FastQ>();
		lsRightFq = new ArrayList<FastQ>();
		//将输入的fastq.gz转换成常规fastq
		lsLeftRun = new ArrayList<>();
		lsRightRun = new ArrayList<>();
		//转换的文件放在这个里面，最后要被删掉
		lsTmp = new ArrayList<>();
		isPrepare = false;
		mismatch = 3;
		fusion = false;
		gtfFile = null;
		seedLen = 22;
		
		/** 将没有mapping上的reads用bowtie2比对到基因组上，仅用于proton数据 */
		mapUnmapedReads = false;
		/** 比对到的index */
		dnaIndex = null;
		
		/** 第二次mapping所使用的命令 */
		lsCmdMapping2nd = new ArrayList<>();
	}
}
