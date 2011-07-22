package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.util.ArrayList;



/**
 * UCSC konwn gene的基因坐标信息
 * @author zong0jie
 *
 */
public class GffCodGene extends GffCodAbs
{
	protected GffCodGene(String chrID, int Coordinate) {
		super(chrID, Coordinate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public GffDetailGene getGffDetailUp() {
		GffDetailGene gffDetailGene = (GffDetailGene)gffDetailUp;
		return gffDetailGene;
	}

	@Override
	public GffDetailGene getGffDetailThis() {
		GffDetailGene gffDetailGene = (GffDetailGene)gffDetailThis;
		return gffDetailGene;
	}

	@Override
	public GffDetailGene getGffDetailDown() {
		GffDetailGene gffDetailGene = (GffDetailGene)gffDetailDown;
		return gffDetailGene;
	}
	

}
