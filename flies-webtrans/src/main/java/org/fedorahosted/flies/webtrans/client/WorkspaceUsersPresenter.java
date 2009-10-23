package org.fedorahosted.flies.webtrans.client;

import java.util.Arrays;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.fedorahosted.flies.gwt.model.Person;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.webtrans.client.ui.HasChildTreeNodes;
import org.fedorahosted.flies.webtrans.client.ui.HasFilter;
import org.fedorahosted.flies.webtrans.client.ui.HasNodeMouseOverHandlers;
import org.fedorahosted.flies.webtrans.client.ui.HasNodeMouseOutHandlers;
import org.fedorahosted.flies.webtrans.client.ui.TreeNode;
import org.fedorahosted.flies.webtrans.client.ui.TreeNodeImpl;

import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display> {

	public static final Place PLACE = new Place("WorkspaceUsersPresenter");
	
	public interface Display extends WidgetDisplay{
		HasChildTreeNodes<Person> getTree();
		HasFilter<Person> getFilter();
		HasNodeMouseOverHandlers getNodeMouseOver();
		HasNodeMouseOutHandlers getNodeMouseOut();
	}
	
	@Inject
	public WorkspaceUsersPresenter(final Display display, final EventBus eventBus) {
		super(display, eventBus);
	}
	
	
	@Override
	public Place getPlace() {
		return PLACE;
	}

	@Override
	protected void onBind() {
		Person [] translators = new Person[]{
			new Person( new PersonId("bob"), "Bob"),
			new Person( new PersonId("jane"), "Jane"),
			new Person( new PersonId("bill"), "Bill")
			};	
		getDisplay().getFilter().setList(Arrays.asList(translators));
		
		final DecoratedPopupPanel dpp = new DecoratedPopupPanel(true);
		
		getDisplay().getNodeMouseOver().addNodeMouseOverHandler(new MouseOverHandler() {

			@Override
			public void onMouseOver(MouseOverEvent event) {
				if (event.getSource() instanceof TreeNodeImpl<?>) {
					TreeNodeImpl<Person> source = (TreeNodeImpl<Person>) event.getSource();	
					System.out.println("popup for person with id "+source.getObject().getId().toString());
					dpp.setWidget(new Label(source.getObject().getId().toString()));
					dpp.setPopupPosition(source.getAbsoluteLeft(), source.getAbsoluteTop());
					dpp.show();
				}
			}
			
		});
		
		getDisplay().getNodeMouseOut().addNodeMouseOutHandler(new MouseOutHandler() {
		
			@Override
			public void onMouseOut(MouseOutEvent event) {
				dpp.clear();
				dpp.hide();
			}
			
		});
		
	}

	@Override
	protected void onPlaceRequest(PlaceRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUnbind() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshDisplay() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void revealDisplay() {
		// TODO Auto-generated method stub
		
	}

}
