package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
/**
 * 还没返回结果的bam文件<p>
 * 
 * <b>每次mapping都new一个新的对象</b>
 * @author zong0jie
 *
 */
public class MapRsem implements MapRNA, IntCmdSoft {
	private static final Logger logger = Logger.getLogger(MapRsem.class);
	
	Species species;
	GffChrSeq gffChrSeq = null;
	GffChrAbs gffChrAbs = null;
	/** 由GffFile自动生成 */
	String gene2isoFile = "";
	String refFile = "";
	/** 自动生成 */
	String rsemIndex = "";
	
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
	
	/**跑完之后的gene表达值保存在这个里面 */
	ArrayListMultimap<String, Double> mapGeneID2LsExp;
	/**跑完之后的gene表达值保存在这个里面 */
	ArrayListMultimap<String, Integer> mapGeneID2LsCounts; 
	
	/** rsem 到 rpkm是增加了10^6 倍 */
	int foldRsem2RPKM = 1000000;
	
	public MapRsem() {
		SoftWareInfo softWareInfoRsem = new SoftWareInfo();
		softWareInfoRsem.setName(SoftWare.rsem);
		SoftWareInfo softWareInfoBowtie = new SoftWareInfo();
		softWareInfoBowtie.setName(SoftWare.bowtie);
		setExePath(softWareInfoRsem.getExePath(), softWareInfoBowtie.getExePath());
	}

	/**
	 * 设定Gff文件
	 * @param gffFile
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
		if (gffChrAbs == null) {
			return;
		}
		gffChrSeq = new GffChrSeq(gffChrAbs);
		this.species = gffChrAbs.getSpecies();
	}
	/**
	 * 设定bwa所在的文件夹以及待比对的路径
	 * @param exePathRsem rsem的路径 如果在根目录下则设置为""或null
	 * @param exePathBowtie bowtie的路径，什么时候rsem支持bowtie2了，那么再修正
	 */
	public void setExePath(String exePathRsem, String exePathBowtie) {
		if (exePathRsem == null || exePathRsem.trim().equals(""))
			this.exePathRsem = "";
		else
			this.exePathRsem = FileOperate.addSep(exePathRsem);
		
		if (exePathBowtie == null || exePathBowtie.trim().equals(""))
			this.exePathBowtie = "";
		else
			this.exePathBowtie = FileOperate.addSep(exePathBowtie);
	}
	/**
	 * 设定refFile
	 * @param refFile
	 */
	public void setFileRef(String refFile) {
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
	
	public SoftWare getBowtieVersion() {
		return SoftWare.bowtie;
	}
	/** mapping完后获得结果，为RPKM
	 * 为防止一个geneID对应多个exp的value，所以用list来报存value
	 *  */
	public ArrayListMultimap<String, Double> getMapGeneID2LsExp() {
		return mapGeneID2LsExp;
	}
	/** mapping完后获得结果，为Counts
	 * 为防止一个geneID对应多个exp的value，所以用list来报存value
	 *  */
	public ArrayListMultimap<String, Integer> getMapGeneID2LsCounts() {
		return mapGeneID2LsCounts;
	}
	/** 产生全新的reference */
	private void createGene2IsoAndRefSeq() {
		if (FileOperate.isFileExistAndBigThanSize(gene2isoFile, 0)) {
			return;
		}
		if (gffChrAbs == null) {
			return;
		}
		gene2isoFile = FileOperate.changeFileSuffix(refFile, "_gene2Iso", "txt");
		TxtReadandWrite txtGene2Iso = new TxtReadandWrite(gene2isoFile, true);
		SeqFastaHash seqFastaHash = new SeqFastaHash(refFile, null, false);
		//先找gff文件里面有没有对应的geneName，没有再找数据库，再没有就直接贴上基因名
		for (String geneIDstr : seqFastaHash.getLsSeqName()) {
			GffGeneIsoInfo gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchISO(geneIDstr);
			String symbol = null;
			if (gffGeneIsoInfo != null) {
				//TODO 可能会出错
				symbol = gffGeneIsoInfo.getParentGeneName();
			} else {
				GeneID geneID = new GeneID(geneIDstr, species.getTaxID());
				symbol = geneID.getSymbol();
			}
			if (symbol == null || symbol.equals("")) {
				symbol = geneIDstr;
			}
			txtGene2Iso.writefileln(symbol + "\t" + geneIDstr);
		}
		seqFastaHash.close();
		txtGene2Iso.close();
		gffChrAbs.close();
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
	private void IndexMakeBowtie() {
		createGene2IsoAndRefSeq();
		rsemIndex = FileOperate.changeFileSuffix(refFile, "_rsemIndex", "");
		if (FileOperate.isFileExist(rsemIndex + ".3.ebwt") == true)
			return;
		List<String> lsCmd = getLsCmdIndex();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
	}
	
	private List<String> getLsCmdIndex() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePathRsem+"rsem-prepare-reference");
		lsCmd.add("--transcript-to-gene-map");
		lsCmd.add(gene2isoFile);
		lsCmd.add(refFile);
		lsCmd.add(rsemIndex);
		return lsCmd;
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
		IndexMakeBowtie();
		List<String> lsCmd = getLsCmdMapping();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
		copeResult();
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
		lsCmd.add(rsemIndex);
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
		List<String> lsInfo = getLsCmdMapping();
		CmdOperate cmdOperate = new CmdOperate(lsInfo);
		lsCmd.add(cmdOperate.getCmdExeStr());
		return lsCmd;
	}
	
	/** 整理结果文件，主要是整理gene.result,整理成gene list */
	private void copeResult() {
		mapGeneID2LsExp = ArrayListMultimap.create();
		mapGeneID2LsCounts = ArrayListMultimap.create();
		TxtReadandWrite txtReadGeneExp = new TxtReadandWrite(outPathPrefix+".genes.results", false);
		for (String geneInfo : txtReadGeneExp.readlines()) {
			String[] ss = geneInfo.split("\t");
			double valueRPKM = 0;
			int valueCounts = 0;
			try {
				valueRPKM = Double.parseDouble(ss[2]);
				valueCounts = Integer.parseInt(ss[1]);
			} catch (Exception e) {
				continue;
			}
			mapGeneID2LsExp.put(ss[0], valueRPKM * foldRsem2RPKM);
			mapGeneID2LsCounts.put(ss[1], valueCounts);
		}
		txtReadGeneExp.close();
	}
	
	/** 设定Gene2Iso文件，如果有文件就用这个文件。
	 * 如果文件不存在，则从GffChrAbs中生成这个文件
	 *  */
	@Override
	public void setGtf_Gene2Iso(String gtfFile) {
		this.gene2isoFile = gtfFile;
	}
	
}
