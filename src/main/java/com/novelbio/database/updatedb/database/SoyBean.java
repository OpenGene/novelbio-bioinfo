package com.novelbio.database.updatedb.database;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modcopeid.CopedID;
import com.novelbio.generalConf.NovelBioConst;

public class SoyBean {
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
	 * ��Tigr��Gff�ļ�����gene2GO���ݿ⣬����NCBIGO��UniGO������
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
		soyDbXref.updateFile(soyDbxref, false);
		
		SoyGeneInfo soyGeneInfo = new SoyGeneInfo();
		soyGeneInfo.setTaxID(taxID);
		soyGeneInfo.setReadFromLine(1);
		soyGeneInfo.setTxtWriteExcep(SoyGeneInfo);
	}
}
/**
 * ��ncbi��soybean�Ķ��ձ������ݿ��IDת����
 * @param dbxref
 */
class SoyDbXref extends ImportPerLine
{
	/**
	 * ��ncbi��soybean�Ķ��ձ������ݿ��IDת����
	 * @param dbxref
	 */
	@Override
	boolean impPerLine(String lineContent) {
		//��һ��glmaxID���ڶ��� ncbiID��������geneID
		String[] ss = lineContent.split("\t");
		CopedID copedID = new CopedID(ss[0], taxID);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_GLYMAX_SOYBASE, true);
		copedID.setUpdateGeneID(ss[2], CopedID.IDTYPE_GENEID);
		copedID.update(true);
		copedID = new CopedID(ss[1], taxID);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_GENEAC, true);
		copedID.setUpdateGeneID(ss[2], CopedID.IDTYPE_GENEID);
		return copedID.update(true);
	}
}
/**
 * /media/winE/Bioinformatics/GenomeData/soybean/Gmax_109_annotation_info.txt
 * ��geneInfo�������ݿ�
 * @param dbxref
 */
class SoyGeneInfo extends ImportPerLine
{
	/**
	 * ��soybean��annotation�������ݿ�
	 * @param dbxref
	 */
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		CopedID copedID = new CopedID(ss[0], taxID);
		copedID.setUpdateDBinfo(NovelBioConst.DBINFO_GLYMAX_SOYBASE, true);
		GeneInfo geneInfo = new GeneInfo();
		geneInfo.setSymb(CopedID.removeDot(ss[0]));
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