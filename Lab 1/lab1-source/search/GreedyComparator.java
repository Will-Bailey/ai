package search;

import java.util.*;

public class GreedyComparator implements Comparator<Node> {
	public int compare(Node x, Node y) {
		if (x.getState().getEstimatedDistanceToGoal() < y.getState().getEstimatedDistanceToGoal()) {
			return -1;
		}
		if (x.getState().getEstimatedDistanceToGoal() > y.getState().getEstimatedDistanceToGoal()) {
			return 1;
		}
		return 0;
	}	
}