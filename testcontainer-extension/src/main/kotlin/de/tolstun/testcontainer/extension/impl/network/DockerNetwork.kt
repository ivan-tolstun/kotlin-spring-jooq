package de.tolstun.testcontainer.extension.impl.network

import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.Network
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.random.Random


object CurrentDockerNetwork {


    val INTERN_NETWORK: Network = Network.NetworkImpl.builder().build()


    val DOCKER_HOST by lazy {
        InetAddress.getByName(DockerClientFactory.instance().dockerHostIpAddress())
    }


    val DOCKER_CLIENT by lazy {
        DockerClientFactory.instance().client()
    }


    fun randomFreePort(from: Int, until: Int): Int {

        var port = Random.nextInt(from, until)
        var numberOfAttempts = 100

        do {

            val isPortFree =
                try { ServerSocket(port, 0, DOCKER_HOST).use { it.close() }; true; }
                catch (e: Exception) { port = Random.nextInt(from, until); numberOfAttempts--; false; }

        } while (!isPortFree && numberOfAttempts > 0)

        return port
    }

}



