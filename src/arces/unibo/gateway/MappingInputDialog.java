package arces.unibo.gateway;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import arces.unibo.gateway.mapping.MNMapping;
import arces.unibo.gateway.mapping.MPMapping;
import arces.unibo.gateway.mapping.ResourceAction;

import javax.swing.JRadioButton;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MappingInputDialog extends JDialog {

	public interface MappingInputDialogListener {
		public void newMNMapping(MNMapping mapping);
		public void newMPMapping(MPMapping mapping);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -4050416195581364898L;
	private final JPanel contentPanel = new JPanel();
	private JTextField URI;
	private JTextField requestPattern;
	private JTextField responsePattern;
	private JTextField actionURI;
	private JTextField actionValue;

	private JRadioButton rdbtnProtocol;
	private JRadioButton rdbtnNetwork;
	private JTextField resourceURI;
	
	private MappingInputDialogListener listener;
	
	public void setListener(MappingInputDialogListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MappingInputDialog dialog = new MappingInputDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public MappingInputDialog() {
		setResizable(false);
		setTitle("Create new mapping");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{105, 128, 85, 0, 0};
		gbl_contentPanel.rowHeights = new int[]{23, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		
		JLabel lblNewLabel = new JLabel("URI");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		contentPanel.add(lblNewLabel, gbc_lblNewLabel);
			
		//Group the radio buttons.
	    ButtonGroup group = new ButtonGroup();
					
		URI = new JTextField();
		GridBagConstraints gbc_URI = new GridBagConstraints();
		gbc_URI.insets = new Insets(0, 0, 5, 5);
		gbc_URI.fill = GridBagConstraints.HORIZONTAL;
		gbc_URI.gridx = 1;
		gbc_URI.gridy = 0;
		contentPanel.add(URI, gbc_URI);
		URI.setColumns(10);
			
		rdbtnProtocol = new JRadioButton("Protocol");
		rdbtnProtocol.setSelected(true);
		GridBagConstraints gbc_rdbtnProtocol = new GridBagConstraints();
		gbc_rdbtnProtocol.anchor = GridBagConstraints.NORTHEAST;
		gbc_rdbtnProtocol.insets = new Insets(0, 0, 5, 5);
		gbc_rdbtnProtocol.gridx = 2;
		gbc_rdbtnProtocol.gridy = 0;
		contentPanel.add(rdbtnProtocol, gbc_rdbtnProtocol);
		group.add(rdbtnProtocol);
		
		rdbtnNetwork = new JRadioButton("Network");
		GridBagConstraints gbc_rdbtnNetwork = new GridBagConstraints();
		gbc_rdbtnNetwork.anchor = GridBagConstraints.NORTHWEST;
		gbc_rdbtnNetwork.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnNetwork.gridx = 3;
		gbc_rdbtnNetwork.gridy = 0;
		contentPanel.add(rdbtnNetwork, gbc_rdbtnNetwork);
		group.add(rdbtnNetwork);
	
		JLabel lblRequestPattern = new JLabel("Request pattern");
		GridBagConstraints gbc_lblRequestPattern = new GridBagConstraints();
		gbc_lblRequestPattern.anchor = GridBagConstraints.EAST;
		gbc_lblRequestPattern.insets = new Insets(0, 0, 5, 5);
		gbc_lblRequestPattern.gridx = 0;
		gbc_lblRequestPattern.gridy = 2;
		contentPanel.add(lblRequestPattern, gbc_lblRequestPattern);
	
		requestPattern = new JTextField();
		GridBagConstraints gbc_requestPattern = new GridBagConstraints();
		gbc_requestPattern.gridwidth = 3;
		gbc_requestPattern.insets = new Insets(0, 0, 5, 0);
		gbc_requestPattern.fill = GridBagConstraints.HORIZONTAL;
		gbc_requestPattern.gridx = 1;
		gbc_requestPattern.gridy = 2;
		contentPanel.add(requestPattern, gbc_requestPattern);
		requestPattern.setColumns(10);
	
		JLabel lblResponsePattern = new JLabel("Response pattern");
		GridBagConstraints gbc_lblResponsePattern = new GridBagConstraints();
		gbc_lblResponsePattern.anchor = GridBagConstraints.EAST;
		gbc_lblResponsePattern.insets = new Insets(0, 0, 5, 5);
		gbc_lblResponsePattern.gridx = 0;
		gbc_lblResponsePattern.gridy = 3;
		contentPanel.add(lblResponsePattern, gbc_lblResponsePattern);
	
		responsePattern = new JTextField();
		GridBagConstraints gbc_responsePattern = new GridBagConstraints();
		gbc_responsePattern.gridwidth = 3;
		gbc_responsePattern.insets = new Insets(0, 0, 5, 0);
		gbc_responsePattern.fill = GridBagConstraints.HORIZONTAL;
		gbc_responsePattern.gridx = 1;
		gbc_responsePattern.gridy = 3;
		contentPanel.add(responsePattern, gbc_responsePattern);
		responsePattern.setColumns(10);
		
		JLabel lblResourceUri = new JLabel("Resource URI");
		GridBagConstraints gbc_lblResourceUri = new GridBagConstraints();
		gbc_lblResourceUri.anchor = GridBagConstraints.EAST;
		gbc_lblResourceUri.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourceUri.gridx = 0;
		gbc_lblResourceUri.gridy = 4;
		contentPanel.add(lblResourceUri, gbc_lblResourceUri);
		
		resourceURI = new JTextField();
		GridBagConstraints gbc_resourceURI = new GridBagConstraints();
		gbc_resourceURI.gridwidth = 3;
		gbc_resourceURI.insets = new Insets(0, 0, 5, 5);
		gbc_resourceURI.fill = GridBagConstraints.HORIZONTAL;
		gbc_resourceURI.gridx = 1;
		gbc_resourceURI.gridy = 4;
		contentPanel.add(resourceURI, gbc_resourceURI);
		resourceURI.setColumns(10);
	
		JLabel lblActionUri = new JLabel("Action URI");
		GridBagConstraints gbc_lblActionUri = new GridBagConstraints();
		gbc_lblActionUri.anchor = GridBagConstraints.EAST;
		gbc_lblActionUri.insets = new Insets(0, 0, 5, 5);
		gbc_lblActionUri.gridx = 0;
		gbc_lblActionUri.gridy = 5;
		contentPanel.add(lblActionUri, gbc_lblActionUri);
	
		actionURI = new JTextField();
		GridBagConstraints gbc_actionURI = new GridBagConstraints();
		gbc_actionURI.gridwidth = 3;
		gbc_actionURI.insets = new Insets(0, 0, 5, 0);
		gbc_actionURI.fill = GridBagConstraints.HORIZONTAL;
		gbc_actionURI.gridx = 1;
		gbc_actionURI.gridy = 5;
		contentPanel.add(actionURI, gbc_actionURI);
		actionURI.setColumns(10);
	
		JLabel lblActionValue = new JLabel("Action value");
		GridBagConstraints gbc_lblActionValue = new GridBagConstraints();
		gbc_lblActionValue.anchor = GridBagConstraints.EAST;
		gbc_lblActionValue.insets = new Insets(0, 0, 0, 5);
		gbc_lblActionValue.gridx = 0;
		gbc_lblActionValue.gridy = 6;
		contentPanel.add(lblActionValue, gbc_lblActionValue);
	
		actionValue = new JTextField();
		GridBagConstraints gbc_actionValue = new GridBagConstraints();
		gbc_actionValue.gridwidth = 3;
		gbc_actionValue.fill = GridBagConstraints.HORIZONTAL;
		gbc_actionValue.gridx = 1;
		gbc_actionValue.gridy = 6;
		contentPanel.add(actionValue, gbc_actionValue);
		actionValue.setColumns(10);
	
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("OK");
		okButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(rdbtnProtocol.isSelected()){
					MPMapping mapping = new MPMapping(URI.getText(),requestPattern.getText(),responsePattern.getText(),
							new ResourceAction(resourceURI.getText(),actionURI.getText(),actionValue.getText()));
					if (listener != null) listener.newMPMapping(mapping);
				}
				else {
					MNMapping mapping = new MNMapping(URI.getText(),requestPattern.getText(),responsePattern.getText(),
							new ResourceAction(resourceURI.getText(),actionURI.getText(),actionValue.getText()));
					if (listener != null) listener.newMNMapping(mapping);	
				}
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
	
	
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
	}
}
