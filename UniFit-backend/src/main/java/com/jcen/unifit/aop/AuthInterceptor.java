package com.jcen.unifit.aop;

import com.jcen.unifit.annotation.AuthCheck;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.enums.UserRoleEnum;
import com.jcen.unifit.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        String[] mustRoles = authCheck.mustRoles();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(request);

        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null || UserRoleEnum.BAN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // Check single role (backward compatibility)
        if (mustRole != null && !mustRole.isEmpty()) {
            UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustRoleEnum == null) {
                return joinPoint.proceed();
            }
            if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        // Check multiple roles
        if (mustRoles != null && mustRoles.length > 0) {
            boolean hasRole = false;
            for (String role : mustRoles) {
                UserRoleEnum roleEnum = UserRoleEnum.getEnumByValue(role);
                if (roleEnum != null && roleEnum.equals(userRoleEnum)) {
                    hasRole = true;
                    break;
                }
            }
            if (!hasRole) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }

        return joinPoint.proceed();
    }
}
