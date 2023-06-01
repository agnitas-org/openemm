
package com.agnitas.reporting.birt.external.dataset;

import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.CompanyInfoDao;
import org.agnitas.emm.core.commons.util.ConfigService;

import com.agnitas.dao.AdminDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.LicenseDao;
import com.agnitas.dao.impl.AdminDaoImpl;
import com.agnitas.dao.impl.ComCompanyDaoImpl;
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

        ConfigTableDao configTableDao = new ConfigTableDaoImpl();
        ((ConfigTableDaoImpl) configTableDao).setDataSource(dataSource);
        ((ConfigTableDaoImpl) configTableDao).setJavaMailService(javaMailservice);
        newConfigService.setConfigTableDao(configTableDao);
        
        CompanyInfoDao companyInfoDao = new CompanyInfoDao();
        companyInfoDao.setDataSource(dataSource);
        companyInfoDao.setJavaMailService(javaMailservice);
        newConfigService.setCompanyInfoDao(companyInfoDao);
        
        ComCompanyDao companyDao = new ComCompanyDaoImpl();
        ((ComCompanyDaoImpl) companyDao).setDataSource(dataSource);
        ((ComCompanyDaoImpl) companyDao).setJavaMailService(javaMailservice);
        newConfigService.setCompanyDao(companyDao);
        
        LicenseDao licenseDao = new LicenseDaoImpl();
        ((LicenseDaoImpl) licenseDao).setDataSource(dataSource);
        ((LicenseDaoImpl) licenseDao).setJavaMailService(javaMailservice);
        newConfigService.setLicenseDao(licenseDao);
        
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
