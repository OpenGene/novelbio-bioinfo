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
		ListMiRNALocation listMiRNALocation = new ListMiRNALocation();
		listMiRNALocation.setSpecies(new Species(10090));
		listMiRNALocation.setReadFileType(ListMiRNALocation.TYPE_RNA_DATA);
		listMiRNALocation.ReadGffarray("/media/winE/Bioinformatics/genome/sRNA/miRNA.dat");
 
		System.out.println(listMiRNALocation.searchMirName("mmu-mir-16-2", 50, 65));
	
		System.out.println("ok");
	}
	private static Logger logger = Logger.getLogger(MiRNACount.class);
	
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
	HashMap<String, ArrayList<String[]>> mapMiRNAname2LsMatureName_Value;
	/**
	 * ������, ���ڽ����
	 */
	HashMap<String, Double> mapMirMature2Value;
	/** ǰ�� */
	HashMap<String, Double> mapMiRNApre2Value;
	
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
		run();
		String outMirValue = outFilePrefix + "MirValue";
		String outMirMatureValue = outFilePrefix + "MirMatureValue";
		
		System.out.println(outMirValue);
		System.out.println(outMirValue);
		
		TxtReadandWrite txtMirValue = new TxtReadandWrite(outMirValue, true);
		TxtReadandWrite txtMirMatureValue = new TxtReadandWrite(outMirMatureValue, true);
		for (Entry<String, Double> entry : mapMiRNApre2Value.entrySet()) {
			txtMirValue.writefileln(entry.getKey() + "\t" + entry.getValue().intValue() + "\t" + seqFastaHashPreMiRNA.getSeqFasta(entry.getKey() ));
		}
		for (Entry<String, ArrayList<String[]>> entry : mapMiRNAname2LsMatureName_Value.entrySet()) {
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
	protected String getSeq(String mirID, String matureID) {
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
		countMiRNA();
	}
	/**
	 * ����ν�Ų�����
	 *��ȡbed�ļ���Ȼ����mirDat�в�����Ϣ����ȷ������
	 * @param outTxt
	 */
	private void countMiRNA() {
		if (countMiRNA)
			return;
		
		mapMiRNAname2LsMatureName_Value = new HashMap<String, ArrayList<String[]>>();
		mapMiRNApre2Value = new HashMap<String, Double>();
		mapMirMature2Value = new HashMap<String, Double>();
		
		countMiRNA = true;
		
		int countLoop = 0;
		for (BedRecord bedRecord : bedSeqMiRNA.readLines()) {
			copeBedRecordAndFillMap(bedRecord);
			
			suspendCheck();
			if (flagStop) break;
			countLoop++;
//			if (countLoop % 1000 == 0) {
//				MiRNAcountProcess miRNAcountProcess = new MiRNAcountProcess();
//				miRNAcountProcess.setReadsNum(countLoop);
//				if (runGetInfo != null) {
//					runGetInfo.setRunningInfo(miRNAcountProcess);
//				}
//			}
		}
		for (Entry<String, ArrayList<String[]>> entry : mapMiRNAname2LsMatureName_Value.entrySet()) {
			ArrayList<String[]> lsvalue = entry.getValue();
			for (String[] strings : lsvalue) {
				if (getSeq(entry.getKey(), strings[0]) == null) {
					continue;
				}
				double countNum = Double.parseDouble(strings[1]);
				mapMirMature2Value.put(entry.getKey() + SepSign.SEP_ID + strings[0], countNum);
			}
		}
	}
	/** һ��һ�д���
	 * �����hashmap
	 *  */
	private void copeBedRecordAndFillMap(BedRecord bedRecord) {
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
	 * @param thisMiRNAcount ������Ҫ�ۼƵ�miRNAcount����Ϊһ��reads����mapping�����miRNA����ôÿ��miRNA��������Ϊ1/count
	 */
	private void addMiRNACount(String miRNAname, double thisMiRNAcount) {
		if (mapMiRNApre2Value.containsKey(miRNAname)) {
			double tmpValue = mapMiRNApre2Value.get(miRNAname);
			mapMiRNApre2Value.put(miRNAname, thisMiRNAcount + tmpValue);
		} else {
			mapMiRNApre2Value.put(miRNAname, thisMiRNAcount);
		}
	}
	/**
	 * ����miRNA�����֣���ֵ���ۼ�����
	 * @param miRNAname miRNA������
	 * @param miRNADetailname miRNA�����������
	 * @param thisMiRNAcount ������Ҫ�ۼƵ�miRNAcount����Ϊһ��reads����mapping�����miRNA����ôÿ��miRNA��������Ϊ1/count
	 */
	private void addMiRNAMatureCount(String miRNAname,String miRNADetailname, double thisMiRNAcount) {
		if (mapMiRNAname2LsMatureName_Value.containsKey(miRNAname)) {
			//��þ������miRNA����Ϣ
			ArrayList<String[]> lsTmpResult = mapMiRNAname2LsMatureName_Value.get(miRNAname);
			for (String[] strings : lsTmpResult) {
				if (strings[0].equals(miRNADetailname)) {
					//�ۼӱ����ֵ�����������
					strings[1] = (Double.parseDouble(strings[1]) + thisMiRNAcount) + "";
					return;
				}
			}
			//���û������˵���ǵ�һ���ҵ���miRNA
			lsTmpResult.add(new String[]{miRNADetailname, thisMiRNAcount+""});
		}
		else {
			ArrayList<String[]> lsTmpResult = new ArrayList<String[]>();
			lsTmpResult.add(new String[]{miRNADetailname, thisMiRNAcount + ""});
			mapMiRNAname2LsMatureName_Value.put(miRNAname, lsTmpResult);
		}
	}
	
	public HashMap<String, Double> getMapMirMature2Value() {
		return mapMirMature2Value;
	}
	public HashMap<String, Double> getMapMiRNApre2Value() {
		return mapMiRNApre2Value;
	}
	
	/** �������ļ���miRNA��ֵ�ϲ����� */
	public ArrayList<String[]> combMapMir2Value(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNA2Value) {
		CombMapMirPre2Value combMapMirPre2Value = new CombMapMirPre2Value(seqFastaHashPreMiRNA);
		return combMapMirPre2Value.combValue(mapPrefix2_mapMiRNA2Value);
	}
	/** �������ļ���miRNA��ֵ�ϲ����� */
	public ArrayList<String[]> combMapMir2MatureValue(HashMap<String, HashMap<String, Double>> mapPrefix2_mapMiRNAMature2Value) {
		CombMapMirMature2Value combMapMirMature2Value = new CombMapMirMature2Value(this);
		return combMapMirMature2Value.combValue(mapPrefix2_mapMiRNAMature2Value);
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

class CombMapMirPre2Value extends MirCombMapGetValueAbs {
	SeqFastaHash seqFastaHashPreMiRNA;
	CombMapMirPre2Value(SeqFastaHash seqFastaHashPreMiRNA) {
		this.seqFastaHashPreMiRNA = seqFastaHashPreMiRNA;
	}
	
	@Override
	protected String[] getTitleIDAndInfo() {	/** �����漰��������miRNA������ */
		String[] titleStart = new String[2];
		titleStart[0] = TitleFormatNBC.miRNApreName.toString();
		titleStart[1] = TitleFormatNBC.mirPreSequence.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		lsTmpResult.add(id);
		lsTmpResult.add(seqFastaHashPreMiRNA.getSeqFasta(id).toString());
	}
}

class CombMapMirMature2Value extends MirCombMapGetValueAbs {
	MiRNACount miRNACount;
	CombMapMirMature2Value(MiRNACount miRNACount) {
		this.miRNACount = miRNACount;
	}
	
	@Override
	protected String[] getTitleIDAndInfo() {	/** �����漰��������miRNA������ */
		String[] titleStart = new String[3];
		titleStart[0] = TitleFormatNBC.miRNApreName.toString();
		titleStart[1] = TitleFormatNBC.miRNAName.toString();
		titleStart[2] = TitleFormatNBC.mirSequence.toString();
		return titleStart;
	}

	@Override
	protected void fillMataInfo(String id, ArrayList<String> lsTmpResult) {
		String[] seqName = id.split(SepSign.SEP_ID);
		lsTmpResult.add(seqName[0]);
		lsTmpResult.add(seqName[1]);
		lsTmpResult.add(miRNACount.getSeq(seqName[0], seqName[1]));
	}
}