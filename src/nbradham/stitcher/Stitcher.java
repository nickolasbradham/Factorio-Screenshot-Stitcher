package nbradham.stitcher;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Handles core execution.
 * 
 * @author Nickolas Bradham
 *
 */
final class Stitcher extends WindowAdapter {

	private static final File OUT_DIR = new File("Stitched");

	private final JFrame frame = new JFrame("Stitcher");
	private final JProgressBar bar = new JProgressBar();
	private final HashMap<String, Screenshot> shots = new HashMap<>();
	private final Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];

	private Iterator<Screenshot> shotIterator;
	private boolean run = true;

	/**
	 * Displays GUI, handles loading, and handles stitching threads.
	 */
	private void start() {
		try {
			SwingUtilities.invokeAndWait(() -> {
				frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				frame.setResizable(false);
				frame.addWindowListener(this);
				frame.setLayout(new GridBagLayout());

				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.weightx = .5;

				bar.setStringPainted(true);
				bar.setString("Ready.");
				frame.add(new JLabel("Overall:"), gbc);

				gbc.gridx = 1;
				frame.add(new JLabel(new ImageIcon(Stitcher.class.getResource("/anim.gif"))), gbc);

				gbc.gridx = 2;
				frame.add(bar, gbc);

				gbc.gridx = 0;
				gbc.gridy = 1;
				gbc.gridwidth = 3;
				JPanel scrollPane = new JPanel();
				scrollPane.setLayout(new BoxLayout(scrollPane, BoxLayout.Y_AXIS));

				JScrollPane scroll = new JScrollPane(scrollPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

				for (byte i = 0; i < threads.length; i++) {
					Worker w = new Worker(i);
					scrollPane.add(w.getGUI());
					threads[i] = new Thread(w);
				}
				frame.add(scroll, gbc);

				frame.pack();
				frame.setVisible(true);
			});
		} catch (InvocationTargetException | InterruptedException e1) {
			JOptionPane.showMessageDialog(frame, "Something went wrong (" + e1 + ").");
		}

		JFileChooser jfc = new JFileChooser(
				Paths.get(System.getenv("appdata"), "Factorio", "script-output", "screenshots").toFile());
		jfc.setDialogTitle("Select dir of screenshots to stitch");
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if (jfc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
			cleanup();
			return;
		}

		try {
			for (File split : jfc.getSelectedFile().listFiles()) {
				String[] name = split.getName().split("_");
				if (!shots.containsKey(name[0]))
					shots.put(name[0], new Screenshot());
				shots.get(name[0]).add(new Split(split, Byte.parseByte(name[1].substring(1)),
						Byte.parseByte(name[2].substring(1, name[2].lastIndexOf('.')))));
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Something went wrong when loading screenshot files(" + e
					+ "). Make sure you picked the right directory.");
			cleanup();
			return;
		}

		bar.setMaximum(shots.size());
		shotIterator = shots.values().iterator();
		OUT_DIR.mkdir();

		for (Thread t : threads)
			t.start();

		for (Thread t : threads)
			try {
				t.join();
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(frame, "An exception has occurred: " + e);
			}

		if (run)
			JOptionPane.showMessageDialog(frame, "Done.");
		cleanup();
	}

	/**
	 * Flags the threads to stop and disposes frame.
	 */
	private void cleanup() {
		run = false;
		SwingUtilities.invokeLater(() -> frame.dispose());
	}

	/**
	 * Retrieves the next {@link Screenshot} to stitch.
	 * 
	 * @return The next Screenshot or null if there are no more to stitch.
	 */
	private synchronized Screenshot next() {
		return shotIterator.hasNext() ? shotIterator.next() : null;
	}

	/**
	 * Increments the progress bar.
	 */
	private synchronized void incProg() {
		SwingUtilities.invokeLater(() -> {
			int val = bar.getValue() + 1;
			bar.setValue(val);
			bar.setString(val + "/" + bar.getMaximum());
		});
	}

	@Override
	public final void windowClosing(WindowEvent e) {
		if (JOptionPane.showConfirmDialog(frame, "Are you sure you want to cancel stitching?", "Confirm exit",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			cleanup();
	}

	/**
	 * Constructs and starts a new {@link Stitcher} instance.
	 * 
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
		new Stitcher().start();
	}

	/**
	 * Handles stitching screenshots.
	 * 
	 * @author Nickolas Bradham
	 *
	 */
	private class Worker implements Runnable {

		private final JProgressBar bar = new JProgressBar();
		private final byte id;

		/**
		 * Constructs a new Worker and sets ID.
		 * 
		 * @param setID
		 */
		private Worker(byte setID) {
			id = setID;
			bar.setStringPainted(true);
			bar.setString("Ready.");
		}

		/**
		 * Retrieves the GUI component.
		 * 
		 * @return A new JPanel instance holding the GUI elements of this Worker.
		 */
		private JPanel getGUI() {
			JPanel panel = new JPanel();
			panel.add(new JLabel("Thread " + id + ": "));
			panel.add(bar);
			return panel;
		}

		/**
		 * Updates the progress bar on the AWT thread.
		 * 
		 * @param val The value of the bar.
		 * @param max The maximum of the bar.
		 */
		private void updateProg(int val, int max) {
			SwingUtilities.invokeLater(() -> {
				bar.setValue(val);
				bar.setMaximum(max);
				bar.setString(val + " /" + max);
			});
		}

		@Override
		public void run() {
			Screenshot shot;
			while ((shot = next()) != null) {
				int max = shot.numSplits();
				updateProg(0, max);
				try {
					Split split = shot.getSplit(0);
					BufferedImage s0 = ImageIO.read(split.file());
					int width = s0.getWidth(), height = s0.getHeight();

					BufferedImage bi = new BufferedImage(width * shot.lengthX(), height * shot.lengthY(),
							BufferedImage.TYPE_3BYTE_BGR);
					Graphics g = bi.createGraphics();
					g.drawImage(s0, width * split.x(), height * split.y(), null);

					for (short ind = 1; ind < max && run; ind++) {
						updateProg(ind, max);
						split = shot.getSplit(ind);
						g.drawImage(ImageIO.read(split.file()), width * split.x(), height * split.y(), null);
					}

					String fName = split.file().getName();
					ImageIO.write(bi, "jpg", new File(OUT_DIR, fName.substring(0, fName.indexOf('_')) + ".jpg"));
					updateProg(max, max);
					incProg();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}