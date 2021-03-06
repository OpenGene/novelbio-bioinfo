/**
 * Copyright Copyright 2010-12 Simon Andrews
 *
 *    This file is part of FastQC.
 *
 *    FastQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    FastQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with FastQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package uk.ac.babraham.FastQC.Modules;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.QualityEncoding.PhredEncoding;

import com.novelbio.ExceptionResultFileError;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class BasicStats extends FastQCmodules implements QCModule {
	private static String titFileType = "File Type";
	private static String titEncoding = "Encoding";
	private static String titTotalSeq = "Total Sequences";
	private static String titTotalBase = "Total Bases";
	private static String titSeqLen = "Sequence length";
	private static String titGCpercent = "%GC";
	
	private String name = null;
	private int actualCount = 0;
	private int filteredCount = 0;
	private int minLength = 0;
	private int maxLength = 0;
	
	private long allBase = 0;
	private double gcPercentage = 0;
	private String encoding = "";
	
	
	private long gCount = 0;
	private long cCount = 0;
	private long aCount = 0;
	private long tCount = 0;
	private long nCount = 0;
	private long allCount = 0;
	private long gcCount = 0;
	private char lowestChar = 126;
	private String fileType = null;
	
	public void fillBasicStats(String txtBasicFile) {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(txtBasicFile);
		for (String content : txtReadandWrite.readlines()) {
			String[] ss = content.split("\t");
			//TODO 填充allCount gcCount actualCount等
			
		}
	}
	public BasicStats add(BasicStats basicStats) {
		BasicStats allBasicStats = new BasicStats();
		allBasicStats.name = name;
		allBasicStats.actualCount = actualCount + basicStats.actualCount;
		allBasicStats.filteredCount = filteredCount + basicStats.filteredCount;
		allBasicStats.minLength = Math.min(minLength, basicStats.minLength);
		allBasicStats.maxLength = Math.max(maxLength, basicStats.maxLength);
		allBasicStats.gcCount = gcCount + allBasicStats.gcCount;
		allBasicStats.cCount = cCount + allBasicStats.cCount;
		allBasicStats.gCount = gCount + allBasicStats.gCount;
		allBasicStats.aCount = aCount + allBasicStats.aCount;
		allBasicStats.tCount = tCount + allBasicStats.tCount;
		allBasicStats.nCount = nCount + allBasicStats.nCount;
		allBasicStats.allCount = allCount + allBasicStats.allCount;
		return allBasicStats;
	}
	
	public String description() {
		return "Calculates some basic statistics about the file";
	}
	
	public boolean ignoreFilteredSequences() {
		return false;
	}

	
	public void reset () {
		minLength = 0;
		maxLength = 0;
		gCount = 0;
		cCount = 0;
		aCount = 0;
		tCount = 0;
		nCount = 0;
		allCount = 0;
		gcCount = 0;
		
		allBase = 0;
		gcPercentage = 0;
	}

	public String name() {
		return "Basic Statistics";
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public synchronized void processSequence(Sequence sequence) {

		if (name == null) name = sequence.file().name();
		
		// If this is a filtered sequence we simply count it and move on.
//		if (sequence.isFiltered()) {
//			filteredCount++;
//			return;
//		}
		
		actualCount++;
		
		if (fileType == null) {
			if (sequence.getColorspace() != null) {
				fileType = "Colorspace converted to bases";
			}
			else {
				fileType = "Conventional base calls";
			}
		}
		
		if (actualCount == 1) {
			minLength = sequence.getSequence().length();
			maxLength = sequence.getSequence().length();
		}
		else {
			if (sequence.getSequence().length() < minLength) minLength = sequence.getSequence().length();
			if (sequence.getSequence().length() > maxLength) maxLength = sequence.getSequence().length();
		}

		char [] chars = sequence.getSequence().toCharArray();
		for (int c=0;c<chars.length;c++) {			
			switch (chars[c]) {
				case 'G': ++gCount;break;
				case 'A': ++aCount;break;
				case 'T': ++tCount;break;
				case 'C': ++cCount;break;
				case 'N': ++nCount;break;			
			}
		}
		
		chars = sequence.getQualityString().toCharArray();
		for (int c=0;c<chars.length;c++) {
			if (chars[c] < lowestChar) {
				lowestChar = chars[c];
			}
		}
	}
	
	public boolean raisesError() {
		return false;
	}

	public boolean raisesWarning() {
		return false;
	}

	public BufferedImage getBufferedImage(int width,int height) {
		return null;
	}
	
	public int getMinLength() {
		return minLength;
	}
	public int getMaxLength() {
		return maxLength;
	}
	
	public double getGCpersentage() {
		if (gcPercentage > 0) {
			return gcPercentage;
		}
		
		allCount = getBaseNum();
		if (allCount > 0) {
			return ((gCount+cCount)*100)/allCount;
		}
		else {
			return 0.0;
		}
	}
	
	public String getFileType() {
		return fileType;
	}
	
	public String getEncoding() {
		if (!StringOperate.isRealNull(encoding)) {
			return encoding;
		}
		return PhredEncoding.getFastQEncodingOffset(lowestChar).toString();
	}
	
	public String getName() {
		return name;
	}
	
	public int getReadsNum() {
		return actualCount;
	}
	public long getBaseNum() {
		if (allBase > 0) {
			return allBase;
		}
		return aCount+tCount+gCount+cCount;
	}
	public String getSeqLen() {
		if (minLength == maxLength) {
			return ""+minLength;
		}
		else {
			return minLength+"-"+maxLength;
		}
	}
	
	public Map<String, String> getResult() {
		//TODO 计算 allCount，GC比，等
		ResultsTable table = new ResultsTable();
		Map<String, String> mapResult = new LinkedHashMap<String, String>();
		for (int i = 1; i < table.getRowCount(); i++) {
			if(i == 0)
				mapResult.put(table.getColumnName(0), table.getColumnName(1));
				mapResult.put( table.getValueAt(i,0), table.getValueAt(i,1));
		}
		return mapResult;
	}
	
	private class ResultsTable extends AbstractTableModel {
				
		private static final long serialVersionUID = 1L;
		
		private String [] rowNames = new String [] {
//				"Filename",
				titFileType,//执行不到，因为外面是 i 从1开始 循环
				titEncoding,
				titTotalSeq,
				titTotalBase,
//				"Filtered Sequences",
				titSeqLen,
				titGCpercent,
		};		
		
		// Sequence - Count - Percentage
		public int getColumnCount() {
			return 2;
		}
	
		public int getRowCount() {
			return rowNames.length;
		}
	
		public String getValueAt(int rowIndex, int columnIndex) {
			allCount = getBaseNum();
			switch (columnIndex) {
				case 0: return rowNames[rowIndex];
				case 1:
					switch (rowIndex) {
//					case 0 : return name;
					case 0 : return fileType;
					case 1 : return getEncoding();
					case 2 : return ""+ actualCount;
					case 3 : return ""+ allCount;
//					case 4 : return ""+filteredCount;
					case 4 :
						if (minLength == maxLength) {
							return ""+minLength;
						}
						else {
							return minLength+"-"+maxLength;
						}
					case 5 : 
						if (allCount > 0) {
							return "" + getGCpersentage();
						}
						else {
							return 0 + "";
						}
					
					}
			}
			return null;
		}
		
		public String getColumnName (int columnIndex) {
			switch (columnIndex) {
				case 0: return "Measure";
				case 1: return "Value";
			}
			return null;
		}
		
		public Class<?> getColumnClass (int columnIndex) {
			switch (columnIndex) {
			case 0: return String.class;
			case 1: return String.class;
		}
		return null;
			
		}
	}
	
	/** 读取basicStats的内容，填充本类
	 * @param basicStatsFile
	 * @param isFirst 是否为第一条链，true读取第二行，false读取第三行
	 */
	public void readTable(String basicStatsFile, boolean isFirst) {
		TxtReadandWrite txtRead = new TxtReadandWrite(basicStatsFile);
		Map<String, Integer> mapName2ColNum = null;
		int rowNum = 0;
		
		for (String content : txtRead.readlines()) {
			if (rowNum == 0) {
				mapName2ColNum = getMapName2ColNum(content);
			} else if(isFirst && rowNum == 1) {
				fillInfo(basicStatsFile, mapName2ColNum, content);
			} else if (!isFirst && rowNum == 2 ) {
				fillInfo(basicStatsFile, mapName2ColNum, content);
			}
			
			if (rowNum > 3) {
				break;
			}
			rowNum++;
		}
		txtRead.close();
	}
	
	private Map<String, Integer> getMapName2ColNum(String content) {
		Map<String, Integer> mapName2ColNum = new HashMap<>();
		String[] ss = content.trim().split("\t");
		for (int i = 0; i < ss.length; i++) {
			String title = ss[i];
			mapName2ColNum.put(title, i);
		}
		return mapName2ColNum;
	}
	
	private void fillInfo(String fileName, Map<String, Integer> mapName2ColNum, String content) {
		String[] ss = content.trim().split("\t");
		if (ss.length != mapName2ColNum.size()) {
			throw new ExceptionResultFileError("basicstats file not correct:" + fileName + "  detailInfo: line column number is not equals title column number");
		}
		for (String string : mapName2ColNum.keySet()) {
			int colNum = mapName2ColNum.get(string);
			String tmpValue = ss[colNum];
			if (string.equals(titEncoding)) {
				encoding = tmpValue;
			} else if (string.equals(titFileType)) {
				fileType = tmpValue;
			} else if (string.equals(titGCpercent)) {
				gcPercentage = Double.parseDouble(tmpValue);
			} else if (string.equals(titTotalBase)) {
				allBase = Long.parseLong(tmpValue);
			} else if (string.equals(titTotalSeq)) {
				actualCount = Integer.parseInt(tmpValue);
			} else if (string.equals(titSeqLen)) {
				String[] len = tmpValue.split("-");
				minLength = Integer.parseInt(len[0]);
				if (len.length > 1) {
					maxLength = Integer.parseInt(len[1]);
				} else {
					maxLength = Integer.parseInt(len[0]);
				}
			}
		}
	}
	
	@Override
	public String getSavePath(String outPrefix) {
		return outPrefix + "basicStats.xls";
	}
	

}
