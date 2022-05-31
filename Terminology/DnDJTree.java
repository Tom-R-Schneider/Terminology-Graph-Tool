package Terminology;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.util.Set;

/*  w  w w.  j a va 2s.  com*/
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DnDJTree extends JPanel {
  JTree tree;
  DefaultTreeModel treeModel;

  public DnDJTree(JSONObject term_graph, String domain, JSONArray term_no_path) {
    setLayout(new GridLayout(1, 3));
    tree = new JTree(get_unused_TreeModel(term_no_path, domain));
    tree.setDragEnabled(true);
    tree.setPreferredSize(new Dimension(200, 400));
    JScrollPane scroll = new JScrollPane();
    scroll.setViewportView(tree);

    treeModel = get_used_TreeModel(term_graph, domain);
    JTree secondTree = new JTree(treeModel);
    secondTree.setPreferredSize(new Dimension(200, 400));
    secondTree.setTransferHandler(new TransferHandler() {
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
        tree.scrollRectToVisible(tree.getPathBounds(path
            .pathByAddingChild(newNode)));
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
    secondScroll.setViewportView(secondTree);

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
}