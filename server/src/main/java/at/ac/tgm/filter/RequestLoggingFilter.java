package at.ac.tgm.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class RequestLoggingFilter implements Filter {
    
    private static final Logger requestLogger =
            LoggerFactory.getLogger("REQUEST_LOGGER");
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws java.io.IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        
        ContentCachingResponseWrapper wrapper =
                new ContentCachingResponseWrapper((HttpServletResponse) response);
        
        long start = System.currentTimeMillis();
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        
        try {
            MDC.put("clientIp", ip);
            
            chain.doFilter(request, response);
            
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            int status = wrapper.getStatus();
            
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("status", String.valueOf(status));
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                MDC.put("user", auth.getName());
            }
            
            StringBuffer requestURL = req.getRequestURL();
            String queryString = req.getQueryString();
            if (queryString != null) {
                requestURL.append('?').append(queryString);
            }
            
            requestLogger.info("{} {}", req.getMethod(), requestURL.toString());
            
            wrapper.copyBodyToResponse();  // important!
            MDC.clear();
        }
    }
}

