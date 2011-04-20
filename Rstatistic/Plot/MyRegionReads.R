###############################################################################


# 传入Reads密度，以及Region长度，画出Reads密度图
# 
# Author: zong0jie
###############################################################################
#x，reads数目
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
#####################################################################################

myRegionReads = function(x,picLength=1000,heigh=300,thiscol="blue",lwd=1,whichChain="",mainCol="green", file="TSSReads")
{
	thisfile=paste(file,".jpg",sep="")
	if(picLength>2000)
	{
		picLength=2000
	}
	
	jpeg(filename = thisfile, width = picLength, height = heigh)#这样子才能画图并输出
	
	
	maxx=max(x) 
	regionLeft=0
	regionRight=1
	regionall=seq(regionLeft,regionRight,le=length(x))
	
	
	
	############# 正 式 画 图 ##############################
	plot(c(regionLeft,regionRight),c(0,maxx),col=rgb(0,0,0,0),xlab="",main="",ylab="") #画出图像边框
	points(regionall,x,col=thiscol,ty="l",lwd=lwd)
	title(main=paste(whichChain," Reads Density in Region",sep=" "),col="green",font.main=1,cex.main=2)
	title(ylab="Normalized Counts",cex.lab=1.5)
	title(xlab="Region Length",cex.lab=1.5)
	axis(si=1,1:20*(1/20))
	#######################################################
	dev.off() #表示关闭设备
}
#######################################################################################
myTSSReads(x,pi=1200,he=500,lwd=1)
