package com.novelbio.database.updatedb.database;

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.Iterator;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.CopedID;

/**
 * ����NCBI���ص������ļ�����
 * @author zong0jie
 *IDת�����ļ���ַ��ftp://ftp.ncbi.nih.gov/gene/DATA/
 */
public class NCBI {
	public static void main(String[] args) {
		previewGZ("/Volumes/DATA/work/Bioinformatics/DataBase/gene2refseq.gz");
	}
	/**
	 * ��gzѹ����ʽ���ı�������
	 */
	private static void previewGZ(String gzfile)
	{
		TxtReadandWrite txtRead = new TxtReadandWrite(TxtReadandWrite.GZIP, gzfile);
		int i = 0;
		Iterable<String> itString = txtRead.readlines(1);		
		for (String string : itString) {
			System.out.println(string);
			i ++;
			if (i>100) {
				break;
			}
		}
	}
	HashSet<String> hashTaxID=new HashSet<String>();

	/**
	 * ������Ҫ��������ȡ���������������ݿ���
	 * ͬʱ��¼��hashmap�У����������ݿ⵼��ͽ�������Щָ����������
	 * ��һ���Ǿ����taxID
	 * @param taxIDfile
	 */
	private void setTaxID(String taxIDfile) {
		TxtReadandWrite txtTaxID=new TxtReadandWrite(taxIDfile, false);
		for (String string : txtTaxID.readlines()) {
			String[] ss=string.split("\t");
			hashTaxID.add(ss[0]);
		}
	}
	
	/**
	 * ��gene2accion����ļ��������ݿ⣬������ָ��������
	 * �ļ���ʽ����<br>
		 * tax_id	GeneID	status	RNA_ID	RNA_gi	pro_ID	pro_gi	  genomic_ID	genomic_gi	start_on_genomic_ID	end_on_genomic_ID	orientation	assembly<br>
    9	1246502	-	-	-	CBC03500.1	257287470	HB661642.1	257287469	-	-	?	- <br>
    9	1246502	VALIDATED	-	-	NP_047187.1	10954458	NC_001911.1	10954454	3040	4590	+	-<br>
    9	1246503	-	-	-	AAD12601.1	3282741	AF041837.1	3282736	-	-	?	-
	 */
	@SuppressWarnings("unused")
	private void importGene2Acc(String gene2AccFile, boolean gzip) {
		TxtReadandWrite txtGene2Acc;
		if (gzip)
			txtGene2Acc = new TxtReadandWrite(TxtReadandWrite.GZIP, gene2AccFile);
		else 
			txtGene2Acc = new TxtReadandWrite(gene2AccFile, false);
		//�ӵڶ��п�ʼ��ȡ
		for (String content : txtGene2Acc.readlines(2)) {
			String[] ss = content.split("\t");
			
		}
		
	}

	
	
	
}


interface importPerLine
{
	/**
	 * �趨taxID��
	 * @param hashTaxID
	 */
	void setTaxID(HashSet<Integer> hashTaxID);
	/**
	 * ���д��������Ϣ
	 * @param lineContent
	 */
	void impPerLine(String lineContent);
}
/**
 * ��gene2accion��gene2refseq.gz�������ļ��������ݿ⣬������ָ��������
 * �ļ���ʽ����<br>
 * tax_id	GeneID	status	RNA_ID	RNA_gi	pro_ID	pro_gi	  genomic_ID	genomic_gi	start_on_genomic_ID	end_on_genomic_ID	orientation	assembly<br>
9	1246502	-	-	-	CBC03500.1	257287470	HB661642.1	257287469	-	-	?	- <br>
9	1246502	VALIDATED	-	-	NP_047187.1	10954458	NC_001911.1	10954454	3040	4590	+	-<br>
9	1246503	-	-	-	AAD12601.1	3282741	AF041837.1	3282736	-	-	?	-
 */
class impGen2Acc implements importPerLine
 {
	HashSet<Integer> hashTaxID = null;
	public void setTaxID(HashSet<Integer> hashTaxID) {
		this.hashTaxID = hashTaxID;
	}
	/**
	 * ��gene2accion����ļ��������ݿ⣬������ָ��������
	 * �ļ���ʽ����<br>
	 * tax_id	GeneID	status	RNA_ID	RNA_gi	pro_ID	pro_gi	  genomic_ID	genomic_gi	start_on_genomic_ID	end_on_genomic_ID	orientation	assembly<br>
	9	1246502	-	-	-	CBC03500.1	257287470	HB661642.1	257287469	-	-	?	- <br>
	9	1246502	VALIDATED	-	-	NP_047187.1	10954458	NC_001911.1	10954454	3040	4590	+	-<br>
	9	1246503	-	-	-	AAD12601.1	3282741	AF041837.1	3282736	-	-	?	-
	 */
	@Override
	public void impPerLine(String content) {
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
 * ��gene2ensembl.gz����ļ��������ݿ⣬ÿ�еĸ�ʽ����
 * tax_id GeneID Ensembl_gene_id RNA_ID Ensembl_rna_id protein_ID Ensembl_protein_id
   7227	30970	FBgn0040373	NM_130477.2	FBtr0070108	NP_569833.1	FBpp0070103
   7227	30970	FBgn0040373	NM_166834.1	FBtr0070107	NP_726658.1	FBpp0070102
 * @param content
 */
class impGen2Ensembl implements importPerLine
{
	HashSet<Integer> hashTaxID = null;
	public void setTaxID(HashSet<Integer> hashTaxID) {
		this.hashTaxID = hashTaxID;
	}
	/**
	 * ��gene2ensembl.gz����ļ��������ݿ⣬ÿ�еĸ�ʽ����
	 * tax_id GeneID Ensembl_gene_id RNA_ID Ensembl_rna_id protein_ID Ensembl_protein_id
       7227	30970	FBgn0040373	NM_130477.2	FBtr0070108	NP_569833.1	FBpp0070103
       7227	30970	FBgn0040373	NM_166834.1	FBtr0070107	NP_726658.1	FBpp0070102
	 * @param content
	 */
	@Override
	public void impPerLine(String content) {
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




