package br.com.cloudnet.agent;

import org.snmp4j.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class GatilhoAlarme2 {

    public void verificarBancoDeDados() throws Exception {
        System.out.println("Iniciando verificação de conectividade com o Banco de Dados...");

        boolean bancoDeDadosOnline = true;

        if (bancoDeDadosOnline) {
            // Caminho feliz: Tudo está funcionando.
            System.out.println("Status: ONLINE. Conexão estável. A operação de rede está normal.");
            System.out.println("-> Nenhuma TRAP foi enviada para o NOC.");
        } else {
            // Caminho crítico: O banco caiu!
            System.out.println("CRÍTICO: Conexão com o Banco de Dados perdida!");
            System.out.println("-> Disparando TRAP SNMP de alerta...");
            dispararTrapSimples();
        }
    }

    private void dispararTrapSimples() throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();
        Snmp snmp = new Snmp(transport);

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(new UdpAddress("127.0.0.1/162"));
        target.setVersion(SnmpConstants.version2c);

        PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);

        OID metricOid = new OID("1.3.6.1.4.1.9999.1.3");
        VariableBinding alerta = new VariableBinding(metricOid, new OctetString("ALERTA DE TESTE 2: Queda de conexao com Banco de Dados"));
        pdu.add(alerta);

        snmp.send(pdu, target);
        System.out.println("Gatilho 2 finalizado! TRAP entregue com sucesso.");
        snmp.close();
    }

    public static void main(String[] args) throws Exception {
        new GatilhoAlarme2().verificarBancoDeDados();
    }
}