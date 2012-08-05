package com.novelbio.analysis.seq.mapping;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FastQ;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.FastQRecord;
import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

import net.sf.picard.filter.FilteringIterator;
import net.sf.samtools.BAMIndexer;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.BlockCompressedInputStream;
import net.sf.samtools.util.BlockCompressedStreamConstants;
import net.sf.samtools.util.IOUtil;
/**
 * ��ȡΪbed�ļ�ʱ����������f-r���
 * ����飬�ر���ת����bed�ļ�ʱ�Ƿ�ȷ����1bp��Ʃ������Ƿ�Ϊ������
 * @author zong0jie
 *
 */
public class SamFile {
	public static void main(String[] args) {
		
		String sam = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/heartK0a14m1_1/accepted_hits.bam";
		SamFile samFile = new SamFile(sam);
		samFile.name();
//		
//		sam = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/heartK0a14m1_2/accepted_hits.bam";
//		samFile = new SamFile(sam);
//		lsMappingInfo = samFile.getMappingInfo();
//		txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(sam, "_statistics", "txt"), true);
//		txtOut.ExcelWrite(lsMappingInfo);
//		txtOut.close();
//		
//		sam = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/heartWTa14m1_1/accepted_hits.bam";
//		samFile = new SamFile(sam);
//		lsMappingInfo = samFile.getMappingInfo();
//		txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(sam, "_statistics", "txt"), true);
//		txtOut.ExcelWrite(lsMappingInfo);
//		txtOut.close();
//		
//		sam = "/media/winF/NBC/Project/Project_FY/FYmouse20111122/tophata15m1/heartWTa14m1_2/accepted_hits.bam";
//		samFile = new SamFile(sam);
//		lsMappingInfo = samFile.getMappingInfo();
//		txtOut = new TxtReadandWrite(FileOperate.changeFileSuffix(sam, "_statistics", "txt"), true);
//		txtOut.ExcelWrite(lsMappingInfo);
//		txtOut.close();

	}
	
	
	public static final int MAPPING_ALLREADS = 2;
	public static final int MAPPING_ALLMAPPEDREADS = 4;
	public static final int MAPPING_UNMAPPED = 8;
	public static final int MAPPING_UNIQUE = 16;
	public static final int MAPPING_REPEAT = 32;
	
	Logger logger = Logger.getLogger(SamFile.class);
	String fileName = "";
	Boolean pairend;
	/**
	 * ��unique mapping�������Ƿ�ֻ�����ȡһ��
	 * @param notUniqueRandomSelectReads
	 */
	boolean uniqueRandomSelectReads = true;
	/** mapping����Ϊ0 */
	int mapQualityFilter = 0;
	/**
	 * ��ȡsam�ļ����࣬��ò�Ҫֱ���ã���getSamFileReader()��������
	 */
	SAMFileReader samFileReader;
	SAMFileWriter samFileWriter;
	
	public SAMFileHeader.SortOrder SORT_ORDER;
	
	/** �Ƿ�Ϊbam�ļ� */
	boolean bamFile = false;
	
	boolean uniqMapping = true;
	
	boolean countReadsNum = false;
	double allReadsNum = 0;
	double unmappedReadsNum = 0;
	double mappedReadsNum = 0;
	double uniqMappedReadsNum = 0;
	double repeatMappedReadsNum = 0;
	double junctionUniReads = 0;
	double junctionAllReads = 0;
	
	public SamFile() {}
	/**��ȡ�����ļ� */
	public SamFile(String samBamFile) {
		setSamFileRead(samBamFile);
	}
	/** 
	 * �����µ�sam�ļ�
	 * @param samFileHeader
	 * @param samFileCreate
	 * @param sorted ������ļ��Ƿ񾭹�����
	 */
	public void setSamFileNew(SAMFileHeader samFileHeader, String samFileCreate, boolean sorted) {
		this.fileName = samFileCreate;
		SAMFileWriterFactory samFileWriterFactory = new SAMFileWriterFactory();
		samFileWriter = samFileWriterFactory.makeSAMOrBAMWriter(samFileHeader, sorted, new File(samFileCreate));
	}
	public void setSamFileRead(String samFileExist) {
		this.fileName = samFileExist;
		File file = new File(samFileExist);
		
		final BufferedInputStream bufferedStream;
		if (file != null)
			try {
				bufferedStream = new BufferedInputStream(new FileInputStream(file));
				this.bamFile = isBAMFile(bufferedStream);
				bufferedStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	public String getFileName() {
		return fileName;
	}
	/**
	 * �Ƿ�ΪuniqMapping��Ĭ��Ϊtrue
	 * @param uniqMapping
	 */
	public void setUniqMapping(boolean uniqMapping) {
		this.uniqMapping = uniqMapping;
	}
	/**
	 * ��unique mapping�������Ƿ�ֻ�����ȡһ��
	 * @param notUniqueRandomSelectReads
	 */
	public void setUniqueRandomSelectOneRead(boolean uniqueRandomSelectReads) {
		this.uniqueRandomSelectReads = uniqueRandomSelectReads;
	}
	/**
	 * ˫�������Ƿ�������һ���bed�ļ�
	 * ��������ǵ������ݣ��������ӳ�����bed�ļ�
	 * ע�⣺�����˫���ļ���<b>����Ԥ������</b>
	 * @param getPairedBed
	 */
	public boolean isPairend() {
		if (pairend != null) {
			 return pairend;
		}
		int countAll = 500;
		int countLines = 0;
		pairend = false;
		for (SamRecord samRecord : readLines()) {
			countLines++;
			if (countLines > countAll) {
				break;
			}
			if (samRecord.isHavePairEnd()) {
				pairend = true;
				break;
			}
		}
		return pairend;
	}
	//TODO ������
	public void setBedInfo(boolean pairendExtend, int mapQuality, int uniqMapping) {
		
	}
	/**
	 * Ĭ��Ϊ10��Ҳ���趨Ϊ0
	 * @param mapQuality
	 */
	public void setMapQuality(int mapQuality) {
		this.mapQualityFilter = mapQuality;
	}

	public void name() {
		SAMFileHeader samFileHeader = getSamFileReader().getFileHeader();
		System.out.println(samFileHeader.toString());
	}
	private SAMFileReader getSamFileReader() {
		File file = new File(fileName);
		samFileReader = new SAMFileReader(file);
		return samFileReader;
	}
	/**
	 * ����readsNum
	 * @param mappingType MAPPING_ALLREADS��
	 * @return -1��ʾ����
	 */
	public long getReadsNum(int mappingType) {
		if (!countReadsNum) {
			getReadsNum();
			countReadsNum = true;
		}
		if (mappingType == MAPPING_ALLREADS) {
			return (long)allReadsNum;
		}
		if (mappingType == MAPPING_UNMAPPED) {
			return (long)unmappedReadsNum;
		}
		if (mappingType == MAPPING_UNIQUE) {
			return (long)uniqMappedReadsNum;
		}
		if (mappingType == MAPPING_REPEAT) {
			return (long)repeatMappedReadsNum;
		}
		if (mappingType == MAPPING_ALLMAPPEDREADS) {
			return (long)mappedReadsNum;
		}
		return -1;
	}
	public ArrayList<String[]> getMappingInfo() {
		if (!countReadsNum) {
			getReadsNum();
			countReadsNum = true;
		}
		
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(new String[]{"allReadsNum", (long)allReadsNum+""});
		lsResult.add(new String[]{"unmappedReadsNum", (long)unmappedReadsNum+""});
		lsResult.add(new String[]{"mappedReadsNum", (long)mappedReadsNum+""});
		lsResult.add(new String[]{"uniqMappedReadsNum", (long)uniqMappedReadsNum+""});
		lsResult.add(new String[]{"repeatMappedReadsNum", (long)repeatMappedReadsNum+""});
		lsResult.add(new String[]{"junctionAllReads", (long)junctionAllReads+""});
		lsResult.add(new String[]{"junctionUniReads", (long)junctionUniReads+""});

		return lsResult;
	}
	private long getReadsNum() {
		allReadsNum = 0;
		unmappedReadsNum = 0;
		mappedReadsNum = 0;
		uniqMappedReadsNum = 0;
		repeatMappedReadsNum = 0;
		junctionAllReads = 0;
		junctionUniReads = 0;
		
		long readsNum = 0;
		SAMFileReader samFileReader = getSamFileReader();
		for (SamRecord samRecord : readLines()) {
			int readsMappedNum = samRecord.getNumMappedReadsInFile();
			allReadsNum = allReadsNum + (double)1/readsMappedNum;
			if (samRecord.isMapped()) {
				mappedReadsNum = mappedReadsNum + (double)1/readsMappedNum;
				if (samRecord.isUniqueMapping()) {
					uniqMappedReadsNum ++;
					if (samRecord.isJunctionReads()) {
						junctionUniReads ++;
					}
				}
				else {
					repeatMappedReadsNum = repeatMappedReadsNum + (double)1/readsMappedNum;
				}
				if (samRecord.isJunctionReads()) {
					junctionAllReads = junctionAllReads + (double)1/readsMappedNum;
				}
			}
			else {
				unmappedReadsNum = unmappedReadsNum + (double)1/readsMappedNum;
			}
		}
		samFileReader.close();
		return readsNum;
	}
	/**
	 * ע���Сд����
	 * @param ReadName reads�����֣�ֻҪд�ؼ��ʾ�����
	 * @return û�ҵ��ͷ���null
	 */
	public SAMRecord getReads(String ReadName) {
		SAMFileReader samFileReader = getSamFileReader();
		SAMRecordIterator samRecordIterator = samFileReader.iterator();
		while (samRecordIterator.hasNext()) {
			SAMRecord samRecord = null;
			try {
				samRecord = samRecordIterator.next();
			} catch (Exception e) {
				continue;
			}
			if (samRecord.getReadName().contains(ReadName)) {
				samRecordIterator.close();
				samFileReader.close();
				return samRecord;
			}
		}
		return null;
	}

	 /**
	  * ���ݺ�׺������Ϊsam��bam
	  * ʵ���Ͽ��ǵ���samtools����������д�����
	  * @param outFile
	  */
	public void sort(String outFile) {
		SAMFileReader samFileReader = getSamFileReader();
		File fileOut = new File(outFile);
		long n = 0;
		samFileReader.getFileHeader().setSortOrder(SORT_ORDER);
		/**
		 * makeSAMOrBAMWriter() ���ݺ�׺������Ϊsam��bam
		 */
		final SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(samFileReader.getFileHeader(), false, fileOut);
		final Iterator<SAMRecord> iterator = samFileReader.iterator();
		while (iterator.hasNext()) {
			writer.addAlignment(iterator.next());
			if (++n % 10000000 == 0) logger.info("Read " + n + " records.");
		}
		
		logger.info("Finished reading inputs, merging and writing to output now.");
		
		samFileReader.close();
		writer.close();
	}
	/**
	 * ��ȡsam�ļ���û��mapping�ϵ�reads�����䱣��Ϊ����fastq�ļ�����������Ĭ��Ϊ�е�
	 * @param getNonUniq �Ƿ񽫷�uniq��Ҳ��ȡ����
	 * @return
	 */
	public FastQ getUnMappedReads(boolean getNonUniq, String outFastQfile) {
		FastQ fastQ = new FastQ(outFastQfile, true);
		for (SamRecord samRecord : readLines()) {
			if (!samRecord.isMapped() || (getNonUniq && !samRecord.isUniqueMapping())) {
				FastQRecord fastQRecord = samRecord.toFastQRecord();
				fastQ.writeFastQRecord(fastQRecord);
			}
		}
		fastQ.closeWrite();
		close();
		return fastQ;
	}
	public SamFile getSingleUnMappedReads() {
		String out = FileOperate.changeFileSuffix(fileName, "_SingleFile", null);
		return getSingleUnMappedReads(out);
	}
	/**
	 * ������һͷmapping�ϣ�һͷû��mapping�ϵ����У���ͷ����ȡ����д��һ��sam�ļ�
	 */
	public SamFile getSingleUnMappedReads(String outSamFile) {
		if (!isPairend()) {
			return null;
		}
		SamFile samFile = new SamFile();
		SAMFileHeader samFileHeader = getSamFileReader().getFileHeader();
		samFile.setSamFileNew(samFileHeader, outSamFile, true);
		
		LinkedHashMap<String, SamRecord> mapName2Record = new LinkedHashMap<String, SamRecord>();
		for (SamRecord samRecord : readLines()) {
			if (!samRecord.isHavePairEnd()) {
				continue;
			}
			//��һ��samRecordд���ļ�
			if (mapName2Record.containsKey(samRecord.getName())) {
				SamRecord samRecord1 = mapName2Record.get(samRecord.getName());
				if (samRecord1.isPaireReads(samRecord)) {
					samFile.writeSamRecord(samRecord1);
					samFile.writeSamRecord(samRecord);
					mapName2Record.remove(samRecord.getName());
					continue;
				}
			}
			//�ҳ�һ��mappingһ��û��mapping�ļ�¼
			if (samRecord.isMapped() ^ samRecord.isMateMapped() ) {
				mapName2Record.put(samRecord.getName(), samRecord);
			}
			removeMap(5000, mapName2Record);
		}
		samFile.close();
		return samFile;
	}
	/** ���������ɾ�����Խ�Լ�ڴ� */
	private void removeMap(int remainNum, LinkedHashMap<String, SamRecord> mapName2Record) {
		if (mapName2Record.size() <= remainNum) {
			return;
		}
		int num = mapName2Record.size() - remainNum;
		int count = 0;
		ArrayList<String> lsName = new ArrayList<String>();
		for (String recordName : mapName2Record.keySet()) {
			if (count > num) {
				break;
			}
			lsName.add(recordName);
			count++;
		}
		for (String recordName : lsName) {
			mapName2Record.remove(recordName);
		}
	}
    /**
     * @param stream stream.markSupported() must be true
     * @return true if this looks like a BAM file.
     */
    private boolean isBAMFile(final InputStream stream)
            throws IOException {
        if (!BlockCompressedInputStream.isValidFile(stream)) {
          return false;
        }
        final int buffSize = BlockCompressedStreamConstants.MAX_COMPRESSED_BLOCK_SIZE;
        stream.mark(buffSize);
        final byte[] buffer = new byte[buffSize];
        readBytes(stream, buffer, 0, buffSize);
        stream.reset();
        final byte[] magicBuf = new byte[4];
        final int magicLength = readBytes(new BlockCompressedInputStream(new ByteArrayInputStream(buffer)), magicBuf, 0, 4);
        return magicLength == BAMFileConstants.BAM_MAGIC.length && Arrays.equals(BAMFileConstants.BAM_MAGIC, magicBuf);
    }
    
    private static int readBytes(final InputStream stream, final byte[] buffer, final int offset, final int length)
            throws IOException {
            int bytesRead = 0;
            while (bytesRead < length) {
                final int count = stream.read(buffer, offset + bytesRead, length - bytesRead);
                if (count <= 0) {
                    break;
                }
                bytesRead += count;
            }
            return bytesRead;
        }
    
	 /** ������Ҫ���򲢱���
	  * @param outFile
	  */
	public void sort(String outFile, boolean bam) {
		SAMFileReader samFileReader = getSamFileReader();
		File fileOut = new File(outFile);
		long n = 0;
		samFileReader.getFileHeader().setSortOrder(SORT_ORDER);
		SAMFileWriter writer = null;
		/**
		 * makeSAMOrBAMWriter() ���ݺ�׺������Ϊsam��bam
		 */
		if (bam) {
			writer = new SAMFileWriterFactory().makeBAMWriter(samFileReader.getFileHeader(), false, fileOut);
		}
		else {
			writer = new SAMFileWriterFactory().makeSAMWriter(samFileReader.getFileHeader(), false, fileOut);
		}
		final Iterator<SAMRecord> iterator = samFileReader.iterator();
		while (iterator.hasNext()) {
			writer.addAlignment(iterator.next());
			if (++n % 10000000 == 0) logger.info("Read " + n + " records.");
		}
		logger.info("Finished reading inputs, merging and writing to output now.");
		samFileReader.close();
		writer.close();
	}
	
	/**
	 * ��ûʵ��
	 * ��sam�ļ�ѹ��Ϊbam�ļ�
	 * �����bam�ļ����򷵻�
	 */
	public void compress() {
		if (bamFile) {
			return;
		}
	}
	/**
	 * �����
	 */
	public void index() {
		SAMFileReader samFileReader = getSamFileReader();
		if (!bamFile) {
			compress();
		}
		File fileOut = new File(FileOperate.changeFileSuffix(fileName, null, "bai"));
		BAMIndexer indexer = new BAMIndexer(fileOut, samFileReader.getFileHeader());

		samFileReader.enableFileSource(true);
		int totalRecords = 0;

		// create and write the content
		for (SAMRecord rec : samFileReader) {
			if (++totalRecords % 1000000 == 0) {
				logger.info(totalRecords + " reads processed ...");
			}
			indexer.processAlignment(rec);
		}
		indexer.finish();
	}
	/**
	 * �ӵڼ��п�ʼ������ʵ����
	 * @param lines ���linesС��1�����ͷ��ʼ��ȡ
	 * @return
	 */
	public Iterable<SamRecord> readlines(int lines) {
		lines = lines - 1;
		Iterable<SamRecord> itContent = readLines();
		if (lines > 0) {
			for (int i = 0; i < lines; i++) {
				itContent.iterator().hasNext();
			}
		}
		return itContent;
	}
	/**
	 * ������ȡ�ļ�
	 */
	public Iterable<SamRecord> readLines() {
		final SAMRecordIterator samRecordIterator = getSamFileReader().iterator();
		return new Iterable<SamRecord>() {
			public Iterator<SamRecord> iterator() {
				return new Iterator<SamRecord>() {
					@Override
					public boolean hasNext() {
						return samRecordIterator.hasNext();
					}

					@Override
					public SamRecord next() {
						SAMRecord samRecord = null;
						try {
							samRecord = samRecordIterator.next();
						} catch (Exception e) {
							logger.error("����");
							return next();
						}
						
						SamRecord samRecordThis = new SamRecord(samRecord);
						return samRecordThis;
					}
					@Override
					public void remove() {
						samRecordIterator.remove();
					}
				};
			}
		};
	}
	
	public BedSeq toBedSingleEnd() {
		return toBedSingleEnd(TxtReadandWrite.TXT, FileOperate.changeFileSuffix(getFileName(), "", "bed"));
	}
	/**
	 * <b>û�п���bed�ļ��������0����1</b>
	 *<b>��uniq mappingֻ֧��bwa�Ľ��</b>
	 * ���ص���
	 * bed�ļ���score��Ϊmapping quality
	 * <b>������ѡ��Ⱦɫ����ںϻ���</b>
	 * @param bedFileCompType bed�ļ���ѹ����ʽ��TxtReadandWrite.TXT���趨
	 * @param bedFile ��������bedFile
	 * @param extend �Ƿ��ӳ�bed�ļ�
	 * @return
	 */
	public BedSeq toBedSingleEnd(String bedFileCompType, String bedFile) {
		BedSeq bedSeq = new BedSeq(bedFile, true);
		bedSeq.setCompressType(null, bedFileCompType);
		for (SamRecord samRecord : readLines()) {
			if (!samRecord.isMapped() || samRecord.getMapQuality() < mapQualityFilter
					|| (uniqMapping && !samRecord.isUniqueMapping()) ) {
				continue;
			}
			if (uniqueRandomSelectReads) {
				BedRecord bedRecord = samRecord.toBedRecordSE();
				bedSeq.writeBedRecord(bedRecord);
			}
			else {
				ArrayList<BedRecord> lsBedRecord = samRecord.toBedRecordSELs();
				for (BedRecord bedRecord : lsBedRecord) {
					bedSeq.writeBedRecord(bedRecord);
				}
			}
		}
		bedSeq.closeWrite();
		close();
		return bedSeq;
	}
	
	
	/**
	 * tobe checked
	 * ����˫�ˣ�����ǵ����ļ����򷵻��ӳ��ĵ���
	 * ��sam�ļ���Ϊbed�ļ�������mapping���������������ɸѡ
	 */
	public BedSeq sam2bedPairEnd(String bedFileCompType, String bedFile) {
		
		
	}
	
	
	public void writeSamRecord(SamRecord samRecord) {
		if (samRecord == null) {
			return;
		}
		samFileWriter.addAlignment(samRecord.getSamRecord());
	}
	public void close() {
		samFileReader.close();
		samFileWriter.close();
	}
}
/**
 * Constants used in reading & writing BAM files
 */
class BAMFileConstants {
    /**
     * The beginning of a BAMRecord is a fixed-size block of 8 int32s
     */
    static final int FIXED_BLOCK_SIZE = 8 * 4;

    /**
     * BAM file magic number.  This is what is present in the gunzipped version of the file,
     * which never exists on disk.
     */

    static final byte[] BAM_MAGIC = "BAM\1".getBytes();
    /**
     * BAM index file magic number.
     */
    static final byte[] BAM_INDEX_MAGIC = "BAI\1".getBytes();
}