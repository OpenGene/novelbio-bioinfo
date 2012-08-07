package com.novelbio.analysis.seq.genomeNew.mappingOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.text.html.parser.Entity;

import org.apache.log4j.Logger;
import org.omg.CosNaming._BindingIteratorImplBase;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
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
	HashMap<String, SampleRefReadsInfo> mapSample2NormReadsInfo = new HashMap<String, SampleRefReadsInfo>();
	String sampleName = "";
	/** Ҫ���Ѿ���sam pileUp�����������ˣ���ô���趨��������sample�ǿ����ҵ��� */

	public MapInfoSnpIndel() {}
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
		if (mapSample2NormReadsInfo.containsKey(sampleName)) {
			return true;
		}
		return false;
	}
	/**
	 * AD Allelic depths for the ref and alt alleles in the order listed
	 * @return
	 */
	public int getAllelic_depths_Ref() {
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
	
	public int getReadsDepth() {
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
		if (gffGeneIsoInfo.equals(mapInfoSnpIndel.getGffIso())) {
			return true;
		}
		else {
			return false;
		}
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
	 * �Ϳ������AF,AN,SB
	 *  AB=0.841;AC=1;AF=0.50;AN=2;BaseQRankSum=0.097;DP=63;Dels=0.00;FS=0.000;HRun=0;HaplotypeScore=0.0000;
	 *  ����GATKinfo���趨��Ϣ
	 * @param GATKInfo
	 */
	public void setBaseInfo(String GATKInfo) {
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
	/**
	 *  ������refbase��Ϣ�Ļ����ϣ����Ҹ�refSnpIndelStartλ������Щindel��snp
	 *  �ҵ���indel����Ӧ��refbase���ܺ�ԭ����refbase��һ��
	 * @param samString
	 */
	public void setSamToolsPilup(String samString, GffChrAbs gffChrAbs) {
		String[] ss = samString.split("\t");
		this.chrID = ss[0];
		this.refSnpIndelStart = Integer.parseInt(ss[1]);//�����᲻�趨������ν����Ϊ�����ʱ�����Ҫ����ͬ��ID
		this.refBase = ss[2];
		this.gffChrAbs = gffChrAbs;
		setGffIso();
		setAllenInfo(Integer.parseInt(ss[3]), ss[4]);
	}
	/**
	 * snp��indel���ڵ�ת¼��
	 * ͬʱ�趨setProp��cis5to3����name������gffGeneIsoInfo����Ϣ
	 */
	private void setGffIso() {
		if (gffChrAbs == null || (gffGeneIsoInfo != null && prop >= 0))
			return;
//TODO
		this.gffGeneIsoInfo = gffChrAbs.getGffHashGene().searchLocation(chrID, refSnpIndelStart).getCodInExonIso();
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
		SampleRefReadsInfo sampleRefReadsInfo = getAndCreateSampleRefReadsInfo();
		sampleRefReadsInfo.setSearchSampileupFile(true);
		sampleRefReadsInfo.setReadDepth(0);
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
				sampleRefReadsInfo.addRefDepth(1); continue;
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
					siteSnpIndelInfo.setOrAddSampleInfo(sampleName);
					siteSnpIndelInfo.addThisBaseNum();
				}
				else {
					siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, gffChrAbs, referenceSeq, thisSeq);
					siteSnpIndelInfo.setOrAddSampleInfo(sampleName);
					siteSnpIndelInfo.setThisReadsNum(1);
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
					siteSnpIndelInfo.setOrAddSampleInfo(sampleName);
					siteSnpIndelInfo.addThisBaseNum();
				}
				else {
					siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, gffChrAbs, refBase, thisSeq);
					siteSnpIndelInfo.setOrAddSampleInfo(sampleName);
					siteSnpIndelInfo.setThisReadsNum(1);
					mapAllen2Num.put(mismatchInfo, siteSnpIndelInfo);
				}
			}
		}
	}
	/** ���ؼ����siteSnpIndelInfo */
	public SiteSnpIndelInfo addAllenInfo(String referenceSeq, String thisSeq) {
		SiteSnpIndelInfo siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, gffChrAbs, referenceSeq, thisSeq);
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
	 * @return ������-1
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
		SiteSnpIndelInfo siteSnpIndelInfo =  mapAllen2Num.get(tmpInfo);
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
		return null;
	}
	/**
	 * �������еķ�ref�Ļ����Լ���Ӧ�����������
	 * ÿ�����趨sampleName
	 */
	public ArrayList<SiteSnpIndelInfo> getLsAllenInfoSortBig2Small() {
		ArrayList<SiteSnpIndelInfo> lsAllenInfo = ArrayOperate.getArrayListValue(mapAllen2Num);
		if (lsAllenInfo.size() == 0) {
			SiteSnpIndelInfo siteSnpIndelInfo = getSiteSnpIndelInfoNone(refBase, refBase);
			if (siteSnpIndelInfo != null) {
				lsAllenInfo.add(siteSnpIndelInfo);
			}
			return lsAllenInfo;
		}
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
			SiteSnpIndelInfo siteSnpIndelInfo = SiteSnpIndelInfoFactory.creatSiteSnpIndelInfo(this, gffChrAbs, refSequence, thisSequence);
			siteSnpIndelInfo.setSampleName(sampleName);
			siteSnpIndelInfo.setThisReadsNum(0);
			return siteSnpIndelInfo;
		}
		else {
			return null;
		}
	}
	/**
	 * ����ȫ��snp���ͺ���������Ϣ
	 * @return
	 */
	public ArrayList<String[]> toStringLsSnp() {
		return toStringLsSnp(null,false);
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
		if (siteSnpIndelInfo == null) {
			return "";
		}
		String tmpResult = getRefID()+"\t"+getRefSnpIndelStart()+"\t" + siteSnpIndelInfoQuery.getReferenceSeq()+"\t" +getAllelic_depths_Ref();
		tmpResult = tmpResult + "\t" +siteSnpIndelInfo.getThisSeq() + "\t" + siteSnpIndelInfo.getThisReadsNum();
		return tmpResult;
	}
	/**
	 * ����ȫ��snp���ͺ���������Ϣ
	 * @return
	 */
	public ArrayList<String[]> toStringLsSnp(Collection<String> lsSampleNames, boolean getGATK) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		LinkedList<String> lsResultTmp = new LinkedList<String>();
		lsResultTmp.add(chrID);//0
		lsResultTmp.add(refSnpIndelStart + "");//1
		
		if (gffGeneIsoInfo != null) {
			lsResultTmp.add(gffGeneIsoInfo.getName());
			lsResultTmp.add(gffGeneIsoInfo.getGeneID().getSymbol());
			lsResultTmp.add(gffGeneIsoInfo.getGeneID().getDescription());
		}
		else
			lsResultTmp.add("");
		if (prop >= 0)
			lsResultTmp.add(prop + "");
		else
			lsResultTmp.add("");
		
		//����ÿ��snp����ʽ
		for (Entry<String, SiteSnpIndelInfo> entry : mapAllen2Num.entrySet()) {
			SiteSnpIndelInfo siteSnpIndelInfo = entry.getValue();
			if (getGATK && !isGATKfiltered(siteSnpIndelInfo)) {
				continue;
			}
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
					for (int i = 0; i < 4; i++)
						lsTmpInfo.add("");
					continue;
				}
				siteSnpIndelInfo.setSampleName(sampleName);
				lsTmpInfo.add(sampleRefReadsInfo.getReadsDepth() + "");
				lsTmpInfo.add(siteSnpIndelInfo.getThisReadsNum() + "");
				lsTmpInfo.add(siteSnpIndelInfo.getFiltered());
				lsTmpInfo.add(siteSnpIndelInfo.getQuality());
			}
			
			lsTmpInfo.add(siteSnpIndelInfo.getOrfShift() + "");
			lsTmpInfo.add(siteSnpIndelInfo.isExon() + "");
			if (siteSnpIndelInfo.isCDS()) {
				lsTmpInfo.add(siteSnpIndelInfo.getRefAAnr().toString());
				lsTmpInfo.add(siteSnpIndelInfo.getRefAAnr().toStringAA());
				lsTmpInfo.add(siteSnpIndelInfo.getThisAAnr().toString());
				lsTmpInfo.add(siteSnpIndelInfo.getThisAAnr().toStringAA());
				lsTmpInfo.add(siteSnpIndelInfo.getAAattrConvert());
				lsTmpInfo.add(siteSnpIndelInfo.getSplitTypeEffected());
			}
			else {
				for (int i = 0; i < 6; i++)
					lsTmpInfo.add("");
			}
			String[] infpoStrings = getStrArray(lsTmpInfo);
			lsResult.add(infpoStrings);
		}
		return lsResult;
	}
	
	private boolean isGATKfiltered(SiteSnpIndelInfo siteSnpIndelInfo) {
		boolean result = false;
		HashMap<String, SampleSnpReadsQuality> mapSample2Snp = siteSnpIndelInfo.mapSample2thisBaseNum;
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
	public static String[] getTitle(Collection<String> lsSampleNames) {
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
	 * ����ѡ�е�mapInfo����ȡsamtools������pileup file���ÿ��λ��ľ�����Ϣ
	 * @param sampleName
	 * @param lsSite ������refbase��������Ϣ
	 * @param samToolsPleUpFile samtools�������ļ�
	 * @param gffChrAbs
	 */
	public static void getSiteInfo(String sampleName, List<MapInfoSnpIndel> lsSite, String samToolsPleUpFile, GffChrAbs gffChrAbs) {
		/** ÿ��chrID��Ӧһ��mapinfo��Ҳ����һ��list */
		HashMap<String, ArrayList<MapInfoSnpIndel>> mapSortedChrID2MapInfo = sortLsMapInfoSnpIndel(lsSite);
		getSiteInfo(sampleName, mapSortedChrID2MapInfo, samToolsPleUpFile, gffChrAbs);
	}
	public static HashMap<String, ArrayList<MapInfoSnpIndel>> sortLsMapInfoSnpIndel(List<MapInfoSnpIndel> lsSite) {
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
	/**
	 * ����ѡ�е�mapInfo����ȡsamtools������pileup file���ÿ��λ��ľ�����Ϣ
	 * @param sampleName �������֡���������mapSortedChrID2LsMapInfo�Ѿ��и�������Ϣ����ô������
	 * @param mapSortedChrID2LsMapInfo LsMapInfo�Ź����list
	 * @param samToolsPleUpFile
	 * @param gffChrAbs
	 * @return �½�һ��hash��Ȼ�󷵻أ����hash��������ı���deep copy��ϵ
	 */
	public static void getSiteInfo(String sampleName, HashMap<String, ArrayList<MapInfoSnpIndel>> mapChrID2SortedLsMapInfo, String samToolsPleUpFile, GffChrAbs gffChrAbs) {
		/** ÿ��chrID��Ӧһ��mapinfo��Ҳ����һ��list */
		TxtReadandWrite txtReadSam = new TxtReadandWrite(samToolsPleUpFile, false);
		String tmpChrID = ""; ArrayList<MapInfoSnpIndel> lsMapInfos = null;
		int mapInfoIndex = 0;// ���ν�����ȥ
		for (String samtoolsLine : txtReadSam.readlines()) {
			String[] ss = samtoolsLine.split("\t");
			int loc = Integer.parseInt(ss[1]);
			if (!ss[0].equals(tmpChrID)) {
				tmpChrID = ss[0];
				lsMapInfos = mapChrID2SortedLsMapInfo.get(tmpChrID);
				mapInfoIndex = 0;
				if (lsMapInfos == null) {
					logger.info("����δ֪ chrID��" + tmpChrID);
					continue;
				}
			}
			//����lsMapInfos�е���Ϣ�����������
			if (lsMapInfos == null || mapInfoIndex >= lsMapInfos.size()) continue;

			//һ��һ������ȥ��ֱ���ҵ�����Ҫ��λ��
			if (loc < lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart())
				continue;
			else {
				if (loc == lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
					addMapSiteInfo(gffChrAbs, sampleName, lsMapInfos.get(mapInfoIndex), samtoolsLine);
					mapInfoIndex++;
				}
				else {
					while (mapInfoIndex < lsMapInfos.size()&& loc > lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
						MapInfoSnpIndel mapInfoSnpIndel = lsMapInfos.get(mapInfoIndex);
						mapInfoSnpIndel.setSampleName(sampleName);
						mapInfoSnpIndel.setSearchSamPileUpFileTrue();
						mapInfoIndex++;
					}
					if (mapInfoIndex >= lsMapInfos.size()) {
						continue;
					} else if (loc == lsMapInfos.get(mapInfoIndex).getRefSnpIndelStart()) {
						addMapSiteInfo(gffChrAbs, sampleName, lsMapInfos.get(mapInfoIndex), samtoolsLine);
						mapInfoIndex++;
					}
				}
			}
		}
		logger.info("readOverFile:" + samToolsPleUpFile);
	}
	private static void addMapSiteInfo(GffChrAbs gffChrAbs, String sampleName, MapInfoSnpIndel mapInfoSnpIndel, String samtoolsLine) {
		if (mapInfoSnpIndel.isContainsSample(sampleName)) {
			return;
		}
		else {
			mapInfoSnpIndel.setSampleName(sampleName);
			mapInfoSnpIndel.setSamToolsPilup(samtoolsLine, gffChrAbs);
		}
	}

}

class SampleRefReadsInfo {
	public SampleRefReadsInfo() { }
	
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
	
	boolean searchSampileupFile = false;
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

class compMapInfoSnpIndelBig2Small implements Comparator<SiteSnpIndelInfo> {
	String sampleName;
	public compMapInfoSnpIndelBig2Small(String sampleName) {
		this.sampleName = sampleName;
	}
	//��������
	public int compare(SiteSnpIndelInfo o1, SiteSnpIndelInfo o2) {
		o1.setSampleName(sampleName);
		o2.setSampleName(sampleName);
		Integer readsNum1 = o1.getThisReadsNum();
		Integer readsNum2 = o2.getThisReadsNum();
		return -readsNum1.compareTo(readsNum2);
	}
}



