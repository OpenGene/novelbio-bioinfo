package com.novelbio.database.model.modcopeid;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.domain.kegg.KGpathway;
import com.novelbio.database.model.modgo.GOInfoAbs;
import com.novelbio.database.model.modkegg.KeggInfo;

public interface CopedIDInt{
 
	/**
	 * idType��������IDTYPE�е�һ��
	 */
	public String getIDtype();
	
	/**
	 * �����accID
	 */
	public String getAccID();


	/**
	 * ���geneID
	 * @return
	 */
	public String getGenUniID();
	
	public int getTaxID() ;
	
	/**
	 * ��øû����description
	 * @return
	 */
	public String getDescription() ;
	/**
	 * ��øû����symbol
	 * @return
	 */
	public String getSymbo();
	
	/**
	 * ��ø�CopeID��List-KGentry,���û�л�Ϊ�գ��򷵻�null
	 * @param blast �Ƿ�blast����Ӧ���ֲ鿴
	 * @param StaxID ���blastΪtrue����ô�趨StaxID
	 * @return ���û�оͷ���null
	 */
	public ArrayList<KegEntity> getKegEntity(boolean blast) ;
	
	
	/**
	 * 	 * ָ��һ��dbInfo�����ظ�dbInfo����Ӧ��accID��û���򷵻�null
	 * @param dbInfo
	 * @return
	 */
	public String getAccIDDBinfo(String dbInfo);
	
	/**
	 * ���blast ѡ��blast�ĵ�һ������<br>
	 * 0:symbol 1:description 2:subjectTaxID 3:evalue 4:symbol 5:description �����blast 0:symbol 1:description
	 * @return
	 */
	public String[] getAnno( boolean blast ) ;
	/**
	 * �����Ӧ��KeggInfo��Ϣ
	 * @return
	 */
	public KeggInfo getKeggInfo();
	/**
	 * blast�������
	 * ����Ҫ�趨blast��Ŀ��
	 * �÷����� setBlastInfo(double evalue, int... StaxID)
	 * ����һϵ�е�Ŀ�����ֵ�taxID�����CopedIDlist
	 * ���û�н����ֱ�ӷ���null
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	public ArrayList<CopedID> getCopedIDLsBlast();
 
	/**
	 * �趨������ֽ���blast
	 * @param evalue
	 * @param StaxID
	 */
	public void setBlastInfo(double evalue, int... StaxID);
	/**
	 * ���ظ�CopeID����Ӧ��GO��Ϣ
	 * @return
	 */
	public ArrayList<AGene2Go> getGene2GO(String GOType);

	
	//////////   GoInfo   ////////////////
	/**
	 * 	blast�������
	 * �����趨blast������
	 * �÷����� setBlastInfo(double evalue, int... StaxID)
	 * ��þ���blast��GoInfo
	 */
	public ArrayList<AGene2Go> getGene2GOBlast(String GOType);
	/**
	 * blast�������
	 * ����Ҫ�趨blast��Ŀ��
	 * �÷����� setBlastInfo(double evalue, int... StaxID)
	 * @return
	 * ����blast����Ϣ������evalue�ȣ���list��getCopedIDLsBlast()�õ���list��һһ��Ӧ��
	 */
	public ArrayList<BlastInfo> getLsBlastInfos();

	/**
	 * ���ص�һ���ȶԵ�������
	 * @return
	 */
	CopedID getCopedIDBlast();
	/**
	 * 	blast�������
	 * �����趨blast������
	 * �÷����� setBlastInfo(double evalue, int... StaxID)
	 * ��þ���blast��KegPath
	 */
	ArrayList<KGpathway> getKegPath(boolean blast);
	/**
	 * ������֪��geneUniID��IDtype
	 * @param geneUniID
	 * @param idType ������CopedID.IDTYPE_GENEID�ȣ����Բ�����
	 */
	void setUpdateGeneID(String geneUniID, String idType);
	/**
	 * ����������Ҫ������GO��Ϣ���������
	 * ����ֻ���Ȼ�ȡGO����Ϣ������������method��ʱ��������
	 * @param GOID
	 * @param GOdatabase
	 * @param GOevidence
	 * @param GORef
	 * @param gOQualifiy
	 */
	void setUpdateGO(String GOID, String GOdatabase, String GOevidence,
			String GORef, String gOQualifiy);
	/**
	 * ������Ҫupdate��geneInfo��ע�ⲻ��Ҫ�趨geneUniID
	 * @param geneInfo
	 */
	void setUpdateGeneInfo(AGeneInfo geneInfo);
	/**
	 * ����µ�ID������UniID����ô��д��ָ�����ļ���
	 * �ļ���Ҫ�ʼ��setָ��
	 * @param updateUniID
	 */
	void update(boolean updateUniID);
	/**
	 * ����µ�ID������UniID����ô��д��ָ�����ļ���
	 * �ļ���Ҫ�ʼ��setָ��
	 * @param updateUniID
	 */
	void setUpdateDBinfo(String DBInfo, boolean overlapDBinfo);
	/**
	 * ����µ�ID������UniID����ô��д��ָ�����ļ���
	 * �ļ���Ҫ�ʼ��setָ��
	 * @param updateUniID
	 */
	void setUpdateRefAccID(int taxID, String DBInfo, String... refAccID);
}
