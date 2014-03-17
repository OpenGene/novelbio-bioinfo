package com.novelbio.analysis.seq.wig;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Map;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;

public class SamFileToWig {
	private static final Logger log = Logger.getLogger(SamFileToWig.class);
	
	public static void main(String[] args) {
		SamFile samFile = new SamFile("/media/winE/NBC/Project/Project_MaHong/huangqiyue/col_accepted_hits.bam");
		samFile.indexMake();
		SamFileToWig samFileToWig = new SamFileToWig();
		samFileToWig.setSamFile(samFile);
		samFileToWig.setOutputFile("/media/winE/NBC/Project/Project_MaHong/huangqiyue/info.wig");
		try {
			samFileToWig.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean defaultZero = false;
	
	public int chunkSize = 10_000_000;
	public String outputFile;
	
	SamFile samFile;
	
	/**
	 * 必须输入排序并index的sam文件
	 * @param samFile
	 */
	public void setSamFile(SamFile samFile) {
		this.samFile = samFile;
	}
	public void setDefaultZero(boolean defaultZero) {
		this.defaultZero = defaultZero;
	}
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
	
	public final void run() throws IOException {
		TrackHeader header = TrackHeader.newWiggle();
		header.setName("Processed " + samFile.getFileName());
		header.setDescription("Processed " + samFile.getFileName());
		Map<String, Long> mapChrID2Len = samFile.getMapChrID2Length();
		try (WigFileWriter writer = new WigFileWriter(outputFile, header)) {
			for (String chr : samFile.getMapChrID2Length().keySet()) {
				processChromosome(samFile, writer, chr, mapChrID2Len);
			}
			writer.close();
		}

	}

	private void processChromosome(SamFile samfile, WigFileWriter writer, String chr, Map<String, Long> mapChrID2Len) throws IOException {
		int chunkStart = 1;
		while (chunkStart < mapChrID2Len.get(chr)) {
			int chunkStop = (int) Math.min(chunkStart+chunkSize-1, mapChrID2Len.get(chr));
			float[] result = compute(samfile, chr, chunkStart, chunkStop);
			// Write the count at each base pair to the output file
			writer.write(new Contig(chr, chunkStart, chunkStop, result));
			// Process the next chunk
			chunkStart = chunkStop + 1;
		}
	}

	/**
	 * Do the computation on a chunk and return the results
	 * Must return chunk.length() values (one for every base pair in chunk)
	 * 
	 * @param chunk the interval to process
	 * @return the results of the computation for this chunk
	 * @throws IOException
	 * @throws WigFileException
	 */
	public float[] compute(SamFile samFile, String chrId, int start, int end) throws IOException {
		float[] sum = new float[end - start + 1];
		int[] count = new int[sum.length];
		
		for (SamRecord samRecord : samFile.readLinesOverlap(chrId, start, end)) {
			for (Align align : samRecord.getAlignmentBlocks()) {
				int entryStart = Math.max(start, align.getStartAbs());
				int entryStop = Math.min(end, align.getEndAbs());
				for (int i = entryStart; i <= entryStop; i++) {
					sum[i-start] += 1/(float)samRecord.getMappedReadsWeight();
					count[i-start]++;
				}
			}
		}
		// Calculate the average at each base pair in the chunk
		for (int i = 0; i < sum.length; i++) {
			if (count[i] != 0 || !defaultZero) {
				sum[i] /= count[i];
			}
		}
		return sum;
	}
	
}
