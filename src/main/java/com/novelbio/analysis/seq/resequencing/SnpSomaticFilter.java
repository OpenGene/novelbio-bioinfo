package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
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
public class SnpSomaticFilter {
	private static final Logger logger = Logger.getLogger(SnpSomaticFilter.class);
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
	Map<String, RefSiteSnpIndel> mapSiteInfo2RefSiteSnpIndel = new TreeMap<String, RefSiteSnpIndel>();
	
	/** ���˺��snpSite������ĳ��λ���������Ϣ */
	ArrayList<RefSiteSnpIndel> lsFilteredRefSite = new ArrayList<RefSiteSnpIndel>();
	/**ÿ��λ���Ӧ��causal snp
	 * ĳ��λ�������ͨ�����˵�snpλ��  */
	ArrayList<RefSiteSnpIndel> lsFilteredRefSnp = new ArrayList<RefSiteSnpIndel>();
	
	/** �������������� */
	SnpFilter snpFilterSamples = new SnpFilter();

	/** ��������֮��Ƚϵ���Ϣ */
	ArrayList<SnpGroupFilterInfo> lsSampleDetailCompare = new ArrayList<SnpGroupFilterInfo>();
	
	boolean getVCFflag = false;
	
	/** �ж�Ϊsnp Heto�����е�snp��������С�ڸ���ֵ */
	public void setSnp_Hete_Contain_SnpProp_Min(double snp_Hete_Contain_SnpProp_Min) {
		snpFilterSamples.setSnp_Hete_Contain_SnpProp_Min(snp_Hete_Contain_SnpProp_Min);
	}
	
	/** �ж�Ϊsnp Heto�����е�snp��������С�ڸ���ֵ */
	public void setSnp_HetoMore_Contain_SnpProp_Min(double snp_HetoMore_Contain_SnpProp_Min) {
		snpFilterSamples.setSnp_HetoMore_Contain_SnpProp_Min(snp_HetoMore_Contain_SnpProp_Min);
	}
	
	public void addSnpFromVcfFile(String sampleName, String vcfFile) {
		lsSample2VcfFiles.add(new String[]{sampleName, vcfFile});
	}
	
	public void addSnpFromNBCfile(String sampleName, String nbcFile) {
		lsSample2NBCfiles.add(new String[]{sampleName, nbcFile});
	}
	
	public void addSnpFromPileUpFile(String sampleName, int snpLevel, String pileUpfile) {
		SnpCalling snpCalling = new SnpCalling();
		snpCalling.setGffChrAbs(gffChrAbs);
		snpCalling.setMapSiteInfo2RefSiteSnpIndel(mapSiteInfo2RefSiteSnpIndel);
		snpCalling.setSnpLevel(snpLevel);
		snpCalling.addSnpFromPileUpFile(sampleName, pileUpfile, FileOperate.changeFileSuffix(pileUpfile, "_outSnp", "txt"));
		lsSample2PileUpFiles.add(snpCalling);
	}
	
	/** ����ЩpileUp���ļ��������е�snp�ľ���ϸ�� */
	public void addSampileupFile(String sampleName, String sampileupFile) {
		lsSample2SamPileupFile.add(new String[]{sampleName, sampileupFile});
	}
	
	/** ���������ľ�����Ϣ */
	public void addFilterSample(SnpGroupFilterInfo snpGroupFilterInfo) {
		lsSampleDetailCompare.add(snpGroupFilterInfo);
	}
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	
	/** ���趨snp�ļ�������£���pileup�ļ��л�ȡsnp��Ϣ
	 * ֻҪ�趨��snp�ļ����ɣ��ڲ��Զ���snp calling
	 *  */
	public void readSnpDetailFromFile() {
		readSnpFromFile_To_MapSiteInfo2RefSiteSnpIndel();
		getSnpDetail(mapSiteInfo2RefSiteSnpIndel.values());
		lsFilteredRefSite = ArrayOperate.getArrayListValue(mapSiteInfo2RefSiteSnpIndel);
	}
	
	/** ��һ�������û���趨vcf��flag */
	public boolean getVCFflag() {
		return getVCFflag;
	}
	
	private void readSnpFromFile_To_MapSiteInfo2RefSiteSnpIndel() {
		mapSiteInfo2RefSiteSnpIndel.clear();
		if (lsSample2VcfFiles.size() > 0) {
			getVCFflag = true;
		}
		
		for (String[] sample2vcf : lsSample2VcfFiles) {
			addVcf_To_MapSiteInfo2RefSiteSnpIndel(sample2vcf[0], sample2vcf[1]);
		}
		for (String[] sample2NBCfile : lsSample2NBCfiles) {
			addNBC_To_MapSiteInfo2RefSiteSnpIndel(sample2NBCfile[0], sample2NBCfile[1]);
		}
		for (SnpCalling snpCalling : lsSample2PileUpFiles) {
			//��pileUp�л�ȡsnp�ķ���
			//��pileUp��snp��Ϣ����mapSiteInfo2RefSiteSnpIndel��
			snpCalling.run();
		}
	}
	
	/**
	 * ��gatk����vcf�ļ��е�snp��Ϣ����mapSiteInfo2RefSiteSnpIndel��
	 */
	private void addVcf_To_MapSiteInfo2RefSiteSnpIndel(String sampleName, String vcfFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(vcfFile, false);
		for (String vcfLines : txtRead.readlines()) {
			if (vcfLines.startsWith("#")) continue;
			String[] ss = vcfLines.split("\t");
			
			try {Integer.parseInt(ss[vcfCols.colSnpStart]); } catch (Exception e) { continue; }
			
			RefSiteSnpIndel refSiteSnpIndel = new RefSiteSnpIndel(gffChrAbs, sampleName);
			refSiteSnpIndel.setVcfLines(sampleName, vcfCols, vcfLines);
			
			addSnp_2_mapSiteInfo2RefSiteSnpIndel(refSiteSnpIndel);
		}
	}
	
	/**
	 * ��gatk����vcf�ļ��е�snp��Ϣ����mapSiteInfo2RefSiteSnpIndel��
	 */
	private void addNBC_To_MapSiteInfo2RefSiteSnpIndel(String sampleName, String novelbioFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(novelbioFile, false);
		for (String vcfLines : txtRead.readlines()) {
			if (vcfLines.startsWith("#")) continue;
			String[] ss = vcfLines.split("\t");
			
			try {Integer.parseInt(ss[vcfCols.colSnpStart]); } catch (Exception e) { continue; }
			
			RefSiteSnpIndel refSiteSnpIndel = new RefSiteSnpIndel(gffChrAbs, sampleName);
			refSiteSnpIndel.setNBCLines(sampleName, vcfLines);
			addSnp_2_mapSiteInfo2RefSiteSnpIndel(refSiteSnpIndel);
		}
	}
	
	private void addSnp_2_mapSiteInfo2RefSiteSnpIndel(RefSiteSnpIndel refSiteSnpIndel) {
		String key = refSiteSnpIndel.getRefID() + SepSign.SEP_ID + refSiteSnpIndel.getRefSnpIndelStart();
		if (mapSiteInfo2RefSiteSnpIndel.containsKey(key)) {
			RefSiteSnpIndel maInfoSnpIndelExist = mapSiteInfo2RefSiteSnpIndel.get(key);
			maInfoSnpIndelExist.addAllenInfo(refSiteSnpIndel);
			return;
		}
		else {
			mapSiteInfo2RefSiteSnpIndel.put(key, refSiteSnpIndel);
		}
	}
	
	private void getSnpDetail(Collection<RefSiteSnpIndel> colRefSiteSnpIndels) {
		SnpDetailGet snpDetailGet = new SnpDetailGet();
		snpDetailGet.setGffChrAbs(gffChrAbs);
		snpDetailGet.setMapChrID2InfoSnpIndel(colRefSiteSnpIndels);
		for (String[] sample2PileUp : lsSample2SamPileupFile) {
			snpDetailGet.addSample2PileupFile(sample2PileUp[0], sample2PileUp[1]);
		}
		snpDetailGet.run();
	}
	
	
	/** ������readSnpDetailFromPileUp֮��ִ�� */
	public void filterSnp() {
		snpFilterSamples.clearSampleFilterInfo();
		for (SnpGroupFilterInfo snpGroupInfoFilter : lsSampleDetailCompare) {
			snpFilterSamples.addSampleFilterInfo(snpGroupInfoFilter);
		}
		
		lsFilteredRefSite.clear();
		lsFilteredRefSnp.clear();
		for (RefSiteSnpIndel refSiteSnpIndel : mapSiteInfo2RefSiteSnpIndel.values()) {
			ArrayList<SiteSnpIndelInfo> lsSiteSnpIndelInfo = snpFilterSamples.getFilterdSnp(refSiteSnpIndel);
			if (lsSiteSnpIndelInfo.size() > 0) {
				lsFilteredRefSite.add(refSiteSnpIndel);
				RefSiteSnpIndel reSiteSnpIndelFiltered = refSiteSnpIndel.clone();
				reSiteSnpIndelFiltered.setLsSiteSnpIndelInfo(lsSiteSnpIndelInfo);
				lsFilteredRefSnp.add(reSiteSnpIndelFiltered);
			}
		}
	}
	
	/**
	 * ����ɸѡ����siteλ��
	 * û����fiterSnp����������ȫ��call������λ��
	 * ��fiterSnp����������ͨ���ʼ��λ�㣬λ���к���ȫ��snp���
	 * @return
	 */
	public ArrayList<RefSiteSnpIndel> getLsFilteredSite() {
		return lsFilteredRefSite;
	}
	/**
	 * ����ɸѡ����snpλ��
	 * û����fiterSnp������Ϊ��
	 * ��fiterSnp����������ͨ���ʼ��λ�㣬λ���н�����causal snp���
	 * @return
	 */
	public ArrayList<RefSiteSnpIndel> getLsFilteredSnp() {
		return lsFilteredRefSnp;
	}
	/**
	 * ���ر���ɸѡ���漰����������
	 * @return
	 */
	public LinkedHashSet<String> getSetSampleName() {
		LinkedHashSet<String> setSample = new LinkedHashSet<String>();
		for (String[] strings : lsSample2VcfFiles) {
			setSample.add(strings[0]);
		}
		for (String[] strings : lsSample2NBCfiles) {
			setSample.add(strings[0]);
		}
		for (String[] strings : lsSample2SamPileupFile) {
			setSample.add(strings[0]);
		}
		return setSample;
	}
	
	/**
	 * �����˺�Ľ��д���ı���
	 * ���û�й���ֻ������readSnpDetailFromFile���Ǿͽ���ȡ��detailд���ı�
	 * @param txtFile
	 */
	public void writeToFile(String txtFile) {
		LinkedHashSet<String> setSample = getSetSampleName();
		
		TxtReadandWrite txtOut = new TxtReadandWrite(txtFile, true);
		txtOut.writefileln(RefSiteSnpIndel.getTitleFromSampleName(setSample));
		//����д����˺��snpλ��
		ArrayList<RefSiteSnpIndel> lsWriteIn = lsFilteredRefSnp;
		if (lsFilteredRefSnp == null || lsFilteredRefSnp.size() == 0) {
			lsWriteIn = lsFilteredRefSite;
		}
		for (int i = 0; i < lsWriteIn.size(); i++) {
			RefSiteSnpIndel refSiteSnpIndel = lsFilteredRefSite.get(i);
			ArrayList<String[]> lsResult = refSiteSnpIndel.toStringLsSnp(setSample, false, getVCFflag);
			for (String[] strings : lsResult) {
				txtOut.writefileln(strings);
			}
		}
		txtOut.close();
	}
	
	/** ���ù��˵�������Ϣ */
	public void clearSampleFilterInfo() {
		lsSampleDetailCompare.clear();
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

}
