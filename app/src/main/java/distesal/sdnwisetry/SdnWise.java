package distesal.sdnwisetry;

/**
 * @author Sebastiano Milardo
 */

import com.github.sdnwiselab.sdnwise.configuration.Configurator;
import com.github.sdnwiselab.sdnwise.controller.Controller;
import com.github.sdnwiselab.sdnwise.controller.ControllerFactory;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableActionCallback;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow;
import com.github.sdnwiselab.sdnwise.graphStream.Graph;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SdnWise class of the SDN-WISE project. It loads the configuration file and
 * starts the Adaptation, the FlowVisor and the Controller.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public final class SdnWise extends Observable {

    private Controller controller;
    private InputStream controllerInputStream;

    private Observer delegate;

    public SdnWise(InputStream controllerInputStream, Observer delegate) {
        this.controllerInputStream = controllerInputStream;
        this.delegate = delegate;
    }

    /**
     * Returns the Controller layer of the SDN-WISE network.
     *
     * @return the returned Controller Layer.
     */
    public Controller getController() {
        return controller;
    }

    /**
     * Starts the Controller layer of the SDN-WISE network. The configurations
     * are specified in the config.ini file contained in the resources of the
     * project.
     * The options specified in this file are: a "lower" Adapter, in order to
     * communicate with
     * the flowVisor (See the Adapter javadoc for more info), an "algorithm" for
     * calculating the shortest path in the network. The only supported at the
     * moment is "DIJKSTRA".
     * A "map" which contains informations regarding the "TIMEOUT" in order to
     * remove a non responding node from the topology, a "RSSI_RESOLUTION" value
     * that triggers an event when a link rssi value changes more than the
     * set threshold. "GRAPH" option that set the kind of gui used for the
     * representation of the network, possible values are "GFX" for a GraphStream
     * based one and "SOCKET_IO" for an experimental web based one. In the last
     * case it also possible to specify the web address ("GRAPH_ADDR") of the
     * representation.
     *
     * @return the Controller layer of the current SDN-WISE network.
     */

    public Graph getNetworkGraph(Controller controller) {
        return controller.getNetworkGraph().getGraph();
    }

    public Controller startController() {
        Configurator conf = Configurator.load(controllerInputStream);
        controller = ControllerFactory.getController(conf.getController());
        new Thread(controller).start();
        controller.getNetworkGraph().addObserver(delegate);
        return controller;
    }

    /**
     * Starts an example of a SDN-WISE network. This method creates a Controller,
     * a FlowVisor and an Adaptation plus a simulated network.
     */

    public void startExemplaryControlPlane() {
        // creates the Controller layer
        controller = startController();

        // registers 11 nodes for the specified Controller in the FlowVisor
        HashSet<NodeAddress> nodeSetAll = new HashSet<NodeAddress>();
        for (int i = 0; i <= 11; i++) {
            nodeSetAll.add(new NodeAddress(i));
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(SdnWise.class.getName()).log(Level.SEVERE, null, ex);
        }


        // Lets add a new rule to a node. First lets create a rule
        FlowTableEntry fte = new FlowTableEntry();

        FlowTableWindow ftw = new FlowTableWindow();
        ftw.setLocation(FlowTableWindow.SDN_WISE_PACKET)
                .setOperator(FlowTableWindow.SDN_WISE_EQUAL)
                .setPos(NetworkPacket.SDN_WISE_TYPE)
                .setValueLow(0);

        FlowTableWindow[] array = new FlowTableWindow[3];
        array[0] = ftw;
        array[1] = new FlowTableWindow();
        array[2] = new FlowTableWindow();

        FlowTableActionCallback ftaf = new FlowTableActionCallback();
        ftaf.setCallbackId(1);

        fte.setWindows(array);
        fte.setAction(ftaf);
        // then we send it to the node
        controller.addRule((byte) 1, new NodeAddress("0.1"), fte);
    }
}


