package com.novelbio.database.updatedb.database;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.model.geneanno.AgeneUniID;
import com.novelbio.database.model.geneanno.Gene2Go;
import com.novelbio.database.model.geneanno.GeneInfo;

/**
 * 酿酒酵母的网址：
 * http://www.yeastgenome.org/download-data/curation
 * @author zong0jie
 *
 */
public class Yeast {
	String yeastDBxrefFile = "";
	String SGD_featuresFile = "";
	String Gene_AssociationFile ="";
	String Pipas_FunFile = "";
	String Pipas_GO_SlimFile = "";
	String ppa_ncbi_geneidFile = "";
	public void setGene_AssociationFile(String gene_AssociationFile) {
		Gene_AssociationFile = gene_AssociationFile;
	}
	public void setPipas_Fun(String pipas_Fun) {
		Pipas_FunFile = pipas_Fun;
	}
	public void setPipas_GO_Slim(String pipas_GO_Slim) {
		Pipas_GO_SlimFile = pipas_GO_Slim;
	}
	public void setSGD_featuresFile(String sGD_featuresFile) {
		SGD_featuresFile = sGD_featuresFile;
	}
	public void setYeastDBxrefFile(String yeastDBxrefFile) {
		this.yeastDBxrefFile = yeastDBxrefFile;
	}
	public void setPpa_ncbi_geneidFile(String ppa_ncbi_geneidFile) {
		this.ppa_ncbi_geneidFile = ppa_ncbi_geneidFile;
	}
	
	public void update() {
		YeastDBxref yeastDBxref = new YeastDBxref();
		yeastDBxref.setTaxID(4932);
		String outyeastDBxrefFile = FileOperate.changeFileSuffix(yeastDBxrefFile, "out", null);
		yeastDBxref.setTxtWriteExcep(outyeastDBxrefFile);
		yeastDBxref.setUpdateIntoUniID(false);
		yeastDBxref.updateFile(yeastDBxrefFile);
		yeastDBxref.setUpdateIntoUniID(true);
		yeastDBxref.updateFile(outyeastDBxrefFile);
		
		SGD_features sgd_features = new SGD_features();
		sgd_features.setTaxID(4932);
		sgd_features.updateFile(SGD_featuresFile);
		
		Gene_Association gene_Association = new Gene_Association();
		gene_Association.setTaxID(4932);
		gene_Association.updateFile(Gene_AssociationFile);
		
//		Ppa_ncbi_geneid ppa_ncbi_geneid = new Ppa_ncbi_geneid();
//		ppa_ncbi_geneid.setTaxID(4922);
//		ppa_ncbi_geneid.updateFile(ppa_ncbi_geneidFile);
		
//		Pipas_Fun pipas_Fun = new Pipas_Fun();
//		pipas_Fun.setTaxID(4922);
//		pipas_Fun.updateFile(Pipas_FunFile);
		
//		Pipas_GO_Slim pipas_GO_Slim = new Pipas_GO_Slim();
//		pipas_GO_Slim.setTaxID(4922);
//		pipas_GO_Slim.updateFile(Pipas_GO_SlimFile);
	}
}
/**
 * dbxref.tab
 * read from first line
 * Need To Be Checked 
 * 要导入两次，第一次导入产生一个无法导入的文件，然后再导入一次
 * @author zong0jie
 *
 */
class YeastDBxref extends ImportPerLine {
	/**
	 * if the ID cannot find int the database, whether insert it into UniID table
	 */
	boolean updateIntoUniID = false;
	/**
	 * if the ID cannot find int the database, whether insert it into UniID table
	 */
	public void setUpdateIntoUniID(boolean updateIntoUniID) {
		this.updateIntoUniID = updateIntoUniID;
	}
	/**
	 * 覆盖该方法来设定从第几行开始读取
	 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		
		if (ss[1].equals("IUBMB") || ss[1].equals("TCDB") || ss[1].equals("DIP") || ss[1].equals("BioGRID") || ss[1].equals("MetaCyc") || ss[1].equals("EUROSCARF")) {
			return true;
		}
		GeneID copedID = null;
		if (ss[1].equals("NCBI") && ss[2].equals("Gene ID")) {
			copedID = new GeneID(GeneID.IDTYPE_GENEID, ss[0], taxID);
		}
		else {
			copedID = new GeneID(ss[0], taxID);
		}
		
		if (ss[1].equals("NCBI") && ss[2].equals("RefSeq protein version ID")) {
			copedID.setUpdateDBinfo(DBAccIDSource.RefSeqPro, false);
		}
		else if (ss[1].equals("GenBank/EMBL/DDBJ") && ss[2].equals("Protein version ID")) {
			copedID.setUpdateDBinfo(DBAccIDSource.NCBI, false);
		}
		else if (ss[2].equals("UniProt/Swiss-Prot ID")) {
			copedID.setUpdateDBinfo(DBAccIDSource.Uniprot, false);
		}
		else if (ss[2].equals("UniProt/TrEMBL ID")) {
			copedID.setUpdateDBinfo(DBAccIDSource.Ensembl_TRS, false);
		}
		else if (ss[2].equals("UniParc ID")) {
			copedID.setUpdateDBinfo(DBAccIDSource.UniprotPARC, false);
		}
		else if (ss[2].equals("NCBI protein GI")) {
			copedID.setUpdateDBinfo(DBAccIDSource.ProteinGI, false);
		}
		else if (ss[2].equals("RefSeq Accession")) {
			copedID.setUpdateDBinfo(DBAccIDSource.RefSeqRNA, false);
		}
		else if (ss[2].equals("RefSeq protein version ID")) {
			copedID.setUpdateDBinfo(DBAccIDSource.RefSeqRNA, false);
		}

		copedID.setUpdateRefAccID(ss[3], ss[4]);
		if (ss.length == 6) {
			copedID.addUpdateRefAccID(ss[5]);
		}
		//whether the ID is insert in the database
		boolean flag = true;
		flag = copedID.update(false);
		//there has one GenBank/EMBL/DDBJ	Protein version ID mapping to 2 ssgdID, so just using ncbi geneID and refseqID to mapping ID
		if (flag || ss[2].equals("RefSeq Accession") || (ss[1].equals("NCBI") && ss[2].equals("Gene ID")) || (updateIntoUniID && flag == false)) {
			copedID.setUpdateAccID(ss[3]); copedID.setUpdateDBinfo(DBAccIDSource.GeneAC, false);
			copedID.update(updateIntoUniID);
			copedID.setUpdateAccID(ss[4]); copedID.setUpdateDBinfo(DBAccIDSource.SSC_ScerID, true);
			copedID.update(false);
			if (ss.length == 6) {
				copedID.setUpdateAccID(ss[5]); copedID.setUpdateDBinfo(DBAccIDSource.Symbol, true);
				copedID.update(false);
			}
		}
		return flag;
	}
}
/**
 * SGD_features.tab.txt
 * read from first line
 * Need To Be Checked 
 * @author zong0jie
 *
 */
class SGD_features extends ImportPerLine {
	/**
	 * 覆盖该方法来设定从第几行开始读取
	 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
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
		GeneID copedID = new GeneID(ss[0], taxID);
		copedID.setUpdateDBinfo(DBAccIDSource.SSC_ScerID, false);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setDBinfo(DBAccIDSource.SSC_ScerID.toString());
		geneInfo.setTypeOfGene(ss[1]);

		//set gene symbol
		if (ss[4] != null && !ss[4].equals("")) {
			geneInfo.setSymb(ss[4]);
		} else {
			AgeneUniID symbol = copedID.getAccIDDBinfo(DBAccIDSource.Symbol.toString());
			if (symbol != null) {
				geneInfo.setSymb(symbol.getAccID());
			}
			else if (!ss[3].equals("") ) {
				geneInfo.setSymb(ss[3]);
			}
		}
		if (!ss[5].equals("")) {
			for (String string : ss[5].split("\\|")) {
				geneInfo.addSynonym(string);
			}
		}
		if (!ss[13].equals("")) {
			geneInfo.setModDate(ss[13]);
		}
		geneInfo.setDescrp(ss[15]);

		copedID.setUpdateGeneInfo(geneInfo);
		return copedID.update(true);
	}
}
/**
 * read from first line
 * Need To Be Checked 
 * @author zong0jie
 *
 */
class Gene_Association extends ImportPerLine {
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
		if (ss[1].equals("S000004660")) {
			System.out.println();
		}
		GeneID copedID = new GeneID(ss[1], taxID);
		copedID.setUpdateDBinfo(DBAccIDSource.SSC_ScerID, false);
		Gene2Go gene2Go = new Gene2Go();
		gene2Go.setGOID(ss[4]);
		gene2Go.addDBName(DBAccIDSource.SSC_ScerID.toString());
		gene2Go.addEvidence(ss[6]);
		String[] ssRef = ss[5].split("\\|");
		for (String string : ssRef) {
			gene2Go.addReference(string);
		}
		gene2Go.setQualifier(ss[3]);
		copedID.addUpdateGO(gene2Go);
		return copedID.update(false);
	}
}

/**
 * taxID 644223
 * Pichia_GS115_function-1009.txt
 * Need To Be Checked 
 * read from the second line
 * @author zong0jie
 *
 */
class Ppa_ncbi_geneid extends ImportPerLine {
	/**
	 * 覆盖该方法来设定从第几行开始读取
	 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		String accID = ss[0].split(":")[1];
		String genUniID =ss[1].replace("ncbi-geneid:", "").replace("equivalent", "").trim();
		GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, genUniID, taxID);
		copedID.setUpdateAccID(accID);
		copedID.setUpdateDBinfo(DBAccIDSource.PPA_PichiaID, false);
		return copedID.update(true);
	}
}

/**
 * taxID 644223
 * Pichia_GS115_function-1009.txt
 * Need To Be Checked 
 * read from the second line
 * @author zong0jie
 *
 */
class Pipas_Fun extends ImportPerLine {
	/**
	 * 覆盖该方法来设定从第几行开始读取
	 */
	protected void setReadFromLine() {
		this.readFromLine = 2;
	}
	@Override
	boolean impPerLine(String lineContent) {
		boolean flag0 = true, flag1 = true, flag2 = true, flag3 = true;
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(ss[0], taxID);
		
		if (ss.length >= 2) {
			copedID.addUpdateRefAccID(ss[1]);
		}
		if (ss.length >= 3) {
			copedID.addUpdateRefAccID(ss[2]);
		}
		copedID.setUpdateDBinfo(DBAccIDSource.PPA_PichiaID, true);
		flag0 = copedID.update(true);
		
		if (ss.length >= 2) {
			copedID.setUpdateAccID(ss[1]);
			copedID.setUpdateDBinfo(DBAccIDSource.NCBI, false);
			flag1 = copedID.update(true);
		}
		if (ss.length >= 3) {
			copedID.setUpdateAccID(ss[2]);
			copedID.setUpdateDBinfo(DBAccIDSource.PPA_PichiaID, false);
			flag2 = copedID.update(true);
		}
		if (ss.length >= 4) {
			GeneInfo geneInfo = new GeneInfo();
			geneInfo.setDescrp(ss[3]);
			geneInfo.setDBinfo(DBAccIDSource.PPA_PichiaID.toString());
			copedID.setUpdateGeneInfo(geneInfo);
			flag3 = copedID.update(false);
		}
		return flag0 && flag1 && flag2 && flag3;
	}
}

class Pipas_GO_Slim extends ImportPerLine {
	/**
	 * 覆盖该方法来设定从第几行开始读取
	 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		ss[0] = "PAS_"+ss[0];
		GeneID copedID = new GeneID(ss[0], taxID);
		
		String[] ssGo = ss[1].split(";");
		for (String string2 : ssGo) {
			Gene2Go gene2Go = new Gene2Go();
			gene2Go.setGOID(string2);
			copedID.setUpdateDBinfo(DBAccIDSource.PPA_PichiaID, false);
			copedID.addUpdateGO(string2, DBAccIDSource.PPA_PichiaID, null, null, null);
		}
		return copedID.update(false);
	}
}
