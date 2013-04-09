package com.novelbio.database.service.servgeneanno;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.mongorepo.geneanno.RepoBlastInfo;
import com.novelbio.database.service.SpringFactory;

public class ManageBlastInfo {
	@Autowired
	RepoBlastInfo repoBlastInfo;
	
	public ManageBlastInfo() {
		repoBlastInfo = (RepoBlastInfo)SpringFactory.getFactory().getBean("repoBlastInfo");
	}

	/**
	 * 查找指定物种的，全部BlastInfo
	 * @param queryID 待查找ID，一般是genUniID
	 * @param taxIDQ query物种ID
	 * @param taxIDS blast到的物种ID
	 * @param evalue 设定阈值 如果evalue <= -1或evalue >=5，则不起作用
	 * @return
	 */
	public List<BlastInfo> queryBlastInfoLs(String queryID, int taxIDQ, int taxIDS, double evalue) {
		return repoBlastInfo.findByQueryIDAndSubTaxID(queryID, taxIDQ, taxIDS);
	}
	
	/**
	 * 查找指定物种的，符合条件的第一个BlastInfo
	 * @param queryID 待查找ID，一般是genUniID
	 * @param taxIDQ query物种ID
	 * @param taxIDS blast到的物种ID
d * @param evalue 设定阈值 如果evalue <= -1或evalue >=5，则不起作用
	 * @return
	 */
	public BlastInfo queryBlastInfo(String queryID, int taxIDQ, int taxIDS, double evalue) {
		List<BlastInfo> lsBlastInfos = repoBlastInfo.findByQueryIDAndSubTaxID(queryID, taxIDQ, taxIDS);
		if (lsBlastInfos != null && lsBlastInfos.size() > 0) {
			Collections.sort(lsBlastInfos);//排序选择最小的一项
			BlastInfo blastInfo = lsBlastInfos.get(0);
			if (evalue < 5 && evalue > -1 && blastInfo.getEvalue() <= evalue) {
				return blastInfo;
			}
		}
		return null;
	}
	
	/**
	 * 查找符合条件的第一个BlastInfo
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
}
