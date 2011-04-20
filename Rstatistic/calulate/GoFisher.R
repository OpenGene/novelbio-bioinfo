#读取文本中的四列，计算fisher_pvalue和BH的fdr
#文本第一行为正文，如果是标题，标题不能为中文
#待读取的列必须依次为:第1列 差异基因落在GO中的数据 第2列差异基因数              第3列go_gene_count 第4列全部基因数 
#最后把所有数据都写入新的文本并且在最后两列加上pvalue和fdr
##################读   取  文  件#############################################
readfile="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOInfo.txt";#待读取的文件
readrow=c(1,2,3,4) #待读取的列
writefile="/media/winE/Bioinformatics/R/practice_script/platform/GoFisher/GOAnalysis.txt";#最后写入的文件
###################开  始  计  算################################################

#data=read.table(readfile,he=T,sep="\t")
rowdata=read.table(readfile,he=F,sep="\t")
#将待处理的几列提取出来
data=cbind(rowdata[,readrow[1]],rowdata[,readrow[2]],rowdata[,readrow[3]],rowdata[,readrow[4]])
dimdata=dim(rowdata)
datarowNum=dimdata[1]
datacolumNum=dimdata[2]
#最后结果p
p=0
for (i in 1:datarowNum)
{
	tmpdata=matrix(data[i,],nr=2,by=T)
	fisher=fisher.test(tmpdata)
	p[i]=fisher$p.value
}
fdr=p.adjust(p,"BH")
logp=-log2(p)
enrichment=(data[,1]/data[,2])/(data[,3]/data[,4])
rowdata[,datacolumNum+1]=p
rowdata[,datacolumNum+2]=fdr
#colnames(data)[datacolumNum+1]="pvalue"
rowdata[,datacolumNum+3]=enrichment
rowdata[,datacolumNum+4]=logp
#colnames(data)[datacolumNum+2]="pvalue"
##############写  入  结  果#################
write.table(rowdata, file = writefile,sep = "\t", eol = "\n")
