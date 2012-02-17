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
	/**
	 * ������ݿ���û���ҵ���Ӧ��accID�����趨ʱ���������ʲôtaxID�����صľ���ͬ����taxID
	 * @return
	 */
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
	public String getSymbol();
	
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
	 * ���趨blast����� ���blast * 0:symbol 1:description  2:subjectSpecies 3:evalue
	 * 4:symbol 5:description �����blast 0:symbol 1:description
	 * 
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
	 * ����������Ҫ������GO��Ϣ���������<br>
	 * ����ֻ���Ȼ�ȡGO����Ϣ������������method��ʱ��������<br>
	 * �����������ϵ����
	 * @param GOID
	 * @param GOdatabase
	 * @param GOevidence
	 * @param GORef a GO_REF identifier. See section 8 and
http://www.geneontology.org/doc/GO.references<br>
for an explanation of the reference types used.<br>
Examples: PMID:9058808<br>
DOI:10.1046/j.1469-8137.2001.00150.x<br>
GO_REF:0000002<br>
GO_REF:0000020<br>
GO_REF:0000004<br>
	 * @param gOQualifiy
	 */
	void setUpdateGO(String GOID, String GOdatabase, String GOevidence,
			String GORef, String gOQualifiy);
	/**
	 * ������Ҫupdate��geneInfo��ע�ⲻ��Ҫ�趨geneUniID�������ǵ�������pubmedID��Ϣ������
	 * <b>���Ҫ�趨geneinfo��dbinfo</b>��dbinfo���ж���geneinfo���ݿ���Դ����Ϣ
	 * 
	 * @param geneInfo
	 */
	public void setUpdateGeneInfo(AGeneInfo geneInfo);
	/**
	 * ����µ�ID������UniID����ô�ͷ���false
	 * ����������ɹ���Ҳ����false
	 * �ļ���Ҫ�ʼ��setָ��
	 * @param updateUniID
	 */
	boolean update(boolean updateUniID);
	/**
	 * ����µ�ID������UniID����ô��д��ָ�����ļ���
	 * �ļ���Ҫ�ʼ��setָ��
	 * @param updateUniID
	 */
	void setUpdateDBinfo(String DBInfo, boolean overlapDBinfo);
	/**
	 * ��¼���������������ݿ��ID Ʃ����һ��ID��NCBI�ı��ID�й��������ñ��ID���������ݿ⣬�Ա��ø�accID����Ӧ��genUniID
	 * @param updateUniID
	 */
	void setUpdateRefAccID(String... refAccID);
	/**
	 * ��¼���������������ݿ��ID Ʃ����һ��ID��NCBI�ı��ID�й��������ñ��ID���������ݿ⣬�Ա��ø�accID����Ӧ��genUniID
	 */
	void setUpdateRefAccID(ArrayList<String> lsRefAccID);
	/**
	 * ���û��QueryID, SubjectID, taxID�е��κ�һ��Ͳ����� ���evalue>50 �� evalue<0���Ͳ�����
	 * �����������ϵ����
	 * @param SubAccID Ŀ�����ֵ�accID
	 * @param subDBInfo Ŀ�����ֵ����ݿ���Դ
	 * @param SubTaxID Ŀ�����ֵ�����ID
	 * @param evalue ���ƶ�evalue
	 * @param identities ���ƶ���Ϣ
	 */
	void setUpdateBlastInfo(String SubAccID, String subDBInfo, int SubTaxID,
			double evalue, double identities);
	/**
	 * �趨accID������geneUniID����new copedID����ʱʹ��
	 * @param accID
	 */
	void setUpdateAccID(String accID);
	/**
	 * ����geneinfo��Ϣ
	 * @return
	 */
	AGeneInfo getGeneInfo();
	/**
	 * �ڲ���refaccID��Ϊ���ս�������ID��ʱ���Ƿ������uniqID
	 * uniqID���ø����Ĳο�ID���ҵ����ݿ��е�Ψһ����
	 * true��ֻ�е�uniqIDʱ������
	 * null��Ĭ�ϲ���--��uniqIDҲ����������ֻ������һ������
	 * false����uniqIDҲ������������������ȫ��ID���ù�����δʵ��
	 * @param uniqID
	 */
	void setUpdateRefAccID(Boolean uniqID);
	/**
	 * ���blast��geneUniID��ȥ
	 * @param SubGenUniID
	 * @param subIDtype
	 * @param subDBInfo
	 * @param SubTaxID
	 * @param evalue
	 * @param identities
	 */
	void setUpdateBlastInfo(String SubGenUniID, String subIDtype,
			String subDBInfo, int SubTaxID, double evalue, double identities);

}
