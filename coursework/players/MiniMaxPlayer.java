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

    public void doMove() {
        state.setOrientation(index, chooseMove(this.game));
    }

    public int chooseMove(Snake game) {
        long startTime= System.currentTimeMillis();
        long timeup = startTime + 100;
        
        GameState state = this.state;
        int depth = 0;
        int depthLimit = 5;
        int player = getIndex();
        
        int bestMove = -1;
        int lastBest = -1;
        int bestValue = 0;
        int lastMove;

        for (; System.currentTimeMillis() < timeup; depthLimit++){
            //Reinitialise variables for this iteration of depthLimit
            depth = 0;
            bestMove = -1;
            bestValue = 0;

            //Start searching the tree of possible outcomes
            for (GameState child : getChildren(state, player)) {
                int value = claimSpaceUtility(getNextState(child, getNextPlayer(state, player), depth, depthLimit, timeup), player);
                
                if(System.currentTimeMillis() > timeup){
                    break;
                }

                if (bestMove == -1 || value > bestValue){
                    bestValue = value;
                    bestMove = child.getLastOrientation(player);
                }
            }
            if (System.currentTimeMillis() < timeup)
            lastBest = bestMove;
        }
        lastMove = lastBest;
        return lastMove;
    }

    //Predict the moves that players will make and return the state that gives the current player the highest utility. Assumes all other players are playing using the same utility as the MiniMax Player
    public GameState getNextState(GameState state, int currentPlayer, int depth, int depthLimit, long timeup) {
        //If we have searched up to the depth limit or the state we're exploring is game over, stop recurtsing and return the current state.
        if(state.isGameOver() || depth == depthLimit) {
            return state;
        } else {
            //Initialise variables
            int nextPlayer;
            int value;
            int bestValue=0;
            GameState bestChild = state;
            GameState newChild = state;
            
            //for each possible state that can be reached from the current state in one move.
            for (GameState child : getChildren(state, currentPlayer)){
                if (System.currentTimeMillis() > timeup) {
                    return state;
                }

                nextPlayer = getNextPlayer(state, currentPlayer);
                
                //Compate the child to the current best state and replace where necessary.
                newChild = getNextState(child, nextPlayer, depth+1, depthLimit, timeup);
                value = claimSpaceUtility(newChild, currentPlayer);

                if (value > bestValue) {
                    bestValue = value;
                    bestChild = child;
                }
            }
            return bestChild;
        }
    }

    //Return and array list of all the states that can be reached from the current state by the player
    public List<GameState> getChildren(GameState state, int player) {
        List<GameState> children = new ArrayList<GameState>();
        for (int i=1; i <= 4; i++) {
            if(state.isLegalMove(player, i)){
                children.add(tryMove(player, i, state));
            }
        }
    return children;
    }

    //Return the resultant state of the given player making the given when in teh given state
    public GameState tryMove(int player, int move, GameState startState) {
        GameState newState = new GameState(startState);
        newState.setOrientation(player, move);
        newState.updatePlayerPosition(player);
        return newState;
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

    //Return the utility of a given state fora  given player. Will rturn -1 if the player is dead and will return max value if the player is the last one alive.
    public int getUtility(GameState state, int player) {
        //Count how many players are dead
        int countDead=0;
        for (int i=1; i<state.getNrPlayers(); i++) {
            if(state.isDead(i)) {
                countDead++;
            }
        }

        //If player is the last one alive (and has therefore won the game)
        if ((countDead == state.getNrPlayers()-1) && (!state.isDead(player))){
            return Integer.MAX_VALUE;
        
        //If player is dead
        } else if (state.isDead(player)){
            return Integer.MIN_VALUE;

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
        int size = state.getSize(player);
        for (GameState child : getChildren(state, player)){
            if(child.getSize(player) > size) {
                return true;
            }
        }
        return false;
    }

    public List<GameState> predictTargetStates(GameState state) {
        List<GameState> potentialTargetStates = new ArrayList<GameState>();
        for (int i=1; i<=5; i++) {
            GameState potentialTargetState = new GameState(state);
            potentialTargetState.chooseNextTarget();
            potentialTargetStates.add(potentialTargetState);
        }
        return potentialTargetStates;
    }

    public int claimSpaceUtility(GameState state, int player) {
        //Count how many players are dead
        int countDead=0;
        for (int i=1; i<state.getNrPlayers(); i++) {
            if(state.isDead(i)) {
                countDead++;
            }
        }

        //If player is the last one alive (and has therefore won the game)
        if ((countDead == state.getNrPlayers()-1) && (!state.isDead(player))){
            return Integer.MAX_VALUE;
        
        //If player is dead
        } else if (state.isDead(player)){
            return Integer.MIN_VALUE;

        } else {
            int utility=0;
            for (int opponent=0; opponent<state.getNrPlayers(); opponent++) {
                if (!(opponent==player)) {
                    utility += Math.abs(state.getPlayerX(opponent).get(0) - state.getPlayerX(player).get(0));
                    utility += Math.abs(state.getPlayerY(opponent).get(0) - state.getPlayerY(player).get(0));
                }   
            }
            return utility;
        }
    }
}