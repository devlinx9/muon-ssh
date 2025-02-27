package muon.app.ssh;

public interface CachedCredentialProvider {
    String getCachedPassword();

    void cachePassword(String password);

    String getCachedPassPhrase();

    void setCachedPassPhrase(String cachedPassPhrase);

    String getCachedUser();

    void setCachedUser(String user);
}
