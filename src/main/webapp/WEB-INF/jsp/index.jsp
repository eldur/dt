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
<style type="text/css">
      body {
        padding-top: 60px;
        padding-bottom: 40px;
      }
    </style>
<link rel="stylesheet" type="text/css" href="/assets/bootstrap/css/bootstrap-responsive.css">
<link rel="stylesheet" type="text/css" href="/assets/main.css">

</head>
<body>
<div class="navbar navbar-inverse navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container-fluid">
      
			<a class="brand" href="#">
			  Δt Reporting
			</a>
    </div>
  </div>
</div>


<div class="container-fluid">
	<div class="row-fluid">
		<div class="span12">
<c:import url="${view}.jspf" />
	  </div>
	</div>
</div>

</body>
</html>