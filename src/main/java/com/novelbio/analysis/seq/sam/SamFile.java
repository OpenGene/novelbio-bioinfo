package com.novelbio.analysis.seq.sam;

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
import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.fastq.FastQRecord;
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
	SamReader samReader = new SamReader();
	SAMFileWriter samFileWriter;
	
	public SAMFileHeader.SortOrder SORT_ORDER;
	
	/** �Ƿ�Ϊbam�ļ� */
	boolean bamFile = false;
	
	boolean uniqMapping = false;
	
	SamFileStatistics samFileStatistics;
	
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
		if (file != null) {
			try {
				bufferedStream = new BufferedInputStream(new FileInputStream(file));
				this.bamFile = isBAMFile(bufferedStream);
				bufferedStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		samReader.setFileName(samFileExist);
	}
	public String getFileName() {
		return fileName;
	}
	public String getName() {
		return samReader.getName();
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
		return samReader.isPairend();
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


	public SamFileStatistics getStatistics() {
		samFileStatistics = new SamFileStatistics();
		samFileStatistics.setSamFile(this);
		samFileStatistics.statistics();
		return samFileStatistics;
	}
	
	public Iterable<SamRecord> readLines() {
		return samReader.readLines();
	}
	public Iterable<SamRecord> readLines(int num) {
		return samReader.readLines(num);
	}
	/**
	 * ע���Сд����
	 * @param ReadName reads�����֣�ֻҪд�ؼ��ʾ�����
	 * @return û�ҵ��ͷ���null
	 */
	public SamRecord getReads(String ReadName) {
		return samReader.getReads(ReadName);
	}
	public SAMFileHeader getHeader() {
		return samReader.getsamfilehead();
	}
	/**
	 * ��ȡsam�ļ���û��mapping�ϵ�reads�����䱣��Ϊ����fastq�ļ�����������Ĭ��Ϊ�е�
	 * @param getNonUniq �Ƿ񽫷�uniq��Ҳ��ȡ����
	 * @return
	 */
	public FastQ getUnMappedReads(boolean getNonUniq, String outFastQfile) {
		FastQ fastQ = new FastQ(outFastQfile, true);
		for (SamRecord samRecord : samReader.readLines()) {
			if (!samRecord.isMapped() || (getNonUniq && !samRecord.isUniqueMapping())) {
				FastQRecord fastQRecord = samRecord.toFastQRecord();
				fastQ.writeFastQRecord(fastQRecord);
			}
		}
		fastQ.close();
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
		SAMFileHeader samFileHeader = samReader.getsamfilehead();
		samFile.setSamFileNew(samFileHeader, outSamFile, true);
		
		LinkedHashMap<String, SamRecord> mapName2Record = new LinkedHashMap<String, SamRecord>();
		for (SamRecord samRecord : samReader.readLines()) {
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

	 /**
	  * ���ݺ�׺������Ϊsam��bam
	  * ʵ���Ͽ��ǵ���samtools����������д�����
	  * @param outFile
	  */
	public void sort(String outFile) {
		//TODO
		
	}
	 /** ������Ҫ���򲢱���
	  * @param outFile
	  */
	public void sort(String outFile, boolean bam) {
		//TODO
	}
	/**
	 * ��ûʵ��
	 * ��sam�ļ�ѹ��Ϊbam�ļ�
	 * �����bam�ļ����򷵻�
	 */
	public void compress() {
		//TODO
	}
	/**
	 * �����
	 */
	public void index() {
		//TODO
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
		for (SamRecord samRecord : samReader.readLines()) {
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
		//TODO
		return null;
	}
	
	
	public void writeSamRecord(SamRecord samRecord) {
		if (samRecord == null) {
			return;
		}
		samFileWriter.addAlignment(samRecord.getSamRecord());
	}
	public void close() {
		samReader.close();
		try { samFileWriter.close(); } catch (Exception e) { }
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