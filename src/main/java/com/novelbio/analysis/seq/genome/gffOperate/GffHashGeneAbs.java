package com.novelbio.analysis.seq.genome.gffOperate;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListHashSearch;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;

public abstract class GffHashGeneAbs extends ListHashSearch<GffDetailGene, GffCodGene, GffCodGeneDU, ListGff> implements GffHashGeneInf {
	private static Logger logger = Logger.getLogger(GffHashGeneAbs.class);
	int taxID = 0;
	String acc2GeneIDfile = "";
	String gfffile = "";
	private HashMap<String, String> mapGeneID2AccID = null;
	
	public GffHashGeneAbs() {
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
		mapGeneID2AccID = new HashMap<String, String>();
	}
	/**
	 * 在读取文件后如果有什么需要设置的，可以写在setOther();方法里面
	 * @param gfffilename
	 */
	public void ReadGffarray(String gfffilename) {
		this.acc2GeneIDfile = FileOperate.changeFileSuffix(gfffilename, "_accID2geneID", "list");
		super.ReadGffarray(gfffilename);
		for (ListGff listGff : mapChrID2ListGff.values()) {
			listGff.sort();
			for (int i = 0; i < listGff.size(); i++) {
				GffDetailGene gffDetailGene = listGff.get(i);
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					gffGeneIsoInfo.sort();
					try {
						gffGeneIsoInfo.setATGUAGncRNA();
					} catch (Exception e) {
						gffGeneIsoInfo.setATGUAGncRNA();
					}
				}
				try {
					gffDetailGene.removeDupliIso();
				} catch (Exception e) {
					gffDetailGene.removeDupliIso();
				}
			}
		}
	}
	
	public int getTaxID() {
		return taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	/**
	 * 输入基因名，返回基因的坐标信息等
	 * 可以输入accID
	 * @param accID
	 * @return
	 */
	public GffDetailGene searchLOC(String accID) {
		GffDetailGene gffDetailGene = super.searchLOC(accID);
		if (gffDetailGene == null) {
			GeneID copedID = new GeneID(accID, taxID, false);
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
	public GffDetailGene searchLOC(GeneID copedID) {
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
    		logger.info("cannotFind the ID: "+ accID);
			return null;
		}
		GffGeneIsoInfo gffGeneIsoInfoOut = gffdetail.getIsolist(accID);
		if (gffGeneIsoInfoOut == null) {
			gffGeneIsoInfoOut = gffdetail.getLongestSplit();
		}
		return gffGeneIsoInfoOut;
	}
	/**
	 * 返回全体内含子，长度从小到大排序
	 * @return
	 */
	public ArrayList<Integer> getLsIntronSortedS2M() {
		ArrayList<Integer> lsIntronLen = new ArrayList<Integer>();
		for(Entry<String, ListGff> entry:mapChrID2ListGff.entrySet()) {
			String key = entry.getKey();
			ListGff value = entry.getValue();
			int chrLOCNum=value.size();
		    //一条一条染色体的去检查内含子和外显子的长度
		    for (int i = 0; i < chrLOCNum; i++) {
				GffDetailGene tmpUCSCgene=value.get(i);
				GffGeneIsoInfo gffGeneIsoInfoLong = tmpUCSCgene.getLongestSplit();
				gffGeneIsoInfoLong.getLsIntron();
				for (ExonInfo intronInfo : gffGeneIsoInfoLong) {
					lsIntronLen.add(intronInfo.Length());
				}
			}
		}
		Collections.sort(lsIntronLen);
		return lsIntronLen;
	}
	/**
	 * 获得Gene2GeneID在数据库中的信息，并且写入文本，一般不用
	 */
	private ArrayList<String[]> getGene2ID() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		
		ArrayList<String> lsAccID = getLsNameNoRedundent();
		for (String accID : lsAccID) {
			GeneID copedID = new GeneID(accID, taxID, false);
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
		txtAccID2GeneID.ExcelWrite(getGene2ID());
	}

	/**
	 * 输入
	 * @param txtaccID2GeneID
	 * @return
	 * hashGeneID2Acc，一个geneID对应多个accID的时候，accID用“//”隔开
	 */
	private HashMap<String, String> getHashGeneID2Acc(String txtaccID2GeneID) {
		if (mapGeneID2AccID != null && mapGeneID2AccID.size() > 0) {
			return mapGeneID2AccID;
		}
		if (!FileOperate.isFileExist(txtaccID2GeneID)) {
			writeAccID2GeneID(txtaccID2GeneID);
		}
		mapGeneID2AccID = new HashMap<String, String>();
		TxtReadandWrite txtAcc2GenID = new TxtReadandWrite(txtaccID2GeneID, false);
		ArrayList<String> lsAccID = txtAcc2GenID.readfileLs();
		for (String string : lsAccID) {
			if (string == null || string.trim().equals("")) {
				continue;
			}
			String[] ss = string.split("\t");
			if (mapGeneID2AccID.containsKey(ss[1])) {
				mapGeneID2AccID.put(ss[1], mapGeneID2AccID.get(ss[1])+"//"+ss[0]);
			}
			else {
				mapGeneID2AccID.put(ss[1], ss[0]);
			}
		}
		return mapGeneID2AccID;
	}
	/**
	 * 将基因装入GffHash中
	 * @param chrID
	 * @param gffDetailGene
	 */
	public void addGffDetailGene(String chrID, GffDetailGene gffDetailGene) {
		chrID = chrID.toLowerCase();
		if (!mapChrID2ListGff.containsKey(chrID)) {
			ListGff lsGffDetailGenes = new ListGff();
			mapChrID2ListGff.put(chrID, lsGffDetailGenes);
		}
		ListGff lsGffDetailGenes = mapChrID2ListGff.get(chrID);
		lsGffDetailGenes.add(gffDetailGene);
	}
	/**
	 * 
	 * 将文件写入GTF中
	 * @param GTFfile
	 * @param title 给该GTF起个名字
	 */
	@Override
	public void writeToGTF(String GTFfile,String title) {
		TxtReadandWrite txtGtf = new TxtReadandWrite(GTFfile, true);
		ArrayList<String> lsChrID = ArrayOperate.getArrayListKey(mapChrID2ListGff);
		//把得到的ChrID排个序
		TreeSet<String> treeSet = new TreeSet<String>();
		for (String string : lsChrID) {
			treeSet.add(string);
		}
		for (String string : treeSet) {
			ArrayList<GffDetailGene> lsGffDetailGenes = mapChrID2ListGff.get(string);
			writeToGTF(txtGtf, lsGffDetailGenes, title);
		}
		txtGtf.close();
	}
	/**
	 * 将一个染色体中的信息写入文本，按照GTF格式
	 * @param txtWrite
	 * @param lsGffDetailGenes
	 */
	private void writeToGTF(TxtReadandWrite txtWrite, ArrayList<GffDetailGene> lsGffDetailGenes, String title) {
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			String geneGTF = gffDetailGene.getGTFformate(title);
			txtWrite.writefileln(geneGTF.trim());
		}
	}
	/**
	 * 将一个染色体中的 含有不止一个转录本的 基因信息写入文本，按照GTF格式
	 * 也就是说，仅含有一个转录本的基因就不写入文本了
	 * @param txtWrite
	 * @param lsGffDetailGenes
	 * @param title
	 */
	@Override
	public void writeToGFFIsoMoreThanOne(String GFFfile, String title) {
		TxtReadandWrite txtGtf = new TxtReadandWrite(GFFfile, true);
		ArrayList<String> lsChrID = ArrayOperate.getArrayListKey(mapChrID2ListGff);
		//把得到的ChrID排个序
		TreeSet<String> treeSet = new TreeSet<String>();
		for (String string : lsChrID) {
			treeSet.add(string);
		}
		for (String string : treeSet) {
			ArrayList<GffDetailGene> lsGffDetailGenes = mapChrID2ListGff.get(string);
			writeToGFFIsoMoreThanOne(txtGtf, lsGffDetailGenes, title);
		}
		txtGtf.close();
	}
	/**
	 * 将一个染色体中的 含有不止一个转录本的 基因信息写入文本，按照GTF格式
	 * 也就是说，仅含有一个转录本的基因就不写入文本了
	 * @param txtWrite
	 * @param lsGffDetailGenes
	 * @param title
	 */
	private void writeToGFFIsoMoreThanOne(TxtReadandWrite txtWrite, ArrayList<GffDetailGene> lsGffDetailGenes, String title) {
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			if (gffDetailGene.getLsCodSplit().size() <= 1) {
				continue;
			}
			String geneGFF = gffDetailGene.getGFFformate(title);
			txtWrite.writefileln(geneGFF.trim());
		}
	}
	@Override
	public void writeGene2Iso(String Gene2IsoFile) {
		TxtReadandWrite txtGtf = new TxtReadandWrite(Gene2IsoFile, true);
		HashSet<String> setRemoveRedundentID = new HashSet<String>();
		ArrayList<GffDetailGene> lsGffDetailGenes = getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				GeneID copedID = gffGeneIsoInfo.getGeneID();
				if (copedID.getIDtype() != GeneID.IDTYPE_ACCID || copedID.getSymbol() == null || copedID.getSymbol().equals("")) {
					String symbol = copedID.getSymbol();
					if (symbol.equals("")) {
						symbol = copedID.getAccID();
					}
					if (isNotRedundent(setRemoveRedundentID, symbol, gffGeneIsoInfo.getName())) {
						txtGtf.writefileln(symbol + "\t" + gffGeneIsoInfo.getName());
					}
				}
				else {
					if (gffGeneIsoInfo.getName().contains(SepSign.SEP_ID)) {
						logger.error(gffGeneIsoInfo.getName());
					}
					if (isNotRedundent(setRemoveRedundentID, gffDetailGene.getNameSingle(), gffGeneIsoInfo.getName())) {
						txtGtf.writefileln(gffDetailGene.getNameSingle() + "\t" + gffGeneIsoInfo.getName());
					}
				}
			}
		}
		txtGtf.close();
	}
	
	private boolean isNotRedundent(HashSet<String> setRemoveRedundentID, String symbol, String geneID) {
		if (setRemoveRedundentID.contains(symbol + SepSign.SEP_ID + geneID)) {
			return false;
		}
		else {
			setRemoveRedundentID.add(symbol + SepSign.SEP_ID + geneID);
			return true;
		}
	}
}
