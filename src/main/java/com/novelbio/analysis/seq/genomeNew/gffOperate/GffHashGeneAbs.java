package com.novelbio.analysis.seq.genomeNew.gffOperate;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.test.testextend.a;

public abstract class GffHashGeneAbs extends GffHash<GffDetailGene,GffCodGene> implements GffHashGeneInf
{
	int taxID = 0;
	public GffHashGeneAbs(int taxID) {
		this.taxID = taxID;
	}
	public GffHashGeneAbs() {
	}
	
//	/**
//	 * ��ϣ��geneID--LOCϸ��<br>
//	 * ���ڿ��ٽ�geneID��Ŷ�Ӧ��LOC��ϸ��<br>
//	 * hash��LOCID��--GeneInforlist������LOCID����������Ŀ��� <br>
//	 */
//	public HashMap<String,GffDetailGene> getLocHashtable() {
//		return hashGeneIDGffDetail;
//	}
	
	/**
	 * ���������/geneID�����ػ����������Ϣ��
	 * @param accID
	 * @return
	 */
	public GffDetailGene getGeneDetail(String accID) {
		GffDetailGene gffDetailGene = locHashtable.get(accID);
		if (gffDetailGene == null) {
			gffDetailGene = getLocHashtable().get(accID);
		}
		if (gffDetailGene == null) {
			CopedID copedID = new CopedID(accID, taxID, false);
			gffDetailGene = getLocHashtable().get(copedID.getGenUniID());
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
		
		for(Entry<String, ArrayList<GffDetailGene>> entry:Chrhash.entrySet())
		{
			String key = entry.getKey();
			ArrayList<GffDetailGene> value = entry.getValue();
			int chrLOCNum=value.size();
			allupBpLength=allupBpLength+chrLOCNum*upBp;
		    //һ��һ��Ⱦɫ���ȥ����ں��Ӻ������ӵĳ���
		    for (int i = 0; i < chrLOCNum; i++) 
			{
				GffDetailGene tmpUCSCgene=value.get(i);
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
	 * ���Gene2GeneID�����ݿ��е���Ϣ������д���ı���һ�㲻��
	 */
	public ArrayList<String[]> getGene2ID() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		
		ArrayList<String> lsAccID = getLOCIDList();
		for (String accID : lsAccID) {
			CopedID copedID = new CopedID(accID, taxID, false);
			String[] tmpAccID = new String[2];
			tmpAccID[0] = copedID.getAccID();
			tmpAccID[1] = copedID.getGenUniID();
			lsResult.add(tmpAccID);
		}
		return lsResult;
	}
	/**
	 * һ��Gff�ļ�ֻ���Դ˾ͺ�
	 * ����ȡ��Gff�ļ��е�AccIDת��ΪGeneID���ұ������ı��У��´�ֱ�Ӷ�ȡ���ı����ɻ��AccID��GeneID�Ķ��ձ����ٲ���
	 * @param txtAccID2GeneID
	 */
	public void writeAccID2GeneID(String txtaccID2GeneID) {
		TxtReadandWrite txtAccID2GeneID = new TxtReadandWrite(txtaccID2GeneID, true);
		txtAccID2GeneID.ExcelWrite(getGene2ID(), "\t", 1, 1);
	}
	/**
	 * ����
	 * @param txtaccID2GeneID
	 * @return
	 */
	public HashMap<String, String> getAcc2GeneID(String txtaccID2GeneID) {
		HashMap<String, String> hashAcc2GeneID = new HashMap<String, String>();
		TxtReadandWrite txtAcc2GenID = new TxtReadandWrite(txtaccID2GeneID, false);
		ArrayList<String> lsAccID = txtAcc2GenID.readfileLs();
		for (String string : lsAccID) {
			if (string == null || string.trim().equals("")) {
				continue;
			}
			String[] ss = string.split("\t");
			hashAcc2GeneID.put(ss[0], ss[1]);
		}
		return hashAcc2GeneID;
	}
	
	
	
	
	
	
	
	
}
