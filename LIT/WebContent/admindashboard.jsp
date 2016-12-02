<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%
/**
* This class handles the Admin Dashboard.
* Provides a button to initiate the data collection sequence
* as well as providing access to the Omitted Words List
* Written by Joel Galva
*/
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Admin Dashboard</title>
</head>
<body>
	<h1>Administrator Dashboard</h1>
	<!--  Logic to initiate the data collection and handle/print responses. -->
	<script type="text/javascript"
    src="http://code.jquery.com/jquery-latest.min.js"></script>
	<script>
    $(document).on("click", "#datacollection",
            function() {
    			// turn off the buttons during the collection so only one task goes on at a time
    			$('#datacollection').attr('disabled','disabled');
    			$('#opener').attr('disabled','disabled');
    			// start the data collection
				$.get("DataCollection", function(){});
				var data = "";
				// request status on an interval
    			var interval = setInterval(function()
    				{
    				    $.ajax({
    				        url: 'DataCollection',
    				        data: data,
    				        type: 'post',
    				        success: function(data){ 
    				        	// if finished, turn buttons back on and exit interval loop
    				        	if (data == "0") {
    				        		$('#output').append("Finished successfully!");
    				        		$('#datacollection').attr('enabled','enabled');
    				        		$('#opener').attr('enabled','enabled');
    				        		clearInterval(interval);
    				        	}	
    				        	// if finished with error, do the same as above with message
    				        	else if (data == "1") {
    				        		$('#output').append("Finished with errors!");
    				        		$('#datacollection').attr('enabled','enabled');
    				        		$('#opener').attr('enabled','enabled');
    				        		clearInterval(interval);
    				        	}
    				        	// otherwise, print status to div
    				        	else if (data.length > 1){
    				        		$('#output').append(data +'<br/>');
    				        	}
    				        }
       					});
    				}, 10000);
            });
    </script>
    <!-- Button to start data collection and div where status will be printed. -->
    <button id="datacollection">Start DataCollection</button>
    <div id="output"></div>
		
	<!-- Add in logic to pull the omitted words list -->>
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
	<a href="logout.jsp">Logout</a>
</body>
</html>