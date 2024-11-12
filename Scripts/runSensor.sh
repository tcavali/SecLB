#!/bin/bash

params=("10.0.0.1:9622" "10.0.0.4:2011" "10.0.0.5:8347")

for param in "${params[@]}"; do
    echo "Ëxecutando consulta para par IP:Porta: $param"
    python3 /home/onos/sensor.py "$param"

    sleep 3
done

echo "Execução completa"
