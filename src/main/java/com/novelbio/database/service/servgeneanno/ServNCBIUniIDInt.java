package com.novelbio.database.service.servgeneanno;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.database.domain.geneanno.AgeneUniID;

public interface ServNCBIUniIDInt {
	public AgeneUniID queryNCBIUniID(AgeneUniID QueryNCBIID);
	
	public List<? extends AgeneUniID> queryLsAgeneUniID(AgeneUniID QueryNCBIID);
	
	public void insertNCBIUniID(AgeneUniID ageneUniID);
	
	public void updateNCBIUniID(AgeneUniID ageneUniID);
	/**
	 * 首先用指定的数据库查找NCBIID表
	 * 如果找到了就返回找到的第一个的ncbiid对象
	 * 如果没找到，再去除dbinfo查找，如果还没找到，就返回Null
	 * @param geneID
	 * @param taxID
	 * @param dbInfo 为null表示不设置
	 * @return
	 */
	public AgeneUniID queryGenUniID(int idType, String geneUniID, int taxID, String dbInfo);
	
	/**
	 * <b>没有accID，放弃升级</b>
	 * 没有该ID就插入，有该ID的话看如果需要override，如果override且数据库不一样，就覆盖升级
	 * @param nCBIID
	 * @param override
	 */
	public boolean updateNCBIUniID(AgeneUniID ncbiid, boolean override);
	/**
	 * 如果存在则返回第一个找到的geneID
	 * 不存在就返回null
	 * @param geneID 输入geneID
	 * @param taxID 物种ID
	 * @return
	 */
	public AgeneUniID queryNCBIUniID(int idType, String geneID, int taxID);

}
