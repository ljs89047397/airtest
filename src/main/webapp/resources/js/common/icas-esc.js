(function(global) {
  global.IcasEsc = {
    esc: function(s) {
      if (s == null) return '';
      return String(s)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    },
    escAttr: function(s) { return IcasEsc.esc(s); }
  };
})(window);
