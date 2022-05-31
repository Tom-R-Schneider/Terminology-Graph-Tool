package Terminology;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Terminology_interface frame = new Terminology_interface();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Terminology_interface() throws IOException {
		// Initialize JFrame
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		//Initialize JPanel
		contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());
		setContentPane(contentPane);

		ui_elements = new JSONObject();
		// Get data from Excel sheet
		load_data();
		// Draw terminology graph using selected domain and configured relation paths
		draw_graph();
		//create_unused_term_column();
		//create_used_term_column();
		
		// Recreate Graph using JTree
		TreeGraph tree_graph = (TreeGraph) ui_elements.get("term_graph");
		JSONObject term_graph = tree_graph.get_term_graph();
	    DnDJTree newContentPane = new DnDJTree(term_graph, domain, term_no_path);
	    newContentPane.setOpaque(true);
	    GridBagConstraints c = get_ui_grid_specifics("used_tree");
	    add(newContentPane, c);
























	}
	
	// Very likely will be completly redone from scratch
	public void create_term_tree() {
		
		JLabel domain_term_label = new JLabel("Domain Terms");

		GridBagConstraints c = new GridBagConstraints();
		c = get_ui_grid_specifics("checkbox_tree_label");
		contentPane.add(domain_term_label, c);

		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Domain");

		String[] term_list = {"ant", "bee", "beetle", "worm"};
		JSONObject term_map = new JSONObject();
		String temp_letter;

		for (int i = 0; i < term_list.length; i++) {

			temp_letter = String.valueOf(term_list[i].charAt(0));

			if (!(term_map.containsKey(temp_letter))) {

				term_map.put(temp_letter, new JSONArray());
			}

			JSONArray arr = (JSONArray) term_map.get(temp_letter);        
			arr.add(term_list[i]);
		}

		System.out.println(term_map);

		DefaultMutableTreeNode category = null;
		DefaultMutableTreeNode book = null;

		
		// Sort terms (keys) for visualization
		Set<String> setOfKeys = term_map.keySet();

		TreeSet<String> myTreeSet = new TreeSet<String>();
		myTreeSet.addAll(setOfKeys);

		String[] sorted_letters = Arrays.stream(myTreeSet.toArray())
				.<String>map((Object v) -> v.toString()).toArray(String[]::new);

		System.out.println(sorted_letters);


		for (String key : sorted_letters) {

			System.out.println(key);
			System.out.println(term_map);

			JSONArray letter_terms = (JSONArray) term_map.get(key);
			System.out.println(letter_terms);

			category = new DefaultMutableTreeNode(key);
			top.add(category);

			for (int i = 0; i < letter_terms.size(); i++) {  
				book = new DefaultMutableTreeNode(letter_terms.get(i));
				category.add(book);
			}
		}

		JTree  tree = new JTree(top);
		TreeModel tree_model = tree.getModel();
		JCheckBoxTree checkbox_tree = new JCheckBoxTree();
		checkbox_tree.setModel(tree_model);
		JScrollPane treeView = new JScrollPane(checkbox_tree);
		ui_elements.put("tree_view", checkbox_tree);

		c = get_ui_grid_specifics("checkbox_tree");
		contentPane.add(treeView, c);
		checkbox_tree.addCheckChangeEventListener(new JCheckBoxTree.CheckChangeEventListener() {
			public void checkStateChanged(JCheckBoxTree.CheckChangeEvent event) {
				// For Debugging (correctness and laziness)
				update_selected_term_list();

			}
		});
	}
	
	//Used to store GridBagConstraints for all elements in one place
	public GridBagConstraints get_ui_grid_specifics(String element) {

		GridBagConstraints c = new GridBagConstraints();

		switch(element)  {

		case "checkbox_tree_label":
			c.gridx = 0;
			c.gridy = 0;
			break;

		case "unused_tree":
			c.gridx = 0;
			c.gridy = 1;
			c.weighty = 1;
			c.anchor = GridBagConstraints.NORTHWEST;
			break;

		case "selected_term_label":
			c.gridx = 1;
			c.gridy = 0;
			break;

		case "used_tree":
			c.gridx = 1;
			c.gridy = 1;
			c.anchor = GridBagConstraints.NORTHWEST;
			break;
		case "term_graph":
			c.gridx = 2;
			c.gridy = 1;
			c.gridwidth = 3;
			c.gridheight = 3;
			c.anchor = GridBagConstraints.NORTHWEST;
			break;
		}
		return c;
	}
	
	// Very likely will be completly redone from scratch
	public void update_selected_term_list() {
		JList selected_terms = (JList) ui_elements.get("selected_terms");
		String[] test = {"Hxllo", "Guten Tag"};

		JCheckBoxTree tree = (JCheckBoxTree) ui_elements.get("tree_view");

		DefaultListModel<String> listModel = new DefaultListModel<>();
		TreePath[] checked_paths = tree.getCheckedPaths();

		for (TreePath path : checked_paths) {

			String temp_path = path.toString();
			String[] nodes = temp_path.split(",");

			if (nodes.length == 3) {

				String term = nodes[2].replaceAll("]", "");
				listModel.addElement(term);
			}
		}
		selected_terms.setModel(listModel);
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
	public void update_graph() {

		TreeGraph canvas = (TreeGraph) ui_elements.get("term_graph");	
	}

	// Used to load data from excel sheet into application
	public void load_data() throws IOException {

		domain = "Rohr";
		terms = new JSONArray();
		//reading data from a file in the form of bytes  
		FileInputStream fis = new FileInputStream("C:\\Users\\Tom Schneider\\Desktop\\terminology_data.xlsx");  
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
	
	public void create_used_term_column() {
		
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Used Terms in " + domain);
		
		// Recreate Graph using JTree
		TreeGraph tree_graph = (TreeGraph) ui_elements.get("term_graph");
		JSONObject term_graph = tree_graph.get_term_graph();
		
		Set<String> keys = term_graph.keySet();
		for (String key: keys) {
			
			rec_create_used_term_nodes((JSONObject) term_graph.get(key), top);
		}

		
	    JTree tree = new JTree(top);
	    JScrollPane treeView = new JScrollPane(tree);
	    GridBagConstraints c = get_ui_grid_specifics("used_tree");
		contentPane.add(treeView, c);
	}
	
	public void create_unused_term_column() {
		
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Unused Terms in " + domain);
		DefaultMutableTreeNode letter = null;
	    DefaultMutableTreeNode word = null;
		JSONObject letters_used = new JSONObject();
		
		// Create Branches for letters used and store terms in them 
		for (int i = 0; i < term_no_path.size(); i++) {	
		    
		    String term = (String) term_no_path.get(i);
			char term_char = Character.toUpperCase(term.charAt(0));
			
		    if (!letters_used.containsKey(term_char)) {
		    	
		    	letter = new DefaultMutableTreeNode(term_char);
		    	top.add(letter);
		    	letters_used.put(term_char, letter);  	
		    }
		    
		    letter = (DefaultMutableTreeNode) letters_used.get(term_char);
		    letter.add(new DefaultMutableTreeNode(term));
		}
		
	    JTree tree = new JTree(top);
	    JScrollPane treeView = new JScrollPane(tree);
	    GridBagConstraints c = get_ui_grid_specifics("unused_tree");
		contentPane.add(treeView, c);
		
	}
	

}





































