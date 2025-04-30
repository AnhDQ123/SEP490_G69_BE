package org.ffb_be.service.map;

import lombok.RequiredArgsConstructor;
import org.cloudinary.json.JSONArray;
import org.cloudinary.json.JSONObject;
import org.ffb_be.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
@RequiredArgsConstructor
@Service
@Transactional
public class MapService {

    @Value("${google.maps.api.key}")  // Lấy API Key từ application.properties
    private String apiKey;
    @Value("${routes.api.key}")  // Lấy API Key từ application.properties
    private String ROUTES_API_URL;
    private final RestTemplate restTemplate=new RestTemplate();



    public String getGeocode(String address) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address + "&key=" + apiKey;

        // Gửi yêu cầu HTTP GET và nhận kết quả JSON
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Trả về kết quả JSON
        return response.getBody();
    }

    public String getReverseGeocode(double lat, double lng) {
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&key=" + apiKey;

        // Gửi yêu cầu HTTP GET và nhận kết quả JSON
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Trả về kết quả JSON
        return response.getBody();
    }
    public String getRoute(String origin, String destination) {
        // Tạo URL với các tham số cần thiết
        String url = ROUTES_API_URL + "?origin=" + origin + "&destination=" + destination + "&key=" + apiKey;

        // Gửi yêu cầu GET đến API của Google Maps
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Trả về kết quả dưới dạng JSON (có thể phân tích dữ liệu theo nhu cầu)
        return response.getBody();
    }

    public double[] getLatLngFromAddress(String address) {
        try {
            JSONObject json = new JSONObject(getGeocode(address));
            JSONArray results = json.getJSONArray("results");
            JSONObject location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
            return new double[]{location.getDouble("lat"), location.getDouble("lng")};
        } catch (Exception e) {
            throw new BadRequestException("Không thể lấy toạ độ từ địa chỉ. Vui lòng kiểm tra lại.");
        }
    }
}
