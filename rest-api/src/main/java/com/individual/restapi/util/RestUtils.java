package com.individual.restapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;

import static com.individual.restapi.util.RestUtils.Action.POST;
import static com.individual.restapi.util.RestUtils.Action.PUT;
import static java.util.Objects.isNull;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
public class RestUtils {

    public static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // This is the only method that uses the default OkHttpClient instance from the config class.
    private static OkHttpClient okHttpClient = new OkHttpClient();

    public enum Action {
        GET, POST, PUT, DELETE
    }

    private RestUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Response doGet(
            OkHttpClient okHttpClient,
            String url,
            Map<String, String> queryParameters,
            Map<String, String> headers
    ) throws IOException {
        // This method to set the other OkHttpClient instance if needed.
        RestUtils.okHttpClient = okHttpClient;
        Request request = buildGetRequest(url, queryParameters, headers);
        return execute(request);
    }

    public static <T> Response doPut(
            OkHttpClient okHttpClient,
            String url,
            T requestBody,
            Map<String, String> headers
    ) throws IOException {
        RestUtils.okHttpClient = okHttpClient;
        Request request = buildRequest(url, requestBody, headers, PUT);
        return execute(request);
    }

    public static <T> Response doPost(
            OkHttpClient okHttpClient,
            String url,
            T requestBody,
            Map<String, String> headers
    ) throws IOException {
        RestUtils.okHttpClient = okHttpClient;
        Request request = buildRequest(url, requestBody, headers, POST);
        return execute(request);
    }

    public static <R> R doGet(
            OkHttpClient okHttpClient,
            String url,
            Map<String, String> queryParameters,
            Map<String, String> headers,
            TypeReference<R> responseType
    ) throws IOException {
        RestUtils.okHttpClient = okHttpClient;
        Request request = buildGetRequest(url, queryParameters, headers);
        return execute(request, responseType);
    }

    public static <T, R> R doPut(
            OkHttpClient okHttpClient,
            String url,
            T requestBody,
            Map<String, String> headers,
            TypeReference<R> responseType
    ) throws IOException {
        RestUtils.okHttpClient = okHttpClient;
        Request request = buildRequest(url, requestBody, headers, PUT);
        return execute(request, responseType);
    }

    public static <T, R> R doPost(
            OkHttpClient okHttpClient,
            String url,
            T requestBody,
            Map<String, String> headers,
            TypeReference<R> responseType
    ) throws IOException {
        if (okHttpClient != null)
            RestUtils.okHttpClient = okHttpClient;
        Request request = buildRequest(url, requestBody, headers, POST);
        return execute(request, responseType);
    }


    private static Request buildGetRequest(String url,
                                           Map<String, String> queryParameters,
                                           Map<String, String> headers) {
        final HttpUrl httpUrl = HttpUrl.parse(url);
        if (isNull(httpUrl)) throw new IllegalArgumentException("Invalid url: " + url);

        final HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        queryParameters.forEach(urlBuilder::addQueryParameter);

        String finalUrl = urlBuilder.build().toString();
        Request.Builder requestBuilder = new Request.Builder().url(finalUrl);

        headers.forEach(requestBuilder::header);
        log.info("Call Rest Api with GET : " + finalUrl);

        return requestBuilder.build();
    }

    private static <T> Request buildRequest(String url, T requestBody, Map<String, String> headers,
                                            Action action) throws JsonProcessingException {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        headers.forEach(requestBuilder::header);

        String reqBodyInString = objectMapper.writeValueAsString(requestBody);
        RequestBody body = RequestBody.create(reqBodyInString, MediaType.parse(APPLICATION_JSON_VALUE));
        Request request = null;
        if (action == POST) {
            request = requestBuilder.post(body).build();
        } else if (action == PUT) {
            request = requestBuilder.put(body).build();
        }
        log.info("Call Rest Api with {} : {}", action, url);
        log.info(reqBodyInString);
        return request;
    }

    private static <R> R execute(Request request, TypeReference<R> responseType) throws IOException {
        Call call = okHttpClient.newCall(request);
        try (Response response = call.execute()) {
            final ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return null;
            }
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String resBody = responseBody.string();
            return objectMapper.readValue(resBody, responseType);
        }
    }

    private static Response execute(Request request) throws IOException {
        Call call = okHttpClient.newCall(request);
        return call.execute();
    }
}
