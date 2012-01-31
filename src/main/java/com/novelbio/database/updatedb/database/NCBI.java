package com.novelbio.database.updatedb.database;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modcopeid.CopedID;

/**
 * 升级NCBI下载的所有文件的类
 * @author zong0jie
 *ID转换的文件网址：ftp://ftp.ncbi.nih.gov/gene/DATA/
 */
public class NCBI {
	public static void main(String[] args) {
		previewGZ("/Volumes/DATA/work/Bioinformatics/DataBase/GO/gene_association.goa_uniprot.gz");
	}
	/**
	 * 看gz压缩格式的文本的内容
	 */
	private static void previewGZ(String gzfile)
	{
		TxtReadandWrite txtRead = new TxtReadandWrite(TxtReadandWrite.GZIP, gzfile);
		int i = 0;
		Iterable<String> itString = txtRead.readlines(1);		
		for (String string : itString) {
			System.out.println(string);
			i ++;
			if (i>500) {
				break;
			}
		}
	}
}

/**
 * 将gene2accion和gene2refseq.gz这两个文件导入数据库，仅导入指定的物种
 * 文件格式如下<br>
 * tax_id	GeneID	status	RNA_ID	RNA_gi	pro_ID	pro_gi	  genomic_ID	genomic_gi	start_on_genomic_ID	end_on_genomic_ID	orientation	assembly<br>
9	1246502	-	-	-	CBC03500.1	257287470	HB661642.1	257287469	-	-	?	- <br>
9	1246502	VALIDATED	-	-	NP_047187.1	10954458	NC_001911.1	10954454	3040	4590	+	-<br>
9	1246503	-	-	-	AAD12601.1	3282741	AF041837.1	3282736	-	-	?	-
 */
class impGen2Acc extends ImportPerLine
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
 * 将gene2ensembl.gz这个文件导入数据库，每行的格式如下
 * tax_id GeneID Ensembl_gene_id RNA_ID Ensembl_rna_id protein_ID Ensembl_protein_id
   7227	30970	FBgn0040373	NM_130477.2	FBtr0070108	NP_569833.1	FBpp0070103
   7227	30970	FBgn0040373	NM_166834.1	FBtr0070107	NP_726658.1	FBpp0070102
 * @param content
 */
class impGen2Ensembl extends ImportPerLine
{
	/**
	 * 将gene2ensembl.gz这个文件导入数据库，每行的格式如下
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
 * 因为一个基因可能有有多个uniprotID，为提高效率，采用static设定的copedID，方便连续统计
 * 将gene_refseq_uniprotkb_collab.gz这个文件导入数据库，每行的格式如下
#Format: NCBI_protein_accession UniProtKB_protein_accession (tab is used as a separator, pound sign - start of a comment)
AP_000046	Q96678
AP_000047	Q96679
AP_000048	P68968
AP_000048	P68969
 * @param content
 */
class impGeneRef2UniID extends ImportPerLine
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
 * 将geneInfo.gz这个文件导入数据库
 * @param content
 */
class impGene2Info extends ImportPerLine
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
class impGene2Pub extends ImportPerLine
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
 * 导入GO信息，在导入Go2Term文件后导入该表
 * @author zong0jie
 *
 */
class impGene2GO extends ImportPerLine
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


