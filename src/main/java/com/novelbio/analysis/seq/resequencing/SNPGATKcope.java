package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;

/**
 * ��ȡ����GATK��vcf����ļ���Ȼ���ò���snp�������ÿ��snp����Ϣ�����ڻ���ȵ�
 * @author zong0jie
 */
public class SNPGATKcope {
	Logger logger = Logger.getLogger(SNPGATKcope.class);
	GffChrAbs gffChrAbs;

	/** vcf���� */
	VcfCols vcfCols = new VcfCols();
	/** �������������Ǵ���Щ�ı��л�ȡsnp����Ϣ */
	ArrayList<String[]> lsSample2VcfFiles = new ArrayList<String[]>();
	ArrayList<String[]> lsSample2NBCfiles = new ArrayList<String[]>();
	ArrayList<SnpCalling> lsSample2PileUpFiles = new ArrayList<SnpCalling>();

	/** 0��sampleName<br>
	 * 1��SampleFile  */
	ArrayList<String[]> lsSample2SamPileupFile = new ArrayList<String[]>();

	/** ���ڶ��������snpȥ����ģ�����key��ʾ��snp���ڵ������Ϣ��value���Ǹ�λ������snp��� */
	TreeMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel = new TreeMap<String, MapInfoSnpIndel>();
	/**���˺��snp */
	ArrayList<MapInfoSnpIndel> lsFilteredSnp = new ArrayList<MapInfoSnpIndel>();
	/**ÿ��λ���Ӧ��causal snp
	 * һ��λ����ܴ��ڶ��snp������װ��list����  */
	ArrayList<ArrayList<SiteSnpIndelInfo>> lsFilteredSite = new ArrayList<ArrayList<SiteSnpIndelInfo>>();
	
	/** �������������� */
	SnpFilter sampleFilter = new SnpFilter();

	/** ��������֮��Ƚϵ���Ϣ */
	ArrayList<SnpGroupFilterInfo> lsSampleDetailCompare = new ArrayList<SnpGroupFilterInfo>();
	
	public static void main(String[] args) {
		snpCalling();
		snpCalling2();
	}
	
	public static void snpFilter() {
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		snpgatKcope.setGffChrAbs(new GffChrAbs(9606));
		
//		snpgatKcope.addVcfToLsSnpIndel("2A", parentPath + "2A_SNPrecal_IndelFiltered.vcf");
//		snpgatKcope.addVcfToLsSnpIndel("2B", parentPath + "2B_SNPrecal_IndelFiltered.vcf");
		
		String parentFile = "/media/winF/NBC/Project/Project_HXW/20121018/mapping/";
		snpgatKcope.addSnpFromNBCfile("10A", parentFile + "10A_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");
		snpgatKcope.addSnpFromNBCfile("10A", parentFile + "10B_sorted_realign_removeDuplicate_pileup_SnpInfo.txt");

		snpgatKcope.addSampileupFile("10A", parentFile + "10A_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("10B", parentFile + "10B_sorted_realign_removeDuplicate_pileup.gz");
		
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
		
		snpgatKcope = new SNPGATKcope();
		
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
		
		
		
		
		snpgatKcope = new SNPGATKcope();

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
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		snpgatKcope.setGffChrAbs(new GffChrAbs(39947));
		
		snpgatKcope.setSnp_HetoMore_Contain_SnpProp_Min(0.4);
		
		String parentFile = "/media/winE/NBC/Project/PGM/";
		snpgatKcope.addSampileupFile("ILM", parentFile + "BZ9522_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("PGM", parentFile + "CombineAlignments_CA_yuli-all_yuli1-10_001_sorted_pileup.gz");
		
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
	
	public static void snpCalling2() {
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		snpgatKcope.setGffChrAbs(new GffChrAbs(39947));
		
		snpgatKcope.setSnp_HetoMore_Contain_SnpProp_Min(0.4);
		
		String parentFile = "/media/winE/NBC/Project/PGM/";
		snpgatKcope.addSampileupFile("ILM", parentFile + "BZ9522_sorted_realign_removeDuplicate_pileup.gz");
		snpgatKcope.addSampileupFile("PGM", parentFile + "CombineAlignments_CA_yuli-all_yuli1-10_001_sorted_pileup.gz");
		
		SnpGroupFilterInfo sampleDetailPGM = new SnpGroupFilterInfo();
		sampleDetailPGM.addSampleName("PGM");
		sampleDetailPGM.setSampleRefHomoNum(1, 1);
		sampleDetailPGM.setSampleSnpIndelHetoNum(0, 0);
		sampleDetailPGM.setSampleSnpIndelHomoNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetailPGM);
		
		SnpGroupFilterInfo sampleDetailILM = new SnpGroupFilterInfo();
		sampleDetailILM.addSampleName("ILM");
		sampleDetailILM.setSampleRefHomoNum(0, 0);
		sampleDetailILM.setSampleSnpIndelNum(1, 1);
		sampleDetailILM.setSampleSnpIndelHetoLessNum(0, 0);
		sampleDetailILM.setSampleSnpIndelHetoNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetailILM);
		
		snpgatKcope.readSnpDetailFromFile();
		snpgatKcope.writeToFile(parentFile + "ILMsnpvsPGM.xls");
		
		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile(parentFile + "ILMsnpvsPGM_Filtered.xls");
	}
	
	/** �ж�Ϊsnp Heto�����е�snp��������С�ڸ���ֵ */
	public void setSnp_Hete_Contain_SnpProp_Min(double snp_Hete_Contain_SnpProp_Min) {
		sampleFilter.setSnp_Hete_Contain_SnpProp_Min(snp_Hete_Contain_SnpProp_Min);
	}
	
	/** �ж�Ϊsnp Heto�����е�snp��������С�ڸ���ֵ */
	public void setSnp_HetoMore_Contain_SnpProp_Min(double snp_HetoMore_Contain_SnpProp_Min) {
		sampleFilter.setSnp_HetoMore_Contain_SnpProp_Min(snp_HetoMore_Contain_SnpProp_Min);
	}
	
	public void addSnpFromVcfFile(String sampleName, String vcfFile) {
		lsSample2VcfFiles.add(new String[]{sampleName, vcfFile});
	}
	public void addSnpFromNBCfile(String sampleName, String nbcFile) {
		lsSample2NBCfiles.add(new String[]{sampleName, nbcFile});
	}
	public void addSnpFromPileUpFile(String sampleName, SnpGroupFilterInfo snpGroupInfoFilter, String pileUpfile) {
		SnpCalling snpCalling = new SnpCalling();
		snpCalling.setGffChrAbs(gffChrAbs);
		snpCalling.setMapSiteInfo2MapInfoSnpIndel(mapSiteInfo2MapInfoSnpIndel);
		snpCalling.setSampleDetail(snpGroupInfoFilter);
		snpCalling.addSnpFromPileUpFile(sampleName, pileUpfile, FileOperate.changeFileSuffix(pileUpfile, "_outSnp", "txt"));
		lsSample2PileUpFiles.add(snpCalling);
	}
	
	/** ����ЩpileUp���ļ��������е�snp�ľ���ϸ�� */
	public void addSampileupFile(String sampleName, String sampileupFile) {
		lsSample2SamPileupFile.add(new String[]{sampleName, sampileupFile});
	}
	/** ���ù��˵�������Ϣ */
	public void clearSampleFilterInfo() {
		lsSampleDetailCompare.clear();
	}
	/** ���������ľ�����Ϣ */
	public void addFilterSample(SnpGroupFilterInfo snpGroupFilterInfo) {
		lsSampleDetailCompare.add(snpGroupFilterInfo);
	}
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * ��gatk����vcf�ļ��е�snp��Ϣ����mapSiteInfo2MapInfoSnpIndel��
	 */
	private void addVcfToLsSnpIndel(String sampleName, String vcfFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(vcfFile, false);
		for (String vcfLines : txtRead.readlines()) {
			if (vcfLines.startsWith("#")) continue;
			String[] ss = vcfLines.split("\t");
			
			try {Integer.parseInt(ss[vcfCols.colSnpStart]); } catch (Exception e) { continue; }
			
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, sampleName);
			mapInfoSnpIndel.setVcfLines(sampleName, vcfCols, vcfLines);
			
			addSnp_2_mapSiteInfo2MapInfoSnpIndel(mapInfoSnpIndel);
		}
	}
	/**
	 * ��gatk����vcf�ļ��е�snp��Ϣ����mapSiteInfo2MapInfoSnpIndel��
	 */
	private void addNBCToLsSnpIndel(String sampleName, String novelbioFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(novelbioFile, false);
		for (String vcfLines : txtRead.readlines()) {
			if (vcfLines.startsWith("#")) continue;
			String[] ss = vcfLines.split("\t");
			
			try {Integer.parseInt(ss[vcfCols.colSnpStart]); } catch (Exception e) { continue; }
			
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, sampleName);
			mapInfoSnpIndel.setNBCLines(sampleName, vcfLines);
			addSnp_2_mapSiteInfo2MapInfoSnpIndel(mapInfoSnpIndel);
		}
	}
	
	private void addSnp_2_mapSiteInfo2MapInfoSnpIndel(MapInfoSnpIndel mapInfoSnpIndel) {
		String key = mapInfoSnpIndel.getRefID() + SepSign.SEP_ID + mapInfoSnpIndel.getRefSnpIndelStart();
		if (mapSiteInfo2MapInfoSnpIndel.containsKey(key)) {
			MapInfoSnpIndel maInfoSnpIndelExist = mapSiteInfo2MapInfoSnpIndel.get(key);
			maInfoSnpIndelExist.addAllenInfo(mapInfoSnpIndel);
			return;
		}
		else {
			mapSiteInfo2MapInfoSnpIndel.put(key, mapInfoSnpIndel);
		}
//		logger.error("tree map size: " +mapSiteInfo2MapInfoSnpIndel.size());
	}
	
	/** ���趨snp�ļ�������£���pileup�ļ��л�ȡsnp��Ϣ
	 * ֻҪ�趨��snp�ļ����ɣ��ڲ��Զ���snp calling
	 *  */
	public void readSnpDetailFromFile() {
		for (String[] sample2vcf : lsSample2VcfFiles) {
			addVcfToLsSnpIndel(sample2vcf[0], sample2vcf[1]);
		}
		for (String[] sample2NBCfile : lsSample2NBCfiles) {
			addNBCToLsSnpIndel(sample2NBCfile[0], sample2NBCfile[1]);
		}
		addPileupToLsSnpIndel();
		getSnpDetail(mapSiteInfo2MapInfoSnpIndel.values());
		lsFilteredSnp = ArrayOperate.getArrayListValue(mapSiteInfo2MapInfoSnpIndel);
	}
	
	/** 
	 * ����vcf�����Ǵ�pileUp�л�ȡsnp�ķ���
	 * ��pileUp��snp��Ϣ����mapSiteInfo2MapInfoSnpIndel��
	 * ͬʱ����һ��snp����Ϣ��
	 */
	private void addPileupToLsSnpIndel() {
		for (SnpCalling snpCalling : lsSample2PileUpFiles) {
			snpCalling.run();
		}
	}
	
	private void getSnpDetail(Collection<MapInfoSnpIndel> colMapInfoSnpIndels) {
		SnpDetailGet snpDetailGet = new SnpDetailGet();
		snpDetailGet.setGffChrAbs(gffChrAbs);
		snpDetailGet.setMapChrID2InfoSnpIndel(colMapInfoSnpIndels);
		for (String[] sample2PileUp : lsSample2SamPileupFile) {
			snpDetailGet.addSample2PileupFile(sample2PileUp[0], sample2PileUp[1]);
		}
		snpDetailGet.run();
	}
	/** ������readSnpDetailFromPileUp֮��ִ�� */
	public void filterSnp() {
		sampleFilter.clearSampleFilterInfo();
		for (SnpGroupFilterInfo snpGroupInfoFilter : lsSampleDetailCompare) {
			sampleFilter.addSampleFilterInfo(snpGroupInfoFilter);
		}
		
		lsFilteredSite.clear();
		lsFilteredSnp.clear();
		for (MapInfoSnpIndel mapInfoSnpIndel : mapSiteInfo2MapInfoSnpIndel.values()) {
			ArrayList<SiteSnpIndelInfo> lsSiteSnpIndelInfo = sampleFilter.getFilterdSnp(mapInfoSnpIndel);
			if (lsSiteSnpIndelInfo.size() > 0) {
				lsFilteredSnp.add(mapInfoSnpIndel);
				lsFilteredSite.add(lsSiteSnpIndelInfo);
			}
		}
	}
	public void writeToFile(String txtFile) {
		LinkedHashSet<String> setSample = new LinkedHashSet<String>();
		for (String[] strings : lsSample2VcfFiles) {
			setSample.add(strings[0]);
		}
		for (String[] strings : lsSample2SamPileupFile) {
			setSample.add(strings[0]);
		}
		
		TxtReadandWrite txtOut = new TxtReadandWrite(txtFile, true);
		txtOut.writefileln(MapInfoSnpIndel.getTitleFromSampleName(setSample));
		ArrayList<SiteSnpIndelInfo> lsSiteSnpIndelInfos = new ArrayList<SiteSnpIndelInfo>();
		for (int i = 0; i < lsFilteredSnp.size(); i++) {
			if (lsFilteredSite != null && lsFilteredSite.size() > 0) {
				lsSiteSnpIndelInfos = lsFilteredSite.get(i);
			}
			MapInfoSnpIndel mapInfoSnpIndel = lsFilteredSnp.get(i);
			ArrayList<String[]> lsResult = mapInfoSnpIndel.toStringLsSnp(setSample, false, lsSiteSnpIndelInfos);
			for (String[] strings : lsResult) {
				txtOut.writefileln(strings);
			}
		}
		txtOut.close();
	}
	
	
	
	/**
	 * �����ı�����domain��Ϣ����þ���domain����Ϣ
	 * @param txtExcelSNP
	 * @param domainFile
	 * @param outFile
	 */
//	public void setDomainInfo(String txtExcelSNP, String domainFile, String outFile) {
//		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
//		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, 
//				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
//		DomainPfam.readDomain(domainFile);
//		ArrayList<String[]> lsSnp = ExcelTxtRead.readLsExcelTxt(txtExcelSNP, 1);
//		for (int i = 1; i < lsSnp.size(); i++) {
//			String[] ss = lsSnp.get(i);
//			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, chrID, refSnpIndelStart)//(0, ss[0], Integer.parseInt(ss[1]), ss[3], ss[4]);
//			GffCodGene gffcod = gffHashGene.searchLocation(mapInfoSnpIndel.getRefID(), mapInfoSnpIndel.getRefSnpIndelStart());
//			String tmp = "";
//			if (gffcod.isInsideLoc()) {
//				GffDetailGene gffDetailGene = gffcod.getGffDetailThis();
//				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
//					DomainPfam domainPfam = DomainPfam.getDomainPfam(gffGeneIsoInfo.getName());
//					if (domainPfam == null) {
//						continue;
//					}
//					domainPfam.setAALoc(gffGeneIsoInfo.getCod2ATGmRNA(gffcod.getCoord())/3);
//					tmp = domainPfam.toString();
//					break;
//				}
//				
//			}
//			String result = ArrayOperate.cmbString(ss, "\t");
//			result = result + "\t" + tmp;
//			txtOut.writefileln(result);
//		}
//		txtOut.close();
//	}
//	
}
