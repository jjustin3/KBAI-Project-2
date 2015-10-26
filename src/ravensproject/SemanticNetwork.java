package ravensproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a class for building semantic networks over two figures.
 */
public class SemanticNetwork {

    private Generator generator;
    private List<List<RavensObject>> objectPairs;
    private Map<String, List<String>> transformationMap;

    public SemanticNetwork(Generator generator) {
        this.generator = generator;
        objectPairs = new ArrayList<>();
        transformationMap = new HashMap<>();
    }

    /**
     * This method is used to score and return the best relationship
     * between two figure's objects. This returns the objects and their
     * respective list of changes between themselves and their partner
     * object in the other figure.
     *
     * @param figure1
     * @param figure2
     * @return The map containing the objects and attribute changes
     */
    public Map<String, List<String>> formRelationships(RavensFigure figure1,
                                                       RavensFigure figure2) {

        // Retrieve figure1's objects and figure2's objects for comparison
        HashMap<String, RavensObject> figure1Objects = figure1.getObjects();
        HashMap<String, RavensObject> figure2Objects = figure2.getObjects();

        // Compare number of objects in each figure
        List<String> figure1Names = new ArrayList<>(figure1Objects.keySet());
        List<String> figure2Names = new ArrayList<>(figure2Objects.keySet());
        while (figure1Names.size() != figure2Names.size()) {
            if (figure1Names.size() > figure2Names.size())
                figure2Names.add(null);
            else if (figure1Names.size() < figure2Names.size())
                figure1Names.add(null);
        }

        List<String> sizeList = new ArrayList<>();
        sizeList.add("very small");
        sizeList.add("small");
        sizeList.add("medium");
        sizeList.add("large");
        sizeList.add("very large");
        sizeList.add("huge");

        // Get all permutations of figure2 for comparison to figure1
        List<List<String>> figure2Permutations = generator.generatePermutations(figure2Names);

        int bestScore = 0;
        Map<String, List<String>> bestRelationships = new HashMap<>();
        for (List<String> permutation : figure2Permutations) {
            int score = 0;

            Map<String, List<String>> relationships = new HashMap<>();
            List<List<RavensObject>> pairs = new ArrayList<>();
            Map<String, List<String>> transformations = new HashMap<>();
            for (List<String> pair : (List<List<String>>)generator.formPairs(figure1Names, permutation)) {
                RavensObject fig1Object = figure1Objects.get(pair.get(0));
                RavensObject fig2Object = figure2Objects.get(pair.get(1));
                List<String> fig1AttrList = new ArrayList<>();
                List<String> fig2AttrList = new ArrayList<>();
                List<String> transformationList = new ArrayList<>();

                if (fig1Object == null && fig2Object != null) {
                    fig2AttrList.add("added");
                    transformationList.add("added");
                } else if (fig1Object != null && fig2Object == null) {
                    fig1AttrList.add("deleted");
                    transformationList.add("deleted");
                } else if (fig1Object != null && fig2Object != null) {

                    //add only when pair exists (i.e. if no objects in pair are null)
                    List<RavensObject> figurePair = new ArrayList<>();
                    figurePair.add(fig1Object);
                    figurePair.add(fig2Object);
                    pairs.add(figurePair);

                    HashMap<String, String> fig1Attributes = fig1Object.getAttributes();
                    HashMap<String, String> fig2Attributes = fig2Object.getAttributes();

                    if (compareAttributes(fig1Attributes, fig2Attributes, "shape")) {
                        score += 5;
                        fig2AttrList.add("sameShape");
                    } else if (fig1Attributes.get("shape") != null && fig2Attributes.get("shape") != null) {
                        fig2AttrList.add("diffShape");
                        transformationList.add("transform");
                    }

                    if (compareAttributes(fig1Attributes, fig2Attributes, "size")) {
                        score += 5; // Todo - maybe lower these scores
                        fig2AttrList.add("sameSize");
                    } else if (fig1Attributes.get("size") != null && fig2Attributes.get("size") != null) {
                        score += 2;
                        fig2AttrList.add("diffSize");
                        if (sizeList.indexOf(fig1Attributes.get("size"))
                                < sizeList.indexOf(fig2Attributes.get("size")))
                            transformationList.add("grow");
                        else if (sizeList.indexOf(fig1Attributes.get("size"))
                                > sizeList.indexOf(fig2Attributes.get("size")))
                            transformationList.add("shrink");
                    }

                    if (compareAttributes(fig1Attributes, fig2Attributes, "width")) {
                        fig2AttrList.add("sameWidth");
                    } else if (fig1Attributes.get("width") != null && fig2Attributes.get("width") != null) {
                        score += 2;
                        fig2AttrList.add("diffWidth");
                        if (sizeList.indexOf(fig1Attributes.get("width"))
                                < sizeList.indexOf(fig2Attributes.get("width")))
                            transformationList.add("widen");
                        else if (sizeList.indexOf(fig1Attributes.get("width"))
                                > sizeList.indexOf(fig2Attributes.get("width")))
                            transformationList.add("compress");
                    }

                    if (compareAttributes(fig1Attributes, fig2Attributes, "height")) {
                        fig2AttrList.add("sameHeight");
                    } else if (fig1Attributes.get("height") != null && fig2Attributes.get("height") != null) {
                        score += 2;
                        fig2AttrList.add("diffHeight");
                        if (sizeList.indexOf(fig1Attributes.get("height"))
                                < sizeList.indexOf(fig2Attributes.get("height")))
                            transformationList.add("heighten");
                        else if (sizeList.indexOf(fig1Attributes.get("height"))
                                > sizeList.indexOf(fig2Attributes.get("height")))
                            transformationList.add("shorten");
                    }

                    if (compareAttributes(fig1Attributes, fig2Attributes, "fill")) {
                        score += 5;
                        fig2AttrList.add("sameFill");
                    } else if (fig1Attributes.get("fill") != null && fig2Attributes.get("fill") != null) {
                        score += 2;
                        String fill = "diffFill";

                        List<String> hasFill = new ArrayList<>();
                        hasFill.add(fig1Attributes.get("fill"));
                        hasFill.add(fig2Attributes.get("fill"));
                        if (!hasFill.contains("yes") || !hasFill.contains("no")) // Todo - might be && and not ||...
                            fill = determineFill(fig1Attributes.get("fill"), fig2Attributes.get("fill"));

                        fig2AttrList.add(fill);
                        if (fig1Attributes.get("fill").equals("yes") && fig2Attributes.get("fill").equals("no"))
                            transformationList.add("unfill");
                        else if (fig1Attributes.get("fill").equals("no") && fig2Attributes.get("fill").equals("yes"))
                            transformationList.add("fill");
                        else
                            transformationList.add("fill-"+fill);
                    }

                    if (compareAttributes(fig1Attributes, fig2Attributes, "alignment")) {
                        score += 5;
                        fig2AttrList.add("sameAlignment");
                    } else if (fig1Attributes.get("alignment") != null && fig2Attributes.get("alignment") != null) {
                        score += 2;
                        String align = determineAlignment(
                                fig1Attributes.get("alignment"), fig2Attributes.get("alignment")
                        );
                        fig2AttrList.add(align);
                        transformationList.add("align-"+align);
                    }

                    if (compareAttributes(fig1Attributes, fig2Attributes, "angle")) {
                        score += 5;
                        fig2AttrList.add("sameAngle");
                    } else if (fig1Attributes.get("angle") != null && fig2Attributes.get("angle") != null) {
                        score += 2;
                        int angleDiff = Math.abs(Integer.parseInt(fig2Attributes.get("angle"))
                                - Integer.parseInt(fig1Attributes.get("angle")));
                        fig2AttrList.add(Integer.toString(angleDiff));
                        transformationList.add(Integer.toString(angleDiff));
                    }

                    //this won't work because proportion will change with each object added
                    if (fig1Attributes.get("left-of") != null && fig2Attributes.get("left-of") != null) {
                        String[] fig1LeftOf = fig1Attributes.get("left-of").split(",");
                        String[] fig2LeftOf = fig1Attributes.get("left-of").split(",");
                        double fig1Proportion = 1 - ((double) fig1LeftOf.length
                                / (double) figure1Objects.keySet().size());
                        double fig2Proportion = 1 - ((double) fig2LeftOf.length
                                / (double) figure2Objects.keySet().size());

                        if (fig2Proportion == fig1Proportion)
                            score += 5;
                    }

                    if (fig1Attributes.get("above") != null && fig2Attributes.get("above") != null) {
                        String[] fig1Above = fig1Attributes.get("above").split(",");
                        String[] fig2Above = fig1Attributes.get("above").split(",");
                        double fig1Proportion = 1 - ((double) fig1Above.length
                                / (double) figure1Objects.keySet().size());
                        double fig2Proportion = (double) fig2Above.length
                                / (double) figure2Objects.keySet().size();
                        if (fig1Proportion == fig2Proportion)
                            score += 5;
                    }

                    // Todo - check if this is actually beneficial or if it is detrimental
                    if (fig1Attributes.get("overlaps") != null && fig2Attributes.get("overlaps") != null) {
                        String[] fig1Above = fig1Attributes.get("overlaps").split(",");
                        String[] fig2Above = fig1Attributes.get("overlaps").split(",");
                        double fig1Proportion = 1 - ((double) fig1Above.length
                                / (double) figure1Objects.keySet().size());
                        double fig2Proportion = (double) fig2Above.length
                                / (double) figure2Objects.keySet().size();
                        if (fig1Proportion == fig2Proportion)
                            score += 5;
                    }


                }

                // Todo - get rid of this and add "unchanged" for every fig attribute if unchanged
                if (transformationList.isEmpty())
                    transformationList.add("unchanged");

                if (fig1Object != null && !fig1AttrList.isEmpty())
                    relationships.put(fig1Object.getName(), fig1AttrList);
                if (fig2Object != null && !fig2AttrList.isEmpty())
                    relationships.put(fig2Object.getName(), fig2AttrList);

                String fig1Name = "";
                String fig2Name = "";
                if (fig1Object != null)
                    fig1Name = fig1Object.getName();
                if (fig2Object != null)
                    fig2Name = fig2Object.getName();

                transformations.put(fig1Name+"-"+fig2Name, transformationList);
            }

            // Update the best relationship if this current score is better than the best
            if (score > bestScore) {
                bestRelationships = relationships;
                objectPairs = pairs;
                transformationMap = transformations;
                bestScore = score;
            }

        }

        return bestRelationships;
    }

    /**
     * This method compares the attributes of each figure. The point is to pull this
     * logic out of the main algorithm because it is repeated so much.
     *
     * @param fig1Attributes
     * @param fig2Attributes
     * @param attribute
     * @return Whether or not the attributes are the same
     */
    public boolean compareAttributes (HashMap<String, String> fig1Attributes,
                                      HashMap<String, String> fig2Attributes,
                                      String attribute) {

        String fig1Attribute = fig1Attributes.get(attribute);
        String fig2Attribute = fig2Attributes.get(attribute);
        if(fig1Attribute != null && fig2Attribute != null)
            if (fig1Attribute.equals(fig2Attribute))
                return true;
        return false;
    }

    /**
     * This method determines the alignment change between two
     * figures and returns the string of that change.
     *
     * @param fig1Align
     * @param fig2Align
     * @return Alignment change between figures
     */
    public String determineAlignment(String fig1Align, String fig2Align) {
        String[] fig1Attrs = fig1Align.split("-");
        String[] fig2Attrs = fig2Align.split("-");
        String vertChange = "";
        String horizChange = "";
        String change;

        if (fig1Attrs[0].equals("bottom") && fig2Attrs[0].equals("top"))
            vertChange = "up";
        else if (fig1Attrs[0].equals("top") && fig2Attrs[0].equals("bottom"))
            vertChange = "down";

        if (fig1Attrs[1].equals("left") && fig2Attrs[1].equals("right"))
            horizChange = "right";
        if (fig1Attrs[1].equals("right") && fig2Attrs[1].equals("left"))
            horizChange = "left";

        if (!vertChange.equals("") && !horizChange.equals(""))
            change = vertChange + "-" + horizChange;
        else
            change = vertChange + horizChange;

        return change;
    }

    /**
     * This method determines the fill change between two
     * figures and returns the string of that change.
     *
     * @param fig1Fill
     * @param fig2Fill
     * @return Fill change between figures
     */
    public String determineFill(String fig1Fill, String fig2Fill) {
        String[] fig1Attrs = fig1Fill.split("-");
        String[] fig2Attrs = fig2Fill.split("-");
        int change = 0; //rotation change in degrees

        switch (fig1Attrs[0]) {
            case "bottom":
                switch (fig2Attrs[0]) {
                    case "top":
                        change = 180;
                        break;
                    case "right":
                        change = 90;
                        break;
                    case "left":
                        change = 270;
                        break;
                }
                break;
            case "top":
                switch (fig2Attrs[0]) {
                    case "bottom":
                        change = 180;
                        break;
                    case "right":
                        change = 270;
                        break;
                    case "left":
                        change = 90;
                        break;
                }
                break;
            case "left":
                switch (fig2Attrs[0]) {
                    case "top":
                        change = 270;
                        break;
                    case "bottom":
                        change = 90;
                        break;
                    case "right":
                        change = 180;
                        break;
                }
                break;
            case "right":
                switch (fig2Attrs[0]) {
                    case "top":
                        change = 90;
                        break;
                    case "bottom":
                        change = 270;
                        break;
                    case "left":
                        change = 180;
                        break;
                }
                break;
        }

        return Integer.toString(change);
    }

    public List<List<RavensObject>> getObjectPairs() {
        return objectPairs;
    }

    public Map<String, List<String>> getTransformationMap() {
        return transformationMap;
    }
}
