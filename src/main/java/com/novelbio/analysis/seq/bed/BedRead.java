package com.novelbio.analysis.seq.bed;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataOperate.TxtReadandWrite.TXTtype;
import com.novelbio.base.fileOperate.FileOperate;

public class BedRead {
	TxtReadandWrite txtRead;
	
	BedRead(String bedFile) {
		txtRead = new TxtReadandWrite(bedFile);
	}
	
	public String getFileName() {
		return txtRead.getFileName();
	}
	/**
	 * 指定bed文件，按照chrID和坐标进行排序<br>
	 * @param sortBedFile 排序后的文件全名<br>
	 * 返回名字为FileOperate.changeFileSuffix(getFileName(), "_sorted", null);
	 */
	public String sort()  {
		String fileName = txtRead.getFileName();		
		String outFile = null;
		fileName = FileOperate.changeFileSuffix(fileName, "_sorted", null);
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #第一列起第一列终止排序，第二列起第二列终止按数字排序,第三列起第三列终止按数字排序
		return sort(outFile);
	}
	
	/**
	 * 指定bed文件，按照chrID和坐标进行排序
	 * @param sortBedFile 排序后的文件全名
	 */
	public String sort(String sortBedFile)  {
		return sortBedFile(1, sortBedFile, 2,3);
	}
	
	/**
	 * 指定bed文件，以及需要排序的列数，产生排序结果
	 * @param chrID ChrID所在的列，从1开始记数，按照字母数字排序
	 * @param sortBedFile 排序后的文件全名
	 * @param arg 除ChrID外，其他需要排序的列，按照数字排序，实际列
	 * @throws Exception
	 */
	public String sortBedFile(int chrID, String sortBedFile, int...arg)  {
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #第一列起第一列终止排序，
		//第二列起第二列终止按数字排序,第三列起第三列终止按数字排序
		String tmpTxt = txtRead.getFileName();
		
		TXTtype txtTtypeThis = TXTtype.getTxtType(txtRead.getFileName());
		if (txtTtypeThis != TXTtype.Txt) {
			tmpTxt = FileOperate.changeFileSuffix(txtRead.getFileName(), "_unzip", "txt");
			txtRead.unZipFile(tmpTxt);
		}
		String cmd = "sort";
		
		if (chrID != 0) {
			cmd = cmd + " -k" + chrID + "," + chrID + " ";
		}
		for (int i : arg) {
			cmd = cmd + " -k" + i + "," + i + "n ";
		}
		
		//TODO 检查是否正确
		if (txtTtypeThis == TXTtype.Gzip) {
			sortBedFile = FileOperate.changeFileSuffix(sortBedFile, "", "");
			cmd = cmd + tmpTxt  +" | gzip -c > " + sortBedFile;
		} else if (txtTtypeThis == TXTtype.Bzip2) {
			sortBedFile = FileOperate.changeFileSuffix(sortBedFile, "", "");
			cmd = cmd + tmpTxt  +" | bzip2 -c > " + sortBedFile;
		} else if (txtTtypeThis == TXTtype.Txt) {
			sortBedFile = FileOperate.changeFileSuffix(sortBedFile, "", "");
			cmd = cmd + tmpTxt + " > " + sortBedFile;
		}
		
		CmdOperate cmdOperate = new CmdOperate(cmd,"sortBed");
		cmdOperate.run();
		return sortBedFile;
	}
 
	/**
	 * 读取前几行，不影响{@link #readLines()}
	 * @param num
	 * @return
	 */
	public ArrayList<BedRecord> readHeadLines(int num) {
		ArrayList<BedRecord> lsResult = new ArrayList<BedRecord>();
		int i = 0;
		for (BedRecord bedRecord : readLines()) {
			if (i >= num) {
				break;
			}
			lsResult.add(bedRecord);
		}
		return lsResult;
	}
	/**
	 * 读取前几行，不影响{@link #readLines()}
	 * @param num
	 * @return
	 */
	public BedRecord readFirstLine() {
		BedRecord bedRecord = readLines().iterator().next();
		close();
		return bedRecord;
	}

	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<BedRecord> readLines(int lines) {
		lines = lines - 1;
		try {
			Iterable<BedRecord> itContent = readPerlines();
			if (lines > 0) {
				for (int i = 0; i < lines; i++) {
					itContent.iterator().hasNext();
				}
			}
			return itContent;
		} catch (Exception e) {
			return null;
		}
	}
	
	public Iterable<BedRecord> readLines() {
		try {
			return readPerlines();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 迭代读取文件
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<BedRecord> readPerlines() throws Exception {
		final BufferedReader bufread =  txtRead.readfile(); 
		return new Iterable<BedRecord>() {
			public Iterator<BedRecord> iterator() {
				return new Iterator<BedRecord>() {
					public boolean hasNext() {
						return bedRecord != null;
					}
					public BedRecord next() {
						BedRecord retval = bedRecord;
						bedRecord = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					BedRecord getLine() {
						BedRecord bedRecord = null;
						try {
							String linestr = bufread.readLine();
							if (linestr == null) {
								return null;
							}
							bedRecord = new BedRecord(linestr);
						} catch (IOException ioEx) {
							bedRecord = null;
						}
						return bedRecord;
					}
					BedRecord bedRecord = getLine();
				};
			}
		};
	}
	
	public void close() {
		txtRead.close();
	}
}
