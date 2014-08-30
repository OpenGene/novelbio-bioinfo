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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import uk.ac.babraham.FastQC.Sequence.Sequence;
import uk.ac.babraham.FastQC.Sequence.Contaminant.ContaminantHit;
import uk.ac.babraham.FastQC.Sequence.Contaminant.ContaminentFinder;

public class OverRepresentedSeqs extends FastQCmodules implements QCModule {

	protected HashMap<String, Integer>sequences = new HashMap<String, Integer>();
	protected int count = 0;
	private OverrepresentedSeq [] overrepresntedSeqs = null;
	private boolean calculated = false;
	private boolean frozen = false;
	private DuplicationLevel duplicationModule;
	
	// This is the number of different sequences we want to track
	private final int OBSERVATION_CUTOFF = 200000;
	// This is a count of how many unique sequences we've seen so far
	// so we know when to stop adding them.
	private int uniqueSequenceCount = 0;
	// This was the total count at the point at which we saw our total
	// number of unique sequences, so we know what to correct by when
	// extrapolating to the whole file
	protected int countAtUniqueLimit = 0;
	
	
	public OverRepresentedSeqs () {
		duplicationModule = new DuplicationLevel(this);
	}
	
	public String description() {
		return "Identifies sequences which are overrepresented in the set";
	}
	
	public boolean ignoreFilteredSequences() {
		return true;
	}
	
	/** duplicate 序列和数量的对照表，从大到小排列
	 * string[2] 0: 数量
	 * 1: 序列
	 * @return
	 */
	public List<String[]> getLsNum2Seq() {
		List<String[]> lsNum2Seq = new ArrayList<String[]>();
		for (String seq : sequences.keySet()) {
			Integer num = sequences.get(seq);
			lsNum2Seq.add(new String[]{num+"", seq});
		}
		//按照序列数量从大到小排序
		Collections.sort(lsNum2Seq, new Comparator<String[]>() {
			public int compare(String[] o1, String[] o2) {
				Integer intO1 = Integer.parseInt(o1[0]);
				Integer intO2 = Integer.parseInt(o2[0]);
				return -intO1.compareTo(intO2);
			}
		});
		return lsNum2Seq;
	}
	
	public DuplicationLevel duplicationLevelModule () {
		return duplicationModule;
	}

	
	public DuplicationLevel getDuplicationLevelModule () {
		return duplicationModule;
	}
	private synchronized void getOverrepresentedSeqs () {

		// If the duplication module hasn't already done
		// its calculation it needs to do it now before
		// we stomp all over the data
		duplicationModule.calculateLevels();
		
		Iterator<String> s = sequences.keySet().iterator();
		List<OverrepresentedSeq>keepers = new ArrayList<OverrepresentedSeq>();
		
		while (s.hasNext()) {
			String seq = s.next();
			double percentage = ((double)sequences.get(seq)/count)*100;
			if (percentage > 0.1) {
				OverrepresentedSeq os = new OverrepresentedSeq(seq, sequences.get(seq), percentage);
				keepers.add(os);
			}
		}
		
		overrepresntedSeqs = keepers.toArray(new OverrepresentedSeq[0]);
		Arrays.sort(overrepresntedSeqs);
		calculated  = true;
		sequences.clear();
		
	}
	
	public void reset () {
		count = 0;
		sequences.clear();
	}

	public String name() {
		return "Overrepresented sequences";
	}

	public synchronized void processSequence(Sequence sequence) {
		
		calculated = false;
		
		++count;
		
		// Since we rely on identity to match sequences we can't trust really long
		// sequences, so anything over 75bp gets truncated to 50bp.
		String seq = sequence.getSequence();
		if (seq.length() > 75) {
			seq = new String(seq.substring(0, 50));
		}
				
		if (sequences.containsKey(seq)) {
			sequences.put(seq, sequences.get(seq)+1);
		}
		else {
			if (! frozen) {
				sequences.put(seq, 1);
				++uniqueSequenceCount;
				countAtUniqueLimit = count;
				if (uniqueSequenceCount == OBSERVATION_CUTOFF) {
					frozen = true;
				}

			}
		}		
	}
	
	private class ResultsTable extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		
		private OverrepresentedSeq [] seqs;
		
		public ResultsTable (OverrepresentedSeq [] seqs) {
			this.seqs = seqs;
		}
		
		
		// Sequence - Count - Percentage
		public int getColumnCount() {
			return 4;
		}

		public int getRowCount() {
			return seqs.length;
		}

		public String getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0: return seqs[rowIndex].seq();
				case 1: return seqs[rowIndex].count()+"";
				case 2: return seqs[rowIndex].percentage()+"";
				case 3: return seqs[rowIndex].contaminantHit();
					
			}
			return null;
		}
		
		public String getColumnName (int columnIndex) {
			switch (columnIndex) {
				case 0: return "Sequence";
				case 1: return "Count";
				case 2: return "Percentage";
				case 3: return "Possible Source";
			}
			return null;
		}
		
		public Class<?> getColumnClass (int columnIndex) {
			switch (columnIndex) {
			case 0: return String.class;
			case 1: return Integer.class;
			case 2: return Double.class;
			case 3: return String.class;
		}
		return null;
			
		}
	}
	
	private class OverrepresentedSeq implements Comparable<OverrepresentedSeq>{
		
		private String seq;
		private int count;
		private double percentage;
		private ContaminantHit contaminantHit;
		
		public OverrepresentedSeq (String seq, int count, double percentage) {
			this.seq = seq;
			this.count = count;
			this.percentage = percentage;
			this.contaminantHit = ContaminentFinder.findContaminantHit(seq);
		}
		
		public String seq () {
			return seq;
		}
		
		public int count () {
			return count;
		}
		
		public double percentage () {
			return percentage;
		}
		
		public String contaminantHit () {
			if (contaminantHit == null) {
				return "No Hit";
			}
			else {
				return contaminantHit.toString();
			}
		}

		public int compareTo(OverrepresentedSeq o) {
			return o.count-count;
		}
	}

	public boolean raisesError() {
		if (!calculated) getOverrepresentedSeqs();
		if (overrepresntedSeqs.length>0) {
			if (overrepresntedSeqs[0].percentage > 1) {
				return true;
			}
		}
		return false;
	}

	public boolean raisesWarning() {
		if (!calculated) getOverrepresentedSeqs();

		if (overrepresntedSeqs.length > 0) return true;
		return false;
	}

	public Map<String, String> getResult() {
		if (!calculated) getOverrepresentedSeqs();
		ResultsTable table = new ResultsTable(overrepresntedSeqs);
		Map<String, String> mapResult = new LinkedHashMap<String, String>();
		for (int i = 0; i < table.getRowCount(); i++) {
			if(i == 0)
				mapResult.put(table.getColumnName(0), table.getColumnName(1));
			mapResult.put( table.getValueAt(i,0), table.getValueAt(i,1));
		}
		return mapResult;
		
	}

	public BufferedImage getBufferedImage(int width, int heigth) {
		// TODO Auto-generated method stub
		return null;
	}

}
