###############################################################################

# 读取java CompareList产生的文本pvalueCal，返回pvalue
# 
# Author: zong0jie
###############################################################################
# 给定ls1和ls2的数目，总数据集数目，和交集数目
# 返回该交集的exactly概率和at least 概率
# 读取的overlapInfo文件中包含以下信息并都用空格隔开： ls1数目 ls2数目 交集数目 总数据集数目
# overlapFile 待读取的文件
# writefile 待写入的文件
# Author: zong0jie
###############################################################################
#参数设定
overlapFile="./compare_lists_cgi/output/RpvalueCal"  #待读取的java产生的文件,因为读取时是从compare_lists_cgi的上一层目录读取，所以此时R的工作空间为compare_lists_cgi的上一层目录
writefile="./compare_lists_cgi/output/Probability values"
#################################################################################

ovlp=scan(overlapFile)

strResult1="probability that 2 random subsets have"
strResult2="resp."
strResult3="elements, picked from a set of"
strResult4="elements, have an intersection"

strResult= paste(strResult1, ovlp[1],strResult2,ovlp[2],strResult3,ovlp[4],strResult4,sep=" ")


strFinal1="of exactly"
strFinal2="elements is"

strFinal3="of at least"
strFinal4="elements is"

exactPvalue=function(ls1,ls2,intersection,all)
{
	exactpvalue= choose(all,ls1) * choose(ls1,intersection)*choose((all-ls1),(ls2-intersection)) / (choose(all,ls1)*choose(all,ls2))
	exactpvalue
}

atLeastPvalue=function(ls1,ls2,intersection,all)
{
	i=0
	atLstP=0
	lsmin=min(ls1,ls2)
	#将所有数值小于ls1（写的不一定对）的pvalue累加起来
	for(i in intersection:lsmin)
	{
		atLstP=atLstP+exactPvalue(ls1,ls2,i,all)
	}
	atLstP
}
exPvalue = exactPvalue(ovlp[1],ovlp[2],ovlp[3],ovlp[4])
atlsPvalue = atLeastPvalue(ovlp[1],ovlp[2],ovlp[3],ovlp[4])

exPresult = paste(strFinal1,ovlp[3],strFinal2,exPvalue,sep=" ")
atlsPresult = paste(strFinal3,ovlp[3],strFinal4,atlsPvalue,sep=" ")

write(strResult,file=writefile)
write(exPresult,file=writefile,ap=T)
write(atlsPresult,file=writefile,ap=T)
