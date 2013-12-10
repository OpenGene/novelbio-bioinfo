filePath = "${workspace}"
fileName = "${filename}"
setwd(filePath)
library(limma)
eset=read.table(file=fileName,he=T,sep="\t",row.names=1)
<#if !islog2>
eset = log2(eset)
</#if>
design = model.matrix(~ -1+factor (c(${design})))
colnames(design) = c(${SampleName})
contrast.matrix = makeContrasts( ${PairedInfo}levels=design)
fit = lmFit(eset, design)
fit2 = contrasts.fit(fit, contrast.matrix)
fit2.eBayes = eBayes(fit2)
<#list pair2filename?keys as pair>
write.table(topTable(fit2.eBayes, coef="${pair}", adjust="fdr", sort.by="B", number=50000),  file="${pair2filename[pair]}", row.names=T, sep="\t")
</#list>
