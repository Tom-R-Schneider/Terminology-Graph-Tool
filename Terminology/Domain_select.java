package Terminology;

import java.awt.Button;
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
	
	public static void main(String[] args) throws IOException {
		Domain_select domain_frame = new Domain_select();
		domain_frame.setLocationRelativeTo(null);
		domain_frame.setVisible(true);


	}
	
	public Domain_select() throws IOException {
		
		String curr_path = System.getProperty("user.dir");
		// Check if data folder exists
		if (Files.notExists(Paths.get(curr_path + "\\data"))) {
			new File(curr_path + "\\data").mkdirs();
		}
		excel_path = curr_path + "\\data\\terminology_data.xlsx";
		// Check if excel file exists
		if (Files.notExists(Paths.get(excel_path))) {
			
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
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		GridBagConstraints c = new GridBagConstraints();
		//Initialize JPanel
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new GridBagLayout());
		setContentPane(contentPane);
		JLabel label = new JLabel("Please select a domain: ");
		
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		contentPane.add(label,c);
		
		String[] domains = get_domains();
		JComboBox<String> domain_picklist = new JComboBox<String>(domains);
		c.gridx = 1;
		c.gridy = 0;
		contentPane.add(domain_picklist, c);
		
		JButton start_terminology_interface_button = new JButton("OK");
		start_terminology_interface_button.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
				String selected_domain = (String) domain_picklist.getSelectedItem();
				dispose();
				try {
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
		
		
		JButton add_new_domain = new JButton("Add new domain");
		add_new_domain.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
				
				try {
					add_domain(domain_picklist);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
	        }  
	    });		
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(10,0,0,0);  //top padding
		c.anchor = GridBagConstraints.SOUTHWEST;
		contentPane.add(add_new_domain, c);
		
		
		JButton remove_domain = new JButton("Remove selected domain");
		remove_domain.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
				
				try {
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
	public String[] get_domains() throws IOException {
		
		
		JSONArray domains = new JSONArray();
		FileInputStream fis = new FileInputStream(excel_path);  
		//constructs an XSSFWorkbook object, by buffering the whole stream into the memory  
		XSSFWorkbook wb = new XSSFWorkbook(fis);  


		Sheet sheet = wb.getSheetAt(1);   //getting the XSSFSheet object at given index  
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
		for (int i = 0; i < domains.size(); i++)  {
			domain_array[i] = (String) domains.get(i);
		}
		
		return domain_array;
	}
	
	public void add_domain(JComboBox domain_picklist) throws IOException {
		
		String domain = JOptionPane.showInputDialog(this, "Add a new domain");
		System.out.println(domain);
		
		JSONArray domains = new JSONArray();
		FileInputStream fis = new FileInputStream(excel_path);  
		//constructs an XSSFWorkbook object, by buffering the whole stream into the memory  
		XSSFWorkbook wb = new XSSFWorkbook(fis);  


		Sheet sheet = wb.getSheetAt(1);   //getting the XSSFSheet object at given index  
		int rows = sheet.getPhysicalNumberOfRows();
		
		System.out.println(rows);
		Row row = sheet.createRow(rows);
		row.createCell(0).setCellValue(domain);
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
	
public void delete_domain(JComboBox domain_picklist) throws IOException {
		
		String selected_domain = (String) domain_picklist.getSelectedItem();
		
		FileInputStream fis = new FileInputStream(excel_path);  
		//constructs an XSSFWorkbook object, by buffering the whole stream into the memory  
		XSSFWorkbook wb = new XSSFWorkbook(fis);  


		Sheet sheet = wb.getSheetAt(1);   //getting the XSSFSheet object at given index  
		int rows = sheet.getPhysicalNumberOfRows();
		
		// Start at i = 1 to ignore header
		// First delete domain
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
		sheet = wb.getSheetAt(0);   //getting the XSSFSheet object at given index  
		rows = sheet.getPhysicalNumberOfRows();
		// Start at i = 1 to ignore header
		// Second delete all terms with deleted domain
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
