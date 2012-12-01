package com.novelbio.database.updatedb.database;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.NovelBioConst;

public class AmiGO {
	/**
	 * http://www.geneontology.org/GO.downloads.files.shtml
	 * GO.terms_alt_ids
	 */
	
	/**
	 * http://www.geneontology.org/GO.downloads.annotations.shtml
	 * annotation
	 */
	
//	ImpGen2Acc
	
}


class ImpGOExtObo extends ImportPerLine {
	private static Logger logger = Logger.getLogger(ImpGOExtObo.class);
	
	/** queryID��ʵ��ID */
	HashMap<String, String> mapGOquery2GOID = new HashMap<String, String>();
	
	/**
	 * ��Ϊ��Ҫ���еĵ���
	 * ���Ը��Ƿ���
	 */
	@Override
	public void updateFile(String gene2AccFile, boolean gzip) {
		TxtReadandWrite txtGene2Acc;
		if (gzip) {
			txtGene2Acc = new TxtReadandWrite(TxtReadandWrite.GZIP, gene2AccFile);
		} else {
			txtGene2Acc = new TxtReadandWrite(gene2AccFile, false);
		}
		
		//�ӵڶ��п�ʼ��ȡ����һ�ε���
		String tmpContent = null;
		for (String content : txtGene2Acc.readlines(2)) {
			if (content.contains("GO:0030530")) {
				logger.error("stop");
			}
			
			if (content.startsWith("[Term]")) {
				tmpContent = content;
				continue;
			}
			if (content.equals("")) {
				impPerLine(tmpContent);
				tmpContent = null;
			}
			if (tmpContent != null) {
				tmpContent = tmpContent + "\r\n" + content;
			}
		}
		//�ӵڶ��п�ʼ��ȡ���ڶ��ε���
		for (String content : txtGene2Acc.readlines(2)) {
			if (content.startsWith("[Term]")) {
				tmpContent = content;
				continue;
			}
			if (content.equals("")) {
				impPerLineObsolete(tmpContent);
				tmpContent = null;
			}
			if (tmpContent != null) {
				tmpContent = tmpContent + "\r\n" + content;
			}
		}
		copeMapGOquery2GOID();
		updateConvertID();
		//�ӵڶ��п�ʼ��ȡ�������ε���
		for (String content : txtGene2Acc.readlines(2)) {
			if (content.startsWith("[Term]")) {
				tmpContent = content;
				continue;
			}
			if (content.equals("")) {
				impPerLineChild(tmpContent);
				tmpContent = null;
			}
			if (tmpContent != null) {
				tmpContent = tmpContent + "\r\n" + content;
			}
		}
		txtGene2Acc.close();
		if (txtWriteExcep != null) {
			txtWriteExcep.close();
		}
	}
	
	/**
	 * ��һ�ε������е���Ϣ��
	 */
	@Override
	public boolean impPerLine(String lineContent) {
		if (lineContent == null || lineContent.equals("")) {
			return true;
		}
		String[] ss = lineContent.split("\r\n");
		if (lineContent.contains("is_obsolete: true")) {
			return true;
		}
		ArrayList<String> lsQueryID = new ArrayList<String>();
		Go2Term go2Term = new Go2Term();
		for (String string : ss) {
			//GOID
			if (string.startsWith("id:")) {
				go2Term.setGoID(string.replace("id:", "").trim());
			}
			//GOterm
			if (string.startsWith("name:")) {
				go2Term.setGoTerm(string.replace("name:", "").trim());
			}
			//GO Function
			if (string.startsWith("namespace: ")) {
				if (string.equals("namespace: biological_process")) {
					go2Term.setGoFunction(Go2Term.FUN_SHORT_BIO_P);
				}
				if (string.equals("namespace: molecular_function")) {
					go2Term.setGoFunction(Go2Term.FUN_SHORT_MOL_F);
				}
				if (string.equals("namespace: cellular_component")) {
					go2Term.setGoFunction(Go2Term.FUN_SHORT_CEL_C);
				}
			}
			//GO Definition
			if (string.startsWith("def:")) {
				go2Term.setDefinition(string.replace("def:", "").trim());
			}
			//Parent
			if (string.startsWith("is_a:")) {
				String parentGOID = extractGOID(string);
				if (parentGOID == null) {
					logger.error("is_a ��û�ж�Ӧ��GOID��" + string);
				}
				else {
					go2Term.addParent(parentGOID, Go2Term.RELATION_IS);
				}
			}
			if (string.startsWith("is_a:")) {
				String parentGOID = extractGOID(string);
				if (parentGOID == null) {
					logger.error("is_a ��û�ж�Ӧ��GOID��" + string);
				}
				else {
					go2Term.addParent(parentGOID, Go2Term.RELATION_IS);
				}
			}
			if (string.startsWith("relationship:")) {
				String parentGOID = extractGOID(string);
				if (parentGOID == null) {
					logger.error("is_a ��û�ж�Ӧ��GOID��" + string);
					return false;
				}
				if (string.contains("part_of")) {
					go2Term.addParent(parentGOID, Go2Term.RELATION_PARTOF);
				}
				else if (string.contains("negatively_regulates")) {
					go2Term.addParent(parentGOID, Go2Term.RELATION_REGULATE_NEG);
				}
				else if (string.contains("regulates")) {
					go2Term.addParent(parentGOID, Go2Term.RELATION_REGULATE);
				}
				else if (string.contains("positively_regulates")) {
					go2Term.addParent(parentGOID, Go2Term.RELATION_REGULATE_POS);
				}
			}
			if (string.startsWith("alt_id:")) {
				lsQueryID.add(extractGOID(string));
			}
		}
		go2Term.update();
		
		mapGOquery2GOID.put(go2Term.getGoID(), go2Term.getGoID());
		for (String string2 : lsQueryID) {
			mapGOquery2GOID.put(string2, go2Term.getGoID());
		}
		return true;
	}
	
	/**
	 * �ڶ��βŵ��� ��ʱGO ��Ϣ
	 */
	void impPerLineObsolete(String lineContent) {
		if (lineContent == null) {
			return;
		}
		if (!lineContent.contains("is_obsolete: true")) {
			return;
		}
		String[] ss = lineContent.split("\r\n");
		
		ArrayList<String> lsGOIDConsider = new ArrayList<String>();
		ArrayList<String> lsGOIDReplace = new ArrayList<String>();
		String GOID = "";
		for (String string : ss) {
			//Parent
			if (string.startsWith("id:")) {
				GOID = extractGOID(string);
				break;
			}
		}
			
		for (String string : ss) {
			if (string.startsWith("consider:")) {
				lsGOIDConsider.add(string.replace("consider:", "").trim());
			}
			if (string.startsWith("replaced_by:")) {
				lsGOIDReplace.add(string.replace("replaced_by:", "").trim());
			}
		}
		
		if (lsGOIDReplace.size() > 0) {
			importReplaceAndConsider(lsGOIDReplace, GOID);			
		}
		else if (lsGOIDConsider.size() > 0) {
			importReplaceAndConsider(lsGOIDConsider, GOID);			
		}
	}
	/**
	 * ���ȵ�����BP����Ϣ�����û��BP��Ϣ���ŵ��볣����Ϣ
	 * �Ӻ���ǰ���룬��ΪԽ����ķ���Խϸ
	 * @param lsReplaceAndConsider
	 * @param GOID
	 */
	private void importReplaceAndConsider(ArrayList<String> lsReplaceAndConsider, String GOID) {
		for (int i = lsReplaceAndConsider.size() - 1; i >= 0; i--) {
			Go2Term go2Term = Go2Term.queryGo2Term(lsReplaceAndConsider.get(i));
			if (go2Term == null) {
				continue;
			}
			else if (go2Term.getGoFunction().equals(Go2Term.FUN_SHORT_BIO_P)) {
				mapGOquery2GOID.put(GOID, lsReplaceAndConsider.get(i));
				return;
			}
		}
		mapGOquery2GOID.put(GOID, lsReplaceAndConsider.get(lsReplaceAndConsider.size() - 1));
		return;
	}
	
	/**
	 * ���� mapGOquery2GOID
	 * ��Ϊ��ЩqueryID--GOID������GOIDҲ��ʱ�ˣ�����Ҫ��������queryID����
	 */
	private void copeMapGOquery2GOID() {
		HashMap<String, String> mapGOquery2GOIDFinal = new HashMap<String, String>();
		for (String queryGOID : mapGOquery2GOID.keySet()) {
			String goIDsubject = mapGOquery2GOID.get(queryGOID);
			while (goIDsubject != mapGOquery2GOID.get(goIDsubject)) {
				goIDsubject = mapGOquery2GOID.get(goIDsubject);
			}
			mapGOquery2GOIDFinal.put(queryGOID, goIDsubject);
		}
		mapGOquery2GOID = mapGOquery2GOIDFinal;
	}
	
	private void updateConvertID() {
		for (String queryGOid : mapGOquery2GOID.keySet()) {
			String subjectGOid = mapGOquery2GOID.get(queryGOid);
			Go2Term go2Term = new Go2Term();
			go2Term.setGoIDQuery(queryGOid);
			go2Term.setGoID(subjectGOid);
			go2Term.update();
		}
	}
	
	/**
	 * �����βŵ���  ������Ϣ
	 */
	void impPerLineChild(String lineContent) {
		if (lineContent == null) {
			return;
		}
		if (lineContent.contains("is_obsolete: true")) {
			return;
		}
		String[] ss = lineContent.split("\r\n");
		String childID = "";
		for (String string : ss) {
			if (string.startsWith("id:")) {
				childID = string.replace("id:", "").trim();
			}
		}
		for (String string : ss) {
			//Parent
			if (string.startsWith("is_a:")) {
				String GOID = extractGOID(string);
				if (GOID == null) {
					logger.error("is_a ��û�ж�Ӧ��GOID��" + string);
				}
				else {
					Go2Term go2Term = Go2Term.queryGo2TermDB(GOID);
					go2Term.addChild(childID, Go2Term.RELATION_IS);
					go2Term.update();
				}
			}
			if (string.startsWith("relationship:")) {
				String GOID = extractGOID(string);
				if (GOID == null) {
					logger.error("is_a ��û�ж�Ӧ��GOID��" + string);
					return;
				}
				if (string.contains("part_of")) {
					Go2Term go2Term = Go2Term.queryGo2TermDB(GOID);
					go2Term.addChild(childID, Go2Term.RELATION_PARTOF);
					go2Term.update();
				}
				else if (string.contains("negatively_regulates")) {
					Go2Term go2Term = Go2Term.queryGo2TermDB(GOID);
					go2Term.addChild(childID, Go2Term.RELATION_REGULATE_NEG);
					go2Term.update();
				}
				else if (string.contains("relationship: regulates")) {
					Go2Term go2Term = Go2Term.queryGo2TermDB(GOID);
					go2Term.addChild(childID, Go2Term.RELATION_REGULATE);
					go2Term.update();
				}
				else if (string.contains("positively_regulates")) {
					Go2Term go2Term = Go2Term.queryGo2TermDB(GOID);
					go2Term.addChild(childID, Go2Term.RELATION_REGULATE_POS);
					go2Term.update();
				}
			}
		}
	}
	private String extractGOID(String GOIDstring)
	{
		ArrayList<String[]> lsResult = PatternOperate.getPatLoc(GOIDstring, "GO:\\d+", false);
		if (lsResult == null || lsResult.size() == 0) {
			return null;
		}
		return lsResult.get(0)[0];
	}
}


/**
 * �Ȱ�������ȫ����һ����ٵ������������󽫱��û���ҵ�NCBIID�ĵ���uniprotID��
 * �����ļ�gene_association.goa_uniprot.gz
 * ��ַ��http://www.geneontology.org/gene-associations
 * @author zong0jie
 *
 */
class Impgene_associationgoa_uniprot extends ImportPerLine {
	private static Logger logger = Logger.getLogger(Impgene_associationgoa_uniprot.class);
	/**
	 * �ӵ�5�п�ʼ��ȡ
	 */
	protected void setReadFromLine() {
		this.readFromLine = 5;
	}
	/**
	 * �ļ���ʽ
	 * Since we deal with proteins rather than genes, the semantics of some
fields in our files may be slightly different to other gene association files.

1.  DB
Database from which annotated entry has been taken.
For the UniProtKB and UniProtKB Complete Proteomes gene associaton files: UniProtKB
For the PDB association file:  PDB
Example: UniProtKB

2.  DB_Object_ID
A unique identifier in the database for the item being annotated.
Here: an accession number or identifier of the annotated protein
(or PDB entry for the gene_association.goa_pdb file)
For the UniProtKB and UniProtKB Complete Proteomes gene association files: a UniProtKB Accession.
Examples O00165

3.  DB_Object_Symbol
A (unique and valid) symbol (gene name) that corresponds to the DB_Object_ID.
An officially approved gene symbol will be used in this field when available.
Alternatively, other gene symbols or locus names are applied.
If no symbols are available, the identifier applied in column 2 will be used.
Examples: G6PC
CYB561
MGCQ309F3

4.  Qualifier
This column is used for flags that modify the interpretation of an
annotation.
If not null, then values in this field can equal: NOT, colocalizes_with, contributes_to,
NOT | contributes_to, NOT | colocalizes_with
Example: NOT

5.  GO ID
The GO identifier for the term attributed to the DB_Object_ID.
Example: GO:0005634

6.  DB:Reference
A single reference cited to support an annotation.
Where an annotation cannot reference a paper, this field will contain
a GO_REF identifier. See section 8 and
http://www.geneontology.org/doc/GO.references
for an explanation of the reference types used.
Examples: PMID:9058808
DOI:10.1046/j.1469-8137.2001.00150.x
GO_REF:0000002
GO_REF:0000020
GO_REF:0000004
GO_REF:0000003
GO_REF:0000019
GO_REF:0000023
GO_REF:0000024
GO_REF:0000033

7.  Evidence
One of either EXP, IMP, IC, IGI, IPI, ISS, IDA, IEP, IEA, TAS, NAS,
NR, ND or RCA.
Example: TAS

8.  With
An additional identifier to support annotations using certain
evidence codes (including IEA, IPI, IGI, IMP, IC and ISS evidences).
Examples: UniProtKB:O00341
InterPro:IPROO1878
RGD:123456
CHEBI:12345
Ensembl:ENSG00000136141
GO:0000001
EC:3.1.22.1

9.  Aspect
One of the three ontologies, corresponding to the GO identifier applied.
P (biological process), F (molecular function) or C (cellular component).
Example: P

10. DB_Object_Name
Name of protein
The full UniProt protein name will be present here,
if available from UniProtKB. If a name cannot be added, this field
will be left empty.
Examples: Glucose-6-phosphatase
Cellular tumor antigen p53
Coatomer subunit beta

11. Synonym
Gene_symbol [or other text]
Alternative gene symbol(s), IPI identifier(s) and UniProtKB/Swiss-Prot identifiers are
provided pipe-separated, if available from UniProtKB. If none of these identifiers
have been supplied, the field will be left empty.
Example:  RNF20|BRE1A|IPI00690596|BRE1A_BOVIN
IPI00706050
MMP-16|IPI00689864

12. DB_Object_Type
What kind of entity is being annotated.
Here: protein (or protein_structure for the
gene_association.goa_pdb file).
Example: protein

13. Taxon_ID
Identifier for the species being annotated.
Example: taxon:9606

14. Date
The date of last annotation update in the format 'YYYYMMDD'
Example: 20050101

15. Assigned_By
Attribute describing the source of the annotation.  One of
either UniProtKB, AgBase, BHF-UCL, CGD, DictyBase, EcoCyc, EcoWiki, Ensembl,
FlyBase, GDB, GeneDB_Spombe,GeneDB_Pfal, GOC, GR (Gramene), HGNC, Human Protein Atlas,
JCVI, IntAct, InterPro, LIFEdb, PAMGO_GAT, MGI, Reactome, RGD,
Roslin Institute, SGD, TAIR, TIGR, ZFIN, PINC (Proteome Inc.) or WormBase.
Example: UniProtKB

16. Annotation_Extension
Contains cross references to other ontologies/databases that can be used to qualify or
enhance the GO term applied in the annotation.
The cross-reference is prefaced by an appropriate GO relationship; references to multiple ontologies
can be entered.
Example: part_of(CL:0000084)
occurs_in(GO:0009536)
has_input(CHEBI:15422)
has_output(CHEBI:16761)
has_participant(UniProtKB:Q08722)
part_of(CL:0000017)|part_of(MA:0000415)

17. Gene_Product_Form_ID
The unique identifier of a specific spliceform of the protein described in column 2 (DB_Object_ID)
Example:O43526-2
	 */
	@Override
	public boolean impPerLine(String lineContent) {

		String[] ss = lineContent.split("\t");
		int taxID = 0;
		try {
			taxID = Integer.parseInt(ss[12].replace("taxon:", ""));
		} catch (Exception e) {
			logger.error("taxID����" + lineContent);
		}
		
		if (!hashTaxID.contains(taxID)) {
			return true;
		}
		GeneID copedID = new GeneID(ss[1], taxID);
		//�ҵ����ʵı�NCBI��UniProt��������UniID
		copedID.setUpdateRefAccID(ss[1],ss[2]);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_UNIPROT_UNIID, false);
		copedID.update(true);
		//����symbol��description
//		copedID.setUpdateAccID(ss[2]);
//		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_SYMBOL, false);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setSymb(ss[2]);
		geneInfo.setDescrp(ss[9]);
		geneInfo.setDBinfo(NovelBioConst.DBINFO_UNIPROT_GenralID);
		copedID.setUpdateGeneInfo(geneInfo);
		copedID.setUpdateGO(ss[4], NovelBioConst.DBINFO_UNIPROTID, ss[6], ss[5], ss[3]);
		return copedID.update(true);
	}
	

}