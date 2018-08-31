package com.novelbio.software.gbas;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.ExceptionNbcFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.RandomFileInt;
import com.novelbio.base.fileOperate.RandomFileInt.RandomFileFactory;
import com.novelbio.bioinfo.fasta.Base;
import com.novelbio.bioinfo.fasta.ExceptionSeqFasta;
import com.novelbio.base.fileOperate.SeekablePathInputStream;

public class PlinkPedReader implements Closeable {

	/**
	 * 一列一个样本，<br>
	 * IRIS_313-15910 IRIS_313-15910 0 0 0 -9 G G T T A A T T T T A A C C G G T T G G<br>
	 * 其中第1列样本名，第2列家系名<br>
	 * 第3,4,5,6基本都是 0 0 0 -9，暂时不用管<br>
	 * 后面每两位是一个locus，每两个snp和{@link #plinkBim} 中的坐标对应
	 * 譬如:
	 * G G <--> 1 1579
	 * T T <--> 1 3044
	 * 以此类推
	 * <br><br>
	 * #==============================================#<br>
	 * 考虑建立索引然后随机读取进行优化<br>
	 */
	String plinkPed;
	SeekablePathInputStream seekablePathInputStream;
	
	/**
	 * key: 种系<br>
	 * value: <br>
	 * 0. 种系起点坐标<br>
	 * 1. 第一个碱基的起点坐标<br>
	 * 2. 本行有多少碱基
	 */
	Map<String, long[]> mapLine2Index = new LinkedHashMap<>();
	
	public PlinkPedReader(String plinkPed) {
		this.plinkPed = plinkPed;
		readIndex(plinkPed + ".index");
		try {
			seekablePathInputStream = FileOperate.getSeekablePathInputStream(plinkPed);
		} catch (Exception e) {
			throw new ExceptionNbcFile("read file error " + plinkPed, e);
		}
	}
	
	@VisibleForTesting
	protected void readIndex(List<String[]> lsIndex) {
		mapLine2Index = readIndexLs(lsIndex);
	}
	@VisibleForTesting
	protected void readIndex(String indexFile) {
		if (FileOperate.isFileExistAndBigThan0(indexFile)) {
			mapLine2Index = readIndexFile(indexFile);
		}
	}
	
	private static Map<String, long[]> readIndexFile(String indexFile) {
		Map<String, long[]> mapLine2Index = new LinkedHashMap<>();
		TxtReadandWrite txtRead = new TxtReadandWrite(indexFile);
		for (String content : txtRead.readlines()) {
			if (content.startsWith("#")) continue;
			String[] ss = content.split("\t");
			long[] index = new long[3];
			index[0] = Long.parseLong(ss[1]);
			index[1] = Long.parseLong(ss[2]);
			index[2] = Long.parseLong(ss[3]);
			mapLine2Index.put(ss[0].trim(), index);
		}
		txtRead.close();
		return mapLine2Index;
	}
	
	private static Map<String, long[]> readIndexLs(List<String[]> lsIndex) {
		Map<String, long[]> mapLine2Index = new LinkedHashMap<>();
		for (String[] contents : lsIndex) {
			long[] index = new long[3];
			index[0] = Long.parseLong(contents[1]);
			index[1] = Long.parseLong(contents[2]);
			index[2] = Long.parseLong(contents[3]);
			mapLine2Index.put(contents[0].trim(), index);
		}
		return mapLine2Index;
	}
	
	public List<String> getLsAllSamples() {
		return new ArrayList<>(mapLine2Index.keySet());
	}
	
	public Iterable<Allele> readAllelsFromSample(String sampleName) {
		return readAllelsFromSample(sampleName, 0);
	}
	/**
	 *  从第几个位置开始，从1开始计数
	 * @param sampleName
	 * @param siteStart
	 * @return
	 */
	public Iterable<Allele> readAllelsFromSample(String sampleName, int siteStart) {
		try {
			return readAllelsFromSampleExp(sampleName, siteStart);
		} catch (Exception e) {
			close();
			throw new ExceptionNbcFile("read file error " + plinkPed, e);
		}
	}
	/**
	 * @param sampleName
	 * @param siteStart 从第几个位置开始，从1开始
	 * @return
	 * @throws IOException 
	 */
	private Iterable<Allele> readAllelsFromSampleExp(String sampleName, int siteStart) throws IOException {
		long[] lineStart2BaseStart = mapLine2Index.get(sampleName);
		if (siteStart <= 0) siteStart = 1;		
		long start = (siteStart-1) * 4 + lineStart2BaseStart[1];
		seekablePathInputStream.seek(start);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(seekablePathInputStream));
		long[] site = new long[]{siteStart};
		long end = lineStart2BaseStart[2];
		
		return new Iterable<Allele>() {
			public Iterator<Allele> iterator() {
				return new Iterator<Allele>() {
					Allele allele = getNext();

					public boolean hasNext() {
						return allele != null;
					}
					public Allele next() {
						Allele retval = allele;
						allele = getNext();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					Allele getNext() {
						if (site[0] > end) {
							return null;
						}
						char[] cAllel = new char[4];
						try {
							int i = bufferedReader.read(cAllel);
							if (i != 4 || cAllel[1] != ' ' || (cAllel[3] != ' ' && cAllel[3] != '\r' && cAllel[3] != '\n')) {
								close();
								throw new ExceptionNbcFile("read plinkped file error " + plinkPed);
							}
						} catch (Exception e) {
							close();
							throw new ExceptionNbcFile("read plinkped file error " + plinkPed);
						}
						Allele allele = new Allele();
						allele.setAllele1(cAllel[0]);
						allele.setAllele2(cAllel[2]);
						allele.setIndex((int) site[0]);
						site[0]++;
						return allele;
					}
				};
			}
		};
	}
	
	/**
	 * @param sampleName
	 * @param siteStart 从第几个位置开始，从1开始计数
	 * @param siteEnd 到第几个位置结束，从1开始计数
	 * @return
	 * @throws IOException 
	 */
	public List<Allele> readAllelsFromSample(String sampleName, int siteStart, int siteEnd) throws IOException {
		List<Allele> lsResult = new ArrayList<>();
		
		long[] lineStart2BaseStart = mapLine2Index.get(sampleName);
		if (siteStart <= 0) siteStart = 1;
		if (siteEnd <=0 || siteEnd > lineStart2BaseStart[2]) siteEnd = (int)lineStart2BaseStart[2];
		
		long start = (siteStart-1) * 4 + lineStart2BaseStart[1];
		long end = siteEnd*4 + lineStart2BaseStart[1];
		seekablePathInputStream.seek(start);
		
		Allele allele = null;
		for (int i = 0; i < end-start; i++) {
			int baseInt = seekablePathInputStream.read();
			if (i%4 == 0) {
				allele = new Allele();
				lsResult.add(allele);
			}
			
			//注意这里allel的ref和alt都是根据plinkPed里面的顺序指定的，并不是实际的ref和alt
			if (i%4 == 0) {
				char base = (char)baseInt;
				if (base == ' ') {
					throw new ExceptionNbcFile("read plinkPed error " + plinkPed);
				}
				allele.setAllele1(base);
			} else if (i%4 == 2) {
				char base = (char)baseInt;
				if (base == ' ') {
					throw new ExceptionNbcFile("read plinkPed error " + plinkPed);
				}
				allele.setAllele2(base);
			}
		}
		return lsResult;
	}
	
	/**
	 * 给定一系列位点，提取某个样本中的实际突变情况
	 * @param sample
	 * @param lsAlleles 从plinkBim中读取的位点
	 * @return
	 */
	public List<Allele> getLsAlleleFromSample(String sample, List<Allele> lsAlleles) {
		if (lsAlleles.isEmpty()) {
			return new ArrayList<>();
		}
		
		List<Allele> lsAlleleResult = new ArrayList<>();

		int start = lsAlleles.get(0).getIndex();

		Iterator<Allele> itAllelesRef = lsAlleles.iterator();
		Iterator<Allele> itAllelesSample = readAllelsFromSample(sample, start).iterator();
		
		Allele alleleRef = itAllelesRef.next();
		Allele alleleSample = itAllelesSample.next();
		while (true) {
			if (alleleRef.getIndex() == alleleSample.getIndex() ) {
				alleleSample.setRef(alleleRef);
				lsAlleleResult.add(alleleSample);
				if (!itAllelesRef.hasNext()) {
					break;
				}
				alleleRef = itAllelesRef.next();
				alleleSample = itAllelesSample.next();
				continue;
			} else if (alleleRef.getIndex() > alleleSample.getIndex()) {
				if (!itAllelesSample.hasNext()) {
					throw new ExceptionNBCPlink("error sample " + sample + " doesnot have " + alleleRef.toString());
				}
				alleleSample = itAllelesSample.next();
				continue;
			} else if (alleleRef.getIndex() < alleleSample.getIndex()) {
				throw new ExceptionNBCPlink("error sample " + sample + " doesnot have " + alleleRef.toString());
			}
		}
		return lsAlleleResult;
	}
	@Override
	public void close() {
		try {
			if (seekablePathInputStream != null) {
				seekablePathInputStream.close();		
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public static List<String> getLsSamples(String plinkPedIndex) {
		Map<String, long[]> mapLine2Index = readIndexFile(plinkPedIndex);
		return new ArrayList<>(mapLine2Index.keySet());
	}
	
	public static String createPlinkPedIndex(String plinkPed) {
		String indexFile = plinkPed + ".index";
		if (!FileOperate.isFileExistAndBigThan0(indexFile)) {
			createPlinkPedIndex(plinkPed, indexFile);
		}
		return indexFile;
	}
	
	public static void createPlinkPedIndex(String plinkPed, String plinkPedIndex) {
		List<String[]> lsIndexes;
		try {
			lsIndexes = createPlinkPedIndexLs(plinkPed);
		} catch (IOException e) {
			throw new ExceptionNbcFile("read plinkPed errpr " + plinkPed, e);
		}
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkPedIndex, true);
		for (String[] index : lsIndexes) {
			txtWrite.writefileln(index);
		}
		txtWrite.close();
	}
	
	/**
	 * 建索引，本方法有错误
	 * index 文件格式如下<br>
	 * line lineStart BaseStart<br>
	 * line: 种系<br>
	 * lineStart 本行的起始坐标，从0开始<br>
	 * lineStart 本行第一个碱基，从0开始<br>
	 * @param plinkPed 输入plinkPed文件
	 * @param plinkIndex 输出的index文件
	 * @throws IOException
	 */
	@VisibleForTesting
	protected static List<String[]> createPlinkPedIndexLs(String plinkPed) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(FileOperate.getInputStream(plinkPed)));
		
		//String[] 其中0: SampleName 1:SampleStart 2:SampleLocStart 3:BaseNum
		List<String[]> lsIndexes = new ArrayList<>();
		String[] sampleIndex =null;
		
		//用来获取 1:SampleStart 2:SampleLocStart 的flag
		boolean isSampleStart = true;
		
		int result = 0;
		long site = 0;
		long baseNum = 0;
		//当出现6个空格的时候，意味着开始读取snp了
		int spaceNum = 0;
		StringBuilder sBuilder = null;
		boolean isCountBase = false;
		while ((result = bufferedReader.read()) > 0) {
			char charInfo = (char)result;
			//TODO 待测试
			if (isSampleStart && charInfo != '\r' && charInfo != '\n') {
				if (sampleIndex != null) {
					sampleIndex[3] = baseNum/2 + "";
					baseNum = 0;
				}
				sampleIndex = new String[4];
				lsIndexes.add(sampleIndex);
				sampleIndex[1] = site+"";
				sBuilder = new StringBuilder();
				isSampleStart = false;
				spaceNum = 0;
			}
			if (spaceNum <= 6) {
				sBuilder.append((char)result);
				if (charInfo == ' ') {
					spaceNum++;
					if (spaceNum == 1) {
						//注意这里样本名不能含有空格
						sampleIndex[0] = sBuilder.toString().trim();
					}
				}
			}

			if (spaceNum == 6) {
				sampleIndex[2] = site +"";
				isCountBase = true;
			}
			
			//不支持苹果的换行符 "\r"
			if (charInfo == '\r' || charInfo == '\n') {
				isCountBase = false;
				isSampleStart = true;
			}
			if (isCountBase && charInfo != ' ') {
				baseNum++;
			}
			site++;
		}
		
		sampleIndex[3] = baseNum/2 + "";
		return lsIndexes;
	}

}
