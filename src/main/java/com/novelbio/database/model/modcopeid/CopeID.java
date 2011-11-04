package com.novelbio.database.model.modcopeid;


import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery;
import com.novelbio.database.service.ServAnno;

/**
 * ר�ŶԻ����ID��һЩ�������<br>
 * Ҳ���Խ������ID�ϲ����������ҽ���ɢ��ID�洢��һ��Hashmap��
 * @author zong0jie
 *
 */
public class CopeID {
	/**
	 * blast�Ľ����������dbj|AK240418.1|
	 * �������AK240418ץ����������
	 * @return
	 */
	public static String getBlastAccID(String blastGenID) {
		String[] ss = blastGenID.split("\\|");
		return removeDot(ss[1]);
	}
	
	/**
	 *  ���ȳ�ȥ�ո�
	 * �������XM_002121.1���ͣ���ô��.1ȥ��
	 * @param accID
	 * @return accID without .1
	 */
	public static String removeDot(String accID)
	{
		String tmpGeneID = accID.trim();
		int dotIndex = tmpGeneID.lastIndexOf(".");
		//�������XM_002121.1����
		if (dotIndex>0 && tmpGeneID.length() - dotIndex == 2) {
			tmpGeneID = tmpGeneID.substring(0,dotIndex);
		}
		return tmpGeneID;
	}
	

//	static HashMap<String, String> hashGeneID2AccID = new HashMap<String, String>();
	/**
	 * ������ҵ�geneID��accID�Ĺ�ϵ��ֻ�е���Ҫ�ϲ�IDʱ��hashmap�Ż����<br>
	 * key:ĳһ�������µ�hashgene2AccID��Ϣ��Ʃ���ϵ����µ��򱳾�
	 * 
	 * key:geneID/UniID<br>
	 * value:accID//accID//accID
	 */
	static HashMap<String, HashMap<String, String>> hashCondGeneID2AccID = new HashMap<String, HashMap<String,String>>();
	/**
	 * ��ñ�����ҵ�geneID��accID�Ĺ�ϵ��ֻ�е���Ҫ�ϲ�IDʱ��hashmap�Ż����<br>
	 * ������������getGenID����<br>
	 * @param ָ����һ�������µ�hashGene2Acc��Ʃ���ϵ����µ��򱳾�
	 * key:geneID/UniID<br>
	 * value:accID//accID//accID
	 * @return
	 */
	public static HashMap<String, String> getHashGenID2AccID(String cond) {
		return hashCondGeneID2AccID.get(cond);
	}
	
	/**
	 * ���������accID��ȥ�ظ��õ�
	 */
	static HashSet<String> hashAccID = new HashSet<String>();
	
	/**
	 * ���������geneID��ȥ�ظ��õ�
	 */
	static HashSet<String> hashGenID = new HashSet<String>();
	
	/**
	 * ���� accID��taxID,accID���֮ǰ��һ��trim
	 * ��Ϊ�ܳ�����һ��accID���Ӧ���geneID�������Ƿ���list
	 * @param accID
	 * @param taxID ���accID����symbol��taxID����Ϊ0
	 * @param sepID �Ƿ�ֿ�ID
	 * @param hashGeneID2AccID ÿ��geneID ��Ӧ�� accID�б��ںϲ�IDʱ�õ�
	 * @return
	 * arraylist-string[3]
	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
	 * 1: accID<br>
	 * 2: ����ת����ID<br>
	 * ��hashGeneID2AccID�е��ظ�ID�᷵��null
	 */
	private static ArrayList<String[]> combainID( String accID, int taxID,boolean sepID,HashMap<String, String> hashGeneID2AccID) 
	{
		
		////////////////////////ȥ�ظ�///////////////////////////////////////////////////
		if (hashAccID.contains(accID)) {
			return null;
		}
		else {
			hashAccID.add(accID);
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////
		ArrayList<String[]> lsAccResult = new ArrayList<String[]>();
		
		ArrayList<String> lsaccID = ServAnno.getNCBIUni(accID, taxID);
		String type = lsaccID.get(0);
		 for (int i = 1 ; i<lsaccID.size();i++) {
			 String accID2;
			 String tmpGenID = lsaccID.get(i);
			 
			 String[] accIDResult = new String[3];
			 accIDResult[0] = type;
			 accIDResult[1] = accID;
			 accIDResult[2] = tmpGenID;

			 
			 
			 lsAccResult.add(accIDResult);
			 //////////////////////////////////////////////////
			 if ((accID2 = hashGeneID2AccID.get(tmpGenID))!=null) //���hash�����Ѿ����ڣ����������accID����hash��
			 {
				 String[] ss = accID2.split("//");
				 boolean add = true;
				for (String string : ss) {
					if (string.equals(accID)) {
						add = false;
						break;
					}
				}
				if (add) {
					accID2 = accID2 + "//" + accID;
					hashGeneID2AccID.put(tmpGenID, accID2);
				}
			 }
			 else//û�о�װ��hashMap
			 {
				hashGeneID2AccID.put(tmpGenID, accID);
			 }
			 //////////////////////////////////////////////////
			 if (!sepID) //�ϲ�ID
			 {
					if (hashGenID.contains(tmpGenID)) {
						return null;
					}
					else {
						hashGenID.add(tmpGenID);
					}
			 }
			 //////////////////////////////////////////////////
		}
		 return lsAccResult;
	}
	
	/**
	 * ����һϵ��accID���Լ�taxID, ����һϵ�е�accIDȥ�ظ������ϳ�һ��list,����һ��accID��Ӧ���geneID������Ѿ����ǽ�ȥ
	 * ������Խ���go�Լ�pathway�ķ���
	 * ���colAccID.size() == 0��ֱ�ӷ���һ���յĽ��������size==0,������null
	 * @param condition ָ��������Ʃ���ϵ����µ��򱳾�
	 * @param colAccID һϵ�е�accID�������list��ʽ
	 * @param taxID
	 * @param sepID
	 * true�����ϲ�ID�����е�accID����ȥ�ظ���õ����
	 * false���ϲ�ID�����е�accID����ͬgeneID��ֻ����һ�����hashGeneID2AccID��Ȼ��ӽ�ȥ
	 * @return 
	 * ����һϵ�е�accIDȥ�ظ������ϳ�һ��list<br>
	 * arrayList-string[3] :<br>
	 * 0: ID���ͣ�"geneID"��"uniID"��"accID"<br>
	 * 1: accID<br>
	 * 2: ����ת����ID<br>
	 */
	public static ArrayList<String[]> getGenID(String condition, AbstractCollection<String> colAccID, int taxID,boolean sepID)
	{
		hashAccID = new HashSet<String>();
		hashGenID = new HashSet<String>();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		if (colAccID.size() == 0) {
			return lsResult;
		}
		HashMap<String, String> hashGeneID2AccID = new HashMap<String, String>();
		hashCondGeneID2AccID.put(condition, hashGeneID2AccID);
		for (String string : colAccID) {
			if (string == null || string.trim().equals("")) {
				continue;
			}
			ArrayList<String[]> lsTmp = combainID(removeDot(string), taxID,sepID,hashGeneID2AccID);
			if (lsTmp != null ) {
				lsResult.addAll(lsTmp);
			}
		}
		return lsResult;
	}
	
	/**
	 * 
	 * 
	 * �ںϲ�ID������£����ֿ���accIDװ���ϲ�geneID��ǰ��ȥ��
	 * ��Ҫ��һ��geneID��Ӧ�ö��accID��Ȼ�����õ��˺ϲ�geneID��list����ô�����Ҫ��accID������ȥ��Ҳ��������һ����������ͬ�ķǺϲ�ID��
	 * @param condition ���ĳ�������µ�hashgene2AccID��Ϣ��Ʃ���ϵ����µ��򱳾���hashgene2AccID�ڲ���ʱ������ݲ�ͬ��condition������ͬ��hashgene2AccID��Ʃ���ϵ�һ�����µ�һ��������һ��
	 * @param lsGenInfo ������gene��Ϣ
	 * @param searchCol ָ��lsGenInfo�ĵڼ���ΪΨһID�У�Ҳ����geneID�У���0��ʼ����
	 * @param replaceCol ָ��lsGenInfo�ĵڼ�����QueryID�У�Ҳ����̽���У���0��ʼ����
	 * @param sepID �Ƿ���Ҫ�ֿ������true����ô��������û�仯
	 * @return
	 */
	public static ArrayList<String[]> copeCombineID(String condition, ArrayList<String[]> lsGenInfo,int searchCol,int replaceCol,boolean sepID) {
		   ArrayList<String[]> lsResultFinal = null;
	        //����ϲ�ID����ôҪ��ÿһ�������accID�Ե���Ӧ��geneIDǰ��
			HashMap<String, String> hashGeneID2AccID = hashCondGeneID2AccID.get(condition);
	        if (!sepID) 
	        {
	        	//////////�ϲ�lsGoResult///////////////////////////////////////////////////////
	        	lsResultFinal = new ArrayList<String[]>();
	        	for (String[] strings : lsGenInfo) 
	        	{
	        		//���ĳ��geneID������accID
	        	
	        		String[] accIDarray = hashGeneID2AccID.get(strings[searchCol]).split("//");
	        		for (int i = 0; i < accIDarray.length; i++) {
	        			//����ЩaccID�����ӵ���geneID�ϣ�����װ��list
						String[] tmpResultFinal = new String[strings.length];
						for (int j = 0; j < tmpResultFinal.length; j++) {
							tmpResultFinal[j] = strings[j];
						}
						tmpResultFinal[replaceCol] = accIDarray[i];
						lsResultFinal.add(tmpResultFinal);
	 				}
				}
	        	return lsResultFinal;
			}
	        else 
	        {
	        	return  lsGenInfo;
			}
	        
	}
	
}
