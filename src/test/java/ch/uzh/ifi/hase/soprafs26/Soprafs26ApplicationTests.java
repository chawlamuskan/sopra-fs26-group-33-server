package ch.uzh.ifi.hase.soprafs26;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ch.uzh.ifi.hase.soprafs26.service.GeocodingService;

@SpringBootTest
class Soprafs26ApplicationTests {

	@MockitoBean GeocodingService geocodingService;
	
	@Test
	void contextLoads() {
	}

}