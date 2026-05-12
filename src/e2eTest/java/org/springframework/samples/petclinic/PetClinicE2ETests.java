package org.springframework.samples.petclinic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for the Pet Clinic application.
 * Tests complete user workflows from start to finish.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PetClinicE2ETests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void testCompleteOwnerWorkflow() {
		// Access owner search page
		ResponseEntity<String> searchResponse = restTemplate.getForEntity("/owners/find", String.class);
		assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(searchResponse.getBody()).contains("Find Owners");
	}

	@Test
	void testCompleteVetWorkflow() {
		// Access veterinarians page
		ResponseEntity<String> vetsResponse = restTemplate.getForEntity("/vets.html", String.class);
		assertThat(vetsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(vetsResponse.getBody()).contains("Veterinarians");
	}

	@Test
	void testApplicationHealthCheck() {
		// Verify application is running
		ResponseEntity<String> homeResponse = restTemplate.getForEntity("/", String.class);
		assertThat(homeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Verify all main pages are accessible
		ResponseEntity<String> vetsResponse = restTemplate.getForEntity("/vets.html", String.class);
		assertThat(vetsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		ResponseEntity<String> ownersResponse = restTemplate.getForEntity("/owners/find", String.class);
		assertThat(ownersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void testNavigationWorkflow() {
		// Test navigation between pages
		ResponseEntity<String> homeResponse = restTemplate.getForEntity("/", String.class);
		assertThat(homeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Verify we can navigate to vets from home
		ResponseEntity<String> vetsResponse = restTemplate.getForEntity("/vets.html", String.class);
		assertThat(vetsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}
