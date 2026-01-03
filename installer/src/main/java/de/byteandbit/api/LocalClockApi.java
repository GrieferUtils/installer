package de.byteandbit.api;

import de.byteandbit.Constants;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;

/**
 * Api for checking the system clock
 */
public class LocalClockApi {

	public static boolean is_clock_in_sync() {
		try {
			NTPUDPClient client = new NTPUDPClient();
			client.setDefaultTimeout(3000);

			InetAddress address = InetAddress.getByName(Constants.CLOCK_NTP_SERVER);
			TimeInfo info = client.getTime(address);
			info.computeDetails();

			long offset = info.getOffset(); // Difference in ms between local and NTP

			return Math.abs(offset) < Constants.CLOCK_ACCEPTABLE_DEVIATION_MS;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not reach NTP server.");
			return true;
		}
	}

}
