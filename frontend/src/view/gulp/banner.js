const gulp = require('gulp');
const fs = require('fs');
const insert = require('gulp-insert');
const lodash = require('lodash');
const config = require('./config');

module.exports = {
  bannerJs: () => {
    return gulp.src(`${config.assets}/application.min.js`)
      .pipe(insert.prepend(readBanner('application.js')))
      .pipe(gulp.dest(config.assets));
  },
  bannerBirtJs: () => {
    return gulp.src(`${config.assets}/birt.min.js`)
      .pipe(insert.prepend(readBanner('birt.js')))
      .pipe(gulp.dest(config.assets));
  },
  bannerCss: () => {
    return gulp.src(`${config.assets}/application.min.css`)
      .pipe(insert.prepend(readBanner('application.css')))
      .pipe(gulp.dest(config.assets));
  },
  bannerLanding: () => {
    return gulp.src(`${config.assets}/landing.min.css`)
      .pipe(insert.prepend(readBanner('application.css')))
      .pipe(gulp.dest(config.assets));
  }
};

function readBanner(fileName) {
  const now = new Date();

  const day = String(now.getDate()).padStart(2, '0');
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const currentDate =  `${day}-${month}-${(now.getFullYear())}`;

  const banner = fs.readFileSync(`./assets/banner/${fileName}`, 'utf8');
  return lodash.template(banner)({currentDate}) + '\n';
}