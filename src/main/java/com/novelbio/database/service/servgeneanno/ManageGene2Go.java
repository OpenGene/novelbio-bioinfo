package com.novelbio.database.service.servgeneanno;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.mongorepo.geneanno.RepoGene2Go;
import com.novelbio.database.service.SpringFactory;
@Service
public class ManageGene2Go {
	private static Logger logger = Logger.getLogger(ManageGene2Go.class);
	@Autowired
	private RepoGene2Go repoGene2Go;
	
	public ManageGene2Go() {
		repoGene2Go = (RepoGene2Go)SpringFactory.getFactory().getBean("repoGene2Go");
	}
	
	public List<Gene2Go> queryLsGene2Go(int geneID) {
		return repoGene2Go.findByGeneID(geneID);
	}
	/** 推荐用这个，谁知道有没有可能一个geneID对应多个物种啊 */
	public List<Gene2Go> queryLsGene2Go(int geneID, int taxid) {
		return repoGene2Go.findByGeneIDAndTaxID(geneID, taxid);
	}
	public Gene2Go queryGene2Go(long geneID, int taxID,String GOID) {
		if (GOID == null) {
			return null;
		}
		return repoGene2Go.findByGeneIDAndTaxIDAndGOID(geneID, taxID, GOID);
	}
	
	/** 直接保存 */
	public void saveGene2Go(Gene2Go gene2Go) {
		repoGene2Go.save(gene2Go);
	}
	
	/**
	 * 输入geneUniID以及具体的内容，看是否需要升级
	 * 能插入就插入，已经有了就判端与数据库中是否一致，不一致就升级
	 * @param genUniID
	 * @param taxID 以该taxID为准
	 * @param gene2Go
	 */
	public boolean updateGene2Go(String genUniID, int taxID, AGene2Go gene2Go) {
		gene2Go.setTaxID(taxID);
		Gene2Go gene2GoOld = queryGene2Go(Long.parseLong(genUniID), taxID, gene2Go.getGOID());
		if (gene2GoOld != null) {
			if (gene2GoOld.addInfo(gene2Go)) {
				repoGene2Go.save(gene2GoOld);
			}
		} else {
			Gene2Go gene2GoNew = new Gene2Go();
			gene2GoNew.copyInfo(gene2Go);
			gene2GoNew.setGeneUniID(genUniID);
			gene2GoNew.setTaxID(taxID);
			if (gene2GoNew.getGOID() == null) {
				logger.error("出现未知GOID：" + gene2GoNew.getGOID());
				return false;
			}
			try {
				repoGene2Go.save(gene2GoNew);
			} catch (Exception e) {//出错原因可能是有两个连续一样的goID，然后连续插入的时候第一个goID还没来得及建索引，导致第二个直接就插入了然后出错
				logger.error(gene2Go.getGOID() + " " + gene2GoNew.getGeneUniId() + " " + gene2Go.getTaxID());
				return false;
			}
		}
		return true;
	}
	
}
