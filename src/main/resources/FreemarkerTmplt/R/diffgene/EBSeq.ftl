filePath = "${workspace}"
fileName = "${filename}"
setwd(filePath)
library(EBSeq)
data = read.table(fileName, he=T, sep="\t", row.names=1)
data = as.matrix(data)
size = MedianNorm(data)

<#list mapOut2Compare_vector?keys as outFile >
	dataRun=data[,c(${mapOut2Compare_vector[outFile][0]})]
	sizeRun = size[c(${mapOut2Compare_vector[outFile][0]})]
	EBOutRun<- EBTest(Data=dataRun, Conditions=as.factor(c(${mapOut2Compare_vector[outFile][1]})),sizeFactors=sizeRun, maxround=15)
	GenePP1=GetPPMat(EBOutRun)
	FDR<-GenePP1[,1]
	
	${mapOut2sample[outFile][0]}<- unlist(EBOutRun$C1Mean)
	${mapOut2sample[outFile][1]}<- unlist(EBOutRun$C2Mean)
	LogFC=PostFC(EBOutRun)$RealFC
	
	out<-cbind(${mapOut2sample[outFile][0]},${mapOut2sample[outFile][1]}, LogFC,FDR )	
	colnames(out)<-c("${mapOut2sample[outFile][0]}","${mapOut2sample[outFile][1]}","LogFC", "FDR")
	write.table(out, file="${outFile}", row.names=TRUE,col.names=TRUE,sep="\t")
	
</#list>
