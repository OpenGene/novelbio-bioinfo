library(VennDiagram)
<#assign params="">
<#list data?keys as itemKey>
	<#if params == "">
		<#assign params = '\"'+ itemKey+'\"=' + "A"+itemKey_index>
	<#else>
		<#assign params = params + ',\"' + itemKey +'\"=A' + itemKey_index>
	</#if>
data=read.table("${data[itemKey]}", header=F,sep="\t") 
A${itemKey_index}=as.vector(data[,1])
</#list>
data=list(${params})
venn.diagram(x=data,filename="${vennImage.savePath}",height=${vennImage.height}, width=${vennImage.width},fill=${vennImage.fillColors},cat.col=${vennImage.catcol}<#if vennImage.main != "">, main="${vennImage.main}"</#if><#if vennImage.sub != "">, sub="${vennImage.sub}"</#if>,alpha=${vennImage.alpha},scaled = TRUE,rotation.degree = ${vennImage.rotation},fontface = "bold",margin = ${vennImage.margin},cex=${vennImage.cex}, main.cex=${vennImage.maincex}, cat.cex=${vennImage.catcex})


