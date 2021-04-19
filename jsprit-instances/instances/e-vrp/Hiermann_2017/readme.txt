Instance-set for the 'Hybrid Heterogeneous Electric Fleet routing problem with Time Windows and recharging stations'

Each file consists of a table of nodes, followed by available the vehicle types (fleet), and the cost values for fossil fuel and electricity. The parts are separated by a single empty line. All instances uses euclidian distances; travel times between nodes equals the distance.

Nodes:
First line is the header describing the node table
types:
- d = depot
- f = recharging station
- c = customer

Fleet:
First line is the header describing the vehicle type table
columns: 
- 'class'    = vehicle class (CV,PHV,BEV)
- 'capacity' = the transport capacity (to fulfill the customer demands)
- 'fuel'     = fuel consumption rate
- 'battery'  = battery capacity
- 'energy'   = electric energy consumption rate
- 'cost'     = fixed cost per vehicle usage