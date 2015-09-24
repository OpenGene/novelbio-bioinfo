package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.analysis.seq.fasta.StrandType;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.ExonCluster;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.PredictRetainIntron;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict;
import com.novelbio.analysis.seq.genome.gffOperate.exoncluster.SpliceTypePredict.SplicingAlternativeType;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.rnaseq.ISpliceTestModule.SpliceTestFactory;
import com.novelbio.base.dataStructure.Alignment;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.generalConf.TitleFormatNBC;

/**
 * 一个ExonSplicingTest专门检测一个ExonCluster
 * 而一个ExonCluster会存在超过一种的剪接形式，所以这里会选择其中最显著的一个结果
 * 
 * 可变剪接的检验
 * 有一个需要修正的地方
 * 就是alt3和alt5
 * 有时候这个只相差3个bp，就是边界就相差1-3个氨基酸
 * 这种我觉得很扯淡--不过后来发现是有文献依据的
 * 我觉得这种要被过滤掉
 */
public class ExonSplicingTestMX extends ExonSplicingTest {
	public ExonSplicingTestMX(ExonCluster exonCluster) {
		super(exonCluster);
		// TODO Auto-generated constructor stub
	}

	private static final Logger logger = Logger.getLogger(ExonSplicingTestMX.class);

}


