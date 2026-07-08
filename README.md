![OpenEMM Dashboard](https://www.agnitas.de/wp-content/uploads/2024/12/OpenEMM-Dashboard-EN.png)
# Resources for OpenEMM

[Interactive Demo Tour of OpenEMM](https://app.storylane.io/share/hmj7avv1g76e))

[Free Demo Access for OpenEMM](https://www.agnitas.de/en/openemm-demo/)

## Website of OpenEMM with Marketing Information:

[OpenEMM Website](https://www.agnitas.de/en/e-marketing_manager/email-marketing-software-variants/openemm/)

## Wiki of OpenEMM with Technical Information:

[OpenEMM Wiki](https://wiki.openemm.org)

## Download of Executable Code of OpenEMM:

[OpenEMM Binaries Download](https://www.agnitas.de/en/download/openemm-binaries/)

## Forum for OpenEMM Support:

[OpenEMM Forum](https://forum.openemm.org)

## Source Code of OpenEMM:

[OpenEMM GitHub Repository](https://github.com/agnitas-org/openemm)


### Building the Runtime:

The files in directory `runtime` provide a runtime environment for OpenEMM and tool **OST** (OpenEMM Support Tool) in subdirectory `bin`. **OST** helps you to install, update and administrate OpenEMM. Just copy the content of directory `runtime` to `/home/openemm/` on your server and start **OST** with `/home/openemm/bin/OST.sh`.

You can either use **OST** to download the executable OpenEMM tarballs provided by the original OpenEMM developer AGNITAS, or you can build the executable code yourself as described below.

### Building the Backend:

This should be done on a server with the same Linux operating system as the server where OpenEMM will be deployed:

`$ git clone https://github.com/agnitas-org/openemm`

`$ cd openemm/backend`

`$ chmod 755 build-backend.py`

`$ ./build-backend.py`

This creates the OpenEMM backend tarball in the current directory. Copy this tarball to the home directory of OpenEMM, unpack the tarball and create a symlink:

`cp openemm-backend-{version}.tar.gz /home/openemm`

`cd /home/openemm/release/backend`

`tar xapf ~/openemm-backend-${version}.tar.gz`

`ln -s V${version} current`

(Replace expression {version} with the version number from the tarball file name.)

You can start the OpenEMM backend with

`/home/openemm/bin/backend.sh start`


### Building the Frontend:

Copy template files `build.properties.default`, `build-birt.properties.default` and `build-ws.properties.default` to files with names `build.properties`, `build-birt.properties` and `build-ws.properties` and adapt the paths listed in those three files to your needs. Make sure that the `appname`s you use are listed in the Host tag of your Tomcat's configuration file `server.xml`. `deploydir` is usually `/home/openemm/webapps/`. `deploytarget` should be left unchanged..

Build the executable code with

`$ cd openemm/frontend`

`$ ant -f build.xml`

You can start the OpenEMM frontend with

`/home/openemm/bin/emm.sh start`


## Extensions for OpenEMM:

[OpenEMM Plus](https://www.agnitas.de/en/openemm-plus/)

---

If you think that you have found a security problem with OpenEMM, please contact us at email address security@agnitas.de to initiate a responsible disclosure of the security vulnerability you may have found.
