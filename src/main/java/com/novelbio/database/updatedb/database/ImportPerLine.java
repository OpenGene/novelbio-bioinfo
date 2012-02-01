package com.novelbio.database.updatedb.database;

import java.util.HashSet;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.TaxInfo;
/**
 * 将待升级的文件按照行读取的abstract类，因为很多文件都是一行一个信息，
 * 那么就可以按照行将文件信息导入数据库
 * @author zong0jie
 *
 */
abstract class ImportPerLine
{
	static HashSet<Integer> hashTaxID = null;
	static String taxIDfile = "";
	int readFromLine = 2;
	/**
	 * 覆盖该方法来设定从第几行开始读取
	 */
	protected void setReadFromLine() {
		this.readFromLine = 2;
	}
	/**
	 * 将指定的文件导入数据库，必须是每一行都能单独导入的表
	 * 如果需要导入多行，譬如amiGO的信息，请覆盖该方法
	 */
	public void importInfoPerLine(String gene2AccFile, boolean gzip) {
		setReadFromLine();
		TxtReadandWrite txtGene2Acc;
		if (gzip)
			txtGene2Acc = new TxtReadandWrite(TxtReadandWrite.GZIP, gene2AccFile);
		else 
			txtGene2Acc = new TxtReadandWrite(gene2AccFile, false);
		//从第二行开始读取
		for (String content : txtGene2Acc.readlines(readFromLine)) {
			impPerLine(content);
		}
		impEnd();
	}
	
	/**
	 * 读取taxID文件，其中taxID在第一列，然后将taxID读取进入hash表，方便后续处理
	 * 后续导入文件就仅导入这些物种的信息
	 * 仅读取一次
	 * @param taxIDfile
	 */
	public void setTaxID(String taxIDfile) {
		if (ImportPerLine.taxIDfile.equals(taxIDfile)) {
			return;
		}
		hashTaxID = new HashSet<Integer>();
		TxtReadandWrite txtTaxID=new TxtReadandWrite(taxIDfile, false);
		for (String string : txtTaxID.readlines()) {
			if (string.trim().equals("")) {
				continue;
			}
			String[] ss=string.split("\t");
			ss = ArrayOperate.copyArray(ss, 5);
			TaxInfo taxInfo = new TaxInfo();
			taxInfo.setTaxID(Integer.parseInt(ss[0]));
//			taxInfo.setChnName(ss[1]);
			taxInfo.setLatin(ss[2]);
			taxInfo.setComName(ss[3]);
			taxInfo.setAbbr(ss[4]);
			taxInfo.update();
			hashTaxID.add(Integer.parseInt(ss[0]));
		}
	}
	/**
	 * 按行处理具体信息
	 * @param lineContent
	 */
	abstract void impPerLine(String lineContent);
	/**
	 * 结尾的时候做的工作，譬如最后还需要导入一次什么东西，就重写该函数
	 */
	void impEnd()
	{}
}
