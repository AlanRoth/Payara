package fish.payara.samples.rest.management;

import com.sun.enterprise.util.JDK;
import fish.payara.samples.ServerOperations;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TLSSupportTest extends RestManagementTest {
    private final List<String> SUPPORTED_SSL_PROTOCOLS = Arrays.asList("TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3");

    @Before
    public void before() {
        Assume.assumeTrue(JDK.isTls13Supported() || JDK.isOpenJSSEFlagRequired());
    }

    @Test
    @Ignore
    public void when_socket_ssl_protocols_returned_expect_supported_tls_versions() throws IOException {
        SocketFactory factory = SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 8181);
        List<String> sslProtocols = Arrays.asList(socket.getSSLParameters().getProtocols());

        assertEquals(SUPPORTED_SSL_PROTOCOLS, sslProtocols);
        assertTrue(Arrays.asList(socket.getSupportedProtocols()).contains("TLSv1.3"));
    }

    @Test
    public void when_tls13_is_supported_expect_tls13_used_by_default() throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InterruptedException {
        SocketFactory factory = SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 8181);

        addSelfSignedCertificate();

        socket.startHandshake();
        String protocol = socket.getSession().getProtocol();
        socket.close();
        assertEquals("TLSv1.3", protocol);
    }

    private void addSelfSignedCertificate() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        X509Certificate cert = ServerOperations.createSelfSignedCertificate(keyPair);

        assertNotNull("Failed generating SelfSignedCertificate for test", cert);
        ServerOperations.addCertificateToContainerTrustStore(cert);
    }
}
