package com.novelbio.database.updatedb.database;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.CopedID;

public class Ensembl {

}

class EnsembleGTF extends ImportPerLine
{
	/**
	 * 将指定的文件导入数据库，如果是重复的基因，就不导入了
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
			if (content.split(regex)) {
				
			}
			impPerLine(content);
		}
		impEnd();
	}
	/**
	 * 判断两行是不是来自同一个基因
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
			//TODO 与基因组坐标文件进行比较
		}
		String[] ss = lineContent.split("\t");
		for (String string : ss) {
			if (string.contains("gene_id")) {
				copedID
			}
		}
		
		
	}
	
}