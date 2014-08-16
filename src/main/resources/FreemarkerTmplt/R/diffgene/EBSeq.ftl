filePath = "${workspace}"
fileName = "${filename}"
setwd(filePath)
library(EBSeq)
data = read.table(fileName, he=T, sep="\t", row.names=1)
data = as.matrix(data)
size = QuantileNorm(data, 0.75)

<#list mapOut2Compare_vector?keys as outFile >
dataRun=data[,c(${mapOut2Compare_vector[outFile][0]})]
sizeRun = size[c(${mapOut2Compare_vector[outFile][0]})]
vec = as.factor(c(${mapOut2Compare_vector[outFile][1]}))
	<#if isSensitive>
EBOutRun<-EBTest(Data=dataRun, Conditions=vec,sizeFactors=sizeRun, maxround=1, ApproxVal=0.3)
	<#else>
EBOutRun<-EBTest(Data=dataRun, Conditions=vec,sizeFactors=sizeRun, maxround=15)
	</#if>
GenePP1=GetPPMat(EBOutRun)
FDR<-GenePP1[,1]

LogFC=log2(PostFC(EBOutRun)$RealFC)

out<-cbind(unlist(EBOutRun$C1Mean),unlist(EBOutRun$C2Mean), LogFC,FDR )	
colnames(out)<-c(levels(vec)[1],levels(vec)[2],"LogFC", "FDR")

write.table(out, file="${outFile}", row.names=TRUE,col.names=TRUE,sep="\t")
</#list>
