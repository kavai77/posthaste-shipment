package com.posthaste.shipment;

import com.google.firebase.database.FirebaseDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ShipmentApplicationTests {

	@MockitoBean
	FirebaseDatabase firebaseDatabase;

	@Test
	void contextLoads() {
	}

}
