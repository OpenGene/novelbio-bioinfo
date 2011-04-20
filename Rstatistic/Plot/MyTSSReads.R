###############################################################################


# 传入TssReads密度，以及Tss左右两端bp数，画出Reads密度图,被TssDistance类调用
# 
# Author: zong0jie
###############################################################################
#x，TSS左右的reads数目
#region TSS左右的Reads范围
#PicLength 图片长度
#heigh 图像高度 
#thsicol 画图的线的颜色
#whichChain 哪条链，仅仅用于在图片上显示出来而已。默认为无
#mainCol 主字体的颜色
#file 保存的文件名，默认保存在R的工作空间，名字为PeakDensity
#####################################################################################
#参数设定
#待写入
motifsetwd="/media/winG/bioinformation/R/practice_script/platform/tmp/"
setwd("/media/winG/bioinformation/R/practice_script/platform/tmp")
x=scan("tss.txt");
region=scan("parameter.txt");
#####################################################################################

myTSSReads = function(x,region=10000,picLength=1000,heigh=300,thiscol="blue",lwd=1,whichChain="",mainCol="green", file="TSSReads")
{
	region=region*2
	thisfile=paste(file,".jpg",sep="")
	if(picLength>2000)
	{
		picLength=2000
	}
	
	jpeg(filename = thisfile, width = picLength, height = heigh)#这样子才能画图并输出
	
	
	maxx=max(x) 
	regionLeft=-region/2
	regionRight=region/2
	regionall=seq(regionLeft,regionRight,le=length(x))
	
	
	
	############# 正 式 画 图 ##############################
	plot(c(regionLeft,regionRight),c(0,maxx),col=rgb(0,0,0,0),xlab="",main="",ylab="") #按最长的染色体画出图像边框
	points(regionall,x,col=thiscol,ty="l",lwd=lwd)
	title(main=paste(whichChain," Reads Density Near TSS",sep=" "),col="green",font.main=1,cex.main=2)
	title(ylab="Normalized Counts",cex.lab=1.5)
	title(xlab="Position relative to TSS",cex.lab=1.5)
	axis(si=1,1:20*(region/20)-region/2)
	#######################################################
	dev.off() #表示关闭设备
}
#######################################################################################
myTSSReads(x,region,pi=1200,he=500,lwd=1)
