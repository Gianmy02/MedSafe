package it.unisa.project.medsafe;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Disabilitato per i test unitari - richiede configurazione Azure completa")
class MedsafeApplicationTests {

	@Test
	void contextLoads() {
	}

}
