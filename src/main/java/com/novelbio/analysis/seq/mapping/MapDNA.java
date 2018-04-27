package com.novelbio.analysis.seq.mapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamToBamSort;
import com.novelbio.base.ExceptionNullParam;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;
import com.novelbio.database.service.SpringFactoryBioinfo;

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

	boolean writeToBam = true;
	
	IndexMappingMaker indexMaker;
	
	public MapDNA(SoftWare softWare) {
		indexMaker = IndexMappingMaker.createIndexMaker(softWare);
	}
	public IndexMappingMaker getIndexMaker() {
		return indexMaker;
	}
	/** 待比对的染色体 */
	public void setChrIndex(String chrFile) {
		indexMaker.setChrIndex(chrFile);
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
		indexMaker.IndexMake();
		
		SamFile samFile = mapping();
		if (!writeToBam || samFile == null) {
			return null;
		}
//		String fileNameFinal = getOutNameCope();		
//		FileOperate.moveFile(true, samFile.getFileName(), fileNameFinal);
		logger.info("mapping 结束");
//		samFile = new SamFile(fileNameFinal);
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
		String fileNameFinal = getOutNameCope();
//		String fileNameTmp = FileOperate.changeFileSuffix(fileNameFinal, "_TmpMap", null);
		SamToBamSort samToBamSort = new SamToBamSort(fileNameFinal, inputStream, isPairEnd());
		samToBamSort.setWriteToBam(writeToBam);
		samToBamSort.setNeedSort(isNeedSort);
		samToBamSort.setAddMultiHitFlag(isSetMulitFlag);
		samToBamSort.setLsAlignmentRecorders(lsAlignmentRecorders);
		samToBamSort.convertAndFinish();
		return samToBamSort.getSamFileBam();
	}
	/** 运行失败后删除文件 */
	protected void deleteFailFile() {
		FileOperate.deleteFileFolder(getOutNameCope());
	}
	/** 根据是否转化为bam文件以及是否排序，返回相应的文件名 */
	public String getOutNameCope() {
		String resultSamName = FileOperate.changeFileSuffix(outFileName, "", "bam");
		if (isNeedSort) {
			resultSamName = FileOperate.changeFileSuffix(resultSamName, ".sorted", null);
		}
		return resultSamName;
	}
		
	/**
	 * 目前只有bwa和bowtie2两种
	 * @param softMapping
	 * @return
	 */
	public static MapDNAint creatMapDNA(SoftWare softMapping) {
		MapDNAint mapSoftware = null;
		if (softMapping == SoftWare.bwa_aln) {
			mapSoftware = (MapDNAint)SpringFactoryBioinfo.getFactory().getBean(MapBwaAln.class);
		} else if (softMapping == SoftWare.bwa_mem) {
			mapSoftware = (MapDNAint)SpringFactoryBioinfo.getFactory().getBean(MapBwaMem.class);
		} else if (softMapping == SoftWare.bowtie) {
			mapSoftware = (MapDNAint)SpringFactoryBioinfo.getFactory().getBean(MapBowtie.class);
		} else if (softMapping == SoftWare.bowtie2) {
			mapSoftware = (MapDNAint)SpringFactoryBioinfo.getFactory().getBean(MapBowtie2.class);
		} else {
			throw new ExceptionNullParam("No Such Param:" + softMapping.toString());
		}
		return mapSoftware;
	}
}
