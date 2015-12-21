# TOSCA-MART
This repository contains the Java code implementing the prototype implementation of the TOSCA-MART 
matchmaking and adaptation approach. The approach has been presented in 
<br> 
_J. Soldani, T. Binz, U. Breitenbücher, F. Leymann, A. Brogi. 
**TOSCA-MART: A Method for Adapting and Reusing Cloud Applications.** 
[To appear in: Journal of Systems and Software]._ 

If you wish to reuse the sources or the dataset of TOSCA specifications in this repository, please properly 
cite the above mentioned paper. Below you can find the BibTex reference:
```
@article{Soldani2016,
  author = {Soldani, Jacopo and Binz, Tobias and Breitenbücher, Uwe and Leymann, Frank and Brogi, Antonio},
  title = {TOSCA-MART: A Method for Adapting and Reusing Cloud Applications},
  journal = {Journal of Systems and Software},
  year = {2016},
  note = {[In press]}
}
```

## About the code
In the following, we will give further details about the prototype implementation of TOSCA-MART, about the greedy
implementation of another matching technique, and about the dataset exploited to test them. 

#### TOSCA-MART
The _TOSCA-MART/src_ folder contains the prototype implementation of the TOSCA-MART matchmaking and adaptation approach. 
More precisely, the concrete implementation of TOSCA-MART is given by ```TOSCAMart``` Java class (defined in
_TOSCA-MART/src/mart/TOSCAMart.java_).

To operate TOSCA-MART, one simply have to invoke the (static) ```run``` method of ```TOSCAMart``` by providing it with the
following three arguments:
- ```nodeTypeLoc```, that is a string containing the URL where to retrieve the TOSCA node type to be matched.
- ```servTempLocs```, that is a string containing the URL where to retrieve (a text file containing) all the URLs of the service templates available in the repository. 
- ```howManyImplementations```, that is the threshold representing the maximum number of fragments to be matched and adapted.
As a result, return the methods returns the list of strings containing the names of TOSCA specification that have been generated by adapting the matched fragments.

#### GreedyImplementation
TBD

#### Dataset
TBD
