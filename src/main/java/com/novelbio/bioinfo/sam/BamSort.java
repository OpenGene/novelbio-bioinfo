package com.novelbio.bioinfo.sam;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMFileWriterImpl;
import htsjdk.samtools.SAMSequenceDictionary;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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
	int maxRecordsInRam = 2500000;
	String ExePath = "";
		
	public void setSamFile(SamFile samFile) {
		this.samFile = samFile;
		PathDetail.getTmpPathWithSep();
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
		SAMFileHeader header = samFile.getHeader();
		if (header.getSortOrder() == SortOrder.coordinate) {
			return samFile.getFileName();
		}
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(ExePath + "samtools"); lsCmd.add("sort");
		lsCmd.add(samFile.getFileName()); lsCmd.add(FileOperate.changeFileSuffix(outFile, "", ""));
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
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

		SAMFileHeader samFileHeader = samFile.getHeader();
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
		String tmpFile = FileOperate.getFileTmpName(sortBamFile);
		samFileHeader.setSortOrder(SORT_ORDER);
		boolean isFilterUnique = false;
		if (filterUniqeMap && !BamFilterUnique.isUniqueMapped(samFile)) {
			isFilterUnique = true;
			BamFilterUnique.setAttributeUnique(samFileHeader);
		}
		
		OutputStream os = null;
		try {
			os = FileOperate.getOutputStream(tmpFile);
		} catch (IOException e1) {
			throw new ExceptionSamError("cannot open sam out file " + tmpFile);
		}
		
		SamWriter writer = new SamWriter(false, samFileHeader, os, true, maxRecordsInRam);
		int i = 0;
		
		try {
			for (SamRecord rec : samFile.readLines()) {
				if (isFilterUnique && !rec.isUniqueMapping()) {
					continue;
				}
				if (i++ % 10000000 == 0) {
					logger.info("write " + i + " reads");
				}
				if (samSequenceDictionary != null) {
					samReorder.copeReads(rec);
				}
				writer.writeToSamFileln(rec);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			samFile.close();
			writer.close();
		}

		FileOperate.moveFile(true, tmpFile, sortBamFile);
		return sortBamFile;
	}

}
