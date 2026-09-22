package br.com.cloudnet.agent;

import org.snmp4j.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class GatilhoAlarme2 {

    public void dispararTrapSimples() throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();
        Snmp snmp = new Snmp(transport);

        // Configura o destino: O IP do NOC e a porta 162
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(new UdpAddress("127.0.0.1/162"));
        target.setVersion(SnmpConstants.version2c);

        // Monta o pacote de alerta (TRAP)
        PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);

        // Adiciona uma OID diferente (final .1.3) para diferenciar do Gatilho 1
        OID metricOid = new OID("1.3.6.1.4.1.9999.1.3");
        VariableBinding alerta = new VariableBinding(metricOid, new OctetString("ALERTA DE TESTE 2: Queda de conexao com Banco de Dados"));
        pdu.add(alerta);

        // Dispara o alerta pela rede
        snmp.send(pdu, target);
        System.out.println("Gatilho 2 disparado! TRAP enviada com sucesso para o NOC.");
        snmp.close();
    }

    public static void main(String[] args) throws Exception {
        new GatilhoAlarme2().dispararTrapSimples();
    }
}