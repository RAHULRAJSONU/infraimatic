package com.ezyinfra.product.configuration;

import org.modelmapper.ModelMapper;
import org.modelmapper.record.RecordModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper().registerModule(new RecordModule());
    }

//    @Bean
//    public FilterRegistrationBean<OncePerRequestFilter> contentCachingFilter() {
//        FilterRegistrationBean<OncePerRequestFilter> bean =
//                new FilterRegistrationBean<>();
//
//        bean.setFilter(new OncePerRequestFilter() {
//            @Override
//            protected void doFilterInternal(
//                    HttpServletRequest request,
//                    HttpServletResponse response,
//                    FilterChain filterChain)
//                    throws ServletException, IOException {
//
//                filterChain.doFilter(
//                        new ContentCachingRequestWrapper(request),
//                        response
//                );
//            }
//        });
//
//        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
//        return bean;
//    }

}