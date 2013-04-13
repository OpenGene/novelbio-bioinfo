package com.novelbio.database.service.servgeneanno;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.UniGene2Go;
import com.novelbio.database.mongorepo.geneanno.RepoUniGene2Go;
import com.novelbio.database.service.SpringFactory;

public class ManageUniGene2Go {
	private static Logger logger = Logger.getLogger(ManageUniGene2Go.class);
	@Autowired
	private RepoUniGene2Go repoUniGene2Go;
	
	public ManageUniGene2Go() {
		repoUniGene2Go = (RepoUniGene2Go)SpringFactory.getFactory().getBean("repoUniGene2Go");
	}
	public List<UniGene2Go> queryLsGene2Go(String uniID) {
		return repoUniGene2Go.findByUniID(uniID);
	}
	
	/** 推荐用这个，谁知道有没有可能一个geneID对应多个物种啊 */
	public List<UniGene2Go> queryLsGene2Go(String uniID, int taxid) {
		return repoUniGene2Go.findByUniIDAndTaxID(uniID.toLowerCase(), taxid);
	}
	
	public UniGene2Go queryGene2Go(String uniID, int taxID,String GOID) {
		if (GOID == null) {
			return null;
		}
		return repoUniGene2Go.findByUniIDAndTaxIDAndGOID(uniID.toLowerCase(), taxID, GOID);
	}
	/** 直接保存 */
	public void saveGene2Go(UniGene2Go gene2Go) {
		repoUniGene2Go.save(gene2Go);
	}
	/**
	 * 
	 * 输入geneUniID以及具体的内容，看是否需要升级
	 * 能插入就插入，已经有了就判端与数据库中是否一致，不一致就升级
	 * @param genUniID
	 * @param taxID 以该taxID为准
	 * @param gene2Go
	 */
	public boolean updateGene2Go(String genUniID, int taxID, AGene2Go gene2Go) {
		gene2Go.setTaxID(taxID);
		UniGene2Go gene2GoOld = queryGene2Go(genUniID, taxID, gene2Go.getGOID());
		if (gene2GoOld != null) {
			if (gene2GoOld.addInfo(gene2Go)) {
				repoUniGene2Go.save(gene2GoOld);
			}
		} else {
			UniGene2Go gene2GoNew = new UniGene2Go();
			gene2GoNew.copyInfo(gene2Go);
			gene2GoNew.setGeneUniID(genUniID);
			gene2GoNew.setTaxID(taxID);
			if (gene2GoNew.getGOID() == null) {
				logger.error("出现未知GOID：" + gene2GoNew.getGOID());
				return false;
			}
			try {
				repoUniGene2Go.save(gene2GoNew);
			} catch (Exception e) {//出错原因可能是有两个连续一样的goID，然后连续插入的时候第一个goID还没来得及建索引，导致第二个直接就插入了然后出错
				logger.error(gene2Go.getGOID() + " " + gene2GoNew.getGeneUniId() + " " + gene2Go.getTaxID());
				return false;
			}
		}
		return true;
	}
	
}
