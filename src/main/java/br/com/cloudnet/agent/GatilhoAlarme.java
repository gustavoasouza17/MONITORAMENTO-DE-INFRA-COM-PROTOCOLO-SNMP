package br.com.cloudnet.agent;

import org.snmp4j.*;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.util.ArrayList;
import java.util.List;

public class GatilhoAlarme {

    // Define o limite crítico (ex: 70% de uso da memória alocada)
    private static final double LIMITE_USO_MEMORIA = 70.0;

    public void monitorarRecursos() throws Exception {
        System.out.println("Iniciando monitoramento de recursos do servidor...");

        while (true) {
            // Captura os dados reais da máquina virtual Java
            long totalMemoria = Runtime.getRuntime().totalMemory();
            long memoriaLivre = Runtime.getRuntime().freeMemory();
            long memoriaUsada = totalMemoria - memoriaLivre;

            // Calcula o percentual de uso
            double percentualUsado = ((double) memoriaUsada / totalMemoria) * 100;

            System.out.printf("Monitoramento ativo: %.2f%% da memória em uso.%n", percentualUsado);

            // Condição de Alarme e Ação: avalia o limite de recursos
            if (percentualUsado > LIMITE_USO_MEMORIA) {
                System.out.println("CRÍTICO: Limite excedido! Disparando TRAP SNMP para o NOC...");
                dispararTrap(percentualUsado);

                // Pausa de 15 segundos após disparar para não inundar a rede com o mesmo alerta
                Thread.sleep(15000);
            } else {
                // Aguarda 3 segundos antes da próxima medição
                Thread.sleep(3000);
            }
        }
    }

    private void dispararTrap(double percentual) throws Exception {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        transport.listen();
        Snmp snmp = new Snmp(transport);

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(new UdpAddress("127.0.0.1/162"));
        target.setVersion(SnmpConstants.version2c);

        PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);

        // Define a MIB/OID customizada para consumo de memória[cite: 2]
        OID metricOid = new OID("1.3.6.1.4.1.9999.1.2");
        String mensagemAlerta = String.format("ALERTA DE RECURSO: Uso de memoria atingiu %.2f%%", percentual);
        VariableBinding alerta = new VariableBinding(metricOid, new OctetString(mensagemAlerta));

        pdu.add(alerta);

        snmp.send(pdu, target);
        snmp.close();
    }

    public static void main(String[] args) throws Exception {
        new GatilhoAlarme().monitorarRecursos();
    }
}