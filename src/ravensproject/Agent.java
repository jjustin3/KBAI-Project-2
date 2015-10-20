package ravensproject;

// Uncomment these lines to access image processing.
//import java.awt.Image;
//import java.io.File;
//import javax.imageio.ImageIO;

import java.util.*;

/**
 * Your Agent for solving Raven's Progressive Matrices. You MUST modify this
 * file.
 * 
 * You may also create and submit new files in addition to modifying this file.
 * 
 * Make sure your file retains methods with the signatures:
 * public Agent()
 * public char Solve(RavensProblem problem)
 * 
 * These methods will be necessary for the project's main method to run.
 * 
 */
public class Agent {

    private Generator generator;
    private Random random;
    private SemanticNetwork semanticNetwork;

    /**
     * The default constructor for your Agent. Make sure to execute any
     * processing necessary before your Agent starts solving problems here.
     * 
     * Do not add any variables to this signature; they will not be used by
     * main().
     * 
     */
    public Agent() {
        generator = new Generator();
        random = new Random();
        semanticNetwork = new SemanticNetwork(generator);
    }
    /**
     * The primary method for solving incoming Raven's Progressive Matrices.
     * For each problem, your Agent's Solve() method will be called. At the
     * conclusion of Solve(), your Agent should return a String representing its
     * answer to the question: "1", "2", "3", "4", "5", or "6". These Strings
     * are also the Names of the individual RavensFigures, obtained through
     * RavensFigure.getName().
     * 
     * In addition to returning your answer at the end of the method, your Agent
     * may also call problem.checkAnswer(String givenAnswer). The parameter
     * passed to checkAnswer should be your Agent's current guess for the
     * problem; checkAnswer will return the correct answer to the problem. This
     * allows your Agent to check its answer. Note, however, that after your
     * agent has called checkAnswer, it will *not* be able to change its answer.
     * checkAnswer is used to allow your Agent to learn from its incorrect
     * answers; however, your Agent cannot change the answer to a question it
     * has already answered.
     * 
     * If your Agent calls checkAnswer during execution of Solve, the answer it
     * returns will be ignored; otherwise, the answer returned at the end of
     * Solve will be taken as your Agent's answer to this problem.
     * 
     * @param problem the RavensProblem your agent should solve
     * @return your Agent's answer to this problem
     */
    public int Solve(RavensProblem problem) {

        // Array for potential answers
        List<String> answers = new ArrayList<>();

        // Todo - don't hardcode this for 2x2... make dynamic
        // Retrieve figures from problem
        RavensFigure figA = problem.getFigures().get("A");
        RavensFigure figB = problem.getFigures().get("B");
        RavensFigure figC = problem.getFigures().get("C");
        RavensFigure fig1 = problem.getFigures().get("1");
        RavensFigure fig2 = problem.getFigures().get("2");
        RavensFigure fig3 = problem.getFigures().get("3");
        RavensFigure fig4 = problem.getFigures().get("4");
        RavensFigure fig5 = problem.getFigures().get("5");
        RavensFigure fig6 = problem.getFigures().get("6");

        // Determine relationships between objects in figures
        Map<String, List<String>>  figAtoFigB = semanticNetwork.formRelationships(figA, figB);
        Map<String, List<String>>  figCtoFig1 = semanticNetwork.formRelationships(figC, fig1);
        Map<String, List<String>>  figCtoFig2 = semanticNetwork.formRelationships(figC, fig2);
        Map<String, List<String>>  figCtoFig3 = semanticNetwork.formRelationships(figC, fig3);
        Map<String, List<String>>  figCtoFig4 = semanticNetwork.formRelationships(figC, fig4);
        Map<String, List<String>>  figCtoFig5 = semanticNetwork.formRelationships(figC, fig5);
        Map<String, List<String>>  figCtoFig6 = semanticNetwork.formRelationships(figC, fig6);

        // Store relationships between C and solutions to map
        Map<String, Map<String, List<String>>> step1Sols = new HashMap<>();
        step1Sols.put("1", figCtoFig1);
        step1Sols.put("2", figCtoFig2);
        step1Sols.put("3", figCtoFig3);
        step1Sols.put("4", figCtoFig4);
        step1Sols.put("5", figCtoFig5);
        step1Sols.put("6", figCtoFig6);

        // Determine transformations between figures
        int figAtoFigBDiff = figB.getObjects().keySet().size() - figA.getObjects().keySet().size();
        List<List<List<String>>> figAtoFigBPerms = generator.generatePermutations(new ArrayList(figAtoFigB.values()));
        Map<String, Integer> step1Scores = new HashMap<>();
        List<String> sol1List = new ArrayList<>(step1Sols.keySet());
        for (String sol : sol1List) {
            step1Scores.put(sol, 0);

            int figCtoSolDiff = problem.getFigures().get(sol).getObjects().keySet().size()
                    - figC.getObjects().keySet().size();

            if (figAtoFigBDiff == figCtoSolDiff)
                step1Scores.put(sol, Math.abs(figCtoSolDiff) + 6);

            List<List<String>> rels = findBestRelationship(new ArrayList<>(step1Sols.get(sol).values()), figAtoFigBPerms, problem);

            for (List<List<String>> pair : (List<List<List<String>>>)generator.formPairs(
                    rels,
                    new ArrayList<>(step1Sols.get(sol).values()))) {

                int tempScore = step1Scores.get(sol) + generator.intersection(pair.get(0), pair.get(1)).size();
                step1Scores.put(sol, tempScore);
            }
        }

        for (String sol : sol1List) {
            if (step1Scores.get(sol).equals(Collections.max(step1Scores.values())))
                answers.add(sol);
        }

        if (answers.size() > 1) {
            List<String> figBLocations = getLocations(figB);
            Map<String, Integer> step2Scores = new HashMap<>();

            for (String ans : answers) {
                List<String> ansFigLocations = getLocations(problem.getFigures().get(ans));
                int tempScore = generator.intersection(figBLocations, ansFigLocations).size();
                step2Scores.put(ans, tempScore);
            }

            for (String sol : answers)
                if (step2Scores.get(sol) < Collections.max(step2Scores.values()))
                    answers.remove(sol);

        }

        if (answers.size() > 1)
            return Integer.parseInt(answers.get(random.nextInt(answers.size())));
        else if (answers.size() < 1)
            return -1; // for skipping
        return Integer.parseInt(answers.get(0));
    }

    /**
     * This method gets the locations of the objects in the figure relative
     * to one-another.
     *
     * @param figure
     * @return The list of relative locations contained in the figure
     */
    public List<String> getLocations(RavensFigure figure) {
        Map<String, RavensObject> figureObjects = figure.getObjects();

        // Get relative locations of each object in the figure
        List<String> locations = new ArrayList<>();
        for (String name : figureObjects.keySet()) {
            Map<String, String> figAttributes = figureObjects.get(name).getAttributes();

            if(figAttributes.get("overlaps") != null)
                locations.add("overlaps");
            if(figAttributes.get("inside") != null)
                locations.add("inside");
            if(figAttributes.get("above") != null)
                locations.add("above");
            if(figAttributes.get("left-of") != null)
                locations.add("left-of");
        }

        return locations;
    }

    // somewhat lost a working product (need to commit more) so this is what I have to resort to :(
    public List<List<String>> findBestRelationship(List<List<String>> list1, List<List<List<String>>> permList, RavensProblem problem) {
        int bestScore = 0;
        List<List<String>> bestList = new ArrayList<>();

        for (List<List<String>> p1 : permList) {
            int score = 0;
            List<List<String>> smallest = p1;
            if (list1.size() < smallest.size())
                smallest = list1;

            for (int i = 0; i < smallest.size(); i++) {
                score += generator.intersection(p1.get(i), list1.get(i)).size();
            }

            if (score > bestScore) {
                bestScore = score;
                bestList = p1;
            }

        }

        return bestList;
    }
}
