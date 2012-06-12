package com.novelbio.analysis.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.log4j.Logger;


import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 给周琴做分析的一套东西
 * 人类里面默认是
 * NovelBioConst.GENOME_GFF_TYPE_UCSC, 和
 * NovelBioConst.GENOME_PATH_UCSC_HG18_GFF_REFSEQ
 * 这两个
 * @author zong0jie
 *
 */
public class PenncnvCope {
	private static final Logger logger = Logger.getLogger(PenncnvCope.class);
	
	public static final String CN0 = "cn0";
	public static final String CN1 = "cn1";
	public static final String CN2 = "cn2";
	public static final String CN3 = "cn3";
	public static final String CN4 = "cn4";
	
	GffHashGene gffHashGene = null;
	String gffType = NovelBioConst.GENOME_GFF_TYPE_UCSC;
	String gffFile = NovelBioConst.GENOME_PATH_UCSC_HG18_GFF_REFSEQ;
	
	String state = "";
	
	HashMap<CopedID, HashMap<String, String>> hashGeneCnv = new HashMap<CopedID, HashMap<String,String>>();
	/**
	 * 用于copedID排序
	 */
	LinkedHashSet<CopedID> hashCopedID = new LinkedHashSet<CopedID>();
	/**
	 * 用于sample排序
	 */
	LinkedHashSet<String> hashSample = new LinkedHashSet<String>();
	
	public static void main(String[] args)
	{
		PenncnvCope penncnvCope = new PenncnvCope();
//		penncnvCope.setParam(GffType, GffFIle);
		String txtFile = "/media/winE/NBC/Project/SNP_ZQ110826/ZQsnpRaw.txt";
		String outFile = "/media/winE/NBC/Project/SNP_ZQ110826/ZQsnpOut.txt";
		penncnvCope.readTxt(txtFile);
		ArrayList<String[]> lsResult = penncnvCope.writeResult();
		TxtReadandWrite txtResult = new TxtReadandWrite(outFile, true);
		txtResult.ExcelWrite(lsResult, "\t", 1, 1);
	}
	
	
	/**
	 * 默认hg18的信息
	 * @param GffType
	 * @param GffFIle
	 */
	public void setParam(String GffType, String GffFIle)
	{
		this.gffFile = GffFIle;
		this.gffType = GffType;
	}
	
	public void readTxt(String txtFile)
	{
		gffHashGene = new GffHashGene(gffType, gffFile);
		TxtReadandWrite txtCNVRead = new TxtReadandWrite(txtFile, false);
		ArrayList<String> lsCNV = txtCNVRead.readfileLs();
		lsCNV.remove(0);
		for (String string : lsCNV) {
			String[] ss = string.split("\t");
			if (hashSample.contains(ss[0])) {
				continue;
			}
			hashSample.add(ss[0]);
		}
		for (String string : lsCNV) {
			String[] ss = string.split("\t");
			setLoc(ss[1], Integer.parseInt(ss[2]), Integer.parseInt(ss[3]), ss[0], getCNstate(ss[6]));
		}
	}
	
	/**
	 * 查找某个cnv的基因情况
	 * @param chrID
	 * @param cod1
	 * @param cod2
	 * @param sampleName
	 * @param cnvstate
	 */
	private void setLoc(String chrID, int cod1, int cod2, String sampleName, String cnvstate) {
		GffCodGeneDU gffCodGeneDU = gffHashGene.searchLocation(chrID, cod1, cod2);
		ArrayList<CopedID> lsCoveredGenes = gffCodGeneDU.getAllCoveredGenes();
		for (CopedID copedID : lsCoveredGenes) {
			if (!hashCopedID.contains(copedID)) {
				hashCopedID.add(copedID);
			}
			//添加copedID
			if (hashGeneCnv.containsKey(copedID)) {
				HashMap<String, String> hashTmpSample2Cnv = hashGeneCnv.get(copedID);
				hashTmpSample2Cnv.put(sampleName, cnvstate);
			}
			else {
				HashMap<String, String> hashTmpSample2Cnv = getLsCNgene(hashSample);
				hashTmpSample2Cnv.put(sampleName, cnvstate);
				hashGeneCnv.put(copedID, hashTmpSample2Cnv);
			}
		}
		System.out.println(chrID + "\t"+ cod1 + "\t"+ cod2);
	}
	
	public ArrayList<String[]> writeResult() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		String[] title = new String[hashSample.size() + 3];
		title[0] = "NCBIID";
		title[1] = "Symbol";
		title[2] = "Description";
		int k = 3;
		for (String string : hashSample) {
			title[k] = string; k++;
		}
		
		for (CopedID copedID : hashCopedID) {
			String[] tmpResult = new String[hashSample.size() + 3];
			tmpResult[0] = copedID.getAccID();
			tmpResult[1] = copedID.getSymbol();
			tmpResult[2] = copedID.getDescription();
			HashMap<String, String> hashSample2Cnv = hashGeneCnv.get(copedID);
			int m = 3;
			for (String string : hashSample) {
				tmpResult[m] = hashSample2Cnv.get(string); m++;
			}
			lsResult.add(tmpResult);
		}
		lsResult.add(0,title);
		return lsResult;
	}
	
	
	/**
	 * 给定cnstate的文字，返回具体的cn等级
	 * @param cnstate 类似"state5,cn=3"
	 * @return
	 * 出错就返回null
	 * 返回CN0-CN4中的一个
	 */
	private String getCNstate(String cnstate)
	{
		cnstate = cnstate.replace("\"", "");
		String cnNum = cnstate.split(",")[1].split("=")[1];
		int cn = -1;
		try {
			cn = Integer.parseInt(cnNum);
		} catch (Exception e) {
			logger.error("find unknown format cnv info: "+ cnstate);
			return null;
		}
		if (cn < 0 || cn >4) {
			logger.error("find unknown format cnv info: "+ cnstate);
			return null;
		}
		return "cn"+cn;
	}
	

	
	/**
	 * @param colSampleName 需要是给定顺序的，去冗余的sample名
	 * 返回一系列的lsGenes都初始化cnstates为cn2
	 * 同时初始化hashSample，这个用于sample排序的
	 */
	private HashMap<String, String> getLsCNgene(Collection<String> colSampleName)
	{
		HashMap<String, String> hashSample2Cnv = new HashMap<String, String>();
		for (String string : colSampleName) {
			hashSample2Cnv.put(string, PenncnvCope.CN2);
		}
		return hashSample2Cnv;
	}
}

//class CnGene
//{
//	String sampleName = "";
//	String cnstate = PenncnvCope.CN2;
//	public CnGene(String sampleName, String cnstate) {
//		this.sampleName = sampleName;
//		this.cnstate = cnstate;
//	}
//	public String getCnstate() {
//		return cnstate;
//	}
//	public String getSampleName() {
//		return sampleName;
//	}
//
//	
//	
//	
//}
