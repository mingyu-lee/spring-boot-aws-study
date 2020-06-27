package me.hoonmaro.study.springboot.config.auth;

import lombok.RequiredArgsConstructor;
import me.hoonmaro.study.springboot.domain.user.Role;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/css/**", "/images/**", "/js/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.
                csrf().disable()
                .headers().frameOptions().disable()
            .and()
                .authorizeRequests()
                .antMatchers("/", "/he-console/**").permitAll()
                .antMatchers("/api/v1/**").hasRole(Role.USER.name())
                .anyRequest().authenticated()
            .and()
                .logout()
                    .logoutSuccessUrl("/")
            .and()
                .oauth2Login()
                    .userInfoEndpoint()
                        .userService(customOAuth2UserService);
    }
}
