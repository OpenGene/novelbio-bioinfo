package com.novelbio.database.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.analysis.annotation.copeID.CopedID;
import com.novelbio.database.DAO.FriceDAO.DaoFCGene2GoInfo;
import com.novelbio.database.entity.friceDB.AGene2Go;
import com.novelbio.database.entity.friceDB.Blast2GeneInfo;
import com.novelbio.database.entity.friceDB.BlastInfo;
import com.novelbio.database.entity.friceDB.Gene2Go;
import com.novelbio.database.entity.friceDB.Gene2GoInfo;
import com.novelbio.database.entity.friceDB.GeneInfo;
import com.novelbio.database.entity.friceDB.Go2Term;
import com.novelbio.database.entity.friceDB.NCBIID;
import com.novelbio.database.entity.friceDB.Uni2GoInfo;
import com.novelbio.database.entity.friceDB.UniGene2Go;
import com.novelbio.database.entity.friceDB.UniGeneInfo;
import com.novelbio.database.entity.friceDB.UniProtID;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;
import com.novelbio.database.mapper.geneanno.MapGene2Go;
import com.novelbio.database.mapper.geneanno.MapGo2Term;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.mapper.geneanno.MapUniGene2Go;
import com.novelbio.database.mapper.geneanno.MapUniProtID;

public class ServGo {
	/**
	 * 存储Go2Term的信息
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 */
	static HashMap<String, String[]> hashGo2Term = new HashMap<String, String[]>();
	
	/**
	 * 将所有GO信息提取出来放入hash表中，方便查找
	 * 存储Go2Term的信息
	 * key:Go
	 * value:GoInfo
	 * 0:QueryGoID,1:GoID,2:GoTerm 3:GoFunction
	 * 如果已经查过了一次，自动返回
	 */
	public static HashMap<String, String[]> getHashGo2Term() {
		if (!hashGo2Term.isEmpty()) {
			return hashGo2Term;
		}
		Go2Term go2Term = new Go2Term();
		ArrayList<Go2Term> lsGo2Terms = MapGo2Term.queryLsGo2Term(go2Term);
		for (Go2Term go2Term2 : lsGo2Terms) 
		{
			String[] strgo2term = new String[4];
			strgo2term[0] = go2Term2.getGoIDQuery(); strgo2term[1] = go2Term2.getGoID();
			strgo2term[2] = go2Term2.getGoTerm(); strgo2term[3] = go2Term2.getGoFunction();
			hashGo2Term.put(strgo2term[0], strgo2term);
		}
		return hashGo2Term;
	}
	
	
	
	/**
	 * @param genInfo
	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
	 * @param taxID
	 * @return Gen2GoInfo 的具体信息
	 */
	public static Gene2GoInfo getGen2GoInfo(String[] genInfo,int taxID)
	{
			NCBIID ncbiid = new NCBIID();
			ncbiid.setAccID(genInfo[1]);
			ncbiid.setGeneId(Long.parseLong(genInfo[2]));
			ncbiid.setTaxID(taxID);
			return DaoFCGene2GoInfo.queryGeneDetail(ncbiid);
	}
	
	/**
	 * @param genInfo
	 * 0: ID类型："geneID"或"uniID"或"accID"<br>
	 * 1: accID<br>
	 * 2: 具体转换的ID<br>
	 * @param taxID
	 * @return Uni2GenGoInfo 的具体信息
	 */
	public static Uni2GoInfo getUni2GenGoInfo(String[] genInfo,int taxID)
	{
		UniProtID uniProtID = new UniProtID();
		uniProtID.setAccID(genInfo[1]);
		uniProtID.setUniID(genInfo[2]);
		uniProtID.setTaxID(taxID);
		return DaoFCGene2GoInfo.queryUniDetail(uniProtID);
	}
	
	/**
	 * @param ncbiid geneID属性必须有
	 * @return
	 */
	public static ArrayList<Gene2Go> getGen2Go(NCBIID ncbiid)
	{
		long GeneID = ncbiid.getGeneId();
		return MapGene2Go.queryGene2Go(GeneID);
	}

	
	
	
	/**
	 * @param uniProtID uniID属性必须有
	 * @return
	 */
	public static ArrayList<UniGene2Go> getUniGen2Go(UniProtID uniProtID)
	{
		return MapUniGene2Go.queryUniGene2Go(uniProtID.getUniID());
	}
	
}
