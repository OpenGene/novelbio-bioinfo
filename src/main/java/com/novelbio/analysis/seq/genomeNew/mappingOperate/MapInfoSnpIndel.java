package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.tools.Mas3.getProbID;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.SnpIndelRs;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.database.service.servgeneanno.ServSnpIndelRs;
import com.novelbio.database.updatedb.database.CopeDBSnp132;
/**
 * 有设定flag就当snp，没有设定flag就当indel
 * @author zong0jie
 *
 */
public class MapInfoSnpIndel extends MapInfo{
	static final String TYPE_INSERT = "insert";
	static final String TYPE_DELETION = "deletion";
	static final String TYPE_MISMATCH = "mismatch";
	static final String SEP = "@//@";
	/**
	 * snp的类型，TYPE_INSERT等
	 */
	String type = "";
	/**
	 * allen的碱基+sep+Type
	 * 2: 数量
	 */
	HashMap<String, int[]> hashAlle = new HashMap<String, int[]>();
	/**
	 * 输入sam那一行，然后填充本类
	 * @param samString
	 */
	public MapInfoSnpIndel(String samString)
	{
		super(samString.split("\t")[0]);
		String[] ss = samString.split("\t");
		this.refID = ss[0];
		this.startLoc = Integer.parseInt(ss[1]);
		this.refBase = ss[2];
		this.Read_Depth_Filtered = Integer.parseInt(ss[3]);
		setAllenInfo(ss[4]);
	}
	
	public void setSamToolsPilup(String samString)
	{
		String[] ss = samString.split("\t");
		this.refID = ss[0];
//		this.startLoc = Integer.parseInt(ss[1]);
		this.refBase = ss[2];
		this.Read_Depth_Filtered = Integer.parseInt(ss[3]);
		setAllenInfo(ss[4]);
	}
	/**
	 * snp初始化
	 * @param taxID
	 * @param chrID
	 * @param startLoc
	 * @param endLoc
	 */
	public MapInfoSnpIndel(int taxID,String chrID, int startLoc, String refBase, String thisBase) {
		super(chrID);
		this.taxID = taxID;
		//flagLoc有东西说明是snp
		if (refBase.trim().length() == 1 && thisBase.trim().length() == 1) {
			this.flagLoc = startLoc;
			this.startLoc = startLoc;
		}
		else {
			this.startLoc = startLoc;
			this.endLoc = startLoc+ refBase.trim().length() - 1;
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
	}
	/**
	 * 返回snp的类型，TYPE_INSERT等
	 */
	public String getType()
	{
		return this.type;
	}
	
	private void setAllenInfo(String pipeUpInfo)
	{
		char[] pipInfo = pipeUpInfo.toCharArray();
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
				if (c == '+') {
					type = TYPE_INSERT;
				}
				else {
					type = TYPE_DELETION;
				}
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
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 给定序列和错配方式，返回所含有的reads堆叠数
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
	 * 给定序列和错配方式，返回所含有的reads堆叠数
	 * 从hash表中获得
	 * @param seqInfo
	 * @param seqType
	 * @return
	 */
	public int getSeqType(MapInfoSnpIndel mapInfoSnpIndel) {
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
		if (num == null) {
			return 0;
		}
		else {
			return num[0];
		}
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
	
	
	public int getStartReal() {
		if (type.equals(TYPE_MISMATCH)) {
			return getStart();
		}
		else {
			return getStart() + 1;
		}
	}
	
	
	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// 静态方法，获得所有指定区域的位点的信息 /////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 给定选中的mapInfo，以及samtools产生的pileup file
	 * 获得每个位点的具体信息
	 */
	public static void getSiteInfo(List<MapInfoSnpIndel> lsSite, String txtSamToolsFile) {
		MapInfo.setCompType(MapInfo.COMPARE_LOCSITE);
		Collections.sort(lsSite);
		/**
		 * 每个chrID对应一组mapinfo
		 */
		HashMap<String, ArrayList<MapInfoSnpIndel>> hashChrIDMapInfo = new LinkedHashMap<String, ArrayList<MapInfoSnpIndel>>();
		//按照chr位置装入hash表
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
		int mapInfoIndex = 0;//依次进行下去
		for (String content : txtReadSam.readlines()) {
			String[] ss = content.split("\t");
			if (!ss[0].equals(tmpChrID)) {
				tmpChrID = ss[0];
				lsMapInfos = hashChrIDMapInfo.get(tmpChrID);
				mapInfoIndex = 0;
			}
			
			if (Integer.parseInt(ss[1]) == 9714530) {
				System.out.println("stop");
			}
			
			if (lsMapInfos == null) {
				break;
			}
			if (mapInfoIndex >= lsMapInfos.size()) {
				continue;
			}
			if (Integer.parseInt(ss[1]) < lsMapInfos.get(mapInfoIndex).getStart()) {
				continue;
			}
			else if (Integer.parseInt(ss[1]) == lsMapInfos.get(mapInfoIndex).getStart()) {
				if (lsMapInfos.get(mapInfoIndex).getType().equals(TYPE_INSERT)) {
					System.out.println("stop");
				}
				lsMapInfos.get(mapInfoIndex).setSamToolsPilup(content);
				mapInfoIndex++;
			}
			else {
				while (mapInfoIndex < lsMapInfos.size() && Integer.parseInt(ss[1]) > lsMapInfos.get(mapInfoIndex).getStart() ) {
					mapInfoIndex++;
				}
				if (mapInfoIndex >= lsMapInfos.size()) {
					continue;
				}
				else if (Integer.parseInt(ss[1]) == lsMapInfos.get(mapInfoIndex).getStart()) {
					lsMapInfos.get(mapInfoIndex).setSamToolsPilup(content);
					mapInfoIndex++;
				}
			}
		}
		System.out.println("readOverFile:" + txtSamToolsFile);
	}
	
	
	
	
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	boolean isExon = true;
	public void setExon(boolean isExon) {
		this.isExon = isExon;
	}
	public boolean isExon() {
		return isExon;
	}
	/**
	 * snp在基因长度的百分比
	 */
	double prop = 0;
	/**
	 * snp在基因长度的百分比
	 * 越小越靠近头部
	 * 0-1之间
	 */
	public void setProp(double prop) {
		this.prop = prop;
	}
	/**
	 * snp在基因长度的百分比
	 * 越小越靠近头部
	 */
	public double getProp() {
		return prop;
	}
	
	int taxID = 0;
	private static Logger logger = Logger.getLogger(MapInfoSnpIndel.class);
	SnpIndelRs snpIndelRs;
	String refAAseq = "";
	String thisAaSeq = "";
	String thisBase = "";
	String refBase = "";
	String thisAAnr = "";
	String refAAnr = "";
	
	public void setThisAAnr(String thisAAnr) {
		this.thisAAnr = thisAAnr;
	}
	public String getThisAAnr() {
		return thisAAnr;
	}
	public void setRefAAnr(String refAAnr) {
		this.refAAnr = refAAnr;
	}
	public String getRefAAnr() {
		return refAAnr;
	}
	public void setRefBase(String refBase) {
		this.refBase = refBase;
	}
	public void setThisBase(String thisBase) {
		this.thisBase = thisBase;
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
	
	/**
	 * AD
	 * Allelic depths for the ref and alt alleles in the order listed
	 */
	int Allelic_depths_Ref = 0;
	/**
	 * AD
	 * Allelic depths for the ref and alt alleles in the order listed
	 */
	int Allelic_depths_Alt = 0;
	/**
	 * DP
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
	
	/**
	 * 该snp的质量
	 */
	String quality = "";
	/**
	 * 是否符合标准
	 */
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
	 * 返回snp位点，没有snp则返回负数，表示这是一个indel位置
	 */
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
	public void setRefAAseq(String refAAseq) {
		this.refAAseq = refAAseq;
	}
	
	public String getRefAAseq() {
		return refAAseq;
	}
	/**
	 * 改变的氨基酸序列
	 * @param aaSeq
	 */
	public void setThisAaSeq(String thisAaSeq) {
		if (thisAaSeq != null && !thisAaSeq.equals("")) {
			this.thisAaSeq = thisAaSeq;
		}
	}
	/**
	 * 改变的氨基酸序列
	 * @param aaSeq
	 */
	public String getThisAaSeq() {
		return thisAaSeq;
	}
	/**
	 * 该区域的氨基酸序列
	 * @param aaSeq
	 */
	public String getAaSeq() {
		return thisAaSeq;
	}

	/**
	 * 移码突变
	 * @param orfShift
	 */
	public void setOrfShift(int orfShift) {
		this.orfShift = orfShift;
	}
	/**
	 * 移码突变
	 * @param orfShift
	 */
	public int getOrfShift() {
		return orfShift;
	}

	/**
	 * snp或indel所在的转录本
	 */
	public void setGffIso(GffGeneIsoInfo gffGeneIsoInfo) {
		this.gffGeneIsoInfo = gffGeneIsoInfo;
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
	 * @param mapInfoSnpIndel
	 * @return
	 */
	public boolean isSameIso(MapInfoSnpIndel mapInfoSnpIndel)
	{
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
//			else return;
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
		else {
			return;
		}
//		else if (flagLoc > 0) {
//			snpIndelRs.setLocStart(flagLoc - 1);
//			snpIndelRs.setLocStart(flagLoc);
//		}
//		else if (flagLoc <= 0) {
//			snpIndelRs.setLocStart(startLoc);
//			snpIndelRs.setLocStart(endLoc);
//		}
//		this.snpIndelRs = servSnpIndelRs.querySnpIndelRs(snpIndelRs);
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
	
	public static String getMyTitle()
	{
		String result = "ChrID\tStartLoc\tRefBase\tAllelic_depths_Ref\tThisBase\tAllelic_depths_Alt \tQuality\tFilter\tAllele_Frequency\tAllele_Balance_Hets()\tIsInExon\tDistance_To_Start\t" + 
		"RefAAnr\tRefAAseq\tThisAAnr\tThisAASeq\tAA_chemical_property\tOrfShift\tSnpDB_ID\tGeneAccID\tGeneSymbol\tGeneDescription";
		return result;
	}
	
	public String toString() {
		int startLoc = this.startLoc;
		if (!this.type.equals(TYPE_MISMATCH)) {
			startLoc = startLoc - 1; 
		}
		String result = refID + "\t" + startLoc + "\t" + refBase + "\t" + this.Allelic_depths_Ref + "\t" + thisBase + "\t" + 
		this.Allelic_depths_Alt + "\t" + quality + "\t" + this.Filter + "\t" + this.Allele_Frequency + "\t" + getAllele_Balance_Hets() + "\t" + isExon()+"\t" + prop +"\t"+
		refAAnr +"\t"+this.refAAseq + "\t" + thisAAnr +"\t"+this.thisAaSeq ;
		if (refAAseq != null && refAAseq.length() ==3 && thisAaSeq != null && thisAaSeq.length() == 3) {
			result = result + "\t" + AminoAcid.cmpAAquality(refAAseq, thisAaSeq);
		}
		else {
			result = result + "\t" + "";
		}
		result = result + "\t" + this.getOrfShift();
		result = result + "\t" + snpRsID;
//		getSnpIndelRs();
//		if (snpIndelRs != null) {
//			result = result + "\t" + snpIndelRs.getSnpRsID();
//		}
//		else {
//			result = result + "\t" + "";
//		}
		
//		if (snpIndelRs != null) {
//			if (snpIndelRs.getAvHet() == -1) {
//				result = result + "\t" + "AvHet_No Info" + "\t" + "AvHetSE_No Info";
//			}
//			else {
//				result = result + "\t" + snpIndelRs.getAvHet() + "\t" + snpIndelRs.getAvHetSE();
//			}
//		}
//		else {
//			result = result + "\t" + "AvHet_No Info" + "\t" + "AvHetSE_No Info";
//		}
		if (getGffIso() != null) {
			result = result + "\t" + getGffIso().getName();
			CopedID copedID = new CopedID(getGffIso().getName(), taxID, false);
			result = result + "\t" + copedID.getSymbol() +"\t"+copedID.getDescription();
		}
		else {
			result = result + "\t \t \t " ;
		}
		
		return result;
	}
	
	
}
