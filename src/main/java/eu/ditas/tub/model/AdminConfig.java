package eu.ditas.tub.model;

public class AdminConfig {
    private String url;
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "AdminConfig{" +
                "url='" + url + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
