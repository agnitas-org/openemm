**OpenEMM 2015 was succeeded by the completely revised OpenEMM 2020.**

Visit the website of OpenEMM 2020:

[OpenEMM 2020 Website](https://www.agnitas.de/en/e-marketing_manager/email-marketing-software-variants/openemm/)

Download the executable release of OpenEMM 2020:

[OpenEMM 2020 Binaries Download](https://www.agnitas.de/en/download/openemm-binaries/)

Read the OpenEMM 2019 Wiki with Installation & Configuration Guide:

[OpenEMM 2020 Wiki](https://wiki.openemm.org)

And join the OpenEMM support forum:

[OpenEMM Forum](https://forum.openemm.org)

**Compilation of OpenEMM backend**

If you want to compile the OpenEMM backend on a Linux platform that does not offer static libraries (like CentOS 8), the static libraries for _libssl_ and _libcrypto_ will be missing. To fix that edit file _Makefile_ in directory _src/c/xmlback/_ and remove parameters "-Wl,-Bstatic" and "-Wl,-Bdynamic" from the "LIBS" definition. 
