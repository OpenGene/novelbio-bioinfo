package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailRepeat;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashRepeat;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.listOperate.ListCodAbs;
/**
 * bed文件在repeat和gene上的分布情况，可以单独设定repeat或者是gene
 * @author zong0jie
 *
 */
public class ReadsOnRepeatGene implements AlignmentRecorder {
	GffHashRepeat gffHashRepeat = null;
	GffChrAbs gffChrAbs = null;
	Map<String, Double> mapRepeatName2Value;
	Map<String, Double> mapRepeatFamily2Value;
	Map<String, Double> mapGeneStructure2Value;
	/**
	 * 读取repeat文件
	 * @param repeatGffFile
	 */
	public void setGffGene(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 读取repeat文件，如果没有repeat则不读取
	 * @param repeatGffFile
	 */
	public void readGffRepeat(String repeatGffFile) {
		if (FileOperate.isFileExistAndBigThanSize(repeatGffFile, 10) && (gffHashRepeat == null || !gffHashRepeat.getGffFilename().equals(repeatGffFile)) ) {
			gffHashRepeat = new GffHashRepeat();
			gffHashRepeat.ReadGffarray(repeatGffFile);
		}
	}
	
	public List<String> getLsRepeatName() {
		Set<String> setRepeatName = new LinkedHashSet<>();
		for (GffDetailRepeat gffDetailRepeat : gffHashRepeat.getGffDetailAll()) {
			setRepeatName.add(gffDetailRepeat.getRepName());
		}
		return new ArrayList<>(setRepeatName);
	}
	public List<String> getLsRepeatFamily() {
		Set<String> setRepeatFamily = new LinkedHashSet<>();
		for (GffDetailRepeat gffDetailRepeat : gffHashRepeat.getGffDetailAll()) {
			setRepeatFamily.add(gffDetailRepeat.getRepFamily());
		}
		return new ArrayList<>(setRepeatFamily);
	}
	public List<String> getLsGeneStructure() {
		List<String> lsGeneStructure = new ArrayList<>();
		lsGeneStructure.add("Exon");
		lsGeneStructure.add("Trans_Exon");
		lsGeneStructure.add("Intron");
		lsGeneStructure.add("Intergenic");
		return lsGeneStructure;
	}
	
	public void initial() {
		mapRepeatName2Value = new HashMap<String, Double>();
		mapRepeatFamily2Value = new HashMap<String, Double>();
		mapGeneStructure2Value = new HashMap<String, Double>();
	}
	
	public void countReadsInfo(AlignSeq alignSeq) {
		mapRepeatName2Value = new HashMap<String, Double>();
		mapRepeatFamily2Value = new HashMap<String, Double>();
		mapGeneStructure2Value = new HashMap<String, Double>();
		
		for (AlignRecord alignRecord : alignSeq.readLines()) {
			String repeatInfo = null;
			if (gffHashRepeat != null) {//如果没有读取repeat文件，则返回
				repeatInfo = searchReadsRepeat(alignRecord.getRefID(), alignRecord.getStartAbs(), alignRecord.getEndAbs());
				if (repeatInfo != null) {
					addHashRepeat(repeatInfo, alignRecord.getMappedReadsWeight());
				}
			}
			if (gffChrAbs != null && gffChrAbs.getGffHashGene() != null) {
				int[] geneLocInfo = searchGene(alignRecord.isCis5to3(), alignRecord.getRefID(), alignRecord.getStartAbs(), alignRecord.getEndAbs());
				if (geneLocInfo != null) {
					addHashGene(geneLocInfo[0], geneLocInfo[1]==1 ,alignRecord.getMappedReadsWeight());
				}
			}
		}
	}
	
	@Override
	public Align getReadingRegion() {
		return null;
	}
	
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		if (!alignRecord.isMapped()) {
			return;
		}
		String repeatInfo = null;
		if (gffHashRepeat != null) {//如果没有读取repeat文件，则返回
			repeatInfo = searchReadsRepeat(alignRecord.getRefID(), alignRecord.getStartAbs(), alignRecord.getEndAbs());
			if (repeatInfo != null) {
				addHashRepeat(repeatInfo, alignRecord.getMappedReadsWeight());
			}
		}
		if (gffChrAbs != null && gffChrAbs.getGffHashGene() != null) {
			int[] geneLocInfo = searchGene(alignRecord.isCis5to3(), alignRecord.getRefID(), alignRecord.getStartAbs(), alignRecord.getEndAbs());
			if (geneLocInfo != null) {
				addHashGene(geneLocInfo[0], geneLocInfo[1]==1 ,alignRecord.getMappedReadsWeight());
			}
		}
	}
	
	@Override
	public void summary() { }
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
	 * 将searchReadsRepeat获得的结果导入hashRepeatName和hashRepeatFamily表中
	 */
	private void addHashRepeat(String repeatInfo, int mapNum) {
		//RepeatName\\\RepeatFamily,
		String[] repeat = repeatInfo.split("///");
		if (mapRepeatName2Value.containsKey(repeat[0])) {
			double num = mapRepeatName2Value.get(repeat[0]);
			mapRepeatName2Value.put(repeat[0], (double)1/mapNum + num);
		}
		else {
			mapRepeatName2Value.put(repeat[0], (double)1/mapNum);
		}
		if (mapRepeatFamily2Value.containsKey(repeat[1])) {
			double num = mapRepeatFamily2Value.get(repeat[1]);
			mapRepeatFamily2Value.put(repeat[1], (double)1/mapNum + num);
		}
		else {
			mapRepeatFamily2Value.put(repeat[1], (double)1/mapNum);
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
				key = "Trans_Exon";
		} else if (geneLocType == GffGeneIsoInfo.COD_LOC_INTRON) {
			key = "Intron";
		} else if (geneLocType == GffGeneIsoInfo.COD_LOC_OUT) {
			key = "Intergenic";
		}
		
		if (mapGeneStructure2Value.containsKey(key)) {
			double num = mapGeneStructure2Value.get(key);
			mapGeneStructure2Value.put(key, (double)1/mapNum + num);
		} else {
			mapGeneStructure2Value.put(key, (double)1/mapNum);
		}
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
		int locationInfo = gffCodGene.getGffDetailThis().getLongestSplitMrna().getCodLoc(gffCodGene.getCoord());
		boolean cisFinal = (gffCodGene.getGffDetailThis().getLongestSplitMrna().isCis5to3() == cis5to3);
		int ori = 0;//方向
		if (cisFinal) {
			ori = 1;
		}
		return new int[]{locationInfo, ori};
	}
	
	public Map<String, Double> getMapGeneStructure2Value() {
		return mapGeneStructure2Value;
	}
	public Map<String, Double> getMapRepeatFamily2Value() {
		return mapRepeatFamily2Value;
	}
	public Map<String, Double> getMapRepeatName2Value() {
		return mapRepeatName2Value;
	}
	
}
