<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h1>Dashboard</h1>
	<button type="button" onclick="alert('Running. Takes up to 10 minutes.')">Run Data Collection</button>
		

	<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.2/themes/smoothness/jquery-ui.css" />
  	<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
  	<script src="http://code.jquery.com/ui/1.10.2/jquery-ui.js"></script>
	
	<div id="dialog">
  		<form action="form_action.asp">
			<select name="words" multiple>
  				<option value="word1">word1</option>
  				<option value="word2">word2</option>
  				<option value="word3">word3</option>
  				<option value="word4">word4</option>
			</select>
			<input type="submit">
		</form>
	</div>
 
	<button id="opener">Edit Omitted Words</button>
	<script type="text/javascript">
		$(function() {
    		$( "#dialog" ).dialog({
     		 autoOpen: false,
     		 show: {
        		effect: "blind",
        		duration: 1000
      		},
      		hide: {
        		effect: "explode",
        		duration: 1000
      		}
    	});
 
    	$( "#opener" ).click(function() {
      		$( "#dialog" ).dialog( "open" );
    	});
  		});
	</script>
</body>
</html>