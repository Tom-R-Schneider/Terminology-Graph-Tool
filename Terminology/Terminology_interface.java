package Terminology;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class Terminology_interface extends JFrame {

	private JPanel contentPane;
	// Used to store all UI elements 
	private JSONObject ui_elements;
	// Used to store all terms and their relation paths
	private JSONArray terms;
	private String domain;
	// All terms without relation path for display
	private JSONArray term_no_path;
	private Terminology_interface frame;
	private JSONArray new_terms;
	private JSONArray deleted_terms;
	private String excel_path;

	public Terminology_interface(String domain) throws IOException {
		// Initialize JFrame
		String curr_path = System.getProperty("user.dir");
		excel_path = curr_path + "\\data\\terminology_data.xlsx";
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1500, 750);
		setLocationRelativeTo(null);
		//Initialize JPanel
		contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());
		setContentPane(contentPane);
		this.contentPane = contentPane;
		this.domain = domain;

		ui_elements = new JSONObject();
		// Get data from Excel sheet
		load_data();
		// Draw terminology graph using selected domain and configured relation paths
		create_labels();
		draw_graph();
		
		create_term_columns();
	
		create_buttons();























	}
	
	public void create_labels() {
		JLabel l = new JLabel();
		 
        // add text to label
        l.setText("Unused domain terms");
        
        GridBagConstraints c = get_ui_grid_specifics("unused_term_label");
        
        add(l, c);
        
        l.setText("Used domain terms");
        c = get_ui_grid_specifics("used_term_label");
        add(l, c);
        
	}
	
	//Used to store GridBagConstraints for all elements in one place
	public GridBagConstraints get_ui_grid_specifics(String element) {

		GridBagConstraints c = new GridBagConstraints();

		switch(element)  {

		case "unused_term_label":
			c.gridx = 0;
			c.gridy = 0;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.25;
			break;

		case "unused_and_used_terms":
			c.gridx = 0;
			c.gridy = 1;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 0.5;
			c.weighty = 0.5;
			break;

		case "used_term_label":
			c.gridx = 1;
			c.gridy = 0;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.25;
			break;

		case "term_graph":
			c.gridx = 2;
			c.gridy = 0;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.gridheight = 3;
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1.0;
			break;
		case "add_button":
			c.gridx = 0;
			c.gridy = 2;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.25;
			break;
		case "save_button":
			c.gridx = 1;
			c.gridy = 2;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 0.25;
			break;
			
		}
		return c;
	}

	// Used to prepare information for creating a TreeGraph (JPanel) for relation visualization
	public void draw_graph() {
		JSONArray relations_list = new JSONArray();
		term_no_path = new JSONArray();

		for (int i = 0; i < terms.size(); i++) {

			String[] term_data = (String[]) terms.get(i);
			if (term_data[1] != "") {

				relations_list.add(term_data[1]);
			} else {

				term_no_path.add(term_data[0]);
			}
		}
		GridBagConstraints c = get_ui_grid_specifics("term_graph");
		TreeGraph canvas = new TreeGraph(relations_list);
		JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(new Dimension(500,500));
		scroll.setViewportView(canvas);
		ui_elements.put("term_graph", canvas);
		getContentPane().add(scroll, c);
	}

	// Used to load data from excel sheet into application
	public void load_data() throws IOException {

		terms = new JSONArray();
		new_terms = new JSONArray();
		deleted_terms = new JSONArray();
		//reading data from a file in the form of bytes  
		FileInputStream fis = new FileInputStream(excel_path);  
		//constructs an XSSFWorkbook object, by buffering the whole stream into the memory  
		XSSFWorkbook wb = new XSSFWorkbook(fis);  


		Sheet sheet = wb.getSheetAt(0);   //getting the XSSFSheet object at given index  
		int rows = sheet.getPhysicalNumberOfRows();
		System.out.println(rows);
		for (int i = 1; i < rows; i++) {
			Row row = sheet.getRow(i);
			Cell cell = row.getCell(2);
			String term_domain = cell.getStringCellValue(); 
			System.out.println(term_domain);
			System.out.println(domain);

			if (term_domain.equals(domain)) {

				cell = row.getCell(0);
				String term_name = cell.getStringCellValue(); 

				cell = row.getCell(1);
				String relations;
				
				if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {

					relations = "";

				} else {

					relations = cell.getStringCellValue(); 
				}

				System.out.println(relations);
				String[] term_data = {term_name, relations};
				terms.add(term_data);

			}
			

		}  
	}
	
	public void create_term_columns() {
		
		// Recreate Graph using JTree
		TreeGraph tree_graph = (TreeGraph) ui_elements.get("term_graph");
		JSONObject term_graph = tree_graph.get_term_graph();
	    DnDJTree newContentPane = new DnDJTree(term_graph, domain, term_no_path, this);
	    newContentPane.setOpaque(true);
	    GridBagConstraints c = get_ui_grid_specifics("unused_and_used_terms");
	    ui_elements.put("term_columns", newContentPane);
	    add(newContentPane, c);
	}
	
	public void update_term_graph(String term, String relation_path) {
		
		term_no_path.remove(term);
		String[] term_relation = {term, relation_path};
		terms.add(term_relation);
		TreeGraph curr_drawing = (TreeGraph) ui_elements.get("term_graph");
		
		JSONArray relations_list = new JSONArray();
		term_no_path = new JSONArray();

		for (int i = 0; i < terms.size(); i++) {

			String[] term_data = (String[]) terms.get(i);
			if (term_data[1] != "") {

				relations_list.add(term_data[1]);
			} else {

				term_no_path.add(term_data[0]);
			}
		}
		curr_drawing.update_graph(relations_list);
	}
	
	public void create_buttons() {
		
		//Button and Logic for adding terms into the domain 
		JButton add_terms = new JButton("Add new term");  
		add_terms.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
	            String term = JOptionPane.showInputDialog(frame, "Add a new term");
	            String[] term_data = {term, ""};
				terms.add(term_data);
				new_terms.add(term);
				DnDJTree term_columns = (DnDJTree) ui_elements.get("term_columns");
				term_columns.update_unused_terms(term);
	        }  
	    });
		GridBagConstraints c = get_ui_grid_specifics("add_button");
		getContentPane().add(add_terms, c);
		
		//Button and Logic for saving changes in the domain
		JButton save_button = new JButton("Save Changes");  
		save_button.addActionListener(new ActionListener(){  
			public void actionPerformed (ActionEvent e){  
				
				// Change terms to JSONObject at some point as it is overall better for the project
				JSONObject term_json = new JSONObject();
				JSONArray append_list = new JSONArray();
				for (int i = 0; i < terms.size(); i++) {
					
					String[] temp_term = (String[]) terms.get(i);
					term_json.put(temp_term[0], temp_term[1]);
				}
				//reading data from a file in the form of bytes  
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(excel_path);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}  
				//constructs an XSSFWorkbook object, by buffering the whole stream into the memory  
				XSSFWorkbook wb = null;
				try {
					wb = new XSSFWorkbook(fis);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}  


				Sheet sheet = wb.getSheetAt(0);   //getting the XSSFSheet object at given index  
				int rows = sheet.getPhysicalNumberOfRows();
				System.out.println(rows);
				for (int i = 1; i < rows; i++) {
					Row row = sheet.getRow(i);
					Cell cell = row.getCell(2);
					String term_domain = cell.getStringCellValue(); 
					System.out.println(term_domain);
					System.out.println(domain);

					if (term_domain.equals(domain)) {

						cell = row.getCell(0);
						String term_name = cell.getStringCellValue();

						cell = row.getCell(1);
						String relations;
					

						if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
							row.createCell(1).setCellValue("");
							cell = row.getCell(1);

						}

						relations = cell.getStringCellValue(); 
						
							
						if (!relations.equals(term_json.get(term_name))) {
							System.out.println("Look here");
							System.out.println(term_json.get(term_name));
							cell.setCellValue((String) term_json.get(term_name));
						}

						System.out.println(relations);
						String[] term_data = {term_name, relations};
						terms.add(term_data);

					}
					

				}  
				for (int i = 0; i < new_terms.size(); i++) {
					Row row = sheet.createRow(rows + i);
					row.createCell(0).setCellValue((String) new_terms.get(i));
				    row.createCell(1).setCellValue((String) term_json.get(new_terms.get(i)));
				    row.createCell(2).setCellValue(domain);
					
				}
				
				FileOutputStream os = null;
				try {
					os = new FileOutputStream(excel_path);
				} catch (FileNotFoundException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				try {
					wb.write(os);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				
					
	        }  
	    });
		c = get_ui_grid_specifics("save_button");
		getContentPane().add(save_button, c);
	}
	
	public void update_deleted_terms(String term_to_be_deleted) {
		
		deleted_terms.add(term_to_be_deleted);
	}
	

}





































