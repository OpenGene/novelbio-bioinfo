package com.novelbio.database.model.modgo;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.AGene2Go;

public interface GOInfoInt {

	
	/**
	 * 将多个CopedID的GOInfoAbs放在一起，取并集去冗余
	 * @param lsGoInfo 多个GOInfoAbs的list
	 * @return
	 */
	public ArrayList<AGene2Go> getLsGen2Go(ArrayList<? extends AGene2Go> lsGoInfo, String GOType);
	/**
	 * 根据具体的GO_TYPE的标记，获得本GeneID的GO信息
	 * @param GOType 如果是GO_ALL，则返回全部的GO信息
	 * @return
	 */
	public ArrayList<AGene2Go> getLsGene2Go(String GOType);

}
