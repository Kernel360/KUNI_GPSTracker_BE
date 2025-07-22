package org.example;

import java.net.URI;
import java.net.http.*;
import java.util.*;
import org.json.*;

public class TmapVehicleSimulator {

    static class LatLng {
        double lat, lng;
        LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    static double calculateDistance(LatLng p1, LatLng p2) {
        double R = 6371000;
        double dLat = Math.toRadians(p2.lat - p1.lat);
        double dLng = Math.toRadians(p2.lng - p1.lng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(p1.lat)) * Math.cos(Math.toRadians(p2.lat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    static long estimateSleepTime(double distMeters, double speedKmph) {
        double speedMps = (speedKmph * 1000) / 3600.0;
        return (long)((distMeters / speedMps) * 1000);
    }

    static List<LatLng> getTmapRoute(String startX, String startY, String endX, String endY, String appKey) throws Exception {
        String url = "https://apis.openapi.sk.com/tmap/routes?version=1&format=json";

        JSONObject body = new JSONObject();
        body.put("startX", startX);
        body.put("startY", startY);
        body.put("endX", endX);
        body.put("endY", endY);
        body.put("reqCoordType", "WGS84GEO");
        body.put("resCoordType", "WGS84GEO");
        body.put("searchOption", 0);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("appKey", appKey)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("[DEBUG] 응답코드: " + response.statusCode());
        System.out.println("[DEBUG] 응답본문:\n" + response.body());

        JSONObject json = new JSONObject(response.body());
        JSONArray features = json.getJSONArray("features");

        List<LatLng> path = new ArrayList<>();
        for (int i = 0; i < features.length(); i++) {
            JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");
            if (geometry.getString("type").equals("LineString")) {
                JSONArray coords = geometry.getJSONArray("coordinates");
                for (int j = 0; j < coords.length(); j++) {
                    JSONArray coord = coords.getJSONArray(j);
                    double lng = coord.getDouble(0);
                    double lat = coord.getDouble(1);
                    path.add(new LatLng(lat, lng));
                }
            }
        }

        return path;
    }

    static void simulateVehicle(List<LatLng> path, double speedKmph) throws InterruptedException {
        for (int i = 0; i < path.size(); i++) {
            LatLng point = path.get(i);
            System.out.printf("차량 위치: (%.6f, %.6f)\n", point.lat, point.lng);

            if (i < path.size() - 1) {
                double dist = calculateDistance(point, path.get(i + 1));
                long sleep = estimateSleepTime(dist, speedKmph);
                Thread.sleep(sleep);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String startX = "126.9780";  // longitude
        String startY = "37.5665";   // latitude
        String endX = "126.9900";
        String endY = "37.5700";
        String appKey = System.getenv("APP_KEY"); //실제 발급된 AppKey 넣기

        List<LatLng> path = getTmapRoute(startX, startY, endX, endY, appKey);
        simulateVehicle(path, 30.0); //설정된 속도 이동 시뮬레이션

    }
}
