package com.novelbio.software.tssplot;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.gff.ExonInfo;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.gff.ListGff;
import com.novelbio.bioinfo.gff.GffGene.GeneStructure;
import com.novelbio.bioinfo.gffchr.GffChrAbs;
import com.novelbio.bioinfo.mappedreads.EnumMapNormalizeType;
import com.novelbio.bioinfo.mappedreads.MapReads;
import com.novelbio.bioinfo.mappedreads.RegionInfo;
import com.novelbio.bioinfo.mappedreads.RegionInfo.RegionInfoComparator;

import de.erichseifert.gral.util.GraphicsUtils;
/**
 * 绘制染色体分布的图
 * @author zong0jie
 *
 */
public class GffPlotChrome {
	private static final Logger logger = Logger.getLogger(GffPlotChrome.class);

	GffChrAbs gffChrAbs = new GffChrAbs();
	int maxresolution = 10000;
	MapReads mapReads;
	EnumMapNormalizeType mapNormType = EnumMapNormalizeType.allreads;
	
	int[] tssTesRegion;
	
	public GffPlotChrome() {
	}
	
	public GffPlotChrome(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/**
	 * 将GffChrAbs导入
	 * @param gffChrAbs
	 */
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	/** 如果需要看全基因组上的tss或tes分布情况，就需要设定该参数 */
	public void setTssRegion(int[] tssRegion) {
		this.tssTesRegion = tssRegion;
	}
	public void setMapReads(MapReads mapReads) {
		this.mapReads = mapReads;
	}
	/**
	 * 每隔多少位取样,如果设定为1，则算法会变化，然后会很精确
	 * @return
	 */
	public int getThisInv() {
		return mapReads.getBinNum();
	}
	/**
	 * 按照染色体数，统计每个染色体上总位点数，每个位点数， string[4] 0: chrID 1: readsNum 2: readsPipNum
	 * 3: readsPipMean
	 * @return
	 */
	public ArrayList<String[]> getChrLenInfo() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String> lsChrID = mapReads.getChrIDLs();
		for (String string : lsChrID) {
			String[] chrInfoTmp = new String[4];
			chrInfoTmp[0] = string;
			chrInfoTmp[1] = mapReads.getChrReadsNum(string) + "";
			chrInfoTmp[2] = mapReads.getChrReadsPipNum(string) + "";
			chrInfoTmp[3] = mapReads.getChrReadsPipMean(string) + "";
			lsResult.add(chrInfoTmp);
		}
		return lsResult;
	}
	
	/**
	 * 主要用于全基因组做图的，所以结果不按照基因方向进行颠倒
	 * @param geneStructure
	 * @param gffDetailGene1
	 * @param num 具体第几个，譬如马红就想看第一个内含子或者第一个外显子 小于等于0表示看全体
	 * @return
	 */
	public void setFilterChrDistInfo(GeneStructure geneStructure, int num) {
		if (geneStructure == GeneStructure.ALLLENGTH) {
			mapReads.setMapChrID2LsAlignments(null);
			return;
		}
		
		GffHashGene gffHashGene = gffChrAbs.getGffHashGene();
		HashMap<String, List<? extends Alignment>> mapChrID2LsAlignment = new HashMap<String, List<? extends Alignment>>();
		for (String chrID : gffHashGene.getMapChrID2LsGff().keySet()) {
			List<RegionInfo> lsAlignment = new ArrayList<RegionInfo>();
			ListGff listGff = gffHashGene.getMapChrID2LsGff().get(chrID.toLowerCase());
			for (GffGene gffDetailGene : listGff) {
				lsAlignment.addAll(getGeneStructureRangeForChrPlot(geneStructure, gffDetailGene, num));
			}
			
			RegionInfoComparator comparator = new RegionInfoComparator();
			comparator.setCompareType(RegionInfoComparator.COMPARE_LOCSITE);
			comparator.setMin2max(true);
			Collections.sort(lsAlignment, comparator);
			mapChrID2LsAlignment.put(chrID.toLowerCase(), lsAlignment);
		}
		mapReads.setMapChrID2LsAlignments(mapChrID2LsAlignment);
	}
	/**
	 * 主要用于全基因组做图的，所以结果不按照基因方向进行颠倒
	 * @param geneStructure
	 * @param gffDetailGene
	 * @param num 具体第几个，譬如马红就想看第一个内含子或者第一个外显子 小于等于0表示看全体
	 * @return
	 */
	private ArrayList<RegionInfo> getGeneStructureRangeForChrPlot(GeneStructure geneStructure, GffGene gffDetailGene, int num) {
		GffIso gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		ArrayList<RegionInfo> lsResult = new ArrayList<RegionInfo>();
		
		if (geneStructure == GeneStructure.TSS) {
			RegionInfo siteInfo = new RegionInfo(gffDetailGene.getRefID());
			if (gffGeneIsoInfo.isCis5to3()) {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTSSsite() + tssTesRegion[0], gffGeneIsoInfo.getTSSsite() + tssTesRegion[1]);
			} else {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTSSsite() - tssTesRegion[1], gffGeneIsoInfo.getTSSsite() - tssTesRegion[0]);
			}
			lsResult.add(siteInfo);
		}
		
		else if (geneStructure == GeneStructure.TES) {
			RegionInfo siteInfo = new RegionInfo(gffDetailGene.getRefID());
			if (gffGeneIsoInfo.isCis5to3()) {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTESsite() + tssTesRegion[0], gffGeneIsoInfo.getTESsite() + tssTesRegion[1]);
			} else {
				siteInfo.setStartEndLoc(gffGeneIsoInfo.getTESsite() - tssTesRegion[1], gffGeneIsoInfo.getTESsite() - tssTesRegion[0]);
			}
			lsResult.add(siteInfo);
		}
		
		else if (geneStructure == GeneStructure.EXON) {
			if (num <= 0) {
				for (ExonInfo exonInfo : gffGeneIsoInfo) {
					RegionInfo siteInfo = new RegionInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
					lsResult.add(siteInfo);
				}
			} else {
				if (gffGeneIsoInfo.size() > num) {
					RegionInfo siteInfo = new RegionInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(gffGeneIsoInfo.get(num - 1).getStartAbs(), gffGeneIsoInfo.get(num - 1).getEndAbs());
					lsResult.add(siteInfo);
				}
			}
		}
		
		else if (geneStructure == GeneStructure.INTRON) {
			if (num <= 0) {
				for (ExonInfo exonInfo : gffGeneIsoInfo.getLsIntron()) {
					RegionInfo siteInfo = new RegionInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
					lsResult.add(siteInfo);
				}
			} else {
				ArrayList<ExonInfo> lsIntron = gffGeneIsoInfo.getLsIntron();
				if (lsIntron.size() >= num) {
					RegionInfo siteInfo = new RegionInfo(gffDetailGene.getRefID());
					siteInfo.setStartEndLoc(lsIntron.get(num - 1).getStartAbs(), lsIntron.get(num - 1).getEndAbs());
					lsResult.add(siteInfo);
				}
			}
			
		} else if (geneStructure == GeneStructure.UTR5) {
			for (ExonInfo exonInfo : gffGeneIsoInfo.getUTR5seq()) {
				RegionInfo siteInfo = new RegionInfo(gffDetailGene.getRefID());
				siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
				lsResult.add(siteInfo);
			}
		} else if (geneStructure == GeneStructure.UTR3) {
			for (ExonInfo exonInfo : gffGeneIsoInfo.getUTR3seq()) {
				RegionInfo siteInfo = new RegionInfo(gffDetailGene.getRefID());
				siteInfo.setStartEndLoc(exonInfo.getStartAbs(), exonInfo.getEndAbs());
				lsResult.add(siteInfo);
			}
		}
		return lsResult;
	}
 
	/**
	 * 画出所有染色体上密度图
	 * 用java画
	 * @param outPathPrefix 输出文件夹+前缀
	 * @throws Exception
	 */
	public void plotAllChrDist(String outPathPrefix) {
		ArrayList<String[]> chrlengthInfo = gffChrAbs.getSeqHash().getChrLengthInfo();
		//find the longest chromosome's density
		double[] chrReads = getChrDensity(chrlengthInfo.get(chrlengthInfo.size() - 1)[0], maxresolution);
		double axisY = MathComput.median(chrReads, 95)*4;
		for (int i = chrlengthInfo.size() - 1; i >= 0; i--) {
			try {
				plotChrDist(chrlengthInfo.get(i)[0], maxresolution, axisY, FileOperate.changeFileSuffix(outPathPrefix, "_"+chrlengthInfo.get(i)[0], "png"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 给定染色体，返回该染色体上reads分布
	 * 
	 * @param chrID
	 *            第几个软色体
	 * @param maxresolution
	 *            最长分辨率
	 * @param axisY y轴边界
	 * @param outFileName 输出文件名，带后缀"_chrID"
	 * @throws Exception
	 */
	private void plotChrDist(String chrID, int maxresolution, double axisY, String outFileName) throws Exception {
		int[] resolution = gffChrAbs.getSeqHash().getChrRes(chrID, maxresolution);
		long chrLengthMax = gffChrAbs.getSeqHash().getChrLenMax();
		double interval = ((int)(chrLengthMax/30)/1000)*1000;
		long chrLength = gffChrAbs.getSeqHash().getChrLength(chrID);
		
		/////////////////////   plotScatter can only accept double data   //////////////////////////////
		double[] resolutionDoub = new double[resolution.length];
		for (int i = 0; i < resolution.length; i++) {
			resolutionDoub[i] = i;
		}
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		double[] chrReads = null;
		try {
			chrReads = getChrDensity(chrID.toLowerCase(), resolution.length);
		} catch (Exception e) {
			logger.error("出现未知chrID：" + chrID);
			return;
		}
		if (chrReads == null) {
			return;
		}
		
		PlotScatter plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_SCATTERPLOT);
		plotScatter.setAxisX(0, maxresolution);
		plotScatter.setAxisY(0, axisY);
		plotScatter.setMapNum2ChangeX(0, 0, resolution.length, chrLength, interval);

		DotStyle dotStyle = new DotStyle();
		Paint colorGradient = DotStyle.getGridentColor(GraphicsUtils.deriveDarker(Color.blue), Color.blue);
		dotStyle.setColor(colorGradient);
		dotStyle.setStyle(DotStyle.STYLE_AREA);
		plotScatter.addXY(resolutionDoub, chrReads, dotStyle);
		
		//////////////////添加边框///////////////////////////////
		DotStyle dotStyleBroad = new DotStyle();
		dotStyleBroad.setStyle(DotStyle.STYLE_LINE);
		dotStyleBroad.setColor(Color.RED);
		dotStyleBroad.setSize(DotStyle.SIZE_B);
		double[] xstart = new double[]{0,0}; double[] xend= new double[]{resolutionDoub[resolutionDoub.length-1], resolutionDoub[resolutionDoub.length-1]};
		double[] y = new double[]{0, axisY};
		plotScatter.addXY(xend, y, dotStyleBroad);
		plotScatter.addXY(xstart, y, dotStyleBroad.clone());
		//////////////////////////////////////////////////////////////
		
		plotScatter.setBg(Color.WHITE);
		plotScatter.setAlpha(false);
		//坐标轴mapping
//		plotScatter.setMapNum2ChangeY(0, 0, axisY, 500, 100);
		plotScatter.setTitle(chrID + " Reads Density", null);
		plotScatter.setTitleX("Chromosome Length", null, 0);
		plotScatter.setTitleY("Normalized Reads Counts", null, (int)axisY/5);
		
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_ML);
		
		plotScatter.saveToFile(outFileName, 10000, 1000);
	}

	/**
	 * 返回某条染色体上的reads情况，是密度图 主要用于基因组上，一条染色体上的reads情况
	 * 
	 * @param chrID
	 * @param binNum
	 *            分成几个区间
	 * @parm type 取样方法 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return 没有的话就返回null
	 */
	public double[] getChrDensity(String chrID, int binNum) {
		double[] tmpResult = mapReads.getReadsDensity(chrID, 0, 0, binNum);
		return tmpResult;
	}
}
