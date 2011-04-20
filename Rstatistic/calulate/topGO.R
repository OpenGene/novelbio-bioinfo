#读取workPath下的GeneID.txt文件和BG2GO.txt文件，其中GeneID为一个一个geneID空格分开，用scan读取
#BG2GO.txt是topGo支持的BackGround文件
#读取parameter.txt文件，第一个记录选择BP、MF、CC，第二个记录写文件，默认是GoResult.txt ，第三个是数字，表示显示多少个GOTerm ,第四个记录GOInfo，就是每个GO对应的基因文件名
workPath = "/media/winE/Bioinformatics/R/practice_script/platform/topGO"
setwd(workPath)
library(topGO)
#第一个记录选择BP、MF、CC，第二个记录写文件，默认是GoResult.txt
#第三个是数字，表示显示多少个GOTerm ,第四个记录GOInfo，就是每个GO对应的基因文件名
parameter = scan("parameter.txt",what="character")
#读取感兴趣基因，按照字符串读取
calGeneID = scan("GeneID.txt",what="character")

#读取背景
geneID2GO = readMappings(file = "BG2Go.txt")

#获得基因名字
geneNames = names(geneID2GO)

#设定 geneList
geneList = factor(as.integer(geneNames %in% calGeneID))
names(geneList) = geneNames

#设定GO分析类型
GOdata = new("topGOdata", ontology = parameter[1], allGenes = geneList, annot = annFUN.gene2GO,gene2GO = geneID2GO)

#显著性分析
# Fisher
#test.stat = new("classicCount", testStatistic = GOFisherTest, name = "Fisher test")
#resultFisher = getSigGroups(GOdata, test.stat)
#Ks
#test.stat = new("classicScore", testStatistic = GOKSTest, name = "KS tests")
#resultKS = getSigGroups(GOdata, test.stat)

#elim algorithm with KS test 
#test.stat = new("elimScore", testStatistic =  GOFisherTest,, name = "Fisher test", cutOff = 0.05)
#resultElim = getSigGroups(GOdata, test.stat)
resultFis = runTest(GOdata, algorithm = "elim", statistic = "fisher")

######这个似乎很有趣#####weight algorithm with Fisher’s exact test one types:
#test.stat = new("weightCount", testStatistic = GOFisherTest, name = "Fisher test", sigRatio = "ratio")
#resultWeight = getSigGroups(GOdata, test.stat)
GONum = as.numeric(parameter[3])
#1:GOID,2:GOTerm,3:BGanoNum,4:SiganoNum,5:Expected,6:pvalue
allRes = GenTable(GOdata, pvalue = resultFis, orderBy = "pvalue",numChar = GONum, topNodes = GONum)
#背景基因注释的个数
allNumBG = rep(numGenes(GOdata),length(allRes[,1]))
#差异基因注释的个数
allNumSig = rep(length(sigGenes(GOdata)),length(allRes[,1]))
#富集倍数
foldEnrichment = (allRes[[4]]/allNumSig)/(allRes[[3]]/allNumBG)
#-logP
logP =-log2(as.numeric(allRes[[6]])) 
#fdr
fdr=p.adjust(as.numeric(allRes[[6]]),"BH")
#整理结果
allResResult = cbind(allRes[1],allRes[2],allRes[4],allNumSig,allRes[3],allNumBG,allRes[6],fdr,foldEnrichment,logP)
write.table(allResResult,file = parameter[2],sep="\t")

#printGraph(GOdata, resultElim, firstSigNodes = 5, fn.prefix = "tGO", useInfo = "all", pdfSW = TRUE)
GOInfoFile = parameter[4]
#清空文件
write("",file=GOInfoFile)
for(i in 1:GONum)
{
	write(paste("#",allRes[i,1],sep=""),file = GOInfoFile,ap = T)
	GOID = genesInTerm(GOdata,whichGO = allRes[i,1])[[1]]
	write(GOID,file = GOInfoFile, ap = T)
}
