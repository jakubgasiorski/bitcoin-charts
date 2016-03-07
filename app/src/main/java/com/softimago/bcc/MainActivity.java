package com.softimago.bcc;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.softimago.bcc.R;
import com.softimago.bcc.controllers.ApplicationCommander;
import com.softimago.bcc.controls.ChartView;
import com.softimago.bcc.layout.ChartFragment;

public class MainActivity extends AppCompatActivity implements ChartFragment.OnLoginFragmentInteractionListener
{

    private FragmentManager _fragmentManager;
    private ApplicationCommander _appCommander;
    private ChartFragment _fragmentChart;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _appCommander = new ApplicationCommander();
        _fragmentManager = getSupportFragmentManager();

        _fragmentChart = new ChartFragment();

        _fragmentManager.beginTransaction().replace(R.id.content, _fragmentChart).commit();


    }

    @Override
    protected void onStart()
    {
        super.onStart();

    }


    @Override
    public void onBackPressed()
    {
        if (getFragmentManager().getBackStackEntryCount() > 1)
        {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public boolean isConnected()
    {
        return ApplicationCommander.isConnectedToInternet(this);
    }

    public void loadData() throws Exception
    {

        _appCommander.getDataForMarketPrice();
    }

    public void loadDataToChart(ChartView chartView)
    {
        chartView.setChartCompilationData(_appCommander.getCompilationForChart());
    }

    public void zoomDataInChart(ChartView chartView, float zoomFactor)
    {
        _appCommander.setZoom(zoomFactor);
        chartView.setChartCompilationData(_appCommander.getCompilationForChart());
    }

    public void scrollDataInChart(ChartView chartView, float scrollFactor)
    {
        _appCommander.setOffset(scrollFactor);
        chartView.setChartCompilationData(_appCommander.getCompilationForChart());
    }
}
