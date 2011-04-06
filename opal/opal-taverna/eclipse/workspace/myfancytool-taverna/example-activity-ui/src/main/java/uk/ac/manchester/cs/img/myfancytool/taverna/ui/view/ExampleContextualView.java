package uk.ac.manchester.cs.img.myfancytool.taverna.ui.view;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.rpc.ServiceException;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

import uk.ac.manchester.cs.img.myfancytool.taverna.ExampleActivity;
import uk.ac.manchester.cs.img.myfancytool.taverna.ExampleActivityConfigurationBean;
import uk.ac.manchester.cs.img.myfancytool.taverna.ui.config.ExampleConfigureAction;

import edu.sdsc.nbcr.opal.*;
import edu.sdsc.nbcr.opal.gui.common.GetServiceListHelper;
import edu.sdsc.nbcr.opal.gui.common.OPALService;

@SuppressWarnings("serial")
public class ExampleContextualView extends ContextualView {
	private final ExampleActivity activity;
	private JLabel description = new JLabel("ads");

	public ExampleContextualView(ExampleActivity activity) {
		this.activity = activity;
		initView();
	}
	
	protected Map<String, String> getOpalServiceDescs(String url) {
		Map<String, String> arg_desc = new HashMap<String, String> ();
		String name = null;
		String desc = null;
		
		AppServiceLocator asl = new AppServiceLocator();
		AppServicePortType appServicePort;
		AppMetadataType amt;
		
		boolean has_tagged = true;
		boolean has_untagged = true;
		boolean has_flags = true;
		boolean is_basic = true;
			
		try {
			appServicePort = asl.getAppServicePort(new java.net.URL(url));
			amt = appServicePort.getAppMetadata(new AppMetadataInputType());
			
			ParamsType [] paramType = null;
			ArrayList args = new ArrayList();
			
			try { 
				amt.getTypes().getTaggedParams();
			} catch (NullPointerException npe) {
				has_tagged = false;
			}
			
			try { 
				amt.getTypes().getUntaggedParams();
			} catch (NullPointerException npe) {
				has_untagged = false;
			}
			
			try {
				amt.getTypes().getFlags();
			} catch (NullPointerException npe) {
				has_flags = false;
			}
			
			if (has_tagged == true || has_untagged == true || has_flags == true)
				is_basic = false;
			
			if (!is_basic) {
				ParamsArrayType taggedArrayType = amt.getTypes().getTaggedParams();	
				ParamsArrayType untaggedArrayType = amt.getTypes().getUntaggedParams();	
			
				if ( (taggedArrayType != null) && (taggedArrayType.getParam() != null ) ) {
					paramType = taggedArrayType.getParam();
					
					for (int i = 0; i < paramType.length; i++) {
						name = paramType[i].getId().toString();
						desc = paramType[i].getTextDesc().toString();
						arg_desc.put(name, desc);
					}	
				}
				
				if ( (untaggedArrayType != null) && (untaggedArrayType.getParam() != null ) ) {
					paramType = untaggedArrayType.getParam();
					
					for (int i = 0; i < paramType.length; i++) {
						name = paramType[i].getId().toString();
						desc = paramType[i].getTextDesc().toString();
						arg_desc.put(name, desc);
					}	
				}
				
				if ((amt.getTypes().getFlags() != null) && (amt.getTypes().getFlags().getFlag() != null)) {
					FlagsType [] flagsType = amt.getTypes().getFlags().getFlag();
			
					for (int i = 0; i < flagsType.length; i++) {
						name = flagsType[i].getId().toString();
						desc = flagsType[i].getTextDesc().toString();
						arg_desc.put(name, desc);
					}	
				}
			}
			else {
				arg_desc.put("commandLine", "Command line arguments");
				arg_desc.put("inFiles", "List of input files as a String, separated with spaces");
				arg_desc.put("numProcs", "Number of processors");
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FaultType e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return arg_desc;
	}

	@Override
	public JComponent getMainFrame() {
		JPanel jPanel = new JPanel();
		//jPanel.add(description);
		Map<String, String> descs;
		
		ExampleActivityConfigurationBean configuration = activity
		.getConfiguration();

		JLabel service_desc = new JLabel ("opal_ws_panel");
		service_desc.setText("SPIDER PIG");
		
		String opal_url = configuration.getExampleUri().toString();
		String service_name = configuration.getExampleString();
		int pos = service_name.indexOf("Opal Web Service ");
		opal_url = opal_url + '/' + service_name.substring(pos + 1);
		pos = service_name.indexOf("_from_");
		opal_url = opal_url.substring(0, opal_url.indexOf("_from_"));
		
		jPanel.setLayout(new GridLayout(0, 1));
	    
	    descs = getOpalServiceDescs(opal_url);
	    
	    Set set = descs.entrySet();
		Iterator i = set.iterator();
		int maxlength = 0;
		/*
		while (i.hasNext()) {
			Map.Entry kv = (Map.Entry)i.next();
			String k = (String) kv.getKey();
			String v = (String) kv.getValue();

			if (k.length() > maxlength) 
				maxlength = k.length();
		}

		
		set = descs.entrySet();
		i = set.iterator();		
		*/
		while (i.hasNext()) {
			Map.Entry kv = (Map.Entry)i.next();
			String k = (String) kv.getKey();
			String v = (String) kv.getValue();
			//int guard = maxlength - k.length();
			
			/*
			for (int j = 0; j < guard; j++)
				k += " ";
			
			jPanel.add(new JLabel(k + "   " + v)); */
			
			//GridBagConstraints gbc = new GridBagConstraints();
			//gbc.fill = GridBagConstraints.NONE;
			//gbc.gridwidth = 15;
			//gbc.gridy = 15;
			
			jPanel.add(new JLabel(k + ":      " + v));			
		}
	
			/*
		jPanel.add(service_desc);
		*/

		refreshView();
		
		return jPanel;
	}

	@Override
	public String getViewTitle() {
		ExampleActivityConfigurationBean configuration = activity
				.getConfiguration();
		return "Opal web service " + configuration.getExampleString();
	}

	/**
	 * Typically called when the activity configuration has changed.
	 */
	@Override
	public void refreshView() {
		ExampleActivityConfigurationBean configuration = activity
				.getConfiguration();
		description.setText("Opal web service " + configuration.getExampleUri()
				+ " - " + configuration.getExampleString());
		// TODO: Might also show extra service information looked
		// up dynamically from endpoint/registry
	}

	/**
	 * View position hint
	 */
	@Override
	public int getPreferredPosition() {
		// We want to be on top
		return 100;
	}
	
	@Override
	public Action getConfigureAction(final Frame owner) {
		return new ExampleConfigureAction(activity, owner);
	}

}
