/*
 * (C) Copyright 2018 Daniel Braun (http://www.daniel-braun.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package net.torabipour.ChatHubBot.model.utils.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Java library for reverse geocoding using Nominatim
 *
 * @author Daniel Braun
 * @version 0.2
 *
 */
public class NominatimReverseGeocodingJAPI {

    private final String NominatimInstance = "https://nominatim.openstreetmap.org";

    private int zoomLevel = 18;

    public NominatimReverseGeocodingJAPI() {
    }

    public NominatimReverseGeocodingJAPI(int zoomLevel) {
        if (zoomLevel < 0 || zoomLevel > 18) {
            System.err.println("invalid zoom level, using default value");
            zoomLevel = 18;
        }

        this.zoomLevel = zoomLevel;
    }

    public Address getAdress(double lat, double lon) {
        Address result = null;
        String urlString = NominatimInstance + "/reverse?format=json&addressdetails=1&lat=" + String.valueOf(lat) + "&lon=" + String.valueOf(lon) + "&zoom=" + zoomLevel;
        try {
            result = new Address(getJSON(urlString), zoomLevel);
        } catch (IOException e) {
            System.err.println("Can't connect to server.");
            e.printStackTrace();
        }
        return result;
    }

    private String getJSON(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.addRequestProperty("User-Agent", "Mozilla/4.76");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String text;
        StringBuilder result = new StringBuilder();
        while ((text = in.readLine()) != null) {
            result.append(text);
        }

        in.close();
        return result.toString();
    }
}
