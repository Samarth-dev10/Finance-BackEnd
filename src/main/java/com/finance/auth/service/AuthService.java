package com.finance.auth.service;

import com.finance.auth.model.LoginRq;
import com.finance.auth.model.LoginRs;
import com.finance.model.FinResponse;
import com.finance.user.model.UserRs;


public interface AuthService {


    FinResponse<LoginRs> login(LoginRq loginRq);


    FinResponse<UserRs> getCurrentUserProfile();
}