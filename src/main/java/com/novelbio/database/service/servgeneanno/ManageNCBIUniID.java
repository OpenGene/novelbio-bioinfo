package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.mongorepo.geneanno.RepoNCBIID;
import com.novelbio.database.mongorepo.geneanno.RepoUniID;
import com.novelbio.database.service.SpringFactory;

public class ManageNCBIUniID {
	private static final Logger logger = Logger.getLogger(ManageNCBIUniID.class);
	@Autowired
	private RepoNCBIID repoNCBIID;
	@Autowired
	private RepoUniID repoUniID;
	
	public ManageNCBIUniID() {
		repoNCBIID = (RepoNCBIID) SpringFactory.getFactory().getBean("repoNCBIID");
		repoUniID = (RepoUniID) SpringFactory.getFactory().getBean("repoUniID");
	}
	
	public AgeneUniID findByGeneIDAndAccID(int geneType, String geneUniID, String accID) {
		if (geneType == GeneID.IDTYPE_GENEID) {
			return repoNCBIID.findByGeneIDAndAccID(Long.parseLong(geneUniID), accID);
		} else if (geneType == GeneID.IDTYPE_UNIID) {
			return repoUniID.findByUniIDAndAccID(geneUniID, accID);
		}
		return null;
	}
	
	public AgeneUniID findByGeneUniIDAndAccIDAndTaxID(int geneType, String geneUniID, String accID, int taxID){
		if (geneType == GeneID.IDTYPE_GENEID) {
			return repoNCBIID.findByGeneIDAndAccIDAndTaxID(Long.parseLong(geneUniID), accID, taxID);
		} else if (geneType == GeneID.IDTYPE_UNIID) {
			return repoUniID.findByUniIDAndAccIDAndTaxID(geneUniID, accID, taxID);
		}
		return null;
	}
	
	public AgeneUniID findByAccIDAndTaxID(int geneType, String accID, int taxID) {
		if (geneType == GeneID.IDTYPE_GENEID) {
			return repoNCBIID.findByAccIDAndTaxID( accID, taxID);
		} else if (geneType == GeneID.IDTYPE_UNIID) {
			return repoUniID.findByAccIDAndTaxID(accID, taxID);
		}
		return null;
	}
	
	public List<AgeneUniID> findByGeneUniID(int geneType, String geneUniID) {
		List<? extends AgeneUniID> lsResult = new ArrayList<AgeneUniID>();
		if (geneType == GeneID.IDTYPE_GENEID) {
			lsResult = repoNCBIID.findByGeneID(Long.parseLong(geneUniID));
		} else if (geneType == GeneID.IDTYPE_UNIID) {
			lsResult = repoUniID.findByUniID(geneUniID);
		}
		return new ArrayList<AgeneUniID>(lsResult);
	}
	
	/**
	 * 如果存在则返回第一个找到的geneID
	 * 不存在就返回null
	 * @param geneID 输入geneID
	 * @param taxID 物种ID
	 * @return
	 */
	public AgeneUniID findByGeneUniIDAndTaxIdFirst(int geneType, String geneUniID, int taxID) {
		List<? extends AgeneUniID> lsResult = findByGeneUniIDAndTaxId(geneType, geneUniID, taxID);
		if (lsResult.size() > 0) {
			return lsResult.get(0);
		}
		return null;
	}
	
	public List<AgeneUniID> findByGeneUniIDAndTaxId(int geneType, String geneUniID, int taxID) {
		List<? extends AgeneUniID> lsResult = new ArrayList<AgeneUniID>();
		if (geneType == GeneID.IDTYPE_GENEID) {
			lsResult = repoNCBIID.findByGeneIDAndTaxId(Long.parseLong(geneUniID), taxID);
		} else if (geneType == GeneID.IDTYPE_UNIID) {
			lsResult = repoUniID.findByUniIDAndTaxId(geneUniID, taxID);
		}
		return new ArrayList<AgeneUniID>(lsResult);
	}

	public List<AgeneUniID> findByAccID(int geneType, String accID) {
		List<? extends AgeneUniID> lsResult = new ArrayList<AgeneUniID>();
		if (geneType == GeneID.IDTYPE_GENEID) {
			lsResult = repoNCBIID.findByAccID(accID);
		} else if (geneType == GeneID.IDTYPE_UNIID) {
			lsResult = repoUniID.findByAccID(accID);
		}
		return new ArrayList<AgeneUniID>(lsResult);
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
	public AgeneUniID queryGenUniID(int idType, String geneUniID, int taxID, String dbName) {
		if (dbName != null) 
			dbName = dbName.trim();
		else 
			dbName = "";
		
		List<AgeneUniID> lsAgeneUniIDs = findByGeneUniIDAndTaxId(idType, geneUniID, taxID);
		if ((lsAgeneUniIDs == null || lsAgeneUniIDs.size() < 1)) {
			return null;
		}
		for (AgeneUniID ageneUniID : lsAgeneUniIDs) {
			if (ageneUniID.getDataBaseInfo().getDbName().equals(dbName)) {
				return ageneUniID;
			}
		}
		return lsAgeneUniIDs.get(0);
		
	}
	
	/**
	 * ncbiid必须填写完全
	 * <b>没有accID，放弃升级</b>
	 * 没有该ID就插入，有该ID的话看如果需要override，如果override且数据库不一样，就覆盖升级
	 * @param nCBIID
	 * @param override
	 */
	public boolean updateNCBIUniID(AgeneUniID ncbiid, boolean override) {
		if (ncbiid.getAccID() == null) {
			logger.error("accID不存在，不能升级");
			return false;
		}
		if (ncbiid.getAccID().length() > 30) {
			logger.error("accID太长：" + ncbiid.getAccID() + "\t" + ncbiid.getDataBaseInfo().getDbName());
			return false;
		}
		if (ncbiid.getAccID().contains("GO:")) {
			logger.error("不能导入GO信息");
			return false;
		}
		AgeneUniID ageneUniID = findByGeneIDAndAccID(ncbiid.getGeneIDtype(), ncbiid.getGenUniID(), ncbiid.getAccID());
		if (ageneUniID == null) {
			if (ncbiid.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
				repoNCBIID.save((NCBIID)ncbiid);
			} else {
				repoUniID.save((UniProtID)ncbiid);
			}
		} else if (override) {
			if (ageneUniID.getTaxID() == ncbiid.getTaxID() && ageneUniID.getDataBaseInfo().equals(ncbiid.getDataBaseInfo())) {
				return true;
			}
			ageneUniID.setTaxID(ncbiid.getTaxID());
			ageneUniID.setDataBaseInfo(ncbiid.getDataBaseInfo());
			if (ncbiid.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
				repoNCBIID.save((NCBIID)ageneUniID);
			} else {
				repoUniID.save((UniProtID)ageneUniID);
			}
		}
		return true;
	}

}
