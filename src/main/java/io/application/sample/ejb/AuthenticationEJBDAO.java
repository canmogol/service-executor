package io.application.sample.ejb;


import javax.inject.Named;

@Named
public class AuthenticationEJBDAO {
    public boolean authenticate(String username, String password) {
        return true;
    }
}
