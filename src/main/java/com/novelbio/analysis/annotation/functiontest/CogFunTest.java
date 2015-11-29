package com.novelbio.analysis.annotation.functiontest;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.annotation.cog.COGanno;
import com.novelbio.analysis.annotation.cog.CogInfo;
import com.novelbio.analysis.annotation.cog.EnumCogType;
import com.novelbio.base.dataStructure.FisherTest;
import com.novelbio.database.domain.geneanno.GOtype;
import com.novelbio.database.model.modgeneid.GeneID;

public class CogFunTest extends FunctionTest {
	COGanno cogAnno;
	
	/** Cog不需要blast到其他物种，本身他就是blast到Cog数据库的 */
	@Deprecated
	public void setBlastInfo(double evalue, List<Integer> lsBlastTaxId) {}
	/** 返回一个空的list */
	public List<Integer> getBlastTaxID() {
		return new ArrayList<>();
	}
	public boolean isBlast() {
		return false;
	}
	
	public void setCogAnno(COGanno cogAnno) {
		this.cogAnno = cogAnno;
		cogAnno.initial();
	}
	
	@Override
	protected Map<String, GeneID2LsItem> readFromBGfile(Collection<String[]> lsTmpGeneID2LsItem) {
		Map<String, GeneID2LsItem> lsGeneID2LsItem = new LinkedHashMap<String, GeneID2LsItem>();
		for (String[] strings : lsTmpGeneID2LsItem) {
			GeneID2LsCog geneID2LsCog = new GeneID2LsCog();
			geneID2LsCog.setGeneUniID(strings[0]);
			String[] items = strings[1].split(",");
			for (String item : items) {
				geneID2LsCog.addItemID(item);
			}
			lsGeneID2LsItem.put(strings[0].toLowerCase(), geneID2LsCog);
		}
		return lsGeneID2LsItem;
	}

	@Override
	protected GeneID2LsItem convert2Item(GeneID geneID) {
		GeneID2LsCog geneId2LsCog = new GeneID2LsCog();
		geneId2LsCog.setGeneID(geneID, isBlast());
		CogInfo cogInfo = cogAnno.getCogInfoFromGeneUniId(geneID.getAccID(), geneID.getGeneUniID());
		geneId2LsCog.setCogInfo(cogInfo);
		if (!geneId2LsCog.isValidate()) {
			return null;
		}
		return geneId2LsCog;
	}
	
	/**
	 * 待修正
	 * 返回Gene2ItemPvalue
	 * @param Type
	 * @return
	 * 根据不同的StatisticTestGene2Item子类有不同的情况
	 */
	public ArrayList<StatisticTestGene2Item> getGene2ItemPvalue() {
		ArrayList<StatisticTestGene2Item> lsTestResult = new ArrayList<StatisticTestGene2Item>();
		Map<String, StatisticTestResult> mapItem2StatictResult = getMapItemID2StatisticsResult();
		Set<String> setAccID = new HashSet<String>();//用来去重复的
		for (GeneID2LsItem geneID2LsItem : lsTest) {
			for (GeneID geneID : mapGeneUniID2LsGeneID.get(geneID2LsItem.getGeneUniID().toLowerCase())) {
				if (setAccID.contains(geneID.getAccID())) continue;
				
				setAccID.add(geneID.getAccID());
				StatisticTestGene2Item statisticTestGene2Item = creatStatisticTestGene2Item();
				statisticTestGene2Item.setGeneID(((GeneID2LsCog)geneID2LsItem).convert2Abbr(cogAnno), geneID, isBlast());
				statisticTestGene2Item.setStatisticTestResult(mapItem2StatictResult);
				CogInfo cogInfo = cogAnno.getCogInfoFromGeneUniId(geneID.getAccID(), geneID.getGeneUniID());
				((StatisticTestGene2Cog)statisticTestGene2Item).setCogInfo(cogInfo);
				((StatisticTestGene2Cog)statisticTestGene2Item).setCogAnno(cogAnno);
				lsTestResult.add(statisticTestGene2Item);
			}
		}
		return lsTestResult;
	}
	
	/**
	 * booRun 新跑一次 返回最后的结果，ElimGO需要覆盖该方法 对结果排个序
	 * 返回最后的结果，ElimGO需要覆盖该方法
	 * @throws Exception 
	 * 没有就返回null<br>
	 * <b>结果已经排过序了</b>
	 */
	public ArrayList<StatisticTestResult> getTestResult() {
		if (statisticsTest == null) {
			statisticsTest = new FisherTest();
		}
		if (lsTestResult != null && lsTestResult.size() > 10) return lsTestResult;
		
		List<GeneID2LsItem> lstest = convertLsGeneId2Cog(getFilteredLs(lsTest));
		if (lstest.size() == 0) return null;
		
		List<GeneID2LsItem> lsbg = convertLsGeneId2Cog(getFilteredLs(mapBGGeneID2Items.values()));
		lsTestResult = getFisherResult(statisticsTest, lstest, lsbg, BGnum);
		for (StatisticTestResult statisticTestResult : lsTestResult) {
			statisticTestResult.setItemTerm(getItemTerm(statisticTestResult.getItemID()));
		}
		return lsTestResult;
	}
	
	protected GeneID2LsItem generateGeneID2LsItem() {
		return new GeneID2LsCog();
	}

	protected ArrayListMultimap<String, GeneID> getGo2GeneUniID() {
		ArrayListMultimap<String, GeneID> hashGo2LsGene = ArrayListMultimap.create();
		ArrayList<StatisticTestGene2Item> lsStatisticTestGene2Items = getGene2ItemPvalue();
		for (StatisticTestGene2Item statisticTestGene2Item : lsStatisticTestGene2Items) {
			GeneID2LsItem geneID2LsItem = convert2ItemFromBG(statisticTestGene2Item.getGeneID(), false);
			if (geneID2LsItem == null) {
				continue;
			}
			GeneID2LsCog geneID2LsCog = (GeneID2LsCog)geneID2LsItem;
			geneID2LsCog = geneID2LsCog.convert2Abbr(cogAnno);
			for (String goid : geneID2LsCog.getSetItemID()) {
				hashGo2LsGene.put(goid, statisticTestGene2Item.getGeneID());
			}
		}
		return hashGo2LsGene;
	}
	
	private List<GeneID2LsItem> convertLsGeneId2Cog(List<GeneID2LsItem> lsGeneID2LsItems) {
		List<GeneID2LsItem> lsGeneID2LsCogs = new ArrayList<>();
		for (GeneID2LsItem geneID2LsItem : lsGeneID2LsItems) {
			GeneID2LsCog geneID2LsCog = (GeneID2LsCog)geneID2LsItem;
			GeneID2LsCog geneID2LsCogConvert = geneID2LsCog.convert2Abbr(cogAnno);
			lsGeneID2LsCogs.add(geneID2LsCogConvert);
		}
		return lsGeneID2LsCogs;
	}
	
	public BufferedImage getImagePvalue() {
		List<StatisticTestResult> lsTestResults = getTestResult();
		if (lsTestResults == null || lsTestResults.size() == 0) {
			return null;
		}
		try {
			BufferedImage bfImageLog2Pic = GoPathBarPlot.drawLog2PvaluePicture(lsTestResults, 30, getTitle());
			return bfImageLog2Pic;
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}
	
	public BufferedImage getImageEnrichment() {
		List<StatisticTestResult> lsTestResults = getTestResult();
		if (lsTestResults == null || lsTestResults.size() == 0) {
			return null;
		}
		try {
			BufferedImage bfImageLog2Pic = GoPathBarPlot.drawEnrichmentPicture(lsTestResults, 30, getTitle());
			return bfImageLog2Pic;
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}
	
	@Override
	protected StatisticTestGene2Item creatStatisticTestGene2Item() {
		return new StatisticTestGene2Cog();
	}

	@Override
	protected String getItemTerm(String item) {
		if (item.contains(":")) {
			item = item.split(":")[1];
		}
		String[] cogInfo = cogAnno.queryAnnoFromCogAbbr(item);
		return cogInfo[0];
	}

	@Override
	public void setDetailType(GOtype gotype) { }

	@Override
	protected String getTitle() {
		return "COG-Analysis";
	}
	
	@Override
	protected TestType getTestType() {
		if (cogAnno.getCogType() == EnumCogType.COG) {
			return TestType.COG;
		} else {
			return TestType.KOG;
		}
		
	}


}
