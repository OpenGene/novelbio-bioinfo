package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.base.cmd.CmdOperate;


/**
 * simple script : Trinity.pl --seqType fq --JM 10G --left reads_1.fq --right reads_2.fq --CPU 6 
 * @author ywd
 *
 */
public class Trinity {
	//TODO宗博设定
	String trinityPlPath="trinity.pl";
	
	/**
	 * type of reads: ( cfa, cfq, fa, or fq )
	 */
	String seqType = "fq";
	
	/**
	 * (Jellyfish Memory) number of GB of system memory to use for   k-mer counting by jellyfish  (eg. 10G) *include the 'G' char
	 */
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

	private String getSeqType() {
		if (seqType == null) {
			return "";
		}
		return " --seqType " +  seqType;
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

	private String getJellyfishMemory() {
		if (JellyfishMemory <= 0) {
			return "";
		}
		return " --JM " +  JellyfishMemory + "G";
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

	private String getFastQ() {
		String resultFq = "";
		if ((lsLeftFq == null || lsLeftFq.size() == 0)//单端
				|| lsRightFq == null || lsRightFq.size() == 0
				) {
			List<String> lsSingle = new ArrayList<>();
			if (lsLeftFq != null) lsSingle.addAll(lsLeftFq);
			if (lsRightFq != null) lsSingle.addAll(lsRightFq);
			String resultSingle = "";
			for (String string : lsSingle) {
				resultSingle = resultSingle + CmdOperate.addQuot(string) + " ";
			}
			resultFq = " --single " + resultSingle;
		} else {//双端
			String resultLeft = "", resultRight = "";
			for (String leftFq : lsLeftFq) {
				resultLeft = resultLeft + CmdOperate.addQuot(leftFq) + " ";
			}
			for (String rightFq : lsRightFq) {
				resultRight = resultRight + CmdOperate.addQuot(rightFq) + " ";
			}
			resultFq = " --left " + resultLeft + " -- right " + resultRight;
		}
		return resultFq;
	}
	
	private boolean isSingleEnd() {
		if ((lsLeftFq == null || lsLeftFq.size() == 0)//单端
				|| lsRightFq == null || lsRightFq.size() == 0
				) {
			return true;
		}
		return false;
	}
	
	private String getSS_lib_type() {
		if (strandSpecific == StrandSpecific.NONE) {
			return "";
		} else {
			if (isSingleEnd()) {
				if (strandSpecific == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
					return " --SS_lib_type F ";
				} else {
					return " --SS_lib_type R ";
				}
			} else {
				if (strandSpecific == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
					return " --SS_lib_type FR ";
				} else {
					return " --SS_lib_type RF ";
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

	private String getOutput() {
		if (output == null) {
			return "";
		}
		return " --output " +  output;
	}

	/**线程数，默认20线程 */
	public void setThreadNum(int threadNum) {
		if (threadNum > 0) {
			this.threadNum = threadNum;
		}
	}

	private String getCPU() {
		if (threadNum <= 0) {
			return "";
		}
		return " --CPU " +  threadNum + " ";
	}
	
	/** 最短contig的长度，默认200 */
	public void setMin_contig_length(int min_contig_length) {
		this.min_contig_length = min_contig_length;
	}

	private String getMin_contig_length() {
		if (min_contig_length <= 0) {
			return "";
		}
		return " --min_contig_length " +  min_contig_length;
	}

	private String getJaccard_clip() {
		if (isJaccard_clip) {
			return " --jaccard_clip ";
		} else {
			return "";
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
			return " --prep ";
		}
		return "";
	}
	
	/** Only prepare files (high I/O usage) and stop before kmer counting.
	 * 默认false
	 */
	public void setIsJustPrep(boolean prep) {
		this.isJustPrep = prep;
	}

	private String getNo_cleanup() {
		if (!isCleanup) {
			return " --no_cleanup ";
		}
		return"";
	}
	/** 是否删除中间文件 
	 * @param isCleanUp 默认为true
	 */
	public void setIsCleanUp(boolean isCleanUp) {
		this.isCleanup = isCleanUp;
	}

	private String getFull_cleanup() {
		if (!isFull_cleanup) {
			return "";
		} else {
			return " --full_cleanup ";
		}
	}
	
	/**
	 * 是否删光所有临时文件
	 * @param full_cleanup 默认false
	 */
	public void setIsFull_cleanup(boolean full_cleanup) {
		this.isFull_cleanup = full_cleanup;
	}

	private String getMin_kmer_cov() {
		if (min_kmer_cov <= 0) {
			return "";
		}
		return " --min_kmer_cov " +  min_kmer_cov;
	}
	/** min count for K-mers to be assembled by Inchworm (default: 1)<br> 
	 * 就走默认吧
	 */
	public void setMin_kmer_cov(int min_kmer_cov) {
		this.min_kmer_cov = min_kmer_cov;
	}

	private String getInchworm_cpu() {
		return" --inchworm_cpu " + threadNum;
	}

	private String getMax_reads_per_graph() {
		if (max_reads_per_graph <= 0) {
			return "";
		}
		return" --max_reads_per_graph " + max_reads_per_graph;
	}
	
	/** 每张图里面有多少条reads，走默认就好 */
	public void setMax_reads_per_graph(int max_reads_per_graph) {
		this.max_reads_per_graph = max_reads_per_graph;
	}

	private String getNo_run_chrysalis() {
		if (no_run_chrysalis) {
			return " --no_run_chrysalis ";
		} else {
			return "";
		}
	}
	/** stop Trinity after Inchworm and before running Chrysalis，默认false不用设定 */
	public void setNo_run_chrysalis(boolean no_run_chrysalis) {
		this.no_run_chrysalis = no_run_chrysalis;
	}

	private String getBfly_opts() {
		if (bfly_opts == null || bfly_opts.trim().equals("")) {
			return "";
		}
		return " --bfly_opts " + bfly_opts;
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
	
	private String getMax_number_of_paths_per_node() {
		if (max_number_of_paths_per_node < 0) {
			return "";
		}
		return " --max_number_of_paths_per_node " + max_number_of_paths_per_node;
	}
	/** only most supported (N) paths are extended from node A->B, 
	 * mitigating combinatoric path explorations. (default: 10)<br>
	 * 大概是每个节点能有多少分支的意思，这个一般不要设置，走默认就好
	 *  */
	public void setMax_number_of_paths_per_node(int max_number_of_paths_per_node) {
		this.max_number_of_paths_per_node = max_number_of_paths_per_node;
	}

	private String getPairs_distance() {
		if (isSingleEnd() || group_pairs_distance <= 0) {
			return "";
		}
		return " --group_pairs_distance " + group_pairs_distance;
	}
	
	/** 双端测序的建库长度，默认500 */
	public void setPairs_distance(int group_pairs_distance) {
		this.group_pairs_distance = group_pairs_distance;
	}

	private String getPath_reinforcement_distance() {
		if (path_reinforcement_distance <= 0) {
			return "";
		}
		return " --path_reinforcement_distance " + path_reinforcement_distance;
	}
	/** minimum overlap of reads with growing transcript path (default: PE: 75, SE: 25)，一般不用设定 */
	public void setPath_reinforcement_distance(int path_reinforcement_distance) {
		this.path_reinforcement_distance = path_reinforcement_distance;
	}

	private String getNo_triplet_lock() {
		if (isNo_triplet_lock) {
			return " --no_triplet_lock ";
		} else {
			return "";
		}
	}
	/**
	 * do not lock triplet-supported nodes 
	 *  默认false，一般不修改 */
	public void setNo_triplet_lock(boolean no_triplet_lock) {
		this.isNo_triplet_lock = no_triplet_lock;
	}

	private String getBflyHeapSpaceMax() {
		if (bflyHeapSpaceMax <= 0) {
			return "";
		}
		return " --bflyHeapSpaceMax " + bflyHeapSpaceMax + "G";
	}
	/** java max heap space setting for butterfly (default: 50G) => yields command */
	public void setBflyHeapSpaceMax(int bflyHeapSpaceMax) {
		this.bflyHeapSpaceMax = bflyHeapSpaceMax;
	}

	private String getBflyHeapSpaceInit() {
		if (bflyHeapSpaceInit <= 0) {
			return "";
		}
		return" --bflyHeapSpaceInit " + bflyHeapSpaceInit + "G";
	}

	public void setBflyHeapSpaceInit(int bflyHeapSpaceInit) {
		this.bflyHeapSpaceInit = bflyHeapSpaceInit;
	}
	
	private String getBflyCPU() {
		if (threadNum <= 0) {
			return "";
		}
		return " --bflyCPU " + threadNum;
	}

	private String getBflyCalculateCPU() {
		if (threadNum <= 0) {
			return "";
		}
		return "  --bflyCalculateCPU " + threadNum;
	}

	private String getGrid_computing_module() {
		if (grid_computing_module == null || grid_computing_module.trim().equals("")) {
			return "";
		}
		return " --grid_computing_module " + grid_computing_module;
	}

	public void setGrid_computing_module(String grid_computing_module) {
		this.grid_computing_module = grid_computing_module;
	}
	
	public void runTrinity() {
		String cmdScript = trinityPlPath + getSeqType() + getJellyfishMemory() + getFastQ() + getSS_lib_type() + getOutput()
					+ getCPU() + getMin_contig_length() + getJaccard_clip() + getPrep() + getNo_cleanup()
					+ getFull_cleanup() + getMin_kmer_cov() + getInchworm_cpu() + getMax_reads_per_graph() 
					+ getNo_run_chrysalis() + getBfly_opts() + getMax_number_of_paths_per_node() + getPairs_distance() 
					+ getPath_reinforcement_distance() + getNo_triplet_lock() + getBflyHeapSpaceInit()
					+ getBflyHeapSpaceMax() + getBflyCPU() + getBflyCalculateCPU() + getGrid_computing_module();
			
		CmdOperate cmdOperate = new CmdOperate(cmdScript);
		cmdOperate.run();
	}
	
	public String getResultPath() {
		//TODO 返回拼接好的文件的路径
		return "";
	}
}
