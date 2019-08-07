<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>
<xsl:template match="/">
portTypeName=<xsl:value-of select="properties/portTypeName"/>
wsdlLocationUri=<xsl:value-of select="properties/wsdlLocationUri"/>

# Expiration time of ExportSubscriberToFTP jobs
exportSubscriberToFtp.jobs.statusExpireMinutes=<xsl:value-of select="properties/exportSubscriberToFtp/jobs/statusExpireMinutes"/>
exportSubscriberToFtp.ftp.connection.timeoutSeconds=<xsl:value-of select="properties/exportSubscriberToFtp/ftp/connection/timeoutSeconds"/>
</xsl:template>
</xsl:stylesheet>
