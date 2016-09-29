package io.application.sample.ejb;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class AuthenticationEJBService {

    @Inject
    private AuthenticationEJBDAO authenticationEJBDAO;

    public boolean authenticate(String username, String password) {
        return authenticationEJBDAO.authenticate(username, password);
    }


}
