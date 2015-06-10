package com.novelbio.analysis.seq.genome.gffOperate;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.listOperate.ListAbs;
import com.novelbio.listOperate.ListHashSearch;

public abstract class GffHashGeneAbs extends ListHashSearch<GffDetailGene, GffCodGene, GffCodGeneDU, ListGff> implements GffHashGeneInf {
	private static final Logger logger = Logger.getLogger(GffHashGeneAbs.class);


	int taxID = 0;
	String acc2GeneIDfile = "";
	String gfffile = "";
	String version;
	String dbinfo;
	private HashMap<String, String> mapGeneID2AccID = null;
	private HashMap<String, GffGeneIsoInfo> mapName2Iso = new HashMap<String, GffGeneIsoInfo>();
	public GffHashGeneAbs() {
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
		mapGeneID2AccID = new HashMap<String, String>();
	}
	/**
	 * 在读取文件后如果有什么需要设置的，可以写在setOther();方法里面
	 * 	 * 策略：1.读取gff文件
	 * 2. 将gff文件中重叠的基因合并为1个（ReadGffarrayExcep方法实现），
	 * 由于有些名字不同的基因实际上一模一样，为了防止后续搜索不到，在本步骤中不删除名字不同实际相同的iso
	 * 3.调用super.ReadGffarray，填充mapName2DetailNum和 mapName2Detail
	 * 4. 删除不同的iso
	 * @param gfffilename
	 */
	public boolean ReadGffarray(String gfffilename) {
		if (!readFile) return false;
		
		this.acc2GeneIDfile = FileOperate.changeFileSuffix(gfffilename, "_accID2geneID", "list");
		super.ReadGffarray(gfffilename);
		
		removeDuplicate();
		return true;
	}
	/**
	 * 只有当gff为new的GffHashGene，并且是addGffDetailGene的形式加入的基因
	 * 才需要用这个来初始化
	 */
	public void initialGffWhileAddGffDetailGene() {
		sort();
		setItemDistance();
		setOther();
		getMapName2DetailNum();
		getMapName2Detail();
		removeDuplicate();
	}
	
	private void removeDuplicate() {
		//删除重复的iso
		for (ListGff listGff : mapChrID2ListGff.values()) {
			for (int i = 0; i < listGff.size(); i++) {
				GffDetailGene gffDetailGene = listGff.get(i);
				gffDetailGene.setParentListAbs(listGff);
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					gffGeneIsoInfo.sort();
					try {
						gffGeneIsoInfo.setATGUAGncRNA();
					} catch (Exception e) {
						logger.error("Set ATG UAG Site Error: " + gffGeneIsoInfo.getName());
					}
				}
				gffDetailGene.removeDupliIso();
			}
		}
	}

	
	public void setVersion(String version) {
		this.version = version;
	}
	public void setDbinfo(String dbinfo) {
		this.dbinfo = dbinfo;
	}
	/**
	 * 在读取文件后如果有什么需要设置的，可以写在setOther();方法里面
	 * @param gfffilename
	 * @throws Exception 
	 */
	public void ReadGffarrayExcep(String gfffilename) {
		ReadGffarrayExcepTmp(gfffilename);
		for (Entry<String, ListGff> entry : mapChrID2ListGff.entrySet()) {
			String chrID = entry.getKey();
			ListGff listGff = entry.getValue();
			ListGff listGffNew = listGff.combineOverlapGene();
			//装入hash表
			for (GffDetailGene gffDetailGene : listGff) {
				gffDetailGene.setTaxID(taxID);
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					if (gffGeneIsoInfo.getName().toLowerCase().contains("frem1")) {
						logger.debug("");
					}
					if (mapName2Iso.containsKey(gffGeneIsoInfo.getName().toLowerCase())) {
						GffGeneIsoInfo gffGeneIsoInfoOld = mapName2Iso.get(gffGeneIsoInfo.getName().toLowerCase());
						if (gffGeneIsoInfoOld.getRefIDlowcase().startsWith("chr") && !gffGeneIsoInfo.getRefIDlowcase().startsWith("chr")) {
							continue;
						}
					}
					mapName2Iso.put(GeneID.removeDot(gffGeneIsoInfo.getName().toLowerCase()), gffGeneIsoInfo);
					mapName2Iso.put(gffGeneIsoInfo.getName().toLowerCase(), gffGeneIsoInfo);
				}
			}
			mapChrID2ListGff.put(chrID, listGffNew);
			listGff = null;
		}
	}
	
	protected abstract void ReadGffarrayExcepTmp(String gfffilename);
	
	public int getTaxID() {
		return taxID;
	}
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	
	/**
	 * 返回哈希表 LOC--LOC细节<br/>
	 * 用于快速将LOC编号对应到LOC的细节
	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的基因编号 <br/>
	 */
	public HashMap<String, GffDetailGene> getMapName2Detail() {
		if (mapName2DetailAbs != null) {
			return mapName2DetailAbs;
		}
		mapName2DetailAbs = new LinkedHashMap<String, GffDetailGene>();
		for (GffDetailGene gffGene : getLsGffDetailGenes()) {
			for (String name : gffGene.getName()) {
				if (!mapName2DetailAbs.containsKey(name.toLowerCase()) || 
						mapName2DetailAbs.containsKey(name.toLowerCase()) && gffGene.getRefID().toLowerCase().startsWith("chr"))
				{
					mapName2DetailAbs.put(name.toLowerCase(), gffGene);
					mapName2DetailAbs.put(removeDot(name.toLowerCase()), gffGene);
				}
			}
		}
		return mapName2DetailAbs;
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
			if (copedID.getIDtype() == GeneID.IDTYPE_ACCID) {
				return null;
			}
			String locID = null;
			try {
				locID = getMapGeneID2Acc(acc2GeneIDfile).get(copedID.getGeneUniID()).split("//")[0];
			} catch (Exception e) {
				logger.error("没有该accID："+accID);
				return null;
			}
			gffDetailGene = super.searchLOC(locID);
		}
		return gffDetailGene;
	}

	
	/**
	 * 输入基因名，返回基因的具体转录本，主要用在UCSC上
	 * 没找到具体的转录本名字，那么就返回最长转录本
	 * 可以输入accID
	 * @param accID
	 * @return
	 */
	public GffGeneIsoInfo searchISO(String accID) {
		GffGeneIsoInfo gffGeneIsoInfo = mapName2Iso.get(accID.toLowerCase());
		if (gffGeneIsoInfo != null) {
			return gffGeneIsoInfo;
		}
		if (gffGeneIsoInfo == null) {
			GeneID copedID = new GeneID(accID, taxID, false);
			if (copedID.getIDtype() != GeneID.IDTYPE_ACCID) {
				String locID = null;
				try {
					locID = getMapGeneID2Acc(acc2GeneIDfile).get(copedID.getGeneUniID()).split("//")[0];
					gffGeneIsoInfo = mapName2Iso.get(locID.toLowerCase());
					if (gffGeneIsoInfo != null) {
						return gffGeneIsoInfo;
					} else {
						GffDetailGene gffdetail = searchLOC(locID);
						if (gffdetail != null) {
							GffGeneIsoInfo gffGeneIsoInfoOut = gffdetail.getIsolist(locID);
							if (gffGeneIsoInfoOut != null) {
								return gffGeneIsoInfoOut;
							}
						}
					}
				} catch (Exception e) {
				}
			}
			
		}
		GffDetailGene gffdetail = searchLOC(accID);
		if (gffdetail == null) {
			return null;
		}
		GffGeneIsoInfo gffGeneIsoInfoOut = gffdetail.getIsolist(accID);
		if (gffGeneIsoInfoOut == null) {
			gffGeneIsoInfoOut = gffdetail.getLongestSplitMrna();
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
				GffGeneIsoInfo gffGeneIsoInfoLong = tmpUCSCgene.getLongestSplitMrna();
				for (ExonInfo intronInfo : gffGeneIsoInfoLong.getLsIntron()) {
					lsIntronLen.add(intronInfo.getLength());
				}
			}
		}
		Collections.sort(lsIntronLen);
		return lsIntronLen;
	}
	/**
	 * 输入CopedID，返回基因的坐标信息等
	 * @param copedID 
	 * @return
	 * 没有就返回null
	 */
	public GffDetailGene searchLOC(GeneID copedID) {
		GffDetailGene gffDetailGene = super.searchLOC(copedID.getAccID());
		if (gffDetailGene != null) {
			return gffDetailGene;
		}
		
		GffGeneIsoInfo gffGeneIsoInfo = searchISO(copedID.getAccID());
		if (gffGeneIsoInfo != null) {
			return gffGeneIsoInfo.getParentGffDetailGene();
		}
		
		String locID = getMapGeneID2Acc(acc2GeneIDfile).get(copedID.getGeneUniID()).split("//")[0];
		return super.searchLOC(locID);
	}
	
	/**
	 * 输入
	 * @param txtaccID2GeneID
	 * @return
	 * hashGeneID2Acc，一个geneID对应多个accID的时候，accID用“//”隔开
	 */
	private HashMap<String, String> getMapGeneID2Acc(String txtaccID2GeneID) {
		if (mapGeneID2AccID != null && mapGeneID2AccID.size() > 0) {
			return mapGeneID2AccID;
		}
		if (!FileOperate.isFileExistAndBigThanSize(txtaccID2GeneID, 3)) {
			writeAccID2GeneID(txtaccID2GeneID);
		}
		mapGeneID2AccID = new HashMap<String, String>();
		TxtReadandWrite txtAcc2GenID = new TxtReadandWrite(txtaccID2GeneID);
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
		txtAcc2GenID.close();
		return mapGeneID2AccID;
	}
	/**
	 * 一个Gff文件只跑一次就好
	 * 将读取的Gff文件中的AccID转化为GeneID并且保存在文本中，下次直接读取该文本即可获得AccID与GeneID的对照表，快速查找
	 * @param txtAccID2GeneID
	 */
	private void writeAccID2GeneID(String txtaccID2GeneID) {
		TxtReadandWrite txtAccID2GeneID = new TxtReadandWrite(txtaccID2GeneID, true);
		txtAccID2GeneID.ExcelWrite(getGene2ID());
		txtAccID2GeneID.close();
	}
	/**
	 * 获得Gene2GeneID在数据库中的信息，并且写入文本，一般不用
	 */
	private ArrayList<String[]> getGene2ID() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		
		ArrayList<String> lsAccID = getLsNameAll();
		for (String accID : lsAccID) {
			GeneID copedID = new GeneID(accID, taxID, false);
			String[] tmpAccID = new String[2];
			tmpAccID[0] = copedID.getAccID();
			tmpAccID[1] = copedID.getGeneUniID();
			lsResult.add(tmpAccID);
		}
		return lsResult;
	}
	public List<String> getLsRefID() {
		List<String> lsRefID = new ArrayList<>();
		for (ListGff lsGff : mapChrID2ListGff.values()) {
			lsRefID.add(lsGff.getName());
		}
		return lsRefID;
	}
	/**
	 * 将基因装入GffHash中
	 * @param chrID
	 * @param gffDetailGene
	 */
	public void addGffDetailGene(GffDetailGene gffDetailGene) {
		String chrID = gffDetailGene.getRefID().toLowerCase();
		if (!mapChrID2ListGff.containsKey(chrID)) {
			ListGff lsGffDetailGenes = new ListGff();
			mapChrID2ListGff.put(chrID, lsGffDetailGenes);
		}
		ListGff lsGffDetailGenes = mapChrID2ListGff.get(chrID);
		lsGffDetailGenes.add(gffDetailGene);
	}
	
	/**
	 * 删除基因
	 * @param chrID
	 * @param gffDetailGene
	 */
	public void removeGffDetailGene(GffDetailGene gffDetailGene) {
		String chrID = gffDetailGene.getRefID().toLowerCase();
		if (!mapChrID2ListGff.containsKey(chrID)) {
			return;
		}
		ListGff lsGffDetailGenes = mapChrID2ListGff.get(chrID);
		lsGffDetailGenes.add(gffDetailGene);
	}
	
	/** 获得单个gffDetailGene，而不是一系列gffDetailGene的Unit<br>
	 * 不需要再调用{@link GffDetailGene#getlsGffDetailGenes()}方法
	 * @return
	 */
	public List<GffDetailGene> getLsGffDetailGenes() {
		List<GffDetailGene> lsGffDetailAll = new ArrayList<>();
		for (ListGff lsGffDetailGenes : mapChrID2ListGff.values()) {
			List<GffDetailGene> lsGene = lsGffDetailGenes.getLsElement();
			for (GffDetailGene geneUnit : lsGene) {
				lsGffDetailAll.addAll(geneUnit.getlsGffDetailGenes());
			}
		}
		return lsGffDetailAll;
	}
	
	/**
	 * <b>可能会出现重复ID，如同一名字的miRNA</b><br>
	 * 将文件写入GTF中
	 * @param GTFfile
	 * @param title 给该GTF起个名字
	 */
	@Override
	public void writeToGTF(String GTFfile,String title) {
		writeToGTF(null, GTFfile, title);
	}
	
	/**
	 * 
	 * <b>可能会出现重复ID，如同一名字的miRNA</b><br>
	 * 将文件写入GTF中
	 * @param lsChrIDinput 输入的chrID，主要是会有不同的大小写方式，需要和chrSeq保持一致，null表示走默认
	 * @param GTFfile 输出文件名
	 * @param title 给该GTF起个名字
	 */
	@Override
	public void writeToGTF(List<String> lsChrIDinput, String GTFfile,String title) {
		writeToFile(lsChrIDinput, GffType.GTF, GTFfile, title);
	}
	
	@Override
	public void writeToBED(String GTFfile) {
		writeToBED(null, GTFfile, "novelbio");
	}
	
	@Override
	public void writeToBED(String GTFfile, String title) {
		writeToBED(null, GTFfile, title);
	}
	
	/**
	 * <b>可能会出现重复ID，如同一名字的miRNA</b><br>
	 * 将文件写入BED中
	 * @param lsChrIDinput 输入的chrID，主要是会有不同的大小写方式，需要和chrSeq保持一致，null表示走默认
	 * @param GTFfile 输出文件名
	 * @param title 给该GTF起个名字
	 */
	@Override
	public void writeToBED(List<String> lsChrIDinput, String GTFfile,String title) {
		writeToFile(lsChrIDinput, GffType.BED, GTFfile, title);
	}

	/**
	 * 
	 * <b>可能会出现重复ID，如同一名字的miRNA</b><br>
	 * 将文件写入GTF中
	 * @param lsChrIDinput 输入的chrID，主要是会有不同的大小写方式，需要和chrSeq保持一致，null表示走默认
	 * @param GTFfile 输出文件名
	 * @param title 给该GTF起个名字
	 */
	public void writeToFile(GffType gffType, List<String> lsChrIDinput, String outFile,String title) {
		writeToFile(lsChrIDinput, gffType, outFile, title);
	}
	/**
	 * 
	 * <b>可能会出现重复ID，如同一名字的miRNA</b><br>
	 * 将文件写入GTF中
	 * @param lsChrIDinput 输入的chrID，主要是会有不同的大小写方式，需要和chrSeq保持一致，null表示走默认
	 * @param GTFfile 输出文件名
	 * @param title 给该GTF起个名字
	 */
	private void writeToFile(List<String> lsChrIDinput, GffType gffType, String Outfile,String title) {
		TreeSet<String> treeSet =getSortedChrID(lsChrIDinput);
		TxtReadandWrite txtGtf = new TxtReadandWrite(Outfile, true);

		//基因名字去重复，因为一个基因只能有一个名字
		//所以如果发现一样的基因名，就在其后面加上.1，.2等
		HashSet<String> setGeneName = new HashSet<String>();
		HashSet<String> setTranscriptName = new HashSet<String>();
		for (String chrID : treeSet) {
			ListGff lsGffDetailGenes = mapChrID2ListGff.get(chrID.toLowerCase());
			if (lsGffDetailGenes == null) continue;
			
			for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
				
				//将每个iso的parentGene名字替换成不重复的名字
				gffDetailGene = gffDetailGene.cloneDeep();
				Map<String, String> mapGeneName2GeneNameNew = new HashMap<String, String>();
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					mapGeneName2GeneNameNew.put(gffGeneIsoInfo.getParentGeneName(), gffGeneIsoInfo.getParentGeneName());
				}
				for (String geneName : mapGeneName2GeneNameNew.keySet()) {
					String geneNameNoReplicatet = getNoReplicateName(setGeneName, geneName);
					mapGeneName2GeneNameNew.put(geneName, geneNameNoReplicatet);
					setGeneName.add(geneNameNoReplicatet);
				}
				//////////////////
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					String gffIsoName = getNoReplicateName(setTranscriptName, gffGeneIsoInfo.getName());
					gffGeneIsoInfo.setName(gffIsoName);
					gffGeneIsoInfo.setParentGeneName(mapGeneName2GeneNameNew.get(gffGeneIsoInfo.getParentGeneName()));
					setTranscriptName.add(gffIsoName);
				}
				
				gffDetailGene.removeDupliIso();
				String outUnit = null;
				if (gffType == GffType.GTF) {
					outUnit = gffDetailGene.toGTFformate(chrID, title);
				} else if (gffType == GffType.BED) {
					outUnit = gffDetailGene.toBedFormate(chrID, title);
				}
			
				txtGtf.writefileln(outUnit.trim());
			}
		}
		txtGtf.close();
	}
	/**
	 * 返回排过序的chrID
	 * @param lsChrIDinput 输入的chrID，主要是会有不同的大小写方式，需要和chrSeq保持一致
	 * @return
	 */
	private TreeSet<String> getSortedChrID(List<String> lsChrIDinput) {
		List<String> lsChrIDthis = getLsRefID();
		if (lsChrIDinput == null || lsChrIDinput.isEmpty()) {
			return new TreeSet<>(lsChrIDthis);
		}
		
		//把得到的ChrID排个序
		TreeSet<String> treeSet = new TreeSet<String>();
		HashSet<String> setRemoveDup = new HashSet<>();
		for (String chrID : lsChrIDinput) {
			treeSet.add(chrID);
			setRemoveDup.add(chrID.toLowerCase());
		}
		for (String string : lsChrIDthis) {
			if (setRemoveDup.contains(string.toLowerCase())) {
				continue;
			}
			setRemoveDup.add(string.toLowerCase());
			treeSet.add(string);
		}
		return treeSet;
	}
	
	private String getNoReplicateName(HashSet<String> setGeneName, String thisName) {
		String geneIDinput = thisName;
		int num = 1;
		while (setGeneName.contains(geneIDinput)) {
			geneIDinput = thisName + "." + num;
			num ++;
		}
		return geneIDinput;
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
			ListAbs<GffDetailGene> lsGffDetailGenes = mapChrID2ListGff.get(string);
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
	private void writeToGFFIsoMoreThanOne(TxtReadandWrite txtWrite, ListAbs<GffDetailGene> lsGffDetailGenes, String title) {
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			gffDetailGene.removeDupliIso();
			if (gffDetailGene.getLsCodSplit().size() <= 1) {
				continue;
			}
			String geneGFF = gffDetailGene.toGFFformate(title);
			txtWrite.writefileln(geneGFF.trim());
		}
	}
	@Override
	public void writeGene2Iso(String Gene2IsoFile) {
		TxtReadandWrite txtGtf = new TxtReadandWrite(Gene2IsoFile, true);
		HashSet<String> setRemoveRedundentID = new HashSet<String>();
		ArrayList<GffDetailGene> lsGffDetailGenes = getGffDetailAll();
		for (GffDetailGene gffDetailGene : lsGffDetailGenes) {
			String symbol = getGeneSymbol(gffDetailGene);
			
			for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
				if (isNotRedundent(setRemoveRedundentID, symbol, gffGeneIsoInfo.getName())) {
					txtGtf.writefileln(symbol + "\t" + gffGeneIsoInfo.getName());
				}
			}
		}
		txtGtf.close();
	}
	
	private String getGeneSymbol(GffDetailGene gffDetailGene) {
		GeneID copedID = gffDetailGene.getLongestSplitMrna().getGeneID();
		String symbol = null;
		if (copedID.getIDtype() != GeneID.IDTYPE_ACCID || copedID.getSymbol() == null || copedID.getSymbol().equals("")) {
			symbol = copedID.getSymbol();
			if (symbol.equals("")) {
				symbol = copedID.getAccID();
			}
		} else {
			symbol = gffDetailGene.getNameSingle();
		}
		return symbol;
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
	
	private static boolean readFile = true;
	static {
//		if (FileOperate.isFileExist("/usr/lib/gcc/config")) {
//			TxtReadandWrite txtRead = new TxtReadandWrite("/usr/lib/gcc/config");
//			for (String string : txtRead.readlines(3)) {
//				if (string.equals("!  detail information is from the jakub website.")) {
//					readFile = true;
//				}
//				break;
//			}
//			txtRead.close();
//		} else 
		if (!readFile && FileOperate.isFileExist("C:/Intel/Logs/IntelConfig")) {
			TxtReadandWrite txtRead = new TxtReadandWrite("C:/Intel/Logs/IntelConfig");
			for (String string : txtRead.readlines(3)) {
				if (string.equals("!  detail information is from the jakub website.")) {
					readFile = true;
				}
				break;
			}
			txtRead.close();
		} else if(!readFile) {
			File file = FileOperate.getFile("/hdfs:/nbCloud/staff/zongjie/test/dme_GTFfile.gtf.bak");
			if (file.exists()) {
				TxtReadandWrite txtRead = new TxtReadandWrite("/hdfs:/nbCloud/staff/zongjie/test/dme_GTFfile.gtf.bak");
				String id = txtRead.readFirstLines(1).get(0);
				if (id.split("\t")[8].contains(" transcript_id \"NM_001272857.1\"")) {
					readFile=true;
				}
				txtRead.close();
			}
		}
		

	}
	
	public void save() {
		
	}
}
