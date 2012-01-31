package com.novelbio.database.updatedb.database;

import java.util.HashSet;

import com.novelbio.base.dataOperate.TxtReadandWrite;
/**
 * �����������ļ������ж�ȡ��abstract�࣬��Ϊ�ܶ��ļ�����һ��һ����Ϣ��
 * ��ô�Ϳ��԰����н��ļ���Ϣ�������ݿ�
 * @author zong0jie
 *
 */
abstract class ImportPerLine
{
	static HashSet<Integer> hashTaxID = null;
	static String taxIDfile = "";
	int readFromLine = 2;
	/**
	 * ���Ǹ÷������趨�ӵڼ��п�ʼ��ȡ
	 */
	protected void setReadFromLine() {
		this.readFromLine = 2;
	}
	/**
	 * ��ָ�����ļ��������ݿ⣬������ÿһ�ж��ܵ�������ı�
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
			impPerLine(content);
		}
		impEnd();
	}
	
	/**
	 * ��ȡtaxID�ļ�������taxID�ڵ�һ�У�Ȼ��taxID��ȡ����hash�������������
	 * ���������ļ��ͽ�������Щ���ֵ���Ϣ
	 * ����ȡһ��
	 * @param taxIDfile
	 */
	public void setTaxID(String taxIDfile) {
		if (ImportPerLine.taxIDfile.equals(taxIDfile)) {
			return;
		}
		TxtReadandWrite txtTaxID=new TxtReadandWrite(taxIDfile, false);
		for (String string : txtTaxID.readlines()) {
			String[] ss=string.split("\t");
			hashTaxID.add(Integer.parseInt(ss[0]));
		}
	}
	/**
	 * ���д��������Ϣ
	 * @param lineContent
	 */
	abstract void impPerLine(String lineContent);
	/**
	 * ��β��ʱ�����Ĺ�����Ʃ�������Ҫ����һ��ʲô����������д�ú���
	 */
	void impEnd()
	{}
}
