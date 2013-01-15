

import java.awt.Color;
import java.util.ArrayList;

import javax.jnlp.FileOpenService;

import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrPlotTss;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsChangFang;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsChangFang.CGmethyType;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.database.model.species.Species;

public class Yanghongxing {
	static String gTFyanghongxing = "/media/winF/NBC/Project/Project_MaHong/changfang/methyl_site_flower19_ec4.gtf";
	
	public static void main(String[] args) {
		Species species = new Species(3702, "tair9");
		MapReadsChangFang mapReads = new MapReadsChangFang();
		mapReads.setGTFyanghongxing(gTFyanghongxing);
		mapReads.setMapChrID2Len(species.getMapChromInfo());
		mapReads.setInvNum(5);
		mapReads.run();
		long allReadsNum = mapReads.getAllReadsNum();
		
		Yanghongxing yanghongxing = new Yanghongxing();

		
		yanghongxing.plotTss(CGmethyType.ALL, allReadsNum);
		yanghongxing.plotTss(CGmethyType.CG, allReadsNum);
		yanghongxing.plotTss(CGmethyType.CHG, allReadsNum);
		yanghongxing.plotTss(CGmethyType.CHH, allReadsNum);
	}

	public void plotTss(CGmethyType cgmethyType, long normalizedNum) {
		String path = "/media/winF/NBC/Project/Project_MaHong/changfang/readsDestributionPileup/";
		FileOperate.createFolders(path);
		path = path + cgmethyType;
		Species species = new Species(3702, "tair9");
		MapReadsChangFang mapReads = new MapReadsChangFang();
		mapReads.setGTFyanghongxing(gTFyanghongxing);
		mapReads.setcGmethyType(cgmethyType);
		mapReads.setMapChrID2Len(species.getMapChromInfo());

		mapReads.setInvNum(5);
		mapReads.run();
		mapReads.setAllReadsNum(normalizedNum);
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		
		GffChrPlotTss gffChrPlotTss;
		
		DotStyle dotStyle = new DotStyle();
		dotStyle.setColor(DotStyle.getGridentColor(Color.RED, Color.ORANGE));
		dotStyle.setStyle(DotStyle.STYLE_LINE);
		dotStyle.setSize(DotStyle.SIZE_MB);
		PlotScatter plotScatter = null;
		
		gffChrPlotTss = new GffChrPlotTss(gffChrAbs);
		gffChrPlotTss.setSplitNum(500);
		gffChrPlotTss.setMapReads(mapReads);
		gffChrPlotTss.setPlotTssTesRange(new int[]{-4000,4000});
		gffChrPlotTss.setTsstesRange(new int[]{-1500, 1500});
		gffChrPlotTss.setPileupExonIntron(true);
		///////////////////////////////////////////////
		gffChrPlotTss.clearCollectionInfo();
		gffChrPlotTss.setGeneIDGenome();
		gffChrPlotTss.setGeneStructure(GeneStructure.TSS);
		plotScatter = gffChrPlotTss.plotLine(dotStyle);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_M);
		plotScatter.saveToFile(path + "tss.png", 2000, 1000);
	
		///////////////////////////////////////////////
		gffChrPlotTss.clearCollectionInfo();
		gffChrPlotTss.setGeneIDGenome();
		
		gffChrPlotTss.setGeneStructure(GeneStructure.TES);
		plotScatter = gffChrPlotTss.plotLine(dotStyle);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_M);
		plotScatter.saveToFile(path + "tes.png", 2000, 1000);
		
		///////////////////////////////////////////////
		gffChrPlotTss.clearCollectionInfo();
		gffChrPlotTss.setGeneIDGenome();
		
		gffChrPlotTss.setGeneStructure(GeneStructure.ALLLENGTH);
		plotScatter = gffChrPlotTss.plotLine(dotStyle);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_M);
		plotScatter.saveToFile(path + "allGene.png", 2000, 1000);
		
		///////////////////////////////////////////////
		gffChrPlotTss.clearCollectionInfo();
		gffChrPlotTss.setGeneIDGenome();
		
		gffChrPlotTss.setGeneStructure(GeneStructure.INTRON);
		plotScatter = gffChrPlotTss.plotLine(dotStyle);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_M);
		plotScatter.saveToFile(path + "Intron.png", 2000, 1000);
		
		///////////////////////////////////////////////
		gffChrPlotTss.clearCollectionInfo();
		gffChrPlotTss.setGeneIDGenome();
		
		gffChrPlotTss.setGeneStructure(GeneStructure.INTRON);
		ArrayList<Integer> lsIntronNum = new ArrayList<Integer>();
		lsIntronNum.add(1);
		gffChrPlotTss.setLsExonIntronNumGetOrExclude(lsIntronNum);
		gffChrPlotTss.setGetOrExclude(true);
		plotScatter = gffChrPlotTss.plotLine(dotStyle);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_M);
		plotScatter.saveToFile(path + "Intron1.png", 2000, 1000);
		///////////////////////////////////////////////
		gffChrPlotTss.clearCollectionInfo();
		gffChrPlotTss.setGeneIDGenome();
		
		gffChrPlotTss.setGeneStructure(GeneStructure.INTRON);
		lsIntronNum = new ArrayList<Integer>();
		lsIntronNum.add(-1);
		gffChrPlotTss.setLsExonIntronNumGetOrExclude(lsIntronNum);
		gffChrPlotTss.setGetOrExclude(true);
		plotScatter = gffChrPlotTss.plotLine(dotStyle);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_M);
		plotScatter.saveToFile(path + "Intron-1.png", 2000, 1000);
		/////////////////////////////////////////////////////////
		gffChrPlotTss.clearCollectionInfo();
		gffChrPlotTss.setGeneIDGenome();
		
		lsIntronNum = new ArrayList<Integer>();
		lsIntronNum.add(1);lsIntronNum.add(-1);
		gffChrPlotTss.setGeneStructure(GeneStructure.INTRON);
		gffChrPlotTss.setLsExonIntronNumGetOrExclude(lsIntronNum);
		gffChrPlotTss.setGetOrExclude(false);
		plotScatter = gffChrPlotTss.plotLine(dotStyle);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_M);
		plotScatter.saveToFile(path + "Intron_Exclude_1and-1.png", 2000, 1000);
		
		/////////////////////////////////////////////////////////
		gffChrPlotTss.clearCollectionInfo();
		gffChrPlotTss.setGeneIDGenome();
		
		gffChrPlotTss.setGeneStructure(GeneStructure.EXON);
		plotScatter = gffChrPlotTss.plotLine(dotStyle);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_M);
		plotScatter.saveToFile(path + "Exon.png", 2000, 1000);
		
		///////////////////////////////////////////////
		gffChrPlotTss.clearCollectionInfo();
		gffChrPlotTss.setGeneIDGenome();
		
		gffChrPlotTss.setGeneStructure(GeneStructure.EXON);
		ArrayList<Integer> lsExonNum = new ArrayList<Integer>();
		lsExonNum.add(1);
		gffChrPlotTss.setLsExonIntronNumGetOrExclude(lsExonNum);
		gffChrPlotTss.setGetOrExclude(true);
		plotScatter = gffChrPlotTss.plotLine(dotStyle);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_M);
		plotScatter.saveToFile(path + "exon1.png", 2000, 1000);
		
		/////////////////////////////////////////////////////////
		gffChrPlotTss.clearCollectionInfo();
		gffChrPlotTss.setGeneIDGenome();
		
		lsExonNum = new ArrayList<Integer>();
		lsExonNum.add(-1);
		gffChrPlotTss.setLsExonIntronNumGetOrExclude(lsExonNum);
		gffChrPlotTss.setGetOrExclude(true);
		plotScatter = gffChrPlotTss.plotLine(dotStyle);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_M);
		plotScatter.saveToFile(path + "exon-1.png", 2000, 1000);
		
		/////////////////////////////////////////////////////////
		gffChrPlotTss.clearCollectionInfo();
		gffChrPlotTss.setGeneIDGenome();
		
		lsExonNum = new ArrayList<Integer>();
		lsExonNum.add(1); lsExonNum.add(-1);
		gffChrPlotTss.setLsExonIntronNumGetOrExclude(lsExonNum);
		gffChrPlotTss.setGetOrExclude(false);
		plotScatter = gffChrPlotTss.plotLine(dotStyle);
		plotScatter.setBg(Color.WHITE);
		plotScatter.setInsets(PlotScatter.INSETS_SIZE_M);
		plotScatter.saveToFile(path + "exon_Exclude_1and-1.png", 2000, 1000);
	}
	
	
}
