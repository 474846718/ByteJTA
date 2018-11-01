/**
 * Copyright 2014-2018 yangming.liu<bytefox@126.com>.
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
package org.bytesoft.bytejta.supports.dubbo.config;

import java.util.List;

import javax.transaction.UserTransaction;

import org.bytesoft.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClients;

@ImportResource({ "classpath:bytejta-supports-dubbo.xml" })
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement
public class DubboSupportConfiguration implements TransactionManagementConfigurer, ApplicationContextAware, EnvironmentAware {
	static final Logger logger = LoggerFactory.getLogger(DubboSupportConfiguration.class);
	static final String CONSTANT_MONGODBURI = "spring.data.mongodb.uri";

	static final String CONSTANTS_SKEN_ID = "skeleton@org.bytesoft.transaction.remote.RemoteCoordinator";
	static final String CONSTANTS_STUB_ID = "stub@org.bytesoft.transaction.remote.RemoteCoordinator";

	static final int CONSTANTS_TIMEOUT_MILLIS = 6000;
	static final String CONSTANTS_TIMEOUT_KEY = "org.bytesoft.bytejta.timeout";

	private Environment environment;
	private ApplicationContext applicationContext;

	public PlatformTransactionManager annotationDrivenTransactionManager() {
		JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
		jtaTransactionManager.setTransactionManager(this.applicationContext.getBean(TransactionManager.class));
		jtaTransactionManager.setUserTransaction(this.applicationContext.getBean(UserTransaction.class));
		return jtaTransactionManager;
	}

	@ConditionalOnMissingBean(com.mongodb.client.MongoClient.class)
	@ConditionalOnProperty(CONSTANT_MONGODBURI)
	@org.springframework.context.annotation.Bean
	public com.mongodb.client.MongoClient mongoClient(@Autowired(required = false) com.mongodb.MongoClient mongoClient) {
		if (mongoClient == null) {
			return MongoClients.create(this.environment.getProperty(CONSTANT_MONGODBURI));
		} else {
			List<ServerAddress> addressList = mongoClient.getAllAddress();
			StringBuilder ber = new StringBuilder();
			for (int i = 0; addressList != null && i < addressList.size(); i++) {
				ServerAddress address = addressList.get(i);
				String host = address.getHost();
				int port = address.getPort();
				if (i == 0) {
					ber.append(host).append(":").append(port);
				} else {
					ber.append(",").append(host).append(":").append(port);
				}
			}
			return MongoClients.create(String.format("mongodb://%s", ber.toString()));
		}
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

}
