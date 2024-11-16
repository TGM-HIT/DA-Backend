package at.ac.tgm.config;

import at.ac.tgm.UserRoles;
import at.ac.tgm.ad.entry.UserEntry;
import at.ac.tgm.ad.service.UserService;
import at.ac.tgm.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Filter for authorizing requests based on the "Authorization: Bearer ..." header. When
 * a valid JWT has been found, load the user details from the database and set the
 * authenticated principal for the duration of this request.
 */
//@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final UserService jwtUserDetailsService;
    //private final UserDetailsService userDetailsService;
    private final JwtTokenService jwtTokenService;

    //@Autowired
    public JwtRequestFilter(final UserService jwtUserDetailsService,
                            /*final UserDetailsService userDetailsService,*/
                            final JwtTokenService jwtTokenService) {
        this.jwtUserDetailsService = jwtUserDetailsService;
        //this.userDetailsService = userDetailsService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
            final HttpServletResponse response, final FilterChain chain) throws IOException,
            ServletException {
        // look for Bearer auth header
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        final String token = header.substring(7);
        final String username = jwtTokenService.validateTokenAndGetUsername(token);
        if (username == null) {
            // validation failed or token expired
            chain.doFilter(request, response);
            return;
        }

        final UserEntry userDetails;
        try {
            
            userDetails = jwtUserDetailsService.findByUserPrincipalName(username).orElseThrow();
        } catch (final NoSuchElementException userNotFoundEx) {
            // user not found
            chain.doFilter(request, response);
            return;
        }

        final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, List.of(new SimpleGrantedAuthority(UserRoles.getRoleFromDn(userDetails.getDistinguishedName()))));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        // set user details on spring security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // continue with authenticated user
        chain.doFilter(request, response);
    }

}
