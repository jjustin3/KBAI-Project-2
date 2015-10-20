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

    public SemanticNetwork(Generator generator) {
        this.generator = generator;
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
            else if (figure1Names.size() < figure2Names.size()) {
                figure1Names.add(null);
            }
        }

        // Get all permutations of figure2 for comparison to figure1
        List<List<String>> figure2Permutations = generator.generatePermutations(figure2Names);

        int bestScore = 0;
        Map<String, List<String>> bestRelationships = new HashMap<>();
        for (List<String> permutation : figure2Permutations) {
            int score = 0;

            Map<String, List<String>> relationships = new HashMap<>();
            for (List<String> pair : (List<List<String>>)generator.formPairs(figure1Names, permutation)) {
                RavensObject fig1Object = figure1Objects.get(pair.get(0));
                RavensObject fig2Object = figure2Objects.get(pair.get(1));
                List<String> fig1AttrList = new ArrayList<>();
                List<String> fig2AttrList = new ArrayList<>();

                if (fig1Object == null && fig2Object != null)
                    fig2AttrList.add("added");
                else if (fig1Object != null && fig2Object == null)
                    fig1AttrList.add("deleted");
                else if (fig1Object != null && fig2Object != null) {
                    HashMap<String, String> fig1Attributes = fig1Object.getAttributes();
                    HashMap<String, String> fig2Attributes = fig2Object.getAttributes();

                    if (compareAttributes(fig1Attributes, fig2Attributes, "shape")) {
                        score += 5;
                        fig2AttrList.add("sameShape");
                    } else if (fig1Attributes.get("shape") != null && fig2Attributes.get("shape") != null)
                        fig2AttrList.add("diffShape");

                    if (compareAttributes(fig1Attributes, fig2Attributes, "size")) {
                        score += 5;
                        fig2AttrList.add("sameSize");
                    } else if (fig1Attributes.get("size") != null && fig2Attributes.get("size") != null) {
                        score += 2;
                        fig2AttrList.add("diffSize");
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
                        if (!hasFill.contains("yes") || !hasFill.contains("no"))
                            fill = determineFill(fig1Attributes.get("fill"), fig2Attributes.get("fill"));

                        fig2AttrList.add(fill);
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
                    }

                    if (compareAttributes(fig1Attributes, fig2Attributes, "angle")) {
                        score += 5;
                        fig2AttrList.add("sameAngle");
                    } else if (fig1Attributes.get("angle") != null && fig2Attributes.get("angle") != null) {
                        score += 2;
                        int angleDiff = Math.abs(Integer.parseInt(fig2Attributes.get("angle"))
                                - Integer.parseInt(fig1Attributes.get("angle")));
                        fig2AttrList.add(Integer.toString(angleDiff));
                    }

                }

                if (fig1Object != null && !fig1AttrList.isEmpty())
                    relationships.put(fig1Object.getName(), fig1AttrList);
                if (fig2Object != null && !fig2AttrList.isEmpty())
                    relationships.put(fig2Object.getName(), fig2AttrList);
            }

            // Update the best relationship if this current score is better than the best
            if (score > bestScore) {
                bestRelationships = relationships;
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
}
