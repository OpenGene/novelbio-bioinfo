package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailRepeat;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashRepeat;
import com.novelbio.analysis.seq.rnaseq.GffGeneCluster;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;
import com.novelbio.generalConf.TitleFormatNBC;
/**
 * bed�ļ���repeat��gene�ϵķֲ���������Ե����趨repeat������gene
 * @author zong0jie
 *
 */
public class ReadsOnRepeatGene {
	GffHashRepeat gffHashRepeat = null;
	GffChrAbs gffChrAbs = null;
	HashMap<String, Double> mapRepeatName2Value;
	HashMap<String, Double> mapRepeatFamily2Value;
	HashMap<String, Double> mapGeneStructure2Value;
	/**
	 * ��ȡrepeat�ļ�
	 * @param repeatGffFile
	 */
	public void setGffGene(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * ��ȡrepeat�ļ������û��repeat�򲻶�ȡ
	 * @param repeatGffFile
	 */
	public void readGffRepeat(String repeatGffFile) {
		if (FileOperate.isFileExistAndBigThanSize(repeatGffFile, 10) && (gffHashRepeat == null || !gffHashRepeat.getGffFilename().equals(repeatGffFile)) ) {
			gffHashRepeat = new GffHashRepeat();
			gffHashRepeat.ReadGffarray(repeatGffFile);
		}
	}
	public void countReadsInfo(String bedFile) {
		mapRepeatName2Value = new HashMap<String, Double>();
		mapRepeatFamily2Value = new HashMap<String, Double>();
		mapGeneStructure2Value = new HashMap<String, Double>();
		
		
		BedSeq bedSeq = new BedSeq(bedFile);
		for (BedRecord bedRecord : bedSeq.readLines()) {
			String repeatInfo = null;
			if (gffHashRepeat != null) {//���û�ж�ȡrepeat�ļ����򷵻�
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
		if (mapRepeatName2Value.size() == 0) {
			return;
		}
		
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		ArrayList<String> lsKey = ArrayOperate.getArrayListKey(mapRepeatName2Value);
		Collections.sort(lsKey);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsKey) {
			lsResult.add(new String[]{string, mapRepeatName2Value.get(string).intValue() + ""});
		}
		txtOut.ExcelWrite(lsResult);
	}
	public void writeToFileRepeatFamily(String outFile) {
		if (mapRepeatFamily2Value.size() == 0) {
			return;
		}
		
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		ArrayList<String> lsKey = ArrayOperate.getArrayListKey(mapRepeatFamily2Value);
		Collections.sort(lsKey);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsKey) {
			lsResult.add(new String[]{string, mapRepeatFamily2Value.get(string).intValue() + ""});
		}
		txtOut.ExcelWrite(lsResult);
	}
	public void writeToFileGeneProp(String outFile) {
		if (mapGeneStructure2Value.size() == 0) {
			return;
		}
		
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		ArrayList<String> lsKey = ArrayOperate.getArrayListKey(mapGeneStructure2Value);
		Collections.sort(lsKey);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsKey) {
			lsResult.add(new String[]{string, mapGeneStructure2Value.get(string).intValue() + ""});
		}
		txtOut.ExcelWrite(lsResult);
	}
	/**
	 * ��searchReadsRepeat��õĽ������hashRepeatName��hashRepeatFamily����
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
	 * �����ҵĽ������hashgene��Ϣ
	 * @param geneLocType �����ڻ����е�λ�ã���gffsearch�õ�
	 * @param cis �Ƿ���û���ͬ����
	 * @param mapNum ��uniq mapping�Ļ���mapping���˶��ٲ�ͬ��ref��
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
		if (mapGeneStructure2Value.containsKey(key)) {
			double num = mapGeneStructure2Value.get(key);
			mapGeneStructure2Value.put(key, (double)1/mapNum + num);
		}
		else {
			mapGeneStructure2Value.put(key, (double)1/mapNum);
		}
	}
	/**
	 * ���ظ�reads���ڵ�repeat��λ��
	 * @param chrID
	 * @param start
	 * @param end
	 * @return
	 * RepeatName///RepeatFamily, ���ݽ������repeat�����
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
	 * ���� 0: �Ǵ���exon��intron����outgene
	 * 1: 0: ����򷴷�����ÿ��� 1���������ͬ����
	 * ʵ���϶���miRNA���ԣ�ֻ�д����������п��Ƿ����������
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
		int ori = 0;//����
		if (cisFinal) {
			ori = 1;
		}
		return new int[]{locationInfo, ori};
	}
	
	public HashMap<String, Double> getMapGeneStructure2Value() {
		return mapGeneStructure2Value;
	}
	public HashMap<String, Double> getMapRepeatFamily2Value() {
		return mapRepeatFamily2Value;
	}
	public HashMap<String, Double> getMapRepeatName2Value() {
		return mapRepeatName2Value;
	}
	
	/** �������ļ���MapGeneStructure2Value��ֵ�ϲ����� */
	public ArrayList<String[]> combMapGeneStructure2Value(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNA2Value) {
		CombMapGeneInfo combMapGeneInfo = new CombMapGeneInfo();
		return combMapGeneInfo.combValue(mapPrefix2_mapMiRNA2Value);
	}
	/** �������ļ���MapRepatName��ֵ�ϲ����� */
	public ArrayList<String[]> combMapRepatName(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNAMature2Value) {
		CombMapRepeatName combMapRepeatName = new CombMapRepeatName();
		return combMapRepeatName.combValue(mapPrefix2_mapMiRNAMature2Value);
	}
	/** �������ļ���MapRepatFamily��ֵ�ϲ����� */
	public ArrayList<String[]> combMapRepatFamily(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNAMature2Value) {
		CombMapRepeatFamily combMapRepeatFamily = new CombMapRepeatFamily();
		return combMapRepeatFamily.combValue(mapPrefix2_mapMiRNAMature2Value);
	}
	
}

class CombMapGeneInfo extends MirCombMapGetValueAbs {
	
	@Override
	protected String[] getTitleIDAndInfo() {	/** �����漰��������miRNA������ */
		String[] titleStart = new String[1];
		titleStart[0] = TitleFormatNBC.GeneStructure.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		lsTmpResult.add(id);
	}
}

class CombMapRepeatName extends MirCombMapGetValueAbs {
	
	@Override
	protected String[] getTitleIDAndInfo() {	/** �����漰��������miRNA������ */
		String[] titleStart = new String[1];
		titleStart[0] = TitleFormatNBC.RepeatName.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		lsTmpResult.add(id);
	}
}

class CombMapRepeatFamily extends MirCombMapGetValueAbs {
	
	@Override
	protected String[] getTitleIDAndInfo() {	/** �����漰��������miRNA������ */
		String[] titleStart = new String[1];
		titleStart[0] = TitleFormatNBC.RepeatFamily.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		lsTmpResult.add(id);
	}
}



