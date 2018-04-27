package com.novelbio.database.updatedb.database;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.model.geneanno.Gene2Go;
import com.novelbio.database.model.geneanno.GeneInfo;

public class SoyBean {
	public static void main(String[] args) {
		PatternOperate patternOperate = new PatternOperate("(?<=target:Soybean:).+?(?=;)", false);
		String aa = patternOperate.getPatFirst(">target:Soybean:AFFX-BioB-M_at; affx|AFFX-BioB-M; ");
		System.out.println(aa);
//		SoyBean soyBean = new SoyBean();
//		soyBean.getAffyIDseq("/media/winE/Bioinformatics/Affymetrix/soybean/Soybean.target");
		
		SeqFastaHash seqFastaHash = new SeqFastaHash("/media/winE/Bioinformatics/GenomeData/soybean/Gmax_109_cds.fa/Gmax_109_cds.fa", 
				".+?(?=\\|)", false);
		seqFastaHash.writeToFile(FileOperate.changeFileSuffix("/media/winE/Bioinformatics/GenomeData/soybean/Gmax_109_cds.fa/Gmax_109_cds.fa", "_seq", "fa"));
	}
	int taxID = 3847;
	String soyDbxref;
	String SoyGeneInfo;
	String soyIdConvertFile;
	String soyGff;
	String soyGOFile;
	/**
	 * /media/winE/Bioinformatics/GenomeData/soybean/ncbi/dbxref.xls
	 * @param soyDbxref
	 */
	public void setSoyDbxref(String soyDbxref) {
		this.soyDbxref = soyDbxref;
	}
	public void setSoyGeneInfo(String SoyGeneInfo) {
		this.SoyGeneInfo = SoyGeneInfo;
	}
	public void setSoyGff(String soyGff) {
		this.soyGff = soyGff;
	}
	public void setSoyGOFile(String soyGOFile) {
		this.soyGOFile = soyGOFile;
	}
	public void setSoyIdConvertFile(String soyIdConvertFile) {
		this.soyIdConvertFile = soyIdConvertFile;
	}
	
	/**
	 * 将Tigr的Gff文件导入gene2GO数据库，倒入NCBIGO和UniGO两个表
	 * @param gffRapDB
	 * @param outFIle
	 * @throws Exception
	 */
	public void update() {
		if (soyDbxref != null) {
			SoyDbXref soyDbXref = new SoyDbXref();
			soyDbXref.setTaxID(taxID);
			soyDbXref.setReadFromLine(1);
			soyDbXref.setTxtWriteExcep(FileOperate.changeFileSuffix(soyDbxref, "_out", null));
			soyDbXref.updateFile(soyDbxref);
		}
		
		if (SoyGeneInfo != null) {
			SoyGeneInfo soyGeneInfo = new SoyGeneInfo();
			soyGeneInfo.setTaxID(taxID);
			soyGeneInfo.setReadFromLine(1);
			soyGeneInfo.setTxtWriteExcep(SoyGeneInfo);
		}
		
		if (soyGff != null) {
//			SoyGffdbxref soyGffdbxref = new SoyGffdbxref();
//			soyGffdbxref.setTaxID(taxID);
//			soyGffdbxref.updateFile(soyGff);
			
//			SoyGffDescription soyGffDescription = new SoyGffDescription();
//			soyGffDescription.setTaxID(taxID);
//			soyGffDescription.updateFile(soyGff);
		}
		
//		if (soyIdConvertFile != null) {
//			SoyIdConvert soyId = new SoyIdConvert();
//			soyId.setTaxID(taxID);
//			soyId.updateFile(soyIdConvertFile);
//		}
//		
		if (soyGOFile != null) {
			SoyGO soyGO = new SoyGO();
			soyGO.setTaxID(taxID);
			soyGO.updateFile(soyGOFile);
		}
	}
	
	
	public void getAffyIDseq(String affTargetFile) {
		SeqFastaHash seqFastaHash = new SeqFastaHash(affTargetFile, "(?<=target:Soybean:).+?(?=;)", false);
		seqFastaHash.writeToFile(FileOperate.changeFileSuffix(affTargetFile, "_seq", "fa"));
	}
}
/**
 * 将ncbi与soybean的对照表导入数据库的ID转换表
 * @param dbxref
 */
class SoyDbXref extends ImportPerLine {
	/**
	 * 将ncbi与soybean的对照表导入数据库的ID转换表
	 * @param dbxref
	 */
	@Override
	boolean impPerLine(String lineContent) {
		//TODO
		//第一个glmaxID，第二个 ncbiID，第三个geneID
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(ss[0], taxID);
		copedID.setUpdateDBinfo(DBAccIDSource.SOYBASE, true);
		copedID.setUpdateGeneID(ss[2], GeneID.IDTYPE_GENEID);
		copedID.update(true);
		copedID = new GeneID(ss[1], taxID);
		copedID.setUpdateDBinfo(DBAccIDSource.SOYBASE, true);
		copedID.setUpdateGeneID(ss[2], GeneID.IDTYPE_GENEID);
		return copedID.update(true);
	}
}
/**
 * /media/winE/Bioinformatics/GenomeData/soybean/Gmax_109_annotation_info.txt
 * 将geneInfo表导入数据库
 * @param dbxref
 */
class SoyGeneInfo extends ImportPerLine {
	/**
	 * 将soybean的annotation导入数据库
	 * @param dbxref
	 */
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(ss[0], taxID);
		copedID.setUpdateDBinfo(DBAccIDSource.SOYBASE, true);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setSymb(GeneID.removeDot(ss[0]));
		if (ss.length < 9) {
			geneInfo.setDescrp("");
		} else {
			geneInfo.setDescrp(ss[8]);
		}
		copedID.setUpdateGeneInfo(geneInfo);
		return copedID.update(true);
	}
}

class SoyGffdbxref extends ImportPerLine {
	/**
	 * 将soybean的annotation导入数据库
	 * @param dbxref
	 */
	@Override
	boolean impPerLine(String lineContent) {
		if (lineContent.startsWith("#")) {
			return true;
		}
		lineContent = StringOperate.decode(lineContent);
		String[] ss = lineContent.split("\t");
		if ( !ss[2].equals("transcript")) {
			return true;
		}
		
		String[] discriptions = ss[8].split(";");
		GeneID copedID = null;
		for (String string : discriptions) {
			if (string.contains("ID=")) {
				String accId = string.replace("ID=", "").split(":")[1];
				accId = accId.toLowerCase().replace("glyma", "Glyma");
				copedID = new GeneID(accId, taxID);
			} else if (string.contains("Dbxref=")) {
				String[] dbxrefs = string.replace("Dbxref=", "").split(",");
				for (String dbxref : dbxrefs) {
					if (dbxref.contains("RefSeq")) {
						copedID.addUpdateRefAccID(dbxref.split(":")[1]);
					}
				}
			}
		}
		return copedID.update(true);
	}
}

class SoyGffDescription extends ImportPerLine {
	/**
	 * 将soybean的annotation导入数据库
	 * @param dbxref
	 */
	@Override
	boolean impPerLine(String lineContent) {
		lineContent = StringOperate.decode(lineContent);
		String[] ss = lineContent.split("\t");
		if (!ss[2].equals("gene") && !ss[2].equals("transcript")) {
			return true;
		}
		GeneID copedID = null;
		if (ss[2].equals("gene")) {
			String[] discriptions = ss[8].split(";");
			for (String string : discriptions) {
				if (string.contains("ID=")) {
					String accId = string.replace("ID=", "").split(":")[1];
					copedID = new GeneID(accId, taxID);
				} else if (string.contains("description=")) {
					String disc = string.replace("description=", "");
					disc = disc.split("\\[")[0].trim();
					GeneInfo geneInfo = new GeneInfo();
					geneInfo.setDBinfo(DBAccIDSource.SOYBASE.toString());
					geneInfo.setDescrp(disc);
					copedID.setUpdateGeneInfo(geneInfo);
				}
			}
		} else if (ss[2].equals("transcript")) {
			String[] discriptions = ss[8].split(";");
			for (String string : discriptions) {
				if (string.contains("ID=")) {
					String accId = string.replace("ID=", "").split(":")[1];
					copedID = new GeneID(accId, taxID);
				} else if (string.contains("Ontology_term")) {
					String[] goTerms = string.replace("Ontology_term=", "").split(",");
					for (String goTerm : goTerms) {
						goTerm = goTerm.trim().replaceFirst("GO:", "");
						Gene2Go gene2Go = new Gene2Go();
						gene2Go.setGOID(goTerm);
						copedID.addUpdateGO(gene2Go);
					}
				}
			}
		}
		return copedID.update(false);
	}
}

class SoyIdConvert extends ImportPerLine {
	/**
	 * 将soybean的annotation导入数据库
	 * @param dbxref
	 */
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(ss[1], taxID);
		copedID.addUpdateRefAccID(ss[0]);
		return copedID.update(false);
	}
}

class SoyGO extends ImportPerLine {
	/**
	 * 将soybean的annotation导入数据库
	 * @param dbxref
	 */
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		if (!ss[1].equals("GO")) {
			return true;
		}
		GeneID copedID = new GeneID(ss[0], taxID);
		Gene2Go gene2Go = new Gene2Go();
		gene2Go.setGOID(ss[2]);
		copedID.addUpdateGO(gene2Go);		
		return copedID.update(false);
	}
}