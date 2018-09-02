package com.novelbio.bioinfo.gff;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.base.binarysearch.BinarySearch;
import com.novelbio.bioinfo.base.binarysearch.BsearchSite;
import com.novelbio.bioinfo.base.binarysearch.BsearchSiteDu;
import com.novelbio.bioinfo.base.binarysearch.ListEleSearch;
import com.novelbio.bioinfo.base.binarysearch.ListHashSearch;
import com.novelbio.database.domain.modgeneid.GeneID;

public abstract class GffHashGeneAbs extends ListEleSearch<GffGene, ListGff> implements GffHashGeneInf {
	private static final Logger logger = LoggerFactory.getLogger(GffHashGeneAbs.class);


	int taxID = 0;
	String acc2GeneIDfile = "";
	String gfffile = "";
	String version;
	String dbinfo;
	private HashMap<String, GffIso> mapName2Iso = new HashMap<String, GffIso>();
	public GffHashGeneAbs() {
		mapChrID2ListGff = new LinkedHashMap<String, ListGff>();
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
		this.acc2GeneIDfile = FileOperate.changeFileSuffix(gfffilename, "_accID2geneID", "list");
		super.ReadGffarray(gfffilename);
		
		return true;
	}
	
	/**
	 * 在读取文件后如果有什么需要设置的，可以写在setOther();方法里面，本方发为空，直接继承即可
	 */
	protected void setOther() {
		removeDuplicate();
	}
	
	/**
	 * 只有当gff为new的GffHashGene，并且是addGffDetailGene的形式加入的基因
	 * 才需要用这个来初始化
	 */
	public void initialGffWhileAddGffDetailGene() {
		sort();
		setItemDistance();
		setOther();
		getMapName2Detail();
	}
	
	private void removeDuplicate() {
		//删除重复的iso
		for (ListGff listGff : mapChrID2ListGff.values()) {
			for (int i = 0; i < listGff.size(); i++) {
				GffGene gffDetailGene = listGff.get(i);
				gffDetailGene.setParent(listGff);
				for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					gffGeneIsoInfo.sortOnly();
					try {
						gffGeneIsoInfo.setATGUAGncRNA();
					} catch (Exception e) {
						logger.error("Set ATG UAG Site Error: " + gffGeneIsoInfo.getName());
					}
				}

				gffDetailGene.removeDupliIsoInGene();
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
			listGff.sort();
			ListGff listGffNew = listGff.combineOverlapGene();
			//装入hash表
			for (GffGene gffDetailGene : listGff) {
				gffDetailGene.setTaxID(taxID);
				for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					if (mapName2Iso.containsKey(gffGeneIsoInfo.getName().toLowerCase())) {
						GffIso gffGeneIsoInfoOld = mapName2Iso.get(gffGeneIsoInfo.getName().toLowerCase());
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
	public HashMap<String, GffGene> getMapName2Detail() {
		if (mapName2DetailAbs != null) {
			return mapName2DetailAbs;
		}
		mapName2DetailAbs = new LinkedHashMap<String, GffGene>();
		for (GffGene gffGene : getLsGffDetailGenes()) {
			for (String name : gffGene.getLsNameAll()) {
				if (!mapName2DetailAbs.containsKey(name.toLowerCase()) || 
						mapName2DetailAbs.containsKey(name.toLowerCase()) && gffGene.getChrId().toLowerCase().startsWith("chr"))
				{
					mapName2DetailAbs.put(name.toLowerCase(), gffGene);
					mapName2DetailAbs.put(removeDot(name.toLowerCase()), gffGene);
				}
			}
		}
		return mapName2DetailAbs;
	}
	
	/**
	 * 获得的每一个信息都是实际的而没有clone
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 采用clone的方法获得信息
	 * 没找到就返回null
	 * @param chrID 内部自动转化为小写
	 * @param cod1 坐标
	 */
	public GffCodGene searchLocation(String chrID, int cod1) {
		chrID = chrID.toLowerCase();
		ListGff Loclist =  getMapChrID2LsGff().get(chrID);// 某一条染色体的信息
		if (Loclist == null) {
			addChrIdCannotFind(chrID);
			return null;
		}
		BinarySearch<GffGene> binarySearch = new BinarySearch<>(Loclist.getLsElement());
		return new GffCodGene(binarySearch.searchLocation(cod1));//(chrID, Math.min(cod1, cod2));
	}
	public GffCodGeneDU searchLocation(String chrID, int cod1, int cod2) {
		ListGff Loclist =  getMapChrID2LsGff().get(chrID.toLowerCase());// 某一条染色体的信息
		if (Loclist == null) {
			addChrIdCannotFind(chrID);
			return null;
		}
		BinarySearch<GffGene> binarySearch = new BinarySearch<>(Loclist.getLsElement());
		BsearchSiteDu<GffGene> gffCodDu = binarySearch.searchLocationDu(cod1, cod2);
		return new GffCodGeneDU(gffCodDu);
	}
	
	public GffCodGeneDU searchLocation(Alignment alignment) {
		return searchLocation(alignment.getChrId(), alignment.getStartAbs(), alignment.getEndAbs());
	}
	
	public GffGene searchLOC(String accID) {
		return searchLOCWithoutDB(accID);
	}
	
	/**
	 * 输入基因名，返回基因的坐标信息等，不查找数据库
	 * 可以输入accID
	 * @param accID
	 * @return
	 */
	public GffGene searchLOCWithoutDB(String accID) {
		GffGene gffDetailGene = super.searchLOC(accID);
		if (gffDetailGene == null) {
			return null;
		}
		for (GffGene gene : gffDetailGene.getlsGffDetailGenes()) {
			if (gene.getName().equalsIgnoreCase(accID)) {
				return gene;
			}
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
	public GffIso searchISOwithoutDB(String accID) {
		GffIso gffGeneIsoInfo = mapName2Iso.get(accID.toLowerCase());
		if (gffGeneIsoInfo != null) {
			return gffGeneIsoInfo;
		}
		GffGene gffdetail = null;
		gffdetail = searchLOCWithoutDB(accID);

		if (gffdetail == null) return null;
		
		GffIso gffGeneIsoInfoOut = gffdetail.getIsolist(accID);
		if (gffGeneIsoInfoOut == null) {
			gffGeneIsoInfoOut = gffdetail.getLongestSplitMrna();
		}
		return gffGeneIsoInfoOut;
	}
	
	@Override
	public GffIso searchISO(String accID) {
		return searchISOwithoutDB(accID);
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
				GffGene tmpUCSCgene=value.get(i);
				GffIso gffGeneIsoInfoLong = tmpUCSCgene.getLongestSplitMrna();
				for (ExonInfo intronInfo : gffGeneIsoInfoLong.getLsIntron()) {
					lsIntronLen.add(intronInfo.getLength());
				}
			}
		}
		Collections.sort(lsIntronLen);
		return lsIntronLen;
	}
	
	/** 染色体都小写 */
	public  HashMap<String, ListGff> getMapChrID2LsGff() {
		return mapChrID2ListGff;
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
	 * @param chrId
	 * @param gffDetailGene
	 */
	public void addGffDetailGene(GffGene gffDetailGene) {
		String chrID = gffDetailGene.getChrId().toLowerCase();
		if (!mapChrID2ListGff.containsKey(chrID)) {
			ListGff lsGffDetailGenes = new ListGff();
			mapChrID2ListGff.put(chrID, lsGffDetailGenes);
		}
		ListGff lsGffDetailGenes = mapChrID2ListGff.get(chrID);
		lsGffDetailGenes.add(gffDetailGene);
	}
	
	/**
	 * 删除基因
	 * @param chrId
	 * @param gffDetailGene
	 */
	public void removeGffDetailGene(GffGene gffDetailGene) {
		String chrID = gffDetailGene.getChrId().toLowerCase();
		if (!mapChrID2ListGff.containsKey(chrID)) {
			return;
		}
		ListGff lsGffDetailGenes = mapChrID2ListGff.get(chrID);
		lsGffDetailGenes.add(gffDetailGene);
	}
	
	/** 获得单个gffDetailGene，而不是一系列gffDetailGene的Unit<br>
	 * 不需要再调用{@link GffGene#getlsGffDetailGenes()}方法
	 * @return
	 */
	public List<GffGene> getLsGffDetailGenes() {
		List<GffGene> lsGffDetailAll = new ArrayList<>();
		for (ListGff lsGffDetailGenes : mapChrID2ListGff.values()) {
			List<GffGene> lsGene = lsGffDetailGenes.getLsElement();
			lsGffDetailAll.addAll(lsGene);
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
	public void writeToBED(String bedFile) {
		writeToBED(null,bedFile, "novelbio");
	}
	
	@Override
	public void writeToBED(String bedFile, String title) {
		writeToBED(null, bedFile, title);
	}
	
	/**
	 * <b>可能会出现重复ID，如同一名字的miRNA</b><br>
	 * 将文件写入BED中
	 * @param lsChrIDinput 输入的chrID，主要是会有不同的大小写方式，需要和chrSeq保持一致，null表示走默认
	 * @param GTFfile 输出文件名
	 * @param title 给该GTF起个名字
	 */
	@Override
	public void writeToBED(List<String> lsChrIDinput, String bedFile,String title) {
		writeToFile(lsChrIDinput, GffType.BED, bedFile, title);
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
	private void writeToFile(List<String> lsChrIDinput, GffType gffType, String outFile,String title) {
		TreeSet<String> treeSet =getSortedChrID(lsChrIDinput);
		String outFileTmp = FileOperate.changeFileSuffix(outFile, "_tmp", null);
		TxtReadandWrite txtGtf = new TxtReadandWrite(outFileTmp, true);

		//基因名字去重复，因为一个基因只能有一个名字
		//所以如果发现一样的基因名，就在其后面加上.1，.2等
		HashSet<String> setGeneName = new HashSet<String>();
		HashSet<String> setTranscriptName = new HashSet<String>();
		for (String chrID : treeSet) {
			ListGff lsGffDetailGenes = mapChrID2ListGff.get(chrID.toLowerCase());
			if (lsGffDetailGenes == null) continue;
			
			for (GffGene gffDetailGene : lsGffDetailGenes.getLsElement()) {
				//将每个iso的parentGene名字替换成不重复的名字
				gffDetailGene = gffDetailGene.cloneDeep();
				Map<String, String> mapGeneName2GeneNameNew = new HashMap<String, String>();
				for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					mapGeneName2GeneNameNew.put(gffGeneIsoInfo.getParentGeneName(), gffGeneIsoInfo.getParentGeneName());
				}
				for (String geneName : mapGeneName2GeneNameNew.keySet()) {
					String geneNameNoReplicatet = getNoReplicateName(setGeneName, geneName);
					mapGeneName2GeneNameNew.put(geneName, geneNameNoReplicatet);
					setGeneName.add(geneNameNoReplicatet);
				}
				//////////////////
				for (GffIso gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
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
				} else if (gffType == GffType.GFF3) {
					List<String> lsTmp = gffDetailGene.toGFFformate(chrID, title);
					StringBuilder stringBuilder = new StringBuilder();
					for (String string : lsTmp) {
						stringBuilder.append(string + TxtReadandWrite.ENTER_LINUX);
                    }
					outUnit = stringBuilder.toString();
				} else {
					txtGtf.close();
					throw new ExceptionNbcGFF("Unsupport Gff type");
				}
			
				txtGtf.writefileln(outUnit.trim());
			}
		}
		txtGtf.close();
		FileOperate.moveFile(true, outFileTmp, outFile);
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
//		int num = 1;
//		while (setGeneName.contains(geneIDinput)) {
//			geneIDinput = thisName + "." + num;
//			num ++;
//		}
		return geneIDinput;
	}
	
}
