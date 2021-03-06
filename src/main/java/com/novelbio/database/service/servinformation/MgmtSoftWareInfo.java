package com.novelbio.database.service.servinformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.dao.information.RepoSoftwareInfo;
import com.novelbio.database.model.information.SoftWareInfo;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;
import com.novelbio.database.service.SpringFactoryBioinfo;
import com.novelbio.generalconf.PathDetailNBC;
/**
 * 待修正，就是自动化的update工作，参照servGeneInfo
 * @author zong0jie
 *
 */
public class MgmtSoftWareInfo {
	@Autowired
	RepoSoftwareInfo repoSoftwareInfo;
	static Map<String, SoftWareInfo> mapKey2Info;
	static MgmtSoftWareInfo manageSoftWareInfo;
	
	private MgmtSoftWareInfo() {
		fillMap();
		repoSoftwareInfo = (RepoSoftwareInfo)SpringFactoryBioinfo.getFactory().getBean("repoSoftwareInfo");
	}
	
	private void fillMap() {
		if (mapKey2Info != null) return;
		
		mapKey2Info = new HashMap<>();
		String fileSoft = PathDetailNBC.getSoftwareInfo();
		if (!FileOperate.isFileExistAndBigThanSize(fileSoft, 0)) {
			return;
		}
		updateInfo(fileSoft, false);
	}
	
	/**
	 * 将配置信息导入数据库
	 * @param txtFile 	 配置信息：第一行，item名称
	 */
	public void updateInfo(String txtFile, boolean updateToDB) {
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
			update(softWareInfo, updateToDB);
		}
	}

	public SoftWareInfo findSoftwareByName(String softName) {
		if (mapKey2Info != null && !mapKey2Info.isEmpty()) {
			SoftWareInfo softWareInfo = mapKey2Info.get(softName.toLowerCase());
			if (softWareInfo != null) {
				return softWareInfo;
			}
		}
		return repoSoftwareInfo.findBySoftName(softName);
	}
	public SoftWareInfo findSoftwareByName(SoftWare softWare) {
		return findSoftwareByName(softWare.toString());
	}
	/**
	 * 先查找有没有该项，没有就插入，有就升级
	 * @param softWareInfo
	 */
	public void update(SoftWareInfo softWareInfo, boolean updateToDB) {
		if (updateToDB) {
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
		mapKey2Info.put(softWareInfo.getName().toLowerCase(), softWareInfo);
	}
	
	public static MgmtSoftWareInfo getInstance() {
		if (manageSoftWareInfo == null) {
			manageSoftWareInfo = new MgmtSoftWareInfo();
		}
		return manageSoftWareInfo;
	}
}
