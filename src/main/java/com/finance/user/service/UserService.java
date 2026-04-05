package com.finance.user.service;

import com.finance.model.FinResponse;
import com.finance.model.PagedRs;
import com.finance.user.model.*;

public interface UserService {


    FinResponse<UserRs> createUser(UserCreateRq createRq);


    FinResponse<UserRs> getUserById(Long id);


    FinResponse<PagedRs<UserSummaryRs>> getAllUsers(String query, int page, int size);


    FinResponse<UserRs> editUser(Long id, UserEditRq editRq);


    FinResponse<UserRs> updateUserStatus(Long id, UserStatusRq statusRq);


    FinResponse<Void> deleteUser(Long id);
}