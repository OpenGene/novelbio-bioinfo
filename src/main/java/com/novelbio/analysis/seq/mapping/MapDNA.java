package com.novelbio.analysis.seq.mapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamToBamSort;
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
		
	/** 因为mapping完后会将sam文件转成bam文件，这时候就可以顺带的做一些工作 */
	List<AlignmentRecorder> lsAlignmentRecorders = new ArrayList<AlignmentRecorder>();
	/** 结果是否需要排序 */
	boolean isNeedSort = false;
	String outFileName = "";
	/** 因为mapping完后会将sam文件转成bam文件，这时候就可以顺带的做一些工作 */
	public void setLsAlignmentRecorders(List<AlignmentRecorder> lsAlignmentRecorders) {
		this.lsAlignmentRecorders = lsAlignmentRecorders;
	}
	
	/** 加入的samStatistics在samToBam的时候会自动加上染色体长度等信息 */
	public void addAlignmentRecorder(AlignmentRecorder alignmentRecorder) {
		this.lsAlignmentRecorders.add(alignmentRecorder);
	}
	
	/** 输出的bam文件是否需要排序 */
	public void setSortNeed(boolean isNeedSort) {
		this.isNeedSort = isNeedSort;
	}
	
	/** 输入已经过滤好的fastq文件 */
	public abstract void setFqFile(FastQ leftFq, FastQ rightFq);
	
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}
	
	/**
	 * 百分之多少的mismatch，或者几个mismatch
	 * @param mismatch
	 */
	public abstract void setMismatch(double mismatch);
	
	public abstract void setChrIndex(String chrFile);
	/**
	 * 设定bwa所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 * @param chrFile
	 */
	public abstract void setExePath(String exePath);
	
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
	/**
	 * 默认gap为4，如果是indel查找的话，设置到5或者6比较合适
	 * @param gapLength
	 */
	public abstract void setGapLength(int gapLength);
	
	protected abstract boolean isPairEnd();
	/**
	 * mapping
	 * @return
	 */
	public SamFile mapReads() {
		boolean isIndexMake = IndexMake(false);
		SamFile samFile = mapping();
		if (samFile == null && !isIndexMake) {
//			IndexMake(true);
//			mapping();
		}
		logger.info("mapping 结束");
		return samFile;
	}
	
	public List<AlignmentRecorder> getLsAlignmentRecorders() {
		return lsAlignmentRecorders;
	}
	
	/**
	 * 是否顺利执行
	 * 实际上只要mapping能执行起来，譬如运行个10s没出错，就说明索引没问题了
	 * @return SamFile
	 * null 表示运行失败
	 */
	protected abstract SamFile mapping();
	
	/**
	 * @param isSetMulitFlag 是否需要设定非unique mapping的标签，目前 只有bowtie2需要
	 * @param inputStream 内部关闭流
	 * @param isNeedSort 看是否需要排序
	 * @return null表示运行失败，失败了也不删除文件
	 */
	protected SamFile copeSamStream(boolean isSetMulitFlag, InputStream inputStream, boolean isNeedSort) {
		SamFile samFileIn = new SamFile(inputStream);
		SamToBamSort samToBamSort = new SamToBamSort(getOutNameCope(), samFileIn, isPairEnd());
		if (isNeedSort) {
			samToBamSort.setNeedSort(true);
		}
		if (isSetMulitFlag) {
			samToBamSort.setAddMultiHitFlag(isSetMulitFlag);
		}
		samToBamSort.setLsAlignmentRecorders(lsAlignmentRecorders);
		samToBamSort.convert();
		samFileIn.close();
		if (FileOperate.isFileExistAndBigThanSize(getOutNameCope(), 0)) {
			return samToBamSort.getSamFileBam();
		} else {
			return null;
		}
	}
	/** 运行失败后删除文件 */
	protected void deleteFailFile() {
		FileOperate.DeleteFileFolder(getOutNameCope());
	}
	/** 根据是否转化为bam文件以及是否排序，返回相应的文件名 */
	private String getOutNameCope() {
		String resultSamName = FileOperate.changeFileSuffix(outFileName, "", "bam");
		if (isNeedSort) {
			resultSamName = FileOperate.changeFileSuffix(resultSamName, "_sorted", null);
		}
		return resultSamName;
	}
	
	/**
	 * 构建索引
	 * @parcam force 默认会检查是否已经构建了索引，是的话则返回。
	 * 如果face为true，则强制构建索引
	 * @return
	 */
	public abstract boolean IndexMake(boolean force);

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
		if (softMapping == SoftWare.bwa) {
			mapSoftware = (MapDNAint)SpringFactory.getFactory().getBean("mapBwa");
		} else if (softMapping == SoftWare.bowtie || softMapping == SoftWare.bowtie2) {
			mapSoftware = (MapDNAint)SpringFactory.getFactory().getBean("mapBowtie");
			mapSoftware.setSubVersion(softMapping);
		}
		return mapSoftware;
	}
}
