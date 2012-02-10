package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.geneanno.MapUniGene2Go;
import com.novelbio.database.mapper.geneanno.MapUniProtID;
import com.novelbio.database.service.AbsGetSpring;

@Service
public class ServUniProtID extends AbsGetSpring implements MapUniProtID{
	@Inject
	MapUniProtID mapUniProtID;
	
	public ServUniProtID() {
		mapUniProtID = (MapUniProtID) factory.getBean("mapUniProtID");
	}
	
	@Override
	public UniProtID queryUniProtID(UniProtID QueryUniProtID) {
		return mapUniProtID.queryUniProtID(QueryUniProtID);
	}

	@Override
	public ArrayList<UniProtID> queryLsUniProtID(UniProtID QueryUniProtID) {
		return mapUniProtID.queryLsUniProtID(QueryUniProtID);
	}

	@Override
	public void insertUniProtID(UniProtID UniProtID) {
		mapUniProtID.insertUniProtID(UniProtID);
		
	}

	@Override
	public void updateUniProtID(UniProtID UniProtID) {
		mapUniProtID.updateUniProtID(UniProtID);
		
	}
	
	/**
	 * 首先用指定的数据库查找UniProtID表
	 * 如果找到了就返回找到的第一个的uniprotID对象
	 * 如果没找到，再去除dbinfo查找，如果还没找到，就返回Null
	 * @param geneID
	 * @param taxID
	 * @param dbInfo
	 * @return
	 */
	public UniProtID queryGenUniID(String uniID, int taxID, String dbInfo) {
		if (dbInfo != null) 
			dbInfo = dbInfo.trim();
		else 
			dbInfo = "";
		
		UniProtID uniProtID = new UniProtID();
		uniProtID.setUniID(uniID); uniProtID.setTaxID(taxID);
		if (!dbInfo.trim().equals("")) {
			uniProtID.setDBInfo(dbInfo.trim());
		}
		ArrayList<UniProtID> lsUniProtIDs= queryLsUniProtID(uniProtID);
		//如果带数据库的没找到，就重置数据库
		if (!dbInfo.equals("") && (lsUniProtIDs == null || lsUniProtIDs.size() < 1) ) {
			uniProtID.setDBInfo("");
			lsUniProtIDs = queryLsUniProtID(uniProtID);
		}
		if ((lsUniProtIDs == null || lsUniProtIDs.size() < 1)) {
			return null;
		}
		else {
			return lsUniProtIDs.get(0);
		}
	}
	
	/**
	 * 没有该ID就插入，有该ID的话看如果需要override，如果override且数据库不一样，就覆盖升级<br>
	 * 注意这里只比较第一个dbInfo
	 * @param nCBIID
	 * @param override
	 */
	public void updateUniProtID(UniProtID uniProtID, boolean override) {
		String db = uniProtID.getDBInfo();
		//查询的时候为了防止查不到，先除去dbinfo的信息
		uniProtID.setDBInfo("");
		ArrayList<UniProtID> lsResult = mapUniProtID.queryLsUniProtID(uniProtID);
		if (lsResult == null || lsResult.size() == 0) {
			uniProtID.setDBInfo(db);
			try {
				mapUniProtID.insertUniProtID(uniProtID);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		else {
			if (override && !lsResult.get(0).getDBInfo().equals(db)) {
				uniProtID.setDBInfo(db);
				mapUniProtID.updateUniProtID(uniProtID);
			}
		}
	}
	/**
	 * 如果存在则返回第一个找到的geneID
	 * 不存在就返回null
	 * @param geneID 输入geneID
	 * @param taxID 物种ID
	 * @return
	 */
	public UniProtID getUniProtID(String genUniID, int taxID) {
		UniProtID uniProtID = new UniProtID();
		uniProtID.setGenUniID(genUniID);
		uniProtID.setTaxID(taxID);
		List<UniProtID> lsUniIds = mapUniProtID.queryLsUniProtID(uniProtID);
		if (lsUniIds == null || lsUniIds.size() == 0) {
			return null;
		}
		else
			return lsUniIds.get(0);
	}

}
