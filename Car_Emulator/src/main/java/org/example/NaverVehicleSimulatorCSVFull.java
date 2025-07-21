package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.net.URI;
import java.net.http.*;
import org.json.*;

public class NaverVehicleSimulatorCSVFull {

    // 경로 유효성 검사용 최소/최대 거리 (KM 단위)
    static final double MIN_DISTANCE_KM = 5.0;
    static final double MAX_DISTANCE_KM = 100.0;

    // CSV 파일 한 줄에 해당하는 차량 주기 정보 클래스
    public static class CycleInfo {
        String mdn;      // 차량 ID
        String tid = "A001";  // 단말 타입 ID
        String mid = "6";     // 모델 ID
        String pv = "5";      // 펌웨어 버전
        String did = "1";     // 데이터 ID
        int cCnt;             // 사이클 카운트
        String timestamp;     // 타임스탬프
        String sec;           // 초
        String gcd = "A";           // GPS 상태 코드
        String lat;           // 위도 (1/1000000 단위)
        String lon;           // 경도 (1/1000000 단위)
        String ang;           // 각도
        String spd;           // 속도 (km/h)
        String sum;           // 누적 거리 (m)
        String bat;           // 배터리 상태

        @Override
        public String toString() {
            // CSV 포맷에 맞게 문자열로 변환
            return String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    mdn, tid, mid, pv, did, cCnt, timestamp, sec, 
                     gcd, lat, lon, ang, spd, sum, bat);
        }
    }

    // 위도, 경도 좌표 클래스
    static class LatLng {
        double lat, lng;
        LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        @Override
        public String toString() {
            return String.format("LatLng(lat=%.6f, lng=%.6f)", lat, lng);
        }

    }

    //네이버 API주는 경로들 사이의 각도 -> 이게 차의 방량이 된다
    static double calculateBearing(LatLng from, LatLng to) {
        double lat1 = Math.toRadians(from.lat);
        double lat2 = Math.toRadians(to.lat);
        double dLng = Math.toRadians(to.lng - from.lng);

        double y = Math.sin(dLng) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2)
                - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360; // 0~360도 값으로 정규화
    }


    // 경로 결과: 좌표 리스트, 총 거리(m), 총 소요 시간(s)
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

    // 대한민국 내 랜덤 좌표 생성
    static LatLng generateRandomLatLngInKorea() {
        Random rand = new Random();
        double lat = 34.5 + rand.nextDouble() * (37.8 - 34.5);
        double lng = 126.5 + rand.nextDouble() * (129.3 - 126.5);
        return new LatLng(lat, lng);
    }

    // 최소/최대 거리 범위에 맞는 출발/도착 지점 생성
    static LatLng[] generateValidStartAndEndPoints() {
        LatLng start, end;
        double distanceKm;

        while (true) {
            start = generateRandomLatLngInKorea();
            end = generateRandomLatLngInKorea();
            distanceKm = calculateDistance(start, end) / 1000.0;
            if (distanceKm >= MIN_DISTANCE_KM && distanceKm <= MAX_DISTANCE_KM) break;
        }

        return new LatLng[]{start, end};
    }

    // 차량 고유 번호 생성 (EMU + 8자리 숫자)
    static String generateMDN() {
        String prefix = "EMU";
        StringBuilder numberPart = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < 8; i++) numberPart.append(rand.nextInt(10));
        return prefix + numberPart;
    }

    // 두 좌표 간 거리 계산 (Haversine 공식)
    static double calculateDistance(LatLng p1, LatLng p2) {
        double R = 6371000; // 지구 반지름 (m)
        double dLat = Math.toRadians(p2.lat - p1.lat);
        double dLng = Math.toRadians(p2.lng - p1.lng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(p1.lat)) * Math.cos(Math.toRadians(p2.lat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // 네이버 길찾기 API 호출 (출발~도착 좌표에 대해 경로 및 거리 정보 획득)
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

    // 차량 1대를 시뮬레이션하는 클래스 (Runnable 구현)
    static class VehicleSimulator implements Runnable {
        private final String apiKeyId;
        private final String apiKey;
        private final List<Double> speedHistory = new ArrayList<>(); // 이전 주기의 속도 저장

        VehicleSimulator(String apiKeyId, String apiKey) {
            this.apiKeyId = apiKeyId;
            this.apiKey = apiKey;
        }

        // 기존 simulateVehicleWithVariableSpeed()를 아래 코드로 교체
        private void simulateVehicleWithVariableSpeed(String mdn, RouteResult route) throws InterruptedException {
            List<LatLng> path = route.path;
            List<Double> segmentDistances = new ArrayList<>();
            List<Double> accumulatedDistances = new ArrayList<>();
            accumulatedDistances.add(0.0);
        
            // 각 경로 구간별 거리 계산 (누적 거리 리스트 생성)
            for (int i = 0; i < path.size() - 1; i++) {
                double dist = calculateDistance(path.get(i), path.get(i + 1));
                segmentDistances.add(dist);
                accumulatedDistances.add(accumulatedDistances.get(i) + dist);
            }
        
            int elapsedSeconds = 0;
            int cycleCount = 1;
            int batteryLevel = 135;
            double totalDistance = 0.0; // 누적 이동 거리 (m)
            String filename = mdn + ".csv";
            boolean fileExists = new File(filename).exists();
        
            try (FileWriter fw = new FileWriter(filename, true)) {
                if (!fileExists) {
                    fw.write("mdn,tid,mid,pv,did,cCnt,timestamp,sec,gcd,lat,lon,ang,spd,sum,bat\n");
                }
        
                LocalDateTime startTime = LocalDateTime.now();
                Random random = new Random();
        
                while (true) {
                    // 이번 1분 동안의 시작/끝 속도 (20~50km/h)
                    double startSpeedKmph = 20 + random.nextDouble() * 30;
                    double endSpeedKmph = 20 + random.nextDouble() * 30;
                    double deltaSpeedKmph = (endSpeedKmph - startSpeedKmph) / 60.0; // 초당 속도 변화량 (선형 보간)
        
                    List<CycleInfo> batchData = new ArrayList<>();
        
                    for (int sec = 0; sec < 60; sec++) {
                        double currentSpeedKmph = startSpeedKmph + deltaSpeedKmph * sec;
                        double currentSpeedMps = currentSpeedKmph * 1000 / 3600.0;
        
                        int totalSec = elapsedSeconds + sec;
                        totalDistance += currentSpeedMps * 1.0; // v * Δt (Δt = 1초)
        
                        CycleInfo cycle = new CycleInfo();
                        cycle.mdn = mdn;
                        cycle.cCnt = cycleCount;
                        cycle.timestamp = startTime.plusSeconds(totalSec)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        cycle.sec = String.format("%02d", sec);
        
                        if (totalDistance >= accumulatedDistances.get(accumulatedDistances.size() - 1)) {
                            // 도착 지점 기록
                            cycle.gcd = "V";
                            LatLng dest = path.get(path.size() - 1);
                            cycle.lat = String.valueOf((int) (dest.lat * 1_000_000));
                            cycle.lon = String.valueOf((int) (dest.lng * 1_000_000));
                            cycle.spd = "0";
                            double finalDistance = accumulatedDistances.get(accumulatedDistances.size() - 1);
                            cycle.sum = String.valueOf((int) finalDistance);
                            cycle.bat = String.valueOf(batteryLevel);
                            cycle.ang = "0";
                            batchData.add(cycle);
                            break;
                        } else {
                            // 현재 위치 보간
                            int segmentIndex = 0;
                            while (segmentIndex < accumulatedDistances.size() - 1 &&
                                   accumulatedDistances.get(segmentIndex + 1) < totalDistance) {
                                segmentIndex++;
                            }
        
                            double segmentStartDist = accumulatedDistances.get(segmentIndex);
                            double segmentLength = segmentDistances.get(segmentIndex);
                            double ratio = (totalDistance - segmentStartDist) / segmentLength;
        
                            LatLng start = path.get(segmentIndex);
                            LatLng end = path.get(segmentIndex + 1);
        
                            double lat = start.lat + (end.lat - start.lat) * ratio;
                            double lng = start.lng + (end.lng - start.lng) * ratio;
        
                            cycle.lat = String.valueOf((int) (lat * 1_000_000));
                            cycle.lon = String.valueOf((int) (lng * 1_000_000));
                            cycle.spd = String.valueOf((int) currentSpeedKmph);
                            cycle.sum = String.valueOf((int) totalDistance);
                            cycle.bat = String.valueOf(batteryLevel);
                            cycle.ang = String.valueOf((int) calculateBearing(start, end));
                            batchData.add(cycle);
                        }
        
                        // 30분마다 배터리 1씩 감소
                        if (totalSec % 1800 == 0 && totalSec != 0) {
                            batteryLevel = Math.max(0, batteryLevel - 1);
                        }
                    }
        
                    // CSV로 기록
                    for (CycleInfo ci : batchData) {
                        fw.write(ci.toString() + "\n");
                    }
                    fw.flush();
        
                    elapsedSeconds += 60;
                    cycleCount++;
        
                    if (totalDistance >= accumulatedDistances.get(accumulatedDistances.size() - 1)) {
                        break; // 경로 끝
                    }
        
                    Thread.sleep(10); // 실제 시뮬레이션에선 60000 (1분)
                }
        
            } catch (IOException e) {
                System.err.println("CSV 파일 저장 실패: " + e.getMessage());
            }
        }


        @Override
        public void run() {
            try {
                String mdn = generateMDN();
                LatLng[] points = generateValidStartAndEndPoints();
                RouteResult route = getNaverRoute(
                        String.valueOf(points[0].lng), String.valueOf(points[0].lat),
                        String.valueOf(points[1].lng), String.valueOf(points[1].lat),
                        apiKeyId, apiKey);
                simulateVehicleWithVariableSpeed(mdn, route);
            } catch (Exception e) {
                System.err.println("차량 시뮬레이션 중 에러 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // 네이버 API 키
        Dotenv dotenv = Dotenv.load();  // .env 파일 로드
        String apiKeyId = dotenv.get("NAVER_API_KEY_ID");
        String apiKey = dotenv.get("NAVER_API_KEY");

        int numberOfVehicles = 1; // 시뮬레이션할 차량 수
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < numberOfVehicles; i++) {
            VehicleSimulator simulator = new VehicleSimulator(apiKeyId, apiKey);
            Thread thread = new Thread(simulator);
            threads.add(thread);
            thread.start();
        }

        // 모든 차량 쓰레드 종료 대기
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("쓰레드 조인 실패");
            }
        }
    }
}
