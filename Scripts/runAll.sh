#!/bin/bash

sudo python /home/onos/mininet/examples/sim_http_server.py &
MININET_PID=$!
sleep 25
bash /home/onos/runSensor.sh
wait $MININET_PID
echo "Fim da execução"