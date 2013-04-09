package com.novelbio.database.service.servinformation;

import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.mongorepo.information.RepoSoftwareInfo;
import com.novelbio.database.service.SpringFactory;
/**
 * 待修正，就是自动化的update工作，参照servGeneInfo
 * @author zong0jie
 *
 */
public class ManageSoftWareInfo {
	@Autowired
	RepoSoftwareInfo repoSoftwareInfo;
	public ManageSoftWareInfo() {
		repoSoftwareInfo = (RepoSoftwareInfo)SpringFactory.getFactory().getBean("repoSoftwareInfo");
	}
	public SoftWareInfo findSoftwareByName(String softName) {
		return repoSoftwareInfo.findBySoftName(softName);
	}
	public SoftWareInfo findSoftwareByName(SoftWare softWare) {
		return repoSoftwareInfo.findBySoftName(softWare.name());
	}
	/**
	 * 先查找有没有该项，没有就插入，有就升级
	 * @param softWareInfo
	 */
	public void update(SoftWareInfo softWareInfo) {
		SoftWareInfo softWareInfoS= repoSoftwareInfo.findBySoftName(softWareInfo.getName());
		if (softWareInfoS == null) {
			repoSoftwareInfo.save(softWareInfo);
		} else {
			if (!softWareInfoS.equalsDeep(softWareInfo)) {
				softWareInfo.setId(softWareInfoS.getId());
				repoSoftwareInfo.save(softWareInfo);
			}
		}
	}
}
