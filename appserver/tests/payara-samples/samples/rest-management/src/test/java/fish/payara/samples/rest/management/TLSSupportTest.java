package fish.payara.samples.rest.management;

import com.sun.enterprise.util.JDK;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TLSSupportTest extends RestManagementTest {
    private final List<String> SUPPORTED_SSL_PROTOCOLS = Arrays.asList("TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3");

    @Before
    public void before() {
        Assume.assumeTrue(JDK.isTls13Supported() || JDK.isOpenJSSEFlagRequired());
    }

    @Test
    public void when_socket_ssl_protocols_returned_expect_supported_tls_versions() throws IOException {
        SocketFactory factory = SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 8181);
        List<String> sslProtocols = Arrays.asList(socket.getSSLParameters().getProtocols());

        assertEquals(SUPPORTED_SSL_PROTOCOLS, sslProtocols);
        assertTrue(Arrays.asList(socket.getSupportedProtocols()).contains("TLSv1.3"));
    }
}
