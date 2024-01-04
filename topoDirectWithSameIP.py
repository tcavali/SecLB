#!/usr/bin/env python

from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, UserSwitch
from mininet.node import IVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import TCLink, Intf
from subprocess import call

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

    info( '*** Add switches\n')
    s1 = net.addSwitch('s1', cls=OVSKernelSwitch,protocols='OpenFlow13')

    info( '*** Add hosts\n')
    h1 = net.addHost('h1', cls=Host, ip='10.0.0.1', mac='00:00:00:00:00:01', defaultRoute=None)
    h2 = net.addHost('h2', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02', defaultRoute=None)
    h3 = net.addHost('h3', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02', defaultRoute=None)
    h4 = net.addHost('h4', cls=Host, ip='10.0.0.2', mac='00:00:00:00:00:02', defaultRoute=None)
    h5 = net.addHost('h5', cls=Host, ip='10.0.0.5', mac='00:00:00:00:00:05', defaultRoute=None)
    
    info( '*** Add links\n')
    net.addLink(s1, h1)
    net.addLink(s1, h2)
    net.addLink(s1, h3)
    net.addLink(s1, h4)
    net.addLink(s1, h5)

    info( '*** Starting network\n')
    net.build()
    info( '*** Starting controllers\n')
    #for controller in net.controllers:
    c0.start()

    h3.cmd("arp -s 10.0.0.1 00:00:00:00:00:01")
    h4.cmd("arp -s 10.0.0.1 00:00:00:00:00:01")
    h5.cmd("arp -s 10.0.0.1 00:00:00:00:00:01")
    
    info( '*** Starting switches\n')
    net.get('s1').start([c0])
    
    info( '*** Post configure switches and hosts\n')
    s1.cmd("ovs-vsctl del-port s1-eth5")
    #s1.cmd("ovs-vsctl add-port s1 s1-eth5 -- --id=@p get port s1-eth5 -- --id=@m create mirror name=m0 select-all=true output-port=@p -- set bridge s1 mirrors=@m")
    s1.cmd("ovs-vsctl add-port s1 s1-eth5 -- --id=@p get port s1-eth5 -- --id=@m create mirror name=m0 select-all=false select_src_port=s1-eth1 output-port=@p -- set bridge s1 mirrors=@m")
    
    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    myNetwork()

