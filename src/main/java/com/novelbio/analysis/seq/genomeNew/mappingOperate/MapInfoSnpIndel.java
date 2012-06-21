package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.tools.Mas3.getProbID;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.SnpIndelRs;
import com.novelbio.database.model.modcopeid.GeneID;
import com.novelbio.database.service.servgeneanno.ServSnpIndelRs;
import com.novelbio.database.updatedb.database.CopeDBSnp132;
/**
 * 解析samtools产生的pile up信息，将每一行生成一个本类，专门存储堆叠信息
 * 有设定flag就当snp，没有设定flag就当indel
 * @author zong0jie
 *
 */
public class MapInfoSnpIndel extends MapInfo {
	public static final String TYPE_INSERT = "insert";
	public static final String TYPE_DELETION = "deletion";
	public static final String TYPE_MISMATCH = "mismatch";
	public static final String TYPE_CORRECT = "correct";
	public static final String SEP = "@//@";
	/**  snp的类型，TYPE_INSERT等 */
	String type = TYPE_CORRECT;
	/** 
	 * <b>里面都是正向的序列</b>
	 * 该位置可能有不止一种的插入缺失或是碱基替换类型，那么就用该hash表来存储这么多种信息
	 * allen的碱基+sep+Type
	 * value: 数量，用数组仅仅为了能够传递地址  */
	HashMap<String, int[]> hashAlle = new HashMap<String, int[]>();

	int taxID = 0;
	private static Logger logger = Logger.getLogger(MapInfoSnpIndel.class);
	SnpIndelRs snpIndelRs;
//	String refAAseq = "";
//	String thisAaSeq = "";
	String thisBase = "";
	String refBase = "";
	String thisAAnr = "";
	/** snp在基因中的位置，0-1之间，0.1表示snp在基因长度*0.1的位置处  */
	double prop = 0;
	/** 本snp或indel所在的起点 */
	int refSnpIndelStart = 0;
	/** 本snp或indel所在的refgenome上的终点，如果为snp，则起点和终点一样 */
	int refSnpIndelEnd = 0;
	/**
	 * snp在基因中的位置，0-1之间，0.1表示snp在基因长度*0.1的位置处
	 * 越小越靠近头部
	 * 0-1之间
	 */
	private void setProp(double prop) {
		this.prop = prop;
	}
	/**
	 * snp在基因长度的百分比
	 * 越小越靠近头部
	 */
	public double getProp() {
		return prop;
	}
	/**
	 * 获得snp或indel在ref上的起点，实际位点
	 * @return
	 */
	public int getRefSnpIndelStart() {
		return refSnpIndelStart;
	}
	/** 本snp或indel所在的refgenome上的终点，如果为snp，则起点和终点一样 */
	public int getRefSnpIndelEnd() {
		return refSnpIndelEnd;
	}
	/**
	 * 按照方向获得snp或indel在ref上的起点
	 * @return
	 */
	public int getRefSnpIndelStartCis() {
		if (cis5to3) {
			return refSnpIndelStart;
		}
		else {
			return refSnpIndelEnd;
		}
	}
	/** 按照方向获得snp或indel在ref上的终点，如果为snp则起点和终点一样 */
	public int getRefSnpIndelEndCis() {
		if (cis5to3) {
			return refSnpIndelEnd;
		}
		else {
			return refSnpIndelStart;
		}
	}
	
//	/**
//	 * 输入samtools产生的pile up文件的一行，然后填充本类
//	 * 注意没有设定refSnpIndelEnd
//	 * @param samString
//	 */
//	public MapInfoSnpIndel(String samString) {
//		String[] ss = samString.split("\t");
//		this.refID = ss[0];
//		this.refSnpIndelStart = Integer.parseInt(ss[1]);
//		this.refBase = ss[2];
//		this.Read_Depth_Filtered = Integer.parseInt(ss[3]);
//		setAllenInfo(ss[4]);
//	}
	/**
	 *  在已有refbase信息的基础上，查找该refSnpIndelStart位点有哪些indel或snp
	 *  找到的indel所对应的refbase可能和原来的refbase不一样
	 * @param samString
	 */
	public void setSamToolsPilup(String samString) {
		String[] ss = samString.split("\t");
		this.refID = ss[0];
		this.refSnpIndelStart = Integer.parseInt(ss[1]);//本行舍不设定都无所谓，因为输入的时候就是要求相同的ID
//		this.refBase = ss[2];//ref就不设定了
		this.Read_Depth_Filtered = Integer.parseInt(ss[3]);
		setAllenInfo(ss[4]);
	}
	/**
	 * @param taxID 物种
	 * @param chrID 染色体号
	 * @param snpLoc snp位点
	 * @param refBase ref的序列
	 * @param thisBase 本序列
	 */
	public MapInfoSnpIndel(int taxID,String chrID, int snpLoc, String refBase, String thisBase) {
		super(chrID);
		this.taxID = taxID;
		//flagLoc有东西说明是snp
		if (refBase.trim().length() == 1 && thisBase.trim().length() == 1) {
			this.refSnpIndelStart = snpLoc;
		}
		else {
			this.refSnpIndelStart = snpLoc;
			this.refSnpIndelEnd = snpLoc+ refBase.trim().length() - 1;
		}
	    this.refBase = refBase;
	    this.thisBase = thisBase;
	    if (refBase.length() == 1 && thisBase.length() == 1) {
	    	this.type = TYPE_MISMATCH;
		}
	    else if (refBase.length() < thisBase.length()) {
			this.type = TYPE_INSERT;
		}
	    else if (refBase.length() > thisBase.length()) {
			this.type = TYPE_DELETION;
		}
	    hashAlle.put(thisBase + SEP + type, new int[]{0});
	}
	/**
	 * 返回snp的类型，TYPE_INSERT等
	 */
	public String getType() {
		return this.type;
	}
	/**
	 * 重新设定Allelic_depths_Ref，和hashAlle信息
	 *  给定samtools产生的pile up那个pileup信息，计算该位点的堆叠情况<br>
	 * 格式如下<br> ...........,.............,....,....,.,.,..,..,...,....,.^!.<br>解释:<br>
	 *  <b>.</b> :match to the reference base on the forward strand<br>
	 *  <b>,</b> :match on the reverse strand, <br>
	 *  <b>’>’</b> or<b> ’<’ </b> :a reference skip<br>
	 *  <b>‘ACGTN’ </b> :mismatch on the forward strand<br> 
	 *  <b>‘acgtn’</b> :mismatch on the reverse strand<br>
	 *  <b> ‘\+[0-9]+[ACGTNacgtn]+’</b> :insertion between this reference position and the next reference position.
	 *  The length of the insertion is given by the integer in the pattern, followed by the inserted sequence.<br>
	 *  <b>‘-[0-9]+[ACGTNacgtn]+’</b> represents a deletion from the reference. The deleted bases will be presented as<b> ‘*’</b> in the following lines. 
	 *  <b>‘^’</b>the start of a read. The ASCII of the character following ‘^’ minus 33 gives the mapping quality. 
	 *  <b>‘$’</b> marks the end of a read segment.
	 * @param pileUpInfo 输入 ...........,.............,....,....,.,.,..,..,...,....,.^!. 这种东西
	 */
	private void setAllenInfo(String pileUpInfo) {
		Allelic_depths_Ref = 0; hashAlle = new HashMap<String, int[]>();
		char[] pipInfo = pileUpInfo.toCharArray();
		for (int i = 0; i < pipInfo.length; i++) {
			char c = pipInfo[i];
			if (c == '$')
				continue;
			if (c == '^') {
				i ++;
				continue;
			}
			else if (c == ',' || c == '.' ) {
				Allelic_depths_Ref++;
				continue;
			}
			else if (c == '+' || c == '-') {
				int tmpInDelNum = 0;
				i ++;
				//如果开头是“+”号，则获得+号后的数字，也就是indel的长度
				for (; i < pipInfo.length; i++) {
					char tmpNum = pipInfo[i];
					if (tmpNum >= 48 && tmpNum <=57) {
						tmpInDelNum = tmpInDelNum*10 + tmpNum -  48;
					}
					else {
						i--;
						break;
					}
				}
				//获得具体的字符
				char[] tmpSeq = new char[tmpInDelNum];
				for (int j = 0; j < tmpSeq.length; j++) {
					i++;
					tmpSeq[j] = pipInfo[i];
				}
				String indel = String.copyValueOf(tmpSeq);
				//装入hash表
				String type = "";
				if (c == '+')
					type = TYPE_INSERT;
				else
					type = TYPE_DELETION;
				String indelInfo = (indel+SEP+type).toLowerCase();
				if (hashAlle.containsKey(indelInfo)) {
					int[] tmpNum = hashAlle.get(indelInfo);
					tmpNum[0]++;
				}
				else {
					int[] tmpNum = new int[]{1};
					hashAlle.put(indelInfo, tmpNum);
				}
			}
			else if (c == '*') {
				continue;
			}
			//mismatch
			else {
				type = TYPE_MISMATCH;
				String mismatchInfo = (pipInfo[i] + SEP + TYPE_MISMATCH).toLowerCase();
				if (hashAlle.containsKey(mismatchInfo)) {
					int[] tmpNum = hashAlle.get(mismatchInfo);
					tmpNum[0]++;
				}
				else {
					int[] tmpNum = new int[]{1};
					hashAlle.put(mismatchInfo, tmpNum);
				}
			}
		}
	}
	/**
	 * 给定序列和错配方式，返回所含有的reads堆叠数
	 * 因为本位点可能有多种错配，所以给定一个然后查找，看能找到几个
	 * 从hash表中获得
	 * @param seqInfo
	 * @param seqType
	 * @return
	 */
	public int getSeqType(String seqInfo, String seqType) {
		String tmpInfo = (seqInfo.trim()+SEP+seqType).toLowerCase();
		int[] num = hashAlle.get(tmpInfo);
		if (num == null) {
			return 0;
		}
		else {
			return num[0];
		}
	}
	
	/**
	 * 给定mapInfoSnpIndel，根据其<b>ref</b>,<b>refbase</b>，<b>thisbase</b>和<b>indel</b>的type，查找本位置某种type indel的数量。<br>
	 * 注意，输入的mapInfoSnpIndel必须只能有一种type。也就是只能指定一种形式的错配，<br>
	 * 此外输入的indel在查找的时候会将第一位删除，因为GATK出来的第一位是indel的前一位<br>
	 * 返回该种形式错配以及相应序列所含有的reads堆叠数
	 * 从hash表中获得
	 * @param mapInfoSnpIndel 正常的别的样本的信息
	 * @return 出错返回-1
	 */
	public int getSeqTypeNum(MapInfoSnpIndel mapInfoSnpIndel) {
		if (mapInfoSnpIndel.getRefSnpIndelStart() != getRefSnpIndelStart()) {
			logger.error("输入的查找位点不是同一个，本位点：" + getRefSnpIndelStart() + "查找位点：" + mapInfoSnpIndel.getRefSnpIndelStart());
			return -1;
		}
		String seqInfo = "";
		if (mapInfoSnpIndel.getType().equals(TYPE_DELETION)) {
			seqInfo = mapInfoSnpIndel.getRefBase().substring(1);
		}
		else if (mapInfoSnpIndel.getType().equals(TYPE_INSERT)) {
			seqInfo = mapInfoSnpIndel.getThisBase().substring(1);
		}
		else {
			seqInfo = mapInfoSnpIndel.getThisBase();
		}
		String tmpInfo = (seqInfo.trim()+SEP+mapInfoSnpIndel.getType()).toLowerCase();
		int[] num = hashAlle.get(tmpInfo);
		if (num == null)
			return 0;
		else
			return num[0];
	}
	/**
	 * 给定mapInfoSnpIndel，根据其<b>ref</b>,<b>refbase</b>，<b>thisbase</b>和<b>indel</b>的type，查找本位置某种type indel的数量。<br>
	 * 注意，输入的mapInfoSnpIndel必须只能有一种type。也就是只能指定一种形式的错配，<br>
	 * 此外输入的indel在查找的时候会将第一位删除，因为GATK出来的第一位是indel的前一位<br>
	 * 返回该种形式错配以及相应序列所含有的reads堆叠数
	 * 从hash表中获得
	 * @param mapInfoSnpIndel 正常的别的样本的信息
	 * @return 返回描述性的话:<br>
	 * refID \t  refStart \t refBase  \t  depth \t indelBase  \t indelNum   <br>
	 * 出错返回"";
	 */
	public String getSeqTypeNumStr(MapInfoSnpIndel mapInfoSnpIndel) {
		//也就是用别的位点的检测出的错配信息去查找本位点的错配信息，最后返回string结果
		//这里不能用mapInfoSnpIndel.getRefBase()来代替otherMap.getRefBase()
		//因为mapInfoSnpIndel.getRefBase()可能并不是真正的ref，特别为缺失的时候
		int num = getSeqTypeNum(mapInfoSnpIndel);
		if (num == -1) {
			return "";
		}
		String tmpResult = getRefID()+"\t"+getRefSnpIndelStart()+"\t" + mapInfoSnpIndel.getRefBase()+"\t" +getAllelic_depths_Ref();
		tmpResult = tmpResult + "\t" +mapInfoSnpIndel.getThisBase() + "\t" + getSeqTypeNum(mapInfoSnpIndel);
		return tmpResult;
	}
	
	
	/**
	 * 返回所有的非ref的基因以及对应的种类和数量
	 * list-string[3]
	 * 0：seq
	 * 1：type
	 * 2：num
	 * @return
	 */
	public ArrayList<String[]> getAllAllenInfo() {
		ArrayList<String[]> lsAllenInfo = new ArrayList<String[]>();
		for (Entry<String, int[]> entry : hashAlle.entrySet()) {
			String[] tmpInfo = new String[3];
			tmpInfo[0] = entry.getKey().split(SEP)[0];
			tmpInfo[1] = entry.getKey().split(SEP)[1];
			tmpInfo[2] = entry.getValue() + "";
			lsAllenInfo.add(tmpInfo);
		}
		return lsAllenInfo;
	}
	/**
	 * 我忘了这个是干啥的了，
	 * 获得实际的起点，indel需要加上1
	 * @return
	 */
	public int getStartRealaaa() {
		if (type.equals(TYPE_MISMATCH)) {
			return getStart();
		}
		else {
			return getStart() + 1;
		}
	}
	
	boolean isExon = false;
	public void setExon(boolean isExon) {
		this.isExon = isExon;
	}
	public boolean isExon() {
		return isExon;
	}
	public void setRefBase(String refBase) {
		this.refBase = refBase;
	}
	
	/**
	 * 移码，0，1，2三种
	 */
	int orfShift = 0; 
	/**
	 * snp或indel所在的转录本
	 */
	GffGeneIsoInfo gffGeneIsoInfo;
	ServSnpIndelRs servSnpIndelRs = new ServSnpIndelRs();
	
	/** AD
	 * Allelic depths for the ref and alt alleles in the order listed
	 */
	int Allelic_depths_Ref = 0;
	/**  AD
	 * Allelic depths for the ref and alt alleles in the order listed
	 */
	int Allelic_depths_Alt = 0;
	/**  DP
	 * Read Depth (only filtered reads used for calling
	 */
	int Read_Depth_Filtered = 0;
	/**
	 * GQ
	 * The Genotype Quality, as a Phred-scaled confidence at the true genotype is the one provided in GT. In diploid case, 
	 * if GT is 0/1, then GQ is really L(0/1) / (L(0/0) + L(0/1) + L(1/1)), where L is the likelihood of the NGS sequencing data
	 *  under the model of that the sample is 0/0, 0/1/, or 1/1. 
	 * 好像是碱基的质量
	 */
	double Genotype_Quality = 0;
	/**
	 * AF
	 * Allele Frequency, for each ALT allele, in the same order as listed
	 * 1表示纯合子，0.5表示杂合，多个样本可能结果会不同
	 */
	double Allele_Frequency = 1;
	/**
	 * AN
	 * 总共多少个等位，一般都为2个，ref一个，改变一个，出现3个的时候我暂时没见过，所以可以日志一下看看情况
	 */
	int Total_number_of_alleles = 2;
	/**
	 * SB, 
	 * How much evidence is there for Strand Bias (the variation being seen on only the forward or only the reverse strand) in the reads?
	 *  Higher SB values denote more bias (and therefore are more likely to indicate false positive calls).
	 */
	double Strand_Bias = 0;
	//##INFO=<ID=AB,Number=1,Type=Float,Description="Allele Balance for hets (ref/(ref+alt))">
	//##INFO=<ID=AC,Number=A,Type=Integer,Description="Allele count in genotypes, for each ALT allele, in the same order as listed">
	//##INFO=<ID=AF,Number=A,Type=Float,Description="Allele Frequency, for each ALT allele, in the same order as listed">
	//##INFO=<ID=AN,Number=1,Type=Integer,Description="Total number of alleles in called genotypes">
	//##INFO=<ID=BaseQRankSum,Number=1,Type=Float,Description="Z-score from Wilcoxon rank sum test of Alt Vs. Ref base qualities">
	//##INFO=<ID=DB,Number=0,Type=Flag,Description="dbSNP Membership">
	//##INFO=<ID=DP,Number=1,Type=Integer,Description="Filtered Depth">
	//##INFO=<ID=DS,Number=0,Type=Flag,Description="Were any of the samples downsampled?">
	//##INFO=<ID=Dels,Number=1,Type=Float,Description="Fraction of Reads Containing Spanning Deletions">
	//##INFO=<ID=FS,Number=1,Type=Float,Description="Phred-scaled p-value using Fisher's exact test to detect strand bias">
	//##INFO=<ID=HRun,Number=1,Type=Integer,Description="Largest Contiguous Homopolymer Run of Variant Allele In Either Direction">
	//##INFO=<ID=HaplotypeScore,Number=1,Type=Float,Description="Consistency of the site with at most two segregating haplotypes">
	//##INFO=<ID=InbreedingCoeff,Number=1,Type=Float,Description="Inbreeding coefficient as estimated from the genotype likelihoods per-sample when compared against the Hardy-Weinberg expectation">
	//##INFO=<ID=MQ,Number=1,Type=Float,Description="RMS Mapping Quality">
	//##INFO=<ID=MQ0,Number=1,Type=Integer,Description="Total Mapping Quality Zero Reads">
	//##INFO=<ID=MQRankSum,Number=1,Type=Float,Description="Z-score From Wilcoxon rank sum test of Alt vs. Ref read mapping qualities">
	//##INFO=<ID=QD,Number=1,Type=Float,Description="Variant Confidence/Quality by Depth">
	//##INFO=<ID=ReadPosRankSum,Number=1,Type=Float,Description="Z-score from Wilcoxon rank sum test of Alt vs. Ref read position bias">
	//##INFO=<ID=SB,Number=1,Type=Float,Description="Strand Bias">
	//##INFO=<ID=VQSLOD,Number=1,Type=Float,Description="Log odds ratio of being a true variant versus being false under the trained gaussian mixture model">
	//##INFO=<ID=culprit,Number=1,Type=String,Description="The annotation which was the worst performing in the Gaussian mixture model, likely the reason why the variant was filtered out">
    //##SelectVariants="analysis_type=SelectVariants input_file=[] sample_metadata=[] read_buffer_size=null phone_home=STANDARD read_filter=[] intervals=null excludeIntervals=null reference_sequence=/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa rodBind=[A_BWA_raw_SnpsIndels.vcf] rodToIntervalTrackName=null BTI_merge_rule=UNION nonDeterministicRandomSeed=false DBSNP=null downsampling_type=null downsample_to_fraction=null downsample_to_coverage=null baq=OFF baqGapOpenPenalty=40.0 performanceLog=null useOriginalQualities=false defaultBaseQualities=-1 validation_strictness=SILENT unsafe=null num_threads=1 interval_merging=ALL read_group_black_list=null processingTracker=null restartProcessingTracker=false processingTrackerStatusFile=null processingTrackerID=-1 allow_intervals_with_unindexed_bam=false disable_experimental_low_memory_sharding=false logging_level=INFO log_to_file=null help=false out=org.broadinstitute.sting.gatk.io.stubs.VCFWriterStub NO_HEADER=org.broadinstitute.sting.gatk.io.stubs.VCFWriterStub sites_only=org.broadinstitute.sting.gatk.io.stubs.VCFWriterStub sample_name=null sample_expressions=null sample_file=null select_expressions=[] excludeNonVariants=false excludeFiltered=false keepOriginalAC=false discordance= concordance= inputAF= keepAFSpectrum=false afFile= family_structure_file=null family_structure= mendelianViolation=false mendelianViolationQualThreshold=0.0 select_random_number=0 select_random_fraction=0.0 selectSNPs=true selectIndels=false outMVFile=null"
	//##UnifiedGenotyper="analysis_type=UnifiedGenotyper input_file=[A_BWA_recal.bam] sample_metadata=[] read_buffer_size=null phone_home=STANDARD read_filter=[] intervals=[/media/winE/Bioinformatics/snp/snp/target_intervals.bed/target_intervals.bed] excludeIntervals=null reference_sequence=/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa rodBind=[/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/snp/dbsnp_132.hg19_cope.vcf] rodToIntervalTrackName=null BTI_merge_rule=UNION nonDeterministicRandomSeed=false DBSNP=null downsampling_type=null downsample_to_fraction=null downsample_to_coverage=500 baq=OFF baqGapOpenPenalty=40.0 performanceLog=null useOriginalQualities=false defaultBaseQualities=-1 validation_strictness=SILENT unsafe=null num_threads=4 interval_merging=ALL read_group_black_list=null processingTracker=null restartProcessingTracker=false processingTrackerStatusFile=null processingTrackerID=-1 allow_intervals_with_unindexed_bam=false disable_experimental_low_memory_sharding=false logging_level=INFO log_to_file=null help=false genotype_likelihoods_model=BOTH p_nonref_model=EXACT heterozygosity=0.001 pcr_error_rate=1.0E-4 genotyping_mode=DISCOVERY output_mode=EMIT_VARIANTS_ONLY standard_min_confidence_threshold_for_calling=50.0 standard_min_confidence_threshold_for_emitting=10.0 noSLOD=false assume_single_sample_reads=null abort_at_too_much_coverage=-1 min_base_quality_score=17 min_mapping_quality_score=20 max_deletion_fraction=0.05 min_indel_count_for_genotyping=5 indel_heterozygosity=1.25E-4 indelGapContinuationPenalty=10.0 indelGapOpenPenalty=45.0 indelHaplotypeSize=80 doContextDependentGapPenalties=true getGapPenaltiesFromData=false indel_recal_file=indel.recal_data.csv indelDebug=false dovit=false GSA_PRODUCTION_ONLY=false exactCalculation=LINEAR_EXPERIMENTAL ignoreSNPAlleles=false output_all_callable_bases=false genotype=false out=org.broadinstitute.sting.gatk.io.stubs.VCFWriterStub NO_HEADER=org.broadinstitute.sting.gatk.io.stubs.VCFWriterStub sites_only=org.broadinstitute.sting.gatk.io.stubs.VCFWriterStub debug_file=null metrics_file=null annotation=[AlleleBalance, DepthOfCoverage, FisherStrand]"
	//##source=SelectVariants
	
	/** 该snp的质量 */
	String quality = "";
	/** 是否符合标准 */
	String Filter = "";
	public String getQuality() {
		return quality;
	}
	/**
	 * 设定该snp的质量
	 * GATK的第6列，从0计算为第5列
	 */
	public void setQuality(String quality) {
		this.quality = quality;
	}
	public void setSnpFilter(String snpINFO) {
		this.Filter = snpINFO;
	}
	public String getSnpINFO() {
		return Filter;
	}
	/**
	 * 设定thisSite测序深度
	 * @return
	 */
	public void setAllelicDepthsAlt(int Allelic_depths_Alt) {
		this. Allelic_depths_Alt = Allelic_depths_Alt;
	}
	/**
	 * 设定refSite测序深度
	 * @return
	 */
	public void setAllelicDepthsRef(int Allelic_depths_Ref) {
		this. Allelic_depths_Ref = Allelic_depths_Ref;
	}
	/**
	 * AF Allele Frequency, for each ALT allele, in the same order as listed 1表示纯合子，0.5表示杂合，多个样本可能结果会不同
	 */
	public double getAllele_Frequency() {
		return Allele_Frequency;
	}
	/**
	 * AD Allelic depths for the ref and alt alleles in the order listed
	 * @return
	 */
	public int getAllelic_depths_Alt() {
		return Allelic_depths_Alt;
	}
	/**
	 * AD Allelic depths for the ref and alt alleles in the order listed
	 * @return
	 */
	public int getAllelic_depths_Ref() {
		return Allelic_depths_Ref;
	}
	/**
	 * GQ The Genotype Quality, as a Phred-scaled confidence at the true genotype is the one provided in GT.
	 *  In diploid case, if GT is 0/1, then GQ is really L(0/1) / (L(0/0) + L(0/1) + L(1/1)), where L is the likelihood 
	 *  of the NGS sequencing data under the model of that the sample is 0/0, 0/1/, or 1/1. 好像是碱基的质量
	 * @return
	 */
	public double getGenotype_Quality() {
		return Genotype_Quality;
	}
	
	public int getRead_Depth_Filtered() {
		return Read_Depth_Filtered;
	}
	public void setFilter(String filter) {
		Filter = filter;
	}
	/**
	 * DBsnp的ID
	 * @return
	 */
	public String getSnpRsID() {
		return snpRsID;
	}
	/**
	 * SB, How much evidence is there for Strand Bias (the variation being seen
	 *  on only the forward or only the reverse strand) in the reads? Higher SB 
	 *  values denote more bias (and therefore are more likely to indicate false 
	 *  positive calls).
	 * @return
	 */
	public double getStrand_Bias() {
		return Strand_Bias;
	}
	/**
	 * 物种ID
	 * @return
	 */
	public int getTaxID() {
		return taxID;
	}
	/**
	 * AN 总共多少个等位，一般都为2个，ref一个，改变一个，出现3个的时候我暂时没见过，所以可以日志一下看看情况
	 * @return
	 */
	public int getTotal_number_of_alleles() {
		return Total_number_of_alleles;
	}
	/**
	 * Allele Balance for hets
	 * (ref/(ref+alt))
	 * @return
	 */
	public double getAllele_Balance_Hets() {
		return (double)Allelic_depths_Ref/(Allelic_depths_Ref+Allelic_depths_Alt);
	}
	/**
	 * 设置
	 * GT:AD:DP:GQ:PL	0/1:53,10:63:99:150,0,673
	 */
	public void setFlag(String flagTitle, String flagDetail)
	{
		String[] ssFlag = flagTitle.split(":");
		String[] ssValue = flagDetail.split(":");
		for (int i = 0; i < ssFlag.length; i++) {
			if (ssFlag[i].equals("AD")) {
				String[] info = ssValue[i].split(",");
				Allelic_depths_Ref = Integer.parseInt(info[0]);
				Allelic_depths_Alt = Integer.parseInt(info[1]); 
			}
			else if (ssFlag[i].equals("DP")) {
				Read_Depth_Filtered = Integer.parseInt(ssValue[i]); 
			}
			else if (ssFlag[i].equals("GQ")) {
				Genotype_Quality = Double.parseDouble(ssValue[i]);
			}
		}
	}
	/**
	 * 就看这三项：AF,AN,SB
	 *  AB=0.841;AC=1;AF=0.50;AN=2;BaseQRankSum=0.097;DP=63;Dels=0.00;FS=0.000;HRun=0;HaplotypeScore=0.0000;
	 *  给定GATKinfo，设定信息
	 * @param GATKInfo
	 */
	public void setBaseInfo(String GATKInfo) {
		String[] ssValue = GATKInfo.split(";");
		for (String string : ssValue) {
			String[] tmpInfo = string.split("=");
			if (tmpInfo[0].equals("AF")) {
				Allele_Frequency = Double.parseDouble(tmpInfo[1]);
			}
			else if (tmpInfo[0].equals("AN")) {
				Total_number_of_alleles = Integer.parseInt(tmpInfo[1]);
			}
			else if (tmpInfo[0].equals("SB")) {
				Strand_Bias =  Double.parseDouble(tmpInfo[1]);
			}
		}
	}
	/**
	 * 无用
	 */
	@Deprecated
	public int getFlagSite() {
		return flagLoc;
	}
	/**
	 * 实际的序列
	 * @return
	 */
	public String getThisBase() {
		return thisBase;
	}
	/**
	 * 参考序列
	 * @return
	 */
	public String getRefBase() {
		return refBase;
	}
	
	public String getRefAAseq() {
		return seqFasta.toStringAA();
	}
	
	/**
	 * 跟方向相关
	 * 给定序列和起始位点，用snp位点去替换序列，同时将本次替换是否造成移码写入orfshift
	 * @param thisBase 给定序列--该序列必须是正向，然后
	 * @param cis5to3 给定序列的正反向
	 * @param startLoc  在序列的哪一个点开始替换. 0表示插到最前面。1表示从第一个开始替换
	 * 如果ref为""，则将序列插入在startBias那个碱基的后面
	 * @return
	 */
	private SeqFasta replaceSnpIndel(String replace, int startLoc, int endLoc) {
		SeqFasta seqFasta = getSeqFasta().clone();
		if (seqFasta.toString().equals("")) {
			return new SeqFasta();
		}
		seqFasta.modifySeq(startLoc, endLoc, replace, false, false);
		//修改移码
		orfShift = Math.abs(replace.length() - refBase.length())%3;
		return seqFasta;
	}
	/** snp所在refnr上的位置 */
	int replaceLoc = 0;
	/**
	 * 用于snp位点的替换，也就是用新的snp或indel来替换老的位点
	 * @param replaceLoc 当序列为正向时的位置，所要替换的点在序列上的位置，内部根据方向自动颠倒
	 */
	public void setReplaceLoc(int replaceLoc) {
		this.replaceLoc = replaceLoc;
	}
	/**
	 * 如果一个位点有两个以上的snp，就可能会出错
	 * 获得本snp位置最多变异的AA序列
	 * 注意要通过{@link #setCis5to3(Boolean)}来设定 插入序列在基因组上是正向还是反向
	 * 还要通过{@link #setReplaceLoc(int)}来设定插入在refnr上的位置
	 * @return 没有的话就返回一个空的seqfasta
	 */
	public SeqFasta getThisAAnr() {
		int max = -1;
		String maxIndelType = "";//获得变异最大的点的序列
		for (Entry<String, int[]> entry : hashAlle.entrySet()) {
			if (entry.getValue()[0] > max) {
				maxIndelType = entry.getKey();
				max = entry.getValue()[0];
			}
		}
		String seq = maxIndelType.split(SEP)[0];
		if (cis5to3 != null && !cis5to3) {
			seq = SeqFasta.reservecom(seq);
		}
		if (gffGeneIsoInfo == null)
			return new SeqFasta();
		
		int startReplace = replaceLoc;
		int endReplace = startReplace + refBase.length() - 1;
		return replaceSnpIndel(seq, startReplace, endReplace);
	}
	
	/**
	 * 移码突变，注意调用前务必先调用
	 * {@link #getThisAAnr()}
	 * @param orfShift
	 */
	public int getOrfShift() {
		return orfShift;
	}

	/**
	 * snp或indel所在的转录本
	 * 同时设定setProp，cis5to3，和name，都用gffGeneIsoInfo的信息
	 */
	public void setGffIso(GffGeneIsoInfo gffGeneIsoInfo) {
		this.gffGeneIsoInfo = gffGeneIsoInfo;
		if (gffGeneIsoInfo == null) {
			return;
		}
		setProp( (double)gffGeneIsoInfo.getCod2TSSmRNA(getRefSnpIndelStart()) / (gffGeneIsoInfo.getCod2TSSmRNA(getRefSnpIndelStart())  - gffGeneIsoInfo.getCod2TESmRNA(getRefSnpIndelStart())) );
		setCis5to3(gffGeneIsoInfo.isCis5to3());
		setName(gffGeneIsoInfo.getName());
	}
	/**
	 * 获得所在的转录本
	 * @return
	 */
	public GffGeneIsoInfo getGffIso() {
		return gffGeneIsoInfo;
	}
	/**
	 * 判断另一个snp或者indel是不是与本mapInfo在同一个转录本中
	 * 两个mapInfoSnpIndel都必须有gffGeneIsoInfo设置好
	 * @param mapInfoSnpIndel
	 * @return
	 */
	public boolean isSameIso(MapInfoSnpIndel mapInfoSnpIndel) {
		if (gffGeneIsoInfo.equals(mapInfoSnpIndel.getGffIso())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	String snpRsID = "";
	/**
	 * 设定snpID，自动获得对应的DBsnp信息
	 * @param snpRsID
	 */
	public void setDBSnpID(String snpRsID) {
		if (snpRsID != null && !snpRsID.trim().equals("")) {
			if (!snpRsID.equals(snpRsID)) {
				logger.error("本dbspnID与输入的dbsnpID不对应："+snpRsID + snpIndelRs.getSnpRsID());
			}
		}
		this.snpRsID = snpRsID;
	}
	
	
	/**
	 * 设定DBsnp的信息，有设定flag就当snp，没有设定flag就当indel
	 * @param snpIndelRs
	 */
	private void setSnpIndelRs() {
		SnpIndelRs snpIndelRs = new SnpIndelRs();
		snpIndelRs.setChrID(refID);
		snpIndelRs.setTaxID(this.taxID);
		if (snpRsID != null && !snpRsID.equals("")) {
			snpIndelRs.setSnpRsID(snpRsID);
			this.snpIndelRs = servSnpIndelRs.querySnpIndelRs(snpIndelRs);
			return;
		}
		else
			return;
	}
	/**
	 * 如果在SNPDB中有记载，获得记载的信息
	 * @return
	 */
	public SnpIndelRs getSnpIndelRs() {
		if (snpIndelRs != null) {
			return snpIndelRs;
		}
		setSnpIndelRs();
		return snpIndelRs;
	}
	public String toString() {
		int flagLoc = this.flagLoc;
		if (!this.type.equals(TYPE_MISMATCH)) {
			flagLoc = flagLoc - 1; 
		}
		
		String refnr = getSeqFasta().toString();
		String refaa = getSeqFasta().toStringAA(false);
		String thisnr =  getThisAAnr().toString();
		String thisaa = getThisAAnr().toStringAA(false);
		
		
		String result = refID + "\t" + refSnpIndelStart + "\t" + refBase + "\t" + this.Allelic_depths_Ref + "\t" + thisBase + "\t" + 
		this.Allelic_depths_Alt + "\t" + quality + "\t" + this.Filter + "\t" + this.Allele_Frequency + "\t" + getAllele_Balance_Hets() + "\t" + isExon()+"\t" + prop +"\t"+
		refnr +"\t"+refaa + "\t" + thisnr +"\t"+thisaa;
		if (refaa.length() ==3  && thisaa.length() == 3) {
			result = result + "\t" + SeqFasta.cmpAAquality(refaa, thisaa);
		}
		else {
			result = result + "\t" + "";
		}
		result = result + "\t" + this.getOrfShift();
		result = result + "\t" + snpRsID;
		if (getGffIso() != null) {
			result = result + "\t" + getGffIso().getName();
			GeneID copedID = new GeneID(getGffIso().getName(), taxID, false);
			result = result + "\t" + copedID.getSymbol() +"\t"+copedID.getDescription();
		}
		else
			result = result + "\t \t \t " ;
		return result;
	}
	/**
	 * 用于比较的，从小到大比
	 * 先比refID，然后比start，end，或者比flag或者比score
	 * 比score的时候就不考虑refID了
	 */
	@Override
	public int compareTo(MapInfo map) {
		MapInfoSnpIndel mapInfoOther = (MapInfoSnpIndel) map;
		if (compareInfo == COMPARE_LOCFLAG) {
			int i = refID.compareTo(mapInfoOther.refID);
			if (i != 0) {
				return i;
			}
			if (flagLoc == mapInfoOther.flagLoc) {
				return 0;
			}
			if (min2max) {
				return flagLoc < mapInfoOther.flagLoc ? -1:1;
			}
			else {
				return flagLoc > mapInfoOther.flagLoc ? -1:1;
			}
		}
		else if (compareInfo == COMPARE_LOCSITE) {
			int i = refID.compareTo(mapInfoOther.refID);
			if (i != 0) {
				return i;
			}
			if (refSnpIndelStart == mapInfoOther.refSnpIndelStart) {
				if (refSnpIndelEnd == mapInfoOther.refSnpIndelEnd) {
					return 0;
				}
				if (min2max) {
					return refSnpIndelEnd < mapInfoOther.refSnpIndelEnd ? -1:1;
				}
				else {
					return refSnpIndelEnd > mapInfoOther.refSnpIndelEnd ? -1:1;
				}
			}
			if (min2max) {
				return refSnpIndelStart < mapInfoOther.refSnpIndelStart ? -1:1;
			}
			else {
				return refSnpIndelStart > mapInfoOther.refSnpIndelStart ? -1:1;
			}
		}
		else if (compareInfo == COMPARE_SCORE) {
			if (score == mapInfoOther.score) {
				return 0;
			}
			if (min2max) {
				return score < mapInfoOther.score ? -1:1;
			}
			else {
				return score > mapInfoOther.score ? -1:1;
			}
		}
		return 0;
	}
	/////////////////////////////////////// 静态方法，获得所有指定区域的位点的信息 ///////////////////////////////
	public static String getMyTitle() {
		String result = "ChrID\tSnpLoc\tRefBase\tAllelic_depths_Ref\tThisBase\tAllelic_depths_Alt \tQuality\tFilter\tAllele_Frequency\tAllele_Balance_Hets()\tIsInExon\tDistance_To_Start\t" + 
		"RefAAnr\tRefAAseq\tThisAAnr\tThisAASeq\tAA_chemical_property\tOrfShift\tSnpDB_ID\tGeneAccID\tGeneSymbol\tGeneDescription";
		return result;
	}
	/**
	 * 给定选中的mapInfo，读取samtools产生的pileup file获得每个位点的具体信息
	 * @param lsSite 仅包含refbase和坐标信息
	 * @param txtSamToolsFile samtools产生的文件
	 */
	public static void getSiteInfo(List<MapInfoSnpIndel> lsSite, String txtSamToolsFile) {
		// 排序，以提高效率
		MapInfo.setCompType(MapInfo.COMPARE_LOCSITE);
		Collections.sort(lsSite);
		/** 每个chrID对应一组mapinfo，也就是一个list */
		HashMap<String, ArrayList<MapInfoSnpIndel>> hashChrIDMapInfo = new LinkedHashMap<String, ArrayList<MapInfoSnpIndel>>();
		// 按照chr位置装入hash表
		for (MapInfoSnpIndel mapInfo : lsSite) {
			ArrayList<MapInfoSnpIndel> lsMap = hashChrIDMapInfo.get(mapInfo.getRefID());
			if (lsMap == null) {
				lsMap = new ArrayList<MapInfoSnpIndel>();
				hashChrIDMapInfo.put(mapInfo.getRefID(), lsMap);
			}
			lsMap.add(mapInfo);
		}
		for (ArrayList<MapInfoSnpIndel> lsMapInfos : hashChrIDMapInfo.values()) {
			Collections.sort(lsMapInfos);
		}
		TxtReadandWrite txtReadSam = new TxtReadandWrite(txtSamToolsFile, false);
		String tmpChrID = ""; ArrayList<MapInfoSnpIndel> lsMapInfos = null;
		int mapInfoIndex = 0;// 依次进行下去
		for (String content : txtReadSam.readlines()) {
			String[] ss = content.split("\t");
			if (!ss[0].equals(tmpChrID)) {
				tmpChrID = ss[0];
				lsMapInfos = hashChrIDMapInfo.get(tmpChrID);
				mapInfoIndex = 0;
			}
			if (lsMapInfos == null) {
				logger.error("出现未知 chrID：" + tmpChrID);
				break;
			}
				
			//所有lsMapInfos中的信息都查找完毕了
			if (mapInfoIndex >= lsMapInfos.size()) {
				continue;
			}
			//一行一行找下去，直到找到所需要的位点
			if (Integer.parseInt(ss[1]) < lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
				continue;
			} else if (Integer.parseInt(ss[1]) == lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
				lsMapInfos.get(mapInfoIndex).setSamToolsPilup(content);
				mapInfoIndex++;
			} else {
				while (mapInfoIndex < lsMapInfos.size()&& Integer.parseInt(ss[1]) > lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
					mapInfoIndex++;
				}
				if (mapInfoIndex >= lsMapInfos.size()) {
					continue;
				} else if (Integer.parseInt(ss[1]) == lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
					lsMapInfos.get(mapInfoIndex).setSamToolsPilup(content);
					mapInfoIndex++;
				}
			}
		}
		logger.info("readOverFile:" + txtSamToolsFile);
	}

}
