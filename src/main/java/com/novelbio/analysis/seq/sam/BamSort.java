package com.novelbio.analysis.seq.sam;

import java.io.File;

import net.sf.samtools.SAMFileHeader.SortOrder;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class BamSort {
//	samtools view -bt /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa.fai $SAMFile  > "$SAMPrix".bam
	
	private static final String INPUT = null;
	String ExePath = "";
	String bamFile;
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
	public void setBamFile(String bamFile) {
		this.bamFile = bamFile;
	}
	public String sort(String outFile) {
		String cmd = ExePath + "samtools sort " + "\"" + bamFile + "\"" + " " + "\"" + FileOperate.changeFileSuffix(outFile, "", "") + "\"";
		CmdOperate cmdOperate = new CmdOperate(cmd,"sortBam");
		cmdOperate.run();
		return FileOperate.changeFileSuffix(outFile, "", "") + ".bam";
	}
	
	   
	public SAMFileHeader.SortOrder SORT_ORDER;
	
	private void test(String sortBamFile) {
		File fileOut = new File(sortBamFile);
		SamFile samFile = new SamFile("");
		SAMFileReader reader = samFile.samReader.samFileReader;
        reader.getFileHeader().setSortOrder(SORT_ORDER);
        SAMFileWriter writer = new SAMFileWriterFactory().makeSAMOrBAMWriter(reader.getFileHeader(), false, fileOut);

        ProgressLogger progress = new ProgressLogger(log, (int) 1e7, "Read");
        for (final SAMRecord rec: reader) {
            writer.addAlignment(rec);
            progress.record(rec);
        }

        log.info("Finished reading inputs, merging and writing to output now.");

        reader.close();
        writer.close();
        return 0;
	}
	
}
