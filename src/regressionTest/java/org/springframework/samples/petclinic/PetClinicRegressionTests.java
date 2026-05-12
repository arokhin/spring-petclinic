package org.springframework.samples.petclinic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Regression tests for the Pet Clinic application.
 * Ensures previously working features continue to work correctly.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PetClinicRegressionTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testWelcomePageReturnsCorrectView() throws Exception {
		mockMvc.perform(get("/"))
			.andExpect(status().isOk())
			.andExpect(view().name("welcome"));
	}

	@Test
	void testVetsPageAccessible() throws Exception {
		mockMvc.perform(get("/vets.html"))
			.andExpect(status().isOk());
	}

	@Test
	void testOwnersFindPageAccessible() throws Exception {
		mockMvc.perform(get("/owners/find"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testVetsJsonApiAccessible() throws Exception {
		// Test that the JSON API is accessible
		mockMvc.perform(get("/vets"))
			.andExpect(status().isOk());
	}
}
