package mohammed.payloadwatch.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UrlValidatorTest {

    @Test
    public void testSafeUrls() {
        assertTrue(UrlValidator.isSafe("https://www.google.com"));
        assertTrue(UrlValidator.isSafe("http://example.com/api/data"));
        assertTrue(UrlValidator.isSafe("https://api.github.com/users"));
    }

    @Test
    public void testUnsafeUrls_Localhost() {
        assertFalse(UrlValidator.isSafe("http://localhost"));
        assertFalse(UrlValidator.isSafe("https://127.0.0.1:8080"));
        assertFalse(UrlValidator.isSafe("http://[::1]/"));
    }

    @Test
    public void testUnsafeUrls_PrivateNetworks() {
        assertFalse(UrlValidator.isSafe("http://192.168.1.100/admin"));
        assertFalse(UrlValidator.isSafe("http://10.0.0.5/secret"));
        assertFalse(UrlValidator.isSafe("https://172.16.0.1/"));
    }

    @Test
    public void testUnsafeUrls_CloudMetadata() {
        assertFalse(UrlValidator.isSafe("http://169.254.169.254/latest/meta-data/"));
    }

    @Test
    public void testInvalidUrls() {
        assertFalse(UrlValidator.isSafe("ftp://server.com")); // Only http/https allowed
        assertFalse(UrlValidator.isSafe("file:///etc/passwd"));
        assertFalse(UrlValidator.isSafe("not_a_url"));
        assertFalse(UrlValidator.isSafe(""));
    }
}
