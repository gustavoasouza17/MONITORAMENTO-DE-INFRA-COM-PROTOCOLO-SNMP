package br.com.cloudnet.agent;

import org.snmp4j.*;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class IniciandoEscutoTraps implements CommandResponder {

    public void iniciarEscuta() throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping(new UdpAddress("0.0.0.0/162"));
        Snmp snmp = new Snmp(transport);

        snmp.addCommandResponder(this);
        transport.listen();
        System.out.println("Servidor NOC aguardando alertas críticos na porta 162...");

        // Esta estrutura impede que o programa feche, mantendo a porta aberta
        while (true) {
            Thread.sleep(5000); // Pausa a thread principal a cada 5 segundos
        }
    }

    @Override
    public <A extends Address> void processPdu(CommandResponderEvent<A> commandResponderEvent) {
        PDU pdu = commandResponderEvent.getPDU();
        if (pdu != null) {
            System.out.println("====== ALERTA CRÍTICO RECEBIDO ======");
            System.out.println("Origem: " + commandResponderEvent.getPeerAddress());
            System.out.println("Métricas da TRAP: " + pdu.getVariableBindings());
        }
    }

    public static void main(String[] args) throws Exception {
        new IniciandoEscutoTraps().iniciarEscuta();
    }
}