package it.arcidiacono.weatherforecast;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import it.arcidiacono.weatherforecast.operation.WeatherOperation;
import it.arcidiacono.weatherforecast.owm.OWMClient;
import it.arcidiacono.weatherforecast.service.WeatherService;

public class ApplicationBinder extends AbstractBinder {
	@Override
	protected void configure () {
		bindOperations();

		bindServices();

		bindClasses();

		bindSingletons();
	}

	private void bindOperations () {
		bind(WeatherOperation.class).to(WeatherOperation.class);
	}

	private void bindServices () {
		bind(WeatherService.class).to(WeatherService.class);
	}

	private void bindClasses () {
		bind(OWMClient.class).to(OWMClient.class);
	}

	private void bindSingletons () {
		bind(CityDictionary.class).to(CityDictionary.class).in(Singleton.class);
	}

}
