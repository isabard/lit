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
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
</head>
<body>
	<h1>Administrator Dashboard</h1>
	<!--  Logic to initiate the data collection and handle/print responses. -->
	<script>
    $(document).on("click", "#datacollection",
            function() {
    			// turn off the buttons during the collection so only one task goes on at a time
    			$('#datacollection').prop('disabled', true);
		        $('#editowl').prop('disabled', true);
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
    				        		$('#datacollection').prop('disabled', false);
    				        		$('#editowl').prop('disabled', false);
    				        		clearInterval(interval);
    				        	}	
    				        	// if finished with error, do the same as above with message
    				        	else if (data == "1") {
    				        		$('#output').append("Finished with errors!");
    				        		$('#datacollection').prop('disabled', false);
    				        		$('#editowl').prop('disabled', false);
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
		
	<!-- Edit Ommitted Words List -->
	<script>
	 $(document).on("click", "#editowl", 
			 function() {
		 		// turn off the buttons during the collection so only one task goes on at a time
    			$('#datacollection').attr('disabled','disabled');
    			$('#editowl').attr('disabled','disabled');
    			// get the current list
    			var data = "";
    			$.ajax({
    					url: 'EditOmmittedWordsList',
    					data: data,
    					type: 'get',
    					success: function(data) {
    						// add elements for text and add/remove buttons
	    					$('#add').prop('disabled', false);
	    					$('#remove').prop('disabled', false);
	    					$('#cancel').prop('disabled',false);
	    					$('#owloutput').text("Current words:");
	    					$('#owloutput').append('<br>');
	    					$('#owloutput').append(data);
    					},
    					error: function(request, status, error) {alert(request.responseText);}
    			});
	 });
	 </script>
	 <script>
	// add to list and give output
		$(document).on("click", "#add", 
				function() {
				$.ajax({
					url: 'EditOmmittedWordsList',
					data: {Type:'Add',Word:$('#word').val()},
					type: 'post',
					success: function(responseText){
						$('#owloutput').text(responseText);
		        		$('#datacollection').prop('disabled', false);
		        		$('#editowl').prop('disabled', false);
		        		$('#add').prop('disabled',true);
		        		$('#remove').prop('disabled',true);
		        		$('#cancel').prop('disabled',true);
					}
				});
		});
	</script>
	<script>
		// remove from list and give output
		$(document).on("click", "#remove", 
				function() {
					$.ajax({
						url: 'EditOmmittedWordsList',
						data: {Type:'Remove',Word:$('#word').val()},
						type: 'post',
						success: function(responseText){
							$('#owloutput').text(responseText);
							$('#datacollection').prop('disabled', false);
			        		$('#editowl').prop('disabled', false);
			        		$('#add').prop('disabled',true);
			        		$('#remove').prop('disabled',true);
			        		$('#cancel').prop('disabled',true);
						}
					});
		});
	</script>
	<script>
	// cancel edit owl
	$(document).on("click", "#cancel", 
			function(){
				$('#owloutput').text("Cancelled EditOmmittedWordsList");
				$('#datacollection').prop('disabled', false);
				$('#editowl').prop('disabled', false);
				$('#add').prop('disabled',true);
				$('#remove').prop('disabled',true);
				$('#cancel').prop('disabled',true);
	});
	</script>
	<!-- Button to edit OWL and status print -->
	<button id="editowl">Edit OmmittedWordsList</button>
	<br>
	<input type="text" id="word" name="word" />
	<button id="add" disabled>Add</button>
	<button id="remove" disabled>Remove</button>
	<button id="cancel" disabled>Cancel</button>
	<div id ="owloutput" style='overflow:auto;width:800px;height:250px' ></div>
	
	<a href="logout.jsp">Logout</a>
</body>
</html>