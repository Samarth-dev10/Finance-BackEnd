package com.finance.role.service;

import com.finance.model.FinResponse;
import com.finance.role.model.RoleRs;

import java.util.List;
public interface RoleService {


    FinResponse<List<RoleRs>> getAllRoles();

    FinResponse<RoleRs> getRoleById(Long id);
}