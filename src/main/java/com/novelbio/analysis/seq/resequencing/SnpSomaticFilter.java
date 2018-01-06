package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 读取几个GATK的vcf结果文件，然后获得并集snp，并标记每个snp的信息，所在基因等等
 * @author zong0jie
 */
public class SnpSomaticFilter {
	private static final Logger logger = Logger.getLogger(SnpSomaticFilter.class);
//	GffChrAbs gffChrAbs;

	/** vcf的列 */
	VcfCols vcfCols = new VcfCols();
	/** 以下三个，都是从这些文本中获取snp的信息 */
	ArrayList<String[]> lsSample2VcfFiles = new ArrayList<String[]>();
	ArrayList<String[]> lsSample2NBCfiles = new ArrayList<String[]>();
	ArrayList<SnpCalling> lsSample2PileUpFiles = new ArrayList<SnpCalling>();

	/** 0：sampleName<br>
	 * 1：SampleFile  */
	ArrayList<String[]> lsSample2SamPileupFile = new ArrayList<String[]>();

	/** 用于多个样本的snp去冗余的，其中key表示该snp所在的起点信息，value就是该位点具体的snp情况
	 * key小写
	 *  */
	Map<String, RefSiteSnpIndel> mapSiteInfo2RefSiteSnpIndel = new ConcurrentHashMap<String, RefSiteSnpIndel>();
	
	/** 过滤后的snpSite，包含某个位点的所有信息 */
	ArrayList<RefSiteSnpIndel> lsFilteredRefSite = new ArrayList<RefSiteSnpIndel>();
	/**每个位点对应的causal snp
	 * 某个位点仅包含通过过滤的snp位点  */
	ArrayList<RefSiteSnpIndel> lsFilteredRefSnp = new ArrayList<RefSiteSnpIndel>();
	
	/** 用来过滤样本的 */
	SnpFilter snpFilterSamples = new SnpFilter();
	
	boolean getVCFflag = false;
	
	/** 判定为snp Heto所含有的snp比例不得小于该数值 */
	public void setSnp_Heto_Contain_SnpProp_Min(double snp_Hete_Contain_SnpProp_Min) {
		snpFilterSamples.setSnp_Hete_Contain_SnpProp_Min(snp_Hete_Contain_SnpProp_Min);
	}
	
	/** 判定为snp Heto所含有的snp比例不得小于该数值 */
	public void setSnp_HetoMore_Contain_SnpProp_Min(double snp_HetoMore_Contain_SnpProp_Min) {
		snpFilterSamples.setSnp_HetoMore_Contain_SnpProp_Min(snp_HetoMore_Contain_SnpProp_Min);
	}
	
	public void addSnpFromVcfFile(String sampleName, String vcfFile) {
		lsSample2VcfFiles.add(new String[]{sampleName, vcfFile});
	}
	
	public void addSnpFromNBCfile(String sampleName, String nbcFile) {
		lsSample2NBCfiles.add(new String[]{sampleName, nbcFile});
	}
	
	public void addSnpFromPileUpFile(String sampleName, SnpLevel snpLevel, String pileUpfile) {
		SnpCalling snpCalling = new SnpCalling();
		snpCalling.setMapSiteInfo2RefSiteSnpIndel(mapSiteInfo2RefSiteSnpIndel);
		snpCalling.setSnpLevel(snpLevel);
		snpCalling.addSnpFromPileUpFile(sampleName, pileUpfile, FileOperate.changeFileSuffix(pileUpfile, "_outSnp", "txt"));
		lsSample2PileUpFiles.add(snpCalling);
	}
	
	/** 在这些pileUp的文件中找已有的snp的具体细节 */
	public void addSampileupFile(String sampleName, String sampileupFile) {
		lsSample2SamPileupFile.add(new String[]{sampleName, sampileupFile});
	}
	
	/** 过滤样本的具体信息 */
	public void addFilterGroup(SnpGroupFilterInfo snpGroupFilterInfo) {
		snpFilterSamples.addSampleFilterInfo(snpGroupFilterInfo);
	}
	
	/** 在设定snp文件的情况下，从pileup文件中获取snp信息
	 * 只要设定好snp文件即可，内部自动做snp calling
	 *  */
	public void readSnpDetailFromFile() {
		readSnpFromFile_To_MapSiteInfo2RefSiteSnpIndel();
		getSnpDetail(mapSiteInfo2RefSiteSnpIndel.values());
		lsFilteredRefSite = ArrayOperate.getArrayListValue(mapSiteInfo2RefSiteSnpIndel);
	}
	
	/** 看一下这个有没有设定vcf的flag */
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
			//从pileUp中获取snp的方法
			//将pileUp的snp信息加入mapSiteInfo2RefSiteSnpIndel中
			snpCalling.run();
		}
	}
	
	/**
	 * 将gatk里面vcf文件中的snp信息加入mapSiteInfo2RefSiteSnpIndel中
	 */
	private void addVcf_To_MapSiteInfo2RefSiteSnpIndel(String sampleName, String vcfFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(vcfFile, false);
		for (String vcfLines : txtRead.readlines()) {
			if (vcfLines.startsWith("#")) continue;
			String[] ss = vcfLines.split("\t");
			
			try {Integer.parseInt(ss[vcfCols.colSnpStart]); } catch (Exception e) { continue; }
			
			RefSiteSnpIndel refSiteSnpIndel = new RefSiteSnpIndel(sampleName);
			refSiteSnpIndel.setVcfLines(sampleName, vcfCols, vcfLines);
			
			addSnp_2_mapSiteInfo2RefSiteSnpIndel(refSiteSnpIndel);
		}
		txtRead.close();
	}
	
	/**
	 * 将gatk里面vcf文件中的snp信息加入mapSiteInfo2RefSiteSnpIndel中
	 */
	private void addNBC_To_MapSiteInfo2RefSiteSnpIndel(String sampleName, String novelbioFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(novelbioFile, false);
		for (String vcfLines : txtRead.readlines()) {
			if (vcfLines.startsWith("#")) continue;
			String[] ss = vcfLines.split("\t");
			
			try {Integer.parseInt(ss[vcfCols.colSnpStart]); } catch (Exception e) { continue; }
			
			RefSiteSnpIndel refSiteSnpIndel = new RefSiteSnpIndel(sampleName);
			refSiteSnpIndel.setNBCLines(sampleName, vcfLines);
			addSnp_2_mapSiteInfo2RefSiteSnpIndel(refSiteSnpIndel);
		}
		txtRead.close();
	}
	
	private void addSnp_2_mapSiteInfo2RefSiteSnpIndel(RefSiteSnpIndel refSiteSnpIndel) {
		String key = refSiteSnpIndel.getKeySiteInfo();
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
		snpDetailGet.setMapChrID2InfoSnpIndel(colRefSiteSnpIndels);
		for (String[] sample2PileUp : lsSample2SamPileupFile) {
			snpDetailGet.addSample2PileupFile(sample2PileUp[0], sample2PileUp[1]);
		}
		snpDetailGet.run();
	}
	
	/** 必须在readSnpDetailFromPileUp之后执行 */
	public void filterSnp() {
		lsFilteredRefSite.clear();
		lsFilteredRefSnp.clear();
		for (RefSiteSnpIndel refSiteSnpIndel : mapSiteInfo2RefSiteSnpIndel.values()) {
			ArrayList<SnpRefAltInfo> lsSiteSnpIndelInfo = snpFilterSamples.getFilterdSnp(refSiteSnpIndel);
			if (lsSiteSnpIndelInfo.size() > 0) {
				lsFilteredRefSite.add(refSiteSnpIndel);
				RefSiteSnpIndel reSiteSnpIndelFiltered = refSiteSnpIndel.clone();
				reSiteSnpIndelFiltered.setLsSiteSnpIndelInfo(lsSiteSnpIndelInfo);
				lsFilteredRefSnp.add(reSiteSnpIndelFiltered);
			}
		}
	}
	
	/**
	 * 返回筛选过的site位点
	 * 没有用fiterSnp方法，返回全体call出来的位点
	 * 用fiterSnp方法，返回通过质检的位点，位点中含有全部snp情况
	 * @return
	 */
	public ArrayList<RefSiteSnpIndel> getLsFilteredSite() {
		return lsFilteredRefSite;
	}
	
	/**
	 * 返回筛选过的snp位点
	 * 没有用filterSnp方法，为空
	 * 用filterSnp方法，返回通过质检的位点，位点中仅含有causal snp情况
	 * @return
	 */
	public ArrayList<RefSiteSnpIndel> getLsFilteredSnp() {
		return lsFilteredRefSnp;
	}
	/**
	 * 返回本次筛选中涉及到的样本名
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
	 * 讲过滤后的结果写入文本。
	 * 如果没有过滤只运行了readSnpDetailFromFile，那就将读取的detail写入文本
	 * @param txtFile
	 */
	public void writeToFile(GffChrAbs gffChrAbs, boolean simpleTable, String txtFile) {
		LinkedHashSet<String> setSample = getSetSampleName();
		
		TxtReadandWrite txtOut = new TxtReadandWrite(txtFile, true);
		txtOut.writefileln(RefSiteSnpIndel.getTitleFromSampleName(setSample, getVCFflag, true));
		//优先写入过滤后的snp位点
		ArrayList<RefSiteSnpIndel> lsWriteIn = lsFilteredRefSnp;
		if (lsFilteredRefSnp == null || lsFilteredRefSnp.size() == 0) {
			lsWriteIn = lsFilteredRefSite;
		}
		for (RefSiteSnpIndel refSiteSnpIndel : lsWriteIn) {
			refSiteSnpIndel.setGffChrAbs(gffChrAbs);
			ArrayList<String[]> lsResult = refSiteSnpIndel.toStringLsSnp(setSample, getVCFflag, true);
			refSiteSnpIndel.setGffChrAbs(null);
			for (String[] strings : lsResult) {
				txtOut.writefileln(strings);
			}
		}
		txtOut.close();
	}
	
	/** 重置过滤的样本信息 */
	public void clearGroupFilterInfo() {
		snpFilterSamples.clearGroupFilterInfo();
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

}
