package com.novelbio.analysis.seq.genomeNew.gffOperate;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.chipseq.repeatMask.repeatRun;
import com.novelbio.analysis.seq.genomeNew.listOperate.ListAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.CompSubArrayCluster;
import com.novelbio.base.dataStructure.CompSubArrayInfo;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.test.testextend.a;

public abstract class GffHashGeneAbs extends GffHash<GffDetailGene,GffCodGene, GffCodGeneDU> implements GffHashGeneInf
{
	int taxID = 0;
	String acc2GeneIDfile = "";
	String gfffile = "";
	public GffHashGeneAbs() {
		Chrhash = new LinkedHashMap<String, ListAbs<GffDetailGene>>();
		hashGeneID2Acc = new HashMap<String, String>();
	}
	private static Logger logger = Logger.getLogger(GffHashGeneAbs.class);
	
	/**
	 * 在读取文件后如果有什么需要设置的，可以写在setOther();方法里面
	 * @param gfffilename
	 */
	public void ReadGffarray(String gfffilename) {
		this.acc2GeneIDfile = FileOperate.changeFileSuffix(gfffilename, "_accID2geneID", "list");
		super.ReadGffarray(gfffilename);
	}
	
	public int getTaxID() {
		return taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
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
	 * 输入基因名，返回基因的坐标信息等
	 * 可以输入accID
	 * @param accID
	 * @return
	 */
	public GffDetailGene searchLOC(String accID) {
		GffDetailGene gffDetailGene = super.searchLOC(accID);
		if (gffDetailGene == null) {
			CopedID copedID = new CopedID(accID, taxID, false);
			String locID = null;
			try {
				locID = getHashGeneID2Acc(acc2GeneIDfile).get(copedID.getGenUniID()).split("//")[0];
			} catch (Exception e) {
				logger.error("没有该accID："+accID);
				return null;
			}
		
			gffDetailGene = super.searchLOC(locID);
		}
		return gffDetailGene;
	}
	/**
	 * 输入CopedID，返回基因的坐标信息等
	 * @param copedID 
	 * @return
	 * 没有就返回null
	 */
	public GffDetailGene searchLOC(CopedID copedID) {
		String locID = getHashGeneID2Acc(acc2GeneIDfile).get(copedID.getGenUniID()).split("//")[0];
		return super.searchLOC(locID);
	}
	
	/**
	 * 输入基因名，返回基因的具体转录本，主要用在UCSC上
	 * 没找到具体的转录本名字，那么就返回最长转录本
	 * 可以输入accID
	 * @param accID
	 * @return
	 */
	public GffGeneIsoInfo searchISO(String accID) {
		GffDetailGene gffdetail = searchLOC(accID);
		if (gffdetail == null) {
			return null;
		}
		GffGeneIsoInfo gffGeneIsoInfoOut = gffdetail.getIsolist(accID);
		if (gffGeneIsoInfoOut == null) {
			gffGeneIsoInfoOut = gffdetail.getLongestSplit();
		}
		return gffGeneIsoInfoOut;
	}
	/**
	 * 保存所有gffDetailGene
	 */
	ArrayList<GffDetailGene> lsGffDetailGenesAll = new ArrayList<GffDetailGene>();
	/**
	 * 返回所有GffDetailGene
	 * @return
	 */
	public ArrayList<GffDetailGene> getGffDetailGenesAll()
	{
		if (lsGffDetailGenesAll.size() != 0) {
			return lsGffDetailGenesAll;
		}
		for (ListAbs<GffDetailGene> lsGffDetailGenes : Chrhash.values()) {
			lsGffDetailGenesAll.addAll(lsGffDetailGenes);
		}
		return lsGffDetailGenesAll;
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
		
		for(Entry<String, ListAbs<GffDetailGene>> entry:Chrhash.entrySet())
		{
			String key = entry.getKey();
			ListAbs<GffDetailGene> value = entry.getValue();
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
	 * 一个Gff文件只跑一次就好
	 * 将读取的Gff文件中的AccID转化为GeneID并且保存在文本中，下次直接读取该文本即可获得AccID与GeneID的对照表，快速查找
	 * @param txtAccID2GeneID
	 */
	private void writeAccID2GeneID(String txtaccID2GeneID) {
		TxtReadandWrite txtAccID2GeneID = new TxtReadandWrite(txtaccID2GeneID, true);
		txtAccID2GeneID.ExcelWrite(getGene2ID(), "\t", 1, 1);
	}
	private HashMap<String, String> hashGeneID2Acc = null;
	/**
	 * 输入
	 * @param txtaccID2GeneID
	 * @return
	 * hashGeneID2Acc，一个geneID对应多个accID的时候，accID用“//”隔开
	 */
	private HashMap<String, String> getHashGeneID2Acc(String txtaccID2GeneID) {
		if (hashGeneID2Acc != null && hashGeneID2Acc.size() > 0) {
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
	
	
	/**
	 * 将基因装入GffHash中
	 * @param chrID
	 * @param gffDetailGene
	 */
	public void addGffDetailGene(String chrID, GffDetailGene gffDetailGene) {
		
		if (!Chrhash.containsKey(chrID.toLowerCase())) {
			ListAbs<GffDetailGene> lsGffDetailGenes = new ListAbs<GffDetailGene>();
			Chrhash.put(chrID, lsGffDetailGenes);
		}
		ListAbs<GffDetailGene> lsGffDetailGenes = Chrhash.get(chrID.toLowerCase());
		lsGffDetailGenes.add(gffDetailGene);
	}
	
	/**
	 * 
	 * 将文件写入GTF中
	 * @param GTFfile
	 * @param title 给该GTF起个名字
	 */
	@Override
	public void writeToGTF(String GTFfile,String title)
	{
		TxtReadandWrite txtGtf = new TxtReadandWrite(GTFfile, true);
		ArrayList<String> lsChrID = ArrayOperate.getArrayListKey(Chrhash);
		//把得到的ChrID排个序
		TreeSet<String> treeSet = new TreeSet<String>();
		for (String string : lsChrID) {
			treeSet.add(string);
		}
		for (String string : treeSet) {
			ArrayList<GffDetailGene> lsGffDetailGenes = Chrhash.get(string);
			writeToGTF(txtGtf, lsGffDetailGenes, title);
		}
		txtGtf.close();
	}
	/**
	 * 将一个染色体中的信息写入文本，按照GTF格式
	 * @param txtWrite
	 * @param lsGffDetailGenes
	 */
	private void writeToGTF(TxtReadandWrite txtWrite, ArrayList<GffDetailGene> lsGffDetailGenes, String title)
	{
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			String geneGTF = gffDetailGene.getGTFformate(title);
			txtWrite.writefileln(geneGTF.trim());
		}
	}
	
	@Override
	public void writeToGFFIso(String GFFfile, String title) {

		TxtReadandWrite txtGtf = new TxtReadandWrite(GFFfile, true);
		ArrayList<String> lsChrID = ArrayOperate.getArrayListKey(Chrhash);
		//把得到的ChrID排个序
		TreeSet<String> treeSet = new TreeSet<String>();
		for (String string : lsChrID) {
			treeSet.add(string);
		}
		for (String string : treeSet) {
			ArrayList<GffDetailGene> lsGffDetailGenes = Chrhash.get(string);
			writeToGFFIso(txtGtf, lsGffDetailGenes, title);
		}
		txtGtf.close();
	}
	
	/**
	 * 将一个染色体中的信息写入文本，按照GTF格式
	 * @param txtWrite
	 * @param lsGffDetailGenes
	 */
	private void writeToGFFIso(TxtReadandWrite txtWrite, ArrayList<GffDetailGene> lsGffDetailGenes, String title)
	{
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			if (gffDetailGene.getLsCodSplit().size() <= 1) {
				continue;
			}
			String geneGFF = gffDetailGene.getGFFformate(title);
			txtWrite.writefileln(geneGFF.trim());
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
