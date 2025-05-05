package org.ffb_be.config.security.security;

import lombok.RequiredArgsConstructor;
import org.ffb_be.entity.User;
import org.ffb_be.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(()-> new UsernameNotFoundException("User not found!"));
    if (user.getStatus().equals("INACTIVE")) {
        throw new UsernameNotFoundException("User has been deactivated!");
    }
        return new UserSecurity(user);
    }


}
