(function () {
  var $win = $(window),
      $nav = $('#navigation');

  var SELECTED = 'selected';

  function layout () {
    var height = $win.height() - $('#header').height();
    $nav.height(height);
    $("#reading-area").height(height);
  }

  function scroll_into_view ($me) {
    if($me.length) {
      var me = $me[0],
          rect = me.getBoundingClientRect();
      if(rect.top < 0 || rect.top > $win.height()) {
        me.scrollIntoView();
      }
    }
  }

  function select (context, id) {
    var $me = $('#' + id);
    scroll_into_view($me);
    if(!$me.hasClass(SELECTED)) {
      $("." + SELECTED, context).removeClass(SELECTED);
      $me.addClass(SELECTED);
      _.defer(function () {
        // expand navigation if collapsed
        $me.closest('li.collapse').removeClass('collapse');
      });
      return true;
    }
    return false;
  }

  $win.resize(_.debounce(layout, 100));
  layout();

  window.RM = $.extend(window.RM, {
    layout: {
      select: select,
      scroll_into_view: scroll_into_view
    }
  });
})();
