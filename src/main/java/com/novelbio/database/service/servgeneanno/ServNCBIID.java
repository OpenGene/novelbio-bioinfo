package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.service.AbsGetSpring;
@Service
public class ServNCBIID extends AbsGetSpring implements MapNCBIID{
	private static Logger logger = Logger.getLogger(ServNCBIID.class);
	@Inject
	private MapNCBIID mapNCBIID;
	public ServNCBIID()  
	{
		mapNCBIID = (MapNCBIID) factory.getBean("mapNCBIID");
	}
	//TODO 正规写法
//	@Inject
//	protected MapNCBIID mapNCBIID;
//	private static ServGeneAnno info; 
//	@PostConstruct
//	public void init()
//	{
//	    info = this;
//	    info.mapNCBIID = this.mapNCBIID;
//	}
	@Override
	public NCBIID queryNCBIID(NCBIID QueryNCBIID) {
		return mapNCBIID.queryNCBIID(QueryNCBIID);
	}
	@Override
	public ArrayList<NCBIID> queryLsNCBIID(NCBIID QueryNCBIID) {
		return mapNCBIID.queryLsNCBIID(QueryNCBIID);
	}
	@Override
	public void insertNCBIID(NCBIID nCBIID) {
		mapNCBIID.insertNCBIID(nCBIID);
		
	}
	@Override
	public void updateNCBIID(NCBIID nCBIID) {
		mapNCBIID.updateNCBIID(nCBIID);
		
	}
	/**
	 * 首先用指定的数据库查找NCBIID表
	 * 如果找到了就返回找到的第一个的ncbiid对象
	 * 如果没找到，再去除dbinfo查找，如果还没找到，就返回Null
	 * @param geneID
	 * @param taxID
	 * @param dbInfo 为null表示不设置
	 * @return
	 */
	public NCBIID queryGenUniID(int geneID, int taxID, String dbInfo) {
		if (dbInfo != null) 
			dbInfo = dbInfo.trim();
		else 
			dbInfo = "";
		
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(geneID); ncbiid.setTaxID(taxID);
		if (!dbInfo.trim().equals("")) {
			ncbiid.setDBInfo(dbInfo.trim());
		}
		ArrayList<NCBIID> lsNcbiids= queryLsNCBIID(ncbiid);
		//如果带数据库的没找到，就重置数据库
		if (!dbInfo.equals("") && (lsNcbiids == null || lsNcbiids.size() < 1) ) {
			ncbiid.setDBInfo("");
			lsNcbiids= queryLsNCBIID(ncbiid);
		}
		if ((lsNcbiids == null || lsNcbiids.size() < 1)) {
			return null;
		}
		else {
			return lsNcbiids.get(0);
		}
	}
	
	/**
	 * <b>没有accID，放弃升级</b>
	 * 没有该ID就插入，有该ID的话看如果需要override，如果override且数据库不一样，就覆盖升级
	 * @param nCBIID
	 * @param override
	 */
	public boolean updateNCBIID(NCBIID ncbiid, boolean override) {
		String db = ncbiid.getDBInfo();
		//查询的时候为了防止查不到，先除去dbinfo的信息
		ncbiid.setDBInfo("");
		if (ncbiid.getAccID() == null) {
			logger.error("accID不存在，不能升级");
			return false;
		}
		ArrayList<NCBIID> lsResult = mapNCBIID.queryLsNCBIID(ncbiid);
		if (lsResult == null || lsResult.size() == 0) {
			//插入的时候再加上
			ncbiid.setDBInfo(db);
			try {
				mapNCBIID.insertNCBIID(ncbiid);
				return true;
			} catch (Exception e) {
				logger.error("cannot insert into database: " + ncbiid.getAccID());
				e.printStackTrace();
				return false;
			}
		}
		else {
			if (override && !lsResult.get(0).getDBInfo().equals(db)) {
				ncbiid.setDBInfo(db);
				try {
					mapNCBIID.updateNCBIID(ncbiid);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * 如果存在则返回第一个找到的geneID
	 * 不存在就返回null
	 * @param geneID 输入geneID
	 * @param taxID 物种ID
	 * @return
	 */
	public NCBIID queryNCBIID(int geneID, int taxID) {
		if (geneID <= 0) {
			return null;
		}
		NCBIID ncbiid = new NCBIID();
		ncbiid.setGeneId(geneID);
		ncbiid.setTaxID(taxID);
		ArrayList<NCBIID> lsResult = mapNCBIID.queryLsNCBIID(ncbiid);
		if (lsResult == null || lsResult.size() == 0) {
			return null;
		}
		else {
			return lsResult.get(0);
		}
	}
}
