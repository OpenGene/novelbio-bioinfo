package com.novelbio.analysis.seq.rnaseq;

import com.novelbio.base.cmd.CmdOperate;


/**
 * simple script : Trinity.pl --seqType fq --JM 10G --left reads_1.fq --right reads_2.fq --CPU 6 
 * @author ywd
 *
 */
public class Trinity {
	//TODO宗博设定
	String trinityPlPath="";
	
	/**
	 * type of reads: ( cfa, cfq, fa, or fq )
	 */
	String seqType;
	
	/**
	 * (Jellyfish Memory) number of GB of system memory to use for   k-mer counting by jellyfish  (eg. 10G) *include the 'G' char
	 */
	String JellyfishMemory;
	
	/**
	 * left reads, one or more (separated by space),Fq文件
	 */
	String leftFq;
	
	/**
	 * right reads, one or more (separated by space),Fq文件
	 */
	String rightFq;
	
	/**
	 * if unpaired reads,single reads, one or more (note, if single file contains pairs, can use flag: --run_as_paired )
	 */
	String singleFq;
	
	/**
	  *Strand-specific RNA-Seq read orientation.
	  *        if paired: RF or FR,
	  *        if single: F or R.   (dUTP method = RF)
	  *        See web documentation.
	 */
	String SS_lib_type;
	
	/**
	 * name of directory for output (will be created if it doesn't already exist)
	 */
	String output;
	
	/**
	 * number of CPUs to use, default: 2
	 */
	String CPU;
	
	/**
	 * minimum assembled contig length to report  (def=200)
	 */
	String min_contig_length;
	
	
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
	String jaccard_clip;
	
	/**
	 * Only prepare files (high I/O usage) and stop before kmer counting.
	 */
	String prep;
	
	/**
	 * retain all intermediate input files.
	 */
	String no_cleanup;
	
	/**
	 * only retain the Trinity fasta file, rename as ${output_dir}.Trinity.fasta
	 */
	String full_cleanup;
	
	/**
	 * get the Trinity literature citation and those of tools leveraged within.
	 */
	String cite;
	
	/**
	 * reports Trinity version (trinityrnaseq-r2013-02-25) and exits.
	 */
	String version;
	
	/**
	 * min count for K-mers to be assembled by  Inchworm (default: 1)
	 */
	String min_kmer_cov;
	
	/**
	 * number of CPUs to use for Inchworm, default is min(6, --CPU option)
	 */
	String inchworm_cpu;
	
	/**
	 * maximum number of reads to anchor within   a single graph (default: 200000)
	 */
	String max_reads_per_graph;
	
	/**
	 *stop Trinity after Inchworm and before running Chrysalis 
	 */
	String no_run_chrysalis;
	
	/**
	 * stop Trinity just before running the
	 *  parallel QuantifyGraph computes, to
	 *  leverage a compute farm and massively
	 *  parallel execution..
	 */
	String no_run_quantifygraph;
	
	/**
	 * name of directory for chrysalis output (will be created if it doesn't already exist) 
	 * default( "chrysalis" )
	 */
	String chrysalis_output;
	
	/**
	 * additional parameters to pass through to butterfly
	 */
	String bfly_opts;
	
	/**
	 * only most supported (N) paths are extended from node A->B,  mitigating combinatoric path explorations. (default: 10)
	 */
	String max_number_of_paths_per_node ;
	
	
	/**
	 * maximum length expected between fragment pairs (default: 500)
	 *     (reads outside this distance are treated as single-end)
	 */
	String group_pairs_distance;
	
	/**
	 * minimum overlap of reads with growing transcript 
	 * 	path (default: PE: 75, SE: 25)
	 */
	String path_reinforcement_distance;
	
	/**
	 * do not lock triplet-supported nodes
	 */
	String no_triplet_lock;
	
	/**
	 * java max heap space setting for butterfly
	 * 	(default: 20G) => yields command
	 */
	String bflyHeapSpaceMax;
	
	/**
	 * java initial hap space settings for
	 *	butterfly (default: 1G) => yields command
	 */
	String bflyHeapSpaceInit;
	
	/**
	 * threads for garbage collection
	 * (default, not specified, so java decides)
	 */
	String bflyGCThreads;
	
	/**
	 * CPUs to use (default will be normal   number of CPUs; e.g., 2)
	 */
	String bflyCPU;
	
	/**
	 * Calculate CPUs based on 80% of max_memory
	 * divided by maxbflyHeapSpaceMax
	 */
	String bflyCalculateCPU;
	
	/**
	 * stops after the Chrysalis stage. You'll
	 *      need to run the Butterfly computes
	 *      separately, such as on a computing grid.
	 *      Then, concatenate all the Butterfly assemblies by running:
	 *        'find trinity_out_dir/ -name "*allProbPaths.fasta" 
	 *         -exec cat {} + > trinity_out_dir/Trinity.fasta'
	 */
	String no_run_butterfly;
	
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

	public void setSeqType(String seqType) {
		this.seqType = seqType;
	}

	private String getJellyfishMemory() {
		if (JellyfishMemory == null) {
			return "";
		}
		return " --JM " +  JellyfishMemory;
	}

	public void setJellyfishMemory(String jellyfishMemory) {
		JellyfishMemory = jellyfishMemory;
	}

	private String getLeftFq() {
		if (leftFq == null) {
			return "";
		}
		return " --left " + leftFq;
	}

	public void setLeftFq(String leftFq) {
		this.leftFq = leftFq;
	}

	private String getRightFq() {
		if (rightFq == null) {
			return "";
		}
		return " --right " +  rightFq;
	}

	public void setRightFq(String rightFq) {
		this.rightFq = rightFq;
	}

	private String getSingleFq() {
		if (singleFq == null) {
			return "";
		}
		return " --single " + singleFq;
	}

	public void setSingleFq(String singleFq) {
		this.singleFq = singleFq;
	}

	private String getSS_lib_type() {
		if (SS_lib_type == null) {
			return "";
		}
		return " SS_lib_type " + SS_lib_type;
	}

	public void setSS_lib_type(String sS_lib_type) {
		SS_lib_type = sS_lib_type;
	}

	private String getOutput() {
		if (output == null) {
			return "";
		}
		return " --output " +  output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	private String getCPU() {
		if (CPU == null) {
			return "";
		}
		return " --CPU " +  CPU;
	}

	public void setCPU(String cPU) {
		CPU = cPU;
	}

	private String getMin_contig_length() {
		if (min_contig_length == null) {
			return "";
		}
		return " --min_contig_length " +  min_contig_length;
	}

	public void setMin_contig_length(String min_contig_length) {
		this.min_contig_length = min_contig_length;
	}

	private String getJaccard_clip() {
		if (jaccard_clip == null) {
			return "";
		}
		return " --jaccard_clip " + jaccard_clip;
	}

	public void setJaccard_clip(String jaccard_clip) {
		this.jaccard_clip = jaccard_clip;
	}

	private String getPrep() {
		if (prep == null) {
			return "";
		}
		return " --prep " + prep;
	}

	public void setPrep(String prep) {
		this.prep = prep;
	}

	private String getNo_cleanup() {
		if (no_cleanup == null) {
			return "";
		}
		return" --no_cleanup " + no_cleanup;
	}

	public void setNo_cleanup(String no_cleanup) {
		this.no_cleanup = no_cleanup;
	}

	private String getFull_cleanup() {
		if (full_cleanup == null) {
			return "";
		}
		return " --full_cleanup " + full_cleanup;
	}

	public void setFull_cleanup(String full_cleanup) {
		this.full_cleanup = full_cleanup;
	}

	private String getCite() {
		if (cite == null) {
			return "";
		}
		return " --cite " + cite;
	}

	public void setCite(String cite) {
		this.cite = cite;
	}

	private String getVersion() {
		if (version == null) {
			return "";
		}
		return " --version " + version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	private String getMin_kmer_cov() {
		if (min_kmer_cov == null) {
			return "";
		}
		return " --min_kmer_cov " +  min_kmer_cov;
	}

	public void setMin_kmer_cov(String min_kmer_cov) {
		this.min_kmer_cov = min_kmer_cov;
	}

	private String getInchworm_cpu() {
		if (inchworm_cpu == null) {
			return "";
		}
		return" --inchworm_cpu " + inchworm_cpu;
	}

	public void setInchworm_cpu(String inchworm_cpu) {
		this.inchworm_cpu = inchworm_cpu;
	}

	private String getMax_reads_per_graph() {
		if (max_reads_per_graph == null) {
			return "";
		}
		return" --max_reads_per_graph " + max_reads_per_graph;
	}

	public void setMax_reads_per_graph(String max_reads_per_graph) {
		this.max_reads_per_graph = max_reads_per_graph;
	}

	private String getNo_run_chrysalis() {
		if (no_run_chrysalis == null) {
			return "";
		}
		return " --no_run_chrysalis " + no_run_chrysalis;
	}

	public void setNo_run_chrysalis(String no_run_chrysalis) {
		this.no_run_chrysalis = no_run_chrysalis;
	}

	private String getNo_run_quantifygraph() {
		if (no_run_quantifygraph == null) {
			return "";
		}
		return" --no_run_quantifygraph " + no_run_quantifygraph;
	}

	public void setNo_run_quantifygraph(String no_run_quantifygraph) {
		this.no_run_quantifygraph = no_run_quantifygraph;
	}

	private String getChrysalis_output() {
		if (chrysalis_output == null) {
			return "";
		}
		return " --chrysalis_output " + chrysalis_output;
	}

	public void setChrysalis_output(String chrysalis_output) {
		this.chrysalis_output = chrysalis_output;
	}

	private String getBfly_opts() {
		if (bfly_opts == null) {
			return "";
		}
		return " --bfly_opts " + bfly_opts;
	}

	public void setBfly_opts(String bfly_opts) {
		this.bfly_opts = bfly_opts;
	}

	private String getMax_number_of_paths_per_node() {
		if (max_number_of_paths_per_node == null) {
			return "";
		}
		return " --max_number_of_paths_per_node " + max_number_of_paths_per_node;
	}

	public void setMax_number_of_paths_per_node(String max_number_of_paths_per_node) {
		this.max_number_of_paths_per_node = max_number_of_paths_per_node;
	}

	private String getGroup_pairs_distance() {
		if (group_pairs_distance == null) {
			return "";
		}
		return " --group_pairs_distance " + group_pairs_distance;
	}

	public void setGroup_pairs_distance(String group_pairs_distance) {
		this.group_pairs_distance = group_pairs_distance;
	}

	private String getPath_reinforcement_distance() {
		if (path_reinforcement_distance == null) {
			return "";
		}
		return " --path_reinforcement_distance " + path_reinforcement_distance;
	}

	public void setPath_reinforcement_distance(String path_reinforcement_distance) {
		this.path_reinforcement_distance = path_reinforcement_distance;
	}

	private String getNo_triplet_lock() {
		if (no_triplet_lock == null) {
			return "";
		}
		return " --no_triplet_lock " + no_triplet_lock;
	}

	public void setNo_triplet_lock(String no_triplet_lock) {
		this.no_triplet_lock = no_triplet_lock;
	}

	private String getBflyHeapSpaceMax() {
		if (bflyHeapSpaceMax == null) {
			return "";
		}
		return " --bflyHeapSpaceMax " + bflyHeapSpaceMax;
	}

	public void setBflyHeapSpaceMax(String bflyHeapSpaceMax) {
		this.bflyHeapSpaceMax = bflyHeapSpaceMax;
	}

	private String getBflyHeapSpaceInit() {
		if (bflyHeapSpaceInit == null) {
			return "";
		}
		return" --bflyHeapSpaceInit " + bflyHeapSpaceInit;
	}

	public void setBflyHeapSpaceInit(String bflyHeapSpaceInit) {
		this.bflyHeapSpaceInit = bflyHeapSpaceInit;
	}

	private String getBflyGCThreads() {
		if (bflyGCThreads == null) {
			return "";
		}
		return " --bflyGCThreads " +  bflyGCThreads;
	}

	public void setBflyGCThreads(String bflyGCThreads) {
		this.bflyGCThreads = bflyGCThreads;
	}

	private String getBflyCPU() {
		if (bflyCPU == null) {
			return "";
		}
		return " --bflyCPU " + bflyCPU;
	}

	public void setBflyCPU(String bflyCPU) {
		this.bflyCPU = bflyCPU;
	}

	private String getBflyCalculateCPU() {
		if (bflyCalculateCPU == null) {
			return "";
		}
		return "  --bflyCalculateCPU " + bflyCalculateCPU;
	}

	public void setBflyCalculateCPU(String bflyCalculateCPU) {
		this.bflyCalculateCPU = bflyCalculateCPU;
	}

	private String getNo_run_butterfly() {
		if (no_run_butterfly == null) {
			return "";
		}
		return " --no_run_butterfly " +  no_run_butterfly;
	}

	public void setNo_run_butterfly(String no_run_butterfly) {
		this.no_run_butterfly = no_run_butterfly;
	}

	private String getGrid_computing_module() {
		if (grid_computing_module == null) {
			return "";
		}
		return " --grid_computing_module " + grid_computing_module;
	}

	public void setGrid_computing_module(String grid_computing_module) {
		this.grid_computing_module = grid_computing_module;
	}
	
	
	public void runTrinity() {
		String cmdScript = trinityPlPath + getSeqType() + getJellyfishMemory() + getLeftFq() + getRightFq() + getSingleFq() + getSS_lib_type() + getOutput() + getCPU() + getMin_contig_length() 
												+ getJaccard_clip() + getPrep() + getNo_cleanup() + getFull_cleanup() + getCite() + getVersion() + getMin_kmer_cov() + getInchworm_cpu() + getMax_reads_per_graph() 
												+ getNo_run_chrysalis() + getNo_run_quantifygraph() + getChrysalis_output() + getBfly_opts() + getMax_number_of_paths_per_node() + getGroup_pairs_distance() 
												+ getPath_reinforcement_distance() + getNo_triplet_lock() + getBflyHeapSpaceInit() + getBflyHeapSpaceMax() + getBflyGCThreads() + getBflyCPU() + getBflyCalculateCPU()
												+ getNo_run_butterfly() + getGrid_computing_module();
		
	CmdOperate cmdOperate = new CmdOperate(cmdScript);
	cmdOperate.run();
	}
	
	
}
