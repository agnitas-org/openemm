const gulp = require('gulp');

const {
  buildJs,
  compileJs,
  buildJsRedesigned,
  compileJsRedesigned,
  buildBirtJs,
  compileBirtJs
} = require('./gulp/uglify');

const {
  bannerJs,
  bannerJsRedesigned,
  bannerBirtJs,
  bannerCss,
  bannerCssRedesigned,
  bannerLanding
} = require('./gulp/banner');

const {
  buildCSS,
  buildRedesignedCSS,
  buildFormCSS,
  buildLandingCSS
} = require('./gulp/sass');

const {
  compileCss,
  compileRedesignedCss,
  compileLanding
} = require('./gulp/cssmin');

const {
  cleanStyleguide,
  cleanStyleguideSourceAssets,
  copyAssetsForStyleguide,
  buildStyleguide
} = require('./gulp/styleguide');

const {
  oldSprite,
  workflow,
  dashboard,
  wysiwyg,
  sprite
} = require('./gulp/svg_sprite');

gulp.task('compile_js_classic', gulp.series(buildJs, compileJs, bannerJs));
gulp.task('compile_js_redesigned', gulp.series(buildJsRedesigned, compileJsRedesigned, bannerJsRedesigned));
gulp.task('compile_birtjs', gulp.series(bannerBirtJs, compileBirtJs, bannerBirtJs));
gulp.task('compile_js', gulp.series('compile_js_classic', 'compile_js_redesigned'));

gulp.task('compile_css', gulp.series(buildCSS, compileCss, bannerCss, buildFormCSS));
gulp.task('compile_css_redesigned', gulp.series(buildRedesignedCSS, compileRedesignedCss, bannerCssRedesigned));
gulp.task('compile_landing_css', gulp.series(buildLandingCSS, compileLanding, bannerLanding));
gulp.task('compile_forms_css', buildFormCSS);

gulp.task('svg_sprite', gulp.series(sprite, oldSprite, workflow, dashboard, wysiwyg));
gulp.task('docs', require('./gulp/connect'));

gulp.task('styleguide', gulp.series('svg_sprite', buildJs, buildBirtJs, buildJsRedesigned, buildRedesignedCSS, cleanStyleguide, copyAssetsForStyleguide, buildStyleguide, cleanStyleguideSourceAssets));

gulp.task('watch', () => {
  gulp.watch(`assets/js/**/*.js`, gulp.series('compile_js'));
  gulp.watch(`assets/sass_redesign/**/*.scss`, gulp.series('compile_css_redesigned', 'compile_landing_css', 'compile_forms_css'));
  gulp.watch(`assets/core/images/**/*`, gulp.series('svg_sprite'));
});

gulp.task('default', gulp.series('watch'));
