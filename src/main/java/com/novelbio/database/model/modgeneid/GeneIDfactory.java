package com.novelbio.database.model.modgeneid;

public class GeneIDfactory implements GeneIDfactoryInt {
	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * @param idType  ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID ����ID
	 */
	public GeneIDInt createGeneID(String idType, String genUniID, int taxID) {
		GeneIDabs geneID = null;
		genUniID = genUniID.trim();
		if (genUniID.equals("")) {
			genUniID = null;
		}
		if (idType.equals(GeneID.IDTYPE_UNIID)) {
			geneID = new GeneIDUni(null, genUniID, taxID);
		}
		else if (idType.equals(GeneID.IDTYPE_GENEID)) {
			geneID = new GeneIDNcbi(null, genUniID, taxID);
		}
		else if (idType.equals(GeneID.IDTYPE_ACCID)) {
			geneID = new GeneIDAccID(null, genUniID, taxID);
		}
		return geneID;
	}
	
	
	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * 
	 * @param idType
	 *            ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID
	 *            ����ID
	 */
	public GeneIDInt createGeneID(String accID,String idType, String genUniID, int taxID) {
		GeneIDabs geneID = null;
		if (accID != null) {
			accID = accID.replace("\"", "").trim();
			if (accID.equals("")) {
				accID = null;
			}
		}
		if (idType.equals(GeneID.IDTYPE_UNIID)) {
			geneID = new GeneIDUni(accID, genUniID, taxID);
		}
		else if (idType.equals(GeneID.IDTYPE_GENEID)) {
			geneID = new GeneIDNcbi(accID, genUniID, taxID);
		}
		else if (idType.equals(GeneID.IDTYPE_ACCID)) {
			geneID = new GeneIDAccID(accID, genUniID, taxID);
		}
		return geneID;
	}
}
