# TOSCA-MART
This repository contains the [source code](https://github.com/jacopogiallo/TOSCA-MART/blob/master/README.md#source) of a Java implementation of the TOSCA-MART matchmaking and adaptation approach, which has been presented in 
> _J. Soldani, T. Binz, U. Breitenbücher, F. Leymann, A. Brogi. <br>
> **TOSCA-MART: A Method for Adapting and Reusing Cloud Applications.** <br>
> [To appear in: Journal of Systems and Software]._ 

It also contains a [dataset](https://github.com/jacopogiallo/TOSCA-MART#dataset) repository of TOSCA application specifications that can be used to test TOSCA-MART (or other approaches for matchmaking and reusing TOSCA application specifications). 

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

## <a name="source"></a> About the source code
In the following, we will give further details about the prototype implementation of TOSCA-MART, about the greedy
implementation of another matching technique, and about the dataset exploited to test them. 

#### TOSCA-MART
The **TOSCA-MART** folder contains the prototype implementation of the TOSCA-MART matchmaking and adaptation approach. 

The concrete implementation of TOSCA-MART is given by the ```TOSCAMart``` Java class (defined in
[TOSCAMart.java](https://github.com/jacopogiallo/TOSCA-MART/blob/master/TOSCA-MART/src/mart/TOSCAMart.java)). To operate TOSCA-MART, one simply have to invoke the (static) ```run``` method of ```TOSCAMart``` by providing it with the
following three arguments:
- ```nodeTypeLoc```, that is a string containing the URL where to retrieve the TOSCA node type to be matched (e.g., [DesiredEnvironment.tosca](https://raw.githubusercontent.com/jacopogiallo/TOSCA-MART/master/Dataset/NodeTypes/DesiredEnvironment.tosca)).
- ```servTempLocs```, that is a string containing the URL where to retrieve (a text file containing) all the URLs of the service templates available in the repository (e.g., [service-templates.txt](https://raw.githubusercontent.com/jacopogiallo/TOSCA-MART/master/Dataset/ServiceTemplates/service-templates.txt)). 
- ```howManyImplementations```, that is the threshold representing the maximum number of fragments to be matched and adapted.

As a result, return the methods returns the list of strings containing the names of TOSCA specification that have been generated by adapting the matched fragments.

#### GreedyImplementation
The **GreedyImplementation** folder contains a greedy implementation of the matchmaking and adaptation presented in 
> _A. Brogi, J. Soldani. <br> 
> **Finding available services in TOSCA-compliant clouds**. <br> 
> Science of Computer Programming, Volumes 115–116, Pages 177-198, 1 January–1 February 2016_.

The concrete implementation of such an approach is given by the ```GreedyMatching``` Java class (defined in [GreedyMatching.java](https://github.com/jacopogiallo/TOSCA-MART/blob/master/GreedyMatching/src/greedy/GreedyMatching.java)). To operate it, one simply have to invoke the (static) ```run``` method of ```GreedyMatching``` by providing it with the following three arguments:
- ```nodeTypeLoc```, that is a string containing the URL where to retrieve the TOSCA node type to be matched (e.g., [DesiredEnvironment.tosca](https://raw.githubusercontent.com/jacopogiallo/TOSCA-MART/master/Dataset/NodeTypes/DesiredEnvironment.tosca)).
- ```servTempLocs```, that is a string containing the URL where to retrieve (a text file containing) all the URLs of the service templates available in the repository (e.g., [service-templates.txt](https://raw.githubusercontent.com/jacopogiallo/TOSCA-MART/master/Dataset/ServiceTemplates/service-templates.txt)).
- ```howManyImplementations```, that is the threshold representing the maximum number of fragments to be matched and adapted.

As a result, return the methods returns the list of strings containing the names of TOSCA specification that have been generated by adapting the matched fragments.

## <a name="dataset"></a> About the dataset
The **Dataset** folder contains the TOSCA definitions of 1395 different service templates (along with all the types needed to define them). The URLs of all available service templates are listed in [service-templates.txt](https://raw.githubusercontent.com/jacopogiallo/TOSCA-MART/master/Dataset/ServiceTemplates/service-templates.txt) (one per line).

More precisely, there are 279 service templates providing different deployment solutions for 7 applications (4 web services and 3 web applications - with a database in the back-end).
The application stacks in such service templates are then replicated 2, 3, 4 and 5 times to permit increasing the amount of features provided by a service template's topology. (We employ the following naming convention:
[Moodle_X-2.tosca](https://raw.githubusercontent.com/jacopogiallo/TOSCA-MART/master/Dataset/ServiceTemplates/Moodle_X-2.tosca), [Moodle_X-3.tosca](https://raw.githubusercontent.com/jacopogiallo/TOSCA-MART/master/Dataset/ServiceTemplates/Moodle_X-3.tosca), [Moodle_X-4.tosca](https://raw.githubusercontent.com/jacopogiallo/TOSCA-MART/master/Dataset/ServiceTemplates/Moodle_X-4.tosca), and [Moodle_X-5.tosca](https://raw.githubusercontent.com/jacopogiallo/TOSCA-MART/master/Dataset/ServiceTemplates/Moodle_X-5.tosca), are service templates whose topology is that of [Moodle_X.tosca](https://raw.githubusercontent.com/jacopogiallo/TOSCA-MART/master/Dataset/ServiceTemplates/Moodle_X.tosca) replicated 2, 3, 4, and 5 times, respectively).

_Note: The repository contains plastic models of cloud applications (i.e., the service templates in the repository do not contain any software artifacts that permit running the modeled application). The objective of the repository is indeed to provide a plastic dataset to test matchmaking and adaptation techniques, such as TOSCA-MART or GreedyImplementation._
