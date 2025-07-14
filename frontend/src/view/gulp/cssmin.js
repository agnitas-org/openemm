const gulp = require('gulp');
const cleanCSS = require('gulp-clean-css');
const rename = require('gulp-rename');
const config = require('./config');

const minifyCSS = (srcFiles, destFile) => {
  return gulp.src(srcFiles)
    .pipe(cleanCSS({ compatibility: 'ie8', level: { 1: { specialComments: 0 } } }))
    .pipe(rename({ suffix: '.min' }))
    .pipe(gulp.dest(destFile));
};

module.exports = {
  compileCss: () => {
    return minifyCSS([
      `${config.assets}/application.css`,
      `${config.assets}/help.css`
    ], `${config.assets}`);
  },
  compileRedesignedCss: () => {
    return minifyCSS([
      `${config.assets}/application.redesigned.css`,
      `${config.assets}/help.css`
    ], `${config.assets}`);
  },
  compileLanding: () => {
    return minifyCSS([
      `${config.assets}/landing.css`
    ], `${config.assets}`);
  }
};
