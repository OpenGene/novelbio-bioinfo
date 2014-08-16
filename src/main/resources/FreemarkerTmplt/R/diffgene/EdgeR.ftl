filePath = "${workspace}"
fileName = "${filename}"
setwd(filePath)
library(edgeR)
<#if isSensitive>
bcv = 0.01
<#else>
bcv = 0.3
</#if>
plus = 0
data = read.table(fileName, he=T, sep="\t", row.names=1)
data = data + plus
group = factor( c(${Group}) )
m = DGEList(counts=data,group=group)
<#if isReplicate>
m = estimateCommonDisp(m);m = estimateTagwiseDisp(m)
</#if>


<#list mapCompare2Outfile?keys as Compare>
  	<#if isReplicate>
result = exactTest(m, pair=c(${Compare})); resultFinal=result$table;
result2=cbind(resultFinal,fdr=p.adjust(resultFinal[,3])); write.table(result2,
file="${mapCompare2Outfile[Compare]}",sep="\t")
  	<#else>
result = exactTest(m, pair=c(${Compare}), dispersion=bcv^2); 
resultFinal=result$table; 
result2=cbind(resultFinal,fdr=p.adjust(resultFinal[,3]));  
write.table(result2, file="${mapCompare2Outfile[Compare]}",sep="\t")
  	</#if>
</#list>
