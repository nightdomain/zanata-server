package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter.Display;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;


@Test(groups = { "unit-tests" })
public class WorkspaceUsersPresenterTest
{
   // object under test
   WorkspaceUsersPresenter workspaceUsersPresenter;

   //injected mocks
   Display mockDisplay = createMock(Display.class);
   EventBus mockEventBus = createMock(EventBus.class);

   @BeforeMethod
   public void resetMocks()
   {
      reset(mockDisplay, mockEventBus);
      workspaceUsersPresenter = new WorkspaceUsersPresenter(mockDisplay, mockEventBus);
   }

   public void setEmptyUserList()
   {
      mockDisplay.clearUserList();
      expectLastCall().once();

      replay(mockDisplay, mockEventBus);

      workspaceUsersPresenter.bind();
      workspaceUsersPresenter.setUserList(new ArrayList<Person>());

      verify(mockDisplay, mockEventBus);
   }

   public void setNonEmptyUserList()
   {
      mockDisplay.clearUserList();
      expectLastCall().once();
      mockDisplay.addUser("John Smith", "john@zanata.org");
      expectLastCall().once();
      mockDisplay.addUser("Smith John", "smith@zanata.org");
      expectLastCall().once();
      mockDisplay.addUser("Smohn Jith", "smohn@zanata.org");
      expectLastCall().once();

      replay(mockDisplay, mockEventBus);

      workspaceUsersPresenter.bind();

      ArrayList<Person> people = new ArrayList<Person>();
      people.add(new Person(new PersonId("person1"), "John Smith", "john@zanata.org"));
      people.add(new Person(new PersonId("person2"), "Smith John", "smith@zanata.org"));
      people.add(new Person(new PersonId("person3"), "Smohn Jith", "smohn@zanata.org"));
      workspaceUsersPresenter.setUserList(people);

      verify(mockDisplay, mockEventBus);
   }
}
