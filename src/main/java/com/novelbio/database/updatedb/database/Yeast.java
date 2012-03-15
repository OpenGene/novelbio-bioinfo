package com.novelbio.database.updatedb.database;

import org.apache.commons.validator.util.Flags;

import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.generalConf.NovelBioConst;

public class Yeast {

}
/**
 * 待检验
 * 要导入两次，第一次导入产生一个无法导入的文件，然后再导入一次
 * @author zong0jie
 *
 */
class YeastDBxref extends ImportPerLine
{
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		
		if (ss[1].equals("IUBMB") || ss[1].equals("TCDB") || ss[1].equals("DIP") || ss[1].equals("BioGRID") || ss[1].equals("MetaCyc") || ss[1].equals("EUROSCARF")) {
			return true;
		}
		CopedID copedID = null;
		if (ss[1].equals("NCBI") && ss[2].equals("Gene ID")) {
			copedID = new CopedID(CopedID.IDTYPE_GENEID, ss[0], taxID);
		}
		else {
			copedID = new CopedID(ss[0], taxID);
		}
		if (ss[1].equals("NCBI") && ss[2].equals("RefSeq protein version ID")) {
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_PROTEIN, false);
		}
		else if (ss[1].equals("GenBank/EMBL/DDBJ") && ss[2].equals("Protein version ID")) {
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBIID, false);
		}
		else if (ss[2].equals("UniProt/Swiss-Prot ID")) {
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_UNIPROT_GenralID, false);
		}
		else if (ss[2].equals("UniProt/TrEMBL ID")) {
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_TRS, false);
		}
		else if (ss[2].equals("UniParc ID")) {
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_UNIPROT_UNIPARC, false);
		}
		else if (ss[2].equals("NCBI protein GI")) {
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_PROGI, false);
		}
		else if (ss[2].equals("RefSeq Accession")) {
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ, false);
		}
		else if (ss[2].equals("RefSeq protein version ID")) {
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ, false);
		}
		else {
			return true;
		}
		copedID.setUpdateRefAccID(ss[3]); copedID.setUpdateRefAccID(ss[4]);
		if (ss.length == 6) {
			copedID.setUpdateRefAccID(ss[5]);
		}
		//whether the ID is insert in the database
		boolean flag = true;
		flag = copedID.update(false);
		copedID.setUpdateAccID(ss[3]); copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI, false);
		copedID.update(false);
		copedID.setUpdateAccID(ss[4]); copedID.setUpdateDBinfo(NovelBioConst.DBINFO_SSC_ID, true);
		copedID.update(false);
		
		if (ss.length == 6) {
			copedID.setUpdateAccID(ss[5]); copedID.setUpdateDBinfo(NovelBioConst.DBINFO_SYMBOL, true);
			copedID.update(false);
		}
		
		return flag;
	}
}
/**
 * 待检验
 * @author zong0jie
 *
 */
class SGD_features extends ImportPerLine
{
	/**
	 * 0.   Primary SGDID (mandatory)
1.   Feature type (mandatory)
2.   Feature qualifier (optional)
3.   Feature name (optional)
4.   Standard gene name (optional)
5.   Alias (optional, multiples separated by |)
6.   Parent feature name (optional)
7.   Secondary SGDID (optional, multiples separated by |)
8.   Chromosome (optional)
9.   Start_coordinate (optional)
10.  Stop_coordinate (optional)
11.  Strand (optional)
12.  Genetic position (optional)
13.  Coordinate version (optional)
14.  Sequence version (optional)
15.  Description (optional)
	 */
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		if (ss.length < 16) {
			return true;
		}
		CopedID copedID = new CopedID(ss[0], taxID);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setTypeOfGene(ss[1]);
		//set gene symbol
		if (ss[4] != null && !ss[4].equals("")) {
			geneInfo.setSymb(ss[4]);
		}
		else {
			String symbol = copedID.getAccIDDBinfo(NovelBioConst.DBINFO_SYMBOL);
			if (symbol != null) {
				geneInfo.setSymb(symbol);
			}
			else if (!ss[3].equals("") ) {
				geneInfo.setSymb(ss[3]);
			}
		}
		if (!ss[5].equals("")) {
			geneInfo.setSynonym(ss[5]);
		}
		if (!ss[13].equals("")) {
			geneInfo.setModDate(ss[13]);
		}
		geneInfo.setDescrp(ss[15]);
		geneInfo.setDBinfo(NovelBioConst.DBINFO_SSC_ID);
		copedID.setUpdateGeneInfo(geneInfo);
		return copedID.update(true);
	}
	
}



