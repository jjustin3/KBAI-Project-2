package ravensproject;

import java.util.List;
import java.util.Map;

/**
 * Created by justinjackson on 10/24/15.
 */
public class Relationship {

    private SemanticNetwork semanticNetwork;
    private Generator generator;
    private String name; //the relationship name i.e. "BC"
    private RavensFigure fig1, fig2;
    private Map<String, List<String>> relationship;
    private List<List<RavensObject>> objectPairs;
    private Map<String, List<String>> transformationMap;
    private int numObjDiff; //maybe move to scores in SemanticNetwork

    // Todo - maybe pass in SemanticNetwork and Generator to recycle logic
    public Relationship(RavensFigure fig1, RavensFigure fig2) {
        generator = new Generator();
        semanticNetwork = new SemanticNetwork(generator);
        this.fig1 = fig1;
        this.fig2 = fig2;
        name = fig1.getName() + "-" + fig2.getName();
        relationship = semanticNetwork.formRelationships(fig1, fig2);
        objectPairs = semanticNetwork.getObjectPairs();
        transformationMap = semanticNetwork.getTransformationMap();
        numObjDiff = fig2.getObjects().keySet().size() - fig1.getObjects().keySet().size();
    }

    public String getName() {
        return name;
    }

    public RavensFigure getFig1() {
        return fig1;
    }

    public RavensFigure getFig2() {
        return fig2;
    }

    public Map<String, List<String>> getRelationship() {
        return relationship;
    }

    public List<List<RavensObject>> getObjectPairs() {
        return objectPairs;
    }

    public Map<String, List<String>> getTransformationMap() {
        return transformationMap;
    }

    public int getNumObjDiff() {
        return numObjDiff;
    }

}
