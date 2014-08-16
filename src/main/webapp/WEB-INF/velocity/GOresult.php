<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Novelbio </title>
<link href="nbcTitle.css" rel="stylesheet" type="text/css" />
<link href="workTab.css" rel="stylesheet" type="text/css" />
</head>

<body>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
<div class="nbcTitle" id="GO"><img src="NBClogofinal2_400resolution.png" width="148" height="62" alt="nbcLogo" />
  <div class="nbcTitleInfo" id="GOInfo">Novelbio</div>
</div>
<div class="workTab" id="GOtab">
<ul>

<?php echo $state;
if ($state == 'OK')
{
	echo "<p><a href=\"../../gofile/goResult\">GOresult</a></p>";
}

?>
<?php foreach($upload_data1 as $item => $value):?>
<li><?php echo $item;?>: <?php echo $value;?></li>
<?php endforeach; ?>
</ul>
<ul>
<?php foreach($upload_data2 as $item => $value):?>
<li><?php echo $item;?>: <?php echo $value;?></li>
<?php endforeach; ?>
</ul>

<ul>
<?php foreach($param as $item => $value):?>
<li><?php echo $item;?>: <?php echo $value;?></li>
<?php endforeach; ?>
</ul>
<ul>
<?php foreach($info as $item => $value):?>
<li><?php echo $item;?>: <?php echo $value;?></li>
<?php endforeach; ?>
</ul>
<ul>
<?php echo $result;?>
</ul>
<p>&nbsp;</p>
</div>
</body>
</html>
