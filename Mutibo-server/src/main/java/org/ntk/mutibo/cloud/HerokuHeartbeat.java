package org.ntk.mutibo.cloud;

import org.junit.Test;
import org.ntk.mutibo.api.MutiboApi;
import org.ntk.mutibo.client.SecuredRestBuilder;
import org.ntk.mutibo.client.UnsafeHttpsClient;

import retrofit.client.ApacheClient;

public class HerokuHeartbeat {

	private final String TEST_URL = "https://ntk4.herokuapp.com";

	private final String USERNAME1 = "admin";
	private final String PASSWORD = "pass";
	private final String CLIENT_ID = "mobile";

	private MutiboApi svc = new SecuredRestBuilder()
			.setClient(new ApacheClient(UnsafeHttpsClient.createUnsafeClient())).setEndpoint(TEST_URL)
			.setLoginEndpoint(TEST_URL + MutiboApi.TOKEN_PATH)
			// .setLogLevel(LogLevel.FULL)
			.setUsername(USERNAME1).setPassword(PASSWORD).setClientId(CLIENT_ID).build().create(MutiboApi.class);

	public static void main(String[] args) {

		new HerokuHeartbeat().heartbeat();

	}

	private void heartbeat() {
		boolean done = false;

		while (!done) {
			try {
				svc.getItemSet(1);

				Thread.sleep(30 * 60 * 1000);
			} catch (InterruptedException e) {
				System.out.println("Thread was interrupted. Ending the test case");
				done = true;
			} catch (Exception e) {
				System.out.println(String.format("Error occurred: %s. Ending the test case", e.getMessage()));
				done = true;
			}
		}
	}

	@Test
	public void testGetAdminUser() {

		heartbeat();
	}
}
