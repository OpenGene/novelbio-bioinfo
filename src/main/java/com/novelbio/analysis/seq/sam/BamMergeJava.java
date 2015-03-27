package com.novelbio.analysis.seq.sam;

import htsjdk.samtools.MergingSamRecordIterator;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMFileReader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SamFileHeaderMerger;
import htsjdk.samtools.SamReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.PathDetail;
import com.novelbio.base.fileOperate.FileOperate;

/** java实现的merge
 * 根据 picard.sam.MergeSamFiles改装而来
 * @author novelbio
 *
 */
public class BamMergeJava implements BamMergeInt {
	private static final Logger logger = Logger.getLogger(BamMergeJava.class);
	List<String> lsBamFile = new ArrayList<String>();
	String outFileName;
	
	/** 能否按照坐标进行排序 */
	SAMFileHeader.SortOrder SORT_ORDER = SortOrder.coordinate;
	
	//=====================  以下是中间变量 ====================
	/** 用于MergingSamRecordIterator提samrecord文件的list */
	private Collection<SamReader> lsReaders = new ArrayList<>();
	private List<SAMFileHeader> lsHeaders = new ArrayList<SAMFileHeader>();
	
	private boolean MERGE_SEQUENCE_DICTIONARIES = false;
	/** MergingSamRecordIterator 类所使用的参数 */
	private boolean mergingSamRecordIteratorAssumeSorted = false;
	/** 输出的head中的sortOrder */
	private SortOrder sortOrder;
	/** 输入文件是否已经排序 */
	private boolean presorted = false;
	
   /** Option to create a background thread to encode, " +
            "compress and write to disk the output file. The threaded version uses about 20% more CPU and decreases " +
            "runtime by ~20% when writing out a compressed BAM file */
    private boolean USE_THREADING = false;
    
    @Override
    public void addBamFile(String bamFile) {
    	lsBamFile.add(bamFile);
    }
    public void setLsBamFile(List<String> lsBamFile) {
		this.lsBamFile = lsBamFile;
	}
	/** 如果后缀不为bam，则文件后缀自动添加.bam */
	public void setOutFileName(String outFileName) {
		outFileName = outFileName.trim();
		if (!outFileName.endsWith(".bam")) {
			if (!outFileName.endsWith(".")) {
				outFileName = outFileName + ".";
			}
			outFileName = outFileName + "bam";
		}
		this.outFileName = outFileName;
	}
    public void setSORT_ORDER(SAMFileHeader.SortOrder SORT_ORDER) {
		SORT_ORDER = SORT_ORDER;
	}
    
    public SamFile mergeSam() {
    	prepareReader();
		setSortOrderInfo();
    	merge();
    	if (!FileOperate.isFileExistAndBigThanSize(outFileName, 0)) {
			throw new SamErrorException("cannot merge file: " + outFileName);
		}
    	SamFile samFile = new SamFile(outFileName);
    	return samFile;
    }
    
    /** 准备工作 */
	private void prepareReader() {
		lsReaders.clear();
		lsHeaders.clear();
		
		SAMSequenceDictionary dict = null;
		presorted = true;
		for (String bamFile : lsBamFile) {
			SamFile samFile = new SamFile(bamFile);
			SAMFileHeader header = samFile.getHeader();
            // A slightly hackish attempt to keep memory consumption down when merging multiple files with
            // large sequence dictionaries (10,000s of sequences). If the dictionaries are identical, then
            // replace the duplicate copies with a single dictionary to reduce the memory footprint. 
            if (dict == null) {
                dict = header.getSequenceDictionary();
            }
            else if (dict.equals(header.getSequenceDictionary())) {
            	header.setSequenceDictionary(dict);
            }
            presorted = presorted && header.getSortOrder() == SORT_ORDER;
            lsReaders.add(samFile.getSamReader().getSamFileReader());
            lsHeaders.add(header);
		}
	}
	
	/** 设定一系列相关信息 */
	private void setSortOrderInfo() {
        if (presorted || SORT_ORDER == SortOrder.unsorted) {
        	logger.info("Input files are in same order as output so sorting to temp directory is not needed.");
        	sortOrder = SORT_ORDER;
            mergingSamRecordIteratorAssumeSorted = presorted;
        } else {
        	logger.info("Sorting input files using temp directory " + PathDetail.getTmpPath());
        	sortOrder = SAMFileHeader.SortOrder.unsorted;
            mergingSamRecordIteratorAssumeSorted = false;
        }
	}
	
	/** 开始合并 */
	private void merge() {
		final SamFileHeaderMerger headerMerger = new SamFileHeaderMerger(sortOrder, lsHeaders, MERGE_SEQUENCE_DICTIONARIES);
		final MergingSamRecordIterator iterator = new MergingSamRecordIterator(headerMerger, lsReaders, mergingSamRecordIteratorAssumeSorted);
		final SAMFileHeader header = headerMerger.getMergedHeader();
		header.setSortOrder(SORT_ORDER);
		final SAMFileWriterFactory samFileWriterFactory = new SAMFileWriterFactory();
		if (USE_THREADING) samFileWriterFactory.setUseAsyncIo(true);
		
		File file = FileOperate.getFile(outFileName);
		final SAMFileWriter out = samFileWriterFactory.makeSAMOrBAMWriter(header, presorted, file);

	        // Lastly loop through and write out the records
		while (iterator.hasNext()) {
			final SAMRecord record = iterator.next();
			out.addAlignment(record);
		}

		logger.info("Finished reading inputs.");
		out.close();
	}
	
	@Override
	public List<String> getCmdExeStr() {
		return new ArrayList<String>();
	}
	@Override
	public void clear() {
		lsBamFile.clear();
		
	}
}
