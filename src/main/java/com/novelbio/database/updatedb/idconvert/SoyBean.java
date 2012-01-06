package com.novelbio.database.updatedb.idconvert;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.model.modcopeid.CopedID;

public class SoyBean {
	int taxID = 3847;
	public static void main(String[] args) {
		SoyBean soyBean = new SoyBean();
		soyBean.updateDbxref("/media/winE/Bioinformatics/GenomeData/soybean/ncbi/dbxref.xls");
		soyBean.updateGeneInfo("/media/winE/Bioinformatics/GenomeData/soybean/Gmax_109_annotation_info.txt");
		
	}
	/**
	 * 将ncbi与soybean的对照表导入数据库的ID转换表
	 * @param dbxref
	 */
	public void updateDbxref(String dbxref) {
		TxtReadandWrite txtDbxref = new TxtReadandWrite(dbxref, false);
		for (String string : txtDbxref.readlines()) {
			//第一个glmaxID，第二个 ncbiID，第三个geneID
			String[] ss = string.split("\t");
			CopedID copedID = new CopedID(ss[0], taxID);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_GLYMAX_SOYBASE, true);
			copedID.setUpdateGeneID(ss[2], CopedID.IDTYPE_GENEID);
			copedID.update(true);
			copedID = new CopedID(ss[1], taxID);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_NCBI_ACC_GENEAC, true);
			copedID.setUpdateGeneID(ss[2], CopedID.IDTYPE_GENEID);
			copedID.update(true);
		}
	}
	public void updateGeneInfo(String geneInfoFile) {
		TxtReadandWrite txtGeneInfo = new TxtReadandWrite(geneInfoFile, false);
		for (String string : txtGeneInfo.readlines()) {
			String[] ss = string.split("\t");
			CopedID copedID = new CopedID(ss[0], taxID);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_GLYMAX_SOYBASE, true);
			GeneInfo geneInfo = new GeneInfo();
			geneInfo.setSymbol(CopedID.removeDot(ss[0]));
			if (ss.length < 9) {
				geneInfo.setDescription("");
			}
			else {
				geneInfo.setDescription(ss[8]);
			}
			copedID.setUpdateGeneInfo(geneInfo);
			copedID.update(true);
		}
		
	}
}
