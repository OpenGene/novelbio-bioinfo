package com.novelbio.bioinfo.annotation.genanno;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.domain.modomim.MgmtMorbidMap;
import com.novelbio.database.domain.modomim.MgmtOMIMUnit;
import com.novelbio.database.model.geneanno.AGene2Go;
import com.novelbio.database.model.geneanno.GOtype;
import com.novelbio.database.model.omim.MIMInfo;
import com.novelbio.database.model.omim.MorbidMap;


public class AnnoOMIM extends AnnoAbs {
	MgmtMorbidMap mgmtMorbidMap = MgmtMorbidMap.getInstance();
	MgmtOMIMUnit mgmtOMIMUnit = MgmtOMIMUnit.getInstance();
	GOtype gOtype;
	
	public void setgOtype(GOtype gOtype) {
		this.gOtype = gOtype;
	}
	@Override
	public List<String[]> getInfo(int taxID, String accID) {
		//TODO
		GeneID copedID = new GeneID(accID, taxID);
		int geneId = Integer.parseInt(copedID.getGeneUniID());
		List<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String> lsResultTmp = new ArrayList<String>();
		lsResultTmp.add(accID + "");
		List<MorbidMap> liMorbidMap = mgmtMorbidMap.findInfByGeneId(geneId);	
		if (liMorbidMap.size() == 0) {
			fillLsResult(accID + "", lsResult, 8);	
			return lsResult;
		}
		for (MorbidMap morbidMap : liMorbidMap) {
			ArrayList<String> lsTmp = (ArrayList<String>) lsResultTmp.clone();
			lsTmp.add(morbidMap.getGeneMimId() + "");
			if (morbidMap.getGeneMimId() != 0) {
				MIMInfo mimInfo = mgmtOMIMUnit.findByMimId(morbidMap.getGeneMimId());
				lsTmp.add(mimInfo.getDesc());
				List<String> listRef = mimInfo.getListRef();
				lsTmp.add(listRef.get(0));
			}
			if (morbidMap.getPhenMimId() == 0) {
				lsTmp.add("");
				lsTmp.add("");
				lsTmp.add("");
			} else {
				lsTmp.add(morbidMap.getPhenMimId() + "");
			}
			if (morbidMap.getPhenMimId() != 0) {
				MIMInfo mimInfo = mgmtOMIMUnit.findByMimId(morbidMap.getPhenMimId());
				lsTmp.add(mimInfo.getDesc());
				List<String> listRef = mimInfo.getListRef();
				lsTmp.add(listRef.get(0));
			}
			List<String> listDis = morbidMap.getListDis();
			String disease = "";
			for (String disContent : listDis) {
				disease = disease.concat(disContent + "");
			}
			lsTmp.add(disease);
			lsResult.add(lsTmp.toArray(new String[0]));
		}
		if (lsResult.size() == 0) {
//			fillLsResult(geneID.getAccID(), lsResult, 5);   如果基因ID从数据库中查询的获取的话，使用该语句
			fillLsResult(accID + "", lsResult, 8);	//此句做测试使用
		}
		return lsResult;
	}
	private void fillLsResult(String accID, List<String[]> lsResult, int arrayLength) {
		String[] tmpResult = new String[arrayLength];
		tmpResult[0] = accID;
		for (int i = 1; i < tmpResult.length; i++) {                                  
			tmpResult[i] = "";
		}
		lsResult.add(tmpResult);
	}
	@Override
	protected List<String[]> getInfoBlast(int taxID, int subTaxID, double evalue, String accID) {
		// TODO Auto-generated method stub
		GeneID geneID = new GeneID(accID, taxID);
		List<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String> lsResultTmp = new ArrayList<String>();
		lsResultTmp.add(geneID.getSymbol());
		geneID.setBlastInfo(evalue, subTaxID);
		if (geneID.getGeneIDBlast() != null) {
			String accIDBlast = geneID.getBlastAccID(accID);
			GeneID geneIDBlast = new GeneID(accIDBlast, taxID);
			lsResultTmp.add(geneIDBlast.getLsBlastInfos().get(0).getEvalue() + "");
			lsResultTmp.add(geneIDBlast.getGeneIDBlast().getSymbol());
			int geneIdBlastint = Integer.parseInt(geneIDBlast.getGeneUniID());
			List<MorbidMap> liMorbidMapBlast = mgmtMorbidMap.findInfByGeneId(geneIdBlastint);	
			if (liMorbidMapBlast.size() == 0) {
				fillLsResult(accID + "", lsResult, 8);	
				return lsResult;
			}
			for (MorbidMap morbidMap : liMorbidMapBlast) {
				ArrayList<String> lsTmp = (ArrayList<String>) lsResultTmp.clone();
				lsTmp.add(morbidMap.getGeneMimId() + "");
				if (morbidMap.getGeneMimId() != 0) {
					MIMInfo mimInfo = mgmtOMIMUnit.findByMimId(morbidMap.getGeneMimId());
					lsTmp.add(mimInfo.getDesc());
					List<String> listRef = mimInfo.getListRef();
					lsTmp.add(listRef.get(0));
				}
				if (morbidMap.getPhenMimId() == 0) {
					lsTmp.add("");
					lsTmp.add("");
					lsTmp.add("");
				} else {
					lsTmp.add(morbidMap.getPhenMimId() + "");
				}
				if (morbidMap.getPhenMimId() != 0) {
					MIMInfo mimInfo = mgmtOMIMUnit.findByMimId(morbidMap.getPhenMimId());
					lsTmp.add(mimInfo.getDesc());
					List<String> listRef = mimInfo.getListRef();
					lsTmp.add(listRef.get(0));
				}
				List<String> listDis = morbidMap.getListDis();
				String disease = "";
				for (String disContent : listDis) {
					disease = disease.concat(disContent + "");
				}
				lsTmp.add(disease);
				lsResult.add(lsTmp.toArray(new String[0]));
			}
			if (lsResult.size() == 0) {
//				fillLsResult(geneID.getAccID(), lsResult, 5);   如果基因ID从数据库中查询的获取的话，使用该语句
				fillLsResult(accID + "", lsResult, 8);	//此句做测试使用
			}
			
		}

		return lsResult;
	}
	
	public String[] getTitle() {
		List<String> lsTitle = new ArrayList<String>();
		lsTitle.add("Symbol/AccID");
		lsTitle.add("GeneMIMID");
		lsTitle.add("GeneOMIMDesc");
		lsTitle.add("GeneOMIMRefence");
		lsTitle.add("PhenMIMID");
		lsTitle.add("PhenOMIMDesc");
		lsTitle.add("PhenOMIMRefence");
		lsTitle.add("Disease");
		return lsTitle.toArray(new String[0]);
	}
}
