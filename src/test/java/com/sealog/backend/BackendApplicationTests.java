package com.sealog.backend;

import com.sealog.backend.support.AbstractContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests extends AbstractContainerTest {

	@Test
	void contextLoads() {
	}

}
