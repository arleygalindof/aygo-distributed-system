package com.aygo.distributedapp.cluster;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ObjectMessage;
import org.jgroups.Receiver;
import org.jgroups.View;

import java.util.Scanner;

/**
 * Nodo simple de prueba para JGroups.
 * Permite enviar y recibir mensajes dentro de un canal de grupo.
 */
public class SimpleClusterNode implements Receiver {

    private JChannel channel;

    public void start() throws Exception {
        // Creamos el canal y le damos un nombre de grupo (por ejemplo, "ClusterTest")
        channel = new JChannel(); // Usa la configuración por defecto UDP.xml
        channel.setReceiver(this);
        channel.connect("ClusterTest");

        System.out.println("Nodo unido al clúster. Escribe mensajes para enviarlos a los demás nodos.");
        System.out.println("Presiona Ctrl+C para salir.\n");

        // Permitir que el usuario escriba mensajes para enviarlos
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            Message msg=new ObjectMessage(null, line);

            channel.send(msg);
        }
    }

    @Override
    public void receive(Message msg) {
        System.out.println("Mensaje recibido desde " + msg.src() + ": " + msg.getObject());
    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("Vista actualizada: " + view);
    }

    public static void main(String[] args) throws Exception {
        new SimpleClusterNode().start();
    }
}
