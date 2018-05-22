package com.novelbio.database.domain.species;

import java.util.Map;
import java.util.Set;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.analysis.seq.fasta.format.ChrFileFormat;
import com.novelbio.analysis.seq.fasta.format.NCBIchromFaChangeFormat;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.sam.SamIndexRefsequence;
import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.curator.CuratorNBC;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.util.ServiceEnvUtil;
import com.novelbio.database.model.geneanno.SpeciesFile;
import com.novelbio.generalconf.PathDetailNBC;

/**
 * 把一个基因组文件切分为一条染色体一个文件，放在一个文件夹内
 * 譬如circRNA的预测软件，以及mapsplice，都需要把染色体切分后放在文件夹内
 * 那么由于某些基因组，譬如小麦等，其染色体数量会有几万条，为了提高效率，
 * 我们会结合gff文件，以及染色体长度，选择大约前4000条染色体生成文件夹，多
 * 的就不要了
 * 
 * @author zong0jie
 * @data 2016年7月18日
 */
public class SpeciesFileSepChr {
	private static final Logger logger = LoggerFactory.getLogger(SpeciesIndexMappingMaker.class);
	
	protected static final String chrSepPath = "chrSep";
	protected static final String chrSepFolder = "chr_sep_fold";

	/** 完成的flag */
	private static final String chrSepFlag = ".chrsep.finish";
	
	/** 一个染色体最多切分出多少条序列，这个除了测试，最好不要改 */
	public static int maxSeqNum = 4000;
	/** 染色体最短的序列不能短于这个长度 */
	public static int minLen = 1000;
	
	private SpeciesFile speciesFile;
	/** 输入第三方的chrSeq */
	private String chrSeq;
	/** 第三方chrSeq的输出路径 */
	private String outPath;
	
	/** 保存index的文件夹路径，默认从配置文件走 */
	private String genomePath = PathDetailNBC.getGenomePath();
	/** 是否需要分布式锁 */
	private boolean isLock = false;
	
	/** 仅用于测试 */
	@VisibleForTesting
	Set<String> setChrId;
	
	/** 
	 * 如果speciesFile存在，则会保存到系统指定的路径下
	 * 如果SpceisFile不存在，则会保存到当前chrSeq的路径下
	 * @param speciesFile
	 */
	public void setSpeciesFile(SpeciesFile speciesFile) {
		this.speciesFile = speciesFile;
		this.chrSeq = speciesFile.getChromSeqFile();
	}
	/** 
	 * 如果speciesFile存在，则会保存到系统指定的路径下
	 * 如果SpceisFile不存在，则会保存到当前chrSeq的路径下
	 * @param speciesFile
	 */
	public void setChrSeq(String chrSeq) {
		setChrSeq(chrSeq, FileOperate.getPathName(chrSeq));
	}
	/** 
	 * 如果speciesFile存在，则会保存到系统指定的路径下
	 * 如果SpceisFile不存在，则会保存到当前chrSeq的路径下
	 * @param speciesFile
	 */
	protected void setChrSeq(String chrSeq, String outPath) {
		this.chrSeq = chrSeq;
		this.outPath = FileOperate.addSep(outPath);
	}
	
	@VisibleForTesting
	protected void setSetChrId(Set<String> setChrId) {
		this.setChrId = setChrId;
	}
	public static void setMaxSeqNum(int maxSeqNum) {
		SpeciesFileSepChr.maxSeqNum = maxSeqNum;
	}
	public static void setMinLen(int minLen) {
		SpeciesFileSepChr.minLen = minLen;
	}
	/** 是否需要分布式锁 */
	public void setLock(boolean isLock) {
	    this.isLock = isLock;
    }
	
	/** 仅用于测试，保存index的文件夹路径，默认从配置文件走 */
	protected void setGenomePath(String genomePath) {
	    this.genomePath = FileOperate.addSep(genomePath);
    }
	
	private Set<String> getSetChrId() {
		if (!ArrayOperate.isEmpty(setChrId)) {
			return setChrId;
		}
		
		if (speciesFile.getGffType() != null) {
			GffHashGene gffHashGene = new GffHashGene(speciesFile.getGffType(), speciesFile.getGffFile());
			setChrId = gffHashGene.getMapChrID2LsGff().keySet();
        }
		return setChrId;
	}
	
	public void generateChrSepFiles() {
		InterProcessMutex lock = null;
		try {
			if (isLock && ServiceEnvUtil.isHadoopEnvRun() ) {
				lock = CuratorNBC.getInterProcessMutex(getLockPath(chrSeq));
				lock.acquire();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			logger.info("start splite " + chrSeq);

			if (FileOperate.isFileExistAndBigThan0(getChrSepFinishFlag())) return;
			
			String chrSeqFileOne = generateChrFile(chrSeq);
			seperateChrFile(chrSeqFileOne);
			
			TxtReadandWrite txtWrite = new TxtReadandWrite(getChrSepFinishFlag(), true);
			txtWrite.writefileln("finish");
			txtWrite.close();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (isLock && ServiceEnvUtil.isHadoopEnvRun() ) {
					lock.release();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	protected String getLockPath(String chrFile) {
		String lockPath = chrFile.replace(PathDetailNBC.getGenomePath(), "") + "chrSep";
		lockPath = FileOperate.removeSplashHead(lockPath, false).replace("/", "_").replace("\\", "_").replace(".", "_");
		return lockPath;
	}
	
	private String generateChrFile(String chrRaw) {		
		String chrSeqFileOne = getChrSepFileOne();
		FileOperate.createFolders(FileOperate.getPathName(chrSeqFileOne));
		
		Map<String, Long> mapChrID2ChrLen = SamIndexRefsequence.generateIndexAndGetMapChrId2Len(chrRaw);
		if (mapChrID2ChrLen.size() > maxSeqNum) {
			ChrFileFormat chrFileFormat = new ChrFileFormat();
			chrFileFormat.setIncludeChrId(getSetChrId());
			chrFileFormat.setRefSeq(chrRaw);
			chrFileFormat.setResultSeq(chrSeqFileOne);
			chrFileFormat.setMinLen(minLen);
			chrFileFormat.setMaxNum(maxSeqNum);
			chrFileFormat.rebuild();
		} else {
			FileOperate.copyFile(chrRaw, chrSeqFileOne, true);
			FileOperate.copyFile(SamIndexRefsequence.getIndexFile(chrRaw), SamIndexRefsequence.getIndexFile(chrSeqFileOne), true);
		}
		
		SamIndexRefsequence samIndexRefsequence = new SamIndexRefsequence();
		samIndexRefsequence.setRefsequence(chrSeqFileOne);
		samIndexRefsequence.indexSequence();
		return chrSeqFileOne;
	}
	
	private void seperateChrFile(String chrSeqFileOne) {
		String chrSepFolder = getChrSepFolder();
		FileOperate.deleteFileFolder(chrSepFolder);
		FileOperate.createFolders(chrSepFolder);
		NCBIchromFaChangeFormat ncbIchromFaChangeFormat = new NCBIchromFaChangeFormat();
		ncbIchromFaChangeFormat.setChromFaPath(chrSeqFileOne, "");
		ncbIchromFaChangeFormat.writeToSepFile(chrSepFolder);
	}
	
	/** 染色体切分是否结束的flag */
	public String getChrSepFinishFlag() {
		return getParentPathChrSepPath() + FileOperate.getFileName(chrSeq) + chrSepFlag;
	}
	
	/**
	 * 如果chrSeq与要输出的chrSeqOne路径全名相同，那么判断chrSeq中的序列数量是否小于maxSeqNum
	 * 如果小于maxSeqNum，那就可以直接用chrSeq
	 * @return
	 */
	public String getChrSepFileOne() {
		if (StringOperate.isRealNull(chrSeq)) {
			chrSeq = speciesFile.getChromSeqFile();
		}
		String chrSeqOne = getParentPathChrSepPath() + FileOperate.getFileName(chrSeq);
		
		Map<String, Long> mapChrID2ChrLen = SamIndexRefsequence.generateIndexAndGetMapChrId2Len(chrSeq);
		if (mapChrID2ChrLen.size() > maxSeqNum) {
			return FileOperate.changeFileSuffix(chrSeqOne, ".sepOne", null);
		}
		
		if (FileOperate.isFileExistAndBigThan0(chrSeqOne) && !FileOperate.getFileName(chrSeqOne).equals(FileOperate.getFileName(chrSeq))) {
			return FileOperate.changeFileSuffix(chrSeqOne, ".sepOne", null);
		}
		return chrSeqOne;
	}
	/** 返回切分染色体的具体文件夹 */
	public String getChrSepFolder() {
		return getParentPathChrSepPath() + chrSepFolder + FileOperate.getSepPath();
	}
	
	/**
	 * 返回切分染色体的合并的染色体文件，因为部分染色体数量太多，那么如果直接切分可能会产生几万个染色体文件
	 * 因此我们需要把染色体文件进行处理，仅提取前2000条序列
	 */
	private String getParentPathChrSepPath() {
		if (speciesFile != null) {
			return genomePath + chrSepPath + FileOperate.getSepPath() + getPathToVersion();
		} else {
			return outPath;
		}
	}

	
	/** 物种版本的相对路径，到版本为止
	 * 如 9606/GRCh38/<br>
	 * 包含最后的"/"
	 */
	private String getPathToVersion() {
		if(speciesFile.getTaxID() == 0 || StringOperate.isRealNull(speciesFile.getVersion()))
			throw new ExceptionNbcSpeciesNotExist("species taxId cannot be 0, and version must exist");
		return speciesFile.getTaxID() + FileOperate.getSepPath() + speciesFile.getVersion() + FileOperate.getSepPath();
	}
	


}
