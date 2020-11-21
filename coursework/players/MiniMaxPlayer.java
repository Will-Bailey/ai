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
        long timeup = startTime + 100;
        
        GameState state = this.state;
        int depth = 0;
        int depthLimit = 5;
        int player = getIndex();
        int nextPlayer = getNextPlayer(state, player);
        
        int bestMove;
        int lastBestMove=1;
        double bestValue;
        double myNewValue;

        for (; System.currentTimeMillis() < timeup; depthLimit++){
            //Reinitialise variables for this iteration of depthLimit
            depth = 0;
            bestMove = -1;
            bestValue = 0;
            myNewValue = 0;

            //Start searching the tree of possible outcomes
            for (int move : getLegalMoves(state, player)){
                GameState nextState = tryMove(player, move, state);

                //Compare the next state's utility to the current best state and replace where necessary.
                List<Double> newValues = getMaxUtility(nextState, nextPlayer, depth+1, depthLimit, timeup);

                if (newValues.size() > 0) {
                    myNewValue = newValues.get(player);
                } else {
                    myNewValue = Integer.MIN_VALUE;
                }

                if (bestMove == -1 || myNewValue > bestValue) {
                    bestValue = myNewValue;
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
     * The value returned is identical to what would be returned by:
     *
     *
     * @param  state the current state of the game at the point where the move is bening chosen
     * @param  currentPlayer the player who's turn it is. This is the player that will me maximising the utility they can achieve
     * @param  depth the current depth of the search 
     * @param  depthLimit the maximum depth that the search will be allowed to reach before incrementing the iterative deepening
     * @param  timeup the value wdhich, when compared to the current clock time will tell the algorithm if it's time limit has expired
     * 
     * @return a list of utilities (one for each player) that are resultant of currentPlayer picking the move that maximises thier utility.
     */
    public List<Double> getMaxUtility(GameState state, int currentPlayer, int depth, int depthLimit, long timeup) {
        //Initialise variables
        double myAverageValue=0;
        double bestValue = -Double.MAX_VALUE;
        List<Double> bestValues = new ArrayList<Double>();
        int nextPlayer = getNextPlayer(state, currentPlayer);
        List<GameState> potentialStates = new ArrayList<GameState>();
        
        boolean willScore = false;
        for (int i=0; i<state.getNrPlayers(); i++) {
            if (willScore(state, i)) {
                willScore = true;
            }
        }

        //If we have searched up to the depth limit or the state we're exploring is game over, stop recursing and return the current state's utility values for each player.
        if(state.isGameOver() || depth == depthLimit)
            return getAllUtilities(state);

        //Fore
        for (int move : getLegalMoves(state, currentPlayer)){
            List<List<Double>> potentialValues = new ArrayList<List<Double>>();
                        
            if (System.currentTimeMillis() > timeup) {
                return new ArrayList<Double>();
            }

            //Compare the next state's utility to the current best state and replace where necessary.
            if (willScore) {
                potentialStates = getPotentialStates(state);
            } else {
                GameState nextState = tryMove(currentPlayer, move, state);
                potentialStates.add(nextState);
            }

            //For
            for (GameState potentialState : potentialStates) {
                List<Double> newValues = getMaxUtility(potentialState, nextPlayer, depth+1, depthLimit, timeup);
                if (newValues.size() > 0) {
                    potentialValues.add(newValues);
                } 
            }
            
            List<Double> averageValues = calcAverages(potentialValues, state.getNrPlayers());

            myAverageValue = averageValues.get(currentPlayer);

            if (myAverageValue > bestValue) {
                bestValue = myAverageValue;
                bestValues = averageValues; 
            }
        }
        return bestValues;
    }

    //Return and array list of all the states that can be reached from the current state by the player
    public List<GameState> getNextStates(GameState state, int player) {
        List<GameState> nextStates = new ArrayList<GameState>();
        for (int i=1; i<=4; i++) {
            if(state.isLegalMove(player, i)){
                nextStates.add(tryMove(player, i, state));
            }
        }
    return nextStates;
    }

    public List<Double> calcAverages(List<List<Double>> input, int nrPlayers) {
        //Initialise the output array with the correct size and filled with 0s
        List<Double> output = new ArrayList<Double>();
        for (int i=0; i<nrPlayers; i++) {
            output.add(i, 0.0);
        }

        //For each set of values in the input, add them to the 
        for (List<Double> i : input) {
            for (int j=0; j<i.size(); j++) {
                output.set(j, (output.get(j)+i.get(j))/input.size());
            }
        }
        return output;
    }

    //Return the resultant state of the given player making the given when in teh given state
    public GameState tryMove(int player, int move, GameState startState) {
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
    public int getNextPlayer(GameState state, int currentPlayer) {
        int nextPlayer;
        nextPlayer = (currentPlayer + 1) % state.getNrPlayers();
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
        
        //If player is dead
        } else if (state.isDead(player)){
            return -Double.MAX_VALUE;

        //If the player will score        
        } else if (willScore(state, player)){
            return state.getHeight() + state.getWidth() + 1; 

        } else {
            int utility=0;    
            utility = -(Math.abs(state.getTargetX() - state.getPlayerX(player).get(0)) + Math.abs(state.getTargetY() - state.getPlayerY(player).get(0)));
            state.updatePlayerPosition(player);

            return utility;
        }
    }

    //return a boolean value stating whether the player will score a point from the given state. This is needed to know when a chance move is going to be taken.
    public boolean willScore(GameState state, int player) {
        if (state.isDead(player)) {
            return false;
        }

        int size = state.getSize(player);
        for (GameState nextState : getNextStates(state, player)){
            if(nextState.getSize(player) > size) {
                return true;
            }
        }
        return false;
    }

    public List<GameState> getPotentialStates(GameState state) {
        List<GameState> potentialTargetStates = new ArrayList<GameState>();
        for (int i=1; i<=5; i++) {
            GameState potentialTargetState = new GameState(state);
            potentialTargetState.chooseNextTarget();
            potentialTargetStates.add(potentialTargetState);
        }
        return potentialTargetStates;
    }
}