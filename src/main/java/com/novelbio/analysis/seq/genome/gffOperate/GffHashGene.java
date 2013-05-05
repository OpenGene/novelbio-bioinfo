package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.modgeneid.GeneID;

/**
 * 
 * 本类加载时不返回信息，只有在结束时才会返回是否成功
 * @author zong0jie
 */
public class GffHashGene extends RunProcess<Integer> implements GffHashGeneInf {
	GffHashGeneAbs gffHashGene = null;
	GffType gffType;
	String gffFile;
	
	/**
	 * 新建一个GffHashGeneUCSC的类，需要readGffFile
	 */
	public GffHashGene() { }
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
		flagFinish = read(gffType, gffFile);
	}
	/**
	 * 读取并初始化，可以用isFinished()来判定是否顺利运行完毕
	 * @param gffFile 根据文件后缀名判断是GFF还是GTF
	 */
	public GffHashGene(String gffFile) {
		String suffix = FileOperate.getFileNameSep(gffFile)[1];
		if (suffix.trim().toLowerCase().equals("gff") || suffix.trim().toLowerCase().equals("gff3")) {
			this.gffType = GffType.NCBI;
		} else if (suffix.trim().toLowerCase().equals("gtf")) {
			this.gffType = GffType.GTF;
		} else {
			this.gffType = GffType.UCSC;
		}
		
		this.gffFile = gffFile;
		flagFinish = read(gffType, gffFile);
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
	
	@Override
	protected void running() {
		flagFinish = read(gffType, gffFile);
	}
	
	private boolean read(GffType gffType, String gffFile) {
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

	public void setTaxID(int taxID) {
		if (taxID > 0) {
			gffHashGene.setTaxID(taxID);
		}
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

	public GffDetailGene searchLOC(String LOCID) {
		LOCID = GeneID.removeDot(LOCID);
		return gffHashGene.searchLOC(LOCID);
	}
	@Override
	public GffDetailGene searchLOC(GeneID copedID) {
		return gffHashGene.searchLOC(copedID);
	}

	public GffDetailGene searchLOC(String chrID, int LOCNum) {
		return gffHashGene.searchLOC(chrID, LOCNum);
	}

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
		return gffHashGene.getTaxID();
	}
	public  HashMap<String, ListGff> getMapChrID2LsGff() {
		return gffHashGene.getMapChrID2LsGff();
	}
	/** 返回所有不重复GffDetailGene */
	public ArrayList<GffDetailGene> getGffDetailAll() {
		return gffHashGene.getGffDetailAll();
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
	public void addGffDetailGene(String chrID, GffDetailGene gffDetailGene) {
		gffHashGene.addGffDetailGene(chrID, gffDetailGene);
	}

	public void writeToGTF(String GTFfile) {
		gffHashGene.writeToGTF(GTFfile, "novelbio");
	}
	@Override
	public void writeToGTF(String GTFfile, String title) {
		gffHashGene.writeToGTF(GTFfile, title);
	}
	
	/**
	 * 该方法待修正
	 */
	@Override
	public void writeToGFFIsoMoreThanOne(String GTFfile, String title) {
		gffHashGene.writeToGFFIsoMoreThanOne(GTFfile, title);
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
					setGeneID.add(GeneID.removeDot(geneIsoInfo.getName()));
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

	
}
