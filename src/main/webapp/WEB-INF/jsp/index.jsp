<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
<head><title>dt Report</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/> 
<link rel="apple-touch-icon" href="/assets/Apple-touch-icon.png" />
<link rel="shortcut icon" href="/assets/favicon.ico" />
<link rel="stylesheet" type="text/css" href="/assets/bootstrap/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/assets/bootstrap/css/bootstrap-responsive.css">
<link rel="stylesheet" type="text/css" href="/assets/main.css">
</head>
<body>
<div class="navbar navbar-fixed-top">
  <div class="navbar-inner">
    <div class="container">
      
<a class="brand" href="#">
  dt 
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

<br />
<h4></h4>
<div class="row">
  <div class="span10">
<c:import url="${view}.jspf" />
  </div>
  <div class="span2">...</div>
</div>


<br /><br />
<p id="info">${report.info}</p>

</div>
 <script src="/assets/bootstrap/js/bootstrap.js"></script>

    <script type='text/javascript'>
    
      if (!window.WebSocket)
        alert("WebSockets are not supported by this browser");
    
      function $() { return document.getElementById(arguments[0]); }
      function $F() { return document.getElementById(arguments[0]).value; }
      
      function getKeyCode(ev) { if (window.event) return window.event.keyCode; return ev.keyCode; } 
      
      var reports = {
    		  connect: function(name) {
          this.name=name;
          var location = document.location.toString().replace('http://','ws://').replace('https://','wss://');
          this.ws=new WebSocket("ws://${srvSocket}/ws/");
          this.ws.onopen=this.onOpen;
          this.ws.onmessage=this.onMessage;
          this.ws.onclose=this.onClose;
        },
        
        onOpen: function(){
          $('info').innerHTML='c';
        },
        
        send: function(message){
          if (this.ws)
            this.ws.send(message);
        },
        
        onMessage: function(m) {
          if (m.data){
        	  $('info').innerHTML= "Msg: "+ m.data; 
          }
        },
        
        onClose: function(m) {
          this.ws=null;
          $('info').innerHTML='off';;
        }
        
      };
      reports.connect("${reportID}");
      
    </script>
</body>
</html>