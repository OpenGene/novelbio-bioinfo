package com.novelbio.nbcgui.controltest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.annotation.functiontest.TopGO.GoAlgorithm;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.nbcgui.FoldeCreate;

/** 同时把BP、MF、CC三个类型都做了 */
@Component
@Scope("prototype")
public class CtrlGOall implements CtrlTestGOInt {
	private static final String pathSaveTo = "GO-Analysis_result";
	
	Map<GOtype, CtrlGO> mapGOtype2CtrlGO = new LinkedHashMap<GOtype, CtrlGO>();
	GoAlgorithm goAlgorithm;
	int taxID = 0;
	List<Integer> lsBlastTaxID = new ArrayList<Integer>();
	boolean isCluster = false;
	String saveParentPath = "";
	String savePathPrefix = "";
	
	@Override
	public void setTaxID(int taxID) {
		for (CtrlGO ctrlGO : mapGOtype2CtrlGO.values()) {
			ctrlGO.setTaxID(taxID);
		}
		this.taxID = taxID;
	}
	
	@Override
	public void setLsAccID2Value(ArrayList<String[]> lsAccID2Value) {
		for (CtrlGO ctrlGO : mapGOtype2CtrlGO.values()) {
			ctrlGO.setLsAccID2Value(lsAccID2Value);
		}
	}

	@Override
	public void setUpDown(double up, double down) {
		for (CtrlGO ctrlGO : mapGOtype2CtrlGO.values()) {
			ctrlGO.setUpDown(up, down);
		}
	}

	@Override
	public void setBlastInfo(double blastevalue, List<Integer> lsBlastTaxID) {
		for (CtrlGO ctrlGO : mapGOtype2CtrlGO.values()) {
			ctrlGO.setBlastInfo(blastevalue, lsBlastTaxID);
		}
		this.lsBlastTaxID = lsBlastTaxID;
	}
	
	/** 只能是最初的一列基因那个BG文件，不能是gene_P_Item那种文件 */
	@Override
	public void setLsBG(String fileName) {
		for (CtrlGO ctrlGO : mapGOtype2CtrlGO.values()) {
			ctrlGO.setLsBG(fileName);
		}
	}

	@Override
	public void setIsCluster(boolean isCluster) {
		this.isCluster = isCluster;
		for (CtrlGO ctrlGO : mapGOtype2CtrlGO.values()) {
			ctrlGO.setIsCluster(isCluster);
		}
	}
	
	@Override
	public boolean isCluster() {
		return isCluster;
	}
	
	@Override
	public void saveExcel(String excelPath) {
		String saveExcelPrefix = FoldeCreate.createAndInFold(excelPath, pathSaveTo);
		if (saveExcelPrefix.endsWith("\\") || saveExcelPrefix.endsWith("/")) {
			saveParentPath = saveExcelPrefix;
		} else {
			saveParentPath = FileOperate.getParentPathName(saveExcelPrefix);
			savePathPrefix = FileOperate.getFileName(saveExcelPrefix);
		}
		
		for (CtrlGO ctrlGO : mapGOtype2CtrlGO.values()) {
			String saveName;
			if (saveExcelPrefix.endsWith("\\") || saveExcelPrefix.endsWith("/")) {
				saveName = saveExcelPrefix + ctrlGO.getResultBaseTitle() + ".xls";
			} else {
				saveName = FileOperate.changeFilePrefix(saveExcelPrefix, ctrlGO.getResultBaseTitle() + "_", "xls");
			}
			ctrlGO.saveExcel(saveName);
		}
	}
	
	/** 获得保存到的文件夹路径 */
	@Override
	public String getSaveParentPath() {
		return saveParentPath;
	}
	
	/** 获得保存到文件夹的前缀，譬如保存到/home/zong0jie/stage10，那么前缀就是stage10 */
	@Override
	public String getSavePrefix() {
		return savePathPrefix;
	}
	
	@Override
	public void setGoAlgorithm(GoAlgorithm goAlgorithm) {
		CtrlGO ctrlGO = new CtrlGO();
		ctrlGO.setGoAlgorithm(goAlgorithm);
		ctrlGO.setGOType(GOtype.BP);
		mapGOtype2CtrlGO.put(GOtype.BP, ctrlGO);
		
		ctrlGO = new CtrlGO();
		ctrlGO.setGoAlgorithm(goAlgorithm);
		ctrlGO.setGOType(GOtype.MF);
		mapGOtype2CtrlGO.put(GOtype.MF, ctrlGO);
		
		ctrlGO = new CtrlGO();
		ctrlGO.setGoAlgorithm(goAlgorithm);
		ctrlGO.setGOType(GOtype.CC);
		mapGOtype2CtrlGO.put(GOtype.CC, ctrlGO);

		this.goAlgorithm = goAlgorithm;
	}

	@Override
	public GoAlgorithm getGoAlgorithm() {
		return goAlgorithm;
	}

	@Override
	public void setGOlevel(int levelNum) {
		for (CtrlGO ctrlGO : mapGOtype2CtrlGO.values()) {
			ctrlGO.setGOlevel(levelNum);
		}
	}

	@Override
	public void clearParam() {
		for (CtrlGO ctrlGO : mapGOtype2CtrlGO.values()) {
			ctrlGO.clearParam();
		}
	}

	@Override
	public void run() {
		List<Thread> lsThreads = new ArrayList<Thread>();
		for (CtrlGO ctrlGO : mapGOtype2CtrlGO.values()) {
			Thread thread = new Thread(ctrlGO);
			thread.start();
			lsThreads.add(thread);
		}
		for (Thread thread : lsThreads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
//		while (true) {
//			boolean isfinish = true;
//			for (CtrlGO ctrlGO : mapGOtype2CtrlGO.values()) {
//				if (ctrlGO.isRunning()) {
//					isfinish = false;
//					try { Thread.sleep(1000); } catch (InterruptedException e) { }
//					break;
//				}
//			}
//			if (isfinish) {
//				break;
//			}
//		}
	}

	@Override
	public int getTaxID() {
		return taxID;
	}

	@Override
	public List<Integer> getBlastTaxID() {
		return lsBlastTaxID;
	}

	@Override
	public Map<GOtype, CtrlGO> getMapResult_Prefix2FunTest() {
		return mapGOtype2CtrlGO;
	}

	@Override
	public String getResultBaseTitle() {
		return "GO-Analysis";
	}
}
