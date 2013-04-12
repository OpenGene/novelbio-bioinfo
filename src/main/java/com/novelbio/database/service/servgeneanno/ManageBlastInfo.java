package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.SepSign;
import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.mongorepo.geneanno.RepoBlastInfo;
import com.novelbio.database.service.SpringFactory;

public class ManageBlastInfo {
	static double[] lock = new double[0];
	static int num = 10000;
	static ArrayListMultimap<String, BlastInfo> mapQueryIDTaxIDQTaxIDS_2_BlastInfo = ArrayListMultimap.create();
	static ArrayListMultimap<String, BlastInfo> mapQueryIDTaxIDQ_2_BlastInfo = ArrayListMultimap.create();
	/** 这个做缓存 */
	static List<BlastInfo> lsBlastInfosUpdate = new ArrayList<BlastInfo>(num);
	
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
	public List<BlastInfo> queryBlastInfoLs(String queryID, int taxIDQ, int taxIDS) {
		List<BlastInfo> lsBlastInfosDB =  repoBlastInfo.findByQueryIDAndSubTaxID(queryID, taxIDQ, taxIDS);
		List<BlastInfo> lsBlastInfosCach = mapQueryIDTaxIDQTaxIDS_2_BlastInfo.get(queryID + SepSign.SEP_ID + taxIDQ + SepSign.SEP_ID + taxIDS);
		lsBlastInfosDB.addAll(lsBlastInfosCach);
		return lsBlastInfosDB;
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
		List<BlastInfo> lsBlastInfos = queryBlastInfoLs(queryID, taxIDQ, taxIDS);
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
		List<BlastInfo> lsBlastInfosDB = repoBlastInfo.findByQueryID(queryID, taxIDQ);
		List<BlastInfo> lsBlastInfosCach = mapQueryIDTaxIDQ_2_BlastInfo.get(queryID + SepSign.SEP_ID + taxIDQ);
		lsBlastInfosDB.addAll(lsBlastInfosCach);
		return lsBlastInfosDB;
	}
	
	public void save(BlastInfo blastInfo, boolean saveToDB) {
		synchronized (lock) {
			if (blastInfo == null) {
				return;
			}
			if (lsBlastInfosUpdate.size() < num-10 && !saveToDB) {
				lsBlastInfosUpdate.add(blastInfo);
			} else {
				repoBlastInfo.save(lsBlastInfosUpdate);
				mapQueryIDTaxIDQ_2_BlastInfo.clear();
				mapQueryIDTaxIDQTaxIDS_2_BlastInfo.clear();
				lsBlastInfosUpdate.clear();
			}
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
