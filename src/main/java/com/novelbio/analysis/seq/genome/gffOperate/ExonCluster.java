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

import org.apache.velocity.runtime.directive.Foreach;

import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;

public class ExonCluster {
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
	 */
	ArrayList<ArrayList<ExonInfo>> lsIsoExon = new ArrayList<ArrayList<ExonInfo>>();
	ArrayList<GffGeneIsoInfo> lsIsoParent = new ArrayList<GffGeneIsoInfo>();
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
	public void setExonClusterBefore(ExonCluster exonClusterBefore) {
		this.exonClusterBefore = exonClusterBefore;
	}
	public void setExonClusterAfter(ExonCluster exonClusterAfter) {
		this.exonClusterAfter = exonClusterAfter;
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
	 * @param gffGeneIsoInfo
	 * @param lsExon
	 */
	public void addExonCluster(GffGeneIsoInfo gffGeneIsoInfo, ArrayList<ExonInfo> lsExon) {
		lsIsoExon.add(lsExon);
		mapIso2LsExon.put(gffGeneIsoInfo, lsExon);
	}
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
		
		//ǰ���exonһ��
		if (exonClusterBefore != null && exonClusterBefore.isSameExon()
				&& exonClusterAfter != null && exonClusterAfter.isSameExon()
				)
		{
			if (mapIso2ExonNumSkipTheCluster.size() > 0) {
				setCassette(setSplicingTypes);
			}
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
			else {
				setSplicingTypes.addAll(searchBeforeExon(lsSingleExonInfo));
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
			else {
				setSplicingTypes.addAll(searchAfterExon(lsSingleExonInfo));
			}
		}
		setSplicingTypes.addAll(getSingleSiteSpliteType(lsSingleExonInfo));

		if (setSplicingTypes.contains(ExonSplicingType.altstart) || setSplicingTypes.contains(ExonSplicingType.altend)) {
			if (setSplicingTypes.size() > 1) {
				ExonSplicingType exonSplicingType = setSplicingTypes.iterator().next();
				setSplicingTypes = new HashSet<ExonCluster.ExonSplicingType>();
				setSplicingTypes.add(exonSplicingType);
			}
		}
		if (setSplicingTypes.size() == 0) {
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
	 * ��ΪCassetteʱ���趨Ϊ����Cassette���Ƕ��
	 */
	private void setCassette(HashSet<ExonSplicingType> setSplicingTypes) {
		//��������exon��iso
		if (mapIso2ExonNumSkipTheCluster.size() == 0 || exonClusterBefore == null || exonClusterAfter == null) {
			return;
		}
		
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
		if (!isIsoExistBeforeAndAfter(lsIso_ExonExist) || !isIsoExistBeforeAndAfter(mapIso2ExonNumSkipTheCluster.keySet())) {
			return;
		}
		
		boolean casstteMulti = false;
		for (ArrayList<ExonInfo> lsExon : lsIsoExon) {
			if (lsExon.size() > 1) {
				casstteMulti = true;
				break;
			}
		}
		if (casstteMulti) {
			setSplicingTypes.add(ExonSplicingType.cassette_multi);
		} else {
			setSplicingTypes.add(ExonSplicingType.cassette);
		}
	}
	/**
	 * ���exon����Ӧ��iso��ǰ���exoncluster���Ƿ�Ҳ����
	 * ��Ҫ�������ж�casstte��
	 * @return
	 */
	private boolean isIsoExistBeforeAndAfter(Collection<GffGeneIsoInfo> lsIso_ExonExist) {
		boolean beforeAndAfterContainHaveIso = false;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsIso_ExonExist) {
			if (exonClusterBefore.getMapIso2LsExon().containsKey(gffGeneIsoInfo)
					&&
					exonClusterAfter.getMapIso2LsExon().containsKey(gffGeneIsoInfo)
					) {
				beforeAndAfterContainHaveIso = true;
			}
		}
		return beforeAndAfterContainHaveIso;
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
	/** �鿴ǰһ��exoncluster����Ϣ */
	private LinkedList<ExonSplicingType> searchBeforeExon(List<ExonInfo> lsExonInfo) {
		LinkedList<ExonSplicingType> setSplicingTypes = new LinkedList<ExonSplicingType>();

		for (ExonInfo exonInfo : lsExonInfo) {
			if (!exonClusterBefore.getMapIso2LsExon().containsKey(exonInfo.getParent())) {
				setSplicingTypes.add(ExonSplicingType.mutually_exon); 
			}
		}
		return setSplicingTypes;
	}
	/** �鿴ǰһ��exoncluster����Ϣ */
	private LinkedList<ExonSplicingType> searchAfterExon(List<ExonInfo> lsExonInfo) {
		LinkedList<ExonSplicingType> setSplicingTypes = new LinkedList<ExonSplicingType>();

		for (ExonInfo exonInfo : lsExonInfo) {
			if (!exonClusterAfter.getMapIso2LsExon().containsKey(exonInfo.getParent())) {
				setSplicingTypes.add(ExonSplicingType.mutually_exon);
			}
		}
		return setSplicingTypes;
	}
	/** ���жϱ�λ��Ŀɱ�������
	 * Ҳ���ǽ��ж�alt5��alt3
	 */
	private LinkedList<ExonSplicingType> getSingleSiteSpliteType(List<ExonInfo> lsExonInfo) {
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

