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
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.QualityEncoding.PhredEncoding;

public class BasicStats extends FastQCmodules implements QCModule {

	private String name = null;
	private int actualCount = 0;
	private int filteredCount = 0;
	private int minLength = 0;
	private int maxLength = 0;
	private long gCount = 0;
	private long cCount = 0;
	private long aCount = 0;
	private long tCount = 0;
	private long nCount = 0;
	private char lowestChar = 126;
	private String fileType = null;
	
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
	
	public double getGCpersentage() {
		if (aCount+tCount+gCount+cCount > 0) {
			return ((gCount+cCount)*100)/(aCount+tCount+gCount+cCount);
		}
		else {
			return 0.0;
		}
	}
	
	public String getFileType() {
		return fileType;
	}
	
	public String getEncoding() {
		return PhredEncoding.getFastQEncodingOffset(lowestChar).toString();
	}
	
	public String getName() {
		return name;
	}
	
	public int getReadsNum() {
		return actualCount;
	}
	public long getBaseNum() {
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
				"File type",
				"Encoding",
				"Total Sequences",
//				"Filtered Sequences",
				"Sequence length",
				"%GC",
		};		
		
		// Sequence - Count - Percentage
		public int getColumnCount() {
			return 2;
		}
	
		public int getRowCount() {
			return rowNames.length;
		}
	
		public String getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0: return rowNames[rowIndex];
				case 1:
					switch (rowIndex) {
//					case 0 : return name;
					case 0 : return fileType;
					case 1 : return PhredEncoding.getFastQEncodingOffset(lowestChar).toString();
					case 2 : return ""+actualCount;
//					case 4 : return ""+filteredCount;
					case 3 :
						if (minLength == maxLength) {
							return ""+minLength;
						}
						else {
							return minLength+"-"+maxLength;
						}
						
						
					case 4 : 
						if (aCount+tCount+gCount+cCount > 0) {
							return ""+(((gCount+cCount)*100)/(aCount+tCount+gCount+cCount));
						}
						else {
							return 0+"";
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
	

}
