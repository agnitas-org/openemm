<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>
<xsl:template match="/">
#######################################################
# Properties file with EMM-Settings                   #
#######################################################

#######################################################
# General values                                      #
#######################################################
# Full version number of deployed application version (e.g.: 15.10.120-hf5)
ApplicationVersion=${ApplicationVersion}

# Remove versionsign from emm application sites
live.version=${isLiveServer}
# Show beta-text in application logo
beta.version=${isBetaServer}
# Show legacy-text in application logo
legacy.version=${isServiceServer}

<xsl:if test="properties/server/ignoreDeletedMessages" >
# Ignore deleted messages (Only Agnitas Beta server should do so)
ignoreDeletedMessages=<xsl:value-of select="properties/server/ignoreDeletedMessages"/>
</xsl:if>

#######################################################
# Database settings                                   #
#######################################################
# Name of the configured jndi database used by the application
jdbc.emmDB.jndiName=emm_db
# Name of the configured internal jndi derby database for statistic purposes
jdbc.tmpDB.jndiName=embedded

<xsl:if test="properties/server/url" >
# URL for PDF reports
system.url=<xsl:value-of select="properties/server/url"/>
</xsl:if>

#######################################################
# EMM defaults                                        #
#######################################################
<xsl:if test="properties/mailgun/system" >
# Url of merger used for triggering
system.mailgun=<xsl:value-of select="properties/mailgun/system"/>
</xsl:if>
# Path to ghostscript installation
system.ghostscript=/usr/bin/gs
# Path to salt file for login passwords
system.salt.file=<xsl:value-of select="properties/security/salt-file"/>

# Installation path of html to pdf converter application
wkhtmltopdf=<xsl:value-of select="properties/htmlPdfConverter"/>
# Installation path of html to image converter application
wkhtmltoimage=<xsl:value-of select="properties/htmlImageConverter"/>

# Directory for velocity logs
velocity.logdir=<xsl:value-of select="properties/directory/velocity_log"/>

#######################################################
# Mail addresses                                      #
#######################################################
# Contact address for support requests to be shown to users
mailaddress.frontend=<xsl:value-of select="properties/mailaddresses/frontend"/>
# Contact address for support to be shown to users
mailaddress.support=<xsl:value-of select="properties/mailaddresses/support"/>
# Email recipient for critical error mails of the application
mailaddress.error=<xsl:value-of select="properties/mailaddresses/error"/>
# Email address to inform of new uploaded files
mailaddress.upload.database=<xsl:value-of select="properties/mailaddresses/upload_database"/>
# Email address to inform of new uploaded files
mailaddress.upload.support=<xsl:value-of select="properties/mailaddresses/upload_support"/>
# Email address to archivate report mails
mailaddress.report_archive=<xsl:value-of select="properties/mailaddresses/report_archive"/>

# Directory for external components (images)
component.directory=<xsl:value-of select="properties/directory/component"/>
system.cdn=<xsl:value-of select="properties/cdn"/>

#######################################################
# Caching                                             #
#######################################################
# Maximum cachesize of images delivered by rdir application
hostedImage.maxCache=<xsl:value-of select="properties/rdir/hostedImage/maxCache"/>
# Maximum cachetime of images delivered by rdir application
hostedImage.maxCacheTimeMillis=<xsl:value-of select="properties/rdir/hostedImage/maxCacheTimeMillis"/>

# Maximum cachesize of eventmails delivered by mailgun
mailgun.maxCache=100
# Maximum cachetime of eventmails delivered by mailgun
mailgun.maxCacheTimeMillis=300000

# Maximum cachesize of meassuered links in the rdir application
rdir.urls.maxCache=<xsl:value-of select="properties/rdir/urls/maxCache"/>
# Maximum cachetime of meassuered links in the rdir application
rdir.urls.maxCacheTimeMillis=<xsl:value-of select="properties/rdir/urls/maxCacheTimeMillis"/>

# Maximum cachesize of mailings in the rdir application
rdir.mailingIds.maxCache=500
# Maximum cachetime of mailings in the rdir application
rdir.mailingIds.maxCacheTimeMillis=300000

#######################################################
# Preview                                             #
#######################################################
<xsl:if test="properties/preview/mailgun/cache/size" >
# Maximum number of cached previews
preview.mailgun.cache.size = <xsl:value-of select="properties/preview/mailgun/cache/size"/>
</xsl:if>
<xsl:if test="properties/preview/mailgun/cache/age" >
# Maximum age of cached previews
preview.mailgun.cache.age = <xsl:value-of select="properties/preview/mailgun/cache/age"/>
</xsl:if>
<xsl:if test="properties/preview/page/cache/size">
# Maximum number of cached fullviews
preview.page.cache.size = <xsl:value-of select="properties/preview/page/cache/size"/>
</xsl:if>
<xsl:if test="properties/preview/page/cache/age">
# Maximum age of cached fullviews
preview.page.cache.age = <xsl:value-of select="properties/preview/page/cache/age"/>
</xsl:if>
<xsl:if test="properties/preview/logname">
# Logfile namepart of preview generation
preview.logname = <xsl:value-of select="properties/preview/logname" />
</xsl:if>
<xsl:if test="properties/preview/loglevel">
# Loglevel of preview generation
preview.loglevel = <xsl:value-of select="properties/preview/loglevel" />
</xsl:if>

<xsl:if test="properties/reporting/birt">
#######################################################
# Birt Statistic                                      #
#######################################################
# Url of the birt statistic application
birt.url=<xsl:value-of select="properties/reporting/birt/url"/>
<xsl:if test="properties/reporting/birt/url_intern">
# Url of the birt statistic application to be used for internal purposes like email reports only by birt itself when behind firewall
birt.url.intern=<xsl:value-of select="properties/reporting/birt/url_intern"/>
</xsl:if>
# Url of the birt application to go deeper in some statistic values
birt.drilldownurl=<xsl:value-of select="properties/reporting/birt/drilldownurl"/>
# Path of public key file for secured statistics calls (public-private-key method)
birt.publickeyfile=<xsl:value-of select="properties/reporting/birt/publickeyfile"/>
# Path of private key file for secured statistics calls (public-private-key method)
birt.privatekeyfile=<xsl:value-of select="properties/reporting/birt/privatekeyfile"/>
</xsl:if>

#######################################################
# Configuration for Undo feature                      #
#######################################################
# Maximum number of undo steps to keep
undo.limit=15

#######################################################################
# Internal Java-Mail configuration (NOT for mailings sent by backend) #
#######################################################################
# SMTP host for java system mails
system.mail.host=<xsl:value-of select="properties/smtp/host"/>
# Sender address
mailaddress.sender=<xsl:value-of disable-output-escaping="yes" select="properties/mailaddresses/sender"/>
# Sender name
mailaddress.sender.name=<xsl:value-of disable-output-escaping="yes" select="properties/mailaddresses/sender_name"/>
# ReplyTo address
mailaddress.replyto=<xsl:value-of disable-output-escaping="yes" select="properties/mailaddresses/replyto"/>
# ReplyTo name
mailaddress.replyto.name=<xsl:value-of disable-output-escaping="yes" select="properties/mailaddresses/replyto_name"/>
# Bounce address
mailaddress.bounce=<xsl:value-of disable-output-escaping="yes" select="properties/mailaddresses/bounce"/>

#######################################################
# Backend (Mailgun.ini replacement)                   #
#######################################################
# Mailgun loglevel for log informations
mailgun.ini.loglevel=<xsl:value-of select="properties/mailgun/ini/loglevel"/>
<xsl:if test="properties/mailgun/ini/maildir" >
# Spool directory for test mails
mailgun.ini.maildir=<xsl:value-of select="properties/mailgun/ini/maildir"/>
</xsl:if>
# Default encoding for mail generation
mailgun.ini.default_encoding=<xsl:value-of select="properties/mailgun/ini/default_encoding"/>
# Default characterset for mail generation
mailgun.ini.default_charset=<xsl:value-of select="properties/mailgun/ini/default_charset"/>
# Default blocksize for mail generation
mailgun.ini.blocksize=<xsl:value-of select="properties/mailgun/ini/blocksize"/>
# Path to directory for mailer xml packages
mailgun.ini.metadir=<xsl:value-of select="properties/mailgun/ini/metadir"/>
# Installation path of xmlback binary
mailgun.ini.xmlback=<xsl:value-of select="properties/mailgun/ini/xmlback"/>
# Logfile for accounting information in mail generation
mailgun.ini.account_logfile=<xsl:value-of select="properties/mailgun/ini/account_logfile"/>
# Check xml package of mailre right after creation? (true or false)
mailgun.ini.xmlvalidate=<xsl:value-of select="properties/mailgun/ini/xmlvalidate"/>
# Domainpart of created email messageids
mailgun.ini.domain=<xsl:value-of select="properties/mailgun/ini/domain"/>
# Boundary textpart for multipart email messages
mailgun.ini.boundary=<xsl:value-of select="properties/mailgun/ini/boundary"/>
# Number of mails to wait before start of showing progress information
mailgun.ini.mail_log_number=<xsl:value-of select="properties/mailgun/ini/mail_log_number"/>
# EndOfLine character for mail generation
mailgun.ini.eol=<xsl:value-of select="properties/mailgun/ini/eol"/>
# Value of header information (X-Mailer)
mailgun.ini.mailer=<xsl:value-of select="properties/mailgun/ini/mailer"/>
# Storage path of event based mails directly sent by this server
mailgun.ini.directdir=<xsl:value-of select="properties/mailgun/ini/directdir"/>

#######################################################
# Extension System (Plugins)                          #
#######################################################
# EMM plugin directory
plugins.home=<xsl:value-of select="properties/plugins"/>
# User for birt plugins
birt.host.user=<xsl:value-of select="properties/birt-hostuser"/>
# Birt plugin directory
birt.plugin.directory=<xsl:value-of select="properties/birt-plugins"/>

#######################################################
# Manual                                              #
#######################################################
# Installpath of online help files
manual_install_path=<xsl:value-of select="properties/manual_install_path"/>
# Available languages of online help
onlinehelp.languages=de,en

#######################################################
# Host authentication (Two-Way-Authentication)        #
#######################################################
# Allow insecure cookies for Two-Way-Authentication
<xsl:choose>
	<xsl:when test="properties/hostauthentication/cookies/httpsOnly">
hostauthentication.cookies.https.only=<xsl:value-of select="properties/hostauthentication/cookies/httpsOnly"/>
	</xsl:when>
	<xsl:otherwise>
hostauthentication.cookies.https.only=true
	</xsl:otherwise>
</xsl:choose>

</xsl:template>
</xsl:stylesheet>
