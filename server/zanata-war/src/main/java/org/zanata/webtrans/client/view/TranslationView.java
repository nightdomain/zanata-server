/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.TranslationPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.SplitLayoutPanelHelper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationView extends Composite implements TranslationPresenter.Display
{
   interface TranslationViewUiBinder extends UiBinder<LayoutPanel, TranslationView>
   {
   }

   private static TranslationViewUiBinder uiBinder = GWT.create(TranslationViewUiBinder.class);

   @UiField(provided = true)
   LayoutPanel sidePanelOuterContainer, southPanelContainer;

   @UiField
   TabLayoutPanel southPanelTab;

   @UiField
   LayoutPanel editorContainer, sidePanelContainer;

   @UiField(provided = true)
   ToggleButton toogleOptionsButton;

   @UiField(provided = true)
   ToggleButton toogleSouthButton;

   LayoutPanel tmPanel, userPanel;

   /*
    * TODO: temporary disable glossary functionalities
    */
   // @UiField
   LayoutPanel glossaryPanel;

   @UiField
   SplitLayoutPanel mainSplitPanel;

   final WebTransMessages messages;

   private double panelWidth = 20;
   private double southHeight = 30;

   boolean disableGlossary = true;

   @Inject
   public TranslationView(Resources resources, WebTransMessages messages)
   {
      this.messages = messages;

      StyleInjector.inject(resources.style().getText(), true);
      sidePanelOuterContainer = new LayoutPanel();
      southPanelContainer = new LayoutPanel();

      tmPanel = new LayoutPanel();
      userPanel = new LayoutPanel();
      
      toogleOptionsButton = new ToggleButton(messages.hideEditorOptionsLabel(), messages.showEditorOptionsLabel());
      toogleOptionsButton.setTitle(messages.hideEditorOptions());
      toogleOptionsButton.setDown(true);

      toogleSouthButton = new ToggleButton(messages.minimiseLabel(), messages.restoreLabel());

      initWidget(uiBinder.createAndBindUi(this));
      mainSplitPanel.setWidgetMinSize(sidePanelOuterContainer, (int) panelWidth);
      mainSplitPanel.setWidgetMinSize(southPanelContainer, (int) southHeight);


      southPanelTab.add(tmPanel, messages.translationMemoryHeading());
      if (!disableGlossary)
      {
         southPanelTab.add(glossaryPanel, "Glossary");
      }
      southPanelTab.add(userPanel, messages.nUsersOnline(0));


   }

   @Override
   public void setTranslationMemoryView(Widget translationMemoryView)
   {
      tmPanel.clear();
      tmPanel.add(translationMemoryView);
   }

   @Override
   public void setWorkspaceUsersView(Widget workspaceUsersView)
   {
      userPanel.clear();
      userPanel.add(workspaceUsersView);
   }

   /*
    * TODO: temporary disable glossary functionalities
    */
   @Override
   public void setGlossaryView(Widget glossaryView)
   {
      if (!disableGlossary)
      {
         glossaryPanel.clear();
         glossaryPanel.add(glossaryView);
      }
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void setEditorView(Widget editorView)
   {
      this.editorContainer.add(editorView);
   }

   @Override
   public void setSidePanel(Widget sidePanel)
   {
      sidePanelContainer.clear();
      sidePanelContainer.add(sidePanel);
   }

   @Override
   public void setSidePanelViewVisible(boolean visible)
   {
      mainSplitPanel.forceLayout();
      Widget splitter = SplitLayoutPanelHelper.getAssociatedSplitter(mainSplitPanel, sidePanelOuterContainer);
      if (visible)
      {
         SplitLayoutPanelHelper.setSplitPosition(mainSplitPanel, sidePanelOuterContainer, panelWidth);
      }
      else
      {
         panelWidth = mainSplitPanel.getWidgetContainerElement(sidePanelOuterContainer).getOffsetWidth();
         SplitLayoutPanelHelper.setSplitPosition(mainSplitPanel, sidePanelOuterContainer, 0);
      }
      splitter.setVisible(visible);
      mainSplitPanel.animate(200);

   }

   @Override
   public void setSouthPanelViewVisible(boolean visible)
   {
      mainSplitPanel.forceLayout();
      Widget splitter = SplitLayoutPanelHelper.getAssociatedSplitter(mainSplitPanel, southPanelContainer);
      if (visible)
      {
         SplitLayoutPanelHelper.setSplitPosition(mainSplitPanel, southPanelContainer, southHeight);
      }
      else
      {
         southHeight = mainSplitPanel.getWidgetContainerElement(southPanelContainer).getOffsetHeight();
         SplitLayoutPanelHelper.setSplitPosition(mainSplitPanel, southPanelContainer, 30);
      }
      splitter.setVisible(visible);
      mainSplitPanel.animate(200);

   }

   @Override
   public ToggleButton getToogleOptionsButton()
   {
      return toogleOptionsButton;
   }

   @Override
   public ToggleButton getToogleSouthButton()
   {
      return toogleSouthButton;
   }

   @Override
   public void updateWorkspaceUsersTitle(String title)
   {
      southPanelTab.setTabText(1, title);
   }
}