
  var reports = {

	connect: function(name) {
      this.name = name;
      var location = document.location.toString().replace('http://','ws://').replace('https://','wss://');
      this.ws = new WebSocket(socketUrl);
      this.ws.onopen = this.onOpen;
      this.ws.onmessage = this.onMessage;
      this.ws.onerror = this.onError;
      this.ws.onclose = this.onClose;
    },
    
    onOpen: function(){
      $('#info').text('c');
    },
    
    onError: function(){
        $('#info').text('error');
      },
    
    send: function(message){
      if (this.ws)
        this.ws.send(message);
    },
    
    onMessage: function(m) {
      if (m.data){
    	  $('.table tr.copy').each(function() {
    		  $(this).remove();
    	  } );
    	  
    	  $.each($.parseJSON(m.data), function(i, field) {
       		  var main = $('#template').clone().attr('id', field.htmlid).addClass("copy");
       		  $(".mainLabel", main).text(field.id);
       		  $(".mainLabel", main).addClass(field.status);
       		  if (field.statusName != undefined) {
       			  $(".mainLabel", main).after(' <span class="mainLabel label ' + field.status +'">' + field.statusName + '</span>');
       			  
       		  }
       		  $(".groupLabelText", main).text(field.path);
       		  $(".title", main).text(field.title);
       		  $(".comment", main).text(field.comment);
       		  $(".mainDuration", main).text(field.duration);
       		  $(".mainPercentage", main).css('width', field.durationPercentage + '%');
       	  	  var subLabel = $(field.sub);
       	 
       		  main.removeClass("hidden");
       		  main.appendTo('.table');
       			if (subLabel.length > 1) {
       		      $(".mainRow", main).attr("rowspan", subLabel.length + 1);
       		      $.each(subLabel, function(i, field) {
       			
          		var sub = $('#subTemplate').clone().attr('id', field.htmlid).addClass("copy");
          		$(".subDuration", sub).text(field.duration);
          		$(".subPercentage", sub).css('width', field.durationPercentage + '%');
          		$(".subLabel", sub).text(field.path);
          		sub.removeClass("hidden");
          		sub.appendTo('.table');
       			});
       			} else {
       				$(".mainRow", main).attr("rowspan", 1);
       			}
    
    	  });
    	  var ids = new Array;
    	  i=0;
    	  $('.table tr.copy').each(function() {
    		  ids[i++] = $(this).attr('id');
    	  });
    	  this.send(ids);
      }
    },
    
    onClose: function(m) {
      this.ws = null;
      $('#info').text('off');
    }, 
    
  init: function() {
		if (!window.WebSocket) {
	        alert("WebSockets are not supported by this browser");
	}
  }
  };
		
