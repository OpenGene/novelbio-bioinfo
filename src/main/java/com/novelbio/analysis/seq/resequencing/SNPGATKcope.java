package com.novelbio.analysis.seq.resequencing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.generalConf.NovelBioConst;
/**
 * 读取几个GATK的vcf结果文件，然后获得并集snp，并标记每个snp的信息，所在基因等等
 * @author zong0jie
 */
public class SNPGATKcope {
	Logger logger = Logger.getLogger(SNPGATKcope.class);
	GffChrAbs gffChrAbs;

	/** vcf的列 */
	VcfCols vcfCols = new VcfCols();
	ArrayList<String[]> lsSample2VcfFiles = new ArrayList<String[]>();
	/** 0：sampleName<br>
	 * 1：SampleFile  */
	ArrayList<String[]> lsSample2SamPileupFile = new ArrayList<String[]>();
	/**多个vcf文件的并集snp */
	ArrayList<MapInfoSnpIndel> lsUnionSnp = new ArrayList<MapInfoSnpIndel>();
	/** 用于多个样本的snp去冗余的，其中key表示该snp所在的起点信息，value就是该位点具体的snp情况 */
	HashMap<String, MapInfoSnpIndel> mapSiteInfo2MapInfoSnpIndel = new HashMap<String, MapInfoSnpIndel>();
	
	/** 用来过滤样本的 */
	SnpSampleFilter sampleFilter = new SnpSampleFilter();
	/**过滤获得的causal snp */
	ArrayList<SiteSnpIndelInfo> lsFilteredSite;
	
	
	public static void main(String[] args) {
		String parentPath = "/media/winF/NBC/Project/Project_HXW/20120705/";
		SNPGATKcope snpgatKcope = new SNPGATKcope();
		snpgatKcope.setGffChrAbs(new GffChrAbs(9606));
		
//		snpgatKcope.addVcfFile("2A", parentPath + "2A_SNPrecal_IndelFiltered.vcf");
//		snpgatKcope.addVcfFile("2B", parentPath + "2B_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addVcfFile("3A", parentPath + "3A_SNPrecal_IndelFiltered.vcf");
		snpgatKcope.addVcfFile("3B", parentPath + "3B_SNPrecal_IndelFiltered.vcf");
//		snpgatKcope.addSampileupFile("2A", parentPath + "2A_piluptest.txt");
//		snpgatKcope.addSampileupFile("2B", parentPath + "2B_piluptest.txt");
//		snpgatKcope.addSampileupFile("2A", parentPath + "2A_detailmpileup.txt");
//		snpgatKcope.addSampileupFile("2B", parentPath + "2B_detailmpileup.txt");
		snpgatKcope.addSampileupFile("3A", parentPath + "3A_detailmpileup.txt");
		snpgatKcope.addSampileupFile("3B", parentPath + "3B_detailmpileup.txt");
		
		SampleDetail sampleDetail2A = new SampleDetail();
		sampleDetail2A.addSampleName("2A");
		sampleDetail2A.setSampleRefHomoNum(1, 1);
		sampleDetail2A.setSampleSnpIndelHetoNum(0, 0);
		sampleDetail2A.setSampleSnpIndelHomoNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail2A);
		
		SampleDetail sampleDetail2B = new SampleDetail();
		sampleDetail2B.addSampleName("2B");
		sampleDetail2B.setSampleRefHomoNum(0, 0);
		sampleDetail2B.setSampleSnpIndelNum(1, 1);
		sampleDetail2B.setSampleSnpIndelHetoLessNum(0, 0);
		snpgatKcope.addFilterSample(sampleDetail2B);
		snpgatKcope.execute();
//		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/result_withSampileup_3Bvs3A.xls");
		
		snpgatKcope.filterSnp();
		snpgatKcope.writeToFile("/media/winF/NBC/Project/Project_HXW/result_withSampileup_3Bvs3A_filter.xls");
		
	}

	public void addVcfFile(String sampleName, String vcfFile) {
		lsSample2VcfFiles.add(new String[]{sampleName, vcfFile});
	}
	public void addSampileupFile(String sampleName, String sampileupFile) {
		lsSample2SamPileupFile.add(new String[]{sampleName, sampileupFile});
	}
	/** 重置过滤的样本信息 */
	public void clearSampleFilterInfo() {
		sampleFilter.clearSampleFilterInfo();
	}
	/** 过滤样本的具体信息 */
	public void addFilterSample(SampleDetail sampleDetail) {
		sampleFilter.addSampleFilterInfo(sampleDetail);
	}
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void execute() {
		lsFilteredSite = null;

		for (String[] sample2vcf : lsSample2VcfFiles) {
			addVcfToLsSnpIndel(sample2vcf[0], sample2vcf[1]);
		}
		HashMap<String, ArrayList<MapInfoSnpIndel>> mapInfoSnpIndel = MapInfoSnpIndel.sort_MapChrID2InfoSnpIndel(lsUnionSnp);
		for (String[] sample2PileUp : lsSample2SamPileupFile) {
			MapInfoSnpIndel.getSiteInfo(sample2PileUp[0], mapInfoSnpIndel, sample2PileUp[1], gffChrAbs);
		}
	}
	/**
	 * 将gatk里面vcf文件中的snp信息加入mapSiteInfo2MapInfoSnpIndel中
	 */
	private void addVcfToLsSnpIndel(String sampleName, String vcfFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(vcfFile, false);
		for (String vcfLines : txtRead.readlines()) {
			if (vcfLines.startsWith("#")) continue;
			String[] ss = vcfLines.split("\t");
			
			try {Integer.parseInt(ss[vcfCols.colSnpStart]); } catch (Exception e) { continue; }
			
			MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs);
			mapInfoSnpIndel.setSampleName(sampleName);
			mapInfoSnpIndel.setVcfLines(sampleName, vcfCols, vcfLines);
			
			String key = mapInfoSnpIndel.getRefID() + SepSign.SEP_ID + mapInfoSnpIndel.getRefSnpIndelStart();
			if (mapSiteInfo2MapInfoSnpIndel.containsKey(key)) {
				MapInfoSnpIndel maInfoSnpIndelExist = mapSiteInfo2MapInfoSnpIndel.get(key);
				maInfoSnpIndelExist.addAllenInfo(mapInfoSnpIndel);
				continue;
			}
			else {
				mapSiteInfo2MapInfoSnpIndel.put(key, mapInfoSnpIndel);
				lsUnionSnp.add(mapInfoSnpIndel);
			}
		}
	}
	/** 必须在execute之后执行 */
	public void filterSnp() {
		lsFilteredSite = new ArrayList<SiteSnpIndelInfo>();
		ArrayList<MapInfoSnpIndel> lsFilteredSnp = new ArrayList<MapInfoSnpIndel>();
		for (MapInfoSnpIndel mapInfoSnpIndel : lsUnionSnp) {
			SiteSnpIndelInfo siteSnpIndelInfo = sampleFilter.isFilterdSnp(mapInfoSnpIndel);
			if (siteSnpIndelInfo != null) {
				lsFilteredSnp.add(mapInfoSnpIndel);
				lsFilteredSite.add(siteSnpIndelInfo);
			}
		}
		lsUnionSnp = lsFilteredSnp;
	}
	public void writeToFile(String txtFile) {
		ArrayList<String> lsSample = new ArrayList<String>();
		for (String[] strings : lsSample2VcfFiles) {
			lsSample.add(strings[0]);
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(txtFile, true);
		txtOut.writefileln(MapInfoSnpIndel.getTitleFromSampleName(lsSample, true));
		SiteSnpIndelInfo siteSnpIndelInfo = null;
		for (int i = 0; i < lsUnionSnp.size(); i++) {
			if (lsFilteredSite != null)
				siteSnpIndelInfo = lsFilteredSite.get(i);
				
			MapInfoSnpIndel mapInfoSnpIndel = lsUnionSnp.get(i);
			ArrayList<String[]> lsResult = mapInfoSnpIndel.toStringLsSnp(lsSample, false, siteSnpIndelInfo);
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
