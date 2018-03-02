
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.io.*" %>
<%@ page import="metavideo.common.ETCLib" %><%--
  Created by IntelliJ IDEA.
  User: changgyu78
  Date: 2017-12-24
  Time: 오후 2:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%!

/*



* */
%>
<%

  String requestBody="";
  String responseBody="";
  try {
    requestBody=ETCLib.watchDog(request.getInputStream());
    System.out.println("[테스트리시버가 수신한 request Body] "+requestBody);
  }catch (IOException ioe){
    responseBody = "IOException으로 받을 수 없음.";
  }

  responseBody = "받은 내용: "+requestBody;
  ETCLib.printFileLog(responseBody);

%>
<html>
  <head>
    <title>callback tester</title>
  </head>
  <body>
================
<%=responseBody%>
================
  </body>
</html>
