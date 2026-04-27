package mohammed.payloadwatch.config;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

public class UrlValidator {

    public static boolean isSafe(String urlString) {
        try {
            URI uri = new URI(urlString);
            String host = uri.getHost();

            if (host == null) {
                return false;
            }

            // Only allow http and https
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }

            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress address : addresses) {
                if (address.isLoopbackAddress() ||
                    address.isAnyLocalAddress() ||
                    address.isLinkLocalAddress() ||
                    address.isSiteLocalAddress() ||
                    isReservedAddress(address)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isReservedAddress(InetAddress address) {
        byte[] addr = address.getAddress();
        if (addr.length == 4) { // IPv4
            // Check for 169.254.0.0/16 (Link-local, often used for cloud metadata)
            if ((addr[0] & 0xFF) == 169 && (addr[1] & 0xFF) == 254) {
                return true;
            }
            // Check for 100.64.0.0/10 (Carrier-grade NAT)
            if ((addr[0] & 0xFF) == 100 && (addr[1] & 0xC0) == 64) {
                return true;
            }
            // Check for 192.0.0.0/24 (IETF Protocol Assignments)
            if ((addr[0] & 0xFF) == 192 && (addr[1] & 0xFF) == 0 && (addr[2] & 0xFF) == 0) {
                return true;
            }
        }
        return false;
    }
}
