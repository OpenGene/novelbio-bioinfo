package com.novelbio.analysis.seq.rnaseq;

import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.analysis.IntCmdSoft;

public class StringTie  implements IntCmdSoft {
	private static final Logger logger = Logger.getLogger(StringTie.class);
	public static final String tmpFolder = "tmpStringTie/";
	

	
	/** 重构的最短转录本的长度 */
	int minIsoLen = 200;
	/** 最小的anchor length for junctions (默认：10) */
	int minAnchorJuncLen = 10;
	/** 最小的junction coverage */
	int minJuncCoverage = 1;
	/** gap between read mappings triggering a new bundle */
	int gapToNewIso = 50;
	/** output file with reference transcripts that are covered by reads */
	boolean outputExistIso = true;
	
	String gtfFile;
	/** only estimates the abundance of given reference transcripts (requires {@link #gtfFile}) */
	boolean justOutRefIso = false;
	
	int thread = 8;
	/** 输出文件名 */
	String outfile;
	
	@Override
	public List<String> getCmdExeStr() {
		// TODO Auto-generated method stub
		return null;
	}

}
