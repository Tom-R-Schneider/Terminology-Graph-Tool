package Terminology;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;

public class Domain_select extends JFrame {

	private String excel_path;

	// Start the application
	public static void main(String[] args) throws IOException {
		
		Domain_select domain_frame = new Domain_select();
		domain_frame.setLocationRelativeTo(null);
		domain_frame.setVisible(true);

	}

	public Domain_select() throws IOException {

		String curr_path = System.getProperty("user.dir");
		excel_path = curr_path + "\\data\\terminology_data.xlsx";
		
		// Check if data folder exists and create it if not 
		if (Files.notExists(Paths.get(curr_path + "\\data"))) {
			new File(curr_path + "\\data").mkdirs();
		}
		
		// Check if excel file exists and create it if not 
		if (Files.notExists(Paths.get(excel_path))) {

			// 2 Sheets: 1 for domains and 1 for terms
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet("terms");

			Row row = sheet.createRow(0);
			row.createCell(0).setCellValue("term");
			row.createCell(1).setCellValue("relations");
			row.createCell(2).setCellValue("domain");

			sheet = workbook.createSheet("domains");
			row = sheet.createRow(0);
			row.createCell(0).setCellValue("domain");

			try (FileOutputStream outputStream = new FileOutputStream(excel_path)) {
				workbook.write(outputStream);
			}
		}
		
		// Create the initial frame to ask the user to select/delete/add domain
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		GridBagConstraints c = new GridBagConstraints();
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());
		setContentPane(contentPane);
		
		JLabel label = new JLabel("Please select a domain: ");
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		contentPane.add(label, c);

		// Initialize domains that were already configured by the user
		String[] domains = get_domains();
		JComboBox<String> domain_picklist = new JComboBox<String>(domains);
		c.gridx = 1;
		c.gridy = 0;
		contentPane.add(domain_picklist, c);

		// Button to start the actual tool
		JButton start_terminology_interface_button = new JButton("OK");
		start_terminology_interface_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selected_domain = (String) domain_picklist.getSelectedItem();
				dispose();
				try {
					// Terminology_interface from the java file of the same name
					Terminology_interface frame = new Terminology_interface(selected_domain);
					frame.setVisible(true);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		c.gridx = 2;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		contentPane.add(start_terminology_interface_button, c);

		// Button to add new domains
		JButton add_new_domain = new JButton("Add new domain");
		add_new_domain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					// Function to add new domain to excel sheet and picklist
					add_domain(domain_picklist);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(10, 0, 0, 0); // top padding
		c.anchor = GridBagConstraints.SOUTHWEST;
		contentPane.add(add_new_domain, c);

		// Button to delete selected domain
		JButton remove_domain = new JButton("Remove selected domain");
		remove_domain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					// Function to delete selected domain from picklist and excel sheet
					delete_domain(domain_picklist);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.SOUTHEAST;
		contentPane.add(remove_domain, c);

	}
	
	// LOad domains from excel sheet to let user select project domain
	public String[] get_domains() throws IOException {

		JSONArray domains = new JSONArray();
		FileInputStream fis = new FileInputStream(excel_path);
		XSSFWorkbook wb = new XSSFWorkbook(fis);

		Sheet sheet = wb.getSheetAt(1); // getting the XSSFSheet object at given index
		int rows = sheet.getPhysicalNumberOfRows();

		// Start at i = 1 to ignore header
		for (int i = 1; i < rows; i++) {
			Row row = sheet.getRow(i);
			Cell cell = row.getCell(0);

			if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {

				break;
			}
			String domain = cell.getStringCellValue();
			System.out.println(domain);
			domains.add(domain);
		}

		// Convert JSONArray to String[]
		String[] domain_array = new String[domains.size()];
		for (int i = 0; i < domains.size(); i++) {
			domain_array[i] = (String) domains.get(i);
		}

		return domain_array;
	}

	// Function to add new domain to excel sheet and picklist
	public void add_domain(JComboBox domain_picklist) throws IOException {

		// Ask user to provide new domain in option pane
		String domain = JOptionPane.showInputDialog(this, "Add a new domain");
		System.out.println(domain);

		FileInputStream fis = new FileInputStream(excel_path);
		XSSFWorkbook wb = new XSSFWorkbook(fis);

		// Append new domain to existing domains
		Sheet sheet = wb.getSheetAt(1);
		int rows = sheet.getPhysicalNumberOfRows();
		System.out.println(rows);
		Row row = sheet.createRow(rows);
		row.createCell(0).setCellValue(domain);
		// Add new domain to picklist as well 
		domain_picklist.addItem(domain);
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

	// Function to delete selected domain from picklist and excel sheet
	public void delete_domain(JComboBox domain_picklist) throws IOException {

		// Get selected domain to delete
		String selected_domain = (String) domain_picklist.getSelectedItem();

		FileInputStream fis = new FileInputStream(excel_path);
		XSSFWorkbook wb = new XSSFWorkbook(fis);

		Sheet sheet = wb.getSheetAt(1); // getting the XSSFSheet object at given index
		int rows = sheet.getPhysicalNumberOfRows();

		// First delete domain from domain sheet
		// Start at i = 1 to ignore header
		for (int i = 1; i < rows; i++) {
			Row row = sheet.getRow(i);
			Cell cell = row.getCell(0);
			String curr_domain = cell.getStringCellValue();
			if (selected_domain.contentEquals(curr_domain)) {

				sheet.removeRow(sheet.getRow(i));
				sheet.shiftRows(i + 1, rows, -1);
				domain_picklist.removeItem(curr_domain);
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
				break;
			}

		}
		
		sheet = wb.getSheetAt(0); // getting the XSSFSheet object at given index
		rows = sheet.getPhysicalNumberOfRows();
		
		// Second delete all terms with deleted domain
		// Start at i = 1 to ignore header
		for (int i = 1; i < rows; i++) {
			System.out.println(i);
			Row row = sheet.getRow(i);
			Cell cell = row.getCell(2);
			String curr_domain = cell.getStringCellValue();
			
			if (selected_domain.contentEquals(curr_domain)) {

				sheet.removeRow(sheet.getRow(i));
				sheet.shiftRows(i + 1, rows, -1);
				i--;
				rows--;
			}

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

}
