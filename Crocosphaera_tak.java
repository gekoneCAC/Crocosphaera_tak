/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/
/*/ gekone 03.2021 ALGATECH /*/
/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/
//{ variables
// Claiming variables with "var" in front allows them to be carried between macros.
var dirIMG = "empty";
var splitDir0 = "empty";
var splitDirORG = "empty";
var splitDirPNG = "empty";
var splitDirMAS = "empty";

var mynameX = "empty";
var mynameXcut = "empty";
var varFrames = 0;
var varSlices = 0;
var varWidth = 0;
var varHeight = 0;
var varChannels = 0;

//}
/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/
macro "Reload Domain macro [i]"{
run("Install...", "install=[M:\\...(your location path here)...\\Crocosphaera_tak.txt]"); // version for Windows
//run("Install...", "install=[/Users/...(your location path here).../Crocosphaera_tak.txt]"); // version for Mac
}
/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/
macro "closeAll [0]"{
// I like having this all-around macro to get rid of unnecessary windows.
totalOpenImages=nImages;
for(k=0;k<totalOpenImages;k++){
	titleCLOSE = getTitle();
		if (titleCLOSE != "all"){
		close();
		}
	}
if (isOpen("ROI Manager")) {
	selectWindow("ROI Manager");
	run("Close");
	}
if (isOpen("Results")) {
	selectWindow("Results");
	run("Close");
	}
if (isOpen("Log")) {
	selectWindow("Log");
	run("Close");
	}
}
/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/
macro "set basic variables [s]"{
// Here I get basic information asnd some of them I assign to Global variables (in the beginning of file).
// I create some folders as well to store processed images and results files there.
dirIMG = getDirectory("image");
mynameX = getTitle();
mynameXcut = substring(mynameX, 0, lengthOf(mynameX)-4);
getDimensions(width, height, channels, slices, frames);
varFrames = frames;
varSlices = slices;
varWidth = width;
varHeight = height;
varChannels = channels;
splitDir0 = dirIMG + "/" + mynameXcut;
splitDirORG = dirIMG + "/" + mynameXcut + "/ORG";
splitDirPNG = dirIMG + "/" + mynameXcut + "/PNG";
splitDirMAS = dirIMG + "/" + mynameXcut + "/MAS";
File.makeDirectory(splitDir0);
File.makeDirectory(splitDirORG);
File.makeDirectory(splitDirPNG);
File.makeDirectory(splitDirMAS);

// Here I pre-prepare image to select individual cells. Will stop in the middle to allow user "add" missing cell part if required.
selectWindow(mynameX);
setLocation(20, 20);
run("Duplicate...", "duplicate");
rename("idealna_kopia");
run("Make Composite");
Stack.setPosition(6, 1, 1);
run("Delete Slice", "delete=channel");
Stack.setPosition(1, 1, 1);
run("RGB Color");
run("8-bit");
setThreshold(100, 255);
setOption("BlackBackground", false);
run("Convert to Mask");
rename("ObrazekMaska0");
run("Fill Holes");
run("Yellow");
setForegroundColor(255, 255, 255);
setLocation(560, 20);

run("Line Width...", "line=2");
setTool("freeline");
}
/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/
// Important command "D". Will be used after the "S" if needed to fill gaps in cell shapes and after "A" if needed to separate merged cells.
macro "draw [d]"{
roiManager("Add");
roiManager("Select", 0);
run("Draw", "slice");
selectWindow("ObrazekMaska0");
setForegroundColor(255, 255, 255);
roiManager("Select", 0);
run("Draw", "slice");
run("Select None");
setForegroundColor(255, 0, 0);
roiManager("Select", 0);
roiManager("Delete");
// This is very important thing (used also in "close all" macro. Closes window only if its opened (avoids error "no window with name xxx opened").
if (isOpen("drawboard")) {
	selectWindow("drawboard");
	run("Select None");
	}
}
/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/
// This part follows the separation of the cells. After this it stops and allow user to use "D" macro to further separate cells.
// Draw on the new, disposable "drawboard" window.
macro "setA [a]"{
run("Convert to Mask");
//run("Dilate");
run("Fill Holes");
//run("Erode");
setForegroundColor(255, 255, 255);

selectWindow(mynameX);
run("Duplicate...", "title=drawboard channels=1");
run("RGB Color");
setForegroundColor(255, 0, 0);
run("Line Width...", "line=2");
setTool("freeline");
}
/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/
macro "setC [c]"{
// This part finalazes cell separation and iterates thru ROIs to analyse them and save them.
selectWindow("ObrazekMaska0");
run("Set Measurements...", "area centroid perimeter bounding fit shape feret's redirect=None decimal=2");
run("Analyze Particles...", "size=2-Infinity  circularity=0.20-1.00 display exclude clear add in_situ");
selectWindow("ObrazekMaska0");
run("Select None");
run("Flatten");
// Saves overlay mask. To know, if needed, which cell is which.
saveAs("TIFF", splitDir0 + "/" + mynameXcut + "_mask.tif");
close();
// Saving basic Results.
selectWindow("Results");
saveAs("Results", splitDir0 + "/" + mynameXcut + "_results.csv");
run("Close");

// Counting ROIs and preparing arrays to populate with cell data later.
nROIs = roiManager("count");
ListaFolderow = newArray(nROIs);
ListaNazw = newArray(nROIs);
arrayCH1x = newArray(nROIs);
arrayCH2x = newArray(nROIs);
arrayCH3x = newArray(nROIs);
arrayCH4x = newArray(nROIs);
arrayCH5x = newArray(nROIs);
arrayCH6x = newArray(nROIs);

arrayCH1i = newArray(nROIs);
arrayCH2i = newArray(nROIs);
arrayCH3i = newArray(nROIs);
arrayCH4i = newArray(nROIs);
arrayCH5i = newArray(nROIs);
arrayCH6i = newArray(nROIs);

arrayCH1o = newArray(nROIs);
arrayCH2o = newArray(nROIs);
arrayCH3o = newArray(nROIs);
arrayCH4o = newArray(nROIs);
arrayCH5o = newArray(nROIs);
arrayCH6o = newArray(nROIs);

// Iterate thru each ROI individually.
setForegroundColor(255, 255, 255);
for (k=0; k<=nROIs-1; k++){
	selectWindow("ObrazekMaska0");
	roiManager("Select", k);
	ROInazwaXcut = k + 1;
	run("Enlarge...", "enlarge=2 pixel");
	run("Duplicate...", "title=CopyMask duplicate");
	selectWindow("CopyMask");
	run("Canvas Size...", "width=80 height=80 position=Center");
	
	run("Convert to Mask");
	makeRectangle(0, 0, 80, 80);
	setForegroundColor(255, 255, 255);
	run("Draw", "slice");
	run("Select None");
	doWand(40, 40);
	getRawStatistics(nPixels, mean, min, max);
	if (nPixels == 6400){
		makeRectangle(22, 26, 31, 28);
		}
	setForegroundColor(0, 0, 0);
	run("Fill", "slice");
	run("Make Inverse");
	setForegroundColor(255, 255, 255);
	run("Fill", "slice");
	run("Select None");
	run("Convert to Mask");
	
	// Get cell out of image.
	selectWindow(mynameX);
	roiManager("Select", k);
	run("Enlarge...", "enlarge=2 pixel");
	run("Duplicate...", "title=CopyCell duplicate");
	selectWindow("CopyCell");
	run("Canvas Size...", "width=80 height=80 position=Center");
	
	// Get mask out of image.
	selectWindow("CopyMask");
	run("Create Selection");
	selectWindow("CopyCell");
	run("Restore Selection");
	run("Clear Outside", "stack");
	
	// Save cell.
	selectWindow("CopyCell");
	saveAs("TIFF", splitDirORG + "/" + mynameXcut + "_" + ROInazwaXcut + ".tif");
	selectWindow(mynameXcut + "_" + ROInazwaXcut + ".tif");
	
	// Populate location arrays.
	ListaFolderow[k] = getDirectory("image");
	ListaNazw[k] = getTitle();
	close();
	
	// Save mask.
	selectWindow("CopyMask");
	saveAs("TIFF", splitDirMAS + "/" + mynameXcut + "_" + ROInazwaXcut + "_mask.tif");
	selectWindow(mynameXcut + "_" + ROInazwaXcut + "_mask.tif");
	close();
	}
selectWindow("idealna_kopia");
close();

run("closeAll [0]");

// Iterate thru all cells.
nImagesToOpen = nROIs;
for(k=0;k<nImagesToOpen;k++){
	open(ListaFolderow[k] + ListaNazw[k]);
	mynameY = getTitle();
	roiManager("Add");
	run("Enlarge...", "enlarge=-9 pixel");
	roiManager("Add");
	nROIs = roiManager("count");
	
	// Important ERROR handling part!
	roiManager("Select", 0);
	getRawStatistics(nPixels, mean, min, max);
	varPixTestA = nPixels;
	roiManager("Select", 1);
	getRawStatistics(nPixels, mean, min, max);
	varPixTestB = nPixels;
	if(varPixTestA == varPixTestB){
		selectWindow(mynameY);
		makeRectangle(28, 26, 25, 26);
		roiManager("Add");
		roiManager("Select", 1);
		roiManager("Delete");
		}
	
	// Create cell outer ring.
	selectWindow(mynameY);
	roiManager("Select", newArray(0,1));
	roiManager("XOR");
	roiManager("Add");

	roiManager("Select", 0);
	roiManager("Rename", "roiX");
	roiManager("Select", 1);
	roiManager("Rename", "roiI");
	roiManager("Select", 2);
	roiManager("Rename", "roiO");
	
	// Iterate thru all 6 channels.
	// This is very "uprofessional" but very clean to understand. Instead of making one big loop. I split it in 6 loops.
	selectWindow(mynameY);
	for(c=1;c<7;c++){
		selectWindow(mynameY);
		Stack.setPosition(c, 1, 1);
		if(c == 1){
			roiManager("Select", 0);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH1x[k] = mean;
			roiManager("Select", 1);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH1i[k] = mean;
			roiManager("Select", 2);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH1o[k] = mean;
		}
		else if(c == 2){
			roiManager("Select", 0);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH2x[k] = mean;
			roiManager("Select", 1);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH2i[k] = mean;
			roiManager("Select", 2);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH2o[k] = mean;
		}
		else if(c == 3){
			roiManager("Select", 0);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH3x[k] = mean;
			roiManager("Select", 1);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH3i[k] = mean;
			roiManager("Select", 2);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH3o[k] = mean;
		}
		else if(c == 4){
			roiManager("Select", 0);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH4x[k] = mean;
			roiManager("Select", 1);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH4i[k] = mean;
			roiManager("Select", 2);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH4o[k] = mean;
		}
		else if(c == 5){
			roiManager("Select", 0);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH5x[k] = mean;
			roiManager("Select", 1);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH5i[k] = mean;
			roiManager("Select", 2);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH5o[k] = mean;
		}
		else if(c == 6){
			roiManager("Select", 0);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH6x[k] = mean;
			roiManager("Select", 1);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH6i[k] = mean;
			roiManager("Select", 2);
			getRawStatistics(nPixels, mean, min, max);
			arrayCH6o[k] = mean;
		}
	}
	if (isOpen("ROI Manager")) {
		selectWindow("ROI Manager");
		run("Close");
		}
	close();
	}
// Create final Results.
for(k=0;k<nImagesToOpen;k++){
	setResult("name", k, ListaNazw[k]);
	setResult("ch1x", k, arrayCH1x[k]);
	setResult("ch1i", k, arrayCH1i[k]);
	setResult("ch1o", k, arrayCH1o[k]);
	
	setResult("ch2x", k, arrayCH2x[k]);
	setResult("ch2i", k, arrayCH2i[k]);
	setResult("ch2o", k, arrayCH2o[k]);
	
	setResult("ch3x", k, arrayCH3x[k]);
	setResult("ch3i", k, arrayCH3i[k]);
	setResult("ch3o", k, arrayCH3o[k]);
	
	setResult("ch4x", k, arrayCH4x[k]);
	setResult("ch4i", k, arrayCH4i[k]);
	setResult("ch4o", k, arrayCH4o[k]);

	setResult("ch5x", k, arrayCH5x[k]);
	setResult("ch5i", k, arrayCH5i[k]);
	setResult("ch5o", k, arrayCH5o[k]);

	setResult("ch6x", k, arrayCH6x[k]);
	setResult("ch6i", k, arrayCH6i[k]);
	setResult("ch6o", k, arrayCH6o[k]);
	}
// Here I commented some parts as for reasons unknows to me it sometimes gives error and sometimes not. Adjust to your needs.
//selectWindow("Results");
saveAs("Results", splitDir0 + "/" + mynameXcut + "_resultsIO.csv");
//run("Close");
run("closeAll [0]");
}

/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/
macro "test [t]"{
// I always keep one "test" macro where I test small function parts to see if it will work in the final run.
run("Convert to Mask");
run("Convert to Mask");
doWand(40, 40);
run("Make Inverse");
setForegroundColor(255, 255, 255);
run("Fill", "slice");

run("Select None");
run("Convert to Mask");


}
/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/

