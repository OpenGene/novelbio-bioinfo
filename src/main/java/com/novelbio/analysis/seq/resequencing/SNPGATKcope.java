package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.poi.ss.util.SSCellRange;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGeneRefSeq;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;
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
		String parentPath = "/media/winF/NBC/Project/Project_HXW/20120705/";
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		snpgatKcope.setGffChrAbs(new GffChrAbs(9606));
		
		snpgatKcope.addVcfToLsSnpIndel("2A", parentPath + "2A_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addVcfToLsSnpIndel("2B", parentPath + "2B_SNPrecal_IndelFiltered.vcf");
		
		snpgatKcope.addSampileupFile("2A", parentPath + "2A_detailmpileup.txt");
		snpgatKcope.addSampileupFile("2B", parentPath + "2B_detailmpileup.txt");
		
		SnpGroupFilterInfo sampleDetail2A = new SnpGroupFilterInfo();
		sampleDetail2A.addSampleName("2A");
		sampleDetail2A.setSampleRefHomoNum(1, 1);
		sampleDetail2A.setSampleSnpIndelHetoNum(0, 0);
		sampleDetail2A.setSampleSnpIndelHomoNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail2A);
		
		SnpGroupFilterInfo sampleDetail2B = new SnpGroupFilterInfo();
		sampleDetail2B.addSampleName("2B");
		sampleDetail2B.setSampleRefHomoNum(0, 0);
		sampleDetail2B.setSampleSnpIndelNum(1, 1);
		sampleDetail2B.setSampleSnpIndelHetoLessNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail2B);
		
		snpgatKcope.readSnpDetailFromPileUp();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/result_withSampileup_2Bvs2A2.xls");
		
		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/result_withSampileup_2Bvs2A_filter2.xls");
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
		logger.error("tree map size: " +mapSiteInfo2MapInfoSnpIndel.size());
	}
	
	/** ���趨snp�ļ�������£���pileup�ļ��л�ȡsnp��Ϣ
	 * ֻҪ�趨��snp�ļ����ɣ��ڲ��Զ���snp calling
	 *  */
	public void readSnpDetailFromPileUp() {
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
			if (mapInfoSnpIndel.getRefID().equals("XM_003500889") && mapInfoSnpIndel.getRefSnpIndelStart() == 2736) {
				logger.error("stop");
			}
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
