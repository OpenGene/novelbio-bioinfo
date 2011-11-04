package com.novelbio.database.model.modgo;

import java.util.ArrayList;

import com.novelbio.database.domain.geneanno.AGene2Go;

public interface GOInfoInt {

	
	/**
	 * �����CopedID��GOInfoAbs����һ��ȡ����ȥ����
	 * @param lsGoInfo ���GOInfoAbs��list
	 * @return
	 */
	public ArrayList<AGene2Go> getLsGen2Go(ArrayList<? extends AGene2Go> lsGoInfo, String GOType);
	/**
	 * ���ݾ����GO_TYPE�ı�ǣ���ñ�GeneID��GO��Ϣ
	 * @param GOType �����GO_ALL���򷵻�ȫ����GO��Ϣ
	 * @return
	 */
	public ArrayList<AGene2Go> getLsGene2Go(String GOType);

}
