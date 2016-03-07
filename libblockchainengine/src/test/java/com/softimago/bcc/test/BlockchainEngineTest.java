package com.softimago.bcc.test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.softimago.bcc.engine.BlockchainEngine;
import com.softimago.bcc.engine.JsonParser;
import com.softimago.bcc.engine.bo.ChartPoint;
import com.softimago.bcc.engine.bo.ChartType;
import com.softimago.bcc.engine.bo.RealChartCompilation;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStream;
import java.lang.Exception;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

public class BlockchainEngineTest
{
    private static final int MOCK_HTTP_PORT = 8888;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(MOCK_HTTP_PORT);

    @Test
    public void testMarketPriceParser() throws Exception
    {
        InputStream is = getClass().getClassLoader().getResourceAsStream("test/market-price.json");
        Assert.assertNotNull(is);

        JsonParser<Map<String, List<ChartPoint>>> parser = JsonParser.CHART_POINTS;

        Map<String, List<ChartPoint>> collection = parser.parse(is);
        Assert.assertNotNull(collection);
        Assert.assertNotNull(collection.get("values"));
        Assert.assertEquals(366, collection.get("values").size());

        Calendar cal = Calendar.getInstance();
        cal.setTime(collection.get("values").get(0).date);
        int year = cal.get(Calendar.YEAR);
        Assert.assertEquals(2015, year);
    }

    // test with mock for http requests
    @Test
    public void testChartMarketPriceRequest() throws Exception
    {
        InputStream is = BlockchainEngineTest.class.getClassLoader().getResourceAsStream("test/market-price.json");
        java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");

        stubFor(any(urlMatching("/charts/market-price"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody(scanner.hasNext() ? scanner.next() : "")));

        BlockchainEngine engine = BlockchainEngine.BlockchainEngineFactory("http://localhost:8888/charts/%s", ChartType.MARKET_PRICE);
        Assert.assertNotNull(engine);
        Assert.assertNotNull(engine.getChartPoints());
        Assert.assertNotEquals(0, engine.getChartPoints().size());

    }

    // test integration - call real http server
    @Test
    public void testIntegrationChartMarketPriceRequest() throws Exception
    {
        BlockchainEngine engine = BlockchainEngine.BlockchainEngineFactory(ChartType.MARKET_PRICE);

        Assert.assertNotNull(engine);
        Assert.assertNotNull(engine.getChartPoints());
        Assert.assertNotEquals(0, engine.getChartPoints().size());
    }


    @Test
    public void testChartCompilation() throws  Exception
    {
        InputStream is = BlockchainEngineTest.class.getClassLoader().getResourceAsStream("test/market-price.json");
        java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");

        stubFor(any(urlMatching("/charts/market-price"))
                .willReturn(aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-Type", "application/json")
                        .withBody(scanner.hasNext() ? scanner.next() : "")));

        BlockchainEngine engine = BlockchainEngine.BlockchainEngineFactory("http://localhost:8888/charts/%s", ChartType.MARKET_PRICE);

        RealChartCompilation rcc = engine.getChartCompilation(3, 3, 30);

        Assert.assertNotNull(rcc);
        Assert.assertEquals(30, rcc.points.size());
        Assert.assertEquals(3, rcc.averageXLabels.size());
        Assert.assertEquals(3, rcc.averageYLabeles.size());
        Assert.assertEquals(engine.getChartPoints().get(0).date, rcc.points.get(0).original.date);
        Assert.assertEquals(engine.getChartPoints().get(engine.getChartPoints().size()-1).date, rcc.points.get(29).original.date);
    }
}
