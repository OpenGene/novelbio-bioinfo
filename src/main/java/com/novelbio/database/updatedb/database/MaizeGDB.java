package com.novelbio.database.updatedb.database;

import org.apache.log4j.Logger;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.NovelBioConst;

public class MaizeGDB {
	private static Logger logger = Logger.getLogger(MaizeGDB.class);

	int taxID = 4577;
	String maizeDbxref = "";
	String maizeGeneInfoFile = "";
	/**
	 * /media/winE/Bioinformatics/GenomeData/soybean/ncbi/dbxref.xls
	 * @param soyDbxref
	 */
	public void setMaizeDbxref(String soyDbxref) {
		this.maizeDbxref = soyDbxref;
	}
	public void setMaizeGeneInfo(String maizeGeneInfo) {
		this.maizeGeneInfoFile = maizeGeneInfo;
	}
	/**
	 * 将Tigr的Gff文件导入gene2GO数据库，倒入NCBIGO和UniGO两个表
	 * @param gffRapDB
	 * @param outFIle
	 * @throws Exception
	 */
	public void update()
	{
		MaizeAccID maizeAccID = new MaizeAccID();
		maizeAccID.setTaxID(taxID);
		maizeAccID.setReadFromLine(2);
		String outfile = FileOperate.changeFileSuffix(maizeDbxref, "_out", null);
		maizeAccID.setTxtWriteExcep(FileOperate.changeFileSuffix(maizeDbxref, "_out", null));
		maizeAccID.setUniProtID(false);
//		maizeAccID.updateFile(maizeDbxref, false);
		
		maizeAccID = new MaizeAccID();
		maizeAccID.setTaxID(taxID);
		//第二次读就从第一行开始读取了
		maizeAccID.setReadFromLine(1);
		maizeAccID.setTxtWriteExcep(FileOperate.changeFileSuffix(outfile, "_out2", null));
		maizeAccID.setUniProtID(true);
//		maizeAccID.updateFile(outfile, false);
		
		MaizeGeneInfo maizeGeneInfo = new MaizeGeneInfo();
		maizeGeneInfo.setReadFromLine(2);
		maizeGeneInfo.setTxtWriteExcep(FileOperate.changeFileSuffix(maizeGeneInfoFile, "_out", null));
		maizeGeneInfo.updateFile(maizeGeneInfoFile, false);
		
		MaizeGO maizeGO = new MaizeGO();
		maizeGO.setReadFromLine(2);
		maizeGO.setTxtWriteExcep(FileOperate.changeFileSuffix(maizeDbxref, "_GOout", null));
		maizeGO.updateFile(maizeDbxref, false);
	}
}

/**
 * www.maizeseq
 * uence.org
 * 上的ZmB73_5a_xref.txt
 * 从第二行开始读
 */
class MaizeAccID extends ImportPerLine
{
	boolean uniProtID = false;
	/**
	 * 是否将没有查到的ID导入uniProtID
	 * @param uniProtID
	 */
	public void setUniProtID(boolean uniProtID) {
		this.uniProtID = uniProtID;
	}
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		if (ss[1].equals("GO"))
			return true;
		
		GeneID copedID = null;
		if (ss[0].startsWith("AC"))
			copedID = new GeneID(ss[0], taxID);
		else
			copedID = new GeneID(ss[0].split("_")[0], taxID);
		
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_MAIZE_MGDB, true);
		//是否成功升级的标志。因为后面要升级两次
		boolean flag = false;
		//如果是geneID
		if (ss[1].equals("EntrezGene")) {
			copedID.setUpdateGeneID(ss[2], GeneID.IDTYPE_GENEID);
			//geneID的话就不是其他的数据库ID，就不需要再导入一次了
			return copedID.update(uniProtID);
		}
		//否则的话就常规导入
		else {
			copedID.setUpdateRefAccID(ss[2]);
			flag = copedID.update(uniProtID);
		}
		//常规导入后还需要将本ID再导入一次
		 copedID.setUpdateAccID(ss[2]);
		String dbInfo = null;
		 if (ss[1].equals("RefSeq_dna"))
			 dbInfo = NovelBioConst.DBINFO_NCBI_ACC_REFSEQ;
		else if (ss[1].equals("RefSeq_peptide"))
			dbInfo = NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_PROTEIN;
		else if (ss[1].equals("UniGene"))
			dbInfo = NovelBioConst.DBINFO_UNIPROT_UNIGENE;
		 if (dbInfo != null) {
			 copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ, false);
			 copedID.update(uniProtID);
		 }
		 return flag;
	}
}

/**
 * /media/winE/Bioinformatics/GenomeData/maize/ZmB73_5a_gene_descriptors.txt
 * 从第二行开始读取
 * @author zong0jie
 *
 */
class MaizeGeneInfo extends ImportPerLine
{
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		if (ss.length < 4)
			return true;
		
		GeneID copedID = new GeneID(ss[0], taxID);
		AGeneInfo geneInfo = new GeneInfo();
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_MAIZE_MGDB, false);
		geneInfo.setDescrp(ss[3]);
		geneInfo.setSymb(ss[0]);
		geneInfo.setDBinfo(NovelBioConst.DBINFO_MAIZE_MGDB);
		copedID.setUpdateGeneInfo(geneInfo);
		return copedID.update(false);
	}
}
/**
 * www.maizeseq
 * uence.org
 * 上的ZmB73_5a_xref.txt
 * 从第二行开始读
 */
class MaizeGO extends ImportPerLine
{
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		if (!ss[1].equals("GO"))
			return true;
			
		GeneID copedID = null;
		if (ss[0].startsWith("AC"))
			copedID = new GeneID(ss[0], taxID);
		else
			copedID = new GeneID(ss[0].split("_")[0], taxID);
			
		copedID.setUpdateGO(ss[2], NovelBioConst.DBINFO_MAIZE_MGDB, null, null, null);
		return copedID.update(false);
	}
}