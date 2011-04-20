# 简单的coexpression计算方法，输入一个矩阵，第一列为title，返回两两之间的pearson系数和pvalue
# 格式如下：
#Gene	A	A	A
#A2M	1.27	2.41	3.62
#AHSG	0.54	0.82	1.07
#AMBP	0.65	1.25	3.05
#ANPEP	1.13	0.97	0.45
#APOD	0.89	0.86	0.48
#APOF	0.92	2.88	29.36
#APOH	1.43	1.25	2.74
#AZGP1	0.51	0.81	1.13
#BTD	1.06	0.87	0.54
#C1RL	1.4	0.94	0.63
#....
# Author: zong0jie
#####################################################################################
#参数设定
#待写入
workPath="/media/winE/Bioinformatics/R/practice_script/platform/coExpression"
setwd(workPath)
fileName="Data.txt";
param = scan("parameter.txt");# 参数设置，第一个为pearson阈值，第二个为pvalue阈值
resultFile = "result.txt"
#####################################################################################
pearsonCutOff = param[1];pvalueCutoff=param[2]
data = read.table(fileName,he=F,sep="\t") #第一行也是数据，不能添加row.names 因为row.names为主键，而这里会有重复基因
dataName = as.vector(data[,1])
data = as.matrix(data[,-1])
dataRowNum=dim(data)[1] #转换为矩阵方便后面计算
NumResult = ((dataRowNum-1)+1)*(dataRowNum-1)/2
#结果数据表
Result = data.frame(GENE1=0,Gene2=0,interaction=0,pvalue=0)
num=1;
for(i in 1:(dataRowNum-1))
{
	for(j in (i+1):dataRowNum)
	{
		tmpGene1 = dataName[i];
		tmpGene2 = dataName[j];
		tmpResult = cor.test(data[i,],data[j,])
		tmpcor = tmpResult$estimate
		tmppvalue = tmpResult$p.value
		if( tmppvalue>pvalueCutoff & ((tmpcor>=0 & tmpcor<pearsonCutOff) | (tmpcor <= 0 & tmpcor > -pearsonCutOff ) ) )
		{
			next
		}
		
		Result[num,]=c(tmpGene1,tmpGene2,tmpcor,tmppvalue)
		num = num+1
	}
}
pvalue = as.numeric(Result$pvalue)
fdr = p.adjust(pvalue,me="BH")
Result[length(Result)+1]=fdr;names(Result)[length(Result)]="fdr"
write.table(Result,resultFile,sep="\t")
