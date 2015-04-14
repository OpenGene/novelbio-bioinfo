package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.IntCmdSoft;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;


/**
 * simple script : Trinity.pl --seqType fq --JM 10G --left reads_1.fq --right reads_2.fq --CPU 6 
 * @author ywd
 *
 */
public class Trinity implements IntCmdSoft {
	String exePath = "";	
	/**
	 * type of reads: ( cfa, cfq, fa, or fq )
	 */
	String seqType = "fq";
	
	/** (Jellyfish Memory) number of GB of system memory to use for   k-mer counting by jellyfish  (eg. 10G) *include the 'G' char */
	int JellyfishMemory = 60;
	
	/**
	 * left reads, one or more (separated by space),Fq文件
	 */
	List<String> lsLeftFq;
	
	/**
	 * right reads, one or more (separated by space),Fq文件
	 */
	List<String> lsRightFq;
	
	/**
	  *Strand-specific RNA-Seq read orientation.
	  *        if paired: RF or FR,
	  *        if single: F or R.   (dUTP method = RF)
	  *        See web documentation.
	 */
	StrandSpecific strandSpecific = StrandSpecific.NONE;
	
	/** name of directory for output (will be created if it doesn't already exist) */
	String output;
	
	/** number of CPUs to use, default: 2
	 * 我这里设置成20
	 */
	int threadNum = 20;
	
	/** minimum assembled contig length to report  (def=200) */
	int min_contig_length = 0;
	
	/**
	 * option, set if you have paired reads and
	 * you expect high gene density with UTR
	 * overlap (use FASTQ input file format
	 * for reads).
	 * (note: jaccard_clip is an expensive
	 * operation, so avoid using it unless
	 * necessary due to finding excessive fusion
	 *  transcripts w/o it.)
	 */
	boolean isJaccard_clip = false;
	
	/**
	 * If you have especially large RNA-Seq data sets involving many hundreds of
	 * millions of reads to billions of reads, consider performing an in silico
	 * normalization of the full data set using Trinity --normalize_reads. The
	 * default normalization process should work well for most data sets. If you
	 * prefer to manually set normalization-related parameters, you can find the
	 * options under the full Trinity usage info:
	 */
	boolean isNormalizeReads = false;
	/** Only prepare files (high I/O usage) and stop before kmer counting. */
	boolean isJustPrep = false;
	
	/** 是否删除中间文件 */
	boolean isCleanup = true;
	
	/** only retain the Trinity fasta file, rename as ${output_dir}.Trinity.fasta */
	boolean isFull_cleanup = false;
	
	/** min count for K-mers to be assembled by  Inchworm (default: 1) */
	int min_kmer_cov;
	
//	/** number of CPUs to use for Inchworm, default is min(6, --CPU option) */
//	int inchworm_cpu;
	
	/**
	 * maximum number of reads to anchor within   a single graph (default: 200000)
	 */
	int max_reads_per_graph = 0;
	
	String genome;
	String genomeSortedBam;
	/** maximum allowed intron length (also maximum fragment span on genome) */
	int intronMaxLen = 50000;
	/**
	 *stop Trinity after Inchworm and before running Chrysalis 
	 */
	boolean no_run_chrysalis = false;
	
//	/**
//	 * stop Trinity just before running the
//	 *  parallel QuantifyGraph computes, to
//	 *  leverage a compute farm and massively
//	 *  parallel execution..
//	 */
//	String no_run_quantifygraph;
	
//	/**
//	 * name of directory for chrysalis output (will be created if it doesn't already exist) 
//	 * default( "chrysalis" )
//	 */
//	String chrysalis_output;
	
	/**
	 * additional parameters to pass through to butterfly
	 */
	String bfly_opts;
	
	/**
	 * only most supported (N) paths are extended from node A->B,  mitigating combinatoric path explorations. (default: 10)
	 */
	int max_number_of_paths_per_node ;
	
	
	/**
	 * maximum length expected between fragment pairs (default: 500)
	 *     (reads outside this distance are treated as single-end)
	 */
	int group_pairs_distance = 500;
	
	/**
	 * minimum overlap of reads with growing transcript 
	 * 	path (default: PE: 75, SE: 25)
	 */
	int path_reinforcement_distance = 0;
	
	/** do not lock triplet-supported nodes */
	boolean isNo_triplet_lock = false;
	
	/**
	 * java max heap space setting for butterfly
	 * 	(default: 20G) => yields command
	 */
	int bflyHeapSpaceMax = 50;
	
	/**
	 * java initial hap space settings for
	 *	butterfly (default: 1G) => yields command
	 */
	int bflyHeapSpaceInit = 0;
	
	/**
	 * Perl module in /home/zong0jie/下载/trinityrnaseq_r2013-02-25/PerlLibAdaptors/ 
	 *                                    that implements 'run_on_grid()' 
	 *                                   for naively parallel cmds. (eg. 'BroadInstGridRunner')
	 */
	String grid_computing_module;
	
	public Trinity() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.trinity);
//		this.exePath = softWareInfo.getExePathRun();
	}
	
	/** trinity的所在路径 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.equals("")) {
			return;
		}
		this.exePath = FileOperate.addSep(exePath);
	}
	
	/** 这几种 cfa, cfq, fa, or fq，默认fq */
	public void setSeqType(String seqType) {
		if (seqType == null || seqType.equals("")) {
			return;
		}
		seqType = seqType.trim().toLowerCase();
		if (!seqType.equals("cfa") && !seqType.equals("cfq") && !seqType.equals("fa") && !seqType.equals("fq")) {
			return;
		}
		this.seqType = seqType;
	}

	private String[] getSeqType() {
		if (seqType == null) {
			return null;
		}
		return new String[]{"--seqType", seqType};
	}
	
	private String[] getJellyfishMemory() {
		if (JellyfishMemory <= 0) {
			return null;
		}
		return new String[]{"--JM", JellyfishMemory + "G"};
	}
	/**
	 * (Jellyfish Memory) number of GB of system memory to use for k-mer counting by jellyfish (eg. 10G)
	 * 默认60G
	 */
	public void setJellyfishMemory(int jellyfishMemory) {
		JellyfishMemory = jellyfishMemory;
	}
	
	/** 务必先设定Left再设定Right */
	public void setLsLeftFq(List<String> lsLeftFq) {
		this.lsLeftFq = lsLeftFq;
	}
	/** 务必先设定Left再设定Right */
	public void setLsRightFq(List<String> lsRightFq) {
		this.lsRightFq = lsRightFq;
	}

	private List<String> getFastQ() {
		List<String> lsFq = new ArrayList<>();
		if (isSingleEnd()) {
			lsFq.add("--single");
			if (lsLeftFq != null) lsFq.addAll(lsLeftFq);
			if (lsRightFq != null) lsFq.addAll(lsRightFq);			
		} else {//双端
			lsFq.add("--left");
			lsFq.addAll(lsLeftFq);
			lsFq.add("--right");
			lsFq.addAll(lsRightFq);
		}
		return lsFq;
	}
	
	private boolean isSingleEnd() {
		if ((lsLeftFq == null || lsLeftFq.size() == 0)//单端
				|| lsRightFq == null || lsRightFq.size() == 0
				) {
			return true;
		}
		return false;
	}
	
	//TODO 确定好是FR还是RF
	private String[] getSS_lib_type() {
		if (strandSpecific == StrandSpecific.NONE || strandSpecific == StrandSpecific.UNKNOWN) {
			return null;
		} else {
			if (isSingleEnd()) {
				if (strandSpecific == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
					return new String[]{"--SS_lib_type", "F"};
				} else {
					return new String[]{"--SS_lib_type", "R"};
				}
			} else {
				if (strandSpecific == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
					return new String[]{"--SS_lib_type", "FR"};
				} else {
					return new String[]{"--SS_lib_type", "RF"};
				}
			}
		}
	}

	public void setSS_lib_type(StrandSpecific strandSpecific) {
		if (strandSpecific != null) {
			this.strandSpecific = strandSpecific;
		}
	}
	
	/** 设定输出文件夹 必选 */
	public void setOutputPath(String output) {
		this.output = output;
	}

	private String[] getOutput() {
		if (output == null) {
			return null;
		}
		return new String[]{"--output", output};
	}
	
	/**
	 * If you have especially large RNA-Seq data sets involving many hundreds of
	 * millions of reads to billions of reads, consider performing an in silico
	 * normalization of the full data set using Trinity --normalize_reads. The
	 * default normalization process should work well for most data sets. If you
	 * prefer to manually set normalization-related parameters, you can find the
	 * options under the full Trinity usage info:
	 */
	public void setNormalizeReads(boolean isNormalizeReads) {
		this.isNormalizeReads = isNormalizeReads;
	}
	
	private String isNormalizeReads() {
		if (isNormalizeReads) {
			return "--normalize_reads";
		}
		return null;
	}
	
	public void setGenome(String genome) {
		this.genome = genome;
	}
	/** 有的话就用该bam文件指导拼接，没有就自己做mapping */
	public void setGenomeSortedBam(String genomeSortedBam) {
		this.genomeSortedBam = genomeSortedBam;
	}
	/** 最长intron的长度，也是用于指导拼接的，默认50000bp */
	public void setIntronMaxLen(int intronMaxLen) {
		if (intronMaxLen <= 0) return;
		
		this.intronMaxLen = intronMaxLen;
	}
	
	private String[] getGenomeGuid() {
		List<String> lsGenomeGuid = new ArrayList<>();
		if (!StringOperate.isRealNull(genome)) {
			lsGenomeGuid.add("--genome"); lsGenomeGuid.add(genome);
			if (!StringOperate.isRealNull(genomeSortedBam)) {
				lsGenomeGuid.add("--genome_guided_use_bam"); lsGenomeGuid.add(genomeSortedBam);
			}
			if (intronMaxLen > 0) {
				lsGenomeGuid.add("--genome_guided_max_intron"); lsGenomeGuid.add(intronMaxLen + "");
			}
		}
		return lsGenomeGuid.toArray(new String[0]);
	}
	
	/**线程数，默认20线程 */
	public void setThreadNum(int threadNum) {
		if (threadNum > 0) {
			this.threadNum = threadNum;
		}
	}

	private String[] getCPU() {
		if (threadNum <= 0) {
			return null;
		}
		return new String[]{"--CPU", threadNum + ""};
	}
	
	/** 最短contig的长度，默认200 */
	public void setMin_contig_length(int min_contig_length) {
		this.min_contig_length = min_contig_length;
	}

	private String[] getMin_contig_length() {
		if (min_contig_length <= 0) {
			return null;
		}
		return new String[]{"--min_contig_length", min_contig_length+""};
	}

	private String getJaccard_clip() {
		if (isJaccard_clip) {
			return "--jaccard_clip";
		} else {
			return null;
		}
	}
	
	/**
	 * option, set if you have paired reads and you expect high gene density with
	 *  UTR overlap (use FASTQ input file format for reads). (note: jaccard_clip is an
	 *  expensive operation, so avoid using it unless necessary due to finding 
	 *  excessive fusion transcripts w/o it.)
	 * @param jaccard_clip 默认false，一般不要修改它。真菌的考虑设置为true，问王俊宁确认
	 */
	public void setIsJaccard_clip(Boolean jaccard_clip) {
		this.isJaccard_clip = jaccard_clip;
	}

	private String getPrep() {
		if (isJustPrep) {
			return "--prep";
		}
		return null;
	}
	
	/** Only prepare files (high I/O usage) and stop before kmer counting.
	 * 默认false
	 */
	public void setIsJustPrep(boolean prep) {
		this.isJustPrep = prep;
	}

	private String getNo_cleanup() {
		if (!isCleanup) {
			return "--no_cleanup";
		}
		return null;
	}
	/** 是否删除中间文件 
	 * @param isCleanUp 默认为true
	 */
	public void setIsCleanUp(boolean isCleanUp) {
		this.isCleanup = isCleanUp;
	}

	private String getFull_cleanup() {
		if (!isFull_cleanup) {
			return null;
		} else {
			return "--full_cleanup";
		}
	}
	
	/**
	 * 是否删光所有临时文件
	 * @param full_cleanup 默认false
	 */
	public void setIsFull_cleanup(boolean full_cleanup) {
		this.isFull_cleanup = full_cleanup;
	}

	private String[] getMin_kmer_cov() {
		if (min_kmer_cov <= 0) {
			return null;
		}
		return new String[]{"--min_kmer_cov",  min_kmer_cov + ""};
	}
	/** min count for K-mers to be assembled by Inchworm (default: 1)<br> 
	 * 就走默认吧
	 */
	public void setMin_kmer_cov(int min_kmer_cov) {
		this.min_kmer_cov = min_kmer_cov;
	}

	private String[] getInchworm_cpu() {
		return new String[]{"--inchworm_cpu", threadNum+ ""};
	}

	private String[] getMax_reads_per_graph() {
		if (max_reads_per_graph <= 0) {
			return null;
		}
		return new String[]{"--max_reads_per_graph", max_reads_per_graph+ ""};
	}
	
	/** 每张图里面有多少条reads，走默认就好 */
	public void setMax_reads_per_graph(int max_reads_per_graph) {
		this.max_reads_per_graph = max_reads_per_graph;
	}

	private String getNo_run_chrysalis() {
		if (no_run_chrysalis) {
			return "--no_run_chrysalis";
		} else {
			return null;
		}
	}
	/** stop Trinity after Inchworm and before running Chrysalis，默认false不用设定 */
	public void setNo_run_chrysalis(boolean no_run_chrysalis) {
		this.no_run_chrysalis = no_run_chrysalis;
	}

	private String[] getBfly_opts() {
		if (bfly_opts == null || bfly_opts.trim().equals("")) {
			return null;
		}
		return new String[]{"--bfly_opts", bfly_opts + ""};
	}
	/** additional parameters to pass through to butterfly
	 * 一般不设定
	 *  */
	public void setBfly_opts(String bfly_opts) {
		if (bfly_opts == null || bfly_opts.trim().equals("")) {
			return;
		}
		this.bfly_opts = bfly_opts;
	}
	
	private String[] getMax_number_of_paths_per_node() {
		if (max_number_of_paths_per_node <= 0) {
			return null;
		}
		return new String[]{"--max_number_of_paths_per_node", max_number_of_paths_per_node + ""};
	}
	/** only most supported (N) paths are extended from node A->B, 
	 * mitigating combinatoric path explorations. (default: 10)<br>
	 * 大概是每个节点能有多少分支的意思，这个一般不要设置，走默认就好
	 *  */
	public void setMax_number_of_paths_per_node(int max_number_of_paths_per_node) {
		this.max_number_of_paths_per_node = max_number_of_paths_per_node;
	}

	private String[] getPairs_distance() {
		if (isSingleEnd() || group_pairs_distance <= 0) {
			return null;
		}
		return new String[]{"--group_pairs_distance", group_pairs_distance + ""};
	}
	
	/** 双端测序的建库长度，默认500 */
	public void setPairs_distance(int group_pairs_distance) {
		this.group_pairs_distance = group_pairs_distance;
	}

	private String[] getPath_reinforcement_distance() {
		if (path_reinforcement_distance <= 0) {
			return null;
		}
		return new String[]{"--path_reinforcement_distance ", path_reinforcement_distance + ""};
	}
	/** minimum overlap of reads with growing transcript path (default: PE: 75, SE: 25)，一般不用设定 */
	public void setPath_reinforcement_distance(int path_reinforcement_distance) {
		this.path_reinforcement_distance = path_reinforcement_distance;
	}

	private String getNo_triplet_lock() {
		if (isNo_triplet_lock) {
			return "--no_triplet_lock";
		} else {
			return null;
		}
	}
	/**
	 * do not lock triplet-supported nodes 
	 *  默认false，一般不修改 */
	public void setNo_triplet_lock(boolean no_triplet_lock) {
		this.isNo_triplet_lock = no_triplet_lock;
	}

	private String[] getBflyHeapSpaceMax() {
		if (bflyHeapSpaceMax <= 0) {
			return null;
		}
		return new String[]{"--bflyHeapSpaceMax", bflyHeapSpaceMax + "G"};
	}
	
	/** java max heap space setting for butterfly (default: 50G) => yields command */
	public void setBflyHeapSpaceMax(int bflyHeapSpaceMax) {
		this.bflyHeapSpaceMax = bflyHeapSpaceMax;
	}

	private String[] getBflyHeapSpaceInit() {
		if (bflyHeapSpaceInit <= 0) {
			return null;
		}
		return new String[]{"--bflyHeapSpaceInit", bflyHeapSpaceInit + "G"};
	}

	public void setBflyHeapSpaceInit(int bflyHeapSpaceInit) {
		this.bflyHeapSpaceInit = bflyHeapSpaceInit;
	}
	
	private String[] getBflyCPU() {
		if (threadNum <= 0) {
			return null;
		}
		return new String[]{"--bflyCPU", threadNum + ""};
	}

//	private String getBflyCalculateCPU() {
//		return "--bflyCalculateCPU";
//	}

	private String[] getGrid_computing_module() {
		if (grid_computing_module == null || grid_computing_module.trim().equals("")) {
			return null;
		}
		return new String[]{"--grid_computing_module", grid_computing_module + ""};
	}

	public void setGrid_computing_module(String grid_computing_module) {
		this.grid_computing_module = grid_computing_module;
	}
	
	public void runTrinity() {
		List<String> lsCmd = getLsCmd();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setRedirectInToTmp(true);
		for (String leftFq:lsLeftFq) {
			cmdOperate.addCmdParamInput(leftFq);
		}
		for (String rightFq:lsRightFq) {
			cmdOperate.addCmdParamInput(rightFq);
		}
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamOutput(output);
		cmdOperate.run();
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("run trinity error:", cmdOperate);
		}
	}
	
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePath + "Trinity");
		ArrayOperate.addArrayToList(lsCmd, getSeqType());
		ArrayOperate.addArrayToList(lsCmd, getJellyfishMemory() );
		lsCmd.addAll(getFastQ());
		ArrayOperate.addArrayToList(lsCmd, getSS_lib_type());
		ArrayOperate.addArrayToList(lsCmd, getOutput());
		ArrayOperate.addArrayToList(lsCmd, getCPU());
		ArrayOperate.addArrayToList(lsCmd, getMin_contig_length());
		addString(lsCmd, getJaccard_clip());
		addString(lsCmd, getPrep());
		addString(lsCmd, getNo_cleanup());
		addString(lsCmd, getFull_cleanup());
		addString(lsCmd, isNormalizeReads());
		ArrayOperate.addArrayToList(lsCmd, getMin_kmer_cov());
		ArrayOperate.addArrayToList(lsCmd, getInchworm_cpu());
		ArrayOperate.addArrayToList(lsCmd, getMax_reads_per_graph());
		ArrayOperate.addArrayToList(lsCmd, getGenomeGuid());
		addString(lsCmd, getNo_run_chrysalis());
		ArrayOperate.addArrayToList(lsCmd, getBfly_opts());
		ArrayOperate.addArrayToList(lsCmd, getMax_number_of_paths_per_node());
		ArrayOperate.addArrayToList(lsCmd, getPairs_distance());
		ArrayOperate.addArrayToList(lsCmd, getPath_reinforcement_distance());
		addString(lsCmd, getNo_triplet_lock());
		ArrayOperate.addArrayToList(lsCmd, getBflyHeapSpaceInit());
		ArrayOperate.addArrayToList(lsCmd, getBflyHeapSpaceMax());
		ArrayOperate.addArrayToList(lsCmd, getBflyCPU());
		ArrayOperate.addArrayToList(lsCmd, getGrid_computing_module());
		return lsCmd;
	}
	
	private void addString(List<String> lsCmd, String param) {
		if (StringOperate.isRealNull(param)) {
			return;
		}
		lsCmd.add(param);
	}
	
	/** 返回拼接好的文件的路径 */
	public String getResultPath() {
		return output + ".Trinity.fasta";
//		return FileOperate.addSep(output) + "trinity.fa";
	}

	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmd = getLsCmd();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		String cmd = cmdOperate.getCmdExeStr();
		List<String> lsCmdOut = new ArrayList<>();
		lsCmdOut.add(cmd);
		return lsCmdOut;
	}
	
}
