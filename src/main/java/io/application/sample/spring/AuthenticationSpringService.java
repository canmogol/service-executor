package io.application.sample.spring;

public class AuthenticationSpringService {

    private AuthenticationSpringDAO authenticationSpringDAO;

    public boolean authenticate(String username, String password) {
        return authenticationSpringDAO.authenticate(username, password);
    }

    public void setAuthenticationSpringDAO(AuthenticationSpringDAO authenticationSpringDAO) {
        this.authenticationSpringDAO = authenticationSpringDAO;
    }
}
