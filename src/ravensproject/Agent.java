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

        // Determine diagonal relationship to solutions (i.e. A -> #)
        List<Relationship> solRelationshipsListDiag = new ArrayList<>();
        for (String name : solutionKeyList) {
            if (lastRavensFigureDiag != null) {
                RavensFigure rFig = figureMap.get(name);
                Relationship relationship = new Relationship(lastRavensFigureDiag, rFig);
                solRelationshipsListDiag.add(relationship);
            } else
                System.out.println("lastRavensFigureDiag not defined."); //debug only
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
        List<RavensFigure> solutionList = generator.intersection(solutionListLR, solutionListUD);

        // If there exist only one diagonal solution, use it. If not, ignore it
        Map<String, Integer> diagRelationshipScores = new HashMap<>();
        List<RavensFigure> diagSolutions;
        String diagSolution = null;
        if (!diagonalRelationships.isEmpty()) {
            diagRelationshipScores.putAll(determineDiagonalScore(diagonalRelationships, solRelationshipsListDiag));
            diagSolutions = new ArrayList<>(determineBestSolutions(figureMap, diagRelationshipScores));
            if (diagSolutions.size() == 1)
                diagSolution = diagSolutions.get(0).getName();
        }

        // If solutionList is empty, assign it one of the non-empty solution lists
        if (solutionList.isEmpty()) {
            if (!solutionListLR.isEmpty())
                solutionList = solutionListLR;
            else if (!solutionListUD.isEmpty())
                solutionList = solutionListUD;
        }

        // Put the solution names into a list of strings
        List<String> solStrings = new ArrayList<>();
        for (RavensFigure solution : solutionList)
            solStrings.add(solution.getName());

        System.out.println(solStrings);
        if (diagSolution != null)
            System.out.println(diagSolution);

        // If there is exactly one solution, return it
        // If there are more than one solution, check if a diagonal exists and use it
        // If there are less than four solutions and no diagonal, guess
        // If there are more than four solutions, skip
        if (solStrings.size() == 1)
            return Integer.parseInt(solStrings.get(0));
        else if (solStrings.size() > 1 && solStrings.size() < 4) {
            if (diagSolution != null && solStrings.contains(diagSolution))
                return Integer.parseInt(diagSolution);
            else
                return Integer.parseInt(solStrings.get(random.nextInt(solutionList.size())));
        }
        return -1;
    }

    /**
     * This method creates a key list to be used for iteration when building out Raven
     * Progressive Matrices. The regex determines whether it will be a solution key
     * list or a problem figure key list.
     *
     * @param figureMap
     * @param regex
     * @return The list of keys
     */
    public List<String> createKeyList(Map<String, RavensFigure> figureMap, String regex) {
        List<String> keyList= new ArrayList<>();
        for (String name : figureMap.keySet())
            if (name.matches(regex))
                keyList.add(name);
        Collections.sort(keyList);

        return keyList;
    }

    /**
     * This method creates a List of Lists resembling a Raven Progressive Matrix.
     *
     * @param figureMap
     * @param figureKeyList
     * @param row
     * @param col
     * @return The Raven Progressive Matrix
     */
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

    /**
     * This method generates a Raven Progressive Matrix reading from the top of a column
     * to the bottom of a column. This gives the agent a different approach when trying
     * to find solutions.
     *
     * @param ravensFiguresList
     * @return The Raven Progressive Matrix
     */
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

    /**
     * This method gets the diagonal elements (excluding the last, unknown one) which allows
     * the agent another different approach to find or reinforce a solution.
     *
     * @param ravensFiguresList
     * @return The diagonal list of RavenFigures
     */
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

    /**
     * This method determines the scores for comparing the first row(s) of a Raven Progressive
     * Matrix to the last row. The score can vary due to a solution being pushed into the last
     * row and then evaluated.
     *
     * @param probRelationshipList
     * @param solRelationshipList
     * @return The map of scores for each solution tried
     */
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

    /**
     * This method determines the diagonal scores in the same manner as above. A solution is attempted
     * and the scores are returned for each of these.
     *
     * @param diagRelationshipList
     * @param solRelationshipList
     * @return The map of scores for each attempted solution
     */
    public Map<String, Integer> determineDiagonalScore(List<Relationship> diagRelationshipList,
                                                       List<Relationship> solRelationshipList) {

        Map<String, Integer> solRelationshipScores = new HashMap<>(); //store all scores for evaluation of confidence
        List<String> diagTransformations = determineTransformations(diagRelationshipList); //same thing as getTransformations
        List<String> objDiffs = new ArrayList<>(determineNumObjGrowing(diagRelationshipList));
        List<List<String>> tempDiagObjDiffs = new ArrayList<>();
        tempDiagObjDiffs.add(objDiffs);
        for (Relationship solRelationship : solRelationshipList) {
            List<Relationship> tempSolRelationshipList = new ArrayList<>();
            tempSolRelationshipList.add(solRelationship);
            List<String> tempSolObjDiffs = new ArrayList<>(determineNumObjGrowing(tempSolRelationshipList));

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

            tempTransformations.removeAll(Arrays.asList("unchanged"));

            score += determineTransformationScores(tempDiagObjDiffs, tempSolObjDiffs);
            score -= tempTransformations.size();

            solRelationshipScores.put(solRelationship.getName(), score);
        }

        return solRelationshipScores;
    }

    /**
     * This method determines the transformations along a row in a Raven Progressive
     * Matrix. It is used for scoring.
     *
     * @param relationships
     * @return The list of transformations along a row
     */
    public List<String> determineTransformations(List<Relationship> relationships) {

        List<String> simpleTransformations = new ArrayList<>();
        for (Relationship relationship : relationships) {
            Map<String, List<String>> transformations = relationship.getTransformationMap();
            for (List<String> pairTransformations : transformations.values())
                for (String transformation : pairTransformations)
                    simpleTransformations.add(transformation);
        }

        return simpleTransformations;
    }

    /**
     * This method determines the scores of the transformations.
     *
     * @param transformationsList
     * @param tempTransformations
     * @return The score for the attempted transformation comparison
     */
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

            solTransformations.removeAll(Arrays.asList("unchanged"));

            score -= solTransformations.size();
        }

        return score;
    }

    /**
     * This method determines if the number of objects between relationships
     * are increasing or decreasing. If it alternates, it is ignored.
     *
     * @param relationshipList
     * @return The list of object differences between relationships
     */
    public List<String> determineNumObjGrowing(List<Relationship> relationshipList) {
        List<String> objDiffs = new ArrayList<>();
        for (Relationship relationship : relationshipList) {
            if (relationship.getNumObjDiff() > 0)
                objDiffs.add("growing");
            else if (relationship.getNumObjDiff() < 0)
                objDiffs.add("shrinking");
        }

        if (objDiffs.contains("growing") && objDiffs.contains("shrinking")) {
            objDiffs.remove("growing");
            objDiffs.remove("shrinking");
        }

        return objDiffs;
    }

    /**
     * This method determines which solutions hold the highest score.
     *
     * @param figureMap
     * @param solScoresMap
     * @return The list of RavensFigures holding the highest score
     */
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
