package framework.component;

import java.io.File;
import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Duration;

/**
 * TreeView that, given a directory, will show it in a treeview.
 * 
 * @author sraj144
 *
 */
public class TreeViewDirectoryViewer extends TreeView<File> {

	File dir;

	Timeline treeSearchTimeline;

	/**
	 * Default
	 */
	public TreeViewDirectoryViewer() {
		setPrefWidth(300);

		treeSearchTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
			try {
				setupTreeView();
			} catch (Exception e) {
				System.out.println("Failed to update treeview from directory");
				e.printStackTrace();
			}
		}));

		treeSearchTimeline.setCycleCount(Timeline.INDEFINITE);
		treeSearchTimeline.stop();

	}

	public TreeViewDirectoryViewer(File dir) {
		this();
		this.dir = dir;
	}

	public void runBackground() {
		treeSearchTimeline.play();
	}

	public void stopBackground() {
		treeSearchTimeline.stop();
	}

	/**
	 * Method for when it is assumed that there will be an existing dir.
	 * 
	 * @throws IOException
	 */
	public void setupTreeView() throws IOException {

		if (dir == null) {
			throw new NullPointerException("Directory not set.");
		}

		setupTreeView(dir);
	}

	public void setupTreeView(File dir) throws IOException {
		// Make sure it is dir
		if (dir.isFile()) {
			throw new IOException("Not a dir");
		}

		// Used for finding out how to get expandable nodes.
		// http://stackoverflow.com/questions/26690247/how-to-make-directories-expandable-in-javafx-treeview
		findFiles(dir, null);
	}

	/**
	 * Refer to
	 * http://stackoverflow.com/questions/26690247/how-to-make-directories-
	 * expandable-in-javafx-treeview.
	 * 
	 * @param dir
	 * @param parent
	 */
	private void findFiles(File dir, TreeItem<File> parent) {
		TreeItem<File> root = new TreeItem<>(dir);
		root.setExpanded(true);
		try {
			File[] files = dir.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					System.out.println("directory:" + file.getCanonicalPath());
					findFiles(file, root);
				} else {
					System.out.println("     file:" + file.getCanonicalPath());
					root.getChildren().add(new TreeItem<>(file));
				}

			}
			if (parent == null) {
				this.setRoot(root);
			} else {
				parent.getChildren().add(root);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
