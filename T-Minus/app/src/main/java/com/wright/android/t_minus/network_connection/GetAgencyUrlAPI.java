package com.wright.android.t_minus.network_connection;

import android.os.AsyncTask;

import com.wright.android.t_minus.objects.Manifest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class GetAgencyUrlAPI extends AsyncTask<String, Void, ArrayList<Manifest>> {
    final private OnFinished mFinishedInterface;
    private final ArrayList<Manifest> manifestArrayList;


    public interface OnFinished {
        void onAgencyFinished(ArrayList<Manifest> _url);
    }

    public GetAgencyUrlAPI(OnFinished _finished, ArrayList<Manifest> manifests) {
        mFinishedInterface = _finished;
        manifestArrayList = manifests;
    }

    @Override
    protected ArrayList<Manifest> doInBackground(String... _params) {
        for (Manifest manifest:manifestArrayList) {
            if(manifest.getImageUrl() == null) {
                manifest.setAgencyURL(parseJSON(NetworkUtils.getNetworkData("https://autocomplete.clearbit.com/v1/companies/suggest?query="
                        + manifest.getAgencyName())));
            }
        }
        return manifestArrayList;
    }

    private String parseJSON(String api){
        try {
            JSONArray response = new JSONArray(api);
            if (response.length()>0){
                return response.getJSONObject(0).getString("logo");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(ArrayList<Manifest> _result) {
        super.onPostExecute(_result);
        // Update the UI
        if (_result != null) {
            mFinishedInterface.onAgencyFinished(_result);
        }
    }
}
