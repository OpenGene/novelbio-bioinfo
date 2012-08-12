package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.modcopeid.GeneType;

public class GffHashCufflinkGTF extends GffHashGeneAbs{
	GffHashGene gffHashRef;
	double likelyhood = 0.4;//���ƶ���0.4���ڵ�ת¼������Ϊͬһ������
	/**
	 * �趨�ο������Gff�ļ�
	 * @param gffHashRef
	 */
	public void setGffHashRef(GffHashGene gffHashRef) {
		this.gffHashRef = gffHashRef;
	}
	
	String transcript = "transcript";
	@Override
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
		HashMap<String, ArrayList<GffGeneIsoInfo>> mapChrID2LsIso = new HashMap<String, ArrayList<GffGeneIsoInfo>>();
		
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename, false);
		// ��������
		ArrayList<GffGeneIsoInfo> lsGeneIsoInfos = null;
		
		GffGeneIsoInfo gffGeneIsoInfo = null;
		String tmpChrID = "";
		String tmpTranscriptName = "";
		for (String content : txtgff.readlines() ) {
			if (content.charAt(0) == '#')
				continue;

			String[] ss = content.split("\t");// ����tab�ֿ�
			
			// �µ�Ⱦɫ��
			if (!tmpChrID.equals(ss[0].toLowerCase()) ) {
				tmpChrID = ss[0].toLowerCase();
				lsGeneIsoInfos = getChrID2LsGffGeneIso(tmpChrID, mapChrID2LsIso);
			}
			if (ss[2].equals(transcript) 
				|| !ss[8].split(";")[1].replace("transcript_id", "").replace("\"", "").trim().equals(tmpTranscriptName)) 
			{
				tmpTranscriptName = ss[8].split(";")[1].replace("transcript_id", "").replace("\"", "").trim();
				
				boolean cis = getLocCis(ss[6], tmpChrID, Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
				gffGeneIsoInfo = GffGeneIsoInfo.createGffGeneIso(tmpTranscriptName, GeneType.mRNA, cis);
				lsGeneIsoInfos.add(gffGeneIsoInfo);
				if (ss[2].equals(transcript)) {
					continue;
				}
			}
			if (ss[2].equals("exon")) {
				gffGeneIsoInfo.addExon( Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
			}
		}
		CopeChrIso(mapChrID2LsIso);
		txtgff.close();
	}
	
	private ArrayList<GffGeneIsoInfo> getChrID2LsGffGeneIso(String chrID, HashMap<String, ArrayList<GffGeneIsoInfo>> mapChrID2LsIso) {
		 ArrayList<GffGeneIsoInfo> lsGeneIsoInfos = null;
		if (!mapChrID2LsIso.containsKey(chrID)) {
			lsGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
			mapChrID2LsIso.put(chrID,lsGeneIsoInfos);
		}
		else {
			lsGeneIsoInfos = mapChrID2LsIso.get(chrID);
		}
		return lsGeneIsoInfos;
	}
	
	private void CopeChrIso(HashMap<String, ArrayList<GffGeneIsoInfo>> hashChrIso) {
		ArrayList<String> lsChrID = ArrayOperate.getArrayListKey(hashChrIso);
		for (String chrID : lsChrID) {
			ArrayList<GffGeneIsoInfo> arrayList = hashChrIso.get(chrID);
			copeChrInfo(chrID, arrayList);
		}
	}
	
	
	/**
	 * ����ĳ��Ⱦɫ�����Ϣ
	 */
	private void copeChrInfo(String chrID, List<GffGeneIsoInfo> lsGeneIsoInfos) {
		ListGff lsResult = new ListGff();
		lsResult.setName(chrID);
		
		if (lsGeneIsoInfos == null)// ����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�ض̲�װ��LOCChrHashIDList
			return;
		//����
		Collections.sort(lsGeneIsoInfos, new Comparator<GffGeneIsoInfo>() {
			public int compare(GffGeneIsoInfo o1, GffGeneIsoInfo o2) {
				Integer o1Start = o1.getStartAbs();
				Integer o2Start = 0;
				o2Start = o2.getStartAbs();

				return o1Start.compareTo(o2Start);
			}
		});
		//����װ��gffdetailGene��
		GffDetailGene gffDetailGene = null;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfos) {
			gffGeneIsoInfo.sort();
			if (gffDetailGene == null) {
				gffDetailGene = new GffDetailGene(lsResult, gffGeneIsoInfo.getName(), gffGeneIsoInfo.isCis5to3());
				gffDetailGene.addIso(gffGeneIsoInfo);
				lsResult.add(gffDetailGene);
				continue;
			}
			
			double[] gffIsoRange = new double[]{gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs()};
			double[] gffGeneRange = new double[]{gffDetailGene.getStartAbs(), gffDetailGene.getEndAbs()};
			double[] compResult = ArrayOperate.cmpArray(gffIsoRange, gffGeneRange);
			if (compResult[2] > GffDetailGene.OVERLAP_RATIO || compResult[3] > GffDetailGene.OVERLAP_RATIO
					||
					gffDetailGene.getSimilarIso(gffGeneIsoInfo, likelyhood) != null
					) {
				gffDetailGene.addIso(gffGeneIsoInfo);
			}
			else {
				gffDetailGene = new GffDetailGene(lsResult, gffGeneIsoInfo.getName(), gffGeneIsoInfo.isCis5to3());
				gffDetailGene.addIso(gffGeneIsoInfo);
				lsResult.add(gffDetailGene);
				continue;
			}
		}
		mapChrID2ListGff.put(chrID, lsResult);
	}
	/**
	 * ����chrID�����꣬���ظõ�Ӧ�����������Ǹ���
	 * ����������������û�и�����ص�refGff����ֱ�ӷ���true
	 * @param chrID
	 * @param LocID
	 * @return
	 */
	private boolean getLocCis(String ss, String chrID, int LocIDStart, int LocIDEnd) {
		if (ss.equals("+")) {
			return true;
		}
		else if (ss.equals("-")) {
			return false;
		}
		else {
			if (gffHashRef == null) {
				return true;
			}
			int LocID = (LocIDStart + LocIDEnd )/2;
			GffCodGene gffCodGene = gffHashRef.searchLocation(chrID, LocID);
			if (gffCodGene == null) {
				return true;
			}
			if (gffCodGene.isInsideLoc()) {
				return gffCodGene.getGffDetailThis().isCis5to3();
			}
			else {
				int a = Integer.MAX_VALUE;
				int b = Integer.MAX_VALUE;
				if (gffCodGene.getGffDetailUp() != null) {
					a = gffCodGene.getGffDetailUp().getCod2End(LocID);
				}
				if (gffCodGene.getGffDetailDown() != null) {
					b = gffCodGene.getGffDetailDown().getCod2Start(LocID);
				}
				if (a < b) {
					return gffCodGene.getGffDetailUp().isCis5to3();
				}
				else {
					return gffCodGene.getGffDetailDown().isCis5to3();
				}
			}
		}
	}
	
	
}
