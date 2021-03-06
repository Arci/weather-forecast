package it.arcidiacono.weatherforecast;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import it.arcidiacono.weatherforecast.exception.ServiceExceptionMapper;
import it.arcidiacono.weatherforecast.exception.ValidationExceptionMapper;

@ApplicationPath("/")
public class WeatherApplication extends ResourceConfig {

	private static final String RESOURCE_PACKAGE = "it.arcidiacono.weatherforecast.resource";

	public WeatherApplication() {
		packages(RESOURCE_PACKAGE);

		register(new ApplicationBinder());

		register(JacksonFeature.class);
		register(ObjectMapperContextResolver.class);

		register(ServiceExceptionMapper.class);
		register(ValidationExceptionMapper.class);

		register(new OpenApiResource());
	}

}
