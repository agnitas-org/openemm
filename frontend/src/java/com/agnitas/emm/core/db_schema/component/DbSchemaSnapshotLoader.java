/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.db_schema.component;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import com.agnitas.emm.core.db_schema.bean.DbSchemaSnapshot;
import com.agnitas.emm.core.db_schema.service.DbSchemaSnapshotService;
import jakarta.servlet.ServletContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DbSchemaSnapshotLoader implements InitializingBean {

    private static final Logger logger = LogManager.getLogger(DbSchemaSnapshotLoader.class);

    private final DbSchemaSnapshotService snapshotService;
    private final ServletContext servletContext;

    public DbSchemaSnapshotLoader(DbSchemaSnapshotService snapshotService, @Autowired(required = false) ServletContext servletContext) {
        this.snapshotService = snapshotService;
        this.servletContext = servletContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // hack for junit tests
        if (servletContext == null) {
            return;
        }

        String vendorName = snapshotService.getVendorName();
        File sqlDir = new File(servletContext.getRealPath("/WEB-INF/sql/" + vendorName));

        if (!sqlDir.exists() || !sqlDir.isDirectory()) {
            logger.warn("Can't find SQL directory for DB vendor {}!", vendorName);
            return;
        }

        Optional<File> schemaFile = Stream.of(sqlDir.listFiles())
                .filter(f -> f.getName().startsWith("emm-" + vendorName + "-schema"))
                .findAny();

        if (schemaFile.isEmpty()) {
            return;
        }

        DbSchemaSnapshot snapshot = snapshotService.read(schemaFile.get());
        if (!snapshotService.exists(snapshot.getVersionNumber())) {
            snapshotService.save(snapshot);
        }
    }
}
