package com.novelbio.analysis.seq.mapping;

import htsjdk.samtools.reference.FastaSequenceIndex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.fasta.ChrSeqHash;
import com.novelbio.analysis.seq.fasta.SeqFastaReader;
import com.novelbio.analysis.seq.fasta.format.ChrFileFormat;
import com.novelbio.analysis.seq.fasta.format.NCBIchromFaChangeFormat;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.sam.SamIndexRefsequence;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.curator.CuratorNBC;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.SpeciesIndexMappingMaker;
import com.novelbio.generalConf.PathDetailNBC;

/**
 * 如果添加新的index软件，需要在
 * {@linkplain SpeciesIndexMappingMaker} 类中添加建索引的代码
 * @author novelbio
 *
 */
public abstract class IndexMappingMaker {
	private static final Logger logger = LoggerFactory.getLogger(IndexMappingMaker.class);
	
	private static Map<SoftWare, Class<?>> mapSoft2Index = new HashMap<>();
	private static Set<SoftWare> lsSoftDna = new LinkedHashSet<>();
	private static Set<SoftWare> lsSoftRna = new LinkedHashSet<>();
	
	static {
		Class<?>[] clazzs = IndexMappingMaker.class.getDeclaredClasses();
		for (Class<?> class1 : clazzs) {
			if (!IndexMappingMaker.class.isAssignableFrom(class1)) {
	            	continue;
            }
			try {
				IndexMappingMaker indexMaker = (IndexMappingMaker) class1.newInstance();
				mapSoft2Index.put(indexMaker.getSoftWare(), class1);
				if (indexMaker.getMappingType() == EnumMappingType.DNA) {
					lsSoftDna.add(indexMaker.getSoftWare());
				} else if (indexMaker.getMappingType() == EnumMappingType.RNA) {
					lsSoftRna.add(indexMaker.getSoftWare());
				}
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	String exePath;
	
	String chrFile;
	
	String outFileName;
	
	SoftWare softWare;
	
	private String version;
	
	boolean isLock = true;
	
	public IndexMappingMaker(SoftWare softWare) {
		SoftWareInfo softWareInfo = new SoftWareInfo(softWare);
		this.exePath = softWareInfo.getExePathRun();
		this.softWare = softWare;
	}
	public SoftWare getSoftWare() {
	    return softWare;
    }
	
	protected abstract EnumMappingType getMappingType();
	
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
			logger.error("index make error:" + chrFile, e);
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
		
		if (FileOperate.isFileFolderExist(chrFile)) {
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
	
	public boolean isIndexFinished() {
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
	public static IndexMappingMaker createIndexMaker(SoftWare softWare) {
		if (softWare.toString().startsWith("bwa_")) {
			softWare = SoftWare.bwa_mem;
        }
		if (!mapSoft2Index.containsKey(softWare)) {
			throw new ExceptionNbcMappingSoftNotSupport("cannot find mapping software " + softWare);
        }
		try {
	        return (IndexMappingMaker) mapSoft2Index.get(softWare).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("initial mapping index error " + softWare, e);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("initial mapping index error " + softWare, e);

		}
	}
	
	public static class ExceptionNbcMappingSoftNotSupport extends RuntimeException {
		private static final long serialVersionUID = 4141495075098892818L;

		public ExceptionNbcMappingSoftNotSupport(String msg) {
			super(msg);
		}
	}
	
	/** 返回全体可以建索引的软件 */
	public static Set<SoftWare> getLsIndexDNA() {
		return lsSoftDna;
	}
	
	/** 返回全体可以建索引的软件 */
	public static Set<SoftWare> getLsIndexRNA() {
		return lsSoftRna;
	}
	
public static class IndexBwa extends IndexMappingMaker {
	public IndexBwa() {
		super(SoftWare.bwa_mem);
	}
	protected EnumMappingType getMappingType() {
		return EnumMappingType.DNA;
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

public static class IndexBowtie extends IndexMappingMaker {
	public IndexBowtie() {
		super(SoftWare.bowtie);
	}
	protected EnumMappingType getMappingType() {
		return EnumMappingType.DNA;
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

public static class IndexBowtie2 extends IndexMappingMaker {
	public IndexBowtie2() {
		super(SoftWare.bowtie2);
	}
	protected EnumMappingType getMappingType() {
		return EnumMappingType.DNA;
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

public static class IndexTophat extends IndexMappingMaker {
	private static final Logger logger = LoggerFactory.getLogger(IndexMappingMaker.class);
	
	String exePathBowtie;
	IndexMappingMaker indexBowtie;
	String tophatVersion;
	
	String gtfFile;
	
	/** 把gtf文件拷贝到染色体文件下去，只有当gtf由数据库提供时才需要移动位置 */
	boolean moveGtfToChr;
	
	public IndexTophat() {
		super(SoftWare.tophat);
	}
	
	protected EnumMappingType getMappingType() {
		return EnumMappingType.RNA;
	}
	public void setChrIndex(String chrFile) {
		this.chrFile = chrFile;
		if (indexBowtie != null) indexBowtie.setChrIndex(chrFile);
	}
	public void setBowtieVersion(SoftWare bowtieSoft) {
		if (bowtieSoft != SoftWare.bowtie && bowtieSoft != SoftWare.bowtie2) {
			throw new ExceptionNbcParamError("can only set mapping software as bowtie or bowtie2, but is: " + bowtieSoft);
		}
		indexBowtie = IndexMappingMaker.createIndexMaker(bowtieSoft);
		
		if (chrFile != null) indexBowtie.setChrIndex(chrFile);
	}
	
	/** 把gtf文件拷贝到染色体文件下去，只有当gtf由数据库提供时才需要移动位置 */
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
		if (FileOperate.isFileExistAndBigThan0(gtfFile)) {
			GffHashGene.checkFile(gtfFile, chrFile);
        }
		indexBowtie.setChrIndex(chrFile);
		super.IndexMake();
	}
	
	private void makeIndexChr() {
		if (FileOperate.isFileExist(indexBowtie.getIndexFinishedFlag())) {
			return;
		}
		indexBowtie.setLock(isLock);
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
	
	public boolean isIndexFinished() {
		boolean isBowtieFinish = FileOperate.isFileExist(indexBowtie.getIndexFinishedFlag());
		String gtfFlag = getIndexFinishedFlagGtf();
		if (gtfFlag == null) {
			return isBowtieFinish;
        }
		return isBowtieFinish && FileOperate.isFileExist(gtfFlag);
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

public static class IndexMapSplice extends IndexMappingMaker {
	/** mapsplice的后缀名，因为mapsplice要求文本中的序列不能太多，譬如小于4000条 */
	private static final String mapSpliceSuffix = ".novelbio_mapSplice_suffix";
	protected static int maxSeqNum = 4000;
	private static int minLen = 1000;
	
	Set<String> setChrInclude;
	String chrRaw;
	String chrSepFold;
	
	/** 有的chr文件中会包含太多的 chrId，譬如 trinity的结果会产生几万个 chrId
	 * 而mapsplice要求 每个chr一个文本，所以这里我们为了防止文本产生太多，需要将含有太多 chr 的染色体文件进行过滤，仅保留长的几千个文件 */
	boolean modifyChrFile = false;
	
	public IndexMapSplice() {
		super(SoftWare.mapsplice);
	}
	protected EnumMappingType getMappingType() {
		return EnumMappingType.RNA;
	}
	/** 给定gffHashGene，返回gffHashGene中包含有基因的chrId */
	public void setGffHashGene(GffHashGene gffHashGene) {
		if (gffHashGene == null) return;
		setChrInclude = gffHashGene.getMapChrID2LsGff().keySet();
	}
	/** 包含有基因的chrId，可以从gff文件中获取 */
	public void setSetChrInclude(Set<String> setChrInclude) {
	    this.setChrInclude = setChrInclude;
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
		super.makeIndex();
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
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "src/MapSplice/bowtie-build");
		lsCmd.add(chrFile);
		lsCmd.add(IndexBowtie.getChrNameWithoutSuffix(chrFile));
		return lsCmd;
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
				Map<String, Long> mapChrID2ChrLen = SamIndexRefsequence.generateIndexAndGetMapChrId2Len(chrRaw);
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
		if (!FileOperate.isFileFolderExist(chrSepFold)) {
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
		lsCmdVersion.add("python");
		lsCmdVersion.add(exePath + "mapsplice.py");
		lsCmdVersion.add("--version");
		CmdOperate cmdOperate = new CmdOperate(lsCmdVersion);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.setGetLsStdOut();
		cmdOperate.run();
		List<String> lsInfo = cmdOperate.getLsErrOut();
		try {
			version = lsInfo.get(0).split("MapSplice")[1].trim();
		} catch (Exception e) {
			throw new ExceptionCmd("cannot get mapsplice version" , e);
		}
		return version;
	}
	
}

public static class IndexHisat2 extends IndexMappingMaker {
	
	public IndexHisat2() {
		super(SoftWare.hisat2);
	}
	protected EnumMappingType getMappingType() {
		return EnumMappingType.RNA;
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

/** 不输入 chrFile，输入的是refseqAllIso文件 */
public static class IndexRsem extends IndexMappingMaker {
	String gene2IsoFile;
	public IndexRsem() {
		super(SoftWare.rsem);
	}
	protected EnumMappingType getMappingType() {
		return EnumMappingType.RNA;
	}
	@Override
	public String getIndexName() {
		return FileOperate.changeFileSuffix(chrFile, "_rsemIndex", "");
	}
	
	public void setGene2IsoFile(String gene2IsoFile) {
		this.gene2IsoFile = gene2IsoFile;
	}
	
	@Override
	protected List<String> getLsCmdIndex() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath +"rsem-prepare-reference");
		lsCmd.add("--transcript-to-gene-map");
		lsCmd.add(gene2IsoFile);
		lsCmd.add(chrFile);
		lsCmd.add(getIndexName());
		return lsCmd;
	}

	@Override
	public String getMapVersion() {
		//TODO 暂时没有想办法获得RSEM的版本。如果真的要获得，可以去rsem的文件夹中找 WHAT_IS_NEW 文件，里面有rsem的版本信息
		return "unknown";
	}

}


//TODO 新的类必须写在 IndexMappingMaker 类的内部，这样才能被上面的反射获取到
}

enum EnumMappingType {
	DNA, RNA
}
