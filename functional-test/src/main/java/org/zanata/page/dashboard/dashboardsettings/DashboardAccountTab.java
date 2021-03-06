/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.page.dashboard.dashboardsettings;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.dashboard.DashboardBasePage;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class DashboardAccountTab extends DashboardBasePage {

    public static final String INCORRECT_OLD_PASSWORD_ERROR =
            "Old password is incorrect, please check and try again.";

    public static final String FIELD_EMPTY_ERROR = "may not be empty";

    public static final String PASSWORD_LENGTH_ERROR =
            "size must be between 6 and 20";

    public static final String EMAIL_TAKEN_ERROR =
            "This email address is already taken";

    private By emailField = By.id("email-update-form:emailField:email");
    private By updateEmailButton = By.id("email-update-form:updateEmailButton");
    private By oldPasswordField = By.id("passwordChangeForm:oldPasswordField:oldPassword");
    private By newPasswordField = By.id("passwordChangeForm:newPasswordField:newPassword");
    private By changePasswordButton = By.id("passwordChangeForm:changePasswordButton");

    public DashboardAccountTab(WebDriver driver) {
        super(driver);
    }

    public DashboardAccountTab typeNewAccountEmailAddress(String emailAddress) {
        log.info("Enter email {}", emailAddress);
        readyElement(emailField).clear();
        readyElement(emailField).sendKeys(emailAddress);
        return new DashboardAccountTab(getDriver());
    }

    public DashboardAccountTab clickUpdateEmailButton() {
        log.info("Click Update Email");
        clickElement(updateEmailButton);
        return new DashboardAccountTab(getDriver());
    }

    public DashboardAccountTab typeOldPassword(String oldPassword) {
        log.info("Enter old password {}", oldPassword);
        readyElement(oldPasswordField).clear();
        readyElement(oldPasswordField).sendKeys(oldPassword);
        return new DashboardAccountTab(getDriver());
    }

    public DashboardAccountTab typeNewPassword(String newPassword) {
        log.info("Enter new password {}", newPassword);
        readyElement(newPasswordField).clear();
        readyElement(newPasswordField).sendKeys(newPassword);
        return new DashboardAccountTab(getDriver());
    }

    public DashboardAccountTab clickUpdatePasswordButton() {
        log.info("Click Update Password");
        clickElement(changePasswordButton);
        return new DashboardAccountTab(getDriver());
    }
}
