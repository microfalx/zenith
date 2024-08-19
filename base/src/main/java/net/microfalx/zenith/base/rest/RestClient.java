package net.microfalx.zenith.base.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import java.io.StringReader;
import java.net.URI;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

/**
 * A rest client for Selenium end points.
 */
public class RestClient<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);

    private final URI uri;
    private final Class<T> resultType;

    private ObjectMapper mapper;

    public static <T> RestClient<T> create(URI uri, Class<T> resultType) {
        return new RestClient<>(uri, resultType);
    }

    public RestClient(URI uri, Class<T> resultType) {
        requireNonNull(uri);
        requireNonNull(resultType);
        this.uri = uri;
        this.resultType = resultType;
    }

    /**
     * Executes the Rest API call and returns the JSON object as a mapped Java Object.
     *
     * @return the result
     */
    public T execute() {
        createMapper();
        org.springframework.web.client.RestClient.RequestHeadersSpec<?> request = createRequest();
        T result = null;
        try {
            String body = request.retrieve().body(String.class);
            if (body != null) {
                result = mapper.readValue(new StringReader(body), resultType);
            }
        } catch (Exception e) {
            throw new RestException("Failed to execute request to '" + uri + "'", e);
        }
        if (result == null) {
            throw new RestException("An empty response was returned by '" + uri + "'");
        }
        return result;
    }

    private void createMapper() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
    }

    private org.springframework.web.client.RestClient.RequestHeadersSpec<?> createRequest() {
        return org.springframework.web.client.RestClient.create(uri.toASCIIString()).get()
                .accept(MediaType.APPLICATION_JSON);
    }
}
