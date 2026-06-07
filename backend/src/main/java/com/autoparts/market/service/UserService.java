package com.autoparts.market.service;

import com.autoparts.market.entity.User;
import com.autoparts.market.mapper.UserMapper;
import com.autoparts.market.security.JwtTokenUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final CartService cartService;

    /** 手机号+验证码注册 */
    public Map<String, Object> registerByPhone(String phone, String password) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        w.eq(User::getPhone, phone);
        if (userMapper.selectCount(w) > 0) throw new RuntimeException("该手机号已注册");

        User user = new User();
        user.setUsername("user_" + phone.substring(phone.length() - 6));
        user.setPassword(passwordEncoder.encode(password != null ? password : "123456"));
        user.setPhone(phone);
        user.setRole("USER");
        userMapper.insert(user);

        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refresh = jwtTokenUtil.generateRefreshToken(user.getId());
        return buildLoginResult(user, token, refresh);
    }

    /** 手机号+验证码登录 */
    public Map<String, Object> loginByPhone(String phone) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        w.eq(User::getPhone, phone);
        User user = userMapper.selectOne(w);
        if (user == null) throw new RuntimeException("该手机号未注册");

        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refresh = jwtTokenUtil.generateRefreshToken(user.getId());
        return buildLoginResult(user, token, refresh);
    }

    public Map<String, Object> register(String username, String password, String phone, String email, String sessionId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole("USER");
        userMapper.insert(user);

        if (sessionId != null && !sessionId.isBlank()) {
            cartService.mergeTempToUser(sessionId, user.getId().toString());
        }

        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId());
        return buildLoginResult(user, token, refreshToken);
    }

    public Map<String, Object> login(String username, String password, String sessionId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (sessionId != null && !sessionId.isBlank()) {
            cartService.mergeTempToUser(sessionId, user.getId().toString());
        }

        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenUtil.generateRefreshToken(user.getId());
        return buildLoginResult(user, token, refreshToken);
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtTokenUtil.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }
        Long userId = jwtTokenUtil.getUserId(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");

        String newToken = jwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(user.getId());
        return buildLoginResult(user, newToken, newRefreshToken);
    }

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public void updateUserInfo(Long userId, String phone, String email) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setPhone(phone);
        user.setEmail(email);
        userMapper.updateById(user);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private Map<String, Object> buildLoginResult(User user, String token, String refreshToken) {
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole());
        result.put("user", userInfo);
        return result;
    }
}
