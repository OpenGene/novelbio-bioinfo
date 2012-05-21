package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.xb.xsdschema.impl.ExplicitGroupImpl;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListHashBin;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.generalConf.NovelBioConst;

/**
 * ���������ڴ�
 * �о�λ������λ������
 * Ʃ���ȡһ�������miRNA��һ������ļ׻�����Ȼ���о�����������
 * x�᣺�������ratio
 * y�᣺������ļ׻�������sicer-dif���
 * @author zong0jie
 *
 */
public class DifLoc2DifLoc {
	Logger logger = Logger.getLogger(DifLoc2DifLoc.class);
	public static void main2(String[] args) {
		
		String bedSrna1 = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_3N/bgi_3N.bed";
		String bedSrna2 = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_2N/bgi_2N.bed";
		
		
		BedSeq bedSeq = new BedSeq(bedSrna1);
		bedSeq = bedSeq.extend(240);
		bedSeq.sortBedFile();
		
//		 bedSeq = new BedSeq(bedSrna2);
//		 bedSeq = bedSeq.extend(240);
//		 bedSeq.sortBedFile();
	}
	public static void main(String[] args) {
		String gffFile = NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ;
		String bedSrna1 = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_N/bgi_N_extend_sorted.bed";
		String bedSrna2 = "/media/winE/NBC/Project/Project_ZHY_Lab/miRNA/result_advance_3N/bgi_3N_extend_sorted.bed";
		String bedMethy1 = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/Nextend_sort.bed";
		String bedMethy2 = "/media/winE/NBC/Project/Project_ZHY_Lab/MeDIP-Seq_20110506/RawData_and_AlignmentResult/mappingFile/3Nextend_sort.bed";
		String outFile = "/media/winE/NBC/Project/Project_ZHY_Lab/sRNAvsMethylation/N-3N.txt";
		DifLoc2DifLoc exp2Location = new DifLoc2DifLoc();
		exp2Location.setGffFile(gffFile);
		exp2Location.addMapInfo("sRNA", bedSrna1, bedSrna2);
		exp2Location.addMapInfo("methy", bedMethy1, bedMethy2);
		exp2Location.readDifExpGene(typeTss, "sRNA", "methy", FileOperate.changeFileSuffix(outFile, "_tss", null));

		exp2Location.readDifExpGene(typeGeneAll, "sRNA", "methy", FileOperate.changeFileSuffix(outFile, "_geneAll", null));

		exp2Location.readDifExpGene(typeGeneBody, "sRNA", "methy", FileOperate.changeFileSuffix(outFile, "_geneBody", null));
	}
	/** ����2K */
	int tssRegion = 2000;
	GffHashGene gffHashGene = new GffHashGene();
	String chrLenFile = "/media/winE/Bioinformatics/GenomeData/Rice/TIGRRice/ChromFa_chrLen.list";
	int binNum = 20;
	LinkedHashMap<String, ArrayList<MapReads>> hashMapReads = new LinkedHashMap<String, ArrayList<MapReads>>();
	static int typeTss = 2;
	static int typeGeneAll = 4;
	static int typeGeneBody = 8;
	/**
	 * �趨Ⱦɫ�峤���ļ�
	 * @param chrLenFile
	 */
	public void setChrLenFile(String chrLenFile) {
		this.chrLenFile = chrLenFile;
	}
	/**
	 * Ĭ��gff��ucsc��gff�ļ�
	 * @param gffFile
	 */
	public void setGffFile( String gffFile) {
		gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, gffFile);
	}
	/**
	 * ����˳����룬����
	 * @param prefix
	 * @param mapFile1
	 * @param mapFile2
	 */
	public void addMapInfo(String prefix, String mapFile1, String mapFile2) {
		MapReads mapReads1 = new MapReads(chrLenFile, binNum, mapFile1);
		mapReads1.ReadMapFile();
		MapReads mapReads2 = new MapReads(chrLenFile, binNum, mapFile2);
		mapReads2.ReadMapFile();
		ArrayList<MapReads> lsMapReads = new ArrayList<MapReads>();
		lsMapReads.add(mapReads1);
		lsMapReads.add(mapReads2);
		hashMapReads.put(prefix, lsMapReads);
	}
	/**
	 * ȫ�����������
	 * @param type ���ͣ���tss����geneEnd
	 * @param prefix1 ��һ�����ͣ���sRNA
	 * @param prefix2 �ڶ������ͣ���methylation
	 * @param lsGeneID 
	 * @param txtOutInfo
	 */
	public void readDifExpGene(int type, String prefix1, String prefix2,  String txtOutInfo) {
		ArrayList<String> lsGeneID = gffHashGene.getLOCIDList();
		readDifExpGene(type, prefix1, prefix2, lsGeneID, txtOutInfo);
	}
	/**
	 * @param type ���ͣ���tss����geneEnd
	 * @param prefix1 ��һ�����ͣ���sRNA
	 * @param prefix2 �ڶ������ͣ���methylation
	 * @param lsGeneID 
	 * @param txtOutInfo
	 */
	public void readDifExpGene(int type, String prefix1, String prefix2, ArrayList<String> lsGeneID, String txtOutInfo) {
		ArrayList<String[]> lsOutGeneInfo = new ArrayList<String[]>();
		for (String strings : lsGeneID) {
			Double tssInfo1 = null,tssInfo2 = null;
			if (type == typeTss) {
				tssInfo1 = getGeneTssMap(prefix1, strings);
				tssInfo2 = getGeneTssMap(prefix2, strings);
			}
			else if (type == typeGeneBody) {
				tssInfo1 = getGeneBodyMap(prefix1, strings);
				tssInfo2 = getGeneBodyMap(prefix2, strings);
			}
			else if (type == typeGeneAll) {
				tssInfo1 = getGeneAll(prefix1, strings);
				tssInfo2 = getGeneAll(prefix2, strings);
			}
			if (tssInfo1 != null && tssInfo2 != null) {
				String[] strTss = new String[]{strings, tssInfo1 + "", tssInfo2 + ""};
				lsOutGeneInfo.add(strTss);
			}
		}
		TxtReadandWrite txtInfo = new TxtReadandWrite(txtOutInfo, true);
		txtInfo.ExcelWrite(lsOutGeneInfo, "\t", 1, 1);
		txtInfo.close();
	}

	/**
	 * �������򣬻�øû���tss����sicer-dif�ķ���
	 * @param geneID
	 * @return ���а����û���tss����׻�����ֵ
	 */
	private Double getGeneBodyMap(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID());
		int start = 0, end = 0;
		if (gffGeneIsoInfo.isCis5to3()) {
			start = gffGeneIsoInfo.getTSSsite() + tssRegion;
		}
		else {
			start = gffGeneIsoInfo.getTSSsite() - tssRegion;
		}
		end = gffGeneIsoInfo.getTESsite();
		mapInfo.setStartEndLoc(start, end);
		ArrayList<MapReads> lsMapReads = hashMapReads.get(prefix);
		lsMapReads.get(0).getRegion(mapInfo, 20, 0);
		Double score1 = mapInfo.getMean();
		if (score1 == null) {
			return null;
		}
		lsMapReads.get(1).getRegion(mapInfo, 20, 0);
		double score2 = mapInfo.getMean();
		return (score1 + 1)/(score2 + 1);
	}
	
	/**
	 * �������򣬻�øû���tss����sicer-dif�ķ���
	 * @param geneID
	 * @return ���а����û���tss����׻�����ֵ
	 */
	private Double getGeneTssMap(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID(),gffGeneIsoInfo.getTSSsite() - tssRegion, gffGeneIsoInfo.getTSSsite() + tssRegion);
		ArrayList<MapReads> lsMapReads = hashMapReads.get(prefix);
		lsMapReads.get(0).getRegion(mapInfo, 20, 0);
		if (mapInfo.getDouble() == null) {
			logger.error("������δ֪ID��" + mapInfo.getRefID() + " " + mapInfo.getStart() + " " + mapInfo.getEnd());
			return null;
		}
		double score1 = mapInfo.getMean();
		lsMapReads.get(1).getRegion(mapInfo, 20, 0);
		double score2 = mapInfo.getMean();
		return (score1 + 0.01)/(score2 + 0.01);
	}
	
	/**
	 * �������򣬻�øû���ȫ������ķ���
	 * @param geneID
	 * @return ���а����û���tss����׻�����ֵ
	 */
	private Double getGeneAll(String prefix, String geneID) {
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		if (gffGeneIsoInfo == null) {
			return null;
		}
		MapInfo mapInfo = new MapInfo(gffGeneIsoInfo.getChrID(),gffGeneIsoInfo.getTSSsite(), gffGeneIsoInfo.getTESsite());
		ArrayList<MapReads> lsMapReads = hashMapReads.get(prefix);
		lsMapReads.get(0).getRegion(mapInfo, 20, 0);
		Double score1 = mapInfo.getMean();
		if (score1 == null) {
			return null;
		}
		lsMapReads.get(1).getRegion(mapInfo, 20, 0);
		Double score2 = mapInfo.getMean();
		return (score1 + 1)/(score2 + 1);
	}
}
