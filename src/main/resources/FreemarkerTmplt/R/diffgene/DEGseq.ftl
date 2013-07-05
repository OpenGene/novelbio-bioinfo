filePath = "${workspace}"
fileName = "${filename}"
setwd(filePath)
library(DEGseq)

<#list mapSample2LsCol?keys as SampleName>
  ${SampleName} = readGeneExp(fileName, header=T, sep='\t', geneCol=${mapSample2LsCol[SampleName][0]}, valCol=c(${mapSample2LsCol[SampleName][1]}))
</#list>

<#assign SampleTreat=0>
<#assign SampleTreatNum=1>
<#assign SampleTreatName=2>
<#assign SampleControl=3>
<#assign SampleControlNum=4>
<#assign SampleControlName=5>
<#assign OutDir=6>

<#list lsOutFileInfo as outInfo>
   DEGexp(geneExpMatrix1 = ${outInfo[SampleTreat]}, geneCol1 = 1, 
   expCol1 = c(${outInfo[SampleTreatNum]}), groupLabel1 = '${outInfo[SampleTreatName]}', 
   geneExpMatrix2 = ${outInfo[SampleControl]}, geneCol2 = 1, expCol2 = c(${outInfo[SampleControlNum]}), 
   groupLabel2 = '${outInfo[SampleControlName]}', method = 'MARS', outputDir='${outInfo[OutDir]}')
</#list>
