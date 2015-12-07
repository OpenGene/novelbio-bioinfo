package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.fasta.ChrSeqHash;
import com.novelbio.analysis.seq.fasta.format.ChrFileFormat;
import com.novelbio.analysis.seq.fasta.format.NCBIchromFaChangeFormat;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.sam.SamIndexRefsequence;
import com.novelbio.base.ExceptionParamError;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.curator.CuratorNBC;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.generalConf.PathDetailNBC;

public abstract class MapIndexMaker {
	private static final Logger logger = LoggerFactory.getLogger(MapIndexMaker.class);
	
	String exePath;
	
	String chrFile;
	
	String outFileName;
	
	SoftWare softWare;
	
	private String version;
	
	boolean isLock = true;
	
	public MapIndexMaker(SoftWare softWare) {
		SoftWareInfo softWareInfo = new SoftWareInfo(softWare);
		this.exePath = softWareInfo.getExePathRun();
		this.softWare = softWare;
	}
	
	public String getExePath() {
		return exePath;
	}
	/** 是否加入全局锁 */
	public void setLock(boolean isLock) {
		this.isLock = isLock;
	}
	
	public void setChrIndex(String chrFile) {
		this.chrFile = chrFile;
	}
	public void IndexMake() {
		if (isIndexFinished()) {
			return;
		}
		
		InterProcessMutex lock = null;
		try {
			if (isLock) {
				lock = CuratorNBC.getInterProcessMutex(getLockPath());
				lock.acquire();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			tryMakeIndex();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (isLock) {
					lock.release();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	protected String getLockPath() {
		String lockPath = chrFile.replace(PathDetailNBC.getGenomePath(), "") + softWare.toString();
		lockPath = FileOperate.removeSplashHead(lockPath, false).replace("/", "_").replace("\\", "_").replace(".", "_");
		return lockPath;
	}
	
	protected void tryMakeIndex() {
		if (isIndexFinished()) {
			return;
		}
		try {
			try {
				makeIndex();
			} catch (Exception e) {
				//重试一次
				makeIndex();
			}
		} catch (Exception e) {
			logger.error("index make error:" + chrFile);
			throw new RuntimeException("index make error:" + chrFile, e);
		}
	}
	
	/**
	 * 构建索引
	 * @parcam force 默认会检查是否已经构建了索引，是的话则返回。
	 * 如果face为true，则强制构建索引
	 * @return
	 */
	protected void makeIndex() {
		SamIndexRefsequence samIndexRefsequence = new SamIndexRefsequence();
		samIndexRefsequence.setRefsequence(chrFile);
		samIndexRefsequence.indexSequence();
		
		String parentPath = FileOperate.getParentPathNameWithSep(chrFile);
		List<String> lsCmd = getLsCmdIndex();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setRedirectInToTmp(true);
		cmdOperate.setRedirectOutToTmp(true);
		
		if (FileOperate.isFileFoldExist(chrFile)) {
			String runInfoPath = FileOperate.getParentPathNameWithSep(chrFile);
			FileOperate.createFolders(runInfoPath);
			
			cmdOperate.setStdOutPath(runInfoPath + "IndexMake_Stdout.txt", false, true);
			cmdOperate.setStdErrPath(runInfoPath + "IndexMake_Stderr.txt", false, true);

			cmdOperate.setOutRunInfoFileName(runInfoPath + "IndexMaking.txt");
		}
		
		for (String path : lsCmd) {
			if (path.equals(chrFile)) {
				cmdOperate.addCmdParamInput(chrFile, false);
			} else if (path.startsWith(parentPath)) {
				cmdOperate.addCmdParamOutput(path, false);
			}
		}
		cmdOperate.addCmdParamOutput(chrFile, false);
		cmdOperate.runWithExp(softWare.toString() + " index error:");
		generateFinishFlag();
	}
	
	/** 产生文件结束的flag */
	protected void generateFinishFlag() {
		TxtReadandWrite txtWriteFinishFlag = new TxtReadandWrite(getIndexFinishedFlag(), true);
		txtWriteFinishFlag.writefileln("finished");
		txtWriteFinishFlag.close();
	}
	
	protected boolean isIndexFinished() {
		return FileOperate.isFileExist(getIndexFinishedFlag());
	}
	
	/** 用这个来标记index是否完成 */
	protected String getIndexFinishedFlag() {
		String suffix = softWare.toString();
		if (softWare == SoftWare.bwa_aln || softWare == SoftWare.bwa_mem) {
			suffix = "bwa";
		}
		return FileOperate.changeFileSuffix(chrFile, "_indexFinished_" + suffix, "");
	}

	protected abstract List<String> getLsCmdIndex();	
	
	/** 返回索引所对应的索引名 */
	public abstract String getIndexName();
	
	/** 返回索引所对应的文件名 */
	public String getChrFile() {
		return chrFile;
	}
	
	public String getVersion() {
		if (version == null) {
			version = getMapVersion();
		}
		return version;
	}
	
	/** 返回mapping软件所对应的版本 */
	public abstract String getMapVersion();
	
	//TODO 考虑修改成spring托管
	public static MapIndexMaker createIndexMaker(SoftWare softWare) {
		MapIndexMaker indexMaker = null;
		if (softWare == SoftWare.bwa_aln || softWare == SoftWare.bwa_mem) {
			indexMaker = new IndexBwa();
		} else if (softWare == SoftWare.bowtie) {
			indexMaker = new IndexBowtie();
		} else if (softWare == SoftWare.bowtie2) {
			indexMaker = new IndexBowtie2();
		} else if (softWare == SoftWare.hisat2) {
			indexMaker = new IndexHisat2();
		} else if (softWare == SoftWare.mapsplice) {
			indexMaker = new IndexMapSplice();
		} else if (softWare == SoftWare.tophat) {
			indexMaker = new IndexTophat();
		} else {
			throw new ExceptionParamError("cannot find index " + softWare);
		}
		return indexMaker;
	}
	

public static class IndexBwa extends MapIndexMaker {
	public IndexBwa() {
		super(SoftWare.bwa_mem);
	}
	
	@Override
	protected List<String> getLsCmdIndex() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "bwa");
		lsCmd.add("index");
		ArrayOperate.addArrayToList(lsCmd, getChrLen(chrFile));
		lsCmd.add(chrFile);
		return lsCmd;
	}

	/**
	 * 根据基因组大小判断采用哪种编码方式
	 * @return 已经在前后预留空格，直接添加上idex就好
	 * 小于500MB的用 -a is
	 * 大于500MB的用 -a bwtsw
	 */
	private static String[] getChrLen(String chrFile) {
		long size = FileOperate.getFileSizeLong(chrFile);
		if (size/1024/1024 > 500) {
			return new String[]{"-a", "bwtsw"};
		} else {
			return new String[]{"-a", "is"};
		}
	}

	@Override
	public String getIndexName() {
		return chrFile;
	}
	
	@Override
	public String getMapVersion() {
		List<String> lsCmdVersion = new ArrayList<>();
		lsCmdVersion.add(exePath + "bwa");
		CmdOperate cmdOperate = new CmdOperate(lsCmdVersion);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.run();
		String version = null;
		try {
			List<String> lsInfo = cmdOperate.getLsErrOut();
			version = lsInfo.get(2).toLowerCase().replace("version:", "").trim();
		} catch (Exception e) {
			throw new ExceptionCmd("cannot get bwa version:\n" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
		}
		return version;
	}
}

public static class IndexBowtie extends MapIndexMaker {
	public IndexBowtie() {
		super(SoftWare.bowtie);
	}
	
	@Override
	protected List<String> getLsCmdIndex() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "bowtie-build");
		lsCmd.add(chrFile);
		lsCmd.add(getChrNameWithoutSuffix(chrFile));
		return lsCmd;
	}
	
	@Override
	public String getIndexName() {
		return getChrNameWithoutSuffix(chrFile);
	}
	
	/** 获得没有后缀名的序列，不带引号 */
	protected static String getChrNameWithoutSuffix(String chrFile) {
		String chrFileName = FileOperate.getParentPathNameWithSep(chrFile) + FileOperate.getFileNameSep(chrFile)[0];
		return chrFileName;
	}

	public String getMapVersion() {
		String version = null;
		List<String> lsCmdVersion = new ArrayList<>();
		lsCmdVersion.add(exePath + "bowtie");
		lsCmdVersion.add("--version");
		CmdOperate cmdOperate = new CmdOperate(lsCmdVersion);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.setGetLsStdOut();
		cmdOperate.runWithExp("get bowtie version error:");
		List<String> lsInfo = cmdOperate.getLsStdOut();
		try {
			version = lsInfo.get(0).toLowerCase().split("version")[1].trim();
		} catch (Exception e) {
			throw new ExceptionCmd("cannot get bowtie version", e);
		}
		return version;

	}
	
}

public static class IndexBowtie2 extends MapIndexMaker {
	public IndexBowtie2() {
		super(SoftWare.bowtie2);
	}
	
	@Override
	public String getIndexName() {
		return IndexBowtie.getChrNameWithoutSuffix(chrFile);
	}
	
	@Override
	protected List<String> getLsCmdIndex() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "bowtie2-build");
		lsCmd.add(chrFile);
		lsCmd.add(IndexBowtie.getChrNameWithoutSuffix(chrFile));
		return lsCmd;
	}
	
	public String getMapVersion() {
		String version = null;
		List<String> lsCmdVersion = new ArrayList<>();
		lsCmdVersion.add(exePath + "bowtie2");
		lsCmdVersion.add("--version");
		CmdOperate cmdOperate = new CmdOperate(lsCmdVersion);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.setGetLsStdOut();
		cmdOperate.runWithExp("get bowtie2 version error:");
		List<String> lsInfo = cmdOperate.getLsStdOut();
		try {
			version = lsInfo.get(0).toLowerCase().split("version")[1].trim();
		} catch (Exception e) {
			throw new ExceptionCmd("cannot get bowtie2 version", e);
		}

		return version;

	}
}

public static class IndexTophat extends MapIndexMaker {
	private static final Logger logger = LoggerFactory.getLogger(MapIndexMaker.class);
	
	String exePathBowtie;
	MapIndexMaker indexBowtie;
	String tophatVersion;
	
	String gtfFile;
	
	/** 把gtf文件拷贝到染色体文件下去，只有当gtf由数据库提供时才需要移动位置 */
	boolean moveGtfToChr;
	
	public IndexTophat() {
		super(SoftWare.tophat);
	}
	
	public void setBowtieVersion(SoftWare bowtieSoft) {
		if (bowtieSoft != SoftWare.bowtie && bowtieSoft != SoftWare.bowtie2) {
			throw new ExceptionParamError("can only set mapping software as bowtie or bowtie2, but is: " + bowtieSoft);
		}
		indexBowtie = MapIndexMaker.createIndexMaker(bowtieSoft);
	}
	
	public void setGtfFile(String gtfFile, boolean moveGtfToChr) {
		this.gtfFile = gtfFile;
		this.moveGtfToChr = moveGtfToChr;
	}
	
	public SoftWare getBowtieSoft() {
		return indexBowtie.softWare;
	}
	
	protected void tryMakeIndex() {
		//把数据库记录的gtf文件复制到chrFile的同一个文件夹下
		if (!StringOperate.isRealNull(gtfFile) && moveGtfToChr) {
			String chrPath = FileOperate.getPathName(chrFile);
			String gtfNew = chrPath + FileOperate.getFileName(gtfFile);
			if (!FileOperate.isFileExistAndBigThanSize(gtfNew, 0)) {
				FileOperate.copyFile(gtfFile, gtfNew, false);
			}
			gtfFile = gtfNew;
		}
		
		try {
			try {
				makeIndex();
			} catch (Exception e) {
				//重试一次
				makeIndex();
			}
		} catch (Exception e) {
			logger.error("index make error:" + chrFile);
			throw new RuntimeException("index make error:" + chrFile, e);
		}
	}
	
	protected String getLockPath() {
		String lockPath = chrFile.replace(PathDetailNBC.getGenomePath(), "");
		if (!StringOperate.isRealNull(gtfFile)) {
			lockPath += gtfFile;
		}
		lockPath = FileOperate.removeSplashHead(lockPath, false).replace("/", "_").replace("\\", "_").replace(".", "_");
		return lockPath;
	}
	
	/**
	 * 构建索引
	 * @parcam force 默认会检查是否已经构建了索引，是的话则返回。
	 * 如果face为true，则强制构建索引
	 * @return
	 */
	protected void makeIndex() {
		makeIndexChr();
		makeIndexGtf();
	}

	public void IndexMake() {
		indexBowtie.setChrIndex(chrFile);
		super.IndexMake();
	}
	
	private void makeIndexChr() {
		if (FileOperate.isFileExist(indexBowtie.getIndexFinishedFlag())) {
			return;
		}
		indexBowtie.makeIndex();
	}
	
	private void makeIndexGtf() {
		if (getIndexFinishedFlagGtf() == null || FileOperate.isFileExist(getIndexFinishedFlagGtf())) {
			return;
		}
		List<String> lsCmd = getLsCmdIndexGtf();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setRedirectOutToTmp(true);
		String runInfoPath = FileOperate.getParentPathNameWithSep(chrFile);
		FileOperate.createFolders(runInfoPath);
		cmdOperate.setStdOutPath(runInfoPath + "IndexGtfMake_Stdout.txt", false, true);
		cmdOperate.setStdErrPath(runInfoPath + "IndexGtfMake_Stderr.txt", false, true);
		cmdOperate.setOutRunInfoFileName(runInfoPath + "IndexGtfMaking.txt");
		cmdOperate.addCmdParamOutput(getIndexGff());
		cmdOperate.run();
		if(!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("tophat index error:\n" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
		}
		TxtReadandWrite txtWriteFinishFlag = new TxtReadandWrite(getIndexFinishedFlagGtf(), true);
		txtWriteFinishFlag.writefileln("finished");
		txtWriteFinishFlag.close();
	}
	
	private List<String> getLsCmdIndexGtf() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "tophat");
		if (indexBowtie.softWare == SoftWare.bowtie) {
			lsCmd.add("--bowtie1");
		}
		
		lsCmd.add("-G");
		lsCmd.add(gtfFile);
		lsCmd.add("--transcriptome-index=" + getIndexGff());
		lsCmd.add(indexBowtie.getIndexName());
		return lsCmd;
	}
	
	protected boolean isIndexFinished() {
		return FileOperate.isFileExist(indexBowtie.getIndexFinishedFlag()) && FileOperate.isFileExist(getIndexFinishedFlagGtf());
	}
	
	private String getIndexFinishedFlagGtf() {
		if (StringOperate.isRealNull(gtfFile)) {
			return null;
		}
		
		return FileOperate.changeFileSuffix(getGffFolder() + FileOperate.getFileNameSep(gtfFile)[0], "_" + indexBowtie.softWare.toString() + "_tophat_indexFinished", "");
	}
	
	public String getIndexGff() {
		return getGffFolder() + FileOperate.getFileNameSep(gtfFile)[0];
	}
	
	private String getGffFolder() {
		return gtfFile + "_" + indexBowtie.softWare.toString() + "_tophat_folder" + FileOperate.getSepPath();
	}
	
	protected List<String> getLsCmdIndex() {
		indexBowtie.setChrIndex(chrFile);
		return indexBowtie.getLsCmdIndex();
	}
	
	/** 返回索引所对应的索引名 */
	public String getIndexName() {
		return indexBowtie.getIndexName();
	}
	
	public String getVersionBowtie() {
		return indexBowtie.getVersion();
	}
	
	/** 返回mapping软件所对应的版本 */
	public String getMapVersion() {
		String version = null;
		try {
			List<String> lsCmdVersion = new ArrayList<>();
			lsCmdVersion.add(exePath + "tophat");
			lsCmdVersion.add("--version");
			CmdOperate cmdOperate = new CmdOperate(lsCmdVersion);
			cmdOperate.setTerminateWriteTo(false);
			cmdOperate.setGetLsStdOut();
			cmdOperate.run();
			List<String> lsInfo = cmdOperate.getLsStdOut();
			version = lsInfo.get(0).toLowerCase().replace("tophat", "").trim();
		} catch (Exception e) {
			throw new ExceptionCmd("cannot get bowtie2 version:", e);
		}

		return version;
	}
	
}

public static class IndexMapSplice extends MapIndexMaker {
	/** mapsplice的后缀名，因为mapsplice要求文本中的序列不能太多，譬如小于4000条 */
	private static final String mapSpliceSuffix = ".novelbio_mapSplice_suffix";
	protected static int maxSeqNum = 4000;
	private static int minLen = 1000;
	
	MapIndexMaker indexBowtie = MapIndexMaker.createIndexMaker(SoftWare.bowtie);

	Set<String> setChrInclude;
	String chrRaw;
	String chrSepFold;
	
	/** 有的chr文件中会包含太多的 chrId，譬如 trinity的结果会产生几万个 chrId
	 * 而mapsplice要求 每个chr一个文本，所以这里我们为了防止文本产生太多，需要将含有太多 chr 的染色体文件进行过滤，仅保留长的几千个文件 */
	boolean modifyChrFile = false;
	
	public IndexMapSplice() {
		super(SoftWare.mapsplice);
	}
	
	public void setGffHashGene(GffHashGene gffHashGene) {
		if (gffHashGene == null) return;
		setChrInclude = gffHashGene.getMapChrID2LsGff().keySet();
	}

	/**
	 * 构建索引
	 * @parcam force 默认会检查是否已经构建了索引，是的话则返回。
	 * 如果face为true，则强制构建索引
	 * @return
	 */
	protected void makeIndex() {
		generateChrIndex();
		generateChrSepFold();
		indexBowtie.setChrIndex(chrFile);
		if (!indexBowtie.isIndexFinished()) {
			indexBowtie.IndexMake();
		}
		generateFinishFlag();
	}
	
	
	/** 用这个来标记index是否完成 */
	protected String getIndexFinishedFlag() {
		return getIndexFinishedFlag(chrFile);
	}
	
	/** 用这个来标记index是否完成 */
	private String getIndexFinishedFlag(String chrFile) {
		String suffix = softWare.toString();
		if (softWare == SoftWare.bwa_aln || softWare == SoftWare.bwa_mem) {
			suffix = "bwa";
		}
		return FileOperate.changeFileSuffix(chrFile, "_indexFinished_" + suffix, "");
	}
	
	@Override
	protected List<String> getLsCmdIndex() {
		//TODO 用不到了，因为 makeIndex 已经被重写了
		return null;
	}
	
	@Override
	public void setChrIndex(String chrRaw) {
		this.chrRaw = chrRaw;
		if (chrRaw.contains(mapSpliceSuffix) || FileOperate.isFileExistAndBigThanSize(getIndexFinishedFlag(chrRaw), 0)) {
			//输入的就是mapsplice的chr文件，或者该chrFile已经有建好了 mapsplice的索引，那么就直接使用该chrResult作为索引
			chrFile = chrRaw;
		} else {
			//先判定 修正过的chrFile是否存在，如果存在则返回
			//如果不存在，则标记需要生成 修正的chrFile
			chrFile = FileOperate.changeFileSuffix(chrRaw, mapSpliceSuffix, null);
			modifyChrFile = true;
			if (!FileOperate.isFileExistAndBigThanSize(chrFile, 0)) {
				ChrSeqHash chrSeqHash = new ChrSeqHash(chrRaw, "");
				Map<String, Long> mapChrID2ChrLen = chrSeqHash.getMapChrLength();
				chrSeqHash.close();
				if (mapChrID2ChrLen.size() <= maxSeqNum) {
					chrFile = chrRaw;
					modifyChrFile = false;
				}
			}
		}
		chrSepFold = FileOperate.addSep(FileOperate.changeFileSuffix(chrFile, "_sep_fold", ""));
	}
	
	/** 将染色体文件进行过滤，仅保留4000来条染色体，因为太多的话mapsplice可能会报错 */
	private void generateChrIndex() {
		if (FileOperate.isFileExistAndBigThanSize(chrFile, 0)) {
			super.setChrIndex(chrFile);
			return;
		}

		if (modifyChrFile) {
			ChrFileFormat chrFileFormat = new ChrFileFormat();
			chrFileFormat.setIncludeChrId(setChrInclude);
			chrFileFormat.setRefSeq(chrRaw);
			chrFileFormat.setResultSeq(chrFile);
			chrFileFormat.setMinLen(minLen);
			chrFileFormat.setMaxNum(maxSeqNum);
			chrFileFormat.rebuild();
		} else {
			FileOperate.copyFile(chrRaw, chrFile, true);
		}
	}
	/** 生成一个文件夹，其中每条染色体一个文件 */
	private void generateChrSepFold() {
		if (!FileOperate.isFileFoldExist(chrSepFold)) {
			FileOperate.createFolders(chrSepFold);
			NCBIchromFaChangeFormat ncbIchromFaChangeFormat = new NCBIchromFaChangeFormat();
			ncbIchromFaChangeFormat.setChromFaPath(chrFile, "");
			ncbIchromFaChangeFormat.writeToSepFile(chrSepFold);
		}
	}
	
	public String getChrSepFolder() {
		return chrSepFold;
	}
	
	@Override
	public String getIndexName() {
		return getChrNameWithoutSuffix(chrFile);
	}
	
	/** 获得没有后缀名的序列，不带引号 */
	protected static String getChrNameWithoutSuffix(String chrFile) {
		String chrFileName = FileOperate.getParentPathNameWithSep(chrFile) + FileOperate.getFileNameSep(chrFile)[0];
		return chrFileName;
	}

	public String getMapVersion() {
		String version = null;
		List<String> lsCmdVersion = new ArrayList<>();
		lsCmdVersion.add(exePath + "bowtie");
		lsCmdVersion.add("--version");
		CmdOperate cmdOperate = new CmdOperate(lsCmdVersion);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.setGetLsStdOut();
		cmdOperate.runWithExp("get bowtie version error:");
		List<String> lsInfo = cmdOperate.getLsStdOut();
		try {
			version = lsInfo.get(0).toLowerCase().split("version")[1].trim();
		} catch (Exception e) {
			throw new ExceptionCmd("cannot get bowtie2 version:");
		}
		return version;
	}
	
}

public static class IndexHisat2 extends MapIndexMaker {
	
	public IndexHisat2() {
		super(SoftWare.hisat2);
	}
	
	@Override
	public String getIndexName() {
		return IndexBowtie.getChrNameWithoutSuffix(chrFile);
	}
	
	@Override
	protected List<String> getLsCmdIndex() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "hisat2-build");
		lsCmd.add(chrFile);
		lsCmd.add(IndexBowtie.getChrNameWithoutSuffix(chrFile));
		return lsCmd;
	}

	@Override
	public String getMapVersion() {
		String version = null;
		List<String> lsCmdVersion = new ArrayList<>();
		lsCmdVersion.add(exePath + "hisat2");
		lsCmdVersion.add("--version");
		CmdOperate cmdOperate = new CmdOperate(lsCmdVersion);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.setGetLsStdOut();
		cmdOperate.runWithExp("get hisat2 version error:");
		List<String> lsInfo = cmdOperate.getLsStdOut();
		try {
			version = lsInfo.get(0).toLowerCase().split("version")[1].trim();
		} catch (Exception e) {
			throw new ExceptionCmd("cannot get hisat2 version:");
		}
		return version;
	}

}
}