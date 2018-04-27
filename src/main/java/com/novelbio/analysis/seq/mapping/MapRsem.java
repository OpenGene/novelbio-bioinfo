package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.GeneExpTable;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.mapping.IndexMappingMaker.IndexRsem;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.species.SpeciesFileExtract;
import com.novelbio.database.model.information.SoftWareInfo;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;
/**
 * 还没返回结果的bam文件<p>
 * 
 * <b>每次mapping都new一个新的对象</b>
 * @author zong0jie
 *
 */
public class MapRsem implements MapRNA {
	private static final Logger logger = LoggerFactory.getLogger(MapRsem.class);
	
	/** 由GffFile自动生成 */
	String gene2isoFile = "";
	String refFile = "";
	
	/** rsem的路径 */
	String exePathRsem = "";
	/** bowtie的路径 */
	String exePathBowtie = "";
	
	/** 线程数 */
	int threadNum = 4;
	
	List<FastQ> lsLeftFq;
	List<FastQ> lsRightFq;
	
	boolean pairend = false;
	/** 输出文件夹以及前缀 */
	String outPathPrefix = "";
	
	IndexRsem indexRsem = (IndexRsem) IndexMappingMaker.createIndexMaker(SoftWare.rsem);
	
	/** rsem 到 rpkm是增加了10^6 倍 */
	int foldRsem2RPKM = 1000000;
	
	public MapRsem() {
		SoftWareInfo softWareInfoRsem = new SoftWareInfo();
		softWareInfoRsem.setName(SoftWare.rsem);
		SoftWareInfo softWareInfoBowtie = new SoftWareInfo();
		softWareInfoBowtie.setName(SoftWare.bowtie);
		this.exePathRsem = softWareInfoRsem.getExePathRun();
		this.exePathBowtie = softWareInfoBowtie.getExePathRun();
	}
	
	@Override
	public void setIntronLenMin(int intronLenMin) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIntronLenMax(int intronLenMax) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 设定refFile
	 * @param refFile
	 */
	public void setRefIndex(String refFile) {
		this.refFile = refFile;
	}
	public void setThreadNum(int threadNum) {
		if (threadNum > 0 && threadNum < 24) {
			this.threadNum = threadNum;
		}
	}
	/** 没用 */
	public void setIndelLen(int indelLen) {}
	/** 没用 */
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype) {}

	/** 没用 */
	public void setInsert(int insert) {}
	/** 没用 */
	public void setMismatch(int mismatch) {}
	
	public SoftWare getSoftWare() {
		return SoftWare.bowtie;
	}
	/** 产生全新的reference */
	private void createGene2IsoAndRefSeq() {
		if (FileOperate.isFileExistAndBigThanSize(gene2isoFile, 0)) {
			return;
		}
		gene2isoFile = SpeciesFileExtract.getRefrna_Gene2Iso(refFile);
		if (!FileOperate.isFileExistAndBigThan0(gene2isoFile)) {
			throw new ExceptionNbcParamError("no gene2iso file!");
        }
	}
	private String[] getThreadNum() {
		return new String[]{"-p", threadNum + ""};
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
	
	public void setOutPathPrefix(String outPathPrefix) {
		this.outPathPrefix = outPathPrefix;
	}
	/**
	 * 制作索引，输入是用bowtie1还是bowtie2做索引
	 * @param bowtie2
	 */
	private void IndexMakeRsem() {
		createGene2IsoAndRefSeq();
		indexRsem.setChrIndex(refFile);
		indexRsem.setGene2IsoFile(gene2isoFile);
		indexRsem.IndexMake();
	}
	
	private String getOffset() {
		if (lsLeftFq.get(0).getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return "--phred64-quals";
		}
		return "--phred33-quals";
	}
	private String[] getBowtiePath() {
		if (exePathBowtie != null && !exePathBowtie.equals("")) {
			return new String[]{"--bowtie-path", exePathBowtie + "bowtie"};
		}
		return null;
	}
	private String getPairend() {
		if (lsLeftFq.size() > 0 && lsRightFq.size() > 0)
			pairend = true;
		else
			pairend = false;
		
		if (pairend) {
			return "--paired-end";
		}
		return null;
	}
	/**
	 * 比对序列并计算表达
	 * @return
	 */
	public void mapReads() {
		IndexMakeRsem();
		List<String> lsCmd = getLsCmdMapping();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
	}
	
	private List<String> getLsCmdMapping() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePathRsem + "rsem-calculate-expression");
		ArrayOperate.addArrayToList(lsCmd, getBowtiePath());
		addLsCmdStr(lsCmd, getOffset());
		addLsCmdStr(lsCmd, getPairend());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		String left = lsLeftFq.get(0).getReadFileName();
		for (int i = 1; i < lsLeftFq.size(); i++) {
			left += "," + lsLeftFq.get(i);
		}
		lsCmd.add(left);
		if (lsRightFq.size() > 0) {
			String right = lsRightFq.get(0).getReadFileName();
			for (int i = 1; i < lsRightFq.size(); i++) {
				right += "," + lsRightFq.get(i);
			}
			lsCmd.add(right);
		}
		lsCmd.add(indexRsem.getIndexName());
		lsCmd.add(outPathPrefix);
		return lsCmd;
	}
	
	private void addLsCmdStr(List<String> lsCmd, String param) {
		if(param == null) return;
		lsCmd.add(param);
	}
	
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmd = new ArrayList<>();
		CmdOperate cmdOperate = new CmdOperate(getLsCmdMapping());
		lsCmd.add(cmdOperate.getCmdExeStr());
		return lsCmd;
	}
	
	/** 整理结果文件，主要是整理gene.result,整理成gene list */
	public void getGeneExpInfo(String prefix, GeneExpTable expFPKM, GeneExpTable expCounts) {
		expCounts.setCurrentCondition(prefix);
		expFPKM.setCurrentCondition(prefix);
		List<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(outPathPrefix+".genes.results", 1);
		List<String> lsGeneName = new ArrayList<>();
		for (String[] ss : lsInfo) {
			lsGeneName.add(ss[0]);
		}
		expCounts.addLsGeneName(lsGeneName);
		expFPKM.addLsGeneName(lsGeneName);
		for (String[] ss : lsInfo) {
			double valueRPKM = 0;
			int valueCounts = 0;
			try {
				valueRPKM = Double.parseDouble(ss[2]);
				valueCounts = Integer.parseInt(ss[1]);
			} catch (Exception e) {
				continue;
			}
			expFPKM.addGeneExp(ss[0], valueRPKM * foldRsem2RPKM);
			expCounts.addGeneExp(ss[1], valueCounts);
		}
	}
	
	/** 设定Gene2Iso文件，如果有文件就用这个文件。
	 * 如果文件不存在，则从GffChrAbs中生成这个文件
	 *  */
	@Override
	public void setGtfFiles(String gtfFile) {
		this.gene2isoFile = gtfFile;
	}
	
	public static Map<String, String> mapPredictPrefix2File(String outPathPrefix) {
		Map<String, String> mapPrefix2Value = new HashMap<>();
		mapPrefix2Value.put("geneExp", outPathPrefix + ".genes.results");
		mapPrefix2Value.put("isoExp", outPathPrefix + ".isoforms.results");
		return mapPrefix2Value;
	}

	@Override
	public String getFinishName() {
		return outPathPrefix + ".genes.results";
	}

	@Override
    public IndexMappingMaker getIndexMappingMaker() {
	    return indexRsem;
    }
	
}
