package Terminology;

import java.awt.*;       // Using AWT's Graphics and Color
import java.awt.event.*; // Using AWT event classes and listener interfaces
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;    // Using Swing's components and containers

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

	


public class TreeGraph extends JPanel {
	// Override paintComponent to perform your own painting
	private String[] layers;
	private JPanel drawing;
	private JSONArray term_relations;
	private JPanel contentPane;
	private GridBagConstraints c;
	public int x;
	public int y;

	public TreeGraph(String[] paths) {
		get_graph_layers(paths);
		JPanel drawing = new JPanel();
		this.drawing = drawing;


	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);  
		// paint parent's background
		setBackground(Color.WHITE);  // set background color for this JPanel
		int[] string_lengths = new int[layers.length];
		int max_length = 0;
		g.setFont(new Font("Monospaced", Font.PLAIN, 12));
		for (int i = 0; i < layers.length; i++) {

			string_lengths[i] = g.getFontMetrics().stringWidth(layers[i]);
			System.out.println(string_lengths[i]);
			if (string_lengths[i] > max_length) {

				max_length = string_lengths[i];
			}	
		}
		int y = layers.length * 50 + 50;
		int x = max_length + 150;
		this.x = x;
		this.y = y;
		System.out.println(x + ", " + y);
		this.setPreferredSize(new Dimension(x, y));
		String[] words_in_layer;
		g.setColor(Color.BLACK);    // set the drawing color
		int anchor_x;
		int anchor_y;
		JSONObject word_anchor_points = new JSONObject();
		JSONObject word_coordinates = new JSONObject();
		for (int i = 0; i < layers.length; i++) {
			JSONArray word_arr = new JSONArray();
			words_in_layer = layers[i].split("      ");
			anchor_x = (x - string_lengths[i]) / 2;
			anchor_y = i * 50 + 50;
			g.drawString(layers[i], anchor_x, anchor_y);

			int word_anchor_x = 0;
			int word_anchor_y = anchor_y - 10;
			for (int j = 0; j < words_in_layer.length; j++) {
				if (word_anchor_x == 0) {

					word_anchor_x = anchor_x + g.getFontMetrics().stringWidth(words_in_layer[j])/2;

				} else {

					word_anchor_x += g.getFontMetrics().stringWidth(words_in_layer[j - 1])/2 + g.getFontMetrics().stringWidth("      ") + g.getFontMetrics().stringWidth(words_in_layer[j])/2;
				}

				int[] word_coordinate = {i, j};
				word_coordinates.put(words_in_layer[j], word_coordinate);
				int[][] single_word_anchors = {{word_anchor_x, word_anchor_y + 15},{word_anchor_x, word_anchor_y}};
				word_arr.add(single_word_anchors);
			}
			word_anchor_points.put(i, word_arr);

		}	
		for (int i = 0; i < term_relations.size(); i++) {

			String[] term_relation = (String[]) term_relations.get(i);
			int[] line_coordinates = new int[4];

			for (int j = 0; j < 2; j++) {
				int[] word = (int[]) word_coordinates.get(term_relation[j]);
				JSONArray word_arr = (JSONArray) word_anchor_points.get(word[0]);
				int[][] word_coor = (int[][]) word_arr.get(word[1]);
				line_coordinates[j*2] = word_coor[j][0];
				line_coordinates[j*2+1] = word_coor[j][1];
			}
			g.drawLine(line_coordinates[0], line_coordinates[1], line_coordinates[2], line_coordinates[3]);
		}
	}
	public void update_params(String[] layers) {

	}
	public void get_graph_layers(String[] paths) {
		JSONObject layers = new JSONObject();
		term_relations = new JSONArray();
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
			for (int i = 0; i < nodes.length - 1; i++) {

				String[] relation = {nodes[i], nodes[i+1]};
				term_relations.add(relation);
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
				layer_strings[i] += temp_term + "      ";
			}
			layer_strings[i] = layer_strings[i].replaceAll("null", "");
			layer_strings[i] = layer_strings[i].substring(0, layer_strings[i].length() -6);
			System.out.println(layer_strings[i]);

			if (temp_chars > counter[1]) {

				counter[1] = temp_chars;
				counter[0] = i;
			}
		}

		this.layers = layer_strings;
	}
}


