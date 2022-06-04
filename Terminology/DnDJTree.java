package Terminology;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.util.Enumeration;
import java.util.Set;

/*  w  w w.  j a va 2s.  com*/
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DnDJTree extends JPanel {
  JTree tree;
  JTree second_tree;
  DefaultTreeModel treeModel;
  

  public DnDJTree(JSONObject term_graph, String domain, JSONArray term_no_path, Terminology_interface frame) {
    setLayout(new GridLayout(1, 3));
    tree = new JTree(get_unused_TreeModel(term_no_path, domain));
    tree.setDragEnabled(true);
    tree.setPreferredSize(new Dimension(200, 400));
    JScrollPane scroll = new JScrollPane();
    scroll.setViewportView(tree);

    treeModel = get_used_TreeModel(term_graph, domain);
    JTree second_tree = new JTree(treeModel);
    second_tree.setPreferredSize(new Dimension(200, 400));
    second_tree.setTransferHandler(new TransferHandler() {
      @Override
      public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
          return false;
        }
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath path = dl.getPath();
        int childIndex = dl.getChildIndex();

        String data;
        try {
          data = (String) support.getTransferable().getTransferData(
              DataFlavor.stringFlavor);
        } catch (Exception e) {
          return false;
        }
        if (childIndex == -1) {
          childIndex = tree.getModel().getChildCount(
              path.getLastPathComponent());
        }

        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(data);
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) path
            .getLastPathComponent();
        treeModel.insertNodeInto(newNode, parentNode, childIndex);
        tree.makeVisible(path.pathByAddingChild(newNode));
        DefaultTreeModel tree_model = (DefaultTreeModel) tree.getModel();
        MutableTreeNode root = (MutableTreeNode) tree_model.getRoot();
        JSONObject new_path = new JSONObject();
        new_path.put(0, domain);
        
        rec_find_node_to_delete(root, data);
       
        tree_model.reload(root);
        DefaultTreeModel second_tree_model = (DefaultTreeModel) second_tree.getModel();
        MutableTreeNode second_root = (MutableTreeNode) second_tree_model.getRoot();
        rec_find_new_node_path(second_root, data, new_path, 1);
        
        String new_path_string = domain;
        
        for (int i = 1; i < new_path.size(); i++) {
        	
        	String temp_term = (String) new_path.get(i);
        	new_path_string += "/" + temp_term;
        }
        System.out.println(new_path_string);
        frame.update_term_graph(data, new_path_string);
        tree.scrollRectToVisible(tree.getPathBounds(path.pathByAddingChild(newNode)));
        
        return true;
      }

      public boolean canImport(TransferSupport support) {
        if (!support.isDrop()) {
          return false;
        }
        support.setShowDropLocation(true);
        if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          System.out.println("only string is supported");
          return false;
        }
        JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
        TreePath path = dl.getPath();
        if (path == null) {
          return false;
        }
        return true;
      }
    });
    JScrollPane secondScroll = new JScrollPane();
    secondScroll.setViewportView(second_tree);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(scroll, BorderLayout.CENTER);
    JPanel btmPanel = new JPanel(new BorderLayout());
    btmPanel.add(secondScroll, BorderLayout.CENTER);

    add(topPanel);
    add(btmPanel);
  }
  private DefaultTreeModel get_unused_TreeModel(JSONArray term_no_path, String domain) {
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

    DefaultTreeModel model = new DefaultTreeModel(top);
    return model;
  }
  private DefaultTreeModel get_used_TreeModel(JSONObject term_graph, String domain) {
	  DefaultMutableTreeNode top = new DefaultMutableTreeNode("Used Terms in " + domain);
		
		
		Set<String> keys = term_graph.keySet();
		for (String key: keys) {
			
			rec_create_used_term_nodes((JSONObject) term_graph.get(key), top);
		}



    DefaultTreeModel model = new DefaultTreeModel(top);
    return model;
  }
  
  private void rec_create_used_term_nodes(JSONObject curr_sub_terms, DefaultMutableTreeNode curr_node) {
		
		Set<String> keys = curr_sub_terms.keySet();
		for (String key: keys) {
			DefaultMutableTreeNode new_node = new DefaultMutableTreeNode(key);
			curr_node.add(new_node);
			rec_create_used_term_nodes((JSONObject) curr_sub_terms.get(key), new_node);
			
		}
	}
  private boolean rec_find_node_to_delete(MutableTreeNode curr_node, String to_be_deleted) {
	  int child_count = curr_node.getChildCount();
	  boolean found = false;
	  for (int i = 0; i < child_count; i++) {
		  if (found)  {
			  return true;
		  }
		  MutableTreeNode curr_child = (MutableTreeNode) curr_node.getChildAt(i);
		  String curr_child_string = curr_child.toString();
		  System.out.println(curr_child_string);
		  System.out.println(to_be_deleted);
		  if (curr_child_string.equals(to_be_deleted)) {
			  
			  found = true;
			  curr_child.removeFromParent();
			  return true;		
			  
		  } else {
			  
			  found = rec_find_node_to_delete(curr_child, to_be_deleted);
		  }
		  
	  }
	return false;
  }
  
  private boolean rec_find_new_node_path(MutableTreeNode curr_node, String to_be_found, JSONObject curr_path, int curr_depth) {
	  int child_count = curr_node.getChildCount();
	  boolean found = false;	  
	  
	  for (int i = 0; i < child_count; i++) {
		  
		  MutableTreeNode curr_child = (MutableTreeNode) curr_node.getChildAt(i);
		  String curr_child_string = curr_child.toString();
		  
		  
		  System.out.println(curr_child_string);
		  System.out.println(to_be_found);
		  if (curr_child_string.equals(to_be_found)) {
			  
			  found = true;	
			  
		  } else {
			  
			  found = rec_find_new_node_path(curr_child, to_be_found, curr_path, curr_depth + 1);	 
		  } 
		  
		  if (found)  {
			  curr_path.put(curr_depth, curr_child_string);
			  return true;
		  } 
	  }
	  return false;
  }
  
  public void update_unused_terms(String term) {
	  
	  DefaultTreeModel tree_model = (DefaultTreeModel) tree.getModel();
	  DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree_model.getRoot();
      boolean node_exists = false;
      char term_char = term.charAt(0);
      for (int i = 0; i < root.getChildCount(); i++) {
    	  DefaultMutableTreeNode curr_node = (DefaultMutableTreeNode) root.getChildAt(i);
    	  String temp_char = curr_node.toString();
    	  
    	  if (temp_char.equals(String.valueOf(term_char))) {
    		  node_exists = true;
    		  curr_node.add(new DefaultMutableTreeNode(term));;
    		  
    		  
    	  }
    	  
      }
	  
      if (!node_exists) {
    	  DefaultMutableTreeNode new_char = new DefaultMutableTreeNode(Character.toUpperCase(term_char));
    	  root.add(new_char);
    	  new_char.add(new DefaultMutableTreeNode(term));
      }
      tree_model.reload();
  }
}












