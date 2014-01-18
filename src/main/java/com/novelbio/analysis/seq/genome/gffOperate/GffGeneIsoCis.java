package com.novelbio.analysis.seq.genome.gffOperate;


import org.apache.log4j.Logger;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modgeneid.GeneType;
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
public class GffGeneIsoCis extends GffGeneIsoInfo {
	private static final long serialVersionUID = 8473636267008365629L;
	private static final Logger logger = Logger.getLogger(GffGeneIsoCis.class);
	
	public GffGeneIsoCis() {}
	
	public GffGeneIsoCis(String IsoName, String geneParentName, GeneType geneType) {
		super(IsoName, geneParentName, geneType);
		super.setCis5to3(true);
	}
	public GffGeneIsoCis(String IsoName, String geneParentName, GffDetailGene gffDetailGene, GeneType geneType) {
		super(IsoName, geneParentName, gffDetailGene, geneType);
		super.setCis5to3(true);
	}
	
	@Override
	public int getStartAbs() {
		try {
			return get(0).getStartCis();
		} catch (Exception e) {
			return get(0).getStartCis();
		}
		
	}
	@Override
	public int getEndAbs() {
		return get(size() - 1).getEndCis();
	}

	@Override
	public GffGeneIsoCis clone() {
		GffGeneIsoCis result = null;
		result = (GffGeneIsoCis) super.clone();
		return result;
	
	}

}
