package com.novelbio.database.updatedb.database;

import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.NovelBioConst;

public class BlastUp2DB extends ImportPerLine{
	public  BlastUp2DB() {
		this.readFromLine = 1;
	}
	int subTaxID = 0;
	String queryDBinfo = "";
	public void setQueryDBinfo(String queryDBinfo) {
		this.queryDBinfo = queryDBinfo;
	}
	/**
	 * ref|NP_002932| 这种类型的，就会用正则表达式去抓里面的ID
	 */
	boolean idtypeBlast = false;
	/**
	 *  ref|NP_002932| 这种类型的，就会用正则表达式去抓里面的ID
	 *  id必须在第一个 “|” 和第二个 “|” 中间
	 *  这时候就要将其设定为true。否则的话会将blast的第二列全部导入
	 * @param idtypeBlast 默认是false
	 */
	public void setIDisBlastType(boolean idtypeBlast) {
		this.idtypeBlast = idtypeBlast;
	}
	/**
	 * blast到的物种ID
	 * @param subTaxID
	 */
	public void setSubTaxID(int subTaxID) {
		this.subTaxID = subTaxID;
	}
	String blastDBinfo= null;
	/**
	 * 设定blast到的ID的数据库
	 * @param blastDBinfo
	 */
	public void setBlastDBinfo(String blastDBinfo) {
		this.blastDBinfo = blastDBinfo;
	}
	
	String queryIDType = GeneID.IDTYPE_ACCID;
	String blastIDType = GeneID.IDTYPE_ACCID;
	/**
	 * 第一列，是accID还是geneID还是UniID
	 * @param IDtype 默认是CopedID.IDTYPE_ACCID
	 * @return
	 */
	public void setQueryID(String IDtype) {
		this.queryIDType = IDtype;
	}
	/**
	 * blast到的ID是accID还是geneID还是UniID
	 * @param blastID 默认是CopedID.IDTYPE_ACCID
	 */
	public void setBlastID(String blastID) {
		this.blastIDType = blastID;
	}
	@Override
	boolean impPerLine(String lineContent) {
		String[] ss = lineContent.split("\t");
		GeneID copedID = null;
		if (!queryIDType.equals(GeneID.IDTYPE_ACCID)) {
			copedID = new GeneID(queryIDType, ss[0], taxID);
		}
		else {
			copedID = new GeneID(ss[0], taxID);
		}
		
		copedID.setUpdateDBinfo(queryDBinfo, false);
		if (!blastIDType.equals(GeneID.IDTYPE_ACCID)) {
			copedID.setUpdateBlastInfo(ss[1],blastIDType,  blastDBinfo, subTaxID, Double.parseDouble(ss[10]), Double.parseDouble(ss[2]));
		}
		else {
			String accID = ss[1];
			if (idtypeBlast) {
				accID = GeneID.getBlastAccID(ss[1]);
			}
			//如果没有blastDBinfo，就用已有的accID去获得该blastDBinfo
			if (blastDBinfo == null || blastDBinfo.equals("")) {
				GeneID copedIDBlast = new GeneID(accID, subTaxID);
				blastDBinfo = copedIDBlast.getDBinfo();
			}
			copedID.setUpdateBlastInfo(accID, blastDBinfo, subTaxID, Double.parseDouble(ss[10]), Double.parseDouble(ss[2]));
		}
		return copedID.update(false);
	}
	
}
