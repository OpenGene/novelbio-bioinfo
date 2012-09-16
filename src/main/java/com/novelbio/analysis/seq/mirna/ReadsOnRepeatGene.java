package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailRepeat;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashRepeat;
import com.novelbio.analysis.seq.rnaseq.GffGeneCluster;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;
/**
 * bed文件在repeat和gene上的分布情况，可以单独设定repeat或者是gene
 * @author zong0jie
 *
 */
public class ReadsOnRepeatGene {
	GffHashRepeat gffHashRepeat = null;
	GffChrAbs gffChrAbs = null;
	HashMap<String, Double> hashRepeatName = new HashMap<String, Double>();
	HashMap<String, Double> hashRepeatFamily = new HashMap<String, Double>();
	HashMap<String, Double> hashGeneInfo = new HashMap<String, Double>();
	/**
	 * 读取repeat文件
	 * @param repeatGffFile
	 */
	public void readGffGene(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 读取repeat文件，如果没有repeat则不读取
	 * @param repeatGffFile
	 */
	public void readGffRepeat(String repeatGffFile) {
		if (FileOperate.isFileExistAndBigThanSize(repeatGffFile, 10)) {
			gffHashRepeat = new GffHashRepeat();
			gffHashRepeat.ReadGffarray(repeatGffFile);
		}
	}
	public void countReadsInfo(String bedFile) {
		BedSeq bedSeq = new BedSeq(bedFile);
		for (BedRecord bedRecord : bedSeq.readLines()) {
			String repeatInfo = null;
			if (gffHashRepeat != null) {//如果没有读取repeat文件，则返回
				repeatInfo = searchReadsRepeat(bedRecord.getRefID(), bedRecord.getStartAbs(), bedRecord.getEndAbs());
				if (repeatInfo != null) {
					addHashRepeat(repeatInfo, bedRecord.getMappingNum());
				}
			}
			if (gffChrAbs != null && gffChrAbs.getGffHashGene() != null) {
				int[] geneLocInfo = searchGene(bedRecord.isCis5to3(), bedRecord.getRefID(), bedRecord.getStartAbs(), bedRecord.getEndAbs());
				if (geneLocInfo != null) {
					addHashGene(geneLocInfo[0], geneLocInfo[1]==1 ,bedRecord.getMappingNum());
				}
				
			}
		}
	}
	

	public void writeToFileRepeatName(String outFile) {
		if (hashRepeatName.size() == 0) {
			return;
		}
		
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		ArrayList<String> lsKey = ArrayOperate.getArrayListKey(hashRepeatName);
		Collections.sort(lsKey);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsKey) {
			lsResult.add(new String[]{string, hashRepeatName.get(string).intValue() + ""});
		}
		txtOut.ExcelWrite(lsResult);
	}
	public void writeToFileRepeatFamily(String outFile) {
		if (hashRepeatFamily.size() == 0) {
			return;
		}
		
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		ArrayList<String> lsKey = ArrayOperate.getArrayListKey(hashRepeatFamily);
		Collections.sort(lsKey);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsKey) {
			lsResult.add(new String[]{string, hashRepeatFamily.get(string).intValue() + ""});
		}
		txtOut.ExcelWrite(lsResult);
	}
	public void writeToFileGeneProp(String outFile) {
		if (hashGeneInfo.size() == 0) {
			return;
		}
		
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		ArrayList<String> lsKey = ArrayOperate.getArrayListKey(hashGeneInfo);
		Collections.sort(lsKey);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsKey) {
			lsResult.add(new String[]{string, hashGeneInfo.get(string).intValue() + ""});
		}
		txtOut.ExcelWrite(lsResult);
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
		if (mapNum == 0) {
			return;
		}
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
		if (cod == null || !cod.isInsideLoc()) {
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
		GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(chrID, (start+ end)/2);
		if (gffCodGene == null) {
			return null;
		}
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
