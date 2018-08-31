package com.novelbio.bioinfo.fastq;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.fastq.FastQ;
import com.novelbio.bioinfo.fastq.FastQFilter;
import com.novelbio.bioinfo.fastq.FastQReadingChannel;

import junit.framework.TestCase;

public class TestFastQReadingChannel {
	List<FastQ[]> lsFastQs = new ArrayList<>();
	String parentPath = "src/test/resources/test_file/fastq/PE/";
	
	@Before
	public void beforeCope() {
		List<String[]> lsFastQLR = new ArrayList<>();
		lsFastQLR.add(new String[]{parentPath + "L_correct.1.fq.gz", parentPath + "R_correct.2.fq.gz"});
		for (String[] strings : lsFastQLR) {
			FastQ[] fastQs = convertFastqFile(strings);
			this.lsFastQs.add(fastQs);
		}
	}
	/** 将输入的文件数组转化为FastQ数组 */
	private static FastQ[] convertFastqFile(String[] fastqFile) {
		if (fastqFile == null) return null;
		
		FastQ[] fastQs = new FastQ[fastqFile.length];
		for (int i = 0; i < fastqFile.length; i++) {
			fastQs[i] = new FastQ(fastqFile[i]);
		}
		return fastQs;
	}
	
	@Test
	public void testRunning() {
		beforeCope();
		FastQ fastQWrite1 = new FastQ(parentPath + "filter1.fq", true);
		FastQ fastQWrite2 = new FastQ(parentPath + "filter2.fq", true);
		
		FastQReadingChannel fastQReadingChannel = new FastQReadingChannel();
		fastQReadingChannel.setCheckFormat(true);

		fastQReadingChannel.setFastQRead(lsFastQs);
		// QC before Filter
//		fastQReadingChannel.setFastQC(fastQCbefore[0], fastQCbefore[1]);
		fastQReadingChannel.setOutputResult(true);
		FastQFilter fastQfilterRecord = new FastQFilter();
		fastQfilterRecord.setQualityFilter(FastQ.FASTQ_QUALITY_CHANGE_TO_BEST);
		fastQfilterRecord.fillLsfFQrecordFilters();
		fastQReadingChannel.setFilter(fastQfilterRecord, lsFastQs.get(0)[0].getOffset());
		// QC after Filter
		fastQReadingChannel.setFastQWrite(fastQWrite1, fastQWrite2);
		
		fastQReadingChannel.setThreadNum(8);
		fastQReadingChannel.run();
		
		FileOperate.deleteFileFolder(parentPath + "filter1.fq");
		FileOperate.deleteFileFolder(parentPath + "filter2.fq");
	}
}
