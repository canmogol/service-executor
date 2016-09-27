package com.fererlab.filter;

import com.fererlab.service.Reloader;

import javax.servlet.*;
import java.io.IOException;

public class ReloadFilter implements Filter {

    private final Reloader reloader;

    public ReloadFilter(Reloader reloader) {
        this.reloader = reloader;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            if (reloader != null) {
                reloader.reload();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}
