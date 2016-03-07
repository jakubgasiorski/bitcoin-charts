package com.softimago.bcc.engine;


import com.softimago.bcc.engine.bo.AxisPoint;
import com.softimago.bcc.engine.bo.ChartPoint;
import com.softimago.bcc.engine.bo.ChartType;
import com.softimago.bcc.engine.bo.RealChartCompilation;
import com.softimago.bcc.engine.bo.RealPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class BlockchainEngine
{
    private static final String URL_TO_CHART_API = "https://blockchain.info/charts/%s?format=json";
    private static final String DATE_FORMAT_X_AXIS = "dd-MM-yy";

    private List<ChartPoint> chartPoints;
    private float zoom;
    private float offset;

    private static String getHTTPRequest(String urlToRead) throws Exception
    {
        StringBuilder result = new StringBuilder();

        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null)
        {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    // make it testable by mock
    public static BlockchainEngine BlockchainEngineFactory(ChartType chartType) throws Exception
    {
        return BlockchainEngineFactory(URL_TO_CHART_API, chartType);
    }

    public static BlockchainEngine BlockchainEngineFactory(String urlTemplate, ChartType chartType) throws Exception
    {
        List<ChartPoint> list = null;

        String url = String.format(urlTemplate, chartType);

        // call GET json file
        String json = getHTTPRequest(url);

        JsonParser<Map<String, List<ChartPoint>>> parser = JsonParser.CHART_POINTS;

        Map<String, List<ChartPoint>> collection = parser.parse(json);

        if(collection != null && collection.get("values") != null)
        {
            list = collection.get("values");
        }

        return new BlockchainEngine(list);
    }


    public BlockchainEngine(List<ChartPoint> chp)
    {
        chartPoints = chp;
        zoom = 1.0f;
        offset = 0;

    }

    public List<ChartPoint> getChartPoints()
    {
        return chartPoints;
    }


    /**
     *
     * @param averageYLabelsCount - maximal number of labels for Y axis
     * @param averageXLabelsCount - maximal number of labels for X axis
     * @param maxSeed               - maximal seed for data
     * @return - compilation of all data needed to draw chart. All XY points are in range <0,1>
     *     Function takes data by maximum seed to optimize results. Minimum values count is 2 per axis.
     */
    public RealChartCompilation getChartCompilation(int averageYLabelsCount, int averageXLabelsCount, int maxSeed)
    {
        // check if nothing to do -> no data or not enough data
        if(chartPoints == null || chartPoints.size() < 2 || maxSeed < 2 || averageYLabelsCount < 2 || averageXLabelsCount < 2)
        {
            return null;
        }

        RealChartCompilation compilation = new RealChartCompilation();

        // ==== In calculation take under consideration zoom, offset and seed. ====

        // Collection is already sorted
        float minValue = Float.MAX_VALUE;
        float maxValue = Float.MIN_VALUE;
        int numberOfElements = chartPoints.size();
        float maxTime = (float)chartPoints.get(numberOfElements-1).date.getTime();      // Already sorted, no need to calculate max/min
        float minTime = (float)chartPoints.get(0).date.getTime();
        int clippedNumberOfElements;

        for(ChartPoint chartPoint : chartPoints)
        {
            minValue = Math.min(minValue, chartPoint.value);
            maxValue = Math.max(maxValue, chartPoint.value);
        }

        // Get data and calculate average in range <0,1>
        List<RealPoint> points = new ArrayList<>();
        float deltaValue = maxValue - minValue;
        float deltaTime = maxTime - minTime;
        for(ChartPoint chartPoint : chartPoints)
        {
            RealPoint rp = new RealPoint();
            rp.y = (chartPoint.value - minValue) / deltaValue;
            rp.x = (((float)chartPoint.date.getTime()) - minTime) / deltaTime;
            rp.original = chartPoint;

            points.add(rp);
        }


        // Clip by zoom (left 3 items as minimum)
        clippedNumberOfElements = (int)(((float)numberOfElements) * zoom);
        if(clippedNumberOfElements < 3)
            clippedNumberOfElements = 3;

        if(numberOfElements != clippedNumberOfElements)
        {
            // Use offset to shift
            //int offsetToShift = (int)((float)clippedNumberOfElements * offset);
            int offsetToShift = (int)offset;

            // Left at least three elements if offset is on very right
            if(numberOfElements - offsetToShift < 3)
                offsetToShift = numberOfElements - 3;

            int subStartIndex = offsetToShift;
            int subEndIndex = clippedNumberOfElements + offsetToShift - 1;

            if(subEndIndex >= points.size())
            {
                subEndIndex = points.size() - 1;
            }

            // Sublist creates a view only. For secure do strong copy after all.
            List<RealPoint> pointsZoomedView = new ArrayList<>(points.subList(subStartIndex, subEndIndex));

            // and again normalize results
            float minValueX = Float.MAX_VALUE;
            float maxValueX = Float.MIN_VALUE;
            float minValueY = Float.MAX_VALUE;
            float maxValueY = Float.MIN_VALUE;

            for(RealPoint rp : pointsZoomedView)
            {
                minValueX = Math.min(minValueX, rp.x);
                maxValueX = Math.max(maxValueX, rp.x);
                minValueY = Math.min(minValueY, rp.y);
                maxValueY = Math.max(maxValueY, rp.y);
            }

            float deltaX = maxValueX - minValueX;
            float deltaY = maxValueY - minValueY;

            for(RealPoint rp : pointsZoomedView)
            {
                rp.x = (rp.x - minValueX) / deltaX;
                rp.y = (rp.y - minValueY) / deltaY;
            }

            points = pointsZoomedView;
            clippedNumberOfElements = points.size();
        }


        // Calculate how much needs to be removed by seed. Do approximation and remove data by interpolation.
        // That's optimization what helps speed up drawing process sacrificing precision.
        if(clippedNumberOfElements > maxSeed)
        {
            List<RealPoint> pointsOptimized = new ArrayList<>();

            // calculate delta for interpolation
            float deltaInterpolation = (float)clippedNumberOfElements / (float)(maxSeed - 1);

            for(int i = 0; i < maxSeed; i++)
            {
                int approximatedIndex = (int)((float)i * deltaInterpolation);
                if(approximatedIndex > 0)
                    approximatedIndex--;

                // Check range. If over range take the latest one (quantization)
                if(approximatedIndex < points.size())
                    pointsOptimized.add(points.get(approximatedIndex));
                else
                    pointsOptimized.add(points.get(points.size()));
            }
            compilation.points = pointsOptimized;
        }
        else
        {
            compilation.points = points;
        }

        // Define Y labels
        compilation.averageYLabeles = new ArrayList<>();
        minValue = Float.MAX_VALUE;
        maxValue = Float.MIN_VALUE;
        for(RealPoint rp: points)
        {
            minValue = Math.min(minValue, rp.original.value);
            maxValue = Math.max(maxValue, rp.original.value);
        }
        deltaValue = (maxValue - minValue)/(float)averageYLabelsCount;
        for(int i = 0; i < averageYLabelsCount; i++)
        {
            compilation.averageYLabeles.add(
                new DecimalFormat(".00").format((float)i * deltaValue + minValue)
            );
        }

        // Define X labels
        compilation.averageXLabels = new ArrayList<>();
        if(averageXLabelsCount < points.size())
        {
            // select 5 by approximation
            float deltaInterpolation = (float)points.size() / (float)(averageXLabelsCount-1);

            for(int i = 0; i < averageXLabelsCount; i++)
            {
                int approximatedIndex = Math.round((float) i * deltaInterpolation);
                if(approximatedIndex > 0)
                    approximatedIndex--;

                Date val = points.get(approximatedIndex).original.date;
                compilation.averageXLabels.add(
                        new AxisPoint(new SimpleDateFormat(DATE_FORMAT_X_AXIS).format(val), points.get(approximatedIndex).x)
                );
            }
        }
        else
        {
            // use what we have here
            for(RealPoint rp : points)
            {
                compilation.averageXLabels.add(
                        new AxisPoint(new SimpleDateFormat(DATE_FORMAT_X_AXIS).format(rp.original.date), rp.x)
                );
            }
        }

        return compilation;
    }


    public void setZoom(float zoomValue)
    {
        zoom = zoomValue;
    }

    private final static float SCROLL_FACTOR = 80;

    // offset is guarder progressively
    public float setOffset(float offsetValueAddition)
    {
        int clippedNumberOfElements = (int)((float)chartPoints.size() * zoom);

        int offsetToShift = (int)(SCROLL_FACTOR * offsetValueAddition); // * (-1));  do reverse movement if you want to.

        if(offset + offsetToShift < 0)
        {
            offset = 0;
            return offset;
        }

        // check if it's max
        if(offset + offsetToShift + clippedNumberOfElements > chartPoints.size())
            offset = chartPoints.size() - clippedNumberOfElements;
        else
            offset += offsetToShift;


        return offset;
    }
}
