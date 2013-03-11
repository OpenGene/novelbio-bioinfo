package com.novelbio.database.updatedb.database;

import java.util.ArrayList;

import org.apache.ibatis.annotations.Update;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.NovelBioConst;

public class ZebraFish {
	String ZbGeneIDFile = "";
	String ZbGOFile = "";
	String ZbRefSeqFile = "";
	String ZbEnsembl = "";
	public void setZbEnsembl(String zbEnsembl) {
		ZbEnsembl = zbEnsembl;
	}
	public void setZbGeneIDFile(String zbGeneIDFile) {
		ZbGeneIDFile = zbGeneIDFile;
	}
	public void setZbGOFile(String zbGOFile) {
		ZbGOFile = zbGOFile;
	}
	public void setZbRefSeqFile(String zbRefSeqFile) {
		ZbRefSeqFile = zbRefSeqFile;
	}
	
	public void update() {
//		Zb2GeneID zb2GeneID = new Zb2GeneID();
//		zb2GeneID.setTxtWriteExcep(FileOperate.changeFileSuffix(ZbGeneIDFile, "_out", null));
//		zb2GeneID.updateFile(ZbGeneIDFile, false);
//		
//		ZbRefSeqID zbRefSeqID = new ZbRefSeqID();
//		zbRefSeqID.setTxtWriteExcep(FileOperate.changeFileSuffix(ZbRefSeqFile, "_out", null));
//		zbRefSeqID.setDbInfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ);
//		zbRefSeqID.updateFile(ZbRefSeqFile, false);
//		
//		zbRefSeqID.setTxtWriteExcep(FileOperate.changeFileSuffix(ZbEnsembl, "_out", null));
//		zbRefSeqID.setDbInfo(NovelBioConst.DBINFO_ENSEMBL);
//		zbRefSeqID.updateFile(ZbEnsembl, false);
		
		ZBGO zbgo = new ZBGO();
		zbgo.setTxtWriteExcep(FileOperate.changeFileSuffix(ZbGOFile, "_out", null));
		zbgo.updateFile(ZbGOFile);
	}
}
/**
 * 1: 
 * gene2geneID.txt
 * @author zong0jie
 *
 */
class Zb2GeneID extends ImportPerLine
{
	/**
	 * 覆盖该方法来设定从第几行开始读取
	 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, ss[2], 7955);
		copedID.setUpdateAccID(ss[0]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_DRE_ZFIN, true);
		copedID.update(true);
		
		copedID.setUpdateAccIDNoCoped(ss[1]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_SYMBOL, true);
		return copedID.update(true);
	}
}
/**
 * 2:
 * gene2refseq.txt
 * ensembl_1_to_1.txt
 * <b>记得设定dbInfo</b>
 * @author zong0jie
 *
 */
class ZbRefSeqID extends ImportPerLine
{
	/**
	 * 覆盖该方法来设定从第几行开始读取
	 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	String dbInfo = "";
	/**
	 * @param dbInfo
	 * gene2refseq.txt" :NovelBioConst.DBINFO_NCBI_ACC_REFSEQ <br>
	 * ensembl_1_to_1.txt", NovelBioConst.DBINFO_ENSEMBL
	 * 
	 * @param dbInfo
	 */
	public void setDbInfo(String dbInfo) {
		this.dbInfo = dbInfo;
	}
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(ss[0], 7955);
		copedID.setUpdateRefAccID(ss[2]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_DRE_ZFIN, true);
		copedID.update(true);
		
		copedID.setUpdateAccIDNoCoped(ss[1]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_SYMBOL, true);
		copedID.update(true);
		
		copedID.setUpdateAccID(ss[2]);
		copedID.setUpdateDBinfo(dbInfo, true);
		copedID.update(true);
		return true;
	}
}
/**
 * gene_association.zfin
 * @author zong0jie
 */
class ZBGO extends ImportPerLine
{
	/**
	 * 覆盖该方法来设定从第几行开始读取
	 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	@Override
	boolean impPerLine(String lineContent) {
		if (lineContent.startsWith("!")) {
			return true;
		}
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(ss[1], 7955);
		String pubmed = "";
		if (ss[5].contains("PMID")) {
			pubmed = ss[5].split("\\|")[1].trim();
		}
		else {
			pubmed = null;
		}
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_DRE_ZFIN, true);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setSymb(ss[2]);
		geneInfo.setDescrp(ss[9]);
		geneInfo.setDBinfo(NovelBioConst.DBINFO_DRE_ZFIN);
		geneInfo.setTypeOfGene(ss[11]);
		geneInfo.setModDate(ss[13]);
		
		copedID.setUpdateGeneInfo(geneInfo);
		copedID.addUpdateGO(ss[4], NovelBioConst.DBINFO_DRE_ZFIN, ss[6], pubmed, null);
		return copedID.update(true);
	}
}
