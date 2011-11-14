package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

public class GffHashCufflinkGTF extends GffHashGeneAbs{
	GffHashGene gffHashRef;
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
		// ʵ�����ĸ���
		Chrhash = new HashMap<String, ArrayList<GffDetailGene>>();// һ����ϣ�����洢ÿ��Ⱦɫ��
		locHashtable = new HashMap<String, GffDetailGene>();// �洢ÿ��LOCID���������Ϣ�Ķ��ձ�
		LOCIDList = new ArrayList<String>();// ˳��洢ÿ������ţ��������������ȡ��������
		LOCChrHashIDList = new ArrayList<String>();
		HashMap<String, ArrayList<GffGeneIsoInfo>> hashChrIso = new HashMap<String, ArrayList<GffGeneIsoInfo>>();
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename, false);
		BufferedReader reader = txtgff.readfile();// open gff file
		// ��������
		
		String content = "";
		String chrnametmpString = ""; // Ⱦɫ�����ʱ����
		ArrayList<GffGeneIsoInfo> lsGeneIsoInfos = null;
		GffGeneIsoInfo gffGeneIsoInfo = null;
		String tmpChrID = "";
		while ((content = reader.readLine()) != null)// ������β
		{
			
			if (content.charAt(0) == '#')
				continue;
			String[] ss = content.split("\t");// ����tab�ֿ�
			chrnametmpString = ss[0].toLowerCase();// Сд��chrID
			
			// �µ�Ⱦɫ��
			if (!tmpChrID.equals(chrnametmpString) )
			{
				tmpChrID = chrnametmpString;
				if (!hashChrIso.containsKey(chrnametmpString)) {
					lsGeneIsoInfos = new ArrayList<GffGeneIsoInfo>();
					hashChrIso.put(chrnametmpString,lsGeneIsoInfos);
				}
				else {
					lsGeneIsoInfos = hashChrIso.get(tmpChrID);
				}
			}
			if (ss[2].equals(transcript)) {
				String isoName = ss[8].split(";")[1].replace("transcript_id", "").replace("\"", "").trim();
				if (isoName.equals("transfrag.25525.1")) {
					System.out.println("stop");
				}
				boolean cis = getLocCis(ss[2], chrnametmpString, Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
				if (cis) 
					gffGeneIsoInfo = new GffGeneIsoCis(isoName, chrnametmpString, GffGeneIsoInfo.TYPE_GENE_MRNA);
				else 
					gffGeneIsoInfo = new GffGeneIsoTrans(isoName, chrnametmpString, GffGeneIsoInfo.TYPE_GENE_MRNA);
				lsGeneIsoInfos.add(gffGeneIsoInfo);
				continue;
			}
			gffGeneIsoInfo.addExonCufflinkGTF( Integer.parseInt(ss[3]), Integer.parseInt(ss[4]));
			
		}
		CopeChrIso(hashChrIso);
		txtgff.close();
	}
	
	private void CopeChrIso(HashMap<String, ArrayList<GffGeneIsoInfo>> hashChrIso)
	{
		ArrayList<ArrayList<GffGeneIsoInfo>> lsTmp = ArrayOperate.getArrayListValue(hashChrIso);
		for (ArrayList<GffGeneIsoInfo> arrayList : lsTmp) {
			copeChrInfo(arrayList);
			
		}
	}
	
	
	/**
	 * ����ĳ��Ⱦɫ�����Ϣ
	 */
	private void copeChrInfo(List<GffGeneIsoInfo> lsGeneIsoInfos)
	{
		ArrayList<GffDetailGene> lsResult = new ArrayList<GffDetailGene>();
		if (lsGeneIsoInfos == null)// ����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�ض̲�װ��LOCChrHashIDList
			return;
		//����
		Collections.sort(lsGeneIsoInfos, new Comparator<GffGeneIsoInfo>() {
			@Override
			public int compare(GffGeneIsoInfo o1, GffGeneIsoInfo o2) {
				Integer o1Start = -1;
				if (o1.isCis5to3()) {
					o1Start = o1.getIsoInfo().get(0)[0];
				}
				else {
					o1Start = o1.getIsoInfo().get(o1.getIsoInfo().size() - 1)[1];
				}
				Integer o2Start = -1;
				if (o2.isCis5to3()) {
					o2Start = o2.getIsoInfo().get(0)[0];
				}
				else {
					o2Start = o2.getIsoInfo().get(o2.getIsoInfo().size() - 1)[1];
				}
				return o1Start.compareTo(o2Start);
			}
		});
		//����װ��gffdetailGene��
		GffDetailGene gffDetailGene = null;
		for (GffGeneIsoInfo gffGeneIsoInfo : lsGeneIsoInfos) {
			if (gffGeneIsoInfo.getIsoName().equals("transfrag.6946.1")) {
				System.out.println("test");
			}
			if (gffDetailGene == null) {
				gffDetailGene = new GffDetailGene(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getIsoName(), gffGeneIsoInfo.isCis5to3());
				gffDetailGene.addIso(gffGeneIsoInfo);
				lsResult.add(gffDetailGene);
				continue;
			}
			
			double[] gffIsoRange = new double[]{gffGeneIsoInfo.getStartAbs(), gffGeneIsoInfo.getEndAbs()};
			double[] gffGeneRange = new double[]{gffDetailGene.getNumberstart(), gffDetailGene.getNumberend()};
			
			double[] compResult = ArrayOperate.cmpArray(gffIsoRange, gffGeneRange);
			if (compResult[2] > GffDetailGene.OVERLAP_RATIO || compResult[3] > GffDetailGene.OVERLAP_RATIO) {
				gffDetailGene.addIso(gffGeneIsoInfo);
			}
			else {
				gffDetailGene = new GffDetailGene(gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getIsoName(), gffGeneIsoInfo.isCis5to3());
				gffDetailGene.addIso(gffGeneIsoInfo);
				lsResult.add(gffDetailGene);
				continue;
			}
		}
		Chrhash.put(lsGeneIsoInfos.get(0).getChrID(), lsResult);
	}
	
	
	/**
	 * ����chrID�����꣬���ظõ�Ӧ�����������Ǹ���
	 * @param chrID
	 * @param LocID
	 * @return
	 */
	private boolean getLocCis(String ss, String chrID, int LocIDStart, int LocIDEnd)
	{
		if (ss.equals("+")) {
			return true;
		}
		else if (ss.equals("-")) {
			return false;
		}
		else {
			int LocID = (LocIDStart + LocIDEnd )/2;
			GffCodGene gffCodGene = gffHashRef.searchLocation(chrID, LocID);
			if (gffCodGene == null) {
				return true;
//				System.out.println("error");
			}
			if (gffCodGene.isInsideLoc()) {
				return gffCodGene.getGffDetailThis().isCis5to3();
			}
			else {
				int a = Integer.MAX_VALUE;
				int b = Integer.MAX_VALUE;
				if (gffCodGene.getGffDetailUp() != null) {
					a = gffCodGene.getGffDetailUp().getCod2End();
				}
				if (gffCodGene.getGffDetailDown() != null) {
					b = gffCodGene.getGffDetailDown().getCod2Start();
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
