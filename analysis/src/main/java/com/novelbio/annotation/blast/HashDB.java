package com.novelbio.annotation.blast;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.entity.friceDB.NCBIID;

/**
 * �����ݿ��е�������ȡ���ڴ�����hash�����棬�����Ӷ�ȡ�ٶ�
 * @author zong0jie
 *
 */
public class HashDB {
	/**
	 * ��Ϊnt��nr���ļ���û��geneID������ID�����Ի�õ�����Ҫ�����ж��ǲ���ָ������
	 * ���Ա���������HashMap key��AccID Value��geneID <br>
	 * ��ЩaccID���ܻ��Ӧ���geneID��װ��  key��AccID Value��geneID//geneID
	 * ����hashMapΪstring-string
	 * @param taxID :ָ����Ҫ��õ����֣�ע�ⲻ�ܸ�����ʹ�ã���Ϊ����̫�����ڴ�Բ���
	 * @return hashMap key accID value geneID�ж��geneID����"//"����
	 */
	public static HashMap<String, String> getHashGenID(int taxID)
	{
		NCBIID ncbiid = new NCBIID();
		ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsNcbiids= DaoFSNCBIID.queryLsNCBIID(ncbiid);
		HashMap<String, String> hashAcc2GenID = new HashMap<String, String>();
		String geneID="";
		for (NCBIID ncbiid2 : lsNcbiids) 
		{
			//�����accID�Ѿ�����һ��GeneID
			if (( geneID = hashAcc2GenID.get(ncbiid2.getAccID()))!=null)
			{	//�Ƚ���ǰ��geneID���Ƿ��Ѿ������˸�GeneID
				if (!geneID.contains(ncbiid2.getGeneId()+"")) 
				{	//���û�еĻ����ͽ���geneID��ӽ�ȥ
					geneID = geneID + "//" + ncbiid2.getGeneId();
					hashAcc2GenID.put(ncbiid2.getAccID(), geneID);
				}
			}
			else {
				hashAcc2GenID.put(ncbiid2.getAccID(), ncbiid2.getGeneId()+"");
			}
		}
		return hashAcc2GenID;
	}
}
