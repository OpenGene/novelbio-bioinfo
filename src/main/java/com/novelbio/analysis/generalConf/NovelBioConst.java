package com.novelbio.analysis.generalConf;

public class NovelBioConst {
	public  final static String DBINFO_NCBI = "NCBI";
	public  final static String DBINFO_NCBIID = "NCBIID";
	public final static String DBINFO_UNIPROTID = "UniProtID";
	public  final static String DBINFO_NCBI_ACC_GenralID = "NCBI";
	public final static String DBINFO_NCBI_ACC_REFSEQ = "RefSeq";
	/**
	 * RefSeq�ĵ���ID
	 */
	public final static String DBINFO_NCBI_ACC_REFSEQ_PROTEIN = "RefSeq_protein";
	/**
	 * RefSeq��RNAID
	 */
	public final static String DBINFO_NCBI_ACC_REFSEQ_RNA = "RefSeq_RNA";
	/**
	 * RefSeq��DNAID
	 */
	public final static String DBINFO_NCBI_ACC_REFSEQ_DNA = "RefSeq_DNA";
	/**
	 * ��UCSC��GFF�ļ��г��ֵ�RefSeqID
	 */
	public final static String DBINFO_NCBI_ACC_REFSEQ_UCSC_GFF = "RefSeq_UCSC_GFF";
	/**
	 * NCBIID������ͨ��DBINFO ID
	 */
	public final static String DBINFO_NCBI_ACC_PROGI = "proteinGI";
	/**
	 * NCBIID������ͨ��DBINFO ID
	 */
	public final static String DBINFO_NCBI_ACC_PROAC = "proteinAC";
	/**
	 * NCBIID������ͨ��DBINFO ID
	 */
	public final static String DBINFO_NCBI_ACC_RNAAC = "rnaAC";
	/**
	 * NCBIID������ͨ��DBINFO ID
	 */
	public final static String DBINFO_NCBI_ACC_GENEAC = "geneAC";
	/**
	 * UniProtDB�е�uniIDkey��
	 */
	public final static String DBINFO_UNIPROT_UNIID = "UniProt";
	public final static String DBINFO_UNIPROT_UNIPROTKB_ID = "UniProtKB_ID";
	public final static String DBINFO_UNIPROT_UNIPARC = "UniParc";
	public final static String DBINFO_UNIPROT_UNIGENE = "UniGene";
	public final static String DBINFO_UNIPROT_GenralID = "UniProtID";
	public final static String DBINFO_PIR = "PIR";
	public  final static String DBINFO_EMBL = "EMBL";
	public  final static String DBINFO_EMBL_CDS = "EMBL_CDS";
	public final static String DBINFO_RICE_TIGR = "TIGRrice";
	public final static String DBINFO_RICE_RAPDB = "RapDB";
	public final static String DBINFO_RICE_IRGSP = "IRGSP";
	public final static String DBINFO_AFFY_RICE_31 = "affyRice31";
	public final static String DBINFO_AFFY_HUMAN_U133_PLUS2 = "affy_hsa_U133Plus2";
	public final static String DBINFO_AFFY_MOUSE_430_2 = "affy_mm_430_2";
	public final static String DBINFO_AFFY_PIG = "affy_ssc";
	public final static String DBINFO_AFFY_COW = "affy_bta";
	public final static String DBINFO_AFFY_ATH = "affy_ATH1-121501";
	public final static String DBINFO_SYMBOL = "Symbol";
	public final static String DBINFO_SYNONYMS = "Synonyms";
	
	public final static String DBINFO_NIAS_FLCDNA = "NIAS_FLcDNA";
	public final static String DBINFO_ENSEMBL ="Ensembl";
	public final static String DBINFO_ENSEMBL_TRS ="Ensembl_TRS";
	public final static String DBINFO_ENSEMBL_PRO ="Ensembl_PRO";
	public final static String DBINFO_ENSEMBL_GENE ="Ensembl_Gene";
	public final static String DBINFO_ENSEMBL_RNA ="Ensembl_RNA";
	public final static String DBINFO_IPI = "IPI";	
	public final static String FASTQ_SANGER = "sanger";
	public final static String FASTQ_ILLUMINA = "Illumina";
	/////////////////////////////////////��������Ϣ///////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 *  �����Gff�����ͣ���UCSC��TIGR�ȣ�TIGR��û����
	 */
	public final static String GENOME_GFF_TYPE_UCSC = "UCSC";
	public final static String GENOME_GFF_TYPE_TIGR = "TIGR";
	/**
	 *  ������·��
	 */
	public final static String GENOME_PATH = "/media/winE/Bioinformatics/GenomeData/";
	/**
	 *  UCSC_hg19
	 */
	public final static String GENOME_PATH_UCSC_HG19 = GENOME_PATH+"human/ucsc_hg19/";
	/**
	 *  UCSC_hg19��Ⱦɫ���ļ�·��
	 */
	public final static String GENOME_PATH_UCSC_HG19_CHROM = GENOME_PATH_UCSC_HG19+"ChromFa/";
	/**
	 *  UCSC_hg19��ͳ����Ϣ
	 */
	public final static String GENOME_PATH_UCSC_HG19_STATISTIC = GENOME_PATH_UCSC_HG19+"statisticInfo";
	/**
	 *  UCSC_hg19��RefSeq��Gff�ļ����Ѿ��Ź��򣬲�����������
	 */
	public final static String GENOME_PATH_UCSC_HG19_GFF_REFSEQ = GENOME_PATH_UCSC_HG19+"hg19_refSeqSortUsingNoChrM.txt";
	/**
	 *  UCSC_hg19��repeak��Gff�ļ����Ѿ��Ź���
	 */
	public final static String GENOME_PATH_UCSC_HG19_GFF_REPEAT = GENOME_PATH_UCSC_HG19+"rmsk.txt";
	/**
	 *  UCSC_mm9
	 */
	public final static String GENOME_PATH_UCSC_MM9 = GENOME_PATH+"mouse/ucsc_mm9/";
	/**
	 *  UCSC_mm9��Ⱦɫ���ļ�·��
	 */
	public final static String GENOME_PATH_UCSC_MM9_CHROM = GENOME_PATH_UCSC_MM9+"ChromFa/";
	/**
	 *  UCSC_mm9��ͳ����Ϣ
	 */
	public final static String GENOME_PATH_UCSC_MM9_STATISTIC = GENOME_PATH_UCSC_MM9+"statisticInfo/";
	/**
	 *  UCSC_mm9��RefSeq��Gff�ļ����Ѿ��Ź��򣬲�����������
	 */
	public final static String GENOME_PATH_UCSC_MM9_GFF_REFSEQ = GENOME_PATH_UCSC_MM9+"refseqSortUsing.txt";
	/**
	 *  UCSC_mm9��repeak��Gff�ļ����Ѿ��Ź���
	 */
	public final static String GENOME_PATH_UCSC_MM9_GFF_REPEAT = GENOME_PATH_UCSC_MM9+"repeatmasker";
	/**
	 *  UCSC_mm9��repeak��Gff�ļ����Ѿ��Ź���
	 */
	public final static String GENOME_PATH_UCSC_MM19_STATISTIC_REPEAT = GENOME_PATH_UCSC_MM9_STATISTIC+"repeatregionBackGround.txt";
	/////////////////ˮ��///////////////////////////////////////////////////////////////////
	/**
	 *  ˮ�����ݿ�·��
	 */
	public final static String GENOME_PATH_RICE = GENOME_PATH+"Rice/";
	/**
	 *  RapDB·��
	 */
	public final static String GENOME_PATH_RICE_RAPDB = GENOME_PATH_RICE+"RapDB/";
	/**
	 *  RapDB��ˮ������gff3�ļ�
	 */
	public final static String GENOME_PATH_RICE_RAPDB_GFF_GENE = GENOME_PATH_RICE_RAPDB+"RAP_genes.gff3";
	/**
	 *  TIGRrice��·��
	 */
	public final static String GENOME_PATH_RICE_TIGR = GENOME_PATH_RICE+"TIGRRice/";
	/**
	 *  TIGRrice��ˮ������gff3�ļ�
	 */
	public final static String GENOME_PATH_RICE_TIGR_GFF_GENE = GENOME_PATH_RICE_TIGR+"all.gff3Cope";
	/**
	 *  TIGRrice��ˮ�����������ļ���
	 */
	public final static String GENOME_PATH_RICE_TIGR_CHROM = GENOME_PATH_RICE_TIGR+"ChromFa/";
	/**
	 *  TIGRrice��ˮ��ͳ����Ϣ
	 */
	public final static String GENOME_PATH_RICE_TIGR_STATISTIC = GENOME_PATH_RICE_TIGR+"statisticInof/";

	//////////////////////////////////Rϵ��//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 *  "/media/winE/Bioinformatics/R/practice_script/platform/"
	 */
	public final static String R_WORKSPACE = "/media/winE/Bioinformatics/R/practice_script/platform/";
	
	
	
	/////////////////////////////����Fisher/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * ����Fisher�������ļ�
	 */
	public final static String R_WORKSPACE_Fisher_Info = R_WORKSPACE + "Fisher/Info.txt";
	/**
	 * ����Fisher�ļ���ű�
	 */
	public final static String R_WORKSPACE_FISHER_SCRIPT= R_WORKSPACE + "FisherBHfdr.R";
	/**
	 * ����Fisher�Ľ���ļ�
	 */
	public final static String R_WORKSPACE_Fisher_Result = R_WORKSPACE + "Fisher/Analysis.txt";
	///////////////////////////TopGo��ElimFisher/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * topGo���ļ���
	 */
	public final static String R_WORKSPACE_TOPGO = R_WORKSPACE + "topGO/";
	/**
	 * topGo��R�ű��ļ�
	 */
	public final static String R_WORKSPACE_TOPGO_RSCRIPT = R_WORKSPACE + "topGO.R";
	/**
	 * topGo�Ĳ���
	 * �����ı�����һ����¼ѡ��BP��MF��CC���ڶ�����¼д�ļ���Ĭ����GoResult.txt
	 * �����������֣���ʾ��ʾ���ٸ�GOTerm ,���ĸ���¼GOInfo������ÿ��GO��Ӧ�Ļ����ļ���
	 */
	public final static String R_WORKSPACE_TOPGO_PARAM = R_WORKSPACE_TOPGO + "parameter.txt";
	/**
	 * topGo��gene Go Info�Ľ���ļ���������Ͻ���excel
	 */
	public final static String R_WORKSPACE_TOPGO_GENEGOINFO = R_WORKSPACE_TOPGO + "GeneGOInfo.txt";
	/**
	 * topGo��gene go,go,go�Ľ���ļ�
	 */
	public final static String R_WORKSPACE_TOPGO_BGGeneGo = R_WORKSPACE_TOPGO + "BG2Go.txt";
	/**
	 * topGo�İ���elimFisher�Ľ��table�ļ�
	 */
	public final static String R_WORKSPACE_TOPGO_GORESULT = R_WORKSPACE_TOPGO + "GoResult.txt";
	/**
	 * topGo�İ���elimFisher��ͼƬ
	 */
	public final static String R_WORKSPACE_TOPGO_GOMAP = R_WORKSPACE_TOPGO + "tGOall_elim_10_def.pdf";
	/**
	 * topGo�İ���elimFisher�Ľ��table�ļ�
	 * ÿ��GO���������еı�������
	 * ��ʽΪ<br>
	 * #GO:010101<br>
	 * NM_0110101
	 */
	public final static String R_WORKSPACE_TOPGO_GOINFO = R_WORKSPACE_TOPGO + "GOInfo.txt";
	/**
	 * topGo������Ҫ��GeneID
	 */
	public final static String R_WORKSPACE_TOPGO_GENEID = R_WORKSPACE_TOPGO + "GeneID.txt";
	/////////////////////////����ṹ�ķ���////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * �������ṹ�ļ���·�������ڱ���ChIP-Seq��Peak�ڻ������Ϸֲ���ͳ�Ʒ���
	 */
	public final static String R_WORKSPACE_CHIP_GENESTRUCTURE = R_WORKSPACE + "GeneStructure/";
	/**
	 * �������ṹ�ļ���·�������ڱ���ChIP-Seq��Peak�ڻ������Ϸֲ���ͳ�Ʒ���
	 */
	public final static String R_WORKSPACE_CHIP_GENESTRUCTURE_FILE = R_WORKSPACE_CHIP_GENESTRUCTURE + "GeneStructureStatistics.txt";
	/**
	 * �������ṹ�ļ���·�������ڱ���ChIP-Seq��Peak�ڻ������Ϸֲ���ͳ�Ʒ���
	 */
	public final static String R_WORKSPACE_CHIP_GENESTRUCTURE_RSCRIPT = R_WORKSPACE + "MyBarPlotGeneStructure.R";
	/**
	 * �������ṹ�ļ����ͼ
	 */
	public final static String R_WORKSPACE_CHIP_GENESTRUCTURE_RESULT_PIC = R_WORKSPACE_CHIP_GENESTRUCTURE + "batPlot.jpg";
	/////////////////////////Reads��genome�ϵķֲ�///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * reads��genome�Ϸֲ�����Ϣ�ļ���
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS = R_WORKSPACE + "chrReads/";
	/**
	 * reads��genome�Ϸֲ�����Ϣ���ṩ��R��ͼ�Ĳ����ļ�
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS_PARAM = R_WORKSPACE_CHIP_CHRREADS + "parameter";
	/**
	 * reads��genome�Ϸֲ����������ݣ�X��
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS_X = R_WORKSPACE_CHIP_CHRREADS + "readsx";
	/**
	 * reads��genome�Ϸֲ����������ݣ�Y��,Ҳ����ʵ��reads�ķֲ�
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS_Y = R_WORKSPACE_CHIP_CHRREADS + "readsy";
	/**
	 * reads��genome�Ϸֲ����������ݣ�����ֿ���������Ҳ�����еڶ���mapping�ļ������ǵڶ����ļ���Y��,Ҳ����ʵ�ʵڶ����ļ���reads�ֲ�
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS_2Y = R_WORKSPACE_CHIP_CHRREADS + "reads2y";
	/**
	 * reads��genome�Ϸֲ����������ݣ�����ֿ���������Ҳ�����еڶ���mapping�ļ������ǵڶ����ļ���Y��,Ҳ����ʵ�ʵڶ����ļ���reads�ֲ�
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS_RSCRIPT = R_WORKSPACE + "MyChrReads.R";
	/////////////////////////Reads���ض�Region��Tss��GeneEnd�ϵķֲ�///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * reads���ض�Region��Tss��GeneEnd�ϵķֲ����ļ���
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION = R_WORKSPACE + "regionReads/";
	/**
	 * reads��Tss�ϵķֲ���Ϣ�ļ���20��һ�У�����R��ȡ
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_TSS_R = R_WORKSPACE_CHIP_READS_REGION + "tss.txt";
	/**
	 * reads��geneEnd�ϵķֲ���Ϣ�ļ�
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_R = R_WORKSPACE_CHIP_READS_REGION + "geneEnd.txt";
	/**
	 * reads���ض�Region��Tss��GeneEnd�ϵķֲ�����ͼ����
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_PARAM = R_WORKSPACE_CHIP_READS_REGION + "parameter.txt";
	/**
	 * reads��TSS�ϵ�ͼ
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_TSS_PIC = R_WORKSPACE_CHIP_READS_REGION + "TSSReads.jpg";	
	/**
	 * reads��GeneEnd�ϵ�ͼ
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_PIC = R_WORKSPACE_CHIP_READS_REGION + "GeneEndReads.jpg";	
	/**
	 * reads��TSS�ϵķֲ���Ϣ�ļ���һ�У�����excelʹ��
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_TSS_EXCEL = R_WORKSPACE_CHIP_READS_REGION + "tss2.txt";	
	/**
	 * reads��GeneEnd�ϵķֲ���Ϣ�ļ���һ�У�����excelʹ��
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_EXCEL = R_WORKSPACE_CHIP_READS_REGION + "geneEnd2.txt";	
	/**
	 * reads��TSS�ϵķֲ���Ϣ�ļ���һ�У�����excelʹ��
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_TSS_RSCRIPT = R_WORKSPACE + "MyTSSReads.R";	
	/**
	 * reads��GeneEnd�ϵķֲ���Ϣ�ļ���һ�У�����excelʹ��
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_RSCRIPT = R_WORKSPACE + "MyGeneEndReads.R";	
	/////////////////////////R��density���㣬�����ڼ���motif��ָ�������ϵķֲ�///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * R��density������ļ��У������ڼ���motif��ָ�������ϵķֲ�
	 */
	public final static String R_WORKSPACE_DENSITY = R_WORKSPACE + "NormalDensity/";
	/**
	 * R��density����Ĳ����ļ������汾�μ����������Դ�������������ڼ���motif��ָ�������ϵķֲ���Peak��TSS�ֲ�<br>
	 * ��һ�д����ڶ���xtitle��������ytitle
	 */
	public final static String R_WORKSPACE_DENSITY_PARAM = R_WORKSPACE_DENSITY + "param.txt";
	/**
	 * R��density����ļ����ļ������汾�μ�������ݣ������ڼ���motif��ָ�������ϵķֲ�<br>
	 * ����ÿ��һ����д��һ�У�û�б���
	 */
	public final static String R_WORKSPACE_DENSITY_DATA = R_WORKSPACE_DENSITY + "data.txt";
	/**
	 * R��density����Ľű�
	 */
	public final static String R_WORKSPACE_DENSITY_RSCRIPT = R_WORKSPACE + "NormalDensity.R";
	/**
	 * R��density����Ľ��ͼƬ�����汾�μ�������ݣ������ڼ���motif��ָ�������ϵķֲ�
	 */
	public final static String R_WORKSPACE_DENSITY_PIC = R_WORKSPACE_DENSITY + "density.jpeg";
	
	
	
}
