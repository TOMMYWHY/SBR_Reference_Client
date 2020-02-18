package au.gov.sbr.core.client.gui;

import au.gov.sbr.core.client.beans.ClientBean;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class SBRJavaClientEditBusinessDocumentHeader extends JDialog {
	
	private ClientBean clientBeanForEdit;
	private JTextField busDocSeqNumber = null;
	private JTextField busDocCreationDate = null;
	private JTextField busDocValidationURI = null;
	private JTextField busDocBusGenID = null;
	private JTextField busDocGovGenID = null;
		
	public SBRJavaClientEditBusinessDocumentHeader(JFrame frame,ClientBean clientBean) {
		super(frame,"Edit Business Document Header");
		
		clientBeanForEdit = clientBean;
		
        JPanel businessDocumentFieldControls = new JPanel();
        businessDocumentFieldControls.setLayout(new GridLayout(0,2));
        
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a"); 
        String formattedDate = formatter.format(clientBeanForEdit.getSelectedBusinessDocument().getCreation().getTime());
        
        busDocSeqNumber = new JTextField("");
        busDocCreationDate = new JTextField(formattedDate);
        busDocCreationDate.setEditable(false);
        busDocValidationURI = new JTextField(clientBeanForEdit.getSelectedBusinessDocument().getValidationURI());
        busDocBusGenID = new JTextField(clientBeanForEdit.getSelectedBusinessDocument().getBusinessGeneratedIdentifier());
        busDocGovGenID = new JTextField(clientBeanForEdit.getSelectedBusinessDocument().getGovernmentGeneratedIdentifier());
                
        busDocSeqNumber.setEditable(false);
        
        businessDocumentFieldControls.add(new JLabel("Sequence Number"));
        businessDocumentFieldControls.add(busDocSeqNumber);
        businessDocumentFieldControls.add(new JLabel("Creation Date"));
        businessDocumentFieldControls.add(busDocCreationDate);
        businessDocumentFieldControls.add(new JLabel("Validation URI"));
        businessDocumentFieldControls.add(busDocValidationURI);
        businessDocumentFieldControls.add(new JLabel("Business Generated ID"));
        businessDocumentFieldControls.add(busDocBusGenID);
        businessDocumentFieldControls.add(new JLabel("Government Generated ID"));
        businessDocumentFieldControls.add(busDocGovGenID);
        
        JPanel okButtonControls = new JPanel();
        okButtonControls.setLayout(new FlowLayout());
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){            	
            	//Set the Values
            	SaveNewTextFieldInfo();            	
            	
            }
        });
        okButtonControls.add(okButton);
        
        Container pane = getContentPane();
        pane.add(businessDocumentFieldControls, BorderLayout.NORTH);
        pane.add(okButtonControls, BorderLayout.SOUTH);        
          
	}
	
	private void SaveNewTextFieldInfo() {
		
		String combinedPattern = "dd/MM/yyyy hh:mm:ss a";
		SimpleDateFormat datetimeParser = new SimpleDateFormat(combinedPattern);

		Date dateAndTime = null;
		try {
		dateAndTime = datetimeParser.parse(busDocCreationDate.getText());
		} catch (ParseException ex) {
		ex.printStackTrace();
		}
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTime( dateAndTime );
		
		clientBeanForEdit.getSelectedBusinessDocument().setCreation(cal);
		clientBeanForEdit.getSelectedBusinessDocument().setValidationURI(busDocValidationURI.getText());
		clientBeanForEdit.getSelectedBusinessDocument().setBusinessGeneratedIdentifier(busDocBusGenID.getText());
				
		if  (busDocGovGenID.getText().trim().isEmpty())
		{
			clientBeanForEdit.getSelectedBusinessDocument().setGovernmentGeneratedIdentifier(null);
		}
		else
		{
			clientBeanForEdit.getSelectedBusinessDocument().setGovernmentGeneratedIdentifier(busDocGovGenID.getText());
		}
		
		//Close the frame without closing the application
		this.dispose();
	}
	

}
