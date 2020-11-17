/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

import eightpuzzle.EightPuzzleState;
import java.util.*;

/**
 *
 * @author steven
 */
public class IterativeDeepening {

    LinkedList<Node> frontier;
    static int counter = 0;

    public IterativeDeepening(State initialState) {
        frontier = new LinkedList();
        frontier.add(new Node(initialState, null, null));
    }

    public Node findPathToGoal(int limit) {
        counter = 0;
        while (!frontier.isEmpty()) {
            counter++;
            Node n = frontier.removeLast();
            if (n.getState().isGoal()) {
                /*
                 * We have found the goal. From the representation of the node
                 * we can easily retrieve the path that has led us from the
                 * initial state to this solution
                 */
                return n;
            } else if (n.depth < limit){
               List<Action> actions = n.getState().getLegalActions();
                for (Action action : actions) {
                    State child = n.getState().doAction(action);
                    /* 
                     * Cycle checking: to avoid going in circles, we check if we 
                     * have already encountered this state on the path from the initial
                     * state to the current state
                     */
                    if (!n.alreadyExplored(child)) {
                        frontier.add(new Node(child, n, action));
                        //System.out.println("+");
                    }
                }
            }
        }
        return null;
    }

    public static void testEightPuzzle() {
//        int[][] v = {{1, 4, 7}, {2, 5, 8}, {0, 3, 6}};
//        int [][] v = {{1,0,2},{3,4,5},{6,7,8}};
//        int[][] v = {{1, 2, 5}, {3, 4, 0}, {6, 7, 8}};
        int[][] v = {{1, 5, 8}, {4, 0, 2}, {3,6, 7}};
        Boolean solved = false;
        Node goal = null;
        int limit = 0;
        while (!solved) {
            EightPuzzleState st = new EightPuzzleState(v);
            IterativeDeepening ids = new IterativeDeepening(st);
            System.out.println("Trying with limit = " + limit);
            goal = ids.findPathToGoal(limit);
            if (goal != null){
                solved = true;
            }
            System.out.println(goal);
            System.out.println(solved);
            limit++;
        }

        List<Action> actions = goal.getActions();
        System.out.println("The actions taken to find a solution:" + actions);
        System.out.println("The sequence of states that was encountered:");
        List<State> solution = goal.getPath();
        if (solution != null) {
            for (State st0 : solution) {
                st0.printState();
            }
        }
        System.out.println("The cost of this solution:" + goal.getCost());
        System.out.println("The number of nodes that have been expanded:" + counter);
    }

   
    public static void main(String[] args) {
        testEightPuzzle();
    }
}
