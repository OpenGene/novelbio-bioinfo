filePath = "${workspace}"
setwd(filePath)
library(topGO)

GOType="${GOtype}"
GONum=${GONum}
GoResult="${GoResultFile}"
GoMapPdfPrefix="${GoMapPdfPrefix}"

GOInfoFile="${GOInfoFile}"

firstSigNodesNum=${firstSigNodes}
useInfo="${useInfo}"


calGeneID = scan("${CalGeneIDFile}",what="character")
geneID2GO = readMappings(file = "${BGGeneFile}")

geneNames = names(geneID2GO)
geneList = factor(as.integer(geneNames %in% calGeneID))
names(geneList) = geneNames

GOdata = new("topGOdata", ontology = GOType, allGenes = geneList, annot = annFUN.gene2GO,gene2GO = geneID2GO)
resultFis = runTest(GOdata, algorithm = "${GOAlgorithm}", statistic = "fisher")

#1:GOID,2:GOTerm,3:BGanoNum,4:SiganoNum,5:Expected,6:pvalue
allRes = GenTable(GOdata, pvalue = resultFis, orderBy = "pvalue",numChar = GONum, topNodes = GONum)
allNumBG = rep(numGenes(GOdata),length(allRes[,1]))
allNumSig = rep(length(sigGenes(GOdata)),length(allRes[,1]))
foldEnrichment = (allRes[[4]]/allNumSig)/(allRes[[3]]/allNumBG)
logP =-log2(as.numeric(allRes[[6]])) 
fdr=p.adjust(as.numeric(allRes[[6]]),"BH")
allResResult = cbind(allRes[1],allRes[2],allRes[4],allNumSig,allRes[3],allNumBG,allRes[6],fdr,foldEnrichment,logP)
write.table(allResResult, file = GoResult, sep="\t")
printGraph(GOdata, resultFis, firstSigNodes = firstSigNodesNum, fn.prefix = GoMapPdfPrefix, useInfo = useInfo, pdfSW = TRUE)

write("",file=GOInfoFile)
for(i in 1:GONum)
{
	write(paste("#",allRes[i,1],sep=""),file = GOInfoFile,ap = T)
	GOID = genesInTerm(GOdata,whichGO = allRes[i,1])[[1]]
	write(GOID,file = GOInfoFile, ap = T)
}
