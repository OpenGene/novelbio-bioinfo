package com.novelbio.analysis.seq.sam.pileup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.dataStructure.Alignment;

public class SamPileUp {
	public static void main(String[] args) {
		SamFile samFile = new SamFile("C:\\Users\\jie\\Desktop\\paper\\KOod.bam");
//		samFile.indexMake();chr3:81,944,867-c
		for (SamRecord samRecord : samFile.readLinesContained("chr3", 81944867, 81955050)) {
			System.out.println(samRecord.toString());
		}
	}
	/** 最多可能插入的碱基 */
	int maxBaseNum = 100;
	Map<String, Long> mapChrIDlowcase2Length;
	
	private Queue<SamRecord> queueSamRecord = new ConcurrentLinkedQueue<SamRecord>();

	/**
	 * 设定染色体名称与长度的对照表，注意key为小写
	 * @param mapChrIDlowcase2Length
	 */
	public void setMapChrIDlowcase2Length(
			Map<String, Long> mapChrIDlowcase2Length) {
		this.mapChrIDlowcase2Length = mapChrIDlowcase2Length;
	}
	
	private double[] getRangeInfo(String chrID, int startNum, int endNum, int binNum, int type) {
		int[] startEnd = MapReadsAbs.correctStartEnd(mapChrIDlowcase2Length, chrID, startNum, endNum);
		if (startEnd == null) return null;
		
		for (SamRecord samRecord : queueSamRecord) {
			ArrayList<Align> lsAlign = samRecord.getAlignmentBlocks();
		}
		return null;
	}
	/**
	 * 迭代读取bam文件的coverage
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<Integer> readPerlines(final List<Alignment> lsAlignments) throws Exception {
//		return new Iterable<Integer>() {
//			
//			public Iterator<Integer> iterator() {
//				return new Iterator<Integer>() {
//					public boolean hasNext() {
//						return line != null;
//					}
//					public Integer next() {
//						String retval = line;
//						line = getLine();
//						return retval;
//					}
//					public void remove() {
//						throw new UnsupportedOperationException();
//					}
//					Integer getLine() {
//						String line = null;
//						try {
//							line = bufread.readLine();
//						} catch (IOException ioEx) {
//							line = null;
//						}
//						if (line == null) {
//							close();
//						}
//						return line;
//					}
//					Integer line = getLine();
//				};
//			}
//		};
		
		return null;
	}
	

}


class ItCoverage implements Iterable<Integer> {
	/** 需要统计的region，key为小写 */
	Map<String, Queue<Alignment>> mapChrID2LsAlignment;
	String thisChrID = "";
	Queue<Alignment> lsCalRegionThis;
	Queue<SamRecord> queueSamRecord;
	SamPileupReading samPileupReading;
	LinkedList<Integer> lsTmpResult = new LinkedList<Integer>();
	int numFlag = 1;//指示当前读到的位置
	
	boolean isFinished;
	
	@Override
	public Iterator<Integer> iterator() {
//		return new Iterator<Integer>() {
//			Integer coverage = getLine();
//			
//			public boolean hasNext() {
//				return !isFinished;
//			}
//			
//			public Integer next() {
//				String retval = line;
//				line = getLine();
//				return retval;
//			}
//			
//			public void remove() {
//				throw new UnsupportedOperationException();
//			}
//			
//			Integer getLine() {
//				SamRecord samRecord = queueSamRecord.peek();
//				if (!samRecord.getRefID().equals(thisChrID)) {
//					lsCalRegionThis = mapChrID2LsAlignment.get(samRecord.getRefID().toLowerCase());
//				}
//				queueSamRecord.
//				
//				
//				
//				
//				
//				
//				
//				
//				
//				
//				String line = null;
//				try {
//					line = bufread.readLine();
//				} catch (IOException ioEx) {
//					line = null;
//				}
//				if (line == null) {
//					close();
//				}
//				return line;
//			}
//	
		
//		};
		return null;
	}
}
