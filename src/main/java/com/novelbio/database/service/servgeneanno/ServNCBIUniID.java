package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.mapper.geneanno.MapNCBIID;
import com.novelbio.database.mapper.geneanno.MapUniProtID;
import com.novelbio.database.service.SpringFactory;
@Service
public class ServNCBIUniID {
	private static final Logger logger = Logger.getLogger(ServNCBIUniID.class);
	@Inject
	private MapNCBIID mapNCBIID;
	@Inject
	private MapUniProtID mapUniProtID;
	
	public ServNCBIUniID() {
		mapNCBIID = (MapNCBIID) SpringFactory.getFactory().getBean("mapNCBIID");
		mapUniProtID = (MapUniProtID) SpringFactory.getFactory().getBean("mapUniProtID");
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

	public AgeneUniID queryNCBIUniIID(AgeneUniID QueryNCBIID) {
		if (QueryNCBIID instanceof NCBIID) {
			return mapNCBIID.queryNCBIID((NCBIID)QueryNCBIID);
		} else if (QueryNCBIID instanceof UniProtID) {
			return mapUniProtID.queryUniProtID((UniProtID)QueryNCBIID);
		} else {
			logger.error("出现未知类型: " + QueryNCBIID.getAccID() + "\t" + QueryNCBIID.getGeneIDtype());
			return null;
		}
	}
	
	public ArrayList<? extends AgeneUniID> queryLsAgeneUniID(AgeneUniID QueryNCBIID) {
		if (QueryNCBIID instanceof NCBIID) {
			return mapNCBIID.queryLsNCBIID((NCBIID)QueryNCBIID);
		} else if (QueryNCBIID instanceof UniProtID) {
			return mapUniProtID.queryLsUniProtID((UniProtID)QueryNCBIID);
		} else {
			logger.error("出现未知类型: " + QueryNCBIID.getAccID() + "\t" + QueryNCBIID.getGeneIDtype());
			return new ArrayList<AgeneUniID>();
		}
	}
	
	public void insertNCBIUniID(AgeneUniID ageneUniID) {
		if (ageneUniID instanceof NCBIID) {
			mapNCBIID.insertNCBIID((NCBIID)ageneUniID);
		} else if (ageneUniID instanceof UniProtID) {
			mapUniProtID.insertUniProtID((UniProtID)ageneUniID);
		} else {
			logger.error("出现未知类型: " + ageneUniID.getAccID() + "\t" + ageneUniID.getGeneIDtype());
		}
	}
	
	public void updateNCBIUniID(AgeneUniID ageneUniID) {
		if (ageneUniID instanceof NCBIID) {
			mapNCBIID.updateNCBIID((NCBIID)ageneUniID);
		} else if (ageneUniID instanceof UniProtID) {
			mapUniProtID.updateUniProtID((UniProtID)ageneUniID);
		} else {
			logger.error("出现未知类型: " + ageneUniID.getAccID() + "\t" + ageneUniID.getGeneIDtype());
		}
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
	public AgeneUniID queryGenUniID(int idType, String geneUniID, int taxID, String dbInfo) {
		if (dbInfo != null) 
			dbInfo = dbInfo.trim();
		else 
			dbInfo = "";
		
		AgeneUniID ageneUniID = AgeneUniID.creatAgeneUniID(idType);
		ageneUniID.setGenUniID(geneUniID); ageneUniID.setTaxID(taxID);
		if (!dbInfo.trim().equals("")) {
			ageneUniID.setDataBaseInfo(dbInfo.trim());
		}
		ArrayList<? extends AgeneUniID> lsAgeneUniIDs= queryLsAgeneUniID(ageneUniID);
		//如果带数据库的没找到，就重置数据库
		if (!dbInfo.equals("") && (lsAgeneUniIDs == null || lsAgeneUniIDs.size() < 1) ) {
			ageneUniID.setDataBaseInfo("");
			lsAgeneUniIDs= queryLsAgeneUniID(ageneUniID);
		}
		if ((lsAgeneUniIDs == null || lsAgeneUniIDs.size() < 1)) {
			return null;
		} else {
			return lsAgeneUniIDs.get(0);
		}
	}
	
	/**
	 * <b>没有accID，放弃升级</b>
	 * 没有该ID就插入，有该ID的话看如果需要override，如果override且数据库不一样，就覆盖升级
	 * @param nCBIID
	 * @param override
	 */
	public boolean updateNCBIUniID(AgeneUniID ncbiid, boolean override) {
		if (ncbiid.getAccID().length() > 30) {
			logger.error("accID太长：" + ncbiid.getAccID() + "\t" + ncbiid.getDataBaseInfo().getDbName());
			return false;
		}
		if (ncbiid.getAccID().contains("GO:")) {
			logger.error("不能导入GO信息");
			return false;
		}
		if (ncbiid.getAccID() == null) {
			logger.error("accID不存在，不能升级");
			return false;
		}
		
		String db = ncbiid.getDataBaseInfo().getDbName();
		String geneID = ncbiid.getGenUniID();
		//查询的时候为了防止查不到，先除去dbinfo的信息
		ncbiid.setDataBaseInfo("");
		if (ncbiid.getTaxID() != 0 
				&& !ncbiid.getDataBaseInfo().getDbName().equals(DBAccIDSource.Symbol) 
				&& ncbiid.getDataBaseInfo().getDbName().equals(DBAccIDSource.Synonyms)) {
			ncbiid.setGenUniID("0");
		}

		ArrayList<? extends AgeneUniID> lsResult = queryLsAgeneUniID(ncbiid);
		if (lsResult == null || lsResult.size() == 0) {
			//插入的时候再加上
			ncbiid.setDataBaseInfo(db);
			ncbiid.setGenUniID(geneID);
			try {
				insertNCBIUniID(ncbiid);
				return true;
			} catch (Exception e) {
				logger.error("cannot insert into database: " + ncbiid.getAccID());
				try {
					updateNCBIUniID(ncbiid);
				} catch (Exception e2) {
					e.printStackTrace();
				}
				return false;
			}
		} else {
			AgeneUniID ncbiidSub = lsResult.get(0);
			if (!geneID.equals(ncbiidSub.getGenUniID())) {
				logger.error("该AccID已经对应到了一个不同的GeneID上，因此没有升级该ID" + ncbiid.getAccID() + "geneid:" + ncbiid.getGenUniID());
				return false;
			}
			
			if (override && !ncbiidSub.getDataBaseInfo().getDbName().equals(db)) {
				ncbiid.setDataBaseInfo(db);
				ncbiid.setGenUniID(geneID);
				try {
					updateNCBIUniID(ncbiid);
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
	public AgeneUniID queryNCBIUniID(int idType, String geneID, int taxID) {
		if (geneID == null || geneID.equals("") || geneID.equals("0")) {
			return null;
		}
		AgeneUniID ncbiid = AgeneUniID.creatAgeneUniID(idType);
		ncbiid.setGenUniID(geneID);
		ncbiid.setTaxID(taxID);
		ArrayList<? extends AgeneUniID> lsResult = queryLsAgeneUniID(ncbiid);
		if (lsResult == null || lsResult.size() == 0) {
			return null;
		}
		else {
			return lsResult.get(0);
		}
	}
}
