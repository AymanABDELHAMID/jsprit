The sections of the files are as follows:
1) Dimensions and parameters
2) Depots data
3) Satellites  data
4) Pickup facilities data
5) Customers data
6) Assignments  customers-depots
(matrix with ndep rows and ncus columns, an entry equal to 1 means that the customer belongs to the corresponding depot)


Notation used in the files:
nnode:	number of nodes
ndep:	number of depots
nsat:	number of satellites
npic:	number of pickup facilities
nus:	number of customers
tdemand:total demand
vcap1:	vehicle capacity for the first level
vcap2:	vehicle capacity for the second level
nv1:	number of vehicles in the first level
nv2:	number of vehicles in the second level
vwt1:	working time for vehicles in the first level
vwt2:	working time for vehicles in the second level
speed1:	speed for vehicles in the first level
speed2:	speed for vehicles in the second level
ud1:	unit distance traveling cost for vehicles in the first level
ud2:	unit distance traveling cost for vehicles in the second level
utp:	unit time coefficient associated with CP service
ucp:	unit cost coefficient associated with CP service
utc:	unit time coefficient associated with home delivery
ddemand: total demand that must be served by the depots
dcap:	depot capacity
nveh:	number of vehicles
vfc:	vehicle fixed cost
scap:	satellite capacity
cdemand:customer demand
uhcost:	unit handling cost at satellites
uhtime:	unit handling time at satellites
x-cor:	coordinate x
y-cor:	coordinate y

Eucliden distance is used to compute the distance matrix, i.e., sqrt( (xA-xB)^2+ (yA-yB)^2)