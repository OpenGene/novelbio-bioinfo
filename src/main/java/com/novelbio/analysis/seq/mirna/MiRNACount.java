package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.omg.CosNaming._BindingIteratorImplBase;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * ����ÿ��miRNA�ı��޷�����ܱ��ֵ��ֻ�ܻ��ÿ�����ֵ
 * @author zong0jie
 *
 */
public class MiRNACount extends RunProcess<MiRNACount.MiRNAcountProcess>{
	public static void main(String[] args) {
		String bedFile = "/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/novelMiRNAbed/_miRNA.bed";
		String hairpairMirna = "/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/novelMiRNA/hairpin.fa";
		String matureMirna = "/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/novelMiRNA/mature.fa";
		String rnadatFile = "/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/run/output.mrd";
		String outFilePrefix = "/home/zong0jie/Desktop/platformtest/output/testmiRNApredictDeep/predict_deep_count";
		
		MiRNACount miRNACount = new MiRNACount();
		miRNACount.setBedSeqMiRNA(bedFile);
		miRNACount.setMiRNAfile(hairpairMirna, matureMirna);
		miRNACount.setMiRNAinfo(ListMiRNALocation.TYPE_MIRDEEP, new Species(9606), rnadatFile);
		miRNACount.writeResultToOut(outFilePrefix);
	}
	Logger logger = Logger.getLogger(MiRNACount.class);
	
	/** ���miRNA��λ��Ϣ */
	ListMiRNALocation listMiRNALocation = new ListMiRNALocation();
	/** miRNAǰ�� */
	SeqFastaHash seqFastaHashPreMiRNA = null;
	/** miRNA������ */
	SeqFastaHash seqFastaHashMatureMiRNA = null;
	/** �ȶԵ�bed�ļ� */
	BedSeq bedSeqMiRNA = null;
	/** Mapping��ǰ�嵫��û������������еĺ�׺ */
	String flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix = "_pre";
	/**
	 * ������
	 * key: mirName
	 * value: mirMatureList
	 */
	HashMap<String, ArrayList<String[]>> hashMiRNAname2LsMatureName_Value = new HashMap<String, ArrayList<String[]>>();
	/**
	 * ������, ���ڽ����
	 */
	HashMap<String, Double> mapMirMaturename2Value = new HashMap<String, Double>();
	/** ǰ�� */
	HashMap<String, Double> hashMiRNAvalue = new HashMap<String, Double>();
	
	boolean countMiRNA = false;
	/**
	 * �趨miRNA��ǰ�����кͳ�������
	 * @param hairpairMirna
	 * @param matureMirna
	 */
	public void setMiRNAfile(String hairpairMirna, String matureMirna) {
		seqFastaHashMatureMiRNA = new SeqFastaHash(matureMirna);
		seqFastaHashPreMiRNA = new SeqFastaHash(hairpairMirna);
		countMiRNA = false;
	}
	/**
	 * ����miRNA�ļ���������
	 * @param fileType ��ȡ����miReap���ļ�����RNA.dat ListMiRNALocation.TYPE_RNA_DATA �� ListMiRNALocation.TYPE_MIREAP
	 * @param Species ΪmiRNA.dat�е�������������ļ�����miRNA.dat���ǾͲ���д��
	 * @param rnadatFile
	 */
	public void setMiRNAinfo(int fileType, Species species, String rnadatFile) {
		listMiRNALocation.setSpecies(species);
		listMiRNALocation.setReadFileType(fileType);
		listMiRNALocation.ReadGffarray(rnadatFile);
		countMiRNA = false;
	}
	/** �趨��Ҫ������ֵ��bed�ļ� */
	public void setBedSeqMiRNA(String bedFile) {
		bedSeqMiRNA = new BedSeq(bedFile);
		countMiRNA = false;
	}

	/**
	 * ����ļ�ǰ׺������miRNA�ļ���
	 * @param outFilePrefix
	 */
	public void writeResultToOut(String outFilePrefix) {
		countMiRNA();
		String outMirValue = outFilePrefix + "MirValue";
		String outMirMatureValue = outFilePrefix + "MirMatureValue";
		
		System.out.println(outMirValue);
		System.out.println(outMirValue);
		
		TxtReadandWrite txtMirValue = new TxtReadandWrite(outMirValue, true);
		TxtReadandWrite txtMirMatureValue = new TxtReadandWrite(outMirMatureValue, true);
		for (Entry<String, Double> entry : hashMiRNAvalue.entrySet()) {
			txtMirValue.writefileln(entry.getKey() + "\t" + entry.getValue().intValue() + "\t" + seqFastaHashPreMiRNA.getSeqFasta(entry.getKey() ));
		}
		for (Entry<String, ArrayList<String[]>> entry : hashMiRNAname2LsMatureName_Value.entrySet()) {
			ArrayList<String[]> lsvalue = entry.getValue();
			for (String[] strings : lsvalue) {
				if (getSeq(entry.getKey(), strings[0]) == null) {
					continue;
				}
				double countNum = Double.parseDouble(strings[1]);
				txtMirMatureValue.writefileln(entry.getKey() + "\t" + strings[0] + "\t" + (int)countNum + "\t"+ getSeq(entry.getKey(), strings[0]));
			}
		}
		txtMirValue.close();
		txtMirMatureValue.close();
	}
	
	/**
	 * ����miRNA���������֣���ǰ���л������
	 * @param ID
	 * @return
	 */
	private String getSeq(String mirID, String matureID) {
		if (seqFastaHashMatureMiRNA.getSeqFasta(matureID.toLowerCase()) != null) {
			return seqFastaHashMatureMiRNA.getSeqFasta(matureID.toLowerCase()).toString();
		}
		ListDetailBin listDetailBin = listMiRNALocation.searchLOC(matureID);
		if (listDetailBin == null) {
			ListBin<ListDetailBin > lsInfo = listMiRNALocation.getMapChrID2LsGff().get(matureID);
			if (lsInfo != null) {
				listDetailBin = listMiRNALocation.getMapChrID2LsGff().get(matureID).get(0);
			}
			else {
				if (!matureID.endsWith(flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix)) {
					logger.error("����δ֪ID��" + mirID + " "  + matureID);
				}
				return null;
			}
		}
		SeqFasta seqFasta = seqFastaHashPreMiRNA.getSeq(mirID.toLowerCase(), listDetailBin.getStartAbs(), listDetailBin.getEndAbs());
		System.out.println(matureID);
		if (listDetailBin.getStartAbs() > 40) {
			return seqFasta.reservecom().toString();
		}
		return seqFasta.toString();
	}
	@Override
	protected void running() {
		hashMiRNAname2LsMatureName_Value.clear();
		hashMiRNAvalue.clear();
		mapMirMaturename2Value.clear();
		countMiRNA();
	}
	/**
	 * ����ν�Ų�����
	 *��ȡbed�ļ���Ȼ����mirDat�в�����Ϣ����ȷ������
	 * @param outTxt
	 */
	public void countMiRNA() {
		if (countMiRNA)
			return;
		countMiRNA = true;
		int countLoop = 0;
		for (BedRecord bedRecord : bedSeqMiRNA.readLines()) {
			copeBedRecord(bedRecord);
			
			suspendCheck();
			if (flagStop) break;
			countLoop++;
			if (countLoop % 500 == 0) {
				MiRNAcountProcess miRNAcountProcess = new MiRNAcountProcess();
				miRNAcountProcess.setReadsNum(countLoop);
				if (runGetInfo != null) {
					runGetInfo.setRunningInfo(miRNAcountProcess);
				}
			}
		}
		for (Entry<String, ArrayList<String[]>> entry : hashMiRNAname2LsMatureName_Value.entrySet()) {
			ArrayList<String[]> lsvalue = entry.getValue();
			for (String[] strings : lsvalue) {
				if (getSeq(entry.getKey(), strings[0]) == null) {
					continue;
				}
				double countNum = Double.parseDouble(strings[1]);
				mapMirMaturename2Value.put(entry.getKey() + SepSign.SEP_ID + strings[0], countNum);
			}
		}
	}
	/** һ��һ�д��� */
	private void copeBedRecord(BedRecord bedRecord) {
		String subName = listMiRNALocation.searchMirName(bedRecord.getRefID(), bedRecord.getStartAbs(), bedRecord.getEndAbs());
		//�Ҳ������ֵ��ں������
		if (subName == null) {
			subName = bedRecord.getRefID() + flag_MapTo_PreMirna_NotTo_MatureMirna_Suffix;
		}
		double value = (double)1/bedRecord.getMappingNum();
		addMiRNACount(bedRecord.getRefID(), value);
		addMiRNAMatureCount(bedRecord.getRefID(), subName, value);
	}
	/**
	 * ����miRNA�����֣���ֵ���ۼ�����
	 * @param miRNAname
	 * @param value
	 */
	private void addMiRNACount(String miRNAname, double value) {
		if (hashMiRNAvalue.containsKey(miRNAname)) {
			double tmpValue = hashMiRNAvalue.get(miRNAname);
			hashMiRNAvalue.put(miRNAname, value + tmpValue);
		}
		else {
			hashMiRNAvalue.put(miRNAname, value);
		}
	}
	/**
	 * ����miRNA�����֣���ֵ���ۼ�����
	 * @param miRNAname miRNA������
	 * @param miRNADetailname miRNA�����������
	 * @param value
	 */
	private void addMiRNAMatureCount(String miRNAname,String miRNADetailname, double value) {
		if (hashMiRNAname2LsMatureName_Value.containsKey(miRNAname)) {
			//��þ������miRNA����Ϣ
			ArrayList<String[]> lsTmpResult = hashMiRNAname2LsMatureName_Value.get(miRNAname);
			for (String[] strings : lsTmpResult) {
				if (strings[0].equals(miRNADetailname)) {
					//�ۼӱ����ֵ�����������
					strings[1] = (Double.parseDouble(strings[1]) + value) + "";
					return;
				}
			}
			//���û������˵���ǵ�һ���ҵ���miRNA
			lsTmpResult.add(new String[]{miRNADetailname, value+""});
		}
		else {
			ArrayList<String[]> lsTmpResult = new ArrayList<String[]>();
			lsTmpResult.add(new String[]{miRNADetailname, value + ""});
			hashMiRNAname2LsMatureName_Value.put(miRNAname, lsTmpResult);
		}
	}
	
	public HashMap<String, Double> getMapMirMaturename2Value() {
		return mapMirMaturename2Value;
	}
	public HashMap<String, Double> getMapMiRNAvalue() {
		return hashMiRNAvalue;
	}
	
	
	/** �������ļ���miRNA��ֵ�ϲ����� */
	public ArrayList<String[]> combMapMir2Value(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNA2Value) {
		CombMapMirPre2Value combMapMirPre2Value = new CombMapMirPre2Value(seqFastaHashPreMiRNA);
		return combMapMirPre2Value.combValue(mapPrefix2_mapMiRNA2Value);
	}
	/** �������ļ���miRNA��ֵ�ϲ����� */
	public ArrayList<String[]> combMapMir2MatureValue(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNAMature2Value) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		String[] title = getTitleMature(mapPrefix2_mapMiRNAMature2Value);
		lsResult.add(title);
		
		HashSet<String> setMirNameAll = getAllMirName(mapPrefix2_mapMiRNAMature2Value);
		
		for (String mirName : setMirNameAll) {
			String[] miRNAinfo = new String[title.length + 2];
			miRNAinfo[0] = mirName;
			for (int i = 2; i < title.length; i++) {
				HashMap<String, Double> mapMirna2Value = mapPrefix2_mapMiRNAMature2Value.get(title[i]);
				Double value = mapMirna2Value.get(mirName);
				if (value == null) {
					miRNAinfo[i] = 0 + "";
				} else {
					miRNAinfo[i] = value.intValue() + "";
				}
			}
			String[] seqName = mirName.split(SepSign.SEP_ID);
			miRNAinfo[miRNAinfo.length - 1] = getSeq(seqName[0], seqName[1]);
			lsResult.add(miRNAinfo);
		}
		return lsResult;
	}

	/** �����漰��������miRNA������ */
	private HashSet<String> getAllMirName(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNA2Value) {
		LinkedHashSet<String> setMirNameAll = new LinkedHashSet<String>();
		for (HashMap<String, Double> mapMiRNA2Value : mapPrefix2_mapMiRNA2Value.values()) {
			for (String miRNAname : mapMiRNA2Value.keySet()) {
				setMirNameAll.add(miRNAname);
			}
		}
		return setMirNameAll;
	}
	/** �����漰��������miRNA������ */
	private String[] getTitlePre(HashMap<String, ? extends Object> mapPrefix2Info) {
		String[] title = new String[mapPrefix2Info.size() + 2];
		title[0] = TitleFormatNBC.miRNApreName.toString();
		int i = 1;
		for (String prefix : mapPrefix2Info.keySet()) {
			title[i] = prefix;
			i ++;
		}
		title[title.length - 1] = TitleFormatNBC.mirPreSequence.toString();
		return title;
	}
	/** �����漰��������miRNA������ */
	private String[] getTitleMature(HashMap<String, ? extends Object> mapPrefix2Info) {
		String[] title = new String[mapPrefix2Info.size() + 3];
		title[0] = TitleFormatNBC.miRNApreName.toString();
		title[1] = TitleFormatNBC.miRNAName.toString();
		int i = 1;
		for (String prefix : mapPrefix2Info.keySet()) {
			title[i] = prefix;
			i ++;
		}
		title[title.length - 1] = TitleFormatNBC.mirSequence.toString();
		return title;
	}
	public static class MiRNAcountProcess {
		long readsNum;
		public void setReadsNum(long readsNum) {
			this.readsNum = readsNum;
		}
		public long getReadsNum() {
			return readsNum;
		}
	}
	

}

class CombMapMirPre2Value extends MirCountGetValueAbs {
	SeqFastaHash seqFastaHashPreMiRNA;
	CombMapMirPre2Value(SeqFastaHash seqFastaHashPreMiRNA) {
		this.seqFastaHashPreMiRNA = seqFastaHashPreMiRNA;
	}
	
	@Override
	protected String[] getTitleIDAndInfo() {	/** �����漰��������miRNA������ */
		String[] titleStart = new String[2];
		titleStart[0] = TitleFormatNBC.miRNApreName.toString();
		titleStart[1] = TitleFormatNBC.mirSequence.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		lsTmpResult.add(id);
		lsTmpResult.add(seqFastaHashPreMiRNA.getSeqFasta(id).toString());
	}
}
