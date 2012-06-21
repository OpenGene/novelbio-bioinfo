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
 * ����samtools������pile up��Ϣ����ÿһ������һ�����࣬ר�Ŵ洢�ѵ���Ϣ
 * ���趨flag�͵�snp��û���趨flag�͵�indel
 * @author zong0jie
 *
 */
public class MapInfoSnpIndel extends MapInfo {
	public static final String TYPE_INSERT = "insert";
	public static final String TYPE_DELETION = "deletion";
	public static final String TYPE_MISMATCH = "mismatch";
	public static final String TYPE_CORRECT = "correct";
	public static final String SEP = "@//@";
	/**  snp�����ͣ�TYPE_INSERT�� */
	String type = TYPE_CORRECT;
	/** 
	 * <b>���涼�����������</b>
	 * ��λ�ÿ����в�ֹһ�ֵĲ���ȱʧ���Ǽ���滻���ͣ���ô���ø�hash�����洢��ô������Ϣ
	 * allen�ļ��+sep+Type
	 * value: ���������������Ϊ���ܹ����ݵ�ַ  */
	HashMap<String, int[]> hashAlle = new HashMap<String, int[]>();

	int taxID = 0;
	private static Logger logger = Logger.getLogger(MapInfoSnpIndel.class);
	SnpIndelRs snpIndelRs;
//	String refAAseq = "";
//	String thisAaSeq = "";
	String thisBase = "";
	String refBase = "";
	String thisAAnr = "";
	/** snp�ڻ����е�λ�ã�0-1֮�䣬0.1��ʾsnp�ڻ��򳤶�*0.1��λ�ô�  */
	double prop = 0;
	/** ��snp��indel���ڵ���� */
	int refSnpIndelStart = 0;
	/** ��snp��indel���ڵ�refgenome�ϵ��յ㣬���Ϊsnp���������յ�һ�� */
	int refSnpIndelEnd = 0;
	/**
	 * snp�ڻ����е�λ�ã�0-1֮�䣬0.1��ʾsnp�ڻ��򳤶�*0.1��λ�ô�
	 * ԽСԽ����ͷ��
	 * 0-1֮��
	 */
	private void setProp(double prop) {
		this.prop = prop;
	}
	/**
	 * snp�ڻ��򳤶ȵİٷֱ�
	 * ԽСԽ����ͷ��
	 */
	public double getProp() {
		return prop;
	}
	/**
	 * ���snp��indel��ref�ϵ���㣬ʵ��λ��
	 * @return
	 */
	public int getRefSnpIndelStart() {
		return refSnpIndelStart;
	}
	/** ��snp��indel���ڵ�refgenome�ϵ��յ㣬���Ϊsnp���������յ�һ�� */
	public int getRefSnpIndelEnd() {
		return refSnpIndelEnd;
	}
	/**
	 * ���շ�����snp��indel��ref�ϵ����
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
	/** ���շ�����snp��indel��ref�ϵ��յ㣬���Ϊsnp�������յ�һ�� */
	public int getRefSnpIndelEndCis() {
		if (cis5to3) {
			return refSnpIndelEnd;
		}
		else {
			return refSnpIndelStart;
		}
	}
	
//	/**
//	 * ����samtools������pile up�ļ���һ�У�Ȼ����䱾��
//	 * ע��û���趨refSnpIndelEnd
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
	 *  ������refbase��Ϣ�Ļ����ϣ����Ҹ�refSnpIndelStartλ������Щindel��snp
	 *  �ҵ���indel����Ӧ��refbase���ܺ�ԭ����refbase��һ��
	 * @param samString
	 */
	public void setSamToolsPilup(String samString) {
		String[] ss = samString.split("\t");
		this.refID = ss[0];
		this.refSnpIndelStart = Integer.parseInt(ss[1]);//�����᲻�趨������ν����Ϊ�����ʱ�����Ҫ����ͬ��ID
//		this.refBase = ss[2];//ref�Ͳ��趨��
		this.Read_Depth_Filtered = Integer.parseInt(ss[3]);
		setAllenInfo(ss[4]);
	}
	/**
	 * @param taxID ����
	 * @param chrID Ⱦɫ���
	 * @param snpLoc snpλ��
	 * @param refBase ref������
	 * @param thisBase ������
	 */
	public MapInfoSnpIndel(int taxID,String chrID, int snpLoc, String refBase, String thisBase) {
		super(chrID);
		this.taxID = taxID;
		//flagLoc�ж���˵����snp
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
	 * ����snp�����ͣ�TYPE_INSERT��
	 */
	public String getType() {
		return this.type;
	}
	/**
	 * �����趨Allelic_depths_Ref����hashAlle��Ϣ
	 *  ����samtools������pile up�Ǹ�pileup��Ϣ�������λ��Ķѵ����<br>
	 * ��ʽ����<br> ...........,.............,....,....,.,.,..,..,...,....,.^!.<br>����:<br>
	 *  <b>.</b> :match to the reference base on the forward strand<br>
	 *  <b>,</b> :match on the reverse strand, <br>
	 *  <b>��>��</b> or<b> ��<�� </b> :a reference skip<br>
	 *  <b>��ACGTN�� </b> :mismatch on the forward strand<br> 
	 *  <b>��acgtn��</b> :mismatch on the reverse strand<br>
	 *  <b> ��\+[0-9]+[ACGTNacgtn]+��</b> :insertion between this reference position and the next reference position.
	 *  The length of the insertion is given by the integer in the pattern, followed by the inserted sequence.<br>
	 *  <b>��-[0-9]+[ACGTNacgtn]+��</b> represents a deletion from the reference. The deleted bases will be presented as<b> ��*��</b> in the following lines. 
	 *  <b>��^��</b>the start of a read. The ASCII of the character following ��^�� minus 33 gives the mapping quality. 
	 *  <b>��$��</b> marks the end of a read segment.
	 * @param pileUpInfo ���� ...........,.............,....,....,.,.,..,..,...,....,.^!. ���ֶ���
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
				//�����ͷ�ǡ�+���ţ�����+�ź�����֣�Ҳ����indel�ĳ���
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
				//��þ�����ַ�
				char[] tmpSeq = new char[tmpInDelNum];
				for (int j = 0; j < tmpSeq.length; j++) {
					i++;
					tmpSeq[j] = pipInfo[i];
				}
				String indel = String.copyValueOf(tmpSeq);
				//װ��hash��
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
	 * �������кʹ��䷽ʽ�����������е�reads�ѵ���
	 * ��Ϊ��λ������ж��ִ��䣬���Ը���һ��Ȼ����ң������ҵ�����
	 * ��hash���л��
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
	 * ����mapInfoSnpIndel��������<b>ref</b>,<b>refbase</b>��<b>thisbase</b>��<b>indel</b>��type�����ұ�λ��ĳ��type indel��������<br>
	 * ע�⣬�����mapInfoSnpIndel����ֻ����һ��type��Ҳ����ֻ��ָ��һ����ʽ�Ĵ��䣬<br>
	 * ���������indel�ڲ��ҵ�ʱ��Ὣ��һλɾ������ΪGATK�����ĵ�һλ��indel��ǰһλ<br>
	 * ���ظ�����ʽ�����Լ���Ӧ���������е�reads�ѵ���
	 * ��hash���л��
	 * @param mapInfoSnpIndel �����ı����������Ϣ
	 * @return ������-1
	 */
	public int getSeqTypeNum(MapInfoSnpIndel mapInfoSnpIndel) {
		if (mapInfoSnpIndel.getRefSnpIndelStart() != getRefSnpIndelStart()) {
			logger.error("����Ĳ���λ�㲻��ͬһ������λ�㣺" + getRefSnpIndelStart() + "����λ�㣺" + mapInfoSnpIndel.getRefSnpIndelStart());
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
	 * ����mapInfoSnpIndel��������<b>ref</b>,<b>refbase</b>��<b>thisbase</b>��<b>indel</b>��type�����ұ�λ��ĳ��type indel��������<br>
	 * ע�⣬�����mapInfoSnpIndel����ֻ����һ��type��Ҳ����ֻ��ָ��һ����ʽ�Ĵ��䣬<br>
	 * ���������indel�ڲ��ҵ�ʱ��Ὣ��һλɾ������ΪGATK�����ĵ�һλ��indel��ǰһλ<br>
	 * ���ظ�����ʽ�����Լ���Ӧ���������е�reads�ѵ���
	 * ��hash���л��
	 * @param mapInfoSnpIndel �����ı����������Ϣ
	 * @return ���������ԵĻ�:<br>
	 * refID \t  refStart \t refBase  \t  depth \t indelBase  \t indelNum   <br>
	 * ������"";
	 */
	public String getSeqTypeNumStr(MapInfoSnpIndel mapInfoSnpIndel) {
		//Ҳ�����ñ��λ��ļ����Ĵ�����Ϣȥ���ұ�λ��Ĵ�����Ϣ����󷵻�string���
		//���ﲻ����mapInfoSnpIndel.getRefBase()������otherMap.getRefBase()
		//��ΪmapInfoSnpIndel.getRefBase()���ܲ�����������ref���ر�Ϊȱʧ��ʱ��
		int num = getSeqTypeNum(mapInfoSnpIndel);
		if (num == -1) {
			return "";
		}
		String tmpResult = getRefID()+"\t"+getRefSnpIndelStart()+"\t" + mapInfoSnpIndel.getRefBase()+"\t" +getAllelic_depths_Ref();
		tmpResult = tmpResult + "\t" +mapInfoSnpIndel.getThisBase() + "\t" + getSeqTypeNum(mapInfoSnpIndel);
		return tmpResult;
	}
	
	
	/**
	 * �������еķ�ref�Ļ����Լ���Ӧ�����������
	 * list-string[3]
	 * 0��seq
	 * 1��type
	 * 2��num
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
	 * ����������Ǹ�ɶ���ˣ�
	 * ���ʵ�ʵ���㣬indel��Ҫ����1
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
	 * ���룬0��1��2����
	 */
	int orfShift = 0; 
	/**
	 * snp��indel���ڵ�ת¼��
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
	 * �����Ǽ��������
	 */
	double Genotype_Quality = 0;
	/**
	 * AF
	 * Allele Frequency, for each ALT allele, in the same order as listed
	 * 1��ʾ�����ӣ�0.5��ʾ�Ӻϣ�����������ܽ���᲻ͬ
	 */
	double Allele_Frequency = 1;
	/**
	 * AN
	 * �ܹ����ٸ���λ��һ�㶼Ϊ2����refһ�����ı�һ��������3����ʱ������ʱû���������Կ�����־һ�¿������
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
	
	/** ��snp������ */
	String quality = "";
	/** �Ƿ���ϱ�׼ */
	String Filter = "";
	public String getQuality() {
		return quality;
	}
	/**
	 * �趨��snp������
	 * GATK�ĵ�6�У���0����Ϊ��5��
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
	 * �趨thisSite�������
	 * @return
	 */
	public void setAllelicDepthsAlt(int Allelic_depths_Alt) {
		this. Allelic_depths_Alt = Allelic_depths_Alt;
	}
	/**
	 * �趨refSite�������
	 * @return
	 */
	public void setAllelicDepthsRef(int Allelic_depths_Ref) {
		this. Allelic_depths_Ref = Allelic_depths_Ref;
	}
	/**
	 * AF Allele Frequency, for each ALT allele, in the same order as listed 1��ʾ�����ӣ�0.5��ʾ�Ӻϣ�����������ܽ���᲻ͬ
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
	 *  of the NGS sequencing data under the model of that the sample is 0/0, 0/1/, or 1/1. �����Ǽ��������
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
	 * DBsnp��ID
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
	 * ����ID
	 * @return
	 */
	public int getTaxID() {
		return taxID;
	}
	/**
	 * AN �ܹ����ٸ���λ��һ�㶼Ϊ2����refһ�����ı�һ��������3����ʱ������ʱû���������Կ�����־һ�¿������
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
	 * ����
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
	 * �Ϳ������AF,AN,SB
	 *  AB=0.841;AC=1;AF=0.50;AN=2;BaseQRankSum=0.097;DP=63;Dels=0.00;FS=0.000;HRun=0;HaplotypeScore=0.0000;
	 *  ����GATKinfo���趨��Ϣ
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
	 * ����
	 */
	@Deprecated
	public int getFlagSite() {
		return flagLoc;
	}
	/**
	 * ʵ�ʵ�����
	 * @return
	 */
	public String getThisBase() {
		return thisBase;
	}
	/**
	 * �ο�����
	 * @return
	 */
	public String getRefBase() {
		return refBase;
	}
	
	public String getRefAAseq() {
		return seqFasta.toStringAA();
	}
	
	/**
	 * ���������
	 * �������к���ʼλ�㣬��snpλ��ȥ�滻���У�ͬʱ�������滻�Ƿ��������д��orfshift
	 * @param thisBase ��������--�����б���������Ȼ��
	 * @param cis5to3 �������е�������
	 * @param startLoc  �����е���һ���㿪ʼ�滻. 0��ʾ�嵽��ǰ�档1��ʾ�ӵ�һ����ʼ�滻
	 * ���refΪ""�������в�����startBias�Ǹ�����ĺ���
	 * @return
	 */
	private SeqFasta replaceSnpIndel(String replace, int startLoc, int endLoc) {
		SeqFasta seqFasta = getSeqFasta().clone();
		if (seqFasta.toString().equals("")) {
			return new SeqFasta();
		}
		seqFasta.modifySeq(startLoc, endLoc, replace, false, false);
		//�޸�����
		orfShift = Math.abs(replace.length() - refBase.length())%3;
		return seqFasta;
	}
	/** snp����refnr�ϵ�λ�� */
	int replaceLoc = 0;
	/**
	 * ����snpλ����滻��Ҳ�������µ�snp��indel���滻�ϵ�λ��
	 * @param replaceLoc ������Ϊ����ʱ��λ�ã���Ҫ�滻�ĵ��������ϵ�λ�ã��ڲ����ݷ����Զ��ߵ�
	 */
	public void setReplaceLoc(int replaceLoc) {
		this.replaceLoc = replaceLoc;
	}
	/**
	 * ���һ��λ�����������ϵ�snp���Ϳ��ܻ����
	 * ��ñ�snpλ���������AA����
	 * ע��Ҫͨ��{@link #setCis5to3(Boolean)}���趨 ���������ڻ��������������Ƿ���
	 * ��Ҫͨ��{@link #setReplaceLoc(int)}���趨������refnr�ϵ�λ��
	 * @return û�еĻ��ͷ���һ���յ�seqfasta
	 */
	public SeqFasta getThisAAnr() {
		int max = -1;
		String maxIndelType = "";//��ñ������ĵ������
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
	 * ����ͻ�䣬ע�����ǰ����ȵ���
	 * {@link #getThisAAnr()}
	 * @param orfShift
	 */
	public int getOrfShift() {
		return orfShift;
	}

	/**
	 * snp��indel���ڵ�ת¼��
	 * ͬʱ�趨setProp��cis5to3����name������gffGeneIsoInfo����Ϣ
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
	 * ������ڵ�ת¼��
	 * @return
	 */
	public GffGeneIsoInfo getGffIso() {
		return gffGeneIsoInfo;
	}
	/**
	 * �ж���һ��snp����indel�ǲ����뱾mapInfo��ͬһ��ת¼����
	 * ����mapInfoSnpIndel��������gffGeneIsoInfo���ú�
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
	 * �趨snpID���Զ���ö�Ӧ��DBsnp��Ϣ
	 * @param snpRsID
	 */
	public void setDBSnpID(String snpRsID) {
		if (snpRsID != null && !snpRsID.trim().equals("")) {
			if (!snpRsID.equals(snpRsID)) {
				logger.error("��dbspnID�������dbsnpID����Ӧ��"+snpRsID + snpIndelRs.getSnpRsID());
			}
		}
		this.snpRsID = snpRsID;
	}
	
	
	/**
	 * �趨DBsnp����Ϣ�����趨flag�͵�snp��û���趨flag�͵�indel
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
	 * �����SNPDB���м��أ���ü��ص���Ϣ
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
	 * ���ڱȽϵģ���С�����
	 * �ȱ�refID��Ȼ���start��end�����߱�flag���߱�score
	 * ��score��ʱ��Ͳ�����refID��
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
	/////////////////////////////////////// ��̬�������������ָ�������λ�����Ϣ ///////////////////////////////
	public static String getMyTitle() {
		String result = "ChrID\tSnpLoc\tRefBase\tAllelic_depths_Ref\tThisBase\tAllelic_depths_Alt \tQuality\tFilter\tAllele_Frequency\tAllele_Balance_Hets()\tIsInExon\tDistance_To_Start\t" + 
		"RefAAnr\tRefAAseq\tThisAAnr\tThisAASeq\tAA_chemical_property\tOrfShift\tSnpDB_ID\tGeneAccID\tGeneSymbol\tGeneDescription";
		return result;
	}
	/**
	 * ����ѡ�е�mapInfo����ȡsamtools������pileup file���ÿ��λ��ľ�����Ϣ
	 * @param lsSite ������refbase��������Ϣ
	 * @param txtSamToolsFile samtools�������ļ�
	 */
	public static void getSiteInfo(List<MapInfoSnpIndel> lsSite, String txtSamToolsFile) {
		// ���������Ч��
		MapInfo.setCompType(MapInfo.COMPARE_LOCSITE);
		Collections.sort(lsSite);
		/** ÿ��chrID��Ӧһ��mapinfo��Ҳ����һ��list */
		HashMap<String, ArrayList<MapInfoSnpIndel>> hashChrIDMapInfo = new LinkedHashMap<String, ArrayList<MapInfoSnpIndel>>();
		// ����chrλ��װ��hash��
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
		int mapInfoIndex = 0;// ���ν�����ȥ
		for (String content : txtReadSam.readlines()) {
			String[] ss = content.split("\t");
			if (!ss[0].equals(tmpChrID)) {
				tmpChrID = ss[0];
				lsMapInfos = hashChrIDMapInfo.get(tmpChrID);
				mapInfoIndex = 0;
			}
			if (lsMapInfos == null) {
				logger.error("����δ֪ chrID��" + tmpChrID);
				break;
			}
				
			//����lsMapInfos�е���Ϣ�����������
			if (mapInfoIndex >= lsMapInfos.size()) {
				continue;
			}
			//һ��һ������ȥ��ֱ���ҵ�����Ҫ��λ��
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
