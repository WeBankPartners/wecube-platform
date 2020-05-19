package com.webank.wecube.platform.core.model.workflow;

import java.util.ArrayList;
import java.util.List;

public class GraphNode {
    private String id;
    private List<GraphNode> srcGraphNodes = new ArrayList<>();
    private List<GraphNode> toGraphNodes = new ArrayList<>();

    private boolean visited;

    public GraphNode(String id) {
        super();
        this.id = id;
    }

    public GraphNode() {
        super();
    }

    public static GraphNode getGraphNodeFromRootNodeById(GraphNode rootNode, String id) {
        GraphNode expectedNode = rootNode.getGraphNodeById(id);
        rootNode.resetVisitedAll();
        return expectedNode;
    }

    public GraphNode getGraphNodeById(String externald) {
        this.visited();

        if (id.equals(externald)) {
            return this;
        }

        GraphNode expectedGraphNode = null;

        if (this.toGraphNodes != null) {
            for (GraphNode g : this.toGraphNodes) {
                if (!g.isVisited()) {
                    GraphNode n = g.getGraphNodeById(externald);
                    if (n != null) {
                        expectedGraphNode = n;
                        break;
                    }
                }
            }
        }

        return expectedGraphNode;
    }

    public boolean contains(String id) {
        GraphNode n = getGraphNodeFromRootNodeById(this, id);
        return !(n == null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<GraphNode> getSrcGraphNodes() {
        return srcGraphNodes;
    }

    public void setSrcGraphNodes(List<GraphNode> srcGraphNodes) {
        this.srcGraphNodes = srcGraphNodes;
    }

    public List<GraphNode> getToGraphNodes() {
        return toGraphNodes;
    }

    public void setToGraphNodes(List<GraphNode> toGraphNodes) {
        this.toGraphNodes = toGraphNodes;
    }

    public GraphNode addSrcGraphNodes(GraphNode... graphNodes) {
        if (this.srcGraphNodes == null) {
            this.srcGraphNodes = new ArrayList<>();
        }

        for (GraphNode g : graphNodes) {
            this.srcGraphNodes.add(g);
            if (!g.toGraphNodes.contains(this)) {
                g.addToGraphNodes(this);
            }
        }

        return this;
    }

    public GraphNode addToGraphNodes(GraphNode... graphNodes) {
        if (this.toGraphNodes == null) {
            this.toGraphNodes = new ArrayList<>();
        }

        for (GraphNode g : graphNodes) {
            this.toGraphNodes.add(g);
            if (!g.srcGraphNodes.contains(this)) {
                g.addSrcGraphNodes(this);
            }
        }

        return this;
    }

    public GraphNode removeSrcGraphNodes(GraphNode... graphNodes) {
        if (this.srcGraphNodes != null) {
            for (GraphNode g : graphNodes) {
                this.srcGraphNodes.remove(g);
                if (g.containedInToNodes(this)) {
                    g.removeToGraphNodes(this);
                }
            }
        }

        return this;
    }

    public GraphNode removeToGraphNodes(GraphNode... graphNodes) {
        if (this.toGraphNodes != null) {
            for (GraphNode g : graphNodes) {
                this.toGraphNodes.remove(g);
                if (g.containedInSrcNodes(this)) {
                    g.removeSrcGraphNodes(this);
                }
            }
        }

        return this;
    }

    public boolean containedInToNodes(GraphNode graphNode) {
        if (this.toGraphNodes != null) {
            for (GraphNode g : this.toGraphNodes) {
                if (g.getId().equals(graphNode.getId())) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean containedInSrcNodes(GraphNode graphNode) {
        if (this.srcGraphNodes != null) {
            for (GraphNode g : this.srcGraphNodes) {
                if (g.getId().equals(graphNode.getId())) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public GraphNode visited() {
        this.setVisited(true);
        return this;
    }

    public GraphNode resetVisited() {
        this.setVisited(false);
        return this;
    }

    public GraphNode resetVisitedAll() {
        this.resetVisited();
        if (this.toGraphNodes != null) {
            for (GraphNode g : this.toGraphNodes) {
                if (g.isVisited()) {
                    g.resetVisitedAll();
                }
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "GraphNode [id=" + id + ", visited=" + visited + "]";
    }
    
    
}
