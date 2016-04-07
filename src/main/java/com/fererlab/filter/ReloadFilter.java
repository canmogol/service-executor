package com.fererlab.filter;

import com.fererlab.Main;

import javax.servlet.*;
import java.io.IOException;

public class ReloadFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("ReloadFilter.doFilter");
        try {
            Main.interpreter.reload();
        } catch (Exception e) {
            e.printStackTrace();
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
