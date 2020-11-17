package search;

import java.util.*;

public class PathLengthComparator implements Comparator<Node> {
	public int compare(Node x, Node y) {
		if (x.getCost() < y.getCost()) {
			return -1;
		}
		if (x.getCost() > y.getCost()) {
			return 1;
		}
		return 0;
	}	
}