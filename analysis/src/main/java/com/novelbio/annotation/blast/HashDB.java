package com.novelbio.annotation.blast;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.database.DAO.FriceDAO.DaoFSNCBIID;
import com.novelbio.database.entity.friceDB.NCBIID;

/**
 * 将数据库中的数据提取到内存中用hash来保存，以增加读取速度
 * @author zong0jie
 *
 */
public class HashDB {
	/**
	 * 因为nt和nr的文件中没有geneID和物种ID，所以获得的序列要首先判断是不是指定物种
	 * 所以本方法构建HashMap key：AccID Value：geneID <br>
	 * 有些accID可能会对应多个geneID，装成  key：AccID Value：geneID//geneID
	 * 所以hashMap为string-string
	 * @param taxID :指定想要获得的物种，注意不能给人类使用，因为人类太大了内存吃不消
	 * @return hashMap key accID value geneID有多个geneID的用"//"隔开
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
			//如果该accID已经有了一个GeneID
			if (( geneID = hashAcc2GenID.get(ncbiid2.getAccID()))!=null)
			{	//比较以前的geneID中是否已经含有了该GeneID
				if (!geneID.contains(ncbiid2.getGeneId()+"")) 
				{	//如果没有的话，就将该geneID添加进去
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
