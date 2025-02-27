package muon.app.updater;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;

import javax.net.ssl.*;
import javax.swing.*;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Slf4j
public class CertificateValidator {
    public static synchronized void registerCertificateHook() {
        SSLContext sslContext;
        try {
            try {
                sslContext = SSLContext.getInstance("TLS");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                sslContext = SSLContext.getInstance("SSL");
            }

            TrustManager[] trustAllCerts = new TrustManager[]{new X509ExtendedTrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {


                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    try {
                        for (X509Certificate cert : chain) {
                            cert.checkValidity();
                        }
                    } catch (CertificateException e) {
                        log.error(e.getMessage(), e);
                        if (!confirmCert()) {
                            throw e;
                        }
                    }
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {

                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
                        throws CertificateException {


                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                        throws CertificateException {


                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
                        throws CertificateException {
                    try {
                        for (X509Certificate cert : chain) {
                            cert.checkValidity();
                        }
                    } catch (CertificateException e) {
                        log.error(e.getMessage(), e);
                        if (!confirmCert()) {
                            throw e;
                        }
                    }
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                        throws CertificateException {
                    try {
                        for (X509Certificate cert : chain) {
                            cert.checkValidity();
                        }
                    } catch (CertificateException e) {
                        log.error(e.getMessage(), e);
                        if (!confirmCert()) {
                            throw e;
                        }
                    }
                }
            }};
            sslContext.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static boolean confirmCert() {
        return JOptionPane.showConfirmDialog(null, App.bundle.getString("update_check")) == JOptionPane.YES_OPTION;
    }
}
