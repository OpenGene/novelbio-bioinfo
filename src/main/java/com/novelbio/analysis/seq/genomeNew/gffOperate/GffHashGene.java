package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.catalina.filters.AddDefaultCharsetFilter;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.generalConf.Species;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.CompSubArrayCluster;
import com.novelbio.base.dataStructure.CompSubArrayInfo;
import com.novelbio.database.model.modcopeid.CopedID;

public class GffHashGene implements	GffHashGeneInf, GffHashInf<GffDetailGene, GffCodGene,GffCodGeneDU>{
	
	public static void main(String[] args) {
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, 
				"/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/cufflinkAlla15m1bf/new/novelbioModify_a15m1bf_All_highAll20111220.GTF");
		gffHashGene.writeToGFFIso("/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/cufflinkAlla15m1bf/novelbioModify_a15m1bf_All_high60MISO20111220.GFF3", "novelbio");
		
		
	}
	
	
	
	GffHashGeneAbs gffHashGene = null;
	public GffHashGene(String GffType, String gffFile)
	{
		if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_UCSC)) {
			gffHashGene = new GffHashGeneUCSC();
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_TIGR) ) {
			gffHashGene = new GffHashGenePlant(Species.RICE);
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_TAIR)) {
			gffHashGene = new GffHashGenePlant(Species.ARABIDOPSIS);
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF)) {
			gffHashGene = new GffHashCufflinkGTF();
		}
		gffHashGene.ReadGffarray(gffFile);
	}
	/**
	 * 反方向的转录本，exon是不是从大到小的排列
	 * @param GffType
	 * @param gffFile
	 * @param transExonBig2Small
	 */
	public GffHashGene(String GffType, String gffFile, boolean transExonBig2Small)
	{
		if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_UCSC)) {
			gffHashGene = new GffHashGeneUCSC();
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_TIGR) ) {
			gffHashGene = new GffHashGenePlant(Species.RICE);
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_TAIR)) {
			gffHashGene = new GffHashGenePlant(Species.ARABIDOPSIS);
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF)) {
			gffHashGene = new GffHashCufflinkGTF();
		}
		gffHashGene.setExonTrans(transExonBig2Small);
		gffHashGene.ReadGffarray(gffFile);
	}
	/**
	 * 只设定参数，不读取
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
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_TAIR)) {
			gffHashGene = new GffHashGenePlant(Species.ARABIDOPSIS);
		}
		else if (GffType.equals(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF)) {
			gffHashGene = new GffHashCufflinkGTF();
		}
	}
	public void setTaxID(int taxID)
	{
		gffHashGene.setTaxID(taxID);
	}
	/**
	 * 读取信息
	 * @param gffFile
	 */
	public void readGffFile(String gffFile)
	{
		gffHashGene.ReadGffarray(gffFile);
	}
	/**
	 * 专门给冯英的项目用的，设定ref的Gffinfo
	 */
	public void setGffHash(GffHashGene gffHashRef)
	{
		GffHashCufflinkGTF gff = (GffHashCufflinkGTF)gffHashGene;
		gff.setGffHashRef(gffHashRef);
	}
	
	public GffHashGene()
	{
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

	@Override
	public ArrayList<String> getLOCIDList() {
		return gffHashGene.getLOCIDList();
	}

	@Override
	public ArrayList<String> getLOCChrHashIDList() {
		return gffHashGene.getLOCChrHashIDList();
	}

	@Override
	public String[] getLOCNum(String LOCID) {
		return gffHashGene.getLOCNum(LOCID);
	}

	@Override
	public void setStartRegion(boolean region) {
		gffHashGene.setStartRegion(region);
	}

	@Override
	public HashMap<String, GffDetailGene> getLocHashtable() {
		return gffHashGene.getLocHashtable();
	}

	@Override
	public GffCodGene searchLocation(String chrID, int Coordinate) {
		return gffHashGene.searchLocation(chrID, Coordinate);
	}
	@Override
	public GffGeneIsoInfo searchISO(String LOCID) {
		return gffHashGene.searchISO(LOCID);
	}
	@Override
	public GffDetailGene searchLOC(String LOCID) {
		LOCID = CopedID.removeDot(LOCID);
		return gffHashGene.searchLOC(LOCID);
	}
	@Override
	public GffDetailGene searchLOC(CopedID copedID) {
		return gffHashGene.searchLOC(copedID);
	}
	@Override
	public GffDetailGene searchLOC(String chrID, int LOCNum) {
		return gffHashGene.searchLOC(chrID, LOCNum);
	}
	@Override
	public GffCodGeneDU searchLocation(String chrID, int cod1, int cod2) {
		return gffHashGene.searchLocation(chrID, cod1, cod2);
	}

	@Override
	public String getGffFilename() {
		return gffHashGene.getGffFilename();
	}
	@Override
	public int getTaxID() {
		
		return gffHashGene.getTaxID();
	}
	
	public  HashMap<String, ArrayList<GffDetailGene>> getChrhash()
	{
		return gffHashGene.getChrhash();
	}
	
 	/**
	 * 反方向的转录本，exon是不是从大到小的排列
	 * 仅在CufflinkGTF中使用
	 */
	public void setExonTrans(boolean transExonBig2Small)
	{
		gffHashGene.setExonTrans(transExonBig2Small);
	}
	
	/**
	 * 将基因装入GffHash中
	 * @param chrID
	 * @param gffDetailGene
	 */
	public void addGffDetailGene(String chrID, GffDetailGene gffDetailGene) {
		gffHashGene.addGffDetailGene(chrID, gffDetailGene);
	}
	
	
	/**
	 * 重建转录本时用到，比较两个算法的转录本之间的差异
	 * 两个gffHashGene应该是同一个物种
	 * @param gffHashGene 另一个转录本，本方法可逆--另一个调用该方法得到的结果一样
	 * @return
	 */
	public static GffHashGene compHashGene(GffHashGene gffHashThis, GffHashGene gffHashGene, String chrLen, String gffHashGeneBed, int highExpReads)
	{
		
		GffHashGene gffHashGeneResult = new GffHashGene();
		//不是同一个物种就不比了
		if (gffHashGene.getTaxID() != gffHashThis.getTaxID()) {
			return null;
		}
		GffGeneCluster.setHighExpReads(highExpReads);
		GffGeneCluster.setMapReads(chrLen, gffHashGeneBed);
		for (Entry<String, ArrayList<GffDetailGene>> entry : gffHashThis.getChrhash().entrySet()) {
			String chrID = entry.getKey();
			System.out.println(chrID);
			ArrayList<GffDetailGene> lsThisGffDetail = entry.getValue();
			ArrayList<GffDetailGene> lsCmpGffDetail = gffHashGene.getChrhash().get(chrID);
			if (lsCmpGffDetail == null) {
				for (GffDetailGene gffDetailGene : lsThisGffDetail) {
					gffHashGeneResult.addGffDetailGene(chrID, gffDetailGene);
				}
				continue;
			}
			ArrayList<CompSubArrayCluster>  lstmpArrayClusters = ArrayOperate.compLs2(lsThisGffDetail, lsCmpGffDetail,true);
			for (CompSubArrayCluster compSubArrayCluster : lstmpArrayClusters) {
				//比较每一组里面的this和comp的GffDetailGene
				ArrayList<CompSubArrayInfo> lsThis = compSubArrayCluster.getLsCompSubArrayInfosThis();
				ArrayList<GffDetailGene> lsGffGeneThis = new ArrayList<GffDetailGene>();
				for (CompSubArrayInfo compSubArrayInfo : lsThis) {
					GffDetailGene gene =(GffDetailGene)compSubArrayInfo.cmp;
					lsGffGeneThis.add((GffDetailGene)compSubArrayInfo.cmp);
				}
				ArrayList<CompSubArrayInfo> lsComp = compSubArrayCluster.getLsCompSubArrayInfosComp();
				ArrayList<GffDetailGene> lsGffGeneComp = new ArrayList<GffDetailGene>();
				for (CompSubArrayInfo compSubArrayInfo : lsComp) {
					lsGffGeneComp.add((GffDetailGene)compSubArrayInfo.cmp);
				}
				GffGeneCluster gffGeneCluster = new GffGeneCluster(gffHashThis, gffHashGene, lsGffGeneThis, lsGffGeneComp);

				GffDetailGene gffdetail = gffGeneCluster.getCombGffDetail();
				if (gffdetail == null) {
					continue;
				}
				gffHashGeneResult.addGffDetailGene(chrID, gffdetail);
			}
		}
		return gffHashGeneResult;
	}

	@Override
	public void writeToGTF(String GTFfile, String title) {
		gffHashGene.writeToGTF(GTFfile, title);
	}
	
	@Override
	public void writeToGFFIso(String GTFfile, String title) {
		gffHashGene.writeToGFFIso(GTFfile, title);
	}	
		
}
