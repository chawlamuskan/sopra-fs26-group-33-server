package ch.uzh.ifi.hase.soprafs26.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeocodingService {

    @Value("${GOOGLE_MAPS_API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public String resolveCityFromAddress(String address) {
        try {
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
                + URLEncoder.encode(address, StandardCharsets.UTF_8)
                + "&key=" + apiKey
                + "&language=en";

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) return null;

            List<Map<String, Object>> components =
                (List<Map<String, Object>>) results.get(0).get("address_components");

            return components.stream()
                .filter(c -> ((List<String>) c.get("types")).contains("locality"))
                .map(c -> ((String) c.get("long_name")).toLowerCase())
                .findFirst()
                .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}