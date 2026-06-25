package org.example.system1.service;

import org.example.system1.dao.UserDao;
import org.example.system1.entity.User;
import org.example.system1.util.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UserDao userDao = new UserDao();

    /**
     * 用户登录
     */
    public String login(String username, String password, String role) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 验证角色
        if (role != null && !role.isEmpty() && !role.equals(user.getRole())) {
            throw new RuntimeException("角色不匹配");
        }
        // 验证密码 (BCrypt)
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return JwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    /**
     * 获取用户信息
     */
    public User getUserInfo(Long userId) {
        User user = userDao.findById(userId);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return user;
    }

    /**
     * 重置密码（忘记密码）
     */
    public void resetPassword(String username, String newPassword) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        boolean updated = userDao.updatePassword(username, hashed);
        if (!updated) {
            throw new RuntimeException("密码重置失败，请稍后重试");
        }
    }

    /**
     * 根据用户名获取用户（含角色等信息）
     */
    public User findByUsername(String username) {
        User user = userDao.findByUsername(username);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }
}
