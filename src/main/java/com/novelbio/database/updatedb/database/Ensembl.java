package com.novelbio.database.updatedb.database;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.CopedID;

public class Ensembl {

}

class EnsembleGTF extends ImportPerLine
{
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
		for (String content : txtGene2Acc.readlines(readFromLine)) {
			if (content.split(regex)) {
				
			}
			impPerLine(content);
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
		
	}
	
	
	@Override
	void impPerLine(String lineContent) {
		if (lineContent.contains("geneName")) {
			//TODO ������������ļ����бȽ�
		}
		String[] ss = lineContent.split("\t");
		for (String string : ss) {
			if (string.contains("gene_id")) {
				copedID
			}
		}
		
		
	}
	
}