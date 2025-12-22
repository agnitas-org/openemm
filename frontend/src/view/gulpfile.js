const gulp = require('gulp');

const {
  buildJs,
  compileJs,
  buildBirtJs,
  compileBirtJs
} = require('./gulp/uglify');

const {
  bannerJs,
  bannerBirtJs,
  bannerCss,
  bannerLanding
} = require('./gulp/banner');

const {
  buildCSS,
  buildCssForStyleguide,
  buildFormCSS,
  buildLandingCSS
} = require('./gulp/sass');

const {
  minifyCss,
  minifyLanding
} = require('./gulp/cssmin');

const {
  cleanStyleguide,
  cleanStyleguideSourceAssets,
  copyAssetsForStyleguide,
  buildStyleguide
} = require('./gulp/styleguide');

const {
  workflow,
  dashboard,
  wysiwyg,
  sprite
} = require('./gulp/svg_sprite');

gulp.task('build_js', buildJs);
gulp.task('compile_js', gulp.series(buildJs, compileJs, bannerJs));
gulp.task('compile_birtjs', gulp.series(bannerBirtJs, compileBirtJs, bannerBirtJs));

gulp.task('compile_css', gulp.series(buildCSS, minifyCss, bannerCss));
gulp.task('compile_landing_css', gulp.series(buildLandingCSS, minifyLanding, bannerLanding));
gulp.task('compile_forms_css', buildFormCSS);

gulp.task('svg_sprite', gulp.series(sprite, workflow, dashboard, wysiwyg));

gulp.task('styleguide', gulp.series('svg_sprite', buildBirtJs, buildJs, buildCssForStyleguide, cleanStyleguide, copyAssetsForStyleguide, buildStyleguide, cleanStyleguideSourceAssets));

gulp.task('watch', () => {
  gulp.watch(`assets/js/**/*.js`, gulp.series('compile_js'));
  gulp.watch(`assets/sass/**/*.scss`, gulp.series('compile_css', 'compile_landing_css', 'compile_forms_css'));
  gulp.watch(`assets/core/images/**/*`, gulp.series('svg_sprite'));
});

gulp.task('default', gulp.series('watch'));
