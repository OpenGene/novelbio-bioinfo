package com.novelbio.analysis.seq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
/**
 * ��bam�ļ���bed�ļ�ת��ΪDGE��ֵ
 * @author zong0jie
 *
 */
public class Align2DGEvalue {
	List<AlignSeq> lsAlignSeq;
	List<String> lsTitle;
	String resultFile;
	boolean allTags = true;
	
	HashMap<String, String> mapAccID2GeneID;
	
	public void setSpecies(Species species) {
		mapAccID2GeneID = new HashMap<String, String>();
		if (species != null && species.getTaxID() != 0) {
			String gene2IsoFile = species.getGene2IsoFileFromDB();
			TxtReadandWrite txtGene2Iso = new TxtReadandWrite(gene2IsoFile, false);
			for (String content : txtGene2Iso.readlines()) {
				String[] ss = content.split("\t");
				mapAccID2GeneID.put(ss[1], ss[0]);
			}
		}
	}
	/** �趨sam��bam������bed�ļ� 
	 * ��Щ�ļ��������Ź����
	 * */
	public void setLsAlignSeq(List<AlignSeq> lsAlignSeq, List<String> lsTitle, String resultFile) {
		this.lsAlignSeq = lsAlignSeq;
		this.lsTitle = lsTitle;
		this.resultFile = resultFile;
	}
	
	/** ��������ļ����� */
	public void sort() {
		ArrayList<AlignSeq> lsSortedAlignSeq = new ArrayList<AlignSeq>();
		for (AlignSeq alignSeq : lsAlignSeq) {
			lsSortedAlignSeq.add(alignSeq.sort());
		}
		lsAlignSeq = lsSortedAlignSeq;
	}
	/**
	 * һ��������ж��λ����reads���ǣ�����˵DGE������������һ������Ķ��λ����reads����
	 * ��ѡ��ȫ��reads����ѡ����ߵ��reads
	 * @param allTags
	 */
	public void setAllTags(boolean allTags) {
		this.allTags = allTags;
	}
	/**
	 * �޷��趨compressType
	 * ��bed�ļ�ת����DGE�������Ϣ��ֱ�ӿ�����DEseq������
	 * @param result
	 * @param sort 
	 * @param allTags �Ƿ���ȫ��������tag��false�Ļ���ֻѡ����������tag������
	 * @param bedFile
	 */
	public String dgeCal() {
		ArrayList<HashMap<String, Integer>> lsDGEvalue = new ArrayList<HashMap<String,Integer>>();
		for (AlignSeq alignSeq : lsAlignSeq) {
			try { lsDGEvalue.add(getGeneExpress(alignSeq)); } catch (Exception e) { }
		}
		HashMap<String, int[]> hashResult = combineHashDGEvalue(lsDGEvalue);
		TxtReadandWrite txtOut = new TxtReadandWrite(resultFile, true);
		String title = "GeneID";
		for (String string : lsTitle) {
			title = title + "\t"+ string;
		}
		txtOut.writefileln(title);
		for (Entry<String, int[]> entry : hashResult.entrySet()) {
			String loc = entry.getKey(); int[] value = entry.getValue();
			for (int i : value) {
				loc = loc + "\t" + i;
			}
			txtOut.writefileln(loc);
		}
		txtOut.close();
		return resultFile;
	}
	/**
	 * ����һ��hash��key��locID   value��expressValue
	 * �����Ǻϲ���һ��hash��
	 * @param lsDGEvalue
	 * @return
	 */
	private HashMap<String, int[]> combineHashDGEvalue(ArrayList<HashMap<String, Integer>> lsDGEvalue) {
		HashMap<String, int[]> hashValue = new HashMap<String, int[]>();
		for (int i = 0; i < lsDGEvalue.size(); i++) {
			HashMap<String, Integer> hashTmp = lsDGEvalue.get(i);
			for (Entry<String, Integer> entry : hashTmp.entrySet()) {
				String loc = entry.getKey(); int value = entry.getValue();
			
				if (hashValue.containsKey(loc)) {
					int[] tmpvalue = hashValue.get(loc);
					tmpvalue[i] = value;
				}
				else
				{
					int[] tmpvalue = new int[lsDGEvalue.size()];
					tmpvalue[i] = value;
					hashValue.put(loc, tmpvalue);
				}
			}
		}
		return hashValue;
	}
	/**
	 * @param Alltags true: ѡ��ȫ��tag��false��ֻѡ������tag
	 * @return
	 * ����ÿ����������Ӧ�ı�������������tag֮��--���˷���tag�� �� int[1]ֻ��Ϊ�˵�ַ���á�
	 * �����align��������
	 * @throws Exception
	 */
	private HashMap<String, Integer> getGeneExpress(AlignSeq alignSeq) throws Exception {
		HashMap<String, Integer> mapGene2Exp = new HashMap<String, Integer>();
		ArrayList<double[]> lsTmpExpValue = new ArrayList<double[]>();
		double[] tmpCount = new double[]{0};
		lsTmpExpValue.add(tmpCount);
		AlignRecord lastRecord = null;
		
		for (AlignRecord alignRecord : alignSeq.readLines()) {
			//mapping���������ϵģ��Ǽٵ��ź�
			if (!alignRecord.isMapped() || alignRecord.isCis5to3() != null && !alignRecord.isCis5to3()) {
				continue;
			}
			//�����»���
			if (lastRecord != null && !lastRecord.getRefID().equals(alignRecord.getRefID())) {
				addMapGene2Exp(mapGene2Exp, lastRecord.getRefID(), summary(lsTmpExpValue));
				lsTmpExpValue.clear();
				tmpCount = new double[]{0};
				lsTmpExpValue.add(tmpCount);
			}
			else if (lastRecord != null && alignRecord.getStartCis() > lastRecord.getEndCis()) {
				tmpCount = new double[]{0};
				lsTmpExpValue.add(tmpCount);
			}
			lastRecord = alignRecord;
			tmpCount[0] = tmpCount[0] + (double)1/alignRecord.getMappingNum();
		}
		return mapGene2Exp;
	}
	private void addMapGene2Exp(HashMap<String, Integer> mapGene2Exp, String accID, int expValue) {
		String geneID = "";
		if (mapAccID2GeneID != null) {
			geneID = mapAccID2GeneID.get(accID);
			if (geneID == null) {
				geneID = accID;
			}
		}
		if (mapGene2Exp.containsKey(geneID)) {
			int expOld = mapGene2Exp.get(geneID);
			expValue = expOld + expValue;
		}
		mapGene2Exp.put(geneID, expValue);
	}
	private int summary(ArrayList<double[]> lsReads) {
		int result = 0;
		if (allTags) {
			result = sum(lsReads);
		} else {
			result = max(lsReads);
		}
		return result;
	}
	/**
	 * ����int[0] ֻ��0λ����Ϣ
	 * @param lsReads
	 * @return
	 */
	private int max(ArrayList<double[]> lsReads) {
		double max = lsReads.get(0)[0];
		for (double[] is : lsReads) {
			if (is[0] > max) {
				max = is[0];
			}
		}
		return (int)max;
	}
	/**
	 * ����int[0] ֻ��0λ����Ϣ
	 * @param lsReads
	 * @return
	 */
	private int sum(ArrayList<double[]> lsReads) {
		double sum = 0;
		for (double[] is : lsReads) {
			sum = sum + is[0];
		}
		return (int)sum;
	}
}
