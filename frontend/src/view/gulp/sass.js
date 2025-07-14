const gulp = require('gulp');
const sass = require('gulp-sass')(require('sass')); // Use Dart Sass
const rename = require('gulp-rename');
const cleanCSS = require('gulp-clean-css');
const config = require('./config');
const sourcemaps = require('gulp-sourcemaps');

module.exports = {
  buildCSS: () => {
    return gulp.src([
      `${config.assets}/sass/application.scss`,
      `${config.assets}/sass/help.scss`
    ])
      .pipe(sourcemaps.init())
      .pipe(sass({outputStyle: 'expanded'}).on('error', sass.logError))
      .pipe(cleanCSS({ format: 'beautify', level: { 1: { specialComments: 0 } } }))
      .pipe(sourcemaps.write('./'))
      .pipe(gulp.dest(config.assets));
  },
  buildRedesignedCSS: () => {
    return gulp.src(`${config.assets}/sass_redesign/application.scss`)
      .pipe(sourcemaps.init())
      .pipe(sass({outputStyle: 'expanded'}).on('error', sass.logError))
      .pipe(cleanCSS({ format: 'beautify', level: { 1: { specialComments: 0 } } }))
      .pipe(rename({basename: 'application', suffix: '.redesigned'}))
      .pipe(sourcemaps.write('./'))
      .pipe(gulp.dest(config.assets));
  },
  buildLandingCSS: () => {
    return gulp.src(`${config.assets}/sass_redesign/landing.scss`)
      .pipe(sourcemaps.init())
      .pipe(sass({outputStyle: 'expanded'}).on('error', sass.logError))
      .pipe(cleanCSS({ format: 'beautify', level: { 1: { specialComments: 0 } } }))
      .pipe(sourcemaps.write('./'))
      .pipe(gulp.dest(config.assets));
  },
  buildFormCSS: () => {
    return gulp.src(`${config.assets}/sass/form.scss`)
      .pipe(sourcemaps.init())
      .pipe(sass({outputStyle: 'expanded'}).on('error', sass.logError))
      .pipe(cleanCSS({ format: 'beautify', level: { 1: { specialComments: 0 } } }))
      .pipe(sourcemaps.write('./'))
      .pipe(gulp.dest(config.assets));
  }
}
