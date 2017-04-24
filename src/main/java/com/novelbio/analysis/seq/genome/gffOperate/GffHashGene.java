package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.analysis.seq.genome.ExceptionNbcGFF;
import com.novelbio.analysis.seq.sam.SamIndexRefsequence;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.service.servgff.MgmtGffDetailGene;

/**
 * 
 * 本类加载时不返回信息，只有在结束时才会返回是否成功
 * @author zong0jie
 */
public class GffHashGene extends RunProcess<Integer> implements GffHashGeneInf {
	public static final String GFFDBNAME = "novelbio";
	
	GffHashGeneAbs gffHashGene = null;
	GffType gffType;
	String gffFile;
	int taxID;
	/** 数据库使用，记录物种的测序版本 */
	String version;
	/** 数据库使用，记录gff的来源，是NCBI还是Ensembl */
	String dbinfo;
	/** 发现重复的mRNA名字时，就换一个名字，专用于果蝇 */
	boolean isFilterDuplicateName;
	/**
	 * 新建一个GffHashGeneUCSC的类，需要readGffFile
	 */
	public GffHashGene() {
		gffHashGene =  new GffHashGTF();
	}
	public GffHashGene(GffHashGeneAbs gffHashGene) {
		this.gffHashGene = gffHashGene;
	}

	/**
	 * 读取并初始化，可以用isFinished()来判定是否顺利运行完毕
	 * @param gffType
	 * @param gffFile
	 */
	public GffHashGene(GffType gffType, String gffFile) {
		this.gffType = gffType;
		this.gffFile = gffFile;
		flagStop = read(0, null, null, gffType, gffFile, false);
	}
	/**
	 * @param gffType
	 * @param gffFile
	 * @param isFilterDuplicateGeneName 果蝇中存在重复的基因名
	 */
	public GffHashGene(GffType gffType, String gffFile, boolean isFilterDuplicateGeneName) {
		this.gffType = gffType;
		this.gffFile = gffFile;
		flagStop = read(0, null, null, gffType, gffFile, isFilterDuplicateGeneName);
	}
	/**
	 * 读取并初始化，可以用isFinished()来判定是否顺利运行完毕
	 * @param gffType
	 * @param gffFile
	 * @param isFilterDuplicateGeneName 果蝇中存在重复的基因名
	 */
	public GffHashGene(int taxID, String version, String dbinfo, GffType gffType, String gffFile, boolean isFilterDuplicateGeneName) {
		this.taxID = taxID;
		this.version = version;
		this.gffType = gffType;
		this.gffFile = gffFile;
		this.dbinfo = dbinfo;
		read(taxID,version, dbinfo, gffType, gffFile, isFilterDuplicateGeneName);
	}
	/**
	 * 读取并初始化，可以用isFinished()来判定是否顺利运行完毕
	 * @param gffFile 根据文件后缀名判断是GFF还是GTF
	 */
	public GffHashGene(String gffFile) {
		gffType = readGffTypeFromFileName(gffFile);
		this.gffFile = gffFile;
		read(taxID, version, dbinfo, gffType, gffFile, false);
	}
	/**
	 * 读取并初始化，可以用isFinished()来判定是否顺利运行完毕
	 * @param gffFile 根据文件后缀名判断是GFF还是GTF
	 * @param isFilterDuplicateGeneName 果蝇中存在重复的基因名
	 */
	public GffHashGene(String gffFile, boolean isFilterDuplicateGeneName) {
		gffType = readGffTypeFromFileName(gffFile);
		this.gffFile = gffFile;
		read(taxID, version, dbinfo, gffType, gffFile, isFilterDuplicateGeneName);
	}
	
	private GffType readGffTypeFromFileName(String gffFile) {
		GffType gffType = GffType.GTF;
		String gffFileTmp = gffFile;
		if (gffFile.endsWith(".gz")) {
			gffFileTmp = gffFile.substring(0, gffFile.length()-3);
		}
		String suffix = FileOperate.getFileNameSep(gffFileTmp)[1];
		if (suffix.trim().toLowerCase().equals("gff") || suffix.trim().toLowerCase().equals("gff3")) {
			gffType = GffType.NCBI;
		} else if (suffix.trim().toLowerCase().equals("gtf")) {
			gffType = GffType.GTF;
		} else {
			gffType = GffType.UCSC;
		}
		return gffType;
	}
	
	public void setTaxIdVersion(int taxID, String version, String dbinfo) {
		this.taxID = taxID;
		this.version = version;
		this.dbinfo = dbinfo;
	}
	
	/**
	 * 读取但不初始化<br>
	 * 设定完该信息后可以通过运行run来加载Gff信息
	 * @param gffFile
	 * @param gffType
	 */
	public void setGffInfo(String gffFile) {
		this.gffFile =gffFile;
		this.gffType = readGffTypeFromFileName(gffFile);
	}
	
	/**
	 * 读取但不初始化<br>
	 * 设定完该信息后可以通过运行run来加载Gff信息
	 * @param gffFile
	 * @param gffType
	 */
	public void setGffInfo(GffType gffType, String gffFile) {
		this.gffFile = gffFile;
		this.gffType = gffType;
	}
	
	public String getVersion() {
		return version;
	}
	public String getDbinfo() {
		return dbinfo;
	}
	@Override
	protected void running() {
		read(taxID, version, dbinfo, gffType, gffFile, isFilterDuplicateName);
	}
	
	/**
	 * @param taxID
	 * @param version
	 * @param dbinfo
	 * @param gffType
	 * @param gffFile
	 * @param isFilterDuplicateName 发现重复的mRNA名字时，就换一个名字，专用于果蝇
	 * @return
	 */
	private boolean read(int taxID, String version, String dbinfo, GffType gffType, String gffFile, boolean isFilterDuplicateName) {
		if (gffType == GffType.UCSC) {
			gffHashGene = new GffHashGeneUCSC();
		}
		else if (gffType == GffType.TIGR || gffType == GffType.Plant) {
			gffHashGene = new GffHashGenePlant(gffType);
		}
		else if (gffType == GffType.GTF) {
			gffHashGene = new GffHashGTF();
		}
		else if (gffType == GffType.NCBI) {
			gffHashGene = new GffHashGeneNCBI();
			((GffHashGeneNCBI)gffHashGene).setFilterDuplicateName(isFilterDuplicateName);
		}
		if (taxID > 0) {
			gffHashGene.setTaxID(taxID);
			gffHashGene.setVersion(version);
			gffHashGene.setDbinfo(dbinfo);
		}
		return gffHashGene.ReadGffarray(gffFile);
	}
	
	/** 如果是从Fasta序列而来的gff，就用这个包装 */
	public void setGffHashGeneFromFasta(String seqFasta, String proteinSeq) {
		GffHashGeneRefSeq gffHashGeneRefSeq = new GffHashGeneRefSeq();
		gffHashGeneRefSeq.setProteinSeq(proteinSeq);
		gffHashGeneRefSeq.ReadGffarray(seqFasta);
		this.gffHashGene = gffHashGeneRefSeq;
	}
	public void setGffHashGene(GffHashGeneAbs gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	
	/**
	 * 只有当gff为new的GffHashGene，并且是addGffDetailGene的形式加入的基因
	 * 才需要用这个来初始化
	 */
	public void initialGffWhileAddGffDetailGene() {
		gffHashGene.initialGffWhileAddGffDetailGene();
	}
	/**
	 * 读取信息
	 * @param gffFile
	 */
	public void readGffFile(String gffFile) {
		gffHashGene.ReadGffarray(gffFile);
	}
	/**
	 * 专门给冯英的项目用的，设定ref的Gffinfo
	 */
	public void setGffHash(GffHashGene gffHashRef) {
		GffHashGTF gff = (GffHashGTF)gffHashGene;
		gff.setGffHashRef(gffHashRef);
	}
	
	public void removeDuplicateIso() {
		HashMap<String, ListGff> mapChrID2LsGff = getMapChrID2LsGff();
		for (ListGff listGff : mapChrID2LsGff.values()) {
			for (GffDetailGene gffDetailGene : listGff) {
				gffDetailGene.removeDupliIso();
			}
		}
	}
	
	public void sort() {
		gffHashGene.sort();
	}
	
	/** 顺序存储ChrHash中的ID，这个就是ChrHash中实际存储的ID，如果两个Item是重叠的，就全加入 */
	public ArrayList<String> getLsNameAll() {
		return gffHashGene.getLsNameAll();
	}
	@Override
	public ArrayList<String> getLsNameNoRedundent() {
		return gffHashGene.getLsNameNoRedundent();
	}

	@Override
	public String[] getLOCNum(String LOCID) {
		return gffHashGene.getLOCNum(LOCID);
	}

	public HashMap<String, GffDetailGene> getLocHashtable() {
		return gffHashGene.getMapName2Detail();
	}

	public GffCodGene searchLocation(String chrID, int Coordinate) {
		return gffHashGene.searchLocation(chrID, Coordinate);
	}
	@Override
	public GffGeneIsoInfo searchISO(String LOCID) {
		return gffHashGene.searchISO(LOCID);
	}

	public GffGeneIsoInfo searchISOwithoutDB(String LOCID) {
		return gffHashGene.searchISOwithoutDB(LOCID);
	}
	public GffDetailGene searchLOC(String LOCID) {
		return gffHashGene.searchLOC(LOCID);
	}
	
	public GffDetailGene searchLOCWithoutDB(String LOCID) {
		return gffHashGene.searchLOCWithoutDB(LOCID);
	}
	
	@Override
	public GffDetailGene searchLOC(GeneID copedID) {
		return gffHashGene.searchLOC(copedID);
	}

	public GffDetailGene searchLOC(String chrID, int LOCNum) {
		return gffHashGene.searchLOC(chrID, LOCNum);
	}
	
	@Override
	public GffCodGeneDU searchLocation(Alignment alignment) {
		return gffHashGene.searchLocation(alignment.getRefID(), alignment.getStartAbs(), alignment.getEndAbs());
	}
	
	/**
	 * 内部自动判断 cod1 和 cod2的大小
	 * @param chrID
	 * @param cod1
	 * @param cod2
	 * @return
	 */
	public GffCodGeneDU searchLocation(String chrID, int cod1, int cod2) {
		return gffHashGene.searchLocation(chrID, cod1, cod2);
	}
	public ArrayList<String> getLsChrID() {
		return ArrayOperate.getArrayListKey(gffHashGene.getMapChrID2LsGff());
	}
	@Override
	public String getGffFilename() {
		return gffHashGene.getGffFilename();
	}
	@Override
	public int getTaxID() {
		return taxID;
	}
	/** 染色体都小写 */
	public  HashMap<String, ListGff> getMapChrID2LsGff() {
		return gffHashGene.getMapChrID2LsGff();
	}
	/** 返回所有不重复GffDetailGene */
	public ArrayList<GffDetailGene> getGffDetailAll() {
		return gffHashGene.getGffDetailAll();
	}
	@Override
	public List<GffDetailGene> getLsGffDetailGenes() {
		return gffHashGene.getLsGffDetailGenes();
	}
	/** 返回所有不重复GffDetailGene */
	public ArrayList<Integer> getLsIntronSortedS2M() {
		return gffHashGene.getLsIntronSortedS2M();
	}
	/**
	 * 将基因装入GffHash中
	 * @param chrID
	 * @param gffDetailGene
	 */
	public void addGffDetailGene(GffDetailGene gffDetailGene) {
		gffHashGene.addGffDetailGene(gffDetailGene);
	}

	public void writeToGTF(String GTFfile) {
		gffHashGene.writeToGTF(GTFfile, GFFDBNAME);
	}
	@Override
	public void writeToGTF(String GTFfile, String title) {
		gffHashGene.writeToGTF(GTFfile, title);
	}
	public void writeToGTF(List<String> lsChrID, String GTFfile) {
		gffHashGene.writeToGTF(lsChrID, GTFfile, GFFDBNAME);
	}
	public void writeToFile(GffType gffType, List<String> lsChrID, String outFile) {
		gffHashGene.writeToFile(gffType, lsChrID, outFile, GFFDBNAME);
	}
	public void writeToFile(GffType gffType, String outFile) {
		gffHashGene.writeToFile(gffType, null, outFile, GFFDBNAME);
	}
	@Override
	public void writeToGTF(List<String> lsChrID, String GTFfile, String title) {
		gffHashGene.writeToGTF(lsChrID, GTFfile, title);
	}
	@Override
	public void writeToBED(List<String> lsChrID, String GTFfile, String title) {
		gffHashGene.writeToBED(lsChrID, GTFfile, title);
	}
	@Override
	public void writeToBED(String GTFfile) {
		gffHashGene.writeToBED(GTFfile);
	}
	@Override
	public void writeToBED(String GTFfile, String title) {
		gffHashGene.writeToBED(GTFfile, title);
	}
	
	public void writeToBED(List<String> lsChrID, String GTFfile) {
		gffHashGene.writeToBED(lsChrID, GTFfile, GFFDBNAME);
	}

	@Override
	public void writeGene2Iso(String Gene2IsoFile) {
		gffHashGene.writeGene2Iso(Gene2IsoFile);
	}	
	/** 自动判断染色体 */
	public void addListGff(ListGff listGff) {
		String chrID = listGff.getName();
		gffHashGene.getMapChrID2LsGff().put(chrID.toLowerCase(), listGff);
	}
	
	/** 是否包含指定的chrID
	 * 输入ID会自动转化为小写
	 *  */
	public boolean isContainChrID(String chrID) {
		if (chrID == null) return false;
		
		if (gffHashGene.getMapChrID2LsGff().containsKey(chrID.toLowerCase())) {
			return true;
		}
		return false;
	}
	
	/**
	 * 返回全体iso的set，用于做背景，这个是比较全面的背景
	 * @return
	 */
	public HashSet<String> getSetIsoID() {
		HashSet<String> setGeneID = new HashSet<String>();
		HashMap<String, ListGff> mapChrID2LsGff = getMapChrID2LsGff();
		for (ListGff listGff : mapChrID2LsGff.values()) {
			for (GffDetailGene gffDetailGene : listGff) {
				for (GffGeneIsoInfo geneIsoInfo : gffDetailGene.getLsCodSplit()) {
					setGeneID.add(geneIsoInfo.getName());
				}
			}
		}
		return setGeneID;
	}
	
	/** 从GFF中获得的染色体长度信息，不准，主要用在RNAseq的时候 */
	public HashMap<String, Long> getChrID2LengthForRNAseq() {
		HashMap<String, Long> mapChrID2Length = new HashMap<String, Long>();
		
		HashMap<String, ListGff> mapChrID2LsGff = getMapChrID2LsGff();
		for (String chrID : mapChrID2LsGff.keySet()) {
			ListGff lsGff = mapChrID2LsGff.get(chrID);
			long end = lsGff.get(lsGff.size() - 1).getEndAbs() + 5000;
			mapChrID2Length.put(chrID, end);
		}
		return mapChrID2Length;
	}
	
	/**
	 * 设定每个基因的区间
	 * @param geneNum 每个区间的基因数量，可以设定为10
	 */
	public Map<String, List<int[]>> getMapChrID2LsInterval(int geneNum) {
		Map<String, List<int[]>> mapChrID2LsInterval = new LinkedHashMap<>();
		Map<String, ListGff> mapChrID2ListGff = getMapChrID2LsGff();
		for (String chrID : mapChrID2ListGff.keySet()) {
			int num = 1;
			List<int[]> lsInterval = new ArrayList<>();
			mapChrID2LsInterval.put(chrID, lsInterval);
			int[] interval = null;
			ListGff lsGff = mapChrID2ListGff.get(chrID);
			for (GffDetailGene gffDetailGene : lsGff) {
				if (num == 1) {
					interval = new int[2];
					interval[0] = gffDetailGene.getStartAbs();
					interval[1] = gffDetailGene.getEndAbs();
					lsInterval.add(interval);
				} else {
					interval[1] = gffDetailGene.getEndAbs();
					if (num == geneNum) {
						num = 0;
					}
				}
				num++;
			}
		}
		return mapChrID2LsInterval;
	}
	
	/**
	 * 返回overlap的gene
	 * @return
	 */
	public List<GffDetailGene> getLsOverlapGenes() {
		List<GffDetailGene> lsOverlapGene = new ArrayList<>();
		for (GffDetailGene gffGene : getGffDetailAll()) {
			if (gffGene.isCis5to3Real() != null) {
				continue;
			}
			Map<double[], GffGeneIsoInfo> mapIsoCis = new HashMap<>();
			Map<double[], GffGeneIsoInfo> mapIsoTrans = new HashMap<>();
			for (GffGeneIsoInfo iso : gffGene.getLsCodSplit()) {
				if (iso.isCis5to3()) {
					for (ExonInfo exonInfo : iso) {
						mapIsoCis.put(new double[]{exonInfo.getStartAbs(), exonInfo.getEndAbs()}, iso);
					}
				} else {
					for (ExonInfo exonInfo : iso) {
						mapIsoTrans.put(new double[]{exonInfo.getStartAbs(), exonInfo.getEndAbs()}, iso);
					}
				}
			}
			boolean isTrue = false;
			for (double[] edge : mapIsoCis.keySet()) {
				GffGeneIsoInfo isoCis = mapIsoCis.get(edge);
				int overlapNum = 0;

				for (double[] edgeTrans : mapIsoTrans.keySet()) {
					GffGeneIsoInfo isoTrans = mapIsoTrans.get(edgeTrans);
					if (ArrayOperate.cmpArray(edge, edgeTrans)[1] > 10 && isoCis.size() > 2 && isoTrans.size() > 2 ) {
						overlapNum++;
					}
			
				}
				if (overlapNum > 2) {
					isTrue = true;
					break;
				}
			}
			if (isTrue) {
				lsOverlapGene.add(gffGene);
			}
		}
		return lsOverlapGene;
	}
	
	
	/** 判定这个gff中是否有ncRNA存在，首先要有mRNA存在，因为如果没有mRNA都是ncRNA，很可能是没有预测好orf的Gff文件
	 * 此外还要有miRNA，这是判定该gff文件是否注释清楚的关键
	 * @param gffHashGene
	 * @return
	 */
	public boolean isContainNcRNA() {
		int mRNAnum = 0, miRNAnum = 0, ncRNAnum = 0, all = 0;
		for (GffDetailGene gffDetailGene : getLsGffDetailGenes()) {
			for (GffGeneIsoInfo iso : gffDetailGene.getLsCodSplit()) {
				GeneType geneType = iso.getGeneType();
				all++;
				if (geneType == GeneType.mRNA) {
					mRNAnum++;
				} else if (geneType == GeneType.miRNA) {
					miRNAnum++;
				} else {
					ncRNAnum++;
				}
			}
		}
		if (mRNAnum > all/3 && miRNAnum > 10 && ncRNAnum > 100) {
			return true;
		}
		return false;
	}
	
	public String convertToFile(GffType gfftype, List<String> lsChrId) {
		String gffFile = getGffFilename();
		String outFile = convertNameToOtherFile(gffFile, gfftype);
		if (!StringOperate.isEqual(gffFile, outFile)) {
			gffHashGene.writeToFile(gfftype, lsChrId, outFile, GFFDBNAME);
        }
		return outFile;
	}
	
	/** 仅修改名字 */
	public static String convertNameToOtherFile(String gffFileName, GffType gffType) {
		String suffix = null;
		if (gffType == GffType.NCBI) {
			suffix = "gff3";
		} else if (gffType == GffType.GTF) {
			suffix = "gtf";
		} else if (gffType == GffType.BED) {
			suffix = "bed";
		} else {
			throw new ExceptionNbcGFF("Not support this type " + gffType);
		}
		if (gffFileName.endsWith(".gz")) {
			gffFileName = gffFileName.substring(0, gffFileName.length()-3);
        }
		return FileOperate.changeFileSuffix(gffFileName, "", "gff3|gtf|gff|bed", suffix);
	}
	
	public static String convertToOtherFile(String gffFileName, GffType gffType) {
		String resultFile = convertNameToOtherFile(gffFileName, gffType);
		if (FileOperate.isFileExistAndBigThan0(resultFile)) return resultFile;
		
		GffHashGene gffHashGene = new GffHashGene(gffFileName);
		gffHashGene.writeToFile(gffType, null, resultFile);
		return resultFile;
	}
	
	/** 检查gtf文件的基因坐标是否都落在chrAll.fa的里面
	 * 因为葡萄线粒体的gtf坐标落在了线粒体基因组的外面
	 * 也就是说葡萄线粒体基因nad1 范围 25462--795041
	 * 而线粒体的长度为：773279
	 * 或者gtf文件含有染色体没有的序列
	 */
	public static void checkFile(String gffFile, String chrFile) {
		Map<String, Long> mapChrId2Len = SamIndexRefsequence.generateIndexAndGetMapChrId2Len(chrFile);
		GffHashGene gffHashGene = new GffHashGene(gffFile);
		for (GffDetailGene gffDetailGene : gffHashGene.getGffDetailAll()) {
			Long chrLen = mapChrId2Len.get(gffDetailGene.getRefID().toLowerCase());
			if (chrLen == null) {
//				throw new ExceptionGFF("chromosome file error: " + gffDetailGene.getRefID() + " chrFile doesn't contain this chrId");
				continue;
			}
			if (gffDetailGene.getStartAbs() <= 0 || gffDetailGene.getEndAbs() > chrLen) {
				throw new ExceptionNbcGFF("gff or chromosome file error: " 
						+ gffDetailGene.getRefID() + " " + gffDetailGene.getNameSingle() + " " + gffDetailGene.getStartAbs() + " " + gffDetailGene.getEndAbs() 
						+ " out of chr Range: " + gffDetailGene.getRefID() + " " + chrLen);
			}
		}
	
	}
}
