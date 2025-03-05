package muon.app.updater;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.markusbernhardt.proxy.ProxySearch;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;

import java.net.ProxySelector;
import java.net.URL;

import static muon.app.util.Constants.API_UPDATE_URL;

@Slf4j
public class UpdateChecker {

    static {
        CertificateValidator.registerCertificateHook();
    }

    public static boolean isNewUpdateAvailable() {
        try {
            ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
            ProxySelector myProxySelector = proxySearch.getProxySelector();

            ProxySelector.setDefault(myProxySelector);

            log.info("Checking for url");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            VersionEntry latestRelease = objectMapper.readValue(new URL(API_UPDATE_URL).openStream(),
                                                                new TypeReference<>() {
                                                                });
            log.info("Latest release: {}", latestRelease);
            return latestRelease.compareTo(App.getContext().getVersion()) > 0;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }
}
