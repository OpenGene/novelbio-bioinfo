package com.novelbio.database.service.servgeneanno;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.domain.geneanno.BlastFileInfo;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.mongorepo.geneanno.RepoBlastInfo;
import com.novelbio.database.service.SpringFactory;

public class ManageBlastInfo {
	static double[] lock = new double[0];
//	/** 缓存, key都为小写*/
//	static Map<String, Map<String, BlastInfo>> mapAccIDTaxID_2_mapAccID;
//	/** key为小写 */
//	static Map<String, Integer> mapAccID2TaxID;
	MongoTemplate mongoTemplate;
	@Autowired
	RepoBlastInfo repoBlastInfo;
	
	private ManageBlastInfo() {
		repoBlastInfo = (RepoBlastInfo)SpringFactory.getFactory().getBean("repoBlastInfo");
		mongoTemplate = (MongoTemplate)SpringFactory.getFactory().getBean("mongoTemplate");
	}
	
	/**
	 * @param taxIDQ
	 * @param taxIDS
	 * @param blastFile
	 * @param isBlastFormat 如果subjecdt是accID，具体的accID是否类似 blast的结果，如：dbj|AK240418.1|，那么获得AK240418，一般都是false
	 */
	public static void readBlastFile(int taxIDQ, int taxIDS, String blastFile) {
		ManageBlastInfo manageBlastInfo = ManageBlastInfo.getInstance();
		TxtReadandWrite txtRead = new TxtReadandWrite(blastFile);
		for (String content : txtRead.readlines()) {
			BlastInfo blastInfo = new BlastInfo(taxIDQ, taxIDS, content);
			BlastFileInfo blastFileInfo = new BlastFileInfo();
			blastFileInfo.setFileName(blastFile);
			blastFileInfo.setTmp(true);
			blastFileInfo.setQueryTaxID(taxIDQ);
			blastFileInfo.setSubjectTaxID(taxIDS);
			manageBlastInfo.saveBlastFile(blastFileInfo);
			manageBlastInfo.save(blastInfo);
		}
		txtRead.close();
	}
	
//	/**
//	 * 查找指定物种的，全部BlastInfo
//	 * @param queryID 待查找ID，一般是genUniID
//	 * @param taxIDQ query物种ID
//	 * @param taxIDS blast到的物种ID
//	 * @param evalue 设定阈值 如果evalue <= -1或evalue >=5，则不起作用
//	 * @return
//	 */
//	public List<BlastInfo> queryBlastInfoLs(String queryID, int taxIDQ, int taxIDS) {
//		return repoBlastInfo.findByQueryIDAndSubTaxID(queryID, taxIDQ, taxIDS);
//	}
//	
//	/**
//	 * 查找指定物种的，符合条件的第一个BlastInfo
//	 * @param queryID 待查找ID，一般是genUniID
//	 * @param taxIDQ query物种ID
//	 * @param taxIDS blast到的物种ID
//d * @param evalue 设定阈值 如果evalue <= -1或evalue >=5，则不起作用
//	 * @return
//	 */
//	public BlastInfo queryBlastInfo(String queryID, int taxIDQ, int taxIDS, double evalue) {
//		List<BlastInfo> lsBlastInfos = repoBlastInfo.findByQueryIDAndSubTaxID(queryID, taxIDQ, taxIDS);
//		if (lsBlastInfos != null && lsBlastInfos.size() > 0) {
//			Collections.sort(lsBlastInfos);//排序选择最小的一项
//			BlastInfo blastInfo = lsBlastInfos.get(0);
//			if (evalue < 5 && evalue > -1 && blastInfo.getEvalue() <= evalue) {
//				return blastInfo;
//			}
//		}
//		return null;
//	}
	
	/**
	 * 查找符合条件的List BlastInfo，已经去重复了
	 * @param queryID 待查找ID，一般是genUniID
	 * @param taxID 物种ID
	 * @return
	 */
	public List<BlastInfo> queryBlastInfoLs(String queryID, int taxIDQ) {
		return repoBlastInfo.findByQueryID(queryID, taxIDQ);
	}
	
	public void save(BlastInfo blastInfo) {		
		if (blastInfo != null) {
			repoBlastInfo.save(blastInfo);
		}
	}
	
	public void saveBlastFile(BlastFileInfo blastFileInfo) {		
		if (blastFileInfo != null) {
			mongoTemplate.save(blastFileInfo);
		}
	}
	
	public List<BlastFileInfo> getFileInfoAll() {
		return mongoTemplate.findAll(BlastFileInfo.class);
	}
	public List<BlastFileInfo> queryBlastFile(String fileName) {
		return mongoTemplate.find(new Query(Criteria.where("fileName").is(fileName)), BlastFileInfo.class);
	}
	
	/**
	 * @param fileName
	 * @param queryTaxID 小于0则不考虑
	 * @param subjectTaxID 小于0则不考虑
	 * @return
	 */
	public List<BlastFileInfo> queryBlastFile(String fileName, int queryTaxID, int subjectTaxID) {
		Criteria criteria = Criteria.where("fileName").is(fileName);
		if (queryTaxID > 0) {
			criteria = criteria.and("queryTaxID").is(queryTaxID);
		}
		if (subjectTaxID > 0) {
			criteria = criteria.and("subjectTaxID").is(subjectTaxID);
		}
		return mongoTemplate.find(new Query(criteria), BlastFileInfo.class);
	}
	
	/** 删除某个blastFile以及与之相关的blast信息 */
	public void removeBlastFile(BlastFileInfo blastFileInfo) {
		mongoTemplate.remove(new Query(Criteria.where("blastFileInfo").is(blastFileInfo)), BlastInfo.class);
		mongoTemplate.remove(blastFileInfo);
	}
	
	/**
	 * 给定blastInfo的信息，如果数据库中的本物种已经有了该结果，则比较evalue，用低evalue的覆盖高evalue的
	 * 如果没有，则插入
	 * @param blastInfo
	 */
	public void updateBlast(BlastInfo blastInfo) {
		BlastInfo blastInfoQ = repoBlastInfo.findByQueryIDAndSubID(blastInfo.getQueryID(), blastInfo.getQueryTax(), 
				blastInfo.getSubjectID(), blastInfo.getSubjectTax());
		if (blastInfoQ == null) {
			repoBlastInfo.save(blastInfo);
		} else {
			if (blastInfo.compareTo(blastInfoQ) == -1) {
				blastInfo.setId(blastInfoQ.getId());
				repoBlastInfo.save(blastInfo);
			}
		}
	}
	
	static class ManageHolder {
		static ManageBlastInfo manageBlastInfo = new ManageBlastInfo();
	}
	
	public static ManageBlastInfo getInstance() {
		return ManageHolder.manageBlastInfo;
	}
}
