package com.softimago.bcc.controllers;

import android.content.Context;
import android.net.ConnectivityManager;

import com.softimago.bcc.engine.BlockchainEngine;
import com.softimago.bcc.engine.bo.ChartType;
import com.softimago.bcc.engine.bo.RealChartCompilation;

public class ApplicationCommander
{

    // these values can be as a part of properties (depends how control is going to be used)
    private static final int CHART_ITEMS_ON_X_AXIS = 5;
    private static final int CHART_ITEMS_ON_Y_AXIS = 6;
    private static final int CHART_SEED = 400;                  // 400 works fine for note 3 however it needs performance tests to get this value real for slow machines (lower seed == faster rendering)


    private BlockchainEngine _blockchainEngine;

    public void getDataForMarketPrice() throws Exception
    {
        _blockchainEngine = BlockchainEngine.BlockchainEngineFactory(ChartType.MARKET_PRICE);
    }

    public void setZoom(float zoom)
    {
        if(_blockchainEngine != null)
            _blockchainEngine.setZoom(zoom);

    }

    public void setOffset(float offset)
    {
        if(_blockchainEngine != null)
            _blockchainEngine.setOffset(offset);
    }

    public RealChartCompilation getCompilationForChart()
    {
        if(_blockchainEngine == null)
            return null;

        return _blockchainEngine.getChartCompilation(CHART_ITEMS_ON_Y_AXIS, CHART_ITEMS_ON_X_AXIS, CHART_SEED);
    }

    public static boolean isConnectedToInternet(Context _context)
    {
        ConnectivityManager cm = (ConnectivityManager)_context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.getActiveNetworkInfo() == null);
    }

}
