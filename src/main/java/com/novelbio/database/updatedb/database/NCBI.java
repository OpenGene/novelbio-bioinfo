package com.novelbio.database.updatedb.database;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 升级NCBI下载的所有文件的类
 * @author zong0jie
 *ID转换的文件网址：ftp://ftp.ncbi.nih.gov/gene/DATA/
 */
public class NCBI {
	String taxID = "";
	String gene2Acc = "";
	String gene2Ref = "";
	String gene2ensembl = "";
	String geneRef2UniID = "";
	String gene2Info = "";
	String gene2Pub = "";
	String goExtObo = "";
	String gene2GO = "";
	public static void main(String[] args) {
		
	}
	public void setTaxID(String taxID) {
		this.taxID = taxID;
	}
	public void setGene2AccFile(String gen2Acc, String gene2Ref) {
		this.gene2Acc = gen2Acc;
		this.gene2Ref = gene2Ref;
	}
	public void setGene2Ensembl(String gene2ensembl) {
		this.gene2ensembl = gene2ensembl;
	}
	public void setGeneRef2UniID(String geneRef2UniID) {
		this.geneRef2UniID = geneRef2UniID;
	}
	public void setGene2Info(String gene2Info) {
		this.gene2Info = gene2Info;
	}
	public void setGene2Pub(String gene2Pub) {
		this.gene2Pub = gene2Pub;
	}
	public void setGOExtObo(String goExtObo) {
		this.goExtObo = goExtObo;
	}
	public void setGene2GO(String gene2GO) {
		this.gene2GO = gene2GO;
	}
	public void importFile() {
		ImportPerLine impFile = null;
		impFile = new ImpGen2Acc();
		impFile.setTaxIDFile(taxID);
		impFile.updateFile(gene2Acc, true);
		impFile.updateFile(gene2Ref, true);
		impFile = new ImpGen2Ensembl();
		impFile.updateFile(gene2ensembl, true);
		impFile = new ImpGeneRef2UniID();
		impFile.updateFile(geneRef2UniID, true);
		impFile = new ImpGene2Info();
		impFile.updateFile(gene2Info, true);
		impFile = new ImpGene2Pub();
		impFile.updateFile(gene2Pub, true);
		impFile = new ImpGOExtObo();
		impFile.updateFile(goExtObo, false);
		impFile = new ImpGene2GO();
		impFile.updateFile(gene2GO, true);
	}
}

/**
 * 导入次序：1
 * 将gene2accion和gene2refseq.gz这两个文件导入数据库，仅导入指定的物种
 * 文件格式如下<br>
 * tax_id	GeneID	status	RNA_ID	RNA_gi	pro_ID	pro_gi	  genomic_ID	genomic_gi	start_on_genomic_ID	end_on_genomic_ID	orientation	assembly<br>
9	1246502	-	-	-	CBC03500.1	257287470	HB661642.1	257287469	-	-	?	- <br>
9	1246502	VALIDATED	-	-	NP_047187.1	10954458	NC_001911.1	10954454	3040	4590	+	-<br>
9	1246503	-	-	-	AAD12601.1	3282741	AF041837.1	3282736	-	-	?	-
 */
class ImpGen2Acc extends ImportPerLine
 {
	/**
	 * 将gene2accion和gene2refseq.gz这两个文件导入数据库，仅导入指定的物种
	 * 文件格式如下<br>
	 * tax_id	GeneID	status	RNA_ID	RNA_gi	pro_ID	pro_gi	  genomic_ID	genomic_gi	start_on_genomic_ID	end_on_genomic_ID	orientation	assembly<br>
	9	1246502	-	-	-	CBC03500.1	257287470	HB661642.1	257287469	-	-	?	- <br>
	9	1246502	VALIDATED	-	-	NP_047187.1	10954458	NC_001911.1	10954454	3040	4590	+	-<br>
	9	1246503	-	-	-	AAD12601.1	3282741	AF041837.1	3282736	-	-	?	-
	 */
	@Override
	protected boolean impPerLine(String content) {
		String[] ss = content.split("\t");
		int taxID = Integer.parseInt(ss[0]);
		if (!hashTaxID.contains(taxID)) {
			return true;
		}
		GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, ss[1], taxID);

		copedID.setUpdateAccID(ss[3]);
		if (ss[3].startsWith("NM_") || ss[3].startsWith("NR_"))
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_RNA,
					false);
		else
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_RNAAC, false);
		copedID.update(false);

		copedID.setUpdateAccID(ss[5]);
		if (ss[5].startsWith("NP_") || ss[5].startsWith("XP_")
				|| ss[5].startsWith("YP_"))
			copedID.setUpdateDBinfo(
					NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_PROTEIN, false);
		else
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_PROAC, false);
		copedID.update(false);

		copedID.setUpdateAccID(ss[6]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_PROGI, false);
		copedID.update(false);

		copedID.setUpdateAccID(ss[7]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_GENEAC, false);
		copedID.update(false);
		return true;
	}
}

/**
 * 导入次序：2
 * 将gene2ensembl.gz这个文件导入数据库，每行的格式如下
 * tax_id GeneID Ensembl_gene_id RNA_ID Ensembl_rna_id protein_ID Ensembl_protein_id
   7227	30970	FBgn0040373	NM_130477.2	FBtr0070108	NP_569833.1	FBpp0070103
   7227	30970	FBgn0040373	NM_166834.1	FBtr0070107	NP_726658.1	FBpp0070102
 * @param content
 */
class ImpGen2Ensembl extends ImportPerLine
{
	/**
	 * 将gene2ensembl.gz这个文件导入数据库，每行的格式如下
	 * tax_id GeneID Ensembl_gene_id RNA_ID Ensembl_rna_id protein_ID Ensembl_protein_id
       7227	30970	FBgn0040373	NM_130477.2	FBtr0070108	NP_569833.1	FBpp0070103
       7227	30970	FBgn0040373	NM_166834.1	FBtr0070107	NP_726658.1	FBpp0070102
	 * @param content
	 */
	@Override
	protected boolean impPerLine(String content) {
		String[] ss = content.split("\t");
		int taxID = Integer.parseInt(ss[0]);
		if (!hashTaxID.contains(taxID)) {
			return true;
		}
		GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, ss[1], taxID);
 
		
		copedID.setUpdateAccID(ss[2]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_GENE, false);
		copedID.update(false);
		
		copedID.setUpdateAccID(ss[3]);
		if (ss[3].startsWith("NM_") || ss[3].startsWith("NR_"))
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_RNA, true);
		else 
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_RNAAC, false);
		copedID.update(false);
		
		copedID.setUpdateAccID(ss[4]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_RNA, false);
		copedID.update(false);
		
		copedID.setUpdateAccID(ss[5]);
		if (ss[5].startsWith("NP_") || ss[5].startsWith("XP_") || ss[5].startsWith("YP_"))
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_REFSEQ_PROTEIN, true);
		else 
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_PROAC, false);
		copedID.update(false);
		
		copedID.setUpdateAccID(ss[6]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_ENSEMBL_PRO, false);
		copedID.update(false);
		return true;
	}
}
/**
 * 导入次序：3
 * 因为一个基因可能有有多个uniprotID，为提高效率，采用static设定的copedID，方便连续统计
 * 将gene_refseq_uniprotkb_collab.gz这个文件导入数据库，每行的格式如下
#Format: NCBI_protein_accession UniProtKB_protein_accession (tab is used as a separator, pound sign - start of a comment)
AP_000046	Q96678
AP_000047	Q96679
AP_000048	P68968
AP_000048	P68969
 * @param content
 */
class ImpGeneRef2UniID extends ImportPerLine
{
	static GeneID copedID;
	@Override
	protected boolean impPerLine(String content) {
		String[] ss = content.split("\t");
		if (copedID == null || !copedID.getAccID().equals(GeneID.removeDot(ss[0]))) {
			copedID = new GeneID(ss[0],0);
			if (copedID.getTaxID() == 0 || !hashTaxID.contains(copedID.getTaxID())) {
				return true;
			}
		}
		copedID.setUpdateAccID(ss[1]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_UNIPROT_GenralID, false);
		return copedID.update(false);
	}
}

/**
 * 导入次序：4
 * 将geneInfo.gz这个文件导入数据库
 * @param content
 */
class ImpGene2Info extends ImportPerLine
{
	@Override
	protected boolean impPerLine(String content) {
		String[] ss = content.split("\t");
		int taxID = Integer.parseInt(ss[0]);
		if (!hashTaxID.contains(taxID)) {
			return true;
		}
		GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, ss[1], taxID);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setDBinfo(NovelBioConst.DBINFO_NCBI_ACC_GenralID);
		geneInfo.setSep("\\|");
		geneInfo.setSymb(ss[2]); geneInfo.setLocTag(ss[3]);
		geneInfo.setSynonym(ss[4]); geneInfo.setDbXref(ss[5]);
		geneInfo.setChrm(ss[6]); geneInfo.setMapLoc(ss[7]);
		geneInfo.setDescrp(ss[8]); geneInfo.setTypeOfGene(ss[9]);
		geneInfo.setSymNom(ss[10]); geneInfo.setFullName(ss[11]);
		geneInfo.setNomState(ss[12]); geneInfo.setOtherDesg(ss[13]);
		geneInfo.setModDate(ss[14]);
		copedID.setUpdateGeneInfo(geneInfo);
		return copedID.update(false);
	}
}

/**
 * 导入次序：5
 * 在导入geneInfo后导入这个文件
 * 因为一个基因可能有有多篇文献，为提高效率，采用static设定的copedID，方便连续统计
 * 将gene2pubmed.gz这个文件导入数据库，每行的格式如下
 * #Format: tax_id GeneID PubMed_ID (tab is used as a separator, pound sign - start of a comment)
9	1246500	9873079
9	1246501	9873079
9	1246502	9812361
9	1246502	9873079
 * @param content
 */
class ImpGene2Pub extends ImportPerLine
{
	@Override
	protected boolean impPerLine(String content) {
		String[] ss = content.split("\t");
		int taxID = Integer.parseInt(ss[0]);
		if (!hashTaxID.contains(taxID)) {
			return true;
		}
		AGeneInfo geneInfo = new GeneInfo();
		GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, ss[1], taxID);
		geneInfo.setPubID(ss[2]);
		copedID.setUpdateGeneInfo(geneInfo);
		return copedID.update(false);
	}
}

/**
 * 导入次序：6
 * 导入GO信息，在导入Go2Term文件后导入该表
 * @author zong0jie
 *
 */
class ImpGene2GO extends ImportPerLine
{
	@Override
	protected boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		int taxID = Integer.parseInt(ss[0]);
		if (!hashTaxID.contains(taxID)) {
			return true;
		}
		GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, ss[1], taxID);
		if (ss[6] == null || ss[6].equals("") || ss[6].equals("-")) {
			copedID.setUpdateGO(ss[2], NovelBioConst.DBINFO_NCBI, ss[3], null, ss[4]);
		}
		else {
			copedID.setUpdateGO(ss[2], NovelBioConst.DBINFO_NCBI, ss[3], "PMID:"+ss[6], ss[4]);
		}
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI, false);
		return copedID.update(false);
	}
	void impEnd()
	{
	}
}


