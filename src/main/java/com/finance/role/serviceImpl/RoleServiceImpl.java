package com.finance.role.serviceImpl;

import com.finance.core.constant.ErrorCodes;
import com.finance.core.utility.Utility;
import com.finance.model.FinResponse;
import com.finance.role.mapper.RoleEntityToRoleRsMapper;
import com.finance.role.model.RoleRs;
import com.finance.role.repository.RoleRepository;
import com.finance.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RoleServiceImpl — implements RoleService.
 *
 * This is intentionally simple: roles are reference data seeded in data.sql.
 * The service just reads and maps them — no create/edit/delete needed here.
 *
 * @Service → Spring registers this as a bean in the application context.
 * @RequiredArgsConstructor → Lombok generates constructor injection for final fields.
 * @Slf4j → injects a logger (log.debug, log.error, etc.)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public FinResponse<List<RoleRs>> getAllRoles() {
        log.debug("Fetching all active roles");

        try {
            List<RoleRs> roles = roleRepository.findAll()
                    .stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsActive()))
                    .map(RoleEntityToRoleRsMapper.INSTANCE)
                    .toList();

            return FinResponse.success("Roles fetched successfully", roles);

        } catch (Exception ex) {
            log.error("Error fetching roles: {}", ex.getMessage(), ex);
            FinResponse<List<RoleRs>> response = new FinResponse<>();
            response.addError(Utility.internalError("Failed to fetch roles"));
            return response;
        }
    }

    @Override
    public FinResponse<RoleRs> getRoleById(Long id) {
        log.debug("Fetching role by id={}", id);

        try {
            return roleRepository.findById(id)
                    .map(entity -> FinResponse.success(
                            "Role fetched successfully",
                            RoleEntityToRoleRsMapper.INSTANCE.apply(entity)))
                    .orElseGet(() -> {
                        FinResponse<RoleRs> response = new FinResponse<>();
                        response.addError(Utility.createError(
                                ErrorCodes.NOT_FOUND, "Role not found with id: " + id));
                        return response;
                    });

        } catch (Exception ex) {
            log.error("Error fetching role id={}: {}", id, ex.getMessage(), ex);
            FinResponse<RoleRs> response = new FinResponse<>();
            response.addError(Utility.internalError("Failed to fetch role"));
            return response;
        }
    }
}