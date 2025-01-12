
/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.net.ConnectException;

import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;

public class ServerUtils {

	private static final String SERVER = "http://localhost:8080/";

	/**
	 * This method validates the title with server if it is or
	 * is not duplicate.
	 * @param collectionId - id of the collection that is the note
	 *                        associated with
	 * @param newTitle - the title to be checked
	 * @return true if it is a duplicate, false if it is not
	 * @throws IOException when it returns something else then 200/409 code
	 */
	public boolean validateTitleWithServer(
			final Long collectionId, final String newTitle)
			throws IOException {
		String endpoint = "api/notes/validate-title";

		Response response = ClientBuilder.newClient()
				.target(SERVER)
				.path(endpoint)  // Endpoint path
				.queryParam("title", newTitle)
				.request()
				.get();

		if (response.getStatus() == 409) {
			return true;
		} else if (response.getStatus() == 200) {
			return false;
		} else {
			throw new IOException("Error: " + response.getStatus());
		}
	}

	public boolean isServerAvailable() {
		try {
			ClientBuilder.newClient(new ClientConfig()) //
					.target(SERVER) //
					.request(APPLICATION_JSON) //
					.get();
		} catch (ProcessingException e) {
			if (e.getCause() instanceof ConnectException) {
				return false;
			}
		}
		return true;
	}
}