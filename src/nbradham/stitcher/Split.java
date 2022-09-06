package nbradham.stitcher;

import java.io.File;

/**
 * Holds information of a individual split.
 * 
 * @author Nickolas Bradham
 *
 */
record Split(File file, byte x, byte y) {
}