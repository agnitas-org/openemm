var applicationCommonFiles = [
  '<%= config.runtimeLibs %>/jquery/jquery-3.5.1.js',
  '<%= config.assets %>/js/vendor/jquery-ui-1.12.1.js',
  '<%= config.runtimeLibs %>/lodash/lodash-4.17.20.js',
  '<%= config.assets %>/js/vendor/jquery-i18n-1.1.1.js',
  '<%= config.assets %>/js/vendor/jquery-select2-3.5.2.js',
  '<%= config.assets %>/js/vendor/jquery-endless-scroll-1.6.0.js',
  '<%= config.assets %>/js/vendor/jquery-dirty-0.8.3.js',
  '<%= config.assets %>/js/vendor/bootstrap-dropdown-3.4.1.js',
  '<%= config.assets %>/js/vendor/bootstrap-modal-3.4.1.js',
  '<%= config.assets %>/js/vendor/bootstrap-tooltip-3.4.1.js',
  '<%= config.assets %>/js/vendor/bootstrap-popover-3.4.1.js',
  '<%= config.assets %>/js/vendor/bootstrap-colorpicker-2.5.2.js',
  '<%= config.assets %>/js/vendor/d3.js',
  '<%= config.assets %>/js/vendor/c3.js',
  '<%= config.assets %>/js/vendor/toastr.custom.js',
  '<%= config.assets %>/js/vendor/moment-2.24.0.js',
  '<%= config.assets %>/js/vendor/moment-timezone-with-data-10-year-range.js',
  '<%= config.assets %>/js/vendor/pickadate-3.5.6.js',
  '<%= config.assets %>/js/vendor/pickadate-3.5.6.date.js',
  '<%= config.assets %>/js/vendor/pickadate-3.5.6.time.js',
  '<%= config.assets %>/js/vendor/pickadate-3.5.6.legacy.js',
  '<%= config.runtimeLibs %>/ace/ace_20.12.14/ace.js',
  '<%= config.runtimeLibs %>/ace/ace_20.12.14/ext-language_tools.js',
  '<%= config.assets %>/js/vendor/jquery-perfect-scrollbar-0.5.7.js',
  '<%= config.assets %>/js/vendor/jquery-mousewheel-3.1.9.js',
  '<%= config.assets %>/js/vendor/jquery-doublescroll-0.4.js',
  '<%= config.assets %>/js/vendor/iframe-resizer-host-window-2.7.1.js',
  '<%= config.assets %>/js/vendor/jquery-inputmask-3.1.63.js',
  '<%= config.assets %>/js/vendor/jquery-iframe-transport.js',
  '<%= config.assets %>/js/vendor/dragster.js',
  '<%= config.assets %>/js/vendor/dropzone.js',
  '<%= config.assets %>/js/vendor/emojionearea.js',
  '<%= config.assets %>/js/vendor/jcrop/jquery.color.js',
  '<%= config.assets %>/js/vendor/jcrop/jquery.Jcrop.js',
  '<%= config.assets %>/js/vendor/jQuery.extendext-0.1.1.js',
  '<%= config.assets %>/js/vendor/doT.js',
  '<%= config.assets %>/js/vendor/ag-grid-no-style-1.18.0.1.js',
  '<%= config.assets %>/js/boot/*.js',
  '<%= config.assets %>/js/vendor/querybuilder/query-builder.js',
  '<%= config.assets %>/js/lib/*.js',
  '<%= config.assets %>/js/lib/workflow/*.js',
  '<%= config.assets %>/js/lib/workflow-view/definitions.js',
  '<%= config.assets %>/js/lib/workflow-view/utils.js',
  '<%= config.assets %>/js/lib/workflow-view/connection-constraints.js',
  '<%= config.assets %>/js/lib/workflow-view/node.js',
  '<%= config.assets %>/js/lib/workflow-view/vertex.js',
  '<%= config.assets %>/js/lib/workflow-view/vertex-group.js',
  '<%= config.assets %>/js/lib/workflow-view/node-title-helper.js',
  '<%= config.assets %>/js/lib/workflow-view/minimap.js',
  '<%= config.assets %>/js/lib/workflow-view/canvas.js',
  '<%= config.assets %>/js/lib/workflow-view/dialogs.js',
  '<%= config.assets %>/js/lib/workflow-view/snippets.js',
  '<%= config.assets %>/js/lib/workflow-view/draggable-buttons.js',
  '<%= config.assets %>/js/lib/workflow-view/auto-alignment.js',
  '<%= config.assets %>/js/lib/workflow-view/undo-manager.js',
  '<%= config.assets %>/js/lib/workflow-view/editors-helper.js',
  '<%= config.assets %>/js/lib/workflow-view/mailing-editor-helper.js',
  '<%= config.assets %>/js/lib/workflow-view/mailing-selector.js',
  '<%= config.assets %>/js/lib/workflow-view/editor.js',
  '<%= config.assets %>/js/lib/table/*.js',
  '<%= config.assets %>/js/lib/formbuilder/utils.js',
  '<%= config.assets %>/js/lib/formbuilder/emm-controls.js',
  '<%= config.assets %>/js/lib/formbuilder/templates.js',
  '<%= config.assets %>/js/lib/formbuilder/formbuilder.js',
  '<%= config.assets %>/js/modules/*.js',
  '<%= config.assets %>/js/initializers/*.js',
  '<%= config.assets %>/js/listener/*.js',
  '<%= config.assets %>/js/validators/*.js',
  '<%= config.assets %>/js/controllers/*.js',
  '<%= config.assets %>/js/vendor/interact.js'
],
applicationDevFiles = applicationCommonFiles.concat(['<%= config.assets %>/js/vendor/jquery-migrate-3.3.2.js']),
applicationMinFiles = applicationCommonFiles.concat(['<%= config.assets %>/js/vendor/jquery-migrate-3.3.2.min.js']),//migrate min file does not have logs about deprecated functions
birtFiles = [
  '<%= config.assets %>/js/vendor/iframe-resizer-content-window-2.7.1.js',
  '<%= config.assets %>/js/birt/*.js'
];

module.exports = {
  build_js: {
    files: {
      '<%= config.assets %>/application.js': applicationDevFiles
    },
    options: {
      mangle: false,
      compress: false,
      beautify: true,
      sourcemap: true
    }
  },
  build_birtjs: {
    files: {
      '<%= config.assets %>/birt.js': birtFiles
    },
    options: {
      mangle: false,
      compress: false,
      beautify: true,
      sourcemap: true
    }
  },
  compile_js: {
    files: {
      '<%= config.assets %>/application.min.js': applicationMinFiles
    },
    options: {
      mangle: true,
      compress: {},
      beautify: false,
      sourcemap: false
    }
  },
  compile_birtjs: {
    files: {
      '<%= config.assets %>/birt.min.js': birtFiles
    },
    options: {
      mangle: true,
      compress: {},
      beautify: false,
      sourcemap: false
    }
  }
};
