<!DOCTYPE html>
<!--[if lt IE 7 ]><html class="ie ie6" lang="de"> <![endif]-->
<!--[if IE 7 ]><html class="ie ie7" lang="de"> <![endif]-->
<!--[if IE 8 ]><html class="ie ie8" lang="de"> <![endif]-->
<!--[if (gte IE 9)|!(IE)]><!--><html lang="de"> <!--<![endif]-->
<head>
	
	<!-- Basic Page Needs
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<meta charset="utf-8">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>

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

	<!-- JavaScript
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<script src="jquery.min.js"></script>
	<script src="highlight.js"></script>
	<script src="Table2CSV.js"></script>
	
	<!-- CSS
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<link rel="stylesheet" href="style.css">

	<!-- JavaScript
	–––––––––––––––––––––––––––––––––––––––––––––––––– -->
	<script>
		$( document ).ready(function() {

			//SUBMIT
			$("#loader").hide();
			$('form#searchForm').submit(function(){
				$("#loader").show();
			    $(this).children('input[type=submit]').attr('disabled', 'disabled');
			});

			//HIGHLIGHT FOUND TEXT PASSAGES
			<#if queries??>
				<#list queries as q>
					$("#tableResults").highlight('${q!""}');
				</#list>
			</#if>

			//TOGGLE SOURCES COLUMN
			$(".toggleSources").click(function() {
				$(".columnSource").toggle();
				$(".columnHeaderSource").toggle();
			});

			//EXPORT BUTTON
			$("#exportButton").click(function() {
				$("#tableResults").TableCSVExport({
			        delivery: 'download',
			        filename: 'ANG-DB-${query!"Suche"}-Export.csv',
			        separator: ';'
			    });
			});
		    
		});
	</script>

</head>
<body>

	<div id="siteFrame">

		<div id="title" class="container">
			<!-- TITLE -->
			<div class="two columns"><h1><a href="search">ANG-DB</a></h1></div>

			<!-- SPACER -->
			<div class="one columns">&nbsp;</div>

			<!-- SEARCH -->
			<form action="search" id="searchForm">
				<div class="eight columns searchForm">
					<label for="caseSensInput">Groß-/Kleinschreibung beachten:</label>&nbsp;
					<input type="checkbox" id="caseSensInput" name="casesens" <#if casesens?? && casesens == true>checked</#if> />
					<br/><br/>
					<label for="useYearInput">Quellen ohne / mit anderem Datum ausschließen:</label>&nbsp;
					<input type="checkbox" id="useYearInput" name="useyear" <#if useyear?? && useyear == true>checked</#if> />
					<br/>
					<label for="yearFromInput">Jahr von:</label>&nbsp;
					<input type="number" id="yearFromInput" name="yearfrom" min="1" max="2200" value='${yearfrom!"1516"}' />
					<br/>
					<label for="yearToInput">Jahr bis:</label>&nbsp;
					<input type="number" id="yearToInput" name="yearto" min="1" max="2200" value='${yearto!"2020"}' />
				</div>
				
				<div class="five columns searchForm">
					<label for="queryInput">Suchbegriff:</label>&nbsp;
					<input type="text" id="queryInput" name="query" placeholder="z.B. Job" value='${query!""}' />
					<br/>
					<label for="sourceInput">Quelle:</label>&nbsp;
					<select id="sourceInput" name="source">
						<option value="" <#if source == "">selected</#if>>Alle Quellen</option>
						<option value="twitter" <#if source == "twitter">selected</#if>>Twitter-Korpus (mit Datum)</option>
						<option value="wacky" <#if source == "wacky">selected</#if>>Wacky-Korpus</option>
						<option value="dsa-struktur" <#if source == "dsa-struktur">selected</#if>>DSA-Struktur-Korpus</option>
						<option value="hamburg-dependency-treebank" <#if source == "hamburg-dependency-treebank">selected</#if>>Hamburg-DT</option>
						<option value="dortmunder chat-korpus" <#if source == "dortmunder chat-korpus">selected</#if>>DO Chat-Korpus (mit Datum)</option>
						<option value="spiegel" <#if source == "spiegel">selected</#if>>Spiegel-Archiv (mit Datum)</option>
						<option value="zeit" <#if source == "zeit">selected</#if>>Zeit-Archiv (mit Datum)</option>
						<option value="focus" <#if source == "focus">selected</#if>>Focus-Archiv (mit Datum)</option>
						<option value="juice" <#if source == "juice">selected</#if>>Juice-Archiv (mit Datum)</option>
						<option value="intro" <#if source == "intro">selected</#if>>Intro-Archiv (mit Datum)</option>
					</select>
					<br/><br/>
					<label for="lengthLimitInput">Zeichen vor und nach Fundstelle ausgeben:</label>&nbsp;
					<input type="number" id="lengthLimitInput" name="lengthlimit" min="10" value='${lengthlimit!"200"}' />
					<br/>
					<label for="maxDistanceInput">max. Abstand mehrerer Suchbegriffe:</label>&nbsp;
					<input type="number" id="maxDistanceInput" name="maxdistance" min="1" max="1000" value='${maxdistance!"100"}' />
					<br/><br/>
					<input id="searchButton" type="submit" value="Suchen" class="btn" />
				</div>
			</form>
			
		</div>
		
		<!-- CONTENT -->
		<#if query?? && query != "">
			<div id="pageContent" class="container">

				<!-- RESULTS -->
				<div id="resultsTitle" class="sixteen columns">
					<span class="bold">Suchbegriff:</span> "${query}"
					&nbsp;&nbsp;&mdash;&nbsp;&nbsp;
					<span class="bold">Quelle:</span>
					<#if source?? && source != "">
						"${source}"
					<#else>
						"alle Quellen"
					</#if>
					&nbsp;&nbsp;&mdash;&nbsp;&nbsp;
					<span id="exportButton" class="fakeLink">Tabelle als CSV (z.B. für MS Excel) exportieren</span>
				</div>
				
				<#if results??>
					<table id="tableResults" class="niceTable sixteenColumns">
						<tr id="tableHeader">
							<td class="columnHeaderText">Text <span class="toggleSources fakeLink">(Quellen ein/-ausblenden)</span></td>
							<td class="columnHeaderSource">Quelle</td>
						</tr>
						<#list results as result>
								<tr>
									<td class="columnText" data-match='${result.match!""}'>${result.text!"(kein Text)"}</td>
									<td class="columnSource">${result.source!"(keine Quelle)"}</td>
								</tr>
						</#list>
					</table>
				</#if>

			</div>
		<#else>
			<div class="textCenter" style="color: white; margin-top: 100px;">Bitte benutzen Sie die Suchfunktion!</div>
		</#if>

	</div>

	<div id="loader">
		Bitte warten, Datenbank wird durchsucht.<br/>
		Dies kann u.U. mehrere Minuten dauern...<br/><br/>
		<img src="loader.gif" alt=""/>
	</div>

</body>
</html>
				