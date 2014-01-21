package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.base.dataOperate.TxtReadandWrite;
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
	
	private ManageNCBIUniID() {
		repoNCBIID = (RepoNCBIID) SpringFactory.getFactory().getBean("repoNCBIID");
		repoUniID = (RepoUniID) SpringFactory.getFactory().getBean("repoUniID");
	}
	
	/**
	 * 删除该ageneUnID
	 * 首先判定ID，没有的话再去查找
	 * @param ageneUniID
	 */
	public void delete(AgeneUniID ageneUniID) {
		if (ageneUniID instanceof NCBIID) {
			if (ageneUniID.getId() != null && !ageneUniID.getId().equals("")) {
				repoNCBIID.delete(ageneUniID.getId());
			} else {
				ageneUniID = findByGeneUniIDAndAccIDAndTaxID(GeneID.IDTYPE_GENEID, ageneUniID.getGenUniID(), ageneUniID.getAccID(), ageneUniID.getTaxID());
				if (ageneUniID != null) {
					repoNCBIID.delete(ageneUniID.getId());
				}
			}
		} else {
			if (ageneUniID.getId() != null && !ageneUniID.getId().equals("")) {
				repoUniID.delete(ageneUniID.getId());
			} else {
				ageneUniID = findByGeneUniIDAndAccIDAndTaxID(GeneID.IDTYPE_UNIID, ageneUniID.getGenUniID(), ageneUniID.getAccID(), ageneUniID.getTaxID());
				if (ageneUniID != null) {
					repoUniID.delete(ageneUniID.getId());
				}
			}
		}
	}
	
	/**
	 * @param geneType
	 * @param geneUniID 会查找不去点和去点两种情况
	 * @param accID
	 * @param taxID
	 * @return
	 */
	public AgeneUniID findByGeneUniIDAndAccIDAndTaxID(int geneType, String geneUniID, String accID, int taxID) {
		accID = accID.toLowerCase();
		AgeneUniID ageneUniID = findByGeneUniIDAndAccIDAndTaxIDUnit(geneType, geneUniID, accID, taxID);
		if (ageneUniID == null && accID.contains(".")) {
			String accIDnoDot = GeneID.removeDot(accID);
			if (accIDnoDot.equals(accID) ) {
				return null;
			}
			ageneUniID = findByGeneUniIDAndAccIDAndTaxIDUnit(geneType, geneUniID, accIDnoDot, taxID);
		}
		return ageneUniID;
	}
	
	/**
	 * @param geneType
	 * @param geneUniID
	 * @param accID
	 * @param taxID 小于等于0表示不考虑
	 * @return
	 */
	private AgeneUniID findByGeneUniIDAndAccIDAndTaxIDUnit(int geneType, String geneUniID, String accID, int taxID) {
		if (geneType == GeneID.IDTYPE_GENEID) {
			if (taxID > 0) {
				return repoNCBIID.findByGeneIDAndAccIDAndTaxID(Long.parseLong(geneUniID), accID, taxID);
			} else {
				List<NCBIID> lsResult = repoNCBIID.findByGeneIDAndAccID(Long.parseLong(geneUniID), accID);
				if (lsResult.size() > 0) {
					return lsResult.get(0);
				}
			}
		} else if (geneType == GeneID.IDTYPE_UNIID) {
			if (taxID > 0) {
				return repoUniID.findByUniIDAndAccIDAndTaxID(geneUniID.toLowerCase(), accID, taxID);
			} else {
				List<UniProtID> lsResult = repoUniID.findByUniIDAndAccID(geneUniID.toLowerCase(), accID);
				if (lsResult.size() > 0) {
					return lsResult.get(0);
				}
			}
		}
		return null;
	}
	
	public ArrayList<AgeneUniID> findByAccID(int geneType, String accID, int taxID) {
		accID =  accID.toLowerCase();
		ArrayList<AgeneUniID> lsResult = findByAccIDunit(geneType, accID, taxID);
		if (lsResult.isEmpty() && accID.contains(".")) {
			String accIDnoDot = GeneID.removeDot(accID);
			if (accIDnoDot.equals(accID)) {
				return new ArrayList<AgeneUniID>(lsResult);
			}
			lsResult = findByAccIDunit(geneType, accIDnoDot, taxID);
		}
		
		return lsResult;
	}
	
	private ArrayList<AgeneUniID> findByAccIDunit(int geneType, String accID, int taxID) {
		List<? extends AgeneUniID> lsResult = new ArrayList<AgeneUniID>();
		if (geneType == GeneID.IDTYPE_GENEID) {
			if (taxID > 0) {
				lsResult = repoNCBIID.findByAccIDAndTaxID(accID, taxID);
			} else {
				lsResult = repoNCBIID.findByAccID(accID);
			}
		} else if (geneType == GeneID.IDTYPE_UNIID) {
			if (taxID > 0) {
				lsResult = repoUniID.findByAccIDAndTaxID(accID, taxID);
			} else {
				lsResult = repoUniID.findByAccID(accID);
			}
		}
		return new ArrayList<AgeneUniID>(lsResult);
	}
	
	/**
	 * 如果存在则返回第一个找到的geneID
	 * 不存在就返回null
	 * @param geneID 输入geneID
	 * @param taxID 物种ID，小于等于0表示不设置taxID
	 * @return
	 */
	public AgeneUniID findByGeneUniIDAndTaxIdFirst(int geneType, String geneUniID, int taxID) {
		List<? extends AgeneUniID> lsResult = findByGeneUniID(geneType, geneUniID, taxID);
		if (lsResult.size() > 0) {
			return lsResult.get(0);
		}
		return null;
	}
	
	/**
	 * 不存在就返回空的list
	 * @param geneID 输入geneID
	 * @param taxID 物种ID，小于等于0表示不设置taxID
	 * @return
	 */
	public List<AgeneUniID> findByGeneUniID(int geneType, String geneUniID, int taxID) {
		List<? extends AgeneUniID> lsResult = new ArrayList<AgeneUniID>();
		if (geneType == GeneID.IDTYPE_GENEID) {
			if (taxID > 0) {
				lsResult = repoNCBIID.findByGeneIDAndTaxId(Long.parseLong(geneUniID), taxID);
			} else {
				lsResult = repoNCBIID.findByGeneID(Long.parseLong(geneUniID));
			}
		} else if (geneType == GeneID.IDTYPE_UNIID) {
			if (taxID > 0) {
				lsResult = repoUniID.findByUniIDAndTaxId(geneUniID.toLowerCase(), taxID);
			} else {
				lsResult = repoUniID.findByUniID(geneUniID.toLowerCase());
			}
		}
		return new ArrayList<AgeneUniID>(lsResult);
	}

	/**
	 * 首先用指定的数据库查找NCBIID表
	 * 如果找到了就返回找到的第一个的ncbiid对象
	 * 如果没找到，再去除dbinfo查找，如果还没找到，就返回Null
	 * @param geneID
	 * @param taxID 小于等于0表示不设置
	 * @param dbInfo 为null表示不设置
	 * @return
	 */
	public AgeneUniID queryGenUniID(int idType, String geneUniID, int taxID, String dbName) {
		if (dbName != null) 
			dbName = dbName.trim();
		else 
			dbName = "";
		
		List<AgeneUniID> lsAgeneUniIDs = findByGeneUniID(idType, geneUniID, taxID);
		if ((lsAgeneUniIDs == null || lsAgeneUniIDs.size() < 1)) {
			return null;
		}
		for (AgeneUniID ageneUniID : lsAgeneUniIDs) {
			if (ageneUniID.getDataBaseInfo().getDbName().equalsIgnoreCase(dbName)) {
				return ageneUniID;
			}
		}
		return lsAgeneUniIDs.get(0);
		
	}
	
	public void saveNCBIUniID(AgeneUniID ncbiid) {
		if (ncbiid.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
			repoNCBIID.save((NCBIID)ncbiid);
		} else {
			repoUniID.save((UniProtID)ncbiid);
		}
	}
	/**
	 * ncbiid必须填写完全
	 * <b>没有accID，放弃升级</b>
	 * 没有该ID就插入，有该ID的话看如果需要override，如果override且数据库不一样，就覆盖升级
	 * @param nCBIID
	 * @param override
	 */
	public boolean updateNCBIUniID(AgeneUniID ncbiid, boolean override) {
		if (ncbiid.getAccID() == null || ncbiid.getAccID().equals("")) {
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
		try {
			AgeneUniID ageneUniID = findByGeneUniIDAndAccIDAndTaxID(ncbiid.getGeneIDtype(), ncbiid.getGenUniID(), ncbiid.getAccID(), ncbiid.getTaxID());
			try {
				update(ageneUniID, ncbiid, override);
			} catch (Exception e) {
				update(ageneUniID, ncbiid, override);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private boolean update(AgeneUniID geneUniexist, AgeneUniID ncbiid, boolean override) {
		if (geneUniexist == null) {
			saveNCBIUniID(ncbiid);
			return true;
		}
		
		boolean isSameAccID = (ncbiid.getAccID() == null || geneUniexist.getSetAccID().contains(ncbiid.getAccID().toLowerCase()));
		boolean isDBsame = geneUniexist.getDataBaseInfo().equals(ncbiid.getDataBaseInfo());
		 if (isSameAccID && isDBsame) {
			 return true;
		 }
		 geneUniexist.addAccID(ncbiid.getAccID());
		 if (!isSameAccID) {
			if (override) {
				geneUniexist.setDataBaseInfo(ncbiid.getDataBaseInfo());
			}
			saveNCBIUniID(geneUniexist);
		 } else {
			 if (override) {
				 geneUniexist.setDataBaseInfo(ncbiid.getDataBaseInfo());
				 saveNCBIUniID(geneUniexist);
			 }
		}
		 return true;
	}

	static class ManageNCBIUniIDholder {
		static ManageNCBIUniID manageNCBIUniID = new ManageNCBIUniID();
	}
	
	public static ManageNCBIUniID getInstance() {
		return ManageNCBIUniIDholder.manageNCBIUniID;
	}
}
