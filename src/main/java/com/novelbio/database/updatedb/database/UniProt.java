package com.novelbio.database.updatedb.database;

import java.util.ArrayList;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modcopeid.CopedID;

/**
 * 升级UniProt下载的所有文件的类
 * @author zong0jie
 *UniProt文件的网址
 *ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping
 */
public class UniProt {
	
}

/**
 * 将idmapping_selected.tab导入数据库，仅导入指定的物种
 * 文件格式如下<br>
 1. UniProtKB-AC
2. UniProtKB-ID
3. GeneID (EntrezGene)
4. RefSeq
5. GI
6. PDB
7. GO
8. IPI
9. UniRef100
10. UniRef90
11. UniRef50
12. UniParc
13. PIR
14. NCBI-taxon
15. MIM
16. UniGene
17. PubMed
18. EMBL
19. EMBL-CDS
20. Ensembl
21. Ensembl_TRS
22. Ensembl_PRO
23. Additional PubMed
*/
class idmappingSelected extends ImportPerLine
 {
	/**
	 * 将无法升级的行写入该文本
	 */
	TxtReadandWrite txtWriteExcep = null;
	public void setTxtWriteExcep(String txtWriteExcep) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(txtWriteExcep, true);
	}
	/**
	 * 将idmapping_selected.tab导入数据库，仅导入指定的物种
	 * 文件格式如下<br>
	 1. UniProtKB-AC
	2. UniProtKB-ID
	3. GeneID (EntrezGene)
	4. RefSeq
	5. GI
	6. PDB
	7. GO
	8. IPI
	9. UniRef100
	10. UniRef90
	11. UniRef50
	12. UniParc
	13. PIR
	14. NCBI-taxon
	15. MIM
	16. UniGene
	17. PubMed
	18. EMBL
	19. EMBL-CDS
	20. Ensembl
	21. Ensembl_TRS
	22. Ensembl_PRO
	23. Additional PubMed
	*/
	@Override
	protected void impPerLine(String content) {
		String[] ss = content.split("\t");
		ss = ArrayOperate.copyArray(ss, 23);
		int taxID = Integer.parseInt(ss[13]);
		if (!hashTaxID.contains(taxID)) {
			return;
		}
		CopedID copedID = null;
		//如果geneID存在，那么就新建一个geneUniID的类
		ArrayList<String> lsRefAccID = new ArrayList<String>();
		if (!ss[2].equals("")) {
			if (!ss[2].contains(";")) {
				copedID = new CopedID(CopedID.IDTYPE_GENEID, ss[2], taxID);
			}
			else {
				String[] geneIDs = ss[2].split(";");
				for (String string : geneIDs) {
					copedID = new CopedID(CopedID.IDTYPE_GENEID, string, taxID);
					if (updateInfo(ss[0], copedID, NovelBioConst.DBINFO_UNIPROT_GenralID)) {
						txtWriteExcep.writefileln(content);
					}
					updateInfo(ss[1], copedID, NovelBioConst.DBINFO_UNIPROT_UNIPROTKB_ID);
					updateInfo(ss[7], copedID, NovelBioConst.DBINFO_IPI);
					updateInfo(ss[8].replace("UniRef100_", ""), copedID, NovelBioConst.DBINFO_UNIPROT_UNIGENE);
					updateInfo(ss[9].replace("UniRef90_", ""), copedID, NovelBioConst.DBINFO_UNIPROT_UNIGENE);
					updateInfo(ss[10].replace("UniRef50_", ""), copedID, NovelBioConst.DBINFO_UNIPROT_UNIGENE);
					updateInfo(ss[11], copedID, NovelBioConst.DBINFO_UNIPROT_UNIPARC);
					updateInfo(ss[12], copedID, NovelBioConst.DBINFO_PIR);
					updateInfo(ss[15], copedID, NovelBioConst.DBINFO_UNIPROT_UNIGENE);
					updateInfo(ss[17], copedID, NovelBioConst.DBINFO_EMBL);
					updateInfo(ss[18], copedID, NovelBioConst.DBINFO_EMBL_CDS);
					updateInfo(ss[19], copedID, NovelBioConst.DBINFO_ENSEMBL);
					updateInfo(ss[20], copedID, NovelBioConst.DBINFO_ENSEMBL_TRS);
					updateInfo(ss[21], copedID, NovelBioConst.DBINFO_ENSEMBL_PRO);
				}
				return;
			}
		}
		else {
			//就是要给一个完全没有的ID
			copedID = new CopedID("@@@", taxID);
			lsRefAccID = new ArrayList<String>();
			addListAccID(ss[3], lsRefAccID);
			addListAccID(ss[4], lsRefAccID);
			addListAccID(ss[15], lsRefAccID);
			addListAccID(ss[0], lsRefAccID);
			addListAccID(ss[19], lsRefAccID);
			addListAccID(ss[20], lsRefAccID);
			addListAccID(ss[21], lsRefAccID);
 			copedID.setUpdateRefAccID(lsRefAccID);
		}
		if (updateInfo(ss[0], copedID, NovelBioConst.DBINFO_UNIPROT_GenralID)) {
			txtWriteExcep.writefile(content);
		}
		updateInfo(ss[1], copedID, NovelBioConst.DBINFO_UNIPROT_UNIPROTKB_ID);
		updateInfo(ss[7], copedID, NovelBioConst.DBINFO_IPI);
		updateInfo(ss[8].replace("UniRef100_", ""), copedID, NovelBioConst.DBINFO_UNIPROT_UNIGENE);
		updateInfo(ss[9].replace("UniRef90_", ""), copedID, NovelBioConst.DBINFO_UNIPROT_UNIGENE);
		updateInfo(ss[10].replace("UniRef50_", ""), copedID, NovelBioConst.DBINFO_UNIPROT_UNIGENE);
		updateInfo(ss[11], copedID, NovelBioConst.DBINFO_UNIPROT_UNIPARC);
		updateInfo(ss[12], copedID, NovelBioConst.DBINFO_PIR);
		updateInfo(ss[15], copedID, NovelBioConst.DBINFO_UNIPROT_UNIGENE);
		updateInfo(ss[17], copedID, NovelBioConst.DBINFO_EMBL);
		updateInfo(ss[18], copedID, NovelBioConst.DBINFO_EMBL_CDS);
		updateInfo(ss[19], copedID, NovelBioConst.DBINFO_ENSEMBL);
		updateInfo(ss[20], copedID, NovelBioConst.DBINFO_ENSEMBL_TRS);
		updateInfo(ss[21], copedID, NovelBioConst.DBINFO_ENSEMBL_PRO);
	}
	
	private void addListAccID(String tmpAccID, ArrayList<String> lsRefAccID) {
		String[] ss = tmpAccID.split(";");
		for (String string : ss) {
			if (string != null && string.equals("")) {
				lsRefAccID.add(string);
			}
		}
	}
	
	private boolean updateInfo(String ssID, CopedID copedID, String dbInfo)
	{
		String[] ss = ssID.split(";");
		for (String string : ss) {
			copedID.setUpdateAccID(string);
			copedID.setUpdateDBinfo(dbInfo, false);
			if (!copedID.update(false)) {
				return false;
			}
		}
		return true;
	}
 }

/**
 * 将idmapping_selected.tab中的GO和pubmed信息导入数据库，仅导入指定的物种
 * 文件格式如下<br>
 1. UniProtKB-AC
2. UniProtKB-ID
3. GeneID (EntrezGene)
4. RefSeq
5. GI
6. PDB
7. GO
8. IPI
9. UniRef100
10. UniRef90
11. UniRef50
12. UniParc
13. PIR
14. NCBI-taxon
15. MIM
16. UniGene
17. PubMed
18. EMBL
19. EMBL-CDS
20. Ensembl
21. Ensembl_TRS
22. Ensembl_PRO
23. Additional PubMed
*/
class idmappingSelectedGOPubmed extends idmappingSelected
{
	/**
	 * 将idmapping_selected.tab导入数据库，仅导入指定的物种
	 * 文件格式如下<br>
	 1. UniProtKB-AC
	2. UniProtKB-ID
	3. GeneID (EntrezGene)
	4. RefSeq
	5. GI
	6. PDB
	7. GO
	8. IPI
	9. UniRef100
	10. UniRef90
	11. UniRef50
	12. UniParc
	13. PIR
	14. NCBI-taxon
	15. MIM
	16. UniGene
	17. PubMed
	18. EMBL
	19. EMBL-CDS
	20. Ensembl
	21. Ensembl_TRS
	22. Ensembl_PRO
	23. Additional PubMed
	*/
	@Override
	protected void impPerLine(String content) {
		String[] ss = content.split("\t");
		ss = ArrayOperate.copyArray(ss, 23);
		int taxID = Integer.parseInt(ss[13]);
		if (!hashTaxID.contains(taxID)) {
			return;
		}
		CopedID copedID = null;
		//如果geneID存在，那么就新建一个geneUniID的类
		ArrayList<String> lsRefAccID = new ArrayList<String>();
		if (!ss[2].equals("")) {
			if (!ss[2].contains(";")) {
				copedID = new CopedID(CopedID.IDTYPE_GENEID, ss[2], taxID);
			}
			else {
				String[] geneIDs = ss[2].split(";");
				for (String string : geneIDs) {
					copedID = new CopedID(CopedID.IDTYPE_GENEID, string, taxID);
					if (copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
						txtWriteExcep.writefileln(content);
					}
					updateGO(ss[6], copedID, NovelBioConst.DBINFO_UNIPROTID);
					updatePubmed(ss[16], copedID);
				}
				return;
			}
		}
		else {
			//就是要给一个完全没有的ID
			copedID = new CopedID("@@@", taxID);
			lsRefAccID = new ArrayList<String>();
			addListAccID(ss[3], lsRefAccID);
			addListAccID(ss[4], lsRefAccID);
			addListAccID(ss[15], lsRefAccID);
			addListAccID(ss[0], lsRefAccID);
			addListAccID(ss[19], lsRefAccID);
			addListAccID(ss[20], lsRefAccID);
			addListAccID(ss[21], lsRefAccID);
 			copedID.setUpdateRefAccID(lsRefAccID);
		}
		if (copedID.getIDtype().equals(CopedID.IDTYPE_ACCID)) {
			txtWriteExcep.writefile(content);
		}
		updateGO(ss[6], copedID, NovelBioConst.DBINFO_UNIPROT_GenralID);
		updatePubmed(ss[16], copedID);
	}
	
	private void addListAccID(String tmpAccID, ArrayList<String> lsRefAccID) {
		String[] ss = tmpAccID.split(";");
		for (String string : ss) {
			if (string != null && string.equals("")) {
				lsRefAccID.add(string);
			}
		}
	}
	
	
	private boolean updateGO(String ssGOID, CopedID copedID, String dbInfo)
	{
		String[] ss = ssGOID.split(";");
		for (String string : ss) {
			copedID.setUpdateGO(string, dbInfo, "", "", NovelBioConst.DBINFO_UNIPROTID);
		}
		if (!copedID.update(false)) {
			return false;
		}
		return true;
	}
	private boolean updatePubmed(String ssPubmed, CopedID copedID)
	{
		String[] ss = ssPubmed.split(";");
		for (String string : ss) {
			GeneInfo geneInfo = new GeneInfo();
			geneInfo.setPubmedID(string);
			copedID.setUpdateGeneInfo(geneInfo);
			if (!copedID.update(false)) {
				return false;
			}
		}
		return true;
	}
}