package com.novelbio.analysis.seq.genome.gffOperate;

import java.util.ArrayList;
import java.util.Hashtable;

public abstract class GffHashGene extends GffHash
{
	/**
	 * @param upBp Tss���ζ���bp
	 * @return
	 */
	public abstract ArrayList<Long> getGeneStructureLength(int upBp);
}
