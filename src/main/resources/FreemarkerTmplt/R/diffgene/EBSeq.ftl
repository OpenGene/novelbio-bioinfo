filePath = "${workspace}"
fileName = "${filename}"
setwd(filePath)
library(EBSeq)
data = read.table(fileName, he=T, sep="\t", row.names=1)
size = MedianNorm(data)

<#list mapOut2vector_factor?keys as outFile >
	dataRun=data[,c(${mapOut2Compare_vector[outFile][0]})]
	sizeRun = size[c(${mapOut2Compare_vector[outFile][0]})]
	EBOutRun<- EBTest(Data=dataRun, Conditions=as.factor(c(${mapOut2Compare_vector[outFile][1]})),sizeFactors=sizeRun, maxround=15)
	PP.norep=GetPPMat(EBOut1)
	${mapOut2sample[outFile][0]}<- unlist(EBOut1$C1Mean)
	${mapOut2sample[outFile][1]}<- unlist(EBOut1$C2Mean)
	LogFC<- log(C2M/C1M)
	colnames(out1)<-c("${mapOut2sample[outFile][0]}","${mapOut2sample[outFile][1]}","LogFC", "FDR")
	write.table(out1, file="${outFile}", row.names=TRUE,col.names=TRUE,sep="\t")
</#list>
