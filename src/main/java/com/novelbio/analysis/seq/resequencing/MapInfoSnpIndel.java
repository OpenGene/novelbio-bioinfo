package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo.SnpIndelType;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * ����samtools������pile up��Ϣ����ÿһ������һ�����࣬ר�Ŵ洢�ѵ���Ϣ
 * ���趨flag�͵�snp��û���趨flag�͵�indel
 * @author zong0jie
 *
 */
public class MapInfoSnpIndel implements Comparable<MapInfoSnpIndel>, Cloneable{
	private static Logger logger = Logger.getLogger(MapInfoSnpIndel.class);

	/** 
	 * <b>���涼�����������</b>
	 * ��λ�ÿ����в�ֹһ�ֵĲ���ȱʧ���Ǽ���滻���ͣ���ô���ø�hash�����洢��ô������Ϣ<br>
	 *Key: referenceSeq + SepSign.SEP_ID + thisSeq + SepSign.SEP_ID + snpType <br>
	 * value: ���������������Ϊ���ܹ����ݵ�ַ  */
	TreeMap<String, SiteSnpIndelInfo> mapAllen2Num = new TreeMap<String, SiteSnpIndelInfo>();

	String chrID;
	String refBase = "";
	/** snp�ڻ����е�λ�ã�0-1֮�䣬0.1��ʾsnp�ڻ��򳤶�*0.1��λ�ô�  
	 * -1��ʾû�и���Ŀ
	 * */
	double prop = -1;
	/** ��snp��indel���ڵ���� */
	int refSnpIndelStart = 0;
	/**
	 * snp��indel���ڵ�ת¼��
	 */
	GffGeneIsoInfo gffGeneIsoInfo;
	GffChrAbs gffChrAbs;
	/** ����������reads֮��Ĺ�ϵ */
	TreeMap<String, SampleRefReadsInfo> mapSample2NormReadsInfo = new TreeMap<String, SampleRefReadsInfo>();
	String sampleName = "";
	/** Ҫ���Ѿ���sam pileUp�����������ˣ���ô���趨��������sample�ǿ����ҵ��� */
	
	public MapInfoSnpIndel() {}
	/** @param gffChrAbs */
	public MapInfoSnpIndel(GffChrAbs gffChrAbs, String sampleName) {
		this.gffChrAbs = gffChrAbs;
		this.sampleName = sampleName;
	}
	/**
	 * @param gffChrAbs
	 * @param chrID
	 * @param refSnpIndelStart
	 */
	public MapInfoSnpIndel(GffChrAbs gffChrAbs,String chrID, int refSnpIndelStart) {
		this.gffChrAbs = gffChrAbs;
		this.chrID = chrID;
		this.refSnpIndelStart = refSnpIndelStart;
	    setGffIso();
	}
	
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setRefSnpIndelStart(String chrID, int refSnpIndelStart) {
		this.chrID = chrID;
		this.refSnpIndelStart = refSnpIndelStart;
		setGffIso();
	}
	/** ���ǵ�samPileup�ļ�����ĳһ��ʱ�趨�ģ���ʾ�Ѿ������һ����ֻ����û�ҵ�����
	 * Ȼ�����ֵ���趨Ϊ0
	 *  */
	protected void setSearchSamPileUpFileTrue() {
		SampleRefReadsInfo sampleRefReadsInfo = getAndCreateSampleRefReadsInfo();
		sampleRefReadsInfo.setSearchSampileupFile(true);
	}
	/**
	 * refBase�ڻ����е�λ�ã�0-1֮�䣬0.1��ʾsnp�ڻ��򳤶�*0.1��λ�ô�
	 * ԽСԽ����ͷ��
	 * 0-1֮��
	 */
	private void setProp() {
		if (gffGeneIsoInfo.getCodLoc(getRefSnpIndelStart()) != GffGeneIsoInfo.COD_LOC_EXON) {
			return;
		}
		this.prop = (double)gffGeneIsoInfo.getCod2TSSmRNA(getRefSnpIndelStart())
				/ 
				(gffGeneIsoInfo.getCod2TSSmRNA(getRefSnpIndelStart())  - gffGeneIsoInfo.getCod2TESmRNA(getRefSnpIndelStart()));
	}
	public String getRefID() {
		return chrID;
	}
	/**
	 * �趨�������������������ʱ���趨���������к��ڵ���Ϣ������ӵ���sample��
	 * @param sampleName
	 */
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}
	public boolean isContainsSample(String sampleName) {
		return mapSample2NormReadsInfo.containsKey(sampleName);
	}
	/**
	 * AD Allelic depths for the ref and alt alleles in the order listed
	 * @return
	 */
	public int getReadsNumRef() {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			return 0;
		}
		return sampleRefReadsInfo.getAllelic_depths_Ref();
	}
	/**
	 * GQ The Genotype Quality, as a Phred-scaled confidence at the true genotype is the one provided in GT.
	 *  In diploid case, if GT is 0/1, then GQ is really L(0/1) / (L(0/0) + L(0/1) + L(1/1)), where L is the likelihood 
	 *  of the NGS sequencing data under the model of that the sample is 0/0, 0/1/, or 1/1. �����Ǽ��������
	 * @return
	 */
	public double getGenotype_Quality() {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			return 0;
		}
		return sampleRefReadsInfo.Genotype_Quality;
	}
	
	public int getReadsNumAll() {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			return 0;
		}
		return sampleRefReadsInfo.getReadsDepth();
	}
	/**
	 * SB, How much evidence is there for Strand Bias (the variation being seen
	 *  on only the forward or only the reverse strand) in the reads? Higher SB 
	 *  values denote more bias (and therefore are more likely to indicate false 
	 *  positive calls).
	 * @return
	 */
	public double getStrand_Bias() {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			return 0;
		}
		return sampleRefReadsInfo.getStrand_Bias();
	}
	/**
	 * ����ID
	 * @return
	 */
	public int getTaxID() {
		if (gffChrAbs == null) {
			return 0;
		}
		return gffChrAbs.getTaxID();
	}
	public void setRefBase(String refBase) {
		this.refBase = refBase;
	}
	/**
	 * snp�ڻ��򳤶ȵİٷֱ�
	 * ԽСԽ����ͷ��
	 * -1��ʾû�и���Ŀ
	 */
	public double getProp() {
		return prop;
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
		if (gffGeneIsoInfo != null && gffGeneIsoInfo.equals(mapInfoSnpIndel.getGffIso())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * �����趨������Ϣ�����mapinfosnpindel��Ϣ
	 */
	public void setVcfLines(String sampleName, VcfCols vcfCols, String vcfLines) {
		setSampleName(sampleName);
		String[] inputLines = vcfLines.split("\t");
		refSnpIndelStart = Integer.parseInt(inputLines[vcfCols.colSnpStart]); 
		//TODO :chrID�Ƿ���ҪСд
		chrID = inputLines[vcfCols.colChrID];
		setGffIso();
		SiteSnpIndelInfo siteSnpIndelInfo = addAllenInfo(inputLines[vcfCols.colRefsequence], inputLines[vcfCols.colThisSequence]);
		if (vcfCols.colBaseInfo >= 0)
			setBaseInfo(inputLines[vcfCols.colBaseInfo]);
		if (vcfCols.colQuality >= 0)
			siteSnpIndelInfo.setQuality(inputLines[vcfCols.colQuality]);
		if (vcfCols.colFiltered >= 0)
			siteSnpIndelInfo.setVcfFilterInfo(inputLines[vcfCols.colFiltered]);
		if (vcfCols.colFlagTitle >= 0 && vcfCols.colFlagDetail >= 0) {
			setFlag(inputLines[vcfCols.colFlagTitle], inputLines[vcfCols.colFlagDetail]);
			setDepthAlt(siteSnpIndelInfo, inputLines[vcfCols.colFlagTitle], inputLines[vcfCols.colFlagDetail]);
		}
		if (vcfCols.colSnpDBID>=0) {
			if (!inputLines[vcfCols.colSnpDBID].equals(".")) {
				siteSnpIndelInfo.setDBSnpID(inputLines[vcfCols.colSnpDBID]);
			}
		}
		
	}
	
	/**
	 * �����趨������Ϣ�����mapinfosnpindel��Ϣ
	 * ������ȡsnp��Ϣ������ȡ����snp��Ϣ
	 */
	public void setNBCLines(String sampleName, String novelBioLine) {
		setSampleName(sampleName);
		String[] inputLines = novelBioLine.split("\t");
		//TODO :chrID�Ƿ���ҪСд
		chrID = inputLines[0];
		refSnpIndelStart = Integer.parseInt(inputLines[1]); 
		setGffIso();
		addAllenInfo(inputLines[6], inputLines[7]);
		
	}
	
	/**
	 * �Ϳ������AF,AN,SB
	 *  AB=0.841;AC=1;AF=0.50;AN=2;BaseQRankSum=0.097;DP=63;Dels=0.00;FS=0.000;HRun=0;HaplotypeScore=0.0000;
	 *  ����GATKinfo���趨��Ϣ
	 * @param GATKInfo
	 */
	private void setBaseInfo(String GATKInfo) {
		String[] ssValue = GATKInfo.split(";");
		SampleRefReadsInfo sampleRefReadsInfo = getAndCreateSampleRefReadsInfo();
		for (String string : ssValue) {
			String[] tmpInfo = string.split("=");
			if (tmpInfo[0].equals("SB")) {
				sampleRefReadsInfo.setStrand_Bias(Double.parseDouble(tmpInfo[1]));
			}
		}
	}
	private SampleRefReadsInfo getAndCreateSampleRefReadsInfo() {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			sampleRefReadsInfo = new SampleRefReadsInfo();
			mapSample2NormReadsInfo.put(sampleName, sampleRefReadsInfo);
		}
		return sampleRefReadsInfo;
	}
	/** �趨vcf�е�reads depth�Ǹ��У���Ҫ���趨��vcf�ж�ȡ��reads depth��Ϣ<br>
	 * "GT:AD:DP:GQ:PL", <br>
	 * "0/1:119,100:315:99:3214,0,3784"<br>
	 *  */
	private void setDepthAlt(SiteSnpIndelInfo sampleRefReadsInfo, String flagTitle, String flagDetail) {
		//TODO ������ɾ����һ��Allelic_depths_Alt����Ŀ��������κܺõ���ӽ�ȥ
		String[] ssFlag = flagTitle.split(":");
		String[] ssValue = flagDetail.split(":");
		for (int i = 0; i < ssFlag.length; i++) {
			if (ssFlag[i].equals("AD")) {
				String[] info = ssValue[i].split(",");
				sampleRefReadsInfo.setThisReadsNum(Integer.parseInt(info[1]));
			}
		}
	}
	/**
	 * ������ɾ����һ��Allelic_depths_Alt����Ŀ��������κܺõ���ӽ�ȥ
	 * ����<br>
	 * GT:AD:DP:GQ:PL<br>
	 * 0/1:53,10:63:99:150,0,673<br>
	 */
	private void setFlag(String flagTitle, String flagDetail) {
		//TODO ������ɾ����һ��Allelic_depths_Alt����Ŀ��������κܺõ���ӽ�ȥ
		String[] ssFlag = flagTitle.split(":");
		String[] ssValue = flagDetail.split(":");
		SampleRefReadsInfo sampleRefReadsInfo = getAndCreateSampleRefReadsInfo();
		for (int i = 0; i < ssFlag.length; i++) {
			if (ssFlag[i].equals("AD")) {
				String[] info = ssValue[i].split(",");
				sampleRefReadsInfo.setRefDepth(Integer.parseInt(info[0]));
			}
			else if (ssFlag[i].equals("DP")) {
				sampleRefReadsInfo.setReadDepth( Integer.parseInt(ssValue[i]));
			}
			else if (ssFlag[i].equals("GQ")) {
				sampleRefReadsInfo.setGenotype_Quality(Double.parseDouble(ssValue[i]));
			}
			else if (ssFlag[i].equals("GQ")) {
				
			}
		}
	}
	/**
	 *  ������refbase��Ϣ�Ļ����ϣ����Ҹ�refSnpIndelStartλ������Щindel��snp
	 *  �ҵ���indel����Ӧ��refbase���ܺ�ԭ����refbase��һ��
	 * @param samString
	 */
	public void setSamToolsPilup(String samString, GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
		setSamToolsPilup(samString);
	}
	/**
	 *  ������refbase��Ϣ�Ļ����ϣ����Ҹ�refSnpIndelStartλ������Щindel��snp
	 *  �ҵ���indel����Ӧ��refbase���ܺ�ԭ����refbase��һ��
	 * @param samString
	 */
	public void setSamToolsPilup(String samString) {
		String[] ss = samString.split("\t");
		this.chrID = ss[0];
		this.refSnpIndelStart = Integer.parseInt(ss[1]);//�����᲻�趨������ν����Ϊ�����ʱ�����Ҫ����ͬ��ID
		this.refBase = ss[2];
		setGffIso();
		setAllenInfo(Integer.parseInt(ss[3]), ss[4]);
	}

	/**
	 * snp��indel���ڵ�ת¼��
	 * ͬʱ�趨setProp��cis5to3����name������gffGeneIsoInfo����Ϣ
	 */
	private void setGffIso() {
		if (gffChrAbs == null || gffChrAbs.getGffHashGene() == null || (gffGeneIsoInfo != null && prop >= 0))
			return;

		GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(chrID, refSnpIndelStart);
		this.gffGeneIsoInfo = gffCodGene.getCodInCDSIso();
		if (gffGeneIsoInfo == null && gffCodGene.isInsideLoc()) {
			gffGeneIsoInfo = gffCodGene.getGffDetailThis().getLongestSplit();
		}
		if (gffGeneIsoInfo == null) {
			return;
		}
		setProp();
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
	private void setAllenInfo(int readsDepth, String pileUpInfo) {
		clearSampleReadsNum();
		SampleRefReadsInfo sampleRefReadsInfo = getAndCreateSampleRefReadsInfo();
		sampleRefReadsInfo.setSearchSampileupFile(true);
		sampleRefReadsInfo.setReadDepth(readsDepth);
		String thisSeq = refBase;
		char[] pipInfo = pileUpInfo.toCharArray();
		for (int i = 0; i < pipInfo.length; i++) {
			char c = pipInfo[i];
			if (c == '$' || c == '<' || c == '>') continue;
			if (c == '^' ) {
				i ++; continue;//^������mapping��������������
			}
			else if (c == 'n' || c== 'N') {//��ȷ���Ĵ��䲻���
				if (isNextSiteIndel(pipInfo, i)) {//������indel�Ž�thisSeq�趨ΪN������ֱ������
					thisSeq = "N";
				}
				continue;
			}
			else if (c == ',' || c == '.') {
				if (!isNextSiteIndel(pipInfo, i)) {
					sampleRefReadsInfo.addRefDepth(1); continue;
				}
			}
			else if (c == '+' || c == '-') {
				i = setIndel(thisSeq, c, pipInfo, i);
				thisSeq = refBase;//��λreference
			}
			else if (c == '*') {
				continue;
			}
			//mismatch
			else {
				thisSeq = setMisMatchAndGetRefBase(pipInfo, i);
			}
		}
	}
	private boolean isNextSiteIndel(char[] pipInfo, int thisIndex) {
		int nextIndex = thisIndex + 1;
		if (nextIndex < pipInfo.length && (pipInfo[nextIndex] == '+' || pipInfo[nextIndex] == '-'))
			return true;
		return false;
	}
	/** �趨indel��Ϣ��ͬʱ���ؽ����index
	 * Ҳ���������pipInfo��ȡ����λ��
	 * @param thisSeq
	 * @param indelSymbol
	 * @param pipInfo
	 * @param index
	 * @return
	 */
	private int setIndel(String thisSeq, char indelSymbol, char[] pipInfo, int index) {
		String referenceSeq = refBase;
		int tmpInDelNum = 0;
		index ++;
		//�����ͷ�ǡ�+���ţ�����+�ź�����֣�Ҳ����indel�ĳ���
		for (; index < pipInfo.length; index++) {
			char tmpNum = pipInfo[index];
			//ת��Ϊ�����ַ�
			if (tmpNum >= 48 && tmpNum <=57) {
				tmpInDelNum = tmpInDelNum*10 + tmpNum -  48;
			}
			else {//�����ַ�˵������ͷ�ˣ��ͷ���һλ
				index--;
				break;
			}
		}
		//��þ�����ַ�
		char[] tmpSeq = new char[tmpInDelNum];
		for (int j = 0; j < tmpSeq.length; j++) {
			index++;
			tmpSeq[j] = pipInfo[index];
		}
		String indel = String.copyValueOf(tmpSeq);
		if (indelSymbol == '+') {
			thisSeq = thisSeq + indel;
		} else {
			referenceSeq = referenceSeq + indel;
		}
		
		setSnpIndel(referenceSeq, thisSeq);
		return index;
	}
	
	/** �趨����λ�㣬�������λ����滹���Ų����ȱʧ����ֱ�ӷ��ء�<br>
	 * ����ͽ�����װ��snpInfo
	 * @param referenceSeq
	 * @param pipInfo
	 * @param thisIndex
	 * @return �����Indel�����ظ�λ�����ļ������
	 * �������Indel������refbase
	 */
	private String setMisMatchAndGetRefBase(char[] pipInfo, int thisIndex) {
		String thisSeq = pipInfo[thisIndex] + "";
		if (isNextSiteIndel(pipInfo, thisIndex)) {
			return thisSeq;
		}
		setSnpIndel(refBase, thisSeq);
		return refBase;
	}
	/** ����snp��refsequence��thisSeq����������Ϣ����mapAllen2Num�� */
	private void setSnpIndel(String referenceSeq, String thisSeq) {
		String snpIndelInfo = SiteSnpIndelInfo.getMismatchInfo(chrID, refSnpIndelStart, referenceSeq, thisSeq);
		SiteSnpIndelInfo siteSnpIndelInfo = null;
		
		if (mapAllen2Num.containsKey(snpIndelInfo)) {
			siteSnpIndelInfo = mapAllen2Num.get(snpIndelInfo);
			siteSnpIndelInfo.setOrAddSampleInfo(sampleName);
			siteSnpIndelInfo.addThisBaseNum();
		}
		else {
			siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, referenceSeq, thisSeq);
			siteSnpIndelInfo.setOrAddSampleInfo(sampleName);
			siteSnpIndelInfo.setThisReadsNum(1);
			mapAllen2Num.put(snpIndelInfo, siteSnpIndelInfo);
		}
	}
	/** ��Щ�Ѿ���vcf��������snp���и�snp��depth��Ϣ��������������ձ���������snp��ֵ */
	private void clearSampleReadsNum() {
		for (SiteSnpIndelInfo siteSnpIndelInfo : mapAllen2Num.values()) {
			siteSnpIndelInfo.setSampleName(sampleName);
			siteSnpIndelInfo.setThisReadsNum(0);
		}
		SampleRefReadsInfo sampleRefReadsInfo = getAndCreateSampleRefReadsInfo();
		sampleRefReadsInfo.readDepth = 0;
		sampleRefReadsInfo.Allelic_depths_Ref = 0;
	}
	/** ���ؼ����siteSnpIndelInfo */
	public SiteSnpIndelInfo addAllenInfo(String referenceSeq, String thisSeq) {
		SiteSnpIndelInfo siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, referenceSeq, thisSeq);
		siteSnpIndelInfo.setOrAddSampleInfo(sampleName);
		mapAllen2Num.put(SiteSnpIndelInfo.getMismatchInfo(chrID, refSnpIndelStart, referenceSeq, thisSeq), siteSnpIndelInfo);
		return siteSnpIndelInfo;
	}
	/**
	 * ����һ��mapInfoSnpIndel������snpIndel��Ϣװ�뱾�࣬��ô���������ͬ��snpIndel�ͽ����ظ��������snp��������Ϣ
	 * @param mapInfoSnpIndel
	 */
	public void addAllenInfo(MapInfoSnpIndel mapInfoSnpIndel) {
		Collection<SiteSnpIndelInfo> colSiteSnpIndelInfosInput = mapInfoSnpIndel.mapAllen2Num.values();
		for (SiteSnpIndelInfo siteSnpIndelInfoInput : colSiteSnpIndelInfosInput) {
			SiteSnpIndelInfo siteSnpIndelInfoThis = mapAllen2Num.get(siteSnpIndelInfoInput.getMismatchInfo());
			if (siteSnpIndelInfoThis == null) {
				mapAllen2Num.put(siteSnpIndelInfoInput.getMismatchInfo(), siteSnpIndelInfoInput);
			}
			else {
				siteSnpIndelInfoThis.addSiteSnpIndelInfo(siteSnpIndelInfoInput);
			}
		}
		for (Entry<String, SampleRefReadsInfo> entry : mapInfoSnpIndel.mapSample2NormReadsInfo.entrySet()) {
			if (mapSample2NormReadsInfo.containsKey(entry.getKey())) {
				continue;
			}
			mapSample2NormReadsInfo.put(entry.getKey(), entry.getValue());
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
	 * ����mapInfoSnpIndel��������<b>ref</b>,<b>refbase</b>��<b>thisbase</b>��<b>indel</b>��type�����ұ�λ��ĳ��type indel��������<br>
	 * ע�⣬�����mapInfoSnpIndel����ֻ����һ��type��Ҳ����ֻ��ָ��һ����ʽ�Ĵ��䣬<br>
	 * ���������indel�ڲ��ҵ�ʱ��Ὣ��һλɾ������ΪGATK�����ĵ�һλ��indel��ǰһλ<br>
	 * ���ظ�����ʽ�����Լ���Ӧ���������е�reads�ѵ���
	 * ��hash���л��
	 * @param mapInfoSnpIndel �����ı����������Ϣ
	 * @return ��������mapInfoSnpIndelQuery��û�д������Ϣ���򷵻�null
	 */
	public SiteSnpIndelInfo getSnpIndelNum(MapInfoSnpIndel mapInfoSnpIndelQuery) {
		if (mapInfoSnpIndelQuery.getRefSnpIndelStart() != getRefSnpIndelStart()) {
			logger.error("����Ĳ���λ�㲻��ͬһ������λ�㣺" + getRefSnpIndelStart() + "����λ�㣺" + mapInfoSnpIndelQuery.getRefSnpIndelStart());
			return null;
		}
		SiteSnpIndelInfo siteSnpIndelInfoQuery = mapInfoSnpIndelQuery.getSiteSnpInfoBigAllen();
		return getSnpIndel(siteSnpIndelInfoQuery);
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
	public SiteSnpIndelInfo getSnpIndel(SiteSnpIndelInfo siteSnpIndelInfo) {
		if (siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.CORRECT) {
			logger.error("����Ĳ���λ��û�д�����Ϣ����λ�㣺" + getRefSnpIndelStart());
			return null;
		}
		return getSnpIndel(siteSnpIndelInfo.referenceSeq, siteSnpIndelInfo.thisSeq);
	}
	/**
	 * �������кʹ��䷽ʽ�����������е�reads�ѵ���
	 * ��Ϊ��λ������ж��ִ��䣬���Ը���һ��Ȼ����ң������ҵ�����
	 * ��hash���л��
	 * @param referenceSeq
	 * @param thisSeq
	 * @return
	 */
	public SiteSnpIndelInfo getSnpIndel(String referenceSeq, String thisSeq) {
		String tmpInfo = SiteSnpIndelInfo.getMismatchInfo(chrID, refSnpIndelStart, referenceSeq, thisSeq);
		SiteSnpIndelInfo siteSnpIndelInfo = mapAllen2Num.get(tmpInfo);
		if (siteSnpIndelInfo == null) {
			siteSnpIndelInfo = getSiteSnpIndelInfoNone(referenceSeq, thisSeq);
		}
		if (siteSnpIndelInfo != null) {
			siteSnpIndelInfo.setSampleName(sampleName);
		}
		return siteSnpIndelInfo;
	}
	/**
	 * ������������snpλ��
	 * �Ѿ��趨��sampleName
	 */
	public SiteSnpIndelInfo getSiteSnpInfoBigAllen() {
		ArrayList<SiteSnpIndelInfo> lsAllenInfo = getLsAllenInfoSortBig2Small();
		if (lsAllenInfo.size() > 0) {
			return lsAllenInfo.get(0);
		}
		return SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, refBase, refBase);
	}
	/**
	 * �������еķ�ref�Ļ����Լ���Ӧ�����������
	 * ÿ�����趨sampleName
	 * ���û��SiteSnp���򷵻ر���
	 */
	public ArrayList<SiteSnpIndelInfo> getLsAllenInfoSortBig2Small() {
		ArrayList<SiteSnpIndelInfo> lsAllenInfo = ArrayOperate.getArrayListValue(mapAllen2Num);
		for (SiteSnpIndelInfo siteSnpIndelInfo : lsAllenInfo) {
			siteSnpIndelInfo.setSampleName(sampleName);
		}
		Collections.sort(lsAllenInfo, new compMapInfoSnpIndelBig2Small(sampleName));
		return lsAllenInfo;
	}
	//TODO check
	/** �����Ƿ���ҹ�samPileUp�ļ������ؿ�ֵ�����趨Ϊ0��SiteSnpIndelInfo */
	private SiteSnpIndelInfo getSiteSnpIndelInfoNone(String refSequence, String thisSequence) {
		SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
		if (sampleRefReadsInfo == null) {
			return null;
		}
		if (sampleRefReadsInfo.isSearchSampileupFile() == true) {
			SiteSnpIndelInfo siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, refSequence, thisSequence);
			siteSnpIndelInfo.setSampleName(sampleName);
			siteSnpIndelInfo.setThisReadsNum(0);
			return siteSnpIndelInfo;
		}
		else {
			return null;
		}
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
		SiteSnpIndelInfo siteSnpIndelInfoQuery = mapInfoSnpIndel.getSiteSnpInfoBigAllen();
		return getSeqTypeNumStr(siteSnpIndelInfoQuery);
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
	public String getSeqTypeNumStr(SiteSnpIndelInfo siteSnpIndelInfoQuery) {
		SiteSnpIndelInfo siteSnpIndelInfo = getSnpIndel(siteSnpIndelInfoQuery);
		if (siteSnpIndelInfo.getSnpIndelType() == SnpIndelType.CORRECT) {
			return "";
		}
		String tmpResult = getRefID()+"\t"+getRefSnpIndelStart()+"\t" + siteSnpIndelInfoQuery.getReferenceSeq()+"\t" +getReadsNumRef();
		tmpResult = tmpResult + "\t" +siteSnpIndelInfo.getThisSeq() + "\t" + siteSnpIndelInfo.getReadsNum();
		return tmpResult;
	}
	
	/**
	 * ����ȫ��snp���ͺ���������Ϣ
	 * @return
	 */
	public ArrayList<String[]> toStringLsSnp() {
		return toStringLsSnp(null,false);
	}
	/**
	 * ������ѡ����snp���ͺ���������Ϣ
	 * @return
	 */
	public ArrayList<String[]> toStringLsSnp(ArrayList<SiteSnpIndelInfo> lsMismatchInfo) {
		return toStringLsSnp(null,false, lsMismatchInfo);
	}
	/**
	 * ����������������ȫ��snp���ͺ���������Ϣ
	 * @param lsSampleNames ������
	 * @param getGATK �Ƿ����GATK�϶���snp��ȡ����
	 * @return
	 */
	public ArrayList<String[]> toStringLsSnp(Collection<String> lsSampleNames, boolean getGATK) {
		return toStringLsSnp(lsSampleNames, getGATK, new ArrayList<SiteSnpIndelInfo>());
	}
	/**
	 * @param lsSampleNames
	 * @param getGATK
	 * @param lsMismatchInfo ��ѡ��ָ����snp sizeΪ0�򷵻ر�λ��ȫ��snp��indel�����Ϊ�ձ�ʾ��ȡȫ��������Ϊnull
	 * @return
	 */
	public ArrayList<String[]> toStringLsSnp(Collection<String> lsSampleNames, boolean getGATK, ArrayList<SiteSnpIndelInfo> lsMismatchInfo) {
		HashSet<String> setSnpSite = new HashSet<String>();
		for (SiteSnpIndelInfo siteSnpIndelInfo : lsMismatchInfo) {
			setSnpSite.add(siteSnpIndelInfo.getMismatchInfo());
		}
		return getStringLsSnp(null,false, setSnpSite);
	}
	/**
	 * ����������������ȫ��snp���ͺ���������Ϣ
	 * @param lsSampleNames ������, ȫ����ѡnull
	 * @param getGATK �Ƿ����GATK�϶���snp��ȡ���� û��GATK��ѡfalse
	 * @param setMismatchInfo ��ѡ��ָ����snp sizeΪ0�򷵻ر�λ��ȫ��snp��indel
	 * @return
	 */
	private ArrayList<String[]> getStringLsSnp(Collection<String> lsSampleNames, boolean getGATK, Set<String> setMismatchInfo) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		LinkedList<String> lsResultTmp = new LinkedList<String>();
		lsResultTmp.add(chrID);//0
		lsResultTmp.add(refSnpIndelStart + "");//1
		
		if (gffGeneIsoInfo != null) {
			lsResultTmp.add(gffGeneIsoInfo.getName());
			lsResultTmp.add(gffGeneIsoInfo.getGeneID().getSymbol());
			lsResultTmp.add(gffGeneIsoInfo.getGeneID().getDescription());
		}
		else{
			lsResultTmp.add("");
			lsResultTmp.add("");
			lsResultTmp.add("");
		}
		if (prop >= 0)
			lsResultTmp.add(prop + "");
		else
			lsResultTmp.add("");
		
		//����ÿ��snp����ʽ
		for (Entry<String, SiteSnpIndelInfo> entry : mapAllen2Num.entrySet()) {
			SiteSnpIndelInfo siteSnpIndelInfo = entry.getValue();
			if (getGATK && !isGATKfiltered(siteSnpIndelInfo))
				continue;
			if (!isFilteredSite(setMismatchInfo, siteSnpIndelInfo))
				continue;
			
			LinkedList<String> lsTmpInfo = copyList(lsResultTmp);
			
			lsTmpInfo.add(siteSnpIndelInfo.getReferenceSeq());
			lsTmpInfo.add(siteSnpIndelInfo.getThisSeq());
			lsTmpInfo.add(siteSnpIndelInfo.getSnpIndelRs().getSnpRsID());
			if (lsSampleNames == null) {
				lsSampleNames = mapSample2NormReadsInfo.keySet();
			}
			for (String sampleName : lsSampleNames) {
				SampleRefReadsInfo sampleRefReadsInfo = mapSample2NormReadsInfo.get(sampleName);
				if (sampleRefReadsInfo == null) {
//					for (int i = 0; i < 4; i++)
//						lsTmpInfo.add("");
					continue;
				}
				siteSnpIndelInfo.setSampleName(sampleName);
				lsTmpInfo.add(sampleRefReadsInfo.getReadsDepth() + "");
				lsTmpInfo.add(siteSnpIndelInfo.getReadsNum() + "");
				lsTmpInfo.add(siteSnpIndelInfo.getVcfInfoFilter());
				lsTmpInfo.add(siteSnpIndelInfo.getQuality());
			}
			
			lsTmpInfo.add(siteSnpIndelInfo.getOrfShift() + "");
			lsTmpInfo.add(siteSnpIndelInfo.isExon() + "");
			if (siteSnpIndelInfo.isCDS()) {
				lsTmpInfo.add(siteSnpIndelInfo.getRefAAnr().toString());
				lsTmpInfo.add(siteSnpIndelInfo.getRefAAnr().toStringAA1());
				lsTmpInfo.add(siteSnpIndelInfo.getThisAAnr().toString());
				lsTmpInfo.add(siteSnpIndelInfo.getThisAAnr().toStringAA1());
				lsTmpInfo.add(siteSnpIndelInfo.getAAattrConvert());
				lsTmpInfo.add(siteSnpIndelInfo.getSplitTypeEffected());
			}
			else {
//				for (int i = 0; i < 6; i++)
//					lsTmpInfo.add("");
			}
			String[] infpoStrings = getStrArray(lsTmpInfo);
			lsResult.add(infpoStrings);
		}
		return lsResult;
	}

	/** 
	 * ���ݸ�����������������title
	 * @param lsSampleNames
	 * @return
	 */
	public static String[] getTitleFromSampleName(Collection<String> lsSampleNames) {
		LinkedList<String> lsTitle = new LinkedList<String>();
		lsTitle.add("ChrID");
		lsTitle.add("Loc");
		lsTitle.add("GeneID");
		lsTitle.add("GeneSymbol");
		lsTitle.add("Description");
		lsTitle.add("Distance2GeneStart");
		lsTitle.add("RefSequence");
		lsTitle.add("ThisSequence");
		lsTitle.add("DBsnpID");
		for (String sampleName : lsSampleNames) {
			lsTitle.add(sampleName + "_ReadsDepth");
			lsTitle.add(sampleName + "_ThisReadsNum");
			lsTitle.add(sampleName + "_FilteredFlag");
			lsTitle.add(sampleName + "_Quality");
		}
		lsTitle.add("OrfShift");
		lsTitle.add("IsInExon");
		lsTitle.add("RefAAnr");
		lsTitle.add("RefAA");
		lsTitle.add("ThisAAnr");
		lsTitle.add("ThisAA");
		lsTitle.add("Chemical Transform");
		lsTitle.add("split info");
		String[] infpoStrings = getStrArray(lsTitle);
		return infpoStrings;
	}
	/**
	 * ��ָ���ģ��Ѿ�ͨ�����˵ļ����У������Ƿ����ĳ��snp
	 * �����˷���true
	 * @param setMismatchInfo ָ����ͨ��ɸѡ��snp���� �ռ�����sizeΪ0ֱ�ӷ���true
	 * @param siteSnpIndelInfo ������filter��snp��Ϣ
	 * @return
	 */
	private boolean isFilteredSite(Set<String> setMismatchInfo, SiteSnpIndelInfo siteSnpIndelInfo) {
		if (setMismatchInfo.size() == 0) {
			return true;
		}
		else if (setMismatchInfo.contains(siteSnpIndelInfo.getMismatchInfo())) {
			return true;
		}
		return false;
	}
	
	private boolean isGATKfiltered(SiteSnpIndelInfo siteSnpIndelInfo) {
		boolean result = false;
		Map<String, SampleSnpReadsQuality> mapSample2Snp = siteSnpIndelInfo.mapSample2thisBaseNum;
		for (SampleSnpReadsQuality sampleSnpReadsQuality : mapSample2Snp.values()) {
			if (sampleSnpReadsQuality.quality != null && !sampleSnpReadsQuality.quality.equals("")) {
				return true;
			}
		}
		return result;
	}
	
	private LinkedList<String> copyList(List<String> lsSrc) {
		LinkedList<String> lsResult = new LinkedList<String>();
		for (String string : lsSrc) {
			lsResult.add(string);
		}
		return lsResult;
	}
	private static String[] getStrArray(List<String> lsInfo) {
		String[] strarray = new String[lsInfo.size()];
		int i = 0;
		for (String string : lsInfo) {
			strarray[i] = string;
			i ++;
		}
		return strarray;
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
	public void clear() {
		gffChrAbs = null;
		gffGeneIsoInfo = null;
		mapAllen2Num.clear();
		mapSample2NormReadsInfo.clear();
	}
}
/** ĳ�������ڸ�λ���reference reads���� */
class SampleRefReadsInfo {
	/** AD
	 * ref��reads����
	 */
	int Allelic_depths_Ref = 0;
	/**  DP
	 * ȫ�����˺��reads������only filtered reads used for calling
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
	/** ��˼������sampileup�ļ��в������һ�� */
	boolean searchSampileupFile = false;
	
	public SampleRefReadsInfo() { }
	
	public SampleRefReadsInfo(int readDepth) {
		this.readDepth = readDepth;
	}
	
	protected void setSearchSampileupFile(boolean searchSampileupFile) {
		this.searchSampileupFile = searchSampileupFile;
	}
	public boolean isSearchSampileupFile() {
		return searchSampileupFile;
	}
	public void setReadDepth(int readDepth) {
		this.readDepth = readDepth;
	}
	public void setRefDepth(int allelic_depths_Ref) {
		this.Allelic_depths_Ref = allelic_depths_Ref;
	}
	public void addRefDepth(int num) {
		this.Allelic_depths_Ref = Allelic_depths_Ref + num;
	}
	public void setGenotype_Quality(double genotype_Quality) {
		Genotype_Quality = genotype_Quality;
	}
	/** ����GATK��vcf��Ϣ */
	public void setStrand_Bias(double strand_Bias) {
		Strand_Bias = strand_Bias;
	}
	public int getAllelic_depths_Ref() {
		return Allelic_depths_Ref;
	}
	public double getGenotype_Quality() {
		return Genotype_Quality;
	}

	public int getReadsDepth() {
		return readDepth;
	}
	public double getStrand_Bias() {
		return Strand_Bias;
	}
}
/** �趨��Ҫ�����������Ҳ������������֣�Ȼ����ݸ���������Ϣ�������� */
class compMapInfoSnpIndelBig2Small implements Comparator<SiteSnpIndelInfo> {
	String sampleName;
	public compMapInfoSnpIndelBig2Small(String sampleName) {
		this.sampleName = sampleName;
	}
	//��������
	public int compare(SiteSnpIndelInfo o1, SiteSnpIndelInfo o2) {
		o1.setSampleName(sampleName);
		o2.setSampleName(sampleName);
		Integer readsNum1 = o1.getReadsNum();
		Integer readsNum2 = o2.getReadsNum();
		return -readsNum1.compareTo(readsNum2);
	}
}



