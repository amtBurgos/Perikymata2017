# Perikymata 
######Análisis paleontológico de piezas dentales
######Paleontological analisys of dental pieces

This applications tries to help to detect Perikymata in a tooth image taken by a electronic microscope.

This application Is divided in three stages:
* Stage1:
  * Panorama union of tooth images (or giving the full image from the start).
* Stage2:
  * Prewitt and Gauss filtering for helping the visualization of perikymata.
* Stage3:
  * Marking the regions of interest of the image (start and end of the perikymata zone to calculate the deciles, and start-end of the "measure" to get the real distance between perikymata on the image).
  * Drawing a line and try to auto-mark real perikymata over that line, corrections can be made on errors.
  * Export of the image data on a CSV with the following format

```
"Project name"
Measure unit,"measure unit"
Decile size,"measure size, calculated in the measure unit"

Decile,Coordinates,Distance to previous
"number of decile","X Y coordinates","Distance in the measure unit to previous"

Perikymata per decile
1,2,3,4,5,6,7,8,9,10
"perikymata in decile 1","perikymata in decile 2",...
```

