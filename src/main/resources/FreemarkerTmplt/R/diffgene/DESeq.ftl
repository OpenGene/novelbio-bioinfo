filePath = "${workspace}"
fileName = "${filename}"
setwd(filePath)
library(DESeq)
data = read.table(fileName, he=T, sep="\t", row.names=1)


conds = factor( c(${Group}) )
cds = newCountDataSet( data, conds )
cds = estimateSizeFactors(cds)
<#if isRepeatExp>
   cds =  estimateDispersions(cds)
<#else>
   cds = estimateDispersions( cds, method="blind", sharingMode="fit-only", fitType="local")
</#if>

<#list mapGroup2Out?keys as CompareGroup>
   res = nbinomTest( cds, ${CompareGroup} )
   write.table( res, file="${mapGroup2Out[CompareGroup]}",sep="\t",row.names=F  )
</#list>

