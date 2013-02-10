package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/** 用于查找多样本SnpSomatic的service类
 * 找出somatic snp，并且这些样本的snp在同一个基因内部
 * 首先要把SNPGATKcope对象设定并运行好，然后再执行这个
 *  
 *  */
public class SnpSomaticFinder {
	private static Logger logger = Logger.getLogger(SnpSomaticFinder.class);
	
	public static void main(String[] args) {
		String parentFile = "/media/winF/NBC/Project/Project_HXW/20121018/mapping/";
		
		SnpSomaticFilter snpSomaticFilter = new SnpSomaticFilter();
		
		snpSomaticFilter.addSnpFromNBCfile("5A", parentFile + "5A_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
		snpSomaticFilter.addSnpFromNBCfile("5B", parentFile + "5B_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
		
		snpSomaticFilter.addSnpFromNBCfile("7A", parentFile + "7A_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
		snpSomaticFilter.addSnpFromNBCfile("7B", parentFile + "7B_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
		
		snpSomaticFilter.addSnpFromNBCfile("10A", parentFile + "10A_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
		snpSomaticFilter.addSnpFromNBCfile("10B", parentFile + "10B_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
		
		
		snpSomaticFilter.addSampileupFile("5A", parentFile + "5A_sorted_realign_removeDuplicate_pileup.gz");
		snpSomaticFilter.addSampileupFile("5B", parentFile + "5B_sorted_realign_removeDuplicate_pileup.gz");
		
		snpSomaticFilter.addSampileupFile("7A", parentFile + "7A_sorted_realign_removeDuplicate_pileup.gz");
		snpSomaticFilter.addSampileupFile("7B", parentFile + "7B_sorted_realign_removeDuplicate_pileup.gz");

		
		snpSomaticFilter.addSampileupFile("10A", parentFile + "10A_sorted_realign_removeDuplicate_pileup.gz");
		snpSomaticFilter.addSampileupFile("10B", parentFile + "10B_sorted_realign_removeDuplicate_pileup.gz");
		
		snpSomaticFilter.setSnp_HetoMore_Contain_SnpProp_Min(0.2);
		
		SnpGroupFilterInfo sampleDetailTreat = new SnpGroupFilterInfo();
		sampleDetailTreat.addSampleName("5A");
		sampleDetailTreat.addSampleName("7A");
		sampleDetailTreat.addSampleName("10A");
		
		sampleDetailTreat.setSampleRefHomoNum(1, 3);
		sampleDetailTreat.setSampleSnpIndelHetoNum(0, 0);
		sampleDetailTreat.setSampleSnpIndelHomoNum(0, 0);
		
		SnpGroupFilterInfo sampleDetailCol = new SnpGroupFilterInfo();
		sampleDetailCol.addSampleName("5B");
		sampleDetailCol.addSampleName("7B");
		sampleDetailCol.addSampleName("10B");

		sampleDetailCol.setSampleRefHomoNum(0, 0);
		sampleDetailCol.setSampleSnpIndelHetoMoreNum(1, 3);
		sampleDetailCol.setSampleSnpIndelHetoLessNum(0, 0);
		
		snpSomaticFilter.addFilterGroup(sampleDetailTreat);
		snpSomaticFilter.addFilterGroup(sampleDetailCol);
		
		snpSomaticFilter.readSnpDetailFromFile();
		snpSomaticFilter.writeToFile("/media/winF/NBC/Project/Project_HXW/20121018/result/ColvsTreat.xls");
		
		snpSomaticFilter.filterSnp();
		snpSomaticFilter.writeToFile("/media/winF/NBC/Project/Project_HXW/20121018/result/ColvsTreat_Filter.xls");
		
		snpSomaticFilter.getLsFilteredSnp();
		GeneFilter geneFilter = new GeneFilter();
		geneFilter.setGffChrAbs(new GffChrAbs(9606));
		geneFilter.setSnpLevel(SnpGroupFilterInfo.Heto);
		geneFilter.setSampleFilteredNum(2);
		geneFilter.addSampleName("5B");
		geneFilter.addSampleName("7B");
		geneFilter.addSampleName("10B");
		
		String outFile = "/media/winF/NBC/Project/Project_HXW/20121018/result/ColvsTreat_Filter_filterGene.xls";
		
		ArrayList<RefSiteSnpIndel> lsRefSiteSnpIndels = geneFilter.filterSnpInGene();
		RefSiteSnpIndel.writeToFile(outFile, lsRefSiteSnpIndels, geneFilter.getSetSampleName(), false);
		
	}
	
	SnpSomaticFilter snpSomaticFilter;
	GeneFilter geneFilter = new GeneFilter();
	ArrayList<RefSiteSnpIndel> lsRefSiteSnpIndelsResult;
	Map<String, SnpGroupFilterInfo> mapGroupName2Group;
	
	boolean filterGene = false;
	
	/**
	 * 添加snp文件，必须是NBC的snp格式<br>
	 * 第一列ChrID<br>
	 * 第二列location<br>
	 * 第三列refSeq<br>
	 * 第四列thisSeq<br>
	 * @param lsSnpFile2Prefix<br>
	 * ls-String<br>
	 *  0: SnpFile<br>
	 * 1: Prefix
	 */
	public void setSnpFile(List<String[]> lsSnpFile2Prefix) {
		for (String[] strings : lsSnpFile2Prefix) {
			snpSomaticFilter.addSnpFromNBCfile(strings[1], strings[0]);
		}
	}
	
	/**
	 * 添加snp的PileUp文件
	 * @param lsSnpFile2Prefix
	 * String 0: SnpFile
	 * 1: Prefix
	 */
	public void setSnpPileUpFile(List<String[]> lsSnpPileFile2Prefix) {
		for (String[] strings : lsSnpPileFile2Prefix) {
			snpSomaticFilter.addSampileupFile(strings[1], strings[0]);
		}
	}
	
	/**
	 * 设定group和sample名字之间的关系
	 * @param lsGroup2Sample
	 */
	public void setGroup2Sample(List<String[]> lsGroup2Sample) {
		mapGroupName2Group = new HashMap<String, SnpGroupFilterInfo>();
		ArrayListMultimap<String, String> mapGroup2LsSamples = ArrayListMultimap.create();
		for (String[] strings : lsGroup2Sample) {
			mapGroup2LsSamples.put(strings[0], strings[1]);
		}
		for (String group : mapGroup2LsSamples.keySet()) {
			SnpGroupFilterInfo snpGroup = new SnpGroupFilterInfo();
			List<String> lsSample = mapGroup2LsSamples.get(group);
			for (String sampleName : lsSample) {
				snpGroup.addSampleName(sampleName);
			}
			mapGroupName2Group.put(group, snpGroup);
		}
	}
	
	/**
	 * 设定每个组的信息，譬如snp level，数量等
	 * 输入的list-string[]
	 * 0: groupName
	 * 1: SnpLevel
	 * 2: 有该级别及以上的Snp的最少数量
	 * 3: 有该级别及以上的Snp的最大数量
	 */
	public void setGroupInfo(List<String[]> lsGroupInfo) {
		for (String[] strings : lsGroupInfo) {
			SnpGroupFilterInfo snpGroupFilterInfo = mapGroupName2Group.get(strings[0]);
			try {
				snpGroupFilterInfo.setSampleSnpRegionUp(SnpLevel.getSnpLevel(strings[1]), Integer.parseInt(strings[2]), Integer.parseInt(strings[3]));
			} catch (Exception e) {
				logger.error("SnpGroupFilterInfo 设定时出错");
			}
		}
	}
	
	/**
	 * 设定GeneFilter的信息
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		geneFilter.setGffChrAbs(gffChrAbs);
	}
	/**
	 * 添加要检验的样本名
	 * 如果不输入样本名，或输入空的样本名，则用全体样本名去做分析
	 * @param colTreatName
	 */
	public void setTreatName(Collection<String> colTreatName) {
		geneFilter.setSampleName(colTreatName);
	}
	/** 其中有几个样本出现这种情况就算通过 */
	public void setTreatFilteredNum(int treatFilteredNum) {
		geneFilter.setSampleFilteredNum(treatFilteredNum);
	}
	
	public void setSnpLevel(SnpLevel snpLevel) {
		geneFilter.setSnpLevel(snpLevel);
	}

	/**
	 * 过滤差异的snp
	 */
	public void readPileupFile() {
		snpSomaticFilter.readSnpDetailFromFile();
	}
	
	 public void filterSnp() {
		 snpSomaticFilter.clearGroupFilterInfo();
		for (String groupName : mapGroupName2Group.keySet()) {
			SnpGroupFilterInfo snpGroupFilterInfo = mapGroupName2Group.get(groupName);
			snpSomaticFilter.addFilterGroup(snpGroupFilterInfo);
		}
		snpSomaticFilter.filterSnp();
		lsRefSiteSnpIndelsResult = snpSomaticFilter.getLsFilteredSnp();
		filterGene = false;
	}
	
	/**
	 * 根据基因再进行一次过滤，就是一个基因中有多少snp
	 */
	public void filterByGene() {
		geneFilter.addLsRefSiteSnpIndel(lsRefSiteSnpIndelsResult);
		lsRefSiteSnpIndelsResult = geneFilter.filterSnpInGene();
		filterGene = true;
	}

	public void writeToFile(String fileName) {
		TxtReadandWrite txtOutput = new TxtReadandWrite(fileName, true);
		Set<String> setSampleName = snpSomaticFilter.getSetSampleName();
		if (filterGene) {
			if (geneFilter.getSetSampleName().size() == 0) {
				geneFilter.setSampleName(setSampleName);
			}
			setSampleName = geneFilter.getSetSampleName();
		}
		String[] title = RefSiteSnpIndel.getTitleFromSampleName(setSampleName, snpSomaticFilter.getVCFflag());
		txtOutput.writefileln(title);
		for (RefSiteSnpIndel refSiteSnpIndel : lsRefSiteSnpIndelsResult) {
			ArrayList<String[]> lsTmpResult = refSiteSnpIndel.toStringLsSnp(setSampleName, snpSomaticFilter.getVCFflag());
			for (String[] strings : lsTmpResult) {
				txtOutput.writefileln(strings);
			}
		}
		txtOutput.close();
	}
	
}
