package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailRepeat;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneCluster;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashRepeat;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.generalConf.NovelBioConst;

public class ReadsOnRepeatGene {
	public static void main(String[] args) {
		ReadsOnRepeatGene readsInfo = new ReadsOnRepeatGene();
		String repeatGffFile = "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/rmsk.txt";
		String geneGffUCSC = "/media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/hg19_refSeqSortUsing.txt";
		String outPath = "/media/winF/NBC/Project/Project_Invitrogen/sRNA/resultRepeat/";
		String prix = "TG";
		String bedFile = "/media/winF/NBC/Project/Project_Invitrogen/sRNA/TG_Genomic.bed";
		readsInfo.readGff(repeatGffFile, geneGffUCSC);
		readsInfo.countReadsInfo(bedFile);
		readsInfo.writeToFileGeneProp(outPath + prix + "_GeneProp.txt");
		readsInfo.writeToFileRepeatFamily(outPath + prix + "_RepeatFamilyProp.txt");
		readsInfo.writeToFileRepeatName(outPath + prix + "_RepeatNameProp.txt");
	}
	
	GffHashRepeat gffHashRepeat = new GffHashRepeat();
	GffHashGene gffHashGene = new GffHashGene();
	HashMap<String, Double> hashRepeatName = new HashMap<String, Double>();
	HashMap<String, Double> hashRepeatFamily = new HashMap<String, Double>();
	HashMap<String, Double> hashGeneInfo = new HashMap<String, Double>();
	/**
	 * 读取repeat文件
	 * @param repeatGffFile
	 */
	public void readGff(String repeatGffFile, String geneGffUCSC) {
		gffHashRepeat.ReadGffarray(repeatGffFile);
		gffHashGene.setParam(NovelBioConst.GENOME_GFF_TYPE_UCSC);
		gffHashGene.readGffFile(geneGffUCSC);
	}
	
	public HashMap<String, Double> getHashRepeatName() {
		return hashRepeatName;
	}
	public HashMap<String, Double> getHashRepeatFamily() {
		return hashRepeatFamily;
	}
	public HashMap<String, Double> getHashGeneInfo() {
		return hashGeneInfo;
	}
	

	

	public void countReadsInfo(String bedFile) {
		BedSeq bedSeq = new BedSeq(bedFile);
		for (BedRecord bedRecord : bedSeq.readlines()) {
			String repeatInfo = searchReadsRepeat(bedRecord.getRefID(), bedRecord.getStart(), bedRecord.getEnd());
			if (repeatInfo != null) {
				addHashRepeat(repeatInfo, bedRecord.getMappingNum());
			}
			int[] geneLocInfo = searchGene(bedRecord.isCis5to3(), bedRecord.getRefID(), bedRecord.getStart(), bedRecord.getEnd());
			addHashGene(geneLocInfo[0], geneLocInfo[1]==1 ,bedRecord.getMappingNum());
		}
	}
	

	public void writeToFileRepeatName(String outFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		ArrayList<String> lsKey = ArrayOperate.getArrayListKey(hashRepeatName);
		Collections.sort(lsKey);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsKey) {
			lsResult.add(new String[]{string, hashRepeatName.get(string).intValue() + ""});
		}
		txtOut.ExcelWrite(lsResult, "\t", 1, 1);
	}
	public void writeToFileRepeatFamily(String outFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		ArrayList<String> lsKey = ArrayOperate.getArrayListKey(hashRepeatFamily);
		Collections.sort(lsKey);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsKey) {
			lsResult.add(new String[]{string, hashRepeatFamily.get(string).intValue() + ""});
		}
		txtOut.ExcelWrite(lsResult, "\t", 1, 1);
	}
	public void writeToFileGeneProp(String outFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		ArrayList<String> lsKey = ArrayOperate.getArrayListKey(hashGeneInfo);
		Collections.sort(lsKey);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsKey) {
			lsResult.add(new String[]{string, hashGeneInfo.get(string).intValue() + ""});
		}
		txtOut.ExcelWrite(lsResult, "\t", 1, 1);
	}
	/**
	 * 将searchReadsRepeat获得的结果导入hashRepeatName和hashRepeatFamily表中
	 */
	private void addHashRepeat(String repeatInfo, int mapNum) {
		//RepeatName\\\RepeatFamily,
		String[] repeat = repeatInfo.split("///");
		if (hashRepeatName.containsKey(repeat[0])) {
			double num = hashRepeatName.get(repeat[0]);
			hashRepeatName.put(repeat[0], (double)1/mapNum + num);
		}
		else {
			hashRepeatName.put(repeat[0], (double)1/mapNum);
		}
		if (hashRepeatFamily.containsKey(repeat[1])) {
			double num = hashRepeatFamily.get(repeat[1]);
			hashRepeatFamily.put(repeat[1], (double)1/mapNum + num);
		}
		else {
			hashRepeatFamily.put(repeat[1], (double)1/mapNum);
		}
		
	}
	/**
	 * 将查找的结果放入hashgene信息
	 * @param geneLocType 坐标在基因中的位置，用gffsearch得到
	 * @param cis 是否与该基因同方向
	 * @param mapNum 非uniq mapping的话，mapping到了多少不同的ref上
	 */
	private void addHashGene(int geneLocType, boolean cis,int mapNum) {
		String key = null;
		if (geneLocType == GffGeneIsoInfo.COD_LOC_EXON) {
			if (cis) {
				key = "Exon";
			}
			else
				key = "Trans Exon";
		}
		else if (geneLocType == GffGeneIsoInfo.COD_LOC_INTRON) {
			key = "Intron";
		}
		else if (geneLocType == GffGeneIsoInfo.COD_LOC_OUT) {
			key = "Intergenic";
		}
		if (hashGeneInfo.containsKey(key)) {
			double num = hashGeneInfo.get(key);
			hashGeneInfo.put(key, (double)1/mapNum + num);
		}
		else {
			hashGeneInfo.put(key, (double)1/mapNum);
		}
	}
	
	/**
	 * 返回该reads所在的repeat的位置
	 * @param chrID
	 * @param start
	 * @param end
	 * @return
	 * RepeatName///RepeatFamily, 根据结果计算repeat的情况
	 */
	private String searchReadsRepeat(String chrID, int start, int end) {
		ListCodAbs<GffDetailRepeat> cod = gffHashRepeat.searchLocation(chrID, (start+ end)/2);
		if (!cod.isInsideLoc()) {
			return null;
		}
		return cod.getGffDetailThis().getRepName() + "///" + cod.getGffDetailThis().getRepFamily();
	}
	
	/**
	 * @param chrID
	 * @param start
	 * @param end
	 * @return
	 * 返回 0: 是处于exon，intron还是outgene
	 * 1: 0: 与基因反方向或不用考虑 1：与基因组同方向
	 * 实际上对于miRNA而言，只有处于外显子中考虑方向才有意义
	 */
	private int[] searchGene(boolean cis5to3, String chrID, int start, int end) {
		GffCodGene gffCodGene = gffHashGene.searchLocation(chrID, (start+ end)/2);
		if (!gffCodGene.isInsideLoc()) {
			int[] result = new int[]{GffGeneIsoInfo.COD_LOC_OUT, 0};
			return result;
		}
		int locationInfo = gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc(gffCodGene.getCoord());
		boolean cisFinal = (gffCodGene.getGffDetailThis().getLongestSplit().isCis5to3() == cis5to3);
		int ori = 0;//方向
		if (cisFinal) {
			ori = 1;
		}
		return new int[]{locationInfo, ori};
	}

}
