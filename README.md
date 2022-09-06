# Factorio-Screenshot-Stitcher
Stitches screenshots together produced by [This](https://mods.factorio.com/mod/FacAutoScreenshot) Factorio mod.

The stitching tool provided by the mod author does not work, so I made my own and added a few improvements over the original (mainly adding a painfully basic GUI, some slight optimization, and using all native Java to reduce bloat and dependencies).

Run the program and select the folder containing all the splits to stitch together. The program will then proceed to try and stitch them together and store them to the `Stitched` directory next to the executable. It is multithreaded, but will almost certainly be bottlenecked by reading and writing to storage. It will spawn as many threads as your computer has cores.

Depending on the number of cores available, and the screenshot resolution selected in-game, The amount of RAM needed will vary. Be sure to run with the `-Xmx<RAM>G` VM argument, specifying how much ram Java is allowed to use (replace `<RAM>` with how much system memory you have).

This program relies on the filenames produced by the Factorio mod to position screenshots. Do not change the names or errors will happen. This also means that this stitcher probably won't work with any other mod.
