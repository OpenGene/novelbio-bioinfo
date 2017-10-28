package com.novelbio.analysis.seq.mapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.mapping.IndexMappingMaker.IndexHisat2;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamToBamSort;
import com.novelbio.base.SepSign;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 * HISAT2 version 2.0.0-beta by Daehwan Kim (infphilo@gmail.com, www.ccb.jhu.edu/people/infphilo)
Usage: 
  hisat [options]* -x <bt2-idx> {-1 <m1> -2 <m2> | -U <r>} [-S <sam>]

  <bt2-idx>  Index filename prefix (minus trailing .X.ht2).
  <m1>       Files with #1 mates, paired with files in <m2>.
             Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2).
  <m2>       Files with #2 mates, paired with files in <m1>.
             Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2).
  <r>        Files with unpaired reads.
             Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2).
  <sam>      File for SAM output (default: stdout)

  <m1>, <m2>, <r> can be comma-separated lists (no whitespace) and can be
  specified many times.  E.g. '-U file1.fq,file2.fq -U file3.fq'.

Options (defaults in parentheses):

 Input:
  -q                 query input files are FASTQ .fq/.fastq (default)
  --qseq             query input files are in Illumina's qseq format
  -f                 query input files are (multi-)FASTA .fa/.mfa
  -r                 query input files are raw one-sequence-per-line
  -c                 <m1>, <m2>, <r> are sequences themselves, not files
  -s/--skip <int>    skip the first <int> reads/pairs in the input (none)
  -u/--upto <int>    stop after first <int> reads/pairs (no limit)
  -5/--trim5 <int>   trim <int> bases from 5'/left end of reads (0)
  -3/--trim3 <int>   trim <int> bases from 3'/right end of reads (0)
  --phred33          qualities are Phred+33 (default)
  --phred64          qualities are Phred+64
  --int-quals        qualities encoded as space-delimited integers

 Presets:                 Same as:
  For --end-to-end:
   --very-fast            -D 5 -R 1 -N 0 -L 22 -i S,0,2.50
   --fast                 -D 10 -R 2 -N 0 -L 22 -i S,0,2.50
   --sensitive            -D 15 -R 2 -N 0 -L 22 -i S,1,1.15 (default)
   --very-sensitive       -D 20 -R 3 -N 0 -L 20 -i S,1,0.50

 Alignment:
  -N <int>           max # mismatches in seed alignment; can be 0 or 1 (0)
  -L <int>           length of seed substrings; must be >3, <32 (22)
  -i <func>          interval between seed substrings w/r/t read len (S,1,1.15)
  --n-ceil <func>    func for max # non-A/C/G/Ts permitted in aln (L,0,0.15)
  --dpad <int>       include <int> extra ref chars on sides of DP table (15)
  --gbar <int>       disallow gaps within <int> nucs of read extremes (4)
  --ignore-quals     treat all quality values as 30 on Phred scale (off)
  --nofw             do not align forward (original) version of read (off)
  --norc             do not align reverse-complement version of read (off)

 Spliced Alignment:
  --pen-cansplice <int>              penalty for a canonical splice site (0)
  --pen-noncansplice <int>           penalty for a non-canonical splice site (12)
  --pen-canintronlen <func>          penalty for long introns (G,-8,1) with canonical splice sites
  --pen-noncanintronlen <func>       penalty for long introns (G,-8,1) with noncanonical splice sites
  --min-intronlen <int>              minimum intron length (20)
  --max-intronlen <int>              maximum intron length (500000)
  --known-splicesite-infile <path>   provide a list of known splice sites
  --novel-splicesite-outfile <path>  report a list of splice sites
  --novel-splicesite-infile <path>   provide a list of novel splice sites
  --no-temp-splicesite               disable the use of splice sites found
  --no-spliced-alignment             disable spliced alignment
  --rna-strandness <string>          Specify strand-specific information (unstranded)
  --tmo                              Reports only those alignments within known transcriptome
  --dta                              Reports alignments tailored for transcript assemblers
  --dta-cufflinks                    Reports alignments tailored specifically for cufflinks

 Scoring:
  --ma <int>         match bonus (0 for --end-to-end, 2 for --local) 
  --mp <int>,<int>   max and min penalties for mismatch; lower qual = lower penalty <2,6>
  --sp <int>,<int>   max and min penalties for soft-clipping; lower qual = lower penalty <1,2>
  --np <int>         penalty for non-A/C/G/Ts in read/ref (1)
  --rdg <int>,<int>  read gap open, extend penalties (5,3)
  --rfg <int>,<int>  reference gap open, extend penalties (5,3)
  --score-min <func> min acceptable alignment score w/r/t read length
                     (G,20,8 for local, L,-0.6,-0.6 for end-to-end)

 Reporting:
  (default)          look for multiple alignments, report best, with MAPQ
   OR
  -k <int>           report up to <int> alns per read; MAPQ not meaningful
   OR
  -a/--all           report all alignments; very slow, MAPQ not meaningful

 Effort:
  -D <int>           give up extending after <int> failed extends in a row (15)
  -R <int>           for reads w/ repetitive seeds, try <int> sets of seeds (2)

 Paired-end:
  -I/--minins <int>  minimum fragment length (0)
  -X/--maxins <int>  maximum fragment length (500)
  --fr/--rf/--ff     -1, -2 mates align fw/rev, rev/fw, fw/fw (--fr)
  				(TopHat has a similar option, --library-type option, where fr-firststrand corresponds to R and RF; fr-secondstrand corresponds to F and FR.)
  --no-mixed         suppress unpaired alignments for paired reads
  --no-discordant    suppress discordant alignments for paired reads
  --no-dovetail      not concordant when mates extend past each other
  --no-contain       not concordant when one mate alignment contains other
  --no-overlap       not concordant when mates overlap at all

 Output:
  -t/--time          print wall-clock time taken by search phases
  --un <path>           write unpaired reads that didn't align to <path>
  --al <path>           write unpaired reads that aligned at least once to <path>
  --un-conc <path>      write pairs that didn't align concordantly to <path>
  --al-conc <path>      write pairs that aligned concordantly at least once to <path>
  (Note: for --un, --al, --un-conc, or --al-conc, add '-gz' to the option name, e.g.
  --un-gz <path>, to gzip compress output, or add '-bz2' to bzip2 compress output.)
  --quiet            print nothing to stderr except serious errors
  --met-file <path>  send metrics to file at <path> (off)
  --met-stderr       send metrics to stderr (off)
  --met <int>        report internal counters & metrics every <int> secs (1)
  --no-head          supppress header lines, i.e. lines starting with @
  --no-sq            supppress @SQ header lines
  --rg-id <text>     set read group id, reflected in @RG line and RG:Z: opt field
  --rg <text>        add <text> ("lab:value") to @RG line of SAM header.
                     Note: @RG line only printed when --rg-id is set.
  --omit-sec-seq     put '*' in SEQ and QUAL fields for secondary alignments.

 Performance:
  -o/--offrate <int> override offrate of index; must be >= index's offrate
  -p/--threads <int> number of alignment threads to launch (1)
  --reorder          force SAM output order to match order of input reads
  --mm               use memory-mapped I/O for index; many 'bowtie's can share

 Other:
  --qc-filter        filter out reads that are bad according to QSEQ filter
  --seed <int>       seed for random number generator (0)
  --non-deterministic seed rand. gen. arbitrarily instead of using read attributes
  --version          print version information and quit
  -h/--help          print this usage message
(ERR): hisat2-align exited with value 1

 * @author novelbio
 *
 */
public class MapHisat implements MapRNA {
	private static Logger logger = LoggerFactory.getLogger(MapHisat.class);
	
	/** mapping文件的后缀，包含 ".bam" 字符串 */
	public static final String MAPHISAT2SUFFIX = ".hisat2.bam";
	
	private String exePathHist;
	/** 
	 * Presets:
	 * For --end-to-end: <br>
	 *  --very-fast            -D 5 -R 1 -N 0 -L 22 -i S,0,2.50 <br>
	 *  --fast                 -D 10 -R 2 -N 0 -L 22 -i S,0,2.50 <br>
	 *  --sensitive            -D 15 -R 2 -N 0 -L 22 -i S,1,1.15 (default) <br>
	 *  --very-sensitive       -D 20 -R 3 -N 0 -L 20 -i S,1,0.50 
	 *  */
	private int sensitive = MapBowtie2.Sensitive_Sensitive;
	
	/** skip 输入文件的前几个 reads或者pairs */
	private int skipReads = 0;
	/** trim bases from 5'/left end of reads (0) */
	private int trim5 = 0;
	/** trim bases from 3'/right end of reads (0) */
	private int trim3 = 0;
	/** qualities 是否是 Phred+64 如果值为 false,则表示qualities是 Phred+33 */
	private int fastqOffset = 0;
		
	private int intronLenMin = 20;
	private int intronLenMax = 500000;
	
	/** 最多比对到几个位置，0的话mapquality就有意义，我们这里默认为3 */
	private int alignNum = 5;
	
	private int threadNum = 8;
	
	private boolean downstreamTranscriptomeAssembly = true;
	
	private StrandSpecific strandSpecifictype = StrandSpecific.NONE;
	
	private List<FastQ> lsLeftFq = new ArrayList<>();
	private List<FastQ> lsRightFq = new ArrayList<>();
	
	private 	String spliceTxt;
	/** 新的splice位点 */
	private String novelSplice;
	
	private String outputSam = "";
	
	IndexHisat2 indexHisat2 = (IndexHisat2)IndexMappingMaker.createIndexMaker(SoftWare.hisat2);
	
	public MapHisat() {
		SoftWareInfo softMapSplice = new SoftWareInfo();
		softMapSplice.setName(SoftWare.hisat2);
		this.exePathHist = softMapSplice.getExePathRun();
		indexHisat2.setExePath(exePathHist);
	}
	protected void setExePathHist(String exePathHist) {
		this.exePathHist = exePathHist;
		indexHisat2.setExePath(exePathHist);
	}
	/** trim bases from 5'/left end of reads (0) */
	public void setTrim3(int trim3) {
		this.trim3 = trim3;
	}
	/** trim bases from 5'/left end of reads (0) */
	public void setTrim5(int trim5) {
		this.trim5 = trim5;
	}
	public void setSkipReads(int skipReads) {
		this.skipReads = skipReads;
	}
	
	public void setIntronLenMin(int intronLenMin) {
		this.intronLenMin = intronLenMin;
	}
	public void setIntronLenMax(int intronLenMax) {
		this.intronLenMax = intronLenMax;
	}
	
	public void setSpliceTxt(String spliceTxt) {
		this.spliceTxt = spliceTxt;
	}
	/**
	 * Report alignments tailored for transcript assemblers including StringTie. 
	 * With this option, HISAT2 requires longer anchor lengths for de novo discovery
	 * of splice sites. This leads to fewer alignments with short-anchors, which
	 * helps transcript assemblers improve significantly in computationa and
	 * memory usage.
	 * @param downstreamTranscriptomeAssembly
	 */
	public void setDownstreamTranscriptomeAssembly(boolean downstreamTranscriptomeAssembly) {
		this.downstreamTranscriptomeAssembly = downstreamTranscriptomeAssembly;
	}
	
	@Override
	public void setRefIndex(String chrFile) {
		indexHisat2.setChrIndex(chrFile);
	}
	
	/**
	 * 比对到多个位置的数量，小于等于0表示不考虑该参数
	 * 默认为5
	 */
	public void setAlignNum(int alignNum) {
		this.alignNum = alignNum;
	}
	
	/** 输出文件 */
	@Override
	public void setOutPathPrefix(String outPathPrefix) {
		this.outputSam = outPathPrefix + MAPHISAT2SUFFIX;
	}

	@Override
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	@Override
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype) {
		this.strandSpecifictype = strandSpecifictype;
	}
	
	/** qualities 是否是 Phred+64 如果值为 false,则表示qualities是 Phred+33 */
	public void setPhred64(boolean isPhred64) {
		fastqOffset = isPhred64 ? FastQ.FASTQ_ILLUMINA_OFFSET : FastQ.FASTQ_SANGER_OFFSET;
	}
	
	/** 默认sensitive */
	public void setSensitiveLevel(int sensitive) {
		this.sensitive = sensitive;
	}
	
	/** 设定splicesite和min-intronLen，max-intronLen */
	@Override
	public void setGtfFiles(String gtfFile) {
		spliceTxt = gtfFile;
//		if (!FileOperate.isFileExistAndBigThan0(gtfFile)) return;
//
//		GffHashGene gffHashGene = new GffHashGene(gtfFile);
//		spliceTxt = FileOperate.getPathName(outputSam) + FileOperate.getFileName(gffHashGene.getGffFilename());
//		spliceTxt = FileOperate.changeFileSuffix(spliceTxt, "_spliceSite", "txt");
//		writeSpliceTxt(gffHashGene, spliceTxt);
		
//		int[] intronMinMax = MapTophat.getIntronMinMax(gffHashGene, intronLenMin, intronLenMax);
//		intronLenMin = intronMinMax[0];
//		intronLenMax = intronMinMax[1];
	}
	
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsCmd = new ArrayList<>();
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		lsCmd.add(cmdOperate.getCmdExeStr());
		return lsCmd;
	}

	private List<String> getLsCmd() {
		// linux命令如下
		/**
		 * Usage: hisat [options]* -x <bt2-idx> {-1 <m1> -2 <m2> | -U <r>} [-S <sam>] 
		 * PE reads
		 * ./hisat -x ./Puccinia_striiformis -q -1 CYR32_2_filtered_1.fq.gz -2 CYR32_2_filtered_1.fq.gz -S CYR32_HISAT.sam
		 * SE reads
		 * ./hisat -x ./Puccinia_striiformis -q -U CYR32_2_filtered_1.fq.gz -S CYR32_HISAT_single.sam
		 *  <bt2-idx>  Index filename prefix (minus trailing .X.bt2).
		 *  <m1>       Files with #1 mates, paired with files in <m2>. Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2).
		 *  <m2>       Files with #2 mates, paired with files in <m1>. Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2).
		 *  <r>        Files with unpaired reads. Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2).
		 *  <sam>      File for SAM output (default: stdout)
		 *  PS: <m1>, <m2>, <r> can be comint sensitive = Sensitive_Sensitive;ma-separated lists (no whitespace) and can be specified many times.  E.g. '-U file1.fq,file2.fq -U file3.fq'.
		 */
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(exePathHist + "hisat2");
		lsCmd.add(getFastqOffset());
		ArrayOperate.addArrayToList(lsCmd, getOutNovelSpliceParam());
		ArrayOperate.addArrayToList(lsCmd, getTrim5());
		ArrayOperate.addArrayToList(lsCmd, getTrim3());
		ArrayOperate.addArrayToList(lsCmd, getSkipReads());
		ArrayOperate.addArrayToList(lsCmd, getSensitive());
		ArrayOperate.addArrayToList(lsCmd, getAlignNum());
		ArrayOperate.addArrayToList(lsCmd, getDta());
		
		ArrayOperate.addArrayToList(lsCmd, getIntronlen());
		ArrayOperate.addArrayToList(lsCmd, getThread());
		
		ArrayOperate.addArrayToList(lsCmd, getNovelSpliceSiteInput());
		ArrayOperate.addArrayToList(lsCmd, getSpliceSiteInput());
		
		ArrayOperate.addArrayToList(lsCmd, getStrandSpecifictype());
		ArrayOperate.addArrayToList(lsCmd, getIndex());
		ArrayOperate.addArrayToList(lsCmd, getFqFiles());
		return lsCmd;
	}

	private String getFastqOffset() {
		if (fastqOffset == 0) {
			ArrayList<FastQ> lsFastQs = new ArrayList<>();
			lsFastQs.addAll(lsLeftFq);
			lsFastQs.addAll(lsRightFq);
			fastqOffset = FastQ.getFastqOffset(lsFastQs);
		}
		return fastqOffset == FastQ.FASTQ_ILLUMINA_OFFSET? "--phred64" : "--phred33";
	}
	
	
	private String[] getTrim5() {
		return trim5 > 0? new String[]{"--trim5", trim5 + ""} : null;
	}
	private String[] getTrim3() {
		return trim3 > 0? new String[]{"--trim3", trim3 + ""} : null;
	}
	
	private String[] getSkipReads() {
		return skipReads > 0? new String[]{"--skip", skipReads + ""} : null;
	}

	private String[] getIndex() {
		return new String[]{"-x", indexHisat2.getIndexName()};
	}
	
//	private String[] getInputFileFormat() {
//		return new String[]{"-q"};
//	}
	
	private String[] getSensitive() {
		if(sensitive == MapBowtie2.Sensitive_Very_Fast) {
			return new String[]{"--very-fast"};
		} else if (sensitive == MapBowtie2.Sensitive_Fast) {
			return new String[]{"--fast"};
		} else if (sensitive == MapBowtie2.Sensitive_Very_Sensitive) {
			return new String[]{"--very-sensitive"};
		} else if(sensitive == MapBowtie2.Sensitive_Sensitive) {
			return new String[]{"--sensitive"};
		}
		return null;
	}
	
	private String[] getAlignNum() {
		if (alignNum <= 0) {
			return null;
		} else if (alignNum > 30) {
			return new String[]{"-a"};
		}
		return new String[]{"-k", alignNum + ""};
	}
	
	private String[] getDta() {
		return downstreamTranscriptomeAssembly? new String[]{"--dta"} : new String[]{null};
	}

	private String[] getOutNovelSpliceParam() {
		return new String[]{"--novel-splicesite-outfile", getNovelSpliceSiteFile()};
	}
	
	private String getNovelSpliceSiteFile() {
		return FileOperate.changeFileSuffix(outputSam, ".novel.splicesite", "txt");
	}
	
	/** 返回链的方向 */
	private String[] getStrandSpecifictype() {
		String[] cmd = null;
		if (strandSpecifictype == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
			cmd = isPairend()? new String[]{"--rna-strandness", "FR", "--fr"} : new String[]{"--rna-strandness", "F"};
		} else if (strandSpecifictype == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
			cmd = isPairend()? new String[]{"--rna-strandness", "RF", "--rf"} : new String[]{"--rna-strandness", "R"};
		}
		return cmd;
	}
	
	private String[] getNovelSpliceSiteInput() {
		return StringOperate.isRealNull(novelSplice)? null : new String[]{"--novel-splicesite-infile", novelSplice};
	}
	
	private String[] getSpliceSiteInput() {
		return StringOperate.isRealNull(spliceTxt)? null : new String[]{"--known-splicesite-infile", spliceTxt};
	}
	
	private String[] getIntronlen() {
		List<String> lsIntronLen = new ArrayList<>();
		if (intronLenMin > 0) {
			lsIntronLen.add("--min-intronlen");
			lsIntronLen.add(intronLenMin + "");
		}
		if (intronLenMax > 0) {
			lsIntronLen.add("--max-intronlen");
			lsIntronLen.add(intronLenMax + "");
		}
		return lsIntronLen.toArray(new String[0]);
	}
	
	private String[] getThread() {
		return new String[]{"-p", threadNum + ""};
	}
	
	private boolean isPairend() {
		return !CollectionUtils.isEmpty(lsLeftFq) && !CollectionUtils.isEmpty(lsRightFq);
	}
	
	@Override
	public void setInsert(int insert) {
	}

	@Override
	public void setLeftFq(List<FastQ> lsLeftFq) {
		this.lsLeftFq.clear();
		if (lsLeftFq == null) return;
		this.lsLeftFq = lsLeftFq;
	}

	@Override
	public void setRightFq(List<FastQ> lsRightFq) {
		this.lsRightFq.clear();
		if (lsRightFq == null) return;
		this.lsRightFq = lsRightFq;
	}
	
	private String[] getFqFiles() {
		List<String> lsResult = new ArrayList<>();
		boolean isPairend = isPairend();
		if (isPairend) {
			lsResult.add("-1");
		} else {
			lsResult.add("-U");
		}
		StringBuilder firstBf = new StringBuilder(lsLeftFq.get(0).getReadFileName());
		for (int i = 1; i < lsLeftFq.size(); i++) {
			String fileName = lsLeftFq.get(i).getReadFileName();
			firstBf.append(","); firstBf.append(fileName);
		}
		lsResult.add(firstBf.toString());
		
		if (isPairend()) {
			lsResult.add("-2");
			StringBuilder secondBf = new StringBuilder(lsRightFq.get(0).getReadFileName());
			for (int i = 1; i < lsRightFq.size(); i++) {
				String fileName = lsRightFq.get(i).getReadFileName();
				secondBf.append(","); secondBf.append(fileName);
			}
			lsResult.add(secondBf.toString());
		}
		return lsResult.toArray(new String[0]);
	}
	
	@Override
	public void mapReads() {
		indexHisat2.IndexMake();

		String prefix = FileOperate.getFileName(outputSam);
		String parentPath = FileOperate.getParentPathNameWithSep(outputSam);
		String mapHisatBam = parentPath + prefix + MAPHISAT2SUFFIX;
		if (!FileOperate.isFileExistAndBigThanSize(mapHisatBam, 1_000_000) ||
				!FileOperate.isFileExistAndBigThanSize(mapHisatBam, 1_000)
				) {
			CmdOperate cmdOperate = new CmdOperate(getLsCmd());
			cmdOperate.setRedirectOutToTmp(true);
			cmdOperate.addCmdParamOutput(getNovelSpliceSiteFile());
			cmdOperate.setGetCmdInStdStream(true);
		
			Thread thread = new Thread(cmdOperate);
			thread.setDaemon(true);
			thread.start();
			InputStream inputStream = cmdOperate.getStreamStd();
			SamFile samFile = copeSamStream(false, inputStream);
			
			if (!cmdOperate.isFinishedNormal()) {
				throw new ExceptionCmd("error running hisat:" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
			}
			
		}
	}
	
	/**
	 * @param isSetMulitFlag 是否需要设定非unique mapping的标签，目前 有bowtie2和bwa的 mem需要
	 * @param inputStream 内部关闭流
	 * @param isNeedSort 看是否需要排序
	 * @return null表示运行失败，失败了也不删除文件
	 */
	protected SamFile copeSamStream(boolean isSetMulitFlag, InputStream inputStream) {
		SamToBamSort samToBamSort = new SamToBamSort(outputSam, inputStream, isPairend());
		samToBamSort.setAddMultiHitFlag(true);
		samToBamSort.convertAndFinish();
		return samToBamSort.getSamFileBam();
	}

	@Override
	public String getFinishName() {
		return outputSam;
	}
	
	/** 把gtfFile改称spliceTxt文件，如果文件已经存在则直接返回 */
	public static String convert2SpliceTxt(String gtfFile) {
		String spliceTxt = FileOperate.getParentPathNameWithSep(gtfFile) + FileOperate.getFileName(gtfFile);
		spliceTxt = FileOperate.changeFileSuffix(spliceTxt, ".spliceSite", "txt");
		if (!FileOperate.isFileExistAndBigThan0(spliceTxt)) {
			GffHashGene gffHashGene = new GffHashGene(gtfFile);
			writeSpliceTxt(gffHashGene, spliceTxt);
		}
		return spliceTxt;
	}
	
	/** 把gtfFile改称spliceTxt文件，如果文件已经存在则直接返回 */
	public static String convert2SpliceTxt(String gtfFile, String outPath) {
		FileOperate.createFolders(outPath);
		String spliceTxt = FileOperate.addSep(outPath) + FileOperate.getFileName(gtfFile);
		spliceTxt = FileOperate.changeFileSuffix(spliceTxt, "_spliceSite", "txt");
		if (!FileOperate.isFileExistAndBigThan0(spliceTxt)) {
			GffHashGene gffHashGene = new GffHashGene(gtfFile);
			writeSpliceTxt(gffHashGene, spliceTxt);
		}
		return spliceTxt;
	}
	
	private static void writeSpliceTxt(GffHashGene gffHashGene, String spliceTxt) {
		Set<String> setLoc = new HashSet<>();
		for (GffDetailGene gffDetailGene : gffHashGene.getLsGffDetailGenes()) {
			for (GffGeneIsoInfo iso : gffDetailGene.getLsCodSplit()) {
				if (iso.getLen() <= 1) {
					continue;
				}
				for (ExonInfo exonInfo : iso.getLsIntron()) {
					String strand = "+";
					if (!exonInfo.isCis5to3()) {
						strand = "-";
					}
					setLoc.add(iso.getRefID() + SepSign.SEP_ID + (exonInfo.getStartAbs() -2) + SepSign.SEP_ID + exonInfo.getEndAbs() + SepSign.SEP_ID + strand);
				}
			}
		}
		
		List<String> lsResult = new ArrayList<>(setLoc);
		
		Collections.sort(lsResult, new Comparator<String>() {
			public int compare(String o1, String o2) {
				String[] ss1 = o1.split(SepSign.SEP_ID);
				String[] ss2 = o2.split(SepSign.SEP_ID);
				
				String chrId1 = ss1[0];
				String chrId2 = ss2[0];
				
				Integer start1 = Integer.parseInt(ss1[1]);
				Integer end1 = Integer.parseInt(ss1[2]);
				
				Integer start2 = Integer.parseInt(ss2[1]);
				Integer end2 = Integer.parseInt(ss2[2]);
				
				int compareChrId = chrId1.compareTo(chrId2);
				if (compareChrId != 0) {
					return compareChrId;
				}
				
				int compareStart = start1.compareTo(start2);
				if (compareStart != 0) {
					return compareStart;
				}
				return end1.compareTo(end2);
			}
		});
		
		String tmp = spliceTxt + ".tmp";
		TxtReadandWrite txtWrite = new TxtReadandWrite(tmp, true);
		for (String string : lsResult) {
			String[] ss = string.split(SepSign.SEP_ID);
			txtWrite.writefileln(ss);
		}
		txtWrite.close();
		FileOperate.moveFile(true, tmp, spliceTxt);
	}
	
	@Override
    public IndexMappingMaker getIndexMappingMaker() {
	    return indexHisat2;
    }
}

