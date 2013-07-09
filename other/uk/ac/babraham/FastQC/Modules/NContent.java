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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Map;


import uk.ac.babraham.FastQC.Graphs.BaseGroup;
import uk.ac.babraham.FastQC.Graphs.LineGraph;
import uk.ac.babraham.FastQC.Sequence.Sequence;

public class NContent extends FastQCmodules implements QCModule {

	public long [] nCounts = new long [0];
	public long [] notNCounts = new long [0];
	public boolean calculated = false;
	public double [] percentages = null;
	public String [] xCategories = new String[0];
	
	public boolean ignoreFilteredSequences() {
		return true;
	}
	
	private synchronized void getPercentages () {
		
		BaseGroup [] groups = BaseGroup.makeBaseGroups(nCounts.length);
		
		xCategories = new String[groups.length];

		percentages = new double [groups.length];

		long total;
		long nCount;

		for (int i=0;i<groups.length;i++) {
						
			xCategories[i] = groups[i].toString();

			nCount = 0;
			total = 0;
			
			for (int bp=groups[i].lowerCount()-1;bp<groups[i].upperCount();bp++) {		
				nCount += nCounts[bp];
				total += nCounts[bp];
				total += notNCounts[bp];
			}
			
			percentages[i] = 100*(nCount/(double)total);
		}
				
		calculated = true;
		
	}
		
	public synchronized void processSequence(Sequence sequence) {
		calculated = false;
		char [] seq = sequence.getSequence().toCharArray();
		if (nCounts.length < seq.length) {
			// We need to expand the size of the data structures
			
			long [] nCountsNew = new long [seq.length];
			long [] notNCountsNew = new long [seq.length];

			for (int i=0;i<nCounts.length;i++) {
				nCountsNew[i] = nCounts[i];
				notNCountsNew[i] = notNCounts[i];
			}
			
			nCounts = nCountsNew;
			notNCounts = notNCountsNew;
		}
		
		for (int i=0;i<seq.length;i++) {
			if (seq[i] == 'N') {
				++nCounts[i];
			}
			else {
				++notNCounts[i];
			}
		}
		
	}
	
	public void reset () {
		nCounts = new long[0];
		notNCounts = new long[0];
	}

	public String description() {
		return "Shows the percentage of bases at each position which are not being called";
	}

	public String name() {
		return "Per base N content";
	}

	public boolean raisesError() {
		if (!calculated) getPercentages();
		for (int i=0;i<percentages.length;i++) {
			if (percentages[i] > 20) {
				return true;
			}
		}
		return false;
	}

	public boolean raisesWarning() {
		if (!calculated) getPercentages();
		for (int i=0;i<percentages.length;i++) {
			if (percentages[i] > 5) {
				return true;
			}
		}
		return false;
	}

	public BufferedImage getBufferedImage(int width, int heigth){
		if (!calculated) getPercentages();
		
		BufferedImage b = new BufferedImage(Math.max(width, percentages.length*15),heigth,BufferedImage.TYPE_INT_RGB);
		Graphics g = b.getGraphics();
		
		LineGraph lg = new LineGraph(new double [][] {percentages}, 0d, 100d, "Position in read (bp)", new String [] {"%N"}, xCategories, "N content across all bases");
		lg.paint(g,b.getWidth(),b.getHeight());

		return b;
	}

	public Map<String, String> getResult() {
		// TODO Auto-generated method stub
		return null;
	}

}
