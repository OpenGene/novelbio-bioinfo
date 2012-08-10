package com.novelbio.test.analysis.seq.resequencing;

import junit.framework.TestCase;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.resequencing.MapInfoSnpIndel;
import com.novelbio.analysis.seq.resequencing.SampleDetail;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo;
import com.novelbio.analysis.seq.resequencing.SnpSampleFilter;
import com.novelbio.analysis.seq.resequencing.VcfCols;

public class TestMapInfoSnpIndel extends TestCase {

	MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel();
	@Override
	protected void setUp() throws Exception {
		mapInfoSnpIndel.setGffChrAbs(new GffChrAbs(9606));
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void test() {
		assertReadVcfAndSamPileUpLines();
		assertRawReadSamPileUpLines();
		assertReadsSiteWith3GeneType();
	}
	private void assertReadVcfAndSamPileUpLines() {
		VcfCols vcfCols = new VcfCols();
		SiteSnpIndelInfo siteSnpIndelInfoA;
		SiteSnpIndelInfo siteSnpIndelInfoC;
		
		String vcfLine = "chr6	57512711	rs5001484	G	C	3184.04	TruthSensitivityTranche99.90to100.00	AB=0.543;AC=1;AF=0.50;AN=2;BaseQRankSum=6.555;DB;DP=315;DS;Dels=0.00;FS=2.754;HRun=0;HaplotypeScore=101.0346;MQ=57.84;MQ0=0;MQRankSum=-3.354;QD=10.11;ReadPosRankSum=1.348;SB=-513.84;VQSLOD=-218.9126;culprit=HaplotypeScore	GT:AD:DP:GQ:PL	0/1:119,100:315:99:3214,0,3784";
		mapInfoSnpIndel.setVcfLines("2A", vcfCols, vcfLine);
		
		assertEquals(315, mapInfoSnpIndel.getReadsNumAll());
		assertEquals(119, mapInfoSnpIndel.getReadsNumRef());
		
		siteSnpIndelInfoC = mapInfoSnpIndel.getLsAllenInfoSortBig2Small().get(0);
		assertEquals("c", siteSnpIndelInfoC.getThisSeq().toLowerCase());
		assertEquals(100, siteSnpIndelInfoC.getReadsNum());
		
		mapInfoSnpIndel.setSampleName("2A");
		mapInfoSnpIndel.setSamToolsPilup("chr6	57512711	G	591	C$c$c$Caa$aa$c$.CCCCaaaacaaacc,cC.CCCaaC,aaCCCCaccaacca,c,A.Ac.,$a,cAC$cacc,,aaC.,cc,c,,,,A..A.ACCaC..CAacc,a.C,,c,,a,,aCC,a.aacaacacacacaca,A...ca,,.A..cac,,a.CCCaAA,aa,,Aa,caaca,,a,acca.....,,,,,ca,,,c,,aa,,caa,a,,cccC...,,,,.CA.Cc,,,caca,,a,A...,,.ca,a...a,aaa.c,c,,acc,accaccc,,,,,,,,ac,,,c,cca,AC,,a,a..cC.Cac,A,ac,aCa,,,,c.ca,ac,,aaCAcc,,,,cc,,ca,,c,cac,a,ca,c,,,,cACCC,ccac,aaca,C,,,A.AaaA,,,aaaaacc,,ac,a.,aaacCCc,,caaa,aacca,ccc,c,cca,,ccaaa,aac,aaca,ac,,,a,aca,,aaa,a,ccaaaa,ca,,,aaa,,,a,a,,ac,,c,a,,aaaccac,c,ca,ca,,ca,ca,cc,c,,c,cc,ca,c,c,,,ccac,accccac,,,aaaaacaaaacaaa,,acc,,,ac,,,,,,^],	'!!+.#,!!D30=+===@>==,@,C,=DDD=FFDFFFEEECFFFF.FFFFFFD>EFECFFF*!-HDD?HDFECFF,FFFFEHFEEFBD@FFFFFFDHHHHGFFHHDHHFHHDD3DFEJHHHJFHJJJGJJGJIHFFFJHJJ>HHGHHIJIDHHHHHHHJGJCHHJGHHJJJJBJJIJHGAH>EAHHJIJHJHJJJJHJJGJIIIIJII>HJHIJ6JJJGIIIEGIJJGJJJJIJGJJIJHJIJJJJHHJJHIIJJJJJJJJIJI?EJJJJJIJJJJIJJJIJIJJJJJJGGJIJJHJI6JJIFJJIFIHJJJGJIBJIIJBJIIJHJIJJJIIJJIJJIIJFIHDIJJJJDIJHJ@JJGIIFGIJJJIIGBGII?JHIJIJJHICIIJJJIIIJJJHJIHIJIJJIIIJEJEICHJJJJIHIJIHGGD9IHIJJAJIIIJII@HIIJJ;@@<HJG@JHHHAEHHHHJHHHHHFJFIBHIEFDIFFCFDFFGFFFFHDFCADEHHEFDEEFHFBAFBDFFHGEFEE8DDFEEBEB(FCDCFCFDEEDB>;DDBEDBDDDCCCDDC>DC@DDA,DDDDDDADCD==CD@:@D>");
		assertEquals(591, mapInfoSnpIndel.getReadsNumAll());
		assertEquals(232, mapInfoSnpIndel.getReadsNumRef());
		siteSnpIndelInfoA = mapInfoSnpIndel.getLsAllenInfoSortBig2Small().get(0);
		assertEquals("a", siteSnpIndelInfoA.getThisSeq().toLowerCase());
		assertEquals(183, siteSnpIndelInfoA.getReadsNum());
		siteSnpIndelInfoC = mapInfoSnpIndel.getLsAllenInfoSortBig2Small().get(1);
		assertEquals("c", siteSnpIndelInfoC.getThisSeq().toLowerCase());
		assertEquals(176, siteSnpIndelInfoC.getReadsNum());
	}
	private void assertRawReadSamPileUpLines() {
		mapInfoSnpIndel.setRefSnpIndelStart("chr6", 57512711);
		mapInfoSnpIndel.setSampleName("2A");
		mapInfoSnpIndel.setSamToolsPilup("chr6	57512711	G	591	C$c$c$Caa$aa$c$.CCCCaaaacaaacc,cC.CCCaaC,aaCCCCaccaacca,c,A.Ac.,$a,cAC$cacc,,aaC.,cc,c,,,,A..A.ACCaC..CAacc,a.C,,c,,a,,aCC,a.aacaacacacacaca,A...ca,,.A..cac,,a.CCCaAA,aa,,Aa,caaca,,a,acca.....,,,,,ca,,,c,,aa,,caa,a,,cccC...,,,,.CA.Cc,,,caca,,a,A...,,.ca,a...a,aaa.c,c,,acc,accaccc,,,,,,,,ac,,,c,cca,AC,,a,a..cC.Cac,A,ac,aCa,,,,c.ca,ac,,aaCAcc,,,,cc,,ca,,c,cac,a,ca,c,,,,cACCC,ccac,aaca,C,,,A.AaaA,,,aaaaacc,,ac,a.,aaacCCc,,caaa,aacca,ccc,c,cca,,ccaaa,aac,aaca,ac,,,a,aca,,aaa,a,ccaaaa,ca,,,aaa,,,a,a,,ac,,c,a,,aaaccac,c,ca,ca,,ca,ca,cc,c,,c,cc,ca,c,c,,,ccac,accccac,,,aaaaacaaaacaaa,,acc,,,ac,,,,,,^],	'!!+.#,!!D30=+===@>==,@,C,=DDD=FFDFFFEEECFFFF.FFFFFFD>EFECFFF*!-HDD?HDFECFF,FFFFEHFEEFBD@FFFFFFDHHHHGFFHHDHHFHHDD3DFEJHHHJFHJJJGJJGJIHFFFJHJJ>HHGHHIJIDHHHHHHHJGJCHHJGHHJJJJBJJIJHGAH>EAHHJIJHJHJJJJHJJGJIIIIJII>HJHIJ6JJJGIIIEGIJJGJJJJIJGJJIJHJIJJJJHHJJHIIJJJJJJJJIJI?EJJJJJIJJJJIJJJIJIJJJJJJGGJIJJHJI6JJIFJJIFIHJJJGJIBJIIJBJIIJHJIJJJIIJJIJJIIJFIHDIJJJJDIJHJ@JJGIIFGIJJJIIGBGII?JHIJIJJHICIIJJJIIIJJJHJIHIJIJJIIIJEJEICHJJJJIHIJIHGGD9IHIJJAJIIIJII@HIIJJ;@@<HJG@JHHHAEHHHHJHHHHHFJFIBHIEFDIFFCFDFFGFFFFHDFCADEHHEFDEEFHFBAFBDFFHGEFEE8DDFEEBEB(FCDCFCFDEEDB>;DDBEDBDDDCCCDDC>DC@DDA,DDDDDDADCD==CD@:@D>");
		assertEquals(591, mapInfoSnpIndel.getReadsNumAll());
		assertEquals(232, mapInfoSnpIndel.getReadsNumRef());
		SiteSnpIndelInfo siteSnpIndelInfoA = mapInfoSnpIndel.getLsAllenInfoSortBig2Small().get(0);
		assertEquals("a", siteSnpIndelInfoA.getThisSeq().toLowerCase());
		assertEquals(183, siteSnpIndelInfoA.getReadsNum());
		SiteSnpIndelInfo siteSnpIndelInfoC = mapInfoSnpIndel.getLsAllenInfoSortBig2Small().get(1);
		assertEquals("c", siteSnpIndelInfoC.getThisSeq().toLowerCase());
		assertEquals(176, siteSnpIndelInfoC.getReadsNum());
	}
	/** 有三个基因型的位点 */
	private void assertReadsSiteWith3GeneType() {
		VcfCols vcfCols = new VcfCols();
		
		String vcfLine2A = "chr1	158580921	rs66981873	G	GCA	309.80	GATKStandard	AC=2;AF=1.00;AN=2;DB;DP=14;FS=0.000;HRun=0;HaplotypeScore=51.3672;MQ=63.75;MQ0=0;QD=22.13;SB=-0.00;set=FilteredInAll	GT:AD:DP:GQ:PL	1/1:6,8:14:24.08:352,24,0";
		mapInfoSnpIndel.setVcfLines("2A", vcfCols, vcfLine2A);
		
		String vcfLine2B = "chr1	158580921	.	G	A	86.96	TruthSensitivityTranche99.00to99.90	AB=0.792;AC=1;AF=0.50;AN=2;BaseQRankSum=0.071;DP=24;Dels=0.04;FS=0.000;HRun=2;HaplotypeScore=9.9263;MQ=54.20;MQ0=0;MQRankSum=1.208;QD=3.48;ReadPosRankSum=-2.417;SB=-0.01;VQSLOD=-2.1229;culprit=QD;set=FilteredInAll	GT:AD:DP:GQ:PL	0/1:19,5:24:99:117,0,628";
		mapInfoSnpIndel.setVcfLines("2B", vcfCols, vcfLine2B);

		mapInfoSnpIndel.setSampleName("2A");
		mapInfoSnpIndel.setSamToolsPilup("chr1	158580921	g	15	........+2CA......+2CA.+2CA	BBFF@I@JDFJFBIJ");
		
		mapInfoSnpIndel.setSampleName("2B");
		mapInfoSnpIndel.setSamToolsPilup("chr1	158580921	g	29	............+2CA.+10CACACACACA.+8CACACACA...+2CA..+8CACACACA.A*AAAA..+2CA^]A	9?D@AEC@?JGGJJIGJ+IGH7*()*FF!");
		
		SnpSampleFilter sampleFilter = new SnpSampleFilter();
		
		SampleDetail sampleDetail2A = new SampleDetail();
		sampleDetail2A.addSampleName("2A");
		sampleDetail2A.setSampleRefHomoNum(1, 1);
		sampleDetail2A.setSampleSnpIndelHetoNum(0, 0);
		sampleDetail2A.setSampleSnpIndelHomoNum(0, 0);
		sampleFilter.addSampleFilterInfo(sampleDetail2A);
		
		SampleDetail sampleDetail2B = new SampleDetail();
		sampleDetail2B.addSampleName("2B");
		sampleDetail2B.setSampleRefHomoNum(0, 0);
		sampleDetail2B.setSampleSnpIndelHetoNum(0, 1);
		sampleDetail2B.setSampleSnpIndelHomoNum(0, 1);
		sampleDetail2B.setSampleSnpIndelNum(1, 1);
		sampleFilter.addSampleFilterInfo(sampleDetail2B);
		
		boolean result = sampleFilter.isFilterdSnp(mapInfoSnpIndel);
		assertEquals(true, result);
	}
	//TODO 再测试仪个
	
}
