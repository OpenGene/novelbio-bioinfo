package com.novelbio.analysis.annotation.copeID;

import java.util.ArrayList;
import com.novelbio.analysis.annotation.pathway.kegg.pathEntity.KegEntity;
import com.novelbio.database.entity.friceDB.BlastInfo;

public interface ImpleCopedID {
	
	
	/**
	 * ��ñ�copedID blast����Ӧ���ֵ�blastInfo��Ϣ��û�оͷ���null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public BlastInfo getBlastInfo(int StaxID, double evalue);
	
	
	/**
	 * ��ñ�copedID blast����Ӧ���ֵ�copedID��û�оͷ���null
	 * @param StaxID
	 * @param evalue
	 * @return
	 */
	public CopedID getBlastCopedID(int StaxID,double evalue) ;
	/**
	 * ����һϵ�е�Ŀ�����ֵ�taxID�����CopedIDlist
	 * ���û�н����ֱ�ӷ���null
	 * @param evalue
	 * @param StaxID
	 * @return
	 */
	public ArrayList<CopedID> getBlastCopedID(double evalue, int... StaxID);
	
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
	
}
