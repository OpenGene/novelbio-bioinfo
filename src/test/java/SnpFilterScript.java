

import java.util.ArrayList;

import javax.jnlp.FileOpenService;

import org.apache.ibatis.annotations.Select;

import net.sf.picard.annotation.Gene;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.resequencing.GeneFilter;
import com.novelbio.analysis.seq.resequencing.RefSiteSnpIndel;
import com.novelbio.analysis.seq.resequencing.SnpFilter;
import com.novelbio.analysis.seq.resequencing.SnpSomaticFilter;
import com.novelbio.analysis.seq.resequencing.SnpGroupFilterInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.generalConf.NovelBioConst;

public class SnpFilterScript {
	public static void main(String[] args) {
//		snpFilterAllSample();
//		
//		snpCallingZDB("TF142-3");
//		snpCallingZDB("TF182-1");
//		snpCallingZDB("TF57-1");
//		snpCallingZDB("TF75-4");
//		snpCallingZDB("TF81-2");
	
//		GeneID gneGeneID = new GeneID("LOC_Os06g50380", 0);
//		System.out.println(gneGeneID.getDescription());
		GeneID geneID = new GeneID("test", 59289);
		System.out.println(geneID.getIDtype());
		
		
	
	}
	public static void snpFilterAllSample() {
		SnpSomaticFilter snpgatKcope = new SnpSomaticFilter();
//		snpgatKcope.setGffChrAbs(new GffChrAbs(9606));
		
//		snpgatKcope.addVcfToLsSnpIndel("2A", parentPath + "2A_SNPrecal_IndelFiltered.vcf");
//		snpgatKcope.addVcfToLsSnpIndel("2B", parentPath + "2B_SNPrecal_IndelFiltered.vcf");
		
		String parentFile = "/media/winF/NBC/Project/Project_HXW/allPileUp/";
//		snpgatKcope.addSnpFromNBCfile("10A", parentFile + "10A_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
//		snpgatKcope.addSnpFromNBCfile("10A", parentFile + "10B_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
		
		snpgatKcope.setSnp_HetoMore_Contain_SnpProp_Min(0.2);
//		snpgatKcope.addSnpFromPileUpFile("10A", SnpGroupFilterInfo.HetoMore, parentFile + "10A_sorted_realign_removeDuplicate_pileup.gz");
//		snpgatKcope.addSnpFromPileUpFile("10B", SnpGroupFilterInfo.HetoMore, parentFile + "10B_sorted_realign_removeDuplicate_pileup.gz");
//		snpgatKcope.addSnpFromPileUpFile("5A", SnpGroupFilterInfo.HetoMore, parentFile + "5A_sorted_realign_removeDuplicate_pileup.gz");
//		snpgatKcope.addSnpFromPileUpFile("5B", SnpGroupFilterInfo.HetoMore, parentFile + "5B_sorted_realign_removeDuplicate_pileup.gz");
//		snpgatKcope.addSnpFromPileUpFile("7A", SnpGroupFilterInfo.HetoMore, parentFile + "7A_sorted_realign_removeDuplicate_pileup.gz");
//		snpgatKcope.addSnpFromPileUpFile("7B", SnpGroupFilterInfo.HetoMore, parentFile + "7B_sorted_realign_removeDuplicate_pileup.gz");
//		snpgatKcope.addSnpFromPileUpFile("2A", SnpGroupFilterInfo.HetoMore, parentFile + "2A_sorted_realign_removeDuplicate_pileup.gz");
//		snpgatKcope.addSnpFromPileUpFile("2B", SnpGroupFilterInfo.HetoMore, parentFile + "2B_sorted_realign_removeDuplicate_pileup.gz");
//		snpgatKcope.addSnpFromPileUpFile("3A", SnpGroupFilterInfo.HetoMore, parentFile + "3A_sorted_realign_removeDuplicate_pileup.gz");
//		snpgatKcope.addSnpFromPileUpFile("3B", SnpGroupFilterInfo.HetoMore, parentFile + "3B_sorted_realign_removeDuplicate_pileup.gz");
//		snpgatKcope.addSnpFromPileUpFile("C", SnpGroupFilterInfo.HetoMore, parentFile + "C_sorted_realign_removeDuplicate_pileup.gz");
//		snpgatKcope.addSnpFromPileUpFile("D", SnpGroupFilterInfo.HetoMore, parentFile + "D_sorted_realign_removeDuplicate_pileup.gz");
		
		snpgatKcope.addSnpFromNBCfile("10A", parentFile + "10A_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSnpFromNBCfile("10B", parentFile + "10B_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSnpFromNBCfile("5A", parentFile + "5A_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSnpFromNBCfile("5B", parentFile + "5B_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSnpFromNBCfile("7A", parentFile + "7A_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSnpFromNBCfile("7B", parentFile + "7B_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSnpFromNBCfile("2A", parentFile + "2A_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSnpFromNBCfile("2B", parentFile + "2B_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSnpFromNBCfile("3A", parentFile + "3A_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSnpFromNBCfile("3B", parentFile + "3B_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSnpFromNBCfile("C", parentFile + "C_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSnpFromNBCfile("D", parentFile + "D_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		
		snpgatKcope.addSampileupFile("10A", parentFile + "10A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("10B", parentFile + "10B_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("5A", parentFile + "5A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("5B", parentFile + "5B_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("7A", parentFile + "7A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("7B", parentFile + "7B_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("2A", parentFile + "2A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("2B", parentFile + "2B_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("3A", parentFile + "3A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("3B", parentFile + "3B_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("C", parentFile + "C_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("D", parentFile + "D_sorted_realign_removeDuplicate_pileup.gz");
		
		SnpGroupFilterInfo sampleDetailNorm = new SnpGroupFilterInfo();
		sampleDetailNorm.setSampleUnKnownProp(0.2);
		sampleDetailNorm.addSampleName("10A");
		sampleDetailNorm.addSampleName("5A");
		sampleDetailNorm.addSampleName("7A");
		sampleDetailNorm.addSampleName("2A");
		sampleDetailNorm.addSampleName("3A");
		sampleDetailNorm.addSampleName("D");

		sampleDetailNorm.setSampleRefHomoNum(1, 6);
		sampleDetailNorm.setSampleSnpIndelNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetailNorm);
		
		SnpGroupFilterInfo sampleDetailTreat = new SnpGroupFilterInfo();
		sampleDetailTreat.setSampleUnKnownProp(0.2);
		sampleDetailTreat.addSampleName("10B");
		sampleDetailTreat.addSampleName("5B");
		sampleDetailTreat.addSampleName("7B");
		sampleDetailTreat.addSampleName("2B");
		sampleDetailTreat.addSampleName("3B");
		sampleDetailTreat.addSampleName("C");
		sampleDetailTreat.setSampleRefHomoNum(0, 5);
		sampleDetailTreat.setSampleSnpIndelHetoMoreNum(1, 6);
		sampleDetailTreat.setSampleSnpIndelHetoLessNum(0, 6);
		snpgatKcope.addFilterSample(sampleDetailTreat);
		
		snpgatKcope.readSnpDetailFromFile();
		snpgatKcope.writeToFile(parentFile + "result/NormvsTreat.xls");
		
		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile(parentFile + "result/NormvsTreat_filter.xls");
		
		GeneFilter geneFilter = new GeneFilter();
		geneFilter.setGffChrAbs(new GffChrAbs(9606));
		geneFilter.setSnpLevel(SnpGroupFilterInfo.Heto);
		geneFilter.addLsRefSiteSnpIndel(snpgatKcope.getLsFilteredSnp());
		geneFilter.setTreatFilteredNum(2);
		geneFilter.addTreatName("5B");
		geneFilter.addTreatName("7B");
		geneFilter.addTreatName("10B");
		geneFilter.addTreatName("3B");
		geneFilter.addTreatName("2B");
		geneFilter.addTreatName("C");
		geneFilter.setTreatFilteredNum(2);
		ArrayList<RefSiteSnpIndel> lsFilteredSnp2 = geneFilter.filterSnpInGene();
		String outFile2 = parentFile + "result/filteredGene_2Num.xls";
		RefSiteSnpIndel.writeToFile(outFile2, lsFilteredSnp2);
		
		geneFilter.setTreatFilteredNum(3);
		ArrayList<RefSiteSnpIndel> lsFilteredSnp3 = geneFilter.filterSnpInGene();
		String outFile3 = parentFile + "result/filteredGene_3Num.xls";
		RefSiteSnpIndel.writeToFile(outFile3, lsFilteredSnp3);
		
		geneFilter.setTreatFilteredNum(4);
		ArrayList<RefSiteSnpIndel> lsFilteredSnp4 = geneFilter.filterSnpInGene();
		String outFile4 = parentFile + "result/filteredGene_4Num.xls";
		RefSiteSnpIndel.writeToFile(outFile4, lsFilteredSnp4);
	}
	
	public static void snpFilter() {
		SnpSomaticFilter snpgatKcope = new SnpSomaticFilter();
//		snpgatKcope.setGffChrAbs(new GffChrAbs(9606));
		
//		snpgatKcope.addVcfToLsSnpIndel("2A", parentPath + "2A_SNPrecal_IndelFiltered.vcf");
//		snpgatKcope.addVcfToLsSnpIndel("2B", parentPath + "2B_SNPrecal_IndelFiltered.vcf");
		
		String parentFile = "/media/winF/NBC/Project/Project_HXW/allPileUp/";
//		snpgatKcope.addSnpFromNBCfile("10A", parentFile + "10A_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
//		snpgatKcope.addSnpFromNBCfile("10A", parentFile + "10B_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
		
		snpgatKcope.setSnp_HetoMore_Contain_SnpProp_Min(0.2);
		snpgatKcope.addSnpFromPileUpFile("10A", SnpGroupFilterInfo.HetoMore, parentFile + "10A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile("10B", SnpGroupFilterInfo.HetoMore, parentFile + "10B_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile("5A", SnpGroupFilterInfo.HetoMore, parentFile + "5A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile("5B", SnpGroupFilterInfo.HetoMore, parentFile + "5B_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile("7A", SnpGroupFilterInfo.HetoMore, parentFile + "7A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile("7B", SnpGroupFilterInfo.HetoMore, parentFile + "7B_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile("2A", SnpGroupFilterInfo.HetoMore, parentFile + "2A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile("2B", SnpGroupFilterInfo.HetoMore, parentFile + "2B_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile("3A", SnpGroupFilterInfo.HetoMore, parentFile + "3A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile("3B", SnpGroupFilterInfo.HetoMore, parentFile + "3B_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile("C", SnpGroupFilterInfo.HetoMore, parentFile + "C_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile("D", SnpGroupFilterInfo.HetoMore, parentFile + "D_sorted_realign_removeDuplicate_pileup.gz");

		SnpGroupFilterInfo sampleDetail10A = new SnpGroupFilterInfo();
		sampleDetail10A.addSampleName("10A");
		sampleDetail10A.setSampleRefHomoNum(1, 1);
		sampleDetail10A.setSampleSnpIndelHetoNum(0, 0);
		sampleDetail10A.setSampleSnpIndelHomoNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail10A);
		
		SnpGroupFilterInfo sampleDetail10B = new SnpGroupFilterInfo();
		sampleDetail10B.addSampleName("10B");
		sampleDetail10B.setSampleRefHomoNum(0, 0);
		sampleDetail10B.setSampleSnpIndelNum(1, 1);
		sampleDetail10B.setSampleSnpIndelHetoLessNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail10B);
		
		snpgatKcope.readSnpDetailFromFile();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/20121018/result/10Avs10B.xls");
		
		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/20121018/result/10Avs10B_filter.xls");
		snpgatKcope = null;
		
		snpgatKcope = new SnpSomaticFilter();
		
		snpgatKcope.addSnpFromNBCfile("5A", parentFile + "5A_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
		snpgatKcope.addSnpFromNBCfile("5A", parentFile + "5B_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");

		snpgatKcope.addSampileupFile("5A", parentFile + "5A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("5B", parentFile + "5B_sorted_realign_removeDuplicate_pileup.gz");
		
		SnpGroupFilterInfo sampleDetail5A = new SnpGroupFilterInfo();
		sampleDetail5A.addSampleName("5A");
		sampleDetail5A.setSampleRefHomoNum(1, 1);
		sampleDetail5A.setSampleSnpIndelHetoNum(0, 0);
		sampleDetail5A.setSampleSnpIndelHomoNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail5A);
		
		SnpGroupFilterInfo sampleDetail5B = new SnpGroupFilterInfo();
		sampleDetail5B.addSampleName("5B");
		sampleDetail5B.setSampleRefHomoNum(0, 0);
		sampleDetail5B.setSampleSnpIndelNum(1, 1);
		sampleDetail5B.setSampleSnpIndelHetoLessNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail5B);
		
		snpgatKcope.readSnpDetailFromFile();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/20121018/result/5Avs5B.xls");
		
		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/20121018/result/5Avs5B_filter.xls");
		snpgatKcope = null;
		
		
		
		
		snpgatKcope = new SnpSomaticFilter();

		snpgatKcope.addSnpFromNBCfile("7A", parentFile + "7A_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
		snpgatKcope.addSnpFromNBCfile("7A", parentFile + "7B_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");

		snpgatKcope.addSampileupFile("7A", parentFile + "7A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("7B", parentFile + "7B_sorted_realign_removeDuplicate_pileup.gz");
		
		SnpGroupFilterInfo sampleDetail7A = new SnpGroupFilterInfo();
		sampleDetail7A.addSampleName("7A");
		sampleDetail7A.setSampleRefHomoNum(1, 1);
		snpgatKcope.addFilterSample(sampleDetail7A);
		
		SnpGroupFilterInfo sampleDetail7B = new SnpGroupFilterInfo();
		sampleDetail7B.addSampleName("7B");
		sampleDetail7B.setSampleRefHomoNum(0, 0);
		sampleDetail7B.setSampleSnpIndelNum(1, 1);
		sampleDetail7B.setSampleSnpIndelHetoLessNum(0, 0);
		sampleDetail7B.setSampleSnpIndelHetoLessNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail7B);
		
		snpgatKcope.readSnpDetailFromFile();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/20121018/result/7Avs7B.xls");
		
		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/20121018/result/7Avs7B_filter.xls");
		snpgatKcope = null;		
	}
	public static void snpCalling() {
		SnpSomaticFilter snpgatKcope = new SnpSomaticFilter();
		snpgatKcope.setGffChrAbs(new GffChrAbs(39947));
		
		snpgatKcope.setSnp_HetoMore_Contain_SnpProp_Min(0.4);
		
		String parentFile = "/media/winE/NBC/Project/PGM/";
		
		snpgatKcope.addSnpFromVcfFile("PGM", parentFile + "variants_call/TSVC_variants.vcf");
		snpgatKcope.addSampileupFile("PGM", parentFile + "CombineAlignments_CA_yuli-all_yuli1-10_001_sorted_pileup.gz");
		
		snpgatKcope.addSnpFromNBCfile("ILM", parentFile + "BZ9522_sorted_realign_removeDuplicate_pileup_outSnp.txt");
		snpgatKcope.addSampileupFile("ILM", parentFile + "BZ9522_sorted_realign_removeDuplicate_pileup.gz");

		
		SnpGroupFilterInfo sampleDetailILM = new SnpGroupFilterInfo();
		sampleDetailILM.addSampleName("ILM");
		sampleDetailILM.setSampleRefHomoNum(1, 1);
		sampleDetailILM.setSampleSnpIndelHetoNum(0, 0);
		sampleDetailILM.setSampleSnpIndelHomoNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetailILM);
		
		SnpGroupFilterInfo sampleDetailPGM = new SnpGroupFilterInfo();
		sampleDetailPGM.addSampleName("PGM");
		sampleDetailPGM.setSampleRefHomoNum(0, 0);
		sampleDetailPGM.setSampleSnpIndelNum(1, 1);
		sampleDetailPGM.setSampleSnpIndelHetoLessNum(0, 0);
		sampleDetailPGM.setSampleSnpIndelHetoNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetailPGM);
		
		snpgatKcope.readSnpDetailFromFile();
		snpgatKcope.writeToFile(parentFile + "PGMsnpvsILM.xls");
		
		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile(parentFile + "PGMsnpvsILM_Filtered.xls");
	}
	
	public static void snpCallingZDB(String sampleName) {
		SnpSomaticFilter snpgatKcope = new SnpSomaticFilter();
//		snpgatKcope.setGffChrAbs(new GffChrAbs(39947));
		
		snpgatKcope.setSnp_HetoMore_Contain_SnpProp_Min(0.3);
		
		String parentFile = "/media/winF/NBC/Project/Project_ZDB_Lab/QXL/Project_ZDB/pileup/";
		snpgatKcope.addSnpFromPileUpFile("9522", SnpGroupFilterInfo.HetoMore, parentFile + "a9522_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSnpFromPileUpFile(sampleName, SnpGroupFilterInfo.HetoMore, parentFile + sampleName +  "_sorted_realign_removeDuplicate_pileup.gz");

		snpgatKcope.addSampileupFile("9522", parentFile + "a9522_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile(sampleName, parentFile + sampleName + "_sorted_realign_removeDuplicate_pileup.gz");

		SnpGroupFilterInfo sampleDetail9522 = new SnpGroupFilterInfo();
		sampleDetail9522.addSampleName("9522");
		sampleDetail9522.setSampleRefHomoNum(1, 1);
		sampleDetail9522.setSampleSnpIndelHetoNum(0, 0);
		sampleDetail9522.setSampleSnpIndelHomoNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail9522);
		
		SnpGroupFilterInfo sampleDetailMut = new SnpGroupFilterInfo();
		sampleDetailMut.addSampleName(sampleName);
		sampleDetailMut.setSampleRefHomoNum(0, 0);
		sampleDetailMut.setSampleSnpIndelNum(1, 1);
		sampleDetailMut.setSampleSnpIndelHetoLessNum(0, 0);
		sampleDetailMut.setSampleSnpIndelHetoNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetailMut);
		
		snpgatKcope.readSnpDetailFromFile();
		snpgatKcope.writeToFile(parentFile + "9522snpvs"+sampleName+".xls");
		
		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile(parentFile + "9522snpvs"+sampleName+"_Filtered.xls");
	}
}
