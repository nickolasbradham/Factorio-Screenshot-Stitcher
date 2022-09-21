# Factorio-Screenshot-Stitcher
Stitches screenshot splits from [this](https://mods.factorio.com/mod/FacAutoScreenshot) Factorio mod.

Tested on Windows 10 Home x64 with Java 18.0.2.1 2022-08-18.

When you run this executable jar, it will first display a folder selection window that is asking you to select the folder containing the split images. The default location of these images on Windows is `%AppData%\Factorio\script-output\screenshots\<WORLD SEED>\auto_split_nauvis`. After selecting this folder, the program will start as many threads as you have CPU cores (as reported by `Runtime.getRuntime().availableProcessors()`). These threads will stitch the images from the specified folder and store the full images inside a new folder next to the executable.

If you see some threads change their status to "Out of memory," **don't panic!** As long as one thread remains, all images will be stitched. To avoid this issue, run the program from the command line using `java -Xmx<RAM> -jar Stitcher.jar`, but replace `<RAM>` with how much system memory your computer has.
