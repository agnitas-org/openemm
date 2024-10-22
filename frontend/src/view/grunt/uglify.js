const dashboardFiles = [
  'definitions_redesigned',
  'dashboard-grid_redesigned',
  'dashboard-tile-swapper_redesigned',
  'tiles/initializer/xl-calendar-label/abstract/xl-calendar-label_redesigned',
  'tiles/initializer/xl-calendar-label/xl-calendar-comment-label_redesigned',
  'tiles/initializer/xl-calendar-label/xl-calendar-mailing-label_redesigned',
  'tiles/initializer/xl-calendar-label/xl-calendar-auto-opt-label_redesigned',
  'tiles/initializer/xl-calendar-label/xl-calendar-push-label_redesigned',
  'tiles/initializer/abstract/calendar-base_redesigned',
  'tiles/initializer/calendar_redesigned',
  'tiles/initializer/xl-calendar_redesigned',
  'tiles/validator/calendar-comment-validator',
  'tiles/abstract/draggable-tile',
  'tiles/abstract/base-mailing-statistics-tile',
  'tiles/abstract/base-mailing-device-statistics-tile',
  'tiles/empty-tile',
  'tiles/add-ons-tile',
  'tiles/analysis-tile',
  'tiles/calendar-tile',
  'tiles/xl-calendar-tile',
  'tiles/week-calendar-tile',
  'tiles/imports-exports-tile',
  'tiles/statistics-tile',
  'tiles/clickers-tile',
  'tiles/openers-tile',
  'tiles/mailings-tile',
  'tiles/news-tile',
  'tiles/planning-tile',
  'tiles/workflows-tile',
  'dashboard_redesigned',
].map(file => `<%= assets %>/js/controllers/dashboard/${file}.js`);

const campaignFiles = [
  'comment-editor',
  'field-rules-table_redesigned',
  'node-editor/abstract/node-editor',
  'node-editor/abstract/recipient-transfer-editor',
  'node-editor/start-node-editor',
  'node-editor/parameter-node-editor',
  'node-editor/import-node-editor',
  'node-editor/export-node-editor',
  'node-editor/archive-node-editor',
  'node-editor/mailing-node-editor',
  'node-editor/datebased-mailing-node-editor',
  'node-editor/actionbased-mailing-node-editor',
  'node-editor/followup-mailing-node-editor',
  'node-editor/decision-node-editor',
  'node-editor/sms-mailing-node-editor',
  'node-editor/post-mailing-node-editor',
  'node-editor/deadline-node-editor',
  'node-editor/recipient-node-editor',
].map(file => `<%= assets %>/js/lib/workflow-view/${file}.js`);

const lbFiles = [
  'definitions',
  'categories',
  'promise-progress-watcher',
  'targets',
  'adjustments',
  'resizing-direction',
  'resizing',
  'agn-tags-resolver',
  'undo-manager',
  'ui-utils',
  'child',
  'child-sample',
  'children',
  'child-editor',
  'controls-factory',
  'controls',
  'capsule',
  'droppable',
  'row',
  'grid',
  'rss-importer',
  'editor'
].map(file => `<%= assets %>/js/lib/layout-builder/${file}_redesigned.js`);

const fieldFiles = [
  'field',
  'date-time-field',
  'double-select-field',
  'password-field',
  'required-field',
  'toggle-vis-field',
  'validator-field',
  'value-split-field',
].map(file => `<%= assets %>/js/lib/field/${file}.js`);


var applicationCommonFiles = [
  '<%= runtimeLibs %>/jquery/jquery-3.5.1.js',
  '<%= assets %>/js/vendor/jquery-ui-1.13.1.js',
  '<%= runtimeLibs %>/lodash/lodash-4.17.21.js',
  '<%= assets %>/js/vendor/jquery-i18n-1.1.1.js',
  '<%= assets %>/js/vendor/jquery-select2-3.5.2.js',
  '<%= assets %>/js/vendor/jquery-endless-scroll-1.6.0.js',
  '<%= assets %>/js/vendor/jquery-dirty-0.8.3.js',
  '<%= assets %>/js/vendor/bootstrap-dropdown-3.4.1.js',
  '<%= assets %>/js/vendor/bootstrap-modal-3.4.1.js',
  '<%= assets %>/js/vendor/bootstrap-tooltip-3.4.1.js',
  '<%= assets %>/js/vendor/bootstrap-popover-3.4.1.js',
  '<%= assets %>/js/vendor/bootstrap-colorpicker-2.5.2.js',
  '<%= assets %>/js/vendor/d3.js',
  '<%= assets %>/js/vendor/c3.js',
  '<%= assets %>/js/vendor/toastr.custom.js',
  '<%= assets %>/js/vendor/moment-2.30.1.js',
  '<%= assets %>/js/vendor/moment-timezone-with-data-10-year-range.js',
  '<%= assets %>/js/vendor/pickadate-3.5.6.js',
  '<%= assets %>/js/vendor/pickadate-3.5.6.date.js',
  '<%= assets %>/js/vendor/pickadate-3.5.6.time.js',
  '<%= assets %>/js/vendor/pickadate-3.5.6.legacy.js',
  '<%= assets %>/js/vendor/jquery-perfect-scrollbar-0.5.7.js',
  '<%= assets %>/js/vendor/jquery-mousewheel-3.1.9.js',
  '<%= assets %>/js/vendor/jquery-doublescroll-0.4.js',
  '<%= assets %>/js/vendor/iframe-resizer-host-window-4.3.2.js',
  '<%= assets %>/js/vendor/jquery-inputmask-3.1.63.js',
  '<%= assets %>/js/vendor/jquery-iframe-transport.js',
  '<%= assets %>/js/vendor/dragster.js',
  '<%= assets %>/js/vendor/dropzone.js',
  '<%= assets %>/js/vendor/emojionearea.js',
  '<%= assets %>/js/vendor/jcrop/jquery.color.js',
  '<%= assets %>/js/vendor/jcrop/jquery.Jcrop.js',
  '<%= assets %>/js/vendor/jQuery.extendext-0.1.1.js',
  '<%= assets %>/js/vendor/doT.js',
  '<%= assets %>/js/vendor/ag-grid-no-style-1.18.0.1.js',
  '<%= assets %>/js/boot/*.js',
  '<%= assets %>/js/vendor/querybuilder/query-builder.js',
  '<%= assets %>/js/lib/*.js',
  '<%= assets %>/js/lib/workflow/*.js',
  '<%= assets %>/js/lib/workflow-view/definitions.js',
  '<%= assets %>/js/lib/workflow-view/utils.js',
  '<%= assets %>/js/lib/workflow-view/connection-constraints.js',
  '<%= assets %>/js/lib/workflow-view/node.js',
  '<%= assets %>/js/lib/workflow-view/vertex.js',
  '<%= assets %>/js/lib/workflow-view/vertex-group.js',
  '<%= assets %>/js/lib/workflow-view/node-title-helper.js',
  '<%= assets %>/js/lib/workflow-view/minimap.js',
  '<%= assets %>/js/lib/workflow-view/canvas.js',
  '<%= assets %>/js/lib/workflow-view/dialogs.js',
  '<%= assets %>/js/lib/workflow-view/snippets.js',
  '<%= assets %>/js/lib/workflow-view/draggable-buttons.js',
  '<%= assets %>/js/lib/workflow-view/auto-alignment.js',
  '<%= assets %>/js/lib/workflow-view/undo-manager.js',
  '<%= assets %>/js/lib/workflow-view/editors-helper.js',
  '<%= assets %>/js/lib/workflow-view/mailing-editor-helper.js',
  '<%= assets %>/js/lib/workflow-view/mailing-selector.js',
  '<%= assets %>/js/lib/workflow-view/editor.js',
  '<%= assets %>/js/lib/table/*.js',
  '<%= assets %>/js/lib/formbuilder/utils.js',
  '<%= assets %>/js/lib/formbuilder/emm-controls.js',
  '<%= assets %>/js/lib/formbuilder/templates.js',
  '<%= assets %>/js/lib/formbuilder/formbuilder.js',
  '<%= assets %>/js/modules/*.js',
  '<%= assets %>/js/initializers/*.js',
  '<%= assets %>/js/listener/*.js',
  '<%= assets %>/js/validators/*.js',
  '<%= assets %>/js/controllers/*.js',
  '<%= assets %>/js/controllers/dashboard/*.js',
  '<%= assets %>/js/vendor/interact.js'
],
applicationCommonFiles_redesigned = [
  '<%= runtimeLibs %>/jquery/jquery-3.5.1.js',
  '<%= assets %>/js/vendor/jquery-ui-1.13.1.js',
  '<%= runtimeLibs %>/lodash/lodash-4.17.21.js',
  '<%= assets %>/js/vendor/jquery-i18n-1.1.1.js',
  '<%= node_modules %>/spectrum-colorpicker/spectrum.js',
  '<%= node_modules %>/select2/dist/js/select2.js',
  '<%= runtimeLibs %>/select2-to-tree/select2totree.js',
  '<%= assets %>/js/vendor/jquery-endless-scroll-1.6.0.js',
  '<%= assets %>/js/vendor/jquery-dirty-0.8.3.js',
  '<%= node_modules %>/bootstrap/dist/js/bootstrap.bundle.min.js',
  '<%= node_modules %>/hammerjs/hammer.min.js',
  '<%= node_modules %>/chart.js/dist/chart.umd.js',
  '<%= node_modules %>/chartjs-adapter-date-fns/dist/chartjs-adapter-date-fns.bundle.js',
  '<%= node_modules %>/chartjs-plugin-zoom/dist/chartjs-plugin-zoom.js',
  '<%= node_modules %>/chartjs-plugin-datalabels/dist/chartjs-plugin-datalabels.js',
  '<%= assets %>/js/vendor/toastr.custom_redesigned.js',
  '<%= assets %>/js/vendor/moment-2.30.1.js',
  '<%= assets %>/js/vendor/moment-timezone-with-data-10-year-range.js',
  '<%= assets %>/js/vendor/perfect-scrollbar-1.5.5_redesigned.js',
  '<%= assets %>/js/vendor/jquery-mousewheel-3.1.9.js',
  '<%= assets %>/js/vendor/iframe-resizer-host-window-4.3.2.js',
  '<%= assets %>/js/vendor/jquery-inputmask-3.1.63.js',
  '<%= assets %>/js/vendor/jquery-iframe-transport.js',
  '<%= assets %>/js/vendor/dragster.js',
  '<%= assets %>/js/vendor/dropzone.js',
  '<%= assets %>/js/vendor/jcrop/jquery.color.js',
  '<%= assets %>/js/vendor/jcrop/jquery.Jcrop.js',
  '<%= assets %>/js/vendor/jQuery.extendext-0.1.1.js',
  '<%= assets %>/js/vendor/doT.js',
  '<%= node_modules %>/ag-grid-community/dist/ag-grid-community.min.noStyle.js',
  '<%= assets %>/js/boot/*.js',
  '<%= assets %>/js/vendor/querybuilder/query-builder.js',
  ...fieldFiles,
  '<%= assets %>/js/lib/*.js',
  ...lbFiles,
  '<%= assets %>/js/lib/workflow/*.js',
  '<%= assets %>/js/lib/workflow-view/definitions_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/utils_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/connection-constraints.js',
  '<%= assets %>/js/lib/workflow-view/node_popover_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/node_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/vertex.js',
  '<%= assets %>/js/lib/workflow-view/vertex-group.js',
  '<%= assets %>/js/lib/workflow-view/node-title-helper_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/minimap_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/canvas_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/dialogs_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/snippets_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/draggable-buttons_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/auto-alignment.js',
  '<%= assets %>/js/lib/workflow-view/undo-manager_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/editors-helper_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/mailing-editor-helper_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/mailing-selector_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/grid-background_redesigned.js',
  '<%= assets %>/js/lib/workflow-view/editor_redesigned.js',
  '<%= assets %>/js/lib/table/*.js',
  '<%= assets %>/js/lib/formbuilder/utils.js',
  '<%= assets %>/js/lib/formbuilder/emm-controls.js',
  '<%= assets %>/js/lib/formbuilder/templates_redesigned.js',
  '<%= assets %>/js/lib/formbuilder/formbuilder.js',
  '<%= assets %>/js/modules/*.js',
  '<%= assets %>/js/initializers/*.js',
  '<%= assets %>/js/listener/*.js',
  '<%= assets %>/js/validators/*.js',
  ...dashboardFiles,
  ...campaignFiles,
  '<%= assets %>/js/controllers/*.js',
  '<%= assets %>/js/vendor/interact.js'
],
applicationDevFiles = applicationCommonFiles.concat(['<%= assets %>/js/vendor/jquery-migrate-3.3.2.js']),
applicationMinFiles = applicationCommonFiles.concat(['<%= assets %>/js/vendor/jquery-migrate-3.3.2.min.js']),//migrate min file does not have logs about deprecated functions
applicationDevFiles_redesigned = applicationCommonFiles_redesigned.concat(['<%= assets %>/js/vendor/jquery-migrate-3.3.2.js']),
applicationMinFiles_redesigned = applicationCommonFiles_redesigned.concat(['<%= assets %>/js/vendor/jquery-migrate-3.3.2.min.js']),//migrate min file does not have logs about deprecated functions
birtFiles = [
  '<%= config.assets %>/js/vendor/iframe-resizer-content-window-4.3.2.js',
  '<%= config.assets %>/js/birt/*.js'
];

module.exports = function(grunt) {
  grunt.loadNpmTasks('grunt-template');
  grunt.config.init(grunt.file.readJSON('grunt_config.json'))

  /**
   * filtering required files for each version (redesign and standard)
   1) redesigned: stores all files that do not have a redesigned version, as well as the files themselves for the redesigned version.
   2) standard: stores all files except files for the redesigned version
   */
  function findFilesForOriginalVersion(filesArray) {
    const files = grunt.file.expand(filesArray.map((path) => grunt.template.process(path)));
    const redesignedFiles = files.filter((file) => file.match(/_redesigned\.js$/));

    return files.filter((file) => !redesignedFiles.includes(file));
  }

  function findFilesForRedesignedVersion(filesArray) {
    const files = grunt.file.expand(filesArray.map((path) => grunt.template.process(path)));

    const redesignedFiles = files.filter((file) => file.match(/_redesigned\.js$/));
    const baseNamesOfRedesignedFiles = redesignedFiles.map((file) => file.replace(/_redesigned\.js$/, ''));

    return  files.filter((file) => {
      const basename = file.replace(/\.js$/, '');
      return redesignedFiles.includes(file) || !baseNamesOfRedesignedFiles.includes(basename)
    });
  }

  return {
    build_js: {
      files: {
        '<%= config.assets %>/application.js': findFilesForOriginalVersion(applicationDevFiles)
      },
      options: {
        mangle: false,
        compress: false,
        beautify: true,
        sourcemap: true
      }
    },
    build_js_redesigned: {
      files: {
        '<%= config.assets %>/application.redesigned.js': findFilesForRedesignedVersion(applicationDevFiles_redesigned)
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
        '<%= config.assets %>/application.min.js': findFilesForOriginalVersion(applicationMinFiles)
      },
      options: {
        mangle: true,
        compress: false,
        beautify: false,
        sourcemap: false
      }
    },
    compile_js_redesigned: {
      files: {
        '<%= config.assets %>/application.redesigned.min.js': findFilesForRedesignedVersion(applicationMinFiles_redesigned)
      },
      options: {
        mangle: true,
        compress: false,
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
  }
};
