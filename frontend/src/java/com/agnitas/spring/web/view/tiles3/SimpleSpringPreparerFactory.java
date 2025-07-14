/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.web.view.tiles3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.tiles.TilesException;
import org.apache.tiles.preparer.PreparerException;
import org.apache.tiles.preparer.ViewPreparer;
import org.apache.tiles.preparer.factory.NoSuchPreparerException;

import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;

public class SimpleSpringPreparerFactory extends AbstractSpringPreparerFactory {

    /** Cache of shared ViewPreparer instances: bean name -> bean instance. */
    private final Map<String, ViewPreparer> sharedPreparers = new ConcurrentHashMap<>(16);


    @Override
    protected ViewPreparer getPreparer(String name, WebApplicationContext context) throws TilesException {
        // Quick check on the concurrent map first, with minimal locking.
        ViewPreparer preparer = this.sharedPreparers.get(name);
        if (preparer == null) {
            synchronized (this.sharedPreparers) {
                preparer = this.sharedPreparers.get(name);
                if (preparer == null) {
                    try {
                        Class<?> beanClass = ClassUtils.forName(name, context.getClassLoader());
                        if (!ViewPreparer.class.isAssignableFrom(beanClass)) {
                            throw new PreparerException(
                                    "Invalid preparer class [" + name + "]: does not implement ViewPreparer interface");
                        }
                        preparer = (ViewPreparer) context.getAutowireCapableBeanFactory().createBean(beanClass);
                        this.sharedPreparers.put(name, preparer);
                    }
                    catch (ClassNotFoundException ex) {
                        throw new NoSuchPreparerException("Preparer class [" + name + "] not found", ex);
                    }
                }
            }
        }
        return preparer;
    }

}
