package com.novelbio.database.updatedb.database;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modcopeid.CopedID;

/**
 * ����NCBI���ص������ļ�����
 * @author zong0jie
 *IDת�����ļ���ַ��ftp://ftp.ncbi.nih.gov/gene/DATA/
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
		impFile.setTaxID(taxID);
		impFile.importInfoPerLine(gene2Acc, true);
//		impFile.importInfoPerLine(gene2Ref, true);
//		impFile = new ImpGen2Ensembl();
//		impFile.importInfoPerLine(gene2ensembl, true);
//		impFile = new ImpGeneRef2UniID();
//		impFile.importInfoPerLine(geneRef2UniID, true);
//		impFile = new ImpGene2Info();
//		impFile.importInfoPerLine(gene2Info, true);
//		impFile = new ImpGene2Pub();
//		impFile.importInfoPerLine(gene2Pub, true);
//		impFile = new ImpGOExtObo();
//		impFile.importInfoPerLine(goExtObo, true);
//		impFile = new ImpGene2GO();
//		impFile.importInfoPerLine(gene2GO, true);
	}
}

/**
 * �������1
 * ��gene2accion��gene2refseq.gz�������ļ��������ݿ⣬������ָ��������
 * �ļ���ʽ����<br>
 * tax_id	GeneID	status	RNA_ID	RNA_gi	pro_ID	pro_gi	  genomic_ID	genomic_gi	start_on_genomic_ID	end_on_genomic_ID	orientation	assembly<br>
9	1246502	-	-	-	CBC03500.1	257287470	HB661642.1	257287469	-	-	?	- <br>
9	1246502	VALIDATED	-	-	NP_047187.1	10954458	NC_001911.1	10954454	3040	4590	+	-<br>
9	1246503	-	-	-	AAD12601.1	3282741	AF041837.1	3282736	-	-	?	-
 */
class ImpGen2Acc extends ImportPerLine
 {
	/**
	 * ��gene2accion��gene2refseq.gz�������ļ��������ݿ⣬������ָ��������
	 * �ļ���ʽ����<br>
	 * tax_id	GeneID	status	RNA_ID	RNA_gi	pro_ID	pro_gi	  genomic_ID	genomic_gi	start_on_genomic_ID	end_on_genomic_ID	orientation	assembly<br>
	9	1246502	-	-	-	CBC03500.1	257287470	HB661642.1	257287469	-	-	?	- <br>
	9	1246502	VALIDATED	-	-	NP_047187.1	10954458	NC_001911.1	10954454	3040	4590	+	-<br>
	9	1246503	-	-	-	AAD12601.1	3282741	AF041837.1	3282736	-	-	?	-
	 */
	@Override
	protected void impPerLine(String content) {
		String[] ss = content.split("\t");
		int taxID = Integer.parseInt(ss[0]);
		if (!hashTaxID.contains(taxID)) {
			return;
		}
		CopedID copedID = new CopedID(CopedID.IDTYPE_GENEID, ss[1], taxID);

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
	}
}

/**
 * �������2
 * ��gene2ensembl.gz����ļ��������ݿ⣬ÿ�еĸ�ʽ����
 * tax_id GeneID Ensembl_gene_id RNA_ID Ensembl_rna_id protein_ID Ensembl_protein_id
   7227	30970	FBgn0040373	NM_130477.2	FBtr0070108	NP_569833.1	FBpp0070103
   7227	30970	FBgn0040373	NM_166834.1	FBtr0070107	NP_726658.1	FBpp0070102
 * @param content
 */
class ImpGen2Ensembl extends ImportPerLine
{
	/**
	 * ��gene2ensembl.gz����ļ��������ݿ⣬ÿ�еĸ�ʽ����
	 * tax_id GeneID Ensembl_gene_id RNA_ID Ensembl_rna_id protein_ID Ensembl_protein_id
       7227	30970	FBgn0040373	NM_130477.2	FBtr0070108	NP_569833.1	FBpp0070103
       7227	30970	FBgn0040373	NM_166834.1	FBtr0070107	NP_726658.1	FBpp0070102
	 * @param content
	 */
	@Override
	protected void impPerLine(String content) {
		String[] ss = content.split("\t");
		int taxID = Integer.parseInt(ss[0]);
		if (!hashTaxID.contains(taxID)) {
			return;
		}
		CopedID copedID = new CopedID(CopedID.IDTYPE_GENEID, ss[1], taxID);
 
		
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
	}
}
/**
 * �������3
 * ��Ϊһ������������ж��uniprotID��Ϊ���Ч�ʣ�����static�趨��copedID����������ͳ��
 * ��gene_refseq_uniprotkb_collab.gz����ļ��������ݿ⣬ÿ�еĸ�ʽ����
#Format: NCBI_protein_accession UniProtKB_protein_accession (tab is used as a separator, pound sign - start of a comment)
AP_000046	Q96678
AP_000047	Q96679
AP_000048	P68968
AP_000048	P68969
 * @param content
 */
class ImpGeneRef2UniID extends ImportPerLine
{
	static CopedID copedID;
	@Override
	protected void impPerLine(String content) {
		String[] ss = content.split("\t");
		if (copedID == null || !copedID.getAccID().equals(CopedID.removeDot(ss[0]))) {
			copedID = new CopedID(ss[0],0);
			if (!hashTaxID.contains(copedID.getTaxID())) {
				return;
			}
		}
		copedID.setUpdateAccID(ss[1]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_UNIPROT_GenralID, false);
		copedID.update(false);
	}
}

/**
 * �������4
 * ��geneInfo.gz����ļ��������ݿ�
 * @param content
 */
class ImpGene2Info extends ImportPerLine
{
	@Override
	protected void impPerLine(String content) {
		String[] ss = content.split("\t");
		int taxID = Integer.parseInt(ss[0]);
		if (!hashTaxID.contains(taxID)) {
			return;
		}
		CopedID copedID = new CopedID(CopedID.IDTYPE_GENEID, ss[1], taxID);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setSymbol(ss[2]); geneInfo.setLocusTag(ss[3]);
		geneInfo.setSynonyms(ss[4]); geneInfo.setDbXrefs(ss[5]);
		geneInfo.setChromosome(ss[6]); geneInfo.setMapLocation(ss[7]);
		geneInfo.setDescription(ss[8]); geneInfo.setTypeOfGene(ss[9]);
		geneInfo.setSymNome(ss[10]); geneInfo.setFullName(ss[11]);
		geneInfo.setNomStat(ss[12]); geneInfo.setOtherDesign(ss[13]);
		geneInfo.setModDate(ss[14]);
		geneInfo.setDBinfo(NovelBioConst.DBINFO_NCBI_ACC_GenralID);
		copedID.setUpdateGeneInfo(geneInfo);
		copedID.update(false);
	}
}

/**
 * �������5
 * �ڵ���geneInfo��������ļ�
 * ��Ϊһ������������ж�ƪ���ף�Ϊ���Ч�ʣ�����static�趨��copedID����������ͳ��
 * ��gene2pubmed.gz����ļ��������ݿ⣬ÿ�еĸ�ʽ����
 * #Format: tax_id GeneID PubMed_ID (tab is used as a separator, pound sign - start of a comment)
9	1246500	9873079
9	1246501	9873079
9	1246502	9812361
9	1246502	9873079
 * @param content
 */
class ImpGene2Pub extends ImportPerLine
{
	static CopedID copedID;

	@Override
	protected void impPerLine(String content) {
		String[] ss = content.split("\t");
		int taxID = Integer.parseInt(ss[0]);
		if (!hashTaxID.contains(taxID)) {
			return;
		}
		AGeneInfo geneInfo = new GeneInfo();
		if (copedID == null || !copedID.getGenUniID().equals(ss[1])) {
			copedID = new CopedID(CopedID.IDTYPE_GENEID, ss[1], taxID);
		}
		geneInfo.setPubmedID(ss[2]);
		copedID.setUpdateGeneInfo(geneInfo);
		copedID.update(false);
	}
}

/**
 * �������6
 * ����GO��Ϣ���ڵ���Go2Term�ļ�����ñ�
 * @author zong0jie
 *
 */
class ImpGene2GO extends ImportPerLine
{
	static CopedID copedID;
	@Override
	void impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		int taxID = Integer.parseInt(ss[0]);
		if (!hashTaxID.contains(taxID)) {
			return;
		}
		if (copedID == null || !copedID.getGenUniID().equals(ss[1])) {
			if (copedID != null) {
				copedID.update(false);
			}
			copedID = new CopedID(CopedID.IDTYPE_GENEID, ss[1], taxID);
		}
		copedID.setUpdateGO(ss[2], NovelBioConst.DBINFO_NCBI, ss[3], "PMID:"+ss[6], ss[4]);
	}
	void impEnd()
	{
		copedID.update(false);
	}
}

