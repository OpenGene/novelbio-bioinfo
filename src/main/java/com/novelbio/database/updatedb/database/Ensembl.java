package com.novelbio.database.updatedb.database;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.CopedID;

public class Ensembl {

}

class EnsembleGTF extends ImportPerLine
{
	int taxID = 0;
	public void setTaxID(int taxID) {
		// TODO Auto-generated method stub
		this.taxID = taxID;
	}
	
	/**
	 * ��ָ�����ļ��������ݿ⣬������ظ��Ļ��򣬾Ͳ�������
	 * �����Ҫ������У�Ʃ��amiGO����Ϣ���븲�Ǹ÷���
	 */
	public void importInfoPerLine(String gene2AccFile, boolean gzip) {
		setReadFromLine();
		TxtReadandWrite txtGene2Acc;
		if (gzip)
			txtGene2Acc = new TxtReadandWrite(TxtReadandWrite.GZIP, gene2AccFile);
		else 
			txtGene2Acc = new TxtReadandWrite(gene2AccFile, false);
		//�ӵڶ��п�ʼ��ȡ
		String oldContent = null;
		for (String content : txtGene2Acc.readlines(readFromLine)) {
			if (checkIfSame(oldContent, content)) {
				continue;
			}
			impPerLine(content);
			oldContent = content;
		}
		impEnd();
	}
	/**
	 * �ж������ǲ�������ͬһ������
	 * @param oldLine
	 * @param newLine
	 * @return
	 */
	private boolean checkIfSame(String oldLine, String newLine)
	{
		if (oldLine == null) {
			return false;
		}
		String OldInfo = oldLine.split("\t")[8];
		String ThisInfo = newLine.split("\t")[8];
		String[] ssOld = OldInfo.split(";");
		String[] ssThis = ThisInfo.split(";");
		if (ssOld.length < ssThis.length) {
			return false;
		}
		for (int i = 0; i < ssThis.length; i++) {
			if (ssOld[i].equals("") || ssOld[i].contains("exon_number") || ssOld[i].contains("gene_biotype") 
			|| ssOld[i].contains("transcript_name")) {
				continue;
			}
			if (!ssOld[i].equals(ssThis[i])) {
				return false;
			}
		}
		return true;
	}
	
	
	@Override
	void impPerLine(String lineContent) {
		if (lineContent.contains("geneName")) {
			//TODO ������������ļ����бȽ�
		}
		String[] ss = lineContent.split("\t");
		ArrayList<String> lsRefID = new ArrayList<String>();
		for (String string : ss) {
			if (string.contains("gene_id")) {
				lsRefID.add(string.replace("gene_id", "").replace("\"", "").trim());
			}
			else if (string.contains("transcript_id")) {
				lsRefID.add(string.replace("transcript_id", "").replace("\"", "").trim());
			}
			else if (string.contains("gene_name")) {
				lsRefID.add(string.replace("gene_name", "").replace("\"", "").trim());
			}
			else if (string.contains("protein_id")) {
				lsRefID.add(string.replace("protein_id", "").replace("\"", "").trim());
			}
		}
		CopedID copedID = new CopedID(lsRefID.get(0), taxID);
		copedID.setUpdateRefAccID(lsRefID);
		copedID.setUpdateRefAccID(true);
		//������Ч�ʽϵͣ���������ν��
		for (String string : ss) {
			if (string.contains("gene_id")) {
				copedID.setUpdateAccID(string.replace("gene_id", "").replace("\"", "").trim());
				copedID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_GENE, false);
				copedID.update(true);
			}
			else if (string.contains("transcript_id")) {
				copedID.setUpdateAccID(string.replace("transcript_id", "").replace("\"", "").trim());
				copedID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_TRS, false);
				copedID.update(true);
			}
			else if (string.contains("gene_name")) {
				copedID.setUpdateAccID(string.replace("gene_name", "").replace("\"", "").trim());
				copedID.setUpdateDBinfo(NovelBioConst.DBINFO_SYMBOL, false);
				copedID.update(true);
			}
			else if (string.contains("protein_id")) {
				copedID.setUpdateAccID(string.replace("protein_id", "").replace("\"", "").trim());
				copedID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_PRO, false);
				copedID.update(true);
			}
		}
	}
	
}