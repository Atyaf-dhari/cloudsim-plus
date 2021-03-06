/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.topologies;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a graph containing vertices (nodes) and edges (links),
 * used for input with a network-layer.
 * <p>
 * <p>Graphical-Output Restricions:
 * <ul>
 * <li>EdgeColors: GraphicalProperties.getColorEdge
 * <li>NodeColors: GraphicalProperties.getColorNode
 * </ul>
 * </p>
 *
 * @author Thomas Hohnstein
 * @since CloudSim Toolkit 1.0
 */
public class TopologicalGraph {
    /**
     * The list of links of the network graph.
     */
    private final List<TopologicalLink> linkList;

    /**
     * The list of nodes of the network graph.
     */
    private final List<TopologicalNode> nodeList;

    /**
     * Creates an empty graph-object.
     */
    public TopologicalGraph() {
        linkList = new LinkedList<>();
        nodeList = new LinkedList<>();
    }

    /**
     * Adds an link between two topological nodes.
     *
     * @param edge the topological link
     */
    public void addLink(TopologicalLink edge) {
        linkList.add(edge);
    }

    /**
     * Adds an Topological Node to this graph.
     *
     * @param node the topological node to add
     */
    public void addNode(TopologicalNode node) {
        nodeList.add(node);
    }

    /**
     * Gets the number of nodes contained inside the topological-graph.
     *
     * @return number of nodes
     */
    public int getNumberOfNodes() {
        return nodeList.size();
    }

    /**
     * Gets the number of links contained inside the topological-graph.
     *
     * @return number of links
     */
    public int getNumberOfLinks() {
        return linkList.size();
    }

    /**
     * Gets an iterator through all network-graph links.
     *
     * @return the iterator throug all links
     */
    public Iterator<TopologicalLink> getLinkIterator() {
        return linkList.iterator();
    }

    /**
     * Gets an iterator through all network-graph nodes.
     *
     * @return the iterator through all nodes
     */
    public Iterator<TopologicalNode> getNodeIterator() {
        return nodeList.iterator();
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("topological-node-information: \n");

        for (TopologicalNode node : nodeList) {
            buffer.append(
                String.format("%d | x is: %d y is: %d\n",
                node.getNodeID(), node.getCoordinateX(), node.getCoordinateY()));
        }

        buffer.append("\n\n node-link-information:\n");

        for (TopologicalLink link : linkList) {
            buffer.append(
                String.format("from: %d to: %d delay: %.2f\n",
                link.getSrcNodeID(), link.getDestNodeID(), link.getLinkDelay()));
        }
        return buffer.toString();
    }

}
