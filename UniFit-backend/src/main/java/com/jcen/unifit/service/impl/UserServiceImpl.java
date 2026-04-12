package com.jcen.unifit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.constant.UserConstant;
import com.jcen.unifit.exception.BusinessException;
import com.jcen.unifit.mapper.UserMapper;
import com.jcen.unifit.model.dto.user.UserQueryRequest;
import com.jcen.unifit.model.entity.User;
import com.jcen.unifit.model.vo.LoginUserVO;
import com.jcen.unifit.model.vo.UserVO;
import com.jcen.unifit.service.UserService;
import com.jcen.unifit.service.WeChatService;
import com.jcen.unifit.utils.JwtTokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String SALT = "unifit@2026";
    private static final BigDecimal DEFAULT_RECHARGE_AMOUNT = BigDecimal.valueOf(100);

    @Resource
    private JwtTokenUtils jwtTokenUtils;

    @Resource
    private WeChatService weChatService;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userPhone, String userName) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4 || userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码长度不合法");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }

        QueryWrapper<User> accountQw = new QueryWrapper<>();
        accountQw.eq("user_account", userAccount);
        if (this.count(accountQw) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }

        if (StringUtils.isNotBlank(userPhone)) {
            QueryWrapper<User> phoneQw = new QueryWrapper<>();
            phoneQw.eq("user_phone", userPhone);
            if (this.count(phoneQw) > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已存在");
            }
        }

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes()));
        user.setUserPhone(StringUtils.trimToNull(userPhone));
        user.setUserName(StringUtils.defaultIfBlank(userName, userAccount));
        user.setUserRole(UserConstant.STUDENT_ROLE);
        user.setStatus(1);
        user.setBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        user.setPlanUnlocked(0);

        if (!this.save(user)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLoginByAccount(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号密码不能为空");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount)
                .eq("user_password", DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes()));
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        ensureUserAccessible(user);

        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = getLoginUserVO(user);
        loginUserVO.setToken(jwtTokenUtils.generateToken(user.getId(), user.getUserAccount()));
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLoginByPhone(String userPhone, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userPhone, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号密码不能为空");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_phone", userPhone)
                .eq("user_password", DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes()));
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号或密码错误");
        }
        ensureUserAccessible(user);

        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = getLoginUserVO(user);
        loginUserVO.setToken(jwtTokenUtils.generateToken(user.getId(), user.getUserAccount()));
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLoginByWeChat(String code, String nickName, String avatarUrl, HttpServletRequest request) {
        Map<String, Object> weChatInfo = weChatService.getSessionKeyOrOpenid(code);
        String openid = (String) weChatInfo.get("openid");
        String unionid = (String) weChatInfo.get("unionid");

        if (StringUtils.isBlank(openid)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信登录失败");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mp_open_id", openid);
        User user = this.getOne(queryWrapper);

        boolean isNewUser = false;
        if (user == null) {
            user = new User();
            user.setMpOpenId(openid);
            user.setUnionId(unionid);
            user.setUserRole(UserConstant.STUDENT_ROLE);
            user.setStatus(1);
            user.setUserAccount("wx_" + openid.substring(0, Math.min(10, openid.length())));
            user.setUserPassword(DigestUtils.md5DigestAsHex((SALT + openid).getBytes()));
            user.setUserName(StringUtils.defaultIfBlank(nickName, "微信用户"));
            user.setUserAvatar(avatarUrl);
            user.setBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            user.setPlanUnlocked(0);
            if (!this.save(user)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建微信用户失败");
            }
            isNewUser = true;
        }
        ensureUserAccessible(user);

        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        LoginUserVO loginUserVO = getLoginUserVO(user);
        loginUserVO.setToken(jwtTokenUtils.generateToken(user.getId(), user.getUserAccount()));
        loginUserVO.setIsNewUser(isNewUser);
        return loginUserVO;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User loginUser = (User) userObj;

        if (loginUser != null && loginUser.getId() != null) {
            User latest = this.getById(loginUser.getId());
            if (latest != null) {
                ensureUserAccessible(latest);
                return latest;
            }
        }

        String token = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (StringUtils.isBlank(token) || !jwtTokenUtils.validateToken(token)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        Long userId = jwtTokenUtils.getUserIdFromToken(token);
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        ensureUserAccessible(user);
        return user;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        LoginUserVO vo = new LoginUserVO();
        BeanUtils.copyProperties(user, vo);
        if (vo.getBalance() == null) {
            vo.setBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        if (vo.getPlanUnlocked() == null) {
            vo.setPlanUnlocked(0);
        }
        return vo;
    }

    @Override
    public UserVO getUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        if (vo.getBalance() == null) {
            vo.setBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        if (vo.getPlanUnlocked() == null) {
            vo.setPlanUnlocked(0);
        }
        return vo;
    }

    @Override
    public LoginUserVO rechargeBalance(User loginUser, BigDecimal amount) {
        BigDecimal rechargeAmount = amount == null ? DEFAULT_RECHARGE_AMOUNT : amount;
        if (rechargeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "充值金额必须大于0");
        }
        if (rechargeAmount.compareTo(BigDecimal.valueOf(10000)) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "单次充值金额过大");
        }

        User latest = this.getById(loginUser.getId());
        if (latest == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        BigDecimal currentBalance = latest.getBalance() == null ? BigDecimal.ZERO : latest.getBalance();
        BigDecimal newBalance = currentBalance.add(rechargeAmount).setScale(2, RoundingMode.HALF_UP);
        latest.setBalance(newBalance);
        boolean ok = this.updateById(latest);
        if (!ok) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "充值失败");
        }
        return getLoginUserVO(latest);
    }

    @Override
    public Page<UserVO> listUsers(UserQueryRequest queryRequest) {
        long current = queryRequest.getCurrent() == null ? 1 : queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize() == null ? 10 : queryRequest.getPageSize();

        QueryWrapper<User> qw = new QueryWrapper<>();
        if (StringUtils.isNotBlank(queryRequest.getUserAccount())) {
            qw.like("user_account", queryRequest.getUserAccount());
        }
        if (StringUtils.isNotBlank(queryRequest.getUserName())) {
            qw.like("user_name", queryRequest.getUserName());
        }
        if (StringUtils.isNotBlank(queryRequest.getUserPhone())) {
            qw.like("user_phone", queryRequest.getUserPhone());
        }
        if (StringUtils.isNotBlank(queryRequest.getUserRole())) {
            qw.eq("user_role", queryRequest.getUserRole());
        }
        qw.orderByDesc("id");

        Page<User> page = this.page(new Page<>(current, pageSize), qw);
        Page<UserVO> voPage = new Page<>(current, pageSize, page.getTotal());
        List<UserVO> records = page.getRecords().stream().map(this::getUserVO).collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public java.util.List<UserVO> getUserList(String userRole) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        if (StringUtils.isNotBlank(userRole)) {
            qw.eq("user_role", userRole);
        }
        qw.orderByDesc("id");
        List<User> users = this.list(qw);
        return users.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    private void ensureUserAccessible(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (Integer.valueOf(1).equals(user.getStatus())) {
            if (!UserConstant.BAN_ROLE.equals(user.getUserRole())) {
                return;
            }
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "账号已被封禁");
        }
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "账号已被禁用");
    }
}
