const gulp = require('gulp');
const config = require('./config');
const concat = require('gulp-concat');
const terser = require('gulp-terser');

const path = require('path');
const {sync} = require('glob');

const dashboardFiles = [
  'definitions_redesigned',
  'dashboard-grid_redesigned',
  'dashboard-tile-swapper_redesigned',
  'tiles/initializer/xl-calendar-label/abstract/xl-calendar-label_redesigned',
  'tiles/initializer/xl-calendar-label/xl-calendar-comment-label_redesigned',
  'tiles/initializer/xl-calendar-label/xl-calendar-mailing-label_redesigned',
  'tiles/initializer/xl-calendar-label/xl-calendar-auto-opt-label_redesigned',
  'tiles/initializer/xl-calendar-label/xl-calendar-push-label_redesigned',
  'tiles/initializer/dashboard-calendar-label/abstract/dashboard-calendar-label_redesigned',
  'tiles/initializer/dashboard-calendar-label/dashboard-calendar-comment-label_redesigned',
  'tiles/initializer/dashboard-calendar-label/dashboard-calendar-mailing-label_redesigned',
  'tiles/initializer/dashboard-calendar-label/dashboard-calendar-auto-opt-label_redesigned',
  'tiles/initializer/dashboard-calendar-label/dashboard-calendar-push-label_redesigned',
  'tiles/initializer/abstract/calendar-base-ux-update-rollback_redesigned',
  'tiles/initializer/abstract/calendar-base_redesigned',
  'tiles/initializer/calendar_redesigned',
  'tiles/initializer/calendar-light_redesigned',
  'tiles/initializer/xl-calendar_redesigned',
  'tiles/initializer/dashboard-calendar-period-picker_redesigned',
  'tiles/initializer/dashboard-calendar-comments-manager_redesigned',
  'tiles/initializer/dashboard-calendar_redesigned',
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
  'dashboard-mailing-popover-ux-update-rollback_redesigned',
  'dashboard-mailing-popover_redesigned',
  'dashboard-grid-controller_redesigned',
  'dashboard-calendar-controller_redesigned',
  'dashboard-news_redesigned',
].map(file => `${config.assets}/js/controllers/dashboard/${file}.js`);

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
  'node-editor/split-node-editor',
  'node-editor/mailing-node-editor',
  'node-editor/datebased-mailing-node-editor',
  'node-editor/actionbased-mailing-node-editor',
  'node-editor/followup-mailing-node-editor',
  'node-editor/decision-node-editor',
  'node-editor/sms-mailing-node-editor',
  'node-editor/post-mailing-node-editor',
  'node-editor/deadline-node-editor',
  'node-editor/recipient-node-editor',
].map(file => `${config.assets}/js/lib/workflow-view/${file}.js`);

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
].map(file => `${config.assets}/js/lib/layout-builder/${file}_redesigned.js`);

const fieldFiles = [
  'field',
  'date-time-field',
  'double-select-field',
  'password-field',
  'required-field',
  'toggle-vis-field',
  'validator-field',
  'value-split-field',
].map(file => `${config.assets}/js/lib/field/${file}.js`);

const applicationCommonFiles = [
    `${config.runtimeLibs}/jquery/jquery-3.5.1.js`,
    `${config.assets}/js/vendor/jquery-ui-1.14.1.js`,
    `${config.runtimeLibs}/lodash/lodash-4.17.21.js`,
    `${config.assets}/js/vendor/jquery-i18n-1.1.1.js`,
    `${config.assets}/js/vendor/jquery-select2-3.5.2.js`,
    `${config.assets}/js/vendor/jquery-endless-scroll-1.6.0.js`,
    `${config.assets}/js/vendor/jquery-dirty-0.8.3.js`,
    `${config.assets}/js/vendor/bootstrap-dropdown-3.4.1.js`,
    `${config.assets}/js/vendor/bootstrap-modal-3.4.1.js`,
    `${config.assets}/js/vendor/bootstrap-tooltip-3.4.1.js`,
    `${config.assets}/js/vendor/bootstrap-popover-3.4.1.js`,
    `${config.assets}/js/vendor/bootstrap-colorpicker-2.5.2.js`,
    `${config.assets}/js/vendor/d3.js`,
    `${config.assets}/js/vendor/c3.js`,
    `${config.assets}/js/vendor/toastr.custom.js`,
    `${config.assets}/js/vendor/moment-2.30.1.js`,
    `${config.assets}/js/vendor/moment-timezone-with-data-10-year-range.js`,
    `${config.assets}/js/vendor/pickadate-3.5.6.js`,
    `${config.assets}/js/vendor/pickadate-3.5.6.date.js`,
    `${config.assets}/js/vendor/pickadate-3.5.6.time.js`,
    `${config.assets}/js/vendor/pickadate-3.5.6.legacy.js`,
    `${config.assets}/js/vendor/jquery-perfect-scrollbar-0.5.7.js`,
    `${config.assets}/js/vendor/jquery-mousewheel-3.1.9.js`,
    `${config.assets}/js/vendor/jquery-doublescroll-0.4.js`,
    `${config.assets}/js/vendor/iframe-resizer-host-window-4.3.2.js`,
    `${config.assets}/js/vendor/jquery-inputmask-3.1.63.js`,
    `${config.assets}/js/vendor/jquery-iframe-transport.js`,
    `${config.assets}/js/vendor/dragster.js`,
    `${config.assets}/js/vendor/dropzone.js`,
    `${config.assets}/js/vendor/emojionearea.js`,
    `${config.assets}/js/vendor/jcrop/jquery.color.js`,
    `${config.assets}/js/vendor/jcrop/jquery.Jcrop.js`,
    `${config.assets}/js/vendor/jQuery.extendext-0.1.1.js`,
    `${config.assets}/js/vendor/doT.js`,
    `${config.assets}/js/vendor/ag-grid-no-style-1.18.0.1.js`,
    `${config.assets}/js/boot/*.js`,
    `${config.assets}/js/vendor/querybuilder/query-builder.js`,
    `${config.assets}/js/lib/*.js`,
    `${config.assets}/js/lib/workflow-view/definitions.js`,
    `${config.assets}/js/lib/workflow-view/utils.js`,
    `${config.assets}/js/lib/workflow-view/connection-constraints.js`,
    `${config.assets}/js/lib/workflow-view/node.js`,
    `${config.assets}/js/lib/workflow-view/vertex.js`,
    `${config.assets}/js/lib/workflow-view/vertex-group.js`,
    `${config.assets}/js/lib/workflow-view/node-title-helper.js`,
    `${config.assets}/js/lib/workflow-view/minimap.js`,
    `${config.assets}/js/lib/workflow-view/canvas.js`,
    `${config.assets}/js/lib/workflow-view/dialogs.js`,
    `${config.assets}/js/lib/workflow-view/snippets.js`,
    `${config.assets}/js/lib/workflow-view/draggable-buttons.js`,
    `${config.assets}/js/lib/workflow-view/auto-alignment.js`,
    `${config.assets}/js/lib/workflow-view/undo-manager.js`,
    `${config.assets}/js/lib/workflow-view/editors-helper.js`,
    `${config.assets}/js/lib/workflow-view/mailing-editor-helper.js`,
    `${config.assets}/js/lib/workflow-view/mailing-selector.js`,
    `${config.assets}/js/lib/workflow-view/editor.js`,
    `${config.assets}/js/lib/table/*.js`,
    `${config.assets}/js/lib/formbuilder/utils.js`,
    `${config.assets}/js/lib/formbuilder/emm-controls.js`,
    `${config.assets}/js/lib/formbuilder/templates.js`,
    `${config.assets}/js/lib/formbuilder/formbuilder.js`,
    `${config.assets}/js/initializers/*.js`,
    `${config.assets}/js/listener/*.js`,
    `${config.assets}/js/validators/*.js`,
    `${config.assets}/js/controllers/*.js`,
    `${config.assets}/js/controllers/dashboard/*.js`,
    `${config.assets}/js/vendor/interact.js`
  ],
  applicationCommonFiles_redesigned = [
    `${config.runtimeLibs}/jquery/jquery-3.5.1.js`,
    `${config.assets}/js/vendor/jquery-ui-1.14.1.js`,
    `${config.runtimeLibs}/lodash/lodash-4.17.21.js`,
    `${config.assets}/js/vendor/jquery-i18n-1.1.1.js`,
    `${config.node_modules}/spectrum-colorpicker/spectrum.js`,
    `${config.node_modules}/select2/dist/js/select2.js`,
    `${config.runtimeLibs}/select2-to-tree/select2totree.js`,
    `${config.assets}/js/vendor/jquery-endless-scroll-1.6.0.js`,
    `${config.assets}/js/vendor/jquery-dirty-0.8.3.js`,
    `${config.node_modules}/bootstrap/dist/js/bootstrap.bundle.js`,
    `${config.node_modules}/hammerjs/hammer.js`,
    `${config.node_modules}/chart.js/dist/chart.umd.js`,
    `${config.node_modules}/chartjs-adapter-date-fns/dist/chartjs-adapter-date-fns.bundle.js`,
    `${config.node_modules}/chartjs-plugin-zoom/dist/chartjs-plugin-zoom.js`,
    `${config.node_modules}/chartjs-plugin-datalabels/dist/chartjs-plugin-datalabels.js`,
    `${config.assets}/js/vendor/toastr.custom_redesigned.js`,
    `${config.assets}/js/vendor/moment-2.30.1.js`,
    `${config.assets}/js/vendor/moment-timezone-with-data-10-year-range.js`,
    `${config.assets}/js/vendor/perfect-scrollbar-1.5.5_redesigned.js`,
    `${config.assets}/js/vendor/jquery-mousewheel-3.1.9.js`,
    `${config.assets}/js/vendor/iframe-resizer-host-window-4.3.2.js`,
    `${config.assets}/js/vendor/jquery-inputmask-3.1.63.js`,
    `${config.assets}/js/vendor/jquery-iframe-transport.js`,
    `${config.assets}/js/vendor/dragster.js`,
    `${config.assets}/js/vendor/dropzone.js`,
    `${config.assets}/js/vendor/jcrop/jquery.color.js`,
    `${config.assets}/js/vendor/jcrop/jquery.Jcrop.js`,
    `${config.assets}/js/vendor/jQuery.extendext-0.1.1.js`,
    `${config.assets}/js/vendor/doT.js`,
    `${config.node_modules}/ag-grid-community/dist/ag-grid-community.min.noStyle.js`,
    `${config.assets}/js/boot/*.js`,
    `${config.assets}/js/vendor/querybuilder/query-builder.js`,
    ...fieldFiles,
    `${config.assets}/js/lib/*.js`,
    ...lbFiles,
    `${config.assets}/js/lib/workflow-view/definitions_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/utils_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/connection-constraints.js`,
    `${config.assets}/js/lib/workflow-view/node_popover_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/node_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/vertex.js`,
    `${config.assets}/js/lib/workflow-view/vertex-group.js`,
    `${config.assets}/js/lib/workflow-view/node-title-helper_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/minimap_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/canvas_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/dialogs_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/snippets_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/draggable-buttons_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/auto-alignment.js`,
    `${config.assets}/js/lib/workflow-view/undo-manager_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/editors-helper_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/mailing-editor-helper_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/mailing-selector_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/grid-background_redesigned.js`,
    `${config.assets}/js/lib/workflow-view/editor_redesigned.js`,
    `${config.assets}/js/lib/table/*.js`,
    `${config.assets}/js/lib/formbuilder/utils.js`,
    `${config.assets}/js/lib/formbuilder/emm-controls.js`,
    `${config.assets}/js/lib/formbuilder/templates_redesigned.js`,
    `${config.assets}/js/lib/formbuilder/formbuilder.js`,
    `${config.assets}/js/initializers/*.js`,
    `${config.assets}/js/listener/*.js`,
    `${config.assets}/js/validators/*.js`,
    ...dashboardFiles,
    ...campaignFiles,
    `${config.assets}/js/controllers/*.js`,
    `${config.assets}/js/vendor/interact.js`
  ],
  applicationDevFiles = applicationCommonFiles.concat([`${config.assets}/js/vendor/jquery-migrate-3.3.2.js`]),
  applicationMinFiles = applicationCommonFiles.concat([`${config.assets}/js/vendor/jquery-migrate-3.3.2.min.js`]),//migrate min file does not have logs about deprecated functions
  applicationDevFiles_redesigned = applicationCommonFiles_redesigned.concat([`${config.assets}/js/vendor/jquery-migrate-3.3.2.js`]),
  applicationMinFiles_redesigned = applicationCommonFiles_redesigned.concat([`${config.assets}/js/vendor/jquery-migrate-3.3.2.min.js`]),//migrate min file does not have logs about deprecated functions
  birtFiles = [
    `${config.assets}/js/vendor/iframe-resizer-content-window-4.3.2.js`,
    `${config.assets}/js/birt/*.js`
  ];

/**
 * filtering required files for each version (redesign and standard)
 1) redesigned: stores all files that do not have a redesigned version, as well as the files themselves for the redesigned version.
 2) standard: stores all files except files for the redesigned version
 */
function findFilesForOriginalVersion(filesArray) {
  // Expand paths
  const files = filesArray.flatMap(filePath => {
    return sync(path.resolve(filePath)).reverse();
  });

  // Filter redesigned files
  const redesignedFiles = files.filter(file => file.endsWith('_redesigned.js'));

  // Return original files (excluding redesigned ones)
  return files.filter(file => !redesignedFiles.includes(file));
}

function findFilesForRedesignedVersion(filesArray) {
  const files = filesArray.flatMap(filePath => {
    return sync(path.resolve(filePath)).reverse();
  });

  const redesignedFiles = files.filter((file) => file.match(/_redesigned\.js$/));
  const baseNamesOfRedesignedFiles = redesignedFiles.map((file) => file.replace(/_redesigned\.js$/, ''));

  return files.filter((file) => {
    const basename = file.replace(/\.js$/, '');
    return redesignedFiles.includes(file) || !baseNamesOfRedesignedFiles.includes(basename)
  });
}

module.exports = {
  compileJs: () => {
    return gulp.src(findFilesForOriginalVersion(applicationMinFiles))
      .pipe(concat('application.min.js'))
      .pipe(terser({
        mangle: true,
        compress: false,
        format: {
          beautify: false,
          comments: false
        }
      }))
      .pipe(gulp.dest(config.assets));
  },
  compileJsRedesigned: () => {
    return gulp.src(findFilesForRedesignedVersion(applicationMinFiles_redesigned))
      .pipe(concat('application.redesigned.min.js'))
      .pipe(terser({
        mangle: true,
        compress: false,
        format: {
          beautify: false,
          comments: false
        }
      }))
      .pipe(gulp.dest(config.assets));
  },
  compileBirtJs: () => {
    return gulp.src(birtFiles)
      .pipe(concat('birt.min.js'))
      .pipe(terser({
        mangle: true,
        compress: {},
        format: {
          beautify: false,
          comments: false
        }
      }))
      .pipe(gulp.dest(config.assets));
  },
  buildJs: () => {
    return gulp.src(findFilesForOriginalVersion(applicationDevFiles))
      .pipe(concat('application.js'))
      .pipe(terser({
        format: {
          beautify: true,
          comments: false
        },
        mangle: false,
        compress: false
      }))
      .pipe(gulp.dest(config.assets));
  },
  buildJsRedesigned: () => {
    return gulp.src(findFilesForRedesignedVersion(applicationDevFiles_redesigned))
      .pipe(concat('application.redesigned.js'))
      .pipe(terser({
        format: {
          beautify: true,
          comments: false
        },
        mangle: false,
        compress: false
      }))
      .pipe(gulp.dest(config.assets));
  },
  buildBirtJs: () => {
    return gulp.src(birtFiles)
      .pipe(concat('birt.js'))
      .pipe(terser({
        format: {
          beautify: true,
          comments: false
        },
        mangle: false,
        compress: false
      }))
      .pipe(gulp.dest(config.assets));
  }
}