package pl.docmanager.web.controllers;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import pl.docmanager.Application;
import pl.docmanager.dao.UserRepository;
import pl.docmanager.domain.solution.SolutionBuilder;
import pl.docmanager.domain.user.UserBuilder;
import pl.docmanager.domain.solution.Solution;
import pl.docmanager.domain.user.User;
import pl.docmanager.web.security.AccessValidator;
import pl.docmanager.web.security.ApiTokenDecoder;
import pl.docmanager.web.security.JwtTokenGenerator;
import pl.docmanager.web.security.SecretKeeper;
import pl.docmanager.web.security.WebSecurity;

import java.util.Date;
import java.util.Optional;

import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {Application.class, WebSecurity.class})
public abstract class RestControllerTestBase {

    @MockBean
    protected UserRepository userRepository;
    @SpyBean
    protected AccessValidator accessValidator;
    @SpyBean
    protected ApiTokenDecoder apiTokenDecoder;
    @MockBean
    @Qualifier("userDetailsServiceImpl")
    protected UserDetailsService userDetailsService;

    protected String validToken;

    protected static final String USER_EMAIL = "user@example.com";

    @Before
    public void setup() {

        Solution solution1 = new SolutionBuilder(1).build();
        User user = new UserBuilder(1, solution1).withEmail(USER_EMAIL).build();

        given(userRepository.findByEmail(USER_EMAIL)).willReturn(Optional.of(user));

        validToken = JwtTokenGenerator.generateToken(USER_EMAIL,
                SecretKeeper.getInstance().getSecret(), new Date(System.currentTimeMillis() + 1000000000));
    }
}
