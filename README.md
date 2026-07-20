![OpenEMM Dashboard](https://www.agnitas.de/wp-content/uploads/2024/12/OpenEMM-Dashboard-EN.png)

# :dart: About OpenEMM

OpenEMM is an open source software for email marketing and email automation, developed and maintained by AGNITAS AG. It is our free of charge version of our commercial software E-Marketing Manager (EMM). Installation and administration are fully handled by you! Our software is available in English and German.  
The current version of OpenEMM is **26.05**. 

**Experience OpenEMM yourself with our interactive demo tours:**    

➡️ [Interactive Demo Tour of OpenEMM (English)](https://app.storylane.io/share/hmj7avv1g76e)  
➡️ [Interactive Demo Tour of OpenEMM (German)](https://app.storylane.io/share/tqlok8xti0es)


**Experience OpenEMM yourself with a free demo access:**

➡️ [Free Demo Access for OpenEMM](https://www.agnitas.de/en/openemm-demo/)

## :dizzy: Features

OpenEMM offers tons of features for professional marketing users, among them:

- a console based administration tool for checks, configuration, updates and backups (OST)
- a **responsive web user interface** with great usability and different languages
- a **mail template management** system
- a **visual web form builder**
- mailing, template and web forms import and export to load and exchange prepared mailing templates and web forms
- a **graphical workflow manager** to create complex campaigns with drag&drop
- individual and (GDPR compliant) anonymous tracking of mail openings, link clicks and deep tracking
- **automated bounce management**
- graphical **realtime statistics** with lots of KPIs and configurable reports (PDF and CSV)
- self-defined **target groups** based on recipient profiles, recpient's status and behaviour (created visually or with SQL-like syntax)
- a **scalable multiqueue mail backend** for maximum sending performance
- flexibly configurable data import and export with extensive reporting of results
- **predefined triggers** that can be accessed by HTTPS and can be registered as **webhooks** for 3rd party systems
- a **scripting** feature to enhance the functionality of OpenEMM with **customized triggers**
- sophisticated management of users, user roles and user rights
- an audit-proof searchable and exportable user activity log
- a system status menu with helpful info and configuration options for OpenEMM administrators
- an extensive set of **SOAP webservices** to manage OpenEMM from remote
- a feature-rich **RESTful API** to manage OpenEMM from remote
- a callback API to register **webhooks** for notifications of 3rd party systems about various mailing and recipient events
- **connectors for integration plaforms** Make and N8N

## :open_book: Additional information  

If you want to read more about OpenEMM, make sure to look at:  

🗣️ our [website](https://www.agnitas.de/en/e-marketing_manager/email-marketing-software-variants/openemm/) for **marketing information**   

🧑‍💻 our [wiki](https://wiki-iframe.agnitas.de/doku.php?id=start) for all **technical information**  

📑 the [OpenEMM manual](https://www.agnitas.de/en/openemm-downloads/#1714398725552-fc64ae33-7263), which can be downloaded on our website  

🌟 our [OpenEMM factsheet](https://wiki-iframe.agnitas.de/doku.php?id=start#openemm_downloads) to learn about all the **highlights of version 26.05**. 

##  🤝 Supported environments

OpenEMM supports Linux (and other operating systems like Windows and macOS via Oracle's VirtualBox™). This is the required software stack for **OpenEMM 26.05**:

| Technical Requirements | Details |
| ---------------------- | ------- |
| **Server with Linux operating system** |  Red Hat Enterprise Linux version **8 or 9** or Red Hat based versions such as AlmaLinux 8 and 9 <br>SUSE Linux Enterprise Server 15 with the latest service pack |
| **Database management system (DBMS)** | MariaDB version **10.6.10 or later** |
| **Mail Transfer Agent (MTA)** | Postfix version **3.5 or later** |
| **Java JDK** |  Java OpenJDK **21** (LTS version) |
| **Node.js** |  Node.js version **22 or higher** |
| **Python** |  Python version **3.11 or higher** |
| **Application Server** | Apache Tomcat **11** |

## :file_folder: Download of Executable Code of OpenEMM:

You can find all relevant, free downloads here: [OpenEMM Binaries Download](https://www.agnitas.de/en/download/openemm-binaries/).

## :busts_in_silhouette: Forum for OpenEMM Support:

If you already use OpenEMM and need help, visit our [OpenEMM Forum](https://forum.openemm.org) and explore the different topics with answers to various questions from the community. 

## :information_source: Source Code of OpenEMM:

 The source code of OpenEMM can be found here, in this GitHub repository.


### :construction_worker: Building the Runtime:

The files in directory `runtime` provide a runtime environment for OpenEMM and tool **OST** (OpenEMM Support Tool) in subdirectory `bin`. **OST** helps you to install, update and administrate OpenEMM. Just copy the content of directory `runtime` to `/home/openemm/` on your server and start **OST** with `/home/openemm/bin/OST.sh`.

You can either use **OST** to download the executable OpenEMM tarballs provided by the original OpenEMM developer AGNITAS, or you can build the executable code yourself as described below.

### ⚙️ Building the Backend:

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


### :computer: Building the Frontend:

Copy template files `build.properties.default`, `build-birt.properties.default` and `build-ws.properties.default` to files with names `build.properties`, `build-birt.properties` and `build-ws.properties` and adapt the paths listed in those three files to your needs. Make sure that the `appname`s you use are listed in the Host tag of your Tomcat's configuration file `server.xml`. `deploydir` is usually `/home/openemm/webapps/`. `deploytarget` should be left unchanged..

Build the executable code with

`$ cd openemm/frontend`

`$ ant -f build.xml`

`$ ant -f build-birt.xml`

`$ ant -f build-ws.xml`

To update the database schema, run the shell script that executes any missing database updates:

`$ /home/openemm/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh dbcfg`

Finally, exceute

`$ /home/openemm/webapps/emm/WEB-INF/npm install`

to install all required npm packages.

You can start the OpenEMM frontend with

`/home/openemm/bin/emm.sh start`


## ➕ Extensions for OpenEMM:

Is your email marketing growing, leading to higher demands on your email marketing software? Contact our [consultants](mailto:consulting@agnitas.de) and get advice on [OpenEMM Plus](https://www.agnitas.de/en/openemm-plus/)! In OpenEMM Plus you can purchase individual premium functions as extensions to the free OpenEMM. In that way you only pay for the features you actually need, leaving you with a solution that is perfectly tailored to you.

---

:rotating_light: If you think that you have found a security problem with OpenEMM, please contact us at email address security@agnitas.de to initiate a responsible disclosure of the security vulnerability you may have found.
