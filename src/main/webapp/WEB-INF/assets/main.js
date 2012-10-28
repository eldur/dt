
var formatter = {
	
	formatLabel : function(element, path) {
		
		path.each( function(index,item ){
			 element.append(' <span class="label" >'+item+'</span> ');
		});
	},
	formatLine : function(element, path) {
		path.each( function(index,item ){
			 element.append(' <li>'+item+'</li> ');
		});
	}
};
(function(){

function asWebsocket() {
		this.onOpen = function(){
        $('.brand').css('color', "#08c");
      };
      
      this.onError = function(){
        $('.brand').css('color', "red");
      };
  	this.connect = function(name) {
        this.name = name;
        var location = document.location.toString().replace('http://','ws://').replace('https://','wss://');
        this.ws = new WebSocket(socketUrl);
        this.ws.onopen = this.onOpen;
        this.ws.onmessage = this.onMessage;
        this.ws.onerror = this.onError;
        this.ws.onclose = this.onClose;
        this.ws.ref = this;
      };
      
      
      this.send = function(message){
        if (this.ws) {
        	this.ws.send(message);
        }
      };
      
      this.onClose = function(m) {
          this.ws = null;
          $('.brand').css('color', "");
          this.ref.connect(this.name);
        };
        
        this.init = function() {
        	
    		if (!window.WebSocket) {
    			alert("WebSockets are not supported by this browser");
    		}
    	};
      
}

reports = {
   
    onMessage: function(m) {
      if (m.data){
    	  $('.table tr.copy').each(function() {
    		  $(this).remove();
    	  } );
    	  result = $.parseJSON(m.data);
          $('#report-title').text(result.sum);
          $('#report-period').text(result.start + " - " + result.end + " " + result.now);
    	  $.each(result.positions, function(i, field) {
    		  
       		  var main = $('#template').clone().attr('id', field.htmlid).addClass("copy");
       		  $(".mainLabel", main).text(field.id);
       		  $(".mainLabel", main).addClass(field.status);
       		  if (field.statusName != undefined) {
       			  $(".mainLabel", main).after(' <span class="mainLabel label ' + field.status +'">' + field.statusName + '</span>');
       			  
       		  }
       		  formatter.formatLabel($(".groupLabelText", main), $(field.path));
       		  $(".title", main).text(field.title);
       		  formatter.formatLine($(".comment", main),$(field.comment));
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
          		formatter.formatLabel($(".subLabel", sub),$(field.path));
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
    
  };
asWebsocket.apply(reports);
		
})();
