package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.novelbio.database.domain.geneanno.BlastInfo;
import com.novelbio.database.mapper.geneanno.MapBlastInfo;
import com.novelbio.database.mapper.geneanno.MapGene2Go;
import com.novelbio.database.service.SpringFactory;
@Service
public class ServBlastInfo implements MapBlastInfo {
	@Inject
	MapBlastInfo mapBlastInfo;
	
	public ServBlastInfo() {
		mapBlastInfo = (MapBlastInfo)SpringFactory.getFactory().getBean("mapBlastInfo");
	}
	@Override
	public BlastInfo queryBlastInfo(BlastInfo qBlastInfo) {
		return mapBlastInfo.queryBlastInfo(qBlastInfo);
	}
	@Override
	public ArrayList<BlastInfo> queryLsBlastInfo(BlastInfo qBlastInfo) {
		return mapBlastInfo.queryLsBlastInfo(qBlastInfo);
	}
	@Override
	public void insertBlastInfo(BlastInfo blastInfo) {
		mapBlastInfo.insertBlastInfo(blastInfo);
	}
	@Override
	public void updateBlastInfo(BlastInfo blastInfo) {
		// TODO Auto-generated method stub
		mapBlastInfo.updateBlastInfo(blastInfo);
	}
	/**
	 * 查找符合条件的第一个BlastInfo
	 * @param queryID 待查找ID，一般是genUniID
	 * @param taxID 物种ID
	 * @param evalue 如果evalue <= -1或evalue >=5，则不起作用
	 * @return
	 */
	public BlastInfo queryBlastInfo(String queryID, int taxIDQ, int taxIDS, double evalue) {
		BlastInfo blastInfoTmp = new BlastInfo();
		blastInfoTmp.setEvalue(evalue);
		blastInfoTmp.setQueryID(queryID);
		if (taxIDQ > 0) {
			blastInfoTmp.setQueryTax(taxIDQ);
		}
		if (taxIDS > 0) {
			blastInfoTmp.setSubjectTax(taxIDS);
		}
		ArrayList<BlastInfo> lsBlastInfosTmp = queryLsBlastInfo(blastInfoTmp);
		if (lsBlastInfosTmp != null && lsBlastInfosTmp.size() > 0) 
		{
			Collections.sort(lsBlastInfosTmp);//排序选择最小的一项
			BlastInfo blastInfo = lsBlastInfosTmp.get(0);
			if (evalue < 5 && evalue > -1 && blastInfo.getEvalue() <= evalue) {
				return blastInfo;
			}
		}
		return null;
	}
	/**
	 * 给定blastInfo的信息，如果数据库中的本物种已经有了该结果，则比较evalue，用低evalue的覆盖高evalue的
	 * 如果没有，则插入
	 * @param blastInfo
	 */
	public void updateBlast(BlastInfo blastInfo) {
		BlastInfo blastInfo2 = queryBlastInfo(blastInfo.getQueryID(), blastInfo.getQueryTax(), blastInfo.getSubjectTax(), 1);
		if (blastInfo2 == null) {
			mapBlastInfo.insertBlastInfo(blastInfo);
		}
		//evalue小于等于blastinfo 或者evalue相同但是identity更大
		else if (blastInfo2.getEvalue() < blastInfo.getEvalue()
				|| (blastInfo2.getEvalue() == blastInfo.getEvalue()
				    && blastInfo2.getIdentities() > blastInfo.getIdentities()  )) {
			mapBlastInfo.updateBlastInfo(blastInfo);
		}
	}
}
