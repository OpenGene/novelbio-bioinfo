package com.novelbio.analysis.seq.genomeNew2.gffOperate;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.test.testextend.a;

public abstract class GffHashGeneAbs extends GffHash<GffDetailGene,GffCodGene, GffCodGeneDU> implements GffHashGeneInf
{
	int taxID = 0;
	String acc2GeneIDfile = "";
	
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
	 * ��������������ػ����������Ϣ��
	 * ��������accID
	 * @param accID
	 * @return
	 */
	public GffDetailGene searchLOC(String accID) {
		GffDetailGene gffDetailGene = super.searchLOC(accID);
		if (gffDetailGene == null) {
			CopedID copedID = new CopedID(accID, taxID, false);
			String locID = getHashGeneID2Acc(acc2GeneIDfile).get(copedID.getGenUniID()).split("//")[0];
			gffDetailGene = super.searchLOC(locID);
		}
		return gffDetailGene;
	}
	/**
	 * ����CopedID�����ػ����������Ϣ��
	 * @param copedID 
	 * @return
	 * û�оͷ���null
	 */
	public GffDetailGene searchLOC(CopedID copedID) {
		String locID = getHashGeneID2Acc(acc2GeneIDfile).get(copedID.getGenUniID()).split("//")[0];
		return super.searchLOC(locID);
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
	private ArrayList<String[]> getGene2ID() {
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
	 * һ��Gff�ļ�ֻ��һ�ξͺ�
	 * ����ȡ��Gff�ļ��е�AccIDת��ΪGeneID���ұ������ı��У��´�ֱ�Ӷ�ȡ���ı����ɻ��AccID��GeneID�Ķ��ձ����ٲ���
	 * @param txtAccID2GeneID
	 */
	private void writeAccID2GeneID(String txtaccID2GeneID) {
		TxtReadandWrite txtAccID2GeneID = new TxtReadandWrite(txtaccID2GeneID, true);
		txtAccID2GeneID.ExcelWrite(getGene2ID(), "\t", 1, 1);
	}
	private HashMap<String, String> hashGeneID2Acc = null;
	/**
	 * ����
	 * @param txtaccID2GeneID
	 * @return
	 * hashGeneID2Acc��һ��geneID��Ӧ���accID��ʱ��accID�á�//������
	 */
	private HashMap<String, String> getHashGeneID2Acc(String txtaccID2GeneID) {
		if (hashGeneID2Acc != null) {
			return hashGeneID2Acc;
		}
		if (!FileOperate.isFileExist(txtaccID2GeneID)) {
			writeAccID2GeneID(txtaccID2GeneID);
		}
		hashGeneID2Acc = new HashMap<String, String>();
		TxtReadandWrite txtAcc2GenID = new TxtReadandWrite(txtaccID2GeneID, false);
		ArrayList<String> lsAccID = txtAcc2GenID.readfileLs();
		for (String string : lsAccID) {
			if (string == null || string.trim().equals("")) {
				continue;
			}
			String[] ss = string.split("\t");
			if (hashGeneID2Acc.containsKey(ss[1])) {
				hashGeneID2Acc.put(ss[1], hashGeneID2Acc.get(ss[1])+"//"+ss[0]);
			}
			else {
				hashGeneID2Acc.put(ss[1], ss[0]);
			}
		}
		return hashGeneID2Acc;
	}
	
	@Override
	protected GffCodGene setGffCod(String chrID, int Coordinate) {
		return new GffCodGene(chrID, Coordinate);
	}
	
	@Override
	protected GffCodGeneDU setGffCodDu(ArrayList<GffDetailGene> lsgffDetail,
			GffCodGene gffCod1, GffCodGene gffCod2) {
		return new GffCodGeneDU(lsgffDetail, gffCod1, gffCod2);
	}
}
