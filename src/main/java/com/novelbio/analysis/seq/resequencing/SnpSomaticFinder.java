package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/** 用于查找多样本SnpSomatic的service类
 * 找出somatic snp，并且这些样本的snp在同一个基因内部
 * 首先要把SNPGATKcope对象设定并运行好，然后再执行这个
 *  
 *  */
public class SnpSomaticFinder {
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
		
		snpSomaticFilter.addFilterSample(sampleDetailTreat);
		snpSomaticFilter.addFilterSample(sampleDetailCol);
		
		snpSomaticFilter.readSnpDetailFromFile();
		snpSomaticFilter.writeToFile("/media/winF/NBC/Project/Project_HXW/20121018/result/ColvsTreat.xls");
		
		snpSomaticFilter.filterSnp();
		snpSomaticFilter.writeToFile("/media/winF/NBC/Project/Project_HXW/20121018/result/ColvsTreat_Filter.xls");
		
		snpSomaticFilter.getLsFilteredSnp();
		GeneFilter geneFilter = new GeneFilter();
		geneFilter.setGffChrAbs(new GffChrAbs(9606));
		geneFilter.setSnpLevel(SnpGroupFilterInfo.Heto);
		geneFilter.setTreatFilteredNum(2);
		geneFilter.addTreatName("5B");
		geneFilter.addTreatName("7B");
		geneFilter.addTreatName("10B");
		
		String outFile = "/media/winF/NBC/Project/Project_HXW/20121018/result/ColvsTreat_Filter_filterGene.xls";
		
		ArrayList<RefSiteSnpIndel> lsRefSiteSnpIndels = geneFilter.filterSnpInGene();
		RefSiteSnpIndel.writeToFile(outFile, lsRefSiteSnpIndels, geneFilter.getSetTreat(), false);
		
	}
	
	SnpSomaticFilter snpgatKcope;
	GeneFilter geneFilter = new GeneFilter();
	ArrayList<RefSiteSnpIndel> lsRefSiteSnpIndelsResult;
	
	public void setSnpgatKcope(SnpSomaticFilter snpgatKcope) {
		this.snpgatKcope = snpgatKcope;
	}

	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		geneFilter.setGffChrAbs(gffChrAbs);
	}
	/**
	 * 添加要检验的样本名
	 * @param colTreatName
	 */
	public void addTreatName(Collection<String> colTreatName) {
		geneFilter.addTreatName(colTreatName);
	}
	/** 其中有几个样本出现这种情况就算通过 */
	public void setTreatFilteredNum(int treatFilteredNum) {
		geneFilter.setTreatFilteredNum(treatFilteredNum);
	}
	
	public void setSnpLevel(SnpLevel snpLevel) {
		geneFilter.setSnpLevel(snpLevel);
	}

	public void running() {
		ArrayList<RefSiteSnpIndel> lsFilteredRefSnp = snpgatKcope.getLsFilteredSnp();
		geneFilter.addLsRefSiteSnpIndel(lsFilteredRefSnp);
		lsRefSiteSnpIndelsResult = geneFilter.filterSnpInGene();
	}
	
	public void writeToFile(String fileName) {
		TxtReadandWrite txtOutput = new TxtReadandWrite(fileName, true);
		String[] title = RefSiteSnpIndel.getTitleFromSampleName(geneFilter.getSetTreat(), snpgatKcope.getVCFflag());
		txtOutput.writefileln(title);
		for (RefSiteSnpIndel refSiteSnpIndel : lsRefSiteSnpIndelsResult) {
			ArrayList<String[]> lsTmpResult = refSiteSnpIndel.toStringLsSnp(geneFilter.getSetTreat(), snpgatKcope.getVCFflag());
			for (String[] strings : lsTmpResult) {
				txtOutput.writefileln(strings);
			}
		}
		txtOutput.close();
	}
	
}
