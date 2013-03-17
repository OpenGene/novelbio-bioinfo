package com.novelbio.analysis.seq.sam;

import java.io.File;

//import net.sf.picard.sam.SortSam;
//import net.sf.picard.util.Log;
import net.sf.samtools.SAMFileHeader.SortOrder;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMFileWriterImpl;
import net.sf.samtools.SAMRecord;

import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class BamSort {
	public static void main(String[] args) {
		SamFile samFile = new SamFile("/media/winF/NBC/Project/Project_ZDB_Lab/QXL/Project_ZDB/mapping/Q60-1.bam");
		samFile.sort("/media/winF/NBC/Project/Project_ZDB_Lab/QXL/Project_ZDB/mapping/Q60-2");
	}
//    private final Log log = Log.getInstance(SortSam.class);
    SAMFileHeader.SortOrder SORT_ORDER = SAMFileHeader.SortOrder.coordinate;
    SamFile samFile;
    int maxRecordsInRam = 500000;
    String ExePath = "";
	
    public void setSamFile(SamFile samFile) {
		this.samFile = samFile;
		SAMFileWriterImpl.setDefaultMaxRecordsInRam(maxRecordsInRam);
		PathDetail.setTmpDir(FileOperate.getParentPathName(samFile.getFileName()));
	}
    
	/**
	 * 设定samtools所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals(""))
			this.ExePath = "";
		else
			this.ExePath = FileOperate.addSep(exePath);
	}
	
	/**
	 * 注意：samtools在排序后并不会修改SO:unsorted这个标签
	 * @param outFile
	 * @return
	 */
	public String sortSamtools(String outFile) {
		SAMFileReader reader = samFile.samReader.getSamFileReader();
		if (reader.getFileHeader().getSortOrder() == SortOrder.coordinate) {
			return samFile.getFileName();
		}
		String cmd = ExePath + "samtools sort " + CmdOperate.addQuot(samFile.getFileName()) + " " 
				+ CmdOperate.addQuot(FileOperate.changeFileSuffix(outFile, "", ""));
		CmdOperate cmdOperate = new CmdOperate(cmd,"sortBam");
		cmdOperate.run();
		return FileOperate.changeFileSuffix(outFile, "", "") + ".bam";
	}
	
	public String sortJava(String sortBamFile) {
		File fileOut = new File(sortBamFile);
		SAMFileReader reader = samFile.samReader.getSamFileReader();
		if (reader.getFileHeader().getSortOrder() == SortOrder.coordinate) {
			return sortBamFile;
		}
        reader.getFileHeader().setSortOrder(SORT_ORDER);
        SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(), false, fileOut);

//        ProgressLogger progress = new ProgressLogger(log, (int) 1e7, "Read");
        for (final SAMRecord rec: reader) {
            writer.addAlignment(rec);
//            progress.record(rec);
        }

//        log.info("Finished reading inputs, merging and writing to output now.");

        reader.close();
        writer.close();
        return sortBamFile;
	}
	
}
