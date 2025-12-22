const gulp = require('gulp');
const config = require('./config');
const concat = require('gulp-concat');
const terser = require('gulp-terser');

const path = require('path');
const {sync} = require('glob');

const dashboardFiles = [
  'definitions',
  'dashboard-grid',
  'dashboard-tile-swapper',
  'tiles/initializer/dashboard-calendar-label/abstract/dashboard-calendar-label',
  'tiles/initializer/dashboard-calendar-label/dashboard-calendar-comment-label',
  'tiles/initializer/dashboard-calendar-label/dashboard-calendar-mailing-label',
  'tiles/initializer/dashboard-calendar-label/dashboard-calendar-auto-opt-label',
  'tiles/initializer/dashboard-calendar-label/dashboard-calendar-push-label',
  'tiles/initializer/abstract/calendar-base',
  'tiles/initializer/calendar-light',
  'tiles/initializer/dashboard-calendar-period-picker',
  'tiles/initializer/dashboard-calendar-comments-manager',
  'tiles/initializer/dashboard-calendar',
  'tiles/validator/calendar-comment-validator',
  'tiles/abstract/draggable-tile',
  'tiles/abstract/base-mailing-statistics-tile',
  'tiles/abstract/base-mailing-device-statistics-tile',
  'tiles/empty-tile',
  'tiles/add-ons-tile',
  'tiles/analysis-tile',
  'tiles/calendar-tile',
  'tiles/imports-exports-tile',
  'tiles/statistics-tile',
  'tiles/clickers-tile',
  'tiles/openers-tile',
  'tiles/mailings-tile',
  'tiles/news-tile',
  'tiles/planning-tile',
  'tiles/workflows-tile',
  'dashboard-mailing-popover',
  'dashboard-grid-controller',
  'dashboard-calendar-controller',
  'dashboard-news',
].map(file => `${config.assets}/js/controllers/dashboard/${file}.js`);

const workflowFiles = [
  'comment-editor',
  'field-rules-table',
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

const scheduleFiles = [
  'interval/*.js',
  'schedule-builder.js'
].map(file => `${config.assets}/js/lib/schedule/${file}`);

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
].map(file => `${config.assets}/js/lib/layout-builder/${file}.js`);

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

const mailingContentFiles = [
  'definitions',
  'dyn-content',
  'dyn-tag',
  'html-version-dyn-tag',
  'ai-editor',
  'targets-manager',
  'dyn-tag-editor',
  'content-blocks-storage',
  'content-blocks-manager',
  'emc-text-modules-manager',
  'mailing-content-controller',
].map(file => `${config.assets}/js/controllers/mailing-content/${file}.js`);

const applicationCommonFiles = [
    `${config.runtimeLibs}/jquery/jquery-3.5.1.js`,
    `${config.assets}/js/vendor/jquery-ui-1.14.1.js`,
    `${config.runtimeLibs}/lodash/lodash-4.17.21.js`,
    `${config.assets}/js/vendor/jquery-i18n-1.1.1.js`,
    `${config.node_modules}/marked/marked.min.js`,
    `${config.node_modules}/spectrum-colorpicker/spectrum.js`,
    `${config.node_modules}/select2/dist/js/select2.js`,
    `${config.runtimeLibs}/select2-to-tree/select2totree-1.1.1.js`,
    `${config.assets}/js/vendor/jquery-endless-scroll-1.6.0.js`,
    `${config.assets}/js/vendor/jquery-dirty-0.8.3.js`,
    `${config.node_modules}/bootstrap/dist/js/bootstrap.bundle.js`,
    `${config.node_modules}/hammerjs/hammer.js`,
    `${config.node_modules}/chart.js/dist/chart.umd.js`,
    `${config.node_modules}/chartjs-adapter-date-fns/dist/chartjs-adapter-date-fns.bundle.js`,
    `${config.node_modules}/chartjs-plugin-zoom/dist/chartjs-plugin-zoom.js`,
    `${config.node_modules}/chartjs-plugin-datalabels/dist/chartjs-plugin-datalabels.js`,
    `${config.node_modules}/js-beautify/js/lib/beautify-html.js`,
    `${config.assets}/js/vendor/toastr.custom.js`,
    `${config.assets}/js/vendor/moment-2.30.1.js`,
    `${config.assets}/js/vendor/moment-timezone-with-data-10-year-range.js`,
    `${config.assets}/js/vendor/perfect-scrollbar-1.5.5.js`,
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
    `${config.assets}/js/boot/jodit/*.js`,
    `${config.assets}/js/boot/jodit/plugins/*.js`,
    `${config.assets}/js/vendor/querybuilder/query-builder.js`,
    ...fieldFiles,
    `${config.assets}/js/lib/*.js`,
    ...lbFiles,
    ...scheduleFiles,
    `${config.assets}/js/lib/workflow-view/definitions.js`,
    `${config.assets}/js/lib/workflow-view/utils.js`,
    `${config.assets}/js/lib/workflow-view/connection-constraints.js`,
    `${config.assets}/js/lib/workflow-view/node_popover.js`,
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
    `${config.assets}/js/lib/workflow-view/grid-background.js`,
    `${config.assets}/js/lib/workflow-view/editor.js`,
    `${config.assets}/js/lib/table/*.js`,
    `${config.assets}/js/lib/formbuilder/utils.js`,
    `${config.assets}/js/lib/formbuilder/emm-controls.js`,
    `${config.assets}/js/lib/formbuilder/templates.js`,
    `${config.assets}/js/lib/formbuilder/formbuilder.js`,
    `${config.assets}/js/initializers/*.js`,
    `${config.assets}/js/listener/*.js`,
    `${config.assets}/js/validators/*.js`,
    ...dashboardFiles,
    ...workflowFiles,
    ...mailingContentFiles,
    `${config.assets}/js/controllers/*.js`,
    `${config.assets}/js/vendor/interact.js`
  ],
  applicationDevFiles = applicationCommonFiles.concat([`${config.assets}/js/vendor/jquery-migrate-3.3.2.js`]),
  applicationMinFiles = applicationCommonFiles.concat([`${config.assets}/js/vendor/jquery-migrate-3.3.2.min.js`]),//migrate min file does not have logs about deprecated functions
  birtFiles = [
    `${config.assets}/js/vendor/iframe-resizer-content-window-4.3.2.js`,
    `${config.assets}/js/birt/*.js`
  ];

function resolveFiles(filesArray) {
  return filesArray.flatMap(filePath => sync(path.resolve(filePath)).reverse());
}

module.exports = {
  compileJs: () => {
    return gulp.src(resolveFiles(applicationMinFiles))
      .pipe(concat('application.min.js'))
      .pipe(terser({
        mangle: true,
        compress: true,
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
    return gulp.src(resolveFiles(applicationDevFiles))
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