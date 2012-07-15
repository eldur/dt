<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html>
<head><title>Δt Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/> 
<link rel="apple-touch-icon" href="/assets/Apple-touch-icon.png" />
<link rel="shortcut icon" href="/assets/favicon.ico" />
<link rel="stylesheet" type="text/css" href="/assets/bootstrap/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/assets/bootstrap/css/bootstrap-responsive.css">
<link rel="stylesheet" type="text/css" href="/assets/main.css">
<script src="/assets/jquery-1.7.2.min.js"></script>

</head>
<body>
<div class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      
<a class="brand" href="#">
  Δt 
  <h5>Reporting</h5>
</a>

<ul class="nav">

<c:forEach var="position" items="${allReports}">
  <li <c:if test="${position == reportID}" > class="active" </c:if>><a href="/?r=${position}&m=${param.m}" >${position}</a></li> 
</c:forEach>

</ul>
<ul class="nav">
   <li><a href="/?r=${reportID}&t=${report.previous}" >prev</a></li>
   <li><a href="/?r=${reportID}&t=${report.next}" >next</a></li> 
</ul>
    </div>
  </div>
</div>
<div class="container" style="margin-top: 6em;">
<p id="info">${report.info}</p>
<br />
<h4></h4>
<div class="row">
  <div class="span12">
<c:import url="${view}.jspf" />
  </div>
</div>


</div>
</body>
</html>