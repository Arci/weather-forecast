package it.arcidiacono.weatherforecast.owm;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.arcidiacono.weatherforecast.owm.exception.CityNotFoundException;
import it.arcidiacono.weatherforecast.owm.exception.OWMException;
import it.arcidiacono.weatherforecast.owm.exception.ResponseFormatException;
import it.arcidiacono.weatherforecast.own.Measure;

public class OWMClient {

	private static Logger logger = LoggerFactory.getLogger(OWMClient.class);

	private static final String ENDPOINT = "https://api.openweathermap.org/data/2.5";

	private static final String FORECAST = "forecast";

	private final String apiKey;

	public OWMClient(String apiKey) {
		if(StringUtils.isBlank(apiKey)) {
			throw new NullPointerException("api key cannot be null");
		}
		this.apiKey = apiKey;
	}


	/** Queries OWM for forecast data of the given city.
	 *
	 * @param name the city name
	 * @param country the country code in two characters format
	 * @return a list of {@linkplain Measure} parsed from OWM response
	 * @throws OWMException if any error occur
	 */
	public List<Measure> getForecast (String name, String country) throws OWMException {
		WebTarget webTarget = buildForecastWebTarget(name, country);
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		if (response.getStatus() < Status.BAD_REQUEST.getStatusCode()) {
			return parseResponse(response);
		} else {
			String error = getErrorMessage(response);
			if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
				throw new CityNotFoundException(error);
			}
			throw new OWMException(error, response.getStatus());
		}
	}

	private WebTarget buildForecastWebTarget (String name, String country) {
		Client client = ClientBuilder.newClient();
		return client.target(ENDPOINT)
				.path(FORECAST)
				.queryParam("appid", apiKey)
				.queryParam("q", name + "," + country)
				.queryParam("units", "metric");
	}

	/*
	 * TODO better error handling:
	 * - list can miss
	 * - main can miss
	 * - dt, temp and
	 * pressure can miss or data type conversion can fail Either throw a new
	 * "MalformedResponseException" or log and skip the element
	 */
	private List<Measure> parseResponse (Response response) throws ResponseFormatException {
		List<Measure> measures = new ArrayList<>();
		JsonNode json = asJson(response);
		JsonNode list = json.get("list");
		if (list.isArray()) {
			for (final JsonNode element : list) {
				Instant timestamp = Instant.ofEpochSecond(element.get("dt").asLong());
				JsonNode main = element.get("main");
				Double temperature = main.get("temp").asDouble();
				Double pressure = main.get("pressure").asDouble();
				Measure measure = Measure.of(timestamp, temperature, pressure);
				measures.add(measure);
			}
		}
		return measures;
	}

	/*
	 * TODO better error handling:
	 * - message can miss
	 */
	private String getErrorMessage (Response response) throws ResponseFormatException {
		JsonNode json = asJson(response);
		return json.get("message").asText();
	}

	private JsonNode asJson (Response response) throws ResponseFormatException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readTree(response.readEntity(String.class));
		} catch (IOException e) {
			logger.error("cannot read response body", e);
			throw new ResponseFormatException(e);
		}
	}

}
