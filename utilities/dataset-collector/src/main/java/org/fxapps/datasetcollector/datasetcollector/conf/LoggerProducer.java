package org.fxapps.datasetcollector.datasetcollector.conf;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.logging.Logger;

@Dependent
public class LoggerProducer {

	@Produces
	public Logger produceLogger(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
	}
}