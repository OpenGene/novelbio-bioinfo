package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.Collection;
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
 * ����samtools������pile up��Ϣ����ÿһ������һ�����࣬ר�Ŵ洢�ѵ���Ϣ
 * ���趨flag�͵�snp��û���趨flag�͵�indel
 * @author zong0jie
 *
 */
public class MapInfoSnpIndel implements Comparable<MapInfoSnpIndel>, Cloneable{
	private static Logger logger = Logger.getLogger(MapInfoSnpIndel.class);

	public static final int TYPE_INSERT = 40;
	public static final int TYPE_DELETION = 30;
	public static final int TYPE_MISMATCH = 20;
	public static final int TYPE_CORRECT = 10;

	/** 
	 * <b>���涼�����������</b>
	 * ��λ�ÿ����в�ֹһ�ֵĲ���ȱʧ���Ǽ���滻���ͣ���ô���ø�hash�����洢��ô������Ϣ<br>
	 *Key: referenceSeq + SepSign.SEP_ID + thisSeq + SepSign.SEP_ID + snpType <br>
	 * value: ���������������Ϊ���ܹ����ݵ�ַ  */
	HashMap<String, SiteSnpIndelInfo> mapAllen2Num = new HashMap<String, SiteSnpIndelInfo>();

	String chrID;
	String refBase = "";
	/** snp�ڻ����е�λ�ã�0-1֮�䣬0.1��ʾsnp�ڻ��򳤶�*0.1��λ�ô�  */
	double prop = 0;
	/** ��snp��indel���ڵ���� */
	int refSnpIndelStart = 0;
	/**
	 * snp��indel���ڵ�ת¼��
	 */
	GffGeneIsoInfo gffGeneIsoInfo;
	GffChrAbs gffChrAbs;
	/** ����������reads֮��Ĺ�ϵ */
	HashMap<String, SampleRefReadsInfo> mapSample2NormReadsInfo = new HashMap<String, SampleRefReadsInfo>();
	
	
	
	
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
	
	public MapInfoSnpIndel() {}
	/**
	 * @param gffChrAbs
	 * @param chrID
	 * @param snpLoc
	 * @param referenceSeq
	 * @param thisSeq
	 */
	public MapInfoSnpIndel(GffChrAbs gffChrAbs,String chrID, int refSnpIndelStart) {
		this.gffChrAbs = gffChrAbs;
		this.chrID = chrID;
		//flagLoc�ж���˵����snp
		this.refSnpIndelStart = refSnpIndelStart;
	    setGffIso();
	}
	/**
	 * snp��indel���ڵ�ת¼��
	 * ͬʱ�趨setProp��cis5to3����name������gffGeneIsoInfo����Ϣ
	 */
	private void setGffIso() {
		if (gffChrAbs == null)
			return;

		this.gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLocation(chrID, refSnpIndelStart).getCodInExonIso();
		if (gffGeneIsoInfo == null) {
			return;
		}
		setProp();
	}
	/**
	 * refBase�ڻ����е�λ�ã�0-1֮�䣬0.1��ʾsnp�ڻ��򳤶�*0.1��λ�ô�
	 * ԽСԽ����ͷ��
	 * 0-1֮��
	 */
	private void setProp() {
		this.prop = (double)gffGeneIsoInfo.getCod2TSSmRNA(getRefSnpIndelStart())
				/ 
				(gffGeneIsoInfo.getCod2TSSmRNA(getRefSnpIndelStart())  - gffGeneIsoInfo.getCod2TESmRNA(getRefSnpIndelStart()));
	}
	public String getRefID() {
		return chrID;
	}
	/**
	 * �趨��snp������
	 * GATK�ĵ�6�У���0����Ϊ��5��
	 */
	public void setQuality(String sampleName, String quality, String snpINFO) {
		this.quality = quality;
	}
	public void setSnpFilter(String snpINFO) {
		this.Filter = snpINFO;
	}
	public String getSnpINFO() {
		return Filter;
	}
	/**
	 * �趨refSite�������
	 * @return
	 */
	public void setAllelicDepthsRef(int Allelic_depths_Ref) {
		this. Allelic_depths_Ref = Allelic_depths_Ref;
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
	 * ������ɾ����һ��Allelic_depths_Alt����Ŀ��������κܺõ���ӽ�ȥ
	 * ����
	 * GT:AD:DP:GQ:PL	0/1:53,10:63:99:150,0,673
	 */
	public void setFlag(String flagTitle, String flagDetail) {
		//TODO ������ɾ����һ��Allelic_depths_Alt����Ŀ��������κܺõ���ӽ�ȥ
		String[] ssFlag = flagTitle.split(":");
		String[] ssValue = flagDetail.split(":");
		for (int i = 0; i < ssFlag.length; i++) {
			if (ssFlag[i].equals("AD")) {
				String[] info = ssValue[i].split(",");
				Allelic_depths_Ref = Integer.parseInt(info[0]);
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
			if (tmpInfo[0].equals("SB")) {
				Strand_Bias =  Double.parseDouble(tmpInfo[1]);
			}
		}
	}
	/**
	 *  ������refbase��Ϣ�Ļ����ϣ����Ҹ�refSnpIndelStartλ������Щindel��snp
	 *  �ҵ���indel����Ӧ��refbase���ܺ�ԭ����refbase��һ��
	 * @param samString
	 */
	public void setSamToolsPilup(String sampleName, String samString, GffChrAbs gffChrAbs) {
		String[] ss = samString.split("\t");
		this.chrID = ss[0];
		this.refSnpIndelStart = Integer.parseInt(ss[1]);//�����᲻�趨������ν����Ϊ�����ʱ�����Ҫ����ͬ��ID
		this.refBase = ss[2];
		this.gffChrAbs = gffChrAbs;
		setGffIso();
		setAllenInfo(sampleName, Integer.parseInt(ss[3]), ss[4]);
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
	private void setAllenInfo(String sampleName, int readsDepth, String pileUpInfo) {
		SampleRefReadsInfo sampleRefReadsInfo = new SampleRefReadsInfo(readsDepth);
		mapSample2NormReadsInfo.put(sampleName, sampleRefReadsInfo);		
		int Allelic_depths_Ref = 0; mapAllen2Num = new HashMap<String, SiteSnpIndelInfo>();
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
				//�����ͷ�ǡ�+���ţ�����+�ź�����֣�Ҳ����indel�ĳ���
				for (; i < pipInfo.length; i++) {
					char tmpNum = pipInfo[i];
					//ת��Ϊ�����ַ�
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
				if (c == '+') {
					referenceSeq = refBase;
					thisSeq = refBase + indel;
				}
				else {
					referenceSeq = refBase + indel;
					thisSeq = refBase;
				}
				SiteSnpIndelInfo siteSnpIndelInfo = null;
				String indelInfo = SiteSnpIndelInfo.getMismatchInfo(chrID, refSnpIndelStart, referenceSeq, thisSeq);
				
				if (mapAllen2Num.containsKey(indelInfo)) {
					siteSnpIndelInfo = mapAllen2Num.get(indelInfo);
					siteSnpIndelInfo.addThisBaseNum(sampleName);
				}
				else {
					siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, gffChrAbs, referenceSeq, thisSeq);
					siteSnpIndelInfo.setThisBaseNum(sampleName, 1);
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
				String mismatchInfo = SiteSnpIndelInfo.getMismatchInfo(chrID, refSnpIndelStart, referenceSeq, thisSeq);
				if (mapAllen2Num.containsKey(mismatchInfo)) {
					siteSnpIndelInfo = mapAllen2Num.get(mismatchInfo);
					siteSnpIndelInfo.addThisBaseNum(sampleName);
				}
				else {
					siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, gffChrAbs, refBase, thisSeq);
					siteSnpIndelInfo.setThisBaseNum(sampleName, 1);
					mapAllen2Num.put(mismatchInfo, siteSnpIndelInfo);
				}
			}
		}
	}
	
	public void addAllenInfo(GffChrAbs gffChrAbs, String referenceSeq, String thisSeq) {
		SiteSnpIndelInfo siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, gffChrAbs, referenceSeq, thisSeq);
		mapAllen2Num.put(SiteSnpIndelInfo.getMismatchInfo(chrID, refSnpIndelStart, referenceSeq, thisSeq), siteSnpIndelInfo);
	}
	/**
	 * ����һ��mapInfoSnpIndel������snpIndel��Ϣװ�뱾�࣬��ô���������ͬ��snpIndel������
	 * @param mapInfoSnpIndel
	 */
	public void addAllenInfo(MapInfoSnpIndel mapInfoSnpIndel) {
		Collection<SiteSnpIndelInfo> colSiteSnpIndelInfos = mapInfoSnpIndel.mapAllen2Num.values();
		for (SiteSnpIndelInfo siteSnpIndelInfo : colSiteSnpIndelInfos) {
			if (mapAllen2Num.containsKey(siteSnpIndelInfo.getMismatchInfo())) {
				continue;
			}
			mapAllen2Num.put(siteSnpIndelInfo.getMismatchInfo(), siteSnpIndelInfo);
		}
	}
	/**
	 * ���snp��indel��ref�ϵ���㣬ʵ��λ��
	 * @return
	 */
	public int getRefSnpIndelStart() {
		return refSnpIndelStart;
	}
	/**
	 * 
	 * �������кʹ��䷽ʽ�����������е�reads�ѵ���
	 * ��Ϊ��λ������ж��ִ��䣬���Ը���һ��Ȼ����ң������ҵ�����
	 * ��hash���л��
	 * @param chrID
	 * @param snpStartSite
	 * @param referenceSeq
	 * @param thisSeq
	 * @return
	 */
	public SiteSnpIndelInfo getSnpIndel(String referenceSeq, String thisSeq) {
		String tmpInfo = SiteSnpIndelInfo.getMismatchInfo(chrID, refSnpIndelStart, referenceSeq, thisSeq);
		SiteSnpIndelInfo siteSnpIndelInfo = mapAllen2Num.get(tmpInfo);
		return siteSnpIndelInfo;
	}
	/**
	 * �������кʹ��䷽ʽ�����������е�reads�ѵ���
	 * ��Ϊ��λ������ж��ִ��䣬���Ը���һ��Ȼ����ң������ҵ�����
	 * ��hash���л��
	 * @param referenceSeq
	 * @param thisSeq
	 * @param snpType
	 * @return
	 */
	public SiteSnpIndelInfo getSnpIndelNum(SiteSnpIndelInfo siteSnpIndelInfo) {
		return mapAllen2Num.get(siteSnpIndelInfo.getMismatchInfo());
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
	public SiteSnpIndelInfo getSnpIndelNum(MapInfoSnpIndel mapInfoSnpIndel) {
		if (mapInfoSnpIndel.getRefSnpIndelStart() != getRefSnpIndelStart()) {
			logger.error("����Ĳ���λ�㲻��ͬһ������λ�㣺" + getRefSnpIndelStart() + "����λ�㣺" + mapInfoSnpIndel.getRefSnpIndelStart());
			return null;
		}
		return getSiteSnpInfoBigAllen();
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
	public String getSeqTypeNumStr(String sampleName, MapInfoSnpIndel mapInfoSnpIndel) {
		SiteSnpIndelInfo siteSnpIndelInfoQuery = mapInfoSnpIndel.getSiteSnpInfoBigAllen();
		return getSeqTypeNumStr(sampleName, siteSnpIndelInfoQuery);
	}
	/**
	 * ����mapInfoSnpIndel��������<b>ref</b>,<b>refbase</b>��<b>thisbase</b>��<b>indel</b>��type�����ұ�λ��ĳ��type indel��������<br>
	 * ע�⣬�����mapInfoSnpIndel����ֻ����һ��type��Ҳ����ֻ��ָ��һ����ʽ�Ĵ��䣬<br>
	 * ���������indel�ڲ��ҵ�ʱ��Ὣ��һλɾ������ΪGATK�����ĵ�һλ��indel��ǰһλ<br>
	 * ���ظ�����ʽ�����Լ���Ӧ���������е�reads�ѵ���
	 * ��hash���л��
	 * @param SampleName
	 * @param SiteSnpIndelInfo �����ı����������Ϣ
	 * @return ���������ԵĻ�:<br>
	 * refID \t  refStart \t refBase  \t  depth \t indelBase  \t indelNum   <br>
	 * ������"";
	 */
	public String getSeqTypeNumStr(String sampleName, SiteSnpIndelInfo siteSnpIndelInfoQuery) {
		//Ҳ�����ñ��λ��ļ����Ĵ�����Ϣȥ���ұ�λ��Ĵ�����Ϣ����󷵻�string���
		//���ﲻ����mapInfoSnpIndel.getRefBase()������otherMap.getRefBase()
		//��ΪmapInfoSnpIndel.getRefBase()���ܲ�����������ref���ر�Ϊȱʧ��ʱ��
		SiteSnpIndelInfo siteSnpIndelInfo = getSnpIndelNum(siteSnpIndelInfoQuery);
		if (siteSnpIndelInfo == null) {
			return "";
		}
		String tmpResult = getRefID()+"\t"+getRefSnpIndelStart()+"\t" + siteSnpIndelInfoQuery.getReferenceSeq()+"\t" +getAllelic_depths_Ref();
		tmpResult = tmpResult + "\t" +siteSnpIndelInfo.getThisSeq() + "\t" + siteSnpIndelInfo.getThisBaseNum(sampleName);
		return tmpResult;
	}
	/**
	 * ������������snpλ��
	 */
	public SiteSnpIndelInfo getSiteSnpInfoBigAllen() {
		ArrayList<SiteSnpIndelInfo> lsAllenInfo = ArrayOperate.getArrayListValue(mapAllen2Num);
		Collections.sort(lsAllenInfo, Collections.reverseOrder());
		if (lsAllenInfo.size() > 0) {
			return lsAllenInfo.get(0);
		}
		return null;
	}
	/**
	 * �������еķ�ref�Ļ����Լ���Ӧ����������� 
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
	 * snp�ڻ��򳤶ȵİٷֱ�
	 * ԽСԽ����ͷ��
	 */
	public double getProp() {
		return prop;
	}
	public String getQuality(String sampleName) {
		return mapSample2NormReadsInfo.get(sampleName).getQuality();
	}
	
	/**
	 * �ο�����
	 * @return
	 */
	public String getRefBase() {
		return refBase;
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
	/**
	 * ���ڱȽϵģ���С�����
	 * �ȱ�refID��Ȼ���start��end�����߱�flag���߱�score
	 * ��score��ʱ��Ͳ�����refID��
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
	 * ��δʵ��
	 */
	public MapInfoSnpIndel clone() {
		MapInfoSnpIndel mapInfoSnpIndel;
		try {
			//TODO
			mapInfoSnpIndel = (MapInfoSnpIndel) super.clone();
			return mapInfoSnpIndel;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		logger.error("��¡����");
		return null;
	}
	/**
	 * ����ѡ�е�mapInfo����ȡsamtools������pileup file���ÿ��λ��ľ�����Ϣ
	 * @param lsSite ������refbase��������Ϣ
	 * @param samToolsPleUpFile samtools�������ļ�
	 */
	public static void getSiteInfo(List<MapInfoSnpIndel> lsSite, String samToolsPleUpFile, GffChrAbs gffChrAbs) {
		/** ÿ��chrID��Ӧһ��mapinfo��Ҳ����һ��list */
		HashMap<String, ArrayList<MapInfoSnpIndel>> mapSortedChrID2MapInfo = sortLsMapInfoSnpIndel(lsSite);
		getSiteInfo(mapSortedChrID2MapInfo, samToolsPleUpFile, gffChrAbs);
	}
	
	/**
	 * ����ѡ�е�mapInfo����ȡsamtools������pileup file���ÿ��λ��ľ�����Ϣ
	 * @param mapSortedChrID2LsMapInfo LsMapInfo�Ź����list
	 * @param samToolsPleUpFile
	 * @param gffChrAbs
	 * @return �½�һ��hash��Ȼ�󷵻أ����hash��������ı���deep copy��ϵ
	 */
	public static HashMap<String, ArrayList<MapInfoSnpIndel>> getSiteInfo(HashMap<String, ArrayList<MapInfoSnpIndel>> mapChrID2SortedLsMapInfo, String samToolsPleUpFile, GffChrAbs gffChrAbs) {
		HashMap<String, ArrayList<MapInfoSnpIndel>> mapSortedChrID2LsMapInfoResult = copyHashMap(mapChrID2SortedLsMapInfo);
		/** ÿ��chrID��Ӧһ��mapinfo��Ҳ����һ��list */
		TxtReadandWrite txtReadSam = new TxtReadandWrite(samToolsPleUpFile, false);
		String tmpChrID = ""; ArrayList<MapInfoSnpIndel> lsMapInfos = null; ArrayList<MapInfoSnpIndel> lsMapInfosNew = null;
		int mapInfoIndex = 0;// ���ν�����ȥ
		for (String samtoolsLine : txtReadSam.readlines()) {
			String[] ss = samtoolsLine.split("\t");
			if (!ss[0].equals(tmpChrID)) {
				tmpChrID = ss[0];
				lsMapInfos = mapSortedChrID2LsMapInfoResult.get(tmpChrID);
				mapInfoIndex = 0;
				if (lsMapInfos == null) {
					logger.info("����δ֪ chrID��" + tmpChrID);
					continue;
				}
			}
			
			if (lsMapInfos == null) continue;
			//����lsMapInfos�е���Ϣ�����������
			if (mapInfoIndex >= lsMapInfos.size()) continue;
			
			//һ��һ������ȥ��ֱ���ҵ�����Ҫ��λ��
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
		/** ÿ��chrID��Ӧһ��mapinfo��Ҳ����һ��list */
		HashMap<String, ArrayList<MapInfoSnpIndel>> hashChrIDMapInfo = new LinkedHashMap<String, ArrayList<MapInfoSnpIndel>>();
		// ����chrλ��װ��hash��
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

class SampleRefReadsInfo {
	public SampleRefReadsInfo(int readDepth) {
		this.readDepth = readDepth;
	}
	/** AD
	 * Allelic depths for the ref and alt alleles in the order listed
	 */
	int Allelic_depths_Ref = 0;
	/**  DP
	 * Read Depth (only filtered reads used for calling
	 */
	int readDepth = 0;
	/**
	 * GQ
	 * The Genotype Quality, as a Phred-scaled confidence at the true genotype is the one provided in GT. In diploid case, 
	 * if GT is 0/1, then GQ is really L(0/1) / (L(0/0) + L(0/1) + L(1/1)), where L is the likelihood of the NGS sequencing data
	 *  under the model of that the sample is 0/0, 0/1/, or 1/1. 
	 * �����Ǽ��������
	 */
	double Genotype_Quality = 0;
	/**
	 * SB, 
	 * How much evidence is there for Strand Bias (the variation being seen on only the forward or only the reverse strand) in the reads?
	 *  Higher SB values denote more bias (and therefore are more likely to indicate false positive calls).
	 */
	double Strand_Bias = 0;
	/** ��snp������ */
	String quality = "";
	/** �Ƿ���ϱ�׼ */
	String Filter = "";
	
	public void setRefDepth(int allelic_depths_Ref) {
		this.Allelic_depths_Ref = allelic_depths_Ref;
	}
	public void setGenotype_Quality(double genotype_Quality) {
		Genotype_Quality = genotype_Quality;
	}
	public void setStrand_Bias(double strand_Bias) {
		Strand_Bias = strand_Bias;
	}
	public void setQuality(String quality) {
		this.quality = quality;
	}
	public void setFilter(String filter) {
		Filter = filter;
	}
	
	public int getAllelic_depths_Ref() {
		return Allelic_depths_Ref;
	}
	public double getGenotype_Quality() {
		return Genotype_Quality;
	}
	public String getFilter() {
		return Filter;
	}
	public String getQuality() {
		return quality;
	}
	public int getReadDepth() {
		return readDepth;
	}
	public double getStrand_Bias() {
		return Strand_Bias;
	}
}





