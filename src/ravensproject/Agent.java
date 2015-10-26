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
        System.out.println("Solving "+problem.getName());

        // Array for potential answers
        List<String> answers = new ArrayList<>();

        // Get row and col size
        int row = Character.getNumericValue(problem.getProblemType().charAt(0));
        int col = Character.getNumericValue(problem.getProblemType().charAt(2));

        // Retrieve figures from problem
        Map<String, RavensFigure> figureMap = problem.getFigures();

        // Get list of figure names for problem
        List<String> figureKeyListLR = createKeyList(figureMap, "[A-Z]");

        // Get list of figure names for solutions
        List<String> solutionKeyList = createKeyList(figureMap, "[0-9]");

        // Create list-matrix resembling RPM with null for placeholder on last entry
        List<List<RavensFigure>> ravensFiguresListLR =
                new ArrayList<>(getRavensMatrix(figureMap, figureKeyListLR, row, col));
        List<List<RavensFigure>> ravensFiguresListUD =
                new ArrayList<>(generateUpDownMatrix(ravensFiguresListLR));

        // Todo - find a way to move this to a method without forgetting about lastRavensFigure
        // Determine left-right relationships between objects in figures
        List<List<Relationship>> probRelationshipsListLR = new ArrayList<>(); //list of lists of relationships
        RavensFigure lastRavensFigureLR = null;
        for (List<RavensFigure> figureList : ravensFiguresListLR) {
            List<Relationship> tempRelationshipList = new ArrayList<>();
            for (int i = 0; i < figureList.size() - 1; i++) {
                if (figureList.get(i+1) != null) {
                    RavensFigure rFig1 = figureList.get(i);
                    RavensFigure rFig2 = figureList.get(i+1);
                    Relationship relationship = new Relationship(rFig1, rFig2);
                    tempRelationshipList.add(relationship);
                } else {
                    lastRavensFigureLR = figureList.get(i);
                }
            }
            probRelationshipsListLR.add(tempRelationshipList);
        }

        // Determine up-down relationships between objects in figures
        List<List<Relationship>> probRelationshipsListUD = new ArrayList<>(); //list of lists of relationships
        RavensFigure lastRavensFigureUD = null;
        for (List<RavensFigure> figureList : ravensFiguresListUD) {
            List<Relationship> tempRelationshipList = new ArrayList<>();
            for (int i = 0; i < figureList.size() - 1; i++) {
                if (figureList.get(i+1) != null) {
                    RavensFigure rFig1 = figureList.get(i);
                    RavensFigure rFig2 = figureList.get(i+1);
                    Relationship relationship = new Relationship(rFig1, rFig2);
                    tempRelationshipList.add(relationship);
                } else {
                    lastRavensFigureUD = figureList.get(i);
                }
            }
            probRelationshipsListUD.add(tempRelationshipList);
        }

        // Determine left-right relationship to solutions (i.e. C -> #)
        List<Relationship> solRelationshipsListLR = new ArrayList<>();
        for (String name : solutionKeyList) {
            if (lastRavensFigureLR != null) {
                RavensFigure rFig = figureMap.get(name);
                Relationship relationship = new Relationship(lastRavensFigureLR, rFig);
                solRelationshipsListLR.add(relationship);
            } else
                System.out.println("lastRavensFigureLR not defined."); //debug only
        }

        // Determine up-down relationship to solutions (i.e. B -> #)
        List<Relationship> solRelationshipsListUD = new ArrayList<>();
        for (String name : solutionKeyList) {
            if (lastRavensFigureUD != null) {
                RavensFigure rFig = figureMap.get(name);
                Relationship relationship = new Relationship(lastRavensFigureUD, rFig);
                solRelationshipsListUD.add(relationship);
            } else
                System.out.println("lastRavensFigureUD not defined."); //debug only
        }

        // Perform transformation analysis for left-right
        Map<String, Integer> solScoresMapLR = new HashMap<>();
        solScoresMapLR.putAll(determineScores(probRelationshipsListLR, solRelationshipsListLR));
        Map<String, Integer> solScoresMapUD = new HashMap<>();
        solScoresMapUD.putAll(determineScores(probRelationshipsListUD, solRelationshipsListUD));

        // Determine top picks for LR
        List<RavensFigure> solutionListLR = determineBestSolutions(figureMap, solScoresMapLR);

        // Determine top picks for UD
        List<RavensFigure> solutionListUD = determineBestSolutions(figureMap, solScoresMapLR);

        // Determine that best solutions are what the two have in common
        // Todo - maybe check LR first. If too many options, do UD
        List<RavensFigure> solutionList = generator.intersection(solutionListLR, solutionListUD);

        // If solutionList is empty, assign it one of the non-empty solution lists
        if (solutionList.isEmpty()) {
            if (!solutionListLR.isEmpty())
                solutionList = solutionListLR;
            else if (!solutionListUD.isEmpty())
                solutionList = solutionListUD;
        }

        // Determine diagonal relationship
        List<RavensFigure> ravensFiguresDiag = getRavensFiguresDiagonal(ravensFiguresListLR);
        List<Relationship> diagonalRelationships = new ArrayList<>();
        RavensFigure lastRavensFigureDiag = null;
        for (int i = 0; i < ravensFiguresDiag.size() - 1; i++) {
            if (ravensFiguresDiag.get(i + 1) != null) {
                RavensFigure rFig1 = ravensFiguresDiag.get(i);
                RavensFigure rFig2 = ravensFiguresDiag.get(i + 1);
                Relationship relationship = new Relationship(rFig1, rFig2);
                diagonalRelationships.add(relationship);
            } else {
                lastRavensFigureDiag = ravensFiguresDiag.get(i);
            }
        }

        List<Relationship> solRelationshipsListDiag = new ArrayList<>();
        for (String name : solutionKeyList) {
            if (lastRavensFigureDiag != null) {
                RavensFigure rFig = figureMap.get(name);
                Relationship relationship = new Relationship(lastRavensFigureDiag, rFig);
                solRelationshipsListDiag.add(relationship);
            } else
                System.out.println("lastRavensFigureDiag not defined."); //debug only
        }

        Relationship diagSolutionGuess = null;
        String diagSolution = null;
        if (!diagonalRelationships.isEmpty()) {
            diagSolutionGuess = determineDiagonalScore(diagonalRelationships, solRelationshipsListDiag);
            String[] solutionGuess = diagSolutionGuess.getName().split("-");
            diagSolution = solutionGuess[1];
        }

        List<String> solStrings = new ArrayList<>();
        for (RavensFigure solution : solutionList)
            solStrings.add(solution.getName());

        System.out.println(solStrings);


        if (solStrings.size() > 2 || solStrings.size() <= 0) // < 0 should never happen!
            return -1;
        else if (solStrings.size() == 2)
            return Integer.parseInt(solStrings.get(random.nextInt(solutionList.size())));

        return Integer.parseInt(solStrings.get(0));
    }

    public List<String> createKeyList(Map<String, RavensFigure> figureMap, String regex) {
        List<String> keyList= new ArrayList<>();
        for (String name : figureMap.keySet())
            if (name.matches(regex))
                keyList.add(name);
        Collections.sort(keyList);

        return keyList;
    }

    public List<List<RavensFigure>> getRavensMatrix(Map<String, RavensFigure> figureMap,
                                                    List<String> figureKeyList,
                                                    int row,
                                                    int col) {

        figureKeyList.add(figureKeyList.size(), null); //add null object as placeholder
        List<List<RavensFigure>> ravensFiguresList = new ArrayList<>();
        int ind = 0;
        List<RavensFigure> ravensFigureList = new ArrayList<>();
        while (ind <= (row * col) - 1) {
            ravensFigureList.add(figureMap.get(figureKeyList.get(ind)));
            int realInd = ind + 1;
            if (realInd % col == 0) {
                ravensFiguresList.add(ravensFigureList);
                ravensFigureList = new ArrayList<>();
            }
            ind++;
        }

        return ravensFiguresList;
    }

    public List<List<RavensFigure>> generateUpDownMatrix(List<List<RavensFigure>> ravensFiguresList) {
        List<List<RavensFigure>> ravensFiguresListUD = new ArrayList<>();

        for (int i = 0; i < ravensFiguresList.size(); i++) {
            List<RavensFigure> tempList = new ArrayList<>();
            for (int j = 0; j < ravensFiguresList.size(); j++) {

                if (i == j)
                    tempList.add(j, ravensFiguresList.get(i).get(j));
                else
                    tempList.add(j, ravensFiguresList.get(j).get(i));
            }
            ravensFiguresListUD.add(i, tempList);
        }

        return ravensFiguresListUD;
    }

    public List<RavensFigure> getRavensFiguresDiagonal(List<List<RavensFigure>> ravensFiguresList) {
        List<RavensFigure> ravensFigures = new ArrayList<>();

        for (int i = 0; i < ravensFiguresList.size(); i++) {
            for (int j = 0; j < ravensFiguresList.size(); j++) {
                if (i == j)
                    ravensFigures.add(i, ravensFiguresList.get(i).get(j));
            }
        }

        return ravensFigures;
    }

    public Map<String, Integer> determineScores(List<List<Relationship>> probRelationshipList,
                                                List<Relationship> solRelationshipList) {

        Map<String, Integer> solRelationshipScores = new HashMap<>(); //store all scores for evaluation of confidence
        List<List<String>> transformationsList = new ArrayList<>();
        List<List<String>> objDiffList = new ArrayList<>();
        for (int i = 0; i < probRelationshipList.size() - 1; i++) {
            transformationsList.add(determineTransformations(probRelationshipList.get(i)));
            List<String> objDiffs = new ArrayList<>(determineNumObjGrowing(probRelationshipList.get(i)));
            objDiffList.add(objDiffs);
        }

        Integer bestScore = null;
        for (Relationship solRelation : solRelationshipList) {
            List<Relationship> tempRelationships =
                    new ArrayList<>((probRelationshipList.get(probRelationshipList.size() - 1)));
            tempRelationships.add(solRelation);
            List<String> tempTransformations = determineTransformations(tempRelationships);
            List<String> tempObjDiffs = determineNumObjGrowing(tempRelationships);

            int score = determineTransformationScores(transformationsList, tempTransformations);
            score += determineTransformationScores(objDiffList, tempObjDiffs);
            solRelationshipScores.put(solRelation.getName(), score);
            if ((bestScore == null) || (score > bestScore)) {
                bestScore = score;
            }
        }

        return solRelationshipScores;
    }

    public Relationship determineDiagonalScore(List<Relationship> diagRelationshipList,
                                                       List<Relationship> solRelationshipList) {

        Map<String, Integer> solRelationshipScores = new HashMap<>(); //store all scores for evaluation of confidence
        List<String> diagTransformations = determineTransformations(diagRelationshipList); //same thing as getTransformations
        Integer bestScore = null;
        Relationship bestRelationship = null;
        for (Relationship solRelationship : solRelationshipList) {
            List<String> solTransformations = new ArrayList<>();
            Map<String, List<String>> transformations = solRelationship.getTransformationMap();
            for (List<String> pairTransformations : transformations.values())
                for (String transformation : pairTransformations)
                    solTransformations.add(transformation);

            List<String> tempTransformations = new ArrayList<>(solTransformations);
            int score = 0;
            for (String transformation : diagTransformations) {
                if (tempTransformations.contains(transformation)) {
                    tempTransformations.remove(transformation);
                    score++;
                } else
                    score--;

            }

            // Todo - be careful with this...could remove useful information
            tempTransformations.removeAll(Arrays.asList("unchanged"));

            score -= tempTransformations.size();

            solRelationshipScores.put(solRelationship.getName(), score);
            if ((bestScore == null) || (score > bestScore)) {
                bestScore = score;
                bestRelationship = solRelationship;
            }
        }

        return bestRelationship;
    }

    //takes in a single row of relationships in the RPM relationship matrix
    public List<String> determineTransformations(List<Relationship> relationships) {

        List<String> simpleTransformations = new ArrayList<>();
        for (Relationship relationship : relationships) {
            Map<String, List<String>> transformations = relationship.getTransformationMap();
            for (List<String> pairTransformations : transformations.values())
                for (String transformation : pairTransformations)
                    simpleTransformations.add(transformation);
        }

        //Collections.sort(simpleTransformations); //sorting gets rid of in-order status you need for comparing chronologically
        return simpleTransformations;
    }


    // Todo - modify this so that the score is per line, allowing the last line to compare to the first or second (for 3x3)
    public int determineTransformationScores(List<List<String>> transformationsList,
                                             List<String> tempTransformations) {
        int score = 0;
        for (List<String> transformations : transformationsList) {
            List<String> solTransformations = new ArrayList<>(tempTransformations);

            for (String transformation : transformations) {
                if (solTransformations.contains(transformation)) {
                    solTransformations.remove(transformation);
                    score++;
                } else
                    score--;

            }

            // Todo - be careful with this...could remove useful information
            solTransformations.removeAll(Arrays.asList("unchanged"));

            score -= solTransformations.size();
        }


        return score;
    }

    public List<String> determineNumObjGrowing(List<Relationship> relationshipList) {
        List<String> objDiffs = new ArrayList<>();
        for (Relationship relationship : relationshipList) {
            if (relationship.getNumObjDiff() > 0)
                objDiffs.add("growing");
            else if (relationship.getNumObjDiff() < 0)
                objDiffs.add("shrinking");
//            else
//                objDiffs.add("same")
        }

        if (objDiffs.contains("growing") && objDiffs.contains("shrinking")) {
            objDiffs.remove("growing");
            objDiffs.remove("shrinking");
        }

        return objDiffs;
    }

    public List<RavensFigure> determineBestSolutions(Map<String, RavensFigure> figureMap,
                                                     Map<String, Integer> solScoresMap) {

        int maxScoreLR = Collections.max(solScoresMap.values());
        List<RavensFigure> solutionList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : solScoresMap.entrySet()) {
            if (entry.getValue().equals(maxScoreLR)) {
                String[] solName = entry.getKey().split("-");
                RavensFigure solution = figureMap.get(solName[1]);
                if (!solutionList.contains(solution))
                    solutionList.add(solution);
            }
        }

        return solutionList;
    }

}

/*
Todo:
+ maybe sub-in solution into ravensFiguresList to run analysis on
--> too much re-analysis of items already analyzed
--> try to just sub-in and form relation between sub and surrounding
    figures (i.e. only C -> # and B -> # instead of A -> B, A -> C
    as well...
+ create separate method for comparing relationships between objects
--> called after left-right relationship forming and called after up-down
    relationship forming
+ might have to redo ravenFiguresList for up-down creation (or duplicate)

+ evaluate A -> B and B -> C relationships and choose what scores best/equally

+ currently probRelationshipList only has first 1 or 2 lines (not last line)
 */
