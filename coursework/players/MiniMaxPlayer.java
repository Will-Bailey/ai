package players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import snake.GameState;
import snake.Snake;
import static players.RandomPlayer.rand;

/**
 *
 * @author william
 */

public class MiniMaxPlayer extends RandomPlayer {

    //Basic constructor
    public MiniMaxPlayer(GameState state, int index, Snake game) {
        super(state, index, game);
    }

    
    /**
     * The interface function that is called by the game to perform a move.The function contains much of the same logic as the
     * recursively called getMaxUtility() with the exception that this function picks a single move rather than returning a
     * list of utilities
     */
    public void doMove() {
        long startTime= System.currentTimeMillis();
        long timeup = startTime + 90;
        
        GameState state = this.state;
        int depth = 0;
        int depthLimit = 5;
        int player = getIndex();
        
        int bestMove;
        int lastBestMove=-1;
        double bestValue;
        double myValue;

        for (; System.currentTimeMillis() < timeup; depthLimit++){
            //Reinitialise variables for this iteration of depthLimit
            depth = 0;
            bestMove = -1;
            bestValue = -Double.MAX_VALUE;
            myValue = -Double.MAX_VALUE;

            //Start searching the tree of possible outcomes
            for (int move : getLegalMoves(state, player)){
                GameState nextState = tryMove(state, player, move);
                int nextPlayer = getNextPlayer(nextState, player);
                List<Double> values = getMaxUtility(nextState, nextPlayer, depth, depthLimit, timeup);
                
                if (values.size()>0) {
                    myValue = values.get(player);
                }

                if (bestMove == -1  || myValue > bestValue) {
                    bestValue = myValue;
                    bestMove = move; 
                }
            }

            if (System.currentTimeMillis() < timeup)
                lastBestMove = bestMove;
        }

        state.setOrientation(index, lastBestMove);
    }


    /**
     * The main recursive funtion, used to predict which move a player will make based on the utilities that can be reached
     * from the outcomes.
     *
     * @param state      the current state of the game at the point where the move is bening chosen
     * @param player     the player who's turn it is. This is the player that will me maximising the utility they can achieve
     * @param depth      the current depth of the search 
     * @param depthLimit the maximum depth that the search will be allowed to reach before incrementing the iterative deepening
     * @param timeup     the value wdhich, when compared to the current clock time will tell the algorithm if it's time limit
     *                   has expired
     * 
     * @return a list of utilities (one for each player) that are resultant of player picking the move that maximises
     *         their utility.
     */
    public List<Double> getMaxUtility(GameState state, int player, int depth, int depthLimit, long timeup) {
        //Initialise variables
        double myAverageValue=0;
        double bestValue = -Double.MAX_VALUE;
        int nextPlayer = getNextPlayer(state, player);
        List<Double> bestValues = new ArrayList<Double>();

        //If we have searched up to the depth limit or the state we're exploring is game over, stop recursing and return the current state's utility values for each player.
        if(state.isGameOver() || depth == depthLimit)
            return getAllUtilities(state);

        //For each move that player could choose, find the resultant utility values for each player and pick the one that's best for player
        for (int move : getLegalMoves(state, player)){
            if (System.currentTimeMillis() > timeup) {
                return new ArrayList<Double>();
            }

            List<GameState> predictedStates = new ArrayList<GameState>();
            List<List<Double>> potentialValues = new ArrayList<List<Double>>();

            //Create an array of the states that could stem from this move. In most cases this is just the state that comes from executing the move. In the case where this move scores a point however, there will be multiple states that could come from the move. 
            GameState nextState = tryMove(state, player, move);
            if (willScore(state, player, move)) {
                predictedStates = predictStates(nextState);
            } else {
                predictedStates.add(nextState);
            }

            //For
            for (GameState predictedState : predictedStates) {
                List<Double> newValues = getMaxUtility(predictedState, nextPlayer, depth+1, depthLimit, timeup);
                if (newValues.size() > 0) {
                    potentialValues.add(newValues);
                } 
            }
            
            List<Double> averageValues = calcAverages(potentialValues, state.getNrPlayers());

            if (averageValues.size()>0) {
                myAverageValue = averageValues.get(player);

                if (myAverageValue > bestValue) {
                    bestValue = myAverageValue;
                    bestValues = averageValues; 
                }
            }
        }
        return bestValues;
    }

    /**
     * A utility function used in the chance node implementation. This function takes a list of lists where each list is the
     * utilities for each player in a potential state. The function returns a list of values each one being the average of the
     * values from the same index in each of the otehr lists.
     *
     * @param input     the list of lists that that need to be averaged
     * @param nrPlayers the number of elements in each list, this is needed to iterate over the lists properly (this should
     *                  also be the size of the outputted list)
     *
     * @return a list of doubles, the same length of each of the sub-lists in the input where each element n is the average of
     *         the nth values in each of the sub-lists in the input. 
    **/
    public List<Double> calcAverages(List<List<Double>> input, int nrPlayers) {

        if (input.size()==1) {
            return input.get(0);
        }

        System.out.println("Averageing " + input);

        //Initialise the output array with the correct size
        List<Double> output = new ArrayList<Double>();

        //remove any instances of an empty list from the input (using output as a stand-in here rather than initialising a whole new variable)
        while (input.remove(output)){}

        if (input.size()==0) {
            return new ArrayList<Double>();
        }

        for (int i=0; i<nrPlayers; i++) {
            output.add(i, 0.0);
        }

        //For each sub-list in the input, calculate it's contribution to the averages and add it on to the current value in the averages list
        for (List<Double> i : input) {
            for (int j=0; j<i.size(); j++) {
                double current = output.get(j);
                output.set(j, output.get(j)+i.get(j));
            }
        }

        for (int i=0; i<output.size(); i++) {
            output.set(i, output.get(i)/input.size());
        }

        System.out.println("Result " + output);

        return output;
    }

    //Return the resultant state of the given player making the given when in the given state
    public GameState tryMove(GameState startState, int player, int move) {
        GameState newState = new GameState(startState);
        newState.setOrientation(player, move);
        newState.updatePlayerPosition(player);
        return newState;
    }

    //Find the moves that a given playert can take from a given state
    public List<Integer> getLegalMoves(GameState state, int player) {
        List<Integer> legalMoves = new ArrayList<Integer>();
        for (int i=1; i<=4; i++) {
            if (state.isLegalMove(player, i)) {
                legalMoves.add(i);
            }
        }
        return legalMoves;
    }

    //Find which player will take the next move
    public int getNextPlayer(GameState state, int player) {
        int nextPlayer;
        nextPlayer = (player + 1) % state.getNrPlayers();
        while (state.isDead(nextPlayer)) {
            nextPlayer = (nextPlayer + 1) % state.getNrPlayers();
        }
        return nextPlayer;
    }

    public List<Double> getAllUtilities(GameState state) {
        List<Double> utilities = new ArrayList<Double>();
        for (int player=0; player<state.getNrPlayers(); player++) {
            utilities.add(player, getUtility(state, player));
        }
        return utilities;
    }

    //Return the utility of a given state fora  given player. Will rturn -1 if the player is dead and will return max value if the player is the last one alive.
    public double getUtility(GameState state, int player) {
        //Count how many players are dead
        int countDead=0;
        for (int i=1; i<state.getNrPlayers(); i++) {
            if(state.isDead(i)) {
                countDead++;
            }
        }
        
        //If player is the last one alive (and has therefore won the game)
        if ((countDead == state.getNrPlayers()-1) && (!state.isDead(player))){
            return Double.MAX_VALUE;
        }
        
        //If player is dead
        else if (state.isDead(player)){
            return -Double.MAX_VALUE;
        }

        else {
            return (state.getSize(player)*100)-(Math.abs(state.getTargetX() - state.getPlayerX(player).get(0)) + Math.abs(state.getTargetY() - state.getPlayerY(player).get(0)));
        }
    }

    //return a boolean value stating whether the player will score a point from the given state. This is needed to know when a chance move is going to be taken.
    public boolean willScore(GameState state, int player, int move) {
        GameState nextState = tryMove(state, player, move);
        
        if (state.isDead(player)||nextState.isDead(player)) {
            return false;
        }

        if ((nextState.getPlayerX(player).get(0)==state.getTargetX())&&(nextState.getPlayerY(player).get(0)==state.getTargetY())) {
            return true;
        }
        return false;
    }

    public List<GameState> predictStates(GameState state) {
        List<GameState> potentials = new ArrayList<GameState>();
        for (int i=1; i<=5; i++) {
            GameState potential = new GameState(state);
            potential.chooseNextTarget();
            potentials.add(potential);
        }
        return potentials;
    }
}