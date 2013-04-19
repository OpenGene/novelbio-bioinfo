package com.novelbio.database.updatedb.database;

import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.NovelBioConst;

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
	String soyDbxref = "";
	String SoyGeneInfo = "";
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
	/**
	 * 将Tigr的Gff文件导入gene2GO数据库，倒入NCBIGO和UniGO两个表
	 * @param gffRapDB
	 * @param outFIle
	 * @throws Exception
	 */
	public void update()
	{
		SoyDbXref soyDbXref = new SoyDbXref();
		soyDbXref.setTaxID(taxID);
		soyDbXref.setReadFromLine(1);
		soyDbXref.setTxtWriteExcep(FileOperate.changeFileSuffix(soyDbxref, "_out", null));
		soyDbXref.updateFile(soyDbxref);
		
		SoyGeneInfo soyGeneInfo = new SoyGeneInfo();
		soyGeneInfo.setTaxID(taxID);
		soyGeneInfo.setReadFromLine(1);
		soyGeneInfo.setTxtWriteExcep(SoyGeneInfo);
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
class SoyDbXref extends ImportPerLine
{
	/**
	 * 将ncbi与soybean的对照表导入数据库的ID转换表
	 * @param dbxref
	 */
	@Override
	boolean impPerLine(String lineContent) {
		//第一个glmaxID，第二个 ncbiID，第三个geneID
		String[] ss = lineContent.split("\t");
		GeneID copedID = new GeneID(ss[0], taxID);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_GLYMAX_SOYBASE, true);
		copedID.setUpdateGeneID(ss[2], GeneID.IDTYPE_GENEID);
		copedID.update(true);
		copedID = new GeneID(ss[1], taxID);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_GENEAC, true);
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
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_GLYMAX_SOYBASE, true);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setSymb(GeneID.removeDot(ss[0]));
		if (ss.length < 9) {
			geneInfo.setDescrp("");
		}
		else {
			geneInfo.setDescrp(ss[8]);
		}
		copedID.setUpdateGeneInfo(geneInfo);
		return copedID.update(true);
	}
}
