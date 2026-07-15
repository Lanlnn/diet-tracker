package com.diettracker.security;

import com.diettracker.api.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {
    @Test
    void limitsApiRequestsAndReturnsStructuredRequestId() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(new ObjectMapper(), 1);
        AtomicInteger accepted = new AtomicInteger();

        MockHttpServletRequest first = request("request-1234");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(first, firstResponse, (request, response) -> accepted.incrementAndGet());

        MockHttpServletRequest second = request("request-5678");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(second, secondResponse, (request, response) -> accepted.incrementAndGet());

        assertThat(accepted).hasValue(1);
        assertThat(secondResponse.getStatus()).isEqualTo(429);
        assertThat(secondResponse.getHeader("Retry-After")).isEqualTo("60");
        assertThat(secondResponse.getContentAsString()).contains("RATE_LIMITED", "request-5678");
    }

    private MockHttpServletRequest request(String requestId) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/me");
        request.setRemoteAddr("192.0.2.1");
        request.setAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, requestId);
        return request;
    }
}
