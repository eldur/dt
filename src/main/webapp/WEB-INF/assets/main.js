var DT = {};

(function() {
  DT.formatter = {

    formatLabel : function(element, path) {

      path.each(function(index, item) {
        element.append(' <span class="label" >' + item + '</span> ');
      });
    },
    formatLine : function(element, path) {
      path.each(function(index, item) {
        element.append(' <li>' + item + '</li> ');
      });
    }
  };

  function asWebsocket() {

    this.onOpen = function() {
      $('.brand').css('color', "#08c");
      this.ref.initDatePicker();
    };

    this.onError = function() {
      $('.brand').css('color', "red");
    };

    this.connect = function(socketUrl, name) {
      this.init();
      this.name = name;
      this.socketUrl = socketUrl;
      var location = document.location.toString().replace('http://', 'ws://')
          .replace('https://', 'wss://');
      this.ws = new WebSocket(socketUrl);
      this.ws.onopen = this.onOpen;
      this.ws.onmessage = this.onMessage;
      this.ws.onerror = this.onError;
      this.ws.onclose = this.onClose;
      this.ws.ref = this;

    };

    this.send = function(message) {
      if (this.ws) {
        this.ws.send(message);
      }
    };

    this.onClose = function(m) {
      this.ws = null;
      $('.brand').css('color', "");
      this.ref.connect(this.ref.socketUrl, this.ref.name);
    };

    this.init = function() {

      if (!window.WebSocket) {
        alert("WebSockets are not supported by this browser");
      }
    };

  }

  var cookieKeyStart = "start";
  var cookieKeyEnd = "end";
  var expires = {
    expires : 7
  };
  var $start = $('#date-start');
  var $end = $('#date-end');

  DT.reports = {

    sendNewInterval : function(startValue, endValue) {
      this.send("{\"start\" : \"" + startValue + "\"" + "," + "\"end\" : \""
          + endValue + "\"" + "}");

    },

    initDatePicker : function() {

      var $dateNext = $('#date-next');
      var $datePrevious = $('#date-previous');

      $datePrevious.click(function() {
        DT.reports.sendNewInterval("prev", "prev");
      });

      $dateNext.click(function() {
        DT.reports.sendNewInterval("next", "next");
      });

      var startValue = $.cookie(cookieKeyStart);
      var endValue = $.cookie(cookieKeyEnd);

      $start //
      .datepicker() //
      .on('changeDate', function(ev) {
        $start.datepicker('hide');
        DT.reports.updateInterval($start, $end);
      });

      $end //
      .datepicker() //
      .on('changeDate', function(ev) {
        $end.datepicker('hide');
        DT.reports.updateInterval($start, $end);
      });

      this.updateDatePicker($start, startValue, $end, endValue);
      this.updateInterval($start, $end);

    },

    updateDatePicker : function($start, startValue, $end, endValue) {

      if (startValue) {
        $start.attr("value", startValue);
      }

      if (endValue) {
        $end.attr("value", endValue);
      }
      $start.datepicker('update');
      $end.datepicker('update');

    },
    updateCookie : function(startValue, endValue) {

      $.cookie(cookieKeyStart, startValue, expires);
      $.cookie(cookieKeyEnd, endValue, expires);
    },
    updateInterval : function($start, $end) {
      var startValue = $start.attr("value");
      var endValue = $end.attr("value");
      this.updateCookie(startValue, endValue);
      this.sendNewInterval(startValue, endValue);

    },

    onMessage : function(m) {
      if (m.data) {
        result = $.parseJSON(m.data);
        this.ref.updateDatePicker($start, result.start, $end, result.end);
        this.ref.updateCookie(result.start, result.end);
        $('.table tr.copy').each(function() {
          $(this).remove();
        });
        $('#report-title').text(result.sum);
        $('#report-period').text(
            result.start + " - " + result.end);
        $('#report-update-info').text(result.now);
        $.each(result.positions, function(i, field) {

          var main = $('#template').clone().attr('id', field.htmlid).addClass(
              "copy");
          $(".mainLabel", main).text(field.id);
          $(".mainLabel", main).addClass(field.status);
          if (field.statusName != undefined) {
            $(".mainLabel", main).after(
                ' <span class="mainLabel label ' + field.status + '">'
                    + field.statusName + '</span>');

          }
          DT.formatter.formatLabel($(".groupLabelText", main), $(field.path));
          $(".title", main).text(field.title);
          DT.formatter.formatLine($(".comment", main), $(field.comment));
          $(".mainDuration", main).text(field.duration);
          $(".mainPercentage", main).css('width',
              field.durationPercentage + '%');
          var subLabel = $(field.sub);

          main.removeClass("hidden");
          main.appendTo('.table');
          if (subLabel.length > 1) {
            $(".mainRow", main).attr("rowspan", subLabel.length + 1);
            $.each(subLabel, function(i, field) {

              var sub = $('#subTemplate').clone().attr('id', field.htmlid)
                  .addClass("copy");
              $(".subDuration", sub).text(field.duration);
              $(".subPercentage", sub).css('width',
                  field.durationPercentage + '%');
              DT.formatter.formatLabel($(".subLabel", sub), $(field.path));
              sub.removeClass("hidden");
              sub.appendTo('.table');
            });
          } else {
            $(".mainRow", main).attr("rowspan", 1);
          }

        });
        var ids = new Array;
        i = 0;
        $('.table tr.copy').each(function() {
          ids[i++] = $(this).attr('id');
        });
        this.send("{ \"ids\" : " + JSON.stringify(ids) + "}");
      }
    }

  };
  asWebsocket.apply(DT.reports);

})();
