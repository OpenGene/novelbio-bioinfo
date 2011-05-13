package com.novelbio.analysis.generalConf;

public class NovelBioConst {
	public  final static String DBINFO_NCBI = "NCBI";
	public  final static String DBINFO_NCBIID = "NCBIID";
	public final static String DBINFO_UNIPROTID = "UniProtID";
	public  final static String DBINFO_NCBI_ACC_GenralID = "NCBI";
	public final static String DBINFO_NCBI_ACC_REFSEQ = "RefSeq";
	/**
	 * RefSeq的蛋白ID
	 */
	public final static String DBINFO_NCBI_ACC_REFSEQ_PROTEIN = "RefSeq_protein";
	/**
	 * RefSeq的RNAID
	 */
	public final static String DBINFO_NCBI_ACC_REFSEQ_RNA = "RefSeq_RNA";
	/**
	 * RefSeq的DNAID
	 */
	public final static String DBINFO_NCBI_ACC_REFSEQ_DNA = "RefSeq_DNA";
	/**
	 * 在UCSC的GFF文件中出现的RefSeqID
	 */
	public final static String DBINFO_NCBI_ACC_REFSEQ_UCSC_GFF = "RefSeq_UCSC_GFF";
	/**
	 * NCBIID中最普通的DBINFO ID
	 */
	public final static String DBINFO_NCBI_ACC_PROGI = "proteinGI";
	/**
	 * NCBIID中最普通的DBINFO ID
	 */
	public final static String DBINFO_NCBI_ACC_PROAC = "proteinAC";
	/**
	 * NCBIID中最普通的DBINFO ID
	 */
	public final static String DBINFO_NCBI_ACC_RNAAC = "rnaAC";
	/**
	 * NCBIID中最普通的DBINFO ID
	 */
	public final static String DBINFO_NCBI_ACC_GENEAC = "geneAC";
	/**
	 * UniProtDB中的uniIDkey表
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
	/////////////////////////////////////基因组信息///////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 *  基因的Gff的类型，有UCSC和TIGR等，TIGR还没加入
	 */
	public final static String GENOME_GFF_TYPE_UCSC = "UCSC";
	public final static String GENOME_GFF_TYPE_TIGR = "TIGR";
	/**
	 *  基因组路径
	 */
	public final static String GENOME_PATH = "/media/winE/Bioinformatics/GenomeData/";
	/**
	 *  UCSC_hg19
	 */
	public final static String GENOME_PATH_UCSC_HG19 = GENOME_PATH+"human/ucsc_hg19/";
	/**
	 *  UCSC_hg19的染色体文件路径
	 */
	public final static String GENOME_PATH_UCSC_HG19_CHROM = GENOME_PATH_UCSC_HG19+"ChromFa/";
	/**
	 *  UCSC_hg19的统计信息
	 */
	public final static String GENOME_PATH_UCSC_HG19_STATISTIC = GENOME_PATH_UCSC_HG19+"statisticInfo";
	/**
	 *  UCSC_hg19的RefSeq的Gff文件，已经排过序，不包含线粒体
	 */
	public final static String GENOME_PATH_UCSC_HG19_GFF_REFSEQ = GENOME_PATH_UCSC_HG19+"hg19_refSeqSortUsingNoChrM.txt";
	/**
	 *  UCSC_hg19的repeak的Gff文件，已经排过序
	 */
	public final static String GENOME_PATH_UCSC_HG19_GFF_REPEAT = GENOME_PATH_UCSC_HG19+"rmsk.txt";
	/**
	 *  UCSC_mm9
	 */
	public final static String GENOME_PATH_UCSC_MM9 = GENOME_PATH+"mouse/ucsc_mm9/";
	/**
	 *  UCSC_mm9的染色体文件路径
	 */
	public final static String GENOME_PATH_UCSC_MM9_CHROM = GENOME_PATH_UCSC_MM9+"ChromFa/";
	/**
	 *  UCSC_mm9的统计信息
	 */
	public final static String GENOME_PATH_UCSC_MM9_STATISTIC = GENOME_PATH_UCSC_MM9+"statisticInfo/";
	/**
	 *  UCSC_mm9的RefSeq的Gff文件，已经排过序，不包含线粒体
	 */
	public final static String GENOME_PATH_UCSC_MM9_GFF_REFSEQ = GENOME_PATH_UCSC_MM9+"refseqSortUsing.txt";
	/**
	 *  UCSC_mm9的repeak的Gff文件，已经排过序
	 */
	public final static String GENOME_PATH_UCSC_MM9_GFF_REPEAT = GENOME_PATH_UCSC_MM9+"repeatmasker";
	/**
	 *  UCSC_mm9的repeak的Gff文件，已经排过序
	 */
	public final static String GENOME_PATH_UCSC_MM19_STATISTIC_REPEAT = GENOME_PATH_UCSC_MM9_STATISTIC+"repeatregionBackGround.txt";
	/////////////////水稻///////////////////////////////////////////////////////////////////
	/**
	 *  水稻数据库路径
	 */
	public final static String GENOME_PATH_RICE = GENOME_PATH+"Rice/";
	/**
	 *  RapDB路径
	 */
	public final static String GENOME_PATH_RICE_RAPDB = GENOME_PATH_RICE+"RapDB/";
	/**
	 *  RapDB的水稻基因gff3文件
	 */
	public final static String GENOME_PATH_RICE_RAPDB_GFF_GENE = GENOME_PATH_RICE_RAPDB+"RAP_genes.gff3";
	/**
	 *  TIGRrice的路径
	 */
	public final static String GENOME_PATH_RICE_TIGR = GENOME_PATH_RICE+"TIGRRice/";
	/**
	 *  TIGRrice的水稻基因gff3文件
	 */
	public final static String GENOME_PATH_RICE_TIGR_GFF_GENE = GENOME_PATH_RICE_TIGR+"all.gff3Cope";
	/**
	 *  TIGRrice的水稻基因序列文件夹
	 */
	public final static String GENOME_PATH_RICE_TIGR_CHROM = GENOME_PATH_RICE_TIGR+"ChromFa/";
	/**
	 *  TIGRrice的水稻统计信息
	 */
	public final static String GENOME_PATH_RICE_TIGR_STATISTIC = GENOME_PATH_RICE_TIGR+"statisticInof/";

	//////////////////////////////////R系列//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 *  "/media/winE/Bioinformatics/R/practice_script/platform/"
	 */
	public final static String R_WORKSPACE = "/media/winE/Bioinformatics/R/practice_script/platform/";
	
	
	
	/////////////////////////////常规Fisher/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 常规Fisher的输入文件
	 */
	public final static String R_WORKSPACE_Fisher_Info = R_WORKSPACE + "Fisher/Info.txt";
	/**
	 * 常规Fisher的计算脚本
	 */
	public final static String R_WORKSPACE_FISHER_SCRIPT= R_WORKSPACE + "FisherBHfdr.R";
	/**
	 * 常规Fisher的结果文件
	 */
	public final static String R_WORKSPACE_Fisher_Result = R_WORKSPACE + "Fisher/Analysis.txt";
	///////////////////////////TopGo的ElimFisher/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * topGo的文件夹
	 */
	public final static String R_WORKSPACE_TOPGO = R_WORKSPACE + "topGO/";
	/**
	 * topGo的R脚本文件
	 */
	public final static String R_WORKSPACE_TOPGO_RSCRIPT = R_WORKSPACE + "topGO.R";
	/**
	 * topGo的参数
	 * 参数文本，第一个记录选择BP、MF、CC，第二个记录写文件，默认是GoResult.txt
	 * 第三个是数字，表示显示多少个GOTerm ,第四个记录GOInfo，就是每个GO对应的基因文件名
	 */
	public final static String R_WORKSPACE_TOPGO_PARAM = R_WORKSPACE_TOPGO + "parameter.txt";
	/**
	 * topGo的gene Go Info的结果文件，最后整合进入excel
	 */
	public final static String R_WORKSPACE_TOPGO_GENEGOINFO = R_WORKSPACE_TOPGO + "GeneGOInfo.txt";
	/**
	 * topGo的gene go,go,go的结果文件
	 */
	public final static String R_WORKSPACE_TOPGO_BGGeneGo = R_WORKSPACE_TOPGO + "BG2Go.txt";
	/**
	 * topGo的包含elimFisher的结果table文件
	 */
	public final static String R_WORKSPACE_TOPGO_GORESULT = R_WORKSPACE_TOPGO + "GoResult.txt";
	/**
	 * topGo的包含elimFisher的图片
	 */
	public final static String R_WORKSPACE_TOPGO_GOMAP = R_WORKSPACE_TOPGO + "tGOall_elim_10_def.pdf";
	/**
	 * topGo的包含elimFisher的结果table文件
	 * 每个GO里面所含有的背景基因
	 * 格式为<br>
	 * #GO:010101<br>
	 * NM_0110101
	 */
	public final static String R_WORKSPACE_TOPGO_GOINFO = R_WORKSPACE_TOPGO + "GOInfo.txt";
	/**
	 * topGo计算需要的GeneID
	 */
	public final static String R_WORKSPACE_TOPGO_GENEID = R_WORKSPACE_TOPGO + "GeneID.txt";
	/////////////////////////基因结构的分析////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 保存基因结构文件的路径，用于保存ChIP-Seq的Peak在基因组上分布的统计分析
	 */
	public final static String R_WORKSPACE_CHIP_GENESTRUCTURE = R_WORKSPACE + "GeneStructure/";
	/**
	 * 保存基因结构文件的路径，用于保存ChIP-Seq的Peak在基因组上分布的统计分析
	 */
	public final static String R_WORKSPACE_CHIP_GENESTRUCTURE_FILE = R_WORKSPACE_CHIP_GENESTRUCTURE + "GeneStructureStatistics.txt";
	/**
	 * 保存基因结构文件的路径，用于保存ChIP-Seq的Peak在基因组上分布的统计分析
	 */
	public final static String R_WORKSPACE_CHIP_GENESTRUCTURE_RSCRIPT = R_WORKSPACE + "MyBarPlotGeneStructure.R";
	/**
	 * 保存基因结构文件结果图
	 */
	public final static String R_WORKSPACE_CHIP_GENESTRUCTURE_RESULT_PIC = R_WORKSPACE_CHIP_GENESTRUCTURE + "batPlot.jpg";
	/////////////////////////Reads在genome上的分布///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * reads在genome上分布的信息文件夹
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS = R_WORKSPACE + "chrReads/";
	/**
	 * reads在genome上分布的信息，提供给R画图的参数文件
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS_PARAM = R_WORKSPACE_CHIP_CHRREADS + "parameter";
	/**
	 * reads在genome上分布的输入数据，X轴
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS_X = R_WORKSPACE_CHIP_CHRREADS + "readsx";
	/**
	 * reads在genome上分布的输入数据，Y轴,也就是实际reads的分布
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS_Y = R_WORKSPACE_CHIP_CHRREADS + "readsy";
	/**
	 * reads在genome上分布的输入数据，如果分开正负链，也就是有第二个mapping文件，这是第二个文件的Y轴,也就是实际第二个文件的reads分布
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS_2Y = R_WORKSPACE_CHIP_CHRREADS + "reads2y";
	/**
	 * reads在genome上分布的输入数据，如果分开正负链，也就是有第二个mapping文件，这是第二个文件的Y轴,也就是实际第二个文件的reads分布
	 */
	public final static String R_WORKSPACE_CHIP_CHRREADS_RSCRIPT = R_WORKSPACE + "MyChrReads.R";
	/////////////////////////Reads在特定Region如Tss和GeneEnd上的分布///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * reads在特定Region如Tss和GeneEnd上的分布的文件夹
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION = R_WORKSPACE + "regionReads/";
	/**
	 * reads在Tss上的分布信息文件，20个一行，用于R读取
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_TSS_R = R_WORKSPACE_CHIP_READS_REGION + "tss.txt";
	/**
	 * reads在geneEnd上的分布信息文件
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_R = R_WORKSPACE_CHIP_READS_REGION + "geneEnd.txt";
	/**
	 * reads在特定Region如Tss和GeneEnd上的分布，画图参数
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_PARAM = R_WORKSPACE_CHIP_READS_REGION + "parameter.txt";
	/**
	 * reads在TSS上的图
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_TSS_PIC = R_WORKSPACE_CHIP_READS_REGION + "TSSReads.jpg";	
	/**
	 * reads在GeneEnd上的图
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_PIC = R_WORKSPACE_CHIP_READS_REGION + "GeneEndReads.jpg";	
	/**
	 * reads在TSS上的分布信息文件，一列，用于excel使用
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_TSS_EXCEL = R_WORKSPACE_CHIP_READS_REGION + "tss2.txt";	
	/**
	 * reads在GeneEnd上的分布信息文件，一列，用于excel使用
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_EXCEL = R_WORKSPACE_CHIP_READS_REGION + "geneEnd2.txt";	
	/**
	 * reads在TSS上的分布信息文件，一列，用于excel使用
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_TSS_RSCRIPT = R_WORKSPACE + "MyTSSReads.R";	
	/**
	 * reads在GeneEnd上的分布信息文件，一列，用于excel使用
	 */
	public final static String R_WORKSPACE_CHIP_READS_REGION_GENEEND_RSCRIPT = R_WORKSPACE + "MyGeneEndReads.R";	
	/////////////////////////R的density计算，可用于计算motif在指定序列上的分布///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * R的density计算的文件夹，可用于计算motif在指定序列上的分布
	 */
	public final static String R_WORKSPACE_DENSITY = R_WORKSPACE + "NormalDensity/";
	/**
	 * R的density计算的参数文件，保存本次计算的数据来源和数量，可用于计算motif在指定序列上的分布和Peak在TSS分布<br>
	 * 第一行大标题第二行xtitle，第三行ytitle
	 */
	public final static String R_WORKSPACE_DENSITY_PARAM = R_WORKSPACE_DENSITY + "param.txt";
	/**
	 * R的density计算的计算文件，保存本次计算的数据，可用于计算motif在指定序列上的分布<br>
	 * 数据每行一个，写成一列，没有标题
	 */
	public final static String R_WORKSPACE_DENSITY_DATA = R_WORKSPACE_DENSITY + "data.txt";
	/**
	 * R的density计算的脚本
	 */
	public final static String R_WORKSPACE_DENSITY_RSCRIPT = R_WORKSPACE + "NormalDensity.R";
	/**
	 * R的density计算的结果图片，保存本次计算的数据，可用于计算motif在指定序列上的分布
	 */
	public final static String R_WORKSPACE_DENSITY_PIC = R_WORKSPACE_DENSITY + "density.jpeg";
	
	
	
}
