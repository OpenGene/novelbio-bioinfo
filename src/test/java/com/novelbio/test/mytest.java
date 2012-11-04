package com.novelbio.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.servlet.jsp.tagext.TryCatchFinally;
import javax.swing.tree.ExpandVetoException;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.inference.TestUtils;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;
import org.apache.velocity.tools.config.Data;
import org.junit.experimental.theories.PotentialAssignment.CouldNotGenerateValueException;
import org.w3c.dom.ls.LSSerializer;


import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.SeqComb;
import com.novelbio.analysis.seq.blastZJ.Cell;
import com.novelbio.analysis.seq.blastZJ.LongestCommonSubsequence;
import com.novelbio.analysis.seq.blastZJ.SmithWaterman;
import com.novelbio.analysis.seq.chipseq.PeakCalling;
import com.novelbio.analysis.seq.chipseq.PeakMacs;
import com.novelbio.analysis.seq.chipseq.PeakSicer;
import com.novelbio.analysis.seq.fasta.ChrStringHash;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqFastaHash;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.analysis.seq.fastq.FastQRecordFilter;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.GffChrAnno;
import com.novelbio.analysis.seq.genome.GffChrSeq;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoCis;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGenePlant;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.reseq.LastzAlign;
import com.novelbio.analysis.seq.reseq.ModifySeq;
import com.novelbio.analysis.seq.resequencing.MapInfoSnpIndel;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo;
import com.novelbio.analysis.seq.resequencing.SnpAnnotation;
import com.novelbio.analysis.seq.resequencing.SnpFilter;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.analysis.tools.Mas3.getProbID;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateTime;
import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.gui.GUIFileOpen;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.GraphicCope;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.Rplot;
import com.novelbio.database.domain.geneanno.AGene2Go;
import com.novelbio.database.domain.geneanno.AGeneInfo;
import com.novelbio.database.domain.geneanno.Gene2Go;
import com.novelbio.database.domain.geneanno.GeneInfo;
import com.novelbio.database.domain.geneanno.Go2Term;
import com.novelbio.database.domain.geneanno.NCBIID;
import com.novelbio.database.domain.kegg.noUseKGCentry2Ko2Gen;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.modgeneid.GeneType;
import com.novelbio.database.model.modkegg.KeggInfo;
import com.novelbio.database.model.species.Species;
import com.novelbio.database.service.servgeneanno.ServGeneInfo;
import com.novelbio.generalConf.NovelBioConst;
 import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class mytest {

	private static Logger logger = Logger.getLogger(mytest.class);
	/**
	 * @param args
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		GffChrAbs gffChrAbs = new GffChrAbs(9606);
		GffChrSeq gffChrSeq = new GffChrSeq(gffChrAbs);
		gffChrSeq.setGeneStructure(GeneStructure.INTRON);
		
		GffDetailGene gffGene = gffChrAbs.getGffHashGene().searchLOC(new GeneID("C10orf108", 9606));		
		
		
		System.out.println(gffGene.getLsCodSplit().size());
		for (GffGeneIsoInfo gffGeneIsoInfo : gffGene.getLsCodSplit()) {
			if (gffGeneIsoInfo.getGeneType() == GeneType.tRNA) {
				continue;
			}
			System.out.println(gffGeneIsoInfo.getGeneType());
		}
	}
	
	private void HG18() {
		SnpAnnotation snpAnnotation = new SnpAnnotation();
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile("/media/winE/Bioinformatics/genome/human/hg18_UCSC/ChromFa", null);
		gffChrAbs.setGffFile(9606, NovelBioConst.GENOME_GFF_TYPE_UCSC, "/media/winE/Bioinformatics/genome/human/hg18_UCSC/human_hg18_refseq_UCSC");
		
		gffChrAbs.setFilterGeneBody(true, false, false);
		gffChrAbs.setFilterTssTes(new int[]{-2000,2000}, new int[]{-100,100});
		GffChrAnno gffChrAnno = new GffChrAnno(gffChrAbs);
		gffChrAnno.setColChrID(2);
		gffChrAnno.setSearchSummit(true);
		gffChrAnno.setColSummit(3);
		gffChrAnno.annoFile("/home/zong0jie/桌面/allels_for_jie.txt", "/home/zong0jie/桌面/allels_for_jie_anno_location.txt");
		gffChrAnno.run();
		
//		snpAnnotation.setGffChrAbs(gffChrAbs);
//		snpAnnotation.addTxtSnpFile("/home/zong0jie/桌面/allels_for_jie.txt", "/home/zong0jie/桌面/allels_for_jie_anno.txt");
//		snpAnnotation.setCol(2, 3, 4, 5);
//		snpAnnotation.run();
//		
	}

}


