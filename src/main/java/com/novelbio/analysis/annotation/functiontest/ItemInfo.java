package com.novelbio.analysis.annotation.functiontest;
/**
 * 专门用与FisherTest类中获得Item的情况
 * @author zong0jie
 *
 */
public interface ItemInfo {
	/**
	 * Fisher检验时候用的东西
	 */
	public String[] getItemName(String ItemID);
}
