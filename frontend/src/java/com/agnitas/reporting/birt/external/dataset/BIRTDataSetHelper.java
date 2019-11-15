
package com.agnitas.reporting.birt.external.dataset;

import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.CompanyInfoDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.apache.log4j.Logger;

import com.agnitas.dao.ComAdminDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.LicenseDao;
import com.agnitas.dao.impl.ComAdminDaoImpl;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
import com.agnitas.dao.impl.ConfigTableDaoImpl;
import com.agnitas.dao.impl.LicenseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.JavaMailServiceImpl;

/**
 * Open Emm implemntation of BIRTDataSetHelper for {@link BIRTDataSet }
 */
public class BIRTDataSetHelper {
    @SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(BIRTDataSetHelper.class);

    private static BIRTDataSetHelper instance;
    
    private ConfigService configService;
    private JavaMailService javaMailservice;
    
    private BIRTDataSetHelper() {
    }
    
    private void initConfigService(DataSource dataSource) {
        ConfigService configService = ConfigService.getInstance();
        
        javaMailservice = new JavaMailServiceImpl();
        ((JavaMailServiceImpl) javaMailservice).setConfigService(configService);

        ConfigTableDao configTableDao = new ConfigTableDaoImpl();
        ((ConfigTableDaoImpl) configTableDao).setDataSource(dataSource);
        ((ConfigTableDaoImpl) configTableDao).setJavaMailService(javaMailservice);
        configService.setConfigTableDao(configTableDao);
        
        CompanyInfoDao companyInfoDao = new CompanyInfoDao();
        companyInfoDao.setDataSource(dataSource);
        companyInfoDao.setJavaMailService(javaMailservice);
        configService.setCompanyInfoDao(companyInfoDao);
        
        ComCompanyDao companyDao = new ComCompanyDaoImpl();
        ((ComCompanyDaoImpl) companyDao).setDataSource(dataSource);
        ((ComCompanyDaoImpl) companyDao).setJavaMailService(javaMailservice);
        configService.setCompanyDao(companyDao);
        
        LicenseDao licenseDao = new LicenseDaoImpl();
        ((LicenseDaoImpl) licenseDao).setDataSource(dataSource);
        ((LicenseDaoImpl) licenseDao).setJavaMailService(javaMailservice);
        configService.setLicenseDao(licenseDao);
        
        ComAdminDao adminDao = new ComAdminDaoImpl();
        ((ComAdminDaoImpl) adminDao).setDataSource(dataSource);
        ((ComAdminDaoImpl) adminDao).setJavaMailService(javaMailservice);
        configService.setAdminDao(adminDao);
        
        this.configService = configService;
    }
    
    public static BIRTDataSetHelper getInstance() {
        if (instance == null) {
            instance = new BIRTDataSetHelper();
        }
        return instance;
    }

    public ConfigService getConfigService(DataSource dataSource) {
        if (configService == null) {
            initConfigService(dataSource);
        }
        return configService;
    }
    
    public JavaMailService getJavaMailService(DataSource dataSource) {
        if (javaMailservice == null) {
            initConfigService(dataSource);
        }
        
        return javaMailservice;
    }
}
