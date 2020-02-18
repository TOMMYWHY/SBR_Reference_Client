
package au.gov.sbr.core.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationActionMap;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.swingbinding.JComboBoxBinding;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.JTableBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.jdesktop.swingbinding.JTableBinding.ColumnBinding;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import au.gov.sbr.comn.sbdm_02.MessageAttachmentInstanceBinaryObjectType;
import au.gov.sbr.comn.sbdm_02.MessageEventItemType;
import au.gov.sbr.core.client.beans.BusinessDocumentBean;
import au.gov.sbr.core.client.beans.ClientBean;
import au.gov.sbr.core.client.beans.ResponseBean;
import au.gov.sbr.core.client.gui.util.InsertParametersToStringConverter;
import au.gov.sbr.core.client.gui.util.MessageEventItemSeverityCodeTypeToStringConverter;
import au.gov.sbr.core.client.gui.util.MessageTimestampGenerationSourceCodeTypeToStringConverter;
import au.gov.sbr.core.client.gui.util.SBRCodeToStringConverter;
import au.gov.sbr.core.client.gui.util.XMLGregorianCalendarToTimestampConverter;

/**
 * Represents the SBR 2011 Core Services Reference Client's main window. It
 * contains all the properties that are exposed to the GUI for binding as well
 * as the Commands and code behind them for most of the buttons.
 */
public class SBRJavaClientView extends FrameView {

	private ClientBean clientBean;

	/**
	 * Constructs a new SBRJavaClientView object as a single frame application.
	 * 
	 * @param app
	 *            single frame application.
	 */
	public SBRJavaClientView(SingleFrameApplication app) {
		super(app);
		if (!OrganisationNameCheck.isOrganisationNameSet())
		{
			JOptionPane.showMessageDialog(((JFrame)app.getMainFrame()), "Organisation name must be set " +
					"in configuration file." + System.getProperty("line.separator") + System.getProperty("line.separator") +
					"See bin/au/gov/sbr/core/client/gui/OrganisationNameCheck.properties and/or" +
					System.getProperty("line.separator") +
					"src/au/gov/sbr/core/client/gui/OrganisationNameCheck.properties.", "SBR Java Reference Client", 
					JOptionPane.ERROR_MESSAGE);

			System.exit(0);
		}

		this.clientBean = new ClientBean();

		// showAboutBox();
		initComponents();

		// status bar initialisation - message timeout, idle icon and busy
		// animation, etc
		ResourceMap resourceMap = getResourceMap();
		int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
		messageTimer = new Timer(messageTimeout, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				statusMessageLabel.setText("");
			}
		});
		messageTimer.setRepeats(false);
		
		retryTimer = new Timer((resourceMap.getInteger(
				"default.queue.check.delay.seconds") * 1000), new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				retryItem();
			}
		});
		retryTimer.start();

		int busyAnimationRate = resourceMap
				.getInteger("StatusBar.busyAnimationRate");
		for (int i = 0; i < busyIcons.length; i++) {
			busyIcons[i] = resourceMap
					.getIcon("StatusBar.busyIcons[" + i + "]");
		}
		busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
				statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
			}
		});
		idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
		statusAnimationLabel.setIcon(idleIcon);
		progressBar.setVisible(false);

		// connecting action tasks to status bar via TaskMonitor
		TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
		taskMonitor
				.addPropertyChangeListener(new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						String propertyName = evt.getPropertyName();
						if ("started".equals(propertyName)) {
							if (!busyIconTimer.isRunning()) {
								statusAnimationLabel.setIcon(busyIcons[0]);
								busyIconIndex = 0;
								busyIconTimer.start();
							}
							statusMessageLabel.setText("Submitting");
							progressBar.setVisible(true);
							progressBar.setIndeterminate(true);
						} else if ("done".equals(propertyName)) {
							busyIconTimer.stop();
							statusAnimationLabel.setIcon(idleIcon);
							statusMessageLabel.setText("Done");
							progressBar.setVisible(false);
							progressBar.setValue(0);
						} else if ("message".equals(propertyName)) {
							String text = (String) (evt.getNewValue());
							statusMessageLabel.setText((text == null) ? ""
									: text);
							messageTimer.restart();
						} else if ("progress".equals(propertyName)) {
							int value = (Integer) (evt.getNewValue());
							progressBar.setVisible(true);
							progressBar.setIndeterminate(false);
							progressBar.setValue(value);
						}
					}
				});
	}

	/**
	 * Creates and focuses on the window which describes the SBR Reference
	 * Client software product.
	 */
	@Action
	public void showAboutBox() {
		if (aboutBox == null) {
			JFrame mainFrame = SBRJavaClientApp.getApplication().getMainFrame();
			aboutBox = new SBRJavaClientAboutBox(mainFrame);
			aboutBox.setLocationRelativeTo(mainFrame);
		}
		SBRJavaClientApp.getApplication().show(aboutBox);
	}

	/**
	 * Returns the client bean object.
	 * 
	 * @return the client bean.
	 */
	public ClientBean getClientBean() {
		setActiveServices();
		return clientBean;
	}

	/**
	 * Sets the client bean object.
	 * 
	 * @param clientBean
	 *            client bean object.
	 */
	public void setClientBean(ClientBean clientBean) {
		ClientBean oldClientBean = this.clientBean;
		this.clientBean = clientBean;

		firePropertyChange("clientBean", oldClientBean, clientBean);
	}

	/**
	 * This method is called from within the constructor to initialise the form.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	// <editor-fold defaultstate="collapsed" desc="GUI Code">
	private void initComponents() {

		{
			statusPanel = new javax.swing.JPanel();
			statusPanel.setName("statusPanel"); // NOI18N

			JSeparator statusPanelSeparator = new JSeparator();
			statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

			statusMessageLabel = new javax.swing.JLabel();
			statusMessageLabel.setName("statusMessageLabel"); // NOI18N

			statusMessageLabel.setForeground(Color.blue);
			statusMessageLabel.setText(getResourceMap().getString(
					"statusPanel.default.text")); // NOI18N

			statusAnimationLabel = new javax.swing.JLabel();
			statusAnimationLabel
					.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
			statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

			progressBar = new javax.swing.JProgressBar();
			progressBar.setName("progressBar"); // NOI18N

			javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(
					statusPanel);
			statusPanel.setLayout(statusPanelLayout);
			statusPanelLayout.setVerticalGroup(statusPanelLayout
					.createSequentialGroup().addComponent(statusPanelSeparator,
							GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE).addPreferredGap(
							LayoutStyle.ComponentPlacement.RELATED, 0,
							Short.MAX_VALUE).addGroup(
							statusPanelLayout.createParallelGroup(
									GroupLayout.Alignment.BASELINE)
									.addComponent(progressBar,
											GroupLayout.Alignment.BASELINE,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(statusMessageLabel,
											GroupLayout.Alignment.BASELINE,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(statusAnimationLabel,
											GroupLayout.Alignment.BASELINE,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE))
					.addContainerGap());
			statusPanelLayout
					.setHorizontalGroup(statusPanelLayout
							.createParallelGroup()
							.addComponent(statusPanelSeparator,
									GroupLayout.Alignment.LEADING, 0, 987,
									Short.MAX_VALUE)
							.addGroup(
									GroupLayout.Alignment.LEADING,
									statusPanelLayout
											.createSequentialGroup()
											.addPreferredGap(
													statusPanelSeparator,
													statusMessageLabel,
													LayoutStyle.ComponentPlacement.INDENT)
											.addComponent(statusMessageLabel,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE)
											.addGap(0, 803, Short.MAX_VALUE)
											.addComponent(progressBar,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(
													LayoutStyle.ComponentPlacement.RELATED)
											.addComponent(statusAnimationLabel,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE)
											.addContainerGap()));
		}

		{
			Color sbrBlue = new Color(51, 149, 183);
			mainPanel = new javax.swing.JPanel();
			BorderLayout mainPanelLayout = new BorderLayout();
			mainPanel.setName("mainPanel"); // NOI18N
			mainPanel.setLayout(mainPanelLayout);
			mainPanel.setBackground(sbrBlue);
			{
				topTabbedPane = new JTabbedPane();
				mainPanel.add(topTabbedPane, BorderLayout.CENTER);
				{
					submissionPanel = new javax.swing.JPanel();
					submissionPanel.setName("submissionPanel"); // NOI18N
					GroupLayout submissionPanelLayout = new GroupLayout(
							(JComponent) submissionPanel);
					submissionPanel.setLayout(submissionPanelLayout);
					topTabbedPane.addTab("Request Submission", null,
							submissionPanel, null);
					submissionPanel.setPreferredSize(new java.awt.Dimension(
							1032, 557));
					{
						formPanel = new javax.swing.JPanel();
						GroupLayout formPanelLayout = new GroupLayout(
								(JComponent) formPanel);
						formPanel.setBorder(javax.swing.BorderFactory
								.createTitledBorder(getResourceMap().getString(
										"formPanel.border.title"))); // NOI18N
						formPanel.setName("formPanel"); // NOI18N
						formPanel.setLayout(formPanelLayout);
						{
							formScrollPane = new javax.swing.JScrollPane();
							formScrollPane.setName("formScrollPane"); // NOI18N
							{
								formList = new javax.swing.JList();
								formScrollPane.setViewportView(formList);
								formList
										.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
								formList.setName("formList"); // NOI18N
							}
						}
						formPanelLayout.setHorizontalGroup(formPanelLayout
								.createSequentialGroup().addContainerGap()
								.addComponent(formScrollPane, 0, 94,
										Short.MAX_VALUE).addContainerGap());
						formPanelLayout.setVerticalGroup(formPanelLayout
								.createSequentialGroup()
								.addComponent(formScrollPane, 0, 194,
										Short.MAX_VALUE).addGap(8));
					}
					{
						servicePanel = new javax.swing.JPanel();
						GroupLayout servicePanelLayout = new GroupLayout(
								(JComponent) servicePanel);
						servicePanel.setBorder(javax.swing.BorderFactory
								.createTitledBorder(getResourceMap().getString(
										"servicePanel.border.title"))); // NOI18N
						servicePanel.setName("servicePanel"); // NOI18N
						servicePanel.setLayout(servicePanelLayout);
						serviceButtonGroup = new javax.swing.ButtonGroup();
						{
							listRadio = new javax.swing.JRadioButton();
							listRadio.setVisible(false);
							serviceButtonGroup.add(listRadio);
						}
						{
							prefillRadio = new javax.swing.JRadioButton();
							prefillRadio.setVisible(false);
							serviceButtonGroup.add(prefillRadio);
						}
						{
							prelodgeRadio = new javax.swing.JRadioButton();
							prelodgeRadio.setVisible(false);
							serviceButtonGroup.add(prelodgeRadio);
						}
						{
							lodgeRadio = new javax.swing.JRadioButton();
							lodgeRadio.setVisible(false);
							serviceButtonGroup.add(lodgeRadio);
						}
						{
							altListRadio = new javax.swing.JRadioButton();
							altListRadio.setVisible(false);
							serviceButtonGroup.add(altListRadio);
						}
						{
							altLodgeRadio = new javax.swing.JRadioButton();
							altLodgeRadio.setVisible(false);
							serviceButtonGroup.add(altLodgeRadio);
						}
						servicePanelLayout
								.setHorizontalGroup(servicePanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												servicePanelLayout
														.createParallelGroup()
														.addComponent(
																listRadio,
																GroupLayout.Alignment.LEADING,
																0, 97,
																Short.MAX_VALUE)
														.addGroup(
																GroupLayout.Alignment.LEADING,
																servicePanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				prefillRadio,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				0,
																				46,
																				Short.MAX_VALUE))
														.addGroup(
																GroupLayout.Alignment.LEADING,
																servicePanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				prelodgeRadio,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				0,
																				49,
																				Short.MAX_VALUE))
														.addGroup(
																GroupLayout.Alignment.LEADING,
																servicePanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				lodgeRadio,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				0,
																				49,
																				Short.MAX_VALUE))
														.addGroup(
																GroupLayout.Alignment.LEADING,
																servicePanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				altListRadio,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				0,
																				49,
																				Short.MAX_VALUE))
														.addGroup(
																GroupLayout.Alignment.LEADING,
																servicePanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				altLodgeRadio,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				0,
																				32,
																				Short.MAX_VALUE))));
						servicePanelLayout
								.setVerticalGroup(servicePanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addComponent(listRadio,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(prefillRadio,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(prelodgeRadio,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(lodgeRadio,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(altListRadio,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(altLodgeRadio,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addContainerGap(64, Short.MAX_VALUE));
					}
					{
						documentPanel = new javax.swing.JPanel();
						GroupLayout documentPanelLayout = new GroupLayout(
								(JComponent) documentPanel);
						documentPanel.setBorder(BorderFactory
								.createTitledBorder(getResourceMap().getString(
										"documentPanel.border.title"))); // NOI18N
						documentPanel.setName("documentPanel"); // NOI18N
						documentPanel.setLayout(documentPanelLayout);
						{
							documentScrollPane = new javax.swing.JScrollPane();
							documentScrollPane.setName("documentScrollPane"); // NOI18N
							{
								businessDocumentList = new javax.swing.JList();
								documentScrollPane
										.setViewportView(businessDocumentList);
								businessDocumentList
										.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
								businessDocumentList
										.setName("businessDocumentList"); // NOI18N
								businessDocumentList.addMouseListener(new MouseAdapter(){
									public void mouseReleased(final MouseEvent e) {
										if (e.isPopupTrigger()) {
											JPopupMenu menu = new JPopupMenu();
											JMenuItem editMenuItem = new JMenuItem();
											editMenuItem.setAction(getAppActionMap()
													.get("editBusinessDocumentListItem")); // NOI18N
											editMenuItem.setText("Edit");
											menu.add(editMenuItem);
		
											// Get the position of the click
											final int x = e.getX();
											final int y = e.getY();
		
											// Verify that the click occured on the selected cell
											final int index = businessDocumentList.getSelectedIndex();
											final Rectangle bounds = businessDocumentList.getCellBounds(index, index);
											if (null != bounds && bounds.contains(x, y)) {
												menu.show(e.getComponent(), x, y);
											}
										}
									}
								});
							}
						}
						{
							addBusinessDocumentButton = new javax.swing.JButton();
							addBusinessDocumentButton
									.setAction(getAppActionMap().get(
											"addNewBusinessDocument")); // NOI18N
							addBusinessDocumentButton
									.setText(getResourceMap().getString(
											"addBusinessDocumentButton.text")); // NOI18N
							addBusinessDocumentButton
									.setName("addBusinessDocumentButton"); // NOI18N
						}
						{
							removeBusinessDocumentButton = new javax.swing.JButton();
							removeBusinessDocumentButton
									.setAction(getAppActionMap().get(
											"removeSelectedInstanceDocument")); // NOI18N
							removeBusinessDocumentButton
									.setText(getResourceMap()
											.getString(
													"removeBusinessDocumentButton.text")); // NOI18N
							removeBusinessDocumentButton
									.setName("removeBusinessDocumentButton"); // NOI18N
						}
						{
						}
						documentPanelLayout
								.setHorizontalGroup(documentPanelLayout
										.createSequentialGroup()
										.addGap(7)
										.addGroup(
												documentPanelLayout
														.createParallelGroup()
														.addComponent(
																documentScrollPane,
																GroupLayout.Alignment.LEADING,
																0, 225,
																Short.MAX_VALUE)
														.addGroup(
																GroupLayout.Alignment.LEADING,
																documentPanelLayout
																		.createSequentialGroup()
																		.addGap(
																				0,
																				129,
																				Short.MAX_VALUE)
																		.addComponent(
																				addBusinessDocumentButton,
																				GroupLayout.PREFERRED_SIZE,
																				43,
																				GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				removeBusinessDocumentButton,
																				GroupLayout.PREFERRED_SIZE,
																				43,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				7))));
						documentPanelLayout.linkSize(SwingConstants.HORIZONTAL,
								new Component[] { removeBusinessDocumentButton,
										addBusinessDocumentButton });
						documentPanelLayout
								.setVerticalGroup(documentPanelLayout
										.createSequentialGroup()
										.addComponent(documentScrollPane, 0,
												169, Short.MAX_VALUE)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												documentPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																addBusinessDocumentButton,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																removeBusinessDocumentButton,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addGap(6));
					}
					{
						attachmentPanel = new javax.swing.JPanel();
						GroupLayout attachmentPanelLayout = new GroupLayout(
								(JComponent) attachmentPanel);
						attachmentPanel.setLayout(attachmentPanelLayout);
						{
							attachmentScrollPane = new javax.swing.JScrollPane();
							attachmentScrollPane
									.setName("attachmentScrollPane"); // NOI18N
							{
								attachmentList = new javax.swing.JList();
								attachmentScrollPane
										.setViewportView(attachmentList);
								attachmentList
										.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								attachmentList.setName("attachmentList"); // NOI18N
							}
						}
						{
							removeAttachmentButton = new javax.swing.JButton();
							removeAttachmentButton.setAction(getAppActionMap()
									.get("removeSelectedAttachment")); // NOI18N
							removeAttachmentButton.setText(getResourceMap()
									.getString("removeAttachmentButton.text")); // NOI18N
							removeAttachmentButton
									.setName("removeAttachmentButton"); // NOI18N
						}
						{
							addAttachmentButton = new javax.swing.JButton();
							addAttachmentButton.setAction(getAppActionMap()
									.get("addNewAttachment")); // NOI18N
							addAttachmentButton.setText(getResourceMap()
									.getString("addAttachmentButton.text")); // NOI18N
							addAttachmentButton.setName("addAttachmentButton"); // NOI18N
						}
						attachmentPanel.setBorder(javax.swing.BorderFactory
								.createTitledBorder(getResourceMap().getString(
										"attachmentPanel.border.title"))); // NOI18N
						attachmentPanel.setName("attachmentPanel"); // NOI18N
						attachmentPanelLayout
								.setHorizontalGroup(attachmentPanelLayout
										.createSequentialGroup()
										.addGap(7)
										.addGroup(
												attachmentPanelLayout
														.createParallelGroup()
														.addComponent(
																attachmentScrollPane,
																GroupLayout.Alignment.LEADING,
																0, 225,
																Short.MAX_VALUE)
														.addGroup(
																GroupLayout.Alignment.LEADING,
																attachmentPanelLayout
																		.createSequentialGroup()
																		.addGap(
																				0,
																				129,
																				Short.MAX_VALUE)
																		.addComponent(
																				addAttachmentButton,
																				GroupLayout.PREFERRED_SIZE,
																				43,
																				GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				removeAttachmentButton,
																				GroupLayout.PREFERRED_SIZE,
																				43,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				7))));
						attachmentPanelLayout.linkSize(
								SwingConstants.HORIZONTAL, new Component[] {
										removeAttachmentButton,
										addAttachmentButton });
						attachmentPanelLayout
								.setVerticalGroup(attachmentPanelLayout
										.createSequentialGroup()
										.addComponent(attachmentScrollPane, 0,
												169, Short.MAX_VALUE)
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												attachmentPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																addAttachmentButton,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																removeAttachmentButton,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addGap(6));
					}
					{
						optionsPanel = new javax.swing.JPanel();
						GroupLayout optionsPanelLayout = new GroupLayout(
								(JComponent) optionsPanel);
						optionsPanel.setBorder(javax.swing.BorderFactory
								.createTitledBorder(getResourceMap().getString(
										"optionsPanel.border.title"))); // NOI18N
						optionsPanel.setName("optionsPanel"); // NOI18N
						optionsPanel.setLayout(optionsPanelLayout);
						{
							schemaCheck = new javax.swing.JCheckBox();
							schemaCheck.setText(getResourceMap().getString(
									"schemaCheck.text")); // NOI18N
							schemaCheck.setEnabled(false);
							schemaCheck.setName("schemaCheck"); // NOI18N
						}
						{
							schematronCheck = new javax.swing.JCheckBox();
							schematronCheck.setText(getResourceMap().getString(
									"schematronCheck.text")); // NOI18N
							schematronCheck.setEnabled(false);
							schematronCheck.setName("schematronCheck"); // NOI18N
						}
						{
							xbrlCheck = new javax.swing.JCheckBox();
							xbrlCheck.setText(getResourceMap().getString(
									"xbrlCheck.text")); // NOI18N
							xbrlCheck.setEnabled(false);
							xbrlCheck.setName("xbrlCheck"); // NOI18N
						}
						{
							securityCheck = new javax.swing.JCheckBox();
							securityCheck.setText(getResourceMap().getString(
									"securityCheckBox.text")); // NOI18N
							securityCheck.setName("securityCheck"); // NOI18N
						}
						{
							mtomCheck = new javax.swing.JCheckBox();
							mtomCheck.setText(getResourceMap().getString(
									"mtomCheck.text")); // NOI18N
							mtomCheck.setName("mtomCheck"); // NOI18N
						}
						{
							loggerCheck = new javax.swing.JCheckBox();
							loggerCheck.setText(getResourceMap().getString(
									"loggerCheck.text")); // NOI18N
							loggerCheck.setName("loggerCheck"); // NOI18N
						}
						{
							validateMessageTypeCheck = new javax.swing.JCheckBox();
							validateMessageTypeCheck
									.setText(getResourceMap().getString(
											"validateMessageTypeCheck.text")); // NOI18N
							validateMessageTypeCheck.setEnabled(false);
							validateMessageTypeCheck
									.setName("validateMessageTypeCheck"); // NOI18N
						}
						{
							resendOnErrorCheck = new javax.swing.JCheckBox();
							resendOnErrorCheck.setSelected(true);
							resendOnErrorCheck.setText(getResourceMap().getString(
									"resendOnErrorCheck.text")); //NOI18N
							resendOnErrorCheck.setName("resendOnErrorCheck"); //NOI18N
						}
						optionsPanelLayout.setVerticalGroup(optionsPanelLayout
								.createSequentialGroup().addComponent(
										schemaCheck,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(schematronCheck,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(xbrlCheck,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(securityCheck,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(mtomCheck,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(loggerCheck,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(validateMessageTypeCheck,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(resendOnErrorCheck,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addContainerGap(GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE));
						optionsPanelLayout
								.setHorizontalGroup(optionsPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												optionsPanelLayout
														.createParallelGroup()
														.addGroup(
																GroupLayout.Alignment.LEADING,
																optionsPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				schemaCheck,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				0,
																				22,
																				Short.MAX_VALUE))
														.addComponent(
																schematronCheck,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addGroup(
																GroupLayout.Alignment.LEADING,
																optionsPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				xbrlCheck,
																				GroupLayout.PREFERRED_SIZE,
																				108,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				0,
																				30,
																				Short.MAX_VALUE))
														.addGroup(
																GroupLayout.Alignment.LEADING,
																optionsPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				mtomCheck,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				0,
																				82,
																				Short.MAX_VALUE))
														.addGroup(
																GroupLayout.Alignment.LEADING,
																optionsPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				securityCheck,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				0,
																				78,
																				Short.MAX_VALUE))
														.addGroup(
																GroupLayout.Alignment.LEADING,
																optionsPanelLayout
																		.createSequentialGroup()
																		.addComponent(
																				loggerCheck,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addGap(
																				0,
																				51,
																				Short.MAX_VALUE))
														.addComponent(
																validateMessageTypeCheck,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																resendOnErrorCheck,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE)));
					}
					{
						credentialPanel = new javax.swing.JPanel();
						GroupLayout credentialPanelLayout = new GroupLayout(
								(JComponent) credentialPanel);
						credentialPanel.setLayout(credentialPanelLayout);
						credentialPanel.setBorder(javax.swing.BorderFactory
								.createTitledBorder(getResourceMap().getString(
										"credentialPanel.border.title"))); // NOI18N
						credentialPanel.setName("credentialPanel"); // NOI18N
						{
							keystoreFileLabel = new javax.swing.JLabel();
							keystoreFileLabel.setText(getResourceMap()
									.getString("keystoreFileLabel.text")); // NOI18N
							keystoreFileLabel.setName("keystoreFileLabel"); // NOI18N
						}
						{
							keystoreFileText = new javax.swing.JTextField();
							keystoreFileText.setEditable(false);
							keystoreFileText.setName("keystoreFileText"); // NOI18N
						}
						{
							selectKeystoreButton = new javax.swing.JButton();
							selectKeystoreButton
									.setName("selectKeystoreButton"); // NOI18N
							selectKeystoreButton.setAction(getAppActionMap()
									.get("selectKeyStoreFile")); // NOI18N
							selectKeystoreButton.setText("Select...");
									
						}
						{
							keystoreAliasLabel = new javax.swing.JLabel();
							keystoreAliasLabel.setText(getResourceMap()
									.getString("keystoreAliasLabel.text")); // NOI18N
							keystoreAliasLabel.setName("keystoreAliasLabel"); // NOI18N
						}
						{
							keystoreAliasCombo = new javax.swing.JComboBox();
							keystoreAliasCombo.setName("keystoreAliasCombo"); // NOI18N
						}
						{
							keystoreAliasPasswordLabel = new javax.swing.JLabel();
							keystoreAliasPasswordLabel.setText(getResourceMap()
									.getString(
											"keystoreAliasPasswordLabel.text")); // NOI18N
							keystoreAliasPasswordLabel
									.setName("keystoreAliasPasswordLabel"); // NOI18N
						}
						{
							keystoreAliasPasswordText = new javax.swing.JPasswordField();
							keystoreAliasPasswordText
									.setName("keystoreAliasPasswordText"); // NOI18N
						}
						credentialPanelLayout
								.setVerticalGroup(credentialPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												credentialPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																keystoreFileLabel)
														.addComponent(
																selectKeystoreButton))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												credentialPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																keystoreAliasLabel)
														.addComponent(
																keystoreAliasCombo,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												credentialPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																keystoreAliasPasswordLabel)
														.addComponent(
																keystoreAliasPasswordText,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addContainerGap(
												GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE));
						credentialPanelLayout
								.setHorizontalGroup(credentialPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												credentialPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																keystoreAliasPasswordLabel)
														.addComponent(
																keystoreFileLabel)
														.addComponent(
																keystoreAliasLabel))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												credentialPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.LEADING)
														.addComponent(
																keystoreAliasCombo,
																GroupLayout.Alignment.TRAILING,
																0, 621,
																Short.MAX_VALUE)
														.addComponent(
																keystoreAliasPasswordText,
																GroupLayout.DEFAULT_SIZE,
																621,
																Short.MAX_VALUE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(selectKeystoreButton)
										.addContainerGap());
						credentialPanelLayout
								.setHorizontalGroup(credentialPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												credentialPanelLayout
														.createParallelGroup()
														.addComponent(
																keystoreAliasPasswordLabel,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																179,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																keystoreAliasLabel,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																179,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																keystoreFileLabel,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												credentialPanelLayout
														.createParallelGroup()
														.addComponent(
																keystoreAliasPasswordText,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																601,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																keystoreAliasCombo,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																601,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																keystoreFileText,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																601,
																GroupLayout.PREFERRED_SIZE))
										.addGap(20).addComponent(
												selectKeystoreButton, 0, 82,
												Short.MAX_VALUE)
										.addContainerGap());
						credentialPanelLayout
								.setVerticalGroup(credentialPanelLayout
										.createSequentialGroup()
										.addGroup(
												credentialPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																keystoreFileText,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																keystoreFileLabel,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																selectKeystoreButton,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED,
												0, Short.MAX_VALUE)
										.addGroup(
												credentialPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																keystoreAliasCombo,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																keystoreAliasLabel,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																13,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED,
												0, GroupLayout.PREFERRED_SIZE)
										.addGroup(
												credentialPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																keystoreAliasPasswordText,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																keystoreAliasPasswordLabel,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addContainerGap(16, 16));
						credentialPanelLayout.linkSize(SwingConstants.VERTICAL,
								new Component[] { keystoreFileLabel,
										keystoreAliasLabel,
										keystoreAliasPasswordLabel });
					}
					{
						networkPanel = new javax.swing.JPanel();
						networkPanel.setBorder(javax.swing.BorderFactory
								.createTitledBorder(getResourceMap().getString(
										"networkPanel.border.title"))); // NOI18N
						networkPanel.setName("networkPanel"); // NOI18N
						GroupLayout networkPanelLayout = new GroupLayout(
								(JComponent) networkPanel);
						networkPanel.setLayout(networkPanelLayout);
						{
							coreServicesUrlLabel = new javax.swing.JLabel();
							coreServicesUrlLabel.setText(getResourceMap()
									.getString("coreServicesUrlLabel.text")); // NOI18N
							coreServicesUrlLabel
									.setName("coreServicesUrlLabel"); // NOI18N
						}
						{
							coreServicesUrlText = new javax.swing.JTextField();
							coreServicesUrlText.setName("coreServicesUrlText"); // NOI18N
							coreServicesUrlText.setEditable(false);
						}
						{
							stsUrlLabel = new javax.swing.JLabel();
							stsUrlLabel.setText(getResourceMap().getString(
									"stsUrlLabel.text")); // NOI18N
							stsUrlLabel.setName("stsUrlLabel"); // NOI18N
						}
						{
							stsUrlText = new javax.swing.JTextField();
							stsUrlText.setName("stsUrlText"); // NOI18N
							stsUrlText.setEditable(false);
						}
						networkPanelLayout
								.setVerticalGroup(networkPanelLayout
										.createSequentialGroup()
										.addGroup(
												networkPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																coreServicesUrlText,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																coreServicesUrlLabel,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												networkPanelLayout
														.createParallelGroup(
																GroupLayout.Alignment.BASELINE)
														.addComponent(
																stsUrlText,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																stsUrlLabel,
																GroupLayout.Alignment.BASELINE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED,
												25, Short.MAX_VALUE));
						networkPanelLayout
								.setHorizontalGroup(networkPanelLayout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												networkPanelLayout
														.createParallelGroup()
														.addComponent(
																stsUrlLabel,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																179,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																coreServicesUrlLabel,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																179,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												networkPanelLayout
														.createParallelGroup()
														.addComponent(
																stsUrlText,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																601,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(
																coreServicesUrlText,
																GroupLayout.Alignment.LEADING,
																GroupLayout.PREFERRED_SIZE,
																601,
																GroupLayout.PREFERRED_SIZE))
										.addContainerGap(25, Short.MAX_VALUE));
					}
					{
						sendButton = new javax.swing.JButton();
						sendButton.setAction(getAppActionMap().get(
								"sendRequest")); // NOI18N
						sendButton.setText(getResourceMap().getString(
								"sendButton.text")); // NOI18N
						sendButton.setName("sendButton"); // NOI18N
					}
					submissionPanelLayout
							.setHorizontalGroup(submissionPanelLayout
									.createSequentialGroup()
									.addContainerGap()
									.addGroup(
											submissionPanelLayout
													.createParallelGroup()
													.addGroup(
															GroupLayout.Alignment.LEADING,
															submissionPanelLayout
																	.createSequentialGroup()
																	.addComponent(
																			networkPanel,
																			GroupLayout.PREFERRED_SIZE,
																			927,
																			GroupLayout.PREFERRED_SIZE)
																	.addGap(
																			0,
																			88,
																			Short.MAX_VALUE))
													.addGroup(
															GroupLayout.Alignment.LEADING,
															submissionPanelLayout
																	.createSequentialGroup()
																	.addComponent(
																			credentialPanel,
																			GroupLayout.PREFERRED_SIZE,
																			927,
																			GroupLayout.PREFERRED_SIZE)
																	.addGap(
																			0,
																			88,
																			Short.MAX_VALUE))
													.addGroup(
															GroupLayout.Alignment.LEADING,
															submissionPanelLayout
																	.createSequentialGroup()
																	.addComponent(
																			formPanel,
																			GroupLayout.PREFERRED_SIZE,
																			243,
																			GroupLayout.PREFERRED_SIZE)
																	.addPreferredGap(
																			LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(
																			servicePanel,
																			GroupLayout.PREFERRED_SIZE,
																			180,
																			GroupLayout.PREFERRED_SIZE)
																	.addPreferredGap(
																			LayoutStyle.ComponentPlacement.RELATED)
																	.addGroup(
																			submissionPanelLayout
																					.createParallelGroup()
																					.addComponent(
																							documentPanel,
																							GroupLayout.Alignment.LEADING,
																							GroupLayout.PREFERRED_SIZE,
																							243,
																							GroupLayout.PREFERRED_SIZE)
																					.addGroup(
																							GroupLayout.Alignment.LEADING,
																							submissionPanelLayout
																									.createSequentialGroup()
																									.addGap(
																											90)
																									.addComponent(
																											sendButton,
																											GroupLayout.PREFERRED_SIZE,
																											105,
																											GroupLayout.PREFERRED_SIZE)
																									.addGap(
																											8)))
																	.addPreferredGap(
																			LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(
																			attachmentPanel,
																			GroupLayout.PREFERRED_SIZE,
																			243,
																			GroupLayout.PREFERRED_SIZE)
																	.addPreferredGap(
																			LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(
																			optionsPanel,
																			GroupLayout.PREFERRED_SIZE,
																			167,
																			GroupLayout.PREFERRED_SIZE)
																	.addGap(
																			0,
																			0,
																			Short.MAX_VALUE)))
									.addPreferredGap(
											LayoutStyle.ComponentPlacement.RELATED));
					submissionPanelLayout.linkSize(SwingConstants.HORIZONTAL,
							new Component[] { documentPanel, attachmentPanel,
									formPanel });
					submissionPanelLayout
							.setVerticalGroup(submissionPanelLayout
									.createSequentialGroup()
									.addGap(8)
									.addGroup(
											submissionPanelLayout
													.createParallelGroup()
													.addComponent(
															documentPanel,
															GroupLayout.Alignment.LEADING,
															GroupLayout.PREFERRED_SIZE,
															228,
															GroupLayout.PREFERRED_SIZE)
													.addComponent(
															formPanel,
															GroupLayout.Alignment.LEADING,
															GroupLayout.PREFERRED_SIZE,
															228,
															GroupLayout.PREFERRED_SIZE)
													.addComponent(
															servicePanel,
															GroupLayout.Alignment.LEADING,
															GroupLayout.PREFERRED_SIZE,
															228,
															GroupLayout.PREFERRED_SIZE)
													.addComponent(
															attachmentPanel,
															GroupLayout.Alignment.LEADING,
															GroupLayout.PREFERRED_SIZE,
															228,
															GroupLayout.PREFERRED_SIZE)
													.addComponent(
															optionsPanel,
															GroupLayout.Alignment.LEADING,
															GroupLayout.PREFERRED_SIZE,
															228,
															GroupLayout.PREFERRED_SIZE))
									.addPreferredGap(
											LayoutStyle.ComponentPlacement.RELATED,
											0, GroupLayout.PREFERRED_SIZE)
									.addComponent(credentialPanel,
											GroupLayout.PREFERRED_SIZE, 122,
											GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(
											LayoutStyle.ComponentPlacement.RELATED,
											0, GroupLayout.PREFERRED_SIZE)
									.addComponent(networkPanel,
											GroupLayout.PREFERRED_SIZE, 84,
											GroupLayout.PREFERRED_SIZE).addGap(
											30).addComponent(sendButton,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addContainerGap(37, 37));
					submissionPanelLayout
							.linkSize(SwingConstants.VERTICAL, new Component[] {
									documentPanel, formPanel, servicePanel,
									attachmentPanel, optionsPanel });
				}
				{
					processingPanel = new javax.swing.JPanel();
					topTabbedPane.addTab("Processing Status", null,
							processingPanel, null);
					BorderLayout processingPanelLayout = new BorderLayout();
					processingPanel.setName("processingPanel"); // NOI18N
					processingPanel.setLayout(processingPanelLayout);
					processingPanel.setPreferredSize(new java.awt.Dimension(
							1025, 557));
					{
						requestsPanel = new javax.swing.JPanel();
						processingPanel.add(requestsPanel, BorderLayout.NORTH);
						javax.swing.GroupLayout requestsPanelLayout = new javax.swing.GroupLayout(
								requestsPanel);
						requestsPanel.setBorder(javax.swing.BorderFactory
								.createTitledBorder(getResourceMap().getString(
										"requestsPanel.border.title"))); // NOI18N
						requestsPanel.setName("requestsPanel"); // NOI18N
						requestsPanel.setLayout(requestsPanelLayout);
						{
							requestsScrollPane = new javax.swing.JScrollPane();
						}
						{
							requestsScrollPane.setName("requestsScrollPane"); // NOI18N
							{
								requestTable = new JTable() {
									private static final long serialVersionUID = 1L;

									@Override
									public boolean isCellEditable(int rowIndex, int colIndex) {
										return false;
									}
								};
								requestTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
								requestTable.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseReleased(MouseEvent m) {
										displayPopupActions(m);
									}
								});
								requestsScrollPane
										.setViewportView(requestTable);
							}
						}
						requestsPanelLayout
								.setHorizontalGroup(requestsPanelLayout
										.createSequentialGroup()
										.addContainerGap().addComponent(
												requestsScrollPane, 0, 453,
												Short.MAX_VALUE)
										.addContainerGap());
						requestsPanelLayout
								.setVerticalGroup(requestsPanelLayout
										.createSequentialGroup().addComponent(
												requestsScrollPane, 0, 114,
												Short.MAX_VALUE)
										.addContainerGap());
					}
					{
						responsePanel = new javax.swing.JPanel();
						processingPanel.add(responsePanel, BorderLayout.CENTER);
						responsePanel.setBorder(javax.swing.BorderFactory
								.createTitledBorder(getResourceMap().getString(
										"responsePanel.border.title"))); // NOI18N
						responsePanel.setName("responsePanel"); // NOI18N
						GroupLayout responsePanelLayout = new GroupLayout(
								(JComponent) responsePanel);
						responsePanel.setLayout(responsePanelLayout);
						{
							responseTabbedPane = new JTabbedPane();
							{
								messagePanel = new JPanel();
								responseTabbedPane.addTab("Message", null,
										messagePanel, null);
								GroupLayout messagePanelLayout = new GroupLayout(
										(JComponent) messagePanel);
								messagePanel.setLayout(messagePanelLayout);
								{
									messageTypeLabel = new javax.swing.JLabel();
									messageTypeLabel
											.setText(getResourceMap()
													.getString(
															"messageTypeLabel.text")); // NOI18N
									messageTypeLabel
											.setName("messageTypeLabel"); // NOI18N
								}
								{
									responseMessageTypeLabel = new javax.swing.JLabel();
									responseMessageTypeLabel
											.setName("responseMessageTypeLabel"); // NOI18N
								}
								{
									senderLabel = new javax.swing.JLabel();
									senderLabel.setText(getResourceMap()
											.getString("senderLabel.text")); // NOI18N
									senderLabel.setName("senderLabel"); // NOI18N
								}
								{
									responseSenderLabel = new javax.swing.JLabel();
									responseSenderLabel
											.setName("responseSenderLabel"); // NOI18N
								}
								{
									lodgementReceiptLabel = new javax.swing.JLabel();
									lodgementReceiptLabel
											.setText(getResourceMap()
													.getString(
															"lodgementReceiptLabel.text")); // NOI18N
									lodgementReceiptLabel
											.setName("lodgementReceiptLabel"); // NOI18N
								}
								{
									responseLodgementReceiptLabel = new javax.swing.JLabel();
									responseLodgementReceiptLabel
											.setName("responseLodgementReceiptLabel"); // NOI18N
								}
								{
									messageEventsPanel = new javax.swing.JPanel();
									GroupLayout messageEventsPanelLayout = new GroupLayout(
											(JComponent) messageEventsPanel);
									messageEventsPanel
											.setBorder(javax.swing.BorderFactory
													.createTitledBorder(getResourceMap()
															.getString(
																	"messageEventsPanel.border.title"))); // NOI18N
									messageEventsPanel
											.setName("messageEventsPanel"); // NOI18N
									messageEventsPanel
											.setLayout(messageEventsPanelLayout);
									{
										maximumSeverityLabel = new javax.swing.JLabel();
										maximumSeverityLabel
												.setText(getResourceMap()
														.getString(
																"maximumSeverityLabel.text")); // NOI18N
										maximumSeverityLabel
												.setName("maximumSeverityLabel"); // NOI18N
									}
									{
										responseMaximumSeverityLabel = new javax.swing.JLabel();
										responseMaximumSeverityLabel
												.setName("responseMaximumSeverityLabel"); // NOI18N
									}
									{
										responseMessageEventsCombo = new javax.swing.JComboBox();
										responseMessageEventsCombo
												.setName("responseMessageEventsCombo"); // NOI18N
										responseMessageEventsCombo
												.setRenderer(new DefaultListCellRenderer() {

													/**
                                             *
                                             */
													private static final long serialVersionUID = 1L;

													@Override
													public Component getListCellRendererComponent(
															JList list,
															Object value,
															int index,
															boolean isSelected,
															boolean cellHasFocus) {
														if (value != null) {
															setText(((MessageEventItemType) value)
																	.getMessageEventItemErrorCode());
														}

														return this;
													}
												});
									}
									{
										messageEventPanel = new javax.swing.JPanel();
										messageEventPanel
												.setBorder(javax.swing.BorderFactory
														.createTitledBorder(getResourceMap()
																.getString(
																		"messageEventPanel.border.title"))); // NOI18N
										messageEventPanel
												.setName("messageEventPanel"); // NOI18N
										{
											severityLabel = new javax.swing.JLabel();
											severityLabel
													.setText(getResourceMap()
															.getString(
																	"severityLabel.text")); // NOI18N
											severityLabel
													.setName("severityLabel"); // NOI18N
										}
										{
											shortDescriptionLabel = new javax.swing.JLabel();
											shortDescriptionLabel
													.setText(getResourceMap()
															.getString(
																	"shortDescriptionLabel.text")); // NOI18N
											shortDescriptionLabel
													.setName("shortDescriptionLabel"); // NOI18N
										}
										{
											detailedDescriptionLabel = new javax.swing.JLabel();
											detailedDescriptionLabel
													.setText(getResourceMap()
															.getString(
																	"detailedDescriptionLabel.text")); // NOI18N
											detailedDescriptionLabel
													.setName("detailedDescriptionLabel"); // NOI18N
										}
										{
											locationPanel = new JPanel();
											BorderLayout locationPanelLayout = new BorderLayout();
											locationPanel
													.setLayout(locationPanelLayout);
											locationPanel
													.setBorder(javax.swing.BorderFactory
															.createTitledBorder(getResourceMap()
																	.getString(
																			"locationPanel.border.title"))); // NOI18N
											{
												locationsScrollPane = new JScrollPane();
												locationPanel.add(
														locationsScrollPane,
														BorderLayout.CENTER);
												locationsScrollPane
														.setPreferredSize(new java.awt.Dimension(
																150, 32));
												{
													locationsTable = new JTable();
													locationsScrollPane
															.setViewportView(locationsTable);
												}
											}
										}
										{
											responseSeverityLabel = new JLabel();
										}
										{
											responseShortDescriptionLabel = new JLabel();
										}
										{
											responseDetailedDescriptionTextArea = new JTextArea();
											responseDetailedDescriptionTextArea
													.setEditable(false);
											responseDetailedDescriptionTextArea
													.setWrapStyleWord(true);
											responseDetailedDescriptionTextArea
													.setLineWrap(true);
											responseDetailedDescriptionTextArea
													.setFont(responseShortDescriptionLabel
															.getFont());
											responseDetailedDescriptionTextArea
													.setBackground(responseShortDescriptionLabel
															.getBackground());
										}
										javax.swing.GroupLayout messageEventPanelLayout = new javax.swing.GroupLayout(
												messageEventPanel);
										messageEventPanel
												.setLayout(messageEventPanelLayout);
										messageEventPanelLayout
												.setHorizontalGroup(messageEventPanelLayout
														.createSequentialGroup()
														.addContainerGap()
														.addGroup(
																messageEventPanelLayout
																		.createParallelGroup()
																		.addGroup(
																				messageEventPanelLayout
																						.createSequentialGroup()
																						.addGroup(
																								messageEventPanelLayout
																										.createParallelGroup()
																										.addComponent(
																												detailedDescriptionLabel,
																												GroupLayout.Alignment.LEADING,
																												GroupLayout.PREFERRED_SIZE,
																												GroupLayout.PREFERRED_SIZE,
																												GroupLayout.PREFERRED_SIZE)
																										.addGroup(
																												GroupLayout.Alignment.LEADING,
																												messageEventPanelLayout
																														.createSequentialGroup()
																														.addComponent(
																																shortDescriptionLabel,
																																GroupLayout.PREFERRED_SIZE,
																																GroupLayout.PREFERRED_SIZE,
																																GroupLayout.PREFERRED_SIZE)
																														.addGap(
																																18))
																										.addGroup(
																												GroupLayout.Alignment.LEADING,
																												messageEventPanelLayout
																														.createSequentialGroup()
																														.addComponent(
																																severityLabel,
																																GroupLayout.PREFERRED_SIZE,
																																GroupLayout.PREFERRED_SIZE,
																																GroupLayout.PREFERRED_SIZE)
																														.addGap(
																																65)))
																						.addGap(
																								18)
																						.addGroup(
																								messageEventPanelLayout
																										.createParallelGroup()
																										.addComponent(
																												responseSeverityLabel,
																												GroupLayout.Alignment.LEADING,
																												0,
																												427,
																												Short.MAX_VALUE)
																										.addComponent(
																												responseShortDescriptionLabel,
																												GroupLayout.Alignment.LEADING,
																												0,
																												427,
																												Short.MAX_VALUE)
																										.addComponent(
																												responseDetailedDescriptionTextArea,
																												GroupLayout.Alignment.LEADING,
																												0,
																												427,
																												Short.MAX_VALUE)))
																		.addComponent(
																				locationPanel,
																				GroupLayout.Alignment.LEADING,
																				0,
																				554,
																				Short.MAX_VALUE))
														.addContainerGap());
										messageEventPanelLayout
												.setVerticalGroup(messageEventPanelLayout
														.createSequentialGroup()
														.addGroup(
																messageEventPanelLayout
																		.createParallelGroup(
																				GroupLayout.Alignment.BASELINE)
																		.addComponent(
																				responseSeverityLabel,
																				GroupLayout.Alignment.BASELINE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addComponent(
																				severityLabel,
																				GroupLayout.Alignment.BASELINE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE))
														.addPreferredGap(
																LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(
																messageEventPanelLayout
																		.createParallelGroup(
																				GroupLayout.Alignment.BASELINE)
																		.addComponent(
																				responseShortDescriptionLabel,
																				GroupLayout.Alignment.BASELINE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addComponent(
																				shortDescriptionLabel,
																				GroupLayout.Alignment.BASELINE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE))
														.addPreferredGap(
																LayoutStyle.ComponentPlacement.RELATED)
														.addGroup(
																messageEventPanelLayout
																		.createParallelGroup()
																		.addComponent(
																				responseDetailedDescriptionTextArea,
																				GroupLayout.Alignment.LEADING,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE,
																				GroupLayout.PREFERRED_SIZE)
																		.addGroup(
																				GroupLayout.Alignment.LEADING,
																				messageEventPanelLayout
																						.createSequentialGroup()
																						.addComponent(
																								detailedDescriptionLabel,
																								GroupLayout.PREFERRED_SIZE,
																								GroupLayout.PREFERRED_SIZE,
																								GroupLayout.PREFERRED_SIZE)
																						.addGap(
																								0,
																								24,
																								Short.MAX_VALUE)))
														.addComponent(
																locationPanel,
																GroupLayout.PREFERRED_SIZE,
																117,
																GroupLayout.PREFERRED_SIZE)
														.addContainerGap());
									}
									messageEventsPanelLayout
											.setHorizontalGroup(messageEventsPanelLayout
													.createSequentialGroup()
													.addContainerGap()
													.addGroup(
															messageEventsPanelLayout
																	.createParallelGroup()
																	.addGroup(
																			GroupLayout.Alignment.LEADING,
																			messageEventsPanelLayout
																					.createSequentialGroup()
																					.addComponent(
																							maximumSeverityLabel,
																							GroupLayout.PREFERRED_SIZE,
																							108,
																							GroupLayout.PREFERRED_SIZE)
																					.addPreferredGap(
																							LayoutStyle.ComponentPlacement.RELATED)
																					.addComponent(
																							responseMaximumSeverityLabel,
																							GroupLayout.PREFERRED_SIZE,
																							291,
																							GroupLayout.PREFERRED_SIZE)
																					.addGap(
																							0,
																							235,
																							Short.MAX_VALUE))
																	.addComponent(
																			messageEventPanel,
																			GroupLayout.Alignment.LEADING,
																			0,
																			640,
																			Short.MAX_VALUE)
																	.addComponent(
																			responseMessageEventsCombo,
																			GroupLayout.Alignment.LEADING,
																			0,
																			640,
																			Short.MAX_VALUE))
													.addContainerGap());
									messageEventsPanelLayout
											.setVerticalGroup(messageEventsPanelLayout
													.createSequentialGroup()
													.addGroup(
															messageEventsPanelLayout
																	.createParallelGroup()
																	.addComponent(
																			maximumSeverityLabel,
																			GroupLayout.Alignment.LEADING,
																			GroupLayout.PREFERRED_SIZE,
																			GroupLayout.PREFERRED_SIZE,
																			GroupLayout.PREFERRED_SIZE)
																	.addComponent(
																			responseMaximumSeverityLabel,
																			GroupLayout.Alignment.LEADING,
																			GroupLayout.PREFERRED_SIZE,
																			14,
																			GroupLayout.PREFERRED_SIZE))
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addComponent(
															responseMessageEventsCombo,
															GroupLayout.PREFERRED_SIZE,
															GroupLayout.PREFERRED_SIZE,
															GroupLayout.PREFERRED_SIZE)
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.UNRELATED)
													.addComponent(
															messageEventPanel,
															0, 250,
															Short.MAX_VALUE)
													.addGap(7));
								}
								{
									timestampsPanel = new JPanel();
									BorderLayout timestampsPanelLayout = new BorderLayout();
									timestampsPanel
											.setLayout(timestampsPanelLayout);
									timestampsPanel
											.setBorder(BorderFactory
													.createTitledBorder(getResourceMap()
															.getString(
																	"timestampsPanel.border.title")));
									timestampsPanel.setName("timestampsPanel");
									{
										timestampsScrollPane = new JScrollPane();
										timestampsPanel.add(
												timestampsScrollPane,
												BorderLayout.CENTER);
										timestampsScrollPane
												.setPreferredSize(new java.awt.Dimension(
														319, 189));
										{
											timestampsTable = new JTable();
											timestampsTable.setEnabled(false);
											timestampsScrollPane
													.setViewportView(timestampsTable);
										}
									}
								}
								messagePanelLayout
										.setHorizontalGroup(messagePanelLayout
												.createSequentialGroup()
												.addContainerGap()
												.addGroup(
														messagePanelLayout
																.createParallelGroup()
																.addGroup(
																		messagePanelLayout
																				.createSequentialGroup()
																				.addGroup(
																						messagePanelLayout
																								.createParallelGroup()
																								.addComponent(
																										lodgementReceiptLabel,
																										GroupLayout.Alignment.LEADING,
																										GroupLayout.PREFERRED_SIZE,
																										135,
																										GroupLayout.PREFERRED_SIZE)
																								.addGroup(
																										GroupLayout.Alignment.LEADING,
																										messagePanelLayout
																												.createSequentialGroup()
																												.addComponent(
																														senderLabel,
																														GroupLayout.PREFERRED_SIZE,
																														108,
																														GroupLayout.PREFERRED_SIZE)
																												.addGap(
																														27))
																								.addGroup(
																										GroupLayout.Alignment.LEADING,
																										messagePanelLayout
																												.createSequentialGroup()
																												.addComponent(
																														messageTypeLabel,
																														GroupLayout.PREFERRED_SIZE,
																														124,
																														GroupLayout.PREFERRED_SIZE)
																												.addGap(
																														11)))
																				.addGap(
																						16)
																				.addGroup(
																						messagePanelLayout
																								.createParallelGroup()
																								.addComponent(
																										responseMessageTypeLabel,
																										GroupLayout.Alignment.LEADING,
																										GroupLayout.PREFERRED_SIZE,
																										183,
																										GroupLayout.PREFERRED_SIZE)
																								.addComponent(
																										responseSenderLabel,
																										GroupLayout.Alignment.LEADING,
																										GroupLayout.PREFERRED_SIZE,
																										183,
																										GroupLayout.PREFERRED_SIZE)
																								.addComponent(
																										responseLodgementReceiptLabel,
																										GroupLayout.Alignment.LEADING,
																										GroupLayout.PREFERRED_SIZE,
																										183,
																										GroupLayout.PREFERRED_SIZE))
																				.addGap(
																						19))
																.addComponent(
																		timestampsPanel,
																		GroupLayout.Alignment.LEADING,
																		GroupLayout.PREFERRED_SIZE,
																		353,
																		GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(
														LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(
														messageEventsPanel, 0,
														674, Short.MAX_VALUE));
								messagePanelLayout
										.setVerticalGroup(messagePanelLayout
												.createParallelGroup()
												.addComponent(
														messageEventsPanel,
														GroupLayout.Alignment.LEADING,
														0, 339, Short.MAX_VALUE)
												.addGroup(
														GroupLayout.Alignment.LEADING,
														messagePanelLayout
																.createSequentialGroup()
																.addGap(12)
																.addGroup(
																		messagePanelLayout
																				.createParallelGroup()
																				.addComponent(
																						responseMessageTypeLabel,
																						GroupLayout.Alignment.LEADING,
																						GroupLayout.PREFERRED_SIZE,
																						16,
																						GroupLayout.PREFERRED_SIZE)
																				.addComponent(
																						messageTypeLabel,
																						GroupLayout.Alignment.LEADING,
																						GroupLayout.PREFERRED_SIZE,
																						GroupLayout.PREFERRED_SIZE,
																						GroupLayout.PREFERRED_SIZE))
																.addPreferredGap(
																		LayoutStyle.ComponentPlacement.UNRELATED)
																.addGroup(
																		messagePanelLayout
																				.createParallelGroup()
																				.addComponent(
																						responseSenderLabel,
																						GroupLayout.Alignment.LEADING,
																						GroupLayout.PREFERRED_SIZE,
																						15,
																						GroupLayout.PREFERRED_SIZE)
																				.addComponent(
																						senderLabel,
																						GroupLayout.Alignment.LEADING,
																						GroupLayout.PREFERRED_SIZE,
																						GroupLayout.PREFERRED_SIZE,
																						GroupLayout.PREFERRED_SIZE))
																.addPreferredGap(
																		LayoutStyle.ComponentPlacement.UNRELATED)
																.addGroup(
																		messagePanelLayout
																				.createParallelGroup()
																				.addComponent(
																						responseLodgementReceiptLabel,
																						GroupLayout.Alignment.LEADING,
																						GroupLayout.PREFERRED_SIZE,
																						16,
																						GroupLayout.PREFERRED_SIZE)
																				.addComponent(
																						lodgementReceiptLabel,
																						GroupLayout.Alignment.LEADING,
																						GroupLayout.PREFERRED_SIZE,
																						GroupLayout.PREFERRED_SIZE,
																						GroupLayout.PREFERRED_SIZE))
																.addPreferredGap(
																		LayoutStyle.ComponentPlacement.UNRELATED)
																.addComponent(
																		timestampsPanel,
																		0,
																		243,
																		Short.MAX_VALUE)));
							}
							{
								businessDocumentsPanel = new JPanel();
								BoxLayout businessDocumentsPanelLayout = new BoxLayout(
										businessDocumentsPanel,
										javax.swing.BoxLayout.Y_AXIS);
								businessDocumentsPanel
										.setLayout(businessDocumentsPanelLayout);
								responseTabbedPane.addTab("Business Documents",
										null, businessDocumentsPanel, null);
								businessDocumentsPanel
										.setPreferredSize(new java.awt.Dimension(
												953, 339));
								{
									responseBusinessDocumentsPanel = new JPanel();
									GroupLayout responseBusinessDocumentsPanelLayout = new GroupLayout(
											(JComponent) responseBusinessDocumentsPanel);
									responseBusinessDocumentsPanel
											.setLayout(responseBusinessDocumentsPanelLayout);
									businessDocumentsPanel
											.add(responseBusinessDocumentsPanel);
									responseBusinessDocumentsPanel
											.setBorder(BorderFactory
													.createTitledBorder(getResourceMap()
															.getString(
																	"businessDocumentsPanel.border.title"))); // NOI18N
									responseBusinessDocumentsPanel
											.setPreferredSize(new java.awt.Dimension(
													944, 184));
									{
										xbrlResponseScrollPane = new JScrollPane();
										{
											xbrlResponseList = new JList();
											xbrlResponseScrollPane
													.setViewportView(xbrlResponseList);
										}
									}
									{
										viewDocumentDetailsButton = new JButton();
										viewDocumentDetailsButton
												.setName("viewDocumentDetailsButton");
										viewDocumentDetailsButton
												.setAction(getAppActionMap()
														.get(
																"editBusinessDocumentAction")); // NOI18N
									}
									{
										viewXbrlButton = new JButton();
										viewXbrlButton
												.setName("viewXbrlButton");
										viewXbrlButton
												.setAction(getAppActionMap()
														.get(
																"viewBusinessDocumentAction")); // NOI18N
									}
									{
										saveXbrlButton = new JButton();
										saveXbrlButton
												.setName("saveXbrlButton");
										saveXbrlButton
												.setAction(getAppActionMap()
														.get(
																"saveBusinessDocumentAction")); // NOI18N
									}
									responseBusinessDocumentsPanelLayout
											.setHorizontalGroup(responseBusinessDocumentsPanelLayout
													.createSequentialGroup()
													.addContainerGap()
													.addGroup(
															responseBusinessDocumentsPanelLayout
																	.createParallelGroup()
																	.addComponent(
																			xbrlResponseScrollPane,
																			GroupLayout.Alignment.LEADING,
																			0,
																			959,
																			Short.MAX_VALUE)
																	.addGroup(
																			GroupLayout.Alignment.LEADING,
																			responseBusinessDocumentsPanelLayout
																					.createSequentialGroup()
																					.addGap(
																							0,
																							675,
																							Short.MAX_VALUE)
																					.addComponent(
																							viewDocumentDetailsButton,
																							GroupLayout.PREFERRED_SIZE,
																							95,
																							GroupLayout.PREFERRED_SIZE)
																					.addPreferredGap(
																							LayoutStyle.ComponentPlacement.RELATED)
																					.addComponent(
																							viewXbrlButton,
																							GroupLayout.PREFERRED_SIZE,
																							90,
																							GroupLayout.PREFERRED_SIZE)
																					.addPreferredGap(
																							LayoutStyle.ComponentPlacement.RELATED)
																					.addComponent(
																							saveXbrlButton,
																							GroupLayout.PREFERRED_SIZE,
																							90,
																							GroupLayout.PREFERRED_SIZE)))
													.addContainerGap());
									responseBusinessDocumentsPanelLayout
											.setVerticalGroup(responseBusinessDocumentsPanelLayout
													.createSequentialGroup()
													.addComponent(
															xbrlResponseScrollPane,
															GroupLayout.PREFERRED_SIZE,
															123,
															GroupLayout.PREFERRED_SIZE)
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED,
															0, Short.MAX_VALUE)
													.addGroup(
															responseBusinessDocumentsPanelLayout
																	.createParallelGroup(
																			GroupLayout.Alignment.BASELINE)
																	.addComponent(
																			viewDocumentDetailsButton,
																			GroupLayout.Alignment.BASELINE,
																			GroupLayout.PREFERRED_SIZE,
																			23,
																			GroupLayout.PREFERRED_SIZE)
																	.addComponent(
																			viewXbrlButton,
																			GroupLayout.Alignment.BASELINE,
																			GroupLayout.PREFERRED_SIZE,
																			23,
																			GroupLayout.PREFERRED_SIZE)
																	.addComponent(
																			saveXbrlButton,
																			GroupLayout.Alignment.BASELINE,
																			GroupLayout.PREFERRED_SIZE,
																			23,
																			GroupLayout.PREFERRED_SIZE))
													.addContainerGap());
								}
								{
									responseAttachmentsPanel = new JPanel();
									GroupLayout responseAttachmentsPanelLayout = new GroupLayout(
											(JComponent) responseAttachmentsPanel);
									responseAttachmentsPanel
											.setLayout(responseAttachmentsPanelLayout);
									businessDocumentsPanel
											.add(responseAttachmentsPanel);
									responseAttachmentsPanel
											.setBorder(BorderFactory
													.createTitledBorder(getResourceMap()
															.getString(
																	"attachmentsPanel.border.title"))); // NOI18N
									responseAttachmentsPanel
											.setPreferredSize(new java.awt.Dimension(
													891, 155));
									{
										attachmentResponseScrollPane = new JScrollPane();
										{
											attachmentResponseList = new JList();
											attachmentResponseScrollPane
													.setViewportView(attachmentResponseList);
										}
									}
									{
										viewAttachmentButton = new JButton();
										viewAttachmentButton
												.setName("viewAttachmentButton");
										viewAttachmentButton
												.setAction(getAppActionMap()
														.get(
																"viewAttachmentAction")); // NOI18N
									}
									{
										saveAttachmentButton = new JButton();
										saveAttachmentButton
												.setName("saveAttachmentButton");
										saveAttachmentButton
												.setAction(getAppActionMap()
														.get(
																"saveAttachmentAction")); // NOI18N
									}
									responseAttachmentsPanelLayout
											.setHorizontalGroup(responseAttachmentsPanelLayout
													.createSequentialGroup()
													.addContainerGap()
													.addGroup(
															responseAttachmentsPanelLayout
																	.createParallelGroup()
																	.addComponent(
																			attachmentResponseScrollPane,
																			GroupLayout.Alignment.LEADING,
																			0,
																			958,
																			Short.MAX_VALUE)
																	.addGroup(
																			GroupLayout.Alignment.LEADING,
																			responseAttachmentsPanelLayout
																					.createSequentialGroup()
																					.addGap(
																							0,
																							774,
																							Short.MAX_VALUE)
																					.addComponent(
																							viewAttachmentButton,
																							GroupLayout.PREFERRED_SIZE,
																							90,
																							GroupLayout.PREFERRED_SIZE)
																					.addPreferredGap(
																							LayoutStyle.ComponentPlacement.RELATED,
																							0,
																							GroupLayout.PREFERRED_SIZE)
																					.addComponent(
																							saveAttachmentButton,
																							GroupLayout.PREFERRED_SIZE,
																							90,
																							GroupLayout.PREFERRED_SIZE)))
													.addContainerGap());
									responseAttachmentsPanelLayout
											.setVerticalGroup(responseAttachmentsPanelLayout
													.createSequentialGroup()
													.addComponent(
															attachmentResponseScrollPane,
															GroupLayout.PREFERRED_SIZE,
															94,
															GroupLayout.PREFERRED_SIZE)
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.UNRELATED,
															1, Short.MAX_VALUE)
													.addGroup(
															responseAttachmentsPanelLayout
																	.createParallelGroup(
																			GroupLayout.Alignment.BASELINE)
																	.addComponent(
																			viewAttachmentButton,
																			GroupLayout.Alignment.BASELINE,
																			GroupLayout.PREFERRED_SIZE,
																			23,
																			GroupLayout.PREFERRED_SIZE)
																	.addComponent(
																			saveAttachmentButton,
																			GroupLayout.Alignment.BASELINE,
																			GroupLayout.PREFERRED_SIZE,
																			23,
																			GroupLayout.PREFERRED_SIZE)));
								}
							}
							{
						        codeLabel = new javax.swing.JLabel();
						        faultCodeTextField = new javax.swing.JTextField();
						        subcodeCombo = new javax.swing.JComboBox();					        
						        messageEventItemPanel = new javax.swing.JPanel();
						        subcodeLabel = new javax.swing.JLabel();
						        subcodeTextField = new javax.swing.JTextField();
						        reasonLabel = new javax.swing.JLabel();
						        reasonTextField = new javax.swing.JTextField();
						        nodeLabel = new javax.swing.JLabel();
						        nodeTextField = new javax.swing.JTextField();
						        detailLabel = new javax.swing.JLabel();
						        jScrollPane1 = new javax.swing.JScrollPane();
						        detailTextArea = new javax.swing.JTextArea();
						        notifyUserTextField = new javax.swing.JTextField();
						        exceptionDetailsPanel = new javax.swing.JPanel();

						        exceptionDetailsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Exception Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N
						        responseTabbedPane.addTab("Exception",
										null, exceptionDetailsPanel, null);

						        codeLabel.setText("Code:");
						        codeLabel.setName(""); // NOI18N

						        faultCodeTextField.setEditable(false);
						        faultCodeTextField.setText("<fault code>");
						        faultCodeTextField.setBorder(null);

						        messageEventItemPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, null, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(0, 0, 0))); // NOI18N
						        messageEventItemPanel.setName(""); // NOI18N

						        subcodeLabel.setText("Subcode:");

						        subcodeTextField.setEditable(false);
						        subcodeTextField.setText("<subcode level 2>");
						        subcodeTextField.setBorder(null);

						        reasonLabel.setText("Reason:");

						        reasonTextField.setEditable(false);
						        reasonTextField.setText("<reason>");
						        reasonTextField.setBorder(null);

						        nodeLabel.setText("Node:");

						        nodeTextField.setEditable(false);
						        nodeTextField.setText("<node>");
						        nodeTextField.setBorder(null);

						        detailLabel.setText("Detail:");

						        detailTextArea.setColumns(20);
						        detailTextArea.setEditable(false);
						        detailTextArea.setLineWrap(true);
						        detailTextArea.setRows(5);
						        detailTextArea.setWrapStyleWord(true);
						        detailTextArea.setBorder(null);
						        detailTextArea.setOpaque(false);
						        jScrollPane1.setViewportView(detailTextArea);

						        notifyUserTextField.setEditable(false);
						        notifyUserTextField.setText("<notify user>");
						        notifyUserTextField.setBorder(null);

						        javax.swing.GroupLayout messageEventItemPanelLayout = new javax.swing.GroupLayout(messageEventItemPanel);
						        messageEventItemPanel.setLayout(messageEventItemPanelLayout);

						        messageEventItemPanelLayout.setHorizontalGroup(
						            messageEventItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						            .addGroup(messageEventItemPanelLayout.createSequentialGroup()
						                .addContainerGap()
						                .addGroup(messageEventItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						                    .addComponent(subcodeLabel)
						                    .addComponent(reasonLabel)
						                    .addComponent(nodeLabel)
						                    .addComponent(detailLabel))
						                .addGap(18, 18, 18)
						                .addGroup(messageEventItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						                    .addComponent(reasonTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
						                    .addComponent(nodeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
						                    .addComponent(subcodeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
						                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
						                    .addComponent(notifyUserTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE))
						                .addContainerGap())
						        );
						        messageEventItemPanelLayout.setVerticalGroup(
						            messageEventItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						            .addGroup(messageEventItemPanelLayout.createSequentialGroup()
						                .addGroup(messageEventItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						                    .addComponent(subcodeLabel)
						                    .addComponent(subcodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						                .addGroup(messageEventItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						                    .addComponent(reasonLabel)
						                    .addComponent(reasonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						                .addGroup(messageEventItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						                    .addComponent(nodeLabel)
						                    .addComponent(nodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						                .addGroup(messageEventItemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						                    .addComponent(detailLabel)
						                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						                .addComponent(notifyUserTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						        );

						        GroupLayout exceptionDetailsPanelLayout = new GroupLayout((JComponent)exceptionDetailsPanel);
						        exceptionDetailsPanel.setLayout(exceptionDetailsPanelLayout);
						        exceptionDetailsPanelLayout.setHorizontalGroup(
						        		exceptionDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, exceptionDetailsPanelLayout.createSequentialGroup()
						                .addContainerGap()
						                .addGroup(exceptionDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
						                    .addComponent(messageEventItemPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						                    .addComponent(subcodeCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, 585, Short.MAX_VALUE)
						                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, exceptionDetailsPanelLayout.createSequentialGroup()
						                        .addComponent(codeLabel)
						                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						                        .addComponent(faultCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
						                .addContainerGap())
						        );
						        exceptionDetailsPanelLayout.setVerticalGroup(
						        		exceptionDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						            .addGroup(exceptionDetailsPanelLayout.createSequentialGroup()
						                .addContainerGap()
						                .addGroup(exceptionDetailsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
						                    .addComponent(codeLabel)
						                    .addComponent(faultCodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						                .addComponent(subcodeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						                .addGap(18, 18, 18)
						                .addComponent(messageEventItemPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						        );
							}
						}
						responsePanelLayout
								.setHorizontalGroup(responsePanelLayout
										.createSequentialGroup()
										.addContainerGap().addComponent(
												responseTabbedPane, 0, 1050,
												Short.MAX_VALUE)
										.addContainerGap());
						responsePanelLayout
								.setVerticalGroup(responsePanelLayout
										.createSequentialGroup().addComponent(
												responseTabbedPane, 0, 367,
												Short.MAX_VALUE)
										.addContainerGap());
					}
				}
			}
			{
				headerPanel = new JPanel();
				mainPanel.add(headerPanel, BorderLayout.NORTH);
				BorderLayout headerPanelLayout = new BorderLayout();
				headerPanel.setLayout(headerPanelLayout);
				headerPanel.setName("headerPanel");
				headerPanel.setPreferredSize(new java.awt.Dimension(1013, 114));
				headerPanel.setBackground(sbrBlue);
				{
					sbrLogoLabel = new JLabel();
					headerPanel.add(sbrLogoLabel, BorderLayout.WEST);
					sbrLogoLabel.setName("sbrLogoLabel");
					sbrLogoLabel.setIcon(getResourceMap().getIcon(
							"sbrLogoLabel.icon"));
					sbrLogoLabel.setPreferredSize(new java.awt.Dimension(592,
							115));
				}
				{
					sbrIconLabel = new JLabel();
					headerPanel.add(sbrIconLabel, BorderLayout.EAST);
					sbrIconLabel.setName("sbrIconLabel");
					sbrIconLabel.setIcon(getResourceMap().getIcon(
							"sbrIconLabel.icon"));
					sbrIconLabel.setPreferredSize(new java.awt.Dimension(312,
							122));
					sbrIconLabel.setSize(200, 122);
				}
			}
		}

		setComponent(mainPanel);
		// setMenuBar(menuBar);
		setStatusBar(statusPanel);

		bindingGroup = new BindingGroup();

		Binding binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.listServiceSelected}"),
				listRadio, BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.lodgeServiceSelected}"),
				lodgeRadio, BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.prefillServiceSelected}"),
				prefillRadio, BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.prelodgeServiceSelected}"),
				prelodgeRadio, BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.altListServiceSelected}"),
				altListRadio, BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.altLodgeServiceSelected}"),
				altLodgeRadio, BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		ELProperty eLProperty = ELProperty
				.create("${clientBean.businessDocuments}");
		JListBinding jListBinding = SwingBindings.createJListBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty,
				businessDocumentList);
		jListBinding.setDetailBinding(ELProperty.create("${filename}"));
		bindingGroup.addBinding(jListBinding);
		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.selectedBusinessDocument}"),
				businessDocumentList, BeanProperty.create("selectedElement"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.businessDocumentSelected}"),
				removeBusinessDocumentButton, BeanProperty.create("enabled"));
		bindingGroup.addBinding(binding);

		eLProperty = ELProperty
				.create("${clientBean.selectedBusinessDocument.attachments}");
		jListBinding = SwingBindings.createJListBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty,
				attachmentList);
		jListBinding.setDetailBinding(ELProperty.create("${fileName}"));
		bindingGroup.addBinding(jListBinding);
		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${clientBean.selectedBusinessDocument.selectedAttachment}"),
						attachmentList, BeanProperty.create("selectedElement"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.businessDocumentSelected}"),
				addAttachmentButton, BeanProperty.create("enabled"));
		bindingGroup.addBinding(binding);

		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${clientBean.businessDocumentSelected && clientBean.selectedBusinessDocument.attachmentSelected}"),
						removeAttachmentButton, BeanProperty.create("enabled"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.schemaValidationRequest}"),
				schemaCheck, BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.schematronValidationRequest}"),
				schematronCheck, BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.xbrlValidationRequest}"),
				xbrlCheck, BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.mtomRequest}"), mtomCheck,
				BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.loggerRequest}"), loggerCheck,
				BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.validateMessageTypeResponse}"),
				validateMessageTypeCheck, BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.securityRequest}"),
				securityCheck, BeanProperty.create("selected"));
		bindingGroup.addBinding(binding);

		eLProperty = ELProperty.create("${clientBean.forms}");
		jListBinding = SwingBindings.createJListBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty,
				formList);
		jListBinding.setDetailBinding(ELProperty
				.create("${name} (${receiver})"));
		bindingGroup.addBinding(jListBinding);
		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.selectedForm}"), formList,
				BeanProperty.create("selectedElement"));
		//binding.setSourceNullValue(clientBean.getForms().get(0));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, securityCheck,
				BeanProperty.create("selected"), keystoreFileText, BeanProperty
						.create("enabled"));
		bindingGroup.addBinding(binding);
		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, securityCheck,
				BeanProperty.create("selected"), keystoreAliasCombo,
				BeanProperty.create("enabled"));
		bindingGroup.addBinding(binding);
		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, securityCheck,
				BeanProperty.create("selected"), keystoreAliasPasswordText,
				BeanProperty.create("enabled"));
		bindingGroup.addBinding(binding);
		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, securityCheck,
				BeanProperty.create("selected"), selectKeystoreButton,
				BeanProperty.create("enabled"));
		bindingGroup.addBinding(binding);

		eLProperty = ELProperty.create("${clientBean.keyStore.keyStoreName}");
		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty,
				keystoreFileText, BeanProperty.create("text"));
		bindingGroup.addBinding(binding);

		eLProperty = ELProperty.create("${clientBean.keyStore.aliases}");
		JComboBoxBinding jComboBoxBinding = SwingBindings
				.createJComboBoxBinding(AutoBinding.UpdateStrategy.READ_WRITE,
						this, eLProperty, keystoreAliasCombo);
		bindingGroup.addBinding(jComboBoxBinding);
		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.keyStore.alias}"),
				keystoreAliasCombo, BeanProperty.create("selectedItem"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.keyStore.password}"),
				keystoreAliasPasswordText, BeanProperty.create("text"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.endpoint}"), coreServicesUrlText,
				BeanProperty.create("text"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.stsEndpoint}"), stsUrlText,
				BeanProperty.create("text"));
		bindingGroup.addBinding(binding);

		eLProperty = ELProperty.create("${clientBean.responses}");
		JTableBinding tb = SwingBindings.createJTableBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty,
				requestTable);
		ColumnBinding cb = tb.addColumnBinding(ELProperty
				.create("${requestTimestamp}"));
		cb.setColumnName("Sent");
		cb.setColumnClass(String.class);
		cb.setConverter(new XMLGregorianCalendarToTimestampConverter());
		cb = tb.addColumnBinding(ELProperty.create("${type}"));
		cb.setColumnName("Type");
		cb.setColumnClass(String.class);
		cb = tb.addColumnBinding(ELProperty.create("${status}"));
		cb.setColumnName("Status");
		cb.setColumnClass(String.class);	
		cb = tb.addColumnBinding(ELProperty.create("${details}"));
		cb.setColumnName("Details");
		cb.setColumnClass(String.class);
		bindingGroup.addBinding(tb);

		eLProperty = ELProperty
				.create("${clientBean.selectedResponse.responseTimestamps}");
		tb = SwingBindings.createJTableBinding(AutoBinding.UpdateStrategy.READ,
				this, eLProperty, timestampsTable);
		cb = tb.addColumnBinding(ELProperty
				.create("${messageTimestampGenerationSourceCode}"));
		cb.setColumnName("Source");
		cb.setColumnClass(String.class);
		cb
				.setConverter(new MessageTimestampGenerationSourceCodeTypeToStringConverter());
		cb = tb.addColumnBinding(ELProperty
				.create("${messageTimestampGenerationDatetime}"));
		cb.setColumnName("Timestamp");
		cb.setColumnClass(String.class);
		cb.setConverter(new XMLGregorianCalendarToTimestampConverter());
		bindingGroup.addBinding(tb);

		binding = Bindings.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, ELProperty
						.create("${clientBean.selectedResponse}"),
				requestTable, BeanProperty.create("selectedElement"));
		bindingGroup.addBinding(binding);

		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${(clientBean.selectedResponse == null || clientBean.selectedResponse.SOAPFault == null) ? clientBean.selectedResponse.response.standardBusinessDocumentHeader.messageTypeText : \"\"}"),
						responseMessageTypeLabel, BeanProperty.create("text"));
		bindingGroup.addBinding(binding);

		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${(clientBean.selectedResponse == null || clientBean.selectedResponse.SOAPFault == null) ? clientBean.selectedResponse.response.standardBusinessDocumentHeader.sender.identificationDetailsIdentifierDesignationText : \"\"}"),
						responseSenderLabel, BeanProperty.create("text"));
		bindingGroup.addBinding(binding);

		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${(clientBean.selectedResponse == null || clientBean.selectedResponse.response.standardBusinessDocumentHeader.lodgementReceipt == null) ? \"\" : clientBean.selectedResponse.response.standardBusinessDocumentHeader.lodgementReceipt.lodgementReceiptIdentifier}"),
						responseLodgementReceiptLabel, BeanProperty.create("text"));
		bindingGroup.addBinding(binding);

		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${(clientBean.selectedResponse == null || clientBean.selectedResponse.SOAPFault == null) ? clientBean.selectedResponse.response.standardBusinessDocumentHeader.messageEvent.messageEventMaximumSeverityCode : null}"),
						responseMaximumSeverityLabel, BeanProperty
								.create("text"));
		binding
				.setConverter(new MessageEventItemSeverityCodeTypeToStringConverter());
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, 
				this, ELProperty.create("${(clientBean.selectedResponse == null || clientBean.selectedResponse.SOAPFault == null ? null : clientBean.selectedResponse.SOAPFault.faultCode)}"),
				faultCodeTextField, BeanProperty.create("text"));
		bindingGroup.addBinding(binding);

		eLProperty = ELProperty.create("${clientBean.selectedResponse.responseSOAPsubcodes}");
		jComboBoxBinding = SwingBindings.createJComboBoxBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, subcodeCombo);
		binding.setConverter(new SBRCodeToStringConverter());
		bindingGroup.addBinding(jComboBoxBinding);
		
		binding = Bindings
		.createAutoBinding(
				AutoBinding.UpdateStrategy.READ_WRITE,
				this,
				ELProperty
						.create("${(clientBean.selectedResponse.selectedSOAPsubcodes.value)}"),
				subcodeCombo, BeanProperty.create("selectedItem"));

		binding.setConverter(new SBRCodeToStringConverter());
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, 
				this, ELProperty.create("${clientBean.selectedResponse == null || clientBean.selectedResponse.SOAPFault == null ? null : clientBean.selectedResponse.SOAPFault.faultSubcode}"),
				subcodeTextField, BeanProperty.create("text"));

		binding.setConverter(new SBRCodeToStringConverter());
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE,
				this, ELProperty.create("${clientBean.selectedResponse == null || clientBean.selectedResponse.SOAPFault == null ? null : clientBean.selectedResponse.SOAPFault.faultReason}"),
				reasonTextField, BeanProperty.create("text"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE,
				this, ELProperty.create("${clientBean.selectedResponse == null || clientBean.selectedResponse.SOAPFault == null ? null : clientBean.selectedResponse.SOAPFault.faultNode}"),
				nodeTextField, BeanProperty.create("text"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE,
				this, ELProperty.create("${clientBean.selectedResponse == null || clientBean.selectedResponse.SOAPFault == null || clientBean.selectedResponse.SOAPFault.detail == null ? \"\" : clientBean.selectedResponse.SOAPFault.detail}"),
				detailTextArea, BeanProperty.create("text"));
		bindingGroup.addBinding(binding);

		binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, 
				this, ELProperty.create("${clientBean.selectedResponse == null || clientBean.selectedResponse.SOAPFault == null ? \"\" : clientBean.selectedResponse.SOAPFault.notifyUser}"), 
				notifyUserTextField, BeanProperty.create("text"));
		bindingGroup.addBinding(binding);
		
		eLProperty = ELProperty
				.create("${clientBean.selectedResponse.responseMessageEventItems}");
		jComboBoxBinding = SwingBindings.createJComboBoxBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty,
				responseMessageEventsCombo);
		bindingGroup.addBinding(jComboBoxBinding);

		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${clientBean.selectedResponse.selectedMessageEventItem}"),
						responseMessageEventsCombo, BeanProperty
								.create("selectedItem"));
		bindingGroup.addBinding(binding);

		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${(clientBean.selectedResponse == null || clientBean.selectedResponse.SOAPFault == null) ? clientBean.selectedResponse.selectedMessageEventItem.messageEventItemSeverityCode : null}"),
						responseSeverityLabel, BeanProperty.create("text"));
		binding
				.setConverter(new MessageEventItemSeverityCodeTypeToStringConverter());
		bindingGroup.addBinding(binding);

		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${clientBean.selectedResponse.selectedMessageEventItem}"),
						responseShortDescriptionLabel, BeanProperty
								.create("text"));
		binding.setConverter(new InsertParametersToStringConverter("short"));
		bindingGroup.addBinding(binding);

		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${clientBean.selectedResponse.selectedMessageEventItem}"),
						responseDetailedDescriptionTextArea, BeanProperty
								.create("text"));
		binding.setConverter(new InsertParametersToStringConverter("detailed"));
		bindingGroup.addBinding(binding);

		eLProperty = ELProperty
				.create("${clientBean.selectedResponse.selectedMessageEventItem.messageEventItemLocations}");
		tb = SwingBindings.createJTableBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty,
				locationsTable);
		cb = tb.addColumnBinding(ELProperty
				.create("${businessDocumentSequenceNumber}"));
		cb.setColumnName("Doc Seq No.");
		cb.setColumnClass(BigInteger.class);
		cb = tb.addColumnBinding(ELProperty
				.create("${messageEventItemLocationPathText}"));
		cb.setColumnName("Path");
		cb.setColumnClass(String.class);
		bindingGroup.addBinding(tb);

		eLProperty = ELProperty
				.create("${clientBean.selectedResponse.businessDocuments}");
		jListBinding = SwingBindings.createJListBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty,
				xbrlResponseList);
		jListBinding
				.setDetailBinding(ELProperty
						.create("${sequenceNumber} -- ${businessGeneratedIdentifier} -- ${governmentGeneratedIdentifier}"));
		bindingGroup.addBinding(jListBinding);

		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${clientBean.selectedResponse.selectedBusinessDocument}"),
						xbrlResponseList, BeanProperty
								.create("selectedElement"));
		bindingGroup.addBinding(binding);

		eLProperty = ELProperty
				.create("${clientBean.selectedResponse.selectedBusinessDocument.attachments}");
		jListBinding = SwingBindings.createJListBinding(
				AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty,
				attachmentResponseList);
		jListBinding.setDetailBinding(ELProperty.create("${fileName}"));
		bindingGroup.addBinding(jListBinding);

		binding = Bindings
				.createAutoBinding(
						AutoBinding.UpdateStrategy.READ_WRITE,
						this,
						ELProperty
								.create("${clientBean.selectedResponse.selectedBusinessDocument.selectedAttachment}"),
						attachmentResponseList, BeanProperty
								.create("selectedElement"));
		bindingGroup.addBinding(binding);

		bindingGroup.bind();
	}// </editor-fold>

	/**
	 * Selects an xml keystore file and attempts to load the key store and
	 * list of ABR user credentials from it.
	 */
	@Action
	public void selectKeyStoreFile() {
		JFileChooser chooser = new JFileChooser(".");
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"ABR Keystore Files (keystore.xml)", "xml");
		chooser.addChoosableFileFilter(filter);

		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			this.clientBean.getKeyStore().setKeyStoreFile(
					chooser.getSelectedFile());
		}
	}

	/**
	 * Selects an xml or xbrl file and attempts to add the business document
	 * from it.
	 */
	@Action
	public void addNewBusinessDocument() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"XBRL Instance documents (*.xbrl, *.xml)", "xbrl", "xml");
		chooser.addChoosableFileFilter(filter);

		int returnVal = chooser.showOpenDialog(this.getComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File businessDocument = chooser.getSelectedFile();

			this.clientBean.addBusinessDocument(businessDocument);
		}
	}

	/**
	 * Removes the selected business document from the client bean.
	 */
	@Action
	public void removeSelectedInstanceDocument() {
		
		this.clientBean.getSelectedBusinessDocument().getAttachments()
				.removeAll(this.clientBean.getSelectedBusinessDocument().getAttachments());
		this.clientBean.getBusinessDocuments().remove(
				this.clientBean.getSelectedBusinessDocument());
	}

	/**
	 * Selects an xml or xbrl file and attempts to add the attachment from it.
	 */
	@Action
	public void addNewAttachment() {
		JFileChooser chooser = new JFileChooser();
		
		int returnVal = chooser.showOpenDialog(this.getComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File attachment = chooser.getSelectedFile();

			this.clientBean.getSelectedBusinessDocument().addAttachment(
					attachment);
		}
	}

	/**
	 * Removes the selected attachment from the client bean.
	 */
	@Action
	public void removeSelectedAttachment() {
		this.clientBean.getSelectedBusinessDocument().removeAttachment(
				this.clientBean.getSelectedBusinessDocument().getSelectedAttachment());
	}

	/**
	 * Edits the selected business document from the client bean.
	 */
	@Action
	public void editBusinessDocumentListItem() {
		
	    int width = this.getFrame().getSize().width;
	    int height = this.getFrame().getSize().height;
	    width = width/2;
	    height = height/2;
		
		SBRJavaClientEditBusinessDocumentHeader popupFrame = new SBRJavaClientEditBusinessDocumentHeader(new JFrame(),clientBean);
		popupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		popupFrame.setSize(500, 170);
		popupFrame.setLocation(this.getFrame().getLocation().x + width - (popupFrame.getSize().width/2), this.getFrame().getLocation().y + height - (popupFrame.getSize().height/2));
		popupFrame.setVisible(true);
				
	}

	/**
	 * Attempts to retry sending a request in a queue that previously failed.
	 */
	public void retryItem() 
	{
		ObservableList<ResponseBean> queuedList = this.clientBean.getQueuedResponses();
		if (queuedList == null || queuedList.size() == 0 || !resendOnErrorCheck.isSelected())
			return;

		for (ResponseBean response : queuedList)
		{
			if (response.isResponseOutstanding())
				continue;

			if ((response.getRetryTime().compareTo(new Date()) <= 0) && 
					(response.getStatus() != null && response.getStatus().contains("Queued")))
			{
				this.clientBean.resubmit(response, resendOnErrorCheck.isSelected(), this.getApplication()).execute();

				if (response.getStatus().equals("Completed"))
				{
					this.clientBean.getQueuedResponses().remove(this.clientBean.getSelectedResponse());
					response.setDetails("");
				}
				retryTimer.restart();
			}

    		long diffInSeconds = (response.getRetryTime().getTime() - new Date().getTime()) / 1000;
    		long sec = (diffInSeconds >=60 ? diffInSeconds % 60 : diffInSeconds);
    		long mins = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
    		long hours = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;

			if (hours >= 0 && mins  > 0)
			{
				response.setDetails("Retry at " + 
					new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(response.getRetryTime()) +
					"     " + hours + (hours == 1 ? " hr " : " hrs ") + 
					mins + (mins == 1 ? " min" : " mins"));
			}
			else if (sec > 0)
			{
				response.setDetails("Retry at " + 
						new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(response.getRetryTime()) +
						"     " + sec + (sec == 1 ? " sec " : " secs "));
			}
			else
			{
				response.setDetails("Retry at " + 
						new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(response.getRetryTime()) +
						"     0 secs");
			}
		}
	}

	/**
	 * Attempts to retry sending a request instantly that previously failed.
	 */
	@SuppressWarnings("rawtypes")
	@Action
	public Task retryNow() {
		return this.clientBean.resubmit(this.clientBean.getSelectedResponse(), resendOnErrorCheck.isSelected(), 
				this.getApplication());
	}

	/**
	 * Edits the selected business document from the client bean.
	 */
	@Action
	public void cancelItem() {
		if (this.clientBean.getQueuedResponses().remove(this.clientBean.getSelectedResponse()))
			this.clientBean.getSelectedResponse().setDetails("Cancelled");
	}

	/**
	 * Views the selected business document from the client bean.
	 */
	@Action
	public void editBusinessDocumentAction() {
		BusinessDocumentBean bd = null;
		try {
			bd = this.clientBean.getSelectedResponse()
					.getSelectedBusinessDocument();
			if (bd == null) {
				JOptionPane.showMessageDialog(this.getFrame(),
						"No document selected");
				return;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(),
					"No document selected");
			return;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Sequence No : ");
		sb.append(bd.getBusinessDocument().getBusinessDocumentSequenceNumber());
		sb.append("\n");
		sb.append("Creation Date : ");
		sb.append(bd.getBusinessDocument()
				.getBusinessDocumentCreationDatetime());
		sb.append("\n");
		sb.append("Validation URI : ");
		sb.append(bd.getBusinessDocument()
				.getBusinessDocumentValidationUniformResourceIdentifierText());
		sb.append("\n");
		sb.append("Business Generated Id : ");
		sb.append(bd.getBusinessDocument()
				.getBusinessDocumentBusinessGeneratedIdentifierText());
		sb.append("\n");
		sb.append("Government Generated Id : ");
		sb.append(bd.getBusinessDocument()
				.getBusinessDocumentGovernmentGeneratedIdentifierText());
		sb.append("\n");

		JOptionPane.showMessageDialog(this.getFrame(), sb.toString());
	}

	/**
	 * Views the selected business document from the client bean.
	 */
	@Action
	public void viewBusinessDocumentAction() {
		BusinessDocumentBean bd = null;
		try {
			bd = this.clientBean.getSelectedResponse()
					.getSelectedBusinessDocument();
			if (bd == null) {
				JOptionPane.showMessageDialog(this.getFrame(),
						"No document selected");
				return;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(),
					"No document selected");
			return;
		}

		if (bd.getBusinessDocument() != null) {
			Element el = bd.getInstanceDocument();
			File tempFile = null;
			try {
				tempFile = File.createTempFile("sbrtemp", ".xml");
				tempFile.deleteOnExit();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				createXMLFile(el, tempFile);
				if (Desktop.isDesktopSupported()) 
				{
					Desktop desktop = Desktop.getDesktop();
					if (desktop.isSupported(Desktop.Action.OPEN)) {
						Desktop.getDesktop().open(tempFile);						
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates an xml file using a defined xml element object as input.
	 * 
	 * @param el
	 *            xml element object.
	 * @param tempFile
	 *            temporary file to store the xml.
	 * @throws Exception
	 */
	private void createXMLFile(Element el, File tempFile) throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Node node = doc.importNode(el, true);
		doc.appendChild(node);

		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		StreamResult result = new StreamResult(new FileWriter(tempFile));
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
	}

	/**
	 * Saves the selected business document from the client bean.
	 */
	@Action
	public void saveBusinessDocumentAction() {
		BusinessDocumentBean bd = null;
		try {
			bd = this.clientBean.getSelectedResponse()
					.getSelectedBusinessDocument();
			if (bd == null) {
				JOptionPane.showMessageDialog(this.getFrame(),
						"No document selected");
				return;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(),
					"No document selected");
			return;
		}

		if (bd.getBusinessDocument() != null) {
			Element el = bd.getInstanceDocument();
			JFileChooser chooser = new JFileChooser(new File("."));
			if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this
					.getFrame())) {
				File xmlFile = chooser.getSelectedFile();
				try {
					createXMLFile(el, xmlFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Views the selected attachment from the client bean.
	 */
	@Action
	public void viewAttachmentAction() {
		BusinessDocumentBean bd = null;
		try {
			bd = this.clientBean.getSelectedResponse()
					.getSelectedBusinessDocument();
			if (bd == null) {
				JOptionPane.showMessageDialog(this.getFrame(),
						"No attachment selected");
				return;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(),
					"No attachment selected");
			return;
		}

		if (bd.getSelectedAttachment() != null) {
			MessageAttachmentInstanceBinaryObjectType el = bd
					.getSelectedAttachment().getAttachmentInstance()
					.getMessageAttachmentInstanceBinaryObject();
			String type = el.getContentType();
			byte[] val = el.getValue();
			String ext = "." + type.substring(type.lastIndexOf("/") + 1);
			File tempFile = null;
			try {
				tempFile = File.createTempFile("sbrtemp", ext);
				tempFile.deleteOnExit();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				createBinaryFile(val, tempFile);
				if (Desktop.isDesktopSupported()) 
				{
					Desktop desktop = Desktop.getDesktop();
					if (desktop.isSupported(Desktop.Action.OPEN)) {
						Desktop.getDesktop().open(tempFile);						
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates a binary file using bytes of input.
	 * 
	 * @param val
	 *            bytes of input data.
	 * @param tempFile
	 *            temporary file to store the bytes.
	 */
	private void createBinaryFile(byte[] val, File tempFile) {
		try {
			FileOutputStream out = new FileOutputStream(tempFile);
			out.write(val, 0, val.length);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saves the selected attachment from the client bean.
	 */
	@Action
	public void saveAttachmentAction() {
		BusinessDocumentBean bd = null;
		try {
			bd = this.clientBean.getSelectedResponse()
					.getSelectedBusinessDocument();
			if (bd == null) {
				JOptionPane.showMessageDialog(this.getFrame(),
						"No attachment selected");
				return;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(),
					"No attachment selected");
			return;
		}

		if (bd.getSelectedAttachment() != null) {
			MessageAttachmentInstanceBinaryObjectType el = bd
					.getSelectedAttachment().getAttachmentInstance()
					.getMessageAttachmentInstanceBinaryObject();
			final String type = el.getContentType();
			byte[] val = el.getValue();
			final String ext = "." + type.substring(type.lastIndexOf("/") + 1);
			File binFile = null;
			JFileChooser chooser = new JFileChooser(new File("."));
			chooser.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File f) {
					if (f.getName().toLowerCase().endsWith(ext)) {
						return true;
					}
					return false;
				}

				@Override
				public String getDescription() {
					return (type.substring(type.lastIndexOf("/") +1)).toUpperCase() + " Files (*"+ext.toLowerCase()+")";
				}
			});
			
			chooser.setSelectedFile(new File("attachment"+ext));
			
			if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this
					.getFrame())) {
				binFile = chooser.getSelectedFile();
				try {
					createBinaryFile(val, binFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Given that the prerequisite information has been entered into the
	 * application, submits the data for processing.
	 * 
	 * A form must be selected, and if the option to securely sign the message
	 * request is set, than a password must also be entered in the application.
	 */
	@SuppressWarnings("rawtypes")
	@Action
	public Task sendRequest() {
		if (this.clientBean.getSelectedForm() == null) {
			JOptionPane.showMessageDialog(this.getFrame(), "No Form selected");
			return null;
		}
		if (this.clientBean.isSecurityRequest()) {
			if (this.clientBean.getKeyStore().getAlias() == null) {
				JOptionPane.showMessageDialog(this.getFrame(), "No Credential selected");
				return null;
			}
			if ((this.clientBean.getKeyStore().getPassword() == null)
					|| (this.clientBean.getKeyStore().getPassword().trim()
							.length() == 0)) {
				JOptionPane.showMessageDialog(this.getFrame(),
						"No Password Set");
				return null;
			}
		}

		try {
			return this.clientBean.getSendRequest(resendOnErrorCheck.isSelected(), this.getApplication());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.getFrame(),
					"Error: See logs for details");
			return null;
		}
	}

	/**
	 * Determines and sets the radio button that is to be, by default, selected
	 * based on the visibility of the other radio buttons and what was
	 * previously the selected service.
	 */
	private void setSelectedService() {
		// Initialiser: all radio buttons are not visible, list will be default
		if (!listRadio.isVisible() && !lodgeRadio.isVisible()
				&& !prefillRadio.isVisible() && !prelodgeRadio.isVisible()
				&& !altListRadio.isVisible() && !altLodgeRadio.isVisible()) {
			return;
		}

		if (!listRadio.isVisible() && this.listRadio.isSelected()) {
			this.prefillRadio.setSelected(true);
			setSelectedService();
			return;
		}

		if (!prefillRadio.isVisible() && this.prefillRadio.isSelected()) {
			this.prelodgeRadio.setSelected(true);
			setSelectedService();
			return;
		}

		if (!prelodgeRadio.isVisible() && this.prelodgeRadio.isSelected()) {
			this.lodgeRadio.setSelected(true);
			setSelectedService();
			return;
		}

		if (!lodgeRadio.isVisible() && this.lodgeRadio.isSelected()) {
			this.altListRadio.setSelected(true);
			setSelectedService();
			return;
		}

		if (!altListRadio.isVisible() && this.altListRadio.isSelected()) {
			this.altLodgeRadio.setSelected(true);
			setSelectedService();
			return;
		}

		if (!altLodgeRadio.isVisible() && this.altLodgeRadio.isSelected()) {
			this.listRadio.setSelected(true);
			setSelectedService();
			return;
		}
		return;
	}

	/**
	 * Sets the selected service of the request message based on the radio
	 * button selected.
	 */
	private void setServiceToButton() {
		if (listRadio.isSelected())
			this.clientBean.setSelectedService(JRCServiceType.LIST);
		if (prefillRadio.isSelected())
			this.clientBean.setSelectedService(JRCServiceType.PREFILL);
		if (prelodgeRadio.isSelected())
			this.clientBean.setSelectedService(JRCServiceType.PRELODGE);
		if (lodgeRadio.isSelected())
			this.clientBean.setSelectedService(JRCServiceType.LODGE);
		if (altListRadio.isSelected())
			this.clientBean.setSelectedService(JRCServiceType.ALTLIST);
		if (altLodgeRadio.isSelected())
			this.clientBean.setSelectedService(JRCServiceType.ALTLODGE);
	}

	/**
	 * Shows all active radio buttons and their corresponding text labels and
	 * hides all the ones that are not.
	 */
	private void setActiveServices() {
		this.listRadio.setVisible(this.clientBean.isListRadioEnable());
		if (listRadio.isVisible())
			listRadio.setText(this.clientBean.getListMessageText());

		this.prefillRadio.setVisible(this.clientBean.isPrefillRadioEnable());
		if (prefillRadio.isVisible())
			prefillRadio.setText(this.clientBean.getPrefillMessageText());

		this.prelodgeRadio.setVisible(this.clientBean.isPrelodgeRadioEnable());
		if (prelodgeRadio.isVisible())
			prelodgeRadio.setText(this.clientBean.getPrelodgeMessageText());

		this.lodgeRadio.setVisible(this.clientBean.isLodgeRadioEnable());
		if (lodgeRadio.isVisible())
			lodgeRadio.setText(this.clientBean.getLodgeMessageText());

		this.altListRadio.setVisible(this.clientBean.isAltListRadioEnable());
		if (altListRadio.isVisible())
			altListRadio.setText(this.clientBean.getAltListMessageText());

		this.altLodgeRadio.setVisible(this.clientBean.isAltLodgeRadioEnable());
		if (altLodgeRadio.isVisible())
			altLodgeRadio.setText(this.clientBean.getAltLodgeMessageText());

		setSelectedService();
		setServiceToButton();
	}

	/**
	 * Returns the action map used by this application. Actions defined using
	 * the Action annotation are returned by this method.
	 */
	private ApplicationActionMap getAppActionMap() {
		return Application.getInstance().getContext().getActionMap(this);
	}

	private void displayPopupActions(MouseEvent m) 
	{
		if (m.isPopupTrigger()) 
		{
			if (this.clientBean.getQueuedResponses() != null &&
					this.clientBean.getQueuedResponses().contains(this.clientBean.getSelectedResponse()))
			{
				JPopupMenu menu = new JPopupMenu();
				JMenuItem retryMenuItem = new JMenuItem();
				retryMenuItem.setAction(getAppActionMap()
						.get("retryNow")); // NOI18N
				retryMenuItem.setText("Retry now");
				menu.add(retryMenuItem);
	
	
				JMenuItem cancelMenuItem = new JMenuItem();
				cancelMenuItem.setAction(getAppActionMap()
						.get("cancelItem")); // NOI18N
				cancelMenuItem.setText("Cancel");
				menu.add(cancelMenuItem);
				
				// Get the position of the click
				final int x = m.getX();
				final int y = m.getY();
	
				menu.show(m.getComponent(), x, y);
			}
		}
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton addAttachmentButton;
	private javax.swing.JButton addBusinessDocumentButton;
	private javax.swing.JRadioButton altListRadio;
	private javax.swing.JRadioButton altLodgeRadio;
	private javax.swing.JList attachmentList;
	private javax.swing.JList businessDocumentList;
	private javax.swing.JButton selectKeystoreButton;
	private javax.swing.JList formList;
	private javax.swing.JCheckBox securityCheck;
	private javax.swing.JLabel shortDescriptionLabel;
	private javax.swing.JLabel detailedDescriptionLabel;
	private javax.swing.JLabel keystoreFileLabel;
	private javax.swing.JLabel keystoreAliasLabel;
	private javax.swing.JLabel keystoreAliasPasswordLabel;
	private javax.swing.JLabel coreServicesUrlLabel;
	private javax.swing.JLabel stsUrlLabel;
	private javax.swing.JLabel messageTypeLabel;
	private javax.swing.JLabel senderLabel;
	private javax.swing.JLabel lodgementReceiptLabel;
	private javax.swing.JLabel maximumSeverityLabel;
	private javax.swing.JLabel severityLabel;
	private javax.swing.JPanel submissionPanel;
	private javax.swing.JPanel networkPanel;
	private javax.swing.JPanel messageEventsPanel;
	private javax.swing.JPanel messageEventPanel;
	private javax.swing.JPanel credentialPanel;
	private javax.swing.JPanel servicePanel;
	private javax.swing.JPanel documentPanel;
	private javax.swing.JPanel attachmentPanel;
	private javax.swing.JPanel optionsPanel;
	private javax.swing.JPanel formPanel;
	private javax.swing.JPanel processingPanel;
	private javax.swing.JPanel requestsPanel;
	private javax.swing.JPanel responsePanel;
	private javax.swing.JScrollPane formScrollPane;
	private javax.swing.JScrollPane documentScrollPane;
	private javax.swing.JScrollPane attachmentScrollPane;
	private javax.swing.JScrollPane requestsScrollPane;
	private javax.swing.JTextField coreServicesUrlText;
	private javax.swing.JTextField stsUrlText;
	private javax.swing.JComboBox keystoreAliasCombo;
	private javax.swing.JPasswordField keystoreAliasPasswordText;
	private javax.swing.JTextField keystoreFileText;
	private javax.swing.JRadioButton lodgeRadio;
	private javax.swing.JPanel mainPanel;
	private javax.swing.JCheckBox mtomCheck;
	private javax.swing.JCheckBox loggerCheck;
	private javax.swing.JRadioButton listRadio;
	private javax.swing.JRadioButton prefillRadio;
	private JTable requestTable;
	private JScrollPane locationsScrollPane;
	private JTable locationsTable;
	private JTextArea responseDetailedDescriptionTextArea;
	private JLabel responseShortDescriptionLabel;
	private JLabel responseSeverityLabel;
	private JPanel locationPanel;
	private JButton saveAttachmentButton;
	private JButton viewAttachmentButton;
	private JScrollPane attachmentResponseScrollPane;
	private JList attachmentResponseList;
	private JButton saveXbrlButton;
	private JButton viewXbrlButton;
	private JScrollPane xbrlResponseScrollPane;
	private JButton viewDocumentDetailsButton;
	private JList xbrlResponseList;
	private javax.swing.JRadioButton prelodgeRadio;
	private javax.swing.JProgressBar progressBar;
	private javax.swing.JButton removeAttachmentButton;
	private javax.swing.JButton removeBusinessDocumentButton;
	private javax.swing.JLabel responseLodgementReceiptLabel;
	private javax.swing.JLabel responseMaximumSeverityLabel;
	private javax.swing.JComboBox responseMessageEventsCombo;
	private javax.swing.JLabel responseMessageTypeLabel;
	private javax.swing.JLabel responseSenderLabel;
	private javax.swing.JCheckBox schemaCheck;
	private javax.swing.JCheckBox schematronCheck;
	private JPanel responseAttachmentsPanel;
	private JPanel responseBusinessDocumentsPanel;
	private JScrollPane timestampsScrollPane;
	private JTable timestampsTable;
	private JPanel timestampsPanel;
	private JPanel businessDocumentsPanel;
	private JPanel messagePanel;
	private JTabbedPane responseTabbedPane;
	private JLabel sbrIconLabel;
	private JLabel sbrLogoLabel;
	private JPanel headerPanel;
	private JTabbedPane topTabbedPane;
	private javax.swing.JButton sendButton;
	private javax.swing.ButtonGroup serviceButtonGroup;
	private javax.swing.JLabel statusAnimationLabel;
	private javax.swing.JLabel statusMessageLabel;
	private javax.swing.JPanel statusPanel;
	private javax.swing.JCheckBox validateMessageTypeCheck;
	private javax.swing.JCheckBox xbrlCheck;
	private javax.swing.JLabel codeLabel;
    private javax.swing.JLabel detailLabel;
    private javax.swing.JTextArea detailTextArea;
    private javax.swing.JTextField faultCodeTextField;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel messageEventItemPanel;
    private javax.swing.JLabel nodeLabel;
    private javax.swing.JTextField nodeTextField;
    private javax.swing.JTextField notifyUserTextField;
    private javax.swing.JLabel reasonLabel;
    private javax.swing.JTextField reasonTextField;
    private javax.swing.JComboBox subcodeCombo;
    private javax.swing.JLabel subcodeLabel;
    private javax.swing.JTextField subcodeTextField;
    private JPanel exceptionDetailsPanel;
    private javax.swing.JCheckBox resendOnErrorCheck;
	private org.jdesktop.beansbinding.BindingGroup bindingGroup;
	// End of variables declaration//GEN-END:variables
	private final Timer messageTimer;
	private final Timer busyIconTimer;
	private final Timer retryTimer;
	private final Icon idleIcon;
	private final Icon[] busyIcons = new Icon[15];
	private int busyIconIndex = 0;
	private JDialog aboutBox;
}
