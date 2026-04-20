package com.ledgerx.auth.security.filter;

import com.ledgerx.auth.config.AuthProps;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ClientBasicAuthFilter extends OncePerRequestFilter {

  private static final String BASIC_PREFIX = "Basic ";

  private final AuthProps authProps;

  public ClientBasicAuthFilter(AuthProps authProps) {
    this.authProps = authProps;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    AuthProps.BasicAuthClient cfg = authProps.basicAuthClient();
    return !(cfg.enabled() && cfg.hasCredentials());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    String authorization = req.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization == null || !authorization.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length())) {
      unauthorized(res);
      return;
    }

    String decoded;
    try {
      decoded =
          new String(
              Base64.getDecoder().decode(authorization.substring(BASIC_PREFIX.length()).trim()),
              StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      unauthorized(res);
      return;
    }

    int colon = decoded.indexOf(':');
    if (colon <= 0 || colon == decoded.length() - 1) {
      unauthorized(res);
      return;
    }

    String presentedId = decoded.substring(0, colon);
    String presentedSecret = decoded.substring(colon + 1);

    AuthProps.BasicAuthClient cfg = authProps.basicAuthClient();
    if (!timingSafeEquals(presentedId, cfg.clientId())
        || !timingSafeEquals(presentedSecret, cfg.clientSecret())) {
      unauthorized(res);
      return;
    }

    chain.doFilter(req, res);
  }

  private static void unauthorized(HttpServletResponse res) throws IOException {
    res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"LedgerX API\", charset=\"UTF-8\"");
    res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  static boolean timingSafeEquals(String presented, String expected) {
    if (presented == null || expected == null) {
      return false;
    }
    byte[] a = presented.getBytes(StandardCharsets.UTF_8);
    byte[] b = expected.getBytes(StandardCharsets.UTF_8);
    return MessageDigest.isEqual(a, b);
  }
}
