package com.sysgears.user.resolvers.password;

import com.sysgears.authentication.model.jwt.JwtUserIdentity;
import com.sysgears.authentication.model.jwt.Tokens;
import com.sysgears.authentication.service.jwt.JwtGenerator;
import com.sysgears.user.config.JWTPreAuthenticationToken;
import com.sysgears.user.dto.AuthPayload;
import com.sysgears.user.dto.UserPayload;
import com.sysgears.user.dto.input.ForgotPasswordInput;
import com.sysgears.user.dto.input.LoginUserInput;
import com.sysgears.user.dto.input.RegisterUserInput;
import com.sysgears.user.dto.input.ResetPasswordInput;
import com.sysgears.user.exception.PasswordInvalidException;
import com.sysgears.user.exception.UserNotFoundException;
import com.sysgears.user.model.User;
import com.sysgears.user.repository.UserRepository;
import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtResolver implements GraphQLMutationResolver {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtGenerator jwtGenerator;

    @Transactional(readOnly = true)
    public CompletableFuture<AuthPayload> login(LoginUserInput loginUserInput) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<User> userOpt = userRepository.findByUsernameOrAndEmail(loginUserInput.getUsernameOrEmail());
            if (userOpt.isEmpty()) {
                throw new UserNotFoundException();
            }

            User user = userOpt.get();
            boolean matches = passwordEncoder.matches(loginUserInput.getPassword(), user.getPassword());
            if (!matches) {
                log.debug("Password is invalid");
                throw new PasswordInvalidException();
            }

            JwtUserIdentity jwtUserIdentity = new JwtUserIdentity(
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole(),
                    user.getIsActive(),
                    user.getEmail(),
                    user.getProfile() == null ? null : user.getProfile().getFirstName(),
                    user.getProfile() == null ? null : user.getProfile().getLastName()
            );
            Tokens tokens = jwtGenerator.generateTokens(jwtUserIdentity);
            SecurityContextHolder.getContext().setAuthentication(new JWTPreAuthenticationToken(user, null));

            return new AuthPayload(user, tokens);
        });
    }

    public CompletableFuture<String> forgotPassword(ForgotPasswordInput input) {
        //todo implement
        return null;
    }

    public CompletableFuture<String> resetPassword(ResetPasswordInput input) {
        //todo implement
        return null;
    }

    public CompletableFuture<UserPayload> register(RegisterUserInput input) {
        //todo implement
        return null;
    }
}
