package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;

/**
 * 读取几个GATK的vcf结果文件，然后获得并集snp，并标记每个snp的信息，所在基因等等
 * @author zong0jie
 */
public class SNPGATKcope {
	Logger logger = Logger.getLogger(SNPGATKcope.class);
	GffChrAbs gffChrAbs;

	/** vcf的列 */
	VcfCols vcfCols = new VcfCols();
	/** 以下三个，都是从这些文本中获取snp的信息 */
	ArrayList<String[]> lsSample2VcfFiles = new ArrayList<String[]>();
	ArrayList<String[]> lsSample2NBCfiles = new ArrayList<String[]>();
	ArrayList<SnpCalling> lsSample2PileUpFiles = new ArrayList<SnpCalling>();

	/** 0：sampleName<br>
	 * 1：SampleFile  */
	ArrayList<String[]> lsSample2SamPileupFile = new ArrayList<String[]>();

	/** 用于多个样本的snp去冗余的，其中key表示该snp所在的起点信息，value就是该位点具体的snp情况 */
	Map<String, RefSiteSnpIndel> mapSiteInfo2RefSiteSnpIndel = new TreeMap<String, RefSiteSnpIndel>();
	/**过滤后的snp */
	ArrayList<RefSiteSnpIndel> lsFilteredSnp = new ArrayList<RefSiteSnpIndel>();
	/**每个位点对应的causal snp
	 * 一个位点可能存在多个snp，所以装在list里面  */
	ArrayList<ArrayList<SiteSnpIndelInfo>> lsFilteredSite = new ArrayList<ArrayList<SiteSnpIndelInfo>>();
	
	/** 用来过滤样本的 */
	SnpFilter sampleFilter = new SnpFilter();

	/** 多组样本之间比较的信息 */
	ArrayList<SnpGroupFilterInfo> lsSampleDetailCompare = new ArrayList<SnpGroupFilterInfo>();
	
	/** 判定为snp Heto所含有的snp比例不得小于该数值 */
	public void setSnp_Hete_Contain_SnpProp_Min(double snp_Hete_Contain_SnpProp_Min) {
		sampleFilter.setSnp_Hete_Contain_SnpProp_Min(snp_Hete_Contain_SnpProp_Min);
	}
	
	/** 判定为snp Heto所含有的snp比例不得小于该数值 */
	public void setSnp_HetoMore_Contain_SnpProp_Min(double snp_HetoMore_Contain_SnpProp_Min) {
		sampleFilter.setSnp_HetoMore_Contain_SnpProp_Min(snp_HetoMore_Contain_SnpProp_Min);
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
	
	/** 在这些pileUp的文件中找已有的snp的具体细节 */
	public void addSampileupFile(String sampleName, String sampileupFile) {
		lsSample2SamPileupFile.add(new String[]{sampleName, sampileupFile});
	}
	/** 重置过滤的样本信息 */
	public void clearSampleFilterInfo() {
		lsSampleDetailCompare.clear();
	}
	/** 过滤样本的具体信息 */
	public void addFilterSample(SnpGroupFilterInfo snpGroupFilterInfo) {
		lsSampleDetailCompare.add(snpGroupFilterInfo);
	}
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 将gatk里面vcf文件中的snp信息加入mapSiteInfo2RefSiteSnpIndel中
	 */
	private void addVcfToLsSnpIndel(String sampleName, String vcfFile) {
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
	 * 将gatk里面vcf文件中的snp信息加入mapSiteInfo2RefSiteSnpIndel中
	 */
	private void addNBCToLsSnpIndel(String sampleName, String novelbioFile) {
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
	
	/** 在设定snp文件的情况下，从pileup文件中获取snp信息
	 * 只要设定好snp文件即可，内部自动做snp calling
	 *  */
	public void readSnpDetailFromFile() {
		for (String[] sample2vcf : lsSample2VcfFiles) {
			addVcfToLsSnpIndel(sample2vcf[0], sample2vcf[1]);
		}
		for (String[] sample2NBCfile : lsSample2NBCfiles) {
			addNBCToLsSnpIndel(sample2NBCfile[0], sample2NBCfile[1]);
		}
		addPileupToLsSnpIndel();
		getSnpDetail(mapSiteInfo2RefSiteSnpIndel.values());
		lsFilteredSnp = ArrayOperate.getArrayListValue(mapSiteInfo2RefSiteSnpIndel);
	}
	
	/** 
	 * 不从vcf，而是从pileUp中获取snp的方法
	 * 将pileUp的snp信息加入mapSiteInfo2RefSiteSnpIndel中
	 * 同时导出一份snp的信息表
	 */
	private void addPileupToLsSnpIndel() {
		for (SnpCalling snpCalling : lsSample2PileUpFiles) {
			snpCalling.run();
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
	/** 必须在readSnpDetailFromPileUp之后执行 */
	public void filterSnp() {
		sampleFilter.clearSampleFilterInfo();
		for (SnpGroupFilterInfo snpGroupInfoFilter : lsSampleDetailCompare) {
			sampleFilter.addSampleFilterInfo(snpGroupInfoFilter);
		}
		
		lsFilteredSite.clear();
		lsFilteredSnp.clear();
		for (RefSiteSnpIndel refSiteSnpIndel : mapSiteInfo2RefSiteSnpIndel.values()) {
			ArrayList<SiteSnpIndelInfo> lsSiteSnpIndelInfo = sampleFilter.getFilterdSnp(refSiteSnpIndel);
			if (lsSiteSnpIndelInfo.size() > 0) {
				lsFilteredSnp.add(refSiteSnpIndel);
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
		txtOut.writefileln(RefSiteSnpIndel.getTitleFromSampleName(setSample));
		ArrayList<SiteSnpIndelInfo> lsSiteSnpIndelInfos = new ArrayList<SiteSnpIndelInfo>();
		for (int i = 0; i < lsFilteredSnp.size(); i++) {
			if (lsFilteredSite != null && lsFilteredSite.size() > 0) {
				lsSiteSnpIndelInfos = lsFilteredSite.get(i);
			}
			RefSiteSnpIndel refSiteSnpIndel = lsFilteredSnp.get(i);
			ArrayList<String[]> lsResult = refSiteSnpIndel.toStringLsSnp(setSample, false, lsSiteSnpIndelInfos);
			for (String[] strings : lsResult) {
				txtOut.writefileln(strings);
			}
		}
		txtOut.close();
	}
	
	
	
	/**
	 * 给定文本，和domain信息，获得具体domain的信息
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
