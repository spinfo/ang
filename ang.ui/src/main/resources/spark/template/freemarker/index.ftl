<!DOCTYPE html>
<!--[if lt IE 7 ]><html class="ie ie6" lang="de"> <![endif]-->
<!--[if IE 7 ]><html class="ie ie7" lang="de"> <![endif]-->
<!--[if IE 8 ]><html class="ie ie8" lang="de"> <![endif]-->
<!--[if (gte IE 9)|!(IE)]><!--><html lang="de"> <!--<![endif]-->
<head>
	
	<!-- Basic Page Needs
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<meta charset="utf-8">
	<title>ANG-DB</title>
	<meta name="robots" content="noindex,nofollow">

	<!-- Mobile Specific Metas
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
	
	<!-- CSS Reset
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<link rel="stylesheet" href="reset.css">

	<!-- Skeleton Grid
	––––––––––––––––––––––––––––––––––––––––––––––––––
	<link rel="stylesheet" href="base.css"> -->
	<link rel="stylesheet" href="skeleton.css">
	
	<!-- CSS
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<link rel="stylesheet" href="style.css">

</head>
<body>

	<div id="siteFrame">

		<div id="title" class="container">
			<!-- TITLE -->
			<div class="two columns"><h1>ANG-DB</h1></div>

			<!-- SEARCH -->
			<div class="fourteen columns">
				<form action="search" id="searchForm">
					<label for="queryInput">Suchbegriff:</label>&nbsp;
					<input type="text" id="queryInput" name="query" placeholder="z.B. Job" />
					&nbsp;&nbsp;&nbsp;&nbsp;
					<label for="sourceInput">Quelle:</label>&nbsp;
					<input type="text" id="sourceInput" name="source" placeholder="leer = alle Quellen" />
					&nbsp;&nbsp;&nbsp;&nbsp;
					<input type="submit" value="Suchen" class="btn" onClick="document.getElementById('loader').style.visibility = 'visible';"/>
				</form>
			</div>
		</div>
		
		<!-- CONTENT -->
		<div id="pageContent" class="container">
			<!-- RESULTS -->
			<div class="sixteen columns">
				<#if results??>
					<table id="tableResults">
						<tr class="tableHeader">
							<td class="columnSource">Quelle</td>
							<td class="columnText">Text</td>
						</tr>
						<#list results as result>
							<tr>
								<td>${result.source}</td>
								<td>${result.text}</td>
							</tr>
						</#list>
					</table>
				</#if>
			</div>
		</div>
	</div>

	<div id="loader">
		Bitte warten,<br/>
		Datenbank wird durchsucht...<br/><br/>
		<img src="loader.gif" alt=""/>
	</div>

</body>
</html>
				