package com.novelbio.database.updatedb.database;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.GeneID;
import com.novelbio.generalConf.NovelBioConst;

/**
 * 将affyID与本物种序列blast之后的结果导入数据库
 * 注意导入前需要将表按照evalue从小到大排序
 * @author zong0jie
 *
 */
public class UpdateAffyBlast {
	
	public static void main(String[] args) {
		UpdateAffyBlast updateAffyBlast = new UpdateAffyBlast();
		updateAffyBlast.updateAffy2AccID(3847, "/media/winE/Bioinformatics/BLAST/result/soybean/affySoy2SoyRNAfinal.xls");
	}
	
	private void updateAffy2AccID(int taxID, String affy2)
	{
		TxtReadandWrite txtRead = new TxtReadandWrite(affy2, false);
		for (String content : txtRead.readlines()) {
			String[] ss = content.split("\t");
			GeneID copedID = new GeneID(ss[0], taxID);
			copedID.setUpdateRefAccID(ss[1]);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_AFFY_GLMAX, true);
			copedID.update(true);
		}
	}
	
	
	
	
	
}
