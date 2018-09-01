package com.novelbio.bioinfo.gff;


import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.database.domain.modgeneid.GeneType;
/**
 * 名字通通小写
 * 计算距离时，SnnnC<br>
 * S距离C为5，S和C重合时距离为0<br>
 * CnnnATG<br>
 * C到UTRend的距离: ATGsite - coord - 1;//CnnnATG<br>
 * C到ATG的距离: coord - ATGsite//CnnnATG<br>
 * 距离本外显子起始 nnnCnn为3个碱基，距离终点为2个碱基<br>
 * 距离本外显子起始 Cnn为0个碱基<br>
 * @author zong0jie
 *
 */
public class GffIsoCis extends GffIso {
	private static final long serialVersionUID = 8473636267008365629L;
	private static final Logger logger = LoggerFactory.getLogger(GffIsoCis.class);
	
	public GffIsoCis() {}
	
	public GffIsoCis(String IsoName, String geneParentName, GeneType geneType) {
		super(IsoName, geneParentName, geneType);
		setCis5to3(true);
	}
	public GffIsoCis(String IsoName, String geneParentName, GffGene gffDetailGene, GeneType geneType) {
		super(IsoName, geneParentName, gffDetailGene, geneType);
		setCis5to3(true);
	}

	@Override
	public GffIsoCis clone() {
		GffIsoCis result = null;
		result = (GffIsoCis) super.clone();
		return result;
	}
}
