package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * ����snp��Ϣ��ѡ��ĳ�����������涼�����仯�Ļ���
 * ������ļ���ѡ����snp�ļ�
 * @author zong0jie
 *
 */
public class FilterGene {
	String filename = "";
	ArrayList<String[]> lsInfo = null;
	/**
	 * key: gene
	 * value: gene����Ӧ����
	 */
	HashMap<String, ArrayList<String[]>> hashInfo = new HashMap<String, ArrayList<String[]>>();
	String flag = "TRUE";
	int geneCol = 0;
	
	public static void main(String[] args) {
		String filename = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/snpFinal/All_Result_Filtered.txt";
		int[] aGene = new int[]{2,6};
		int[] bGene = new int[]{3,5,6};
		int[] cGene = new int[]{4,5,6};
		
		int geneCol = 50;
		
		String outFile = "/media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/snpFinal/OutGeneABC";
		
		FilterGene filterGene = new FilterGene();
		filterGene.setFilename(filename, geneCol);
		filterGene.getWantedGene(outFile,aGene, bGene, cGene);
	}
	
	public void setFilename(String filename, int Colgene) {
		this.filename = filename;
		this.geneCol = Colgene - 1;
		lsInfo = ExcelTxtRead.readLsExcelTxt(filename, 1);
		for (String[] strings : lsInfo) {
			ArrayList<String[]> lsTmpInfo  = null;
			if (strings.length <= geneCol || strings[geneCol] == null) {
				
			}
			if (hashInfo.containsKey(strings[geneCol])) {
				lsTmpInfo = hashInfo.get(strings[geneCol]);
			}
			else {
				lsTmpInfo = new ArrayList<String[]>();
				hashInfo.put(strings[geneCol], lsTmpInfo);
			}
			lsTmpInfo.add(strings);
		}
	}

	
	public void getWantedGene(String outFile, int[]... flagCol) {
		ArrayList<HashSet<String>> lsHashGene = new ArrayList<HashSet<String>>();
		for (int[] is : flagCol) {
			lsHashGene.add(getGene(is));
		}
		ArrayList<String> lsGeneID = getOverlapGene(lsHashGene);
		ArrayList<String[]> lsGeneInfo = getLsInfo(lsGeneID);
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.ExcelWrite(lsGeneInfo, "\t", 1, 1);
	}
	/**
	 * ���ݸ����У��жϸ����Ƿ�Ϊtrue�����Ϊtrue������ȡ��Ӧ��gene
	 */
	private HashSet<String> getGene(int... flagCol) {
		HashSet<String> hashResult = new HashSet<String>();
		for (int i = 0; i < flagCol.length; i++) {
			flagCol[i] = flagCol[i] - 1;
		}
		for (String[] strings : lsInfo) {
			for (int i = 0; i < flagCol.length; i++) {
				if (strings[flagCol[i]].equals(flag)) {
					hashResult.add(strings[geneCol]);
					break;
				}
			}
		}
		return hashResult;
	}
	/**
	 * ����һ��hash�������еĻ�����ѡ����
	 * @param hashGenes
	 * @return
	 */
	private ArrayList<String> getOverlapGene(ArrayList<HashSet<String>> lsHashGenes) {
		ArrayList<String> lsResult = new ArrayList<String>();
		HashSet<String> hashGene1 = lsHashGenes.get(0);
		for (String string : hashGene1) {
			boolean flag = true;
			for (int i = 1; i < lsHashGenes.size(); i++) {
				if (!lsHashGenes.get(i).contains(string)) {
					flag = false;
					break;
				}
			}
			//���ÿ��������
			if (flag)
				lsResult.add(string);
		}
		return lsResult;
	}
	
	/**
	 * ����geneID�������и�geneID��������ȫ����ȡ����
	 * @param lsGeneID
	 * @return
	 */
	private ArrayList<String[]> getLsInfo(ArrayList<String> lsGeneID) {
		ArrayList<String[]> lsFinal = new ArrayList<String[]>();
		for (String string : lsGeneID) {
			lsFinal.addAll(hashInfo.get(string));
		}
		return lsFinal;
	}
}
