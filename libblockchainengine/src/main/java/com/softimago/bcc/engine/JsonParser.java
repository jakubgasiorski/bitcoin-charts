package com.softimago.bcc.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.softimago.bcc.engine.bo.ChartPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Date;
import java.util.Map;

public class JsonParser<T>
{
    public static final JsonParser<Map<String, List<ChartPoint>>> CHART_POINTS = new JsonParser<>(new TypeToken<Map<String, List<ChartPoint>>>(){});


    private final TypeToken<T> typeToken;
    private final Gson gson;


    public JsonParser(TypeToken<T> type)
    {
        this.typeToken = type;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(ChartPoint.class, new ChartPointAdapter())
                .create();

    }


    public T parse(String jsonString)
    {
        return gson.fromJson(jsonString, typeToken.getType());
    }

    public T parse(InputStream inputStream)
    {
        Reader inputStreamReader = new InputStreamReader(inputStream);
        Reader streamReader = new BufferedReader(inputStreamReader);

        return gson.fromJson(streamReader, typeToken.getType());
    }


    private class ChartPointAdapter extends TypeAdapter<ChartPoint>
    {
        private static final String VALUE_X = "x";
        private static final String VALUE_Y = "y";

        @Override
        public void write(JsonWriter out, ChartPoint value) throws IOException
        {
            throw new UnsupportedOperationException("Writing is not supported.");
        }

        @Override
        public ChartPoint read(JsonReader in) throws IOException
        {
            Date date = null;
            Float value = null;
            in.beginObject();
            while(in.hasNext())
            {
                String name = in.nextName();
                if(name.equals(VALUE_X))
                {
                    Integer dateVal = gson.fromJson(in, Integer.class);
                    date = new Date((long)dateVal*1000);

                }
                else if(name.equals(VALUE_Y))
                {
                    value = gson.fromJson(in, Float.class);
                }
                else
                {
                    in.skipValue();
                }
            }
            in.endObject();

            return new ChartPoint(date, value);
        }
    }


}
