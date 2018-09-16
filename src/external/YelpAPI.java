package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class YelpAPI {
    //URL components: protocol://hostname:port/endpoint?query
    private static final String HOST = "https://api.yelp.com"; 
    private static final String ENDPOINT = "/v3/businesses/search";
    private static final String DEFAULT_TERM = "";  //keyword to do specific search
    private static final int SEARCH_LIMIT = 20;  // at most how many restaurants to be displayed
    //On yelp.com, it says we need to set Authorization HTTP header value as Bearer + API_KEY
    private static final String TOKEN_TYPE = "Bearer";
    //We need to add authorized key in header of the request to Yelp
    private static final String API_KEY = "843lMgKUEPG-ji03BNESPeY_qIFh65U0i9sGvxOhGSiuSgyLYpTJsgMgwRu0wPokbdDfvnfEhTTX5OK08ahVVixGIKOX_P-RdXt3def_O588F9YvsMW2GYIr1f2BW3Yx";
    
    //according to latitude and longitude, return a list of restaurants 
    public  List<Item> search(double lat, double lon, String term) {
        //if we didnt specify search keyword then just  use default one
        if (term == null || term.isEmpty()) {
            term = DEFAULT_TERM;
        }
        try {
            //Space -> "+" and all characters other than digits, alphabet, ".""-""*""_""-" 
            //are converted firstly into one or more bytes then each byte is converted into "%xy" where xy is two digit hexadecimal representation
            
            term = URLEncoder.encode(term, "UTF-8"); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        //we're concatenating complete URL that we need 
        String query = String.format("term=%s&latitude=%s&longitude=%s&limit=%s", term, lat, lon, SEARCH_LIMIT);
        String url = HOST + ENDPOINT + "?" + query;
        
        try {
            //open a connection but not sure what type of connection, so we need to cast to http because We know it must be a http connection
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection(); 
            connection.setRequestMethod("GET");  //Tell what HTTP method to use
            connection.setRequestProperty("Authorization", TOKEN_TYPE + " " + API_KEY);  //add authorized key in header of the request to Yelp
            int responseCode = connection.getResponseCode();  //Get the status code from an HTTP response message. It will send the request first and then get response code. If code is 200, OK,

            System.out.println("Sending Request to URL: " + url);
            System.out.println("Response Code: " + responseCode);
            if (responseCode != 200) {
                return new ArrayList();
            }
            //Create a BufferedReader to help read text from a character-input stream. Provide for the efficient reading of characters, arrays, and lines.
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine = "";
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            JSONObject obj = new JSONObject(response.toString());  //convert string to JSONObject
            if (!obj.isNull("businesses")) {
                return getItemList(obj.getJSONArray("businesses"));
            }
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

    }
    /**
     * Helper methods
     */
    // Convert JSONArray to a list of item objects using user builder pattern to filter out the information we need
    private List<Item> getItemList(JSONArray restaurants) throws JSONException {
        List<Item> list = new ArrayList<>();
        
        for (int i = 0; i < restaurants.length(); ++i) {
            JSONObject restaurant = restaurants.getJSONObject(i);
            
            ItemBuilder builder = new ItemBuilder();
            if(!restaurant.isNull("id")) {
                builder.setItemId(restaurant.getString("id"));
            }
            if(!restaurant.isNull("name")) {
                builder.setName(restaurant.getString("name"));
            }
            if(!restaurant.isNull("url")) {
                builder.setUrl(restaurant.getString("url"));
            }
            if(!restaurant.isNull("image_url")) {
                builder.setImageUrl(restaurant.getString("image_url"));
            }
            if(!restaurant.isNull("rating")) {
                builder.setRating(restaurant.getDouble("rating"));
            }
            if(!restaurant.isNull("distance")) {
                builder.setDistance(restaurant.getDouble("distance"));
            }
            
            builder.setAddress(getAddress(restaurant));
            builder.setCategories(getCategories(restaurant));
            
            list.add(builder.build());
        }

        return list;
    }
    private Set<String> getCategories(JSONObject restaurant) throws JSONException {
        Set<String> categories = new HashSet<>();
        if (!restaurant.isNull("categories")) {
            JSONArray array = restaurant.getJSONArray("categories");
            for (int i = 0; i < array.length(); i++) {
                JSONObject category = array.getJSONObject(i);
                if (!category.isNull("alias")) {
                    categories.add(category.getString("alias"));
                }
            }
        }
        return categories;
    }

    private String getAddress(JSONObject restaurant) throws JSONException {
        String address = "";
        if (!restaurant.isNull("location")) {
            JSONObject location = restaurant.getJSONObject("location");
            if (!location.isNull("display_address")) {
                JSONArray array = location.getJSONArray("display_address");
                address = array.join(",");  //concatenate strings by a commas between two strings
            }
        }
        return address;

    }


    //used by ourselves to debug search results 
    private void queryAPI(double lat, double lon) {
        List<Item> itemList = search(lat, lon, null);
        for (Item item : itemList) {
            JSONObject jsonObject = item.toJSONObject();
            System.out.println(jsonObject);
        }
    }
    // Main entry for sample Yelp API requests.
   public static void main(String[] args) {
       YelpAPI tmApi = new YelpAPI();
       tmApi.queryAPI(37.38, -122.08);
   }


}


