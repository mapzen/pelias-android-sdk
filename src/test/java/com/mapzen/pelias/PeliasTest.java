package com.mapzen.pelias;

import com.mapzen.pelias.gson.Result;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PeliasTest {
  Pelias peliasWithMock;
  TestCallback callback;
  PeliasService mock;

  @Captor @SuppressWarnings("unused") private ArgumentCaptor<Callback<Result>> cb;

  @Before public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    callback = new TestCallback();
    mock = Mockito.mock(PeliasService.class);
    peliasWithMock = new Pelias(mock);
  }

  @Test public void search_getSearch() throws Exception {
    BoundingBox boundingBox = new BoundingBox(1.0, 2.0, 3.0, 4.0);
    peliasWithMock.search("test", boundingBox, callback);
    verify(mock).getSearch(eq("test"), eq(1.0), eq(2.0), eq(3.0), eq(4.0), cb.capture());
  }

  @Test public void search_getSearchWithLocationProvider() throws Exception {
    peliasWithMock.setLocationProvider(new TestLocationProvider());
    peliasWithMock.search("test", callback);
    verify(mock).getSearch(eq("test"), eq(3.0), eq(4.0), eq(5.0), eq(6.0), cb.capture());
  }

  @Test public void search_getSearchWithFocusPoint() throws Exception {
    peliasWithMock.search("test", 1.0, 2.0, callback);
    verify(mock).getSearch(eq("test"), eq(1.0), eq(2.0), cb.capture());
  }

  @Test public void suggest_getSuggest() throws Exception {
    peliasWithMock.suggest("test", 1.0, 2.0, callback);
    verify(mock).getSuggest(eq("test"), eq(1.0), eq(2.0), cb.capture());
  }

  @Test public void suggest_getSuggestWithLocationProvider() throws Exception {
    peliasWithMock.setLocationProvider(new TestLocationProvider());
    peliasWithMock.suggest("test", callback);
    verify(mock).getSuggest(eq("test"), eq(1.0), eq(2.0), cb.capture());
  }

  @Test public void reverse_getReverseGeocode() throws Exception {
    peliasWithMock.setLocationProvider(new TestLocationProvider());
    peliasWithMock.reverse(30.0, 40.0, callback);
    verify(mock).getReverse(eq(30.0), eq(40.0), cb.capture());
  }

  @Test public void place_shouldSendSearchRequestToServer() throws Exception {
    peliasWithMock.place("osm:venue:3669115471", callback);
    verify(mock).getPlace(eq("osm:venue:3669115471"), cb.capture());
  }

  @Test public void setEndpoint_shouldChangeServiceEndpoint() throws Exception {
    final MockWebServer server = new MockWebServer();
    MockResponse response = new MockResponse();
    server.enqueue(response);
    server.play();
    Pelias pelias = new Pelias(server.getUrl("/").toString());
    pelias.setRequestHandler(new PeliasRequestHandler() {
      @Override public Map<String, String> headersForRequest() {
        HashMap<String, String> headers = new HashMap();
        headers.put("TEST_HEADER", "TEST_HEADER_VALUE");
        return headers;
      }

      @Override public Map<String, String> queryParamsForRequest() {
        HashMap<String, String> params = new HashMap();
        params.put("TEST_PARAM", "TEST_PARAM_VALUE");
        return params;
      }
    });
    pelias.suggest("test", 1.0, 2.0, callback);
    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).contains("/autocomplete");
    assertThat(request.getHeader("TEST_HEADER")).isEqualTo("TEST_HEADER_VALUE");
    assertThat(request.getPath()).contains("TEST_PARAM");
    server.shutdown();
  }

  class TestCallback implements Callback<Result> {
    @Override public void success(Result o, Response response) {
    }

    @Override public void failure(RetrofitError retrofitError) {
    }
  }

  public static class TestLocationProvider implements PeliasLocationProvider {
    @Override public double getLat() {
      return 1.0;
    }

    @Override public double getLon() {
      return 2.0;
    }

    @Override public BoundingBox getBoundingBox() {
      return new BoundingBox(3.0, 4.0, 5.0, 6.0);
    }
  }
}
