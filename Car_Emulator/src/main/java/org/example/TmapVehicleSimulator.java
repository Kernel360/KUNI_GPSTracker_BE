package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import org.json.*;

public class TmapVehicleSimulator {

    static final double MIN_DISTANCE_KM = 5.0;
    static final double MAX_DISTANCE_KM = 100.0;

    static class LatLng {
        double lat, lng;
        LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    static class RouteResult {
        List<LatLng> path;
        double totalDistanceMeters;
        long totalTimeSeconds;

        RouteResult(List<LatLng> path, double totalDistanceMeters, long totalTimeSeconds) {
            this.path = path;
            this.totalDistanceMeters = totalDistanceMeters;
            this.totalTimeSeconds = totalTimeSeconds;
        }
    }

    static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    static LatLng generateRandomLatLngInKorea() {
        Random rand = new Random();
        // 서울~부산 기준 내륙 범위로 제한
        double lat = 34.5 + rand.nextDouble() * (37.8 - 34.5);   // 위도
        double lng = 126.5 + rand.nextDouble() * (129.3 - 126.5); // 경도
        return new LatLng(lat, lng);
    }

    static LatLng[] generateValidStartAndEndPoints() {
        Random rand = new Random();
        LatLng start, end;
        double distanceKm;

        while (true) {
            start = generateRandomLatLngInKorea();
            end = generateRandomLatLngInKorea();
            distanceKm = calculateDistance(start, end) / 1000.0;

            if (distanceKm >= MIN_DISTANCE_KM && distanceKm <= MAX_DISTANCE_KM) {
                break;
            }
        }

        return new LatLng[]{start, end};
    }

    static String generateMDN() {
        String prefix = "EMU";
        StringBuilder numberPart = new StringBuilder();
        Random rand = new Random();

        for (int i = 0; i < 8; i++) {
            numberPart.append(rand.nextInt(10));
        }

        return prefix + numberPart.toString();
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

    static RouteResult getTmapRoute(String startX, String startY, String endX, String endY, String appKey) throws Exception {
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

        JSONObject json = new JSONObject(response.body());
        JSONArray features = json.getJSONArray("features");

        List<LatLng> path = new ArrayList<>();
        double totalDistanceMeters = 0.0;
        long totalTimeSeconds = 0;

        for (int i = 0; i < features.length(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject geometry = feature.getJSONObject("geometry");
            JSONObject properties = feature.getJSONObject("properties");

            if (geometry.getString("type").equals("LineString")) {
                JSONArray coords = geometry.getJSONArray("coordinates");
                for (int j = 0; j < coords.length(); j++) {
                    JSONArray coord = coords.getJSONArray(j);
                    double lng = coord.getDouble(0);
                    double lat = coord.getDouble(1);
                    path.add(new LatLng(lat, lng));
                }
            }

            if (totalDistanceMeters == 0.0 && properties.has("totalDistance")) {
                totalDistanceMeters = properties.getDouble("totalDistance");
            }

            if (totalTimeSeconds == 0 && properties.has("totalTime")) {
                totalTimeSeconds = properties.getLong("totalTime");
            }
        }

        return new RouteResult(path, totalDistanceMeters, totalTimeSeconds);
    }

    static void simulateVehicle(RouteResult route, double speedKmph) throws InterruptedException {
        List<LatLng> path = route.path;
        double totalDistanceMeters = 0.0;

        String startTime = getCurrentTimestamp();
        System.out.println("차량 시동 ON 시간: " + startTime);

        for (int i = 0; i < path.size(); i++) {
            LatLng point = path.get(i);
            System.out.printf("차량 위치: (%.6f, %.6f) | 누적 주행 거리: %.2f km\n",
                    point.lat, point.lng, totalDistanceMeters / 1000.0);

            if (i < path.size() - 1) {
                double dist = calculateDistance(point, path.get(i + 1));
                totalDistanceMeters += dist;

                long sleep = estimateSleepTime(dist, speedKmph);
                Thread.sleep(sleep);
            }
        }

        String endTime = getCurrentTimestamp();
        System.out.println("차량 시동 OFF 시간: " + endTime);

        System.out.printf("TMap 제공 총 주행 거리: %.2f km\n", route.totalDistanceMeters / 1000.0);
        System.out.printf("TMap 예상 소요 시간: %d분 %d초\n",
                route.totalTimeSeconds / 60, route.totalTimeSeconds % 60);
        System.out.printf("Haversine 누적 거리 (근사값): %.2f km\n", totalDistanceMeters / 1000.0);
    }

    static class VehicleSimulator implements Runnable {
        private final String appKey;
        private final double speedKmph;

        VehicleSimulator(String appKey, double speedKmph) {
            this.appKey = appKey;
            this.speedKmph = speedKmph;
        }

        @Override
        public void run() {
            try {
                String mdn = generateMDN();
                LatLng[] points = generateValidStartAndEndPoints();
                LatLng start = points[0];
                LatLng end = points[1];

                String startX = String.valueOf(start.lng);
                String startY = String.valueOf(start.lat);
                String endX = String.valueOf(end.lng);
                String endY = String.valueOf(end.lat);

                System.out.printf("[%s] 차량 시작: (%.6f, %.6f) → (%.6f, %.6f)\n",
                        mdn, start.lat, start.lng, end.lat, end.lng);

                RouteResult route = getTmapRoute(startX, startY, endX, endY, appKey);
                simulateVehicle(route, speedKmph);

            } catch (Exception e) {
                System.err.println("차량 시뮬레이션 중 에러 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String appKey = "4w9GfHF0St6BKhXWcOeZV6O3jXGWgP5E9UU7LBf7"; // 실제 발급받은 키 사용

        int numberOfVehicles = 5; // 차량 수 설정
        double baseSpeed = 30.0;

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < numberOfVehicles; i++) {
            double speed = baseSpeed + (i * 3); // 속도 차이 부여
            VehicleSimulator simulator = new VehicleSimulator(appKey, speed);
            Thread thread = new Thread(simulator);
            threads.add(thread);
            thread.start();
        }

        // 모든 차량이 종료될 때까지 기다림
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("쓰레드 조인 실패");
            }
        }

        System.out.println("모든 차량 시뮬레이션 완료");
    }
}
