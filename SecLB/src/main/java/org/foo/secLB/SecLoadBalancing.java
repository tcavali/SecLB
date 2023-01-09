/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.learningswitch;

import com.google.common.collect.Maps;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onlab.packet.MacAddress;
// Adicionado em 09/11
import org.onlab.packet.ICMP;
import org.onlab.packet.IPacket;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Prefix;
import org.onlab.packet.UDP;

import org.onlab.packet.MacAddress;
import org.onlab.packet.TCP;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Comunicação via socket
import java.io.*;
import java.net.*;
import java.lang.System;

import java.util.Map;
import java.util.Optional;

/**
 * Tutorial class used to help build a basic onos learning switch application.
 * This class contains the solution to the learning switch tutorial.  Change "enabled = false"
 * to "enabled = true" below, to run the solution.
 */
@Component(immediate = true, enabled = true)
public class SecLoadBalancing {
    private PortNumber pn;

    // Instantiates the relevant services.
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    private final Logger log = LoggerFactory.getLogger(getClass());

    // Variáveis para a abertura do socket client
    String host = "";
    String msg = "";
    int len = 0;
    int porta = 0;
    Socket client = null;
    InputStream entrada = null;
    OutputStream saida = null;
    ByteArrayOutputStream os = null;
    byte[] buffer = null;

    /*
     * Defining macTables as a concurrent map allows multiple threads and packets to
     * use the map without an issue.
     */
    protected Map<DeviceId, Map<MacAddress, PortNumber>> macTables = Maps.newConcurrentMap();
    private ApplicationId appId;
    private PacketProcessor processor;

    /**
     * Create a variable of the SwitchPacketProcessor class using the PacketProcessor defined above.
     * Activates the app.
     */
    @Activate
    protected void activate() {
        log.info("Started");
        appId = coreService.getAppId("org.foo.app"); //equal to the name shown in pom.xml file

        processor = new SwitchPacketProcessor();
        packetService.addProcessor(processor, PacketProcessor.director(3));

        /*
         * Restricts packet types to IPV4 and ARP by only requesting those types.
         */
        packetService.requestPackets(DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4).build(), PacketPriority.REACTIVE, appId, Optional.empty());
        packetService.requestPackets(DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_ARP).build(), PacketPriority.REACTIVE, appId, Optional.empty());

        // Tenta abrir a comunicação via socket
        host = "localhost";
        porta = 8347;
//        try{
//            client = new Socket(host,porta);
//            log.info("Cliente conectado ao servidor");
            // Envia o ID 1 para ver se o servidor retorna "Thiago"
//            msg = "1";
//            saida = client.getOutputStream();
//            saida.write(msg.getBytes("UTF-8"));
//
//            // Prepara para receber o retorno dos dados
//            entrada = client.getInputStream();
//            buffer = new byte[1024];
//            os = new ByteArrayOutputStream();
//
//            while((len = entrada.read(buffer)) != -1){
//                os.write(buffer,0,len);
//            }
//            log.info("Mensagem recebida: "+os.toString());

//        }catch(Exception e){
//            log.info("Erro na entrada: "+e.getMessage());
//        }
    }

    /**
     * Deactivates the processor by removing it.
     */
    @Deactivate
    protected void deactivate() {
        // Manda o comando de encerramento para o socket server
//        try {
//            msg = "fim";
//            saida = client.getOutputStream();
//            saida.write(msg.getBytes("UTF-8"));
//        } catch (Exception e) {
//            log.info("Erro no envio do fim: " + e.getMessage());
//        }

        log.info("Stopped");
        packetService.removeProcessor(processor);

        // Fecha serviços do socket client
//        try{
//            saida.close();
//            entrada.close();
//            os.close();
//            client.close();
//        }catch(Exception e){
//            log.info("Erro na saída: "+e.getMessage());
//        }
    }

    /**
     * This class contains pseudo code that you must replace with your own code.  Your job is to
     * send the packet out the port previously learned for the destination MAC, if it
     * exists. Otherwise flood the packet (to all ports).
     */
    private class SwitchPacketProcessor implements PacketProcessor {
        /**
         * Learns the source port associated with the packet's DeviceId if it has not already been learned.
         * Calls actLikeSwitch to process and send the packet.
         * @param pc PacketContext object containing packet info
         */
        @Override
        public void process(PacketContext pc) {
            log.info(pc.toString());
            initMacTable(pc.inPacket().receivedFrom());


            // This is the basic flood all ports switch that is enabled.
            //actLikeHub(pc);

            /*
             * This is the call to the actLikeSwitch method you will be creating. When
             * you are ready to test it, uncomment the line below, and comment out the
             * actLikeHub call above.
             */
            actLikeSwitch(pc);

        }

        /**
         * Example method. Floods packet out of all switch ports.
         *
         * @param pc the PacketContext object passed through from activate() method
         */
        public void actLikeHub(PacketContext pc) {
            // Mexi nessa função para não dar FLOOD no caso do ARP, mas sim redirecionar só para a porta 2.
            // Estou fazendo isso porque configurei os dois servidores com o mesmo MAC e IP, então obtendo a resposta
            // eu consigo somente redirecionar a porta.
            PortNumber outPort = null;
            //outPort = outPort.portNumber("2");
            //pc.treatmentBuilder().setOutput(PortNumber.FLOOD);
            pc.treatmentBuilder().setOutput(outPort.portNumber("2"));
            pc.send();
        }

        /**
         * Ensures packet is of required type. Obtain the PortNumber associated with the inPackets DeviceId.
         * If this port has previously been learned (in initMacTable method) build a flow using the packet's
         * out port, treatment, destination, and other properties.  Send the flow to the learned out port.
         * Otherwise, flood packet to all ports if out port is not learned.
         *
         * @param pc the PacketContext object passed through from activate() method
         */
        public void actLikeSwitch(PacketContext pc) {

            /*
             * Ensures the type of packet being processed is only of type IPV4 (not LLDP or BDDP).  If it is not, return
             * and do nothing with the packet. actLikeSwitch can only process IPV4 packets.
             */
            Ethernet ethPkt = pc.inPacket().parsed();
            //Short type = pc.inPacket().parsed().getEtherType();
            Short type = ethPkt.getEtherType();
            if (type != Ethernet.TYPE_IPV4 && type != Ethernet.TYPE_ARP) {
                return;
            }

            // Armazena o trust score vindo do BD.
            //0 para servidor comum e 1 para servidor decoy
            String trustScore = "0";

            ConnectPoint cp = pc.inPacket().receivedFrom();
            Map<MacAddress, PortNumber> macTable = macTables.get(cp.deviceId());
            MacAddress srcMac = pc.inPacket().parsed().getSourceMAC();
            MacAddress dstMac = pc.inPacket().parsed().getDestinationMAC();
            macTable.put(srcMac, cp.port());
            // A partir daqui eu começo a mudar o código
            for (Map.Entry<MacAddress, PortNumber> entrada : macTable.entrySet()) {
                log.info(entrada.getKey()+"/"+entrada.getValue());
            }

            if (type == Ethernet.TYPE_IPV4) {
                int portaCon = 0;
                //Getting IPv4 packet from ethernet frame
                //IPv4 ipPacket = (IPv4)ethPkt.getPayload();
                IPacket ipPkt = ethPkt.getPayload();
                IPv4 ipPacket = (IPv4) ipPkt;
                IPacket payload = ipPkt.getPayload();
                if (payload != null) {
                    if (payload instanceof TCP) {
                        portaCon = ((TCP) payload).getSourcePort();
                    }else if (payload instanceof UDP){
                        portaCon = ((UDP) payload).getSourcePort();
                    }else if (payload instanceof ICMP){
                        final ICMP icmp = (ICMP) payload;
                        log.info("O pacote é ICMP do tipo "+icmp.getIcmpType()+" com código "+icmp.getIcmpCode());
                    }
                }
                String enderecoIP = ipPacket.fromIPv4Address(ipPacket.getSourceAddress());
                //log.info("Recebido de "+ ipPacket.fromIPv4Address(ipPacket.getSourceAddress())+":"+porta);
                log.info("Recebido de " + enderecoIP + ":"+portaCon);

                // Se o pacote veio com destino ao servidor faz a String com IP e porta para pesquisa no BD
                if (dstMac.equals(dstMac.valueOf("00:00:00:00:00:02"))) {
                    try {
                        client = new Socket(host,porta);
                        log.info("Cliente conectado ao servidor");

                        msg = enderecoIP + ":" + portaCon;
                        saida = client.getOutputStream();
                        saida.write(msg.getBytes("UTF-8"));

                        // Prepara para receber o retorno dos dados
                        entrada = client.getInputStream();
                        buffer = new byte[1024];
                        os = new ByteArrayOutputStream();

                        while ((len = entrada.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        trustScore = os.toString();
                        log.info("Mensagem recebida: " + trustScore);
//                        log.info("Mensagem recebida: " + os.toString());

                    } catch (Exception e) {
                        log.info("Erro na leitura do BD: " + e.getMessage());
                    }finally {
                        try {
                            saida.close();
                            entrada.close();
                            os.close();
                            client.close();
                        }catch (Exception e){
                            log.info("Erro ao encerrar conexão: "+e.getMessage());
                        }
                    }
                }
            }

            /*
             * Learn the destination, source, and output port of the packet using a ConnectPoint and the
             * associated macTable.  If there is a known port associated with the packet's destination MAC Address,
             * the output port will not be null.
             */


            PortNumber outPort = null;
            //log.info("MAC de destino antes do if:"+dstMac.toString());
            if ((type == Ethernet.TYPE_IPV4) && (dstMac.equals(dstMac.valueOf("00:00:00:00:00:02")))){
                if (trustScore.equals("1")) {
                    log.info("Entrei no SvL");
                    outPort = outPort.portNumber("3");
                    //dstMac = dstMac.valueOf("00:00:00:00:00:03");
                    //pc.inPacket().parsed().setDestinationMACAddress("00:00:00:00:00:03");
                } else if (trustScore.equals("2")){
                    log.info("Entrei no SvH");
                    outPort = outPort.portNumber("4");
                }
            }else{
                log.info("Entrei no else");
                outPort = macTable.get(dstMac);
            }

            /*
             * If port is known, set pc's out port to the packet's learned output port and construct a
             * FlowRule using a source, destination, treatment and other properties. Send the FlowRule
             * to the designated output port.
             */
            if (outPort != null) {
                //log.info("MAC de destino depois do if: "+pc.inPacket().parsed().getDestinationMAC().toString());
                pc.treatmentBuilder().setOutput(outPort);
                FlowRule fr = DefaultFlowRule.builder()
                        .withSelector(DefaultTrafficSelector.builder().matchEthDst(dstMac).build())
                        .withTreatment(DefaultTrafficTreatment.builder().setOutput(outPort).build())
                        .forDevice(cp.deviceId()).withPriority(PacketPriority.REACTIVE.priorityValue())
                        .makeTemporary(60)
                        .fromApp(appId).build();

                flowRuleService.applyFlowRules(fr);
                pc.send();
            } else {
            /*
             * else, the output port has not been learned yet.  Flood the packet to all ports using
             * the actLikeHub method
             */
                actLikeHub(pc);
            }
        }

        /**
         * puts the ConnectPoint's device Id into the map macTables if it has not previously been added.
         * @param cp ConnectPoint containing the required DeviceId for the map
         */
        private void initMacTable(ConnectPoint cp) {
            macTables.putIfAbsent(cp.deviceId(), Maps.newConcurrentMap());

        }
    }
}
