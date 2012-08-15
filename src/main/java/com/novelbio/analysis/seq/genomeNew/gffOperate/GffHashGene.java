package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.NovelBioConst;
/**
 * 读取大豆的GFF文件有问题，主要是5UTR和3UTR一块，需要修正
 * @author zong0jie
 *
 */
public class GffHashGene implements GffHashGeneInf{
	
	public static void main(String[] args) {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/cufflinkAlla15m1bf/new/novelbioModify_a15m1bf_All_highAll20111220.GTF");
		gffHashGene.writeToGFFIsoMoreThanOne("/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/cufflinkAlla15m1bf/novelbioModify_a15m1bf_All_high60MISO20111220.GFF3", "novelbio");
	}
	
	GffHashGeneAbs gffHashGene = null;
	public GffHashGene(String GffType, String gffFile) {
		if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_UCSC)) {
			gffHashGene = new GffHashGeneUCSC();
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_TIGR) ) {
			gffHashGene = new GffHashGenePlant(NovelBioConst.GENOME_GFF_TYPE_TIGR);
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_PLANT)) {
			gffHashGene = new GffHashGenePlant(NovelBioConst.GENOME_GFF_TYPE_PLANT);
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF)) {
			gffHashGene = new GffHashCufflinkGTF();
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_NCBI)) {
			gffHashGene = new GffHashGeneNCBI();
		}
		gffHashGene.ReadGffarray(gffFile);
	}
	
	/**
	 * 只设定参数，不读取
	 * @param GffType
	 * @param gffFile
	 */
	public void setGffType(String GffType) {
		if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_UCSC)) {
			gffHashGene = new GffHashGeneUCSC();
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_TIGR) ) {
			gffHashGene = new GffHashGenePlant(NovelBioConst.GENOME_GFF_TYPE_TIGR);
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_PLANT)) {
			gffHashGene = new GffHashGenePlant(NovelBioConst.GENOME_GFF_TYPE_PLANT);
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF)) {
			gffHashGene = new GffHashCufflinkGTF();
		}
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
		GffHashCufflinkGTF gff = (GffHashCufflinkGTF)gffHashGene;
		gff.setGffHashRef(gffHashRef);
	}
	
	public GffHashGene() {
		gffHashGene = new GffHashGeneUCSC();		 
	}
	
	public void removeDuplicateIso() {
		HashMap<String, ListGff> mapChrID2LsGff = getMapChrID2LsGff();
		for (ListGff listGff : mapChrID2LsGff.values()) {
			for (GffDetailGene gffDetailGene : listGff) {
				gffDetailGene.removeDupliIso();
			}
		}
	}
	@Override
	public void setEndRegion(boolean region) {
		gffHashGene.setEndRegion(region);
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

	public void setStartRegion(boolean region) {
		gffHashGene.setStartRegion(region);
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
	@Override
	public void writeToGTF(String GTFfile, String title) {
		gffHashGene.writeToGTF(GTFfile, title);
	}
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
}
