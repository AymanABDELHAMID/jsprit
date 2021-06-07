# Green Vehicle Routing Problem (G-VRP) instances

This folder contains the sets of instances used in the paper:

 - Erdogan, S., Miller-Hooks, E.. 2012. A green vehicle routing problem. Transportation Research Part E: Logistics and Transportation Review 48(1) 100-114.

The files are more or less self-explanatory but feel free to ask me if you want further explanations. In **small examples** are the files without the prefix "Large". The **large example files** are the files with the prefix "Large".

### In small examples:

 - **F** corresponds to Station;
 - **C** to customer; and
 - **D** to depot.

### In large examples

  - **BD** corresponds to Station;
  - **S** also means station (but generated);
  - **MD**, **VA**, and **DC** corresponds to customer locations; and
  - **R** also corresponds to customer locations but randomly generated ones.

 ### In all of them:

In both sets of instances, unless otherwise stated, a fuel tank capacity of **60** gallons and fuel consumption rate of **0.2** gallons per mile were set. The average vehicle speed is assumed to be **40** miles per hour (mph) and the total tour duration limitation was assumed to be **10 h and 45 min**. Service times were assumed to be **30 min** at customer locations and **15 min** at AFS locations.

Under type column, 

 - **c** is customer node;
 - **f** is station node; and 
 - **d** is depot.

 Also,  note that the input files are in **latitude** and **longitude** format. The **Haversine** formula was used to calculate the distances. The Java formula/code piece is given below for your convenience.
```
double lat1 = city1.latitude;
double lat2 = city2.latitude;
double lon1 = city1.longitude;
double lon2 = city2.longitude;
double radiusOfEarth = 4182.44949; // miles, 6371km; 
double dLat = Math.toRadians(lat2-lat1); 
double dLon = Math.toRadians(lon2-lon1); 
double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2); 
double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
double distance = radiusOfEarth * c; return distance;
```

Note above that the earth radius is slightly bigger than it is (~3959 miles).  When the authors dealt with the code, they missed this trivial typo and didn't realize it until the paper was published. So, it doesn't affect anything other than distances slightly. If you want to replicate, you need to use the same number, if not, you can use the actual radius.


