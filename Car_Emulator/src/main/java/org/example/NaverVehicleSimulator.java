package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.URI;
import java.net.http.*;
import org.json.*;

public class NaverVehicleSimulator {

    static final double MIN_DISTANCE_KM = 5.0;
    static final double MAX_DISTANCE_KM = 100.0;

    static final String tid = "A001";
    static final String mid = "6";
    static final String pv = "5";
    static final String did = "1";

    public static class CycleInfo {
        private String sec;  // 발생 시간 '초'
        private String gcd;  // GPS 상태 ('A': 정상)
        private String lat;  // 위도 * 1,000,000
        private String lon;  // 경도 * 1,000,000
        private String ang;  // 방향 (임의)
        private String spd;  // 속도
        private String sum;  // 누적 거리
        private String bat;  // 배터리 (임의)
    }

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
        double lat = 34.5 + rand.nextDouble() * (37.8 - 34.5);
        double lng = 126.5 + rand.nextDouble() * (129.3 - 126.5);
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

    static RouteResult getNaverRoute(String startX, String startY, String endX, String endY,
                                     String apiKeyId, String apiKey) throws Exception {
        String url = String.format("https://maps.apigw.ntruss.com/map-direction/v1/driving?start=%s,%s&goal=%s,%s",
                startX, startY, endX, endY);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-ncp-apigw-api-key-id", apiKeyId)
                .header("x-ncp-apigw-api-key", apiKey)
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject json = new JSONObject(response.body());
        JSONArray routes = json.getJSONObject("route").getJSONArray("traoptimal");
        JSONObject firstRoute = routes.getJSONObject(0);

        JSONArray pathArray = firstRoute.getJSONArray("path");
        List<LatLng> path = new ArrayList<>();

        for (int i = 0; i < pathArray.length(); i++) {
            JSONArray coord = pathArray.getJSONArray(i);
            double lng = coord.getDouble(0);
            double lat = coord.getDouble(1);
            path.add(new LatLng(lat, lng));
        }

        JSONObject summary = firstRoute.getJSONObject("summary");
        double totalDistance = summary.getDouble("distance");
        long totalTime = summary.getLong("duration") / 1000;

        return new RouteResult(path, totalDistance, totalTime);
    }

    static class VehicleSimulator implements Runnable {
        private final String apiKeyId;
        private final String apiKey;
        private final double speedKmph;

        private final List<CycleInfo> cList = new ArrayList<>();
        private int cCnt = 0;

        VehicleSimulator(String apiKeyId, String apiKey, double speedKmph) {
            this.apiKeyId = apiKeyId;
            this.apiKey = apiKey;
            this.speedKmph = speedKmph;
        }

        private void simulateVehicle(String mdn, RouteResult route) throws InterruptedException {
            List<LatLng> path = route.path;
            double totalDistanceMeters = 0.0;

            String startTime = getCurrentTimestamp();
            System.out.println(mdn + " 차량 시동 ON 시간: " + startTime);

            for (int i = 0; i < path.size(); i++) {
                LatLng point = path.get(i);

                CycleInfo cycle = new CycleInfo();
                cycle.sec = String.format("%02d", LocalDateTime.now().getSecond());
                cycle.gcd = "A";
                cycle.lat = String.valueOf((int)(point.lat * 1_000_000));
                cycle.lon = String.valueOf((int)(point.lng * 1_000_000));
                cycle.ang = String.valueOf(new Random().nextInt(360));
                cycle.spd = String.valueOf((int)speedKmph);
                cycle.sum = String.valueOf((int)totalDistanceMeters);
                cycle.bat = String.valueOf(120 + new Random().nextInt(10));
                cList.add(cycle);
                cCnt++;

                System.out.printf(mdn + " 차량 위치: (%.6f, %.6f) | 누적 주행 거리: %.2f km\n",
                        point.lat, point.lng, totalDistanceMeters / 1000.0);

                if (i < path.size() - 1) {
                    double dist = calculateDistance(point, path.get(i + 1));
                    totalDistanceMeters += dist;
                    long sleep = estimateSleepTime(dist, speedKmph);
                    Thread.sleep(sleep);
                }
            }

            String endTime = getCurrentTimestamp();
            System.out.println(mdn + " 차량 시동 OFF 시간: " + endTime);
            System.out.printf(mdn + " 총 주행 거리: %.2f km\n", route.totalDistanceMeters / 1000.0);
            System.out.printf("예상 소요 시간: %d분 %d초\n",
                    route.totalTimeSeconds / 60, route.totalTimeSeconds % 60);
            System.out.printf("Haversine 누적 거리 (근사값): %.2f km\n", totalDistanceMeters / 1000.0);
            System.out.println(mdn + " 기록된 CycleInfo 수: " + cList.size());
        }

        @Override
        public void run() {
            try {
                String mdn = generateMDN();
                LatLng[] points = generateValidStartAndEndPoints();
                LatLng start = points[0];
                LatLng end = points[1];

                System.out.printf("[%s] 차량 시작: (%.6f, %.6f) → (%.6f, %.6f)\n",
                        mdn, start.lat, start.lng, end.lat, end.lng);

                RouteResult route = getNaverRoute(
                        String.valueOf(start.lng), String.valueOf(start.lat),
                        String.valueOf(end.lng), String.valueOf(end.lat),
                        apiKeyId, apiKey);

                simulateVehicle(mdn, route);
            } catch (Exception e) {
                System.err.println("차량 시뮬레이션 중 에러 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String apiKeyId = "6a4zzn5ley";
        String apiKey = "0B0ipBqpWrfYelCQKPqpV9CF9Oyr0g6eHZPCeMNA";

        int numberOfVehicles = 5;
        double baseSpeed = 30.0;

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < numberOfVehicles; i++) {
            double speed = baseSpeed + (i * 3);
            VehicleSimulator simulator = new VehicleSimulator(apiKeyId, apiKey, speed);
            Thread thread = new Thread(simulator);
            threads.add(thread);
            thread.start();
        }

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
