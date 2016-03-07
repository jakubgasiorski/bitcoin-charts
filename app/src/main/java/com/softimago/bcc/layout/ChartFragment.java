package com.softimago.bcc.layout;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softimago.bcc.R;
import com.softimago.bcc.controls.ChartView;


public class ChartFragment extends Fragment implements ScreenScaleGesture.OnScaleListener
{

    public interface OnLoginFragmentInteractionListener
    {
        boolean isConnected();
        void loadData() throws Exception;
        void loadDataToChart(ChartView chartView);
        void zoomDataInChart(ChartView chartView, float zoomFactor);
        void scrollDataInChart(ChartView chartView, float scrollFactor);
    }

    private View _view;
    private Context _context;

    private OnLoginFragmentInteractionListener _listener;
    private ChartView _chartView;


    @Override
    public void onStart()
    {
        super.onStart();

        new LoadDataProcessing().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        _view = inflater.inflate(R.layout.fragment_chart, container, false);

        _chartView = (ChartView)_view.findViewById(R.id.chartView);

        _chartView.setOnTouchListener(new ScreenScaleGesture(_context, this));

        // Inflate the layout for this fragment
        return _view;
    }


    @TargetApi(23)
    @Override public void onAttach(Context context)
    {
        //This method avoid to call super.onAttach(context) if I'm not using api 23 or more
        //if (Build.VERSION.SDK_INT >= 23) {
        super.onAttach(context);
        onAttachToContext(context);
        //}
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            onAttachToContext(activity);
        }
    }

    /*
     * This method will be called from one of the two previous method
     */
    protected void onAttachToContext(Context context)
    {
        _context = context;

        if (context instanceof OnLoginFragmentInteractionListener)
        {
            _listener = (OnLoginFragmentInteractionListener) context;

        } else
        {
            throw new RuntimeException(context.toString() + " must implement OnLoginFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        _listener = null;
    }

    public void onZoom(float scaleFactor)
    {
        _listener.zoomDataInChart(_chartView, scaleFactor);
        _chartView.invalidate();
    }

    public void onSwipe(float distance)
    {
        _listener.scrollDataInChart(_chartView, _chartView.calculateScrollFactor(distance));
        _chartView.invalidate();
    }

    private class LoadDataProcessing extends AsyncTask<String, Void, Boolean>
    {
        private ProgressDialog _ringProgressDialog;

        @Override
        protected Boolean doInBackground(String... params)
        {
            try
            {
                _listener.loadData();
            }
            catch (Exception ex)
            {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            _ringProgressDialog.dismiss();
            if(!result)
            {
                String message;
                if(_listener.isConnected())
                {
                    message = "Cannot load data";
                }
                else
                {
                    message = "No internet connection. Please run WiFi or 3G.";
                }
                // show alert and don't do anything
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(_context);
                dlgAlert.setMessage(message);
                dlgAlert.setTitle("Error");
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(false);
                dlgAlert.create().show();
            }
            else
            {
                _listener.loadDataToChart(_chartView);
                _chartView.invalidate();
            }
        }

        @Override
        protected void onPreExecute()
        {
            _ringProgressDialog = ProgressDialog.show(_context, "Please wait ...", "Loading data in progress.", true);
            _ringProgressDialog.setCancelable(true);
        }

        @Override
        protected void onProgressUpdate(Void... values)
        {
        }
    }

}
