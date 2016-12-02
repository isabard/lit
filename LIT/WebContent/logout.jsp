<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
/**
* This class handles logout requests for the user logged into the admin dashboard.
* Presents a success message and a button to return to the home page @home.jsp
* Written by Joel		
*/%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Logged Out</title>
    </head>
    <body>
        <%
        session.removeAttribute("username");
        session.removeAttribute("password");
        session.invalidate();
        %>
        <h1>Logout was done successfully.</h1>
        <a href="home.jsp">Go home</a>
        
    </body>
</html>