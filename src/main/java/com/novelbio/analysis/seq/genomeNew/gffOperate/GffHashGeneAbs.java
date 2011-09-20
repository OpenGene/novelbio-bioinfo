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
//	 * 哈希表geneID--LOC细节<br>
//	 * 用于快速将geneID编号对应到LOC的细节<br>
//	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的条目编号 <br>
//	 */
//	public HashMap<String,GffDetailGene> getLocHashtable() {
//		return hashGeneIDGffDetail;
//	}
	
	/**
	 * 输入基因名/geneID，返回基因的坐标信息等
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
	 * 	返回外显子总长度，内含子总长度等信息，只统计最长转录本的信息
	 * 有问题
	 * 为一个ArrayList-Integer
	 * 0: all5UTRLength <br>
	 * 1: all3UTRLength <br>
	 * 2: allExonLength 不包括5UTR和3UTR的长度 <br> 
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

		int errorNum=0;//看UCSC中有多少基因的TSS不是最长转录本的起点
		/////////////////////正   式   计   算//////////////////////////////////////////
		
		for(Entry<String, ArrayList<GffDetailGene>> entry:Chrhash.entrySet())
		{
			String key = entry.getKey();
			ArrayList<GffDetailGene> value = entry.getValue();
			int chrLOCNum=value.size();
			allupBpLength=allupBpLength+chrLOCNum*upBp;
		    //一条一条染色体的去检查内含子和外显子的长度
		    for (int i = 0; i < chrLOCNum; i++) 
			{
				GffDetailGene tmpUCSCgene=value.get(i);
				GffGeneIsoInfo gffGeneIsoInfoLong = tmpUCSCgene.getLongestSplit();
				allGeneLength=allGeneLength + Math.abs(gffGeneIsoInfoLong.getTSSsite() - gffGeneIsoInfoLong.getTESsite() + 1);
				///////////////////////看UCSC中有多少基因的TSS不是最长转录本的起点//////////////////////////
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
		System.out.println("getGeneStructureLength: 看UCSC中有多少基因的TSS不是最长转录本的起点"+errorNum);
		return lsbackground;
	}
	
	/**
	 * 获得Gene2GeneID在数据库中的信息，并且写入文本，一般不用
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
	 * 一个Gff文件只跑以此就好
	 * 将读取的Gff文件中的AccID转化为GeneID并且保存在文本中，下次直接读取该文本即可获得AccID与GeneID的对照表，快速查找
	 * @param txtAccID2GeneID
	 */
	public void writeAccID2GeneID(String txtaccID2GeneID) {
		TxtReadandWrite txtAccID2GeneID = new TxtReadandWrite(txtaccID2GeneID, true);
		txtAccID2GeneID.ExcelWrite(getGene2ID(), "\t", 1, 1);
	}
	/**
	 * 输入
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
