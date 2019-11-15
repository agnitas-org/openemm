<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>
<xsl:template match="/">
# Properties file with EMM-BIRT-Settings.

#######################################################
# General values                                      #
#######################################################
# Full version number of deployed application version (e.g.: 15.10.120-hf5)
ApplicationVersion=${ApplicationVersion}

# Remove versionsign from emm application sites
live.version=${isLiveServer}
# Show beta-text in application logo
beta.version=${isBetaServer}

#######################################################
# Birt Statistic                                      #
#######################################################
# Url of the birt statistic application
birt.url=<xsl:value-of select="properties/reporting/birt/url"/>

#######################################################
# Extension System (Plugins)                          #
#######################################################
# EMM BIRT plugin directory
plugins.home=<xsl:value-of select="properties/birt-plugins"/>

#######################################################################
# Internal Java-Mail configuration (NOT for mailings sent by backend) #
#######################################################################
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
# Mail addresses                                      #
#######################################################
# Email recipient for critical error mails of the application
mailaddress.error=<xsl:value-of select="properties/mailaddresses/error"/>

</xsl:template>
</xsl:stylesheet>
