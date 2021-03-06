/*
 * Password Management Servlets (PWM)
 * http://www.pwm-project.org
 *
 * Copyright (c) 2006-2009 Novell, Inc.
 * Copyright (c) 2009-2017 The PWM Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package password.pwm.http.servlet.admin;

import com.novell.ldapchai.exception.ChaiUnavailableException;
import password.pwm.Permission;
import password.pwm.PwmApplication;
import password.pwm.bean.SessionLabel;
import password.pwm.bean.UserIdentity;
import password.pwm.bean.UserInfoBean;
import password.pwm.config.PwmSetting;
import password.pwm.config.UserPermission;
import password.pwm.config.profile.ProfileType;
import password.pwm.config.profile.ProfileUtility;
import password.pwm.config.profile.PwmPasswordPolicy;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.ldap.LdapOperationsHelper;
import password.pwm.ldap.LdapPermissionTester;
import password.pwm.ldap.UserStatusReader;
import password.pwm.util.operations.PasswordUtility;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class UserDebugDataReader {
    public static UserDebugDataBean readUserDebugData(
            final PwmApplication pwmApplication,
            final Locale locale,
            final SessionLabel sessionLabel,
            final UserIdentity userIdentity
    )
            throws PwmUnrecoverableException
    {
        final UserStatusReader userStatusReader = new UserStatusReader(pwmApplication, sessionLabel);

        final UserInfoBean userInfoBean = userStatusReader.populateUserInfoBean(locale, userIdentity);

        final Map<Permission,String> permissions = UserDebugDataReader.permissionMap(pwmApplication, sessionLabel, userIdentity);

        final Map<ProfileType,String> profiles = UserDebugDataReader.profileMap(pwmApplication, sessionLabel, userIdentity);

        final PwmPasswordPolicy ldapPasswordPolicy = PasswordUtility.readLdapPasswordPolicy(pwmApplication, pwmApplication.getProxiedChaiUser(userIdentity));

        final PwmPasswordPolicy configPasswordPolicy = PasswordUtility.determineConfiguredPolicyProfileForUser(
                pwmApplication,
                sessionLabel,
                userIdentity,
                locale
        );

        boolean readablePassword = false;
        try {
            readablePassword = null != LdapOperationsHelper.readLdapPassword(pwmApplication, sessionLabel, userIdentity);
        } catch (ChaiUnavailableException e) {
            /* disregard */
        }

        final UserDebugDataBean userDebugData = UserDebugDataBean.builder()
                .userInfoBean(userInfoBean)
                .permissions(permissions)
                .profiles(profiles)
                .ldapPasswordPolicy(ldapPasswordPolicy)
                .configuredPasswordPolicy(configPasswordPolicy)
                .passwordReadable(readablePassword)
                .build();

        return userDebugData;
    }


    private static Map<Permission, String> permissionMap(
            final PwmApplication pwmApplication,
            final SessionLabel sessionLabel,
            final UserIdentity userIdentity

    )
            throws PwmUnrecoverableException
    {
        final Map<Permission,String> results = new TreeMap<>();
        for (final Permission permission : Permission.values()) {
            final PwmSetting setting = permission.getPwmSetting();
            if (!setting.isHidden() && !setting.getCategory().isHidden() && !setting.getCategory().hasProfiles()) {
                final List<UserPermission> userPermission = pwmApplication.getConfig().readSettingAsUserPermission(permission.getPwmSetting());
                final boolean result = LdapPermissionTester.testUserPermissions(
                        pwmApplication,
                        sessionLabel,
                        userIdentity,
                        userPermission
                );
                results.put(permission, result ? Permission.PermissionStatus.GRANTED.toString() : Permission.PermissionStatus.DENIED.toString());
            }

        }
        return Collections.unmodifiableMap(results);
    }

    private static Map<ProfileType,String> profileMap(
            final PwmApplication pwmApplication,
            final SessionLabel sessionLabel,
            final UserIdentity userIdentity
    ) throws PwmUnrecoverableException
    {
        final Map<ProfileType,String> results = new TreeMap<>();
        for (final ProfileType profileType : ProfileType.values()) {
            final String id = ProfileUtility.discoverProfileIDforUser(
                    pwmApplication,
                    sessionLabel,
                    userIdentity,
                    profileType
            );
            results.put(profileType, id);
        }
        return Collections.unmodifiableMap(results);
    }
}
