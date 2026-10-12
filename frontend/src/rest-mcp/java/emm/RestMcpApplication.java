package com.agnitas.emm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({
        "classpath:applicationContext-rest-mcp.xml",
        "/WEB-INF/applicationContext-commons-basic.xml",
        "/WEB-INF/applicationContext-commons-openemm.xml",
        "/WEB-INF/applicationContext-dao-basic.xml",
        "/WEB-INF/applicationContext-dao-openemm.xml",
        "/WEB-INF/applicationContext-service-basic.xml",
        "/WEB-INF/applicationContext-service-openemm.xml",
        "/WEB-INF/dataAccessContext.xml"
})
public class RestMcpApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(RestMcpApplication.class, args);
    }

}
