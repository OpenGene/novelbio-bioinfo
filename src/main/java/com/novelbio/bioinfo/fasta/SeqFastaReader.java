package com.novelbio.bioinfo.fasta;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.bioinfo.fastq.ExceptionFastq;

public class SeqFastaReader {
	TxtReadandWrite txtRead;
	
	public SeqFastaReader(String fastaFile) {
		txtRead = new TxtReadandWrite(fastaFile);
	}
	
	public SeqFastaReader(File fastaFile) {
		txtRead = new TxtReadandWrite(fastaFile);
	}
	
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<SeqFasta> readlines(int lines) {
		lines = lines - 1;
		Iterable<SeqFasta> itContent = readPerlines();
		if (lines > 0) {
			for (int i = 0; i < lines; i++) {
				itContent.iterator().hasNext();
			}
		}
		return itContent;
	}
	
	/**
	 * 从第几行开始读，是实际行
	 * @param initial 是否进行初始化
	 * @return
	 */
	public Iterable<SeqFasta> readlines() {
		return readPerlines();
	}
	
	/**
	 * 迭代读取文件
	 * @param initial 是否进行初始化，主要用在多线程过滤reads的时候可以先不初始化，在多线程时候才初始化
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<SeqFasta> readPerlines() {
		final BufferedReader bufread =  txtRead.readfile();
		final long[] lineNum = new long[1];
		final List<String> lsTmp = new ArrayList<>();
		return new Iterable<SeqFasta>() {
			public Iterator<SeqFasta> iterator() {
				return new Iterator<SeqFasta>() {
					SeqFasta seqFasta = getLine();
					public boolean hasNext() {
						return seqFasta != null;
					}
					public SeqFasta next() {
						SeqFasta retval = seqFasta;
						seqFasta = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					SeqFasta getLine() {
						SeqFasta seqFastaNew = null;
						try {
							String content = null;
							while ((content = bufread.readLine()) != null) {
								lineNum[0]++;
								content = content.trim();
								if (StringOperate.isRealNull(content)) continue;
								
								if (content.startsWith(">")) {
									if (!lsTmp.isEmpty()) {
										seqFastaNew = creatSeqFasta(lsTmp);
										lsTmp.clear();
										lsTmp.add(content);
										break;
									}
								}
								lsTmp.add(content);
							}
							//最后一条fasta序列
							if (seqFastaNew == null && !lsTmp.isEmpty()) {
								seqFastaNew = creatSeqFasta(lsTmp);
								lsTmp.clear();
							}							
						} catch (ExceptionSeqFasta e) {
							throw new ExceptionFastq("fasta file error: " + txtRead.getFileName() + " on " + lineNum[0] + " line. \n" + e.getMessage());
						} catch (Exception e) {
							throw new ExceptionFastq("fasta file error: " + txtRead.getFileName() + " on " + lineNum[0] + " line.");
						}
						return seqFastaNew;
					}
				};
			}
		};
	}
	
	public void close() {
		txtRead.close();
	}
	
	protected static SeqFasta creatSeqFasta(List<String> lsSeqInfo) {
		if (lsSeqInfo.isEmpty()) {
			throw new ExceptionSeqFasta();
		}
		SeqFasta seqFasta = new SeqFasta();
		int i = 0;
		for (String line : lsSeqInfo) {
			if (i == 0) {
				if (!line.startsWith(">")) {
					throw new ExceptionSeqFasta("no > at the begining of the name");
				}
				seqFasta.setName(line.replaceFirst(">", ""));
			} else {
				//删除所有非字母的符号
				String tmpSeq = line.replace(" ", "");
				for (char c : tmpSeq.toCharArray()) {
					if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122) ) {
						seqFasta.appendSeq(c);
					}
				}
			}
			i++;
		}
		seqFasta.appendFinish();
		return seqFasta;
	}
	
}
