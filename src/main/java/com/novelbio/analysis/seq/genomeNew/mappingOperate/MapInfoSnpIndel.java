package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.text.html.parser.Entity;

import org.apache.log4j.Logger;
import org.omg.CosNaming._BindingIteratorImplBase;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.tools.Mas3.getProbID;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.SepSign;
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
public class MapInfoSnpIndel implements Comparable<MapInfoSnpIndel>, Cloneable{
	public static final int TYPE_INSERT = 40;
	public static final int TYPE_DELETION = 30;
	public static final int TYPE_MISMATCH = 20;
	public static final int TYPE_CORRECT = 10;
//	public static final String SEP = "@//@";
	/**  snp的类型，TYPE_INSERT等 */
	String sampleName = "";;
	/** 
	 * <b>里面都是正向的序列</b>
	 * 该位置可能有不止一种的插入缺失或是碱基替换类型，那么就用该hash表来存储这么多种信息<br>
	 *Key: referenceSeq + SepSign.SEP_ID + thisSeq + SepSign.SEP_ID + snpType <br>
	 * value: 数量，用数组仅仅为了能够传递地址  */
	HashMap<String, SiteSnpIndelInfo> mapAllen2Num = new HashMap<String, SiteSnpIndelInfo>();

	int taxID = 0;
	private static Logger logger = Logger.getLogger(MapInfoSnpIndel.class);
	String chrID;
	String refBase = "";
	/** snp在基因中的位置，0-1之间，0.1表示snp在基因长度*0.1的位置处  */
	double prop = 0;
	/** 本snp或indel所在的起点 */
	int refSnpIndelStart = 0;
	/**
	 * snp或indel所在的转录本
	 */
	GffGeneIsoInfo gffGeneIsoInfo;	
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
	/**
	 * @param taxID 物种
	 * @param chrID 染色体号
	 * @param snpLoc snp位点
	 * @param referenceSeq ref的序列
	 * @param thisSeq 本序列
	 */
	public MapInfoSnpIndel(GffChrAbs gffChrAbs,String chrID, int snpLoc, String referenceSeq, String thisSeq) {
		this.taxID = gffChrAbs.getTaxID();
		this.chrID = chrID;
		//flagLoc有东西说明是snp
		this.refSnpIndelStart = snpLoc;
	    this.refBase = referenceSeq.charAt(0) + "";
	    setGffIso(gffChrAbs);
	    
	    SiteSnpIndelInfo siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, gffChrAbs, referenceSeq, thisSeq);
	    mapAllen2Num.put(siteSnpIndelInfo.getSiteTypeInfo(), siteSnpIndelInfo);
	}
	/**
	 * @param taxID 物种
	 * @param chrID 染色体号
	 * @param snpLoc snp位点
	 * @param referenceSeq ref的序列
	 * @param thisSeq 本序列
	 */
	public MapInfoSnpIndel(int taxID, GffChrAbs gffChrAbs, String pileUpLine) {
		this.taxID = taxID;
		setSamToolsPilup(pileUpLine, gffChrAbs);
	}
	/**
	 * @param taxID 物种
	 * @param chrID 染色体号
	 * @param snpLoc snp位点
	 * @param referenceSeq ref的序列
	 * @param thisSeq 本序列
	 */
	public MapInfoSnpIndel(String chrID, int snpLoc) {
		this.chrID = chrID;
		//flagLoc有东西说明是snp
		this.refSnpIndelStart = snpLoc;
	}
	/**
	 * snp或indel所在的转录本
	 * 同时设定setProp，cis5to3，和name，都用gffGeneIsoInfo的信息
	 */
	private void setGffIso(GffChrAbs gffChrAbs) {
		if (gffChrAbs == null)
			return;

		this.gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLocation(chrID, refSnpIndelStart).getCodInExonIso();
		if (gffGeneIsoInfo == null) {
			return;
		}
		setProp( (double)gffGeneIsoInfo.getCod2TSSmRNA(getRefSnpIndelStart()) / (gffGeneIsoInfo.getCod2TSSmRNA(getRefSnpIndelStart())  - gffGeneIsoInfo.getCod2TESmRNA(getRefSnpIndelStart())) );
	}
	/**
	 * refBase在基因中的位置，0-1之间，0.1表示snp在基因长度*0.1的位置处
	 * 越小越靠近头部
	 * 0-1之间
	 */
	private void setProp(double prop) {
		this.prop = prop;
	}
	/**
	 *  在已有refbase信息的基础上，查找该refSnpIndelStart位点有哪些indel或snp
	 *  找到的indel所对应的refbase可能和原来的refbase不一样
	 * @param samString
	 */
	public void setSamToolsPilup(String samString, GffChrAbs gffChrAbs) {
		String[] ss = samString.split("\t");
		this.refSnpIndelStart = Integer.parseInt(ss[1]);//本行舍不设定都无所谓，因为输入的时候就是要求相同的ID
		this.refBase = ss[2];
		this.Read_Depth_Filtered = Integer.parseInt(ss[3]);
		this.chrID = ss[0];
		setGffIso(gffChrAbs);
		setAllenInfo(refBase, ss[4], gffChrAbs);
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
	private void setAllenInfo(String refBase, String pileUpInfo, GffChrAbs gffChrAbs) {
		Allelic_depths_Ref = 0; mapAllen2Num = new HashMap<String, SiteSnpIndelInfo>();
		String referenceSeq = refBase, thisSeq = refBase;
		char[] pipInfo = pileUpInfo.toCharArray();
		for (int i = 0; i < pipInfo.length; i++) {
			char c = pipInfo[i];
			if (c == '$') continue;
			if (c == '^' ) {
				i ++; continue;
			}
			else if (c == 'n' || c== 'N') {
				continue;
			}
			else if (c == ',' || c == '.') {
				Allelic_depths_Ref++; continue;
			}
			else if (c == '+' || c == '-') {
				int tmpInDelNum = 0;
				i ++;
				//如果开头是“+”号，则获得+号后的数字，也就是indel的长度
				for (; i < pipInfo.length; i++) {
					char tmpNum = pipInfo[i];
					//转换为数字字符
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
				int type = TYPE_CORRECT;
				if (c == '+') {
					type = TYPE_INSERT;
					referenceSeq = refBase;
					thisSeq = refBase + indel;
				}
				else {
					type = TYPE_DELETION;
					referenceSeq = refBase + indel;
					thisSeq = refBase;
				}
				SiteSnpIndelInfo siteSnpIndelInfo = null;
				String indelInfo = (referenceSeq + SepSign.SEP_ID + thisSeq + SepSign.SEP_ID + type).toLowerCase();
				
				if (mapAllen2Num.containsKey(indelInfo)) {
					siteSnpIndelInfo = mapAllen2Num.get(indelInfo);
					siteSnpIndelInfo.addThisBaseNum();
				}
				else {
					siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, gffChrAbs, referenceSeq, thisSeq);
					siteSnpIndelInfo.setThisBaseNum(1);
					mapAllen2Num.put(indelInfo, siteSnpIndelInfo);
				}
			}
			else if (c == '*') {
				continue;
			}
			//mismatch
			else {
				SiteSnpIndelInfo siteSnpIndelInfo = null;
				thisSeq = pipInfo[i] + "";
				String mismatchInfo = (refBase + SepSign.SEP_ID + thisSeq + SepSign.SEP_ID + TYPE_MISMATCH).toLowerCase();
				if (mapAllen2Num.containsKey(mismatchInfo)) {
					siteSnpIndelInfo = mapAllen2Num.get(mismatchInfo);
					siteSnpIndelInfo.addThisBaseNum();
				}
				else {
					siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, gffChrAbs, refBase, thisSeq);
					siteSnpIndelInfo.setThisBaseNum(1);
					mapAllen2Num.put(mismatchInfo, siteSnpIndelInfo);
				}
			}
		}
	}
	public String getRefID() {
		return chrID;
	}

	/**
	 * 获得snp或indel在ref上的起点，实际位点
	 * @return
	 */
	public int getRefSnpIndelStart() {
		return refSnpIndelStart;
	}
	/**
	 * 给定序列和错配方式，返回所含有的reads堆叠数
	 * 因为本位点可能有多种错配，所以给定一个然后查找，看能找到几个
	 * 从hash表中获得
	 * @param referenceSeq
	 * @param thisSeq
	 * @param snpType
	 * @return
	 */
	public SiteSnpIndelInfo getSnpIndel(String referenceSeq, String thisSeq, int snpType) {
		String tmpInfo = (referenceSeq + SepSign.SEP_ID + thisSeq + SepSign.SEP_ID + snpType ).toLowerCase();
		SiteSnpIndelInfo siteSnpIndelInfo = mapAllen2Num.get(tmpInfo);
		return siteSnpIndelInfo;
	}
	/**
	 * 给定序列和错配方式，返回所含有的reads堆叠数
	 * 因为本位点可能有多种错配，所以给定一个然后查找，看能找到几个
	 * 从hash表中获得
	 * @param referenceSeq
	 * @param thisSeq
	 * @param snpType
	 * @return
	 */
	public SiteSnpIndelInfo getSnpIndelNum(SiteSnpIndelInfo siteSnpIndelInfo) {
		return mapAllen2Num.get(siteSnpIndelInfo.getSiteTypeInfo());
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
	public SiteSnpIndelInfo getSnpIndelNum(MapInfoSnpIndel mapInfoSnpIndel) {
		if (mapInfoSnpIndel.getRefSnpIndelStart() != getRefSnpIndelStart()) {
			logger.error("输入的查找位点不是同一个，本位点：" + getRefSnpIndelStart() + "查找位点：" + mapInfoSnpIndel.getRefSnpIndelStart());
			return null;
		}
		return getBigAllenInfo();
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
		SiteSnpIndelInfo siteSnpIndelInfoQuery = mapInfoSnpIndel.getBigAllenInfo();
		return getSeqTypeNumStr(siteSnpIndelInfoQuery);
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
	public String getSeqTypeNumStr(SiteSnpIndelInfo siteSnpIndelInfoQuery) {
		//也就是用别的位点的检测出的错配信息去查找本位点的错配信息，最后返回string结果
		//这里不能用mapInfoSnpIndel.getRefBase()来代替otherMap.getRefBase()
		//因为mapInfoSnpIndel.getRefBase()可能并不是真正的ref，特别为缺失的时候
		SiteSnpIndelInfo siteSnpIndelInfo = getSnpIndelNum(siteSnpIndelInfoQuery);
		if (siteSnpIndelInfo == null) {
			return "";
		}
		String tmpResult = getRefID()+"\t"+getRefSnpIndelStart()+"\t" + siteSnpIndelInfoQuery.getReferenceSeq()+"\t" +getAllelic_depths_Ref();
		tmpResult = tmpResult + "\t" +siteSnpIndelInfo.getThisSeq() + "\t" + siteSnpIndelInfo.getThisBaseNum();
		return tmpResult;
	}
	/**
	 * 返回数量最大的snp位点
	 */
	public SiteSnpIndelInfo getBigAllenInfo() {
		ArrayList<SiteSnpIndelInfo> lsAllenInfo = ArrayOperate.getArrayListValue(mapAllen2Num);
		Collections.sort(lsAllenInfo, Collections.reverseOrder());
		if (lsAllenInfo.size() > 0) {
			return lsAllenInfo.get(0);
		}
		return null;
	}
	/**
	 * 返回所有的非ref的基因以及对应的种类和数量 
	 */
	public ArrayList<SiteSnpIndelInfo> getLsAllenInfoSortBig2Small() {
		ArrayList<SiteSnpIndelInfo> lsAllenInfo = ArrayOperate.getArrayListValue(mapAllen2Num);
		Collections.sort(lsAllenInfo, Collections.reverseOrder());
		return lsAllenInfo;
	}
	public void setRefBase(String refBase) {
		this.refBase = refBase;
	}
	/**
	 * snp在基因长度的百分比
	 * 越小越靠近头部
	 */
	public double getProp() {
		return prop;
	}
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
	 * 参考序列
	 * @return
	 */
	public String getRefBase() {
		return refBase;
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
	/**
	 * 用于比较的，从小到大比
	 * 先比refID，然后比start，end，或者比flag或者比score
	 * 比score的时候就不考虑refID了
	 */
	public int compareTo(MapInfoSnpIndel mapInfoOther) {
		int i = chrID.compareTo(mapInfoOther.chrID);
		if (i != 0) {
			return i;
		}
		Integer site1 = refSnpIndelStart;
		Integer site2 = mapInfoOther.refSnpIndelStart;
		return site1.compareTo(site2);
	}
	/**
	 * 尚未实现
	 */
	public MapInfoSnpIndel clone() {
		MapInfoSnpIndel mapInfoSnpIndel;
		try {
			//TODO
			mapInfoSnpIndel = (MapInfoSnpIndel) super.clone();
			mapInfoSnpIndel.Allele_Frequency = mapInfoSnpIndel.Allele_Frequency;
			return mapInfoSnpIndel;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.error("克隆出错");
		return null;
	}
	/**
	 * 给定选中的mapInfo，读取samtools产生的pileup file获得每个位点的具体信息
	 * @param lsSite 仅包含refbase和坐标信息
	 * @param samToolsPleUpFile samtools产生的文件
	 */
	public static void getSiteInfo(List<MapInfoSnpIndel> lsSite, String samToolsPleUpFile, GffChrAbs gffChrAbs) {
		/** 每个chrID对应一组mapinfo，也就是一个list */
		HashMap<String, ArrayList<MapInfoSnpIndel>> mapSortedChrID2MapInfo = sortLsMapInfoSnpIndel(lsSite);
		getSiteInfo(mapSortedChrID2MapInfo, samToolsPleUpFile, gffChrAbs);
	}
	
	/**
	 * 给定选中的mapInfo，读取samtools产生的pileup file获得每个位点的具体信息
	 * @param mapSortedChrID2LsMapInfo LsMapInfo排过序的list
	 * @param samToolsPleUpFile
	 * @param gffChrAbs
	 * @return 新建一个hash表然后返回，这个hash表与输入的表是deep copy关系
	 */
	public static HashMap<String, ArrayList<MapInfoSnpIndel>> getSiteInfo(HashMap<String, ArrayList<MapInfoSnpIndel>> mapSortedChrID2LsMapInfo, String samToolsPleUpFile, GffChrAbs gffChrAbs) {
		HashMap<String, ArrayList<MapInfoSnpIndel>> mapSortedChrID2LsMapInfoResult = copyHashMap(mapSortedChrID2LsMapInfo);
		/** 每个chrID对应一组mapinfo，也就是一个list */
		TxtReadandWrite txtReadSam = new TxtReadandWrite(samToolsPleUpFile, false);
		String tmpChrID = ""; ArrayList<MapInfoSnpIndel> lsMapInfos = null; ArrayList<MapInfoSnpIndel> lsMapInfosNew = null;
		int mapInfoIndex = 0;// 依次进行下去
		for (String samtoolsLine : txtReadSam.readlines()) {
			String[] ss = samtoolsLine.split("\t");
			if (!ss[0].equals(tmpChrID)) {
				tmpChrID = ss[0];
				lsMapInfos = mapSortedChrID2LsMapInfoResult.get(tmpChrID);
				mapInfoIndex = 0;
				if (lsMapInfos == null) {
					logger.info("出现未知 chrID：" + tmpChrID);
					continue;
				}
			}
			
			if (lsMapInfos == null) continue;
			//所有lsMapInfos中的信息都查找完毕了
			if (mapInfoIndex >= lsMapInfos.size()) continue;
			
			//一行一行找下去，直到找到所需要的位点
			if (Integer.parseInt(ss[1]) < lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
				continue;
			} else if (Integer.parseInt(ss[1]) == lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
				lsMapInfos.get(mapInfoIndex).setSamToolsPilup(samtoolsLine, gffChrAbs);
				mapInfoIndex++;
			} else {
				while (mapInfoIndex < lsMapInfos.size()&& Integer.parseInt(ss[1]) > lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
					mapInfoIndex++;
				}
				if (mapInfoIndex >= lsMapInfos.size()) {
					continue;
				} else if (Integer.parseInt(ss[1]) == lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
					lsMapInfos.get(mapInfoIndex).setSamToolsPilup(samtoolsLine, gffChrAbs);
					mapInfoIndex++;
				}
			}
		}
		logger.info("readOverFile:" + samToolsPleUpFile);
		return mapSortedChrID2LsMapInfoResult;
	}
	private static HashMap<String, ArrayList<MapInfoSnpIndel>> copyHashMap(HashMap<String, ArrayList<MapInfoSnpIndel>> mapSortedChrID2LsMapInfo) {
		HashMap<String, ArrayList<MapInfoSnpIndel>> mapResult = new HashMap<String, ArrayList<MapInfoSnpIndel>>();
		for (Entry<String, ArrayList<MapInfoSnpIndel>> entry : mapSortedChrID2LsMapInfo.entrySet()) {
			ArrayList<MapInfoSnpIndel> lsNew = new ArrayList<MapInfoSnpIndel>();
			ArrayList<MapInfoSnpIndel> lsOld = entry.getValue();
			for (MapInfoSnpIndel mapInfoSnpIndel : lsOld) {
				MapInfoSnpIndel mapInfoSnpIndelNew = new MapInfoSnpIndel(mapInfoSnpIndel.getRefID(), mapInfoSnpIndel.getRefSnpIndelStart());
				lsNew.add(mapInfoSnpIndelNew);
			}
			mapResult.put(entry.getKey(), lsNew);
		}
		return mapResult;
	}
	private static HashMap<String, ArrayList<MapInfoSnpIndel>> sortLsMapInfoSnpIndel(List<MapInfoSnpIndel> lsSite) {
		/** 每个chrID对应一组mapinfo，也就是一个list */
		HashMap<String, ArrayList<MapInfoSnpIndel>> hashChrIDMapInfo = new LinkedHashMap<String, ArrayList<MapInfoSnpIndel>>();
		// 按照chr位置装入hash表
		for (MapInfoSnpIndel mapInfoSnpIndel : lsSite) {
			ArrayList<MapInfoSnpIndel> lsMap = hashChrIDMapInfo.get(mapInfoSnpIndel.getRefID());
			if (lsMap == null) {
				lsMap = new ArrayList<MapInfoSnpIndel>();
				hashChrIDMapInfo.put(mapInfoSnpIndel.getRefID(), lsMap);
			}
			lsMap.add(mapInfoSnpIndel);
		}
		for (ArrayList<MapInfoSnpIndel> lsMapInfos : hashChrIDMapInfo.values()) {
			Collections.sort(lsMapInfos);
		}
		return hashChrIDMapInfo;
	}
}
