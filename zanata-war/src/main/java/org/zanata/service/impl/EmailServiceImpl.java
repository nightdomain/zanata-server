/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.service.impl;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.zanata.ApplicationConfiguration;
import org.zanata.action.VersionGroupJoinAction;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.email.ActivationEmailStrategy;
import org.zanata.email.Addresses;
import org.zanata.email.EmailBuilder;
import org.zanata.email.EmailStrategy;
import org.zanata.email.EmailValidationEmailStrategy;
import org.zanata.email.PasswordResetEmailStrategy;
import org.zanata.email.UsernameChangedEmailStrategy;
import org.zanata.i18n.Messages;
import org.zanata.model.HLocale;
import org.zanata.model.HLocaleMember;
import org.zanata.model.HPerson;
import org.zanata.service.EmailService;

import javax.annotation.Nullable;
import javax.mail.internet.InternetAddress;

import com.google.common.collect.Lists;

import static org.zanata.email.Addresses.getAddresses;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@AutoCreate
@Name("emailServiceImpl")
@Scope(ScopeType.STATELESS)
@Slf4j
public class EmailServiceImpl implements EmailService {

    @In
    private EmailBuilder emailBuilder;

    @In
    private IdentityManager identityManager;

    @In
    private PersonDAO personDAO;

    @In
    private ApplicationConfiguration applicationConfiguration;

    @In
    private VersionGroupJoinAction versionGroupJoinAction;

    @In
    private LocaleDAO localeDAO;

    @In
    private Messages msgs;

    /**
     *
     * @return the list of users with the admin role
     */
    private List<HPerson> getAdmins() {
        // required to read admin users for a non-admin session
        final List<HPerson> admins = new ArrayList<HPerson>();
        new RunAsOperation() {
            @Override
            public void execute() {
                for (Principal admin : identityManager.listMembers("admin")) {
                    admins.add(personDAO.findByUsername(admin.getName()));
                }
            }
        }.addRole("admin").run();

        return admins;
    }

    private List<HPerson> getCoordinators(HLocale locale) {
        List<HPerson> coordinators = new ArrayList<HPerson>();

        for (HLocaleMember member : locale.getMembers()) {
            if (member.isCoordinator()) {
                coordinators.add(member.getPerson());
            }
        }
        return coordinators;
    }

    @Override
    public String sendActivationEmail(String toName,
            String toEmailAddr, String activationKey) {
        InternetAddress to = Addresses.getAddress(toEmailAddr, toName);
        emailBuilder.sendMessage(new ActivationEmailStrategy(activationKey),
                null, to);
        return msgs.get("jsf.Account.ActivationMessage");
    }

    @Override
    public String sendEmailValidationEmail(String toName,
            String toEmailAddr, String activationKey) {
        InternetAddress to = Addresses.getAddress(toEmailAddr, toName);
        emailBuilder.sendMessage(new EmailValidationEmailStrategy(activationKey),
                null, to);
        return msgs.get("jsf.email.accountchange.SentNotification");
    }

    @Override
    public String sendPasswordResetEmail(HPerson person, String key) {
        InternetAddress to = Addresses.getAddress(person);
        emailBuilder.sendMessage(new PasswordResetEmailStrategy(key), null, to);
        return msgs.get("jsf.email.passwordreset.SentNotification");
    }


    /**
     * sends emails to configured admin emails for server, or admin users if no
     * server emails are configured.
     *
     * @param strategy - Email template
     * @param receivedReasons
     *            - Reasons of why users are receiving email as admin can
     *            received via: 1) Direct email from logged in users. 2) User
     *            request to join language team when there's no coordinator. 3)
     *            User request to update their role in language team when
     *            there's no coordinator. 4) User request to join a version group
     *            when there's no maintainer.
     */
    @Override
    public String sendToAdmins(EmailStrategy strategy,
            @Nullable List<String> receivedReasons) {
        List<String> adminEmails = applicationConfiguration.getAdminEmail();
        receivedReasons =
            receivedReasons == null ? Lists.<String> newArrayList()
                : receivedReasons;

        if (!adminEmails.isEmpty()) {
            receivedReasons.add(msgs.get("jsf.email.admin.ReceivedReason"));
            String toName = msgs.get("jsf.ZanataAdministrator");
            emailBuilder.sendMessage(strategy, receivedReasons,
                    getAddresses(adminEmails, toName));
            return msgs.get("jsf.email.admin.SentNotification");
        } else {
            return sendToAdminUsers(strategy, receivedReasons);
        }
    }

    /**
     * Emails admin users with given template
     *
     */
    private String sendToAdminUsers(EmailStrategy strategy,
        List<String> receivedReasons) {
        receivedReasons.add(msgs.get("jsf.email.admin.user.ReceivedReason"));
        emailBuilder.sendMessage(strategy, receivedReasons,
            getAddresses(getAdmins()));
        return msgs.get("jsf.email.admin.SentNotification");
    }

    @Override
    public String sendToLanguageCoordinators(LocaleId localeId,
            EmailStrategy strategy) {
        HLocale locale = localeDAO.findByLocaleId(localeId);

        List<HPerson> coordinators = getCoordinators(locale);
        if (!coordinators.isEmpty()) {
            String receivedReason = msgs.format(
                    "jsf.email.coordinator.ReceivedReason",
                    locale.retrieveNativeName());

            emailBuilder.sendMessage(strategy,
                    Lists.newArrayList(receivedReason),
                    getAddresses(coordinators));
            return msgs.format("jsf.email.coordinator.SentNotification",
                    locale.retrieveNativeName());
        } else {
            String receivedReason =
                    msgs.format(
                            "jsf.email.admin.ReceivedReason.language.noCoordinator",
                            locale.getLocaleId(), locale.retrieveDisplayName());
            return sendToAdmins(strategy, Lists.newArrayList(receivedReason));
        }
    }

    @Override
    public String sendToVersionGroupMaintainers(List<HPerson> maintainers,
            EmailStrategy strategy) {
        if (!maintainers.isEmpty()) {
            String receivedReason = msgs.format(
                    "jsf.email.group.maintainer.ReceivedReason",
                    versionGroupJoinAction.getGroupName());
            emailBuilder.sendMessage(strategy,
                    Lists.newArrayList(receivedReason),
                    getAddresses(maintainers));
            return msgs.format("jsf.email.group.maintainer.SentNotification",
                    versionGroupJoinAction.getGroupName());
        } else {
            String receivedReason =
                msgs.format(
                    "jsf.email.admin.ReceivedReason.versionGroup.noMaintainer",
                    versionGroupJoinAction.getGroupName());
            return sendToAdmins(strategy, Lists.newArrayList(receivedReason));
        }
    }

    @Override
    public String sendUsernameChangedEmail(String email, String newUsername) {
        InternetAddress to = Addresses.getAddress(email, newUsername);
        boolean resetPassword = applicationConfiguration.isInternalAuth();
        emailBuilder.sendMessage(new UsernameChangedEmailStrategy(
                newUsername, resetPassword), null, to);
        return msgs.get("jsf.email.usernamechange.SentNotification");
    }

}
