package vn.vinaacademy.user.helpers;

import org.springframework.stereotype.Component;
import vn.vinaacademy.user.entity.User;
import vn.vinaacademy.common.constant.AuthConstants;

@Component
public class AccessHelper {

    public boolean isAdmin(User user) {
        return checkRole(user, AuthConstants.ADMIN_ROLE);
    }

    public boolean isStaff(User user) {
        return checkRole(user, AuthConstants.STAFF_ROLE);
    }

    public boolean checkRole(User user, String roleCode) {
        return user.getRoles().stream()
                .anyMatch(role -> roleCode.equals(role.getCode()));
    }
}
