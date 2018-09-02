package com.novelbio.software.gbas;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.software.gbas.Allele;
import com.novelbio.software.gbas.PlinkMapReader;

public class TestPlinkMapReader {
	PlinkMapReader plinkMapReader = new PlinkMapReader();
	String plinkBim = "/tmp/test.plink.bim";
	
	@Before
	public void prepare() {
		plinkMapReader.mapChrId2LsGenes = new HashMap<>();
		plinkMapReader.mapChrId2LsGenes.put("chr1", addLsGenes("chr1"));
		plinkMapReader.mapChrId2LsGenes.put("chr2", addLsGenes("chr2"));
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkBim, true);
		List<String> lsTmp = getLsMapInfo("chr2");
		for (String content : lsTmp) {
			txtWrite.writefileln(content);
		}
		lsTmp = getLsMapInfo("chr1");
		for (String content : lsTmp) {
			txtWrite.writefileln(content);
		}
		txtWrite.close();
	}
	@After
	public void after() {
		FileOperate.deleteFileFolder(plinkBim);
	}
	
	private List<GffGene> addLsGenes(String chrId) {
		List<GffGene> lsGenes = new ArrayList<>();
		lsGenes.add(generateGene(chrId, 50, 100));
		lsGenes.add(generateGene(chrId, 200, 300));
		lsGenes.add(generateGene(chrId, 250, 280));
		lsGenes.add(generateGene(chrId, 290, 350));
		lsGenes.add(generateGene(chrId, 400, 600));
		lsGenes.add(generateGene(chrId, 700, 800));
		return lsGenes;
	}
	
	private GffGene generateGene(String chrId, int start, int end) {
		GffGene gffDetailGene = new GffGene();
		gffDetailGene.setChrId(chrId);
		gffDetailGene.setName(start + "_" + end);
		gffDetailGene.setStartAbs(start);
		gffDetailGene.setEndAbs(end);
		return gffDetailGene;
	}
	
	protected static List<String> getLsMapInfo(String chrId) {
		List<String> lsTmp = new ArrayList<>();
		lsTmp.add(chrId + "\ta\tb\t15\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t25\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t50\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t80\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t100\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t200\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t250\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t280\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t300\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t330\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t720\tA\tG");
		lsTmp.add(chrId + "\ta\tb\t850\tA\tG");
		return lsTmp;
	}
	@Test
	public void testPlinkRead() {
		plinkMapReader.setPlinkMap(plinkBim);
		plinkMapReader.initial();
		plinkMapReader.readNextLsAllele();
		GffGene gene = plinkMapReader.getGeneCurrent();
		List<Allele> lsAllele = plinkMapReader.getLsAllelesCurrent();
		assertEquals("50_100", gene.getName());
		assertEquals(3, lsAllele.size());
		assertEquals("chr2\ta\tb\t50\tA\tG", lsAllele.get(0).toString());
		assertEquals("chr2\ta\tb\t80\tA\tG", lsAllele.get(1).toString());
		assertEquals("chr2\ta\tb\t100\tA\tG", lsAllele.get(2).toString());

		plinkMapReader.readNextLsAllele();
		gene = plinkMapReader.getGeneCurrent();
		lsAllele = plinkMapReader.getLsAllelesCurrent();
		assertEquals("200_300", gene.getName());
		assertEquals(4, lsAllele.size());
		assertEquals("chr2\ta\tb\t200\tA\tG", lsAllele.get(0).toString());
		assertEquals("chr2\ta\tb\t250\tA\tG", lsAllele.get(1).toString());
		assertEquals("chr2\ta\tb\t280\tA\tG", lsAllele.get(2).toString());
		assertEquals("chr2\ta\tb\t300\tA\tG", lsAllele.get(3).toString());

		plinkMapReader.readNextLsAllele();
		gene = plinkMapReader.getGeneCurrent();
		lsAllele = plinkMapReader.getLsAllelesCurrent();
		assertEquals("250_280", gene.getName());
		assertEquals(2, lsAllele.size());
		assertEquals("chr2\ta\tb\t250\tA\tG", lsAllele.get(0).toString());
		assertEquals("chr2\ta\tb\t280\tA\tG", lsAllele.get(1).toString());

		plinkMapReader.readNextLsAllele();
		gene = plinkMapReader.getGeneCurrent();
		lsAllele = plinkMapReader.getLsAllelesCurrent();
		assertEquals("290_350", gene.getName());
		assertEquals(2, lsAllele.size());
		assertEquals("chr2\ta\tb\t300\tA\tG", lsAllele.get(0).toString());
		assertEquals("chr2\ta\tb\t330\tA\tG", lsAllele.get(1).toString());
		
		plinkMapReader.readNextLsAllele();
		gene = plinkMapReader.getGeneCurrent();
		lsAllele = plinkMapReader.getLsAllelesCurrent();
		assertEquals("700_800", gene.getName());
		assertEquals(1, lsAllele.size());
		assertEquals("chr2\ta\tb\t720\tA\tG", lsAllele.get(0).toString());
		
		plinkMapReader.readNextLsAllele();
		gene = plinkMapReader.getGeneCurrent();
		lsAllele = plinkMapReader.getLsAllelesCurrent();
		assertEquals("50_100", gene.getName());
		assertEquals(3, lsAllele.size());
		assertEquals("chr1\ta\tb\t50\tA\tG", lsAllele.get(0).toString());
		assertEquals("chr1\ta\tb\t80\tA\tG", lsAllele.get(1).toString());
		assertEquals("chr1\ta\tb\t100\tA\tG", lsAllele.get(2).toString());
	}
	
}
