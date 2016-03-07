package com.softimago.bcc.engine.bo;

// Here you can add more chart types. So far only one is supported.
// Based on blockchain API -> https://blockchain.info/api/charts_api

public enum ChartType
{
    MARKET_PRICE
    {
        public String toString()
        {
            return "market-price";
        }
    }
}