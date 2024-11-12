#!/usr/bin/env python
# -*-coding: utf-8 -*-

from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, UserSwitch
from mininet.node import IVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import TCLink, Intf
import time
import threading
import os
import logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')

def send_traffic(host, ip, port, rate, duration):
    info('Mandando tráfego legítimo (20 pps)')
    #host.cmd('nping --tcp -p 80 --source-port {} --rate {} --count {} {} &'.format(port, rate, duration*rate, ip))
    host.cmd('hping3 -S -p 80 -s {} --keep --interval u50000 --count {} {} &'
             .format(port, rate*duration, ip))

def dos_attack(host, ip, port, duration):
    info('Mandando DoS (2000 pps)')
    rate = 2000
    #host.cmd('nping --tcp -p 80 --source-port {} --rate 2000 --count {} {} &'.format(port, duration*2000, ip))
    host.cmd('hping3 -S -p 80 -s {} --keep --interval u500 --count {} {} &'
             .format(port, rate*duration, ip))

def stop_traffic(host):
    info('Encerrando envio de tráfego')
    host.cmd('killall nping')

def stop_tcpdump(host):
    info('Encerrando captura de pacotes')
    host.cmd('killall tcpdump')

def execute_script(script_path):
    os.system('bash {} &'.format(script_path))

def myNetwork():

    net = Mininet( topo=None,
                   build=False,
                   ipBase='10.0.0.0/8')

    info( '*** Adding controller\n' )
    c0=net.addController(name='c0',
                      controller=RemoteController,
                      ip='127.0.0.1',
                      protocol='tcp',
                      port=6653)
    #net.addController('c0')

    info( '*** Add switches\n' )
    s1 = net.addSwitch('s1', cls=OVSKernelSwitch,protocols='OpenFlow13')

    info( '*** Add hosts\n' )
    # Clientes
    h1 = net.addHost('h1', cls=Host, ip='10.0.0.1', mac='00:00:00:00:00:01', defaultRoute=None)
    h2 = net.addHost('h6', cls=Host, ip='10.0.0.4', mac='00:00:00:00:00:04', defaultRoute=None)
    h3 = net.addHost('h7', cls=Host, ip='10.0.0.5', mac='00:00:00:00:00:05', defaultRoute=None)
    
    # Adding server hosts with the same IP and MAC addresses
    server1 = net.addHost('h3', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02', defaultRoute=None)
    server2 = net.addHost('h2', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02', defaultRoute=None)
    server3 = net.addHost('h4', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02', defaultRoute=None)

    info( '*** Add links\n' )
    net.addLink(s1, h1)
    net.addLink(s1, server2)
    net.addLink(s1, server1)
    net.addLink(s1, server3)
    net.addLink(s1, h2)
    net.addLink(s1, h3)

    info( '*** Starting network\n' )
    net.build()
    net.start()

    arp_table_entries = [
        ("10.0.0.1", "00:00:00:00:00:01"),
        ("10.0.0.4", "00:00:00:00:00:04"),
        ("10.0.0.5", "00:00:00:00:00:05"),
    ]
    
    for ip, mac in arp_table_entries:
        server1.cmd("arp -s {} {}".format(ip, mac))
        server3.cmd("arp -s {} {}".format(ip, mac))

    time.sleep(3)

    info( '*** Starting HTTP servers on server hosts\n' )
    server1.cmd('python -m SimpleHTTPServer 80 &')
    server2.cmd('python -m SimpleHTTPServer 80 &')
    server3.cmd('python -m SimpleHTTPServer 80 &')
    
    time.sleep(3)

    info( '*** Starting packet capture on all hosts\n' )
    info('h1')
    h1.cmd('tcpdump -i h1-eth0 -w /tmp/h1.pcap &')
    info('h2')
    h2.cmd('tcpdump -i h6-eth0 -w /tmp/h2.pcap &')
    info('h3')
    h3.cmd('tcpdump -i h7-eth0 -w /tmp/h3.pcap &')
    info('server 1')
    server1.cmd('tcpdump -i h3-eth0 -w /tmp/server1.pcap &')
    info('server 2')
    server2.cmd('tcpdump -i h2-eth0 -w /tmp/server2.pcap &')
    info('server 3')
    server3.cmd('tcpdump -i h4-eth0 -w /tmp/server3.pcap &')

    time.sleep(3)

    # Start sending legitimate traffic
    traffic_rate = 20  # Adjust this rate as needed (packets per second)
    duration = 80  # Duration for initial legitimate traffic

    #t1 = threading.Thread(target=send_traffic, args=(h1, '10.0.0.2', 9622, traffic_rate, duration))
    t2 = threading.Thread(target=send_traffic, args=(h2, '10.0.0.2', 2011, traffic_rate, duration))
    t3 = threading.Thread(target=send_traffic, args=(h3, '10.0.0.2', 8347, traffic_rate, duration))

    #t1.start()
    t2.start()
    t3.start()

    time.sleep(10)  # Wait for 10 seconds

    # Start DoS attack
    t4 = threading.Thread(target=dos_attack, args=(h1, '10.0.0.2', 9622, 55))
    t4.start()

    time.sleep(3)  # Wait for 3 seconds

    # Execute the sensor.py script
    #t5 = threading.Thread(target=execute_script, args=('/home/onos/runSensor.sh',))
    #t5.start()

    time.sleep(67)

    #t1.join()
    t2.join()
    t3.join()
    t4.join()
    #t5.join()

    stop_traffic(h1)
    stop_traffic(h2)
    stop_traffic(h3)
    stop_tcpdump(h1)
    stop_tcpdump(h2)
    stop_tcpdump(h3)
    stop_tcpdump(server1)
    stop_tcpdump(server2)
    stop_tcpdump(server3)

    #info( '*** Running CLI\n' )
    #CLI(net)

    info( '*** Stopping network\n' )
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    myNetwork()
