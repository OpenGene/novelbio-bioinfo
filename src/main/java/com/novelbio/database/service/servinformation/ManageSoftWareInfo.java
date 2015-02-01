package com.novelbio.database.service.servinformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.mongorepo.information.RepoSoftwareInfo;
import com.novelbio.database.service.SpringFactoryBioinfo;
import com.novelbio.generalConf.PathDetailNBC;
/**
 * 待修正，就是自动化的update工作，参照servGeneInfo
 * @author zong0jie
 *
 */
public class ManageSoftWareInfo {
//	@Autowired
//	RepoSoftwareInfo repoSoftwareInfo;
	static Map<String, SoftWareInfo> mapKey2Info;
	static ManageSoftWareInfo manageSoftWareInfo;
	
	private ManageSoftWareInfo() {
		fillMap();
//		repoSoftwareInfo = (RepoSoftwareInfo)SpringFactory.getFactory().getBean("repoSoftwareInfo");
	}
	
	private void fillMap() {
		if (mapKey2Info != null) return;
		
		mapKey2Info = new HashMap<>();
		String fileSoft = PathDetailNBC.getSoftwareInfo();
		if (!FileOperate.isFileExistAndBigThanSize(fileSoft, 0)) {
			return;
		}
		updateInfo(fileSoft);
	}
	
	/**
	 * 将配置信息导入数据库
	 * @param txtFile 	 配置信息：第一行，item名称
	 */
	public void updateInfo(String txtFile) {
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(txtFile, 0);
		String[] title = lsInfo.get(0);
		Map<String, Integer> mapName2Col = new HashMap<>();
		for (int i = 0; i < title.length; i++) {
			title[i] = title[i].replace("#", "");			
			mapName2Col.put(title[i].toLowerCase().trim(), i);
		}

		for (int i = 1; i < lsInfo.size(); i++) {
			SoftWareInfo softWareInfo = new SoftWareInfo();
			String[] info = lsInfo.get(i);

			Integer m = mapName2Col.get("softwarename");
			softWareInfo.setName(info[m]);
			
			m = mapName2Col.get("description");
			softWareInfo.setDescription(info[m]);
			

			
			m = mapName2Col.get("website");
			softWareInfo.setWebsite(info[m]);
			
			m = mapName2Col.get("installpath");
			softWareInfo.setInstallPath(info[m]);
			
			m = mapName2Col.get("path");
			softWareInfo.setPath(info[m]);
			
			m = mapName2Col.get("usage");
			softWareInfo.setUsage(info[m]);
			
			m = mapName2Col.get("version");
			softWareInfo.setVersion(info[m]);
			
			m = mapName2Col.get("ispath");
			softWareInfo.setInPath(info[m].trim().toLowerCase().equals("true"));
			//升级
			update(softWareInfo);
		}
	}

	public SoftWareInfo findSoftwareByName(String softName) {
		SoftWareInfo softWareInfo = mapKey2Info.get(softName.toLowerCase());
		if (softWareInfo != null) {
			return softWareInfo;
		}
		return null;
//		return repoSoftwareInfo.findBySoftName(softName);
	}
	public SoftWareInfo findSoftwareByName(SoftWare softWare) {
		return findSoftwareByName(softWare.toString());
	}
	/**
	 * 先查找有没有该项，没有就插入，有就升级
	 * @param softWareInfo
	 */
	public void update(SoftWareInfo softWareInfo) {
//		if (updateToDB) {
//			SoftWareInfo softWareInfoS= repoSoftwareInfo.findBySoftName(softWareInfo.getName());
//			if (softWareInfoS == null) {
//				repoSoftwareInfo.save(softWareInfo);
//			} else {
//				if (!softWareInfoS.equalsDeep(softWareInfo)) {
//					softWareInfo.setId(softWareInfoS.getId());
//					repoSoftwareInfo.save(softWareInfo);
//				}
//			}
//		}
		mapKey2Info.put(softWareInfo.getName().toLowerCase(), softWareInfo);
	}
	
	public static ManageSoftWareInfo getInstance() {
		if (manageSoftWareInfo == null) {
			manageSoftWareInfo = new ManageSoftWareInfo();
		}
		return manageSoftWareInfo;
	}
}
