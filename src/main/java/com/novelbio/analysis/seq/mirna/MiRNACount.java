package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFastaHash;
import com.novelbio.analysis.seq.genomeNew.gffOperate.ListDetailBin;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ����ÿ��miRNA�ı��
 * @author zong0jie
 *
 */
public class MiRNACount {
	Logger logger = Logger.getLogger(MiRNACount.class);
	
	/** ���miRNA��λ��Ϣ */
	ListMiRNALocation tmpMiRNALocation = new ListMiRNALocation();
	/** miRNAǰ�� */
	SeqFastaHash seqFastaHashPreMiRNA = null;
	/** miRNA������ */
	SeqFastaHash seqFastaHashMatureMiRNA = null;
	/** �ȶԵ�bed�ļ� */
	BedSeq bedSeqMiRNA = null;
	/**
	 * �趨miRNA��ǰ�����кͳ�������
	 * @param hairpairMirna
	 * @param matureMirna
	 */
	public void setMiRNAfile(String hairpairMirna, String matureMirna) {
		seqFastaHashMatureMiRNA = new SeqFastaHash(matureMirna);
		seqFastaHashPreMiRNA = new SeqFastaHash(hairpairMirna);
	}
	/**
	 * ����miRNA�ļ���������
	 * @param fileType ��ȡ����miReap���ļ�����RNA.dat ListMiRNALocation.TYPE_RNA_DATA �� ListMiRNALocation.TYPE_MIREAP
	 * @param Species ΪmiRNA.dat�е�������������ļ�����miRNA.dat���ǾͲ���д��
	 * @param rnadatFile
	 */
	public void setMiRNAinfo(int fileType, int taxID, String rnadatFile) {
		tmpMiRNALocation.setSpecies(taxID);
		tmpMiRNALocation.setReadFileType(fileType);
		tmpMiRNALocation.ReadGffarray(rnadatFile);
	}
	/** �趨��Ҫ������ֵ��bed�ļ� */
	public void setBedSeqMiRNA(String bedFile) {
		bedSeqMiRNA = new BedSeq(bedFile);
	}
	/**
	 * ������
	 * key: mirName
	 * value: mirMatureList
	 */
	HashMap<String, ArrayList<String[]>> hashMiRNAmatureValue = new HashMap<String, ArrayList<String[]>>();
	/** ǰ�� */
	HashMap<String, Double> hashMiRNAvalue = new HashMap<String, Double>();
	/**
	 * ����ļ�ǰ׺������miRNA�ļ���
	 * @param outFilePrefix
	 */
	public void outResult(String outFilePrefix) {
		countMiRNA();
		String outMirValue = FileOperate.changeFileSuffix(outFilePrefix, "_MirValue", null);
		String outMirMatureValue = FileOperate.changeFileSuffix(outFilePrefix, "_MirMatureValue", null);
		TxtReadandWrite txtMirValue = new TxtReadandWrite(outMirValue, true);
		TxtReadandWrite txtMirMatureValue = new TxtReadandWrite(outMirMatureValue, true);
		for (Entry<String, Double> entry : hashMiRNAvalue.entrySet()) {
//			ListDetailBin lsMiRNA = tmpMiRNALocation.searchLOC(entry.getKey());
			txtMirValue.writefileln(entry.getKey() + "\t" + entry.getValue() + "\t" + seqFastaHashPreMiRNA.getSeqFasta(entry.getKey() ));
		}
		for (Entry<String, ArrayList<String[]>> entry : hashMiRNAmatureValue.entrySet()) {
			ArrayList<String[]> lsvalue = entry.getValue();
			for (String[] strings : lsvalue) {
				txtMirMatureValue.writefileln(entry.getKey() + "\t" + strings[0] + "\t" + strings[1] + "\t"+ getSeq(entry.getKey(), strings[0]));
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
		if (seqFastaHashMatureMiRNA.getSeqFasta(matureID) != null) {
			return seqFastaHashMatureMiRNA.getSeqFasta(matureID).toString();
		}
		ListDetailBin listDetailBin = tmpMiRNALocation.searchLOC(matureID);
		if (listDetailBin == null) {
			ListBin<ListDetailBin > lsInfo = tmpMiRNALocation.getChrhash().get(matureID);
			if (lsInfo != null) {
				listDetailBin = tmpMiRNALocation.getChrhash().get(matureID).get(0);
			}
			else {
				logger.error("����δ֪ID��" + mirID + " "  + matureID);
				return null;
			}
		}
		SeqFasta seqFasta = seqFastaHashPreMiRNA.getSeq(mirID, listDetailBin.getStartAbs(), listDetailBin.getEndAbs());
		System.out.println(matureID);
		if (listDetailBin.getStartAbs() > 40) {
			return seqFasta.reservecom().toString();
		}
		return seqFasta.toString();
	}
	/**
	 * ����ν�Ų�����
	 *��ȡbed�ļ���Ȼ����mirDat�в�����Ϣ����ȷ������
	 * @param outTxt
	 */
	public void countMiRNA() {
		for (BedRecord bedRecord : bedSeqMiRNA.readlines()) {
			String subName = tmpMiRNALocation.searchMirName(bedRecord.getRefID(), bedRecord.getStart(), bedRecord.getEnd());
			if (subName == null) {
				subName = bedRecord.getRefID() + "_pre";
			}
			double value = (double)1/bedRecord.getMappingNum();
			addMiRNACount(bedRecord.getRefID(), value);
			addMiRNAMatureCount(bedRecord.getRefID(), subName, value);
		}
	}
	/**
	 * ����miRNA�����֣���ֵ���ۼ�����
	 * @param miRNAname
	 * @param value
	 */
	private void addMiRNACount(String miRNAname, double value) {
		if (hashMiRNAvalue.containsKey(miRNAname)) {
			double tmpValue = hashMiRNAvalue.get(miRNAname);
			hashMiRNAvalue.put(miRNAname, value+tmpValue);
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
		if (hashMiRNAmatureValue.containsKey(miRNAname)) {
			//��þ������miRNA����Ϣ
			ArrayList<String[]> lsTmpResult = hashMiRNAmatureValue.get(miRNAname);
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
			hashMiRNAmatureValue.put(miRNAname, lsTmpResult);
		}
	}
}
