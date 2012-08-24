package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.util.SSCellRange;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.genomeNew.GffChrSeq;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGeneRefSeq;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.NovelBioConst;
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
	ArrayList<String[]> lsSample2PileUpFiles = new ArrayList<String[]>();
	HashMap<String, SnpGroupInfoFilter> setSample2SampleDetail = new HashMap<String, SnpGroupInfoFilter>();
	/** 0��sampleName<br>
	 * 1��SampleFile  */
	ArrayList<String[]> lsSample2SamPileupFile = new ArrayList<String[]>();

	/** ���ڶ��������snpȥ����ģ�����key��ʾ��snp���ڵ������Ϣ��value���Ǹ�λ������snp��� */
	HashMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel = new HashMap<String, MapInfoSnpIndel>();
	/**���˺��snp */
	ArrayList<MapInfoSnpIndel> lsFilteredSnp = new ArrayList<MapInfoSnpIndel>();
	/**ÿ��λ���Ӧ��causal snp
	 * һ��λ����ܴ��ڶ��snp������װ��list����  */
	ArrayList<ArrayList<SiteSnpIndelInfo>> lsFilteredSite = new ArrayList<ArrayList<SiteSnpIndelInfo>>();
	
	
	/** �������������� */
	SnpFilter sampleFilter = new SnpFilter();

	/** ��������֮��Ƚϵ���Ϣ */
	ArrayList<SnpGroupInfoFilter> lsSampleDetailCompare = new ArrayList<SnpGroupInfoFilter>();
	
	public static void main(String[] args) {
		String parentPath = "/media/winF/NBC/Project/Project_HXW/20120705/";
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		snpgatKcope.setGffChrAbs(new GffChrAbs(9606));
		
//		snpgatKcope.addVcfFile("2A", parentPath + "2A_SNPrecal_IndelFiltered.vcf");
//		snpgatKcope.addVcfFile("2B", parentPath + "2B_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addVcfToLsSnpIndel("3A", parentPath + "3A_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addVcfToLsSnpIndel("3B", parentPath + "3B_SNPrecal_IndelFiltered.vcf");
//		snpgatKcope.addSampileupFile("2A", parentPath + "2A_piluptest.txt");
//		snpgatKcope.addSampileupFile("2B", parentPath + "2B_piluptest.txt");
//		snpgatKcope.addSampileupFile("2A", parentPath + "2A_detailmpileup.txt");
//		snpgatKcope.addSampileupFile("2B", parentPath + "2B_detailmpileup.txt");
		snpgatKcope.addSampileupFile("3A", parentPath + "3A_detailmpileup.txt");
		snpgatKcope.addSampileupFile("3B", parentPath + "3B_detailmpileup.txt");
		
		SnpGroupInfoFilter sampleDetail2A = new SnpGroupInfoFilter();
		sampleDetail2A.addSampleName("3A");
		sampleDetail2A.setSampleRefHomoNum(1, 1);
		sampleDetail2A.setSampleSnpIndelHetoNum(0, 0);
		sampleDetail2A.setSampleSnpIndelHomoNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail2A);
		
		SnpGroupInfoFilter sampleDetail2B = new SnpGroupInfoFilter();
		sampleDetail2B.addSampleName("3B");
		sampleDetail2B.setSampleRefHomoNum(0, 0);
		sampleDetail2B.setSampleSnpIndelNum(1, 1);
		sampleDetail2B.setSampleSnpIndelHetoLessNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail2B);
		
		snpgatKcope.readSnpDetailFromPileUp();
//		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/result_withSampileup_3Bvs3A.xls");
		
		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/result_withSampileup_3Bvs3A_filter.xls");
	}

	
	public void addSnpFromVcfFile(String sampleName, String vcfFile) {
		lsSample2VcfFiles.add(new String[]{sampleName, vcfFile});
	}
	public void addSnpFromNBCfile(String sampleName, String nbcFile) {
		lsSample2NBCfiles.add(new String[]{sampleName, nbcFile});
	}
	public void addSnpFromPileUpFile(String sampleName, SnpGroupInfoFilter snpGroupInfoFilter, String pileUpfile) {
		setSample2SampleDetail.put(sampleName, snpGroupInfoFilter);
		lsSample2PileUpFiles.add(new String[]{sampleName, pileUpfile});
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
	public void addFilterSample(SnpGroupInfoFilter snpGroupInfoFilter) {
		lsSampleDetailCompare.add(snpGroupInfoFilter);
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
	/** 
	 * ����vcf�����Ǵ�pileUp�л�ȡsnp�ķ���
	 * ��pileUp��snp��Ϣ����mapSiteInfo2MapInfoSnpIndel��
	 * ͬʱ����һ��snp����Ϣ��
	 * @param sampleName
	 * @param sampleDetail ���������趨���˵�״̬
	 * @param pileUpFile
	 */
	private void addPileupToLsSnpIndel(String sampleName, SnpGroupInfoFilter snpGroupInfoFilter, String pileUpFile) {
		String outPutFile = FileOperate.changeFileSuffix(pileUpFile, "_SnpInfo", "txt");
		TxtReadandWrite txtOut = new TxtReadandWrite(outPutFile, true);
		
		TxtReadandWrite txtReadPileUp = new TxtReadandWrite(pileUpFile, false);
		snpGroupInfoFilter.clearSampleName();
		snpGroupInfoFilter.addSampleName(sampleName);
		sampleFilter.clearSampleFilterInfo();
		sampleFilter.addSampleFilterInfo(snpGroupInfoFilter);
		int snpNum = 0;
		int allNum = 0;
		for (String pileupLines : txtReadPileUp.readlines()) {
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, sampleName);
			mapInfoSnpIndel.setSamToolsPilup(pileupLines);

			if (sampleFilter.isFilterdSnp(mapInfoSnpIndel)) {
				addSnp_2_mapSiteInfo2MapInfoSnpIndel(mapInfoSnpIndel);
				
				ArrayList<String[]> lsInfo = mapInfoSnpIndel.toStringLsSnp();
				for (String[] strings : lsInfo) {
					txtOut.writefileln(strings);
				}
				
				snpNum++;
				if (snpNum %100 == 0) {
					logger.info("�ҵ�" + snpNum + "��snp");
				}
			}
			allNum++;
			if (allNum %100000 == 0) {
				logger.info("ɨ���" + allNum + "��snp");
			}
		}
		txtOut.close();
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
	}
	
	/** ���趨snp������£���pileup�ļ��л�ȡsnp��Ϣ */
	public void readSnpDetailFromPileUp() {
		for (String[] sample2vcf : lsSample2VcfFiles) {
			addVcfToLsSnpIndel(sample2vcf[0], sample2vcf[1]);
		}
		for (String[] sample2NBCfile : lsSample2NBCfiles) {
			addNBCToLsSnpIndel(sample2NBCfile[0], sample2NBCfile[1]);
		}
		for (String[] sample2PileupFile : lsSample2PileUpFiles) {
			SnpGroupInfoFilter sampleDetail = setSample2SampleDetail.get(sample2PileupFile[0]);
			addPileupToLsSnpIndel(sample2PileupFile[0], sampleDetail, sample2PileupFile[1]);
		}
		HashMap<String, ArrayList<MapInfoSnpIndel>> mapInfoSnpIndel = MapInfoSnpIndel.sort_MapChrID2InfoSnpIndel(mapSiteInfo2MapInfoSnpIndel.values());
		for (String[] sample2PileUp : lsSample2SamPileupFile) {
			MapInfoSnpIndel.getSiteInfo_FromPileUp(sample2PileUp[0], mapInfoSnpIndel, sample2PileUp[1], gffChrAbs);
		}
	}
	/** ������readSnpDetailFromPileUp֮��ִ�� */
	public void filterSnp() {
		sampleFilter.clearSampleFilterInfo();
		for (SnpGroupInfoFilter snpGroupInfoFilter : lsSampleDetailCompare) {
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
		txtOut.writefileln(MapInfoSnpIndel.getTitleFromSampleName(setSample, true));
		HashSet<String> setSnpSite = null;
		for (int i = 0; i < lsFilteredSnp.size(); i++) {
			if (lsFilteredSite != null && lsFilteredSite.size() > 0) {
				setSnpSite = new HashSet<String>();
				ArrayList<SiteSnpIndelInfo> lsSiteSnpIndelInfos = lsFilteredSite.get(i);
				for (SiteSnpIndelInfo siteSnpIndelInfo : lsSiteSnpIndelInfos) {
					setSnpSite.add(siteSnpIndelInfo.getMismatchInfo());
				}
			}
			MapInfoSnpIndel mapInfoSnpIndel = lsFilteredSnp.get(i);
			ArrayList<String[]> lsResult = mapInfoSnpIndel.toStringLsSnp(setSample, false, setSnpSite);
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
	public void setDomainInfo(String txtExcelSNP, String domainFile, String outFile) {
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, 
				NovelBioConst.GENOME_PATH_UCSC_HG19_GFF_REFSEQ);
		DomainPfam.readDomain(domainFile);
		ArrayList<String[]> lsSnp = ExcelTxtRead.readLsExcelTxt(txtExcelSNP, 1);
		for (int i = 1; i < lsSnp.size(); i++) {
			String[] ss = lsSnp.get(i);
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs, chrID, refSnpIndelStart)//(0, ss[0], Integer.parseInt(ss[1]), ss[3], ss[4]);
			GffCodGene gffcod = gffHashGene.searchLocation(mapInfoSnpIndel.getRefID(), mapInfoSnpIndel.getRefSnpIndelStart());
			String tmp = "";
			if (gffcod.isInsideLoc()) {
				GffDetailGene gffDetailGene = gffcod.getGffDetailThis();
				for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
					DomainPfam domainPfam = DomainPfam.getDomainPfam(gffGeneIsoInfo.getName());
					if (domainPfam == null) {
						continue;
					}
					domainPfam.setAALoc(gffGeneIsoInfo.getCod2ATGmRNA(gffcod.getCoord())/3);
					tmp = domainPfam.toString();
					break;
				}
				
			}
			String result = ArrayOperate.cmbString(ss, "\t");
			result = result + "\t" + tmp;
			txtOut.writefileln(result);
		}
		txtOut.close();
	}
	
}
