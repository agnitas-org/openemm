const gulp = require("gulp");
const config = require("./config");
const fs = require('fs-extra');
const path = require('path');
const {exec} = require('child_process');

module.exports = {
  cleanStyleguide: () => {
    return fs.remove(path.join(config.docs, 'styleguide'))
      .then(() => console.log('Styleguide directory cleaned'))
      .catch(err => console.error('Error cleaning styleguide directory:', err));
  },
  cleanStyleguideSourceAssets: () => {
    return fs.remove(path.join(config.docs, 'src', 'assets'))
      .then(() => console.log('Styleguide source assets directory cleaned'))
      .catch(err => console.error('Error cleaning styleguide source assets directory:', err));
  },
  copyAssetsForStyleguide: gulp.series(
    () => fs.copy(`${config.view}/favicon.ico`, `${config.docs}/src/assets/favicon.ico`)
      .then(() => console.log(`Copied ${`${config.view}/favicon.ico`} to ${`${config.docs}/src/assets/favicon.ico`}`))
      .catch(err => console.error(`Error copying ${`${config.view}/favicon.ico`} to ${`${config.docs}/src/assets/favicon.ico`}:`, err)),
    () => {
      const assets = [
        `${config.assets}/fonts`,
        `${config.assets}/core`,
        `${config.assets}/icons-defs.svg`,
        `${config.assets}/application.css`,
        `${config.assets}/application.redesigned.css`,
        `${config.assets}/application.redesigned.js`,
        `${config.assets}/application.js`
      ];

      return Promise.all(assets.map(asset => {
        const dest = path.join(config.docs, 'src', 'assets', path.basename(asset));
        return fs.copy(asset, dest)
          .then(() => console.log(`Copied ${asset} to ${dest}`))
          .catch(err => console.error(`Error copying ${asset} to ${dest}:`, err));
      }));
    }
  ),
  buildStyleguide: () => {
    return new Promise((resolve, reject) => {
      exec(`hologram -c ${config.docs}/src/hologram_config.yml`, (error, stdout, stderr) => {
        if (error) {
          reject(`Error: ${error.message}`);
        }
        if (stderr) {
          console.error(`stderr: ${stderr}`);
        }
        console.log(`stdout: ${stdout}`);
        resolve();
      });
    });
  }
};