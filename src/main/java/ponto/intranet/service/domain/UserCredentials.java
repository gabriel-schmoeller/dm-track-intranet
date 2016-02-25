package ponto.intranet.service.domain;

/**
 * @author gabriel.schmoeller
 */
public class UserCredentials {

    private String user;
    private String pass;

    public UserCredentials() {
    }

    public UserCredentials(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }
}
