package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;

/**
 *  Usage: hisat [options]* -x <bt2-idx> {-1 <m1> -2 <m2> | -U <r>} [-S <sam>] <br>
 *  e.g.:  ./hisat -x ./Puccinia_striiformis -1 CYR32_2_filtered_1.fq.gz -2 CYR32_2_filtered_1.fq.gz > CYR32_HISAT.sam <br>
 *  <bt2-idx>  Index filename prefix (minus trailing .X.bt2). <br>
 *  <m1>       Files with #1 mates, paired with files in <m2>. Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2). <br>
 *  <m2>       Files with #2 mates, paired with files in <m1>. Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2). <br>
 *  <r>        Files with unpaired reads. Could be gzip'ed (extension: .gz) or bzip2'ed (extension: .bz2). <br>
 *  <sam>      File for SAM output (default: stdout) <br>
 *  PS: <m1>, <m2>, <r> can be comma-separated lists (no whitespace) and can be specified many times.  E.g. '-U file1.fq,file2.fq -U file3.fq'. <br>
 *  重要参数说明： <br>
 *  -q  输入文件是FASTQ 格式， .fq/.fastq <br>
 *  --qseq 	输入文件是 Illumina's qseq 格式 <br>
 *  -f 	输入文件是(multi-) FASTA .fa/.mfa <br>
 *  -r 	输入文件是 one-sequence-per-line  <br>
 *  -s/--skip<int> 	跳过第<int>几条 reads/pairs <br>
 *  -5/--trim5<int>	trim <int> bases from 5'/left end of reads (0) <br>
 *  -3/--trim3<int>	trim <int> bases from 3'/right end of reads (0) <br>
 *  --phred33          qualities are Phred+33 (default) <br>
 *  --phred64          qualities are Phred+64 <br>
 *  
 *  Presets: <br>
 * For --end-to-end: <br>
 *  --very-fast            -D 5 -R 1 -N 0 -L 22 -i S,0,2.50 <br>
 *  --fast                 -D 10 -R 2 -N 0 -L 22 -i S,0,2.50 <br>
 *  --sensitive            -D 15 -R 2 -N 0 -L 22 -i S,1,1.15 (default) <br>
 *  --very-sensitive       -D 20 -R 3 -N 0 -L 20 -i S,1,0.50 <br>
 * For --local: <br>
 *  --very-fast-local      -D 5 -R 1 -N 0 -L 25 -i S,1,2.00 <br>
 *  --fast-local           -D 10 -R 2 -N 0 -L 22 -i S,1,1.75 <br>
 *  --sensitive-local      -D 15 -R 2 -N 0 -L 20 -i S,1,0.75 (default) <br>
 *  --very-sensitive-local -D 20 -R 3 -N 0 -L 20 -i S,1,0.50 <br>
 *  Alignment: <br>
  	  -N <int>           max # mismatches in seed alignment; can be 0 or 1 (0) <br>
     -L <int>           length of seed substrings; must be >3, <32 (22) <br>
     -i <func>          interval between seed substrings w/r/t read len (S,1,1.15) <br>
     --n-ceil <func>    func for max # non-A/C/G/Ts permitted in aln (L,0,0.15) <br>
     --dpad <int>       include <int> extra ref chars on sides of DP table (15) <br>
     --gbar <int>       disallow gaps within <int> nucs of read extremes (4) <br>
     --ignore-quals     treat all quality values as 30 on Phred scale (off) <br>
     --nofw             do not align forward (original) version of read (off) <br>
     --norc             do not align reverse-complement version of read (off) <br>
 * Spliced Alignment: <br>
  	  --pen-cansplice <int>              penalty for a canonical splice site (0) <br>
     --pen-noncansplice <int>           penalty for a non-canonical splice site (12) <br>
     --pen-intronlen <func>             penalty for long introns (G,-8,1) <br>
     --min-intronlen <int>              minimum intron length (20) <br>
     --max-intronlen <int>              maximum intron length (500000) <br>
     --known-splicesite-infile <path>   provide a list of known splice sites <br>
     --novel-splicesite-outfile <path>  report a list of splice sites <br>
     --novel-splicesite-infile <path>   provide a list of novel splice sites <br>
     --no-temp-splicesite               disable the use of splice sites found <br>
     --no-spliced-alignment             disable spliced alignment <br>
     --rna-strandness <string>          Specify strand-specific information (unstranded) <br>
 * Paired-end: <br>
     -I/--minins <int>  minimum fragment length (0) <br>
     -X/--maxins <int>  maximum fragment length (500) <br> 
     --fr/--rf/--ff     -1, -2 mates align fw/rev, rev/fw, fw/fw (--fr) <br>
     --no-mixed         suppress unpaired alignments for paired reads <br>
     --no-discordant    suppress discordant alignments for paired reads <br>
     --no-dovetail      not concordant when mates extend past each other <br>
     --no-contain       not concordant when one mate alignment contains other <br>
     --no-overlap       not concordant when mates overlap at all <br>
 * */

public class MapHisat implements MapRNA {
	private static Logger logger = Logger.getLogger(MapHisat.class);
	public static final int Sensitive_Very_Fast = 11;
	public static final int Sensitive_Fast = 12;
	public static final int Sensitive_Sensitive = 13;
	public static final int Sensitive_Very_Sensitive = 14;
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
	int sensitive = Sensitive_Sensitive;
	
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
	
	boolean isLocal = true;
	String exePathHist = "";
	List<FastQ> lsLeftFq = new ArrayList<>();
	List<FastQ> lsRightFq = new ArrayList<>();
	
	String chrFile = "";
	String outputSam = "";
	
	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}
	
	public void setOutputSam(String outputSam) {
		this.outputSam = outputSam;
	}
	
	
	public MapHisat() {
		SoftWareInfo softMapSplice = new SoftWareInfo();
		softMapSplice.setName(SoftWare.hisat);
		this.exePathHist = softMapSplice.getExePathRun();
	}
	
	@Override
	public List<String> getCmdExeStr() {
		// TODO Auto-generated method stub
		
		List<String> lsCmd = new ArrayList<>();
//		lsCmd.add("tophat version: " + getVersionTophat());
//		lsCmd.add(bowtieVersion.toString() + " version: " + mapBowtie.getVersion());
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		lsCmd.add(cmdOperate.getCmdExeStr());
//		if (!lsCmdMapping2nd.isEmpty()) {
//			lsCmd.addAll(lsCmdMapping2nd);
//		}
		return lsCmd;
//		return null;
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
		lsCmd.add(exePathHist + "hisat"); 
		ArrayOperate.addArrayToList(lsCmd, getIndex());
		ArrayOperate.addArrayToList(lsCmd, getInputFileFormat());
		
		ArrayOperate.addArrayToList(lsCmd, getSensitive());
		ArrayOperate.addArrayToList(lsCmd, getOutSam());

//		lsCmd.addAll(getGtfFile());
//		if (bowtieVersion == SoftWare.bowtie2) {
//			ArrayOperate.addArrayToList(lsCmd, getMismatch());
//			lsCmd.addAll(getIndelLen());
//			addLsCmdParam(lsCmd, getSensitive());
//		}
//		addLsCmdParam(lsCmd, getOffset());
//		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
//		ArrayOperate.addArrayToList(lsCmd, getStrandSpecifictype());
//		lsCmd.addAll(getMinCoverageIntron());
//		ArrayOperate.addArrayToList(lsCmd, getMaxCoverageIntron());
//		ArrayOperate.addArrayToList(lsCmd, getMaxSegmentIntron());
//		ArrayOperate.addArrayToList(lsCmd, getOutPathPrefix());
//		lsCmd.add(mapBowtie.getChrNameWithoutSuffix());
//		lsCmd.addAll(getLsFqFile());
		return lsCmd;
	}
	
	private String[] getIndex() {
		return new String[]{"-x",};
	}
	
	private String getChrFile() {
		return chrFile;
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
	
	private String[] getSensitive() {
		if (isLocal) {
			if(sensitive == Sensitive_Very_Fast) {
				return new String[]{"--very-fast-local"};
			} else if (sensitive == Sensitive_Fast) {
				return new String[]{"--fast-local"};
			} else if (sensitive == Sensitive_Very_Sensitive) {
				return new String[]{"--very-sensitive-local"};
			} else {
				return new String[]{"--sensitive-local"};
			}
		} else {
			if(sensitive == Sensitive_Very_Fast) {
				return new String[]{"--very-fast"};
			} else if (sensitive == Sensitive_Fast) {
				return new String[]{"--fast"};
			} else if (sensitive == Sensitive_Very_Sensitive) {
				return new String[]{"--very-sensitive"};
			} else {
				return new String[]{"--sensitive"};
			}
		}
	}
	
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
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRefIndex(String chrFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOutPathPrefix(String outPathPrefix) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIndelLen(int indelLen) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setThreadNum(int threadNum) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInsert(int insert) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLeftFq(List<FastQ> lsLeftFq) {
		// TODO Auto-generated method stub
		if (lsLeftFq == null) return;
		this.lsLeftFq = lsLeftFq;
	}

	@Override
	public void setRightFq(List<FastQ> lsRightFq) {
		// TODO Auto-generated method stub
		if (lsRightFq == null) return;
		this.lsRightFq = lsRightFq;
	}

	@Override
	public void setMismatch(int mismatch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mapReads() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SoftWare getBowtieVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGtf_Gene2Iso(String gtfFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFinishName() {
		// TODO Auto-generated method stub
		return null;
	}

}
