package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public MapIndexMaker(SoftWare softWare) {
		SoftWareInfo softWareInfo = new SoftWareInfo(softWare);
		this.exePath = softWareInfo.getExePathRun();
		this.softWare = softWare;
	}
	
	public void setChrIndex(String chrFile) {
		this.chrFile = chrFile;
	}
	public void IndexMake() {
		if (FileOperate.isFileExist(getIndexFinishedFlag())) {
			return;
		}
		
		String lockPath = chrFile.replace(PathDetailNBC.getGenomePath(), "");
		lockPath = FileOperate.removeSplashHead(lockPath, false).replace("/", "_").replace("\\", "_").replace(".", "_");
		InterProcessMutex lock = CuratorNBC.getInterProcessMutex(lockPath);
		try {
			lock.acquire();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			tryMakeIndex();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				lock.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	private void tryMakeIndex() {
		if (FileOperate.isFileExist(getIndexFinishedFlag())) {
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
		String parentPath = FileOperate.getParentPathNameWithSep(chrFile);
		List<String> lsCmd = getLsCmdIndex();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setRedirectInToTmp(true);
		cmdOperate.setRedirectOutToTmp(true);
		
		if (FileOperate.isFileFoldExist(outFileName)) {
			String runInfoPath = FileOperate.getParentPathNameWithSep(outFileName);
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
		TxtReadandWrite txtWriteFinishFlag = new TxtReadandWrite(getIndexFinishedFlag(), true);
		txtWriteFinishFlag.writefileln("finished");
		txtWriteFinishFlag.close();
	}
	
	/** 用这个来标记index是否完成 */
	private String getIndexFinishedFlag() {
		String suffix = softWare.toString();
		if (softWare == SoftWare.bwa_aln || softWare == SoftWare.bwa_mem) {
			suffix = "bwa";
		}
		return FileOperate.changeFileSuffix(chrFile, "_indexFinished_" + suffix, "");
	}

	protected abstract List<String> getLsCmdIndex();	
	
	/** 返回索引所对应的文件名 */
	public abstract String getIndexName();
	
	public String getVersion() {
		if (version == null) {
			version = getMapVersion();
		}
		return version;
	}
	
	/** 返回mapping软件所对应的版本 */
	public abstract String getMapVersion();
	
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
		}
		return indexMaker;
	}
}

class IndexBwa extends MapIndexMaker {
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
		cmdOperate.runWithExp("get bwa version error:");
		String version = null;
		try {
			List<String> lsInfo = cmdOperate.getLsErrOut();
			version = lsInfo.get(2).toLowerCase().replace("version:", "").trim();
		} catch (Exception e) {
			throw new ExceptionCmd("cannot get bwa version:");
		}
		return version;
	}
}

class IndexBowtie extends MapIndexMaker {
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

class IndexBowtie2 extends MapIndexMaker {
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
		cmdOperate.setGetLsStdOut();
		cmdOperate.runWithExp("get bowtie2 version error:");
		List<String> lsInfo = cmdOperate.getLsStdOut();
		try {
			version = lsInfo.get(0).toLowerCase().split("version")[1].trim();
		} catch (Exception e) {
			throw new ExceptionCmd("cannot get bowtie2 version:");
		}

		return version;

	}
}

class IndexHisat2 extends MapIndexMaker {
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