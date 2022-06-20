package Terminology;

import java.awt.*; // Using AWT's Graphics and Color
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.*; // Using Swing's components and containers

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TreeGraph extends JPanel {

	private String[] layer_strings; // Describes all terms per layer
	private JSONObject layers;
	private JSONObject term_graph; // Stores terms and their relation path
	private JSONArray term_relations;

	public TreeGraph(JSONArray paths) {
		get_graph_layers(paths);
		JPanel drawing = new JPanel();
	}

	// Override paintComponent to perform your own painting
	// Currently the only available approach is to center every layer of the graph
	// More options in later updates
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// paint parent's background
		setBackground(Color.WHITE); // set background color for this JPanel
		int[] string_lengths = new int[layer_strings.length];
		int max_length = 0;
		g.setFont(new Font("Monospaced", Font.PLAIN, 12));
		g.setColor(Color.BLACK);
	
		// Get length of each layer and determine the largest one for drawing dimensions
		for (int i = 0; i < layer_strings.length; i++) {

			string_lengths[i] = g.getFontMetrics().stringWidth(layer_strings[i]);
			System.out.println(string_lengths[i]);
			if (string_lengths[i] > max_length) {

				max_length = string_lengths[i];
			}
		}
		
		// Calculate Dimensions for Drawing
		int y = layer_strings.length * 50 + 50;
		int x = max_length + 150;

		System.out.println(x + ", " + y);
		this.setPreferredSize(new Dimension(x, y));
		// Get words in every layer
		String[] words_in_layer;
		// Anchors for words
		int anchor_x;
		int anchor_y;

		// Each term has 2 anchor points, 1 above and 1 below (y) both in the middle of
		// the word (x)
		JSONObject word_anchor_points = new JSONObject();
		JSONObject word_coordinates = new JSONObject();

		// Get anchor point for each word of each layer
		for (int i = 0; i < layer_strings.length; i++) {

			JSONArray word_arr = new JSONArray();
			words_in_layer = layer_strings[i].split("      ");
			// Calculation of layer anchor points
			anchor_x = (x - string_lengths[i]) / 2;
			anchor_y = i * 50 + 50;
			g.drawString(layer_strings[i], anchor_x, anchor_y);

			int word_anchor_x = 0;
			int word_anchor_y = anchor_y - 10;
			for (int j = 0; j < words_in_layer.length; j++) {

				// First word is calculated by distance to edge of drawing to start of first
				// word + half the length of first word
				if (word_anchor_x == 0) {

					word_anchor_x = anchor_x + g.getFontMetrics().stringWidth(words_in_layer[j]) / 2;

					// Every other word gets calulated by last anchor point + half the length of
					// last word + distance between words + half the length of current word
				} else {

					word_anchor_x += g.getFontMetrics().stringWidth(words_in_layer[j - 1]) / 2
							+ g.getFontMetrics().stringWidth("      ")
							+ g.getFontMetrics().stringWidth(words_in_layer[j]) / 2;
				}

				// Save anchor points for later
				int[] word_coordinate = { i, j };
				word_coordinates.put(words_in_layer[j], word_coordinate);
				int[][] single_word_anchors = { { word_anchor_x, word_anchor_y + 15 },
						{ word_anchor_x, word_anchor_y } };
				word_arr.add(single_word_anchors);
			}
			word_anchor_points.put(i, word_arr);

		}

		// Get all relations and draw Lines using the anchor points calculated in last step
		// First word uses lower anchor, second uses upper anchor
		for (int i = 0; i < term_relations.size(); i++) {

			String[] term_relation = (String[]) term_relations.get(i);
			int[] line_coordinates = new int[4];

			for (int j = 0; j < 2; j++) {
				int[] word = (int[]) word_coordinates.get(term_relation[j]);
				JSONArray word_arr = (JSONArray) word_anchor_points.get(word[0]);
				int[][] word_coor = (int[][]) word_arr.get(word[1]);
				line_coordinates[j * 2] = word_coor[j][0];
				line_coordinates[j * 2 + 1] = word_coor[j][1];
			}
			// Draw line between 2 words with relation
			g.drawLine(line_coordinates[0], line_coordinates[1], line_coordinates[2], line_coordinates[3]);
		}
	}

	public void update_params(String[] layers) {

	}

	// Create layers using provided terms and relation paths from excel to later draw them
	public void get_graph_layers(JSONArray paths) {

		term_relations = new JSONArray();
		term_graph = new JSONObject();
		layers = new JSONObject();

		// For every path in paths
		for (int t = 0; t < paths.size(); t++) {
			String path = (String) paths.get(t);
			String[] nodes = path.split("/");

			// Create path in term graph
			rec_term_graph_creation(term_graph, nodes, 0);

			// Split relations to only have 2 words per relation
			for (int i = 0; i < nodes.length - 1; i++) {

				String[] relation = { nodes[i], nodes[i + 1] };
				term_relations.add(relation);
			}

		}

		rec_layer_creation(term_graph, 0);

		int[] counter = { 0, 0 };
		int temp_chars;
		String temp_term;

		layer_strings = new String[layers.size()];

		System.out.println("LOOK HERE");
		System.out.println(layers);
		// Create one String per layer to later draw entire layer with it
		for (int i = 0; i < layers.size(); i++) {

			temp_chars = 0;
			List arr = (ArrayList) layers.get(i);

			// Add words together
			for (int j = 0; j < arr.size(); j++) {
				temp_term = (String) arr.get(j);
				temp_chars += temp_term.length();
				layer_strings[i] += temp_term + "      ";
			}

			// Clean up layer string
			layer_strings[i] = layer_strings[i].replaceAll("null", "");
			layer_strings[i] = layer_strings[i].substring(0, layer_strings[i].length() - 6);
			System.out.println(layer_strings[i]);

			// Keep track of longest String for drawing dimensions needed later
			if (temp_chars > counter[1]) {

				counter[1] = temp_chars;
				counter[0] = i;
			}
		}
	}

	// Recurrent function to build correctly sorted layers
	private void rec_layer_creation(JSONObject curr_graph_node, int curr_layer) {

		Set<String> terms = curr_graph_node.keySet();
		if (!layers.containsKey(curr_layer) && terms.size() != 0) {

			layers.put(curr_layer, new JSONArray());
		}

		for (String term : terms) {

			JSONArray arr = (JSONArray) layers.get(curr_layer);
			arr.add(term);
			rec_layer_creation((JSONObject) curr_graph_node.get(term), curr_layer + 1);
		}
	}

	// Recurrent function to build term graph structure using JSONObjects
	private void rec_term_graph_creation(JSONObject curr_graph_node, String[] terms, int step) {

		if (!curr_graph_node.containsKey(terms[step])) {

			curr_graph_node.put(terms[step], new JSONObject());
		}

		if (step + 1 < terms.length) {

			rec_term_graph_creation((JSONObject) curr_graph_node.get(terms[step]), terms, step + 1);
		}
	}

	public JSONObject get_term_graph() {
		return term_graph;
	}

	public void update_graph(JSONArray paths) {

		get_graph_layers(paths);
		repaint();
	}

}
