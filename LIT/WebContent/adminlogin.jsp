<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%
/**
* This class handles taking user input for the purpose of user logins.
* Only accepts that info. Passes it to LoginValidate.jsp for actual validation.
* Written by Joel
*/
%>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Admin Login</title>
	</head>
	<body>
		<h1>Admin Login Page</h1>
			<center>
				<form action="LoginValidate.jsp" method= "post">
				<br/>Username:<input type="text" name="username">
				<br/>Password:<input type="password" name="password">
           		<br/><input type="submit" value="Submit">									
				</form>
			</center>
	</body>
</html>