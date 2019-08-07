<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>
<xsl:template match="/">
##############################################################################
# LOG4J settings
##############################################################################

##############################
# Configuration of appenders #
##############################

log4j.appender.ROOT_CONSOLE=org.apache.log4j.ConsoleAppender
log4j.appender.ROOT_CONSOLE.Target=System.out
log4j.appender.ROOT_CONSOLE.Threshold=<xsl:value-of select="properties/logging/rootLogLevel"/>
log4j.appender.ROOT_CONSOLE.layout=org.apache.log4j.PatternLayout
log4j.appender.ROOT_CONSOLE.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n

log4j.appender.APPLICATIONLOGFILE=org.apache.log4j.DailyRollingFileAppender
log4j.appender.APPLICATIONLOGFILE.DatePattern='.'yyyy-MM-dd
log4j.appender.APPLICATIONLOGFILE.File=<xsl:value-of select="properties/logging/applicationLogFile"/>
log4j.appender.APPLICATIONLOGFILE.Append=true
log4j.appender.APPLICATIONLOGFILE.Threshold=<xsl:value-of select="properties/logging/applicationLogLevel"/>
log4j.appender.APPLICATIONLOGFILE.layout=org.apache.log4j.PatternLayout
log4j.appender.APPLICATIONLOGFILE.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n

log4j.appender.APPLICATIONERRORLOGFILE=org.apache.log4j.DailyRollingFileAppender
log4j.appender.APPLICATIONERRORLOGFILE.DatePattern='.'yyyy-MM-dd
log4j.appender.APPLICATIONERRORLOGFILE.File=<xsl:value-of select="properties/logging/applicationErrorLogFile"/>
log4j.appender.APPLICATIONERRORLOGFILE.Append=true
log4j.appender.APPLICATIONERRORLOGFILE.Threshold=ERROR
log4j.appender.APPLICATIONERRORLOGFILE.layout=org.apache.log4j.PatternLayout
log4j.appender.APPLICATIONERRORLOGFILE.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n

log4j.appender.VELOCITY_SECURITY_LOGFILE=org.apache.log4j.DailyRollingFileAppender
log4j.appender.VELOCITY_SECURITY_LOGFILE.DatePattern='.'yyyy-MM-dd
log4j.appender.VELOCITY_SECURITY_LOGFILE.File=<xsl:value-of select="properties/logging/velocitySecurityLogFile"/>
log4j.appender.VELOCITY_SECURITY_LOGFILE.Append=true
log4j.appender.VELOCITY_SECURITY_LOGFILE.Threshold=WARN
log4j.appender.VELOCITY_SECURITY_LOGFILE.layout=org.apache.log4j.PatternLayout
log4j.appender.VELOCITY_SECURITY_LOGFILE.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n

log4j.appender.VELOCITY_SECURITY_SMTPLOG=org.apache.log4j.net.SMTPAppender
log4j.appender.VELOCITY_SECURITY_SMTPLOG.Threshold=WARN
log4j.appender.VELOCITY_SECURITY_SMTPLOG.BufferSize=1
log4j.appender.VELOCITY_SECURITY_SMTPLOG.To=<xsl:value-of select="properties/mailaddresses/frontend"/>
<xsl:choose>
	<xsl:when test="properties/mailaddresses/sender">
log4j.appender.VELOCITY_SECURITY_SMTPLOG.From=<xsl:value-of select="properties/mailaddresses/sender"/>
	</xsl:when>
	<xsl:otherwise>
log4j.appender.VELOCITY_SECURITY_SMTPLOG.From=${deploytarget}@agnitas.de
	</xsl:otherwise>
</xsl:choose>
log4j.appender.VELOCITY_SECURITY_SMTPLOG.SMTPHost=127.0.0.1
log4j.appender.VELOCITY_SECURITY_SMTPLOG.Subject=Velocity Error
log4j.appender.VELOCITY_SECURITY_SMTPLOG.layout=org.apache.log4j.PatternLayout
log4j.appender.VELOCITY_SECURITY_SMTPLOG.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n

<xsl:if test="properties/logging/messageLogFile">
log4j.appender.WS_MESSAGELOGFILE=org.apache.log4j.RollingFileAppender
log4j.appender.WS_MESSAGELOGFILE.File=<xsl:value-of select="properties/logging/messageLogFile"/>
log4j.appender.WS_MESSAGELOGFILE.Threshold=<xsl:value-of select="properties/logging/messageLogLevel"/>
log4j.appender.WS_MESSAGELOGFILE.MaxBackupIndex=3
log4j.appender.WS_MESSAGELOGFILE.MaxFileSize=100MB
log4j.appender.WS_MESSAGELOGFILE.layout=org.apache.log4j.PatternLayout
log4j.appender.WS_MESSAGELOGFILE.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
</xsl:if>

<xsl:if test="properties/logging/endpointLogFile">
log4j.appender.WS_ENDPOINTLOGFILE=org.apache.log4j.RollingFileAppender
log4j.appender.WS_ENDPOINTLOGFILE.File=<xsl:value-of select="properties/logging/endpointLogFile"/>
log4j.appender.WS_ENDPOINTLOGFILE.Threshold=<xsl:value-of select="properties/logging/endpointLogLevel"/>
log4j.appender.WS_ENDPOINTLOGFILE.MaxBackupIndex=3
log4j.appender.WS_ENDPOINTLOGFILE.MaxFileSize=100MB
log4j.appender.WS_ENDPOINTLOGFILE.layout=org.apache.log4j.PatternLayout
log4j.appender.WS_ENDPOINTLOGFILE.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
</xsl:if>

<xsl:if test="properties/logging/eqlLogFile">
log4j.appender.EQLLOGFILE=org.apache.log4j.DailyRollingFileAppender
log4j.appender.EQLLOGFILE.DatePattern='.'yyyy-MM-dd
log4j.appender.EQLLOGFILE.File=<xsl:value-of select="properties/logging/eqlLogFile"/>
log4j.appender.EQLLOGFILE.Append=true
log4j.appender.EQLLOGFILE.Threshold=<xsl:value-of select="properties/logging/eqlLogLevel"/>
log4j.appender.EQLLOGFILE.layout=org.apache.log4j.PatternLayout
log4j.appender.EQLLOGFILE.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
</xsl:if>

<xsl:if test="properties/logging/webpushLogFile">
log4j.appender.WEBPUSHLOGFILE=org.apache.log4j.DailyRollingFileAppender
log4j.appender.WEBPUSHLOGFILE.DatePattern='.'yyyy-MM-dd
log4j.appender.WEBPUSHLOGFILE.File=<xsl:value-of select="properties/logging/webpushLogFile"/>
log4j.appender.WEBPUSHLOGFILE.Append=true
log4j.appender.WEBPUSHLOGFILE.Threshold=<xsl:value-of select="properties/logging/webpushLogLevel"/>
log4j.appender.WEBPUSHLOGFILE.layout=org.apache.log4j.PatternLayout
log4j.appender.WEBPUSHLOGFILE.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
</xsl:if>

<xsl:if test="properties/logging/thirdPartyLogFile">
log4j.appender.THIRDPARTYLOGFILE=org.apache.log4j.DailyRollingFileAppender
log4j.appender.THIRDPARTYLOGFILE.DatePattern='.'yyyy-MM-dd
log4j.appender.THIRDPARTYLOGFILE.File=<xsl:value-of select="properties/logging/thirdPartyLogFile"/>
log4j.appender.THIRDPARTYLOGFILE.Append=true
log4j.appender.THIRDPARTYLOGFILE.Threshold=<xsl:value-of select="properties/logging/thirdPartyLogLevel"/>
log4j.appender.THIRDPARTYLOGFILE.layout=org.apache.log4j.PatternLayout
log4j.appender.THIRDPARTYLOGFILE.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
</xsl:if>

############################
# Configuration of loggers #
############################

# Definition of root logger
log4j.rootLogger=<xsl:value-of select="properties/logging/rootLogLevel"/>, ROOT_CONSOLE, APPLICATIONLOGFILE, APPLICATIONERRORLOGFILE

log4j.logger.org.agnitas.emm.core.velocity=WARN, VELOCITY_SECURITY_LOGFILE, VELOCITY_SECURITY_SMTPLOG, APPLICATIONLOGFILE
log4j.logger.com.agnitas.emm.core.velocity=WARN, VELOCITY_SECURITY_LOGFILE, VELOCITY_SECURITY_SMTPLOG, APPLICATIONLOGFILE

# Logger for 3rd party code
<xsl:choose>
	<xsl:when test="properties/logging/thirdPartyLogFile">
log4j.logger.cz.vutbr.web=WARN, THIRDPARTYLOGFILE
log4j.logger.c.v.web=WARN, THIRDPARTYLOGFILE
log4j.logger.c.v.w=WARN, THIRDPARTYLOGFILE
	</xsl:when>
	<xsl:otherwise>
log4j.logger.cz.vutbr.web=WARN, ROOT_CONSOLE, APPLICATIONLOGFILE
log4j.logger.c.v.web=WARN, ROOT_CONSOLE, APPLICATIONLOGFILE
log4j.logger.c.v.w=WARN, ROOT_CONSOLE, APPLICATIONLOGFILE
	</xsl:otherwise>
</xsl:choose>

<xsl:if test="properties/logging/messageLogFile">
log4j.logger.org.springframework.ws.transport.http=<xsl:value-of select="properties/logging/messageLogLevel"/>, WS_MESSAGELOGFILE
log4j.additivity.org.springframework.ws.transport.http=false
log4j.logger.org.springframework.ws.server.MessageTracing=<xsl:value-of select="properties/logging/messageLogLevel"/>, WS_MESSAGELOGFILE
log4j.logger.org.springframework.ws.client.MessageTracing.sent=<xsl:value-of select="properties/logging/messageLogLevel"/>, WS_MESSAGELOGFILE
log4j.logger.org.springframework.ws.client.MessageTracing.received=<xsl:value-of select="properties/logging/messageLogLevel"/>, WS_MESSAGELOGFILE
log4j.additivity.org.springframework.ws.server.MessageTracing=false
</xsl:if>

<xsl:if test="properties/logging/endpointLogFile">
log4j.logger.com.agnitas.emm.springws.endpoint=<xsl:value-of select="properties/logging/endpointLogLevel"/>, WS_ENDPOINTLOGFILE
log4j.logger.org.agnitas.emm.springws.endpoint=<xsl:value-of select="properties/logging/endpointLogLevel"/>, WS_ENDPOINTLOGFILE
log4j.additivity.com.agnitas.emm.springws.endpoint=false
log4j.additivity.org.agnitas.emm.springws.endpoint=false

log4j.logger.org.agnitas.emm.core.recipient.service.impl.RecipientServiceImpl=<xsl:value-of select="properties/logging/endpointLogLevel"/>, WS_ENDPOINTLOGFILE
log4j.logger.com.agnitas.dao.impl.ComRecipientDaoImpl=<xsl:value-of select="properties/logging/endpointLogLevel"/>, WS_ENDPOINTLOGFILE 
</xsl:if>

<xsl:if test="properties/logging/eqlLogFile">
    <xsl:if test="properties/logging/eqlLogLevel">
log4j.logger.com.agnitas.dao.impl.ComTargetDaoImpl=<xsl:value-of select="properties/logging/eqlLogLevel"/>, EQLLOGFILE
log4j.logger.com.agnitas.emm.core.target.eql=<xsl:value-of select="properties/logging/eqlLogLevel"/>, EQLLOGFILE
    </xsl:if>
</xsl:if>

<xsl:if test="properties/logging/jasperJobWorkerLogFile">
<xsl:if test="properties/logging/jasperJobWorkerLogLevel">
log4j.appender.JASPER_JOB_QUEUE_LOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.JASPER_JOB_QUEUE_LOG.DatePattern='.'yyyy-MM-dd
log4j.appender.JASPER_JOB_QUEUE_LOG.File=<xsl:value-of select="properties/logging/jasperJobWorkerLogFile" />
log4j.appender.JASPER_JOB_QUEUE_LOG.Append=true
log4j.appender.JASPER_JOB_QUEUE_LOG.Threshold=<xsl:value-of select="properties/logging/jasperJobWorkerLogLevel" />
log4j.appender.JASPER_JOB_QUEUE_LOG.layout=org.apache.log4j.PatternLayout
log4j.appender.JASPER_JOB_QUEUE_LOG.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
log4j.logger.com.agnitas.web.ComJasperReportsJobWorker=<xsl:value-of select="properties/logging/jasperJobWorkerLogLevel" />, JASPER_JOB_QUEUE_LOG
</xsl:if>
</xsl:if>

<xsl:if test="properties/logging/hostAuthLogFile">
log4j.appender.HOST_AUTH=org.apache.log4j.DailyRollingFileAppender
log4j.appender.HOST_AUTH.DatePattern='.'yyyy-MM-dd
log4j.appender.HOST_AUTH.File=<xsl:value-of select="properties/logging/hostAuthLogFile" />
log4j.appender.HOST_AUTH.Append=true
log4j.appender.HOST_AUTH.Threshold=DEBUG
log4j.appender.HOST_AUTH.layout=org.apache.log4j.PatternLayout
log4j.appender.HOST_AUTH.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
log4j.logger.com.agnitas.emm.core.logon.LogonUtil=DEBUG, HOST_AUTH
log4j.logger.com.agnitas.emm.core.logon.web.ComLogonAction=DEBUG, HOST_AUTH
</xsl:if>

<xsl:if test="properties/logging/validateLogFile">
log4j.appender.VALIDATE=org.apache.log4j.DailyRollingFileAppender
log4j.appender.VALIDATE.DatePattern='.'yyyy-MM-dd
log4j.appender.VALIDATE.File=<xsl:value-of select="properties/logging/validateLogFile" />
log4j.appender.VALIDATE.Append=true
log4j.appender.VALIDATE.Threshold=DEBUG
log4j.appender.VALIDATE.layout=org.apache.log4j.PatternLayout
log4j.appender.VALIDATE.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
log4j.logger.org.agnitas.emm.core.validator.ModelValidator=DEBUG, VALIDATE
</xsl:if>

<xsl:if test="properties/logging/birtReportingLogFile"> 
log4j.appender.BIRT_REPORTING=org.apache.log4j.DailyRollingFileAppender
log4j.appender.BIRT_REPORTING.DatePattern='.'yyyy-MM-dd
log4j.appender.BIRT_REPORTING.File=<xsl:value-of select="properties/logging/birtReportingLogFile" />
log4j.appender.BIRT_REPORTING.Append=true
log4j.appender.BIRT_REPORTING.Threshold=DEBUG
log4j.appender.BIRT_REPORTING.layout=org.apache.log4j.PatternLayout
log4j.appender.BIRT_REPORTING.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
log4j.logger.com.agnitas.reporting.birt.external.scheduler.service.ReportSendWorker=DEBUG, BIRT_REPORTING
</xsl:if>

<xsl:if test="properties/logging/webpushLogFile">
log4j.appender.WEBPUSH=org.apache.log4j.DailyRollingFileAppender
log4j.appender.WEBPUSH.DatePattern='.'yyyy-MM-dd
log4j.appender.WEBPUSH.File=<xsl:value-of select="properties/logging/webpushLogFile" />
log4j.appender.WEBPUSH.Append=true
log4j.appender.WEBPUSH.Threshold=<xsl:value-of select="properties/logging/webpushLogLevel" />
log4j.appender.WEBPUSH.layout=org.apache.log4j.PatternLayout
log4j.appender.WEBPUSH.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
log4j.logger.com.agnitas.emm.push=DEBUG, WEBPUSH
log4j.logger.com.agnitas.emm.pushsend=DEBUG, WEBPUSH
log4j.logger.com.agnitas.emm.core.push=DEBUG, WEBPUSH
</xsl:if>

<xsl:if test="properties/logging/targetGroupMigrationLogFile">
log4j.appender.TARGETGROUP_MIGRATION=org.apache.log4j.DailyRollingFileAppender
log4j.appender.TARGETGROUP_MIGRATION.DatePattern='.'yyyy-MM-dd
log4j.appender.TARGETGROUP_MIGRATION.File=<xsl:value-of select="properties/logging/targetGroupMigrationLogFile" />
log4j.appender.TARGETGROUP_MIGRATION.Append=true
log4j.appender.TARGETGROUP_MIGRATION.Threshold=<xsl:value-of select="properties/logging/targetGroupMigrationLogLevel" />
log4j.appender.TARGETGROUP_MIGRATION.layout=org.apache.log4j.PatternLayout
log4j.appender.TARGETGROUP_MIGRATION.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
log4j.logger.com.agnitas.emm.core.target.web.listener.TargetGroupMigrationListener=DEBUG, TARGETGROUP_MIGRATION
</xsl:if>

## Debuglog for CleanDBDao
log4j.appender.CLEANLOG=org.apache.log4j.DailyRollingFileAppender
log4j.appender.CLEANLOG.DatePattern='.'yyyy-MM-dd
log4j.appender.CLEANLOG.File=${HOME}/logs/webapps/clean.log
log4j.appender.CLEANLOG.Append=true
log4j.appender.CLEANLOG.Threshold=DEBUG
log4j.appender.CLEANLOG.layout=org.apache.log4j.PatternLayout
log4j.appender.CLEANLOG.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
log4j.logger.com.agnitas.dao.impl.CleanDBDaoImpl=DEBUG, CLEANLOG

## Debuglog for temporary test purposes
#log4j.appender.TESTLOG=org.apache.log4j.DailyRollingFileAppender
#log4j.appender.TESTLOG.DatePattern='.'yyyy-MM-dd
#log4j.appender.TESTLOG.File=${HOME}/logs/webapps/test.log
#log4j.appender.TESTLOG.Append=true
#log4j.appender.TESTLOG.Threshold=DEBUG
#log4j.appender.TESTLOG.layout=org.apache.log4j.PatternLayout
#log4j.appender.TESTLOG.layout.ConversionPattern=%d: %-5p [%t] %c - %m%n
#log4j.logger.-----FullQualifiedPackageOrClassName-----=DEBUG, TESTLOG

</xsl:template>
</xsl:stylesheet>
