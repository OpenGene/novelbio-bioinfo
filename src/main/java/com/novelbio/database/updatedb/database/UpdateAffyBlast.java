package com.novelbio.database.updatedb.database;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.CopedID;

/**
 * ��affyID�뱾��������blast֮��Ľ���������ݿ�
 * ע�⵼��ǰ��Ҫ��������evalue��С��������
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
			CopedID copedID = new CopedID(ss[0], taxID);
			copedID.setUpdateRefAccID(ss[1]);
			copedID.setUpdateDBinfo(NovelBioConst.DBINFO_AFFY_GLMAX, true);
			copedID.update(true);
		}
	}
	
	
	
	
	
}