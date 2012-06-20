package com.novelbio.database.service.servinformation;

import java.util.ArrayList;

import javax.inject.Inject;

import com.novelbio.database.domain.geneanno.SpeciesFile;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.mapper.information.MapSoftWareInfo;
import com.novelbio.database.service.AbsGetSpring;
/**
 * �������������Զ�����update����������servGeneInfo
 * @author zong0jie
 *
 */
public class ServSoftWareInfo extends AbsGetSpring implements MapSoftWareInfo{
	@Inject
	MapSoftWareInfo mapSoftWareInfo;
	public ServSoftWareInfo() {
		mapSoftWareInfo = (MapSoftWareInfo)factory.getBean("mapSoftWareInfo");
	}
	@Override
	public SoftWareInfo querySoftWareInfo(SoftWareInfo softWareInfo) {
		return mapSoftWareInfo.querySoftWareInfo(softWareInfo);
	}

	@Override
	public ArrayList<SoftWareInfo> queryLsSoftWareInfo(SoftWareInfo softWareInfo) {
		return mapSoftWareInfo.queryLsSoftWareInfo(softWareInfo);
	}

	@Override
	public void insertSoftWareInfo(SoftWareInfo softWareInfo) {
		mapSoftWareInfo.insertSoftWareInfo(softWareInfo);
	}

	@Override
	public void updateSoftWareInfo(SoftWareInfo softWareInfo) {
		mapSoftWareInfo.updateSoftWareInfo(softWareInfo);
	}
	/**
	 * �Ȳ�����û�и��û�оͲ��룬�о�����
	 * @param softWareInfo
	 */
	public void update(SoftWareInfo softWareInfo) {
		ArrayList<SoftWareInfo> lsSoftWareInfos = queryLsSoftWareInfo(softWareInfo);
		if (lsSoftWareInfos == null || lsSoftWareInfos.size() == 0) {
			mapSoftWareInfo.insertSoftWareInfo(softWareInfo);
		}
		boolean updateFlag = true;
		for (SoftWareInfo softWareInfo2 : lsSoftWareInfos) {
			if (softWareInfo2.equalsDeep(softWareInfo)) {
				updateFlag = false;
			}
		}
		if (updateFlag) {
			mapSoftWareInfo.updateSoftWareInfo(softWareInfo);
		}
	}
}
