package com.novelbio.analysis.seq.genomeNew.gffOperate;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.test.testextend.a;

public abstract class GffHashGene extends GffHash
{
	int taxID = 0;
	public GffHashGene(int taxID) {
		this.taxID = taxID;
	}
	public GffHashGene() {
	}
	/**
	 * ��ϣ��geneID--LOCϸ��<br>
	 * ���ڿ��ٽ�geneID��Ŷ�Ӧ��LOC��ϸ��<br>
	 * hash��LOCID��--GeneInforlist������LOCID����������Ŀ��� <br>
	 */
	private HashMap<String,GffDetailGene> hashGeneIDGffDetail;
	
	/**
	 * ��ϣ��geneID--LOCϸ��<br>
	 * ���ڿ��ٽ�geneID��Ŷ�Ӧ��LOC��ϸ��<br>
	 * hash��LOCID��--GeneInforlist������LOCID����������Ŀ��� <br>
	 */
	public HashMap<String,GffDetailGene> getHashGeneIDGffDetail() {
		if (hashGeneIDGffDetail == null) {
			hashGeneIDGffDetail = new HashMap<String, GffDetailGene>();
			for (Entry<String, GffDetailAbs> entry : locHashtable.entrySet()) {
				String accID = entry.getKey();
				CopedID copedID = new CopedID(accID, taxID, false);
				hashGeneIDGffDetail.put(copedID.getGenUniID(), (GffDetailGene) entry.getValue());
			}
		}
		return hashGeneIDGffDetail;
	}
	
	/**
	 * ���������/geneID�����ػ����������Ϣ��
	 * @param accID
	 * @return
	 */
	public GffDetailGene getGeneDetail(String accID) {
		GffDetailGene gffDetailGene = (GffDetailGene) locHashtable.get(accID);
		if (gffDetailGene == null) {
			gffDetailGene = getHashGeneIDGffDetail().get(accID);
		}
		if (gffDetailGene == null) {
			CopedID copedID = new CopedID(accID, taxID, false);
			gffDetailGene = getHashGeneIDGffDetail().get(copedID.getGenUniID());
		}
		return gffDetailGene;
	}
	/**
	 * 	�����������ܳ��ȣ��ں����ܳ��ȵ���Ϣ��ֻͳ���ת¼������Ϣ
	 * ������
	 * Ϊһ��ArrayList-Integer
	 * 0: all5UTRLength <br>
	 * 1: all3UTRLength <br>
	 * 2: allExonLength ������5UTR��3UTR�ĳ��� <br> 
	 * 3: allIntronLength <br>
	 * 4: allup2kLength <br>
	 * 5: allGeneLength <br>
	 * @return 
	 */
	public ArrayList<Long> getGeneStructureLength(int upBp)
	{
		ArrayList<Long> lsbackground=new ArrayList<Long>();
		long allGeneLength=0;
		long allIntronLength=0;
		long allExonLength=0;
		long all5UTRLength=0;
		long all3UTRLength=0;
		long allupBpLength=0;

		int errorNum=0;//��UCSC���ж��ٻ����TSS�����ת¼�������
		/////////////////////��   ʽ   ��   ��//////////////////////////////////////////
		
		for(Entry<String, ArrayList<GffDetailAbs>> entry:Chrhash.entrySet())
		{
			String key = entry.getKey();
			ArrayList<GffDetailAbs> value = entry.getValue();
			int chrLOCNum=value.size();
			allupBpLength=allupBpLength+chrLOCNum*upBp;
		    //һ��һ��Ⱦɫ���ȥ����ں��Ӻ������ӵĳ���
		    for (int i = 0; i < chrLOCNum; i++) 
			{
				GffDetailGene tmpUCSCgene=(GffDetailGene)value.get(i);
				GffGeneIsoInfo gffGeneIsoInfoLong = tmpUCSCgene.getLongestSplit();
				allGeneLength=allGeneLength + Math.abs(gffGeneIsoInfoLong.getTSSsite() - gffGeneIsoInfoLong.getTESsite() + 1);
				///////////////////////��UCSC���ж��ٻ����TSS�����ת¼�������//////////////////////////
				if ((tmpUCSCgene.cis5to3&&gffGeneIsoInfoLong.getTSSsite()>tmpUCSCgene.numberstart) 
						|| ( !tmpUCSCgene.cis5to3&& gffGeneIsoInfoLong.getTSSsite()<tmpUCSCgene.numberend ))
				{
					errorNum++;
				}
				all5UTRLength = all5UTRLength + gffGeneIsoInfoLong.getLenUTR5();
				all3UTRLength = all3UTRLength + gffGeneIsoInfoLong.getLenUTR3();
				allExonLength = allExonLength + gffGeneIsoInfoLong.getLenExon(0); 
				allIntronLength = allIntronLength + gffGeneIsoInfoLong.getLenIntron(0); 
			}
		}
		lsbackground.add(all5UTRLength);
		lsbackground.add(all3UTRLength);
		lsbackground.add(allExonLength);
		lsbackground.add(allIntronLength);
		lsbackground.add(allupBpLength);
		lsbackground.add(allGeneLength);
		System.out.println("getGeneStructureLength: ��UCSC���ж��ٻ����TSS�����ת¼�������"+errorNum);
		return lsbackground;
	}
	/**
	 * ��Ҫ����
	 * ����ĳ���ض�LOC����Ϣ
	 * {return locHashtable.get(LOCID);}
	 * @param LOCID ����ĳLOC�����ƣ�ע��������һ���̵����֣�Ʃ����UCSC�����У�����locstring���ֺü�����������һ������֣����ǵ����Ķ̵�����
	 * @return ���ظ�LOCID�ľ���GffDetail��Ϣ������Ӧ��GffDetail�����
	 */
	public abstract GffDetailGene searchLOC(String LOCID);
	
	/**
	 * ��Ҫ����
	 * {return Chrhash.get(chrID).get(LOCNum);}
	 * ����chrID�͸�Ⱦɫ���ϵ�λ�ã�����GffDetail��Ϣ
	 * @param chrID Сд
	 * @param LOCNum ��Ⱦɫ���ϴ���ѰLOC��int���
	 * @return  ���ظ�LOCID�ľ���GffDetail��Ϣ������Ӧ��GffDetail�����
	 */
	public abstract GffDetailGene searchLOC(String chrID,int LOCNum);
}
