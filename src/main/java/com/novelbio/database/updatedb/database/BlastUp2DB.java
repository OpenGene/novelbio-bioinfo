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
	 * ref|NP_002932| �������͵ģ��ͻ���������ʽȥץ�����ID
	 */
	boolean idtypeBlast = false;
	/**
	 *  ref|NP_002932| �������͵ģ��ͻ���������ʽȥץ�����ID
	 *  id�����ڵ�һ�� ��|�� �͵ڶ��� ��|�� �м�
	 *  ��ʱ���Ҫ�����趨Ϊtrue������Ļ��Ὣblast�ĵڶ���ȫ������
	 * @param idtypeBlast Ĭ����false
	 */
	public void setIDisBlastType(boolean idtypeBlast) {
		this.idtypeBlast = idtypeBlast;
	}
	/**
	 * blast��������ID
	 * @param subTaxID
	 */
	public void setSubTaxID(int subTaxID) {
		this.subTaxID = subTaxID;
	}
	String blastDBinfo= null;
	/**
	 * �趨blast����ID�����ݿ�
	 * @param blastDBinfo
	 */
	public void setBlastDBinfo(String blastDBinfo) {
		this.blastDBinfo = blastDBinfo;
	}
	
	String queryIDType = GeneID.IDTYPE_ACCID;
	String blastIDType = GeneID.IDTYPE_ACCID;
	/**
	 * ��һ�У���accID����geneID����UniID
	 * @param IDtype Ĭ����CopedID.IDTYPE_ACCID
	 * @return
	 */
	public void setQueryID(String IDtype) {
		this.queryIDType = IDtype;
	}
	/**
	 * blast����ID��accID����geneID����UniID
	 * @param blastID Ĭ����CopedID.IDTYPE_ACCID
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
			//���û��blastDBinfo���������е�accIDȥ��ø�blastDBinfo
			if (blastDBinfo == null || blastDBinfo.equals("")) {
				GeneID copedIDBlast = new GeneID(accID, subTaxID);
				blastDBinfo = copedIDBlast.getDBinfo();
			}
			copedID.setUpdateBlastInfo(accID, blastDBinfo, subTaxID, Double.parseDouble(ss[10]), Double.parseDouble(ss[2]));
		}
		return copedID.update(false);
	}
	
}
