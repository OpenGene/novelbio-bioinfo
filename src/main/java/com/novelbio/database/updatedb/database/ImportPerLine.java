package com.novelbio.database.updatedb.database;

import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.species.Species;
/**
 * 将待升级的文件按照行读取的abstract类，因为很多文件都是一行一个信息，
 * 那么就可以按照行将文件信息导入数据库
 * @author zong0jie
 *
 */
abstract class ImportPerLine {
	private static Logger logger = Logger.getLogger(ImportPerLine.class);
	static HashSet<Integer> setTaxID = null;
	static String taxIDfile = "";
	int readFromLine = 1;
	//多此一举的设定，回头慢慢修正
	int num = 1;
	int taxID = 0;
	/**
	 * 将无法升级的行写入该文本
	 */
	TxtReadandWrite txtWriteExcep = null;
	/**
	 * 将无法升级的行写入该文本
	 */
	public void setTxtWriteExcep(String txtWriteExcepFile) {
		txtWriteExcep = new TxtReadandWrite(txtWriteExcepFile, true);
	}
	/**
	 * 多此一举的设定
	 * 覆盖该方法来设定从第几行开始读取
	 */
	protected void setReadFromLine() {
		this.readFromLine = num;
	}
	/**
	 * 设定从第几行开始读取
	 */
	protected void setReadFromLine(int num) {
		this.num = num;
	}

	/**
	 * 将指定的文件导入数据库，必须是每一行都能单独导入的表
	 * 如果需要导入多行，譬如amiGO的信息，请覆盖该方法
	 */
	public boolean updateFile(String gene2AccFile) {
		setReadFromLine();
		TxtReadandWrite txtGene2Acc;
		txtGene2Acc = new TxtReadandWrite(gene2AccFile);
		
		
		//从第二行开始读取
		int num = 0;
		for (String content : txtGene2Acc.readlines(readFromLine)) {
			if (content.startsWith("#") || content.startsWith("!")) {
				continue;
			}
			try {
				if (!impPerLine(content)) {
					if (txtWriteExcep != null) {
						txtWriteExcep.writefileln(content);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (txtWriteExcep != null) {
					txtWriteExcep.writefileln(content);
				}
			}
	
			num++;
			if (num%10000 == 0) {
				logger.info(gene2AccFile + " import line number:" + num);
			}
		}
		impEnd();
		txtGene2Acc.close();
		if (txtWriteExcep != null) {
			txtWriteExcep.close();
		}
		logger.info("finished import file " + gene2AccFile);
		return true;
	}
	
	/**
	 * 读取taxID文件，其中taxID在第一列，然后将taxID读取进入hash表，方便后续处理
	 * 后续导入文件就仅导入这些物种的信息
	 * 仅读取一次
	 * @param taxIDfile
	 */
	public static void setTaxIDFile(String taxIDfile) {
		if (ImportPerLine.taxIDfile.equals(taxIDfile)) {
			return;
		}
//		Species.updateTaxInfo(taxIDfile);
		setTaxID = new HashSet<>();
		TxtReadandWrite txtTaxID=new TxtReadandWrite(taxIDfile, false);
		for (String string : txtTaxID.readlines()) {
			if (string.startsWith("#")) {
				continue;
			}
			if (string.trim().equals("")) {
				continue;
			}
			String[] ss=string.split("\t");
			setTaxID.add(Integer.parseInt(ss[0]));
		}
		txtTaxID.close();
	}
	
	public static void addTaxId(int taxId) {
		if (setTaxID == null) {
			setTaxID = new HashSet<>();
		}
		setTaxID.add(taxId);
	}
	/**
	 * 导入单个文件时，设定taxID
	 * @param taxID
	 */
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	/**
	 * 按行处理具体信息
	 * @param lineContent
	 */
	abstract boolean impPerLine(String lineContent);
	/**
	 * 结尾的时候做的工作，譬如最后还需要导入一次什么东西，就重写该函数
	 */
	void impEnd() {}
}
