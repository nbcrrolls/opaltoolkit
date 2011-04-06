package uk.ac.manchester.cs.img.myfancytool.taverna.ui.view;

import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

import uk.ac.manchester.cs.img.myfancytool.taverna.ExampleActivity;
import uk.ac.manchester.cs.img.myfancytool.taverna.ExampleActivityConfigurationBean;

public class ExampleActivityContextViewFactory implements
		ContextualViewFactory<ExampleActivity> {

	public boolean canHandle(Object selection) {
		return selection instanceof ExampleActivity;
	}

	public List<ContextualView> getViews(ExampleActivity selection) {
		return Arrays.<ContextualView>asList(new ExampleContextualView(selection));
	}
}
