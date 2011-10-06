package com.novelbio.analysis.annotation.copeID;

import java.util.ArrayList;

import com.novelbio.analysis.annotation.GO.goEntity.GOInfoAbs;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KeggInfo;
import com.novelbio.database.entity.friceDB.AGene2Go;
import com.novelbio.database.entity.friceDB.AGeneInfo;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.kegg.KGpathway;

public interface CopedIDInt{
	
	
	/**
	 * ��ñ�copedID blast����Ӧ���ֵ�blastInfo��Ϣ��û�оͷ���null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public BlastInfo setBlastInfo(int StaxID, double evalue);
	
	
	/**
	 * ��ñ�copedID blast����Ӧ���ֵ�copedID��û�оͷ���null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public CopedID getCopedIDBlast(int StaxID,double evalue) ;
	
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
	public ArrayList<KegEntity> getKegEntity(boolean blast,int StaxID,double evalue) ;
	
	
	/**
	 * 	 * ָ��һ��dbInfo�����ظ�dbInfo����Ӧ��accID��û���򷵻�null
	 * @param dbInfo
	 * @return
	 */
	public String getAccIDDBinfo(String dbInfo);
	
	/**
	 * ���blast * 0:symbol 1:description 2:subjectTaxID 3:evalue 4:symbol 5:description �����blast 0:symbol 1:description
	 * @return
	 */
	public String[] getAnno( boolean blast, int StaxID, double evalue) ;
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
	 * 	blast�������
	 * �����趨blast������
	 * �÷����� setBlastInfo(double evalue, int... StaxID)
	 * ��þ���blast��KegPath
	 */
	public ArrayList<KGpathway> getKegPathBlast();
//	/**
//	 * blast��������
//	 * ����blast����copedID���� getBlastCopedID(int StaxID,double evalue) �������
//	 * �÷����� setBlastInfo(double evalue, int... StaxID)
//	 * ��þ���blast��KegPath
//	 */
//	public ArrayList<KGpathway> getBlastKegPath(CopedID copedID);
	/**
	 * ��ø�copedID��KegPath
	 */
	public ArrayList<KGpathway> getKegPath();
	/**
	 * �趨������ֽ���blast
	 * @param evalue
	 * @param StaxID
	 */
	public void setBlastLsInfo(double evalue, int... StaxID);
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
}
