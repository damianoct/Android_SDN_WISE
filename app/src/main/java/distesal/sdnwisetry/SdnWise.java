package distesal.sdnwisetry;

/**
 * Created by damianodistefano on 01/07/15.
 */


import com.github.sdnwiselab.sdnwise.adaptation.Adaptation;
import com.github.sdnwiselab.sdnwise.adaptation.AdaptationFactory;
import com.github.sdnwiselab.sdnwise.configuration.Configurator;
import com.github.sdnwiselab.sdnwise.controller.Controller;
import com.github.sdnwiselab.sdnwise.controller.ControllerFactory;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableActionCallback;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableEntry;
import com.github.sdnwiselab.sdnwise.flowtable.FlowTableWindow;
import com.github.sdnwiselab.sdnwise.flowvisor.FlowVisor;
import com.github.sdnwiselab.sdnwise.flowvisor.FlowVisorFactory;
import com.github.sdnwiselab.sdnwise.node.SensorNode;
import com.github.sdnwiselab.sdnwise.node.SinkNode;
import com.github.sdnwiselab.sdnwise.packet.NetworkPacket;
import com.github.sdnwiselab.sdnwise.util.NodeAddress;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SdnWise class of the SDN-WISE project. It loads the configuration file and
 starts the Adaptation, the FlowVisor and the Controller.
 *
 * @author Sebastiano Milardo
 * @version 0.1
 */
public final class SdnWise {

    /**
     * Starts the components of the SDN-WISE Controller. In its default
     * configuration a simulated network of SDN-WISE nodes is started.
     * An SdnWise object is made of three main components: A Controller,
     * an Adaptation, and a FlowVisor.
     * The Controller manages the requests coming from the network, and creates
     * a virtual representation of the topology of the network.
     * The Adaptation adapts the format of  the packets coming from the nodes
     * in order to be accepted by the other components of the architecture and
     * vice versa.
     * The FlowVisor is responsible for authenticating nodes and controllers,
     * allowing the slicing of the network.
     *
     *
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception
    {

        //SdnWise sw = new SdnWise();
        //sw.startExemplaryControlPlane();
    }



    private FlowVisor flowVisor;
    private Adaptation adaptation;
    private Controller controller;
    private InputStream controllerInputStream, adaptationInputStream, flowVisorInputStream;

    public SdnWise(InputStream controllerInputStream, InputStream adaptationInputStream, InputStream flowVisorInputStream)
    {
        this.controllerInputStream = controllerInputStream;
        this.adaptationInputStream = adaptationInputStream;
        this.flowVisorInputStream = flowVisorInputStream;
    }

    /**
     * Returns the Adaptation layer of the SDN-WISE network.
     *
     * @return the returned Adaptation Layer.
     */
    public Adaptation getAdaptation() {
        return adaptation;
    }

    /**
     * Returns the FlowVisor layer of the SDN-WISE network.
     *
     * @return the returned FlowVisor Layer.
     */
    public FlowVisor getFlowVisor(){
        return flowVisor;
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

    public Controller startController()
    {
        //InputStream configFileURI = this.getClass().getResourceAsStream("/config.ini");
        // Configurator conf = Configurator.load(configFileURI);

        Configurator conf = Configurator.load(controllerInputStream);
        controller = ControllerFactory.getController(conf.getController());
        new Thread(controller).start();
        return controller;
    }

    /**
     * Starts the Controller layer of the SDN-WISE network. The path to the
     * configurations are specified in the configFilePath String.
     * The options to be specified in this file are: a "lower" Adapter,
     * in order to communicate with
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
     * @param configFilePath a String that specifies the path to the configuration file.
     * @return the Controller layer of the current SDN-WISE network.
     */

    public Controller startController(String configFilePath){
        InputStream configFileURI = null;
        if (configFilePath == null || configFilePath.isEmpty()) {
            configFileURI = this.getClass().getResourceAsStream("/config.ini");
        } else {
            try {
                configFileURI = new FileInputStream(configFilePath);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SdnWise.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Configurator conf = Configurator.load(configFileURI);
        controller = ControllerFactory.getController(conf.getController());
        new Thread(controller).start();
        return controller;
    }

    /**
     * Starts the FlowVisor layer of the SDN-WISE network. The configurations
     * are specified in the config.ini file contained in the resources of the
     * project.
     * The options to be specified in this file are: a "lower" Adapter,
     * in order to communicate with
     * the Adaptation and an "upper" Adapter to communicate with the Controller
     * (See the Adapter javaoc for more info).
     *
     * @return the Controller layer of the current SDN-WISE network.
     */

    public FlowVisor startFlowVisor() {
        InputStream configFileURI = this.getClass().getResourceAsStream("/config.ini");
        //Configurator conf = Configurator.load(configFileURI);

        Configurator conf = Configurator.load(flowVisorInputStream);
        flowVisor = FlowVisorFactory.getFlowvisor(conf.getFlowvisor());
        new Thread(flowVisor).start();
        return flowVisor;
    }

    /**
     * Starts the FlowVisor layer of the SDN-WISE network. The path to the
     * configurations are specified in the configFilePath String.
     * The options to be specified in this file are: a "lower" Adapter,
     * in order to communicate with
     * the Adaptation and an "upper" Adapter to communicate with the Controller
     * (See the Adapter javadoc for more info).
     *
     * @param configFilePath a String that specifies the path to the configuration file.
     * @return the Controller layer of the current SDN-WISE network.
     */

    public FlowVisor startFlowVisor(String configFilePath){
        InputStream configFileURI = null;
        if (configFilePath == null || configFilePath.equals("")){
            configFileURI = this.getClass().getResourceAsStream("/config.ini");
        } else {
            try {
                configFileURI = new FileInputStream(configFilePath);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SdnWise.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Configurator conf = Configurator.load(configFileURI);
        flowVisor = FlowVisorFactory.getFlowvisor(conf.getFlowvisor());
        new Thread(flowVisor).start();
        return flowVisor;
    }

    /**
     * Starts the Adaptation layer of the SDN-WISE network. The configurations
     * are specified in the config.ini file contained in the resources of the
     * project.
     * The options to be specified in this file are: a "lower" Adapter,
     * in order to communicate with
     * the Nodes and an "upper" Adapter to communicate with the FlowVisor
     * (See the Adapter javaoc for more info).
     *
     * @return the Controller layer of the current SDN-WISE network.
     */

    public Adaptation startAdaptation() {
        InputStream configFileURI = this.getClass().getResourceAsStream("/config.ini");
        //Configurator conf = Configurator.load(configFileURI);

        Configurator conf = Configurator.load(adaptationInputStream);
        adaptation = AdaptationFactory.getAdaptation(conf.getAdaptation());
        new Thread(adaptation).start();
        return adaptation;
    }

    /**
     * Starts the Adaptation layer of the SDN-WISE network. The path to the
     * configurations are specified in the configFilePath String.
     * The options to be specified in this file are: a "lower" Adapter,
     * in order to communicate with
     * the Nodes and an "upper" Adapter to communicate with the FlowVisor
     * (See the Adapter javaoc for more info).
     *
     * @param configFilePath a String that specifies the path to the configuration file.
     * @return the Controller layer of the current SDN-WISE network.
     */


    public Adaptation startAdaptation(String configFilePath) {
        InputStream configFileURI = null;
        if (configFilePath == null || configFilePath.isEmpty()) {
            configFileURI = this.getClass().getResourceAsStream("/config.ini");
        } else {
            try {
                configFileURI = new FileInputStream(configFilePath);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SdnWise.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Configurator conf = Configurator.load(configFileURI);
        adaptation = AdaptationFactory.getAdaptation(conf.getAdaptation());
        new Thread(adaptation).start();
        return adaptation;
    }

    /**
     * Starts an example of a SDN-WISE network. This method creates a Controller,
     * a FlowVisor and an Adaptation plus a simulated network.
     *
     */

    public void startExemplaryControlPlane() {
        // creates the Controller layer
        controller = startController();
        // creates the Adaptaion layer
        //adaptation = startAdaptation();
        // creates the FlowVisor layer
        //flowVisor = startFlowVisor();

        // registers 11 nodes for the specified Controller in the FlowVisor
        HashSet<NodeAddress> nodeSetAll = new HashSet<NodeAddress>();
        for (int i = 0; i<=11; i++){
            nodeSetAll.add(new NodeAddress(i));
        }
//        flowVisor.addController(controller.getId(), nodeSetAll);

        //this.startVirtualNetwork();

        // sleeps a little waiting for all the nodes to show


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
        controller.addRule((byte)1, new NodeAddress("0.1"), fte);

        // now we send a new function to a node
        /*controller.sendFunction(
                (byte) 1,
                new NodeAddress("0.0"),
                new NodeAddress("0.1"),
                new NodeAddress("0.0"),
                (byte) 1,
                "HelloWorld.class");*/


    }

    /**
     * Creates a virtual Network of SDN-WISE nodes. The links between the nodes
     * are specified in each Node#.txt file in the resources directory.
     * These files contain the id of the neighbor, its ip address and port on which
     * it is listening and the rssi between the nodes. For example if in Node0.txt we
     * find 0.1,localhost,7771,215 it means that node 0.0 has 0.1 as neighbor,
     * which is listening on localhost:7771 and the rssi between 0.0 and 0.1 is
     * 215.
     */
    public void startVirtualNetwork(){
        Thread th = new Thread(new SinkNode(
                // its own id
                (byte) 1,
                // its own address
                new NodeAddress("0.0"),
                // listener port
                7770,
                // controller address
                "localhost",
                // controller port
                9991,
                // neigh file
                "Node0.txt",
                // security
                false)
        );
        th.start();

        for(int i=1; i<=11; i++){
            new Thread(new SensorNode(
                    // its own id
                    (byte) 1,
                    // its own address
                    new NodeAddress(i),
                    // listener port
                    7770+i,
                    // neigh file
                    "Node"+i+".txt", false)).start();
        }
    }
}


