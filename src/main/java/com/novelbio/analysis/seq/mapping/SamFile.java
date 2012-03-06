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

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

import net.sf.picard.filter.FilteringIterator;
import net.sf.samtools.BAMIndexer;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.BlockCompressedInputStream;
import net.sf.samtools.util.BlockCompressedStreamConstants;
import net.sf.samtools.util.IOUtil;
/**
 * 提取为bed文件时，仅仅考虑f-r情况
 * 待检查，特别是转换成bed文件时是否精确到了1bp，譬如起点是否为开区间
 * @author zong0jie
 *
 */
public class SamFile {
	Logger logger = Logger.getLogger(SamFile.class);
	String fileName = "";
	boolean getPairedBed = false;
	/**
	 * 单端延长240bp
	 */
	int extend = 240;
	/**
	 * 双端数据是否获得连在一起的bed文件
	 * 如果输入是单端数据，则将序列延长返回bed文件
	 * 注意：如果是双端文件，<b>不能预先排序</b>
	 * @param getPairedBed
	 */
	public void setGetPairedBed(boolean getPairedBed) {
		this.getPairedBed = getPairedBed;
	}
	/**
	 * mapping质量为10
	 */
	int mapQuality = 10;
	public void setBedInfo(boolean pairendExtend, int mapQuality, int uniqMapping) {
		
	}
	
	SAMFileReader samFileReader;
	boolean bamFile = false;
	public SamFile(String samBamFile) {
		File file = new File(samBamFile);
		samFileReader = new SAMFileReader(file);
		this.fileName = samBamFile;
		
		final BufferedInputStream bufferedStream;
		if (file != null)
			try {
				bufferedStream = new BufferedInputStream(new FileInputStream(file));
				this.bamFile = isBAMFile(bufferedStream);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public SAMFileHeader.SortOrder SORT_ORDER;
	 /**
	  * 根据后缀名保存为sam或bam
	  * @param outFile
	  */
	public void sort(String outFile) {
		File fileOut = new File(outFile);
		long n = 0;
		samFileReader.getFileHeader().setSortOrder(SORT_ORDER);
		/**
		 * makeSAMOrBAMWriter() 根据后缀名保存为sam或bam
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
    
	 /** 按照需要排序并保存
	  * @param outFile
	  */
	public void sort(String outFile, boolean bam) {
		File fileOut = new File(outFile);
		long n = 0;
		samFileReader.getFileHeader().setSortOrder(SORT_ORDER);
		SAMFileWriter writer = null;
		/**
		 * makeSAMOrBAMWriter() 根据后缀名保存为sam或bam
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
	 * 将sam文件压缩为bam文件
	 * 如果是bam文件，则返回
	 */
	public void compress() {
		if (bamFile) {
			return;
		}
		
		
	}
	/**
	 * 待检查
	 */
	public void index()
	{
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
	 *<b>非uniq mapping只支持bwa的结果</b>
	 * 返回单端
	 * 将sam文件改为bed文件，根据mapping质量和正反向进行筛选
	 * <b>不能挑选跨染色体的融合基因<b>
	 * @param bedFileCompType bed文件的压缩格式，TxtReadandWrite.TXT等设定
	 * @param bedFile 最后产生的bedFile
	 * @param uniqMapping 是否为uniqmapping
	 * 如果不是uniqmapping，那么mapping数量在第七列
	 * @return
	 * 	/**
	 * 将一行的信息提取为bed文件的格式
	 * 起点从0开始，默认mapping数目为1
	 * @return
	 * 0:chrID
	 * 1:start
	 * 2:end
	 * 3:Mismatching positions/bases
	 * 4:CIGAR  M 0 alignment match (can be a sequence match or mismatch)
I   1 insertion to the reference 
D 2 deletion from the reference
N 3 skipped region from the reference
S 4 soft clipping (clipped sequences present in SEQ)
H  5 hard clipping (clipped sequences NOT present in SEQ)
P  6 padding (silent deletion from padded reference)
=  7 sequence match 
X 8 sequence mismatch

5: strand
6: mapping reads数，1表示uniqmapping
	 */
	public BedSeq sam2bed(String bedFileCompType, String bedFile, boolean uniqMapping, boolean extend) {
		TxtReadandWrite txtBed = new TxtReadandWrite(bedFileCompType, bedFile, true);
		for (SAMRecord samRecord : samFileReader) {
			if (samRecord.getReadUnmappedFlag() || samRecord.getMappingQuality() < mapQuality) {
				continue;
			}
			if (samRecord.getAttribute("XA") == null ) {
				String strand = "+"; int start = samRecord.getAlignmentStart(); int end = samRecord.getAlignmentEnd();
				if (samRecord.getReadNegativeStrandFlag()) {
					strand = "-";
				}
				if (extend) {
					int[] startend = getLoc(samRecord.getAlignmentStart(), samRecord.getAlignmentEnd(), !samRecord.getMateNegativeStrandFlag());
					start = startend[0];
					end = startend[1];
				}
				String tmpResult = samRecord.getReferenceName() + "\t" + start + "\t" + end
						+ "\t" + samRecord.getAttribute("MD") + "\t" + samRecord.getCigarString() + "\t" + strand + "\t1";
				if (getSeqName) {
					tmpResult = tmpResult + "\t" + samRecord.getReferenceName();
				}
				txtBed.writefileln(tmpResult);
			}
			else if (!uniqMapping && samRecord.getAttribute("XA") != null ) {
				 String[] tmpInfo = samRecord.getAttribute("XA").toString().split(";");
				 //添加新的信息
				 for (String string : tmpInfo) {
					String[] tmpResult = null;
					if (getSeqName) {
						tmpResult = new String[8];
					}
					else {
						tmpResult = new String[7];
					}
					String[] info = string.split(",");
					tmpResult[0] = info[0];
					int start1 = Integer.parseInt(info[1].substring(1)) -1;
					int end1 =  start1 + samRecord.getReadLength();
					if (extend) {
						int[] startend = getLoc(start1, end1, info[1].charAt(0) == '+');
						start1 = startend[0];
						end1 = startend[1];
					}
					tmpResult[1] = start1 + "";
					tmpResult[2] =  end1 + "";
					try {
						tmpResult[3] = info[2];
					} catch (Exception e) {
						tmpResult[3] = "none";
					}
					tmpResult[4] = info[2];
					tmpResult[5] = info[1].charAt(0)+"";
					tmpResult[6] = tmpInfo.length + 1 + "";
					 if (getSeqName) {
						 tmpResult[7] = samRecord.getReadName();
					 }
						txtBed.writefileln(tmpResult);
				 }
			}
		}
		txtBed.close();
		BedSeq bedSeq = new BedSeq(bedFile);
		return bedSeq;
	}
	
	/**
	 * 返回双端，如果是单端文件，则返回延长的单端
	 * 将sam文件改为bed文件，根据mapping质量和正反向进行筛选
	 * <b>不能挑选跨染色体的融合基因<b>
	 * @param bedFileCompType bed文件的压缩格式，TxtReadandWrite.TXT等设定
	 * @param bedFile 最后产生的bedFile
	 * @param uniqMapping 是否为uniqmapping
	 * 如果不是uniqmapping，那么mapping数量在第七列
	 * @return
	 * 	/**
	 * 将一行的信息提取为bed文件的格式
	 * 起点从0开始，默认mapping数目为1
	 * @return
	 * 0:chrID
	 * 1:start
	 * 2:end
	 * 3:Mismatching positions/bases
	 * 4:CIGAR  M 0 alignment match (can be a sequence match or mismatch)
I   1 insertion to the reference 
D 2 deletion from the reference
N 3 skipped region from the reference
S 4 soft clipping (clipped sequences present in SEQ)
H  5 hard clipping (clipped sequences NOT present in SEQ)
P  6 padding (silent deletion from padded reference)
=  7 sequence match 
X 8 sequence mismatch

5: strand
6: 
	 */
	public BedSeq sam2bedPairEnd(String bedFileCompType, String bedFile) {
		TxtReadandWrite txtBed = new TxtReadandWrite(bedFileCompType, bedFile, true);
		SAMRecord samRecordOld = null;
		for (SAMRecord samRecord : samFileReader) {
			if (samRecord.getReadUnmappedFlag()) {
				continue;
			}
			if (samRecordOld == null) {
				samRecordOld = samRecord;
				continue;
			}
			String tmpResult = null;
			//相同名字，说明是一对，则根据方向选择起点和终点
			if (samRecordOld.getReadName().equals(samRecord.getReadName())) {
				if (samRecordOld.getReadNegativeStrandFlag() == samRecord.getReadNegativeStrandFlag()) {
					samRecordOld = null;
					continue;
				}
				String strand = "+"; int start = 0; int end = 0;
				if (samRecordOld.getReadNegativeStrandFlag()) {
					strand = "-";
					start = samRecord.getAlignmentStart();
					end = samRecordOld.getAlignmentEnd();
				}
				else {
					start = samRecordOld.getAlignmentStart();
					end = samRecord.getAlignmentEnd();
				}
				tmpResult = samRecordOld.getReferenceName() + "\t" + start + "\t" + end
						+ "\t" + samRecordOld.getAttribute("MD") + "\t" + samRecordOld.getCigarString() + "\t" + strand + "\t1";

				//清空
				samRecordOld = null;
			}
			//说明不是一对，另一条缺失了，
			else {
				String strand = "+"; int start = 0; int end = 0;
				if (samRecord.getReadNegativeStrandFlag()) {
					strand = "-";
				}
				int[] startend = getLoc(samRecordOld.getAlignmentStart(), samRecordOld.getAlignmentEnd(), !samRecordOld.getReadNegativeStrandFlag());
				start = startend[0]; end = startend[1];
				tmpResult = samRecordOld.getReferenceName() + "\t" + start + "\t" + end
						+ "\t" + samRecordOld.getAttribute("MD") + "\t" + samRecordOld.getCigarString() + "\t" + strand + "\t1";
				
				samRecordOld = samRecord;
			}
			
			if (getSeqName) {
				tmpResult = tmpResult + "\t" + samRecordOld.getReadName();
			}
			txtBed.writefile(tmpResult, false);
		}
		txtBed.close();
		BedSeq bedSeq = new BedSeq(bedFile);
		return bedSeq;
	}
	/**
	 * 给定samRecord，根据extend的长度和方向，返回start，end
	 * @param samRecord
	 * @return
	 * int[2] 0: start
	 * 1: end
	 */
	private int[] getLoc(int start, int end, boolean Strand)
	{
		int[] result = new int[2];
		if (end - start >= extend) {
			result[0] = start;
			result[1] = end;
		}
		else {
			//正向
			if (Strand) {
				result[0] = start;
				result[1] = start + extend;
			
			}
			//反向
			else {
				result[0] = end - extend;
				result[1] = end;
			}
		}
		return result;
	}
	
	boolean getSeqName = false;
	/**
	 * 是否在bed文件的最后一列加上seq的名字
	 * @param getSeqName
	 */
	public void setGetSeqName(boolean getSeqName) {
		this.getSeqName = getSeqName;
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