package com.novelbio.analysis.seq.mapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamToBamSort;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.service.SpringFactory;

/**
 * 设定了自动化建索引的方法，并且在mapping失败后会再次建索引
 * 但是还需要补充别的方法，譬如mapping失败后，用一个标准fq文件去做mapping，如果成功则说明索引没问题。
 * 这样才能最好的提高效率
 * @author zong0jie
 *
 */
public abstract class MapDNA implements MapDNAint {
	private static final Logger logger = Logger.getLogger(MapDNA.class);
	/**
	 * 超时时间，意思如果mapping时间大于该时间，index就不太会出错了
	 */
	static int overTime = 50000;
	
	List<FastQ> lsLeftFq = new ArrayList<>();
	List<FastQ> lsRightFq = new ArrayList<>();
	
	/** 因为mapping完后会将sam文件转成bam文件，这时候就可以顺带的做一些工作 */
	List<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<AlignmentRecorder>();
	/** 结果是否需要排序 */
	boolean isNeedSort = false;
	String outFileName = "";
	String prefix;
	/** 待比对的染色体 */
	String chrFile = "";
	
	boolean writeToBam = true;
	
	
	public void setChrIndex(String chrFile) {
		this.chrFile = chrFile;
	}
	/** 因为mapping完后会将sam文件转成bam文件，这时候就可以顺带的做一些工作 */
	public void setLsAlignmentRecorders(List<AlignmentRecorder> lsAlignmentRecorders) {
		this.lsAlignmentRecorders = lsAlignmentRecorders;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	/** 加入的samStatistics在samToBam的时候会自动加上染色体长度等信息 */
	public void addAlignmentRecorder(AlignmentRecorder alignmentRecorder) {
		this.lsAlignmentRecorders.add(alignmentRecorder);
	}
	@Override
	public void setWriteToBam(boolean writeToBam) {
		this.writeToBam = writeToBam;
	}
	/** 输出的bam文件是否需要排序 */
	public void setSortNeed(boolean isNeedSort) {
		this.isNeedSort = isNeedSort;
	}
	
	/**
	 *  输入已经过滤好的fastq文件
	 * @param leftFq
	 * @param rightFq 没有则输入null
	 */
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
	
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}
	
	/** 线程数量，默认4线程 */
	public abstract void setThreadNum(int threadNum);

	public abstract void setMapLibrary(MapLibrary mapLibrary);
	/**
	 * 本次mapping的组，所有参数都不能有空格
	 * @param sampleID 
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform
	 */
	public abstract void setSampleGroup(String sampleID, String LibraryName, String SampleName, String Platform);
	
	protected abstract boolean isPairEnd();
	/**
	 * mapping
	 * @return
	 */
	public SamFile mapReads() {
		PathDetail.getTmpPath();
		IndexMake();
		
		SamFile samFile = mapping();
		if (!writeToBam) {
			return null;
		}
		String fileNameFinal = getOutNameCope();		
		FileOperate.moveFile(true, samFile.getFileName(), fileNameFinal);
		logger.info("mapping 结束");
		samFile = new SamFile(fileNameFinal);
		return samFile;
	}
	
	public List<AlignmentRecorder> getLsAlignmentRecorders() {
		return lsAlignmentRecorders;
	}
	
	/**
	 * 是否顺利执行
	 * 实际上只要mapping能执行起来，譬如运行个10s没出错，就说明索引没问题了
	 * @return SamFile 内部已经关闭过的samfile
	 * null 表示运行失败
	 */
	protected abstract SamFile mapping();
	
	/**
	 * @param isSetMulitFlag 是否需要设定非unique mapping的标签，目前 有bowtie2和bwa的 mem需要
	 * @param inputStream 内部关闭流
	 * @param isNeedSort 看是否需要排序
	 * @return null表示运行失败，失败了也不删除文件
	 */
	protected SamFile copeSamStream(boolean isSetMulitFlag, InputStream inputStream, boolean isNeedSort) {
		SamFile samFileIn = new SamFile(inputStream);
		String fileNameFinal = getOutNameCope();
		String fileNameTmp = FileOperate.changeFileSuffix(fileNameFinal, "_TmpMap", null);
		SamToBamSort samToBamSort = new SamToBamSort(fileNameTmp, samFileIn, isPairEnd());
		samToBamSort.setWriteToBam(writeToBam);
		samToBamSort.setNeedSort(isNeedSort);
		samToBamSort.setAddMultiHitFlag(isSetMulitFlag);
		samToBamSort.setLsAlignmentRecorders(lsAlignmentRecorders);
		samToBamSort.convert();
		samFileIn.close();
		return samToBamSort.getSamFileBam();
	}
	/** 运行失败后删除文件 */
	protected void deleteFailFile() {
		FileOperate.DeleteFileFolder(getOutNameCope());
	}
	/** 根据是否转化为bam文件以及是否排序，返回相应的文件名 */
	public String getOutNameCope() {
		String resultSamName = FileOperate.changeFileSuffix(outFileName, "", "bam");
		if (isNeedSort) {
			resultSamName = FileOperate.changeFileSuffix(resultSamName, "_sorted", null);
		}
		return resultSamName;
	}
	
	public void IndexMake() {
		String flagMakeIndex = ".makeIndexFlag_";
		String flagMakeIndexDetail = null;
		String parentPath = FileOperate.getPathName(chrFile);
		if (isExistIndexFlag(parentPath, flagMakeIndex)) {
			waitUntilIndexFinish(parentPath, flagMakeIndex);
			IndexMake();
		}
		else if (!isIndexExist()) {
			boolean makeIndexSucess = true;
			flagMakeIndexDetail = flagMakeIndex + DateUtil.getNowTimeLongRandom();
			TxtReadandWrite txtWriteFlag = new TxtReadandWrite(parentPath + flagMakeIndexDetail, true);
			txtWriteFlag.close();
			try { Thread.sleep(5000); } catch (Exception e) { }
			List<String> lsFlags = FileOperate.getFoldFileNameLs(parentPath, flagMakeIndex, "*");
			Collections.sort(lsFlags);
			if (lsFlags.get(0).equals(parentPath + flagMakeIndexDetail)) {
				makeIndexSucess = tryMakeIndex(parentPath, flagMakeIndexDetail, flagMakeIndex);
			} else {
				FileOperate.delFile(parentPath + flagMakeIndexDetail);
				waitUntilIndexFinish(parentPath, flagMakeIndex);
				IndexMake();
			}
	
			if (!makeIndexSucess) {
				logger.error("index make error:" + chrFile);
				deleteIndex();
				FileOperate.delFile(parentPath + flagMakeIndex);
				throw new ExceptionCmd("index make error:" + chrFile);
			}
		}
	}
	
	private boolean isExistIndexFlag(String parentPath, String flagMakeIndex) {
		List<String> lsFileName = FileOperate.getFoldFileNameLs(parentPath, flagMakeIndex, "*");
		List<String> lsFileNew = new ArrayList<>();
		List<String> lsFileToBeDelete = new ArrayList<>();
		if (lsFileName.isEmpty()) {
			return false;
		} else {
			for (String flagFile : lsFileName) {
				if (Math.abs(FileOperate.getTimeLastModify(flagFile) - DateUtil.getNowTimeLong()) < 5099834488L) {
					lsFileNew.add(flagFile);
				} else {
					lsFileToBeDelete.add(flagFile);
				}
			}
			for (String string : lsFileToBeDelete) {
				FileOperate.DeleteFileFolder(string);
			}
			if (!lsFileNew.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean tryMakeIndex(String parentPath, String flagMakeIndexDetail, String flagMakeIndex) {
		boolean makeIndexSucess = false;
		try {
			int i = 0;
			try {
				makeIndex();
			} catch (Exception e) {
				makeIndex();
			}
		} catch (Exception e) {
			logger.error("index make error:" + parentPath + chrFile);
			deleteIndex();
			FileOperate.delFile(parentPath + flagMakeIndexDetail);
			throw new RuntimeException("index make error:" + parentPath + chrFile, e);
		}
		for (String fileName : FileOperate.getFoldFileNameLs(parentPath, flagMakeIndex, "*")) {
			FileOperate.delFile(fileName);
		}
		return makeIndexSucess;
	}
	
	/** 等待别的机器把索引建好了 */
	private void waitUntilIndexFinish(String parentPath, String flagMakeIndex) {
		while (FileOperate.getFoldFileNameLs(parentPath, flagMakeIndex, "*").size() > 0) {
			try { Thread.sleep(1000); } catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 构建索引
	 * @parcam force 默认会检查是否已经构建了索引，是的话则返回。
	 * 如果face为true，则强制构建索引
	 * @return
	 */
	protected abstract void makeIndex();
	/** 删除关键的索引文件，意思就是没有建成索引 */
	protected abstract void deleteIndex();
	
	protected abstract boolean isIndexExist();
	
//	protected abstract SamFile copeAfterMapping();
	
	/**
	 * 如果文件后缀名不是.sam，则在文件末尾添加.sam
	 * @param outFileName
	 * @return
	 */
	protected static String addSamToFileName(String outFileName) {
		if (outFileName.endsWith(".sam")) {
			return outFileName;
		} else if (outFileName.endsWith(".")) {
			return outFileName + "sam";
		} else {
			return outFileName + ".sam";
		}
	}
	
	/**
	 * 目前只有bwa和bowtie2两种
	 * @param softMapping
	 * @return
	 */
	public static MapDNAint creatMapDNA(SoftWare softMapping) {
		MapDNAint mapSoftware = null;
		if (softMapping == SoftWare.bwa_aln) {
			mapSoftware = (MapDNAint)SpringFactory.getFactory().getBean(MapBwaAln.class);
		} else if (softMapping == SoftWare.bwa_men) {
			mapSoftware = (MapDNAint)SpringFactory.getFactory().getBean(MapBwaMem.class);
		} else if (softMapping == SoftWare.bowtie || softMapping == SoftWare.bowtie2) {
			mapSoftware = (MapDNAint)SpringFactory.getFactory().getBean(MapBowtie.class);
			((MapBowtie)mapSoftware).setSubVersion(softMapping);
		} else {
			throw new ExceptionNullParam("No Such Param:" + softMapping.toString());
		}
		return mapSoftware;
	}
}
