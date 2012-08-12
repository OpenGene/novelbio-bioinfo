package com.novelbio.test.analysis.seq.resequencing;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.novelbio.analysis.seq.genomeNew.GffChrAbs;
import com.novelbio.analysis.seq.resequencing.MapInfoSnpIndel;
import com.novelbio.analysis.seq.resequencing.SampleDetail;
import com.novelbio.analysis.seq.resequencing.SiteSnpIndelInfo;
import com.novelbio.analysis.seq.resequencing.SnpSampleFilter;
import com.novelbio.analysis.seq.resequencing.VcfCols;

public class TestMapInfoSnpIndel extends TestCase {
	MapInfoSnpIndel mapInfoSnpIndel;
	GffChrAbs gffChrAbs = new GffChrAbs(9606);
	@Override
	protected void setUp() throws Exception {
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
		assertReadsSiteWith3GeneType2();
		assertReadsSiteWith2GeneType3();
		assertReadsSiteWith2GeneType4Write();
	}
	private void assertReadVcfAndSamPileUpLines() {
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs);

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
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs);
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
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs);
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
	/** 有三个基因型的位点 */
	private void assertReadsSiteWith3GeneType2() {
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs);
		MapInfoSnpIndel mapInfoSnpIndel = new MapInfoSnpIndel();
		VcfCols vcfCols = new VcfCols();
		String vcfLine2A = "chr1	69270	.	A	G	597.49	TruthSensitivityTranche99.90to100.00	AC=2;AF=1.00;AN=2;DP=488;DS;Dels=0.00;FS=0.000;HRun=0;HaplotypeScore=0.0000;MQ=6.30;MQ0=416;QD=1.22;SB=-0.01;VQSLOD=-22.2566;culprit=MQ;set=FilteredInAll	GT:AD:DP:GQ:PL	1/1:249,237:488:71.90:630,72,0";
		String vcfLine2B = "chr1	69270	.	A	G	585.45	TruthSensitivityTranche99.90to100.00	AC=2;AF=1.00;AN=2;DP=500;DS;Dels=0.00;FS=0.000;HRun=0;HaplotypeScore=0.0000;MQ=5.62;MQ0=441;QD=1.17;SB=-0.01;VQSLOD=-22.4439;culprit=MQ;set=FilteredInAll	GT:AD:DP:GQ:PL	1/1:297,199:500:74.84:618,75,0";
		
		
		mapInfoSnpIndel.setVcfLines("2A", vcfCols, vcfLine2A);
		mapInfoSnpIndel.setVcfLines("2B", vcfCols, vcfLine2B);

		
		
		String samPileUpLine2A = "chr1	69270	A	544	.$G.GG.G.....,,ggg$.,g,.g..GGG.gg,,,,.G.G.gGGGgG.GG.G.G.g,.....GGGGG.,,g....GGg,GGgg.G,g,.G.g,G...,TGG..G,,,G...GG,,,GG.....ggg,,GG.GG..g,g,,G.G.gG,,gGG.gg,.G.G.,,g,gGGG..ggggg,,G..ggG....GGgggG,gGggg,g,ggg,,,,G.G,,G,,ggG...,G.GgG.Gggggg.GG,g,g,.....g.,,GG,gG.,,g,,g.G..G..,g,g,...G...g,,G,,,g,g,,,.GGGG.,g,,,gGGggg,,,,,,,G..,,G.g,,..g,,,g,,,gggGGGGG,g,.gGG.G.,,,gg,g,G.,GGGGgggGGGgg,g,gggG,,,g...G.,,,,,G.gGG.,,gG,,,.G,g.GGG,,,,,,,G,,G,,g,,GG,,g,,,GGGggGG,G..G..G,c,g,g,.G..gg,GGGG,G,,g,g,,ggGGGG,gggGGGG,gGGGG.GG,GGG,ggGGg.GGgGG.,,GG,,,,,g,,g,^!,	3,@+-D@CCCCDB:DE$CBDB@F@DDDBCDFBDADDB@D>F@@DF>@D:DBDDCD=CAEE@>'DADECDJAAEEDCHCDBHHDFEHFFEFICFFFFGHBFHH<AHCFHHHE:AHC:FFH?HHJJJFHHFIEECFIHGCFDEJIJHF<IJJIIJGJJJJ7F3JFJBEGIJJJIJJFFGJGJIIIIJI-JJ@JJFJ@I3IHJGBJJCDHFIJIDFIBHGCBIJJFHJJHJJAIJIJJGJIAJ/JFJIGJJJJF9GICJJIFGJFFJJIIJJJJ8ECJFIJHIHCJIF<JHF@EHI=FGJIJEJJ@JGF@IIGJII;@FGD7DGJBD@HJJD@IJG?CDJEEHG9IIGIGJ=D7IGFIJJI?DEJJ@IGHIBFJJJEEFJJIFG;F7IEHIE?7@IJGIA.@@>)ECGGJJA>DJ>A5J@5CJ<JJ>3>A55;J55E.5@;>FJ,>D>;,IJIDDDBCFJJJJIJ>(5DADAJJJJCC>GCHH@H@4CCAC:+CHHHH:DCDHGDH@CFHHHFFD>FFF:DCFFDFFFDC>@@4,,A3:>4,>:+:3";
		String samPileUpLine2B = "chr1	69270	A	667	.$..GGGGG.g$,,.G......,,$g$..G.GG.G,,,.,g,,,..,g,G.g,,g,G..,,...gggG..GGGGGGGG,gGg,,,g,,,GGG,g.ggggg,,,,,G.,GGG,...G,,,..G....,,.GGG....,....G..,,g,.G,G,g...g,ggg....G........G..GG,g.,,g..GGg,,gggG,,..G.,,.,G,G,GggGG.g,,,,,..G.,ggg,,,..,.GG.GGG..Gg,gg,G.,.,,.,g,...GG..G,g....g.G.....GGG.G.G....,gG.....g,G.gg,,..GG.GGGggg..GGGGGGG,gG.GG..,,G..G....,,,g,,,.GG,gg,,g,,,GgG,,,,t,...,,g,,,G.,,,g,,,gg,ggG.gg,G...,,g..GG.,,GGG.,,,g,,,.G.,GG.g,.g,,GC.G,gg,,gggGGGGGg,,,GGG.G,g,,GGGG,,gg.GGG,,G,,,,GG.G.gg,,g,gg,,..G.G,,Gg,g,.,g,,,,g,gg,,G,,,,,,,,,,,g,,,g,cg,,..,,,,,,,,g,,,g,,g,,,g,G.gGGG,,,g,,,,,,gg.GGGg,g.GGGg,,g,GGG.GGG,G,GG,.,,,,,GGgcgg,..g,,.,gGg,,,,,g,g,gg,g^4G^4G^!G^!,^!,^!,^!,^!,^!,^!,^!,	DDD-+/-+D%BBCD>CADCDDB!CDD:DDDDDDDDDFDDADCAFDDDF=CHDBDD8AEEEHIJDCCDDD/ADD<CJDHGEA?DHCDCDCHFJJBIJFCCAEFCAFDFCCFE>EHAHHHHHHHCFEB<HAHHHC=H7HBCGCFHEJJ+9<JJIJFAJJIJAGEJJHJJGHJJJJGGIAJI<9JJJI/JCEJJJIH?IJIICCJFJAJFJGJJIII9BCC?JIJJDJJIC?DJJCJF;JIJJJFIJFIJ*HJBJ<:JBIDJIJJGIJJ?AJIHJGIJIJGJIJHJIIJJGJJI?HBIJJJJD=HJID==IEJIIF?HJJIIJJIIHJJIB=IGGJJE=<@EHIHFIIFB8J=DBJJIFIJ=EIH==IJFCB/C)8J0J@FH==AIFE=DI@@CDJFJJAGJJDJJIJA.JIJ8JIC=JIJIGA/HBGDJIJ=JDIFCIF@CJ*IDHJH6BHHHGJEJJHC?=IJFJJ7D.EJJJJ7-DCHJJJ;;J>@A>JIJJI3@A5>@>@>;JFJII@@ID>DAJCD,A@@DCAC@CJ-55A>C;C;5(D@>;C59C>;IJ>(@>@;C;D>A5DC>DC5;@>JJCHGH@A@DC>C:@CCCHHHCC3>FHH)D:>B>GHHHHHH@H:FF(F+4(:>FFD3DD:FF=4@@C@,4A:>4>4:+4+,A/!$!4@4>:>:4";
		mapInfoSnpIndel.setSampleName("2A");
		mapInfoSnpIndel.setSamToolsPilup(samPileUpLine2A);
		mapInfoSnpIndel.setSampleName("2B");
		mapInfoSnpIndel.setSamToolsPilup(samPileUpLine2B);
		
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
		sampleDetail2B.setSampleSnpIndelHetoLessNum(0, 0);
		sampleFilter.addSampleFilterInfo(sampleDetail2B);
		
		boolean result = sampleFilter.isFilterdSnp(mapInfoSnpIndel);
		assertEquals(false, result);
	}
	
	/** 有两个基因型的位点 */
	private void assertReadsSiteWith2GeneType3() {
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs);
		VcfCols vcfCols = new VcfCols();
		String vcfLine2A = "chr1	100671866	rs34229137	T	A	932.62	TruthSensitivityTranche99.00to99.90	AB=0.602;AC=1;AF=0.50;AN=2;BaseQRankSum=1.363;DB;DP=83;Dels=0.00;FS=0.884;HRun=13;HaplotypeScore=9.7086;MQ=58.73;MQ0=0;MQRankSum=2.098;QD=11.24;ReadPosRankSum=0.293;SB=-337.54;VQSLOD=0.0292;culprit=MQ;set=FilteredInAll	GT:AD:DP:GQ:PL	0/1:50,33:83:99:963,0,1528";
		String vcfLine2B = "chr1	100671866	rs34229137	T	A	1557.72	TruthSensitivityTranche99.00to99.90	AB=0.540;AC=1;AF=0.50;AN=2;BaseQRankSum=-0.597;DB;DP=124;Dels=0.00;FS=7.108;HRun=13;HaplotypeScore=14.6854;MQ=58.49;MQ0=0;MQRankSum=1.028;QD=12.56;ReadPosRankSum=0.256;SB=-793.21;VQSLOD=0.1711;culprit=HaplotypeScore;set=FilteredInAll	GT:AD:DP:GQ:PL	0/1:67,57:124:99:1588,0,1923";

		
		mapInfoSnpIndel.setVcfLines("2A", vcfCols, vcfLine2A);
		mapInfoSnpIndel.setVcfLines("2B", vcfCols, vcfLine2B);

		
		
		String samPileUpLine2A = "chr1	100671866	t	93	.$,$a,A.A,..,aa.,a.A,+1aaaaa,,,-1aa,aa,.aAA,,,a,,,,.,,.,a,,a,aAa.AAA,,,.a..a.-1A....aA,,aA..AAa.,,,,,,Aa	;>!@!D!DDDJEIDIGA!;FJHGGG2FD!DBJD!!DDBDD=BGJDBJ@D@D!E6!!J!JJC?DHDJI:HGHHH!3DD!!EE!!!4>>*989!'";
		String samPileUpLine2B = "chr1	100671866	t	138	.$.$AA,a.A..,,,A,,A,A,.A,a,aa.,,,a,,,,aa.,A,..+1AaA..a,-1aA,,A.a,,aaA,,AaaAA,.a,,,,aa.+1A,..A,,aa.A,,A.A.-2AAAA,A.aA..,,aa.AaaaaaaaAA,,,,,,,,a,aAaa.,,,a,	>;!!C!B!BDFFF!HH4JBFB!GEGEADD=DECHFC@4HHIHJJDEJJ!G=CCJJB'BDD,BD!DBIJ;J!AD(B<9IDJJ!BBB5JJB2HHHGHH@$D93FF))96F!!(!(!!!!!985(;<>!!>!!!!>=2>!>";
		mapInfoSnpIndel.setSampleName("2A");
		mapInfoSnpIndel.setSamToolsPilup(samPileUpLine2A);
		mapInfoSnpIndel.setSampleName("2B");
		mapInfoSnpIndel.setSamToolsPilup(samPileUpLine2B);
		
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
		sampleDetail2B.setSampleSnpIndelHetoLessNum(0, 0);
		sampleFilter.addSampleFilterInfo(sampleDetail2B);
		
		boolean result = sampleFilter.isFilterdSnp(mapInfoSnpIndel);
		assertEquals(false, result);
	}
	
	/** 有三个基因型的位点 */
	private void assertReadsSiteWith2GeneType4Write() {
		mapInfoSnpIndel = new MapInfoSnpIndel(gffChrAbs);
		VcfCols vcfCols = new VcfCols();
		String vcfLine2A = "chr1	165370367	rs3215856	T	TA	152.81	PASS	AB=0.762;AC=1;AF=0.50;AN=2;BaseQRankSum=-0.680;DB;DP=16;FS=0.000;HRun=0;HaplotypeScore=38.0502;MQ=60.00;MQ0=0;MQRankSum=-0.906;QD=9.55;ReadPosRankSum=0.680;SB=-0.00;set=Intersection	GT:AD:DP:GQ:PL	0/1:11,5:16:99:192,0,414";
		String vcfLine2B = "chr1	165370367	rs3215856	T	TA	380.77	PASS	AB=0.583;AC=1;AF=0.50;AN=2;BaseQRankSum=-2.899;DB;DP=14;FS=0.000;HRun=0;HaplotypeScore=27.8516;MQ=61.53;MQ0=0;MQRankSum=0.636;QD=27.20;ReadPosRankSum=-0.778;SB=-96.01;set=Intersection	GT:AD:DP:GQ:PL	0/1:4,10:14:99:420,0,126";

		
		mapInfoSnpIndel.setVcfLines("2A", vcfCols, vcfLine2A);
		mapInfoSnpIndel.setVcfLines("2B", vcfCols, vcfLine2B);

		
		
		String samPileUpLine2A = "chr1	165370367	T	17	...+1A.+1A,...+1A..+1A.......+1A	E;EBJIICIEIJHFFFD";
		String samPileUpLine2B = "chr1	165370367	T	19	.+1A.+1A...,+1a.+1A.+1A.+1A.+1A.+1A...+1A.+1A.+1AaaA	CBFDIEFDE?@IIFBE!!$";
		mapInfoSnpIndel.setSampleName("2A");
		mapInfoSnpIndel.setSamToolsPilup(samPileUpLine2A);
		mapInfoSnpIndel.setSampleName("2B");
		mapInfoSnpIndel.setSamToolsPilup(samPileUpLine2B);
		
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
		sampleDetail2B.setSampleSnpIndelHetoLessNum(0, 0);
		sampleFilter.addSampleFilterInfo(sampleDetail2B);
		
		boolean result = sampleFilter.isFilterdSnp(mapInfoSnpIndel);
		assertEquals(true, result);
		ArrayList<String> lsSample = new ArrayList<String>();
		lsSample.add("2A"); lsSample.add("2B");
		ArrayList<String[]> lsResult = mapInfoSnpIndel.toStringLsSnp(lsSample, true);
		for (String[] strings : lsResult) {
			for (String string : strings) {
				System.out.print(string + "\t");
			}
			System.out.println();
		}
	}
}
