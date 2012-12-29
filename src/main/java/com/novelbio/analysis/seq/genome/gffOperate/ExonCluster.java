package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.imageio.stream.IIOByteBuffer;

import org.apache.log4j.Logger;
import org.apache.velocity.runtime.directive.Foreach;

import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.database.domain.geneanno.SepSign;

public class ExonCluster {
	private static Logger logger = Logger.getLogger(ExonCluster.class);
	ExonCluster exonClusterBefore;
	ExonCluster exonClusterAfter;
	
	Boolean sameExon = null;
	String chrID;
	int startLoc = 0;
	int endLoc = 0;
	ArrayList<ExonInfo> lsCombExon;
	/**
	 * list--����isoform
	 * list--ÿ��isoform�и��������exon
	 * �����iso���������exon��������װ�յ�list
	 * �����iso�������������Χ��,�������û�����list
	 */
	ArrayList<ArrayList<ExonInfo>> lsIsoExon = new ArrayList<ArrayList<ExonInfo>>();
	ArrayList<GffGeneIsoInfo> lsIsoParent = new ArrayList<GffGeneIsoInfo>();
	/** ��iso���������exon��������װ�յ�list
	 *  �����iso�������������Χ��,�������û�����list
	 */
	HashMap<GffGeneIsoInfo, ArrayList<ExonInfo>> mapIso2LsExon = new HashMap<GffGeneIsoInfo, ArrayList<ExonInfo>>();
	/**
	 * ��¼������exoncluster��Iso���Ϳ����exoncluster���Ƕ�exon�ģ�ǰһ��exon�ı��<br>
	 */
	HashMap<GffGeneIsoInfo, Integer> mapIso2ExonNumSkipTheCluster = new HashMap<GffGeneIsoInfo, Integer>();
	
	public ExonCluster(String chrID, int start, int end) {
		this.chrID = chrID;
		this.startLoc = Math.min(start, end);
		this.endLoc = Math.max(start, end);
	}
	public String getChrID() {
		return chrID;
	}
	public int getStartLocAbs() {
		return startLoc;
	}
	public int getEndLocAbs() {
		return endLoc;
	}
	public int getStartCis() {
		if (isCis5To3()) {
			return startLoc;
		} else {
			return endLoc;
		}
	}
	public int getEndCis() {
		if (isCis5To3()) {
			return endLoc;
		} else {
			return startLoc;
		}
	}
	public boolean isCis5To3() {
		for (ArrayList<ExonInfo> lsExonInfos : lsIsoExon) {
			if (lsExonInfos.size() > 0) {
				return lsExonInfos.get(0).isCis5to3();
			}
		}
		logger.error("��exonclusterΪ��");
		return true;
	}
	public void setExonClusterBefore(ExonCluster exonClusterBefore) {
		this.exonClusterBefore = exonClusterBefore;
	}
	public void setExonClusterAfter(ExonCluster exonClusterAfter) {
		this.exonClusterAfter = exonClusterAfter;
	}
	public ExonCluster getExonClusterAfter() {
		return exonClusterAfter;
	}
	public ExonCluster getExonClusterBefore() {
		return exonClusterBefore;
	}
	public String getLocInfo() {
		return chrID + ":" + startLoc + "-" + endLoc;
	}
	public int getLength() {
		return Math.abs(endLoc - startLoc);
	}
	/** ���������ڵ�GffGene */
	public GffDetailGene getParentGene() {
		for (ArrayList<ExonInfo> lsExonInfos : lsIsoExon) {
			if (lsExonInfos.size() > 0) {
				return lsExonInfos.get(0).getParent().getParentGffDetailGene();
			}
		}
		return null;
	}
	
	/**
	 * �����iso���������exon��������װ�յ�list
	 * �����iso�������������Χ��,�������û�����list
	 * @param gffGeneIsoInfo
	 * @param lsExon
	 */
	public void addExonCluster(GffGeneIsoInfo gffGeneIsoInfo, ArrayList<ExonInfo> lsExon) {
		lsIsoExon.add(lsExon);
		mapIso2LsExon.put(gffGeneIsoInfo, lsExon);
	}
	
	/** ��iso���������exon��������װ�յ�list
	 * �����iso�������������Χ��,�������û�����list
	 */
	public HashMap<GffGeneIsoInfo, ArrayList<ExonInfo>> getMapIso2LsExon() {
		return mapIso2LsExon;
	}
	/**
	 * list--����isoform
	 * list--ÿ��isoform�и��������exon
	 * �����iso���������exon��������װ�յ�list
	 */
	public ArrayList<ArrayList<ExonInfo>> getLsIsoExon() {
		return lsIsoExon;
	}
	/**
	 * ��ʱ�������������������iso����ͬһ��tss��
	 * ��ôһͷ�ͻ�¶�����棬�����������������Ҫ�ģ���������ɱ���ӷ���
	 * Ʃ��:<br>
	 * 0-1-----2-3-----4-5-----------<br>
	 *  -----------------4'-5'----------<br>
	 *  ��ô0-1-----2-3������exon�Ͳ���������Ҫ�Ķ���������true<br>
	 * @return
	 */
	public boolean isAtEdge() {
		if (exonClusterBefore == null || exonClusterAfter == null ) {
			int thisExistIso = 0;
			for (ArrayList<ExonInfo> lsexons : lsIsoExon) {
				if (lsexons.size() > 0) {
					thisExistIso++;
				}
			}
			if (thisExistIso == 1) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * ��ʱ�������������������iso����ͬһ��tss������β��ͬ
	 * �����������������Ҫ�ģ���������ɱ���ӷ���
	 * Ʃ��:<br>
	 *4----5-----------<br>
	 *  -4'-5'----------<br>
	 *  ������exon�Ͳ���������Ҫ�Ķ���������true<br>
	 * @return
	 */
	public boolean isNotSameTss_But_SameEnd() {
		ArrayList<ExonInfo> lsExonEdge = new ArrayList<ExonInfo>();
		if (exonClusterBefore != null && exonClusterAfter != null) {
			return false;
		}
		for (ArrayList<ExonInfo> lsexons : lsIsoExon) {
			if (lsexons.size() > 2) {
				return false;
			}
			if (lsexons.size() == 1) {
				lsExonEdge.add(lsexons.get(0));
			}
		}
		if (lsExonEdge.size() <= 1) {
			return false;
		}
		
		if (exonClusterBefore == null ) {
			int end = lsExonEdge.get(0).getEndCis();
			for (int i = 1; i < lsExonEdge.size(); i++) {
				if (end != lsExonEdge.get(i).getEndCis()) {
					return false;
				}
			}
			return true;
		}
		else if (exonClusterAfter == null) {
			int start = lsExonEdge.get(0).getStartCis();
			for (int i = 1; i < lsExonEdge.size(); i++) {
				if (start != lsExonEdge.get(i).getStartCis()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * �������Ƿ�Ϊ��ͬ��exon�������ͬ����ôҲ��û�пɱ���ӵ�˵����
	 * @return
	 */
	public boolean isSameExon() {
		if (sameExon != null) {
			return sameExon;
		}
		//����������в�ֹһ��exon��ת¼�������һ��п�Խ��junction��˵�������пɱ��exon
		if (lsIsoExon.size() >= 1 && mapIso2ExonNumSkipTheCluster.size() >= 1) {
			sameExon = false;
			return false;
		}
		sameExon = true;
		if (lsIsoExon.get(0).size() != 1) {
			sameExon = false;
			return false;
		}
		ExonInfo exonOld = lsIsoExon.get(0).get(0);
		for (int i = 1; i < lsIsoExon.size(); i++) {
			if (lsIsoExon.get(i).size() != 1) {
				sameExon = false;
				break;
			}
			//�Ƚϵ�һ�������ˣ���Ϊ���������ֱ�Ӿͷ���false��
			ExonInfo exon = lsIsoExon.get(i).get(0);
			if (!exon.equals(exonOld)) {
				sameExon = false;
				break;
			}
		}
		return sameExon;
	}	
	
	/** ���ظ�exonCluster�е�����exon */
	public ArrayList<ExonInfo> getAllExons() {
		if (lsCombExon != null) {
			return lsCombExon;
		}
		combExon();
		return lsCombExon;
	}
	
	private void combExon() {
		lsCombExon = new ArrayList<ExonInfo>();
		//����ȥ�ظ���hash��
		HashSet<ExonInfo> hashExon = new HashSet<ExonInfo>();
		for (ArrayList<ExonInfo> lsExon : lsIsoExon) {
			for (ExonInfo is : lsExon) {
				hashExon.add( is);
			}
		}
		for (ExonInfo exonInfo : hashExon) {
			lsCombExon.add(exonInfo);
		}
	}
	/**
	 * ��¼IsoName������Ӧ�ĵ�һ��exonNum�ı��<br>
	 * ��������и�IsoName��ת¼������û��exon�������У�Ҳ��������ȥ�ˣ���ô��¼��Iso�ڱ����ǰһ��exon��Num
	 * @param Isoname
	 * @param exonNumStart
	 */
	public void setIso2ExonNumSkipTheCluster(GffGeneIsoInfo gffGeneIsoInfo, int exonNumStart) {
		mapIso2ExonNumSkipTheCluster.put(gffGeneIsoInfo, exonNumStart);
	}
	/**
	 * ��¼������exoncluster��Iso���Ϳ����exoncluster���Ƕ�exon�ģ�ǰһ��exon�ı��<br>
	 */
	public HashMap<GffGeneIsoInfo, Integer> getMapIso2ExonIndexSkipTheCluster() {
		return mapIso2ExonNumSkipTheCluster;
	}
	
	public ExonSplicingType getExonSplicingType() {
		HashSet<ExonSplicingType> setSplicingTypes = getExonSplicingTypeSet();
		if (setSplicingTypes.size() == 0) {
			return ExonSplicingType.unknown;
		}
		return setSplicingTypes.iterator().next();
	}
	
	/**
	 * ��ñ�exoncluster�ļ�������
	 * TODO ����һЩʶ�𲻳���
	 * @return
	 */
	public HashSet<ExonSplicingType> getExonSplicingTypeSet() {
		HashSet<ExonSplicingType> setSplicingTypes = new HashSet<ExonSplicingType>();
		if (isSameExon()) {
			setSplicingTypes.add(ExonSplicingType.sam_exon);
			return setSplicingTypes;
		}
		
		if (isRetainIntron()) {
			setSplicingTypes.add(ExonSplicingType.retain_intron);
		}
		
		ExonSplicingType splicingType = getIfCassette();
		if (splicingType != null) {
			setSplicingTypes.add(splicingType);
		}
		
		ArrayList<ExonInfo> lsSingleExonInfo = getExonInfoSingleLs();
		//ǰ���exon��һ��
		if (exonClusterBefore != null && !exonClusterBefore.isSameExon()) {
			if (isAltStart(lsSingleExonInfo)) {
				setSplicingTypes.add(ExonSplicingType.altstart);
			}
			if (isAltEnd(lsSingleExonInfo)) {
				setSplicingTypes.add(ExonSplicingType.altend);
			}
			else if (isWithMutually(lsSingleExonInfo, true)) {
				setSplicingTypes.add(ExonSplicingType.mutually_exon);
			}
		}
		//�����exon��һ��
		if (exonClusterAfter != null && !exonClusterAfter.isSameExon()) {
			if (isAltStart(lsSingleExonInfo)) {
				setSplicingTypes.add(ExonSplicingType.altstart);
			}
			if (isAltEnd(lsSingleExonInfo)) {
				setSplicingTypes.add(ExonSplicingType.altend);
			}
			else if (isWithMutually(lsSingleExonInfo, false)) {
				setSplicingTypes.add(ExonSplicingType.mutually_exon);
			}
		}
		setSplicingTypes.addAll(getSpliteTypeAlt5Alt3(lsSingleExonInfo));

		if (setSplicingTypes.contains(ExonSplicingType.altstart) || setSplicingTypes.contains(ExonSplicingType.altend)) {
			if (setSplicingTypes.size() > 1) {
				ExonSplicingType exonSplicingType = setSplicingTypes.iterator().next();
				setSplicingTypes = new HashSet<ExonCluster.ExonSplicingType>();
				setSplicingTypes.add(exonSplicingType);
			}
		}
		if (setSplicingTypes.size() == 0) {
			if (isIsosHaveSameBeforeAfterExon(mapIso2LsExon.keySet(), mapIso2ExonNumSkipTheCluster.keySet())) {
				setSplicingTypes.add(ExonSplicingType.cassette);
			}
		}
		
		if (setSplicingTypes .size() == 0) {
			setSplicingTypes.add(ExonSplicingType.unknown);
		}
		return setSplicingTypes;
	}
	
	private boolean isRetainIntron() {
		//retainIntron������������1������һ������exon��2�����������̵�exon
		boolean twoExon = false;
		boolean oneExon = false;
		for (ArrayList<ExonInfo> lsExon : lsIsoExon) {
			if (lsExon.size() > 1) {
				twoExon = true;
			}
			else if (lsExon.size() == 1) {
				oneExon = true;
			}
		}
		return twoExon && oneExon;
	}

	/**
	 * ��ΪCassetteʱ���趨Ϊ����Cassette����Cassette_Multi
	 * ���������Cassette���������򷵻�null
	 */
	private ExonSplicingType getIfCassette() {
		ExonSplicingType splicingType = null;
		//��ñ�λ�������exon��iso
		ArrayList<GffGeneIsoInfo> lsIso_ExonExist = new ArrayList<GffGeneIsoInfo>();
		for (ArrayList<ExonInfo> lsExonInfos : lsIsoExon) {
			if (lsExonInfos.size() > 0) {
				lsIso_ExonExist.add(lsExonInfos.get(0).getParent());
			}
		}
		//�������ʹ��ڵ�Iso��ǰ��λ�������Ƿ񶼴���
		//Ҳ����˵��λ��������iso��ǰ����붼��exon
		//��λ����exon��iso��ǰ��Ҳ���붼��exon
		//��������casstte������
		if (!isOneIsoHaveExonBeforeAndAfter(lsIso_ExonExist) || !isOneIsoHaveExonBeforeAndAfter(mapIso2ExonNumSkipTheCluster.keySet())) {
			return null;
		}
		
		boolean casstteMulti = false;
		for (ArrayList<ExonInfo> lsExon : lsIsoExon) {
			if (lsExon.size() > 1) {
				casstteMulti = true;
				break;
			}
		}
		if (casstteMulti) {
			splicingType = ExonSplicingType.cassette_multi;
		} else {
			splicingType = ExonSplicingType.cassette;
		}
		return splicingType;
	}
	/**
	 * �Ƿ����ĳ��ת¼������ת¼���ڱ�exonǰ�󶼺���exon
	 * ֻҪ��һ�����ھ��ж�Ϊtrue
	 * ��Ҫ�������ж�casstte��
	 * @return
	 */
	private boolean isOneIsoHaveExonBeforeAndAfter(Collection<GffGeneIsoInfo> lsIso_ExonExist) {
		boolean isbeforeAndAfterContainHaveIso = false;
		if (exonClusterBefore == null || exonClusterAfter == null) {
			return false;
		}
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIso_ExonExist) {
			List<ExonInfo> lsExonInfosBefore = exonClusterBefore.getMapIso2LsExon().get(gffGeneIsoInfo);
			List<ExonInfo> lsExonInfosAfter = exonClusterAfter.getMapIso2LsExon().get(gffGeneIsoInfo);
			if (lsExonInfosBefore != null && lsExonInfosBefore.size() > 0
					&&
					lsExonInfosAfter != null && lsExonInfosAfter.size() > 0
					) {
				isbeforeAndAfterContainHaveIso = true;
				break;
			}
		}
		return isbeforeAndAfterContainHaveIso;
	}
	
	/**
	 * ��������exon�Ͳ�������exon��iso�Ƿ�����ͬ��ǰexon�ͺ�exon
	 * @param lsIso_ExonExist ������exon��iso
	 * @param lsIso_ExonSkip ��������exon��iso
	 */
	private boolean isIsosHaveSameBeforeAfterExon(Collection<GffGeneIsoInfo> lsIso_ExonExist, Collection<GffGeneIsoInfo> lsIso_ExonSkip) {
		int initialNum = -1000;
		Set<String> setBeforAfterExist = getIsoHaveBeforeAndAfterExon(initialNum, lsIso_ExonExist);
		Set<String> setBeforAfterSkip = getIsoHaveBeforeAndAfterExon(initialNum, lsIso_ExonSkip);
		for (String string : setBeforAfterSkip) {
			if (string.contains(initialNum + "")) {
				continue;
			}
			if (setBeforAfterExist.contains(string)) {
				return true;
			}
		}
		return false;
	}
	
	/** 
	 * ���ĳ��iso��ǰ��� exon�����λ��
	 * Ʃ��ĳ��iso��ǰ����һ��exon��������һ��exon
	 * ��ͳ��Ϊ0sepsign0
	 * ���ǰ���ǰ����һ��exon������ĺ���ĺ�����һ��exon
	 * ��ͳ��Ϊ
	 * -1sepSign2
	 * @param initialNum ��ʼ�����֣��趨Ϊһ���Ƚϴ�ĸ����ͺã�����趨��Ʃ��-1000
	 * @param lsIso_ExonExist
	 * @return
	 */
	private HashSet<String> getIsoHaveBeforeAndAfterExon(int initialNum, Collection<GffGeneIsoInfo> lsIso_ExonExist) {
		HashSet<String> setBeforeAfter = new HashSet<String>();
		ExonCluster clusterBefore = exonClusterBefore;
		ExonCluster clusterAfter = exonClusterAfter;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIso_ExonExist) {
			int[] beforeAfter = new int[]{initialNum, initialNum};//��ʼ��Ϊ����
			int numBefore = 0, numAfter = 0;//ֱ����һλ��exon���Ϊ0��������һλ���Ϊ-1
			while (clusterBefore != null) {
				if (clusterBefore.isIsoHaveExon(gffGeneIsoInfo)) {
					beforeAfter[0] = numBefore;
					break;
				}
				clusterBefore = clusterBefore.exonClusterBefore;
				numBefore--;
			}
			while (clusterAfter != null) {
				if (clusterAfter.isIsoHaveExon(gffGeneIsoInfo)) {
					beforeAfter[1] = numAfter;
					break;
				}
				clusterAfter = clusterAfter.exonClusterAfter;
				numAfter++;
			}
			String tmpBeforeAfter = beforeAfter[0] + SepSign.SEP_ID + beforeAfter[1];
			setBeforeAfter.add(tmpBeforeAfter);
		}
		return setBeforeAfter;
	}
	
	/**
	 * @param gffGeneIsoInfo ע��gffgeneIsoInfo��д��hashcode
	 * @return
	 */
	private boolean isIsoHaveExon(GffGeneIsoInfo gffGeneIsoInfo) {
		List<ExonInfo> lsExonInfos = getMapIso2LsExon().get(gffGeneIsoInfo);
		if (lsExonInfos != null && lsExonInfos.size() > 0) {
			return true;
		}
		return false;
	}
	
	/** ��ñ�exoncluster�д��ڵĵ���exon
	 * �������������exon���ͺϲ�Ϊһ��
	 *  **/
	public ArrayList<ExonInfo> getExonInfoSingleLs() {
		//�������λ��ÿ��isoֻ��һ��exon��������Ϣ������˵������ retain intron���ֺ�������exon����Ϣ
		//�����ж�cassette��alt5��alt3�⼸��
		ArrayList<ExonInfo> lsExonTmp = new ArrayList<ExonInfo>();
		HashSet<ExonInfo> setRemoveSameExon = new HashSet<ExonInfo>();
		for (ArrayList<ExonInfo> lsExon : lsIsoExon) {
			if (lsExon.size() == 0) {
				continue;
			}
			ExonInfo exonInfo = lsExon.get(0).clone();
			//����exon��չΪһ�����exon
			if (lsExon.size() > 1) {
				exonInfo.setEndCis(lsExon.get(lsExon.size() - 1).getEndCis());
			}
			
			if (setRemoveSameExon.contains(exonInfo)) {
				continue;
			} else {
				setRemoveSameExon.add(exonInfo);
			}
			lsExonTmp.add(exonInfo);
		}
		return lsExonTmp;
	}
	/**
	 * ������getExonInfoSingleLs�ϲ����lsExonInfo������ÿ��ExonInfo��Դ�ڲ�ͬ��Iso
	 * @param lsExonInfo
	 * @return
	 */
	private boolean isAltStart(List<ExonInfo> lsExonInfo) {
		if (lsExonInfo.size() == 1) {
			ExonInfo exonInfo = lsExonInfo.get(0);
			if (exonInfo.getItemNum() == 0) {
				return true;
			}
		}
		return false;
	}
	/**
	 * ������getExonInfoSingleLs�ϲ����lsExonInfo������ÿ��ExonInfo��Դ�ڲ�ͬ��Iso
	 * @param lsExonInfo
	 * @return
	 */
	private boolean isAltEnd(List<ExonInfo> lsExonInfo) {
		if (lsExonInfo.size() == 1) {
			ExonInfo exonInfo = lsExonInfo.get(0);
			if (exonInfo.getItemNum() == exonInfo.getParent().size() - 1) {
				return true;
			}
		}
		return false;
	}
	/**
	 *  �Ƿ��ǰһ��exon���һ��exonΪmutually--Ҳ���ǻ���
	 * @param lsExonInfo
	 * @param withBefore true���Ƿ��ǰһ��exon����
	 * false �Ƿ�ͺ�һ��exon����
	 * @return
	 */
	private boolean isWithMutually(List<ExonInfo> lsExonInfo, boolean withBefore) {
		ExonCluster exonClusterBeforeOrAfter = null;
		if (withBefore) {
			exonClusterBeforeOrAfter = exonClusterBefore;
		} else {
			exonClusterBeforeOrAfter = exonClusterAfter;
		}
		boolean isThisExonMutually = false;
		for (ExonInfo exonInfo : lsExonInfo) {
			ArrayList<ExonInfo> lsExons = exonClusterBeforeOrAfter.getMapIso2LsExon().get(exonInfo.getParent());
			if (lsExons == null || lsExons.size() == 0 ) {
				isThisExonMutually = true;
				break;
			}
		}
		if (!isThisExonMutually) {
			return false;
		}
		//����iso��ǰ�����exon�����ڱ�λ�㲻����exon
		for (GffGeneIsoInfo gffGeneIsoInfo : mapIso2ExonNumSkipTheCluster.keySet()) {
			ArrayList<ExonInfo> lsExons = exonClusterBeforeOrAfter.getMapIso2LsExon().get(gffGeneIsoInfo);
			if (lsExons != null && lsExons.size() > 0) {
				//���Ҳ��Ǳ�iso�����һ��exon
				if (withBefore && lsExonInfo.get(lsExonInfo.size() - 1).getItemNum() != gffGeneIsoInfo.size())  {
					return true;
				}
				if (!withBefore && lsExonInfo.get(0).getItemNum() != 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** ���жϱ�λ��Ŀɱ�������
	 * Ҳ���ǽ��ж�alt5��alt3
	 */
	private LinkedList<ExonSplicingType> getSpliteTypeAlt5Alt3(List<ExonInfo> lsExonInfo) {
		LinkedList<ExonSplicingType> setSplicingTypes = new LinkedList<ExonSplicingType>();
		if (lsExonInfo.size() <= 1) {
			return setSplicingTypes;
		}
		int start = lsExonInfo.get(0).getStartCis(), end = lsExonInfo.get(0).getEndCis();
		for (int i = 1; i < lsExonInfo.size(); i++) {
			ExonInfo exonInfo = lsExonInfo.get(i);
			if (exonInfo.getItemNum() != 0 && start != exonInfo.getStartCis()) {
				setSplicingTypes.add(ExonSplicingType.alt3);
			}
			if (exonInfo.getItemNum() != exonInfo.getParent().size() - 1 && end != exonInfo.getEndCis()) {
				setSplicingTypes.add(ExonSplicingType.alt5);
			}
		}
		return setSplicingTypes;
	}
	/** ��ȡ�б仯������������ȡ������ı��ֵ�������Ƿ�Ϊ����Ŀɱ����
	 * Ʃ��cassttet��ֱ����ȡ����exon
	 * ��alt5�Ⱦ���ȡ������ĸ�Ƭ�� 
	 */
	public SiteInfo getDifSite() {
		HashSet<ExonSplicingType> setExonSplicingTypes = getExonSplicingTypeSet();
		SiteInfo siteInfo = null;
		if (setExonSplicingTypes.size() == 1) {
			if (setExonSplicingTypes.contains(ExonSplicingType.alt5)) {
				siteInfo = getAlt5Site();
			}
			else if (setExonSplicingTypes.contains(ExonSplicingType.alt3)) {
				siteInfo = getAlt3Site();
			}
		}
		if (setExonSplicingTypes.iterator().next() == ExonSplicingType.retain_intron) {
			siteInfo = getRetainIntronSite();
		}
		if (siteInfo == null) {
			siteInfo = new SiteInfo(chrID, startLoc, endLoc);
		}
		return siteInfo;
	}

	/** ���alt5�� alt3�Ĳ���λ�� */
	private SiteInfo getAlt5Site() {
		ArrayList<ExonInfo> lsExonInfo = getExonInfoSingleLs();
		//���ճ�������
		Collections.sort(lsExonInfo, new Comparator<ExonInfo>() {
			public int compare(ExonInfo o1, ExonInfo o2) {
				Integer start1 = o1.getEndCis();
				Integer start2 = o2.getEndCis();
				return start1.compareTo(start2);
			}
		});
		int start = lsExonInfo.get(0).getEndCis();
		int end = lsExonInfo.get(lsExonInfo.size() - 1).getEndCis();
		SiteInfo siteInfo = new SiteInfo(chrID, start, end);
		return siteInfo;
	}
	
	/** ���alt5�� alt3�Ĳ���λ�� */
	private SiteInfo getAlt3Site() {
		ArrayList<ExonInfo> lsExonInfo = getExonInfoSingleLs();
		//���ճ�������
		Collections.sort(lsExonInfo, new Comparator<ExonInfo>() {
			public int compare(ExonInfo o1, ExonInfo o2) {
				Integer start1 = o1.getStartCis();
				Integer start2 = o2.getStartCis();
				return start1.compareTo(start2);
			}
		});
		int start = lsExonInfo.get(0).getStartCis();
		int end = lsExonInfo.get(lsExonInfo.size() - 1).getStartCis();
		SiteInfo siteInfo = new SiteInfo(chrID, start, end);
		return siteInfo;
	}
	
	private SiteInfo getRetainIntronSite() {
		for (ArrayList<ExonInfo> lsExonInfo : lsIsoExon) {
			if (lsExonInfo.size() > 1) {
				int start = lsExonInfo.get(0).getEndCis();
				int end = lsExonInfo.get(1).getStartCis();
				return new SiteInfo(chrID, start, end);
			}
		}
		return null;
	}
	public static enum ExonSplicingType {
		cassette, cassette_multi, alt5, alt3, altend, altstart, mutually_exon, retain_intron, unknown, sam_exon;
		
		static HashMap<String, ExonSplicingType> mapName2Events = new LinkedHashMap<String, ExonCluster.ExonSplicingType>();
		public static HashMap<String, ExonSplicingType> getMapName2SplicingEvents() {
			if (mapName2Events.size() == 0) {
				mapName2Events.put("cassette", cassette);
				mapName2Events.put("cassette_multi", cassette_multi);
				mapName2Events.put("alt5", alt5);
				mapName2Events.put("alt3", alt3);
				mapName2Events.put("altend", altend);
				mapName2Events.put("altstart", altstart);
				mapName2Events.put("mutually_exon", mutually_exon);
				mapName2Events.put("retain_intron", retain_intron);
				mapName2Events.put("unknown", unknown);
				mapName2Events.put("sam_exon", sam_exon);
			}
			return mapName2Events;
		}
	}
}

