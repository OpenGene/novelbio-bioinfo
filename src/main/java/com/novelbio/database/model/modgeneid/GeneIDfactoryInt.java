package com.novelbio.database.model.modgeneid;

public interface GeneIDfactoryInt {

	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * @param idType  ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID ����ID
	 */
	public GeneIDInt createGeneID(String idType, String genUniID, int taxID);
	
	/**
	 * �趨��ʼֵ������֤ ��������ݿ���û���ҵ���Ӧ��geneUniID���򷵻�null ֻ�ܲ���һ��CopedID����ʱaccID = ""
	 * 
	 * @param idType
	 *            ������IDTYPE�е�һ��
	 * @param genUniID
	 * @param taxID
	 *            ����ID
	 */
	public GeneIDInt createGeneID(String accID,String idType, String genUniID, int taxID);

}
