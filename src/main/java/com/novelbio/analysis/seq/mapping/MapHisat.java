package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.beust.jcommander.internal.Lists;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.base.PathDetail;
import com.novelbio.base.SepSign;
import com.novelbio.base.cmd.CmdOperate;
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
	private static Logger logger = Logger.getLogger(MapHisat.class);
	
	public static final int InputFileFormat_FQ = 21;
	public static final int InputFileFormat_Qseq = 22;
	public static final int InputFileFormat_FA = 23;
	public static final int InputFileFormat_OneSeqPLine = 24;
	
	/** 
	 * Presets:
	 * For --end-to-end: <br>
	 *  --very-fast            -D 5 -R 1 -N 0 -L 22 -i S,0,2.50 <br>
	 *  --fast                 -D 10 -R 2 -N 0 -L 22 -i S,0,2.50 <br>
	 *  --sensitive            -D 15 -R 2 -N 0 -L 22 -i S,1,1.15 (default) <br>
	 *  --very-sensitive       -D 20 -R 3 -N 0 -L 20 -i S,1,0.50 
	 *  */
	int sensitive = MapBowtie.Sensitive_Sensitive;
	
	/** 输入文件格式，一共可以是四种 <br>
	 * 1. FASTQ <br>
	 * 2. Illumina's qseq 格式 <br>
	 * 3. (multi-) FASTA .fa/.mfa <br>
	 * 4. one-sequence-per-line  <br>
	 *  */
	int inputFileFormat = InputFileFormat_FQ;
	
	/** skip 输入文件的前几个 reads或者pairs */
	int skipReads = 0;
	/** trim <int> bases from 5'/left end of reads (0) */
	int trim5 = 0;
	/** trim <int> bases from 3'/right end of reads (0) */
	int trim3 = 0;
	/** qualities 是否是 Phred+64 如果值为 false,则表示qualities是 Phred+33 */
	boolean isPhred64 = false;
		
	int intronLenMin = 50;
	int intronLenMax = 500000;
	
	/** 最多比对到几个位置，0的话mapquality就有意义，我们这里默认为3 */
	int alignNum = 3;
	
	String exePathHist;
	
	int threadNum = 8;
	
	StrandSpecific strandSpecifictype = StrandSpecific.NONE;
	
	List<FastQ> lsLeftFq = new ArrayList<>();
	List<FastQ> lsRightFq = new ArrayList<>();
	
	String chrFile;
	String spliceTxt;
	String outputSam = "";
	
	public MapHisat(GffChrAbs gffChrAbs) {
		SoftWareInfo softMapSplice = new SoftWareInfo();
		softMapSplice.setName(SoftWare.hisat2);
		this.exePathHist = softMapSplice.getExePathRun();
		
		if (gffChrAbs.getGffHashGene() == null) return;
		GffHashGene gffHashGene = gffChrAbs.getGffHashGene();
		spliceTxt = PathDetail.getTmpPathRandom() + FileOperate.getFileName(gffHashGene.getGffFilename());
		spliceTxt = FileOperate.changeFileSuffix(spliceTxt, "_spliceSite", "txt");
		setSpliceTxt(gffHashGene, spliceTxt);
		
		int[] intronMinMax = MapTophat.getIntronMinMax(gffHashGene, intronLenMin, intronLenMax);
		intronLenMin = intronMinMax[0];
		intronLenMax = intronMinMax[1];
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
		ArrayOperate.addArrayToList(lsCmd, getIndex());
		ArrayOperate.addArrayToList(lsCmd, getInputFileFormat());
		lsCmd.add(getPhred());
		ArrayOperate.addArrayToList(lsCmd, getSensitive());
		ArrayOperate.addArrayToList(lsCmd, getAlignNum());
		ArrayOperate.addArrayToList(lsCmd, getOutSam());
		return lsCmd;
	}
	
	private String[] getIndex() {
		return new String[]{"-x", chrFile};
	}
	
	private String[] getInputFileFormat() {
		if (inputFileFormat == InputFileFormat_FQ) {
			return new String[]{"-q"};
		} else if (inputFileFormat == InputFileFormat_Qseq) {
			return new String[]{"--qseq"};
		} else if (inputFileFormat == InputFileFormat_FA) {
			return new String[]{"-f"};
		} else if (inputFileFormat == InputFileFormat_OneSeqPLine) {
			return new String[]{"-r"};
		}
		return new String[]{""};
	}
	
	private String getPhred() {
		for (FastQ fastQ : lsLeftFq) {
			if (fastQ.getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
				return "--phred64";
			}
		}
		return "--phred33";
	}
	
	private String[] getSensitive() {
		if(sensitive == MapBowtie.Sensitive_Very_Fast) {
			return new String[]{"--very-fast"};
		} else if (sensitive == MapBowtie.Sensitive_Fast) {
			return new String[]{"--fast"};
		} else if (sensitive == MapBowtie.Sensitive_Very_Sensitive) {
			return new String[]{"--very-sensitive"};
		} else if(sensitive == MapBowtie.Sensitive_Sensitive) {
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
	
	/** 是否可以用流输出呢 */
	//TODO 是否可以用流输出呢
	private String[] getOutSam() {
		return new String[]{"-S",outputSam};
	}
	
	/** 获得没有后缀名的序列，不带引号 */
	protected String getChrNameWithoutSuffix() {
		return getChrNameWithoutSuffix(chrFile);
	}
	
	/** 获得没有后缀名的序列，不带引号 */
	private static String getChrNameWithoutSuffix(String chrFile) {
		String chrFileName = FileOperate.getParentPathNameWithSep(chrFile) + FileOperate.getFileNameSep(chrFile)[0];
		return chrFileName;
	}

	@Override
	public void setRefIndex(String chrFile) {
		this.chrFile = chrFile;
		
	}

	@Override
	public void setOutPathPrefix(String outPathPrefix) {
		this.outputSam = FileOperate.changeFileSuffix(outPathPrefix, "", "bam");
	}

	@Override
	public void setIndelLen(int indelLen) { }

	@Override
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	@Override
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype) {
		this.strandSpecifictype = strandSpecifictype;
	}

	/**
	 * 返回链的方向
	 * @return
	 */
	private String[] getStrandSpecifictype() {
		if (lsRightFq == null || lsRightFq.isEmpty()) {
			return null;
		}
		String[] cmd = null;
		if (strandSpecifictype == StrandSpecific.NONE) {
			
		} else if (strandSpecifictype == StrandSpecific.FIRST_READ_TRANSCRIPTION_STRAND) {
			cmd = new String[]{"--fr"};
		} else if (strandSpecifictype == StrandSpecific.SECOND_READ_TRANSCRIPTION_STRAND) {
			cmd = new String[]{"--rf"};
		}
		return cmd;
	}
	
	@Override
	public void setInsert(int insert) {
	}

	@Override
	public void setLeftFq(List<FastQ> lsLeftFq) {
		if (lsLeftFq == null) return;
		this.lsLeftFq = lsLeftFq;
	}

	@Override
	public void setRightFq(List<FastQ> lsRightFq) {
		if (lsRightFq == null) return;
		this.lsRightFq = lsRightFq;
	}

	@Override
	public void setMismatch(int mismatch) {
	}

	@Override
	public void mapReads() {
	}

	@Override
	public SoftWare getSoftWare() {
		return SoftWare.hisat2;
	}

	@Override
	public void setGtf_Gene2Iso(String gtfFile) {
		GffHashGene gffHashGene = new GffHashGene(gtfFile);
		spliceTxt = PathDetail.getTmpPathRandom() + FileOperate.getFileName(gtfFile);
		spliceTxt = FileOperate.changeFileSuffix(spliceTxt, "_spliceSite", "txt");
		setSpliceTxt(gffHashGene, spliceTxt);
	}

	@Override
	public String getFinishName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static void setSpliceTxt(GffHashGene gffHashGene, String spliceTxt) {
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
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(spliceTxt, true);
		for (String string : lsResult) {
			String[] ss = string.split(SepSign.SEP_ID);
			txtWrite.writefileln(ss);
		}
		txtWrite.close();
	}
	
	public static Map<String, Integer> getMapSensitive() {
		return MapBowtie.getMapSensitive();
	}
	
	public static void index(String chrFile) {
		
	}
}

class MapHisatIndex {
	String chrFile;
	
	public void setChrFile(String chrFile) {
		this.chrFile = chrFile;
	}
	
	public void creatIndex() {
		
	}
}
