###############################################################################

# 读取Motif密度信息然后画图,被Motifsearch类调用
# 
# Author: zong0jie
###############################################################################
#x, 染色体坐标
#y，该坐标下Motif密度信息
#chraMaxLen 最长chrLen的长度
#maxPicLength 最长图片长度
#heigh 图像高度 
#ylength y轴的高度，当有值高于该点时，将该点删除
#thsicol 画图的线的颜色
#whichChain 哪条链，仅仅用于在图片上显示出来而已。默认为无
#mainCol 主字体的颜色
#file 保存的文件名，默认保存在R的工作空间，名字为PeakDensity
#####################################################################################
#参数设定
#待写入
motifsetwd="/media/winG/bioinformation/R/practice_script/"
x=scan(paste(motifsetwd,"motifx",sep=""));
y=scan(paste(motifsetwd,"motify",sep=""));
parameter=read.table(paste(motifsetwd,"parameter",sep=""),he=T)
chrMaxLen=as.integer(as.character(parameter[1,2]))
chrID=as.character(parameter[2,2])
#####################################################################################
#主函数

myMotifDensity = function(x,y,chrMaxLen,chrID="",maxPicLength=10000,heigh=1400,ylength=100,thiscol="blue",whichChain="",mainCol="green", file="MotifDensity")
{	
	thisfile=paste(motifsetwd,file,chrID,".jpg",sep="")
	jpeg(filename = thisfile, width = maxPicLength, height = heigh)#这样子才能画图
	if(maxPicLength>50000)
	{
		maxPicLength=50000
	}
	medy=median(y) 
	lable=c(1:length(x))
	ytmp=y
	lable=lable[y>ylength]  #获得y中大于ylength的序号
	y[y>ylength]=ylength #把y中大的都截短
	#dpi=chrMaxLen/maxPicLength;thiswidth=chrLen/dpi #本图片长度
	plot(c(0,chrMaxLen),c(0,ylength),col=rgb(0,0,0,0),xlab="",main="",ylab="") #按最长的染色体画出图像边框
	points(x,y,col=thiscol,ty="h")
	points(c(0,x[length(x)]),c(ylength,ylength),col="black",ty="h",lwd=5) #画出染色体边界
	
	if(length(lable)>0)
	{
		ybigNum=length(lable) #y中大于ylength的数目
		ybias=c(1:ybigNum*(50/ybigNum)-50)
		text(x[lable]+2,y[lable]+ybias,ytmp[lable]) #x,y数据向量，labes：整数或字符串，默认labels=1:length(x) 这里实际上是批量加点		
	}
	title(main=paste(whichChain,chrID," Motif Distribution",sep=" "),col="green",font.main=1,cex.main=2)
	title(ylab="Motif Density",cex.lab=1.5)
	title(xlab=paste("Length of Chr",chrID,sep=""),cex.lab=1.5)
	
	
	dev.off() #表示关闭设备
}
##########################################################################################

myMotifDensity(x,y,chrMaxLen,chrID)


