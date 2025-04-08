package org.ffb_be.service.map;

import org.cloudinary.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Service
@Transactional
public class MapService {

    @Value("${mapbox.api.key}")
    private String API_KEY;

    private final String GEOCODING_URL = "https://api.mapbox.com/geocoding/v5/mapbox.places/%s.json?access_token=%s&country=VN";

    public double[] getCoordinates(String address) {
        try {
            // Xử lý encode địa chỉ
            String formattedAddress = address.replace(" ", "%20");
            String url = String.format(GEOCODING_URL, formattedAddress, API_KEY);

            // Gửi request
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);

            // Kiểm tra response
            if (response == null) {
                System.out.println("⚠️ Không nhận được phản hồi từ Mapbox.");
                return null;
            }

            // Parse JSON
            JSONObject json = new JSONObject(response);
            if (!json.has("features") || json.getJSONArray("features") == null) {
                System.out.println("⚠️ Không tìm thấy tọa độ cho địa chỉ: " + address);
                return null;
            }

            // Lấy tọa độ từ response
            JSONObject location = json.getJSONArray("features").getJSONObject(0);
            double lng = location.getJSONArray("center").getDouble(0);
            double lat = location.getJSONArray("center").getDouble(1);
            return new double[]{lat, lng};

        } catch (HttpClientErrorException e) {
            System.out.println("🚨 Lỗi HTTP: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            System.out.println("🚨 Lỗi kết nối API Mapbox: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("🚨 Lỗi không xác định: " + e.getMessage());
        }
        return null;
    }
}
