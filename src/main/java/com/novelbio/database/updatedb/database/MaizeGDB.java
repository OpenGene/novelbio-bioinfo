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
	public void update() {
		MaizeAccID maizeAccID = new MaizeAccID();
		maizeAccID.setTaxID(taxID);
		maizeAccID.setReadFromLine(2);
		String outfile = FileOperate.changeFileSuffix(maizeDbxref, "_out", null);
		maizeAccID.setTxtWriteExcep(FileOperate.changeFileSuffix(maizeDbxref, "_out", null));
		maizeAccID.setUniProtID(false);
		maizeAccID.updateFile(maizeDbxref, false);
		
		maizeAccID = new MaizeAccID();
		maizeAccID.setTaxID(taxID);
		//第二次读就从第一行开始读取了
		maizeAccID.setReadFromLine(1);
		maizeAccID.setTxtWriteExcep(FileOperate.changeFileSuffix(outfile, "_out2", null));
		maizeAccID.setUniProtID(true);
		maizeAccID.updateFile(outfile, false);
		
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
class MaizeAccID extends ImportPerLine {
	private static Logger logger = Logger.getLogger(MaizeAccID.class);
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
		if (ss[0].contains("GRMZM2G353867")) {
			logger.debug("stop");
		}
		GeneID copedID = null;
		if (ss[0].startsWith("AC")) {
			copedID = new GeneID(ss[0], taxID);
		} else {
			copedID = new GeneID(ss[0].split("_")[0], taxID);
		}
		
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_MAIZE_MGDB, true);
		//是否成功升级的标志。因为后面要升级两次
		boolean flag = false;
		if (ss[1].equals("EntrezGene")) {
			copedID.setUpdateGeneID(ss[2], GeneID.IDTYPE_GENEID);
			if (ss.length >= 4) {
				GeneInfo geneInfo = new GeneInfo();
				geneInfo.setDBinfo(NovelBioConst.DBINFO_MAIZE_MGDB);
				geneInfo.setDescrp(ss[3]);
				copedID.setUpdateGeneInfo(geneInfo);
			}
			//geneID的话就不是其他的数据库ID，就不需要再导入一次了
			flag = copedID.update(uniProtID);
			updateMaizeACID(ss, copedID);
			return flag;
		}
		//否则的话就常规导入
		else if (!ss[1].equalsIgnoreCase("GO")) {
			copedID.setUpdateRefAccID(ss[2]);
			if (ss.length >= 4) {
				GeneInfo geneInfo = new GeneInfo();
				geneInfo.setDBinfo(NovelBioConst.DBINFO_MAIZE_MGDB);
				geneInfo.setDescrp(ss[3]);
				copedID.setUpdateGeneInfo(geneInfo);
			}

			flag = copedID.update(uniProtID);
		} else {
			copedID.setUpdateRefAccID(copedID.getAccID());
			flag = copedID.update(uniProtID);
		}
		 
		updateMaizeACID(ss, copedID);
		updateDbxrefID(ss, copedID);
		 return flag;
	}
	
	/** 玉米的ACID很奇怪，譬如
	 * AC148152.3_FGT005可以为
	 * AC148152.3_FG005
	 * AC148152.3_FGP005
	 * 三种
	 */
	private void updateMaizeACID(String[] ss, GeneID geneID) {
		 if (ss[0].startsWith("AC")) {
			 geneID.setUpdateAccID(ss[0].replace("FGT", "FG"));
			 geneID.update(uniProtID);
			 geneID.setUpdateAccID(ss[0].replace("FGT", "FGP"));
			 geneID.update(uniProtID);
		}
	}
	
	private void updateDbxrefID(String[] ss, GeneID geneID) {
		if (ss[1].equalsIgnoreCase("GO")) {
			return;
		}
		//常规导入后还需要将本ID再导入一次
		geneID.setUpdateAccID(ss[2]);
		String dbInfo = null;
		 if (ss[1].equals("RefSeq_dna")) {
			 dbInfo = NovelBioConst.DBINFO_NCBI_ACC_REFSEQ;
		 } else if (ss[1].equals("RefSeq_peptide")) {
			 dbInfo = NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_PROTEIN;
		 } else if (ss[1].equals("UniGene")) {
			dbInfo = NovelBioConst.DBINFO_UNIPROT_UNIGENE;
		 }
		 
		 if (dbInfo != null) {
			 geneID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ, false);
			 geneID.update(uniProtID);
		 }
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
		
		if (ss[0].equals("AC194389.3_FG001")) {
			System.out.println("stop");
		}
		
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