/**
 * Copyright 2014-2016 yangming.liu<liuyangming@gmail.com>.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 */
package org.bytesoft.bytejta.supports.spring;

import java.util.ArrayList;
import java.util.List;

import org.bytesoft.transaction.TransactionBeanFactory;
import org.bytesoft.transaction.aware.TransactionBeanFactoryAware;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;

@Deprecated
public class TransactionBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		String beanFactoryBeanId = null;
		List<BeanDefinition> beanFactoryAwareBeanIdList = new ArrayList<BeanDefinition>();
		String[] beanNameArray = beanFactory.getBeanDefinitionNames();
		for (int i = 0; i < beanNameArray.length; i++) {
			String beanName = beanNameArray[i];
			BeanDefinition beanDef = beanFactory.getBeanDefinition(beanName);
			String beanClassName = beanDef.getBeanClassName();

			Class<?> beanClass = null;
			try {
				beanClass = cl.loadClass(beanClassName);
			} catch (Exception ex) {
				continue;
			}

			if (TransactionBeanFactoryAware.class.isAssignableFrom(beanClass)) {
				beanFactoryAwareBeanIdList.add(beanDef);
			}

			if (TransactionBeanFactory.class.isAssignableFrom(beanClass)) {
				if (beanFactoryBeanId == null) {
					beanFactoryBeanId = beanName;
				} else {
					throw new FatalBeanException("Duplicated transaction-bean-factory defined.");
				}
			}

		}

		for (int i = 0; beanFactoryBeanId != null && i < beanFactoryAwareBeanIdList.size(); i++) {
			BeanDefinition beanDef = beanFactoryAwareBeanIdList.get(i);
			MutablePropertyValues mpv = beanDef.getPropertyValues();
			RuntimeBeanReference beanRef = new RuntimeBeanReference(beanFactoryBeanId);
			mpv.addPropertyValue(TransactionBeanFactoryAware.BEAN_FACTORY_FIELD_NAME, beanRef);
		}

	}
}
