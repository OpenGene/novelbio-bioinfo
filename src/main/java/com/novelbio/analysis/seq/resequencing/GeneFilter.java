package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * ��Ϊsnpһ�㲻�ᷢ������ͬ��λ�㣬�������ܷ�������ͬ�Ļ��������������һ��������
 * ����ɸѡ���󲿷�����������ͻ����Ǹ�����
 * @author zong0jie
 */
public class GeneFilter {
	private final static Logger logger = Logger.getLogger(GeneFilter.class);
	GffChrAbs gffChrAbs;
	
	/**
	 * ����������RefSiteSnpIndel
	 * key: siteת��Ϊstring
	 * value: snpλ��
	 */
	HashMap<String, RefSiteSnpIndel> mapSiteInfo2SnpIndel = new HashMap<String, RefSiteSnpIndel>();
	ArrayListMultimap<GeneID, RefSiteSnpIndel> mapGeneID2LsRefSiteSnpIndel = ArrayListMultimap.create();
	/**
	 * ���map��ֻ�е����mapΪnull��ʱ�򣬲Ż���������
	 * key������ͻ��������������������У�Ҳ���������Ӵ�С����
	 * value�����巢��ͻ���һϵ��RefSiteSnpIndel
	 */
	TreeMap<Integer, List<RefSiteSnpIndel>> mapNum2LsMapSnpIndelInfo;
	
	//������
	Set<String> setTreat = new HashSet<String>();
	
	/** ʵ����ͨ�����˵�������Ŀ
	 * ����˵ֻҪĳ��������>=������������ͨ���ʼ죬����Ϊ��gene�ϸ�
	 *  */
	int treatFilteredMinNum = 0;
	
	/** ���˵���snpSiteλ��Ĺ����� */
	SnpFilter snpFilterSingleSite = new SnpFilter();
	
	int snpLevel = SnpGroupFilterInfo.Heto;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
		mapNum2LsMapSnpIndelInfo = null;
	}
	
	/**
	 * ���û����������level
	 * @param snpLevel  SnpGroupFilterInfo.HetoLess ��
	 */
	public void setSnpLevel(int snpLevel) {
		snpFilterSingleSite.setSampleFilterInfoSingle(snpLevel);
		mapNum2LsMapSnpIndelInfo = null;
	}
	/**
	 * ����Ѿ�����ɸѡ��refSiteSnpIndel������ֻ������ͨ�����˵�snp��������������
	 * @param refSiteSnpIndel �ظ���ӵĻ�������ĻḲ��ǰ���
	 */
	public void addLsRefSiteSnpIndel(Collection<RefSiteSnpIndel> colRefSiteSnpIndels) {
		for (RefSiteSnpIndel refSiteSnpIndel : colRefSiteSnpIndels) {
			addRefSiteSnpIndel(refSiteSnpIndel);
		}
	}
	/**
	 * ��Ҫ�ظ����
	 * ����Ѿ�����ɸѡ��refSiteSnpIndel������ֻ������ͨ�����˵�snp��������������
	 * @param refSiteSnpIndel �ظ���ӵĻ�������ĻḲ��ǰ���
	 */
	public void addRefSiteSnpIndel(RefSiteSnpIndel refSiteSnpIndel) {
		//TODO ������refsitesnpindel�н��������˺õ�site��map
		refSiteSnpIndel.setGffChrAbs(gffChrAbs);
		mapSiteInfo2SnpIndel.put(getRefSiteSnpIndelStr(refSiteSnpIndel), refSiteSnpIndel);
		mapNum2LsMapSnpIndelInfo = null;
	}
	
	/** ������Щ��������������Ҫ��snp���˵�������һ�� */
	public void addTreatName(String treatName) {
		setTreat.add(treatName);
		mapNum2LsMapSnpIndelInfo = null;
	}
	/** ������Щ��������������Ҫ��snp���˵�������һ�� */
	public void addTreatName(Collection<String> colTreatName) {
		setTreat.addAll(colTreatName);
		mapNum2LsMapSnpIndelInfo = null;
	}
	
	/** ��������treat���и�gene����Ϊͨ���� */
	public void setTreatFilteredNum(int treatFilteredNum) {
		this.treatFilteredMinNum = treatFilteredNum;
	}
	/** ����趨��treatmentName */
	public Set<String> getSetTreat() {
		return setTreat;
	}
	public ArrayList<RefSiteSnpIndel> filterSnpInGene() {
		ArrayList<RefSiteSnpIndel> lsResult = new ArrayList<RefSiteSnpIndel>();
		if (mapNum2LsMapSnpIndelInfo == null) {
			setMapGeneID2LsRefSiteSnpIndel();
			sortByTreatSampleNum();
		}
		for (Integer filteredTreatNum : mapNum2LsMapSnpIndelInfo.keySet()) {
			if (filteredTreatNum < treatFilteredMinNum) {
				continue;
			}
			lsResult.addAll(mapNum2LsMapSnpIndelInfo.get(filteredTreatNum));
		}
		return lsResult;
	}
	
	
	/** �������refInfoSnpIndel���ջ��������������� */
	private void setMapGeneID2LsRefSiteSnpIndel() {
		for (RefSiteSnpIndel refInfoSnpIndel : mapSiteInfo2SnpIndel.values()) {
			if (refInfoSnpIndel.getGffIso() == null) {
				continue;
			}
			GeneID geneID = refInfoSnpIndel.getGffIso().getGeneID();
			mapGeneID2LsRefSiteSnpIndel.put(geneID, refInfoSnpIndel);
		}
	}
	
	/** ����treat�������������򣬾���Խ���������иû���Ͱ�����������Ȼ�󱣴����treemap */
	private TreeMap<Integer, List<RefSiteSnpIndel>> sortByTreatSampleNum() {
		//�������е�treemap
		mapNum2LsMapSnpIndelInfo =
				new TreeMap<Integer, List<RefSiteSnpIndel>>(new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return -o1.compareTo(o2);
					}
		});
		
		for (GeneID geneID : mapGeneID2LsRefSiteSnpIndel.keySet()) {
			List<RefSiteSnpIndel> lsSnpIndels = mapGeneID2LsRefSiteSnpIndel.get(geneID);
			int filteredSampleNum = getTreatNum(lsSnpIndels);
			mapNum2LsMapSnpIndelInfo.put(filteredSampleNum, lsSnpIndels);
		}
		return mapNum2LsMapSnpIndelInfo;
	}
	
	/** ����ı����ǹ��˺�ֻʣ��causal snp��RefSiteSnpIndel */
	private int getTreatNum(List<RefSiteSnpIndel> lsSnpIndels) {
		HashSet<String> setTreatName = new HashSet<String>();
		for (RefSiteSnpIndel refSiteSnpIndel : lsSnpIndels) {

			//��һ ����ֻ������Щɸѡ������snp��û��ɸѡ�����ľͲ�Ҫ���ǡ�
			//�ڶ� ����ɸѡ������snp��������ȻҪ����ÿһ������������snp�Ƿ񳬹���ֵ
			//���ǰ��ɸѡͨ����snp����
			for (SiteSnpIndelInfo siteSnpIndelInfo : refSiteSnpIndel.mapAllen2Num.values()) {
				for (String treatName : setTreat) {
					siteSnpIndelInfo.setSampleName(treatName);
					if (snpFilterSingleSite.isFilterdSnp(siteSnpIndelInfo)) {
						setTreatName.add(treatName);
					}
				}
			}
		}
		return setTreatName.size();
	}
	
	/** ����MapInfoSnpIndel����������������Ӧ��string��������hashmap��key */
	private static String getRefSiteSnpIndelStr(RefSiteSnpIndel refSiteSnpIndel) {
		return refSiteSnpIndel.getRefID() + SepSign.SEP_ID + refSiteSnpIndel.getRefSnpIndelStart();
	}

}
