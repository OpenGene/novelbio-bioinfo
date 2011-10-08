package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;


import com.novelbio.analysis.seq.genomeNew2.gffOperate.GtfDetailCufIso;
import com.novelbio.analysis.seq.genomeNew2.gffOperate.GtfHashCufIso;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class SplitCope {
	String splitFile = "";
	GtfHashCufIso gtfHashCufIso = null;
	/**splitFile
	 * ָ���ɱ���ӵ��ļ��ͼ������ļ�
	 * @param splitFile
	 * @param fpkmTrackFile
	 */
	public SplitCope(String splitFile, String fpkmTrackFile) {
		gtfHashCufIso = new GtfHashCufIso(fpkmTrackFile);
		this.splitFile = splitFile;
	}
	/**
	 * ��������ļ�����������splitFile
	 * @param splitNewFile
	 * @throws Exception 
	 */
	public void copeSplit(String splitNewFile) throws Exception {
		TxtReadandWrite txtSplit = new TxtReadandWrite();
		txtSplit.setParameter(splitFile, false, true);

		TxtReadandWrite txtSplitNew = new TxtReadandWrite();
		txtSplitNew.setParameter(splitNewFile, true, false);

		ArrayList<String> lsSplit = txtSplit.readfileLs();
		ArrayList<String> lsExpName = GtfDetailCufIso.getExp();
		String ExpInfo = "";
		for (String string : lsExpName) {
			ExpInfo = ExpInfo+"\t"+string;
		}
		txtSplitNew.writefile(lsSplit.get(0) + ExpInfo+"\n");
		for (int i = 1; i < lsSplit.size(); i++) {
			String[] ss = lsSplit.get(i).split("\t");
			if (ss[2].equals("-")) {
				continue;
			}
			String result = getSplitInfo(ss[2],10);
			if (result == null) {
				continue;
			}
			txtSplitNew.writefile(lsSplit.get(i) + result+"\n");
		}
	}
	/**
	 * ָ��geneID�����ظ�geneID�ĸ���ת¼���ı�����
	 * �����ĸ�ʽ
	 * isoName:exp isoName:exp
	 * @param geneID
	 * @param filter ��ֵ��
	 * ���������������һ�ֲű���
	 * 1. ����ת¼���������������ϻ�����ڸ���ֵ
	 * 2. ����ת¼���У�һ����A������ڸ���ֵ����һ��B������ڸ���ֵ
	 * @return ���û�ҵ����ͷ���null
	 */
	private String getSplitInfo(String geneID,double filter) {
		
		GtfDetailCufIso gtfDetailCufIso = gtfHashCufIso.searchLOC(geneID);
		if (gtfDetailCufIso == null) {
			return null;
		}
		ArrayList<String[]> lsIsoExp = gtfDetailCufIso.getIsoExp();

		//�����һ������ÿһ�д���һ��ʱ�ڣ�ÿһ�д���һ�ֿɱ���ӡ�
		//��ô���Ҫ�ﵽ��ֵ���������м������������������ֵ
		//���������м������������������ֵ
		int[][] Info = new int[lsIsoExp.size()][lsIsoExp.get(0).length];//�����м����
		
		for (int i = 0; i < lsIsoExp.size(); i++) {//����ÿһ��
			for (int j = 0; j < lsIsoExp.get(0).length; j++) {//����ÿһ���е�ÿһ���൱�ڱ���ÿһ��
				if (Double.parseDouble(lsIsoExp.get(i)[j].split(":")[1]) >= filter) {
					Info[i][j] = 1;
				}
			}
		}
		int addRow = 0;//����1���м���
		int addCol = 0;//����1���м���
		for (int i = 0; i < Info.length; i++) {
			for (int j = 0; j < Info[i].length; j++) {
				if (Info[i][j] == 1) {
					addRow++;
					break;
				}
			}
		}
		//�͸ո��෴
		for (int i = 0; i < Info[0].length; i++) {
			for (int j = 0; j < Info.length; j++) {
				if (Info[j][i] == 1) {
					addCol++;
					break;
				}
			}
		}
		//����������
		if (!(addCol>=2&&addRow>=2)) {
			return null;
		}
		String result = "";
		for (int i = 0; i < lsIsoExp.size(); i++) {
			String[] expStrings = lsIsoExp.get(i);
			String tmp = "";
			for (String string : expStrings) {
				if (tmp.equals("")) {
					tmp = string;
					continue;
				}
				tmp = tmp + "," + string;
			}
			result = result+"\t"+tmp;
		}
		return result;
	}
	
	
}
