package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.model.geneanno.GeneInfo;

/**
 * 升级UniProt下载的所有文件的类
 * @author zong0jie
 *UniProt文件的网址
 *ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/idmapping
 */
public class UniProt {
	private static final Logger logger = Logger.getLogger(UniProt.class);
	
	String outUniIDFile = "";
	String idmappingSelectedFile = "";
	String taxIDFile = "";
	String gene_associationgoa_uniprotFile = "";
	public void setGene_association_goa_uniprot(String gene_associationgoa_uniprotFile) {
		this.gene_associationgoa_uniprotFile = gene_associationgoa_uniprotFile;
	}
	public void setTaxIDFile(String taxIDFile) {
		this.taxIDFile = taxIDFile;
	}
	public void setOutUniIDFile(String outUniIDFile) {
		this.outUniIDFile = outUniIDFile;
	}
	public void setIdmappingSelectedFile(String idmappingSelectedFile) {
		this.idmappingSelectedFile = idmappingSelectedFile;
	}
	public void update() {
		IdmappingSelected idmappingSelected = new IdmappingSelected();
		IdmappingSelected.setTaxIDFile(taxIDFile);
		idmappingSelected.setTxtWriteExcep(outUniIDFile);
		idmappingSelected.updateFile(idmappingSelectedFile);
		logger.info("finished Import: " + idmappingSelectedFile);
		
		Gene_associationgoa_uniprot impgene_associationgoa_uniprot = new Gene_associationgoa_uniprot();
		if (FileOperate.isFileDirectory(gene_associationgoa_uniprotFile)) {
			List<String> lsFiles = FileOperate.getLsFoldFileName(gene_associationgoa_uniprotFile);
			for (String goaFile : lsFiles) {
				if (!goaFile.contains("_association.goa_")) {
					continue;
				}
				impgene_associationgoa_uniprot.updateFile(goaFile);
				logger.info("finished Import: " + gene_associationgoa_uniprotFile);
			}
		} else {
			impgene_associationgoa_uniprot.updateFile(gene_associationgoa_uniprotFile);
			logger.info("finished Import: " + gene_associationgoa_uniprotFile);
		}
		
		idmappingSelected = new IdmappingSelected();
		idmappingSelected.setUpdateUniprotID(true);
		idmappingSelected.setTxtWriteExcep(FileOperate.changeFileSuffix(outUniIDFile, "_2", null));
		idmappingSelected.updateFile(outUniIDFile);
		logger.info("finished Import: " + outUniIDFile);

		IdmappingSelectedGOPubmed idmappingSelectedGOPubmed = new IdmappingSelectedGOPubmed();
		idmappingSelectedGOPubmed.setTxtWriteExcep(FileOperate.changeFileSuffix(idmappingSelectedFile, "_failed", "txt"));
		idmappingSelectedGOPubmed.updateFile(idmappingSelectedFile);
		logger.info("finished Import GO pubmed: " + idmappingSelectedFile);
		
		idmappingSelectedGOPubmed = new IdmappingSelectedGOPubmed();
		idmappingSelectedGOPubmed.updateFile(FileOperate.changeFileSuffix(idmappingSelectedFile, "_failed", "txt"));
		logger.info("finished Import GO pubmed: " + FileOperate.changeFileSuffix(idmappingSelectedFile, "_failed", "txt"));
	}
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
class IdmappingSelected extends ImportPerLine {
	/**
	 * 搜索不到的ID是否导入uniID表中
	 */
	boolean updateUniprotID = false;
	/**
	 * 搜索不到的ID是否导入uniID表中，默认false
	 */
	public void setUpdateUniprotID(boolean updateUniprotID) {
		this.updateUniprotID = updateUniprotID;
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
	8. UniRef100
	9. UniRef90
	10. UniRef50
	11. UniParc
	12. PIR
	13. NCBI-taxon
	14. MIM
	15. UniGene
	16. PubMed
	17. EMBL
	18. EMBL-CDS
	19. Ensembl
	20. Ensembl_TRS
	21. Ensembl_PRO
	22. Additional PubMed
	*/
	@Override
	protected boolean impPerLine(String content) {
		String[] ss = content.split("\t");
		ss = ArrayOperate.copyArray(ss, 22);
		int taxID = Integer.parseInt(ss[12]);
		if (!setTaxID.contains(taxID)) {
			return true;
		}
		GeneID copedID = null;
		//如果geneID存在，那么就新建一个geneUniID的类
		ArrayList<String> lsRefAccID = new ArrayList<String>();
		if (!ss[2].equals("") && !ss[2].contains(";")) {
			copedID = new GeneID(GeneID.IDTYPE_GENEID, ss[2], taxID);
		}
		else {
			//就是要给一个完全没有的ID
			copedID = new GeneID("", taxID);
			lsRefAccID = new ArrayList<String>();
			lsRefAccID.addAll(getListAccID(ss[3]));
			lsRefAccID.addAll(getListAccID(ss[4]));
			lsRefAccID.addAll(getListAccID(ss[14]));
			lsRefAccID.addAll(getListAccID(ss[0]));
			lsRefAccID.addAll(getListAccID(ss[18]));
			lsRefAccID.addAll(getListAccID(ss[19]));
			lsRefAccID.addAll(getListAccID(ss[20]));
 			copedID.setUpdateRefAccID(lsRefAccID);
		}
		if (!updateInfo(ss[0], copedID, DBAccIDSource.Uniprot)) {
			return false;
		}
		updateInfo(ss[1], copedID, DBAccIDSource.UniprotKB_ID);
//		updateInfo(ss[7], copedID, DBAccIDSource.IPI);
//		updateInfo(ss[8].replace("UniRef100_", ""), copedID, DBAccIDSource.UniprotUniGene);
//		updateInfo(ss[9].replace("UniRef90_", ""), copedID, DBAccIDSource.UniprotUniGene);
//		updateInfo(ss[10].replace("UniRef50_", ""), copedID, DBAccIDSource.UniprotUniGene);
		updateInfo(ss[10], copedID, DBAccIDSource.UniprotPARC);
		updateInfo(ss[11], copedID, DBAccIDSource.PIR);
		updateInfo(ss[14], copedID, DBAccIDSource.UniprotUniGene);
		updateInfo(ss[16], copedID, DBAccIDSource.EMBL);
		updateInfo(ss[17], copedID, DBAccIDSource.EMBL_CDS);
		updateInfo(ss[18], copedID, DBAccIDSource.Ensembl);
		updateInfo(ss[19], copedID, DBAccIDSource.Ensembl_TRS);
		updateInfo(ss[20], copedID, DBAccIDSource.Ensembl_Pro);
		return true;
	}
	/**
	 * 将tmpAccID装入lsRefAccID中
	 * @param tmpAccID
	 * @param lsRefAccID
	 */
	private List<String> getListAccID(String tmpAccID) {
		ArrayList<String> lsRefAccID = new ArrayList<String>();
		if (tmpAccID == null || tmpAccID.equals("")) {
			return new ArrayList<String>();
		}
		String[] ss = tmpAccID.split(";");
		for (String string : ss) {
			if (string != null && !string.equals("")) {
				lsRefAccID.add(string);
			}
		}
		return lsRefAccID;
	}
	/**
	 * 升级ssID到geneID中
	 * @param ssID
	 * @param copedID
	 * @param dbInfo
	 * @param updateUniprotID 搜索不到的ID是否导入uniID表中
	 * @return
	 */
	private boolean updateInfo(String ssID, GeneID copedID, DBAccIDSource dbInfo) {
		if (ssID == null || ssID.equals("")) {
			return true;
		}
		String[] ss = ssID.split(";");
		for (String string : ss) {
			copedID.setUpdateAccID(string);
			copedID.setUpdateDBinfo(dbInfo, false);
			if (!copedID.update(updateUniprotID)) {
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
class IdmappingSelectedGOPubmed extends IdmappingSelected {
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
	8. UniRef100
	9. UniRef90
	10. UniRef50
	11. UniParc
	12. PIR
	13. NCBI-taxon
	14. MIM
	15. UniGene
	16. PubMed
	18. EMBL
	19. EMBL-CDS
	20. Ensembl
	21. Ensembl_TRS
	22. Ensembl_PRO
	23. Additional PubMed
	*/
	@Override
	public boolean impPerLine(String content) {
		String[] ss = content.split("\t");
		ss = ArrayOperate.copyArray(ss, 22);
		int taxID = Integer.parseInt(ss[12]);
		if (!setTaxID.contains(taxID)) {
			return true;
		}
		GeneID copedID = null;
		//如果geneID存在，那么就新建一个geneUniID的类
		ArrayList<String> lsRefAccID = new ArrayList<String>();
		if (!ss[2].equals("") && !ss[2].contains(";")) {
			copedID = new GeneID(GeneID.IDTYPE_GENEID, ss[2], taxID);
		} else {
			//就是要给一个完全没有的ID
			copedID = new GeneID("", taxID);
			lsRefAccID = new ArrayList<String>();
			addListAccID(ss[3], lsRefAccID);
			addListAccID(ss[4], lsRefAccID);
			addListAccID(ss[14], lsRefAccID);
			addListAccID(ss[0], lsRefAccID);
			addListAccID(ss[18], lsRefAccID);
			addListAccID(ss[19], lsRefAccID);
			addListAccID(ss[20], lsRefAccID);
 			copedID.setUpdateRefAccID(lsRefAccID);
		}
		if (copedID.getIDtype() == GeneID.IDTYPE_ACCID) {
			return false;
		}
		if (ss[6] != null && !ss[6].equals("")) {
			updateGO(ss[6], copedID, DBAccIDSource.Uniprot);
		}
		if (ss[15] != null && !ss[15].equals("")) {
			updatePubmed(ss[15], copedID);
		}
		return true;
	}
	
	private void addListAccID(String tmpAccID, ArrayList<String> lsRefAccID) {
		if (tmpAccID == null || tmpAccID.equals("")) {
			return;
		}
		String[] ss = tmpAccID.split(";");
		for (String string : ss) {
			if (string != null && !string.equals("")) {
				lsRefAccID.add(string);
			}
		}
	}
	
	private boolean updateGO(String ssGOID, GeneID copedID, DBAccIDSource dbInfo) {
		String[] ss = ssGOID.split(";");
		for (String string : ss) {
			copedID.addUpdateGO(string, dbInfo, "", null, "");
		}
		if (!copedID.update(false)) {
			return false;
		}
		return true;
	}
	
	private boolean updatePubmed(String ssPubmed, GeneID copedID) {
		String[] ss = ssPubmed.split(";");
		for (String string : ss) {
			GeneInfo geneInfo = new GeneInfo();
			geneInfo.addPubID(string);
			copedID.setUpdateGeneInfo(geneInfo);
			if (!copedID.update(false)) {
				return false;
			}
		}
		return true;
	}
}
