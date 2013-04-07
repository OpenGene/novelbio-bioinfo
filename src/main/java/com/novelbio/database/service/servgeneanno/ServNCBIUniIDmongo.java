package com.novelbio.database.service.servgeneanno;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.novelbio.database.DBAccIDSource;
import com.novelbio.database.domain.geneanno.AgeneUniID;
import com.novelbio.database.domain.geneanno.DBInfo;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.geneanno.UniProtID;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.mongorepo.geneanno.RepoNCBIID;
import com.novelbio.database.mongorepo.geneanno.RepoUniID;
import com.novelbio.database.service.SpringFactory;

public class ServNCBIUniIDmongo implements ServNCBIUniIDInt {

	private static final Logger logger = Logger.getLogger(ManageNCBIUniID.class);
	@Inject
	private RepoNCBIID repoNCBIID;
	@Inject
	private RepoUniID repoUniID;
	
	public ServNCBIUniIDmongo() {
		repoNCBIID = (RepoNCBIID)SpringFactory.getFactory().getBean("repoNCBIID");
		repoUniID = (RepoUniID)SpringFactory.getFactory().getBean("repoUniID");
	}
	
	/**
	 * @param ageneUniID 必须要含有accID<br>
	 * 或geneID和taxID。<br>
	 * 如果都有就一起去query
	 */
	public AgeneUniID queryNCBIUniID(AgeneUniID ageneUniID) {
		if (ageneUniID.isValidGenUniID()) {
			if (ageneUniID.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
				long geneID = Long.parseLong(ageneUniID.getGenUniID());
				if (ageneUniID.getTaxID() <= 0) {
					return repoNCBIID.findByGeneIDAndAccID(geneID, ageneUniID.getAccID());
				} else {
					return repoNCBIID.findByGeneIDAndAccIDAndTaxID(geneID, ageneUniID.getAccID(), ageneUniID.getTaxID());
				}
			} else {
				if (ageneUniID.getTaxID() <= 0) {
					return repoUniID.findByUniIDAndAccID(ageneUniID.getGenUniID(), ageneUniID.getAccID());
				} else {
					return repoUniID.findByUniIDAndAccIDAndTaxID(ageneUniID.getGenUniID(), ageneUniID.getAccID(), ageneUniID.getTaxID());
				}
			}
		} else {
			if (ageneUniID.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
				return repoNCBIID.findByAccIDAndTaxID(ageneUniID.getAccID(), ageneUniID.getTaxID());
			} else {
				return repoUniID.findByAccIDAndTaxID(ageneUniID.getAccID(), ageneUniID.getTaxID());
			}
		}
	}
	/**
	 * @param ageneUniID geneID或accID，两者必须含一个，但是不要输入两个都含有的
	 * 如果geneID和accID两个都有，就去查询{@link #queryNCBIUniID(AgeneUniID)}
	 * 或geneID和taxID两者必须有一个
	 * 内部不检查
	 */
	public List<? extends AgeneUniID> queryLsAgeneUniID(AgeneUniID ageneUniID) {
		if (ageneUniID.isValidGenUniID()) {
			if (ageneUniID.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
				long geneID = Long.parseLong(ageneUniID.getGenUniID());
				if (ageneUniID.getTaxID() <= 0) {
					return repoNCBIID.findByGeneID(geneID);
				} else {
					return repoNCBIID.findByGeneIDAndTaxId(geneID, ageneUniID.getTaxID());
				}
			} else {
				if (ageneUniID.getTaxID() <= 0) {
					return repoUniID.findByUniID(ageneUniID.getGenUniID());
				} else {
					return repoUniID.findByUniIDAndTaxId(ageneUniID.getGenUniID(), ageneUniID.getTaxID());
				}
			}
		} else {
			if (ageneUniID.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
				return repoNCBIID.findByAccID(ageneUniID.getAccID());
			} else {
				return repoUniID.findByAccID(ageneUniID.getAccID());
			}
		}
	}
	
	public void insertNCBIUniID(AgeneUniID ageneUniID) {
		if (ageneUniID.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
			repoNCBIID.save((NCBIID)ageneUniID);
		} else if (ageneUniID.getGeneIDtype() == GeneID.IDTYPE_UNIID) {
			repoUniID.save((UniProtID)ageneUniID);
		} else {
			logger.error("出现未知类型: " + ageneUniID.getAccID() + "\t" + ageneUniID.getGeneIDtype());
		}
	}
	
	/** 只能升级dbInfo一项 */
	public void updateNCBIUniID(AgeneUniID ageneUniID) {
		AgeneUniID ageneUniIDFind = queryNCBIUniID(ageneUniID);
		ageneUniIDFind.setDataBaseInfo(ageneUniID.getDataBaseInfo());
		if (ageneUniIDFind.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
			repoNCBIID.save((NCBIID)ageneUniID);
		} else if (ageneUniIDFind.getGeneIDtype() == GeneID.IDTYPE_UNIID) {
			repoUniID.save((UniProtID)ageneUniID);
		} else {
			logger.error("出现未知类型: " + ageneUniID.getAccID() + "\t" + ageneUniID.getGeneIDtype());
		}
	}
	
	/** 只能升级dbInfo一项 */
	public void saveNCBIUniID(AgeneUniID ageneUniID) {
		if (ageneUniID.getGeneIDtype() == GeneID.IDTYPE_GENEID) {
			repoNCBIID.save((NCBIID)ageneUniID);
		} else if (ageneUniID.getGeneIDtype() == GeneID.IDTYPE_UNIID) {
			repoUniID.save((UniProtID)ageneUniID);
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
		
		List<? extends AgeneUniID> lsAgeneUniIDs = null;
		if (idType == GeneID.IDTYPE_GENEID) {
			long geneID = Long.parseLong(geneUniID);
			if (taxID >= 0) {
				lsAgeneUniIDs = repoNCBIID.findByGeneID(geneID);
			} else {
				lsAgeneUniIDs = repoNCBIID.findByGeneIDAndTaxId(geneID, taxID);
			}
		} else {
			if (taxID >= 0) {
				lsAgeneUniIDs = repoUniID.findByUniID(geneUniID);
			} else {
				lsAgeneUniIDs = repoUniID.findByUniIDAndTaxId(geneUniID, taxID);
			}
		}
		
		for (AgeneUniID ageneUniID : lsAgeneUniIDs) {
			if (!dbInfo.equals("") && ageneUniID.getDataBaseInfo().getDbName().equalsIgnoreCase(dbInfo)) {
				return ageneUniID;
			}
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
		
		String geneID = ncbiid.getGenUniID();
		if (ncbiid.getTaxID() != 0 
				&& !ncbiid.getDataBaseInfo().getDbName().equals(DBAccIDSource.Symbol) 
				&& !ncbiid.getDataBaseInfo().getDbName().equals(DBAccIDSource.Synonyms)) {
			ncbiid.setGenUniID("0");
		}

		AgeneUniID ageneUniID = queryNCBIUniID(ncbiid);
		//query完了就把两个信息给重新设定回去
		ncbiid.setGenUniID(geneID);
		if (ageneUniID == null) {
			try {
				insertNCBIUniID(ncbiid);
				return true;
			} catch (Exception e) {
				logger.error("cannot insert into database: " + ncbiid.getAccID());
				return false;
			}
		} else {
			if (!geneID.equals(ageneUniID.getGenUniID()) && !ageneUniID.getDataBaseInfo().getDbOrg().equals("UniProt")) {
				logger.error("该AccID已经对应到了一个不同的GeneID上，因此没有升级该ID " + ncbiid.getAccID() + "geneid:" + ncbiid.getGenUniID());
				return false;
			}
			DBInfo db = ncbiid.getDataBaseInfo();
			if (override && !ageneUniID.getDataBaseInfo().equals(db)) {
				ageneUniID.setDataBaseInfo(db);
				saveNCBIUniID(ageneUniID);
				return true;
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
		List<? extends AgeneUniID> lsResult = queryLsAgeneUniID(ncbiid);
		if (lsResult == null || lsResult.size() == 0) {
			return null;
		} else {
			return lsResult.get(0);
		}
	}

}
