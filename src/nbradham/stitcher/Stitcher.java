package nbradham.stitcher;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * Handles core execution.
 * 
 * @author Nickolas Bradham
 *
 */
final class Stitcher extends WindowAdapter {

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
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setResizable(false);
		frame.addWindowListener(this);
		frame.setLayout(new FlowLayout());
		bar.setStringPainted(true);
		frame.add(new JLabel("Status:"));
		frame.add(new JLabel(new ImageIcon(Stitcher.class.getResource("/anim.gif"))));
		frame.add(bar);
		frame.pack();
		frame.setVisible(true);

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

		bar.setMaximum(shots.size() + 1);
		shotIterator = shots.values().iterator();

		File output = new File("Stitched");
		output.mkdir();

		for (byte i = 0; i < threads.length; i++) {
			threads[i] = new Thread(() -> {
				Screenshot shot;
				while ((shot = next()) != null) {
					try {
						Split split = shot.getSplit(0);
						BufferedImage s0 = ImageIO.read(split.file());
						int width = s0.getWidth(), height = s0.getHeight();

						BufferedImage bi = new BufferedImage(width * shot.lengthX(), height * shot.lengthY(),
								BufferedImage.TYPE_3BYTE_BGR);
						Graphics g = bi.createGraphics();
						g.drawImage(s0, width * split.x(), height * split.y(), null);

						for (byte ind = 1; ind < shot.numSplits() && run; ind++) {
							split = shot.getSplit(ind);
							g.drawImage(ImageIO.read(split.file()), width * split.x(), height * split.y(), null);
						}

						String fName = split.file().getName();
						ImageIO.write(bi, "jpg", new File(output, fName.substring(0, fName.indexOf('_')) + ".jpg"));
						incProg();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			threads[i].start();
		}

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
		SwingUtilities.invokeLater(() -> bar.setValue(bar.getValue() + 1));
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
}