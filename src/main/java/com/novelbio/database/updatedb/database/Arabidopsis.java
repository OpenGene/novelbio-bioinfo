package com.novelbio.database.updatedb.database;

import com.novelbio.base.SepSign;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.model.geneanno.GeneInfo;

/**
 * 将tair10的数据导入数据库
 * @author zong0jie
 *
 */
public class Arabidopsis {
	public static void main(String[] args) {
		System.out.println(DBAccIDSource.TAIR_ATH.toString());
	}
	String TAIRNCBIGeneIDmapping = "";
	String TAIRNCBIRefSeqMappingPROT = "";
	String TAIRNCBIRefSeqMappingRNA = "";
	String TAIRFunctionalDescriptions = "";
	String AthGO = "";
	/**
	 * 7: ftp://ftp.arabidopsis.org/home/tair/Ontologies/Gene_Ontology/ 
	 * ATH_GO_GOSLIM.txt.gz 这个是gz压缩的 
	 * @param athGO
	 */
	public void setAthGO(String athGO) {
		AthGO = athGO;
	}
	/**
	 * 6: ftp://ftp.arabidopsis.org/home/tair/Genes/TAIR10_genome_release/ 
	 * TAIR10_functional_descriptions
	 * @param tAIR_functional_descriptions
	 */
	public void setTAIR_functional_descriptions(
			String tAIR_functional_descriptions) {
		TAIRFunctionalDescriptions = tAIR_functional_descriptions;
	}
	/**
	 * 1： ftp://ftp.arabidopsis.org/home/tair/Genes/TAIR10_genome_release/TAIR10_NCBI_mapping_files/ 
	 * TAIR10_NCBI_GENEID_mapping
	 * @param tAIRNCBIGeneIDmapping
	 */
	public void setTAIRNCBIGeneIDmapping(String tAIRNCBIGeneIDmapping) {
		TAIRNCBIGeneIDmapping = tAIRNCBIGeneIDmapping;
	}
	/**
	 * 2： ftp://ftp.arabidopsis.org/home/tair/Genes/TAIR10_genome_release/TAIR10_NCBI_mapping_files/ 
	 * TAIR10_NCBI_REFSEQ_mapping_PROT
	 * @param tAIRNCBIRefSeqMappingPROT
	 */
	public void setTAIRNCBIRefSeqMappingPROT(String tAIRNCBIRefSeqMappingPROT) {
		TAIRNCBIRefSeqMappingPROT = tAIRNCBIRefSeqMappingPROT;
	}
	/**
	 * 3： ftp://ftp.arabidopsis.org/home/tair/Genes/TAIR10_genome_release/TAIR10_NCBI_mapping_files/ 
	 * TAIR_NCBI_REFSEQ_mapping_RNA
	 * @param tAIRNCBIRefSeqMappingRNA
	 */
	public void setTAIRNCBIRefSeqMappingRNA(String tAIRNCBIRefSeqMappingRNA) {
		TAIRNCBIRefSeqMappingRNA = tAIRNCBIRefSeqMappingRNA;
	}
	
	public void update() {
		if (FileOperate.isFileExistAndBigThan0(TAIRNCBIGeneIDmapping)) {
			TAIR_NCBI_GENEID_mapping tair_NCBI_GENEID_mapping = new TAIR_NCBI_GENEID_mapping();
			tair_NCBI_GENEID_mapping.setTxtWriteExcep(FileOperate.changeFileSuffix(TAIRNCBIGeneIDmapping, "_out", "txt"));
			tair_NCBI_GENEID_mapping.updateFile(TAIRNCBIGeneIDmapping);
		}
		
		TAIR_NCBI_REFSEQ_mapping_PROT_RNA tair_NCBI_REFSEQ_mapping_PROT_RNA = null;
		if (FileOperate.isFileExistAndBigThan0(TAIRNCBIRefSeqMappingPROT)) {
			tair_NCBI_REFSEQ_mapping_PROT_RNA = new TAIR_NCBI_REFSEQ_mapping_PROT_RNA();
			tair_NCBI_REFSEQ_mapping_PROT_RNA.setTxtWriteExcep(FileOperate.changeFileSuffix(TAIRNCBIRefSeqMappingPROT, "_out", "txt"));
			tair_NCBI_REFSEQ_mapping_PROT_RNA.setProtein(true);
			tair_NCBI_REFSEQ_mapping_PROT_RNA.updateFile(TAIRNCBIRefSeqMappingPROT);
		}
		
		if (FileOperate.isFileExistAndBigThan0(TAIRNCBIRefSeqMappingRNA)) {
			tair_NCBI_REFSEQ_mapping_PROT_RNA.setProtein(false);
			tair_NCBI_REFSEQ_mapping_PROT_RNA.setTxtWriteExcep(FileOperate.changeFileSuffix(TAIRNCBIRefSeqMappingRNA, "_out", "txt"));
			tair_NCBI_REFSEQ_mapping_PROT_RNA.updateFile(TAIRNCBIRefSeqMappingRNA);
		}
		
		if (FileOperate.isFileExistAndBigThan0(TAIRFunctionalDescriptions)) {
			TAIR_functional_descriptions tair_functional_descriptions = new TAIR_functional_descriptions();
			tair_functional_descriptions.setTxtWriteExcep(FileOperate.changeFileSuffix(TAIRFunctionalDescriptions, "_out", "txt"));
			tair_functional_descriptions.updateFile(TAIRFunctionalDescriptions);
		}
		
		if (FileOperate.isFileExistAndBigThan0(AthGO)) {
			ATH_GO_GOSLIM ath_GO_GOSLIM = new ATH_GO_GOSLIM();
			ath_GO_GOSLIM.setTxtWriteExcep(FileOperate.changeFileSuffix(AthGO, "_out", "txt"));
			ath_GO_GOSLIM.updateFile(AthGO);
		}
		

	}
}
/**
 * 1：
 * ftp://ftp.arabidopsis.org/home/tair/Genes/TAIR10_genome_release/TAIR10_NCBI_mapping_files/
 * TAIR10_NCBI_GENEID_mapping
 */
class TAIR_NCBI_GENEID_mapping extends ImportPerLine {
	int taxID = 3702;
	
	/** 覆盖该方法来设定从第几行开始读取 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, ss[0], this.taxID);
		copedID.setUpdateAccID(ss[1]);
		copedID.setUpdateDBinfo(DBAccIDSource.TAIR_ATH, true);
		return copedID.update(true);
	}
}
/**
 * 2-3：
 * ftp://ftp.arabidopsis.org/home/tair/Genes/TAIR10_genome_release/TAIR10_NCBI_mapping_files/
 * TAIR10_NCBI_REFSEQ_mapping_PROT/TAIR_NCBI_REFSEQ_mapping_RNA
 */
class TAIR_NCBI_REFSEQ_mapping_PROT_RNA extends ImportPerLine {
	boolean Protein = false;
	int taxID = 3702;
	
	/** 覆盖该方法来设定从第几行开始读取 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	/**
	 * 导入的是protein还是RNA
	 * @param protein
	 */
	public void setProtein(boolean protein) {
		Protein = protein;
	}
	
	@Override
	boolean impPerLine(String lineContent) {
		if (lineContent.contains(";")) {
			return false;
		}
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(GeneID.IDTYPE_GENEID, ss[0], this.taxID);
		copedID.setUpdateAccID(ss[2]);
		copedID.setUpdateDBinfo(DBAccIDSource.TAIR_ATH, true);
		copedID.setUpdateAccID(ss[1]);
		if (Protein) {
			copedID.setUpdateDBinfo(DBAccIDSource.RefSeqPro, false);
		} else {
			copedID.setUpdateDBinfo(DBAccIDSource.RefSeqRNA, false);
		}
		return copedID.update(true);
	}
}

/**
 * 4：
 *ftp://ftp.arabidopsis.org/home/tair/Genes/TAIR10_genome_release/TAIR10_gene_transcript_associations/
 * TAIR10_Model_cDNA_associations
 */
class TAIR_Model_cDNA_associations extends ImportPerLine {
	int taxID = 3702;
	
	/** 覆盖该方法来设定从第几行开始读取 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}

	@Override
	boolean impPerLine(String lineContent) {
		if (lineContent.startsWith("#") || lineContent.trim().equals("")) {
			return true;
		}
		if (lineContent.contains(";")) {
			return false;
		}
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(ss[0], taxID);
		copedID.setUpdateRefAccID(ss[0], ss[1]);
		copedID.setUpdateDBinfo(DBAccIDSource.TAIR_ATH, true);
		if (!copedID.update(true)) {
			return false;
		}
		copedID.setUpdateAccID(ss[1]);
		copedID.setUpdateDBinfo(DBAccIDSource.NCBI, false);
		if (!copedID.update(false)) {
			return false;
		}
		return true;
	}
}
/**
 * 5: 
 * Uniprot2AGI
 * @author zong0jie
 *
 */
class Uniprot2AGI extends ImportPerLine {
	int taxID = 3702;

	/** 覆盖该方法来设定从第几行开始读取 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		String[] ssAtID = ss[1].split(";");
		//如果一个uniprotID对应多个atID，那么就导入多次
		for (String string : ssAtID) {
			GeneID copedID = new GeneID(ss[0], taxID);
			copedID.setUpdateRefAccID(string);
			copedID.setUpdateDBinfo(DBAccIDSource.Uniprot, false);
			if (!copedID.update(false)) {
				return false;
			}
		}
		return true;
	}
	
}
/**
 * 6: 
 * ftp://ftp.arabidopsis.org/home/tair/Genes/TAIR10_genome_release/
 * TAIR10_functional_descriptions
 */
class TAIR_functional_descriptions extends ImportPerLine {
	int taxID = 3702;
	
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(ss[0], taxID);
		copedID.setUpdateDBinfo(DBAccIDSource.TAIR_ATH, true);

		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setDBinfo(DBAccIDSource.TAIR_ATH.toString());
		geneInfo.setSymb(ss[0]);
		geneInfo.setTypeOfGene(ss[1]);
		if (ss.length < 3) {
			return false;
		}
		geneInfo.addFullName(ss[2]);
		//如果没有description，那么就用fullname去代替
		String description = ss[2];
		if (ss.length >= 4 && ss[3] != null && !ss[3].trim().equals("")) {
			if (description.equals("")) {
				description = ss[3];
			} else {
				description = description + SepSign.SEP_INFO + ss[3];
			}
		}
		if (ss.length >= 5 && ss[4] != null && !ss[4].trim().equals("")) {
			if (description.equals("")) {
				description = ss[4];
			} else {
				description = description + SepSign.SEP_INFO + ss[4];
			}
		}
		geneInfo.setDescrp(description);
		geneInfo.setDBinfo(DBAccIDSource.TAIR_ATH.toString());
		copedID.setUpdateGeneInfo(geneInfo);
		if (!copedID.update(true)) {
			return false;
		}
		return true;
	}
}
/**
 * 7: 
 * ftp://ftp.arabidopsis.org/home/tair/Ontologies/Gene_Ontology/
 * ATH_GO_GOSLIM.txt.gz
 * 这个是gz压缩的
 * Column headers :explanation
1. locus name: standard AGI convention name
2. TAIR accession:the unique identifier for an object in the TAIR database- 
the object type is the prefix, followed by a unique accession number(e.g. gene:12345).  
3. object name : the name of the object (gene, protein, locus) being annotated.
4. relationship type: the relationship between the annotated object and the GO term
5. GO term: the actual string of letters corresponding to the GO ID
6. GO ID: the unique identifier for a GO term.  
7. TAIR Keyword ID: the unique identifier for a keyword in the TAIR database.
8.  Aspect: F=molecular function, C=cellular component, P=biological 13process. 
9. GOslim term: high level GO term helps in functional categorization.
10. Evidence code: three letter code for evidence types (see: http://www.geneontology.org/GO.evidence.html).
11. Evidence description: the analysis that was done to support the annotation
12. Evidence with: supporting evidence for IGI, IPI, IC, IEA and ISS annotations
13. Reference: Either a TAIR accession for a reference (reference table: reference_id) or reference from PubMed (e.g. PMID:1234).  
14. Annotator: TAIR, TIGR or a TAIR community member
15. Date annotated: date the annotation was made.
 */
class ATH_GO_GOSLIM extends ImportPerLine {	
	/** 覆盖该方法来设定从第几行开始读取 */
	protected void setReadFromLine() {
		this.readFromLine = 1;
	}
	int taxID = 3702;
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(ss[0], taxID);
		copedID.setUpdateDBinfo(DBAccIDSource.TAIR_ATH, true);
		copedID.addUpdateGO(ss[5], DBAccIDSource.TAIR_ATH, ss[9], null, ss[13]);
		return copedID.update(false);
	}
}


