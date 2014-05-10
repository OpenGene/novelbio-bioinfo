package com.novelbio.analysis.seq.rnaseq;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.mapping.StrandSpecific;
import com.novelbio.analysis.seq.sam.AlignSeqReading;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.SepSign;
import com.novelbio.base.plot.ImageUtils;
import com.novelbio.database.model.species.Species;

/**
 * 重写RSeQC的junction saturation模块，因为那个会报错
 * @author zong0jie
 *
 */
public class JunctionSaturationJava implements AlignmentRecorder {
	public static void main(String[] args) {
		JunctionSaturationJava junctionSaturation = new JunctionSaturationJava();
		Species species = new Species(9606);
		species.setVersion("hg19_GRCh37");
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		junctionSaturation.setGffHashGene(gffChrAbs.getGffHashGene());
		AlignSeqReading alignSeqReading = new AlignSeqReading(new SamFile("/media/hdfs/nbCloud/public/test/RNASeqMap/human/RKOkd_accepted_hits.bam"));
		alignSeqReading.addAlignmentRecorder(junctionSaturation);
		alignSeqReading.run();
		junctionSaturation.setSavePath("/media/hdfs/nbCloud/public/test/RNASeqMap/human/saturation.png");
		junctionSaturation.plot();
		gffChrAbs.close();
	}
	ShuffleReads shuffleReads = new ShuffleReads();
	StrandSpecific specific = StrandSpecific.UNKNOWN;

	int times = 0; //大于多少次
	int nodeNum = 20; //画多少个点
	XYSeries xyExistJun = new XYSeries("Known Junctions");
	XYSeries xyNovelJun = new XYSeries("Novel Junctions");
	XYSeries xyAllJun = new XYSeries("All Junctions");
	
	/**
	 * junction类型--数量
	 * 数量0 数量
	 * 1 方向：0正向 1反向
	 */
	Map<String, int[]> mapJuncKnown2Num = new HashMap<>();
	/**
	 * junction类型--数量
	 * 数量0 数量
	 * 1 方向：0正向 1反向
	 */
	Map<String, int[]> mapJuncUnKnown2Num = new HashMap<>();
	
	String imageSavePath; //曲线图保存的路径

	GffHashGene gffHashGene;
	
	/** 设定链特异性 */
	public void setStrandSpecific(StrandSpecific specific) {
		this.specific = specific;
	}
	public void setGffHashGene(GffHashGene gffHashGene) {
		this.gffHashGene = gffHashGene;
	}
	/** 出现几次才算是junction */
	public void setTimes(int times) {
		this.times = times;
	}
	/** 画几个点 */
	public void setNodeNum(int nodeNum) {
		this.nodeNum = nodeNum;
	}
	/** 图片保存路径 */
	public void setSavePath(String imageSavePath) {
		this.imageSavePath = imageSavePath;
	}
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		List<Align> lsAlign = alignRecord.getAlignmentBlocks();
		if(lsAlign.size() < 2) return;
		for (int i = 1; i < lsAlign.size(); i++) {
			Align align = new Align(alignRecord.getRefID(), lsAlign.get(i-1).getEndAbs(), lsAlign.get(i).getStartAbs());
			shuffleReads.addAlign(align, alignRecord.isCis5to3(), specific);
		}
	}

	@Override
	public void summary() {
		fillChrId2Jun();
		shuffleReads.summary();
		statisticJunctionInfo();
	}

	/** 填充已知junction的信息 */
	private void fillChrId2Jun() {
		boolean considerStrand = false;
		if (specific == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND || specific == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
			considerStrand = true;
		}
		for (GffDetailGene gffDetailGene : gffHashGene.getGffDetailAll()) {
			for (GffGeneIsoInfo iso : gffDetailGene.getLsCodSplit()) {
				for (int i = 1; i < iso.size(); i++) {
					String jun = getJunInfo(new Align(iso.getRefID(), iso.get(i-1).getEndCis(), iso.get(i).getStartCis()), considerStrand);
					mapJuncKnown2Num.put(jun, new int[1]);
				}
			}
		}
	}
	
	private void statisticJunctionInfo() {
		boolean considerStrand = false;
		if (specific == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND || specific == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
			considerStrand = true;
		}
		double ratioBlock = (double)1/nodeNum;
		int readBlockNum = 1;
		int readNum = 0;

		
		for (Align align : shuffleReads.readlines()) {
			if (readNum >= shuffleReads.getAllReadsNum() * ratioBlock * readBlockNum) {
				getSubsiteInfo((double)readNum/shuffleReads.getAllReadsNum(), xyExistJun, xyNovelJun, xyAllJun);
				readBlockNum++;
			}
			String junInfo = getJunInfo(align, considerStrand);
			int[] readsNum = mapJuncKnown2Num.get(junInfo);
			if (readsNum == null) {
				readsNum = mapJuncUnKnown2Num.get(junInfo);
				if (readsNum == null) {
					readsNum = new int[1];
					mapJuncUnKnown2Num.put(junInfo, readsNum);
				}
			}
			if (readBlockNum <= nodeNum) {
				getSubsiteInfo(1, xyExistJun, xyNovelJun, xyAllJun);
			}
			readsNum[0]++;
			readNum++;
		}
	}
	
	private String getJunInfo(Align align, boolean considerStrand) {
		String junInfo = align.getRefID() + SepSign.SEP_ID + align.getStartAbs() + SepSign.SEP_INFO + align.getEndAbs();
		if (considerStrand) {
			junInfo += SepSign.SEP_INFO + align.isCis5to3();
		}
		return junInfo;
	}
	
	private void getSubsiteInfo(double rate, XYSeries xyExistJun, XYSeries xyNovelJun, XYSeries xyAllJun) {
		int existJunSiteNum = 0, novelJunSiteNum = 0;
		for (int[] readsNum : mapJuncKnown2Num.values()) {
			if (readsNum[0] > 0 && readsNum[0] >= times) {
				existJunSiteNum++;
			}
		}
		for (int[] readsNum : mapJuncUnKnown2Num.values()) {
			if (readsNum[0] >= times) {
				novelJunSiteNum++;
			}
		}
		int allJunSiteNum = existJunSiteNum + novelJunSiteNum;
		
		xyExistJun.add(Math.floor(rate*100), (double)existJunSiteNum/mapJuncKnown2Num.size()*100);  
		xyNovelJun.add(Math.floor(rate*100), (double)novelJunSiteNum/mapJuncKnown2Num.size()*100);  
		xyAllJun.add(Math.floor(rate*100), (double)allJunSiteNum/mapJuncKnown2Num.size()*100);  
	}
	
	public void plot() {
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();  
		xySeriesCollection.addSeries(xyExistJun);  
        xySeriesCollection.addSeries(xyNovelJun);  
        xySeriesCollection.addSeries(xyAllJun);  
		drawImage(xySeriesCollection,imageSavePath);
	}
	
	/**
	 * 根据点画曲线图
	 * @param xySeriesCollection
	 */
	private void drawImage(XYSeriesCollection xySeriesCollection,String savePath){
    	XYSplineRenderer renderer = new XYSplineRenderer();
    	renderer.setBaseShapesVisible(false); //绘制的线条上不显示图例，如果显示的话，会使图片变得很丑陋
    	renderer.setSeriesPaint(0, Color.GREEN); //设置0号数据的颜色。如果一个图中绘制多条曲线，可以手工设置颜色
    	renderer.setPrecision(5); //设置精度，大概就是在源数据两个点之间插入5个点以拟合出一条平滑曲线
    	renderer.setSeriesShapesVisible(0, true);//设置三条线是否显示 点 的形状
    	renderer.setSeriesShapesVisible(1, true);
    	renderer.setSeriesShapesVisible(2, true);
    	//create plot
    	NumberAxis xAxis = new NumberAxis("percentage of bam junctions (%)");
    	xAxis.setAutoRangeIncludesZero(false);
    	NumberAxis yAxis = new NumberAxis("percentage of all know junctions(%)");
    	yAxis.setAutoRangeIncludesZero(false);

    	XYPlot plot = new XYPlot(xySeriesCollection, xAxis, yAxis, renderer);
    	plot.setBackgroundPaint(Color.white);
    	plot.setDomainGridlinePaint(Color.white);
    	plot.setRangeGridlinePaint(Color.white);
    	plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4)); //设置坐标轴与绘图区域的距离
    	JFreeChart chart = new JFreeChart("Junction Saturation", //标题
    			JFreeChart.DEFAULT_TITLE_FONT, //标题的字体，这样就可以解决中文乱码的问题
    			plot,
    			true //不在图片底部显示图例
    	);
    	ImageUtils.saveBufferedImage(chart.createBufferedImage(1024, 768), savePath);
	}

	@Override
	public Align getReadingRegion() {
		return null;
	}
	
	public void clear() {
		shuffleReads = new ShuffleReads();
		specific = StrandSpecific.UNKNOWN;
		times = 0;
		nodeNum = 20;
		xyExistJun = new XYSeries("Known Junctions");
		xyNovelJun = new XYSeries("Novel Junctions");
		xyAllJun = new XYSeries("All Junctions");
		mapJuncKnown2Num.clear();
		mapJuncUnKnown2Num.clear();
		imageSavePath = null;
		gffHashGene = null;
	}
	
}
