/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Assertions;

public final class FlakyTestSupport {

	private static final int FAILURE_PERCENTAGE = Integer.getInteger("petclinic.flaky.failurePercentage", 35);

	private FlakyTestSupport() {
	}

	public static void failRandomly(String testName) {
		int roll = ThreadLocalRandom.current().nextInt(100);
		if (roll < FAILURE_PERCENTAGE) {
			Assertions.fail("Intentional TeamCity flaky-test demo failure in " + testName + " (roll " + roll
					+ " < failure percentage " + FAILURE_PERCENTAGE
					+ "). Set -Dpetclinic.flaky.failurePercentage=0 to disable.");
		}
	}

}
