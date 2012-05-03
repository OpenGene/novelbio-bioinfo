package com.novelbio.analysis.seq.mirna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * ����ÿ��miRNA�ı��
 * @author zong0jie
 *
 */
public class MiRNACount extends BedSeq {
	public static void main(String[] args) {
		String bedFile = "/media/winF/NBC/Project/Project_Invitrogen/sRNA/TG_miRNA.bed";
		String rnadatFile = "/media/winE/Bioinformatics/DataBase/sRNA/miRNA.dat";
		String out = "/media/winF/NBC/Project/Project_Invitrogen/sRNA/result/TG.txt";
		MiRNACount miRNACount = new MiRNACount(bedFile, "HSA", rnadatFile);
		miRNACount.outResult(out);
	}
	
	
	TmpMiRNALocation tmpMiRNALocation = new TmpMiRNALocation();
	/**
	 * ����bed�ļ���������
	 * @param bedFile
	 * @param Species ����ΪHSA
	 */
	public MiRNACount(String bedFile, String Species, String rnadatFile) {
		super(bedFile);
		tmpMiRNALocation.ReadGffarray(rnadatFile);
	}
	
	HashMap<String, ArrayList<String[]>> hashMiRNADetailValue = new HashMap<String, ArrayList<String[]>>();
	HashMap<String, Double> hashMiRNAvalue = new HashMap<String, Double>();
	
	public void outResult(String outFile) {
		countMiRNA();		
		String outMirValue = FileOperate.changeFileSuffix(outFile, "_MirValue", null);
		String outMirDetailValue = FileOperate.changeFileSuffix(outFile, "_MirMatureValue", null);
		TxtReadandWrite txtMirValue = new TxtReadandWrite(outMirValue, true);
		TxtReadandWrite txtMirDetailValue = new TxtReadandWrite(outMirDetailValue, true);
		for (Entry<String, Double> entry : hashMiRNAvalue.entrySet()) {
			txtMirValue.writefileln(entry.getKey() + "\t" + entry.getValue());
		}
		for (Entry<String, ArrayList<String[]>> entry : hashMiRNADetailValue.entrySet()) {
			ArrayList<String[]> lsvalue = entry.getValue();
			for (String[] strings : lsvalue) {
				txtMirDetailValue.writefileln(entry.getKey() + "\t" + strings[0] + "\t" + strings[1]);
			}
		}
		
		txtMirValue.close();
		txtMirDetailValue.close();
	}
	/**
	 * ������
	 * @param outTxt
	 */
	public void countMiRNA() {
		for (String content : txtSeqFile.readlines()) {
			String[] ss = content.split("\t");
			String mirName = ss[0];
			int start = Integer.parseInt(ss[1]);
			int end = Integer.parseInt(ss[2]);
			String subName = tmpMiRNALocation.searchMirName(mirName, start, end);
			if (subName == null) {
				subName = mirName;
			}
			double value = (double)1/Integer.parseInt(ss[6]);
			addMiRNACount(mirName, value);
			addMiRNADetailCount(mirName, subName, value);
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
	 * @param miRNAname
	 * @param value
	 */
	private void addMiRNADetailCount(String miRNAname,String miRNADetailname, double value) {
		if (hashMiRNADetailValue.containsKey(miRNAname)) {
			//��þ������miRNA����Ϣ
			ArrayList<String[]> lsTmpResult = hashMiRNADetailValue.get(miRNAname);
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
			hashMiRNADetailValue.put(miRNAname, lsTmpResult);
		}
	}
}
