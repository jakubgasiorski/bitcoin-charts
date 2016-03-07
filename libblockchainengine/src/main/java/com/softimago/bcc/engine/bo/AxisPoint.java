package com.softimago.bcc.engine.bo;

public class AxisPoint
{
    public String valueToShow;
    public float normalizedPosition;

    public AxisPoint(String value, float position)
    {
        valueToShow = value;
        normalizedPosition = position;
    }
}
