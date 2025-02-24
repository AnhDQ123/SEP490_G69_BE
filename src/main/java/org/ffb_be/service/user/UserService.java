package org.ffb_be.service.user;

import org.ffb_be.dto.auth.userDto.UserCreateDTO;

import java.io.IOException;

public interface UserService {
    void create(UserCreateDTO employeeCreateDTO) throws IOException;

}

