# 给定一系列数，画出它们的柱状图
# 
# Author: zong0jie
###############################################################################
#x, 染色体坐标
#y，该坐标下reads数目
#chrLen 本条chrLen的长度
#chraMaxLen 最长chrLen的长度
#maxPicLength 最长图片长度
#pvalueMethod 计算pvalue使用的方法，默认是"fisher"fisher检验，可以选择"chisq"，也就是卡方检验
#heigh 图像高度 
#ylength y轴的高度，当有值高于该点时，将该点删除
#thsicol 画图的线的颜色
#whichChain 哪条链，仅仅用于在图片上显示出来而已。默认为无
#mainCol 主字体的颜色
#file 保存的文件名，默认保存在R的工作空间，名字为PeakDensity
#################读取的文件格式#####################
#item	PeakInfo	PeakInfo proportion	GenomeBackGround	GenomeBackGround proportion
#5UTR	12	0.010425716768027803	18504993	0.005977687721854752
#3UTR	5	0.004344048653344918	33988082	0.010979206555808614
#Exon	14	0.01216333622936577	31671511	0.010230882142851267
#Intron	355	0.30842745438748914	1101050883	0.35567364680691493
#Up3k	31	0.02693310165073849	41306000	0.013343121450461093
#InterGenic	734	0.6377063423110338	1869155967	0.6037954553221093
#说明，第一列为名字，之后每两列为一个项目，其中第一列是数目，第二列是比例
#####################################################################################
#参数设定
#读取和写入的文件夹为一个文件夹
savewd="/media/winG/NBC/Project/ChIP-SeqCDG20100911/result/geneStructure/" 
#读取的文件名
data=read.table(paste(savewd,"46_NP_GeneStructure.txt",sep=""),sep="\t",he=T)
pvalueMethod="chisq"
txtxBias=-4#pvalue值向右的偏移
txtxInv=1.2#两个pvalue之间的间隔
xlab="Gene Structure"
ylab="Proportion"
file="batPlot"#文件名
#####################################################################################
myBarPlot=function(data,savewd,xlab,ylab,txtxBias=-1.5,txtxInv=1,pvalueMethod="fisher",file="batPlot",offset=0,width=1,picWidh=1500,picHigh=800)
{	
	txtyBias=0.02 #pvalue值向上的偏移
	#sigtxtxBias=-1 #单组值时，pvalue值向右的偏移
	#txtxBias=-1.5 #pvalue值向右的偏移
	legendBias=-0 #legend值向右的偏移
	usualcol=c("darkred","darkblue","darkgreen","blue","orange","red",
			"green","pink","cyan","purple","violet","skyblue",
			"darkcyan","khaki","darkkhaki","gray","black")
	
	##############准备工作################################################################################
	barNames=data[,1];
	i=c(2*1:((length(data)-1)/2)) ;legend=colnames(data)[i] #i存储了每一个项目(如PeakInfo之类的)的实际数目所在列，本例中即为2，4。一般是2，4，6，8，10......
	j=i[-c(length(i))] #除去了最后背景的每个项目的实际所在列
	###计算pvalue，用fisher精确检验,仅用于两列计算pvalue，一列是当前值，一列是背景##
	sumAll=colSums(data[,i]);#将所有待处理的数据提取出来求和
	m=0
	pvalue=0#保存最后的p值
	for(m in 1:length(data[,1]))
	{
		tmpp=0
		num=1
		for(k in j)
		{
			pvaluePro=c(data[m,k],data[m,i[length(i)]],sumAll[num],sumAll[length(i)])
			dim(pvaluePro)=c(2,2);
			################将数字减小一点，因为太大了fisher检验无法进行
			if(pvalueMethod=="fisher")
			{
				pvaluePro[2,]=pvaluePro[2,]/10
				################################################
				tmpp[num]=fisher.test(pvaluePro)$p.value#存储了每个分组的所有项目的pvalue(如 5UTR中 所有项目的pvalue)
			}else if(pvalueMethod=="chisq")
			{
				tmpp[num]=chisq.test(pvaluePro)$p.value#存储了每个分组的所有项目的pvalue(如 5UTR中 所有项目的pvalue)
			}
			
			num=num+1
		}
		if(m==1) #将pvalue
		{
			pvalue=tmpp
		}else
		{
			pvalue=rbind(pvalue,tmpp)#将pvalue横着排在一起，就是每一行一个项目的pvalue
		}
	}
	##############pvalue小数点后几位保留####################################################
	ncol=dim(pvalue)[2]
	pvalue=round(pvalue,4);txtp="pvalue="
	txtpvalue=paste(txtp,pvalue,sep="")
	txtpvalue=matrix(pvalue,byrow=F,ncol=ncol)
	######################################################################
	data=t(as.matrix(data[i+1]))#整理data，只读取3，5，7，9......项
	########################   画   图   ####################################################################################
	thisfile=paste(savewd,file,".jpg",sep="") #文件名
	jpeg(filename = thisfile, width = picWidh, height = picHigh)#这样子才能画图并输出
	data=data-offset
	par( mar=c(5,5,5,5) )
	plotXlen=dim(data)[1]*dim(data)[2]*width+dim(data)[2]*1.5*width#图中x轴的长度
	plotYlen=max(data)*1.1#图中y轴的长度
	plot(c(0,plotXlen),c(0,plotYlen),col=0,main="",xlab="",ylab="",axes=FALSE)
	barcol=usualcol[1:dim(data)[1]]#使用的颜色
	barWidth=width*1#设置柱子之间的距离为柱子宽度的一倍
	barplot(data,width=1, #柱子的宽度
			col=barcol,#颜色，最多不能超过 length(usualcol)
			beside=T,#T时柱子并排，一般我们习惯T
			space=c(0,barWidth), #柱子之间的距离
			names.arg=barNames,cex.names=1.5, #每组柱子的名字
			legend.text=F, #柱状图的图例，后面手动加
			border="white",    #柱子的边框颜色
			cex.axis=1.5, #y轴数字的大小
			offset=offset, #y轴的偏移，是y轴原点为offset所设置的值，但是这个不改变c的高度。也就是说和数据无关，那么做offset的时候c要先减去offset的值
			add=T)
	legend(plotXlen/2*0.8+legendBias,plotYlen*0.95,legend=legend,fill=barcol,cex=1.5) #手动加图例
	#加pvalue
	lable=c(1:length(data[1,])*(width*length(data[,1])+barWidth))
	#if(is.null(dim(pvalue)))
	#{
	#	text(lable+sigtxtxBias,apply(data,2,max)+txtyBias,txtpvalue,cex=1) 
	#}else
	#{
		for(kk in 1:dim(pvalue)[2])
		{
			text(lable+txtxBias+kk*txtxInv-2,apply(data,2,max)+txtyBias,pvalue[,kk],cex=1) #x,y数据向量，labes：整数或字符串，默认labels=1:length(x) 这里实际上是批量加pvalue
		}
	#}
	
	#标题
	#line:标题相对于坐标轴的偏移，远离图片方向的偏移
	title(ylab=ylab,cex.lab=2,line=3) 
	title(xlab=xlab,cex.lab=2,line=3) 
	dev.off();
}
#################################################################################
myBarPlot(data,pvalueMethod=pvalueMethod,txtxInv=txtxInv,txtxBias=txtxBias,xlab=xlab,ylab=ylab,savewd=savewd,file=file)









