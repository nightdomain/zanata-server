/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.administration;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class TranslationMemoryEditPage extends BasePage {

    private By idField = By.id("tmForm:slugField:slug");
    private By descriptionField = By.id("tmForm:descriptionField:description");
    private By saveButton = By.id("tmForm:save");
    private By cancelButton = By.id("tmForm:cancel");

    public TranslationMemoryEditPage(WebDriver driver) {
        super(driver);
    }

    public TranslationMemoryEditPage enterMemoryID(String id) {
        log.info("Enter TM ID {}", id);
        readyElement(idField).sendKeys(id);
        return new TranslationMemoryEditPage(getDriver());
    }

    public TranslationMemoryEditPage enterMemoryDescription(String description) {
        log.info("Enter TM description {}", description);
        readyElement(descriptionField).sendKeys(description);
        return new TranslationMemoryEditPage(getDriver());
    }

    public TranslationMemoryPage saveTM() {
        log.info("Click Save");
        clickElement(saveButton);
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryEditPage clickSaveAndExpectFailure() {
        log.info("Click Save");
        clickElement(saveButton);
        return new TranslationMemoryEditPage(getDriver());
    }

    public TranslationMemoryPage cancelTM() {
        log.info("Click Cancel");
        clickElement(cancelButton);
        return new TranslationMemoryPage(getDriver());
    }

}
