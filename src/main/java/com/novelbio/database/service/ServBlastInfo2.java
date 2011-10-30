package com.novelbio.database.service;

import java.util.ArrayList;

import com.novelbio.database.entity.friceDB.Blast2GeneInfo;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.Gene2GoInfo;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.Uni2GoInfo;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;

public class ServBlastInfo2 {
	/**
	 * @param genInfo
	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
	 * 1: accID<br>
	 * 2: ����ת����ID<br>
	 * @param taxID
	 * @param sep
	 * @return
	 * ����blast����Ϣ�����û�оͷ���null
	 */
	public static BlastInfo getBlastInfo(String[] genInfo,int QtaxID,int StaxID,double evalue)
	{
		BlastInfo blastInfo = null;
		if (genInfo[0].equals("geneID")) {
			NCBIID ncbiid = new NCBIID();
			 ncbiid.setGeneId(Long.parseLong(genInfo[2])); ncbiid.setTaxID(QtaxID);
			 blastInfo = getBlastInfo(ncbiid, evalue, StaxID);
		}
		else if (genInfo[0].equals("uniID")) {
			UniProtID uniProtID = new UniProtID();
			uniProtID.setUniID(genInfo[2]); uniProtID.setTaxID(QtaxID);
			blastInfo = getBlastInfo(uniProtID, evalue, StaxID);
		}
		else
		{
			String accID = genInfo[1];
			blastInfo = getBlastInfo(accID, evalue, StaxID);
		}
		return blastInfo;
	}
	
	/**
	 * @param ncbiid
	 * ע��ncbiid�б��뺬��geneID����Ϊ�������ȥ����blast��Ϣ��
	 * @param evalue
	 * @param StaxID ��Ҫ�Ƚϵ�������ID�����Ϊ0���򲻿�������ID
	 * @return
	 */
	private static BlastInfo getBlastInfo(NCBIID ncbiid,double evalue,int StaxID) {
		//��ʼ��ѯ
		BlastInfo qblastInfo = new BlastInfo();
		qblastInfo.setEvalue(evalue);qblastInfo.setQueryID(ncbiid.getGeneId()+"");
		qblastInfo.setSubjectTax(StaxID);
		ArrayList<BlastInfo> lsBlastInfos = MapBlastInfo.queryLsBlastInfo(qblastInfo);
		if (lsBlastInfos != null && lsBlastInfos.size() > 0 && lsBlastInfos.get(0).getEvalue() <= evalue) 
		{
			return lsBlastInfos.get(0);
		}
		return null;
	}
	/**
	 * @param uniProtID
	 * ע��uniProtID�б��뺬��uniID����Ϊ�������ȥ����blast��Ϣ��
	 * @param evalue
	 * @param StaxID ��Ҫ�Ƚϵ�������ID�����Ϊ0���򲻿�������ID
	 * @return
	 */
	private static BlastInfo getBlastInfo(UniProtID uniProtID,double evalue,int StaxID) {
		//��ʼ��ѯ
		BlastInfo qblastInfo = new BlastInfo();
		qblastInfo.setEvalue(evalue);qblastInfo.setQueryID(uniProtID.getUniID());
		qblastInfo.setSubjectTax(StaxID);
		ArrayList<BlastInfo> lsBlastInfos = MapBlastInfo.queryLsBlastInfo(qblastInfo);
		if (lsBlastInfos != null && lsBlastInfos.size() > 0 && lsBlastInfos.get(0).getEvalue() <= evalue) 
		{
			return lsBlastInfos.get(0);
		}
		return null;
	}
	
	/**
	 * 
	 * @param accID ֱ����accIDȥ��blast
	 * @param evalue
	 * @param StaxID ��Ҫ�Ƚϵ�������ID�����Ϊ0���򲻿�������ID
	 * @return
	 */
	private static BlastInfo getBlastInfo(String accID,double evalue,int StaxID) {
		//��ʼ��ѯ
		BlastInfo qblastInfo = new BlastInfo();
		qblastInfo.setEvalue(evalue);qblastInfo.setQueryID(accID);
		qblastInfo.setSubjectTax(StaxID);
		ArrayList<BlastInfo> lsBlastInfos = MapBlastInfo.queryLsBlastInfo(qblastInfo);
		if (lsBlastInfos != null && lsBlastInfos.size() > 0 && lsBlastInfos.get(0).getEvalue() <= evalue) 
		{
			return lsBlastInfos.get(0);
		}
		return null;
	}
	/**
	 * 
	 * 
	 * @param accID
	 * 	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
	 * 1: accID<br>
	 * 2: ����ת����ID<br>
	 * @param QtaxID
	 * @param blast �Ƿ���Ҫblast�����������blast����ôblastInfo��SubjectGene2GoInfo��SubjectUni2GoInfo��Ϊ��
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public static Blast2GeneInfo getBlastGen2Go(String[] accID, int QtaxID,boolean blast, int StaxID,double evalue) 
	{
		Blast2GeneInfo blast2GeneInfo = new Blast2GeneInfo();
		Gene2GoInfo Qgene2GoInfo =null;
		Uni2GoInfo Quni2GoInfo = null;
		
		if (accID[0].equals("geneID")) {
			Qgene2GoInfo = ServGo.getGen2GoInfo(accID, QtaxID);
		}
		else if(accID[0].equals("uniID"))
		{
			Quni2GoInfo = ServGo.getUni2GenGoInfo(accID, QtaxID);
		}
		else if(accID[0].equals("accID")) {
			Qgene2GoInfo = new Gene2GoInfo();
			Qgene2GoInfo.setQuaryID(accID[2]);
		}
		blast2GeneInfo.setQueryGene2GoInfo(Qgene2GoInfo);
		blast2GeneInfo.setQueryUni2GoInfo(Quni2GoInfo);
		
		if (!blast) {
			return blast2GeneInfo;
		}
		
		///////////////����blast��Ϣ/////////////////////////////////
		BlastInfo blastInfo = getBlastInfo(accID, QtaxID, StaxID, evalue);
		blast2GeneInfo.setBlastInfo(blastInfo);
		////////////////�ѵ�blast��Ϣ���ٻ�ȥ��geneInfo/////////////////////////////////////////
		if (blastInfo != null && blastInfo.getEvalue()<=evalue)
		{
			String tab = blastInfo.getSubjectTab();
			String[] subGenInfo = new String[3];
			subGenInfo[2] = blastInfo.getSubjectID();
			if (tab.equals("NCBIID")) 
			{
				subGenInfo[1] = ServAnno.getGenName(Long.parseLong(subGenInfo[2]));
				subGenInfo[0] = "geneID";
				blast2GeneInfo.setSubjectGene2GoInfo(ServGo.getGen2GoInfo(subGenInfo, StaxID));
			}
			else 
			{
				subGenInfo[1] = ServAnno.getUniGenName(subGenInfo[2]);
				subGenInfo[0] = "uniID";
				blast2GeneInfo.setSubjectUni2GoInfo(ServGo.getUni2GenGoInfo(subGenInfo, StaxID));
			}
		}
		return blast2GeneInfo;
	}
	
	
}
