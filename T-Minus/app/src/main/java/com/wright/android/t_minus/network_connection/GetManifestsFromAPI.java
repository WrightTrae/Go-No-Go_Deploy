package com.wright.android.t_minus.network_connection;

import android.os.AsyncTask;

import com.wright.android.t_minus.objects.LaunchPad;
import com.wright.android.t_minus.objects.Manifest;
import com.wright.android.t_minus.objects.PadLocation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class GetManifestsFromAPI extends AsyncTask<String, Void, ArrayList<Manifest>> {
    final private OnFinished mFinishedInterface;
    final private int offset;
    final private int limit;


    public interface OnFinished {
        void onManifestFinished(ArrayList<Manifest> _manifests);
    }

    public GetManifestsFromAPI(OnFinished _finished, int _offset, int _limit) {
        mFinishedInterface = _finished;
        offset =_offset;
        limit = _limit;
    }

    @Override
    protected ArrayList<Manifest> doInBackground(String... _params) {
        return parseJSON(NetworkUtils.getNetworkData("https://launchlibrary.net/1.4/launch?mode=verbose&" +
                "offset="+offset+
                "&limit="+limit+
                "&startdate="+_params[0]+
                "&enddate="+_params[1]));
    }

    private ArrayList<Manifest> parseJSON(String api){
        ArrayList<Manifest> ManifestArrayList = new ArrayList<>();
        try {
            JSONObject response = new JSONObject(api);
            JSONArray hitsJson = response.getJSONArray("launches");
            for (int i = 0; i < hitsJson.length(); i++) {
                JSONObject obj = hitsJson.getJSONObject(i);
                int id = obj.getInt("id");
                String title = obj.getString("name");
                String time = obj.getString("net");

                time = time.replace("UTC","");
                SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault());
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = df.parse(time);
                df = new SimpleDateFormat("MMM dd, yyyy hh:mm a",Locale.getDefault());
                df.setTimeZone(TimeZone.getDefault());
                String formattedDate = df.format(date);

                JSONObject locationObj = obj.getJSONObject("location");
                String location  = locationObj.getString("name");
                int locationId  = locationObj.getInt("id");
                JSONObject rocketObj = obj.getJSONObject("rocket");
                String imageURL = rocketObj.getString("imageURL");
                if(imageURL.equals("https://s3.amazonaws.com/launchlibrary/RocketImages/placeholder_1920.png")){
                    imageURL = null;
                }

                String agencyURL = null;
                String agencyName = null;

                if(!rocketObj.isNull("agencies")) {
                    JSONArray agencyArray = rocketObj.getJSONArray("agencies");
                    if (agencyArray.length() > 0) {
                        agencyURL = agencyArray.getJSONObject(0).getString("infoURL");
                        agencyName = agencyArray.getJSONObject(0).getString("name");
                    }
                }

                JSONArray padsArrayJSON = locationObj.getJSONArray("pads");
                ArrayList<LaunchPad> launchPads = null;
                for(int j = 0; j < padsArrayJSON.length(); j++){
                    if(launchPads == null){
                       launchPads = new ArrayList<>();
                    }
                    JSONObject padObj = padsArrayJSON.getJSONObject(j);
                    int padId = padObj.getInt("id");
                    String padName = padObj.getString("name");
                    Double padLat = padObj.getDouble("latitude");
                    Double padLong = padObj.getDouble("longitude");
                    launchPads.add(new LaunchPad(padId, padName,padLat,padLong,locationId));
                }
                ManifestArrayList.add(new Manifest(id,title,formattedDate,imageURL, agencyName, agencyURL,
                        new PadLocation(locationId,location,launchPads)));
            }
            return ManifestArrayList;
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }
        return ManifestArrayList;
    }

    @Override
    protected void onPostExecute(ArrayList<Manifest> _result) {
        super.onPostExecute(_result);
        // Update the UI
        if (_result != null) {
            mFinishedInterface.onManifestFinished(_result);
        }
    }
}