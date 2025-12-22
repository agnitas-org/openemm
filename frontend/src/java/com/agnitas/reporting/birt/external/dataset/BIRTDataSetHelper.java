
package com.agnitas.reporting.birt.external.dataset;

import javax.sql.DataSource;

import com.agnitas.emm.core.commons.util.CompanyInfoDao;
import com.agnitas.emm.core.commons.util.ConfigService;

import com.agnitas.dao.AdminDao;
import com.agnitas.dao.CompanyDao;
import com.agnitas.web.perm.LicenseSignatureVerifier;
import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.LicenseDao;
import com.agnitas.dao.impl.AdminDaoImpl;
import com.agnitas.dao.impl.CompanyDaoImpl;
import com.agnitas.dao.impl.ConfigTableDaoImpl;
import com.agnitas.dao.impl.LicenseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.JavaMailServiceImpl;

/**
 * Open Emm implemntation of BIRTDataSetHelper for {@link BIRTDataSet }
 */
public class BIRTDataSetHelper {
    private static BIRTDataSetHelper instance;
    
    private ConfigService configService;
    private JavaMailService javaMailservice;
    
    private BIRTDataSetHelper() {
    }
    
    private void initConfigService(DataSource dataSource) {
        ConfigService newConfigService = ConfigService.getInstance();
        
        javaMailservice = new JavaMailServiceImpl();
        ((JavaMailServiceImpl) javaMailservice).setConfigService(newConfigService);

        newConfigService.setConfigTableDao(new ConfigTableDaoImpl(dataSource, javaMailservice, newConfigService));

        CompanyInfoDao companyInfoDao = new CompanyInfoDao();
        companyInfoDao.setDataSource(dataSource);
        companyInfoDao.setJavaMailService(javaMailservice);
        newConfigService.setCompanyInfoDao(companyInfoDao);
        
        CompanyDao companyDao = new CompanyDaoImpl();
        ((CompanyDaoImpl) companyDao).setDataSource(dataSource);
        ((CompanyDaoImpl) companyDao).setJavaMailService(javaMailservice);
        newConfigService.setCompanyDao(companyDao);
        
        LicenseDao licenseDao = new LicenseDaoImpl();
        ((LicenseDaoImpl) licenseDao).setDataSource(dataSource);
        ((LicenseDaoImpl) licenseDao).setJavaMailService(javaMailservice);
        newConfigService.setLicenseDao(licenseDao);

        newConfigService.setLicenseSignatureVerifier(new LicenseSignatureVerifier(licenseDao));
        
        AdminDao adminDao = new AdminDaoImpl();
        ((AdminDaoImpl) adminDao).setDataSource(dataSource);
        ((AdminDaoImpl) adminDao).setJavaMailService(javaMailservice);
        newConfigService.setAdminDao(adminDao);
        
        // Use only the fully initialized object
        this.configService = newConfigService;
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
