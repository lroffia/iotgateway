package arces.unibo.gateway;

import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import javax.swing.JScrollPane;
import javax.swing.JButton;

import arces.unibo.SEPA.application.SEPALogger;
import arces.unibo.SEPA.application.ApplicationProfile;
import arces.unibo.SEPA.application.SEPALogger.VERBOSITY;
import arces.unibo.gateway.MappingInputDialog.MappingInputDialogListener;
import arces.unibo.gateway.garbagecollector.GarbageCollector;
import arces.unibo.gateway.garbagecollector.GarbageCollectorListener;
import arces.unibo.gateway.mapping.MNMapping;
import arces.unibo.gateway.mapping.MPMapping;
import arces.unibo.gateway.mapping.manager.MappingEventListener;
import arces.unibo.gateway.mapping.manager.MappingManager;

import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;

public class GatewayManager implements MappingEventListener, MappingInputDialogListener, GarbageCollectorListener {	
	//Tables models
	private DefaultTableModel protocolMappingDataModel;
	private DefaultTableModel networkMappingDataModel;
	private DefaultTableModel pendingResourceRequestsDM;
	private DefaultTableModel resourceRequestsDM;
	private DefaultTableModel resourceResponsesDM;
	private DefaultTableModel MPRequestsDM;
	private DefaultTableModel MPResponsesDM;
	private DefaultTableModel MNRequestsDM;
	private DefaultTableModel MNResponsesDM;
	private DefaultTableModel MPRequestsDeletedDM;
	
	private static String APP_PROFILE = "GatewayProfile.sap";
	//Tables headers
	private String protocolMappingHeader[] = new String[] 
			{"Protocol URI", "Request string pattern", "Response string pattern", "Resource URI", "Action URI", "Action value","Mapping URI"};
	private String networkMappingHeader[] = new String[] 
			{"Network URI", "Request string pattern", "Response string pattern", "Resource URI", "Action URI", "Action value","Mapping URI"};	
	private String resourceHeader[] = new String[] 
			{"Timestamp","Resource URI", "Action URI", "Value"};
	private String MPHeader[] = new String[] 
			{"Timestamp","Protocol URI", "Value"};
	private String MNHeader[] = new String[] 
			{"Timestamp","Network URI", "Value"};
	
	private MappingManager mappingManager;
	private GarbageCollector garbageCollector;
	
	private long MPRequestsN = 0;
	private long MPResponsesN = 0;
	private long pendingResourceRequestsN = 0;
	private long resourceRequestsN = 0;
	private long resourceResponsesN = 0;
	private long MNRequestsN = 0;
	private long MNResponsesN = 0;
	private long MPRequestsDeletedN = 0;
	
	ApplicationProfile appProfile = new ApplicationProfile();
	
	private JFrame frmSemanticGatewayManager;
	private JTable table;
	private JScrollPane scrollPane;
	private JLabel lblNetworkMappings;
	private JTable table_1;
	private JScrollPane scrollPane_1;
	private JButton btnAdd;
	private JButton btnRemove;
	private JTabbedPane tabbedPane;
	private JPanel panel;
	private JPanel panel_1;
	private JLabel lblResourcePendingRequests;
	private JTable table_resourcePendingRequests;
	private JScrollPane scrollPane_2;
	private JLabel lblNewLabel;
	private JTable table_MPResponses;
	private JScrollPane scrollPane_3;
	private JLabel lblMpRequests;
	private JTable table_MPRequests;
	private JLabel lblResourceRequests;
	private JTable table_resourceRequests;
	private JLabel lblResourceResponses;
	private JTable table_resourceResponses;
	private JLabel lblNewLabel_1;
	private JLabel lblTotalTriples;
	private JLabel lblNewLabel_2;
	private JTable table_MNRequests;
	private JLabel lblMnResponses;
	private JTable table_MNResponses;
	private JScrollPane scrollPane_4;
	private JScrollPane scrollPane_5;
	private JScrollPane scrollPane_6;
	private JScrollPane scrollPane_7;
	private JScrollPane scrollPane_8;
	private JButton btnClearMPRequests;
	private JButton btnPendingResourceRequests;
	private JButton btnClearMPResponses;
	private JButton btnClearResourceRequests;
	private JButton btnClearResourceResponses;
	private JButton btnClearMNRequests;
	private JButton btnClearMNResponses;
	private JLabel labelMPRequestsN;
	private JLabel lblPendingResourceRequestsN;
	private JLabel labelResourceRequests;
	private JLabel labelMNRequestsN;
	private JLabel labelMNResponsesN;
	private JLabel labelResourceResponsesN;
	private JLabel labelMPResponsesN;
	private JLabel lblMpRequests_1;
	private JLabel lblMpRequestsDeleted;
	private JTable table_MpRequestDeleted;
	private JButton btnClearMpRequestDeleted;
	private JScrollPane scrollPane_9;
	private JLabel messageBox;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GatewayManager window = new GatewayManager();
					window.frmSemanticGatewayManager.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GatewayManager() {
		initialize();
		
		if(!appProfile.load(APP_PROFILE)) {
			SEPALogger.log(VERBOSITY.FATAL, "GW MANAGER", "Failed to load: "+ APP_PROFILE);
			return;
		}
		
		frmSemanticGatewayManager.setTitle("WoT-Gateway Manager @ "+
				appProfile.getParameters().getUrl()+
				appProfile.getParameters().getPath()+
				" UPort:"+appProfile.getParameters().getUpdatePort()+
				" SPort:"+appProfile.getParameters().getSubscribePort());
		
		messageBox.setText("Starting mapping manager...");
		mappingManager = new MappingManager(appProfile,this);
		mappingManager.start();
		
		messageBox.setText("Starting garbage collector...");
		garbageCollector = new GarbageCollector(appProfile,this);
		garbageCollector.start(false, true); 
		
		messageBox.setText("Gateway manager started");
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		final MappingInputDialog dlg = new MappingInputDialog();
		dlg.setListener(this);

		protocolMappingDataModel = new DefaultTableModel(0, 0){
		    /**
			 * 
			 */
			private static final long serialVersionUID = -752721579700272933L;

			@Override
			public boolean isCellEditable(int row, int column)
		    {
		        if (column == 6) return false;
		        
		        return true;
		    }
			
			@Override
			public void setValueAt(Object aValue, int row, int column){
				super.setValueAt(aValue, row, column);
				mappingManager.updateProtocolMapping(getValueAt(row, 6).toString(),
						getValueAt(row, 0).toString(),
						getValueAt(row, 1).toString(),
						getValueAt(row, 2).toString(),
						getValueAt(row, 3).toString(),
						getValueAt(row, 4).toString(),
						getValueAt(row, 5).toString());
			}
		};
		
		protocolMappingDataModel.setColumnIdentifiers(protocolMappingHeader);
		
		networkMappingDataModel = new DefaultTableModel(0, 0){
			/**
			 * 
			 */
			private static final long serialVersionUID = -6795458612569289029L;

			@Override
			public boolean isCellEditable(int row, int column)
		    {
		        if (column == 6) return false;
		        
		        return true;
		    }
			
			@Override
			public void setValueAt(Object aValue, int row, int column){
				super.setValueAt(aValue, row, column);
				mappingManager.updateNetworkMapping(getValueAt(row, 6).toString(),
						getValueAt(row, 0).toString(),
						getValueAt(row, 1).toString(),
						getValueAt(row, 2).toString(),
						getValueAt(row, 3).toString(),
						getValueAt(row, 4).toString(),
						getValueAt(row, 5).toString());
			}
		};
		
		networkMappingDataModel.setColumnIdentifiers(networkMappingHeader);
		
		pendingResourceRequestsDM = new DefaultTableModel(0, 0){
			/**
			 * 
			 */
			private static final long serialVersionUID = -6795458612569289029L;

			@Override
			public boolean isCellEditable(int row, int column)
		    {
		        return false;
		    }
		};
		pendingResourceRequestsDM.setColumnIdentifiers(resourceHeader);

		resourceRequestsDM = new DefaultTableModel(0, 0){
			/**
			 * 
			 */
			private static final long serialVersionUID = 2403052528444271791L;

			@Override
			public boolean isCellEditable(int row, int column)
		    {
		        return false;
		    }
		};
		resourceRequestsDM.setColumnIdentifiers(resourceHeader);
		
		resourceResponsesDM = new DefaultTableModel(0, 0){
			/**
			 * 
			 */
			private static final long serialVersionUID = 5784973092085444932L;

			@Override
			public boolean isCellEditable(int row, int column)
		    {
		        return false;
		    }
		};
		resourceResponsesDM.setColumnIdentifiers(resourceHeader);
		
		MPRequestsDM = new DefaultTableModel(0, 0){
			/**
			 * 
			 */
			private static final long serialVersionUID = 6231730058550334611L;

			@Override
			public boolean isCellEditable(int row, int column)
		    {
		        return false;
		    }
		};
		MPRequestsDM.setColumnIdentifiers(MPHeader);
		
		MPResponsesDM = new DefaultTableModel(0, 0){
			/**
			 * 
			 */
			private static final long serialVersionUID = -4255983650517939210L;

			@Override
			public boolean isCellEditable(int row, int column)
		    {
		        return false;
		    }
		};
		MPResponsesDM.setColumnIdentifiers(MPHeader);
		
		MPRequestsDeletedDM = new DefaultTableModel(0, 0){

			/**
			 * 
			 */
			private static final long serialVersionUID = 2300240321642218453L;

			@Override
			public boolean isCellEditable(int row, int column)
		    {
		        return false;
		    }
		};
		MPRequestsDeletedDM.setColumnIdentifiers(MPHeader);
		
		MNRequestsDM = new DefaultTableModel(0, 0){
			/**
			 * 
			 */
			private static final long serialVersionUID = 2300240321642218453L;

			@Override
			public boolean isCellEditable(int row, int column)
		    {
		        return false;
		    }
		};
		MNRequestsDM.setColumnIdentifiers(MNHeader);
		
		MNResponsesDM = new DefaultTableModel(0, 0){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1850732027696720168L;

			@Override
			public boolean isCellEditable(int row, int column)
		    {
		        return false;
		    }
		};
		MNResponsesDM.setColumnIdentifiers(MNHeader);
		
		frmSemanticGatewayManager = new JFrame();
		frmSemanticGatewayManager.setTitle("WoT Gateway Manager");
		frmSemanticGatewayManager.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mappingManager.stop();
				garbageCollector.stop();
			}
		});
		frmSemanticGatewayManager.setBounds(100, 100, 521, 578);
		frmSemanticGatewayManager.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{521, 0};
		gridBagLayout.rowHeights = new int[]{556, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		frmSemanticGatewayManager.getContentPane().setLayout(gridBagLayout);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		frmSemanticGatewayManager.getContentPane().add(tabbedPane, gbc_tabbedPane);
		
		panel = new JPanel();
		tabbedPane.addTab("Mappings", null, panel, null);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 117, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblProtocolMappings = new JLabel("Protocol mappings");
		GridBagConstraints gbc_lblProtocolMappings = new GridBagConstraints();
		gbc_lblProtocolMappings.anchor = GridBagConstraints.NORTH;
		gbc_lblProtocolMappings.gridwidth = 3;
		gbc_lblProtocolMappings.insets = new Insets(0, 0, 5, 0);
		gbc_lblProtocolMappings.gridx = 0;
		gbc_lblProtocolMappings.gridy = 0;
		panel.add(lblProtocolMappings, gbc_lblProtocolMappings);
		lblProtocolMappings.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		
		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panel.add(scrollPane, gbc_scrollPane);
		table = new JTable(protocolMappingDataModel) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8035546607083119932L;

			@Override
		    public void changeSelection(int rowIndex, int columnIndex,
		            boolean toggle, boolean extend) {
				if(isRowSelected(rowIndex)) {
					removeRowSelectionInterval(rowIndex,rowIndex);
					removeColumnSelectionInterval(columnIndex, columnIndex);
				}
				else super.changeSelection(rowIndex, columnIndex, toggle, extend);
		        
		    }
		};
		scrollPane.setViewportView(table);
		table.setAutoCreateRowSorter(true);
		
		lblNetworkMappings = new JLabel("Network mappings");
		GridBagConstraints gbc_lblNetworkMappings = new GridBagConstraints();
		gbc_lblNetworkMappings.gridwidth = 3;
		gbc_lblNetworkMappings.anchor = GridBagConstraints.NORTH;
		gbc_lblNetworkMappings.insets = new Insets(0, 0, 5, 0);
		gbc_lblNetworkMappings.gridx = 0;
		gbc_lblNetworkMappings.gridy = 2;
		panel.add(lblNetworkMappings, gbc_lblNetworkMappings);
		lblNetworkMappings.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		
		scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridwidth = 3;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 3;
		panel.add(scrollPane_1, gbc_scrollPane_1);
		table_1 = new JTable(networkMappingDataModel) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -1479378785764639121L;

			@Override
		    public void changeSelection(int rowIndex, int columnIndex,
		            boolean toggle, boolean extend) {
				if(isRowSelected(rowIndex)) {
					removeRowSelectionInterval(rowIndex,rowIndex);
				}
				else super.changeSelection(rowIndex, columnIndex, toggle, extend);
		        
		    }
		};
		scrollPane_1.setViewportView(table_1);
		table_1.setAutoCreateRowSorter(true);
		
		btnAdd = new JButton("Add");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.anchor = GridBagConstraints.EAST;
		gbc_btnAdd.insets = new Insets(0, 0, 0, 5);
		gbc_btnAdd.gridx = 1;
		gbc_btnAdd.gridy = 4;
		panel.add(btnAdd, gbc_btnAdd);
		btnAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				dlg.setVisible(true);
			}
		});
		
		btnRemove = new JButton("Remove");
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.gridx = 2;
		gbc_btnRemove.gridy = 4;
		panel.add(btnRemove, gbc_btnRemove);
		btnRemove.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int[] rows = table.getSelectedRows();
				for (int i = 0; i < rows.length; i++) rows[i] = table.convertRowIndexToModel(rows[i]);
				for(int index: rows) mappingManager.removeProtocolMapping(protocolMappingDataModel.getValueAt(index, 6).toString());
				
				rows = table_1.getSelectedRows();
				for (int i = 0; i < rows.length; i++) rows[i] = table_1.convertRowIndexToModel(rows[i]);
				for(int index: rows) mappingManager.removeNetworkMapping(networkMappingDataModel.getValueAt(index, 6).toString());
			}
		});
		
		panel_1 = new JPanel();
		tabbedPane.addTab("Garbage collector", null, panel_1, null);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		lblMpRequests_1 = new JLabel("MP Requests");
		GridBagConstraints gbc_lblMpRequests_1 = new GridBagConstraints();
		gbc_lblMpRequests_1.gridwidth = 6;
		gbc_lblMpRequests_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblMpRequests_1.gridx = 0;
		gbc_lblMpRequests_1.gridy = 0;
		panel_1.add(lblMpRequests_1, gbc_lblMpRequests_1);
		
		scrollPane_4 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_4 = new GridBagConstraints();
		gbc_scrollPane_4.gridwidth = 5;
		gbc_scrollPane_4.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_4.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_4.gridx = 0;
		gbc_scrollPane_4.gridy = 1;
		panel_1.add(scrollPane_4, gbc_scrollPane_4);
		
		table_MPRequests = new JTable(MPRequestsDM);
		scrollPane_4.setViewportView(table_MPRequests);
		
		labelMPRequestsN = new JLabel("--");
		GridBagConstraints gbc_labelMPRequestsN = new GridBagConstraints();
		gbc_labelMPRequestsN.insets = new Insets(0, 0, 5, 0);
		gbc_labelMPRequestsN.gridx = 5;
		gbc_labelMPRequestsN.gridy = 1;
		panel_1.add(labelMPRequestsN, gbc_labelMPRequestsN);
		
		btnClearMPRequests = new JButton("Clear");
		btnClearMPRequests.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				while(MPRequestsDM.getRowCount() > 0) MPRequestsDM.removeRow(0);
			}
		});
		GridBagConstraints gbc_btnClearMPRequests = new GridBagConstraints();
		gbc_btnClearMPRequests.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnClearMPRequests.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearMPRequests.gridx = 4;
		gbc_btnClearMPRequests.gridy = 2;
		panel_1.add(btnClearMPRequests, gbc_btnClearMPRequests);
		
		lblMpRequests = new JLabel("MP Requests (removed)");
		GridBagConstraints gbc_lblMpRequests = new GridBagConstraints();
		gbc_lblMpRequests.gridwidth = 2;
		gbc_lblMpRequests.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblMpRequests.insets = new Insets(0, 0, 5, 5);
		gbc_lblMpRequests.gridx = 0;
		gbc_lblMpRequests.gridy = 3;
		panel_1.add(lblMpRequests, gbc_lblMpRequests);
		
		lblNewLabel = new JLabel("MP Responses (removed)");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 2;
		gbc_lblNewLabel.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 3;
		gbc_lblNewLabel.gridy = 3;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		scrollPane_9 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_9 = new GridBagConstraints();
		gbc_scrollPane_9.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_9.gridwidth = 2;
		gbc_scrollPane_9.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_9.gridx = 0;
		gbc_scrollPane_9.gridy = 4;
		panel_1.add(scrollPane_9, gbc_scrollPane_9);
		
		table_MpRequestDeleted = new JTable(MPRequestsDeletedDM);
		scrollPane_9.setViewportView(table_MpRequestDeleted);
		
		lblMpRequestsDeleted = new JLabel("--");
		GridBagConstraints gbc_lblMpRequestsDeleted = new GridBagConstraints();
		gbc_lblMpRequestsDeleted.insets = new Insets(0, 0, 5, 5);
		gbc_lblMpRequestsDeleted.gridx = 2;
		gbc_lblMpRequestsDeleted.gridy = 4;
		panel_1.add(lblMpRequestsDeleted, gbc_lblMpRequestsDeleted);
		
		scrollPane_3 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.gridwidth = 2;
		gbc_scrollPane_3.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.gridx = 3;
		gbc_scrollPane_3.gridy = 4;
		panel_1.add(scrollPane_3, gbc_scrollPane_3);
		
		table_MPResponses = new JTable(MPResponsesDM);
		scrollPane_3.setViewportView(table_MPResponses);
		
		labelMPResponsesN = new JLabel("--");
		GridBagConstraints gbc_labelMPResponsesN = new GridBagConstraints();
		gbc_labelMPResponsesN.insets = new Insets(0, 0, 5, 0);
		gbc_labelMPResponsesN.gridx = 5;
		gbc_labelMPResponsesN.gridy = 4;
		panel_1.add(labelMPResponsesN, gbc_labelMPResponsesN);
		
		btnClearMPResponses = new JButton("Clear");
		btnClearMPResponses.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				while(MPResponsesDM.getRowCount() > 0) MPResponsesDM.removeRow(0);	
			}
		});
		
		btnClearMpRequestDeleted = new JButton("Clear");
		btnClearMpRequestDeleted.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				while(MPRequestsDeletedDM.getRowCount() > 0) MPRequestsDeletedDM.removeRow(0);		
			}
		});
		GridBagConstraints gbc_btnClearMpRequestDeleted = new GridBagConstraints();
		gbc_btnClearMpRequestDeleted.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearMpRequestDeleted.gridx = 1;
		gbc_btnClearMpRequestDeleted.gridy = 5;
		panel_1.add(btnClearMpRequestDeleted, gbc_btnClearMpRequestDeleted);
		GridBagConstraints gbc_btnClearMPResponses = new GridBagConstraints();
		gbc_btnClearMPResponses.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearMPResponses.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnClearMPResponses.gridx = 4;
		gbc_btnClearMPResponses.gridy = 5;
		panel_1.add(btnClearMPResponses, gbc_btnClearMPResponses);
		
		lblResourcePendingRequests = new JLabel("Resource Pending Requests (removed)");
		GridBagConstraints gbc_lblResourcePendingRequests = new GridBagConstraints();
		gbc_lblResourcePendingRequests.gridwidth = 5;
		gbc_lblResourcePendingRequests.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblResourcePendingRequests.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcePendingRequests.gridx = 0;
		gbc_lblResourcePendingRequests.gridy = 6;
		panel_1.add(lblResourcePendingRequests, gbc_lblResourcePendingRequests);
		
		scrollPane_2 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.gridwidth = 5;
		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 0;
		gbc_scrollPane_2.gridy = 7;
		panel_1.add(scrollPane_2, gbc_scrollPane_2);
		
		table_resourcePendingRequests = new JTable(pendingResourceRequestsDM);
		scrollPane_2.setViewportView(table_resourcePendingRequests);
		
		lblPendingResourceRequestsN = new JLabel("--");
		GridBagConstraints gbc_lblPendingResourceRequestsN = new GridBagConstraints();
		gbc_lblPendingResourceRequestsN.insets = new Insets(0, 0, 5, 0);
		gbc_lblPendingResourceRequestsN.gridx = 5;
		gbc_lblPendingResourceRequestsN.gridy = 7;
		panel_1.add(lblPendingResourceRequestsN, gbc_lblPendingResourceRequestsN);
		
		btnPendingResourceRequests = new JButton("Clear");
		btnPendingResourceRequests.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				while (pendingResourceRequestsDM.getRowCount() > 0) pendingResourceRequestsDM.removeRow(0);
			}
		});
		GridBagConstraints gbc_btnPendingResourceRequests = new GridBagConstraints();
		gbc_btnPendingResourceRequests.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnPendingResourceRequests.insets = new Insets(0, 0, 5, 5);
		gbc_btnPendingResourceRequests.gridx = 4;
		gbc_btnPendingResourceRequests.gridy = 8;
		panel_1.add(btnPendingResourceRequests, gbc_btnPendingResourceRequests);
		
		lblResourceRequests = new JLabel("Resource Requests (removed)");
		GridBagConstraints gbc_lblResourceRequests = new GridBagConstraints();
		gbc_lblResourceRequests.gridwidth = 2;
		gbc_lblResourceRequests.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblResourceRequests.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourceRequests.gridx = 0;
		gbc_lblResourceRequests.gridy = 9;
		panel_1.add(lblResourceRequests, gbc_lblResourceRequests);
		
		lblResourceResponses = new JLabel("Resource Responses (removed)");
		GridBagConstraints gbc_lblResourceResponses = new GridBagConstraints();
		gbc_lblResourceResponses.gridwidth = 2;
		gbc_lblResourceResponses.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblResourceResponses.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourceResponses.gridx = 3;
		gbc_lblResourceResponses.gridy = 9;
		panel_1.add(lblResourceResponses, gbc_lblResourceResponses);
		
		scrollPane_8 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_8 = new GridBagConstraints();
		gbc_scrollPane_8.gridwidth = 2;
		gbc_scrollPane_8.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_8.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_8.gridx = 0;
		gbc_scrollPane_8.gridy = 10;
		panel_1.add(scrollPane_8, gbc_scrollPane_8);
		
		table_resourceRequests = new JTable(resourceRequestsDM);
		scrollPane_8.setViewportView(table_resourceRequests);
		
		labelResourceRequests = new JLabel("--");
		GridBagConstraints gbc_labelResourceRequests = new GridBagConstraints();
		gbc_labelResourceRequests.insets = new Insets(0, 0, 5, 5);
		gbc_labelResourceRequests.gridx = 2;
		gbc_labelResourceRequests.gridy = 10;
		panel_1.add(labelResourceRequests, gbc_labelResourceRequests);
		
		scrollPane_7 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_7 = new GridBagConstraints();
		gbc_scrollPane_7.gridwidth = 2;
		gbc_scrollPane_7.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_7.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_7.gridx = 3;
		gbc_scrollPane_7.gridy = 10;
		panel_1.add(scrollPane_7, gbc_scrollPane_7);
		
		table_resourceResponses = new JTable(resourceResponsesDM);
		scrollPane_7.setViewportView(table_resourceResponses);
		
		labelResourceResponsesN = new JLabel("--");
		GridBagConstraints gbc_labelResourceResponsesN = new GridBagConstraints();
		gbc_labelResourceResponsesN.insets = new Insets(0, 0, 5, 0);
		gbc_labelResourceResponsesN.gridx = 5;
		gbc_labelResourceResponsesN.gridy = 10;
		panel_1.add(labelResourceResponsesN, gbc_labelResourceResponsesN);
		
		btnClearResourceRequests = new JButton("Clear");
		btnClearResourceRequests.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				while(resourceRequestsDM.getRowCount() > 0) resourceRequestsDM.removeRow(0);
			}
		});
		GridBagConstraints gbc_btnClearResourceRequests = new GridBagConstraints();
		gbc_btnClearResourceRequests.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnClearResourceRequests.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearResourceRequests.gridx = 1;
		gbc_btnClearResourceRequests.gridy = 11;
		panel_1.add(btnClearResourceRequests, gbc_btnClearResourceRequests);
		
		btnClearResourceResponses = new JButton("Clear");
		btnClearResourceResponses.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				while(resourceResponsesDM.getRowCount() > 0) resourceResponsesDM.removeRow(0);
			}
		});
		GridBagConstraints gbc_btnClearResourceResponses = new GridBagConstraints();
		gbc_btnClearResourceResponses.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnClearResourceResponses.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearResourceResponses.gridx = 4;
		gbc_btnClearResourceResponses.gridy = 11;
		panel_1.add(btnClearResourceResponses, gbc_btnClearResourceResponses);
		
		lblNewLabel_2 = new JLabel("MN Requests (removed)");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.gridwidth = 2;
		gbc_lblNewLabel_2.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 12;
		panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		lblMnResponses = new JLabel("MN Responses (removed)");
		GridBagConstraints gbc_lblMnResponses = new GridBagConstraints();
		gbc_lblMnResponses.gridwidth = 2;
		gbc_lblMnResponses.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblMnResponses.insets = new Insets(0, 0, 5, 5);
		gbc_lblMnResponses.gridx = 3;
		gbc_lblMnResponses.gridy = 12;
		panel_1.add(lblMnResponses, gbc_lblMnResponses);
		
		scrollPane_6 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_6 = new GridBagConstraints();
		gbc_scrollPane_6.gridwidth = 2;
		gbc_scrollPane_6.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_6.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_6.gridx = 0;
		gbc_scrollPane_6.gridy = 13;
		panel_1.add(scrollPane_6, gbc_scrollPane_6);
		
		table_MNRequests = new JTable(MNRequestsDM);
		scrollPane_6.setViewportView(table_MNRequests);
		
		labelMNRequestsN = new JLabel("--");
		GridBagConstraints gbc_labelMNRequestsN = new GridBagConstraints();
		gbc_labelMNRequestsN.insets = new Insets(0, 0, 5, 5);
		gbc_labelMNRequestsN.gridx = 2;
		gbc_labelMNRequestsN.gridy = 13;
		panel_1.add(labelMNRequestsN, gbc_labelMNRequestsN);
		
		scrollPane_5 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_5 = new GridBagConstraints();
		gbc_scrollPane_5.gridwidth = 2;
		gbc_scrollPane_5.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_5.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_5.gridx = 3;
		gbc_scrollPane_5.gridy = 13;
		panel_1.add(scrollPane_5, gbc_scrollPane_5);
		
		table_MNResponses = new JTable(MNResponsesDM);
		scrollPane_5.setViewportView(table_MNResponses);
		
		labelMNResponsesN = new JLabel("--");
		GridBagConstraints gbc_labelMNResponsesN = new GridBagConstraints();
		gbc_labelMNResponsesN.insets = new Insets(0, 0, 5, 0);
		gbc_labelMNResponsesN.gridx = 5;
		gbc_labelMNResponsesN.gridy = 13;
		panel_1.add(labelMNResponsesN, gbc_labelMNResponsesN);
		
		btnClearMNRequests = new JButton("Clear");
		btnClearMNRequests.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				while (MNRequestsDM.getRowCount()>0) MNRequestsDM.removeRow(0);
			}
		});
		GridBagConstraints gbc_btnClearMNRequests = new GridBagConstraints();
		gbc_btnClearMNRequests.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnClearMNRequests.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearMNRequests.gridx = 1;
		gbc_btnClearMNRequests.gridy = 14;
		panel_1.add(btnClearMNRequests, gbc_btnClearMNRequests);
		
		btnClearMNResponses = new JButton("Clear");
		btnClearMNResponses.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				while(MNResponsesDM.getRowCount() > 0) MNResponsesDM.removeRow(0);
			}
		});
		GridBagConstraints gbc_btnClearMNResponses = new GridBagConstraints();
		gbc_btnClearMNResponses.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearMNResponses.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnClearMNResponses.gridx = 4;
		gbc_btnClearMNResponses.gridy = 14;
		panel_1.add(btnClearMNResponses, gbc_btnClearMNResponses);
		
		lblNewLabel_1 = new JLabel("RDF Store Size (Triples)");
		lblNewLabel_1.setFont(new Font("Dialog", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridwidth = 5;
		gbc_lblNewLabel_1.anchor = GridBagConstraints.NORTH;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 15;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		lblTotalTriples = new JLabel("--");
		lblTotalTriples.setFont(new Font("Dialog", Font.BOLD, 12));
		GridBagConstraints gbc_lblTotalTriples = new GridBagConstraints();
		gbc_lblTotalTriples.gridwidth = 5;
		gbc_lblTotalTriples.insets = new Insets(0, 0, 0, 5);
		gbc_lblTotalTriples.anchor = GridBagConstraints.NORTH;
		gbc_lblTotalTriples.gridx = 0;
		gbc_lblTotalTriples.gridy = 16;
		panel_1.add(lblTotalTriples, gbc_lblTotalTriples);
		
		messageBox = new JLabel("Starting...");
		GridBagConstraints gbc_messageBox = new GridBagConstraints();
		gbc_messageBox.anchor = GridBagConstraints.WEST;
		gbc_messageBox.gridx = 0;
		gbc_messageBox.gridy = 1;
		frmSemanticGatewayManager.getContentPane().add(messageBox, gbc_messageBox);
	}

	@Override
	public void addedMPMappings(ArrayList<MPMapping> mappings) {
		for (MPMapping mapping : mappings) {
			Vector<Object> data = new Vector<Object>();
	        data.add(mapping.getProtocolURI());
	        data.add(mapping.getRequestPattern());
	        data.add(mapping.getResponsePattern());
	        data.add(mapping.getResourceURI());
	        data.add(mapping.getActionURI());
	        data.add(mapping.getActionValue());
	        data.add(mapping.getURI());
			protocolMappingDataModel.addRow(data);
		}		
	}

	@Override
	public void removedMPMappings(ArrayList<MPMapping> mappings) {
		for (MPMapping mapping : mappings){
			for (int index = 0; index < protocolMappingDataModel.getRowCount() ; index++) {
				if (protocolMappingDataModel.getValueAt(index, 6).toString().equals(mapping.getURI())) {
					protocolMappingDataModel.removeRow(index);
					break;
				}
			}
		}
	}

	@Override
	public void addedMNMappings(ArrayList<MNMapping> mappings) {
		for (MNMapping mapping : mappings) {
			Vector<Object> data = new Vector<Object>();
	        data.add(mapping.getNetworkURI());
	        data.add(mapping.getRequestPattern());
	        data.add(mapping.getResponsePattern());
	        data.add(mapping.getResourceURI());
	        data.add(mapping.getActionURI());
	        data.add(mapping.getActionValue());
	        data.add(mapping.getURI());
			networkMappingDataModel.addRow(data);
		}
	}

	@Override
	public void removedMNMappings(ArrayList<MNMapping> mappings) {
		for (MNMapping mapping : mappings){
			for (int index = 0; index < networkMappingDataModel.getRowCount() ; index++) {
				if (networkMappingDataModel.getValueAt(index, 6).toString().equals(mapping.getURI())) {
					networkMappingDataModel.removeRow(index);
					break;
				}
			}
		}
	}

	@Override
	public void newMNMapping(MNMapping mapping) {
		mappingManager.addNetworkMapping(mapping.getNetworkURI(), mapping.getRequestPattern(), mapping.getResponsePattern(), 
				mapping.getResourceURI(), mapping.getActionURI(), mapping.getActionValue());
	}

	@Override
	public void newMPMapping(MPMapping mapping) {
		mappingManager.addProtocolMapping(mapping.getProtocolURI(), mapping.getRequestPattern(), mapping.getResponsePattern(), 
				mapping.getResourceURI(), mapping.getActionURI(), mapping.getActionValue());
	}

	@Override
	public void removedResourcePendingRequest(String resource, String action, String value) {
		Vector<Object> data = new Vector<Object>();
		data.add(new java.sql.Timestamp(new java.util.Date().getTime()));
        data.add(resource);
        data.add(action);
        data.add(value);
        pendingResourceRequestsDM.addRow(data);

    	lblPendingResourceRequestsN.setText(String.format("%d",++pendingResourceRequestsN));
	}

	@Override
	public void removedResourceRequest(String resource, String action, String value) {
		Vector<Object> data = new Vector<Object>();
		data.add(new java.sql.Timestamp(new java.util.Date().getTime()));
		data.add(resource);
        data.add(action);
        data.add(value);
        resourceRequestsDM.addRow(data);
        
        labelResourceRequests.setText(String.format("%d",++resourceRequestsN)); 
	}

	@Override
	public void removedResourceResponse(String resource, String action, String value) {
		Vector<Object> data = new Vector<Object>();
		data.add(new java.sql.Timestamp(new java.util.Date().getTime()));
		data.add(resource);
        data.add(action);
        data.add(value);
        resourceResponsesDM.addRow(data);
        
        labelResourceResponsesN.setText(String.format("%d",++resourceResponsesN));
	}

	@Override
	public void removedMPResponse(String protocol, String value) {
		Vector<Object> data = new Vector<Object>();
		data.add(new java.sql.Timestamp(new java.util.Date().getTime()));
		data.add(protocol);
        data.add(value);
        MPResponsesDM.addRow(data);
        
        labelMPResponsesN.setText(String.format("%d",++MPResponsesN));
	}

	@Override
	public void newMPRequest(String protocol, String value) {
		Vector<Object> data = new Vector<Object>();
		data.add(new java.sql.Timestamp(new java.util.Date().getTime()));
		data.add(protocol);
        data.add(value);
        MPRequestsDM.addRow(data);
        
        labelMPRequestsN.setText(String.format("%d",++MPRequestsN));
	}

	@Override
	public void removedMNResponse(String network, String value) {
		Vector<Object> data = new Vector<Object>();
		data.add(new java.sql.Timestamp(new java.util.Date().getTime()));
		data.add(network);
        data.add(value);
        MNResponsesDM.addRow(data);
        
        labelMNResponsesN.setText(String.format("%d",++MNResponsesN));
	}

	@Override
	public void removedMNRequest(String network, String value) {
		Vector<Object> data = new Vector<Object>();
		data.add(new java.sql.Timestamp(new java.util.Date().getTime()));
		data.add(network);
        data.add(value);
        MNRequestsDM.addRow(data);
        
        labelMNRequestsN.setText(String.format("%d",++MNRequestsN));
	}

	@Override
	public void totalTriples(long triples) {
		lblTotalTriples.setText(String.format("%d", triples));
		
	}

	@Override
	public void removedMPRequest(String protocol, String value) {
		Vector<Object> data = new Vector<Object>();
		data.add(new java.sql.Timestamp(new java.util.Date().getTime()));
		data.add(protocol);
        data.add(value);
        MPRequestsDeletedDM.addRow(data);
        
        lblMpRequestsDeleted.setText(String.format("%d",++MPRequestsDeletedN));
		
	}
}
