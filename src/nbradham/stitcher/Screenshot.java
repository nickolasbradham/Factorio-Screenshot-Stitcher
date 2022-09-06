package nbradham.stitcher;

import java.util.ArrayList;

/**
 * Handles tracking what files are related to which screenshots.
 * 
 * @author Nickolas Bradham
 *
 */
final class Screenshot {

	private final ArrayList<Split> splits = new ArrayList<>();

	/**
	 * Add a split to this screenshot.
	 * 
	 * @param split
	 */
	final void add(Split split) {
		splits.add(split);
	}

	/**
	 * Retrieves a Split from this screenshot.
	 * 
	 * @param ind The index of the desired Split.
	 * @return The requested Split.
	 */
	final Split getSplit(int ind) {
		return splits.get(ind);
	}

	/**
	 * Retrieves how many Splits wide the screenshot is.
	 * 
	 * @return The number of Splits across the x axis.
	 */
	final int lengthX() {
		int res = 0;
		for (Split s : splits)
			res = Math.max(res, s.x());
		return res + 1;
	}

	/**
	 * Retrieves how many Splits high the screenshot is.
	 * 
	 * @return The number of Splits across the y axis.
	 */
	final int lengthY() {
		int res = 0;
		for (Split s : splits)
			res = Math.max(res, s.y());
		return res + 1;
	}

	/**
	 * Retrieves how many Splits this screenshot contains.
	 * 
	 * @return The total number of splits in this screenshot.
	 */
	final int numSplits() {
		return splits.size();
	}
}