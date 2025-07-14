(() => {

  if (!window.Jodit) {
    return;
  }

  const options = {
    width: '100%',
    height: '100%',
    showWordsCounter: false,
    showCharsCounter: false,
    showPlaceholder: false,
    askBeforePasteHTML: false,
    toolbarAdaptive: false,
    addNewLineOnDBLClick: false,
    hidePoweredByJodit: true,
    extraPlugins: ['emm', 'emoji'],
    colors: {
      full: ['#000000', '#993300', '#333300', '#003300', '#003366', '#000080', '#333399', '#333333', '#800000', '#FF6600', '#808000', '#808080', '#008080', '#0000FF', '#666699', '#808080', '#FF0000', '#FF9900', '#99CC00', '#339966', '#33CCCC', '#3366FF', '#800080', '#999999', '#FF00FF', '#FFCC00', '#FFFF00', '#00FF00', '#00FFFF', '#00CCFF', '#993366', '#C0C0C0', '#FF99CC', '#FFCC99', '#FFFF99', '#CCFFCC', '#CCFFFF', '#99CCFF', '#CC99FF', '#FFFFFF']
    },
    colorPickerDefaultTab: 'color',
    wrapNodes: {
      emptyBlockAfterInit: false
    },
    enter: 'br',
    enterBlock: 'div',
    iframeDefaultSrc: '<html><head><title></title></head><body></body></html>',
    iframeStyle: '',
    iframeTitle: '',
    controls: {
      font: {
        list: {
          'Comic Sans MS,cursive': 'Comic Sans MS'
        }
      }
    }
  };

  _.merge(Jodit.defaultOptions, options);

  // load extra plugins located in separate files
  Jodit.defaultOptions.extraPlugins.forEach(pluginName => {
    const script = document.createElement('script');
    script.src = Jodit.BASE_PATH + `plugins/${pluginName}-plugin.js`;
    document.head.appendChild(script);
  });

  // sorting list of fonts
  Jodit.defaultOptions.controls.font.list = Object.fromEntries(Object.entries(Jodit.defaultOptions.controls.font.list)
    .sort(([, valueA], [, valueB]) => valueA.localeCompare(valueB)));

})();