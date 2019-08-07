<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>
<xsl:template match="/">
# the url of the deployed ws-application wsdl
wsdl.url=<xsl:value-of select="properties/wsdlLocationUri"/>emmservices.wsdl

# this username and password should exist in webservice_user_tbl
ws.user=<xsl:value-of select="properties/ws-test/username"/>
ws.password=<xsl:value-of select="properties/ws-test/password"/>
</xsl:template>
</xsl:stylesheet>
