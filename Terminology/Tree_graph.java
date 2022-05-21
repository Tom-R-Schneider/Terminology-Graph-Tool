package Terminology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class Tree_graph {
	
	
	String domain;
	JSONObject layers = new JSONObject();
	
	Tree_graph(String[] paths) {
		
		for (String path: paths) {
			String[] nodes = path.split("/");
			
			for (int i = 0; i < nodes.length; i++) {
				
				if (!(layers.containsKey(i))) {
					
					layers.put(i, new JSONArray());
				}
				
				JSONArray arr = (JSONArray) layers.get(i);  
				if (!arr.contains(nodes[i])) {
					
					arr.add(nodes[i]);
				}
	        }
			
		}
		
		int [] counter = {0, 0};
		int temp_chars;
		String temp_term;
		String[] layer_strings = new String[layers.size()];
		
		for (int i = 0; i < layers.size(); i++) {
			
			temp_chars = 0;
			List arr = (ArrayList) layers.get(i);
			
			for (int j = 0; j < arr.size(); j++) {
				temp_term = (String) arr.get(j);
				temp_chars += temp_term.length();
				layer_strings[i] += temp_term + "______";
			}
			layer_strings[i] = layer_strings[i].replaceAll("null", "");
			layer_strings[i] = layer_strings[i].substring(0, layer_strings[i].length() -6);
			
			if (temp_chars > counter[1]) {
				
				counter[1] = temp_chars;
				counter[0] = i;
			}
		}
		
		System.out.println("Layer " + counter[0] + " has the most Chars with " + counter[1]);
		System.out.println(layer_strings[1]);
	}
	
	public void attach_listener() {
		
		
	}

	
}

