package com.novelbio.analysis.seq.genomeNew.gffOperate;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.database.model.modgeneid.GeneType;
/**
 * ����ͨͨСд
 * �������ʱ��SnnnC<br>
 * S����CΪ5��S��C�غ�ʱ����Ϊ0<br>
 * CnnnATG<br>
 * C��UTRend�ľ���: ATGsite - coord - 1;//CnnnATG<br>
 * C��ATG�ľ���: coord - ATGsite//CnnnATG<br>
 * ���뱾��������ʼ nnnCnnΪ3������������յ�Ϊ2�����<br>
 * ���뱾��������ʼ CnnΪ0�����<br>
 * @author zong0jie
 *
 */
public class GffGeneIsoCis extends GffGeneIsoInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8473636267008365629L;
	private static final Logger logger = Logger.getLogger(GffGeneIsoCis.class);

	public GffGeneIsoCis(String IsoName, GeneType geneType) {
		super(IsoName, geneType);
		super.setCis5to3(true);
	}
	
	public GffGeneIsoCis(String IsoName, GffDetailGene gffDetailGene, GeneType geneType) {
		super(IsoName, gffDetailGene, geneType);
		super.setCis5to3(true);
	}

	@Override
	public int getStartAbs() {
		return get(0).getStartCis();
	}

	@Override
	public int getEndAbs() {
		return get(size() - 1).getEndCis();
	}
	@Override
	protected String getGFFformatExonMISO(String geneID, String title,
			String strand) {
		String geneExon = "";
		for (int i = 0; i < size(); i++) {
			ExonInfo exons = get(i);
			geneExon = geneExon + getChrID() + "\t" +title + "\texon\t" + exons.getStartAbs() + "\t" + exons.getEndAbs()
		     + "\t"+"."+"\t" +strand+"\t.\t"+ "ID=exon:" + getName()  + ":" + (i+1) +";Parent=" + getName() + " "+TxtReadandWrite.ENTER_LINUX;
		}
		return geneExon;
	}
	@Override
	protected String getGTFformatExon(String geneID, String title, String strand) {
		String geneExon = "";
		for (ExonInfo exons : this) {
			geneExon = geneExon + getChrID() + "\t" +title + "\texon\t" + exons.getStartAbs()  + "\t" + exons.getEndAbs() 
		     + "\t"+"."+"\t" +strand+"\t.\t"+ "gene_id \""+geneID+"\"; transcript_id \""+getName()+"\"; "+TxtReadandWrite.ENTER_LINUX;
		}
		return geneExon;
	}
	@Override
	public GffGeneIsoCis clone() {
		GffGeneIsoCis result = null;
		result = (GffGeneIsoCis) super.clone();
		return result;
	
	}

}
