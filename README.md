# Perikymata 2.0
###### Análisis paleontológico de piezas dentales v2.0
###### Paleontological analisys of dental pieces v2.0

This application continues the work started at <a href="https://github.com/Serux/perikymata">the previous version of this project</a>.

Teeth remains are one of the most important elements for taking data about the age, the diet and physical problem of the hominid.
Teeth have lines along the enamel, they area called perikymata. 

Detect perikymata is very useful for get information about the hominid. 

This second version of the application improves the image filtering process for detecting them.

It has three main stages:
- Stitching stage: in this stage, the user selects some tooth fragments images taking by an electronic microscope. Then, the application stitching them together for getting a complete tooth image.
- Rotation and Crop stage: the user can prepare the imagen for the next stage rotating it and cropping the cuspel of the tooth. Then, the user can introduce the measure units and value for the perikymata calculation.
- Filter and export data stage: this stage allows the user to apply an image filtering process to detect perikymata and finally mark them in the image. All data can be exported to CSV file.
