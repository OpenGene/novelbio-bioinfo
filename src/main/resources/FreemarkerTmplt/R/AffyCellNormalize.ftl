filePath = "${workspace}"
fileOutName = "${fileOutName}"
setwd(filePath)
library(gcrma)
<#if isNorm>
   library(affy);
   Data = ReadAffy(${RawCelFile})
<#else>
	library(oligo);
	Data = read.celfiles(${RawCelFile})
</#if>
eset=${normalizedType}(Data)
#NormalizedMethod@//@30#/#eset=mas5(Data)@@10#/#eset=rma(Data)@@20#/#eset=gcrma(Data)
write.exprs(eset, file=fileOutName) #输出标准化的数据
