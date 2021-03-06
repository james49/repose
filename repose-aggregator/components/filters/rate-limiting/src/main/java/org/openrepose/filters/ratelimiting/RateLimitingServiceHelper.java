package org.openrepose.filters.ratelimiting;

import org.openrepose.commons.utils.http.PowerApiHeader;
import org.openrepose.commons.utils.http.header.HeaderValue;
import org.openrepose.commons.utils.http.header.HeaderValueImpl;
import org.openrepose.commons.utils.http.media.MediaType;
import org.openrepose.commons.utils.http.media.MimeType;
import org.openrepose.commons.utils.servlet.http.MutableHttpServletRequest;
import org.openrepose.filters.ratelimiting.write.ActiveLimitsWriter;
import org.openrepose.filters.ratelimiting.write.CombinedLimitsWriter;
import org.openrepose.services.ratelimit.RateLimitingService;
import org.openrepose.services.ratelimit.config.RateLimitList;
import org.openrepose.services.ratelimit.exception.OverLimitException;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RateLimitingServiceHelper {

   private final RateLimitingService service;
   private final ActiveLimitsWriter activeLimitsWriter;
   private final CombinedLimitsWriter combinedLimitsWriter;

   public RateLimitingServiceHelper(RateLimitingService service, ActiveLimitsWriter activeLimitsWriter, CombinedLimitsWriter combinedLimitsWriter) {
      this.service = service;
      this.activeLimitsWriter = activeLimitsWriter;
      this.combinedLimitsWriter = combinedLimitsWriter;
   }

   public MimeType queryActiveLimits(HttpServletRequest request, MediaType preferredMediaType, OutputStream outputStream) {
      RateLimitList rateLimits = service.queryLimits(getPreferredUser(request), getPreferredGroups(request));
      javax.ws.rs.core.MediaType mediaType = activeLimitsWriter.write(rateLimits, getJavaMediaType(preferredMediaType.getMimeType()), outputStream);

      return getReposeMimeType(mediaType);
   }

   public MimeType queryCombinedLimits(HttpServletRequest request, MediaType preferredMediaType, InputStream absoluteLimits, OutputStream outputStream) {
      RateLimitList rateLimits = service.queryLimits(getPreferredUser(request), getPreferredGroups(request));
      javax.ws.rs.core.MediaType mediaType = combinedLimitsWriter.write(rateLimits, getJavaMediaType(preferredMediaType.getMimeType()), absoluteLimits, outputStream);

      return getReposeMimeType(mediaType);
   }

   public void trackLimits(HttpServletRequest request, int datastoreWarnLimit) throws OverLimitException {
      service.trackLimits(getPreferredUser(request), getPreferredGroups(request), decodeURI(request.getRequestURI()), request.getParameterMap(), request.getMethod(), datastoreWarnLimit);
   }

   public MimeType getReposeMimeType(javax.ws.rs.core.MediaType mediaType) {
      return MimeType.guessMediaTypeFromString(mediaType.toString());
   }

   public javax.ws.rs.core.MediaType getJavaMediaType(MimeType reposeMimeType) {
      return new javax.ws.rs.core.MediaType(reposeMimeType.getType(), reposeMimeType.getSubType());
   }

   public String getPreferredUser(HttpServletRequest request) {
      final MutableHttpServletRequest mutableRequest = MutableHttpServletRequest.wrap(request);
      final HeaderValue userNameHeaderValue = mutableRequest.getPreferredHeader(PowerApiHeader.USER.toString(), new HeaderValueImpl(""));

      return userNameHeaderValue.getValue();
   }

   public List<String> getPreferredGroups(HttpServletRequest request) {
      final MutableHttpServletRequest mutableRequest = MutableHttpServletRequest.wrap(request);
      final List<? extends HeaderValue> userGroup = mutableRequest.getPreferredHeaderValues(PowerApiHeader.GROUPS.toString(), null);
      final List<String> groups = new ArrayList<String>();

      for (HeaderValue group : userGroup) {
         groups.add(group.getValue());
      }

      return groups;
   }

    private String decodeURI(String uri) {
        return URI.create(uri).getPath();
    }
}
