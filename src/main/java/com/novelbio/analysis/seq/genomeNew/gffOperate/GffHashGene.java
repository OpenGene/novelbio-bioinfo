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
	 * 哈希表geneID--LOC细节<br>
	 * 用于快速将geneID编号对应到LOC的细节<br>
	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的条目编号 <br>
	 */
	private HashMap<String,GffDetailGene> hashGeneIDGffDetail;
	
	/**
	 * 哈希表geneID--LOC细节<br>
	 * 用于快速将geneID编号对应到LOC的细节<br>
	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的条目编号 <br>
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
	 * 输入基因名/geneID，返回基因的坐标信息等
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
		
		for(Entry<String, ArrayList<GffDetailAbs>> entry:Chrhash.entrySet())
		{
			String key = entry.getKey();
			ArrayList<GffDetailAbs> value = entry.getValue();
			int chrLOCNum=value.size();
			allupBpLength=allupBpLength+chrLOCNum*upBp;
		    //一条一条染色体的去检查内含子和外显子的长度
		    for (int i = 0; i < chrLOCNum; i++) 
			{
				GffDetailGene tmpUCSCgene=(GffDetailGene)value.get(i);
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
	 * 需要覆盖
	 * 查找某个特定LOC的信息
	 * {return locHashtable.get(LOCID);}
	 * @param LOCID 给定某LOC的名称，注意名称是一个短的名字，譬如在UCSC基因中，不是locstring那种好几个基因连在一起的名字，而是单个的短的名字
	 * @return 返回该LOCID的具体GffDetail信息，用相应的GffDetail类接收
	 */
	public abstract GffDetailGene searchLOC(String LOCID);
	
	/**
	 * 需要覆盖
	 * {return Chrhash.get(chrID).get(LOCNum);}
	 * 给定chrID和该染色体上的位置，返回GffDetail信息
	 * @param chrID 小写
	 * @param LOCNum 该染色体上待查寻LOC的int序号
	 * @return  返回该LOCID的具体GffDetail信息，用相应的GffDetail类接收
	 */
	public abstract GffDetailGene searchLOC(String chrID,int LOCNum);
}
