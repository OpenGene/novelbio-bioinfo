package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;
import com.novelbio.database.model.modcopeid.GeneID;
import com.novelbio.generalConf.NovelBioConst;
/**
 * ��ȡ�󶹵�GFF�ļ������⣬��Ҫ��5UTR��3UTRһ�飬��Ҫ����
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
//		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_GLYMAX)) {
//			gffHashGene = new GffHashGenePlant(NovelBioConst.GENOME_GFF_TYPE_GLYMAX);
//		}
		gffHashGene.ReadGffarray(gffFile);
	}
	
	/**
	 * ֻ�趨����������ȡ
	 * @param GffType
	 * @param gffFile
	 */
	public void setParam(String GffType) {
		if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_UCSC)) {
			gffHashGene = new GffHashGeneUCSC();
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_TIGR) ) {
			gffHashGene = new GffHashGenePlant(Species.RICE);
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_PLANT)) {
			gffHashGene = new GffHashGenePlant(Species.ARABIDOPSIS);
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
	 * ��ȡ��Ϣ
	 * @param gffFile
	 */
	public void readGffFile(String gffFile) {
		gffHashGene.ReadGffarray(gffFile);
	}
	/**
	 * ר�Ÿ���Ӣ����Ŀ�õģ��趨ref��Gffinfo
	 */
	public void setGffHash(GffHashGene gffHashRef) {
		GffHashCufflinkGTF gff = (GffHashCufflinkGTF)gffHashGene;
		gff.setGffHashRef(gffHashRef);
	}
	
	public GffHashGene() {
		gffHashGene = new GffHashGeneUCSC();		 
	}
	@Override
	public ArrayList<Long> getGeneStructureLength(int upBp) {
		return gffHashGene.getGeneStructureLength(upBp);
	}

	@Override
	public void setEndRegion(boolean region) {
		gffHashGene.setEndRegion(region);
	}
	/**
	 * ˳��洢ChrHash�е�ID���������ChrHash��ʵ�ʴ洢��ID���������Item���ص��ģ�����ListAbs.SEP������
	 * ��ô��list�е�Ԫ����split("/")�ָ����locHashtable�Ϳ���ȡ��Ӧ��GffDetail��Ŀǰ��Ҫ��Peak�õ�
	 * ˳���ã����Ի��ĳ��LOC�ڻ����ϵĶ�λ��
	 * ����TigrGene��IDÿ������һ��LOCID��Ҳ����˵TIGR��ID����Ҫ�����и��Ȼ����Ҳû��ϵ
	 */
	public ArrayList<String> getLOCChrHashIDList() {
		return gffHashGene.getLOCChrHashIDList();
	}
	@Override
	public ArrayList<String> getLOCIDList() {
		return gffHashGene.getLOCIDList();
	}

	@Override
	public String[] getLOCNum(String LOCID) {
		return gffHashGene.getLOCNum(LOCID);
	}

	public void setStartRegion(boolean region) {
		gffHashGene.setStartRegion(region);
	}

	public HashMap<String, GffDetailGene> getLocHashtable() {
		return gffHashGene.getLocHashtable();
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
		return ArrayOperate.getArrayListKey(gffHashGene.getChrhash());
	}
	@Override
	public String getGffFilename() {
		return gffHashGene.getGffFilename();
	}
	@Override
	public int getTaxID() {
		return gffHashGene.getTaxID();
	}
	public  HashMap<String, ListGff> getChrhash() {
		return gffHashGene.getChrhash();
	}
	/** �������в��ظ�GffDetailGene */
	public ArrayList<GffDetailGene> getGffDetailAll() {
		return gffHashGene.getGffDetailAll();
	}
	/** �������в��ظ�GffDetailGene */
	public ArrayList<Integer> getLsIntronSortedS2M() {
		return gffHashGene.getLsIntronSortedS2M();
	}
	/**
	 * ������װ��GffHash��
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
	
}
