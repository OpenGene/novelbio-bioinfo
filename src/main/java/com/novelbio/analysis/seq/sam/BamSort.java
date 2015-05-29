package com.novelbio.analysis.seq.sam;

import org.apache.log4j.Logger;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMFileReader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMFileWriterImpl;
import htsjdk.samtools.SAMSequenceDictionary;

import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
//import net.sf.picard.sam.SortSam;
//import net.sf.picard.util.Log;

public class BamSort {
	private static final Logger logger = Logger.getLogger(BamSort.class);
	public static void main(String[] args) {
		SamFile samFile = new SamFile("/media/winF/NBC/Project/Project_ZDB_Lab/QXL/Project_ZDB/mapping/Q60-1.bam");
		samFile.sort("/media/winF/NBC/Project/Project_ZDB_Lab/QXL/Project_ZDB/mapping/Q60-2", false);
	}

	// private final Log log = Log.getInstance(SortSam.class);
	SAMFileHeader.SortOrder SORT_ORDER = SAMFileHeader.SortOrder.coordinate;
	SamFile samFile;

	SAMSequenceDictionary samSequenceDictionary;
	int maxRecordsInRam = 5000000;
	String ExePath = "";

	public void setSamFile(SamFile samFile) {
		this.samFile = samFile;
		SAMFileWriterImpl.setDefaultMaxRecordsInRam(maxRecordsInRam);
		PathDetail.getTmpPath();
	}

	/** 根据输入的samSequenceDictionary的顺序来重新排列samHeader中的染色体顺序，只有当选择java模式时才有作用 */
	public void setSamSequenceDictionary(SAMSequenceDictionary samSequenceDictionary) {
		this.samSequenceDictionary = samSequenceDictionary;
	}

	/**
	 * 设定samtools所在的文件夹以及待比对的路径
	 * 
	 * @param exePath
	 *            如果在根目录下则设置为""或null
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals(""))
			this.ExePath = "";
		else
			this.ExePath = FileOperate.addSep(exePath);
	}

	/**
	 * 注意：samtools在排序后并不会修改SO:unsorted这个标签
	 * 
	 * @param outFile
	 * @return
	 */
	@Deprecated
	public String sortSamtools(String outFile) {
		SAMFileReader reader = samFile.getSamReader().getSamFileReader();
		if (reader.getFileHeader().getSortOrder() == SortOrder.coordinate) {
			return samFile.getFileName();
		}
		String cmd = ExePath + "samtools sort " + CmdOperate.addQuot(samFile.getFileName()) + " "
				+ CmdOperate.addQuot(FileOperate.changeFileSuffix(outFile, "", ""));
		CmdOperate cmdOperate = new CmdOperate(cmd, "sortBam");
		cmdOperate.run();
		return FileOperate.changeFileSuffix(outFile, "", "") + ".bam";
	}

	/**
	 * 
	 * @param sortBamFile
	 * @param filterUniqMap
	 *            是否过滤非unique mapped reads
	 * @return
	 */
	public String sortJava(String sortBamFile, boolean filterUniqeMap) {
		if (FileOperate.isFileExistAndBigThanSize(sortBamFile, 0)) {
			return sortBamFile;
		}

		SAMFileReader reader = samFile.getSamReader().getSamFileReader();
		SAMFileHeader samFileHeader = reader.getFileHeader();
		if (samFileHeader.getSortOrder() == SortOrder.coordinate) {
			return samFile.getFileName();
		}
		SamReorder samReorder = null;
		if (samSequenceDictionary != null) {
			samReorder = new SamReorder();
			samReorder.setSamSequenceDictionary(samSequenceDictionary);
			samReorder.setSamFileHeader(samFileHeader);
			samReorder.reorder();
			samFileHeader = samReorder.getSamFileHeaderNew();
		}

		FileOperate.createFolders(FileOperate.getPathName(sortBamFile));
		String tmpFile = FileOperate.changeFileSuffix(sortBamFile, "_tmp", null);
		samFileHeader.setSortOrder(SORT_ORDER);
		boolean isFilterUnique = false;
		if (filterUniqeMap && !BamFilterUnique.isUniqueMapped(samFile)) {
			isFilterUnique = true;
			BamFilterUnique.setAttributeUnique(samFileHeader);
		}

		SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(samFileHeader, false, FileOperate.getFile(tmpFile));

		for (SamRecord rec : samFile.readLines()) {
			if (isFilterUnique && !rec.isUniqueMapping()) {
				continue;
			}
			if (samSequenceDictionary != null) {
				samReorder.copeReads(rec);
			}
			try {
				writer.addAlignment(rec.getSamRecord());
			} catch (Exception e) {
				logger.error("write error: " + rec.toString() , e);
			}
			
			// progress.record(rec);
		}

		// log.info("Finished reading inputs, merging and writing to output now.");

		reader.close();
		writer.close();
		FileOperate.moveFile(true, tmpFile, sortBamFile);
		return sortBamFile;
	}

}
